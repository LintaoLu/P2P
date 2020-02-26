package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.FileData;
import common.Request;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Server.
 * */
public class Server {

    // thread safe
    private ConcurrentHashMap<Integer, String> clients;
    private ConcurrentHashMap<String, Map<Integer, FileData>> globalFiles;
    private ServerSocketListener listener;
    private boolean status;

    public Server() throws IOException { initializeServer(); }

    // load map to memory
    private void initializeServer() throws IOException {
        clients = new ConcurrentHashMap<>();
        File databaseFile = new File("global_files.json");
        // if file already exists will do nothing
        if(databaseFile.createNewFile()) System.out.println("global_files.json is created!");
        Gson gson = new Gson();
        Type type = new TypeToken<ConcurrentHashMap<String, Map<Integer, FileData>>>(){}.getType();
        globalFiles = gson.fromJson(new FileReader("global_files.json"), type);
        if (globalFiles == null) globalFiles = new ConcurrentHashMap<>();
    }

    // save map to disk
    public void saveFileMap() {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter("global_files.json")) {
            gson.toJson(globalFiles, writer);
        } catch (IOException e) { e.printStackTrace(); }
        System.out.println("Serialized HashMap data has been saved in global_files.json");
    }

    public void stopServer() throws IOException {
        if (!status) return;
        saveFileMap();
        if (listener != null) listener.stopListener();
        status = false;
    }

    public void startServer() {
        if (status) return;
        listener = new ServerSocketListener(this);
        new Thread(listener).start();
        status =  true;
    }

    // add client id and client ip
    public void registerClient(int id, String ip) {
        clients.put(id, ip);
    }

    // when client posts file metadata to server, we should add it to database (files map)
    public void registerFile(Request request) {
        Gson gson = new Gson();
        FileData fileData = gson.fromJson(request.getMessageBody(), FileData.class);
        String fileName = fileData.getFileName();
        if (!globalFiles.containsKey(fileName)) { globalFiles.put(fileName, new HashMap<>()); }
        globalFiles.get(fileName).put(fileData.getClientId(), fileData);
    }

    public ConcurrentHashMap<Integer, String> getClients() {
        return clients;
    }

    public ConcurrentHashMap<String, Map<Integer, FileData>> getGlobalFiles() {
        return globalFiles;
    }
}