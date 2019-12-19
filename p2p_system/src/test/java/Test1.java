import Client.Client;
import Common.FileMetadata;
import java.net.UnknownHostException;
import java.util.List;

public class Test1
{
    public static void main(String[] args) throws UnknownHostException
    {
        Client client1 = new Client();
        client1.registerFile("client1", "C:\\Users\\kjc60\\Desktop", "job.txt");
        List<FileMetadata> list = client1.getFileList("job.txt");
        client1.getFile(list.get(0), "D:\\p2p\\job.txt");
    }
}