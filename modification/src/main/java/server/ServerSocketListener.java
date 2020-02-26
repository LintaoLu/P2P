package server;

import common.Utils;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Similar to client-side server. Listen on a port number, receive requests, start
 * handle request thread. I use thread pool to limit thread number so I could avoid
 * server overhead.
 * */
public class ServerSocketListener implements Runnable {

    private ServerSocket serverSocket = null;
    private Server server;

    public ServerSocketListener(Server server) { this.server = server; }

    // kill this thread by throwing an exception
    public void stopListener() throws IOException { serverSocket.close(); }

    @Override
    public void run() {

        ExecutorService pool = Executors.newFixedThreadPool(Utils.SERVER_MAX_THREADS);
        try {
            // server port number: 10000
            serverSocket = new ServerSocket(Utils.SERVER_PORT);
            System.out.println("server is started, waiting for requests...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                //delegate to new thread
                pool.execute(new Thread(new ServerHandleRequest(clientSocket, server)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try { serverSocket.close(); }
                catch (IOException e) { e.printStackTrace(); }
            }
            pool.shutdown();
            System.out.println("server is stopped");
        }
    }
}
