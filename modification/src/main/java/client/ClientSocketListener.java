package client;

import common.Utils;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Socket listener listen on a port number, once it receive a legal request,
 * it will build a connection, and then start a handle request thread to deal with this
 * request.
 * */
public class ClientSocketListener implements Runnable {

    private ServerSocket serverSocket;
    private Client client;

    public ClientSocketListener(Client client) {
        this.client = client;
    }

    public void stopListener() throws IOException { serverSocket.close(); }

    @Override
    public void run() {

        ExecutorService pool = Executors.newFixedThreadPool(Utils.CLIENT_MAX_THREADS);
        try {
            serverSocket = new ServerSocket(client.getClientPort());
            System.out.println("client " + client.getClientId() + " server is started, waiting for requests...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // delegate to new thread
                pool.execute(new Thread(new ClientHandleRequest(clientSocket, client)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { if (serverSocket != null) serverSocket.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
        System.out.println("client " + client.getClientId() + " server is stopped!");
    }
}
