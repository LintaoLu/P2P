package threads;

import peer.Peer;
import utils.Utils;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread 1.
 * */

public class SocketListener implements Runnable {

    private ServerSocket serverSocket;
    private Peer peer;

    public SocketListener(Peer peer) { this.peer = peer; }

    // kill this thread by throwing an exception
    public void stopListener() throws IOException { serverSocket.close(); }

    @Override
    public void run() {
        ExecutorService pool = Executors.newFixedThreadPool(Utils.MAX_THREADS);
        try {
            serverSocket = new ServerSocket(peer.getMyPort());
            String peerName = "peer";
            if (peer.getClass().getName().equals("peer.SuperPeer")) peerName = "super peer";
            System.out.println(peerName + " " + peer.getMyID() + " socket listener is started, waiting for requests...");

            while (true) {
                Socket socket = serverSocket.accept();
                // delegate to new thread
                pool.execute(new HandleRequest(socket, peer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { if (serverSocket != null) serverSocket.close(); }
            catch (IOException e) { e.printStackTrace(); }
            finally { pool.shutdown(); }
        }
    }
}