package Client;

import Common.FileMetadata;
import Common.Request;
import java.io.*;
import java.net.Socket;

public class GetFile implements Runnable
{
    private Request request;
    private String location;

    public GetFile(Request request, String location)
    {
        this.request = request;
        File dir = new File("D:\\p2p");
        if (!dir.exists()) dir.mkdirs();
        if(location == null || location.length() == 0)
            this.location = "D:\\p2p\\" + request.getFileData().getFileName();
        else this.location = location;
    }

    @Override
    public void run()
    {
        FileMetadata fileData = request.getFileData();
        if(fileData == null) return;

        ObjectOutputStream oos = null;
        InputStream is = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        Socket sock = null;

        try
        {
            sock = new Socket(fileData.getClientIP(), 9999);
            System.out.println("Connecting...");
            // send FileData to server
            oos = new ObjectOutputStream(sock.getOutputStream());
            oos.writeObject(fileData);
            oos.flush();
            // receive file
            byte [] mybytearray  = new byte [(int)fileData.getFileSize()];
            is = sock.getInputStream();
            fos = new FileOutputStream(location);
            bos = new BufferedOutputStream(fos);
            int bytesRead = is.read(mybytearray,0,mybytearray.length);
            int current = bytesRead;

            do {
                bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead > -1);

            bos.write(mybytearray, 0 , current);
            bos.flush();
            System.out.println("File " + location + " downloaded (" + current + " bytes read)");
        }
        catch (IOException e) { e.printStackTrace(); }
        finally
        {
            try
            {
                // free memory
                if(fos != null) fos.close();
                if(bos != null) bos.close();
                if(sock != null) sock.close();
                if(oos != null) oos.close();
                if(is != null) is.close();
            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}