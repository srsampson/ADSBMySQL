package adsbmysql;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/*
 * A Class to store configuration parameters.
 */
public final class Config {

    public static final String STATION_ALT = "station.alt";
    public static final String STATION_METAR = "station.airportmetar";
    //
    private int socketPort;
    private int radarid;
    private int radarscan;
    private int databaseTargetTimeout;
    private int databaseMetricTimeout;
    private int homeAlt;
    private String[] metarNames;
    private String socketIP;
    private String databaseHost;
    private String databaseName;
    private String databasePort;
    private String databaseLogin;
    private String databasePassword;
    //
    private Properties Props;
    private String userDir;
    private String fileSeparator;
    private String OSConfPath;

    public Config(String val) {
        String temp;

        socketIP = "127.0.0.1";
        homeAlt = 0;
        radarscan = 3;
        socketPort = 30003;
        databaseTargetTimeout = 3;    // 3 minutes
        //
        Props = null;
        //
        userDir = System.getProperty("user.dir");
        fileSeparator = System.getProperty("file.separator");
        OSConfPath = userDir + fileSeparator + val;

        /*
         * Read the config file into the Properties class Note: The file must
         * exist, or we can not proceed.
         */
        try {
            FileInputStream in = new FileInputStream(OSConfPath);
            Props = new Properties();
            Props.load(in);
        } catch (IOException e1) {
            System.out.println("ADSBMySQL::Config Fatal: Unable to read configuration file " + OSConfPath);
            System.exit(-1);
        }

        /*
         * Now check which properties were selected, and take the defaults if
         * none given.
         */
        if (Props != null) {
            temp = Props.getProperty(STATION_ALT, "0").trim();
            Props.setProperty(STATION_ALT, temp);
            try {
                homeAlt = Integer.parseInt(temp);
            } catch (NumberFormatException e2) {
                homeAlt = 0;
            }

            temp = Props.getProperty(STATION_METAR);
            metarNames = temp.toUpperCase().split(",");

            temp = Props.getProperty("radar.id");
            if (temp == null) {
                radarid = 0;
                System.out.println("radar.id not set, set to 0");
            } else {
                try {
                    radarid = Integer.parseInt(temp.trim());
                } catch (NumberFormatException e3) {
                    radarid = 0;
                }
            }

            temp = Props.getProperty("radar.scan");
            if (temp == null) {
                radarscan = 3;
                System.out.println("radar.scan not set, set to 3 seconds");
            } else {
                try {
                    radarscan = Integer.parseInt(temp.trim());

                    if (radarscan < 1) {
                        radarscan = 1;
                    } else if (radarscan > 13) {
                        radarscan = 13;
                    }
                } catch (NumberFormatException e4) {
                    radarscan = 3;
                }
            }

            temp = Props.getProperty("socket.address");
            if (temp == null) {
                socketIP = "127.0.0.1";
                System.out.println("socket.address not set, set to 127.0.0.1");
            } else {
                socketIP = temp.trim();
            }

            temp = Props.getProperty("socket.port");
            if (temp == null) {
                socketPort = 30003;
                System.out.println("socket.port not set, set to 30003");
            } else {
                try {
                    socketPort = Integer.parseInt(temp.trim());
                } catch (NumberFormatException e5) {
                    socketPort = 30003;
                }
            }

            temp = Props.getProperty("db.targettimeout");
            if (temp == null) {
                databaseTargetTimeout = 3;
                System.out.println("db.targettimeout not set, set to 3 minutes");
            } else {
                try {
                    databaseTargetTimeout = Integer.parseInt(temp.trim());
                } catch (NumberFormatException e6) {
                    databaseTargetTimeout = 3;
                }
            }

            temp = Props.getProperty("db.metrictimeout");
            if (temp == null) {
                databaseMetricTimeout = 6;
                System.out.println("db.metrictimeout not set, set to 6 minutes");
            } else {
                try {
                    databaseMetricTimeout = Integer.parseInt(temp.trim());
                } catch (NumberFormatException e7) {
                    databaseMetricTimeout = 6;
                }
            }
            
            temp = Props.getProperty("db.host");
            if (temp == null) {
                databaseHost = "127.0.0.1";
                System.out.println("db.host not set, set to 127.0.0.1");
            } else {
                databaseHost = temp.trim();
            }

            temp = Props.getProperty("db.name");
            if (temp == null) {
                databaseName = "adsb";
                System.out.println("db.name not set, set to adsb");
            } else {
                databaseName = temp.trim();
            }

            temp = Props.getProperty("db.port");
            if (temp == null) {
                databasePort = "3306";
                System.out.println("db.port not set, set to 3306");
            } else {
                databasePort = temp.trim();
            }

            temp = Props.getProperty("db.login");
            if (temp == null) {
                databaseLogin = "adsb-rw";
                System.out.println("db.login not set, set to adsb-rw");
            } else {
                databaseLogin = temp.trim();
            }

            temp = Props.getProperty("db.password");
            if (temp == null) {
                databasePassword = "secret";
                System.out.println("db.password not set, set to secret");
            } else {
                databasePassword = temp.trim();
            }
        }
    }


    public int getHomeAlt() {
        return homeAlt;
    }

    public String[] getMetarNames() {
        return metarNames;
    }

    /**
     * Getter to provide for multiple detector data in the same database Each
     * network connect should be configured with a different radar ID
     *
     * @return a int Representing a numeric radar ID
     */
    public int getRadarID() {
        return this.radarid;
    }

    public int getRadarScanTime() {
        return this.radarscan;
    }

    /**
     * Getter to return the configuration Basestation TCP port
     *
     * @return an integer Representing the Basestation TCP port
     */
    public int getSocketPort() {
        return socketPort;
    }

    /**
     * Getter to return the Basestation IP or Hostname
     *
     * @return a string Representing the IP or Hostname of the Basestation host
     */
    public String getSocketHost() {
        return socketIP;
    }

    /**
     * Getter to return the filename path of the configuration file
     *
     * @return a string Representing the configuration file path
     */
    public String getOSConfPath() {
        return OSConfPath;
    }

    /**
     * Getter to return the database connection URL
     *
     * @return a string Representing the database URL
     */
    public String getDatabaseURL() {
        return "jdbc:mysql://" + databaseHost + ":" + databasePort + "/" + databaseName;
    }

    /**
     * Getter to return the database login name
     *
     * @return a string Representing the database login name
     */
    public String getDatabaseLogin() {
        return databaseLogin;
    }

    /**
     * Getter to return the database login password
     *
     * @return a string Representing the database login password
     */
    public String getDatabasePassword() {
        return databasePassword;
    }

    /**
     * Getter to return the target timeout value
     *
     * @return an int Representing the database timeout for archiving
     */
    public int getDatabaseTargetTimeout() {
        return databaseTargetTimeout;
    }

    /**
     * Getter to return the metric timeout value
     *
     * @return an int Representing the database timeout for reset metrics
     */
    public int getDatabaseMetricTimeout() {
        return databaseMetricTimeout;
    }
}
