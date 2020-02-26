package Server;

import Common.FileMetadata;
import Common.Request;
import Common.Response;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

class ServerResponse implements Runnable
{
    private final Socket clientSocket;

    public ServerResponse(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run()
    {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try
        {
            System.out.println("Accepted connection : " + clientSocket);
            // receive request
            ois = new ObjectInputStream(clientSocket.getInputStream());
            Request request = (Request) ois.readObject();
            System.out.println("Received message: " + request);
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            Response response = new Response();
            // register file
            if (request.getRequestType() == Request.REGISTER_FILE)
            {
                response.setResponseType(Response.REGISTRATION_STATUS);
                if (MySqlController.registerFile(request.getFileData()))
                    response.setRegisteredStatus(true);
                else response.setRegisteredStatus(false);
            }
            // send a list of files, maybe an empty list if requested files dose not exist
            else if (request.getRequestType() == Request.GET_FILE_LIST)
            {
                List<FileMetadata> fileList = MySqlController.getFileList(request.getFileName());
                response.setResponseType(Response.FILE_LIST);
                response.setFileList(fileList);
            }
            // check if client has registered
            else if (request.getRequestType() == Request.GET_REGISTER_STATUS)
            {
                boolean res = MySqlController.clientAuthorization(request.getClientName(),
                        request.getPassword(), request.getClientIP());
                response.setResponseType(Response.REGISTRATION_STATUS);
                response.setRegisteredStatus(res);
            }
            // register a client
            else if (request.getRequestType() == Request.REGISTER_CLIENT)
            {
                boolean res = MySqlController.registerClient(request.getClientName(),
                        request.getPassword(), request.getClientIP());
                response.setResponseType(Response.REGISTRATION_STATUS);
                response.setRegisteredStatus(res);
            }
            // request type is wrong
            else
            {
                response.setResponseType(Response.MESSAGE);
                response.setMessage("Server doesn't support this request!");
            }
            oos.writeObject(response);
            oos.flush();
        }
        catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }
        finally
        {
            try
            {
                // free memory
                if(oos != null) oos.close();
                if(ois != null) ois.close();
                if(clientSocket != null) clientSocket.close();
            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}