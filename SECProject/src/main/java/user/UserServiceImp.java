package user;



import java.io.IOException;
import java.util.List;

import com.user.grpc.UserService.LocProofRep;
import com.user.grpc.UserService.LocProofReq;
import com.user.grpc.userServiceGrpc.userServiceImplBase;

import io.grpc.stub.StreamObserver;
import shared.Point2D;
import shared.TrackerLocationSystem;

public class UserServiceImp extends userServiceImplBase  {
	
	private int ID;
	
	public  UserServiceImp(int ID) {
		this.ID = ID;
	} 
	
	/**************************************************************************************
	 * 										-requestLocationProof():
	 * - remote procedural call where user received the Location Proof request,
	 * 	handle it and send reply to client that invoke it.
	 * -input
	 * 		- request: client request to proof his location, contain:proverID, epoch and
	 * 		 position where user want to proof
	 * 		
	 * 		- responseObserver allows to response client. 
	 * 
	 * ************************************************************************************/
	@Override
	public void requestLocationProof(LocProofReq request, StreamObserver<LocProofRep> responseObserver) {
		System.out.println("proof location received from " + request.getProverID());
		
		LocProofRep.Builder response = locationProofHandler(request);
		responseObserver.onNext(response.build());
		
		responseObserver.onCompleted();	
		
	}
	
	
	/**************************************************************************************
	 * 										-locationProofHandler():
	 * - handles location proof request received in requestLocationProof method above.
	 * 		it will process all request received in requestLocationProof and return response.
	 * -input
	 * 		- request: request received on method requestLocation(above)
	 * 		
	 * - return the response that will be sended to client.	
	 * 
	 * ************************************************************************************/
	private LocProofRep.Builder locationProofHandler(LocProofReq request){
		LocProofRep.Builder response = LocProofRep.newBuilder();
		int X = request.getLoc().getX();
		int Y = request.getLoc().getY();
		Point2D prov_pos = new Point2D(X, Y);
		int epoch = Integer.parseInt(request.getEpoch());
		String proof = getProof(request.getProverID(), prov_pos, epoch);
		response.setProof(proof);
		return response;	
	}


	/**************************************************************************************
	 * 										-getProof():
	 * - returns the proof that proves that the user that invoke reqProofLoc is closer to him or not
	 * 		the proof have the pattern below:
	 * 		<proverID> <witnessID> <prover position> <witness Point> <distance between them > <True/false>
	 *		true- indicates that they are closer and false other wise
	 * -input
	 * 		- proverID: Id of the prover
	 * 		- prov_pos:position of the prover
	 * 		- epoch:the epoch where the prover wants to prove its location
	 * 		
	 * - return string	
	 * 
	 * ************************************************************************************/
	private String getProof(String proverID, Point2D prov_pos, int epoch) {
		// get witness point in the epoch (epoch)
		UserLocation me = getMyPosInEpoc(ID, epoch);
		Point2D myPointInEpoch = me.getPosition();
		//compute the distance between prover and witness
		double dist = prov_pos.distance(myPointInEpoch);
		// decide if the prover is near or not
		boolean isNear = dist <= 2;
		//compute the result: proverID witnessID proverPoint witnessPoint distace T/F
		// T- if prover is near witness and F - other wise
		String proof = proverID + " " + ID + " " + prov_pos.toString() + " "+ myPointInEpoch.toString() + " " + dist + " " + isNear;
		return proof;
	}
	
	/**************************************************************************************
	 * 										-getMyPosInEpoc():
	 * - return the user location at a given epoch
	 * -input
	 * 		- myID: the Id of the user that want to get its position. 
	 * 		- epoch: the epoch where he wants to get its position
	 * 		
	 * - return: UserLocation
	 * 
	 * ************************************************************************************/
	UserLocation getMyPosInEpoc(int myId, int epoch) {
		try {
			List<UserLocation> users = TrackerLocationSystem.getAllUsersInEpoch(epoch);
			for(UserLocation u: users) {
				if(u.getUserId() == ID && epoch == u.getEpoch())
					return u;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	

}
