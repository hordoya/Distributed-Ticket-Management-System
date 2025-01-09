package de.uniba.rz.entities.message;

import de.uniba.rz.entities.general.Ticket;

import java.io.Serializable;
import java.util.List;

public class MessageBody implements Serializable {
    private List<Ticket> tickets;
    private Ticket singleTicket;

    public MessageBody() {}

    public MessageBody(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public MessageBody(Ticket ticket){
        this.singleTicket = ticket;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public Ticket getSingleTicket() {
        return singleTicket;
    }

    public void setSingleTicket(Ticket singleTicket) {
        this.singleTicket = singleTicket;
    }

    public String toString() {
        return "MessageBody[" + "tickets=" + tickets + ", singleTicket=" + singleTicket + ']';
    }
}
