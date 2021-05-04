package user;

import com.server.grpc.ServerService.BInteger;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.ServerService.Position;
import com.server.grpc.ServerService.obtUseLocHARep;
import com.server.grpc.ServerService.obtUseLocHAReq;
import com.server.grpc.ServerService.usersLocationRep;
import com.server.grpc.ServerService.usersLocationReq;
import com.google.gson.JsonObject;
import com.server.grpc.serverServiceGrpc;
import com.server.grpc.serverServiceGrpc.serverServiceBlockingStub;

import crypto.RSAProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.netty.util.ThreadDeathWatcher;
import shared.DiffieHelman;
import shared.Point2D;
import shared.TrackerLocationSystem;

import java.io.File;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import org.ini4j.Ini;

public class HA {

    private serverServiceBlockingStub serverStub;
    private ManagedChannel channel;
    private final String PRIVATE_KEY_PATH = "resources/private_keys/user0_private.key" ;
    private Key sharedKey;
    private final int myID = 0;

    /**************************************************************************************
     * 											-HA class constructor()
     * -
     *
     * ************************************************************************************/
    public HA(int serverPort) throws Exception {
    	channel =  ManagedChannelBuilder.forAddress("127.0.0.1", serverPort).usePlaintext().build();
        serverStub = serverServiceGrpc.newBlockingStub(channel).withWaitForReady();
    }

    /**************************************************************************************
     * 											-obtainLocationReport()
     *  returns the position of specific user at specific epoch
     *  - input:
     *      - userId: ID of user to check
     *      - epoch: epoch to check
     * @throws Exception 
     *
     * ************************************************************************************/
    public Point2D obtainLocationReport(int userId, int epoch) throws Exception {
    	int myNonce = new Random().nextInt();
    	PrivateKey prvkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH);
		String message = userId + "||" + epoch +"||" + myNonce; 
		if(sharedKey == null) {
			int serverPort = new Ini(new File("variables.ini")).get("Server","server_port", Integer.class);
			sharedKey = DHkeyExchange(serverPort);
		}	
		JsonObject secureReport = TrackerLocationSystem.getSecureText(sharedKey, prvkey, message);
		String secureMessage = secureReport.get("ciphertext").getAsString();
		String messDigSig = secureReport.get("textDigitalSignature").getAsString();
    	obtUseLocHAReq.Builder reportRequest = obtUseLocHAReq.newBuilder().setSecureRequest(secureMessage)
    			.setDigitalSignature(messDigSig);
    	obtUseLocHARep reply = serverStub.obtainLocationReportHA(reportRequest.build());
    	if(!reply.getOnError())
    		return new Point2D(reply.getPosition().getX(), reply.getPosition().getY());
    	else
    		throw new Exception(reply.getErrormessage());
    }

    /**************************************************************************************
     * 											-obtainUsersAtLocation()
     *  returns list of users that were at specific position at specific epoch
     *  - input:
     *      - position: Position to search
     *      - epoch: epoch to search
     * @throws Exception 
     *
     * ************************************************************************************/
    public String obtainUsersAtLocation(Point2D position, int epoch) throws Exception {
    	int myNonce = new Random().nextInt();
    	PrivateKey prvkey = RSAProvider.readprivateKeyFromFile(PRIVATE_KEY_PATH);
		String message = position.toString() + "||" + epoch +"||" + myNonce; 
		if(sharedKey == null) {
			int serverPort = new Ini(new File("variables.ini")).get("Server","server_port", Integer.class);
			sharedKey = DHkeyExchange(serverPort);
		}	
		JsonObject secureReport = TrackerLocationSystem.getSecureText(sharedKey, prvkey, message);
		String secureMessage = secureReport.get("ciphertext").getAsString();
		String messDigSig = secureReport.get("textDigitalSignature").getAsString();
    	
        usersLocationReq locationRequest = usersLocationReq.newBuilder().setSecureRequest(secureMessage).setDigitalSignature(messDigSig).build();
        usersLocationRep reply = serverStub.obtainUsersAtLocation(locationRequest);
        if(reply.getOnError()) {
        	throw new Exception(reply.getErrorMessage());
        }
        return reply.getUsersList();
    }
    
    public void closeChannel() {
    	channel.shutdown();
    }
    
    public Key  DHkeyExchange(int serverPort) throws Exception {
		DiffieHelman df = new DiffieHelman();
		PublicKey dfPubKey = df.getPublicKey();
		String pbkB64 = Base64.getEncoder().encodeToString(dfPubKey.getEncoded());
		String digSigMyDHpubkey = TrackerLocationSystem.getDHkeySigned(dfPubKey, PRIVATE_KEY_PATH);	
		BInteger p =DiffieHelman.write(df.getP());
		BInteger g =DiffieHelman.write(df.getG());
		
		DHKeyExcReq req = DHKeyExcReq.newBuilder().setP(p).setG(g).setMyDHPubKey(pbkB64)
				.setDigSigPubKey(digSigMyDHpubkey).setUserID(myID).build();
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", serverPort).usePlaintext().build();
		serverServiceBlockingStub serverStub = serverServiceGrpc.newBlockingStub(channel);
		
		DHKeyExcRep rep = serverStub.dHKeyExchange(req);
		String servPubKeyPath = "resources/public_keys/server_public.key";
		PublicKey key = RSAProvider.readpublicKeyFromFile(servPubKeyPath);
		String servPbkDigSig = rep.getDigSigPubkey();
		String servPubKey = rep.getMyPubKey();
		channel.shutdown();
		return TrackerLocationSystem.createSecretKey(df, servPbkDigSig, servPubKey, key);	
	}

    public static void main(String[] args) throws Exception {
        String help = "Accept only following formats:\n\tgetReport <ID> <epoch>\n\tgetUsers <X> <Y> <epoch>";
        HA userHa = new HA(Integer.parseInt(args[0]));
        String cmd, arg1, arg2, arg3;
        Scanner sn = new Scanner(System.in);
        System.out.println(help);
        while(true) {
            cmd = sn.next().toLowerCase(Locale.ROOT);
            if (cmd.equals("getreport")) {
                arg1 = sn.next().toLowerCase(Locale.ROOT);
                arg2 = sn.next().toLowerCase(Locale.ROOT);
                try {
                    Point2D reply = userHa.obtainLocationReport(Integer.parseInt(arg1), Integer.parseInt(arg2));
                    System.out.println("Server replied: " + reply.toString());
                } catch (Exception e) {
                	System.out.println(e.getMessage());
                }
            } else if (cmd.equals("getusers")) {
                arg1 = sn.next().toLowerCase(Locale.ROOT);
                arg2 = sn.next().toLowerCase(Locale.ROOT);
                arg3 = sn.next().toLowerCase(Locale.ROOT);
                try {
                    Point2D pos = new Point2D(Integer.parseInt(arg1), Integer.parseInt(arg2));
                    String reply = userHa.obtainUsersAtLocation(pos, Integer.parseInt(arg3));
                    System.out.println("Server replied: " + reply);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }else if(cmd.equals("exit")) {
            	userHa.closeChannel();
            	sn.close();
            } else {
                System.out.println(help);
            }
            sn.nextLine();
        }
        
    }
}