package de.uniba.rz.backend;

import de.uniba.rz.entities.general.Status;
import de.uniba.rz.entities.general.Ticket;
import de.uniba.rz.entities.general.Type;
import de.uniba.rz.entities.general.Helper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response;

import java.util.*;
import java.util.stream.Collectors;

@Path("tickets")
public class WebTicketResources {
    @POST
    public Response createTicket(Ticket newTicket, @Context UriInfo uriInfo) {
        try {
            List<String> errors = new ArrayList<>();

            if (newTicket.getReporter() == null || newTicket.getReporter().isEmpty()) {
                errors.add("Reporter's name is required");
            }
            if (newTicket.getTopic() == null || newTicket.getTopic().isEmpty()) {
                errors.add("Topic is required");
            }
            if (newTicket.getDescription() == null || newTicket.getDescription().isEmpty()) {
                errors.add("Description is required");
            }
            if (newTicket.getType() == null) {
                errors.add("Type is required");
            }
            if (newTicket.getPriority() == null){
                errors.add("Priority is required");
            }

            if (!errors.isEmpty()) {
                return buildResponseObject(Response.Status.BAD_REQUEST, errors);
            }

            Ticket createdTicket = WebServiceRemoteAccess.ticketStore.storeNewTicket(newTicket.getReporter(), newTicket.getTopic(), newTicket.getDescription(), newTicket.getType(), newTicket.getPriority());

            UriBuilder path = uriInfo.getAbsolutePathBuilder();
            path.path(Integer.toString(createdTicket.getId()));
            return Response.created(path.build()).build();

        } catch (Exception e) {
            return buildResponseObject(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected Server Error");
        }
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") int id, Status updatedStatus) throws UnknownTicketException {
        try {
            System.out.println("udpate: " + id);
            Ticket ticket = WebServiceRemoteAccess.ticketStore.updateTicketStatus(id , updatedStatus);

            return Response.ok().entity(ticket).build();
        } catch (IllegalStateException e) {
            return buildResponseObject(Response.Status.FORBIDDEN, e.getMessage());
        } catch (UnknownTicketException e) {
            return buildResponseObject(Response.Status.NOT_FOUND, "Ticket not found with ID " + id);
        } catch (Exception e) {
            return buildResponseObject(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected Server Error");
        }
    }

    @GET
    public List<Ticket> getAllTickets(@QueryParam("name") String nameParam,
                                      @QueryParam("type") String typeParam,
                                      @QueryParam("offset") @DefaultValue("1") int offsetParam,
                                      @QueryParam("limit") @DefaultValue("10") int limitParam) {
        Type type = null;

        System.out.println("get All Ticket");
        final String name = nameParam != null ? nameParam.trim() : null;
        final String typeStr = typeParam != null ? typeParam.trim() : null;

        if (typeStr != null) {
            type = Arrays.stream(Type.values())
                    .filter(x -> x.name().equalsIgnoreCase(typeStr))
                    .findFirst()
                    .orElse(null);


        }

        try {
            List<Ticket> tickets;

            if (name != null && !name.isEmpty() && type != null) {
                tickets = WebServiceRemoteAccess.ticketStore.filterTicketByNameAndType(name, type);
            } else if (name != null && !name.isEmpty()) {
                tickets = WebServiceRemoteAccess.ticketStore.filterTicketByName(name);
            } else if (type != null) {
                tickets = WebServiceRemoteAccess.ticketStore.filterTicketByType(type);
            } else {
                tickets = WebServiceRemoteAccess.ticketStore.getAllTickets();
            }

            System.out.println("get All Ticket : " + tickets.size());
            if(tickets.size()==0) {
                return new ArrayList<Ticket>();//buildResponseObject(Response.Status.OK, new ArrayList<Ticket>());
            }

            tickets.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o.getPriority().getNumerical())));

            List<Ticket> ticketDTOList = paginateTickets(tickets, offsetParam, limitParam).stream()
                    .map(x -> Helper.convertTicketToTicketObject(x))
                    .collect(Collectors.toList());

            return ticketDTOList;
        } catch (Exception e) {
            return new ArrayList<Ticket>();
        }
    }

    @GET
    @Path("{id}")
    public Ticket get(@PathParam("id") int id) {
        Ticket ticket = WebServiceRemoteAccess.ticketStore.getTicket(id);

        if (ticket == null) {
            throw new WebApplicationException("No Ticket found with id: " + id, 404);
        }
        return ticket;
    }

    private <T> Response buildResponseObject(final Response.Status status, final T entity) {
        return Response.status(status)
                .entity(entity)
                .build();
    }

    private List<Ticket> paginateTickets(List<Ticket> tickets, int offset, int limit) {
        int from = offset - 1;
        int to = limit + offset - 1;
        int ticketCount = tickets.size();

        if (from >= ticketCount) {
            return new ArrayList<>();
        }

        if (to >= ticketCount) {
            to = ticketCount;
        }

        return tickets.subList(from, to);
    }
}
