package de.uniba.rz.entities.message;

import java.io.Serializable;

public class MessageDTO implements Serializable {
    private MessageType messageType;
    private int optionalTicketId;
    private MessageBody messageBody;

    public MessageDTO() {

    }

    public MessageDTO(MessageType messageType, MessageBody messageBody, int optionalTicketId){
        this.messageBody = messageBody;
        this.messageType = messageType;
        this.optionalTicketId = optionalTicketId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageBody getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(MessageBody messageBody) {
        this.messageBody = messageBody;
    }

    public int getOptionalTicketId() {
        return optionalTicketId;
    }

    public void setOptionalTicketId(int optionalTicketId) {
        this.optionalTicketId = optionalTicketId;
    }

    public String toString() {
        return "MessageDTO [messageType=" + messageType + ", optionalTicketId=" + optionalTicketId
                + ", messageBody=" + messageBody + "]";
    }
}
