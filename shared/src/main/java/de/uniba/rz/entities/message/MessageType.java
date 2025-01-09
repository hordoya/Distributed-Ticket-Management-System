package de.uniba.rz.entities.message;

public enum MessageType {
    //Client constraints
    RETRIEVE_ALL,
    RETRIEVE_ONE,
    SAVE,
    UPDATE,

    //Server constraints
    SEND_ALL,
    SEND_ONE,
    SAVE_OK,
    UPDATE_OK
}
