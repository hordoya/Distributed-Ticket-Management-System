package de.uniba.rz.backend;

import java.util.List;

import de.uniba.rz.entities.general.Priority;
import de.uniba.rz.entities.general.Status;
import de.uniba.rz.entities.general.Ticket;
import de.uniba.rz.entities.general.Type;

public interface TicketStore {

    Ticket storeNewTicket(String reporter, String topic, String description,
                        Type type, Priority priority);

    Ticket updateTicketStatus(int ticketId, Status newStatus) throws UnknownTicketException, IllegalStateException;

    List<Ticket> getAllTickets();

    List<Ticket> filterTicketByName(String name);

    List<Ticket> filterTicketByType(Type type);

    List<Ticket> filterTicketByNameAndType(String name, Type type);

    Ticket getTicket(int ticketId);
}
