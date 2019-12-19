package Common;

import java.io.Serializable;
import java.util.List;

public class Response implements Serializable
{
    public static final int MESSAGE = 0, FILE_LIST = 1, REGISTRATION_STATUS = 2;

    private int responseType;
    private List<FileMetadata> fileList;
    private String message;
    private boolean registeredStatus;

    public Response() {}

    public Response(int responseType, List<FileMetadata> fileList)
    {
        this.responseType = responseType;
        this.fileList = fileList;
    }

    public Response(int responseType, String message)
    {
        this.responseType = responseType;
        this.message = message;
    }

    public int getResponseType() {
        return responseType;
    }

    public List<FileMetadata> getFileList() {
        return fileList;
    }

    public String getMessage() {
        return message;
    }

    public boolean getRegisteredStatus() { return registeredStatus; }

    public void setFileList(List<FileMetadata> fileList) {
        this.fileList = fileList;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResponseType(int responseType) {
        this.responseType = responseType;
    }

    public void setRegisteredStatus(boolean registeredStatus) {
        this.registeredStatus = registeredStatus;
    }
}