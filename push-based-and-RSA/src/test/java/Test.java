import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import peer.Peer;
import peer.SuperPeer;
import utils.FileData;
import utils.Utils;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Test {

    private static Peer[] peers;
    private static SuperPeer[] superPeers;
    private static Peer[] allPeers;
    private static List<String>[] localFiles;

    @BeforeClass
    public static void setup() throws InterruptedException {
        peers = Utils.normalPeers;
        superPeers = Utils.superPeers;
        allPeers = new Peer[peers.length + superPeers.length];
        localFiles = new ArrayList[peers.length + superPeers.length];
        int index = 0;
        for (Peer peer : peers) {
            allPeers[index++] = peer;
            localFiles[peer.getMyID()-1] = peer.getLocalFiles();
        }
        for (SuperPeer peer : superPeers) {
            allPeers[index++] = peer;
            localFiles[peer.getMyID()-1] = peer.getLocalFiles();
        }
        Arrays.sort(allPeers, Comparator.comparingInt(Peer::getMyID));
        startAll();
    }

    @org.junit.Test
    public void ATestRegisterFiles() throws InterruptedException, IOException {
        for (Peer peer : allPeers) {
            int id = peer.getMyID();
            for (int i = 1; i <= Utils.fileNum; i++) {
                FileData fileData = peer.getFileData("peer" + id + "_" + i + ".txt", id);
                peer.registerFile(fileData);
            }
        }
        TimeUnit.SECONDS.sleep(2);
    }

    @org.junit.Test
    public void BTestDownloadFiles() throws InterruptedException, IOException {
        Peer peer1 = allPeers[0];
        Peer peer2 = allPeers[1];
        SuperPeer peer10 = superPeers[1];
        System.out.println(peer2.getOriginalFile());
        peer2.modifyFile("peer2_2");
        peer2.push("peer2_2");
        System.out.println(peer10.getDownloadFile().get("peer2_2").isValid() + "========================");
        TimeUnit.SECONDS.sleep(2);
        peer10.refresh("peer2_2");
        TimeUnit.SECONDS.sleep(2);
        System.out.println(peer10.getDownloadFile().get("peer2_2").isValid() + "========================");
    }

    @AfterClass
    public static void Teardown() throws IOException, InterruptedException {
        stopAll();
    }

    private static void startAll() throws InterruptedException {
        for (Peer peer : allPeers) peer.start();
        TimeUnit.SECONDS.sleep(1);
    }

    private static void stopAll() throws IOException, InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        for (Peer peer : allPeers) peer.stop();
    }
}