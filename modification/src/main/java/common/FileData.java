package common;

import java.util.Objects;


/**
 * File meta data.
 * */
public class FileData {

    private String fileName;
    private String ip;
    private String filePath;
    private String fileSuffix;
    private int clientId;
    private int port;
    private int fileSize;

    public FileData(String fileName, String ip, String filePath,
                    String fileSuffix, int clientId, int port, int fileSize) {
        this.fileName = fileName;
        this.ip = ip;
        this.filePath = filePath;
        this.fileSuffix = fileSuffix;
        this.clientId = clientId;
        this.port = port;
        this.fileSize = fileSize;
    }

    public FileData(){}

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "FileData{" +
                "fileName='" + fileName + '\'' +
                ", ip='" + ip + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSuffix='" + fileSuffix + '\'' +
                ", clientId=" + clientId +
                ", port=" + port +
                ", fileSize=" + fileSize +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileData fileData = (FileData) o;
        return getClientId() == fileData.getClientId() &&
                getPort() == fileData.getPort() &&
                getFileSize() == fileData.getFileSize() &&
                getFileName().equals(fileData.getFileName()) &&
                getIp().equals(fileData.getIp()) &&
                getFilePath().equals(fileData.getFilePath()) &&
                getFileSuffix().equals(fileData.getFileSuffix());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileName(), getIp(), getFilePath(),
                getFileSuffix(), getClientId(), getPort(), getFileSize());
    }
}
