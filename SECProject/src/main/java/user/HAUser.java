package user;

import com.server.grpc.ServerService.secureReplay;
import com.server.grpc.ServerService.secureRequest;
import com.google.gson.JsonObject;
import com.server.grpc.serverServiceGrpc;
import com.server.grpc.serverServiceGrpc.serverServiceBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import shared.Point2D;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class HAUser extends User {

    private serverServiceBlockingStub serverStub;
    private ManagedChannel channel;

    /**************************************************************************************
     * 											-HA class constructor()
     * -
     *
     * ************************************************************************************/
    public HAUser(int id, int serverPort) throws Exception {
    	super(id, "HA");
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
    public String obtainLocationReport(int userId, int epoch) throws Exception {
    	int myNonce = new Random().nextInt();
    	int serverID = 0;
		String message = userId + "||" + epoch +"||" + myNonce; 
		JsonObject secureReport = getsecureMessage(serverID, message);
		String secureMessage = secureReport.get("ciphertext").getAsString();
		String messDigSig = secureReport.get("textDigitalSignature").getAsString();
		secureRequest.Builder reportRequest = secureRequest.newBuilder().setConfidentMessage(secureMessage)
    			.setMessageDigitalSignature(messDigSig).setUserID(getMyID());
		secureReplay reply = serverStub.obtainLocationReportHA(reportRequest.build());
    	if(!reply.getOnError()) {
    		 serverID = reply.getServerID();
    		 String[] replyFields = getfieldsFromSecureMessage(serverID, reply.getConfidentMessage(), reply.getMessageDigitalSignature());
    		 int serverNonce = Integer.parseInt(replyFields[replyFields.length-1]);
    		 if(serverNonce != myNonce - 1)
    			 throw new Exception("Nonce error");
    		return replyFields[1];
    	}
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
		String message = position.toString() + "||" + epoch +"||" + myNonce; 	
		JsonObject secureReport = getsecureMessage(0, message);
		String secureMessage = secureReport.get("ciphertext").getAsString();
		String messDigSig = secureReport.get("textDigitalSignature").getAsString();
		secureRequest locationRequest = secureRequest.newBuilder().setUserID(this.myID).setConfidentMessage(secureMessage).setMessageDigitalSignature(messDigSig).build();
		secureReplay reply = serverStub.obtainUsersAtLocation(locationRequest);
        if(reply.getOnError()) {
        	throw new Exception(reply.getErrormessage());
        }
        int serverID = reply.getServerID();
        String[] replyFields = getfieldsFromSecureMessage(serverID, reply.getConfidentMessage(), reply.getMessageDigitalSignature());
        int serverNonce = Integer.parseInt(replyFields[replyFields.length-1]);
		if(serverNonce != myNonce - 1)
			throw new Exception("Nonce error");
        return replyFields[0];
    }
    
    public void closeChannel() {
    	channel.shutdown();
    }
    

    public static void main(String[] args) throws Exception {
        String help = "Accept only following formats:\n\tgetReport <ID> <epoch>\n\tgetUsers <X> <Y> <epoch>";
        HAUser userHa = new HAUser(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
        String cmd, arg1, arg2, arg3;
        Scanner sn = new Scanner(System.in);
        System.out.println(help);
        while(true) {
            cmd = sn.next().toLowerCase(Locale.ROOT);
            if (cmd.equals("getreport")) {
                arg1 = sn.next().toLowerCase(Locale.ROOT);
                arg2 = sn.next().toLowerCase(Locale.ROOT);
                try {
                    String reply = userHa.obtainLocationReport(Integer.parseInt(arg1), Integer.parseInt(arg2));
                    System.out.println("Server replied: " + reply);
                } catch (Exception e) {
                	e.printStackTrace();
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
                    e.printStackTrace();
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