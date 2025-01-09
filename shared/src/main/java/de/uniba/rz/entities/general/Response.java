package de.uniba.rz.entities.general;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Response {
    private List<Ticket> ticketList;
    transient Gson gson = new Gson();

    public Response(String json){
        this.ticketList = Arrays.asList(gson.fromJson(json, Ticket[].class));
    }

    public Response(Ticket ticket){
        this.ticketList = new ArrayList<>();
        this.ticketList.add(ticket);
    }

    public Response(List<Ticket> content) {
        this.ticketList = new ArrayList<>();
        this.ticketList.addAll(content);
    }

    public Ticket getTicket(){
        return ticketList.get(0);
    }

    public List<Ticket> getTicketList(){
        return ticketList;
    }
}
