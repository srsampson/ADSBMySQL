package adsbmysql;

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;

/*
 * A Class to display a GUI window.
 */
public final class GUI extends JFrame {

    private static final long RATE = 800L;              // .8 second
    //
    private final Timer timer1;
    private final TimerTask task1;
    //
    private final SocketParse process;
    private final ADSBDatabase db;

    public GUI(ADSBDatabase d, SocketParse c) {
        this.db = d;
        this.process = c;

        initComponents();

        task1 = new UpdateCounters();
        timer1 = new Timer();
        timer1.scheduleAtFixedRate(task1, 10L, RATE);
    }

    @Override
    public void finalize() {
        try {
            super.finalize();
        } catch (Throwable ex) {
        }
        timer1.cancel();
    }

    public void updateCountersDisplay() {
        type1Count.setText(String.valueOf(process.getCallsignCount()));
        type2Count.setText(String.valueOf(process.getSurfaceCount()));
        type3Count.setText(String.valueOf(process.getAirborneCount()));
        type4Count.setText(String.valueOf(process.getVelocityCount()));
        type5Count.setText(String.valueOf(process.getAltitudeCount()));
        type6Count.setText(String.valueOf(process.getSquawkCount()));
        //
        trackCounter.setText(String.valueOf(process.getTrackMetric()));
    }

    private class UpdateCounters extends TimerTask {

        @Override
        public void run() {
            updateCountersDisplay();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        clearCounterButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        type1Count = new javax.swing.JLabel();
        type2Count = new javax.swing.JLabel();
        type3Count = new javax.swing.JLabel();
        type4Count = new javax.swing.JLabel();
        type5Count = new javax.swing.JLabel();
        type6Count = new javax.swing.JLabel();
        trackCounter = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        callsign = new javax.swing.JLabel();
        surface = new javax.swing.JLabel();
        airborne = new javax.swing.JLabel();
        velocity = new javax.swing.JLabel();
        altitude = new javax.swing.JLabel();
        squawk = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ADSBMySQL 1.91");
        setBounds(new java.awt.Rectangle(300, 300, 0, 0));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel13.setText("Clear Counters");

        clearCounterButton.setText("RESET");
        clearCounterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearCounterButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(clearCounterButton)
                .addGap(28, 28, 28))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(clearCounterButton))
                .addGap(29, 29, 29))
        );

        type1Count.setBackground(new java.awt.Color(255, 255, 255));
        type1Count.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        type1Count.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        type1Count.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        type1Count.setOpaque(true);

        type2Count.setBackground(new java.awt.Color(255, 255, 255));
        type2Count.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        type2Count.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        type2Count.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        type2Count.setOpaque(true);

        type3Count.setBackground(new java.awt.Color(255, 255, 255));
        type3Count.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        type3Count.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        type3Count.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        type3Count.setOpaque(true);

        type4Count.setBackground(new java.awt.Color(255, 255, 255));
        type4Count.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        type4Count.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        type4Count.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        type4Count.setOpaque(true);

        type5Count.setBackground(new java.awt.Color(255, 255, 255));
        type5Count.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        type5Count.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        type5Count.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        type5Count.setOpaque(true);

        type6Count.setBackground(new java.awt.Color(255, 255, 255));
        type6Count.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        type6Count.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        type6Count.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        type6Count.setOpaque(true);

        trackCounter.setBackground(new java.awt.Color(255, 255, 255));
        trackCounter.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        trackCounter.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        trackCounter.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        trackCounter.setOpaque(true);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(type1Count, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(type2Count, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(type3Count, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(type4Count, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(type5Count, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(type6Count, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(trackCounter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(type1Count, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(type2Count, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(type3Count, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(type4Count, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(type5Count, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(type6Count, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(trackCounter, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        callsign.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        callsign.setText("Callsign");

        surface.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        surface.setText("Surface");

        airborne.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        airborne.setText("Airborne");

        velocity.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        velocity.setText("Velocity");

        altitude.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        altitude.setText("Altitude");

        squawk.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        squawk.setText("Squawk");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Tracks");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(callsign)
                    .addComponent(surface)
                    .addComponent(airborne)
                    .addComponent(velocity)
                    .addComponent(altitude)
                    .addComponent(squawk))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(callsign)
                .addGap(18, 18, 18)
                .addComponent(surface)
                .addGap(18, 18, 18)
                .addComponent(airborne)
                .addGap(18, 18, 18)
                .addComponent(velocity)
                .addGap(18, 18, 18)
                .addComponent(altitude)
                .addGap(18, 18, 18)
                .addComponent(squawk)
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        surface.getAccessibleContext().setAccessibleParent(jPanel1);
        airborne.getAccessibleContext().setAccessibleParent(jPanel1);
        velocity.getAccessibleContext().setAccessibleParent(jPanel1);
        altitude.getAccessibleContext().setAccessibleParent(jPanel1);
        squawk.getAccessibleContext().setAccessibleParent(jPanel1);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.getAccessibleContext().setAccessibleParent(jPanel4);
        jPanel2.getAccessibleContext().setAccessibleParent(jPanel4);
        jPanel1.getAccessibleContext().setAccessibleParent(jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clearCounterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearCounterButtonActionPerformed
        process.resetCount();
        updateCountersDisplay();
    }//GEN-LAST:event_clearCounterButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        process.close();
        db.close();
        System.runFinalization();
    }//GEN-LAST:event_formWindowClosing
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel airborne;
    private javax.swing.JLabel altitude;
    private javax.swing.JLabel callsign;
    private javax.swing.JButton clearCounterButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel squawk;
    private javax.swing.JLabel surface;
    private javax.swing.JLabel trackCounter;
    private javax.swing.JLabel type1Count;
    private javax.swing.JLabel type2Count;
    private javax.swing.JLabel type3Count;
    private javax.swing.JLabel type4Count;
    private javax.swing.JLabel type5Count;
    private javax.swing.JLabel type6Count;
    private javax.swing.JLabel velocity;
    // End of variables declaration//GEN-END:variables
}
