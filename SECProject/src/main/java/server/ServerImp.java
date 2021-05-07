package server;

import com.server.grpc.ServerService;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.ServerService.secureReplay;
import com.server.grpc.ServerService.secureRequest;
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

    private  String PRIVATE_KEY_PATH;
    private DealWithRequest dealWithReq;
    
    public ServerImp(int id, DealWithRequest dwr) {
    	PRIVATE_KEY_PATH = "resources/private_keys/server" + id +"_private.key";
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
    public void submitLocationReport(secureRequest request, StreamObserver<secureReplay> responseObserver) {
    	try {
    		String[] reqFields = getfieldsFromSecureMessage(request.getUserID(), request.getConfidentMessage(), request.getMessageDigitalSignature());	
	       	int nonce =Integer.parseInt(reqFields[2]);
	       	String report = reqFields[0];
	       	ServerService.secureReplay.Builder response =dealWithReq.submitReportHandler(request.getUserID(), report, nonce);
		    responseObserver.onNext(response.build());
		    responseObserver.onCompleted();
	        
    	}catch (Exception e) {
    		secureReplay.Builder response = secureReplay.newBuilder();
    		response.setOnError(true);
    		response.setErrormessage(e.getMessage());
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
    public void obtainLocationReport(secureRequest request, StreamObserver<secureReplay> responseObserver) {
    	try {
    		
		    String[] reqSplit = getfieldsFromSecureMessage(request.getUserID(), request.getConfidentMessage(), request.getMessageDigitalSignature());
		    int epoch = Integer.parseInt(reqSplit[1]);
		    int nonce = Integer.parseInt(reqSplit[2]);
		    ServerService.secureReplay.Builder response =dealWithReq.obtainReportHandler(request.getUserID(), epoch, nonce);
		    responseObserver.onNext(response.build());
		    responseObserver.onCompleted();
			
		} catch (Exception e) {
			secureReplay.Builder response = secureReplay.newBuilder().setOnError(true)
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
   public void obtainLocationReportHA(secureRequest request, StreamObserver<secureReplay> responseObserver) {
	   try {
		   if(request.getUserID() != 0)
			   throw new Exception("only HA user can execute this functionality");
		   String[] requestValues = getfieldsFromSecureMessage(0, request.getConfidentMessage(), request.getMessageDigitalSignature());
		   int nonce = Integer.parseInt(requestValues[2]);
		   int userID = Integer.parseInt(requestValues[0]);
		   int epoch = Integer.parseInt(requestValues[1]);
		   secureReplay.Builder response = dealWithReq.obtainLocationReportHAHandler(0,userID, epoch, nonce);
		   responseObserver.onNext(response.build());
		   responseObserver.onCompleted();
	   }catch (Exception e) {
		   secureReplay.Builder response = secureReplay.newBuilder().setOnError(true).
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
   public void obtainUsersAtLocation(secureRequest request, StreamObserver<secureReplay> responseObserver) {
	   secureReplay.Builder response;
	   try {
		   if(request.getUserID() != 0)
			   throw new Exception("only HA user can execute this functionality");
		   String[] requestValues = getfieldsFromSecureMessage(0, request.getConfidentMessage(), request.getMessageDigitalSignature());
		   int requestEpoch = Integer.parseInt(requestValues[1]);
		   String position = requestValues[0].substring(1, 4);
		   int x = Integer.parseInt(position.substring(0,1));
		   int y = Integer.parseInt(position.substring(2,3));
		   Point2D requestPoint = new Point2D(x, y);
		   int nonce =Integer.parseInt(requestValues[2]);
		   response = dealWithReq.obtainUsersAtLocationHandler(0,requestPoint, requestEpoch, nonce);
		   responseObserver.onNext(response.build());
		   responseObserver.onCompleted();
			
	   }catch (Exception e) {
		   e.printStackTrace();
		response = secureReplay.newBuilder().setOnError(true)
												.setErrormessage(e.getMessage());
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
    
    public String[] getfieldsFromSecureMessage(int userID, String secureMessage, String digsig) throws Exception {
    	Key sharedKey = dealWithReq.getSharedKey(userID);
    	PublicKey pubkey = TrackerLocationSystem.getUserPublicKey(userID);
		 String messPlainText = AESProvider.getPlainTextOfCipherText(secureMessage, sharedKey);
		 boolean DigSigIsValid = RSAProvider.istextAuthentic(messPlainText, digsig, pubkey);
		 if(DigSigIsValid) {
			 String[] requestValues = messPlainText.split(Pattern.quote("||"));
			 return requestValues;
		 }else {
			 throw new Exception("the message is not authentic");
		 }
	}
    
}
