package message;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MessageManager {

    public static Message parseMessage(InputStreamReader isr) throws IOException {
        Gson gson = new Gson();
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0) break;
            sb.append(line);
        }
        return gson.fromJson(sb.toString(), Message.class);
    }

    public static void updateMessage(Message message, String currentIp, int currentPort,
                                     String destinationIP, int destinationPort, int peerId) {
        message.getVisited().add(peerId);
        message.setCurrentPort(currentPort);
        message.setCurrentIP(currentIp);
        message.setDestinationIP(destinationIP);
        message.setDestinationPort(destinationPort);
    }

    public static void sendMessage(Message message) {
        Gson gson = new Gson();
        OutputStreamWriter osw = null;
        Socket socket = null;

        try {
            socket = new Socket(message.getDestinationIP(), message.getDestinationPort());
            osw = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            osw.write(gson.toJson(message) + "\n");
            // '\n' is necessary because readLine method will be blocked without it.
            osw.write("\n");
            osw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { // free resources
                if (osw != null) osw.close();
                if (socket != null) socket.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
