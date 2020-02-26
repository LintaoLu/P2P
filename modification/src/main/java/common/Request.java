package common;


/**
 * Request class contain information about a request.
 * messageBody is a string that can contains encoded information (Java object, Json, string and anything)
 * I didn't use Java "serializable" because it is too heavy, and can only be used by Java application.
 * */
public class Request {

    private int messageType;
    private String messageBody;
    private String destinationIp;
    private int destinationPort;
    private int clientId;

    public Request(int requestType, String requestBody, String destinationIp,
                   int destinationPort, int clientId) {
        this.messageType = requestType;
        this.messageBody = requestBody;
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
        this.clientId = clientId;
    }

    public Request() { }

    @Override
    public String toString() {
        return "Request{" +
                "messageType=" + messageType +
                ", messageBody='" + messageBody + '\'' +
                ", destinationIp='" + destinationIp + '\'' +
                ", destinationPort=" + destinationPort +
                ", clientId=" + clientId +
                '}';
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
}
