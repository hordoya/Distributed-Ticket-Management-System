package de.uniba.rz.entities.general;

import java.io.*;

public class Helper {
    public static Ticket convertTicketObjectToTicket(Ticket ticket) {
        return new Ticket(
                ticket.getId(),
                ticket.getReporter(),
                ticket.getTopic(),
                ticket.getDescription(),
                ticket.getType(),
                ticket.getPriority(),
                ticket.getStatus()
        );
    }

    public static Ticket convertTicketToTicketObject(Ticket ticket) {
        return new Ticket(
                ticket.getId(),
                ticket.getReporter(),
                ticket.getTopic(),
                ticket.getDescription(),
                ticket.getType(),
                ticket.getPriority(),
                ticket.getStatus()
        );
    }

    public static <T> byte[] convertTicketObjectToByteArray(T obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();

            return bos.toByteArray();
        }
    }

    public static <T> T convertByteArrayToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            T obj = (T) inputStream.readObject();
            return obj;
        }
    }
}
