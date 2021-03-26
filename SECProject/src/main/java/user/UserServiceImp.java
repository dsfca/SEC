package user;



import com.user.grpc.UserService.LocProofRep;
import com.user.grpc.UserService.LocProofReq;
import com.user.grpc.userServiceGrpc.userServiceImplBase;

import io.grpc.stub.StreamObserver;

public class UserServiceImp extends userServiceImplBase  {
	
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
		response.setProof("I get your Location proof request");
		return response;
		
	}
	
	
	

}
