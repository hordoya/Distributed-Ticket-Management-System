package de.uniba.rz.backend;

import com.google.gson.Gson;
import de.uniba.rz.entities.general.Request;
import de.uniba.rz.entities.general.Response;
import de.uniba.rz.entities.general.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Basic interface to enable remote access to the {@link TicketStore} managing the tickets internally
 *
 */
public abstract class RemoteAccess implements Runnable {
	TicketStore ticketStore;
	Gson gson = new Gson();

	/**
	 * Generic startup method which might be used to prepare the actual execution
	 * 
	 * @param ticketStore
	 * reference to the {@link TicketStore} which is used by the application
	 */
    abstract void prepareStartup(TicketStore ticketStore);

    /**
     * Triggers the graceful shutdown of the system.
     */
    abstract void shutdown();

	Response handle(Request request){
		Ticket reqeustedTicket = request.getTicketList();
		switch (request.getMethod()) {
			case GET:
				if (reqeustedTicket == null) {
					return new Response(ticketStore.getAllTickets());
				} else {
					List<Ticket> requestedTicketList = ticketStore.getAllTickets().stream().filter(
							(ticket -> ticket.getId() == request.getTicketList().getId()))
							.collect(Collectors.toList());
					return new Response(requestedTicketList);
				}
			case POST:
				System.out.println("Sending the data" + gson.toJson(reqeustedTicket));
				Ticket resultTicket = ticketStore.storeNewTicket(reqeustedTicket.getReporter(),
						reqeustedTicket.getTopic(), reqeustedTicket.getDescription(),
						reqeustedTicket.getType(), reqeustedTicket.getPriority());
				return new Response(resultTicket);
			case PUT:
				System.out.println("Updating the data" + gson.toJson(reqeustedTicket));
				List<Ticket> ticketList = null;

				try {
					ticketStore.updateTicketStatus(reqeustedTicket.getId(), reqeustedTicket.getStatus());
					ticketList = ticketStore.getAllTickets();
				} catch (UnknownTicketException e) {
					ticketList = new ArrayList<>();
				}
				return new Response(ticketList);
			default:
				throw new IllegalArgumentException("Unknown Method \"" + request.getMethod().getValue() + "\"");
		}
	}
}
