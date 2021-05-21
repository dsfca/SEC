package user;

import java.security.PrivateKey;
import java.security.PublicKey;

import com.user.grpc.UserService.LocProofRep;
import com.user.grpc.UserService.LocProofReq;
import com.user.grpc.userServiceGrpc.userServiceImplBase;

import crypto.RSAProvider;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import shared.Point2D;
import shared.TrackerLocationSystem;

public class UserServiceImp extends userServiceImplBase  {
	
	private int ID;
	private String PRIVATE_KEY_PATH;
	
	public  UserServiceImp(int ID, String privkeypath) {
		this.ID = ID;
		this.PRIVATE_KEY_PATH = privkeypath;
	} 
	
	/**************************************************************************************
	 * 										-requestLocationProof():
	 * - remote procedural call where user received the Location Proof request,
	 * 	handle it and send reply to client that invoke it.
	 * 	Before reply to the client it verifies the digital signature of the client
	 *  if it holds then it process the request, otherwise
	 *   it does not process the request of the client  
	 * -input
	 * 		- request: client request to proof his location, contain:proverID, epoch and
	 * 		 position where user want to proof
	 * 		
	 * 		- responseObserver allows to response client. 
	 * 
	 * ************************************************************************************/
	@Override
	public void requestLocationProof(LocProofReq request, StreamObserver<LocProofRep> responseObserver) {
		try {

			int proverID =  request.getProverID();
			int epoch = request.getEpoch();
			Point2D proverPt = new Point2D(request.getLoc().getX(), request.getLoc().getY());
			
			String req_dig_sig = request.getDigSign();
			String req_conc = proverID +" "+ epoch +" "+ proverPt.toString();
			
			PublicKey provPubKey = TrackerLocationSystem.getInstance().getUserPublicKey(proverID, "user");
			Boolean reqIsAuth = RSAProvider.istextAuthentic(req_conc, req_dig_sig, provPubKey);

			if (!reqIsAuth) {
				responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
				return;
			}
			if (!proverPt.equals(TrackerLocationSystem.getPosInEpoc(proverID, epoch).getPosition())) {
				responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException());
				return;
			}

			LocProofRep.Builder response = locationProofHandler(proverID,epoch, proverPt);
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();

		} catch (Exception e) {
			//something went wrong
			responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
		}
		
	}
	
	
	/**************************************************************************************
	 * 										-locationProofHandler():
	 * - handles location proof request received in requestLocationProof method above.
	 * 		it will process all request received in requestLocationProof and return response.
	 * -input
	 * 		- request: request received on method requestLocation(above)
	 * 		
	 * - return the response that will be sent to client.	
	 * @throws Exception 
	 * 
	 * ************************************************************************************/
	private LocProofRep.Builder locationProofHandler(int proverID, int epoch, Point2D proverPos) throws Exception{
		LocProofRep.Builder response = LocProofRep.newBuilder();
		PrivateKey key = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH, TrackerLocationSystem.password);
		String proof = getProof(proverID, proverPos, epoch);
		String proof_dig_sig = RSAProvider.getTexthashEnWithPriKey(proof, key);
		proof = proof +" "+ proof_dig_sig;
		proof_dig_sig = RSAProvider.getTexthashEnWithPriKey(proof, key);
		response.setProof(proof);
		response.setProofDigSig(proof_dig_sig);
		response.setWitnessID(ID);
		return response;	
	}


	/**************************************************************************************
	 * 										-getProof():
	 * - returns the proof that proves that the user that invoke reqProofLoc is closer to him or not
	 * 		the proof have the pattern below:
	 * 		<proverID> <witnessID> <prover position> <witness Point> <distance between them > <True/false>
	 *		true- indicates that they are close and false other wise
	 * -input
	 * 		- proverID: Id of the prover
	 * 		- prov_pos:position of the prover
	 * 		- epoch:the epoch where the prover wants to prove its location
	 * 		
	 * - return string	
	 * 
	 * ************************************************************************************/
	private String getProof(int proverID, Point2D prov_pos, int epoch) {
		// get witness point in the epoch (epoch)
		UserLocation me = TrackerLocationSystem.getPosInEpoc(ID, epoch);
		Point2D myPointInEpoch = me.getPosition();
		//compute the distance between prover and witness
		double dist = prov_pos.distance(myPointInEpoch);
		// decide if the prover is near or not
		boolean isNear = dist <= 2;
		//compute the result: proverID witnessID proverPoint witnessPoint distace T/F
		// T- if prover is near witness and F - other wise
		String proof = proverID + " " + ID + " " + prov_pos.toString() + " "+ myPointInEpoch.toString() + " " + epoch + " " + isNear;
		return proof;
	}
	
	
	
	
	

}
