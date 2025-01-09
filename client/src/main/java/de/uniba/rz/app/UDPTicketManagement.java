package de.uniba.rz.app;

import com.google.gson.Gson;
import de.uniba.rz.entities.general.Request;
import de.uniba.rz.entities.general.Response;

import java.io.IOException;
import java.net.*;
import java.rmi.server.ServerNotActiveException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class UDPTicketManagement extends GenericTicketManagement {
    AtomicInteger nextId;
    String serverIP;
    int port;

    BlockingQueue<String> queue;
    SocketAddress socketAddress;
    DatagramSocket datagramSocket;

    Gson gson = new Gson();

    public UDPTicketManagement(String serverIP, int port) {
        this.serverIP = serverIP;
        this.port = port;
        this.nextId = new AtomicInteger(1);
        socketAddress = new InetSocketAddress(serverIP, port);
        queue = new ArrayBlockingQueue<>(10);

        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void triggerShutdown(){
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
    }

    @Override
    protected Response send(Request request) throws ServerNotActiveException {
        int timeout = 10000;
        if (request.getMethod() == null){
            throw new ServerNotActiveException("Request method cannot be null");
        }

        AtomicReference<Response> responseRef = new AtomicReference<>();
        Thread thread = new Thread(() -> responseRef.set(sendAndWaitForResponse(datagramSocket,
                socketAddress, request, timeout)));
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerNotActiveException();
        }

        if (responseRef.get() == null){
            throw new ServerNotActiveException("Request failed");
        }

        return responseRef.get();
    }

    private Response sendAndWaitForResponse(DatagramSocket socket, SocketAddress socketAddress,
                                            Request request, int timeout) {
        int length = 50000;
        byte[] buffer = new byte[length];
        DatagramPacket receivedPacket = new DatagramPacket(buffer, length);

        String sentPacket = gson.toJson(request);
        DatagramPacket sendingPacket = new DatagramPacket(sentPacket.getBytes(), sentPacket.length(), socketAddress);

        try {
            socket.send(sendingPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            socket.receive(receivedPacket);
        } catch (IOException e){
            if (e.getClass() == SocketTimeoutException.class){
                System.out.println("Server timed out, closing connection");
                return null;
            }
            e.printStackTrace();
        }

        byte[] receivedData = receivedPacket.getData();
        Response response = gson.fromJson(new String(receivedData).trim(), Response.class);
        System.out.println("Received response: " + new String(receivedData));
        return response;
    }
}
