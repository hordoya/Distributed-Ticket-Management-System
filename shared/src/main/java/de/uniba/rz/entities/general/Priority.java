package de.uniba.rz.entities.general;

import java.io.Serializable;

/**
 * Enumeration to describe the Priority of a {@link Ticket} or
 * {@link TransferTicket}.
 *
 * Possible Values:
 * <ul>
 * <li>{@code CRITICAL}</li>
 * <li>{@code MAJOR}</li>
 * <li>{@code MINOR}</li>
 * </ul>
 *
 */
public enum Priority implements Serializable {
	CRITICAL("CRITICAL", 3), MAJOR("MAJOR", 2), MINOR("MINOR", 1);

	final private String value;
	final private int numerical;
	Priority(String value, int numerical) {
		this.value = value;
		this.numerical = numerical;
	}

	public String getValue(){
		return value;
	}

	public int getNumerical() {
		return numerical;
	}
}
