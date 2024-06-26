/*
 * MetarUpdater.java
 */
package adsbmysql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

/**
 * METAR Reader and Weather Data
 *
 * Ref: http://mtp.jpl.nasa.gov/notes/altitude/altitude.html
 *
 * @author Steve Sampson, January 2020
 */
public final class MetarUpdater {

    private static final long RATE1 = 15L * 60L * 1000L;    // 15 minutes
    private static final String NOAA = "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/";
    //
    //private static final double in_per_mb = 1.0 / 33.86389;
    private static final double mb_per_in = 33.86389;
    private static final double m_per_ft = 0.304800;
    //private static final double ft_per_m = 1.0 / .304800;
    //
    private final Timer timer1;
    private final TimerTask task1;
    //
    private URL nws;
    private BufferedReader in;
    private Statement query;
    private final Connection con;
    private final int homeAlt;
    private final String[] metarLongName;
    private final String[] metarStations;

    public MetarUpdater(Config c, ADSBDatabase d) {
        con = d.getDBConnection();
        homeAlt = c.getHomeAlt();
        metarStations = c.getMetarNames();
        metarLongName = new String[metarStations.length];

        for (int i = 0; i < metarStations.length; i++) {
            metarLongName[i] = NOAA + metarStations[i] + ".TXT";
        }

        task1 = new MetarRefresh();
        timer1 = new Timer();
    }

    public void start() {
        timer1.scheduleAtFixedRate(task1, 0L, RATE1);
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
        }

        timer1.cancel();
    }

    class MetarRefresh extends TimerTask {

        @Override
        public void run() {
            double altimeter = 0.0;
            int tc, tf = 0, dpc, dpf = 0;
            int tval, rh = 0;
            int windSpeed = 0, windDirection = 0, windGust = 0;
            String inputLine;
            String queryString;
            String utcObserve, temp, dptemp;
            long time;

            try {
                time = System.currentTimeMillis();

                for (int j = 0; j < metarLongName.length; j++) {
                    try {
                        nws = new URI(metarLongName[j]).toURL();
                    } catch (IOException | URISyntaxException e1) {
                        // Bad URL, stop thread
                        System.err.println("MetarUpdater::run fatal: Metar URL error");
                        return;
                    }
                    
                    in = new BufferedReader(new InputStreamReader(nws.openStream()));

                    do {
                        try {
                            inputLine = in.readLine();

                            if (inputLine.startsWith("ob:")) {
                                break;
                            }
                        } catch (IOException e2) {
                            // we're screwed, stop thread
                            System.err.println("MetarUpdater::run fatal: Metar IO read error");
                            in.close();
                            return;
                        }
                    } while (true);

                    in.close();

                    String[] token = inputLine.split(" ");   // Tokenize the data input line

                    utcObserve = token[2];

                    // Now it gets difficult because the positions are not fixed
                    boolean bettertemp = false;

                    for (int i = 3; i < token.length; i++) {
                        if (token[i].endsWith("KT")) {
                            if (token[i].startsWith("VRB")) {
                                windSpeed = Integer.parseInt(token[i].substring(3, 5));
                                windDirection = 360;
                            } else {
                                windDirection = Integer.parseInt(token[i].substring(0, 3));
                                windSpeed = Integer.parseInt(token[i].substring(3, 5));
                                String gval = token[i].substring(5, 6);

                                if (gval.equals("G")) {         // Looks like it has a Gust reading
                                    windGust = Integer.parseInt(token[i].substring(6, 8));
                                } else if (gval.equals("K")) {  // No Gust reading, went straight to knots
                                    windGust = 0;
                                } else {
                                    // Beats me
                                    windGust = 0;
                                }
                            }
                            continue;
                        }

                        if (token[i].startsWith("A")) {
                            if (token[i].equals("AUTO")
                                    || token[i].equals("AO2")
                                    || token[i].equals("AO2A")) {// might be AUTO or AO2
                                continue;
                            }

                            int aval = Integer.parseInt(token[i].substring(1));
                            double avald = (double) aval / 100.00;
                            altimeter = avald;

                            continue;
                        }

                        if (token[i].startsWith("Q") && token[i].length() > 3) {
                            double aval = (double) Integer.parseInt(token[i].substring(1));
                            aval *= 0.0295301; // convert to inches
                            altimeter = aval;

                            continue;
                        }

                        if (token[i].length() > 3 && token[i].substring(2, 3).equals("/")) {
                            if (bettertemp) {
                                continue;
                            }

                            if (token[i].substring(2, 5).equals("///")) {
                                continue;
                            }

                            temp = token[i].substring(0, 2);
                            dptemp = token[i].substring(3);

                            if (dptemp.substring(0, 1).equals("M")) {
                                tval = Integer.parseInt(dptemp.substring(1)) * -1;
                            } else {
                                tval = Integer.parseInt(dptemp.substring(0));
                            }

                            dpc = tval;
                            dpf = (int) (((double) dpc * (9.0 / 5.0)) + 32.0);

                            tc = Integer.parseInt(temp);

                            tf = (int) (((double) tc * (9.0 / 5.0)) + 32.0);

                            double Es = 6.11 * Math.pow(10.0, (7.5 * tc / (237.7 + tc)));
                            double E = 6.11 * Math.pow(10.0, (7.5 * dpc / (237.7 + dpc)));

                            rh = (int) ((E / Es) * 100.0);

                            continue;
                        }

                        if (token[i].substring(0, 1).equals("M")) {
                            if (bettertemp) {
                                continue;
                            }

                            temp = token[i].substring(1, 3);
                            dptemp = token[i].substring(4);

                            if (dptemp.substring(0, 1).equals("M")) {
                                tval = Integer.parseInt(dptemp.substring(1)) * -1;
                            } else {
                                tval = Integer.parseInt(dptemp.substring(0));
                            }

                            dpc = tval;
                            dpf = (int) (((double) dpc * (9.0 / 5.0)) + 32.0);

                            tc = Integer.parseInt(temp) * (-1);

                            tf = (int) (((double) tc * (9.0 / 5.0)) + 32.0);

                            double Es = 6.11 * Math.pow(10.0, (7.5 * tc / (237.7 + tc)));
                            double E = 6.11 * Math.pow(10.0, (7.5 * dpc / (237.7 + dpc)));

                            rh = (int) ((E / Es) * 100.0);

                            continue;
                        }

                        if (token[i].startsWith("T")) {
                            if (token[i].substring(1, 2).equals("W") || token[i].substring(1, 2).equals("E")) // might be TWR or TEM
                            {
                                continue;
                            }

                            bettertemp = true;
                            tval = Integer.parseInt(token[i].substring(2, 5));

                            if (token[i].substring(1, 2).equals("1")) {
                                tval *= -1;
                            }

                            tc = tval / 10;
                            tf = (int) (((double) tc * (9.0 / 5.0)) + 32.0);

                            tval = Integer.parseInt(token[i].substring(6));

                            if (token[i].substring(5, 6).equals("1")) {
                                tval *= -1;
                            }

                            dpc = tval / 10;
                            dpf = (int) (((double) dpc * (9.0 / 5.0)) + 32.0);

                            double Es = 6.11 * Math.pow(10.0, (7.5 * tc / (237.7 + tc)));
                            double E = 6.11 * Math.pow(10.0, (7.5 * dpc / (237.7 + dpc)));

                            rh = (int) ((E / Es) * 100.0);
                        }
                    }

                    /*
                     * first find station pressure
                     */
                    double h = (double) homeAlt;		// feet
                    double hm = h * m_per_ft;		// metres
                    double pstn = mb_per_in * (altimeter * Math.pow((288.0 - 0.0065 * hm) / 288.0, 5.2561));

                    /*
                     * Now we can find Pressure Altitude
                     */
                    double press = (int) ((1.0 - Math.pow((pstn / 1013.25), .190284)) * 145366.45); // answer in feet
                    int pressureAlt = (int) (press - h); // answer in feet

                    // OK, write the update to the database
                    queryString = String.format("INSERT INTO metar ("
                            + "airport,utcupdate,utcObserve,temp,dewpoint,humidity,"
                            + "altimeter,pressureAlt,windDirection,windSpeed,windGust)"
                            + " VALUES ('%s',%d,'%s',%d,%d,%d,%.2f,%d,%03d,%d,%d)",
                            metarStations[j],
                            time,
                            utcObserve,
                            tf,
                            dpf,
                            rh,
                            altimeter,
                            pressureAlt,
                            windDirection,
                            windSpeed,
                            windGust);

                    try {
                        query = con.createStatement();
                        query.executeUpdate(queryString);
                    } catch (SQLException e2) {
                        System.err.println("MetarUpdater::Insert error " + e2.getMessage());
                    }

                    query.close();

                    Thread.sleep(5000L); // give the ftp some breathing room
                }
            } catch (InterruptedException | IOException | SQLException | NumberFormatException e1) {
                System.err.println("MetarUpdater::Exception error " + e1.getMessage());
            }

            Thread.yield();
        }
    }
}
