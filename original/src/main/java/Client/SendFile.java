package Client;

import Common.FileMetadata;

import java.io.*;
import java.net.Socket;

public class SendFile implements Runnable
{
    private final Socket sock;

    public SendFile(Socket sock) { this.sock = sock; }

    @Override
    public void run()
    {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        ObjectInputStream ois  = null;

        try
        {
            System.out.println("Accepted connection : " + sock);
            // receive FileData
            ois = new ObjectInputStream(sock.getInputStream());
            FileMetadata fileData = (FileMetadata) ois.readObject();
            // send file
            File myFile = new File (fileData.getFilePath());
            byte [] mybytearray  = new byte [(int)myFile.length()];
            fis = new FileInputStream(myFile);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray,0,mybytearray.length);
            os = sock.getOutputStream();
            System.out.println("Sending " + fileData.getFilePath() + "(" + mybytearray.length + " bytes)");
            os.write(mybytearray,0,mybytearray.length);
            os.flush();
            System.out.println("Done.");
        }
        catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
        finally
        {
            try
            {
                if (bis != null) bis.close();
                if (os != null) os.close();
                if (sock != null) sock.close();
                if (fis != null) fis.close();
            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}