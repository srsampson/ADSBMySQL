package adsbmysql;

public final class Shutdown extends Thread {

    private final SocketParse kp;
    private final ADSBDatabase db;
    private final MetarUpdater mu;

    public Shutdown(SocketParse c, ADSBDatabase d, MetarUpdater m) {
        kp = c;
        db = d;
        mu = m;
    }

    @Override
    public void run() {
        System.out.println("Shutdown started");
        kp.close();
        db.close();
        mu.close();
//        System.runFinalization();
    }
}
