package server;

import com.server.grpc.ServerService;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.ServerService.subLocRepReply;
import com.server.grpc.ServerService.subLocRepReq;
import com.server.grpc.ServerService.usersLocationRep;
import com.server.grpc.ServerService.usersLocationReq;
import com.server.grpc.ServerService.obtLocRepReq;
import com.server.grpc.ServerService.obtUseLocHARep;
import com.server.grpc.ServerService.obtUseLocHAReq;
import com.server.grpc.ServerService.obtLocRepReply;


import com.server.grpc.serverServiceGrpc.serverServiceImplBase;

import crypto.AESProvider;
import crypto.RSAProvider;
import io.grpc.stub.StreamObserver;
import shared.DiffieHelman;
import shared.Point2D;
import shared.TrackerLocationSystem;

import java.math.BigInteger;
import java.security.Key;
import java.security.PublicKey;
import java.util.Base64;
import java.util.regex.Pattern;



public class ServerImp extends serverServiceImplBase {

    private static final String PRIVATE_KEY_PATH = "resources/private_keys/server_private.key";
    private DealWithRequest dealWithReq;
    
    public ServerImp(DealWithRequest dwr) {
		dealWithReq = dwr;
	}
    

	/**************************************************************************************
     *                                  - submitLocationReport()
     *  RPC: server received location report from a user; handle it and send reply
     *  - input:
     *      - request: the submitLocationReportRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
    @Override
    public void submitLocationReport(subLocRepReq request, StreamObserver<subLocRepReply> responseObserver) {
    	try {
	        Key sharedKey = dealWithReq.getSharedKey(request.getUserID());  
	        PublicKey userPubkey = TrackerLocationSystem.getUserPublicKey(request.getUserID());
	        
	        String secureReport = request.getSecureReport();
	        String reportDigSig = request.getReportDigitalSignature();
	        
	        String reportPlaintext = AESProvider.getPlainTextOfCipherText(secureReport, sharedKey);
	        boolean reportIsAuth = RSAProvider.istextAuthentic(reportPlaintext, reportDigSig, userPubkey);
	        if(reportIsAuth) {
	        	int nonce = request.getNonce();
		        ServerService.subLocRepReply.Builder response =dealWithReq.submitReportHandler(request.getUserID(), reportPlaintext, nonce);
		        responseObserver.onNext(response.build());
		        responseObserver.onCompleted();
	        }else {
				throw new Exception("request is not authentic");
			}
	        
    	}catch (Exception e) {
    		subLocRepReply.Builder response = subLocRepReply.newBuilder();
    		response.setReplycode(replyCode.NOK.ordinal());
    		response.setReplymessage(e.getMessage());
    		responseObserver.onNext(response.build());
		    responseObserver.onCompleted();
		}

    }

    /**************************************************************************************
     *                                  - obtainLocationReport()
     *  RPC: server received request from a user to provide user location in specific epoch.
     *  return only his own location history.
     *  This method handles the requests and sends a reply
     *  - input:
     *      - request: the obtainLocationReportRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
    @Override
    public void obtainLocationReport(obtLocRepReq request, StreamObserver<obtLocRepReply> responseObserver) {
    	try {
    		Key sharedKey = dealWithReq.getSharedKey(request.getUserID());
    		PublicKey pubkey = TrackerLocationSystem.getUserPublicKey(request.getUserID());
        	String cipherText = request.getSecureRequest();
			String requestPlainText = AESProvider.getPlainTextOfCipherText(cipherText, sharedKey);
			String digSig = request.getReqDigSig();
			boolean DigSigIsValid = RSAProvider.istextAuthentic(requestPlainText, digSig, pubkey);
			
			if(DigSigIsValid) {
				
		        String[] reqSplit = requestPlainText.split(" ");
		        int epoch = Integer.parseInt(reqSplit[1]);
		        int nonce = Integer.parseInt(reqSplit[2]);
		        ServerService.obtLocRepReply.Builder response =dealWithReq.obtainReportHandler(request.getUserID(), epoch, nonce);
		        responseObserver.onNext(response.build());
		        responseObserver.onCompleted();
			}else {
				throw new Exception("Invalid digital signature");
			}
			
		} catch (Exception e) {			
			 obtLocRepReply.Builder response = obtLocRepReply.newBuilder().setOnError(true)
					 .setErrormessage(e.getMessage());
			 responseObserver.onNext(response.build());
		     responseObserver.onCompleted();
		}
    }

    /**************************************************************************************
     *                                  - obtainLocationReportHA()
     *  RPC: server received request from the HA to provide user location in specific epoch
     *  History of any user can be returned.
     *  This method handles the requests and sends a reply
     *  - input:
     *      - request: the obtainLocationReportRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
   @Override
   public void obtainLocationReportHA(obtUseLocHAReq request, StreamObserver<obtUseLocHARep> responseObserver) {
	   try {
		   Key sharedKey = dealWithReq.getSharedKey(0);
		   PublicKey pubkey = TrackerLocationSystem.getUserPublicKey(0);
		   String secureMessage = request.getSecureRequest();
		   String messDigSig = request.getDigitalSignature();
		   String messPlainText = AESProvider.getPlainTextOfCipherText(secureMessage, sharedKey);
		   boolean DigSigIsValid = RSAProvider.istextAuthentic(messPlainText, messDigSig, pubkey);
		   if(DigSigIsValid) {
			   String[] requestValues = messPlainText.split(Pattern.quote("||"));
			   int nonce = Integer.parseInt(requestValues[2]);
			   int userID = Integer.parseInt(requestValues[0]);
			   int epoch = Integer.parseInt(requestValues[1]);
			   obtUseLocHARep.Builder response = dealWithReq.obtainLocationReportHAHandler(userID, epoch, nonce);
			   responseObserver.onNext(response.build());
			   responseObserver.onCompleted();
		   }else {
			   throw new Exception("the message is not authentic, please use your keys to cipher the messages");
		   }
	   }catch (Exception e) {
		   obtUseLocHARep.Builder response = obtUseLocHARep.newBuilder().setOnError(true).
				   setErrormessage(e.toString()+""+e.getMessage());
		   responseObserver.onNext(response.build());
		   responseObserver.onCompleted();
	   }
   }

    /**************************************************************************************
     *                                  - obtainUsersAtLocation()
     *  RPC: server received request from HA to provide a list of users that were at specific
     *  time on specific location.
     *  - input:
     *      - request: the obtainUsersLocationRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
   @Override
   public void obtainUsersAtLocation(usersLocationReq request, StreamObserver<usersLocationRep> responseObserver) {
	   usersLocationRep.Builder response;
	   try {
		   Key sharedKey = dealWithReq.getSharedKey(0);
		   PublicKey pubkey = TrackerLocationSystem.getUserPublicKey(0);
		   String secureMessage = request.getSecureRequest();
		   String messDigSig = request.getDigitalSignature();
		   String messPlainText = AESProvider.getPlainTextOfCipherText(secureMessage, sharedKey);
		   boolean DigSigIsValid = RSAProvider.istextAuthentic(messPlainText, messDigSig, pubkey);
		   if(DigSigIsValid) {
			   String[] requestValues = messPlainText.split(Pattern.quote("||"));
			   int requestEpoch = Integer.parseInt(requestValues[1]);
			   String position = requestValues[0].substring(1, 4);
			   int x = Integer.parseInt(position.substring(0,1));
			   int y = Integer.parseInt(position.substring(2,3));
			   Point2D requestPoint = new Point2D(x, y);
			   int nonce =Integer.parseInt(requestValues[2]);
			   response = dealWithReq.obtainUsersAtLocationHandler(requestPoint, requestEpoch, nonce);
			   responseObserver.onNext(response.build());
			   responseObserver.onCompleted();
			   }
	   }catch (Exception e) {
		response = usersLocationRep.newBuilder().setOnError(true)
												.setErrorMessage(e.getMessage());
		responseObserver.onNext(response.build());
		   responseObserver.onCompleted();
	}
   }


   
    

    /**************************************************************************************
     * 											- dHKeyExchange()
     * -remote procedural call to exchange Diffie Helmann key between user and server 
     *  - input:
     *      - request: request of the user (contain public key(p^a mod g)
     *       	signed with user private key and big integers p and g)
     *      
     *
     * ************************************************************************************/
    @Override
    public void dHKeyExchange(DHKeyExcReq request, StreamObserver<DHKeyExcRep> responseObserver) {
    	try {   		
    		BigInteger p = DiffieHelman.read(request.getP());
    		BigInteger g = DiffieHelman.read(request.getG());
    		DiffieHelman df = new DiffieHelman(p, g);
			PublicKey key = TrackerLocationSystem.getUserPublicKey(request.getUserID());
			String userPbkDigSig = request.getDigSigPubKey();
			String userPubKey = request.getMyDHPubKey();
		 	Key secretKey = TrackerLocationSystem.createSecretKey(df, userPbkDigSig, userPubKey, key);
		 	dealWithReq.putOrUpdateSharedKeys(request.getUserID(), secretKey);
		 	PublicKey myPubkey = df.getPublicKey();
		 	String pbkB64 = Base64.getEncoder().encodeToString(myPubkey.getEncoded());
		 	String digSigMyDHpubkey = TrackerLocationSystem.getDHkeySigned(myPubkey, PRIVATE_KEY_PATH);
		 	
		 	DHKeyExcRep.Builder rep = DHKeyExcRep.newBuilder().setOnError(false).setDigSigPubkey(digSigMyDHpubkey)
		 			.setMyPubKey(pbkB64);
		 	
		 	responseObserver.onNext(rep.build());
		 	responseObserver.onCompleted();
		 	
		} catch (Exception e) {
			DHKeyExcRep.Builder rep = DHKeyExcRep.newBuilder().setOnError(true)
					.setErrorMessage(e.getMessage());
			responseObserver.onNext(rep.build());
		 	responseObserver.onCompleted();
		}
		
		
    }
    
    private enum replyCode {OK, WRONG_EPOCH, NOK}
    
}
