package de.uniba.rz.app;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.uniba.rz.entities.amqp.AMQPReceiver;
import de.uniba.rz.entities.amqp.AMQPSender;
import de.uniba.rz.entities.general.*;
import de.uniba.rz.entities.message.MessageBody;
import de.uniba.rz.entities.message.MessageDTO;
import de.uniba.rz.entities.message.MessageType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class AMPQTicketManagement implements TicketManagementBackend {
    private Map<Integer, Ticket> tickets = new HashMap<>();
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;
    private AMQPSender amqpSender;

    public AMPQTicketManagement(String server, String serverQueue) {
        tickets = new HashMap<>();

        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(server);

        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            amqpSender = new AMQPSender(channel, serverQueue);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority) throws TicketException {
        Ticket newTicket = new Ticket(0, reporter, topic, description, type, priority, Status.NEW);

        try {
            MessageDTO messageDTO = sendAndReceiveDataFromAMQPServer(MessageType.SAVE, newTicket, 0);
            Ticket createdTicket = messageDTO.getMessageBody().getSingleTicket();
            tickets.put(createdTicket.getId(), createdTicket);
            return createdTicket;
        } catch (IOException | InterruptedException e){
            throw new TicketException("Unexpected error while sending data to AMQP server");
        }
    }

    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        try {
            MessageDTO messageDTO = sendAndReceiveDataFromAMQPServer(MessageType.RETRIEVE_ALL, null, 0);
            List<Ticket> ticketList = messageDTO.getMessageBody().getTickets();
            ticketList.forEach(ticket -> tickets.put(ticket.getId(), ticket));
            return ticketList;
        } catch (IOException | InterruptedException e){
            throw new TicketException("Unexpected error while sending data to AMQP server");
        }
    }

    @Override
    public Ticket getTicketById(int id) throws TicketException {
        Ticket ticket = getSingleTicket(id);

        if (ticket == null) {
            throw new TicketException("Ticket with id " + id + " not found");
        }
        return ticket;
    }

    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        try {
            return updateTicketStatus(id, Status.ACCEPTED);
        } catch (IOException e){
            throw new TicketException("Unexpected error while accepting ticket");
        }
    }

    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        try {
            return updateTicketStatus(id, Status.CLOSED);
        } catch (IOException e){
            throw new TicketException("Unexpected error while accepting ticket");
        }
    }

    @Override
    public Ticket closeTicket(int id) throws TicketException {
        try {
            return updateTicketStatus(id, Status.CLOSED);
        } catch (IOException e){
            throw new TicketException("Unexpected error while closing ticket");
        }
    }

    @Override
    public void triggerShutdown() {
        this.tickets = null;
    }

    private MessageDTO sendAndReceiveDataFromAMQPServer(MessageType messageType,
                                                        Ticket ticket, int optionalTicketId)
            throws IOException, InterruptedException {
        String correlationId = UUID.randomUUID().toString();
        String replyQueueName = channel.queueDeclare().getQueue();

        AMQP.BasicProperties properties = new AMQP.BasicProperties
                .Builder()
                .correlationId(correlationId)
                .replyTo(replyQueueName)
                .build();

        sendDataToAMQPServer(messageType, ticket, optionalTicketId, properties);
        return receiveDataFromAMQPServer(replyQueueName, correlationId);
    }

    private void sendDataToAMQPServer(MessageType messageType, Ticket ticket, int optionalTicketId,
                                      AMQP.BasicProperties amqpProperties) throws IOException {
        MessageBody messageBody = new MessageBody(ticket);
        MessageDTO messageDTO = new MessageDTO(messageType, messageBody, optionalTicketId);

        amqpSender.send(Helper.convertTicketObjectToByteArray(messageDTO), amqpProperties);
    }

    private MessageDTO receiveDataFromAMQPServer(String queueName, String correlationId) throws IOException, InterruptedException {
        final BlockingQueue<MessageDTO> response = new ArrayBlockingQueue<>(1);

        AMQPReceiver<MessageDTO> amqpReceiver = new AMQPReceiver<>(channel, queueName);
        amqpReceiver.receive(correlationId, (messageDTO, properties) ->{
            response.add(messageDTO);
        });

        MessageDTO messageDTO = response.take();
        amqpReceiver.cancel();

        return messageDTO;
    }

    private Ticket getSingleTicket(int id){
        return tickets.get(id);
    }

    private Ticket updateTicketStatus(int ticketId, Status status) throws IOException {
        Ticket ticket = getSingleTicket(ticketId);
        ticket.setStatus(status);

        sendDataToAMQPServer(MessageType.UPDATE, ticket, ticketId, null);
        tickets.put(ticket.getId(), ticket);
        return ticket;
    }
}
