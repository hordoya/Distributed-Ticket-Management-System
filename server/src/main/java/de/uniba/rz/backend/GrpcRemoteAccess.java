package de.uniba.rz.backend;

import java.io.IOException;

public class GrpcRemoteAccess extends RemoteAccess {
    GrpcServer server;

    int port;
    public GrpcRemoteAccess(String portStr) {

        port = Integer.parseInt(portStr);
    }

    @Override
    public void prepareStartup(TicketStore ticketStore) {
        server = new GrpcServer(port, ticketStore);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void run() {
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            server.blockUntilShutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
