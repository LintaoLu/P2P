package Client;

import java.io.*;
import java.net.ServerSocket;

public class ClientResponse implements Runnable
{
    @Override
    public void run()
    {
        ServerSocket servsock = null;
        try
        {
            servsock = new ServerSocket(9999);
            while (true)
            {
                System.out.println("Client side server started, waiting for request...");
                new Thread(new SendFile(servsock.accept())).start();
            }
        }
        catch (IOException e) { e.printStackTrace(); }
        finally
        {
            try { if (servsock != null) servsock.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}