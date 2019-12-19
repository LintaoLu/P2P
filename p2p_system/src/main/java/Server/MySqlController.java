package Server;

import Common.FileMetadata;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySql version: 8.0.18
 * database name: p2p_database
 * username: root
 * password: root
 *
 * create database p2p_database;
 * use p2p_database;
 *
 *
 * CREATE TABLE `p2p_database`.`client_table`
 *      ( `clientName` VARCHAR(20) NOT NULL ,
 *        `clientPassword` VARCHAR(20) NOT NULL ,
 *        PRIMARY KEY (`clientName`))
 *        ENGINE = InnoDB;
 *
 * CREATE TABLE `p2p_database`.`file_table`
 *      ( `clientName` VARCHAR(20) NOT NULL ,
 *        `fileName` VARCHAR(100) NOT NULL ,
 *        `filePath` VARCHAR(300) NOT NULL ,
 *        `fileSize` LONG NOT NULL ,
 *        FOREIGN KEY (`clientName`) REFERENCES `client_table` (`clientName`) ON DELETE CASCADE)
 *        ENGINE = InnoDB;
 *
 * CREATE TABLE `p2p_database`.`ip_table`
 *      ( `clientName` VARCHAR(20) NOT NULL ,
 *        `clientIp` VARCHAR(20) NOT NULL ,
 *        PRIMARY KEY (`clientName`),
 *        FOREIGN KEY (`clientName`) REFERENCES `client_table` (`clientName`) ON DELETE CASCADE)
 *        ENGINE = InnoDB;
 *
 * */

class MySqlController
{
    private static final String connection_first_part = "jdbc:mysql://localhost:3306/";
    private static final String databaseName = "p2p_database";
    private static final String connection_second_part = "?useUnicode=true&useJDBCCompliantTimezoneShift=" +
            "true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String dbusername = "root", dbpassword = "root";

    private static Connection con = null;

    // connect to mySql database
    public static void connect()
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(connection_first_part + databaseName +
                    connection_second_part, dbusername, dbpassword);
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public static boolean registerClient(String username, String password, String ip)
    {
        boolean isSuccess = false;
        try
        {
            String query = "insert into client_table (clientName, clientPassword) values (?, ?);";
            MySqlController.connect();
            PreparedStatement preparedStmt = MySqlController.con.prepareStatement(query);
            preparedStmt.setString(1, username);
            preparedStmt.setString(2, password);
            preparedStmt.execute();
            System.out.println("Client added");

            query = "insert into ip_table (clientName, clientIp) values (?, ?);";
            preparedStmt = MySqlController.con.prepareStatement(query);
            preparedStmt.setString(1, username);
            preparedStmt.setString(2, ip);
            preparedStmt.execute();
            System.out.println("Client IP added");

            isSuccess = true;
            MySqlController.con.close();
        }
        catch (SQLException e)
        {
            System.out.println("An error has occurred on client registration");
            e.printStackTrace();
        }

        return isSuccess;
    }

    /**
     * @input: client name and password.
     * @output: if client exists return a positive integer, else return -1.
     *
     * update client ip at the same time.
     * */

    public static boolean clientAuthorization(String username, String password, String ip)
    {
        boolean clientExist = false;
        try
        {
            String query = "select clientName from client_table where clientName = ? and clientPassword = ? ;";
            MySqlController.connect();
            PreparedStatement preparedStmt = MySqlController.con.prepareStatement(query);
            preparedStmt.setString(1, username);
            preparedStmt.setString(2, password);
            ResultSet resultSet = preparedStmt.executeQuery();
            if (resultSet.next()) clientExist = true;
            System.out.println("Client exists");

            if (clientExist)
            {
                query = "update ip_table set clientIp = ? where clientName = ?;";
                preparedStmt = MySqlController.con.prepareStatement(query);
                preparedStmt.setString(1, ip);
                preparedStmt.setString(2, username);
                preparedStmt.execute();
                System.out.println("ip table is updated");
            }

            MySqlController.con.close();
        }
        catch (SQLException e)
        {
            System.out.println("An sql error has occurred on client authorization or ip update");
            e.printStackTrace();
        }
        return clientExist;
    }

    public static boolean registerFile(FileMetadata fileData)
    {
        if (fileData == null) return false;
        try
        {
            String query = "insert into file_table (clientName, fileName, filePath, fileSize) values (?, ?, ?, ?);";
            MySqlController.connect();
            PreparedStatement preparedStmt = MySqlController.con.prepareStatement(query);
            preparedStmt.setString(1, fileData.getClientName());
            preparedStmt.setString(2, fileData.getFileName());
            preparedStmt.setString(3, fileData.getFilePath());
            preparedStmt.setLong(4, fileData.getFileSize());
            preparedStmt.execute();
            System.out.println("file added");
            MySqlController.con.close();
        }
        catch (SQLException e)
        {
            System.out.println("An error has occurred on file registration");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static List<FileMetadata> getFileList(String fileName)
    {
        List<FileMetadata> files = new ArrayList<>();

        try
        {
            MySqlController.connect();
            String query = "select * from file_table natural join ip_table where fileName = ? ;";
            PreparedStatement preparedStmt = MySqlController.con.prepareStatement(query);
            preparedStmt.setString(1, fileName);
            ResultSet rs = preparedStmt.executeQuery();
            while (rs.next())
            {
                String clientName = rs.getString("clientName");
                String clientIp = rs.getString("clientIp");
                String filePath = rs.getString("filePath");
                long fileSize = rs.getLong("fileSize");

                FileMetadata fileData = new FileMetadata(clientName,
                        clientIp, fileName, filePath, fileSize);
                files.add(fileData);
            }
            System.out.println("getTargetFiles success!");
            MySqlController.con.close();
        }
        catch (SQLException e)
        {
            System.out.println("An error has occurred on getTargetFiles!");
            e.printStackTrace();
        }

        return files;
    }

    public static void main(String[] args)
    {
        //MySqlController.registerClient("client1", "client1", "localhost");
        MySqlController.clientAuthorization("client1", "client1", "127.0.0.51");
    }
}