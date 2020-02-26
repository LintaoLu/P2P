import Client.Client;
import Common.FileMetadata;
import java.net.UnknownHostException;
import java.util.List;

public class Test1
{
    public static void main(String[] args) throws UnknownHostException
    {
        Client client1 = new Client("192.168.1.14");
	    System.out.println(client1.getIP());
        System.out.println(client1.clientAuthorization("client1", "client1"));
        //List<FileMetadata> list = client1.getFileList("job.txt");
        //client1.getFile(list.get(0), "D:\\p2p\\job.txt");
    }
}
