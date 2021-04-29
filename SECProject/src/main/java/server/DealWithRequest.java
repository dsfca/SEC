package server;

import java.io.IOException;
import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.server.grpc.ServerService.Position;
import com.server.grpc.ServerService.obtLocRepReply;
import com.server.grpc.ServerService.subLocRepReply;

import shared.Point2D;
import shared.TrackerLocationSystem;

public class DealWithRequest {

	private static final String PRIVATE_KEY_PATH = "resources/private_keys/server_private.key";
	private Map<Integer, Key> sharedKeys = new HashMap<>();
    private Map<Integer, List<Integer>> usersNonce = new HashMap<>();
    private InteractWithDB DB;
    
    public DealWithRequest() {
    	try {
			DB = new InteractWithDB("variables.ini");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
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
    public subLocRepReply.Builder submitReportHandler(int id, String report, int nonce) throws Exception {
    	List<Integer> userNonces = usersNonce.get(id);
    	subLocRepReply.Builder response = subLocRepReply.newBuilder();
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
			    	 response.setReplycode(replyCode.OK.ordinal());
				     response.setReplymessage("Your report was submitted successfully");
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
    public obtLocRepReply.Builder obtainReportHandler(int userId, int epoch, int nonce) throws Exception {
    	obtLocRepReply.Builder response = obtLocRepReply.newBuilder();
    	List<Integer> userNonces = usersNonce.get(userId);
    		if(userNonces == null)
    			userNonces = new ArrayList<>();
    	if(!userNonces.contains(nonce)) {
    		userNonces.add(nonce);
    		Point2D userPoint = DB.getLocationGivenEpoch(userId, epoch);
    		if(userPoint != null) {
	    		Position.Builder position = Position.newBuilder().setX(userPoint.getX())
	    				.setY(userPoint.getY());
	    		response.setOnError(false);
	    		response.setPos(position).setUserID(userId);
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
    
}