package utils;

/**
 * File metadata
 * */
public class FileData {

    private String fileName;
    private String ip;
    private String filePath;
    private String fileSuffix;
    // who has this file
    private int ownerId;
    private int port;
    private int fileSize;
    // original peer
    private int originalPeer;
    // version
    private int version;
    private boolean isValid;

    public FileData(String fileName, String ip, String filePath,
                    String fileSuffix, int ownerId, int port, int fileSize, int originalPeer) {
        this.fileName = fileName;
        this.ip = ip;
        this.filePath = filePath;
        this.fileSuffix = fileSuffix;
        this.ownerId = ownerId;
        this.port = port;
        this.fileSize = fileSize;
        this.originalPeer = originalPeer;
        isValid = true;
        version = 1;
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

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getOriginalPeer() {
        return originalPeer;
    }

    public void setOriginalPeer(int originalPeer) {
        this.originalPeer = originalPeer;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    @Override
    public String toString() {
        return "FileData{" +
                "fileName='" + fileName + '\'' +
                ", ip='" + ip + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSuffix='" + fileSuffix + '\'' +
                ", ownerId=" + ownerId +
                ", port=" + port +
                ", fileSize=" + fileSize +
                ", originalPeer=" + originalPeer +
                ", version=" + version +
                ", isValid=" + isValid +
                '}';
    }
}
