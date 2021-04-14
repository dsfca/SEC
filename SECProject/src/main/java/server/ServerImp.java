package server;

import com.server.grpc.ServerService;
import com.server.grpc.ServerService.DHKeyExcRep;
import com.server.grpc.ServerService.DHKeyExcReq;
import com.server.grpc.ServerService.Position;
import com.server.grpc.ServerService.subLocRepReply;
import com.server.grpc.ServerService.subLocRepReq;
import com.server.grpc.ServerService.obtLocRepReq;
import com.server.grpc.ServerService.obtLocRepReply;
import com.server.grpc.ServerService.obtUseLocReq;
import com.server.grpc.ServerService.obtUseLocRep;


import com.server.grpc.serverServiceGrpc.serverServiceImplBase;

import crypto.AESProvider;
import crypto.RSAProvider;
import io.grpc.stub.StreamObserver;
import shared.DiffieHelman;
import shared.TrackerLocationSystem;

import java.math.BigInteger;
import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;


public class ServerImp extends serverServiceImplBase {

    private static final String PRIVATE_KEY_PATH = "resources/private_keys/server_private.key";
    private Map<Integer, Key> sharedKeys = new HashMap<>();

	/**************************************************************************************
     *                                  - submitLocationReport()
     *  RPC: server received location report from a user; handle it and send reply
     *  - input:
     *      - request: the submitLocationReportRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
    @Override
    public void submitLocationReport(subLocRepReq request, StreamObserver<subLocRepReply> responseObserver) {
    	try {
	        System.out.println("[Server] Report submit request from " + request.getUserID() +
	                " at epoch " + request.getEpoch());
	        Key sharedKey = sharedKeys.get(request.getUserID());  
	        PublicKey userPubkey = TrackerLocationSystem.getUserPublicKey(request.getUserID());
	        
	        String secureReport = request.getSecureReport();
	        String reportDigSig = request.getReportDigitalSignature();
	        
	        String reportPlaintext = AESProvider.AESCypherDecypher(sharedKey, secureReport, Cipher.DECRYPT_MODE);
	        boolean reportIsAuth = RSAProvider.istextAuthentic(reportPlaintext, reportDigSig, userPubkey);
	        if(reportIsAuth) {
		        ServerService.subLocRepReply.Builder response = submitReportHandler(request);
		        responseObserver.onNext(response.build());
		        responseObserver.onCompleted();
	        }
    	}catch (Exception e) {
			
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
    public void obtainLocationReport(obtLocRepReq request, StreamObserver<obtLocRepReply> responseObserver) {
        System.out.println("[Server] Location report request from " + request.getUserID() +
                " at epoch " + request.getEpoch());

        ServerService.obtLocRepReply.Builder response = obtainReportHandler(request);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

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
    public void obtainLocationReportHA(obtLocRepReq request, StreamObserver<obtLocRepReply> responseObserver) {
        System.out.println("[Server] Location report request from HA to user" + request.getUserID() +
                " at epoch " + request.getEpoch());

        ServerService.obtLocRepReply.Builder response = obtainReportHandler(request);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

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
    public void obtainUsersAtLocation(obtUseLocReq request, StreamObserver<obtUseLocRep> responseObserver) {
        System.out.println("[Server] Users locations request from HA: users in epoch " + request.getEpoch() +
                " at location (" + request.getPos().getX() + ", " + request.getPos().getY() + ")");

        ServerService.obtUseLocRep.Builder response = obtainReportsHandler(request);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /** Possible reply codes to add into submitReportReply */
    private enum replyCode {OK, WRONG_EPOCH, NOK}
    /**************************************************************************************
     * 											- reportHandler()
     *  Handles submitLocationReportRequest received in submitLocationReport() method.
     *  It should validate the request and define appropriate content of subLocRepReply
     *  - input:
     *      - request: The submit request received from a user
     *
     * ************************************************************************************/
    private subLocRepReply.Builder submitReportHandler(subLocRepReq request) {
        subLocRepReply.Builder response = subLocRepReply.newBuilder();

        /**
         * TODO: the code below is just an example of possible reaction to request
         * */
        if (!checkEpoch(request.getEpoch())) {
            response.setReplycode(replyCode.WRONG_EPOCH.ordinal());
            response.setReplymessage("Provided report is not valid for current epoch");
        }

        if (!submitReport()) {
            response.setReplycode(replyCode.NOK.ordinal());
            response.setReplymessage("Your report was not submitted");
        }

        response.setReplycode(replyCode.OK.ordinal());
        response.setReplymessage("Your report was submitted successfully");

        return response;
    }

    private boolean submitReport() {
        // TODO add report to database
        return true;
    }

    private boolean checkEpoch(int epoch) {
        // TODO check if current epoch is asked
        return true;
    }


    /**************************************************************************************
     * 											- reportHandler()
     *  Handles submitLocationReportRequest received in submitLocationReport() method.
     *  It should validate the request and define appropriate content of subLocRepReply
     *  - input:
     *      - request: The submit request received from a user
     *
     * ************************************************************************************/
    private obtLocRepReply.Builder obtainReportHandler(obtLocRepReq request) {
        obtLocRepReply.Builder response = obtLocRepReply.newBuilder();

        /**
         * TODO: if user is not HA and queries not his ID => permission denied
         *      else build response
         * */

        response.setPos(getUserPositionAtEpoch(request.getUserID(), request.getEpoch()));
        response.setUserID(request.getUserID());

        return response;
    }

    private boolean isHA(int userId) {
        // TODO check if he is HA
        return false;
    }

    private Position getUserPositionAtEpoch(int userId, int epoch) {
        // TODO some db query to get position (beware of byzantines)
        Position ret = Position.newBuilder().setX(-1).setY(-1).build();
        return ret;
    }

    /**************************************************************************************
     * 											- reportHandler()
     *  Handles submitLocationReportRequest received in submitLocationReport() method.
     *  It should validate the request and define appropriate content of subLocRepReply
     *  - input:
     *      - request: The submit request received from a user
     *
     * ************************************************************************************/
    private obtUseLocRep.Builder obtainReportsHandler(obtUseLocReq request) {
        obtUseLocRep.Builder response = obtUseLocRep.newBuilder();

        /**
         * TODO: if user is not HA => permission denied else build response
         * */

        response.setEpoch(request.getEpoch());
        response.addAllUserList(getUsersAtEpoch(request.getEpoch()));

        return response;
    }

    private List<String> getUsersAtEpoch(int epoch) {
        // TODO get users at epoch; beware of byzantines
        return new ArrayList<>();
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
		 	putOrUpdateSharedKeys(request.getUserID(), secretKey);
		 	System.out.println("server agree key: " + new String(secretKey.getEncoded()));
		 	PublicKey myPubkey = df.getPublicKey();
		 	String pbkB64 = Base64.getEncoder().encodeToString(myPubkey.getEncoded());
		 	String digSigMyDHpubkey = TrackerLocationSystem.getDHkeySigned(myPubkey, PRIVATE_KEY_PATH);
		 	
		 	DHKeyExcRep.Builder rep = DHKeyExcRep.newBuilder().setDigSigPubkey(digSigMyDHpubkey)
		 			.setMyPubKey(pbkB64);
		 	
		 	responseObserver.onNext(rep.build());
		 	responseObserver.onCompleted();
		 	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
    }
    
    
    /**************************************************************************************
     * 											- putOrUpdateSharedKeys()
     * -
     *  - input:update the shared key between user id and server if both already
     *  	 have a shared key otherwise it(server) adds them.
     *      - id: 
     *		
     *		-key:
     * ************************************************************************************/
    public void putOrUpdateSharedKeys(int id , Key key) {
    	if(sharedKeys.get(id) != null)
    		sharedKeys.replace(id, key);
    	else
    		sharedKeys.put(id, key);
    }
    
}
