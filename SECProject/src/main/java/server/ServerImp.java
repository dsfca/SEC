package server;

import com.server.grpc.ServerService;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.ServerService.Position;
import com.server.grpc.ServerService.subLocRepReply;
import com.server.grpc.ServerService.subLocRepReq;
import com.server.grpc.ServerService.usersLocationReq;
import com.server.grpc.ServerService.obtLocRepReq;
import com.server.grpc.ServerService.obtLocRepReply;


import com.server.grpc.serverServiceGrpc.serverServiceImplBase;

import crypto.AESProvider;
import crypto.RSAProvider;
import io.grpc.stub.StreamObserver;
import shared.DiffieHelman;
import shared.Point2D;
import shared.TrackerLocationSystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerImp extends serverServiceImplBase {

    private static final String PRIVATE_KEY_PATH = "resources/private_keys/server_private.key";
    private Map<Integer, Key> sharedKeys = new HashMap<>();
    private Map<Integer, List<Integer>> usersNonce = new HashMap<>();
    private InteractWithDB DB;
    
    public ServerImp()  {
    	try {
			DB = new InteractWithDB("variables.ini");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	        Key sharedKey = sharedKeys.get(request.getUserID());  
	        PublicKey userPubkey = TrackerLocationSystem.getUserPublicKey(request.getUserID());
	        
	        String secureReport = request.getSecureReport();
	        String reportDigSig = request.getReportDigitalSignature();
	        
	        String reportPlaintext = AESProvider.getPlainTextOfCipherText(secureReport, sharedKey);
	        boolean reportIsAuth = RSAProvider.istextAuthentic(reportPlaintext, reportDigSig, userPubkey);
	        if(reportIsAuth) {
	        	int nonce = request.getNonce();
		        ServerService.subLocRepReply.Builder response = submitReportHandler(request.getUserID(), reportPlaintext, nonce);
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
			e.printStackTrace();
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
    		Key sharedKey = sharedKeys.get(request.getUserID());
    		PublicKey pubkey = TrackerLocationSystem.getUserPublicKey(request.getUserID());
        	String cipherText = request.getSecureRequest();
			String requestPlainText = AESProvider.getPlainTextOfCipherText(cipherText, sharedKey);
			String digSig = request.getReqDigSig();
			boolean DigSigIsValid = RSAProvider.istextAuthentic(requestPlainText, digSig, pubkey);
			
			if(DigSigIsValid) {
				
		        String[] reqSplit = requestPlainText.split(" ");
		        int epoch = Integer.parseInt(reqSplit[1]);
		        int nonce = Integer.parseInt(reqSplit[2]);
		        ServerService.obtLocRepReply.Builder response = obtainReportHandler(request.getUserID(), epoch, nonce);
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
 /*   @Override
    public void obtainLocationReportHA(obtLocRepReq request, StreamObserver<obtLocRepReply> responseObserver) {
    		

    }*/

    /**************************************************************************************
     *                                  - obtainUsersAtLocation()
     *  RPC: server received request from HA to provide a list of users that were at specific
     *  time on specific location.
     *  - input:
     *      - request: the obtainUsersLocationRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
  /*  @Override
    public void obtainUsersAtLocation(usersLocationReq request, StreamObserver<obtUseLocRep> responseObserver) {
        

//        ServerService.obtUseLocRep.Builder response = obtainReportsHandler(request);

  //      responseObserver.onNext(response.build());
    //    responseObserver.onCompleted();
    }*/


    /** Possible reply codes to add into submitReportReply */
    private enum replyCode {OK, WRONG_EPOCH, NOK}
    /**************************************************************************************
     * 											- reportHandler()
     *  Handles submitLocationReportRequest received in submitLocationReport() method.
     *  It should validate the request and define appropriate content of subLocRepReply
     *  - input:
     *      - request: The submit request received from a user
     * @throws Exception 
     *
     * ************************************************************************************/
    private subLocRepReply.Builder submitReportHandler(int id, String report, int nonce) throws Exception {
    	List<Integer> userNonces = usersNonce.get(id);
    	subLocRepReply.Builder response = subLocRepReply.newBuilder();
    	if(userNonces == null)
    		userNonces = new ArrayList<>();
    	if(!userNonces.contains(nonce)) {
    		userNonces.add(nonce);
	    	report = report.replace("[", "").replace("]", "");
	    	String[] reportList = report.split(",");
	    	List<ProofReport> proofReports = getProofReports(reportList); 
	    	Point2D proverPos = null;
	    	int epoch = 0;
	        
	    	if(!proofReports.isEmpty()) {
		    	for(ProofReport pr : proofReports) {
		    		PublicKey witPubKey = TrackerLocationSystem.getUserPublicKey(pr.getWitnessID());
		    		if(pr.proofDigSigIsValid(witPubKey))
		    		DB.addReportToDatabase(pr.getProverID(), pr.getWitnessID(), pr.getProverPoint(),
		    				pr.getWitnessPoint(), pr.getEpoch(), pr.isWitnessIsNearProof(), pr.getWitnessDigSig());
		    		
		    		proverPos = pr.getProverPoint();
		    		epoch = pr.getEpoch();
		    	}
		    	DB.addLocationToValidated(id, proverPos, epoch);
		    	 response.setReplycode(replyCode.OK.ordinal());
			     response.setReplymessage("Your report was submitted successfully");
	    	}else {
	    		throw new Exception("at least a user must be near you to validate you position");
			}
	       
	
	        
    	}else {
    		throw new Exception("Nonce must be different for each request");
    	}
    	return response;
    }

    private List<ProofReport> getProofReports(String[] reportList) {
    	List<ProofReport> reports = new ArrayList<>();
    	for(int i=0; i < reportList.length; i++) {
    		ProofReport proof = new ProofReport(reportList[i]);
    		reports.add(proof);
    	}
		return reports;
	}



    /**************************************************************************************
     * 											- reportHandler()
     *  Handles submitLocationReportRequest received in submitLocationReport() method.
     *  It should validate the request and define appropriate content of subLocRepReply
     *  - input:
     *      - request: The submit request received from a user
     * @throws Exception 
     *
     * ************************************************************************************/
    private obtLocRepReply.Builder obtainReportHandler(int userId, int epoch, int nonce) throws Exception {
    	obtLocRepReply.Builder response = obtLocRepReply.newBuilder();
    	List<Integer> userNonces = usersNonce.get(userId);
    		if(userNonces == null)
    			userNonces = new ArrayList<>();
    	if(!userNonces.contains(nonce)) {
    		userNonces.add(nonce);
    		Point2D userPoint = DB.getLocationGivenEpoch(userId, epoch);
    		Position.Builder position = Position.newBuilder().setX(userPoint.getX())
    				.setY(userPoint.getY());
    		response.setOnError(false);
    		response.setPos(position).setUserID(userId);
    		
    	}else {
			throw new Exception("Invalid nonce");
		}
    	return response;
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
		 	putOrUpdateSharedKeys(request.getUserID(), secretKey);
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
    
    
    /**************************************************************************************
     * 											- putOrUpdateSharedKeys()
     * -
     *  - input:update the shared key between user id and server if both already
     *  	 have a shared key otherwise it(server) adds them.
     *      - id: 
     *		
     *		-key:
     * ************************************************************************************/
    public void putOrUpdateSharedKeys(int id , Key key) {
    	if(sharedKeys.get(id) != null)
    		sharedKeys.replace(id, key);
    	else
    		sharedKeys.put(id, key);
    }
    
}
