package server;

import com.google.gson.JsonObject;
import com.google.protobuf.Empty;
import com.server.grpc.ServerService;
import com.server.grpc.ServerService.BInteger;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.ServerService.DHKeyExcServerReq;
import com.server.grpc.ServerService.secureReplay;
import com.server.grpc.ServerService.secureRequest;
import com.server.grpc.serverServiceGrpc;
import com.server.grpc.serverServiceGrpc.serverServiceStub;
import com.server.grpc.serverServiceGrpc.serverServiceBlockingStub;
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
//import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;



public class ServerImp extends serverServiceImplBase {

    private  String PRIVATE_KEY_PATH;
    private DealWithRequest dealWithReq;
   // private Key [] sharedKey;
    //private int N_timesSharedKeyUsed;
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
    
    private Hashtable<Integer, List<Object>> sharedKey;
    private int N_timesSharedKeyUsed;

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

		//this.sharedKey = new Key [num_servers];
		this.serverChannels = new ArrayList<>();

		this.echos = new Hashtable<>();
		this.readys = new Hashtable<>();

		this.rep_sig_delimiter = "|<<<|";
		
		//Security BRB
		this.sharedKey = new Hashtable<>();
		this.N_timesSharedKeyUsed = new Ini(new File("variables.ini")).get("Server","max_key_usage", Integer.class);
	}
    
    public int getN_timesSharedKeyUsed() {
		return N_timesSharedKeyUsed;
	}
    
    public void incrementUsage(int serverID) {
    	int current = Integer.valueOf((String)this.sharedKey.get(serverID).get(1));
    	this.sharedKey.get(serverID).set(0, current+1);
    }
    
    public int getKeyUsage(int serverID) {
    	return Integer.valueOf((String)this.sharedKey.get(serverID).get(1));
    }
    
    public Key getSharedKey(int serverID) {
    	return (Key) this.sharedKey.get(serverID);
    }
    
    //Editing
    public String signMessage(String message) throws Exception {
		PrivateKey privkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH, TrackerLocationSystem.password);
		return RSAProvider.getTexthashEnWithPriKey(message, privkey);
	}
    
    //Editing
    public String encryptMessage(int serverID, String message) throws Exception {
		if(getSharedKey(serverID) == null || getKeyUsage(serverID) < N_timesSharedKeyUsed) {
			List <Object> tmp = new ArrayList <>();
			tmp.add(DHkeyExchange(serverID, server_start_port + serverID));
			tmp.add(0);
			this.sharedKey.put(serverID, tmp);
		}
		incrementUsage(serverID);
		return AESProvider.getCipherOfPlainText(message, (Key) sharedKey.get(serverID).get(0));
	}
	
    //Editing
	public JsonObject getsecureMessage(int serverID, String message) throws Exception {
		PrivateKey myprivkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH, TrackerLocationSystem.password);
		if(getSharedKey(serverID) == null || getKeyUsage(serverID) < N_timesSharedKeyUsed) {
			List <Object> tmp = new ArrayList <>();
			tmp.add(DHkeyExchange(serverID, server_start_port + serverID));
			tmp.add(0);
			this.sharedKey.put(serverID, tmp);
		}
		JsonObject cipherReq  = TrackerLocationSystem.getInstance().getSecureText(getSharedKey(serverID), myprivkey, message);
		incrementUsage(serverID);
		return cipherReq;
	}
	
	//Editing
	public String[] getfieldsFromSecureMessage(int serverID, String secureMessage, String digsig) throws Exception {
		PublicKey pubkey = TrackerLocationSystem.getInstance().getServerPublicKey(serverID);
		String messPlainText = AESProvider.getPlainTextOfCipherText(secureMessage, getSharedKey(serverID));
		boolean DigSigIsValid = RSAProvider.istextAuthentic(messPlainText, digsig, pubkey);
		if(DigSigIsValid) {
			String[] requestValues = messPlainText.split(Pattern.quote("||"));
			return requestValues;
		}else {
			throw new Exception("ECHO WARNING: the message is not authentic");
		}
	}
	//Editing
	public Key DHkeyExchange(int serverID, int serverPort) throws Exception {
		DiffieHelman df = new DiffieHelman();
		PublicKey dfPubKey = df.getPublicKey();
		String pbkB64 = Base64.getEncoder().encodeToString(dfPubKey.getEncoded());
		String digSigMyDHpubkey = TrackerLocationSystem.getInstance().getDHkeySigned(dfPubKey, PRIVATE_KEY_PATH);	
		BInteger p = DiffieHelman.write(df.getP());
		BInteger g = DiffieHelman.write(df.getG());
		
		DHKeyExcServerReq req = DHKeyExcServerReq.newBuilder().setServerID(this.myId).setP(p).setG(g).setMyDHPubKey(pbkB64)
				.setDigSigPubKey(digSigMyDHpubkey).build();
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", serverPort).usePlaintext().build();
		serverServiceBlockingStub serverStub = serverServiceGrpc.newBlockingStub(channel);
		
		DHKeyExcRep rep = serverStub.dHKeyExchangeServer(req);
		PublicKey key = TrackerLocationSystem.getInstance().getServerPublicKey(serverID);
		String servPbkDigSig = rep.getDigSigPubkey();
		String servPubKey = rep.getMyPubKey();
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
		return TrackerLocationSystem.getInstance().createSecretKey(df, servPbkDigSig, servPubKey, key);	
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
			int nonce = Integer.parseInt(message[message.length - 1]);
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
			///////System.out.println(readys.get(echoMsg).ackCount());
			echoReady.getLatch().await(10, TimeUnit.SECONDS);
			if(echoReady.ackCount() < quorum) {
				System.out.println("ECHO: [Server" + this.myId + "] Did not receive quorum");
			}

			////////System.out.println(echos.get(echoMsg).ackCount());
			if(!(echos.get(echoMsg).ackCount() <= quorum || readys.get(echoMsg).ackCount() <= 2*this.num_byzantines)) {
				/////////System.out.println("DENTRO:" + readys.get(echoMsg).ackCount());
				// Message is considered delivered, time to add it to DB
				ServerService.secureReplay.Builder response = dealWithReq.submitReportHandler(request.getUserID(), report, nonce);
				responseObserver.onNext(response.build());
				responseObserver.onCompleted();
			}



    	}catch (Exception e) {
    		System.out.println(e.getMessage());
    		responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    	}

    }

    /**************************************************************************************
     *                                  - echoSubmitRequest()
     *  Triggered after server receives submitReport request and is considered valid
     *  - input:
     *      - echoMsg to broadcast to all servers
     * TODO: Echomsg enc/dec
     * @throws Exception 
     * ************************************************************************************/
    private void echoSubmitRequest(String echoMsg) throws Exception {

		// Save information, that echo{report, sig} was sent (so duplications are not created)
		if (!echos.containsKey(echoMsg)) {
			ackDetail tmp = new ackDetail(false);
			echos.put(echoMsg, tmp);
		}

		ackDetail echoAck = echos.get(echoMsg);

		if (echoAck.isAckSent()) // Echo for this message has been already sent
			return;

		if (this.serverChannels.isEmpty()) {
			for(int server_id = 0; server_id < this.num_servers; server_id++) {
				ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", server_start_port+server_id)
					.usePlaintext().build();
				serverChannels.add(channel); // Store it for reuse
			}
		}

    	// Broadcast echo message
		serverServiceStub serverAsyncStub;
		secureRequest echo;
		//String signedMessage = signMessage(echoMsg); //######
		for(int server_id = 0; server_id < this.num_servers; server_id++) {
			//String encryptedMessage = encryptMessage(server_id, echoMsg);
			serverAsyncStub = serverServiceGrpc.newStub(serverChannels.get(server_id))
											   .withWaitForReady();

			echo = secureRequest.newBuilder().setUserID(this.myId)
					 						 .setConfidentMessage(echoMsg)
											 .setMessageDigitalSignature("").build();
			//System.out.println("HEYYYYY" + echo); //#######
			serverAsyncStub.submitReportEcho(echo, ignore);
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
		
		if(!echos.get(echoMsg).serverIds.contains(serverId)) { //Prevent Malicious servers from sending multiple
			echoAck.addId(serverId); // Store who sent it
			System.out.println("ECHO: [Server" + this.myId + "] Received echo from Server" + serverId
					+ " count: " + echoAck.ackCount());
		}
		else {
			System.out.println("ECHO: [Server" + this.myId + "] Server" + serverId
					+ " already sent echo, count: " + echoAck.ackCount());
		}
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

		if (this.serverChannels.isEmpty()) {
			for(int server_id = 0; server_id < this.num_servers; server_id++) {
				ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", server_start_port+server_id)
					.usePlaintext().build();
				serverChannels.add(channel); // Store it for reuse
			}
		}

		serverServiceStub serverAsyncStub;
		secureRequest ready;
		for(int server_id = 0; server_id < this.num_servers; server_id++) {
			serverAsyncStub = serverServiceGrpc.newStub(serverChannels.get(server_id))
					.withWaitForReady();

			ready = secureRequest.newBuilder().setUserID(this.myId)
					.setConfidentMessage(readyMsg)
					.setMessageDigitalSignature("").build();
			serverAsyncStub.submitReportReady(ready, ignore);
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


		System.out.println("READY [Server" + this.myId + "] Received ready from Server" + serverId
				+ " count: " + readyAck.ackCount());

		// Accept this message
		if (readyAck.ackCount() > 2*this.num_byzantines) {
			System.out.println("READY [Server" + this.myId + "] Achieved reports > 2f");
			readyAck.getLatch().countDown();
		} else if (readyAck.ackCount() > this.num_byzantines && !readyAck.isAckSent()) {
			System.out.println("READY [Server" + this.myId + "] Amplification step");
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
    		String openText = getPlainText("user", request.getUserID(), request.getConfidentMessage());
			String[] messageFields = getfieldsFromMessage(openText);

			int epoch = Integer.parseInt(messageFields[0]);
			int listenerPort = Integer.parseInt(messageFields[1]);
			int nonce = Integer.parseInt(messageFields[messageFields.length - 1]);
			

		    ServerService.secureReplay.Builder response = dealWithReq.obtainReportHandler(request.getUserID(), epoch, nonce, request.getMessageDigitalSignature(), listenerPort);
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
		   String[] requestValues = getfieldsFromSecureMessage(request.getUserID(), request.getConfidentMessage(), request.getMessageDigitalSignature(), "HA");
		   int userID = Integer.parseInt(requestValues[0]);
		   int epoch = Integer.parseInt(requestValues[1]);
		   int listenerPort = Integer.parseInt(requestValues[2]);
		   int nonce = Integer.parseInt(requestValues[requestValues.length -1]);
		   secureReplay.Builder response = dealWithReq.obtainLocationReportHAHandler(request.getUserID(),userID, epoch, nonce,listenerPort);
		   responseObserver.onNext(response.build());
		   responseObserver.onCompleted();
	   }catch (Exception e) {
		   e.printStackTrace();
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
		   String[] requestValues = getfieldsFromSecureMessage(request.getUserID(), request.getConfidentMessage(), request.getMessageDigitalSignature(), "HA");
		   int requestEpoch = Integer.parseInt(requestValues[1]);
		   String position = requestValues[0].substring(1, 4);
		   int x = Integer.parseInt(position.substring(0,1));
		   int y = Integer.parseInt(position.substring(2,3));
		   Point2D requestPoint = new Point2D(x, y);
		   int nonce =Integer.parseInt(requestValues[2]);
		   response = dealWithReq.obtainUsersAtLocationHandler(request.getUserID(),requestPoint, requestEpoch, nonce);
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

   @Override
   public void readDone(secureRequest request, StreamObserver<Empty> responseObserver) {
	   String[] requestValues;
	try {
		requestValues = getfieldsFromSecureMessage(request.getUserID(), request.getConfidentMessage(), request.getMessageDigitalSignature(), request.getUserType());
		int userID = Integer.parseInt(requestValues[0]);
		int epoch = Integer.parseInt(requestValues[1]);
		dealWithReq.removeListener(request.getUserType(), request.getUserID(), userID, epoch);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
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
			PublicKey key = TrackerLocationSystem.getInstance().getUserPublicKey(request.getUserID(), request.getUserType());
			String userPbkDigSig = request.getDigSigPubKey();
			String userPubKey = request.getMyDHPubKey();
		 	Key secretKey = TrackerLocationSystem.getInstance().createSecretKey(df, userPbkDigSig, userPubKey, key);
		 	dealWithReq.putOrUpdateSharedKeys(request.getUserType(), request.getUserID(), secretKey);
		 	PublicKey myPubkey = df.getPublicKey();
		 	String pbkB64 = Base64.getEncoder().encodeToString(myPubkey.getEncoded());
		 	String digSigMyDHpubkey = TrackerLocationSystem.getInstance().getDHkeySigned(myPubkey, PRIVATE_KEY_PATH);
		 	
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

	@Override
	public void requestMyProofs(secureRequest request, StreamObserver<secureReplay> responseObserver) {
		try {

			String openText = getPlainText("user", request.getUserID(), request.getConfidentMessage());
			String[] messageFields = getfieldsFromMessage(openText);

			String epochs = messageFields[1];
			int nonce = Integer.parseInt(messageFields[2]);

		    ServerService.secureReplay.Builder response = dealWithReq.myProofs(request.getUserID(), epochs, nonce, request.getMessageDigitalSignature());
		    responseObserver.onNext(response.build());
		    responseObserver.onCompleted();

		} catch (Exception e) {
    		responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
		}
	}



	public String[] getfieldsFromSecureMessage(int userID, String secureMessage, String digsig, String usertype) throws Exception {
		return getPlainText(userID, secureMessage, digsig, usertype).split(Pattern.quote("||"));
	}

	public String[] getfieldsFromMessage(String message) {
    	return message.split(Pattern.quote("||"));
	}

	public boolean verifySignature(int userID, String message, String signature) throws Exception {
    	PublicKey pubkey = TrackerLocationSystem.getInstance().getUserPublicKey(userID, "user");
    	return RSAProvider.istextAuthentic(message, signature, pubkey);
	}

	public String getPlainText(String userType, int userID, String ct) throws Exception {
    	Key sharedKey = dealWithReq.getSharedKey(userType,userID);
    	PublicKey pubkey = TrackerLocationSystem.getInstance().getUserPublicKey(userID, "user");
    	return AESProvider.getPlainTextOfCipherText(ct, sharedKey);
	}

	public String getPlainText(int userID, String secureMessage, String digsig, String usertype) throws Exception {
    	Key sharedKey = dealWithReq.getSharedKey(usertype, userID);
    	PublicKey pubkey = TrackerLocationSystem.getInstance().getUserPublicKey(userID, usertype);
    	String messPlainText = AESProvider.getPlainTextOfCipherText(secureMessage, sharedKey);
    	boolean DigSigIsValid = RSAProvider.istextAuthentic(messPlainText, digsig, pubkey);

    	if (!DigSigIsValid)
    		throw new Exception("The message is not authentic");

    	return messPlainText;
	}

	private StreamObserver<Empty> ignore = new StreamObserver<Empty>() {
		@Override public void onNext(Empty empty) {}
		@Override public void onError(Throwable throwable) { }
		@Override public void onCompleted() { }
	};

}