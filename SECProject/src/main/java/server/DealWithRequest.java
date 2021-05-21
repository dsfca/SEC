package server;

import java.io.IOException;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import com.google.gson.JsonObject;
import com.google.protobuf.Empty;
import com.server.grpc.serverServiceGrpc;
import com.server.grpc.ServerService.BInteger;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.ServerService.DHKeyExcServerReq;
import com.server.grpc.ServerService.secureReplay;
import com.server.grpc.serverServiceGrpc.serverServiceBlockingStub;
import com.user.grpc.Listener.secureRequest;
import com.user.grpc.ListenerServiceGrpc;

import crypto.AESProvider;
import crypto.RSAProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import shared.DiffieHelman;
import shared.Point2D;
import shared.TrackerLocationSystem;

public class DealWithRequest {

	private String PRIVATE_KEY_PATH;
	private Map<Integer, Key> normalUsersharedKeys = new HashMap<>();
	private Map<Integer, Key> HAsharedKeys = new HashMap<>();
    private Map<Integer, List<Integer>> usersNonce = new HashMap<>();
    private InteractWithDB DB;
    private int ID;
    private ArrayList<Listener> listeners = new ArrayList<>();
    private boolean isByzantine;
    
    public DealWithRequest(int id, boolean isByzantine) {
    	try {
    		this.ID = id;
    		this.isByzantine = isByzantine;
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

		int nonce = Integer.parseInt(reqFields[reqFields.length -1]);
		if (!verifySignature(id, report, signature)) {
			throw new Exception("SERVER WARNING: Message was not authenticated");
		}

		if (!verifyHashCash(report, nonce)) {
			throw new Exception("SERVER WARNING: Invalid HashCash");
		}

		// Check uniqueness of nonce
		List<Integer> userNonces = usersNonce.get(id);
		if(userNonces == null)
    		userNonces = new ArrayList<>();

		if(userNonces.contains(nonce)) {
    		throw new Exception("SERVER WARNING: Nonce must be unique for each request");
		}

		userNonces.add(nonce);
		report = report.replace("[", "").replace("]", "");
		if(report.length() <= 0) {
    		throw new Exception("SERVER WARNING: You cannot prove your location with 0 users near you!");
		}

		String[] reportList = report.split(",");
    	List<ProofReport> proofReports = getProofReports(reportList);

    	// Check expected number of proofs
    	if( proofReports.size() <= TrackerLocationSystem.getInstance().getNumBizantineUsers()) {
			throw new Exception("SERVER WARNING: Proof size must be larger than number of byzantine users");
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

    	if( proofReports.size() <= TrackerLocationSystem.getInstance().getNumBizantineUsers()) {
			throw new Exception("Proof size must be larger than number of byzantine users");
		}

    	for(ProofReport pr : proofReports) {
   			PublicKey witPubKey = TrackerLocationSystem.getInstance().getUserPublicKey(pr.getWitnessID(), "user");
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
		sendValueToListeners(id, epoch, proverPos.toString());
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
    public secureReplay.Builder obtainReportHandler(int userId, int epoch, int nonce, String sig, int listenerPort) throws Exception {
    	if (!verifyHashCash(String.valueOf(epoch) , nonce))
    		throw new Exception("Invalid HashCash");

    	if (!verifySignature(userId, String.valueOf(epoch), sig))
    		throw new Exception("Message not authentic");

    	secureReplay.Builder response = secureReplay.newBuilder();
    	List<Integer> userNonces = usersNonce.get(userId);
    		if(userNonces == null)
    			userNonces = new ArrayList<>();
    	if(!userNonces.contains(nonce)) {
    		Point2D userPoint;
    		userNonces.add(nonce);   		
    		userPoint = DB.getLocationGivenEpoch(userId, epoch);
    		if(this.isByzantine)
    			userPoint = new Point2D(((int)Math.random()*10), ((int)Math.random()*10));
    		if(userPoint != null) {
	    		String message = userId + "||" +userPoint.toString() + "||" +(nonce - 1);
	    		JsonObject secureMessage = getsecureMessage("user",message, userId);
		 		String confidentMessage = secureMessage.get("ciphertext").getAsString();
		 		String messDigSig = secureMessage.get("textDigitalSignature").getAsString();
	    		response.setOnError(false);
	    		 response.setServerID(ID).setConfidentMessage(confidentMessage).setMessageDigitalSignature(messDigSig);
	    		 addListener(userId, epoch, listenerPort, "user", userId);
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
    
    
    public secureReplay.Builder obtainLocationReportHAHandler(int requestUserID, int userID, int epoch, int nonce, int listenerPort) throws Exception{
    	secureReplay.Builder response = secureReplay.newBuilder();
    	List<Integer> userNonces = usersNonce.get(0);
		if(userNonces == null)
			userNonces = new ArrayList<>();
		if(!userNonces.contains(nonce)) {
		    	Point2D userPoint = DB.getLocationGivenEpoch(userID, epoch);
		    	if(this.isByzantine)
		    		userPoint = new Point2D((int)(Math.random()*10), (int)(Math.random()*10));
		    	String message;
		    	if(userPoint != null)
			    	message = userID + "||" +userPoint.toString() + "||" +(nonce - 1);
		    	else
		    		message = userID + "||" + "null||" + (nonce -1) ;	    	
		    		JsonObject secureMessage = getsecureMessage("HA", message, requestUserID);
			 		String confidentMessage = secureMessage.get("ciphertext").getAsString();
			 		String messDigSig = secureMessage.get("textDigitalSignature").getAsString();
			    	response.setOnError(false);
			    	response.setServerID(this.ID).setConfidentMessage(confidentMessage).setMessageDigitalSignature(messDigSig);
			    	addListener(userID, epoch, listenerPort, "HA", requestUserID);
			    	sendValueToListeners(userID, epoch, userPoint.toString());
					
		}else {
			response.setOnError(true);
			response.setErrormessage("user nonce already exists");
		}
	    return response;
	}

	public secureReplay.Builder myProofs(int userID, String epochs, int nonce, String sig) throws Exception {

		if (!verifySignature(userID, userID + "||" + epochs, sig))
			throw new Exception("Message was not authenticated");

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
    
    
    public Key  DHkeyExchange(int serverID, int serverPort) throws Exception {
		DiffieHelman df = new DiffieHelman();
		PublicKey dfPubKey = df.getPublicKey();
		String pbkB64 = Base64.getEncoder().encodeToString(dfPubKey.getEncoded());
		String digSigMyDHpubkey = TrackerLocationSystem.getInstance().getDHkeySigned(dfPubKey, PRIVATE_KEY_PATH);	
		BInteger p = DiffieHelman.write(df.getP());
		BInteger g = DiffieHelman.write(df.getG());
		
		DHKeyExcServerReq req = DHKeyExcServerReq.newBuilder().setServerID(ID).setP(p).setG(g).setMyDHPubKey(pbkB64)
				.setDigSigPubKey(digSigMyDHpubkey).build();
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", serverPort).usePlaintext().build();
		serverServiceBlockingStub serverStub = serverServiceGrpc.newBlockingStub(channel);
		
		DHKeyExcRep rep = serverStub.dHKeyExchangeServer(req);
		PublicKey key = TrackerLocationSystem.getInstance().getServerPublicKey(serverID);
		String servPbkDigSig = rep.getDigSigPubkey();
		String servPubKey = rep.getMyPubKey();
		channel.shutdown();
		return TrackerLocationSystem.getInstance().createSecretKey(df, servPbkDigSig, servPubKey, key);	
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
		PrivateKey myprivkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH, TrackerLocationSystem.password);
		Key sharedKey = getSharedKey(type,userID);
		JsonObject cipherReq  = TrackerLocationSystem.getInstance().getSecureText(sharedKey, myprivkey, message);
		return cipherReq;
	}

	public boolean verifySignature(int userID, String message, String signature) throws Exception {
		PublicKey pubkey = TrackerLocationSystem.getInstance().getUserPublicKey(userID, "user");
		return RSAProvider.istextAuthentic(message, signature, pubkey);
	}

	public String getPlainText(String type, int userID, String ct) throws Exception {
		Key sharedKey = getSharedKey(type, userID);
		PublicKey pubkey = TrackerLocationSystem.getInstance().getUserPublicKey(userID, "user");
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
	
	/**************************************************************************************
	* 											-addListener()
	* -add listener to array of listener;
	*  
	* 
	* ************************************************************************************/
	public void addListener(int userID, int epoch, int listenerPort, String listenerType, int listenerid) {
		Listener e = new Listener(userID, epoch, listenerPort, listenerType, listenerid);
		listeners.add(e);
	}
	/**************************************************************************************
	* 											-sendValueToListeners()
	* -send new value( userID point) to all listeners of this value.
	 * @throws Exception 
	*  
	* 
	* ************************************************************************************/
	public void sendValueToListeners(int userID, int epoch, String userPoint) throws Exception {
		ListenerServiceGrpc.ListenerServiceStub listenerAsyncStub;
		for(Listener listener: this.listeners) {
			if(listener.isListening(userID, epoch)) {
				JsonObject securerequest = getsecureMessage(listener.getListenerType(), userPoint, listener.getListenerID());
				ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", listener.getListenerPort())
						.usePlaintext().build();
				listenerAsyncStub = ListenerServiceGrpc.newStub(channel).withDeadlineAfter(10, TimeUnit.SECONDS)
						.withWaitForReady();;
				secureRequest  request = secureRequest.newBuilder().setServerID(this.ID)
						.setConfidentMessage(securerequest.get("ciphertext").getAsString())
						.setMessageDigitalSignature(securerequest.get("textDigitalSignature").getAsString())
						.build();
				System.out.println("sending listener value:" +listener.getListenerPort() );
				listenerAsyncStub.informAboutNewWrite(request, ignore);
			}
		}
	}
	
	private StreamObserver<Empty> ignore = new StreamObserver<Empty>() {
		@Override public void onNext(Empty empty) {}
		@Override public void onError(Throwable throwable) { }
		@Override public void onCompleted() { }
	};

	public void removeListener(String listenerType, int listenerID,int userID, int epoch) {
		Listener e = null;
   		for(Listener lis: listeners) {
   			if(lis.equals(listenerType, listenerID,userID, epoch))
   				e = lis;
   		}
   		listeners.remove(e);
   	}

    
}