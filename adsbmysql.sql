CREATE DATABASE  IF NOT EXISTS `adsb` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `adsb`;
-- MySQL dump 10.13  Distrib 8.0.16, for Linux (x86_64)
--
-- Host: nas    Database: adsb
-- ------------------------------------------------------
-- Server version	5.7.26-0ubuntu0.18.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `callsign`
--

DROP TABLE IF EXISTS `callsign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `callsign` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `acid` char(6) NOT NULL,
  `utcdetect` varchar(45) NOT NULL,
  `utcupdate` varchar(45) NOT NULL,
  `callsign` char(8) NOT NULL COMMENT 'Transmitted Callsign',
  `flight_id` bigint(20) unsigned NOT NULL,
  `radar_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_Callsign` (`acid`,`callsign`,`flight_id`,`radar_id`),
  KEY `FK_callsign_acid` (`acid`),
  CONSTRAINT `FK_callsign_acid` FOREIGN KEY (`acid`) REFERENCES `modestable` (`acid`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1 COMMENT='Callsigns Associated with Targets';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `callsign`
--

LOCK TABLES `callsign` WRITE;
/*!40000 ALTER TABLE `callsign` DISABLE KEYS */;
/*!40000 ALTER TABLE `callsign` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metrics`
--

DROP TABLE IF EXISTS `metrics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `metrics` (
  `seq_num` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `utcupdate` varchar(45) NOT NULL,
  `callsignCount` bigint(20) unsigned NOT NULL,
  `surfaceCount` bigint(20) unsigned NOT NULL,
  `airborneCount` bigint(20) unsigned NOT NULL,
  `velocityCount` bigint(20) unsigned NOT NULL,
  `altitudeCount` bigint(20) unsigned NOT NULL,
  `squawkCount` bigint(20) unsigned NOT NULL,
  `trackCount` bigint(20) unsigned NOT NULL,
  `radar_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`seq_num`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1 COMMENT='Socket Metrics';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metrics`
--

LOCK TABLES `metrics` WRITE;
/*!40000 ALTER TABLE `metrics` DISABLE KEYS */;
/*!40000 ALTER TABLE `metrics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modestable`
--

DROP TABLE IF EXISTS `modestable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `modestable` (
  `acid` char(6) NOT NULL,
  `utcdetect` varchar(45) NOT NULL,
  `utcupdate` varchar(45) NOT NULL,
  `acft_reg` varchar(45) DEFAULT NULL,
  `acft_model` varchar(45) DEFAULT NULL,
  `acft_operator` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`acid`) USING BTREE,
  KEY `Index_reg` (`acft_reg`),
  KEY `Index_model` (`acft_model`),
  KEY `Index_operator` (`acft_operator`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Mode-S ICAO Received';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modestable`
--

LOCK TABLES `modestable` WRITE;
/*!40000 ALTER TABLE `modestable` DISABLE KEYS */;
/*!40000 ALTER TABLE `modestable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `target`
--

DROP TABLE IF EXISTS `target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `target` (
  `flight_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Flight ID',
  `radar_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT 'Radar ID that generated this target report',
  `acid` char(6) NOT NULL COMMENT 'Aircraft ID',
  `utcdetect` bigint(20) NOT NULL COMMENT 'UTC Time track detected',
  `utcupdate` bigint(20) NOT NULL COMMENT 'UTC Time track updated',
  `altitude` int(10) DEFAULT NULL COMMENT 'Altitude in feet',
  `groundSpeed` double DEFAULT NULL COMMENT 'Speed over the ground',
  `groundTrack` double DEFAULT NULL COMMENT 'Heading in relation to True North',
  `gsComputed` double DEFAULT NULL COMMENT 'Computed Speed over the ground',
  `gtComputed` double DEFAULT NULL COMMENT 'Computed Heading in relation to True North',
  `callsign` char(8) DEFAULT NULL COMMENT 'Transmitted Callsign',
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `verticalRate` int(10) DEFAULT NULL,
  `quality` int(10) DEFAULT NULL,
  `squawk` int(10) unsigned DEFAULT NULL,
  `alert` tinyint(1) NOT NULL DEFAULT '0',
  `emergency` tinyint(1) NOT NULL DEFAULT '0',
  `spi` tinyint(1) NOT NULL DEFAULT '0',
  `onground` tinyint(1) NOT NULL DEFAULT '0',
  `hijack` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Squawk Code 7500 detected',
  `comm_out` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Squawk Code 7600 detected',
  `hadAlert` tinyint(1) NOT NULL DEFAULT '0',
  `hadEmergency` tinyint(1) NOT NULL DEFAULT '0',
  `hadSPI` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`flight_id`) USING BTREE,
  UNIQUE KEY `FltIDIndex` (`flight_id`,`acid`,`radar_id`) USING BTREE,
  KEY `FK_acid` (`acid`),
  CONSTRAINT `FK_acid` FOREIGN KEY (`acid`) REFERENCES `modestable` (`acid`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=latin1 COMMENT='Active Target Tracks';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `target`
--

LOCK TABLES `target` WRITE;
/*!40000 ALTER TABLE `target` DISABLE KEYS */;
/*!40000 ALTER TABLE `target` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `insertmodes` BEFORE INSERT ON `target` FOR EACH ROW BEGIN
  SET @triggertime = CONCAT(FROM_UNIXTIME(NEW.utcdetect/1000),".",CAST(MOD(NEW.utcdetect,1000) AS CHAR));
  SET @tcount = (SELECT count(1) FROM modestable WHERE acid=NEW.acid);
  IF @tcount > 0 THEN
    UPDATE modestable SET utcupdate=@triggertime WHERE acid=NEW.acid;
  ELSE
    INSERT INTO modestable (acid,utcdetect,utcupdate) VALUES (NEW.acid, @triggertime, @triggertime);
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `updatemodes` BEFORE UPDATE ON `target` FOR EACH ROW BEGIN
  SET @triggertime = CONCAT(FROM_UNIXTIME(NEW.utcupdate/1000),".",CAST(MOD(NEW.utcupdate,1000) AS CHAR));
  UPDATE modestable SET utcupdate=@triggertime WHERE acid=NEW.acid;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `targetecho`
--

DROP TABLE IF EXISTS `targetecho`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `targetecho` (
  `record_num` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Flight ID',
  `flight_id` bigint(20) unsigned NOT NULL,
  `radar_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT 'Radar ID that generated this target report',
  `acid` char(6) NOT NULL COMMENT 'Aircraft ID',
  `utcdetect` varchar(45) NOT NULL COMMENT 'UTC Time detected',
  `altitude` int(10) DEFAULT NULL COMMENT 'Reported Altitude in Feet',
  `latitude` double NOT NULL COMMENT 'latitude in degrees',
  `longitude` double NOT NULL COMMENT 'longitude in degrees',
  `onground` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`record_num`),
  KEY `FK_targetecho_acid` (`acid`),
  CONSTRAINT `FK_targetecho_acid` FOREIGN KEY (`acid`) REFERENCES `modestable` (`acid`)
) ENGINE=InnoDB AUTO_INCREMENT=598 DEFAULT CHARSET=latin1 COMMENT='History of Target Positions';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `targetecho`
--

LOCK TABLES `targetecho` WRITE;
/*!40000 ALTER TABLE `targetecho` DISABLE KEYS */;
/*!40000 ALTER TABLE `targetecho` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `targethistory`
--

DROP TABLE IF EXISTS `targethistory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `targethistory` (
  `record_num` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `flight_id` bigint(20) unsigned NOT NULL DEFAULT '0',
  `radar_id` int(10) unsigned NOT NULL DEFAULT '0',
  `acid` char(6) NOT NULL,
  `utcdetect` varchar(45) NOT NULL,
  `utcfadeout` varchar(45) NOT NULL,
  `altitude` int(10) DEFAULT NULL,
  `groundSpeed` double DEFAULT NULL,
  `groundTrack` double DEFAULT NULL,
  `gsComputed` double DEFAULT NULL,
  `gtComputed` double DEFAULT NULL,
  `callsign` char(8) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `verticalRate` int(10) DEFAULT NULL,
  `squawk` int(10) unsigned DEFAULT NULL,
  `alert` tinyint(1) NOT NULL DEFAULT '0',
  `emergency` tinyint(1) NOT NULL DEFAULT '0',
  `spi` tinyint(1) NOT NULL DEFAULT '0',
  `onground` tinyint(1) NOT NULL DEFAULT '0',
  `hijack` tinyint(1) NOT NULL DEFAULT '0',
  `comm_out` tinyint(1) NOT NULL DEFAULT '0',
  `hadAlert` tinyint(1) NOT NULL DEFAULT '0',
  `hadEmergency` tinyint(1) NOT NULL DEFAULT '0',
  `hadSPI` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`record_num`) USING BTREE,
  UNIQUE KEY `FltIDIndex` (`flight_id`,`acid`,`radar_id`) USING BTREE,
  KEY `FK_targethistory_acid` (`acid`),
  CONSTRAINT `FK_targethistory_acid` FOREIGN KEY (`acid`) REFERENCES `modestable` (`acid`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1 COMMENT='Targets No Longer Active';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `targethistory`
--

LOCK TABLES `targethistory` WRITE;
/*!40000 ALTER TABLE `targethistory` DISABLE KEYS */;
/*!40000 ALTER TABLE `targethistory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'adsb'
--

--
-- Dumping routines for database 'adsb'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-05-10 20:26:56
