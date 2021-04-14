package server;

import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class Server {

    private int port;

    public Server(int port) {
        this.port = port;

        init();
    }

    /**************************************************************************************
     * 											-init()
     * - init server; use localhost as IP
     *
     * ************************************************************************************/
    private void init() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                io.grpc.Server server = ServerBuilder.forPort(port).addService(new ServerImp()).build();
                try {
                    server.start();
                    System.out.println("Server started at " + server.getPort());

                    server.awaitTermination();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };

        new Thread(r).start();
        System.out.println("Server thread running");

    }
    
  //SERVER ATTRIBUTES GET FROM INI FILE 
    public static void main(String [] args) throws InvalidFileFormatException, IOException {
    	Server server = new Server(new Ini(new File("variables.ini")).get("Server","server_port", Integer.class));
    }

}
