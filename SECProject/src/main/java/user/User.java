package user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.user.grpc.UserService;
import com.user.grpc.UserService.LocProofRep;
import com.user.grpc.UserService.LocProofReq;
import com.user.grpc.UserService.Position;
import com.user.grpc.userServiceGrpc;
import com.user.grpc.userServiceGrpc.userServiceBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class User {

	private int userID;
	private int port;
	
	public User(int ID, int port) throws Exception {
		this.userID = ID;
		this.port = port;
		init();
	}
	
	
	/**************************************************************************************
	 * 											-init()
	 * - init user server to receive proof location request in port given in the constructor
	 * and use localhost as IP
	 * 
	 * ************************************************************************************/
	private void init() {
		 Runnable r =	new Runnable() {
				@Override
				public void run() {
					Server userServer = ServerBuilder.forPort(port).addService(new UserServiceImp()).build();
					try {
						userServer.start();
						System.out.println("user server start at " + userServer.getPort());
						
						userServer.awaitTermination();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					
				}
			};
			
			new Thread(r).start();
			System.out.println("thread running");
			
		}
	
	/**************************************************************************************
	 * 										-sndProofRequest()
	 * -send proof request to all user in the channel (channels) and print their 
	 * response(must be changed)
	 * 
	 * -input
	 * 		- channels: list of user channel to send location proof request
	 * 		- ID: ID of the user who pretend to proof his location
	 * 		- epoch: epoch which user want to proof his location
	 * 		- x,y: Location where user want to be prooved. 
	 * 
	 * ************************************************************************************/
	public void sndProofRequest(List<ManagedChannel> channels, String ID, String epoch, int x, int y) {
		userServiceBlockingStub userSerStub;
		Position.Builder pos = Position.newBuilder().setX(x).setY(y);;
		for(ManagedChannel channel : channels) {
			userSerStub = userServiceGrpc.newBlockingStub(channel);
			LocProofReq req = LocProofReq.newBuilder().setProverID(ID).
					setEpoch(epoch).setLoc(pos).build();
			LocProofRep rep = userSerStub.requestLocationProof(req);
			String proof = rep.getProof();
			System.out.println("proof from "+ channel.authority() + ": " + proof);
		}
	}
	
	
	
	
	
	
	
	public static void main(String[] args) {
		try {
			User u2 = new User(2, 9092);
			System.out.println("u2 started");
			User u1 = new User(1, 9091);
			System.out.println("u1 started");
			User u3 = new User(3, 9093);
			System.out.println("u3 started");
			User u4 = new User(4, 9094);
			System.out.println("u4 started");
			
			
			List<ManagedChannel> channels = new ArrayList<>();
			for(int i = 1; i <= 4; i++ ) {
				ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090 + i).usePlaintext().build();
				channels.add(channel);
			}
			
			Thread.sleep(10000);
		
			u1.sndProofRequest(channels, "1", "0", 0,0);

			Thread.sleep(10000);
			
			u2.sndProofRequest(channels, "2", "0", 0,1);
			
			Thread.sleep(10000);
			
			u3.sndProofRequest(channels, "3", "0", 1,0);
			
			Thread.sleep(10000);
			
			u4.sndProofRequest(channels, "4", "0", 1,1);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
	}
	
}


