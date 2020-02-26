import client.Client;
import common.FileData;
import common.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import server.Server;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.Assert.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Test {

    private static Client[] clients;
    private static Server server;
    private static int clientNum = 5, testNum= 10;
    private static List<List<String>> localFiles;
    private static int testCounter = 0;

    @BeforeClass
    public static void setup() throws IOException {
        server = new Server();
        server.startServer();
        Utils.generateTextFiles(clientNum);
        clients = new Client[clientNum];
        localFiles = new ArrayList<>(clientNum);
        for (int i = 0; i < clientNum; i++) {
            clients[i] = new Client(i+1);
            clients[i].startClientServer();
            localFiles.add(clients[i].getLocalFiles());
        }
        Utils.deleteFile("verbose.txt");
        Utils.deleteFile("output.txt");
    }

    /**
     * Register all files on server, assert correctness.
     * */
    @org.junit.Test
    @Order(1)
    public void testRegisterFile() {
        ConcurrentHashMap<String, Map<Integer, FileData>> map = server.getGlobalFiles();
        for(int i = 0; i < clients.length; i++) {
            for(String path : localFiles.get(i)) {
                FileData fileData = clients[i].generateFileData(path);
                clients[i].registerFile(fileData);
                String fileName = fileData.getFileName();
                int clientId = fileData.getClientId();
                try {
                    assertTrue(map.containsKey(fileName) &&
                            map.get(fileName).get(clientId).equals(fileData));
                    System.out.println("register " + fileName + " - passed");
                } catch(AssertionError e) {
                    System.out.println("register " + fileName + " - failed");
                    throw e;
                }
            }
        }
        testCounter++;
    }

    /**
     * Remove files, test correctness.
     * */
    @org.junit.Test
    @Order(2)
    public void testRemoveFile() {
        ConcurrentHashMap<String, Map<Integer, FileData>> map = server.getGlobalFiles();
        for (int i = 0; i < clientNum; i++) {
            Client client = clients[i];
            for (int j = 1; j <= Utils.fileNum; j++) {
                String fileName = "client" + (i+1) + "_" + j;
                client.deleteFile(fileName);
                try {
                    assertTrue(map.get(fileName) == null ||
                            map.get(fileName).get(i+1) == null);
                    System.out.println("remove " + fileName + " - passed");
                } catch(AssertionError e) {
                    System.out.println("register " + fileName + " - failed");
                    throw e;
                }
            }

            for (String path : localFiles.get(i)) {
                // recover files
                FileData fileData = client.generateFileData(path);
                client.registerFile(fileData);
            }
        }
        testCounter++;
    }

    /**
     * Test file transmission.
     * */
    @org.junit.Test
    @Order(3)
    public void testTransmitAllFile() {
        for (int time = 0; time < testNum; time++) {
            for (int i = 0; i < clientNum; i++) {
                Client client = clients[i];
                for (int j = 1; j <= clientNum; j++) {
                    if (j == i+1) continue;
                    for (int k = 1; k <= Utils.fileNum; k++) {
                        String fileName = "client" + j + "_" + k;
                        List<FileData> list = client.findFile(fileName);
                        while (client.hasTask()) {}
                        if (list.size() > 0) client.getFile(list.get(0));
                    }
                }
            }
        }
        testCounter++;
    }

    @AfterClass
    public static void Teardown() throws IOException {
        for(Client client : clients) client.stopClientServer();
        server.stopServer();

        if (testCounter < 3) return;

        Utils.printLog("=========================================================", "output.txt");
        Utils.printLog("Test Result", "output.txt");
        Utils.printLog("=========================================================\n", "output.txt");

        for (int i = 0; i < clientNum; i++) {
            Utils.printLog("client " + (i+1) + " avg register time  is: " + clients[i].getRegisterFileResponseTime()
                            + " milliseconds\n", "output.txt");
        }
        Utils.printLog("---------------------------------------------------", "output.txt");
        for (int i = 0; i < clientNum; i++) {
            Utils.printLog("client " + (i+1) + " avg search time  is: " + clients[i].getSearchFileResponseTime()
                    + " milliseconds\n", "output.txt");
        }
        Utils.printLog("---------------------------------------------------", "output.txt");
        for (int i = 0; i < clientNum; i++) {
            Utils.printLog("client " + (i+1) + " avg delete time  is: " + clients[i].getDeleteFileResponseTime()
                    + " milliseconds\n", "output.txt");
        }
        Utils.printLog("---------------------------------------------------", "output.txt");
        for (int i = 1; i <= Utils.fileNum; i++) {
            Utils.printLog("avg time to download " + i*Utils.fileSize + " bytes files is: "
                    + Utils.getAvgDownloadTimeBySize(i) + " milliseconds\n", "output.txt");
        }
        Utils.printLog("---------------------------------------------------", "output.txt");
        for (int i = 1; i <= clientNum; i++) {
            Utils.printLog("client " + i + " avg download time is "
                    + Utils.getAvgDownloadTimeByClient(i) + " milliseconds\n", "output.txt");
        }
        Utils.printLog("---------------------------------------------------", "output.txt");
        for (int i = 1; i <= clientNum; i++) {
            for (int j = 1; j <= Utils.fileNum; j++) {
                Utils.printLog("client " + i + " avg time to download " + j*Utils.fileSize+ " bytes files is "
                        + Utils.getAvgDownloadTimeByClientAndSize(i, j) + " milliseconds\n", "output.txt");
            }
        }

        Utils.printLog("=========================================================", "output.txt");
        Utils.printLog("Test End", "output.txt");
        Utils.printLog("=========================================================", "output.txt");

        Utils.printMap();
    }
}
