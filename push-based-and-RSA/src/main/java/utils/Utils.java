package utils;

import org.apache.commons.lang3.SystemUtils;
import peer.Peer;
import peer.SuperPeer;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    public static int MAX_THREADS = 10000;
    public static int TTL = 10;
    public static int MESSAGE_QUEUE_SIZE = 1000;
    public static String IP = "127.0.0.1";
    private static AtomicInteger currentMessageID = new AtomicInteger(1);
    public static int[] availablePorts;
    public static Peer[] normalPeers;
    public static SuperPeer[] superPeers;
    public static int fileNum = 2, fileSize = 1000;

    static {
        try {
            //deleteDirectory(new File("GnutellaP2P"));
            File file = new File("output.log");
            Files.deleteIfExists(file.toPath()); //surround it in try catch block
            availablePorts = new int[30];
            int index = 0;
            for (int i = 40066; i < 40096; i++) availablePorts[index++] = i;
            List<int[]> list = parseConfigFile1();
            Map<Integer, Set<Integer>> map = parseConfigFile2();
            generateTextFiles(list.size() + map.size());
            loadPeers(list, map);
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    public static int generateMessageID() {
        return currentMessageID.getAndIncrement();
    }

    private static List<int[]> parseConfigFile1() {
        List<int[]> list = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("config_peer.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                String[] arr = line.split(" ");
                if (arr.length != 2) continue;
                list.add(new int[] { Integer.parseInt(arr[0]), Integer.parseInt(arr[1]) });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private static Map<Integer, Set<Integer>> parseConfigFile2() {
        Map<Integer, Set<Integer>> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("config_superpeer.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                String[] arr = line.split(" ");
                if (arr.length != 2) continue;
                int superPeer = Integer.parseInt(arr[0]), neighbour = Integer.parseInt(arr[1]);
                if (!map.containsKey(superPeer)) {
                    map.put(superPeer, new HashSet<>());
                }
                if (neighbour > 0) map.get(superPeer).add(neighbour);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    private static void loadPeers(List<int[]> list, Map<Integer, Set<Integer>> map) throws IOException {

        Map<Integer, SuperPeer> superPeerMap = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : map.entrySet()) {
            // public SuperPeer(int ID, int port, String IP, int superPeerID, String superPeerIP,
            //                     int superPeerPort) throws IOException
            Set<Integer> neighboursID = entry.getValue();
            int superPeerID = entry.getKey();
            SuperPeer superPeer = new SuperPeer(superPeerID, availablePorts[superPeerID-1], IP,
                    superPeerID, IP, availablePorts[superPeerID-1]);
            Map<Integer, Pair<String, Integer>> neighbours = superPeer.getNeighbours();
            for (int ID : neighboursID) {
                if (!neighbours.containsKey(ID)) {
                    neighbours.put(ID, new Pair<>(IP, availablePorts[ID-1]));
                }
            }
            superPeerMap.put(superPeerID, superPeer);
        }

        normalPeers = new Peer[list.size()];
        for (int i = 0; i < list.size(); i++) {
            int peerID = list.get(i)[0], superPeerID = list.get(i)[1];
            // public Peer(int ID, int port, String IP, int superPeerID, String superPeerIP, int superPeerPort)
            normalPeers[i] = new Peer(peerID, availablePorts[peerID-1], IP, superPeerID, IP, availablePorts[superPeerID-1]);
            superPeerMap.get(superPeerID).addClient(peerID, IP, availablePorts[peerID-1]);
        }

        superPeers = new SuperPeer[superPeerMap.size()];
        int index = 0;
        for (SuperPeer superPeer : superPeerMap.values()) superPeers[index++] = superPeer;

        Arrays.sort(normalPeers, Comparator.comparingInt(Peer::getMyID));
        Arrays.sort(superPeers, Comparator.comparingInt(SuperPeer::getMyID));
    }

    public static void generateTextFiles(int peerNum) throws IOException {
        Random random = new Random();
        File dir = new File("GnutellaP2P");
        if (!dir.exists()) dir.mkdirs();
        for(int i = 1; i <= peerNum; i++) {
            String directoryPath = "GnutellaP2P\\peer_" + i;
            if(SystemUtils.IS_OS_LINUX) directoryPath = "GnutellaP2P/peer_" + i;
            dir = new File(directoryPath);
            if (dir.exists()) continue;
            dir.mkdirs();
            for(int j = 1; j <= fileNum; j++) {
                String filePath = "";
                if(SystemUtils.IS_OS_WINDOWS)
                    filePath = directoryPath + "\\peer" + i + "_" + j + ".txt";
                if(SystemUtils.IS_OS_LINUX)
                    filePath = directoryPath + "/peer" + i + "_" + j + ".txt";
                File file = new File(filePath);
                file.createNewFile();
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.setLength(j*fileSize);
                byte[] temp = new byte[j*fileSize];
                random.nextBytes(temp);
                Utils.writeFileToDisk(file, temp);
                raf.close();
            }
        }
    }

    private static void writeFileToDisk(File file, byte[] temp) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Writes bytes from the specified byte array to this file output stream
            fos.write(temp);
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while writing file " + ioe);
        } finally {
            // close the streams using close method
            try { if (fos != null) { fos.close(); } }
            catch (IOException ioe) { System.out.println("Error while closing stream: " + ioe); }
        }
    }

    public void printLog(String log, String path) throws IOException {
        File logFile = new File(path);
        logFile.createNewFile(); // if file already exists will do nothing
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(path, true)  //Set true for append mode
            );
            writer.newLine();   //Add new line
            writer.write(log);
            writer.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            assert children != null;
            for (File child : children) {
                boolean success = deleteDirectory(child);
                if (!success) return false;
            }
        }
        System.out.println("removing file or directory : " + dir.getName());
        return dir.delete();
    }
}
