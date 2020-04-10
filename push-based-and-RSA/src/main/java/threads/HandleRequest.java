package threads;

import message.Message;
import message.MessageManager;
import peer.Peer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HandleRequest implements Runnable {

    private final Socket socket;
    private final Peer peer;

    public HandleRequest(Socket socket, Peer peer) {
        this.socket = socket;
        this.peer = peer;
    }

    @Override
    public void run() {
        InputStreamReader isr = null;

        try {

            System.out.println(peer.getClass().getName() + " " + peer.getMyID() + " accepted connection : " + socket);
            isr = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
            Message message = MessageManager.parseMessage(isr);
            System.out.println(peer.getClass().getName() + " " + peer.getMyID() + " received request: " + message);
            // check if it's a valid message
            if (!isValidMessage(message)) return;
            peer.markMessage(message);
            message.setTTL(message.getTTL()-1);
            message.getVisited().add(peer.getMyID());
            // enqueue
            peer.getMessageQueue().offer(message);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                // free resources
                if (isr != null) isr.close();
                if (socket != null) socket.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private boolean isValidMessage(Message message) { return message.getMessageId() > 0; }
}
