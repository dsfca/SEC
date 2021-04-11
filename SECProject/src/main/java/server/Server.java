package server;

import io.grpc.ServerBuilder;

import java.io.IOException;

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

}
