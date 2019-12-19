CREATE TABLE `p2p_database`.`client_table` ( `clientName` VARCHAR(20) NOT NULL , `clientPassword` VARCHAR(20) NOT NULL , PRIMARY KEY (`clientName`)) ENGINE = InnoDB;

CREATE TABLE `p2p_database`.`file_table` ( `clientName` VARCHAR(20) NOT NULL , `fileName` VARCHAR(100) NOT NULL , `filePath` VARCHAR(300) NOT NULL , `fileSize` LONG NOT NULL , FOREIGN KEY (`clientName`) REFERENCES `client_table` (`clientName`) ON DELETE CASCADE) ENGINE = InnoDB;

CREATE TABLE `p2p_database`.`ip_table` ( `clientName` VARCHAR(20) NOT NULL , `clientIp` VARCHAR(20) NOT NULL , PRIMARY KEY (`clientName`), FOREIGN KEY (`clientName`) REFERENCES `client_table` (`clientName`) ON DELETE CASCADE) ENGINE = InnoDB;