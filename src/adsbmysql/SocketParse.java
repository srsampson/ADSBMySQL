package adsbmysql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This is a thread that reads the Port 30003 Format TCP data.
 *
 * It Parses the socket data, with each line being terminated with a
 * <CR><LF> There are several command formats to decode.
 *
 * We are only interested in the MSG 1-8 data, and toss out the rest.
 */
public final class SocketParse extends Thread {

    private static final int HEXIDENT = 4;
    private static final int CALLSIGN = 10;
    private static final int ALTITUDE = 11;
    private static final int GSPEED = 12;
    private static final int GTRACK = 13;
    private static final int LATITUDE = 14;
    private static final int LONGITUDE = 15;
    private static final int VRATE = 16;
    private static final int SQUAWK = 17;
    private static final int ALERT = 18;
    private static final int EMERG = 19;
    private static final int SPI = 20;
    private static final int GROUND = 21;
    //
    private static final long RATE1 = 30L * 1000L;              // 30 seconds
    private static final long RATE2 = 5L * 1000L;               // 5 seconds
    //
    private Socket connection;
    private BufferedReader line;
    //
    private final Thread socketReceive;
    //
    private static boolean EOF;
    //
    private final ConcurrentHashMap<String, Track> trackReports;
    //
    private final Config config;
    //
    private InputStream input;
    private final NConverter reg;
    //
    private final Timer timer1;
    private final Timer timer2;
    //
    private final TimerTask task1;
    private final TimerTask task2;
    //
    private long callsignMetric;
    private long surfaceMetric;
    private long airborneMetric;
    private long velocityMetric;
    private long altitudeMetric;
    private long squawkMetric;
    private long airairMetric;

    /*
     * Class constructor
     */
    public SocketParse(Config c) {
        this.config = c;

        trackReports = new ConcurrentHashMap<>();
        reg = new NConverter();

        openSBSSocket();

        socketReceive = new Thread(this);
        socketReceive.setName("SocketParse");
        socketReceive.setPriority(Thread.NORM_PRIORITY + 1);
        socketReceive.start();

        task1 = new UpdateReports();
        timer1 = new Timer();
        timer1.scheduleAtFixedRate(task1, 0L, RATE1);

        task2 = new UpdateTrackQuality();
        timer2 = new Timer();
        timer2.scheduleAtFixedRate(task2, 10L, RATE2);
    }

    /**
     * A method to open a buffered socket connection to a TCP server
     */
    private void openSBSSocket() {
        try {
            connection = new Socket(config.getSocketHost(), config.getSocketPort());
            input = connection.getInputStream();
            line = new BufferedReader(new InputStreamReader(input));
        } catch (IOException e) {
            EOF = true;
        }

        EOF = false;
    }

    /**
     * Method to close down the network TCP interface
     */
    public void close() {
        EOF = true;
        timer1.cancel();
        timer2.cancel();

        try {
            line.close();
            input.close();
            connection.close();
        } catch (IOException e) {
            System.err.println("SocketParse::close exception " + e.toString());
        }
    }

    /*
     * This is called by Database Thread 
     */
    public void resetMetricCount() {
        callsignMetric
                = surfaceMetric
                = airborneMetric
                = velocityMetric
                = altitudeMetric
                = squawkMetric
                = airairMetric = 0L;
    }

    public long getCallsignMetric() {
        return callsignMetric;
    }

    public long getSurfaceMetric() {
        return surfaceMetric;
    }

    public long getAirborneMetric() {
        return airborneMetric;
    }

    public long getVelocityMetric() {
        return velocityMetric;
    }

    public long getAltitudeMetric() {
        return altitudeMetric;
    }

    public long getSquawkMetric() {
        return squawkMetric;
    }

    public long getAirAirMetric() {
        return airairMetric;
    }

    public long getTrackMetric() {
        synchronized (trackReports) {
            return trackReports.size();
        }
    }

    /*
     * This will look through the Track table and delete entries that
     * are over X minutes old.  In that case the target has probably landed
     * or faded-out from coverage.
     */
    private class UpdateReports extends TimerTask {

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long delta;

            for (Track id : getTrackTable()) {
                // find the reports that haven't been updated in X minutes
                delta = Math.abs(currentTime - id.getUpdateTime());

                if (delta >= (config.getDatabaseTargetTimeout() * 60L * 1000L)) {
                    removeTrackReportsVal(id.getAircraftID());
                }
            }
        }
    }

    /*
     * This will look through the Track local table and decrement track quality
     * every 30 seconds that the lat/lon position isn't updated. This timer task
     * is run every 5 seconds.
     */
    private class UpdateTrackQuality extends TimerTask {

        private long delta;
        private long currentTime;

        @Override
        public void run() {
            currentTime = System.currentTimeMillis();
            delta = 0L;
            String acid;

            for (Track id : getTrackTable()) {
                try {

                    if ((id != (Track) null) && (id.getTrackQuality() > 0)) {
                        acid = id.getAircraftID();

                        // find the idStatus reports that haven't been position updated in 30 seconds
                        delta = Math.abs(currentTime - id.getUpdatePositionTime());

                        if (delta >= 30L * 1000L) {
                            id.decrementTrackQuality();
                            id.setUpdateTime(currentTime);
                            putTrackReportsVal(acid, id);   // overwrite
                        }
                    }
                } catch (NoSuchElementException e1) {
                    System.err.println("SocketParse::updateTrackQuality Exception during iteration " + e1.toString());
                }
            }
        }
    }

    /**
     * Method to make a copy of the Track objects
     *
     * @return a vector containing a copy of the Track objects
     */
    private synchronized List<Track> getTrackTable() {
        List<Track> result = new ArrayList<>();

        result.addAll(trackReports.values());

        return result;
    }

    /**
     * Method to make a copy of all modified Track objects
     *
     * @return vector containing a copy of the modified Track objects
     */
    public synchronized List<Track> getTrackUpdatedTable() {
        List<Track> result = new ArrayList<>();

        trackReports.values().stream().filter((id) -> (id.getUpdated() == true)).forEachOrdered((id) -> {
            result.add(id);
        });

        return result;
    }

    /**
     * Method to return the Track object of a specified Aircraft ID (ACID) or
     * null if not found
     *
     * @param acid a string Representing the Mode-S code for the Track requested
     * @return a track object Representing the Mode-S code requested or null if
     * none found
     */
    private synchronized Track getTrackReportsVal(String acid) {
        Track trk = (Track) null;

        try {
            trk = (Track) trackReports.get(acid);
        } catch (NullPointerException e) {
            System.err.println("SocketParse::getTrackReportsVal Exception during get " + e.toString());
        }

        return trk;
    }

    /**
     * Method to put a Track object into the Track table
     *
     * @param acid a string Representing the Mode-S key into the table
     * @param id a track object Representing the Mode-S track
     */
    private synchronized void putTrackReportsVal(String acid, Track id) {
        try {
            trackReports.put(acid, id);
        } catch (NullPointerException e) {
            System.err.println("SocketParse::putTrackReportsVal Exception during put " + e.toString());
        }
    }

    /**
     * Method to remove a Track object from the Track table
     *
     * @param acid a string Representing the Mode-S key into the table
     */
    private synchronized void removeTrackReportsVal(String acid) {
        try {
            trackReports.remove(acid);
        } catch (NullPointerException e) {
            System.err.println("SocketParse::removeTrackReportsVal Exception during remove " + e.toString());
        }
    }

    /**
     * Thread to wait for socket data and push decoded objects onto track queue
     */
    @Override
    public void run() {
        Track id;
        long currentTime;
        String acid;
        String data;
        String callsign;
        String temp;
        int altitude;
        int verticalRate;
        int squawk;
        float groundSpeed;
        float groundTrack;
        float latitude;
        float longitude;
        boolean isOnGround;
        boolean alert;
        boolean emergency;
        boolean spi;
        String[] token;
        int type, gnd;

        while (EOF == false) {
            try {
                while (line.ready()) {
                    data = line.readLine();
                    currentTime = System.currentTimeMillis();

                    if (data.startsWith("MSG")) {
                        token = data.split(",", -2);   // Tokenize the data input line
                        type = Integer.parseInt(token[1].trim());
                        acid = token[HEXIDENT].trim();

                        /*
                         * See if this ACID is on the table already
                         */
                        if ((id = getTrackReportsVal(acid)) == (Track) null) {
                            try {
                                id = new Track(currentTime);
                            } catch (Exception e) {
                                System.err.println("SocketParse::run exception: Unable to allocate a Track " + e.toString());
                                break;
                            }
                        }

                        id.setAircraftID(acid);
                        id.setRegistration(reg.icao_to_n(acid));

                        switch (type) {
                            case 8:
                                temp = token[GROUND].trim();

                                if (!temp.equals("")) {
                                    gnd = Integer.parseInt(temp);

                                    isOnGround = (gnd == -1);
                                } else {
                                    isOnGround = false;
                                }

                                id.setOnGround(isOnGround);
                                break;
                            case 1:
                                callsignMetric++;

                                try {
                                    callsign = token[CALLSIGN].replace('@', ' ').trim();  // This symbol @ means null
                                } catch (Exception e) {
                                    callsign = ""; // so replace with a null
                                }

                                id.setCallsign(callsign);
                                break;
                            case 2:
                                surfaceMetric++;

                                temp = token[ALTITUDE].trim();

                                if (!temp.equals("")) {
                                    altitude = Integer.parseInt(temp);
                                } else {
                                    altitude = -9999;
                                }

                                temp = token[GSPEED].trim();

                                if (!temp.equals("")) {
                                    groundSpeed = Float.parseFloat(temp);
                                } else {
                                    groundSpeed = -999.0F;
                                }

                                temp = token[GTRACK].trim();

                                if (!temp.equals("")) {
                                    groundTrack = Float.parseFloat(temp);
                                } else {
                                    groundTrack = -999.0F;
                                }

                                temp = token[LATITUDE].trim();

                                if (!temp.equals("")) {
                                    latitude = Float.parseFloat(temp);
                                } else {
                                    latitude = -999.0F;
                                }

                                temp = token[LONGITUDE].trim();

                                if (!temp.equals("")) {
                                    longitude = Float.parseFloat(temp);
                                } else {
                                    longitude = -999.0F;
                                }

                                temp = token[GROUND].trim();

                                if (!temp.equals("")) {
                                    gnd = Integer.parseInt(temp);

                                    isOnGround = (gnd == -1);
                                } else {
                                    isOnGround = false;
                                }

                                id.setAltitude(altitude);
                                id.setVelocityData(groundTrack, groundSpeed, 0);
                                id.setPosition(latitude, longitude);
                                id.setOnGround(isOnGround);
                                break;
                            case 3:
                                airborneMetric++;

                                temp = token[ALTITUDE].trim();

                                if (!temp.equals("")) {
                                    altitude = Integer.parseInt(temp);
                                } else {
                                    altitude = -9999;
                                }

                                temp = token[LATITUDE].trim();

                                if (!temp.equals("")) {
                                    latitude = Float.parseFloat(temp);
                                } else {
                                    latitude = -999.0F;
                                }

                                temp = token[LONGITUDE].trim();

                                if (!temp.equals("")) {
                                    longitude = Float.parseFloat(temp);
                                } else {
                                    longitude = -999.0F;
                                }

                                temp = token[ALERT].trim();

                                if (!temp.equals("")) {
                                    alert = Integer.parseInt(temp) != 0;
                                } else {
                                    alert = false;
                                }

                                temp = token[EMERG].trim();

                                if (!temp.equals("")) {
                                    emergency = Integer.parseInt(temp) != 0;
                                } else {
                                    emergency = false;
                                }

                                temp = token[SPI].trim();

                                if (!temp.equals("")) {
                                    spi = Integer.parseInt(temp) != 0;
                                } else {
                                    spi = false;
                                }

                                temp = token[GROUND].trim();

                                if (!temp.equals("")) {
                                    gnd = Integer.parseInt(temp);

                                    isOnGround = (gnd == -1);
                                } else {
                                    isOnGround = false;
                                }

                                id.setAltitude(altitude);
                                id.setPosition(latitude, longitude);
                                id.setOnGround(isOnGround);
                                id.setAlert(alert, emergency, spi);
                                break;
                            case 4:
                                velocityMetric++;

                                temp = token[GSPEED].trim();

                                if (!temp.equals("")) {
                                    groundSpeed = Float.parseFloat(temp);
                                } else {
                                    groundSpeed = -999.0F;
                                }

                                temp = token[GTRACK].trim();

                                if (!temp.equals("")) {
                                    groundTrack = Float.parseFloat(temp);
                                } else {
                                    groundTrack = -999.0F;
                                }

                                temp = token[VRATE].trim();

                                if (!temp.equals("")) {
                                    verticalRate = Integer.parseInt(temp);
                                } else {
                                    verticalRate = -9999;
                                }

                                id.setVelocityData(groundTrack, groundSpeed, verticalRate);
                                break;
                            case 5:
                                altitudeMetric++;

                                temp = token[ALTITUDE].trim();

                                if (!temp.equals("")) {
                                    altitude = Integer.parseInt(temp);
                                } else {
                                    altitude = -9999;
                                }

                                temp = token[ALERT].trim();

                                if (!temp.equals("")) {
                                    alert = Integer.parseInt(temp) != 0;
                                } else {
                                    alert = false;
                                }

                                temp = token[SPI].trim();

                                if (!temp.equals("")) {
                                    spi = Integer.parseInt(temp) != 0;
                                } else {
                                    spi = false;
                                }

                                temp = token[GROUND].trim();

                                if (!temp.equals("")) {
                                    gnd = Integer.parseInt(temp);
                                    isOnGround = (gnd == -1);
                                } else {
                                    isOnGround = false;
                                }

                                id.setAlert(alert, false, spi);
                                id.setOnGround(isOnGround);
                                id.setAltitude(altitude);
                                break;
                            case 6:
                                squawkMetric++;

                                temp = token[ALTITUDE].trim();

                                if (!temp.equals("")) {
                                    altitude = Integer.parseInt(temp);
                                } else {
                                    altitude = -9999;
                                }

                                temp = token[SQUAWK].trim();

                                if (!temp.equals("")) {
                                    squawk = Integer.parseInt(temp);
                                } else {
                                    squawk = -9999;
                                }

                                temp = token[ALERT].trim();

                                if (!temp.equals("")) {
                                    alert = Integer.parseInt(temp) != 0;
                                } else {
                                    alert = false;
                                }

                                temp = token[EMERG].trim();

                                if (!temp.equals("")) {
                                    emergency = Integer.parseInt(temp) != 0;
                                } else {
                                    emergency = false;
                                }

                                temp = token[SPI].trim();

                                if (!temp.equals("")) {
                                    spi = Integer.parseInt(temp) != 0;
                                } else {
                                    spi = false;
                                }

                                temp = token[GROUND].trim();

                                if (!temp.equals("")) {
                                    gnd = Integer.parseInt(temp);

                                    isOnGround = (gnd == -1);
                                } else {
                                    isOnGround = false;
                                }

                                id.setAlert(alert, emergency, spi);
                                id.setOnGround(isOnGround);
                                id.setAltitude(altitude);
                                id.setSquawk(squawk);
                                break;
                            case 7:
                                airairMetric++;

                                temp = token[ALTITUDE].trim();

                                if (!temp.equals("")) {
                                    altitude = Integer.parseInt(temp);
                                } else {
                                    altitude = -9999;
                                }

                                temp = token[GROUND].trim();

                                if (!temp.equals("")) {
                                    gnd = Integer.parseInt(temp);

                                    isOnGround = (gnd == -1);
                                } else {
                                    isOnGround = false;
                                }

                                id.setOnGround(isOnGround);
                                id.setAltitude(altitude);
                        }

                        id.setUpdateTime(currentTime);
                        putTrackReportsVal(acid, id);
                    }
                }

                try {
                    Thread.sleep(0, 1);
                } catch (InterruptedException e) {
                }
            } catch (IOException | NumberFormatException ex) {
            }
        }
    }
}
