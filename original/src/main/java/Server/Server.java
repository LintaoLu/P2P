package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
    public static void main (String [] args )
    {
        ServerSocket serverSocket = null;
        try
        {
            // server port number: 10000
            serverSocket = new ServerSocket(10000);
            System.out.println("server is started, waiting for requests...");

            while (true)
            {
                Socket clientSocket = serverSocket.accept();
                //delegate to new thread
                new Thread(new ServerResponse(clientSocket)).start();
            }
        }
        catch (IOException e) { e.printStackTrace(); }
        finally
        {
            try { if (serverSocket != null) serverSocket.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}