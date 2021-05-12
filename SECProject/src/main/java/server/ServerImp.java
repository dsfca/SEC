package server;

import com.google.gson.JsonObject;
import com.google.protobuf.Empty;
import com.server.grpc.ServerService;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.ServerService.secureReplay;
import com.server.grpc.ServerService.secureRequest;
import com.server.grpc.serverServiceGrpc;
import com.server.grpc.serverServiceGrpc.serverServiceStub;
import com.server.grpc.serverServiceGrpc.serverServiceImplBase;
import crypto.AESProvider;
import crypto.RSAProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.ini4j.Ini;
import shared.DiffieHelman;
import shared.Point2D;
import shared.TrackerLocationSystem;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;



public class ServerImp extends serverServiceImplBase {

    private  String PRIVATE_KEY_PATH;
    private DealWithRequest dealWithReq;
    private Key [] sharedKey;
    private int N_timesSharedKeyUsed;
    private String rep_sig_delimiter;

    private boolean sentReady;

    private int num_servers;
    private int server_start_port;
    private int num_byzantines;
    private int quorum;
    private int myId;
    private List<ManagedChannel> serverChannels;

    private Hashtable<String, ackDetail> echos;
    private Hashtable<String, ackDetail> readys;

    /** This stores details about echos and readys messages during BRB */
    private class ackDetail {
    	private boolean ackSent;
    	private Set<Integer> serverIds;
    	private CountDownLatch countDownLatch;

    	public ackDetail(boolean sent) {
    		ackSent = sent;
    		serverIds = new HashSet<>();
    		countDownLatch = new CountDownLatch(1);
		}
		public boolean isAckSent() {
    		return ackSent;
		}
		public void setAckSent() {
    		ackSent = true;
		}
		public void addId(Integer id) {
    		serverIds.add(id);
		}
		public int ackCount() {
    		return serverIds.size();
		}
		public CountDownLatch getLatch() {
    		return countDownLatch;
		}
		public void delivered() {
    		countDownLatch.countDown();
		}
	}

    public ServerImp(int id, DealWithRequest dwr) throws IOException {
    	PRIVATE_KEY_PATH = "resources/private_keys/server" + id +"_private.key";
		dealWithReq = dwr;
		this.myId = id;
		this.num_servers = new Ini(new File("variables.ini")).get("Server","number_of_servers", Integer.class);
		this.num_byzantines = new Ini(new File("variables.ini")).get("Server","number_of_byzantines", Integer.class);
		this.quorum = (num_servers+num_byzantines)/2;
		this.sentReady = false;
		this.server_start_port = new Ini(new File("variables.ini")).get("Server","server_start_port", Integer.class);

		this.sharedKey = new Key [num_servers];
		this.serverChannels = new ArrayList<>();

		this.echos = new Hashtable<>();
		this.readys = new Hashtable<>();

		this.rep_sig_delimiter = "|<<<|";
	}
    

	/**************************************************************************************
     *                                  - submitLocationReport()
     *  RPC: server received location report from a user; handle it and send reply
     *  - input:
     *      - request: the submitLocationReportRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
    @Override
    public void submitLocationReport(secureRequest request, StreamObserver<secureReplay> responseObserver) {
    	try {
			String[] message = dealWithReq.validateSubmitRequest(request.getUserID(),
															  request.getConfidentMessage(),
															  request.getMessageDigitalSignature());
			String report = message[0];
			int nonce = Integer.parseInt(message[1]);

	       	// Build channels with other servers
			for(int server_id = 0; server_id < this.num_servers; server_id++) {
				ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", server_start_port+server_id)
					.usePlaintext().build();
				serverChannels.add(channel); // Store it for reuse
			}

			// Save latch for later block
			String echoMsg = report + this.rep_sig_delimiter + request.getMessageDigitalSignature();
			ackDetail echoReady = new ackDetail(false);
			readys.put(echoMsg, echoReady);

	       	// Broadcast echo message, wait for delivery but 10s tops
			echoSubmitRequest(report + this.rep_sig_delimiter + request.getMessageDigitalSignature());
			echoReady.getLatch().await(10, TimeUnit.SECONDS);

			// Message is considered delivered, time to add it to DB
	       	ServerService.secureReplay.Builder response = dealWithReq.submitReportHandler(request.getUserID(), report, nonce);
		    responseObserver.onNext(response.build());
		    responseObserver.onCompleted();
	        
    	}catch (Exception e) {
			responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
		}

    }

    /**************************************************************************************
     *                                  - echoSubmitRequest()
     *  Triggered after server receives submitReport request and is considered valid
     *  - input:
     *      - echoMsg to broadcast to all servers
     * TODO: Echomsg enc/dec
     * ************************************************************************************/
    private void echoSubmitRequest(String echoMsg) {

		// Save information, that echo{report, sig} was sent (so duplications are not created)
		if (!echos.containsKey(echoMsg)) {
			ackDetail tmp = new ackDetail(false);
			echos.put(echoMsg, tmp);
		}

		ackDetail echoAck = echos.get(echoMsg);

		if (echoAck.isAckSent()) // Echo for this message has been already sent
			return;

    	// Broadcast echo message
		serverServiceStub serverAsyncStub;
		secureRequest echo;
		for(int server_id = 0; server_id < this.num_servers; server_id++) {
			serverAsyncStub = serverServiceGrpc.newStub(serverChannels.get(server_id))
											   .withWaitForReady();

			echo = secureRequest.newBuilder().setUserID(this.myId)
					 						 .setConfidentMessage(echoMsg)
											 .setMessageDigitalSignature("").build();
			serverAsyncStub.submitReportEcho(echo, null);
		}

		// Echo for the message has been sent.
		echoAck.setAckSent();
	}

	/******* // TODO: Add enc/dec
	 * An ECHO message was received from another server
	 * @param request {serverId, echoMessage, signedEchoMessage}
	 * @param responseObserver is not used. Does not wait for replies.
	 */
	@Override
	public void submitReportEcho(secureRequest request, StreamObserver<Empty> responseObserver) {

		int serverId = request.getUserID();
		String echoMsg = request.getConfidentMessage();

		if (!echos.containsKey(echoMsg)) {
			ackDetail tmp = new ackDetail(false);
			echos.put(echoMsg, tmp);
		}

		ackDetail echoAck = echos.get(echoMsg);
		echoAck.addId(serverId); // Store who sent it

		System.out.println("[Server" + this.myId + "] Received echo from Server" + serverId
				+ " cnt: " + echoAck.ackCount());

		// No quorum of that message yet
		if (echoAck.ackCount() <= this.quorum)
			return;

		// There is quorum of that message, broadcast Ready message
		String readyMsg = echoMsg;
		if (!readys.containsKey(readyMsg)) {
			ackDetail tmp = new ackDetail(false);
			readys.put(echoMsg, tmp);
		}

		ackDetail readyAck = readys.get(readyMsg);
		if (!readyAck.isAckSent())
			readySubmitRequest(readyMsg);

    }

	/**************************************************************************************
	 *                                  - readySubmitRequest()
	 *  Triggered after server receives quorum of echo messages
	 *  - input:
	 *      - readyMsg to broadcast to other server
	 * TODO: readyMsg enc/dec
	 * ************************************************************************************/
	private void readySubmitRequest(String readyMsg) {

		if (!readys.containsKey(readyMsg)) {
			ackDetail tmp = new ackDetail(false);
			readys.put(readyMsg, tmp);
		}

		ackDetail readyAck = readys.get(readyMsg);

		if (readyAck.isAckSent())
			return;

		serverServiceStub serverAsyncStub;
		secureRequest ready;
		for(int server_id = 0; server_id < this.num_servers; server_id++) {
			serverAsyncStub = serverServiceGrpc.newStub(serverChannels.get(server_id))
					.withWaitForReady();

			ready = secureRequest.newBuilder().setUserID(this.myId)
					.setConfidentMessage(readyMsg)
					.setMessageDigitalSignature("").build();
			serverAsyncStub.submitReportReady(ready, null);
		}

		readyAck.setAckSent();
	}

	/*** TODO: enc/dec
	 * A ready Message was received from another server
	 * @param request {serverId, readyMsg, sign(ReadyMsg)}
	 * @param responseObserver is not used; Does not wait to replies.
	 * When readyMsg is considered delivered, it unblocks submitLocationRequest()
	 */
	@Override
	public void submitReportReady(secureRequest request, StreamObserver<Empty> responseObserver) {
		Integer serverId = request.getUserID();
		String readyMsg = request.getConfidentMessage();

		if (!readys.containsKey(readyMsg)) {
			ackDetail tmp = new ackDetail(false);
			readys.put(readyMsg, tmp);
		}

		ackDetail readyAck = readys.get(readyMsg);
		readyAck.addId(serverId);


		System.out.println("[Server" + this.myId + "] Received ready from Server" + serverId
				+ " cnt: " + readyAck.ackCount());

		// Accept this message
		if (readyAck.ackCount() > 2*this.num_byzantines) {
			System.out.println("[Server" + this.myId + "] Delivered report");
			readyAck.getLatch().countDown();
		} else if (readyAck.ackCount() > this.num_byzantines && !readyAck.isAckSent()) {
			System.out.println("[Server" + this.myId + "] Amplification step");
			readySubmitRequest(readyMsg);
		}

	}

	/**************************************************************************************
     *                                  - obtainLocationReport()
     *  RPC: server received request from a user to provide user location in specific epoch.
     *  return only his own location history.
     *  This method handles the requests and sends a reply
     *  - input:
     *      - request: the obtainLocationReportRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
    @Override
    public void obtainLocationReport(secureRequest request, StreamObserver<secureReplay> responseObserver) {
    	try {
    		
		    String[] reqSplit = getfieldsFromSecureMessage(request.getUserID(), request.getConfidentMessage(), request.getMessageDigitalSignature());
		    int epoch = Integer.parseInt(reqSplit[1]);
		    int nonce = Integer.parseInt(reqSplit[2]);
		    ServerService.secureReplay.Builder response = dealWithReq.obtainReportHandler(request.getUserID(), epoch, nonce);
		    responseObserver.onNext(response.build());
		    responseObserver.onCompleted();
			
		} catch (Exception e) {
    		responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
		}
    }

    /**************************************************************************************
     *                                  - obtainLocationReportHA()
     *  RPC: server received request from the HA to provide user location in specific epoch
     *  History of any user can be returned.
     *  This method handles the requests and sends a reply
     *  - input:
     *      - request: the obtainLocationReportRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
   @Override
   public void obtainLocationReportHA(secureRequest request, StreamObserver<secureReplay> responseObserver) {
	   try {
		   if(request.getUserID() != 0)
			   throw new Exception("only HA user can execute this functionality");
		   String[] requestValues = getfieldsFromSecureMessage(0, request.getConfidentMessage(), request.getMessageDigitalSignature());
		   int nonce = Integer.parseInt(requestValues[2]);
		   int userID = Integer.parseInt(requestValues[0]);
		   int epoch = Integer.parseInt(requestValues[1]);
		   secureReplay.Builder response = dealWithReq.obtainLocationReportHAHandler(0,userID, epoch, nonce);
		   responseObserver.onNext(response.build());
		   responseObserver.onCompleted();
	   }catch (Exception e) {
		   secureReplay.Builder response = secureReplay.newBuilder().setOnError(true).
				   setErrormessage(e.toString()+""+e.getMessage());
		   responseObserver.onNext(response.build());
		   responseObserver.onCompleted();
	   }
   }

    /**************************************************************************************
     *                                  - obtainUsersAtLocation()
     *  RPC: server received request from HA to provide a list of users that were at specific
     *  time on specific location.
     *  - input:
     *      - request: the obtainUsersLocationRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
   @Override
   public void obtainUsersAtLocation(secureRequest request, StreamObserver<secureReplay> responseObserver) {
	   secureReplay.Builder response;
	   try {
		   if(request.getUserID() != 0)
			   throw new Exception("only HA user can execute this functionality");
		   String[] requestValues = getfieldsFromSecureMessage(0, request.getConfidentMessage(), request.getMessageDigitalSignature());
		   int requestEpoch = Integer.parseInt(requestValues[1]);
		   String position = requestValues[0].substring(1, 4);
		   int x = Integer.parseInt(position.substring(0,1));
		   int y = Integer.parseInt(position.substring(2,3));
		   Point2D requestPoint = new Point2D(x, y);
		   int nonce =Integer.parseInt(requestValues[2]);
		   response = dealWithReq.obtainUsersAtLocationHandler(0,requestPoint, requestEpoch, nonce);
		   responseObserver.onNext(response.build());
		   responseObserver.onCompleted();
			
	   }catch (Exception e) {
		   e.printStackTrace();
		response = secureReplay.newBuilder().setOnError(true)
												.setErrormessage(e.getMessage());
		responseObserver.onNext(response.build());
		   responseObserver.onCompleted();
	}
   }


   
    

    /**************************************************************************************
     * 											- dHKeyExchange()
     * -remote procedural call to exchange Diffie Helmann key between user and server 
     *  - input:
     *      - request: request of the user (contain public key(p^a mod g)
     *       	signed with user private key and big integers p and g)
     *      
     *
     * ************************************************************************************/
    @Override
    public void dHKeyExchange(DHKeyExcReq request, StreamObserver<DHKeyExcRep> responseObserver) {
    	try {   		
    		BigInteger p = DiffieHelman.read(request.getP());
    		BigInteger g = DiffieHelman.read(request.getG());
    		DiffieHelman df = new DiffieHelman(p, g);
			PublicKey key = TrackerLocationSystem.getUserPublicKey(request.getUserID());
			String userPbkDigSig = request.getDigSigPubKey();
			String userPubKey = request.getMyDHPubKey();
		 	Key secretKey = TrackerLocationSystem.createSecretKey(df, userPbkDigSig, userPubKey, key);
		 	dealWithReq.putOrUpdateSharedKeys(request.getUserID(), secretKey);
		 	PublicKey myPubkey = df.getPublicKey();
		 	String pbkB64 = Base64.getEncoder().encodeToString(myPubkey.getEncoded());
		 	String digSigMyDHpubkey = TrackerLocationSystem.getDHkeySigned(myPubkey, PRIVATE_KEY_PATH);
		 	
		 	DHKeyExcRep.Builder rep = DHKeyExcRep.newBuilder().setOnError(false).setDigSigPubkey(digSigMyDHpubkey)
		 			.setMyPubKey(pbkB64);
		 	
		 	responseObserver.onNext(rep.build());
		 	responseObserver.onCompleted();
		 	
		} catch (Exception e) {
			DHKeyExcRep.Builder rep = DHKeyExcRep.newBuilder().setOnError(true)
					.setErrorMessage(e.getMessage());
			responseObserver.onNext(rep.build());
		 	responseObserver.onCompleted();
		}
	
    }
    
    public String[] getfieldsFromSecureMessage(int userID, String secureMessage, String digsig) throws Exception {
		return getPlainText(userID, secureMessage, digsig).split(Pattern.quote("||"));
	}

	public String[] getfieldsFromMessage(String message) {
    	return message.split(Pattern.quote("||"));
	}

	public boolean verifySignature(int userID, String message, String signature) throws Exception {
    	PublicKey pubkey = TrackerLocationSystem.getUserPublicKey(userID);
    	return RSAProvider.istextAuthentic(message, signature, pubkey);
	}

	public String getPlainText(int userID, String ct) throws Exception {
    	Key sharedKey = dealWithReq.getSharedKey(userID);
    	PublicKey pubkey = TrackerLocationSystem.getUserPublicKey(userID);
    	return AESProvider.getPlainTextOfCipherText(ct, sharedKey);
	}

	public String getPlainText(int userID, String secureMessage, String digsig) throws Exception {
    	Key sharedKey = dealWithReq.getSharedKey(userID);
    	PublicKey pubkey = TrackerLocationSystem.getUserPublicKey(userID);
    	String messPlainText = AESProvider.getPlainTextOfCipherText(secureMessage, sharedKey);
    	boolean DigSigIsValid = RSAProvider.istextAuthentic(messPlainText, digsig, pubkey);

    	if (!DigSigIsValid)
    		throw new Exception("The message is not authentic");

    	return messPlainText;
	}

}
