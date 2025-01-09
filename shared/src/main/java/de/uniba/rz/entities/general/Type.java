package de.uniba.rz.entities.general;

import java.io.Serializable;

/**
 * Enumeration to describe the Type of a {@link Ticket} or
 * {@link TransferTicket}.
 * 
 * Possible Values:
 * <ul>
 * <li>{@code TASK}</li>
 * <li>{@code ENHANCEMENT}</li>
 * <li>{@code BUG}</li>
 * <li>{@code QUESTION}</li>
 * </ul>
 * 
 */
public enum Type implements Serializable {

    TASK("TASK"), ENHANCEMENT("ENHANCEMENT"), BUG("BUG"), QUESTION("QUESTION");

    final private String value;
    Type(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
