package server;

import io.grpc.ServerBuilder;
import shared.TrackerLocationSystem;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import crypto.RSAProvider;

public class Server {

    private int port;
    private DealWithRequest serverDealWithReq;
    private int server_id;

    public Server(int server_id, int port, boolean byzantine) {
        this.port = port;
        this.server_id = server_id;
        serverDealWithReq = new DealWithRequest(this.server_id, byzantine);
    }

    /**************************************************************************************
     * 											-init()
     * - init server; use localhost as IP
     *
     * ************************************************************************************/
    public void init() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    io.grpc.Server server = ServerBuilder.forPort(port).addService(new ServerImp(server_id,serverDealWithReq)).build();
                	verifyKeys(server_id, "server");
                    server.start();
                    System.out.println("INIT SERVER: Server " + getID() + " started at " + server.getPort());

                    server.awaitTermination();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        new Thread(r).start();
        System.out.println("INIT SERVER: Server " + getID() + " thread running");

    }
    
    public int getID() {
    	return this.server_id;
    }
    
    public void verifyKeys(int id, String serverOrUser) throws Exception {
		String pubkeypath = "resources/public_keys/"+serverOrUser +"" + id +"_public.key";
		String privKeyPath = "resources/private_keys/"+serverOrUser +"" + id +"_private.key";	
		try {
			KeyPair mykeypair = RSAProvider.readRSAKey(pubkeypath, privKeyPath, TrackerLocationSystem.password);
		} catch (Exception e) {
			RSAProvider.RSAKeyGenerator(privKeyPath, pubkeypath, TrackerLocationSystem.password);
		}
	} 
    
  //SERVER ATTRIBUTES GET FROM INI FILE 
    public static void main(String [] args) throws InvalidFileFormatException, IOException {
    	Server server = new Server(0,new Ini(new File("variables.ini")).get("Server","server_start_port", Integer.class), false);
    	server.init();
    	
    	System.out.println("sbtring".substring(2,3));
    }

}