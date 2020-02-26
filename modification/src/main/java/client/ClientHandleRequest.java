package client;

import com.google.gson.Gson;
import common.FileData;
import common.Request;
import common.Response;
import common.Utils;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * A thread that handle a request and return a response.
 * */

public class ClientHandleRequest implements Runnable {

    private final Socket clientSocket;
    private Client client;

    public ClientHandleRequest(Socket clientSocket, Client client) {
        this.clientSocket = clientSocket;
        this.client = client;
    }

    /**
     * Return response based on request type.
     * */
    @Override
    public void run()
    {
        client.addTask();

        InputStreamReader isr = null;
        OutputStreamWriter osw = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        Gson gson = new Gson();

        try {
            System.out.println("client server accepted connection : " + clientSocket);
            // receive request
            isr = new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8);
            Request request = parseRequest(isr, gson);
            System.out.println("client server received request: " + request);

            if (request.getMessageType() == Utils.GET_FILE) {
                FileData fileData = gson.fromJson(request.getMessageBody(), FileData.class);
                File myFile = new File (fileData.getFilePath());
                byte [] mybytearray  = new byte [(int)myFile.length()];
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(mybytearray,0,mybytearray.length);
                os = clientSocket.getOutputStream();
                System.out.println("Sending " + fileData.getFilePath() + "(" + mybytearray.length + " bytes)");
                os.write(mybytearray,0,mybytearray.length);
                os.flush();
                System.out.println("file has been sent!");
            }
            else if (request.getMessageType() == Utils.CHECK_FILE) {
                osw = new OutputStreamWriter(clientSocket.getOutputStream());
                Response response = new Response();
                FileData fileData = gson.fromJson(request.getMessageBody(), FileData.class);
                File tempFile = new File(fileData.getFilePath());
                if (tempFile.exists()) response.setResponseType(Utils.RETURN_OK);
                else response.setResponseType(Utils.RETURN_ERROR);
                osw.write(gson.toJson(response));
                osw.write("\n");
                osw.flush();
            }
            else {
                osw = new OutputStreamWriter(clientSocket.getOutputStream());
                Response response = new Response();
                response.setResponseType(Utils.RETURN_ERROR);
                response.setResponseBody("unknown request!");
                osw.write(gson.toJson(response));
                osw.write("\n");
                osw.flush();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.removeTask();
            try {
                // free resources
                if (bis != null) bis.close();
                if (os != null) os.close();
                if (fis != null) fis.close();
                if (osw != null) osw.close();
                if (isr != null) isr.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    /**
     * Input is stream. First buffer all stream in a space, then convert stream to object.
     * */
    private Request parseRequest(InputStreamReader isr, Gson gson) throws IOException {
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0) break;
            sb.append(line);
        }
        return gson.fromJson(sb.toString(), Request.class);
    }
}