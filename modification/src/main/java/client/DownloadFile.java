package client;

import com.google.gson.Gson;
import common.FileData;
import common.Request;
import common.Utils;
import org.apache.commons.lang3.SystemUtils;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


/**
 * Client can download multiple files at the same time. So I use thread.
 * This thread find the files form clients folder, convert this file to
 * binary stream and send it to other client.
 * */
public class DownloadFile implements Runnable {

    private Request request;
    private String localPath;
    private FileData fileData;
    private Client client;
    private long createTime;


    public DownloadFile(Request request, FileData fileData, Client client) {
        createTime = System.currentTimeMillis();
        this.request = request;
        this.client = client;
        this.fileData = fileData;
        File dir = new File("client" + client.getClientId() + "'s_files");
        if (!dir.exists()) dir.mkdirs();
        if (SystemUtils.IS_OS_WINDOWS)
            localPath = "client" +  client.getClientId() + "'s_files\\" +
                    fileData.getFileName() + "." + fileData.getFileSuffix();
        if (SystemUtils.IS_OS_LINUX)
            localPath = "client" +  client.getClientId() + "'s_files/" +
                    fileData.getFileName() + "." + fileData.getFileSuffix();
        client.addTask();
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        OutputStreamWriter osw = null;
        InputStream is = null;
        Socket socket = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            socket = new Socket(request.getDestinationIp(), request.getDestinationPort());
            osw = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            osw.write(gson.toJson(request) + "\n");
            // Line break is necessary because readLine method may be blocked without it.
            osw.write("\n");
            osw.flush();

            // receive file
            byte [] mybytearray  = new byte [fileData.getFileSize()];
            is = socket.getInputStream();
            fos = new FileOutputStream(localPath);
            bos = new BufferedOutputStream(fos);
            int bytesRead = is.read(mybytearray,0,mybytearray.length);
            int current = bytesRead;

            do {
                bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead > -1);

            bos.write(mybytearray, 0 , current);
            bos.flush();
            System.out.println("File " + localPath + " has been downloaded (" + current + " bytes read)");
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // free resources
                if(is != null) is.close();
                if(osw != null) osw.close();
                if(socket != null) socket.close();
                if(fos != null) fos.close();
                if(bos != null) bos.close();
            } catch (Exception e) { e.printStackTrace(); }
        }

        // register file on server because we have one more copy
        FileData fileData = client.generateFileData(localPath);
        client.registerFile(fileData);
        client.removeTask();
        long downloadTime = System.currentTimeMillis() - createTime;
        String fileName = fileData.getFileName();
        Utils.setDownloadTime(client.getClientId(), getFileSize(fileName), fileName, downloadTime);
        Utils.printLog("File " + localPath + " download time " + downloadTime + "\n", "verbose.txt");
    }

    private int getFileSize(String fileName) {
        String[] arr = fileName.split("_");
        return Integer.parseInt(arr[1]);
    }
}
