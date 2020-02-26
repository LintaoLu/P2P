package server;

import com.google.gson.Gson;
import common.FileData;
import common.Request;
import common.Response;
import common.Utils;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse request, do some work, return response.
 * */
public class ServerHandleRequest implements Runnable {

    private final Socket clientSocket;
    private Server server;

    public ServerHandleRequest(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        InputStreamReader isr = null;
        OutputStreamWriter osw = null;
        Gson gson = new Gson();

        try {
            System.out.println("server accepted connection : " + clientSocket);
            // receive request
            isr = new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.length() == 0) break;
                sb.append(line);
            }
            Request request = gson.fromJson(sb.toString(), Request.class);
            System.out.println("server received request: " + request);

            osw = new OutputStreamWriter(clientSocket.getOutputStream());
            Response response = new Response();

            if (request.getMessageType() == Utils.POST_FILE) {
                server.registerFile(request);
                response.setResponseType(Utils.RETURN_OK);
                response.setResponseBody("file has been registered!");
            }
            else if (request.getMessageType() == Utils.FIND_FILE) {
                String fileName = request.getMessageBody();
                List<FileData> list = new ArrayList<>();
                if(server.getGlobalFiles().containsKey(fileName)) {
                    for(FileData fileData : server.getGlobalFiles().get(fileName).values()) {
                        int peerId = fileData.getClientId();
                        if(server.getClients().containsKey(peerId) && request.getClientId() != peerId) {
                            fileData.setIp(server.getClients().get(peerId));
                            list.add(fileData);
                        }
                    }
                }
                response.setResponseType(Utils.RETURN_FILE_LIST);
                response.setResponseBody(gson.toJson(list));
            }
            else if(request.getMessageType() == Utils.UPDATE_IP) {
                server.registerClient(request.getClientId(), request.getMessageBody());
                response.setResponseType(Utils.RETURN_OK);
                response.setResponseBody("client " + request.getClientId() + " ip has been updated!");
            }
            else if (request.getMessageType() == Utils.REMOVE_IP) {
                server.getClients().remove(request.getClientId());
                response.setResponseType(Utils.RETURN_OK);
                response.setResponseBody("client " + request.getClientId() + " ip has been removed!");
            }
            else if (request.getMessageType() == Utils.DELETE_FILE) {
                String fileName = request.getMessageBody();
                int clientId = request.getClientId();
                if (server.getGlobalFiles().containsKey(fileName)) {
                    server.getGlobalFiles().get(fileName).remove(clientId);
                    if (server.getGlobalFiles().get(fileName).size() == 0)
                        server.getGlobalFiles().remove(fileName);
                    response.setResponseType(Utils.RETURN_OK);
                    response.setResponseBody(fileName + " has been deleted!");
                }
                else {
                        response.setResponseType(Utils.RETURN_ERROR);
                        response.setResponseBody(fileName + " doesn't exist!");
                }
            }
            else {
                    response.setResponseType(Utils.RETURN_ERROR);
                    response.setResponseBody("unknown request!");
            }

            osw.write(gson.toJson(response));
            osw.write("\n");
            osw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // free memory
                if(osw != null) osw.close();
                if(isr != null) isr.close();
                if(clientSocket != null) clientSocket.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}
