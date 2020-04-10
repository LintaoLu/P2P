package threads;

import message.Message;
import message.MessageType;
import peer.Peer;
import java.io.IOException;

public class ExecuteTask implements Runnable {

    private Peer peer;
    public ExecuteTask(Peer peer) { this.peer = peer; }

    @Override
    public void run() {
        while (true) {
            try {
                Message message = peer.getMessageQueue().poll();
                MessageType requestType = message.getMessageType();

                switch (requestType) {
                    case REGISTER:
                        peer.handleRegisterFile(message);
                        break;
                    case SEARCH:
                        peer.handleSearch(message);
                        break;
                    case FILEMETA:
                        peer.handleSearchResult(message);
                        break;
                    case DOWNLOAD:
                        peer.handleDownloadFile(message);
                        break;
                    case FILE:
                        peer.handleFile(message);
                        break;
                    case PUSH:
                        peer.handlePush(message);
                        break;
                    default:
                        break;
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
