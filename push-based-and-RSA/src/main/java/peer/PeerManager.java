package peer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import threads.ExecuteTask;
import utils.FileData;
import org.apache.commons.lang3.SystemUtils;
import threads.SocketListener;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PeerManager {

    private Peer peer;
    private SocketListener listener;
    private Thread executeTask;
    private boolean status;

    protected PeerManager(Peer peer) { this.peer = peer; }

    protected void stopSocketListener() throws IOException {
        if (!status) return;
        saveFileMetadata();
        saveFileMap(MapType.ORIGINAL_FILE);
        saveFileMap(MapType.DOWNLOAD_FILE);
        if (listener != null) listener.stopListener();
        if (executeTask != null) executeTask.interrupt();
        status = false;
    }

    protected void startSocketListener() {
        if (status) return;
        listener = new SocketListener(peer);
        executeTask = new Thread(new ExecuteTask(peer));
        new Thread(listener).start();
        executeTask.start();
        status =  true;
    }

    // save maps to disk
    protected void saveFileMap(MapType type) {
        Map<String, FileData> map = null;
        String mapName = null;
        if (type == MapType.ORIGINAL_FILE) {
            map = peer.getOriginalFile();
            mapName = "originalFiles.json";
        }
        else if (type == MapType.DOWNLOAD_FILE) {
            map = peer.getDownloadFile();
            mapName = "downloadFiles.json";
        }
        if (map == null) return;
        String path = getFilePath(mapName);
        if (path == null || path.length() == 0) return;
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(map, writer);
        } catch (IOException e) { e.printStackTrace(); }
        System.out.println("Serialized HashMap data has been saved to " + path);
    }

    protected void saveFileMetadata() {
        Map<String, Map<Integer, FileData>> fileMetadata = peer.getFileMetadata();
        if (fileMetadata == null) return;
        String path = getFilePath("fileMetadata.json");
        if (path == null || path.length() == 0) return;
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(fileMetadata, writer);
        } catch (IOException e) { e.printStackTrace(); }
        System.out.println("Serialized HashMap data has been saved to " + path);
    }

    protected ConcurrentHashMap<String, Map<Integer, FileData>> loadFileMetaData() throws IOException {
        String path = getFilePath("fileMetadata.json");
        if (path == null || path.length() == 0) return new ConcurrentHashMap<>();
        File file = new File(path);
        // if file already exists will do nothing
        if (file.createNewFile()) System.out.println(path + " is created!");
        Gson gson = new Gson();
        Type type = new TypeToken<ConcurrentHashMap<String, Map<Integer, FileData>>>(){}.getType();
        ConcurrentHashMap<String, Map<Integer, FileData>> fileMetadata = gson.fromJson(new FileReader(path), type);
        if (fileMetadata == null) fileMetadata = new ConcurrentHashMap<>();
        return fileMetadata;
    }

    protected Map<String, FileData> loadMap(MapType mapType) throws IOException {
        String path = null;
        if (mapType == MapType.DOWNLOAD_FILE) path = getFilePath("downloadFiles.json");
        else if (mapType == MapType.ORIGINAL_FILE) path = getFilePath("originalFiles.json");
        if (path == null) return new ConcurrentHashMap<>();
        File file = new File(path);
        // if file already exists will do nothing
        if (file.createNewFile()) System.out.println(path + " is created!");
        Gson gson = new Gson();
        Type type = new TypeToken<ConcurrentHashMap<String, FileData>>(){}.getType();
        Map<String, FileData> map = gson.fromJson(new FileReader(path), type);
        if (map == null) map = new ConcurrentHashMap<>();
        return map;
    }

    protected FileData generateFileData(String filePath, int source) {
        long fileSize = new File(filePath).length();
        if(fileSize >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("File is too big!");
        String fileName = "", fileSuffix = "";
        String[] arr1 = new String[0], arr2;
        if (SystemUtils.IS_OS_WINDOWS)
            arr1 = filePath.split("\\\\");
        if (SystemUtils.IS_OS_LINUX)
            arr1 = filePath.split("/");;
        arr2 = arr1[arr1.length-1].split("\\.");
        if (arr2.length > 0) fileName = arr2[0];
        if (arr2.length > 1) fileSuffix = arr2[1];
        if (fileName.equals("")) throw new IllegalArgumentException("file doesn't exist!");
        return new FileData(fileName, peer.getMyIP(), filePath, fileSuffix, peer.getMyID(), peer.getMyPort(), (int)fileSize, source);
    }

    protected String getFilePath(String fileName) {
        String path = null;
        if (SystemUtils.IS_OS_WINDOWS) path = "GnutellaP2P\\peer_" + peer.getMyID() + "\\" + fileName;
        else if (SystemUtils.IS_OS_LINUX) path = "GnutellaP2P/peer_" + peer.getMyID() + "/" + fileName;
        return path;
    }
}
