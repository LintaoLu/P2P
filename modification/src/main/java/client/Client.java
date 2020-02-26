package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.FileData;
import common.Request;
import common.Response;
import common.Utils;
import org.apache.commons.lang3.SystemUtils;
import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Client class is a controller. It can start or close socket listener (only one),
 * register file, download file, register client IP and so on.
 * */

public class Client {

    private final int clientId, clientPort;
    private final String clientIp;
    private ClientSocketListener clientSocketListener;
    private boolean status;
    private AtomicLong registerResponseTime, searchResponseTime, deleteResponseTime;
    private AtomicInteger registerCounter, searchCounter, deleteCounter, workingThread;

    /**
     * Initialization. Also check if client id is valid (valid id should form 1 ~ 10,
     * because this application is running on a single computer, I only allocated 10
     * ports, if it is running in cloud, number of client has no limitation because
     * all clients' can use a same port number).
     * */
    public Client(int clientId) throws UnknownHostException {
        this.clientId = clientId;
        registerResponseTime = new AtomicLong(0);
        searchResponseTime = new AtomicLong(0);
        deleteResponseTime = new AtomicLong(0);
        registerCounter = new AtomicInteger(0);
        searchCounter = new AtomicInteger(0);
        workingThread = new AtomicInteger(0);
        deleteCounter = new AtomicInteger(0);
        clientIp = getIP();
        if (clientId > 10 || clientId <= 0)
            throw new IllegalArgumentException("Invalid Client ID!");
        clientPort = Utils.CLIENT_PORT[clientId-1];
        // when client starts, it should register itself on server
        clientSocketListener = (new ClientSocketListener(this));
    }

    /**
     * Stop client socket listener.
     * 1. If client socket listener is already stopped (status == false) do nothing
     * 2. If client socket listener is working (workingThread == true), do not close it
     *    immediately, wait until the task is done.
     * 3. Unregister this client, so sever know this client is disconnected. If other
     *    clients require data that saves on this client, server will send empty list
     *    to them.
     * */
    public void stopClientServer() throws IOException {
        if (!status) return;
        if (hasTask()) System.out.println("there are some working tasks, please wait...");
        while (hasTask()) { }
        unregisterClient();
        clientSocketListener.stopListener();
        status = false;
    }

    /**
     * Start client socket listener.
     * 1. If client socket listener is already started (status == true) do nothing.
     * 2. Register this client. First reason is to let server know its IP address (
     *    IP address may changed so I didn't hard code it).
     *    Second reason is to let server know this client is now connected to peer to
     *    peer system and could share data.
     * */
    public void startClientServer() {
        if (status) return;
        new Thread(clientSocketListener).start();
        registerClient();
        status = true;
    }

    /**
     * Send a message to server (contains this client's IP) to register this client.
     * */
    private void registerClient() {
        Request request = new Request(Utils.UPDATE_IP, clientIp,
                Utils.SERVER_IP, Utils.SERVER_PORT, clientId);
        Response response = getResponse(request);
        System.out.println(response.getResponseBody());
    }

    /**
     * Send a message to server (contains this client's IP) to unregister this client.
     * */
    private void unregisterClient() {
        Request request = new Request(Utils.REMOVE_IP, clientIp,
                Utils.SERVER_IP, Utils.SERVER_PORT, clientId);
        Response response = getResponse(request);
        System.out.println(response.getResponseBody());
    }

    /**
     * Get client IP address.
     * */
    private String getIP() throws UnknownHostException {
        String ip = "";
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException e) { e.printStackTrace(); }
        return ip;
    }

    /**
     * Send file's meta data to server, do post
     */
    public void registerFile(FileData fileData) {
        Gson gson = new Gson();;
        Request request = new Request(Utils.POST_FILE, gson.toJson(fileData),
                Utils.SERVER_IP, Utils.SERVER_PORT, clientId);
        System.out.println(request);
        long createTime = System.currentTimeMillis();
        Response response = getResponse(request);
        registerResponseTime.addAndGet(System.currentTimeMillis() - createTime);
        registerCounter.incrementAndGet();
        System.out.println(response.getResponseBody());
    }

    /**
     * Tell sever this file is deleted.
     * */
    public void deleteFile(String fileName) {
        Request request = new Request(Utils.DELETE_FILE, fileName,
                Utils.SERVER_IP, Utils.SERVER_PORT, clientId);
        long createTime = System.currentTimeMillis();
        Response response = getResponse(request);
        deleteResponseTime.addAndGet(System.currentTimeMillis() - createTime);
        deleteCounter.incrementAndGet();
        System.out.println(response.getResponseBody());
    }

    /**
     * Find a list of files from server according to file name, do get.
     * */
    public List<FileData> findFile(String fileName) {
        Request request = new Request(Utils.FIND_FILE, fileName,
                Utils.SERVER_IP, Utils.SERVER_PORT, clientId);
        long createTime = System.currentTimeMillis();
        Response response = getResponse(request);
        searchResponseTime.addAndGet(System.currentTimeMillis() - createTime);
        searchCounter.incrementAndGet();
        Gson gson = new Gson();
        Type type = new TypeToken<List<FileData>>(){}.getType();
        List<FileData> list = gson.fromJson(response.getResponseBody(), type);
        if (list == null) list = new ArrayList<>();
        return list;
    }

    /**
     * Start download file thread, so it can download files simultaneously.
     * Before download file, first check if the file still exist.
     * */
    public void getFile(FileData fileData) {
        if (fileData == null) throw new IllegalArgumentException("fileData is null!");
        Gson gson = new Gson();
        Request request = new Request(Utils.CHECK_FILE, gson.toJson(fileData),
                fileData.getIp(), fileData.getPort(), clientId);
        Response response = getResponse(request);
        if (response.getResponseType() != Utils.RETURN_OK) {
            System.out.println("cannot download file " + fileData.getFileName() + "!");
            return;
        }
        System.out.println(response + " file exist!");
        request.setMessageType(Utils.GET_FILE);
        new Thread(new DownloadFile(request, fileData, this)).start();
    }

    /**
     * @param request: a message that contains request type, request information and so on.
     *
     * @return response: a message from server. Response body is encoded string can could
     *                   be use to transmit any data or objects.
     * */
    private Response getResponse(Request request) {

        Gson gson = new Gson();
        Response response = null;
        OutputStreamWriter osw = null;
        InputStreamReader isr = null;
        Socket socket = null;

        try {
            socket = new Socket(request.getDestinationIp(), request.getDestinationPort());
            osw = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            osw.write(gson.toJson(request) + "\n");
            // Line break is necessary because readLine method may be blocked without it.
            osw.write("\n");
            osw.flush();
            isr = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.length() == 0) break;
                sb.append(line).append("\n");
            }
            response = gson.fromJson(sb.toString(), Response.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // free resources
                if (isr != null) isr.close();
                if (osw != null) osw.close();
                if (socket != null) socket.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
        return response;
    }

    /**
     * String (path) parser. Input is a path, this function should use this path to find the file,
     * get it size, prefix and some metadata. Return a file data object.
     * */
    public FileData generateFileData(String filePath) {
        long fileSize = new File(filePath).length() + 200;
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
        if (fileName.equals(""))
            throw new IllegalArgumentException("file doesn't exist!");
        return new FileData(fileName, clientIp, filePath, fileSuffix, clientId, clientPort, (int)fileSize);
    }

    /**
     * Find all files that under this client's folder.
     * Return a list.
     * */
    public List<String> getLocalFiles() {
        List<String> result = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Paths.get("client"+ clientId +"'s_files"))) {
            result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
        } catch (IOException e) { e.printStackTrace(); }
        Collections.sort(result);
        return result;
    }

    public void addTask() {
        workingThread.incrementAndGet();
    }

    public void removeTask() {
        workingThread.decrementAndGet();
    }

    public boolean hasTask() {
        return workingThread.get() != 0;
    }

    public int getClientId() {
        return clientId;
    }

    public int getClientPort() { return clientPort; }

    public long getDeleteFileResponseTime() {
        return deleteResponseTime.get() / deleteCounter.get();
    }

    public long getSearchFileResponseTime() {
        return searchResponseTime.get() / searchCounter.get();
    }

    public long getRegisterFileResponseTime() {
        return registerResponseTime.get() / registerCounter.get();
    }
}