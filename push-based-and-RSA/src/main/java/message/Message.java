package message;

import java.util.HashSet;
import java.util.Set;

public class Message {

    // a global unique message ID
    private final int messageId, sourceId;
    private final MessageType messageType;
    // TTL
    private int TTL;
    // message body
    private String messageBody;
    // destination port number and IP
    private String destinationIP;
    private int destinationPort;
    // sender port and IP
    private String currentIP;
    private int currentPort;
    private Set<Integer> visited;

    public Message(int messageId, MessageType messageType, int TTL, String destinationIP, int destinationPort,
                   String currentIP, int currentPort, int sourceId) {
        this.messageId = messageId;
        this.sourceId = sourceId;
        this.messageType = messageType;
        this.TTL = TTL;
        this.destinationIP = destinationIP;
        this.destinationPort = destinationPort;
        this.currentIP = currentIP;
        this.currentPort = currentPort;
        visited = new HashSet<>();
    }

    public Message(int messageId, MessageType messageType, int TTL, String currentIP,
                   int currentPort, int sourceId) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.TTL = TTL;
        this.currentIP = currentIP;
        this.currentPort = currentPort;
        this.sourceId = sourceId;
        visited = new HashSet<>();
    }

    public int getMessageId() {
        return messageId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public int getTTL() {
        return TTL;
    }

    public void setTTL(int TTL) {
        this.TTL = TTL;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getDestinationIP() {
        return destinationIP;
    }

    public void setDestinationIP(String destinationIP) {
        this.destinationIP = destinationIP;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getCurrentIP() {
        return currentIP;
    }

    public void setCurrentIP(String currentIP) {
        this.currentIP = currentIP;
    }

    public int getCurrentPort() {
        return currentPort;
    }

    public void setCurrentPort(int currentPort) {
        this.currentPort = currentPort;
    }

    public int getSourceID() {
        return sourceId;
    }

    public Set<Integer> getVisited() {
        return visited;
    }

    public void setVisited(Set<Integer> visited) {
        this.visited = visited;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", messageType=" + messageType +
                ", TTL=" + TTL +
                ", messageBody='" + messageBody + '\'' +
                ", destinationIP='" + destinationIP + '\'' +
                ", destinationPort=" + destinationPort +
                ", currentIP='" + currentIP + '\'' +
                ", currentPort=" + currentPort +
                ", sourceID=" + sourceId +
                ", visited=" + visited +
                '}';
    }
}

