package user;

import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.user.grpc.UserService.LocProofRep;
import com.user.grpc.UserService.LocProofReq;
import com.user.grpc.UserService.Position;
import com.user.grpc.userServiceGrpc;
import com.user.grpc.userServiceGrpc.userServiceStub;
import com.server.grpc.serverServiceGrpc;
import com.server.grpc.serverServiceGrpc.serverServiceBlockingStub;
import com.server.grpc.ServerService.subLocRepReq;
import com.server.grpc.ServerService.BInteger;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.ServerService.subLocRepReply;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import crypto.RSAProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import shared.DiffieHelman;
import shared.Point2D;
import shared.TrackerLocationSystem;

public class User {
	
	//distance that user consider to see if a user is near him
	private final double closer_range_dist = 2;
	
	private final   String PRIVATE_KEY_PATH;

	private int myID;
	private int port;
	private int myCurrentEpoch = 0;
	private Point2D myCurrentPoosition; 
	
	
	
	/**************************************************************************************
	* 											-User class constructor()
	* - 
	* 
	* ************************************************************************************/
	public User(int ID) throws Exception {
		this.myID = ID;
		this.port = ID + Integer.parseInt("9090");
		PRIVATE_KEY_PATH = "resources/private_keys/user" + myID + "_private.key";
		init();
		initThreadToSndReqProof();
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
					Server userServer = ServerBuilder.forPort(port).addService(new UserServiceImp(myID, PRIVATE_KEY_PATH)).build();
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
	 * -send proof location request to all user in the channel(this user 
	 * 	are considered users near him). before sending each message it computes the
	 *  digital signature of the message and before considering that the user is near
	 *   or not it verifies the signature of the witness message. if it holds
	 *    then it puts the prove  the list and returns it. 
	 * 
	 * -input
	 * 		- channels: list of user channel to send location proof request
	 * 		- ID: ID of the user who pretend to proof his location
	 * 		- epoch: epoch which user want to proof his location
	 * 		- x,y: Location where user want to be prooved.
	 * @throws Exception generated by readprivatekey function in
	 * 	 rsaprovider class when the path of the private key is not found.
	 * 
	 * - return: List<string>( where each string is the proof of the witness 
	 * 	that the user is actually near him).
	 * 
	 * ************************************************************************************/
	public List<String> sndProofRequest(List<ManagedChannel> channels, int ID, int epoch, Point2D proverPos) throws Exception {

		final CountDownLatch finishLatch = new CountDownLatch(channels.size());
		List<String> proofs = new ArrayList<>();
		StreamObserver<LocProofRep> replyObserver = new StreamObserver<LocProofRep>() {
		
			@Override
			public void onNext(LocProofRep reply) {
				if(reply.getError())
					System.err.println(reply.getMessageError());
				else 
					proofs.add(reply.getProof());
			}

			@Override
			public void onError(Throwable t) {
				Status status = Status.fromThrowable(t);
				System.out.println("[" + ID + "] Error: " + status);
				finishLatch.countDown();
			}

			@Override
			public void onCompleted() {
				finishLatch.countDown();
				
			}
		};
		userServiceStub userAsyncStub;
		Position.Builder pos = Position.newBuilder().setX(proverPos.getX()).setY(proverPos.getY());	
		for(ManagedChannel channel : channels) {
			String sig = get_sig_of(ID +" "+ epoch +" "+ proverPos.toString());
			userAsyncStub = userServiceGrpc.newStub(channel).withDeadlineAfter(5, TimeUnit.SECONDS);
					LocProofReq req = LocProofReq.newBuilder().setProverID(ID)
									 		.setEpoch(epoch).setLoc(pos).setDigSign(sig).build();
					userAsyncStub.requestLocationProof(req, replyObserver);
		}
		finishLatch.await();
		for(ManagedChannel channel : channels)
			channel.shutdown();
		return proofs;
	}

	/**************************************************************************************
	 * 										-submitLocationReport()
	 *	- send location report to the server
	 *
	 * -input
	 * 		- proofs: list of user's proofs gathered using sndProofRequest() method
	 * 		- ID: ID of the user who report his location
	 * 		- epoch: epoch which user want to report his location
	 * 		- x,y: Location of user to report
	 *
	 * - return: List<string>( where each string is the proof of the witness
	 * 	that the user is actually near him).
	 *
	 * ************************************************************************************/
	public void submitLocationReport(List<String> proofs, int ID, int epoch, Point2D position) {
		Position.Builder pos = Position.newBuilder().setX(position.getX()).setY(position.getY());

		serverServiceBlockingStub serverStub = serverServiceGrpc.newBlockingStub(
				ManagedChannelBuilder.forAddress("127.0.0.1", TrackerLocationSystem.getServerPort())
						.usePlaintext().build()
		).withWaitForReady();

		subLocRepReq submitRequest = subLocRepReq.newBuilder().setUserID(ID).setEpoch(epoch)
				.setReport("Location: " + pos.toString() + ", proofs:" + proofs.toString())
				.build();

		subLocRepReply submitReply = serverStub.submitLocationReport(submitRequest);

		System.out.println("[" + ID + "] Got submit reply with code " + submitReply.getReplycode() +
				           ": " + submitReply.getReplymessage());

	}
	
	
	/**************************************************************************************
	* 											-get_sig_of()
	* - return the hash of the string "s" ciphered with private key of
	*   the user(digital signature of string s) 
	* 
	* ************************************************************************************/
	public String get_sig_of(String s) throws Exception {
		PrivateKey priv = RSAProvider.readPrivKey(PRIVATE_KEY_PATH);
		String sig = RSAProvider.getTexthashEnWithPriKey(s, priv);
		return sig;
	}
	
	
	
	
	public Key  DHkeyExchange() throws Exception {
		DiffieHelman df = new DiffieHelman();
		PublicKey dfPubKey = df.getPublicKey();
		String pbkB64 = Base64.getEncoder().encodeToString(dfPubKey.getEncoded());
		String digSigMyDHpubkey = TrackerLocationSystem.getDHkeySigned(dfPubKey, PRIVATE_KEY_PATH);	
		System.out.println("user"+ myID + " DH publik key = " + digSigMyDHpubkey);
		System.out.println("user"+ myID + " publik key = " + pbkB64);
		BInteger p =DiffieHelman.write(df.getP());
		BInteger g =DiffieHelman.write(df.getG());
		
		DHKeyExcReq req = DHKeyExcReq.newBuilder().setP(p).setG(g).setMyDHPubKey(pbkB64)
				.setDigSigPubKey(digSigMyDHpubkey).setUserID(myID).build();
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", TrackerLocationSystem.getServerPort()).usePlaintext().build();
		serverServiceBlockingStub serverStub = serverServiceGrpc.newBlockingStub(channel);
		
		DHKeyExcRep rep = serverStub.dHKeyExchange(req);
		String servPubKeyPath = "resources/public_keys/server_public.key";
		PublicKey key = RSAProvider.readPubKey(servPubKeyPath);
		String servPbkDigSig = rep.getDigSigPubkey();
		String servPubKey = rep.getMyPubKey();
		channel.shutdown();
		return TrackerLocationSystem.createSecretKey(df, servPbkDigSig, servPubKey, key);	
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
		UserLocation myPosInEpoch = TrackerLocationSystem.getMyPosInEpoc(myID, epoch);
		for(UserLocation u: usersInEpoch) {
			if(u.getUserId() != myID) {
				Point2D u_point = u.getPosition();
				double dist = myPosInEpoch.getPosition().distance(u_point);
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
	private void initThreadToSndReqProof(){
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					List<String> proofs;
					int sleepTime;
					Key userNServerSharedKey;
					while(true) {
						sleepTime = (int)(Math.random()*15000 + 45000); //time to sleep between 45s-1min
						System.out.println("**************************** waiting " + sleepTime + " to send proof my location**********************************");
						Thread.sleep(sleepTime);
						myCurrentEpoch = myCurrentEpoch%10 + 1;
						myCurrentPoosition = TrackerLocationSystem.getMyPosInEpoc(myID, myCurrentEpoch).getPosition();
						List<ManagedChannel> closerChannel = getCloserUsers(myCurrentEpoch);
						proofs = sndProofRequest( closerChannel, myID, myCurrentEpoch, myCurrentPoosition);
						System.out.println("ID = "+ myID +" users near me at epoch= " +myCurrentEpoch +"  are : "+proofs);
						if(myCurrentEpoch % 2 == 0) {
							userNServerSharedKey = DHkeyExchange();
							System.out.println("user agree key :" + new String(userNServerSharedKey.getEncoded()));
						}
					//	submitLocationReport(proofs, myID, myCurrentEpoch, myCurrentPoosition);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		};
		
	new Thread(r).start();
	}
	
	
}

