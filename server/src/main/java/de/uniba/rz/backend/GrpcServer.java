package de.uniba.rz.backend;

import de.uniba.rz.entities.general.Priority;
import de.uniba.rz.entities.general.Status;
import de.uniba.rz.entities.general.Ticket;
import de.uniba.rz.entities.general.Type;
import de.uniba.rz.io.rpc.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GrpcServer {
    private final int port;
    private final Server server;
    private final TicketStore ticketStore;

    /**
     * During the construction of our server, the implemented service must be specified. There is also a possibility to
     * expose more than one service.
     */
    public GrpcServer(int port, TicketStore ticketStore) {
        this.port = port;
        this.server = ServerBuilder.forPort(port).addService(new GrpcImpl(ticketStore)).build();
        this.ticketStore = ticketStore;
    }

    /**
     * Starts the server and adds a shutdown hock to orderly shutdown the server.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        server.start();
        System.out.println("Server started and listened on port " + this.port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GrpcServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    /**
     * Method to stop the server, if a server is present.
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Blocking method until the shutdown hock terminates the server.
     *
     * @throws InterruptedException
     */
    void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Custom class for the implementation of the base service, which is an abstract class. The method must be
     * overridden since the default implementation has no implemented logic.
     */
    static class GrpcImpl extends TicketServiceGrpc.TicketServiceImplBase {
        private TicketStore ticketStore;

        public GrpcImpl(TicketStore ticketStore) {
            super();
            this.ticketStore = ticketStore;
        }

        @Override
        public void post(TicketRequest request, StreamObserver<TicketIdResponse> responseObserver) {
            Ticket newTicket = ticketStore.storeNewTicket(
                    request.getReporter(),
                    request.getTopic(),
                    request.getDescription(),
                    Type.valueOf(request.getType().name()),
                    Priority.valueOf(request.getPriority().name())
            );
            responseObserver.onNext(TicketIdResponse.newBuilder().setId(newTicket.getId()).build());
            responseObserver.onCompleted();
        }

        @Override
        public void getAll(EntityRequest request, StreamObserver<TicketResponse> responseObserver) {
            List<Ticket> tickets = ticketStore.getAllTickets();

            for (Ticket ticket : tickets) {
                System.out.println("Sending ticket " + ticket.toString());

                TicketResponse ticketResponse = TicketResponse.newBuilder()
                        .setId(ticket.getId())
                        .setReporter(ticket.getReporter())
                        .setTopic(ticket.getTopic())
                        .setDescription(ticket.getDescription())
                        .setType(TicketType.valueOf(ticket.getType().getValue()))
                        .setPriority(TicketPriority.valueOf(ticket.getPriority().getValue()))
                        .setStatus(TicketStatus.valueOf(ticket.getStatus().getValue()))
                        .build();

                responseObserver.onNext(ticketResponse);
            }
            responseObserver.onCompleted();
        }

        @Override
        public void get(TicketIdRequest request, StreamObserver<TicketResponse> responseObserver) {
            List<Ticket> ticketList = ticketStore.getAllTickets().stream().filter(ticket -> ticket.getId()==request.getId()).collect(Collectors.toList());
            Ticket responseTicket = ticketList.size() != 0? ticketList.get(0) : null;
            if (responseTicket == null){
                responseObserver.onError(new UnknownTicketException("Ticket not found"));
                return;
            }

            TicketResponse ticketResponse = TicketResponse.newBuilder()
                    .setId(responseTicket.getId())
                    .setReporter(responseTicket.getReporter())
                    .setTopic(responseTicket.getTopic())
                    .setDescription(responseTicket.getDescription())
                    .setType(TicketType.valueOf(responseTicket.getType().getValue()))
                    .setPriority(TicketPriority.valueOf(responseTicket.getPriority().getValue()))
                    .setStatus(TicketStatus.valueOf(responseTicket.getStatus().getValue()))
                    .build();

            responseObserver.onNext(ticketResponse);
            responseObserver.onCompleted();
        }

        @Override
        public void updateStatus(UpdateStatusRequest request, StreamObserver<TicketUpdatedResponse> responseObserver) {
            try {
                ticketStore.updateTicketStatus(request.getId(), Status.valueOf(request.getStatus().name()));
            } catch (UnknownTicketException e) {
                responseObserver.onError(new UnknownTicketException("Ticket not found"));
                return;
            }

            responseObserver.onNext(TicketUpdatedResponse.newBuilder().build());
            responseObserver.onCompleted();
        }
    }
}
