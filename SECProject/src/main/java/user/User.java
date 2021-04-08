package user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.user.grpc.UserService.LocProofRep;
import com.user.grpc.UserService.LocProofReq;
import com.user.grpc.UserService.Position;
import com.user.grpc.userServiceGrpc;
import com.user.grpc.userServiceGrpc.userServiceBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import shared.Point2D;
import shared.TrackerLocationSystem;

public class User {
	
	private final double closer_range_dist = 2;

	private int myID;
	private int port;
	private int myCurrentEpoch = -1;
	private int myOldEpoch = -1;
	private int G_width;
	private int G_height;
	private Point2D myCurrentPoosition; 
	private final Point2D HOME;
	
	
	
	/**************************************************************************************
	* 											-User class constructor()
	* - 
	* 
	* ************************************************************************************/
	public User(int ID, int G_width, int  G_height) throws Exception {
		this.myID = ID;
		this.port = ID + Integer.parseInt("9090");
		this.G_width = G_width;
		this.G_height = G_height;
		
		myCurrentPoosition = new Point2D((int)(Math.random()*G_width), (int)(Math.random()*G_height));
		HOME = myCurrentPoosition;
		initThreadToUpdFilePos();
		initThreadForUpdPos();
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
					Server userServer = ServerBuilder.forPort(port).addService(new UserServiceImp(myID)).build();
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
			System.out.println(" user thread for receive locationProof request running");
			
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
	public void sndProofRequest(List<ManagedChannel> channels, String ID, String epoch, Point2D proverPos) {
		userServiceBlockingStub userSerStub;
		Position.Builder pos = Position.newBuilder().setX(proverPos.getX()).setY(proverPos.getY());;
		for(ManagedChannel channel : channels) {
			userSerStub = userServiceGrpc.newBlockingStub(channel);
			LocProofReq req = LocProofReq.newBuilder().setProverID(ID).
					setEpoch(epoch).setLoc(pos).build();
			LocProofRep rep = userSerStub.requestLocationProof(req);
			String proof = rep.getProof();
			System.out.println("proof from "+ channel.authority() + ": " + proof);
		}
	}
	
	/**************************************************************************************
	 * 											-updateEpoc()
	 * - 
	 * 
	 * ************************************************************************************/
	public synchronized void updateEpoc(int epoch) {
		System.out.println("updating epoch of user " + myID);
		myCurrentEpoch = epoch;
		notifyAll();
	}
	
	/**************************************************************************************
	 * 											-updateMyPositionInFile()
	 * - update the user position in the grid file given an epoch.
	 *   if the current position does not change the thread wait until the
	 *   current position change.
	 * 
	 * -input: 
	 * 			-epoch: epoch where the user is.
	 * 
	 * ************************************************************************************/
	public synchronized void updateMyPositionInFile(int epoch) throws InterruptedException {
		while( myCurrentEpoch == myOldEpoch) {
			wait();
		}
		TrackerLocationSystem.update_user_pos(myID, myCurrentPoosition, myCurrentEpoch, port);
		myOldEpoch = myCurrentEpoch;
	}
	
	/**************************************************************************************
	 * 											-getCloserUsers()
	 * - returns the channel of the user that are closer to him in a given epoch 
	 * in order to send them Proof Location request.
	 * 
	 *-input
	 *			-epoch: the epoch of user use to get closer users.
	 *
	 *- returns: list of users channel. 
	 * 
	 * ************************************************************************************/
	public List<ManagedChannel> getCloserUsers(int epoch) throws IOException{
		List<UserLocation> usersInEpoch = TrackerLocationSystem.getAllUsersInEpoch(epoch);
		List<ManagedChannel> closerChannel = new ArrayList<>();
		for(UserLocation u: usersInEpoch) {
			if(u.getUserId() != myID) {
				Point2D u_point = u.getPosition();
				double dist = myCurrentPoosition.distance(u_point);
				if( dist <= closer_range_dist ) {
					ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", u.getPort()).usePlaintext().build();
					closerChannel.add(channel);
				}
			}
		}
		return closerChannel;	
	}
	
	/**************************************************************************************
	 * 											-initThreadToUpdFilePos()
	 * - this procedure start a thread that will run in a loop to update the
	 *   user position in the grid file. 
	 *   before updating the user position the thread sleep between 10 to 15sec.
	 *   when thread finish to update position it send proof request to user near him.
	 *
	 * 
	 *-input
	 *
	 *- returns: void
	 * 
	 * ************************************************************************************/
	private void initThreadToUpdFilePos(){
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						updateMyPositionInFile(myCurrentEpoch);
						int sleepTime = (int)(Math.random()*5 + 10);
						Thread.sleep(sleepTime);
						
						List<ManagedChannel> closerChannel = getCloserUsers(myCurrentEpoch);
						sndProofRequest( closerChannel, ""+myID, ""+myCurrentEpoch, myCurrentPoosition);
						sleepTime = (int)(Math.random()*5 + 10);
						Thread.sleep(sleepTime);
					}
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
				
			}
		};
		
	new Thread(r).start();
	}
	
	/**************************************************************************************
	 * 											-initThreadForUpdPos()
	 * - this thread changes the current position of the user.
	 * 	A user has 45% to move to another position if it's not
	 *  at home other wise it gets back to home
	 *
	 * 
	 *-input
	 *
	 *- returns: void
	 * 
	 * ************************************************************************************/
	private void initThreadForUpdPos() {
		Runnable r = new Runnable() {
			double prob_to_move = 0.45;
			@Override
			public void run() {
				try {
					while(true) {
						Thread.sleep(5000);
						double	prob = Math.random();
						if( prob <= prob_to_move  ) {
							myCurrentPoosition.setXY( (int)(Math.random()*G_width), (int)(Math.random()*G_height) );
							System.out.println("user "+ myID + "changing my position to "+ myCurrentPoosition.toString());
						}
						else if(!HOME.equals(myCurrentPoosition)) {
							myCurrentPoosition.setXY(HOME.getX(), HOME.getY());
						}
					}
				
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}	
		};
		new Thread(r).start();
	}
	
}

