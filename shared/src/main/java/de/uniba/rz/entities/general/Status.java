package de.uniba.rz.entities.general;

import java.io.Serializable;

/**
 * Enumeration to describe the Status of a {@link Ticket} or
 * {@link TransferTicket}.
 * 
 * Possible Values:
 * <ul>
 * <li>{@code NEW}</li>
 * <li>{@code ACCEPTED}</li>
 * <li>{@code REJECTED}</li>
 * <li>{@code CLOSED}</li>
 * </ul>
 * 
 */
public enum Status implements Serializable {
	NEW("NEW"), ACCEPTED("ACCEPTED"), REJECTED("REJECTED"), CLOSED("CLOSED");

	final private String value;
	Status(String value) {
		this.value = value;
	}

	public String getValue(){
		return value;
	}
}
