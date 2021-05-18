package user;

import com.server.grpc.ServerService.secureReplay;
import com.server.grpc.ServerService.secureRequest;
import com.google.gson.JsonObject;
import com.server.grpc.serverServiceGrpc;
import com.server.grpc.serverServiceGrpc.serverServiceBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.ini4j.Ini;
import shared.Point2D;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HAUser extends User {

    private enum MessageType {
        ObtainLocationReport,
        ObtainUsersAtLocation
    }

    /**************************************************************************************
     * 											-HA class constructor()
     * -
     *
     * ************************************************************************************/
    public HAUser(int id) throws Exception {
    	super(id, "HA");
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

        String message = userId + "||" + epoch;
        return asyncReadRequest(message, MessageType.ObtainLocationReport);

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
        String message = position.toString() + "||" + epoch;
        return asyncReadRequest(message, MessageType.ObtainUsersAtLocation);
    }

    private String asyncReadRequest(String message, MessageType type) throws Exception {
        List<Integer> nonces = new ArrayList<>();
        Map<String, Integer> readvals = new HashMap<>();
        Set<Integer> acks = new HashSet<>();

        final CountDownLatch finishLatch = new CountDownLatch(this.num_servers);
        StreamObserver<secureReplay> acksObserver = new StreamObserver<secureReplay>() {
            @Override
            public void onNext(secureReplay secureReplay) {
                int serverId = secureReplay.getServerID();
                if (acks.contains(serverId))
					return;

                acks.add(serverId);
                finishLatch.countDown();

                // Get Nonce from the message
                String[] replyFields = new String[0];
                try {
					replyFields = getfieldsFromSecureMessage(serverId,
							secureReplay.getConfidentMessage(), secureReplay.getMessageDigitalSignature());
				} catch (Exception e) {
					System.out.println(e.getMessage());
					return;
				}

                // Validate nonce
                int serverNonce = Integer.parseInt(replyFields[replyFields.length-1]);
				if(serverNonce != nonces.get(serverId)-1) {
					System.out.println("[user" + myID + "] Submit Report Error: Unexpected nonce");
					return;
				}

				// Compose plaintext message
                String response = "";
				for (int i = 0; i < replyFields.length-1; ++i)
				    response += "||" + replyFields[i];

                if (!readvals.containsKey(response)) {
                    readvals.put(response, 1);
                    return;
                }

                readvals.put(response, readvals.get(response) + 1);
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
				System.out.println("[HA" + myID + "] Error: " + status);
				finishLatch.countDown();
            }

            @Override
            public void onCompleted() { }
        };

        List<ManagedChannel> serverChannels = new ArrayList<>();
		serverServiceGrpc.serverServiceStub serverAsyncStub;

        for(int server_id = 0; server_id < this.num_servers; server_id++) {
            int myNonce = new Random().nextInt();
            nonces.add(myNonce);

            JsonObject secureMessage = getsecureMessage(server_id, message + "||" + myNonce);
            String messagecipher = secureMessage.get("ciphertext").getAsString();
			String messageDigSig = secureMessage.get("textDigitalSignature").getAsString();

			ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", server_start_port+server_id)
					                       .usePlaintext().build();
			serverChannels.add(channel); // Store it for a proper close later
			serverAsyncStub = serverServiceGrpc.newStub(channel).withDeadlineAfter(10, TimeUnit.SECONDS)
																.withWaitForReady();

			secureRequest request = secureRequest.newBuilder().setUserID(myID)
													  				.setConfidentMessage(messagecipher)
													  				.setMessageDigitalSignature(messageDigSig).build();
			if (type == MessageType.ObtainLocationReport) {
			    serverAsyncStub.obtainLocationReport(request, acksObserver);
            } else if (type == MessageType.ObtainUsersAtLocation) {
			    serverAsyncStub.obtainUsersAtLocation(request, acksObserver);
            } else {
			    throw new Exception("Unknown message type.");
            }
        }

        // Wait for all replies (both errors and ok)
		finishLatch.await();

        // Close channels
		for(ManagedChannel ch : serverChannels)
			ch.shutdown();

        if (readvals.size() <= quorum) {
			throw new Exception("Not enough answers");
		}

        // There is more than quorum of returned messages, find the most common
        Integer max = 0;
		String consensus = "";
		for (Map.Entry<String, Integer> entry : readvals.entrySet()) {
			if (entry.getValue() > max) {
				max = entry.getValue();
				consensus = entry.getKey();
			}
		}

        return consensus;
    }

    public static void main(String[] args) throws Exception {
        String help = "Accept only following formats:\n\tgetReport <ID> <epoch>\n\tgetUsers <X> <Y> <epoch>";
//        HAUser userHa = new HAUser(Integer.parseInt(args[0]));
        HAUser userHa = new HAUser(0);
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
            	sn.close();
            } else {
                System.out.println(help);
            }
            sn.nextLine();
        }
        
    }
}