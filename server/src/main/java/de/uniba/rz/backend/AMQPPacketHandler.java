package de.uniba.rz.backend;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import de.uniba.rz.entities.amqp.AMQPSender;
import de.uniba.rz.entities.general.Helper;
import de.uniba.rz.entities.general.Ticket;
import de.uniba.rz.entities.message.MessageBody;
import de.uniba.rz.entities.message.MessageDTO;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AMQPPacketHandler extends Thread {
    private final MessageDTO messageDTO;
    private final TicketStore ticketStore;
    private final AMQP.BasicProperties properties;
    private final Channel channel;

    public AMQPPacketHandler(MessageDTO messageDTO, TicketStore ticketStore, AMQP.BasicProperties properties, Channel channel) {
        this.messageDTO = messageDTO;
        this.ticketStore = ticketStore;
        this.properties = properties;
        this.channel = channel;
    }

    public void run() {
        try {
            processPacket();
        } catch (IOException | UnknownTicketException e) {
            e.printStackTrace();
        }
    }

    private void processPacket() throws IOException, UnknownTicketException {
        MessageBody messageBody = null;

        switch (messageDTO.getMessageType()) {
            case SAVE: {
                Ticket ticket = messageDTO.getMessageBody().getSingleTicket();
                Ticket newTicket = ticketStore.storeNewTicket(ticket.getReporter(),
                        ticket.getTopic(), ticket.getDescription(),
                        ticket.getType(), ticket.getPriority());

                messageBody = new MessageBody(newTicket);
                break;
            } case RETRIEVE_ONE: {
                Ticket retrievedTicket = null;

                for (Ticket ticketFromList : ticketStore.getAllTickets()) {
                    if (ticketFromList.getId() == messageDTO.getOptionalTicketId()) {
                        retrievedTicket = ticketFromList;
                        break;
                    }
                }

                messageBody = new MessageBody(Arrays.asList(retrievedTicket));
                break;
            } case RETRIEVE_ALL: {
                List <Ticket> retrievedTickets = ticketStore.getAllTickets();
                messageBody = new MessageBody(retrievedTickets);
                break;
            } case UPDATE: {
                ticketStore.updateTicketStatus(messageDTO.getOptionalTicketId(),
                        messageDTO.getMessageBody().getSingleTicket().getStatus());
                break;
            } default: {
                System.out.println("Unknown message type: " + messageDTO.getMessageType());
            }
        }

        if (messageBody != null){
            sendResponse(properties.getReplyTo(),
                    properties.getCorrelationId(),
                    messageBody);
        }
    }

    private void sendResponse(String queue, String corelationId, MessageBody messageBody) throws IOException {
        MessageDTO messageDTO = new MessageDTO();
        AMQP.BasicProperties properties = new AMQP.BasicProperties
                .Builder()
                .correlationId(corelationId)
                .build();

        AMQPSender sender = new AMQPSender(channel, queue);
        sender.send(Helper.convertTicketObjectToByteArray(messageDTO), properties);
    }
}
