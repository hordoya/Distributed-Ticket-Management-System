package de.uniba.rz.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.uniba.rz.entities.general.Priority;
import de.uniba.rz.entities.general.Status;
import de.uniba.rz.entities.general.Ticket;
import de.uniba.rz.entities.general.Type;

/**
 * This is a basic implementation of the <code>TicketStore</code> interface for
 * testing purposes only.
 *
 * Caution: This class is neither thread-safe nor does it perform any checks in
 * the updateTicketStatus method
 *
 * Do not use this class in the assignment solution but provide an own
 * implementation of <code>TicketStore</code>!
 */
public class SimpleTicketStore implements TicketStore {

	private int nextTicketId = 0;
	private List<Ticket> ticketList = new ArrayList<>();
	private List<Ticket> tickets; // or
	//private Map<Integer, Ticket> tickets;


	@Override
	public Ticket storeNewTicket(String reporter, String topic, String description, Type type, Priority priority) {
		System.out.println("Creating new Ticket from Reporter: " + reporter + " with the topic \"" + topic + "\"");
		Ticket newTicket = new Ticket(nextTicketId++, reporter, topic, description, type, priority);
		ticketList.add(newTicket);
		return newTicket;
	}

	@Override
	public Ticket updateTicketStatus(int ticketId, Status newStatus) {
		// Your implementation logic here
		// For example:
		Ticket ticket = findTicketById(ticketId); // Replace with actual logic
		if (ticket != null) {
			ticket.setStatus(newStatus);
		}
		return ticket;
	}

	private Ticket findTicketById(int ticketId) {
		// Assuming you have a list or map to store tickets

		for (Ticket ticket : tickets) { // Replace `tickets` with your actual storage
			if (ticket.getId() == ticketId) {
				return ticket;
			}
		}
		return null; // Return null if the ticket is not found
	}


	@Override
	public List<Ticket> getAllTickets() {
		return ticketList;
	}

	public List<Ticket> filterTicketByName(String name){
		String nameLower = (name != null) ? name.toLowerCase() : "";
		return ticketList.stream().filter(
				x -> (!x.getReporter().isEmpty() && x.getReporter().toLowerCase().contains(nameLower))
				|| (!x.getTopic().isEmpty() && x.getTopic().toLowerCase().contains(nameLower))
				|| (!x.getDescription().isEmpty() && x.getDescription().toLowerCase().contains(nameLower))
		).collect(Collectors.toList());
	}

	@Override
	public List<Ticket> filterTicketByType(Type type) {
		return ticketList.stream().filter(x -> x.getType() == type).collect(Collectors.toList());
	}

	@Override
	public List<Ticket> filterTicketByNameAndType(String name, Type type) {
		String nameLower = (!name.isEmpty()) ? name.toLowerCase() : "";

		return ticketList.stream()
				.filter(ticket -> containsIgnoreCase(ticket.getReporter(), nameLower) ||
						containsIgnoreCase(ticket.getTopic(), nameLower) ||
						containsIgnoreCase(ticket.getDescription(), nameLower))
				.collect(Collectors.toList());
	}

	@Override
	public Ticket getTicket(int ticketId) {
		return ticketList.stream()
				.filter(x -> x.getId() == ticketId)
				.findAny()
				.orElse(null);
	}

	private boolean containsIgnoreCase(String source, String substring) {
		return source != null && source.toLowerCase().contains(substring);
	}
}
