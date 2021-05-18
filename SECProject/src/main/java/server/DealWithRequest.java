package server;

import java.io.IOException;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import com.google.gson.JsonObject;
import com.server.grpc.ServerService.secureReplay;


import crypto.AESProvider;
import crypto.RSAProvider;
import shared.Point2D;
import shared.TrackerLocationSystem;

public class DealWithRequest {

	private String PRIVATE_KEY_PATH;
	private Map<Integer, Key> normalUsersharedKeys = new HashMap<>();
	private Map<Integer, Key> HAsharedKeys = new HashMap<>();
    private Map<Integer, List<Integer>> usersNonce = new HashMap<>();
    private InteractWithDB DB;
    private int ID;
    
    public DealWithRequest(int id) {
    	try {
    		this.ID = id;
    		PRIVATE_KEY_PATH = "resources/private_keys/server" + id +"_private.key";
			DB = new InteractWithDB("variables.ini", id);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public String[] validateSubmitRequest(int id, String ciphertext, String signature) throws Exception {
		// Decrypt message
    	String openText = getPlainText("user",id, ciphertext);
		String[] reqFields = getfieldsFromMessage(openText);
		String report = reqFields[0];

		int nonce = Integer.parseInt(reqFields[1]);
		if (!verifySignature(id, report, signature)) {
			throw new Exception("Message was not authenticated");
		}

		if (!verifyHashCash(report, nonce)) {
			throw new Exception("Invalid HashCash");
		}

		// Check uniqueness of nonce
		List<Integer> userNonces = usersNonce.get(id);
		if(userNonces == null)
    		userNonces = new ArrayList<>();

		if(userNonces.contains(nonce)) {
    		throw new Exception("Nonce must be unique for each request");
		}

		userNonces.add(nonce);
		report = report.replace("[", "").replace("]", "");
		if(report.length() <= 0) {
    		throw new Exception("You cannot prove your location with 0 users near you!");
		}

		String[] reportList = report.split(",");
    	List<ProofReport> proofReports = getProofReports(reportList);

    	// Check expected number of proofs
    	if( proofReports.size() <= TrackerLocationSystem.NUM_BIZANTINE_USERS) {
			throw new Exception("Proof size must be larger than number of byzantine users");
		}

		return reqFields;
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
    	secureReplay.Builder response = secureReplay.newBuilder();

    	report = report.replace("[", "").replace("]", "");

    	if(report.length() <= 0) {
    		throw new Exception("You cannot prove your location with 0 users near you!");
		}

    	String[] reportList = report.split(",");
    	List<ProofReport> proofReports = getProofReports(reportList);
    	Point2D proverPos = null;
    	int epoch = 0;

    	if( proofReports.size() <= TrackerLocationSystem.NUM_BIZANTINE_USERS) {
			throw new Exception("Proof size must be larger than number of byzantine users");
		}

    	for(ProofReport pr : proofReports) {
   			PublicKey witPubKey = TrackerLocationSystem.getUserPublicKey(pr.getWitnessID(), "user");
			if(pr.proofDigSigIsValid(witPubKey)) {
				DB.addReportToDatabase(pr.getProverID(), pr.getWitnessID(), pr.getProverPoint(),
						pr.getWitnessPoint(), pr.getEpoch(), pr.isWitnessIsNearProof(),
						pr.getWitnessDigSig());
			}
			proverPos = pr.getProverPoint();
			epoch = pr.getEpoch();
		}

    	DB.addLocationToValidated(id, proverPos, epoch);
		String message = "Your report was submitted successfully||" + (nonce - 1);
		JsonObject secureMessage = getsecureMessage("user",message, id);
		String confidentMessage = secureMessage.get("ciphertext").getAsString();
		String messDigSig = secureMessage.get("textDigitalSignature").getAsString();
		response.setOnError(false);
		response.setServerID(ID).setConfidentMessage(confidentMessage).setMessageDigitalSignature(messDigSig);

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
    public secureReplay.Builder obtainReportHandler(int userId, int epoch, int nonce, String sig) throws Exception {

    	if (!verifyHashCash(userId + "||" + epoch, nonce))
    		throw new Exception("Invalid HashCash");

    	if (!verifySignature(userId, userId + "||" + epoch, sig))
    		throw new Exception("Message not authentic");

    	secureReplay.Builder response = secureReplay.newBuilder();
    	List<Integer> userNonces = usersNonce.get(userId);
    		if(userNonces == null)
    			userNonces = new ArrayList<>();
    	if(!userNonces.contains(nonce)) {
    		userNonces.add(nonce);
    		Point2D userPoint = DB.getLocationGivenEpoch(userId, epoch);
    		if(userPoint != null) {
	    		String message = userId + "||" +userPoint.toString() + "||" +(nonce - 1);
	    		JsonObject secureMessage = getsecureMessage("user",message, userId);
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
		    		JsonObject secureMessage = getsecureMessage("HA", message, requestUserID);
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

	public secureReplay.Builder myProofs(int userID, String epochs, int nonce, String sig) throws Exception {

		if (!verifySignature(userID, userID + "||" + epochs, sig))
			throw new Exception("Messige was not authenticated");

		if (!verifyHashCash(userID + "||" + epochs, nonce))
    		throw new Exception("Invalid HashCash");

		System.out.println("####" + userID + " " + epochs);

    	secureReplay.Builder response = secureReplay.newBuilder();

    	List<Integer> userNonces = usersNonce.get(userID);
    	if(userNonces == null)
			userNonces = new ArrayList<>();

    	if(userNonces.contains(nonce)) {
    		throw new Exception("user nonce already exists");
		}

    	userNonces.add(nonce);

		String[] epochList = epochs.split(";");
		ArrayList <String> proofs = DB.getProofsinEpochs(userID, epochList);

		String message = proofs.toString() + (nonce - 1);
		JsonObject secureMessage = getsecureMessage("user",message, userID);
		String confidentMessage = secureMessage.get("ciphertext").getAsString();
		String messDigSig = secureMessage.get("textDigitalSignature").getAsString();
		response.setOnError(false);
		response.setServerID(ID).setConfidentMessage(confidentMessage).setMessageDigitalSignature(messDigSig);

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
    		JsonObject secureMessage = getsecureMessage("HA",message, userID);
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
     * @throws Exception 
     * ************************************************************************************/
    public void putOrUpdateSharedKeys(String type ,int id , Key key) throws Exception {
    	if(type.equals("HA")) {
    		if(HAsharedKeys.get(id) != null)
    			HAsharedKeys.replace(id, key);
        	else
        		HAsharedKeys.put(id, key);
    		System.out.println("key is stored for "+ type +" = " + id +" ****************************************");
    	}
    	else if(type.equals("user")) {
    		if(normalUsersharedKeys.get(id) != null)
    			normalUsersharedKeys.replace(id, key);
    		else
    			normalUsersharedKeys.put(id, key);
    	}else throw new Exception("there is no user with this name: " + type);
    		
    }
    
    public Key getSharedKey(String type,int ID) throws Exception {
    	if(type.equals("user"))
    		return normalUsersharedKeys.get(ID);
    	else if(type.equals("HA"))
    		return HAsharedKeys.get(ID);
    	else
    		throw new Exception("there is no user with this name: "+ type);
    }
    
    public JsonObject getsecureMessage(String type,String message, int userID) throws Exception {
		PrivateKey myprivkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH);
		Key sharedKey = getSharedKey(type,userID);
		JsonObject cipherReq  = TrackerLocationSystem.getSecureText(sharedKey, myprivkey, message);
		return cipherReq;
	}

	public boolean verifySignature(int userID, String message, String signature) throws Exception {
		PublicKey pubkey = TrackerLocationSystem.getUserPublicKey(userID, "user");
		return RSAProvider.istextAuthentic(message, signature, pubkey);
	}

	public String getPlainText(String type, int userID, String ct) throws Exception {
		Key sharedKey = getSharedKey(type, userID);
		PublicKey pubkey = TrackerLocationSystem.getUserPublicKey(userID, "user");
		return AESProvider.getPlainTextOfCipherText(ct, sharedKey);
	}

	public String[] getfieldsFromMessage(String message) {
    	return message.split(Pattern.quote("||"));
	}

	public boolean verifyHashCash(String message, int nonce) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update((message + nonce).getBytes());
		byte[] hash = messageDigest.digest();
		return (hash[0] == 0 && hash[1] == 0 && hash[2] >>> 4 == 0);
	}
    
}