package adsnet;

import java.util.Calendar;
import java.util.GregorianCalendar;

/*
 * Class to provide Zulu Time in milliseconds.
 */
public final class ZuluMillis {

    private final Calendar cal;

    public ZuluMillis() {
        this.cal = new GregorianCalendar();
    }

    /**
     * Method to return the current time in UTC milliseconds
     *
     * @return a long Representing the time UTC milliseconds
     */
    public long getUTCTime() {
        cal.setTimeInMillis(System.currentTimeMillis());

        return cal.getTimeInMillis()
                - cal.get(Calendar.ZONE_OFFSET)
                - cal.get(Calendar.DST_OFFSET);
    }
}
