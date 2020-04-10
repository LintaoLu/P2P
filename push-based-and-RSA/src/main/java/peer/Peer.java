package peer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import message.Message;
import message.MessageManager;
import message.MessageType;
import utils.FileData;
import utils.MessageQueue;
import utils.Pair;
import utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Peer {

    private final int myID, myPort, superPeerID, superPeerPort;
    private final String myIP, superPeerIP;
    private PeerManager peerManager;
    private Map<String, FileData> originalFile;
    private Map<String, FileData> downloadFile;
    private Map<Integer, FileData> downloadRequests;
    private Map<Integer, Pair<String, Integer>> upstream;
    private MessageQueue<Message> messageQueue;

    public Peer(int myID, int myPort, String myIP, int superPeerID, String superPeerIP,
                int superPeerPort) throws IOException {
        this.myID = myID; this.myIP = myIP;
        this.superPeerID = superPeerID;
        this.superPeerIP = superPeerIP;
        this.superPeerPort = superPeerPort;
        this.myPort = myPort;
        peerManager = new PeerManager(this);
        originalFile = peerManager.loadMap(MapType.ORIGINAL_FILE);
        downloadFile = peerManager.loadMap(MapType.DOWNLOAD_FILE);
        downloadRequests = new ConcurrentHashMap<>();
        upstream = new ConcurrentHashMap<>();
        messageQueue = new MessageQueue<>(Utils.MESSAGE_QUEUE_SIZE);
    }

    public void registerFile(FileData fileData) {
        Gson gson = new Gson();
        Message request = new Message(Utils.generateMessageID(), MessageType.REGISTER, Utils.TTL, superPeerIP,
                superPeerPort, myIP, myPort, myID);
        request.setMessageBody(gson.toJson(fileData));
        if (fileData.getOwnerId() == getMyID()) originalFile.put(fileData.getFileName(), fileData);
        MessageManager.sendMessage(request);
    }

    public void handleRegisterFile(Message message) { return; }

    public void download(String fileName) {
        if (originalFile.containsKey(fileName)) return;
        Message searchRequest = new Message(Utils.generateMessageID(), MessageType.SEARCH, Utils.TTL, superPeerIP,
                superPeerPort, myIP, myPort, myID);
        searchRequest.setMessageBody(fileName);
        System.out.println("peer" + getMyID() + " generate message " + searchRequest);
        MessageManager.sendMessage(searchRequest);
    }

    public void handleSearch(Message message) {
        String fileName = message.getMessageBody();
        if (!originalFile.containsKey(fileName)) return;
        Gson gson = new Gson();
        Message downloadRequest = new Message(message.getMessageId(), MessageType.FILEMETA, Utils.TTL,
                getUpStreamIP(message.getMessageId()), getUpStreamPort(message.getMessageId()),
                getMyIP(), getMyPort(), message.getSourceID());
        downloadRequest.setMessageBody(gson.toJson(originalFile.get(fileName)));
        System.out.println("peer" + getMyID() + " generate message " + downloadRequest);
        MessageManager.sendMessage(downloadRequest);
    }

    public void handleSearchResult(Message message) {
        if (message.getSourceID() != getMyID()) return;
        Gson gson = new Gson();
        Type type = new TypeToken<FileData>(){}.getType();
        FileData fileData = gson.fromJson(message.getMessageBody(), type);
        Message response = new Message(message.getMessageId(), MessageType.DOWNLOAD, Utils.TTL,
                getUpStreamIP(message.getMessageId()), getUpStreamPort(message.getMessageId()),
                getMyIP(), getMyPort(), message.getSourceID());
        response.setMessageBody(gson.toJson(fileData));
        System.out.println("peer" + getMyID() + " generate message " + response);
        downloadRequests.put(response.getMessageId(), fileData);
        MessageManager.sendMessage(response);
    }

    public void handleDownloadFile(Message message) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<FileData>(){}.getType();
        FileData fileData = gson.fromJson(message.getMessageBody(), type);
        if (fileData.getOwnerId() != getMyID()) return;
        Message response = new Message(message.getMessageId(), MessageType.FILE, Utils.TTL,
                getUpStreamIP(message.getMessageId()), getUpStreamPort(message.getMessageId()),
                getMyIP(), getMyPort(), message.getSourceID());
        response.setMessageBody(fileToString(fileData.getFilePath()));
        MessageManager.sendMessage(response);
    }

    protected String fileToString(String path) throws IOException {
        File file = new File(path);
        //init array with file length
        byte[] bytesArray = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray); //read file into bytes[]
        String str = Base64.getEncoder().encodeToString(bytesArray);
        fis.close();
        return str;
    }

    public void handleFile(Message message) {
        if (message.getSourceID() != getMyID()) return;
        FileData fileData = downloadRequests.get(message.getMessageId());
        String path = peerManager.getFilePath(fileData.getFileName() + "." + fileData.getFileSuffix());
        File file = new File(path);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // To be short I use a corrupted PDF string, so make sure to use a valid one if you want to preview the PDF file
            String b64 = message.getMessageBody();
            byte[] decoder = Base64.getDecoder().decode(b64);
            fos.write(decoder);
            System.out.println(fileData.getFileName() + " is Saved");
        } catch (Exception e) {
            e.printStackTrace();
        }
        int latestVersion = fileData.getVersion();
        FileData newData = peerManager.generateFileData(path, fileData.getOwnerId());
        newData.setVersion(latestVersion);
        downloadFile.put(newData.getFileName(), newData);
    }

    public void modifyFile(String fileName) {
        if (!originalFile.containsKey(fileName)) return;
        int version = originalFile.get(fileName).getVersion();
        originalFile.get(fileName).setVersion(version+1);
        push(fileName);
    }

    public void push(String fileName) {
        Gson gson = new Gson();
        Pair<String, Integer> pair = new Pair<>(fileName, originalFile.get(fileName).getVersion());
        Message request = new Message(Utils.generateMessageID(), MessageType.PUSH, Utils.TTL, superPeerIP,
                superPeerPort, myIP, myPort, myID);
        request.setMessageBody(gson.toJson(pair));
        MessageManager.sendMessage(request);
    }

    public void handlePush(Message message) {
        Gson gson = new Gson();
        Type type = new TypeToken<Pair<String, Integer>>(){}.getType();
        Pair<String, Integer> pair = gson.fromJson(message.getMessageBody(), type);
        String fileName = pair.getKey();
        if (!downloadFile.containsKey(fileName)) return;
        int version = pair.getValue();
        FileData fileData = downloadFile.get(fileName);
        if (version != fileData.getVersion()) {
            fileData.setValid(false);
            fileData.setVersion(version);
        }
    }

    public void refresh(String fileName) {
        download(fileName);
    }

    public void markMessage(Message message) {
        // if (message.getSourceID() == getMyID()) return;
        upstream.put(message.getMessageId(), new Pair<>(message.getCurrentIP(), message.getCurrentPort()));
    }

    public String getUpStreamIP(int messageId) {
        if (!upstream.containsKey(messageId))
            throw new IllegalArgumentException("This message has no upstream IP!");
        return upstream.get(messageId).getKey();
    }

    public int getUpStreamPort(int messageId) {
        if (!upstream.containsKey(messageId))
            throw new IllegalArgumentException("This message has no upstream port!");
        return upstream.get(messageId).getValue();
    }

    public List<String> getLocalFiles() {
        List<String> result = new ArrayList<>();
        String path = peerManager.getFilePath("");
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            result = walk.filter(Files::isRegularFile)
                    .map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) { e.printStackTrace(); }
        Collections.sort(result);
        return result;
    }

    public MessageQueue<Message> getMessageQueue() { return messageQueue; }

    public PeerManager getPeerManager() { return peerManager; }

    public int getMyID() { return myID; }

    public int getMyPort() { return myPort; }

    public String getMyIP() { return myIP; }

    public Map<Integer, Pair<String, Integer>> getUpstream() { return upstream; }

    public FileData getFileData(String fileName, int source) {
        return peerManager.generateFileData(peerManager.getFilePath(fileName), source);
    }

    public Map<Integer, FileData> getDownloadRequests() { return downloadRequests; }

    public Map<String, Map<Integer, FileData>> getFileMetadata() { return null; }

    public void start() { peerManager.startSocketListener(); }

    public void stop() throws IOException { peerManager.stopSocketListener(); }

    public Map<String, FileData> getOriginalFile() { return originalFile; }

    public Map<String, FileData> getDownloadFile() { return downloadFile; }
}