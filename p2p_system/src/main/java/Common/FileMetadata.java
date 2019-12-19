package Common;

import java.io.Serializable;

public class FileMetadata implements Serializable
{
    private String clientName;
    private String clientIP;
    private String fileName;
    private String filePath;
    private long fileSize;

    public FileMetadata(String clientName, String clientIP,
                    String fileName, String filePath, long fileSize)
    {
        this.clientName = clientName;
        this.clientIP = clientIP;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    public FileMetadata() {}

    public String getClientIP() { return clientIP; }

    public String getFileName() { return fileName; }

    public String getFilePath() { return filePath; }

    public void setClientIP(String clientIP) { this.clientIP = clientIP; }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }

    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String toString()
    {
        return "[clientName=" + clientName + " clientIP=" + clientIP + " fileName="
                + fileName + " filePath=" + filePath + " fileSize=" + fileSize + "]";
    }
}