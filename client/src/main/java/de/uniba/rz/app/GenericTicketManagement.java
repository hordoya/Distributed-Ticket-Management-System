package de.uniba.rz.app;

import com.google.gson.Gson;
import de.uniba.rz.entities.general.*;

import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenericTicketManagement implements TicketManagementBackend {
    private HashMap<Integer, Ticket> localTicketList = new HashMap<>();
    private final Gson gson = new Gson();

    @Override
    public void triggerShutdown() {

    }

    protected abstract Response send(Request request) throws ServerNotActiveException;

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority) throws TicketException {
        Ticket newTicket = new Ticket(-1, reporter, topic, description, type, priority);
        Request request = new Request(RequestMethod.POST, newTicket);
        Response response;

        try{
            response = send(request);
        } catch (ServerNotActiveException e){
            throw new TicketException(e.getMessage());
        }

        Ticket ticketFromResponse = response.getTicket();
        localTicketList.put(ticketFromResponse.getId(), ticketFromResponse);
        return ticketFromResponse;
    }

    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        Request request = new Request(RequestMethod.GET);
        Response response;

        try {
            response = send(request);
        } catch (ServerNotActiveException e){
            throw new TicketException("Failed to fetch all tickets: " + e.getMessage(), e);
        }

        localTicketList.clear();
        for (Ticket ticket: response.getTicketList()){
            localTicketList.put(ticket.getId(), ticket);
        }

        return localTicketList.entrySet().stream().map(entry -> (Ticket) entry.getValue().clone())
                .collect(Collectors.toList());
    }

    @Override
    public Ticket getTicketById(int id) throws TicketException {
        Ticket ticket = new Ticket(id);
        Request request = new Request(RequestMethod.GET, ticket);
        Response response;

        try {
            response = send(request);
        } catch (ServerNotActiveException e){
            throw new TicketException("Failed to fetch ticket by ID: " + e.getMessage(), e);
        }

        Ticket ticketFromID = response.getTicket();
        if (ticketFromID == null){
            throw new TicketException("Ticket with ID " + id + " not found");
        }
        return ticketFromID;
    }

    private Ticket getTicketFromLocalList(int id) throws TicketException{
        if (!localTicketList.containsKey(id)) {
            throw new TicketException("Could not find ticket with id " + id);
        }
        return localTicketList.get(id);
    }

    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        Ticket modifiedTicket = getTicketFromLocalList(id);

        if (modifiedTicket.getStatus() != Status.NEW){
            throw new TicketException("Can not accept ticket with id " + id +
                    " as it is currently in " + modifiedTicket.getStatus());
        }

        modifiedTicket.setStatus(Status.ACCEPTED);
        Request request = new Request(RequestMethod.PUT, modifiedTicket);
        Response response;

        try {
            response = send(request);
        } catch (ServerNotActiveException e){
            throw new TicketException("Failed to accept ticket: " + e.getMessage(), e);
        }
        return response.getTicket();
    }

    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        Ticket ticketToModify = getTicketFromLocalList(id);
        if (ticketToModify.getStatus() != Status.NEW){
            throw new TicketException("Can not reject ticket with id " + id + " as it is currently in status " + ticketToModify.getStatus());
        }

        ticketToModify.setStatus(Status.REJECTED);
        Request request = new Request(RequestMethod.PUT, ticketToModify);
        Response response;
        try {
            response = send(request);
        } catch (ServerNotActiveException e){
            throw new TicketException("Failed to reject ticket: " + e.getMessage(), e);
        }

        return response.getTicket();
    }

    @Override
    public Ticket closeTicket(int id) throws TicketException {
        Ticket ticketToModify = getTicketFromLocalList(id);
        if (ticketToModify.getStatus() != Status.ACCEPTED){
            throw new TicketException("Can not reject ticket with id " + id + " as it is currently in status " + ticketToModify.getStatus());
        }

        ticketToModify.setStatus(Status.CLOSED);
        Request request = new Request(RequestMethod.PUT, ticketToModify);
        Response response;
        try {
            response = send(request);
        } catch (ServerNotActiveException e){
            throw new TicketException("Failed to close ticket: " + e.getMessage(), e);
        }
        return response.getTicket();
    }
}
