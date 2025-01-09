package de.uniba.rz.entities.general;

public class Request {
    private RequestMethod method;
    private Ticket ticket;

    public Request(RequestMethod method, Ticket ticket){
        this.method = method;
        this.ticket = ticket;
    }

    public Request(RequestMethod method){
        this(method, null);
    }

    public RequestMethod getMethod(){
        return method;
    }

    public void setMethod(RequestMethod method){
        this.method = method;
    }

    public Ticket getTicketList(){
        return ticket;
    }

    public void setListTicket(Ticket ticketList){
        this.ticket = ticketList;
    }
}
