package peer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import message.Message;
import message.MessageManager;
import message.MessageType;
import utils.FileData;
import utils.Pair;
import utils.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SuperPeer extends Peer {

    // key: super peer ID, value: (super peer IP, super peer port)
    private Map<Integer, Pair<String, Integer>> neighbours;
    // key: client ID, value: (client IP, client port)
    private Map<Integer, Pair<String, Integer>> clients;
    // key: file name, value: <peer ID, file metadata>
    private Map<String, Map<Integer, FileData>> fileMetadata;

    public SuperPeer(int ID, int port, String IP, int superPeerID, String superPeerIP,
                     int superPeerPort) throws IOException {
        super(ID, port, IP, superPeerID, superPeerIP, superPeerPort);
        neighbours = new HashMap<>();
        clients = new HashMap<>();
        fileMetadata = getPeerManager().loadFileMetaData();
    }

    public void registerFile(FileData fileData) {
        String fileName = fileData.getFileName();
        if (!fileMetadata.containsKey(fileName)) fileMetadata.put(fileName, new HashMap<>());
        fileMetadata.get(fileName).put(getMyID(), fileData);
        if (fileData.getOwnerId() == getMyID()) getOriginalFile().put(fileData.getFileName(), fileData);
    }

    public void handleRegisterFile(Message message) {
        Gson gson = new Gson();
        FileData fileData = gson.fromJson(message.getMessageBody(), FileData.class);
        String fileName = fileData.getFileName();
        int sourceId = message.getSourceID();
        if (!getFileMetadata().containsKey(fileName)) { getFileMetadata().put(fileName, new HashMap<>()); }
        getFileMetadata().get(fileName).put(sourceId, fileData);
    }

    public void download(String fileName) {
        if (getOriginalFile().containsKey(fileName)) return;
        Message searchRequest = new Message(Utils.generateMessageID(), MessageType.SEARCH, Utils.TTL,
                getMyIP(), getMyPort(), getMyID());
        searchRequest.setMessageBody(fileName);
        System.out.println("peer" + getMyID() + " generate message " + searchRequest);
        if (fileMetadata.containsKey(fileName)) {
            broadCastToPeer(searchRequest);
        }
        broadcastToSuperPeer(searchRequest);
    }

    public void handleSearch(Message message) {
        String fileName = message.getMessageBody();
        if (getOriginalFile().containsKey(fileName)) {
            Gson gson = new Gson();
            Message downloadRequest = new Message(message.getMessageId(), MessageType.FILEMETA, Utils.TTL,
                    getUpStreamIP(message.getMessageId()), getUpStreamPort(message.getMessageId()),
                    getMyIP(), getMyPort(), message.getSourceID());
            downloadRequest.setMessageBody(gson.toJson(getOriginalFile().get(fileName)));
            System.out.println("peer" + getMyID() + " generate message " + downloadRequest);
            MessageManager.sendMessage(downloadRequest);
            return;
        }
        if (fileMetadata.containsKey(fileName)) {
            broadCastToPeer(message);
        }
        broadcastToSuperPeer(message);
    }

    public void handleSearchResult(Message message) {
        Gson gson = new Gson();
        Type type = new TypeToken<FileData>(){}.getType();
        FileData fileData = gson.fromJson(message.getMessageBody(), type);
        if (message.getSourceID() == getMyID()) {
            Message response = new Message(message.getMessageId(), MessageType.DOWNLOAD, Utils.TTL,
                    getUpStreamIP(message.getMessageId()), getUpStreamPort(message.getMessageId()),
                    getMyIP(), getMyPort(), getMyID());
            response.setMessageBody(gson.toJson(fileData));
            getDownloadRequests().put(response.getMessageId(), fileData);
            System.out.println("peer" + getMyID() + " generate message " + response);
            MessageManager.sendMessage(response);
            return;
        }
        if (clients.containsKey(message.getSourceID())) {
            broadCastToPeer(message);
        }
        else broadcastToSuperPeer(message);
    }

    public void handleDownloadFile(Message message) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<FileData>(){}.getType();
        FileData fileData = gson.fromJson(message.getMessageBody(), type);
        if (fileData.getOriginalPeer() == getMyID()) {
            Message response = new Message(message.getMessageId(), MessageType.FILE, Utils.TTL,
                    getUpStreamIP(message.getMessageId()), getUpStreamPort(message.getMessageId()),
                    getMyIP(), getMyPort(), message.getSourceID());
            response.setMessageBody(fileToString(fileData.getFilePath()));
            System.out.println("peer" + getMyID() + " generate message " + response);
            MessageManager.sendMessage(response);
            return;
        }
        if (fileMetadata.containsKey(fileData.getFileName())) {
            broadCastToPeer(message);
        }
        broadcastToSuperPeer(message);
    }

    public void handleFile(Message message) {
        FileData fileData = getDownloadRequests().get(message.getMessageId());
        if (message.getSourceID() == getMyID()) {
            String path = getPeerManager().getFilePath(fileData.getFileName() + "." + fileData.getFileSuffix());
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
            FileData newData = getPeerManager().generateFileData(path, fileData.getOriginalPeer());
            newData.setVersion(latestVersion);
            getDownloadFile().put(newData.getFileName(), newData);
            return;
        }
        if (clients.containsKey(message.getSourceID())) {
            broadCastToPeer(message);
        }
        else broadcastToSuperPeer(message);
    }

    public void push(String fileName) {
        Gson gson = new Gson();
        int version = getOriginalFile().get(fileName).getVersion();
        Pair<String, Integer> pair = new Pair<>(fileName, version);
        Message request = new Message(Utils.generateMessageID(), MessageType.PUSH, Utils.TTL,
                getMyIP(), getMyPort(), getMyID());
        request.setMessageBody(gson.toJson(pair));
        if (fileMetadata.containsKey(fileName)) {
            broadCastToPeer(request);
        }
        else broadcastToSuperPeer(request);
    }

    public void handlePush(Message message) {
        Gson gson = new Gson();
        Type type = new TypeToken<Pair<String, Integer>>(){}.getType();
        Pair<String, Integer> pair = gson.fromJson(message.getMessageBody(), type);
        String fileName = pair.getKey();
        int version = pair.getValue();
        if (getDownloadFile().containsKey(fileName)) {
            FileData fileData = getDownloadFile().get(fileName);
            if (version != fileData.getVersion()) {
                fileData.setValid(false);
                fileData.setVersion(version);
            }
            return;
        }
        if (fileMetadata.containsKey(fileName)) {
            fileMetadata.get(fileName).get(message.getSourceID()).setVersion(version);
        }
        broadCastToPeer(message);
        broadcastToSuperPeer(message);
    }

    private void broadcastToSuperPeer(Message message) {
        for (Map.Entry<Integer, Pair<String, Integer>> entry : neighbours.entrySet()) {
            if (message.getVisited().contains(entry.getKey())) continue;
            Pair<String, Integer> neighbour = entry.getValue();
            MessageManager.updateMessage(message, getMyIP(), getMyPort(), neighbour.getKey(),
                    neighbour.getValue(), getMyID());
            MessageManager.sendMessage(message);
        }
    }

    public void broadCastToPeer(Message message) {
        for (Map.Entry<Integer, Pair<String, Integer>> entry : clients.entrySet()) {
            if (message.getVisited().contains(entry.getKey())) continue;
            Pair<String, Integer> neighbour = entry.getValue();
            MessageManager.updateMessage(message, getMyIP(), getMyPort(), neighbour.getKey(),
                    neighbour.getValue(), entry.getKey());
            MessageManager.sendMessage(message);
        }
    }

    public void addClient(int clientID, String clientIP, int clientPort) {
        clients.put(clientID, new Pair<>(clientIP, clientPort));
    }

    public Map<Integer, Pair<String, Integer>> getNeighbours() {
        return neighbours;
    }

    public Map<String, Map<Integer, FileData>> getFileMetadata() {
        return fileMetadata;
    }
}
