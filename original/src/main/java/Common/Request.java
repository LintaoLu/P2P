package Common;

import java.io.Serializable;

public class Request implements Serializable
{
    public static final int REGISTER_FILE = 0, GET_FILE_LIST = 1,
            GET_FILE = 2, GET_REGISTER_STATUS = 3, REGISTER_CLIENT = 4;

    private int requestType;
    private FileMetadata fileData;
    private String fileName;
    private String clientName, password, clientIP;

    public Request(int requestType) { this.requestType = requestType; }

    public Request(int requestType, String clientName, String password, String clientIP)
    {
        this.requestType = requestType;
        this.clientName = clientName;
        this.password = password;
        this.clientIP = clientIP;
    }

    public Request(int requestType, FileMetadata fileData)
    {
        this.requestType = requestType;
        this.fileData = fileData;
    }

    public Request(int requestType, String fileName)
    {
        this.requestType = requestType;
        this.fileName = fileName;
    }

    public FileMetadata getFileData() { return fileData; }

    public int getRequestType() { return requestType; }

    public String getFileName() { return fileName; }

    public String getClientName() { return clientName; }

    public String getPassword() { return password; }

    public String getClientIP() { return clientIP; }

    public String toString()
    {
        switch (requestType)
        {
            case 0: return "REGISTER_FILE";
            case 1: return "GET_FILE_LIST";
            case 2: return "GET_FILE";
            case 3: return "GET_REGISTER_STATUS";
            case 4: return "REGISTER_CLIENT";
            default: break;
        }
        return "NONE";
    }
}
