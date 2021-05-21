package user;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.ini4j.Ini;
import com.server.grpc.ServerService.secureRequest;
import com.google.gson.JsonObject;
import com.google.protobuf.Empty;
import com.server.grpc.serverServiceGrpc;
import com.server.grpc.ServerService.BInteger;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.serverServiceGrpc.serverServiceBlockingStub;
import com.user.grpc.ListenerServiceGrpc.ListenerServiceImplBase;

import crypto.AESProvider;
import crypto.RSAProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import shared.DiffieHelman;
import shared.TrackerLocationSystem;

public class User extends ListenerServiceImplBase {
	
	
	private  String PRIVATE_KEY_PATH;
	private Key [] sharedKey;
	private int myID;
	private int N_timesSharedKeyUsed;
	//protected int num_servers;
	private String type;
	private Map<Integer, String> answers = new HashMap<>();
	private int myListenerPort;
	//protected int server_start_port;
	//protected int num_byzantines;
	protected int quorum;


	
	public User(int ID, String type) throws Exception {
		this.type = type;
		this.myID = ID;
		PRIVATE_KEY_PATH = "resources/private_keys/"+ type +""+ myID + "_private.key";
		verifyKeys(myID, type);
		int num_servers = TrackerLocationSystem.getInstance().getNumServers();
		this.sharedKey = new Key [num_servers];
		setListenerPort(type, ID);
		int num_byzantines = TrackerLocationSystem.getInstance().getNumBizantineServers();
		this.quorum = (num_servers+num_byzantines)/2;
		startListenerServer(this);
	}
	
	public void setListenerPort(String type, int id) throws Exception {
		String typeOfPort;
		int listenerStartPort;
		if(type.equals("user"))
			typeOfPort = "user_listener_start_port";
		else
			typeOfPort = "HA_listener_start_port";
		listenerStartPort = new Ini(new File("variables.ini")).get("UserSpecs",typeOfPort, Integer.class); 
		this.myListenerPort = listenerStartPort + id;
	}
	
	public int getMyListenerPort() {
		return this.myListenerPort;
	}
	
	public Key getSharedKey(int server_id) {
		return sharedKey[server_id];
	}

	public void setSharedKey(Key sharedKey, int server_id) {
		this.sharedKey[server_id] = sharedKey;
	}

	public int getMyID() {
		return myID;
	}

	public void setMyID(int myID) {
		this.myID = myID;
	}

	public String getPRIVATE_KEY_PATH() {
		return PRIVATE_KEY_PATH;
	}
	public int getN_timesSharedKeyUsed() {
		return N_timesSharedKeyUsed;
	}

	public String signMessage(String message) throws Exception {
		PrivateKey privkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH, TrackerLocationSystem.password);
		return RSAProvider.getTexthashEnWithPriKey(message, privkey);
	}
	
	public synchronized void putValuesOnAnswers(int serverID, String value) {
		System.out.println("server ID = " + serverID + ", value: " + value);
		if(answers.get(serverID) == null)
				answers.put(serverID, value);
		else
			answers.replace(serverID, value);
		if(getMajorityFromAnswers().getValue() > this.quorum)
			notifyAll();
	}
	
	public Map.Entry<String, Integer> getMajorityFromAnswers() {
		Map<String, Integer> values_count = new HashMap<>();
		for(Map.Entry<Integer, String> entry : answers.entrySet()) {
			if(values_count.get(entry.getValue()) == null)
				values_count.put(entry.getValue(), 0);
			
			values_count.put(entry.getValue(), values_count.get(entry.getValue()) + 1);
		}
		Map.Entry<String, Integer> resul =  values_count.entrySet().iterator().next();
		int max = 0;
		for (Map.Entry<String, Integer> entry : values_count.entrySet()) {
			if(entry.getValue() > max) {
				max = entry.getValue();
				resul = entry;
			}
		}
		
		return resul;
	}
	
	public synchronized String readAtomicValue(int userID, int epoch) throws Exception {
		while(getMajorityFromAnswers().getValue() <= this.quorum) {
			System.out.println("waiting for receiving enough answers: majority = " + getMajorityFromAnswers().toString() + ", quorum = " + this.quorum);
			wait();
		}
		String value = getMajorityFromAnswers().getKey();
		answers.clear();
		readDone(userID, epoch);
		return value;
	}

	private void readDone( int userID, int epoch) throws Exception {
		List<ManagedChannel> serverChannels = new ArrayList<>();
		serverServiceGrpc.serverServiceStub serverAsyncStub;
		int num_servers = TrackerLocationSystem.getInstance().getNumServers();
		for(int server_id = 0; server_id < num_servers; server_id++) {
			String message = userID + "||" + epoch;
			JsonObject securemessage = getsecureMessage(server_id, message);
			String messagecipher = securemessage.get("ciphertext").getAsString();
			String messageDigSig = securemessage.get("textDigitalSignature").getAsString();
			int serverPort = TrackerLocationSystem.getInstance().getMyServerPort(server_id);
			ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", serverPort)
                    .usePlaintext().build();
			serverChannels.add(channel); // Store it for a proper close later
			serverAsyncStub = serverServiceGrpc.newStub(channel).withDeadlineAfter(10, TimeUnit.SECONDS)
																.withWaitForReady();
			secureRequest  request = secureRequest.newBuilder().setUserType(this.type)
					.setUserID(getMyID())
	  				.setConfidentMessage(messagecipher)
	  				.setMessageDigitalSignature(messageDigSig).build();
			serverAsyncStub.readDone(request, ignore);
		}
		for(ManagedChannel channel : serverChannels)
			channel.shutdown();
	}

	public String encryptMessage(int serverID, String message) throws Exception {
		if(this.sharedKey[serverID] == null || N_timesSharedKeyUsed % 5 == 0 ) {
			int serverPort = new Ini(new File("variables.ini")).get("Server","server_start_port", Integer.class);
			this.sharedKey[serverID] = DHkeyExchange(serverID, serverPort + serverID);
		}
		N_timesSharedKeyUsed++;
		return AESProvider.getCipherOfPlainText(message, sharedKey[serverID]);
	}
	
	public JsonObject getsecureMessage(int serverID, String message) throws Exception {
		PrivateKey myprivkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH, TrackerLocationSystem.password);
		if(this.sharedKey[serverID] == null || N_timesSharedKeyUsed % 5 == 0 ) {
			int serverPort = new Ini(new File("variables.ini")).get("Server","server_start_port", Integer.class);
			this.sharedKey[serverID] = DHkeyExchange(serverID, serverPort + serverID);
		}
		JsonObject cipherReq  = TrackerLocationSystem.getInstance().getSecureText(this.sharedKey[serverID], myprivkey, message);
		N_timesSharedKeyUsed++;
		return cipherReq;
	}
	
	public String[] getfieldsFromSecureMessage(int serverID ,String secureMessage, String digsig ) throws Exception {
		PublicKey pubkey = TrackerLocationSystem.getInstance().getServerPublicKey(serverID);
		String messPlainText = AESProvider.getPlainTextOfCipherText(secureMessage, sharedKey[serverID]);
		boolean DigSigIsValid = RSAProvider.istextAuthentic(messPlainText, digsig, pubkey);
		if(DigSigIsValid) {
			String[] requestValues = messPlainText.split(Pattern.quote("||"));
			return requestValues;
		}else {
			throw new Exception("the message is not authentic");
		}
	}

	/**************************************************************************************
	* 											-get_sig_of()
	* - return the hash of the string "s" ciphered with private key of
	*   the user(digital signature of string s) 
	* 
	* ************************************************************************************/
	/*public String get_sig_of(String s) throws Exception {
		PrivateKey priv = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH);
		String sig = RSAProvider.getTexthashEnWithPriKey(s, priv);
		return sig;
	}*/
	
	/**************************************************************************************
	* 											-DHkeyExchange()
	* -creates diffie helmann parameters (p, g and p^a mod g) signe
	*  it with its private key and sends it to the server and wait for server response.
	*  once it gets server reply it validate the signature of the server DH public key(p^b mod g)
	*  and compute the secret key.
	*  
	* -return: secret key that client and server agree on.
	* 
	* ************************************************************************************/
	public Key  DHkeyExchange(int serverID, int serverPort) throws Exception {
		DiffieHelman df = new DiffieHelman();
		PublicKey dfPubKey = df.getPublicKey();
		String pbkB64 = Base64.getEncoder().encodeToString(dfPubKey.getEncoded());
		String digSigMyDHpubkey = TrackerLocationSystem.getInstance().getDHkeySigned(dfPubKey, PRIVATE_KEY_PATH);	
		BInteger p = DiffieHelman.write(df.getP());
		BInteger g = DiffieHelman.write(df.getG());
		
		DHKeyExcReq req = DHKeyExcReq.newBuilder().setP(p).setG(g).setMyDHPubKey(pbkB64)
				.setUserType(this.type).setDigSigPubKey(digSigMyDHpubkey).setUserID(myID).build();
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", serverPort).usePlaintext().build();
		serverServiceBlockingStub serverStub = serverServiceGrpc.newBlockingStub(channel);
		
		DHKeyExcRep rep = serverStub.dHKeyExchange(req);
		PublicKey key = TrackerLocationSystem.getInstance().getServerPublicKey(serverID);
		String servPbkDigSig = rep.getDigSigPubkey();
		String servPubKey = rep.getMyPubKey();
		channel.shutdown();
		return TrackerLocationSystem.getInstance().createSecretKey(df, servPbkDigSig, servPubKey, key);	
	}
	
	public void verifyKeys(int id, String serverOrUser) throws Exception {
		String pubkeypath = "resources/public_keys/"+serverOrUser +"" + id +"_public.key";
		String privKeyPath = "resources/private_keys/"+serverOrUser +"" + id +"_private.key";	
		try {
			KeyPair mykeypair = RSAProvider.readRSAKey(pubkeypath, privKeyPath, TrackerLocationSystem.password);
		} catch (Exception e) {
			System.out.println(e);
			RSAProvider.RSAKeyGenerator(privKeyPath, pubkeypath, TrackerLocationSystem.password);
		}
	}

	public int hashCash(String message) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		String cash;
		byte[] hash;
		int counter = new Random().nextInt();

		cash = message + counter;
		while(true) {
			messageDigest.update(cash.getBytes());
			hash = messageDigest.digest();
			if ( hash[0] == 0 && hash[1] == 0 && hash[2] >>> 4 == 0 )
				return counter;
			cash = message + (++counter);
		}
	}
	
	/**************************************************************************************
	* 											-informAboutNewWrite()
	* - user listener function to receive new value from server when the
	*  user doesn't finish the write.(inform user about new value written about some user at epoch). 
	* 
	* ************************************************************************************/
	@Override
	public void informAboutNewWrite(com.user.grpc.Listener.secureRequest request, StreamObserver<Empty> responseObserver) {
		try {
			int serverID = request.getServerID();
			String[] requestValues = getfieldsFromSecureMessage(request.getServerID(), request.getConfidentMessage(), request.getMessageDigitalSignature());
			String waitingPoint = requestValues[0];
			putValuesOnAnswers(serverID, waitingPoint);	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startListenerServer(ListenerServiceImplBase listenerService) {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				io.grpc.Server listenerServer = ServerBuilder.forPort(getMyListenerPort()).
							addService(listenerService).build();
				try {
					listenerServer.start();
					System.out.println("user id = " + getMyID() + ": listener start at port : " + getMyListenerPort());
					listenerServer.awaitTermination();
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(r).start();
	}
	
	
	private StreamObserver<Empty> ignore = new StreamObserver<Empty>() {
		@Override public void onNext(Empty empty) {}
		@Override public void onError(Throwable throwable) { }
		@Override public void onCompleted() { }
	};

}