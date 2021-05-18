package user;

import java.io.File;
import java.security.*;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.ini4j.Ini;

import com.google.gson.JsonObject;
import com.server.grpc.serverServiceGrpc;
import com.server.grpc.ServerService.BInteger;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.serverServiceGrpc.serverServiceBlockingStub;

import crypto.AESProvider;
import crypto.RSAProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import shared.DiffieHelman;
import shared.TrackerLocationSystem;

public class User {
	
	
	private  String PRIVATE_KEY_PATH;
	private Key [] sharedKey;
	protected int myID;
	private int N_timesSharedKeyUsed;
	private int num_servers;
	private String type;
	
	public User(int ID, String type) throws Exception {
		this.type = type;
		this.myID = ID;
		PRIVATE_KEY_PATH = "resources/private_keys/"+ type +""+ myID + "_private.key";
		verifyKeys(myID, type);
		this.num_servers = new Ini(new File("variables.ini")).get("Server","number_of_servers", Integer.class);
		this.sharedKey = new Key [num_servers]; 
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
		PrivateKey privkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH);
		return RSAProvider.getTexthashEnWithPriKey(message, privkey);
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
		PrivateKey myprivkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH);
		if(this.sharedKey[serverID] == null || N_timesSharedKeyUsed % 5 == 0 ) {
			int serverPort = new Ini(new File("variables.ini")).get("Server","server_start_port", Integer.class);
			this.sharedKey[serverID] = DHkeyExchange(serverID, serverPort + serverID);
		}
		JsonObject cipherReq  = TrackerLocationSystem.getSecureText(this.sharedKey[serverID], myprivkey, message);
		N_timesSharedKeyUsed++;
		return cipherReq;
	}
	
	public String[] getfieldsFromSecureMessage(int serverID ,String secureMessage, String digsig ) throws Exception {
		PublicKey pubkey = TrackerLocationSystem.getServerPublicKey(serverID);
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
		String digSigMyDHpubkey = TrackerLocationSystem.getDHkeySigned(dfPubKey, PRIVATE_KEY_PATH);	
		BInteger p = DiffieHelman.write(df.getP());
		BInteger g = DiffieHelman.write(df.getG());
		
		DHKeyExcReq req = DHKeyExcReq.newBuilder().setP(p).setG(g).setMyDHPubKey(pbkB64)
				.setUserType(this.type).setDigSigPubKey(digSigMyDHpubkey).setUserID(myID).build();
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", serverPort).usePlaintext().build();
		serverServiceBlockingStub serverStub = serverServiceGrpc.newBlockingStub(channel);
		
		DHKeyExcRep rep = serverStub.dHKeyExchange(req);
		PublicKey key = TrackerLocationSystem.getServerPublicKey(serverID);
		String servPbkDigSig = rep.getDigSigPubkey();
		String servPubKey = rep.getMyPubKey();
		channel.shutdown();
		return TrackerLocationSystem.createSecretKey(df, servPbkDigSig, servPubKey, key);	
	}
	
	public void verifyKeys(int id, String serverOrUser) throws Exception {
		String pubkeypath = "resources/public_keys/"+serverOrUser +"" + id +"_public.key";
		String privKeyPath = "resources/private_keys/"+serverOrUser +"" + id +"_private.key";	
		try {
			KeyPair mykeypair = RSAProvider.readRSAKey(pubkeypath, privKeyPath);
		} catch (Exception e) {
			RSAProvider.RSAKeyGenerator(privKeyPath, pubkeypath);
		}
	}

	public int hashCash(String message) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
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


}