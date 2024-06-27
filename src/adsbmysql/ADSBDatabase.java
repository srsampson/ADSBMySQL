package adsbmysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public final class ADSBDatabase extends Thread {

    private static final long RATE = 30000L;        // 30 Seconds in milliseconds
    //
    private SocketParse con;
    //
    private Connection db1;
    private Connection db2;
    private Statement query;
    private Statement queryt;
    //
    private Thread database;
    private static boolean EOF;
    //
    private Config config;
    private String acid;
    private int radarid;
    private long metricTimeout;
    private int targetTimeout;
    private long radarscan;
    //
    private Timer targetTimer;
    private Timer metricTimer;
    //
    private TimerTask targetTimeoutTask;
    private TimerTask metricTimeoutTask;

    public ADSBDatabase(Config cf, SocketParse k) {
        this.con = k;
        this.config = cf;
        this.radarid = cf.getRadarID();
        this.radarscan = (long) cf.getRadarScanTime() * 1000L;
        this.acid = "";
        EOF = false;

        targetTimeout = config.getDatabaseTargetTimeout();
        targetTimeoutTask = new TargetTimeoutThread(targetTimeout);
        
        targetTimer = new Timer();
        targetTimer.scheduleAtFixedRate(targetTimeoutTask, 0L, RATE); // Update targets every 30 seconds

        metricTimeout = config.getDatabaseMetricTimeout() * 60L * 1000L; // minutes to milliseconds
        metricTimeoutTask = new MetricTimeoutThread();

        metricTimer = new Timer();
        metricTimer.scheduleAtFixedRate(metricTimeoutTask, 0L, metricTimeout); // Update metrics every 6 minutes
        
        database = new Thread(this);
        database.setName("ADSBDatabase");
        database.setPriority(Thread.NORM_PRIORITY);

        String connectionURL = config.getDatabaseURL();
    
        Properties properties = new Properties();
        properties.setProperty("user", config.getDatabaseLogin());
        properties.setProperty("password", config.getDatabasePassword());
        properties.setProperty("useSSL", "false");
        properties.setProperty("serverTimezone", "UTC");

        /*
         * You need the ODBC MySQL driver library in the same directory you have
         * the executable JAR file of this program.
         */
        try {
            db1 = DriverManager.getConnection(connectionURL, properties);
        } catch (SQLException  e) {
            System.err.println("ADSBDatabase Fatal: Unable to open database 1 " + connectionURL + " " + e.getMessage());
            System.exit(-1);
        }

        try {
            db2 = DriverManager.getConnection(connectionURL, properties);
        } catch (SQLException e3) {
            System.err.println("ADSBDatabase Fatal: Unable to open database 2 " + connectionURL + " " + e3.getMessage());
            System.exit(-1);
        }
    }
    
    public void startup() {
        database.start();

    }

    public void close() {
        EOF = true;

        try {
            con.close();
            db1.close();
            db2.close();
        } catch (SQLException e) {
            System.out.println("ADSBDatabase::close Closing Bug " + e.toString());
            System.exit(-1);
        }
    }

    public Connection getDBConnection() {
        return db2;
    }
    
    @Override
    public void run() {
        List<Track> table;
        ResultSet rs = null;
        String queryString = "";
        int ground, exists;
        long time;

        try {
            while (EOF == false) {

                table = con.getTrackUpdatedTable();

                if (!table.isEmpty()) {
                    for (Track trk : table) {
                        time = trk.getUpdateTime();
                        trk.setUpdated(false);

                        acid = trk.getAircraftID();

                        /*
                         * See if this ACID exists yet in the target table, and
                         * has our radar ID. If it does, we can do an update, and
                         * if not we will do an insert.
                         */
                        try {
                            queryString = String.format("SELECT count(1) AS TC FROM target WHERE acid='%s' && radar_id=%d",
                                    acid, radarid);

                            query = db1.createStatement();
                            rs = query.executeQuery(queryString);

                            if (rs.next() == true) {
                                exists = rs.getInt("TC");
                            } else {
                                exists = 0;
                            }

                            rs.close();
                            query.close();
                        } catch (SQLException e) {
                            try {
                                if (rs != null)
                                    rs.close();
                            } catch (SQLException e9) {
                            }
                            query.close();
                            continue;   // this is not good, so end pass
                        }

                        if ((trk.getOnGround() == true) || (trk.getVirtualOnGround() == true)) {
                            ground = 1;
                        } else {
                            ground = 0;
                        }

                        if (exists > 0) {         // target exists
                            queryString = String.format("UPDATE target SET utcupdate=%d,"
                                    + "altitude=NULLIF(%d, -9999),"
                                    + "groundSpeed=NULLIF(%.1f, -999.0),"
                                    + "groundTrack=NULLIF(%.1f, -999.0),"
                                    + "gsComputed=NULLIF(%.1f, -999.0),"
                                    + "gtComputed=NULLIF(%.1f, -999.0),"
                                    + "callsign='%s',"
                                    + "latitude=NULLIF(%f, -999.0),"
                                    + "longitude=NULLIF(%f, -999.0),"
                                    + "verticalRate=NULLIF(%d, -9999),"
                                    + "verticalTrend=%d,"
                                    + "quality=%d,"
                                    + "squawk=NULLIF(%d, -9999),"
                                    + "alert=%d,"
                                    + "emergency=%d,"
                                    + "spi=%d,"
                                    + "onground=%d,"
                                    + "hijack=%d,"
                                    + "comm_out=%d,"
                                    + "hadAlert=%d,"
                                    + "hadEmergency=%d,"
                                    + "hadSPI=%d"
                                    + " WHERE acid='%s' && radar_id=%d",
                                    time,
                                    trk.getAltitude(),
                                    trk.getGroundSpeed(),
                                    trk.getGroundTrack(),
                                    trk.getComputedGroundSpeed(),
                                    trk.getComputedGroundTrack(),
                                    trk.getCallsign(),
                                    trk.getLatitude(),
                                    trk.getLongitude(),
                                    trk.getVerticalRate(),
                                    trk.getVerticalTrend(),
                                    trk.getTrackQuality(),
                                    trk.getSquawk(),
                                    trk.getAlert() ? 1 : 0,
                                    trk.getEmergency() ? 1 : 0,
                                    trk.getSPI() ? 1 : 0,
                                    ground,
                                    trk.getHijack() ? 1 : 0,
                                    trk.getCommOut() ? 1 : 0,
                                    trk.getHadAlert() ? 1 : 0,
                                    trk.getHadEmergency() ? 1 : 0,
                                    trk.getHadSPI() ? 1 : 0,
                                    acid,
                                    radarid);
                        } else {                // target doesn't exist
                            queryString = String.format("INSERT INTO target ("
                                    + "acid,"
                                    + "radar_id,"
                                    + "utcdetect,"
                                    + "utcupdate,"
                                    + "altitude,"
                                    + "groundSpeed,"
                                    + "groundTrack,"
                                    + "gsComputed,"
                                    + "gtComputed,"
                                    + "callsign,"
                                    + "latitude,"
                                    + "longitude,"
                                    + "verticalRate,"
                                    + "verticalTrend,"
                                    + "quality,"
                                    + "squawk,"
                                    + "alert,"
                                    + "emergency,"
                                    + "spi,"
                                    + "onground,"
                                    + "hijack,"
                                    + "comm_out,"
                                    + "hadAlert,"
                                    + "hadEmergency,"
                                    + "hadSPI) "
                                    + "VALUES ('%s',%d,%d,%d,"
                                    + "NULLIF(%d, -9999),"
                                    + "NULLIF(%.1f,-999.0),"
                                    + "NULLIF(%.1f,-999.0),"
                                    + "NULLIF(%.1f,-999.0),"
                                    + "NULLIF(%.1f,-999.0),"
                                    + "'%s',"
                                    + "NULLIF(%f, -999.0),"
                                    + "NULLIF(%f, -999.0),"
                                    + "NULLIF(%d, -9999),"
                                    + "%d,"
                                    + "%d,"
                                    + "NULLIF(%d, -9999),"
                                    + "%d,%d,%d,%d,%d,%d,%d,%d,%d)",
                                    acid,
                                    radarid,
                                    time,
                                    time,
                                    trk.getAltitude(),
                                    trk.getGroundSpeed(),
                                    trk.getGroundTrack(),
                                    trk.getComputedGroundSpeed(),
                                    trk.getComputedGroundTrack(),
                                    trk.getCallsign(),
                                    trk.getLatitude(),
                                    trk.getLongitude(),
                                    trk.getVerticalRate(),
                                    trk.getVerticalTrend(),
                                    trk.getTrackQuality(),
                                    trk.getSquawk(),
                                    trk.getAlert() ? 1 : 0,
                                    trk.getEmergency() ? 1 : 0,
                                    trk.getSPI() ? 1 : 0,
                                    ground,
                                    trk.getHijack() ? 1 : 0,
                                    trk.getCommOut() ? 1 : 0,
                                    trk.getHadAlert() ? 1 : 0,
                                    trk.getHadEmergency() ? 1 : 0,
                                    trk.getHadSPI() ? 1 : 0);
                        }

                        try {
                            query = db1.createStatement();
                            query.executeUpdate(queryString);
                            query.close();
                        } catch (SQLException e) {
                            query.close();
                            System.out.println("ADSBDatabase::run query target Error: " + queryString + " " + e.getMessage());
                        }

                        if (trk.getUpdatePosition() == true) {
                            trk.setUpdatePosition(false);

                            // Safety check, we don't want NULL's
                            // TODO: Figure out why we get those
                            
                            if ((trk.getLatitude() != -999.0F) && (trk.getLongitude() != -999.0F)) {
                                queryString = String.format("INSERT INTO targetecho ("
                                        + "flight_id,"
                                        + "radar_id,"
                                        + "acid,"
                                        + "utcdetect,"
                                        + "latitude,"
                                        + "longitude,"
                                        + "altitude,"
                                        + "verticalTrend,"
                                        + "onground"
                                        + ") VALUES ("
                                        + "(SELECT flight_id FROM target WHERE acid='%s' && radar_id=%d),"
                                        + "%d,"
                                        + "'%s',"
                                        + "%d,"
                                        + "%f,"
                                        + "%f,"
                                        + "NULLIF(%d, -9999),"
                                        + "%d,"
                                        + "%d)",
                                        acid,
                                        radarid,
                                        radarid,
                                        acid,
                                        time,
                                        trk.getLatitude(),
                                        trk.getLongitude(),
                                        trk.getAltitude(),
                                        trk.getVerticalTrend(),
                                        ground);

                                try {
                                    query = db1.createStatement();
                                    query.executeUpdate(queryString);
                                    query.close();
                                } catch (SQLException e) {
                                    query.close();
                                    System.out.println("ADSBDatabase::run query targetecho Error: " + queryString + " " + e.getMessage());
                                }
                            }
                        }

                        if (!trk.getRegistration().equals("")) {
                            try {

                                queryString = String.format("SELECT count(1) AS RG FROM modestable"
                                        + " WHERE acid='%s'", acid);

                                query = db1.createStatement();
                                rs = query.executeQuery(queryString);

                                if (rs.next() == true) {
                                    exists = rs.getInt("RG");
                                } else {
                                    exists = 0;
                                }

                                rs.close();
                                query.close();
                            } catch (SQLException e) {
                                rs.close();
                                query.close();
                                System.out.println("ADSBDatabase::run query modestable warn: " + queryString + " " + e.getMessage());
                                continue;   // skip the following
                            }

                            if (exists > 0) {
                                queryString = String.format("UPDATE modestable SET acft_reg='%s',utcupdate=%d WHERE acid='%s'",
                                        trk.getRegistration(),
                                        time,
                                        acid);

                                query = db1.createStatement();
                                query.executeUpdate(queryString);
                                query.close();
                            }
                        }

                        if (!trk.getCallsign().equals("")) {
                            try {

                                queryString = String.format("SELECT count(1) AS CS FROM callsign"
                                        + " WHERE callsign='%s' && acid='%s' && radar_id=%d",
                                        trk.getCallsign(), acid, radarid);

                                query = db1.createStatement();
                                rs = query.executeQuery(queryString);

                                if (rs.next() == true) {
                                    exists = rs.getInt("CS");
                                } else {
                                    exists = 0;
                                }

                                rs.close();
                                query.close();
                            } catch (SQLException e) {
                                rs.close();
                                query.close();
                                System.out.println("ADSBDatabase::run query callsign warn: " + queryString + " " + e.getMessage());
                                continue;   // skip the following
                            }

                            if (exists > 0) {
                                queryString = String.format("UPDATE callsign SET utcupdate=%d WHERE callsign='%s' && acid='%s' && radar_id=%d",
                                        time, trk.getCallsign(), acid, radarid);
                            } else {
                                queryString = String.format("INSERT INTO callsign (callsign,flight_id,radar_id,acid,"
                                        + "utcdetect,utcupdate) VALUES ('%s',"
                                        + "(SELECT flight_id FROM target WHERE acid='%s' && radar_id=%d),"
                                        + "%d,'%s',%d,%d)",
                                        trk.getCallsign(),
                                        acid,
                                        radarid,
                                        radarid,
                                        acid,
                                        time,
                                        time);
                            }

                            query = db1.createStatement();
                            query.executeUpdate(queryString);
                            query.close();
                        }
                    }
                }

                /*
                 * Simulate radar RPM
                 */
                try {
                    Thread.sleep(radarscan);
                } catch (InterruptedException f) {
                }
            }
        } catch (NumberFormatException | SQLException e) {
            // Probably an I/O Exception
            try {
                query.close();
            } catch (SQLException e1) {
            }
            System.out.println("ADSBDatabase::run Exception in main loop: " + queryString + " " + e.getMessage());
            close();
        }
    }

    /**
     * TargetTimeoutThread
     *
     * A TimerTask class to move target to history after fade-out,
     */
    private class TargetTimeoutThread extends TimerTask {

        private long time;
        private long timeout;
        private final int min;

        /**
         * Clean up the target table
         *
         * @param to an int Representing the timeout in minutes
         */
        public TargetTimeoutThread(int to) {
            min = to;
        }

        @Override
        public void run() {
            String update;

            time = System.currentTimeMillis();
            timeout = time - (min * 60L * 1000L);    // timeout in milliseconds

            /*
             * This also converts the timestamp to SQL format, as the history is
             * probably not going to need any further computations.
             */
            update = String.format(
                    "INSERT INTO targethistory (flight_id,radar_id,acid,utcdetect,utcfadeout,altitude,groundSpeed,"
                    + "groundTrack,gsComputed,gtComputed,callsign,latitude,longitude,verticalRate,verticalTrend,squawk,alert,emergency,spi,onground,"
                    + "hijack,comm_out,hadAlert,hadEmergency,hadSPI) SELECT flight_id,radar_id,acid,utcdetect,utcupdate,"
                    + "altitude,groundSpeed,groundTrack,gsComputed,gtComputed,callsign,latitude,longitude,verticalRate,verticalTrend,squawk,alert,"
                    + "emergency,spi,onground,hijack,comm_out,hadAlert,hadEmergency,hadSPI FROM target WHERE target.utcupdate <= %d",
                    timeout);

            try {
                queryt = db2.createStatement();
                queryt.executeUpdate(update);
            } catch (SQLException e) {
                System.out.println("ADSBDatabase::run targethistory SQL Error: " + update + " " + e.getMessage());
            }

            update = String.format("DELETE FROM target WHERE utcupdate <= %d", timeout);

            try {
                queryt.executeUpdate(update);
            } catch (SQLException e) {
                System.out.println("ADSBDatabase:run delete SQL Error: " + update + " " + e.getMessage());
            }
        }
    }

    /**
     * MetricTimeoutThread
     *
     * A TimerTask class to reset metrics to zero,
     */
    private class MetricTimeoutThread extends TimerTask {

        private long time;

        @Override
        public void run() {
            String update;

            time = System.currentTimeMillis();

            update = String.format("INSERT INTO metrics SET utcupdate=%d,"
                    + "callsignCount=%d,surfaceCount=%d,"
                    + "airborneCount=%d,velocityCount=%d,"
                    + "altitudeCount=%d,squawkCount=%d,"
                    + "airairCount=%d,trackCount=%d,radar_id=%d",
                    time, con.getCallsignMetric(), con.getSurfaceMetric(),
                    con.getAirborneMetric(), con.getVelocityMetric(),
                    con.getAltitudeMetric(), con.getSquawkMetric(),
                    con.getAirAirMetric(),con.getTrackMetric(), radarid);

            con.resetMetricCount();

            try {
                queryt.executeUpdate(update);
                queryt.close();
            } catch (SQLException e) {
                try {
                    queryt.close();
                } catch (SQLException e4) {
                }
                System.out.println("ADSBDatabase::run metrics SQL Error: " + update + " " + e.getMessage());
            }
        }
    }
}
