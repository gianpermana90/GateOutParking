/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GateOut;

import cls.Ticket;
import config.Params;
import db.queryPayment;
import db.queryTicket;
import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Hades
 */
public class gateOut extends javax.swing.JFrame {

    /**
     * Creates new form gateOut
     */
    private Ticket tkt = new Ticket();

    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    
    private static final int BUFFER_SIZE = 4096;

    private static final long THRESHOLD = 100;
    private static final int MIN_BARCODE_LENGTH = 8;
    private final StringBuffer barcode = new StringBuffer();
    private long lastEventTimeStamp = 0L;

    public gateOut() {
        initComponents();
        setLocationRelativeTo(this);
        
        StyledDocument doc = txtOutput.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        txtOutput.setDisabledTextColor(Color.BLACK);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() != KeyEvent.KEY_RELEASED) {
                    return false;
                }

                if (e.getWhen() - lastEventTimeStamp > THRESHOLD) {
                    barcode.delete(0, barcode.length());
                }

                lastEventTimeStamp = e.getWhen();

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (barcode.length() >= MIN_BARCODE_LENGTH) {
                        getDataTicket(barcode.toString());
                        if (checkExpiredTime()) {
                            txtbarcode.setText(barcode.toString());
                            txtOutput.setText("Scan Berhasil\n \nNamun jam Pembayaran sudah melewati batas, silahkan bayar kelebihan biaya pada staf parkir terdekat");
                        } else {
                            txtbarcode.setText(barcode.toString());
                            txtOutput.setText("Scan Berhasil\n \nTerima kasih");
                        }
                    }
                    barcode.delete(0, barcode.length());
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    clearInfo();
                } else {
                    barcode.append(e.getKeyChar());
                }
                return false;
            }
        });
    }

    private boolean checkExpiredTime() {
        boolean result = false;
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //initiate payment time
            Date payTime = parser.parse(tkt.getPaymentTime());
            //intiate limit time after payment
            Date limitTime;
            Calendar cal = Calendar.getInstance();
            cal.setTime(payTime);
            cal.add(Calendar.MINUTE, 15);
            limitTime = cal.getTime();
            //compare between limit time and current time
            Date now = new Date();
            if(now.after(limitTime)){
                result = true;
            }else{
                result = false;
            }
        } catch (ParseException ex) {
            Logger.getLogger(gateOut.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex){
            System.out.println("ANDA BELUM BAYAR");
        }
        return result;
    }

    private void clearInfo() {
        txtbarcode.setText("-");
        txtGate.setText("-");
        txtJamMasuk.setText("-");
        txtJamKeluar.setText("-");
        txtNoPol.setText("-");
        txtOutput.setText("Scan tiket terlebih dahulu");
    }

    private void getDataTicket(String code) {
        try {
            tkt = new queryTicket().getData(code);
            txtGate.setText(Integer.toString(tkt.getEntranceGate()));
            txtJamMasuk.setText(tkt.getEntranceTime());
            txtJamKeluar.setText(tkt.getPaymentTime());
            txtNoPol.setText(tkt.getLicenseNumber());
        } catch (Exception e) {
            System.out.println("Ada Error");
        }
    }
    
    private void getPic(String ip) throws Exception {
        String urlip = "http://" + ip + "/cgi-bin/snapshot.cgi";
        URL url = new URL(urlip);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        String basicAuth = "Basic " + new String(Base64.encodeBase64("admin:admin".getBytes())); //jangan kesini buat ambil dari server
        httpConn.setRequestProperty("Authorization", basicAuth); //jangan kesini buat ambil dari server
        //System.out.println("Basic Out "+basicAuth); //jangan kesini buat ambil dari server
        //httpConn.setRequestMethod("GET");
        //httpConn.setDoOutput(true);
        int responseCode = httpConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            fileName = ip + ".jpg";

            //System.out.println("Content-Type = " + contentType);
            //System.out.println("Content-Disposition = " + disposition);
            //System.out.println("Content-Length = " + contentLength);
            //System.out.println("fileName = " + fileName);
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = Params.pathFoto + File.separator + fileName;

            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();

    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        panelBase = new javax.swing.JPanel();
        panelMain = new javax.swing.JPanel();
        panelScan = new javax.swing.JPanel();
        panelFront = new javax.swing.JPanel();
        txtbarcode = new javax.swing.JLabel();
        panelInfo = new javax.swing.JPanel();
        panelInfoGrid = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        txtJamMasuk = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtGate = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtJamKeluar = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtNoPol = new javax.swing.JLabel();
        panelOutput = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtOutput = new javax.swing.JTextPane();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(850, 480));
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelBase.setBackground(new java.awt.Color(102, 102, 102));

        panelScan.setBackground(new java.awt.Color(102, 102, 102));
        panelScan.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelScan.setLayout(new java.awt.CardLayout());

        panelFront.setBackground(new java.awt.Color(102, 102, 102));

        txtbarcode.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        txtbarcode.setForeground(new java.awt.Color(255, 255, 255));
        txtbarcode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtbarcode.setText("-");

        javax.swing.GroupLayout panelFrontLayout = new javax.swing.GroupLayout(panelFront);
        panelFront.setLayout(panelFrontLayout);
        panelFrontLayout.setHorizontalGroup(
            panelFrontLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFrontLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtbarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelFrontLayout.setVerticalGroup(
            panelFrontLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFrontLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtbarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelScan.add(panelFront, "card3");

        panelInfo.setBackground(new java.awt.Color(102, 102, 102));
        panelInfo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        panelInfoGrid.setBackground(new java.awt.Color(102, 102, 102));
        panelInfoGrid.setLayout(new java.awt.GridLayout(4, 2));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Jam Masuk");
        panelInfoGrid.add(jLabel8);

        txtJamMasuk.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        txtJamMasuk.setForeground(new java.awt.Color(255, 255, 255));
        txtJamMasuk.setText("-");
        panelInfoGrid.add(txtJamMasuk);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Pintu Masuk");
        panelInfoGrid.add(jLabel2);

        txtGate.setBackground(new java.awt.Color(102, 102, 102));
        txtGate.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        txtGate.setForeground(new java.awt.Color(255, 255, 255));
        txtGate.setText("-");
        panelInfoGrid.add(txtGate);

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Jam Pembayaran");
        panelInfoGrid.add(jLabel10);

        txtJamKeluar.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        txtJamKeluar.setForeground(new java.awt.Color(255, 255, 255));
        txtJamKeluar.setText("-");
        panelInfoGrid.add(txtJamKeluar);

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Nomor Polisi");
        panelInfoGrid.add(jLabel1);

        txtNoPol.setBackground(new java.awt.Color(255, 255, 255));
        txtNoPol.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        txtNoPol.setForeground(new java.awt.Color(255, 255, 255));
        txtNoPol.setText("-");
        panelInfoGrid.add(txtNoPol);

        javax.swing.GroupLayout panelInfoLayout = new javax.swing.GroupLayout(panelInfo);
        panelInfo.setLayout(panelInfoLayout);
        panelInfoLayout.setHorizontalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelInfoGrid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelInfoLayout.setVerticalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelInfoGrid, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelOutput.setBackground(new java.awt.Color(0, 0, 0));

        txtOutput.setEditable(false);
        txtOutput.setBackground(new java.awt.Color(0, 0, 0));
        txtOutput.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        txtOutput.setFocusable(false);
        txtOutput.setOpaque(false);
        jScrollPane1.setViewportView(txtOutput);

        javax.swing.GroupLayout panelOutputLayout = new javax.swing.GroupLayout(panelOutput);
        panelOutput.setLayout(panelOutputLayout);
        panelOutputLayout.setHorizontalGroup(
            panelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
        );
        panelOutputLayout.setVerticalGroup(
            panelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelScan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addComponent(panelScan, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(panelOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout panelBaseLayout = new javax.swing.GroupLayout(panelBase);
        panelBase.setLayout(panelBaseLayout);
        panelBaseLayout.setHorizontalGroup(
            panelBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBaseLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelBaseLayout.setVerticalGroup(
            panelBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBaseLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        getContentPane().add(panelBase, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 850, 480));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(gateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(gateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(gateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(gateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new gateOut().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelBase;
    private javax.swing.JPanel panelFront;
    private javax.swing.JPanel panelInfo;
    private javax.swing.JPanel panelInfoGrid;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelOutput;
    private javax.swing.JPanel panelScan;
    private javax.swing.JLabel txtGate;
    private javax.swing.JLabel txtJamKeluar;
    private javax.swing.JLabel txtJamMasuk;
    private javax.swing.JLabel txtNoPol;
    private javax.swing.JTextPane txtOutput;
    private javax.swing.JLabel txtbarcode;
    // End of variables declaration//GEN-END:variables
}
