package adsbmysql;

import java.util.Locale;

/*
 * Basestation Compatible Port 30003 Socket to MySQL Database
 *
 * This software listens for ADS-B/Mode-S data from a Basestation compatible
 * TCP socket port, and puts the data into MySQL database tables.
 *
 * @version 1.91
 */
public final class Main {

    private static GUI gui;
    private static String config = "adsbmysql.conf";
    private static Config c;
    private static ZuluMillis zulu;

    public static void main(String[] args) {
        zulu = new ZuluMillis();

        /*
         * The user may have a commandline option as to which config file to use
         */
        try {
            if (args[0].equals("-c") || args[0].equals("/c")) {
                config = args[1];
            }
        } catch (Exception e) {
        }

        Locale.setDefault(Locale.US);

        /*
         * Read the configuration file, which must exist or you can't proceed.
         * You need to get the database login parameters, etc...
         */
        c = new Config(config);

        /*
         * We have a config file at this point
         */
        System.out.println("Using config file: " + c.getOSConfPath());

        /*
         * Start the program
         */
        SocketParse con = new SocketParse(c, zulu);
        ADSBDatabase db = new ADSBDatabase(c, con, zulu);

        System.out.println("Program started");

        /*
         * Assume that no graphics is desired
         */
        gui = null;

        if (c.getDisableGui() == false) {
            // Found out the user would like graphics

            gui = new GUI(db, con);
            gui.setVisible(true);
        }

        MetarUpdater mu = new MetarUpdater(c, db, zulu);
        mu.start();

        Shutdown sh = new Shutdown(con, db, mu);
        Runtime.getRuntime().addShutdownHook(sh);
    }
}
