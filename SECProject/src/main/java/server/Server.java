package server;

import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import crypto.RSAProvider;

public class Server {

    private int port;
    private DealWithRequest serverDealWithReq;
    private int ID;

    public Server(int id,int port) {
        this.port = port;
        serverDealWithReq = new DealWithRequest(id);
        this.ID = id;
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
                io.grpc.Server server = ServerBuilder.forPort(port).addService(new ServerImp(ID,serverDealWithReq)).build();
                try {
                	verifyKeys(ID, "server");
                    server.start();
                    System.out.println("Server started at " + server.getPort());

                    server.awaitTermination();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        new Thread(r).start();
        System.out.println("Server thread running");

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
    
  //SERVER ATTRIBUTES GET FROM INI FILE 
    public static void main(String [] args) throws InvalidFileFormatException, IOException {
    	Server server = new Server(0,new Ini(new File("variables.ini")).get("Server","server_start_port", Integer.class));
    	server.init();
    	
    	System.out.println("sbtring".substring(2,3));
    }

}