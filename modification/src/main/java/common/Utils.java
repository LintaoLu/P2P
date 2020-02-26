package common;

import org.apache.commons.lang3.SystemUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class contains static variables.
 * All parameters can be changed, I didn't hard code them.
 * */
public class Utils {

    public static final int FIND_FILE = 1, GET_FILE = 2, POST_FILE = 3, UPDATE_IP = 4,
            DELETE_FILE = 5, CHECK_FILE = 6, REMOVE_IP = 7;
    public static final int RETURN_FILE_LIST = 10, RETURN_ERROR = 11, RETURN_OK = 12;
    public static final int SERVER_MAX_THREADS = 20000;
    public static final int CLIENT_MAX_THREADS = 10;
    public static final int SERVER_PORT = 10000;
    public static final int[] CLIENT_PORT = {9990, 9991, 9992, 9993, 9994, 9995, 9996, 9997, 9998, 9999};
    public static final String SERVER_IP = "127.0.0.1";
    public static final int fileSize = 5000, fileNum = 10;
    // clientId -> (size -> download details)
    public static ConcurrentHashMap<Integer, Map<Integer, Pair>> dataAnalysis;

    static {
        dataAnalysis = new ConcurrentHashMap<>();
    }

    public static void printMap() {
        for (Map.Entry<Integer, Map<Integer, Pair>> entry : dataAnalysis.entrySet()) {
            System.out.println(entry);
        }
    }

    public static void setDownloadTime(int clientId, int size, String fileName, long downloadTime) {
        if (!dataAnalysis.containsKey(clientId)) dataAnalysis.put(clientId, new HashMap<>());
        Map<Integer, Pair> map = dataAnalysis.get(clientId);
        if (!map.containsKey(size)) map.put(size, new Pair(1, downloadTime));
        else {
            Pair pair = map.get(size);
            pair.counter++;
            pair.totalTime += downloadTime;
        }
    }

    public static long getAvgDownloadTimeBySize(int size) {
        long totalTime = 0;
        int counter = 0;
        for (Map<Integer, Pair> map : dataAnalysis.values()) {
            if (!map.containsKey(size)) continue;
            Pair pair = map.get(size);
            totalTime += pair.totalTime;
            counter += pair.counter;
        }
        return totalTime/counter;
    }

    public static long getAvgDownloadTimeByClient(int clientId) {
        if (!dataAnalysis.containsKey(clientId)) return 0;
        Map<Integer, Pair> map = dataAnalysis.get(clientId);
        long totalTime = 0;
        int counter = 0;
        for (Pair pair : map.values()) {
            totalTime += pair.totalTime;
            counter += pair.counter;
        }
        return totalTime/counter;
    }

    public static long getAvgDownloadTimeByClientAndSize(int clientId, int size) {
        if (!dataAnalysis.containsKey(clientId)) return 0;
        Map<Integer, Pair> map = dataAnalysis.get(clientId);
        if (!map.containsKey(size)) return 0;
        Pair pair = map.get(size);
        return pair.getAvgTime();
    }

    public static void generateTextFiles(int clientNum) throws IOException {
        Random random = new Random();
        for(int i = 1; i <= clientNum; i++) {
            String directoryPath = "client" + i + "'s_files";
            File dir = new File(directoryPath);
            if (!dir.exists()) dir.mkdirs();
            for(int j = 1; j <= fileNum; j++) {
                String filePath = "";
                if(SystemUtils.IS_OS_WINDOWS)
                    filePath = directoryPath + "\\client" + i + "_" + j + ".txt";
                if(SystemUtils.IS_OS_LINUX)
                    filePath = directoryPath + "/client" + i + "_" + j + ".txt";
                File file = new File(filePath);
                file.createNewFile();
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.setLength(j*fileSize);
                byte[] temp = new byte[j*fileSize];
                random.nextBytes(temp);
                Utils.writeFile(file, temp);
                raf.close();
            }
        }
    }

    private static void writeFile(File file, byte[] temp) {
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

    public static void printLog(String log, String path) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(path, true)  //Set true for append mode
            );
            writer.newLine();   //Add new line
            writer.write(log);
            writer.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            System.out.println("file doesn't exists, or it is not a file");
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch(Exception e) { e.printStackTrace();}
        System.out.println("Deletion successful.");
    }

    private static class Pair{
        int counter;
        long totalTime;

        Pair(int counter, long totalTime){
            this.counter = counter;
            this.totalTime = totalTime;
        }

        long getAvgTime() {
            return totalTime / counter;
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "counter=" + counter +
                    ", totalTime=" + totalTime +
                    '}';
        }
    }
}