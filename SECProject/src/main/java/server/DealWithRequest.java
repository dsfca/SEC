package server;

import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.google.gson.JsonObject;
import com.server.grpc.ServerService.secureReplay;


import crypto.RSAProvider;
import shared.Point2D;
import shared.TrackerLocationSystem;

public class DealWithRequest {

	private String PRIVATE_KEY_PATH;
	private Map<Integer, Key> sharedKeys = new HashMap<>();
    private Map<Integer, List<Integer>> usersNonce = new HashMap<>();
    private InteractWithDB DB;
    private int ID;
    public DealWithRequest(int id) {
    	try {
    		this.ID = id;
    		PRIVATE_KEY_PATH = "resources/private_keys/server" + id +"_private.key";
			DB = new InteractWithDB("variables.ini");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
    public secureReplay.Builder submitReportHandler(int id, String report, int nonce) throws Exception {
    	List<Integer> userNonces = usersNonce.get(id);
    	secureReplay.Builder response = secureReplay.newBuilder();
    	if(userNonces == null)
    		userNonces = new ArrayList<>();
    	if(!userNonces.contains(nonce)) {
    		userNonces.add(nonce);
	    	report = report.replace("[", "").replace("]", "");
	    	if( report.length() > 0 ) {
		    	String[] reportList = report.split(",");
		    	List<ProofReport> proofReports = getProofReports(reportList); 
		    	Point2D proverPos = null;
		    	int epoch = 0;
		        
		    	if( proofReports.size() > TrackerLocationSystem.NUM_BIZANTINE_USERS) {
			    	for(ProofReport pr : proofReports) {
			    		PublicKey witPubKey = TrackerLocationSystem.getUserPublicKey(pr.getWitnessID());
			    		if(pr.proofDigSigIsValid(witPubKey))
			    			DB.addReportToDatabase(pr.getProverID(), pr.getWitnessID(), pr.getProverPoint(),
			    				pr.getWitnessPoint(), pr.getEpoch(), pr.isWitnessIsNearProof(), pr.getWitnessDigSig());
			    		
			    		proverPos = pr.getProverPoint();
			    		epoch = pr.getEpoch();
			    	}
			    	DB.addLocationToValidated(id, proverPos, epoch);
			    	String message = "Your report was submitted successfully||" + (nonce - 1);
			    	JsonObject secureMessage = getsecureMessage(message, id);
			 		String confidentMessage = secureMessage.get("ciphertext").getAsString();
			 		String messDigSig = secureMessage.get("textDigitalSignature").getAsString();
			    	 response.setOnError(false);
				     response.setServerID(ID).setConfidentMessage(confidentMessage).setMessageDigitalSignature(messDigSig);
		    	}else {
		    		throw new Exception("proof size must be bigger than num of byzantine users");
				}
	    	}else {
	    		throw new Exception("You cannot proove your location with 0 users near you!");
			}
	        
    	}else {
    		throw new Exception("Nonce must be different for each request");
    	}
    	return response;
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
    public secureReplay.Builder obtainReportHandler(int userId, int epoch, int nonce) throws Exception {
    	secureReplay.Builder response = secureReplay.newBuilder();
    	List<Integer> userNonces = usersNonce.get(userId);
    		if(userNonces == null)
    			userNonces = new ArrayList<>();
    	if(!userNonces.contains(nonce)) {
    		userNonces.add(nonce);
    		Point2D userPoint = DB.getLocationGivenEpoch(userId, epoch);
    		if(userPoint != null) {
	    		String message = userId + "||" +userPoint.toString() + "||" +(nonce - 1);
	    		JsonObject secureMessage = getsecureMessage(message, userId);
		 		String confidentMessage = secureMessage.get("ciphertext").getAsString();
		 		String messDigSig = secureMessage.get("textDigitalSignature").getAsString();
	    		response.setOnError(false);
	    		 response.setServerID(ID).setConfidentMessage(confidentMessage).setMessageDigitalSignature(messDigSig);
    		}else {
	    		throw new Exception("first submit your location proof at epoc "+ epoch);
			}
    		
    	}else {
			throw new Exception("Invalid nonce");
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
    
    
    public secureReplay.Builder obtainLocationReportHAHandler(int requestUserID, int userID, int epoch, int nonce) throws Exception{
    	secureReplay.Builder response = secureReplay.newBuilder();
    	List<Integer> userNonces = usersNonce.get(0);
		if(userNonces == null)
			userNonces = new ArrayList<>();
		if(!userNonces.contains(nonce)) {
		    	Point2D userPoint = DB.getLocationGivenEpoch(userID, epoch);
		    	if(userPoint != null) {
			    	String message = userID + "||" +userPoint.toString() + "||" +(nonce - 1);
		    		JsonObject secureMessage = getsecureMessage(message, requestUserID);
			 		String confidentMessage = secureMessage.get("ciphertext").getAsString();
			 		String messDigSig = secureMessage.get("textDigitalSignature").getAsString();
			    	response.setOnError(false);
			    	response.setServerID(this.ID).setConfidentMessage(confidentMessage).setMessageDigitalSignature(messDigSig);
					
		    	}else {
					response.setOnError(true);
					response.setErrormessage("no user position submited at this epoch, try later");
				}
		}else {
			response.setOnError(true);
			response.setErrormessage("user nonce already exists");
		}
	    return response;
	}
    
    public secureReplay.Builder obtainUsersAtLocationHandler(int userID, Point2D requestPoint, int requestEpoch, int nonce) throws Exception {
    	secureReplay.Builder response = secureReplay.newBuilder();
    	List<Integer> userNonces = usersNonce.get(0);
		if(userNonces == null)
			userNonces = new ArrayList<>();
		if(!userNonces.contains(nonce)) {
    
	    	ArrayList<String> usersAtPos = DB.getUsersGivenPosAndEpoch(requestPoint, requestEpoch);
	    	ArrayList<String> uniqueUsers = (ArrayList<String>) usersAtPos.stream()
	    													.distinct()
	    													.collect(Collectors.toList());
	    	String message = uniqueUsers.toString() + "||" +(nonce - 1);
    		JsonObject secureMessage = getsecureMessage(message, userID);
	 		String confidentMessage = secureMessage.get("ciphertext").getAsString();
	 		String messDigSig = secureMessage.get("textDigitalSignature").getAsString();
	    	response.setOnError(false);
	    	response.setServerID(this.ID).setConfidentMessage(confidentMessage).setMessageDigitalSignature(messDigSig);
		}else {
			response.setOnError(true).setErrormessage("nonce must be different for each request");
		}
		return response;
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
    
    public Key getSharedKey(int ID) {
    	return sharedKeys.get(ID);
    }
    
    public JsonObject getsecureMessage(String message, int userID) throws Exception {
		PrivateKey myprivkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH);
		Key sharedKey = getSharedKey(userID);
		JsonObject cipherReq  = TrackerLocationSystem.getSecureText(sharedKey, myprivkey, message);
		return cipherReq;
	}

    
}