/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables.pojos;


import java.io.Serializable;
import java.time.LocalDateTime;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.14.16"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OdataMessageView implements Serializable {

    private static final long serialVersionUID = 1L;

    private String        id;
    private String        conversationId;
    private String        senderId;
    private Boolean       senderIsCustomer;
    private Boolean       receivedByReceiver;
    private Boolean       readByReceiver;
    private Boolean       containsAttachment;
    private LocalDateTime sendAt;
    private String        message;
    private String        translatedMessage;
    private Boolean       isSystemMessage;

    public OdataMessageView() {}

    public OdataMessageView(OdataMessageView value) {
        this.id = value.id;
        this.conversationId = value.conversationId;
        this.senderId = value.senderId;
        this.senderIsCustomer = value.senderIsCustomer;
        this.receivedByReceiver = value.receivedByReceiver;
        this.readByReceiver = value.readByReceiver;
        this.containsAttachment = value.containsAttachment;
        this.sendAt = value.sendAt;
        this.message = value.message;
        this.translatedMessage = value.translatedMessage;
        this.isSystemMessage = value.isSystemMessage;
    }

    public OdataMessageView(
        String        id,
        String        conversationId,
        String        senderId,
        Boolean       senderIsCustomer,
        Boolean       receivedByReceiver,
        Boolean       readByReceiver,
        Boolean       containsAttachment,
        LocalDateTime sendAt,
        String        message,
        String        translatedMessage,
        Boolean       isSystemMessage
    ) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderIsCustomer = senderIsCustomer;
        this.receivedByReceiver = receivedByReceiver;
        this.readByReceiver = readByReceiver;
        this.containsAttachment = containsAttachment;
        this.sendAt = sendAt;
        this.message = message;
        this.translatedMessage = translatedMessage;
        this.isSystemMessage = isSystemMessage;
    }

    /**
     * Getter for <code>public.odata_message_view.id</code>.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.odata_message_view.id</code>.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.odata_message_view.conversation_id</code>.
     */
    public String getConversationId() {
        return this.conversationId;
    }

    /**
     * Setter for <code>public.odata_message_view.conversation_id</code>.
     */
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * Getter for <code>public.odata_message_view.sender_id</code>.
     */
    public String getSenderId() {
        return this.senderId;
    }

    /**
     * Setter for <code>public.odata_message_view.sender_id</code>.
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * Getter for <code>public.odata_message_view.sender_is_customer</code>.
     */
    public Boolean getSenderIsCustomer() {
        return this.senderIsCustomer;
    }

    /**
     * Setter for <code>public.odata_message_view.sender_is_customer</code>.
     */
    public void setSenderIsCustomer(Boolean senderIsCustomer) {
        this.senderIsCustomer = senderIsCustomer;
    }

    /**
     * Getter for <code>public.odata_message_view.received_by_receiver</code>.
     */
    public Boolean getReceivedByReceiver() {
        return this.receivedByReceiver;
    }

    /**
     * Setter for <code>public.odata_message_view.received_by_receiver</code>.
     */
    public void setReceivedByReceiver(Boolean receivedByReceiver) {
        this.receivedByReceiver = receivedByReceiver;
    }

    /**
     * Getter for <code>public.odata_message_view.read_by_receiver</code>.
     */
    public Boolean getReadByReceiver() {
        return this.readByReceiver;
    }

    /**
     * Setter for <code>public.odata_message_view.read_by_receiver</code>.
     */
    public void setReadByReceiver(Boolean readByReceiver) {
        this.readByReceiver = readByReceiver;
    }

    /**
     * Getter for <code>public.odata_message_view.contains_attachment</code>.
     */
    public Boolean getContainsAttachment() {
        return this.containsAttachment;
    }

    /**
     * Setter for <code>public.odata_message_view.contains_attachment</code>.
     */
    public void setContainsAttachment(Boolean containsAttachment) {
        this.containsAttachment = containsAttachment;
    }

    /**
     * Getter for <code>public.odata_message_view.send_at</code>.
     */
    public LocalDateTime getSendAt() {
        return this.sendAt;
    }

    /**
     * Setter for <code>public.odata_message_view.send_at</code>.
     */
    public void setSendAt(LocalDateTime sendAt) {
        this.sendAt = sendAt;
    }

    /**
     * Getter for <code>public.odata_message_view.message</code>.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Setter for <code>public.odata_message_view.message</code>.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter for <code>public.odata_message_view.translated_message</code>.
     */
    public String getTranslatedMessage() {
        return this.translatedMessage;
    }

    /**
     * Setter for <code>public.odata_message_view.translated_message</code>.
     */
    public void setTranslatedMessage(String translatedMessage) {
        this.translatedMessage = translatedMessage;
    }

    /**
     * Getter for <code>public.odata_message_view.is_system_message</code>.
     */
    public Boolean getIsSystemMessage() {
        return this.isSystemMessage;
    }

    /**
     * Setter for <code>public.odata_message_view.is_system_message</code>.
     */
    public void setIsSystemMessage(Boolean isSystemMessage) {
        this.isSystemMessage = isSystemMessage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OdataMessageView (");

        sb.append(id);
        sb.append(", ").append(conversationId);
        sb.append(", ").append(senderId);
        sb.append(", ").append(senderIsCustomer);
        sb.append(", ").append(receivedByReceiver);
        sb.append(", ").append(readByReceiver);
        sb.append(", ").append(containsAttachment);
        sb.append(", ").append(sendAt);
        sb.append(", ").append(message);
        sb.append(", ").append(translatedMessage);
        sb.append(", ").append(isSystemMessage);

        sb.append(")");
        return sb.toString();
    }
}
