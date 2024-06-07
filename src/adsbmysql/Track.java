package adsbmysql;

/*
 * This is the vehicle track object
 */
public final class Track {

    private String acid;            // Aircraft ID
    private String registration;    // N-Number if USA registered
    private int trackQuality;       // 0 - 9 quality value (9 means Firm)
    private int verticalRate;       // fps
    private int verticalTrend;      // -1 = down, 0 = level, 1 = up
    private final int[] trend = new int[10];
    private static int trend_el = 0;
    private float groundSpeed;      // kts
    private float groundTrack;      // deg
    private float groundSpeedComputed;
    private float groundTrackComputed;
    private float latitude;         // aircraft position latitude (- is south)
    private float longitude;        // aircraft position longitude (- is west)
    private int altitude;           // aircraft current altitude in feet
    private String callsign;        // 8 character string
    private int squawk;             // 4 digit octal code
    //
    private boolean alert;          // octal code changed bit
    private boolean emergency;      // emergency bit
    private boolean spi;            // ident bit
    private boolean isOnGround;     // aircraft squat switch activated
    private boolean isVirtOnGround; // Virtual onGround for MMS2
    private boolean hijack;
    private boolean comm_out;
    //
    private boolean hadAlert;
    private boolean hadEmergency;
    private boolean hadSPI;
    //
    private final long currentTime;
    //
    private long updateTime;        // zulu time object was updated
    private long updatePositionTime;// zulu time object lat/lon position was updated
    private boolean updated;        // set on update, cleared on sent
    private boolean updatePosition;

    public Track(long ct) {
        this.currentTime = ct;
        this.acid = "";
        this.registration = "";
        this.groundSpeed = -999.0F;
        this.groundTrack = -999.0F;
        this.groundSpeedComputed = -999.0F;
        this.groundTrackComputed = -999.0F;
        this.latitude = -999.0F;
        this.longitude = -999.0F;
        this.verticalRate = -9999;
        this.verticalTrend = 0;
        this.altitude = -9999;
        this.squawk = -9999;
        this.callsign = "";
        this.trackQuality = 0;
        this.updatePositionTime = 0L;
        this.updateTime = currentTime;
        this.alert = this.emergency = this.spi = this.hadAlert
                = this.hadEmergency = this.hadSPI = hijack = comm_out = false;
        this.updated = updatePosition = false;
        this.isOnGround = isVirtOnGround = false;
    }

    /**
     * Method to increment track quality
     */
    public void incrementTrackQuality() {
        if (this.trackQuality < 9) {
            this.trackQuality++;
            this.updated = true;
        }
    }

    /**
     * Method to decrement track quality
     */
    public void decrementTrackQuality() {
        if (this.trackQuality > 0) {
            this.trackQuality--;
            this.updated = true;
        }
    }

    /**
     * Method to set the track quality
     *
     * @param val an integer Representing the track quality [0...9]
     */
    public void setTrackQuality(int val) {
        if (this.trackQuality != val) {
            this.trackQuality = val;
            this.updated = true;
        }
    }

    /**
     * Method to return track quality
     *
     * @return an integer representing the track quality [0...9]
     */
    public int getTrackQuality() {
        return this.trackQuality;
    }

    /**
     * Method to check if the track has been updated
     *
     * @return boolean which signals if the track has been updated
     */
    public boolean getUpdated() {
        return this.updated;
    }

    /**
     * Method to flag a track as being updated
     *
     * @param val a boolean which signals the track has been updated
     */
    public void setUpdated(boolean val) {
        this.updated = val;
    }

    /**
     * Method to check if the track position has been updated
     *
     * @return boolean which signals if the track position has been updated
     */
    public boolean getUpdatePosition() {
        return this.updatePosition;
    }

    /**
     * Method to flag a track position as being updated or not updated
     *
     * @param val a boolean to set or reset the track position updated status
     */
    public void setUpdatePosition(boolean val) {
        this.updatePosition = val;
    }

    /**
     * Method to return the Aircraft Mode-S Hex ID
     *
     * @return a string Representing the track Mode-S Hex ID
     */
    public String getAircraftID() {
        return this.acid;
    }

    /**
     * Method to set the Aircraft Mode-S Hex ID
     *
     * @param val a string Representing the track Mode-S Hex ID
     */
    public void setAircraftID(String val) {
        this.acid = val;
    }

    /**
     * Method to return the Aircraft N-Number Registration
     *
     * @return a string Representing the track registration
     */
    public String getRegistration() {
        return this.registration;
    }

    /**
     * Method to set the Aircraft N-Number Registration
     *
     * @param val a string Representing the track registration
     */
    public void setRegistration(String val) {
        this.registration = val;
    }

    /**
     * Method to return the tracks updated position time in milliseconds
     *
     * @return a long Representing the track updated position time in
     * milliseconds
     */
    public long getUpdatePositionTime() {
        return this.updatePositionTime;
    }

    /**
     * Method to return the tracks updated time in milliseconds
     *
     * @return a long Representing the track updated time in milliseconds
     */
    public long getUpdateTime() {
        return this.updateTime;
    }

    /**
     * Method to set the track updated time in milliseconds
     *
     * @param val a long Representing the track updated time in milliseconds
     */
    public void setUpdateTime(long val) {
        this.updateTime = val;
    }

    /**
     * Method to return the track vertical rate in feet per second The
     * resolution is +/- 64 fps, with descent being negative
     *
     * @return an integer Representing the track climb or descent rate
     */
    public int getVerticalRate() {
        return this.verticalRate;
    }

    /**
     * Method to set the track vertical rate in feet per second The resolution
     * is +/- 64 fps, with descent being negative
     *
     * @param val an integer Representing the track climb or descent rate
     */
    public void setVerticalRate(int val) {
        if (verticalRate != val) {
            verticalRate = val;
            updated = true;
        }
        
        int vt = 0;

        if (val > 192) {
            trend[trend_el] = 1;
        } else if (val < -192) {
            trend[trend_el] = -1;
        } else {
            trend[trend_el] = 0;
        }

        trend_el = (trend_el + 1) % 10;

        for (int i = 0; i < 10; i++) {
            vt += trend[i];
        }

        if (vt > 0) {
            verticalTrend = 1;
        } else if (vt < 0) {
            verticalTrend = -1;
        } else {
            verticalTrend = 0;
        }
    }

    public synchronized int getVerticalTrend() {
        return verticalTrend;
    }

    /**
     * Method used to return the target ground speed in knots
     *
     * @return target groundspeed in knots
     */
    public float getGroundSpeed() {
        return this.groundSpeed;
    }

    /**
     * Method used to return the target ground track in degrees true north.
     *
     * @return target ground track in degrees true north
     */
    public float getGroundTrack() {
        return this.groundTrack;
    }

    /**
     * Method used to return the target computed ground speed in knots
     *
     * @return target groundspeed in knots
     */
    public float getComputedGroundSpeed() {
        return this.groundSpeedComputed;
    }

    /**
     * Method used to return the target computed ground track in degrees true
     * north.
     *
     * @return target ground track in degrees true north
     */
    public float getComputedGroundTrack() {
        return this.groundTrackComputed;
    }

    public void setComputedGroundSpeed(float val) {
        this.groundSpeedComputed = val;
    }

    public void setComputedGroundTrack(float val) {
        this.groundTrackComputed = val;
    }

    /**
     * Method to set all three velocities
     *
     * <p>
     * Vertical Rate is set to zero for low values, as the aircraft tend to
     * bobble up and down in turbulence, which generates network traffic. The
     * Vertical Rate is negative for descent.
     *
     * @param val1 Ground Track in degrees
     * @param val2 Ground Speed in knots
     * @param val3 Vertical Rate in feet per second
     */
    public void setVelocityData(float val1, float val2, int val3) {
        boolean changed = false;

        if (this.groundTrack != val1) {
            this.groundTrack = val1;
            changed = true;
        }

        if (this.groundSpeed != val2) {
            this.groundSpeed = val2;
            changed = true;
        }

        if (this.verticalRate != val3) {
            this.setVerticalRate(val3);
            changed = true;
        }

        if (changed) {
            this.updated = true;
        }
    }

    /**
     * Method used to set the target altitude in feet MSL (29.92) The virtual
     * onground status is also set if altitude reads 0 feet.
     *
     * @param val an integer Representing altitude in feet MSL or -9999 for null
     */
    public void setAltitude(int val) {
        if (this.altitude != val) {
            this.altitude = val;
            this.isVirtOnGround = (val == 0);
            this.updated = true;
        }
    }

    /**
     * Method used to return the target altitude in feet MSL (29.92)
     *
     * @return an integer Representing the target altitude in feet MSL
     */
    public int getAltitude() {
        return this.altitude;
    }

    /**
     * Method used to return the target latitude in degrees (south is negative)
     *
     * @return a float Representing the target latitude
     */
    public float getLatitude() {
        return this.latitude;
    }

    /**
     * Method used to return the target longitude in degrees (west is negative)
     *
     * @return a float Representing the target longitude
     */
    public float getLongitude() {
        return this.longitude;
    }

    /**
     * Method used to set the target 2D position (latitude, longitude) (south
     * and west are negative)
     *
     * @param val1 a float Representing the target latitude
     * @param val2 a float Representing the target longitude
     */
    public void setPosition(float val1, float val2) {
        boolean changed = false;

        /*
         * Ignore the 0 Lat/ 0 Lon garbage
         */
        if (val1 == 0.0F && val2 == 0.0F) {
            return;
        }

        if (this.latitude != val1) {
            this.latitude = val1;
            changed = true;
        }

        if (this.longitude != val2) {
            this.longitude = val2;
            changed = true;
        }

        if (changed) {
            incrementTrackQuality();
            this.updated = this.updatePosition = true;
            this.updatePositionTime = currentTime;
        }
    }

    /**
     * Method used to return the target callsign
     *
     * @return a string Representing the target callsign
     */
    public String getCallsign() {
        return this.callsign;
    }

    /**
     * Method used to set the target callsign
     *
     * @param val a string Representing the target callsign
     */
    public synchronized void setCallsign(String val) {
        if (!val.equals(this.callsign)) {
            this.callsign = val;
            this.updated = true;
        }
    }

    /**
     * Method used to return the target octal 4-digit squawk
     *
     * @return an integer Representing the target octal squawk
     */
    public int getSquawk() {
        return this.squawk;
    }

    /**
     * Method used to set the target octal 4-digit squawk
     *
     * @param val an integer Representing the target octal squawk
     */
    public void setSquawk(int val) {
        if (this.squawk != val) {
            this.squawk = val;
            this.updated = true;
            this.hijack = val == 7500;
            this.comm_out = val == 7600;
        }
    }

    /**
     * Method used to return the Emergency status
     *
     * @return a boolean Representing the Emergency status
     */
    public boolean getEmergency() {
        return this.emergency;
    }

    /**
     * Method used to return the SPI status
     *
     * @return a boolean Representing the SPI status
     */
    public boolean getSPI() {
        return this.spi;
    }

    /**
     * Method used to return the Hijack status
     *
     * @return a boolean Representing the Hijack status
     */
    public boolean getHijack() {
        return this.hijack;
    }

    public boolean getCommOut() {
        return this.comm_out;
    }

    public boolean getVirtualOnGround() {
        return this.isVirtOnGround;
    }

    /**
     * Method used to return the OnGround status
     *
     * @return a boolean Representing the OnGround status
     */
    public boolean getOnGround() {
        return this.isOnGround;
    }

    /**
     * Method to set the OnGround status
     *
     * @param val a boolean Representing the OnGround status
     */
    public void setOnGround(boolean val) {
        if (this.isOnGround != val) {
            this.isOnGround = val;
            this.updated = true;
        }
    }

    /**
     * Method used to return the Alert status The Alert signals the 4-digit
     * octal squawk has changed
     *
     * @return a boolean Representing the Alert status
     */
    public boolean getAlert() {
        return this.alert;
    }

    /**
     * Method to set all the boolean bits
     *
     * <p>
     * The Alert bit is set if the 4-digit octal code is changed. The Emergency
     * bit is set if the pilot puts in the emergency code The SPI bit is set if
     * the pilots presses the Ident button.
     *
     * @param val1 a boolean Representing the status of the Alert
     * @param val2 a boolean Representing the status of the Emergency
     * @param val3 a boolean Representing the status of the SPI
     */
    public void setAlert(boolean val1, boolean val2, boolean val3) {
        boolean changed = false;

        if (this.alert != val1) {
            this.alert = val1;

            if (alert == true) {
                this.hadAlert = true;
            }

            changed = true;
        }

        if (this.emergency != val2) {
            this.emergency = val2;

            if (this.emergency == true) {
                this.hadEmergency = true;
            }

            changed = true;
        }

        if (this.spi != val3) {
            this.spi = val3;

            if (this.spi == true) {
                this.hadSPI = true;
            }

            changed = true;
        }

        if (changed) {
            this.updated = true;
        }
    }

    public boolean getHadAlert() {
        return this.hadAlert;
    }

    public boolean getHadEmergency() {
        return this.hadEmergency;
    }

    public boolean getHadSPI() {
        return this.hadSPI;
    }
}
