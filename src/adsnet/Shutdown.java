package adsnet;

public final class Shutdown extends Thread {

    private final SocketParse kp;
    private final ADSBDatabase db;

    public Shutdown(SocketParse c, ADSBDatabase d) {
        kp = c;
        db = d;
    }

    @Override
    public void run() {
        System.out.println("Shutdown started");
        kp.close();
        db.close();
        System.runFinalization();
    }
}
