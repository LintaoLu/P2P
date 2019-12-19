package Client;

import Common.FileMetadata;
import Common.Request;
import Common.Response;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Client
{
    private static final int SERVER_PORT = 10000;

    // start client side server, keep it running
    // probably need thread control
    public Client()
    {
        new Thread(new ClientResponse()).start();
    }

    // connect to client and download file to local machine
    // allows multiple connections
    public void getFile(FileMetadata fileData, String location)
    {
        Request request = new Request(Request.GET_FILE, fileData);
        new Thread(new GetFile(request, location)).start();
    }

    // connect to server and get files list
    // only allows one connection
    public List<FileMetadata> getFileList(String fileName)
    {
        List<FileMetadata> res = new ArrayList<>();
        Request request = new Request(Request.GET_FILE_LIST, fileName);
        Response response = getResponse(request);

        if (response.getResponseType() == Response.MESSAGE)
            System.out.println(response.getMessage());
        else if(response.getResponseType() == Response.FILE_LIST)
            res = response.getFileList();

        return res;
    }

    public boolean clientAuthorization(String clientName, String password) throws UnknownHostException
    {
        boolean isRegistered = false;
        Request request = new Request(Request.GET_REGISTER_STATUS, clientName, password, getIP());
        Response response = getResponse(request);

        if (response.getResponseType() == Response.MESSAGE)
            System.out.println(response.getMessage());
        else if(response.getResponseType() == Response.REGISTRATION_STATUS)
            isRegistered = response.getRegisteredStatus();

        return isRegistered;
    }

    public boolean registerClient(String clientName, String password) throws UnknownHostException
    {
        boolean success = false;
        Request request = new Request(Request.REGISTER_CLIENT, clientName, password, getIP());
        Response response = getResponse(request);

        if (response.getResponseType() == Response.MESSAGE)
            System.out.println(response.getMessage());
        else if(response.getResponseType() == Response.REGISTRATION_STATUS)
            success = response.getRegisteredStatus();

        return success;
    }

    public boolean registerFile(String clientName, String filePath, String fileName) throws UnknownHostException
    {
        FileMetadata fileData = createFileData(clientName, filePath, fileName);

        boolean success = false;
        Request request = new Request(Request.REGISTER_FILE, fileData);
        Response response = getResponse(request);

        if (response.getResponseType() == Response.MESSAGE)
            System.out.println(response.getMessage());
        else if(response.getResponseType() == Response.REGISTRATION_STATUS)
            success = response.getRegisteredStatus();

        return success;
    }

    private String getIP() throws UnknownHostException
    {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostAddress();
    }

    private FileMetadata createFileData(String clientName, String filePath, String fileName) throws UnknownHostException
    {
        String path = filePath + "\\" + fileName;
        // must bigger than real size
        long fileSize = new File(path).length() + 200;
        if(fileSize >= Integer.MAX_VALUE) throw new IllegalArgumentException("File is too big!");
        return new FileMetadata(clientName,  getIP(), fileName, path, fileSize);
    }

    private Response getResponse(Request request)
    {
        Response response = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        Socket socket = null;

        try
        {
            socket = new Socket(getIP(), SERVER_PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(request);
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());
            response = (Response) ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
        finally
        {
            try
            {
                // free memory
                if(ois != null) ois.close();
                if(oos != null) oos.close();
                if(socket != null) socket.close();
            }
            catch (Exception e) { e.printStackTrace(); }
        }

        return response;
    }
}