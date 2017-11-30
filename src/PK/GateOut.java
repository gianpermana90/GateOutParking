/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PK;

import cls.Member;
import rfid.RFIDcommand;
import cls.Ticket;
import org.nfctools.mf.MfCardListener;
import org.nfctools.spi.acs.Acr122ReaderWriter;
import org.nfctools.spi.acs.AcsTerminal;
import org.nfctools.utils.CardTerminalUtils;
import config.Params;
import db.queryTicket;
import rfid.ThreadRFID;
import interfaces.readerBarcode;
import interfaces.readerRFID;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Hades
 */
public class GateOut extends javax.swing.JFrame implements readerBarcode, readerRFID {

    /**
     * Creates new form PintuKeluar
     */
    //initial value for barcode program
    private static final long THRESHOLD = 100;
    private static final int MIN_BARCODE_LENGTH = 8;
    private final StringBuffer barcode = new StringBuffer();
    private long lastEventTimeStamp = 0L;
    //tiket & member
    private Ticket tkt = new Ticket();
    private Member mbr = new Member();
    //format date
    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    //initial for get picture
    private static final int BUFFER_SIZE = 4096;
    private String saveFilePath;
    //Scanner mode
    private int scannerMode = Params.ScannerMode;
    //Thread RFID Scanner
    private ThreadRFID card = new ThreadRFID(this);
    public int MemberID = 0;

    public GateOut() {
        initComponents();
        //Set Maximum Width and Height UI
        this.setExtendedState(MAXIMIZED_BOTH);
        //Set Format Text Area
        StyledDocument doc = txtOutput.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        txtOutput.setDisabledTextColor(Color.BLACK);
        //Barcode Listener
        barcodeListener();
        //RFID Listener
        card.start();
    }

    private void getPic(String ip) throws Exception {
//        String urlip = "http://" + ip + "/cgi-bin/snapshot.cgi";
        String urlip = "http://" + ip + "/giantlab/" + ip + ".jpg";
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
            saveFilePath = Params.pathFoto + File.separator + fileName;
//            System.out.println(saveFilePath);
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
            saveFilePath = "";
        }
        httpConn.disconnect();
    }

    private void showImage(String filePath, JLabel labelCam1) {
        BufferedImage image1 = null;
        Image cam1 = null;
        String errorCode = "";
        if (!filePath.equals("")) {
            try {
                image1 = ImageIO.read(new File(filePath));
                cam1 = image1.getScaledInstance(labelCam1.getWidth(), labelCam1.getHeight(), Image.SCALE_SMOOTH);
                labelCam1.setIcon(new ImageIcon(cam1));
            } catch (IOException exp) {
                errorCode = "Gambar Tidak Ditemukan";
            }
            if (errorCode.equalsIgnoreCase("gambar tidak ditemukan")) {
                labelCam1.setText(errorCode);
                labelCam1.setIcon(null);
            } else {
                labelCam1.setText("");
            }
        } else {
            labelCam1.setText("Gambar Tidak Ditemukan");
            labelCam1.setIcon(null);
        }

    }

    private void showParkingPhotos() {
        try {
            getPic(Params.ipCam);
        } catch (Exception ex) {
            Logger.getLogger(GateOut.class.getName()).log(Level.SEVERE, null, ex);
        }
        showImage(saveFilePath, labelCam1);
        showImage(saveFilePath, labelCam2);
    }

    private int checkExpiredTime() {
        int result = 1;
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
            if (now.after(limitTime)) {
                result = 1;
            } else {
                result = 2;
            }
        } catch (ParseException ex) {
            Logger.getLogger(GateOut.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
//            System.out.println("Pembayaran Belum Dilakukan");
            result = 3;
        }
        return result;
    }

    public void getDataMember(int memberID, String expired, String parkDate, String parkTime) {
        mbr = new queryTicket().getMemberDetails(memberID);
        mbr.setStatus(parkDate.replaceAll("\\s+", "") + " " + parkTime.replace("\\s+", ""));
        mbr.setExpired(expired.replaceAll("\\s+", ""));
        txtbarcode.setText(mbr.getName());
        labelInfo1.setText("Jam Masuk");
        txtJamMasuk.setText(mbr.getStatus());
        labelInfo2.setText("Masa Berlaku");
        txtGate.setText(mbr.getExpired());
        labelInfo3.setText("Instansi");
        txtJamBayar.setText(mbr.getInstansi());
        labelInfo4.setText("Nomor Polisi");
        txtNoPol.setText(mbr.getLicenseNumber());
        txtOutput.setText("Scan Berhasil\n \nTerima kasih");
        showParkingPhotos();
        openGate();
    }

    public void openGate() {
        try {
            Process p = Runtime.getRuntime().exec("python openGate.py");
            String s = null;
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException ex) {
            Logger.getLogger(GateOut.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void clearInfo() {
        txtbarcode.setText("-");
        txtGate.setText("-");
        txtJamMasuk.setText("-");
        txtJamBayar.setText("-");
        txtNoPol.setText("-");
        txtOutput.setText("Scan tiket terlebih dahulu");
        labelCam1.setIcon(new ImageIcon(""));
        labelCam2.setIcon(null);
    }

    private void showDataTicket(String code) {
        clearInfo();
        labelInfo1.setText("Jam Masuk");
        txtGate.setText(Integer.toString(tkt.getEntranceGate()));
        labelInfo2.setText("Pintu Masuk");
        txtJamMasuk.setText(tkt.getEntranceTime());
        labelInfo3.setText("Jam Bayar");
        txtJamBayar.setText(tkt.getPaymentTime());
        labelInfo4.setText("Nomor Polisi");
        txtNoPol.setText(tkt.getLicenseNumber());
    }

    private void getDataTicket(String code) {
        try {
            tkt = new queryTicket().getData(code);
            txtGate.setText(Integer.toString(tkt.getEntranceGate()));
            txtJamMasuk.setText(tkt.getEntranceTime());
            txtJamBayar.setText(tkt.getPaymentTime());
            txtNoPol.setText(tkt.getLicenseNumber());
        } catch (Exception e) {
            System.out.println("Ada Error");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBase = new javax.swing.JPanel();
        panelFoto = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        labelCam1 = new javax.swing.JLabel();
        labelCam2 = new javax.swing.JLabel();
        panelScan = new javax.swing.JPanel();
        txtbarcode = new javax.swing.JLabel();
        panelInfo = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        labelInfo1 = new javax.swing.JLabel();
        txtJamMasuk = new javax.swing.JLabel();
        labelInfo2 = new javax.swing.JLabel();
        txtGate = new javax.swing.JLabel();
        labelInfo3 = new javax.swing.JLabel();
        txtJamBayar = new javax.swing.JLabel();
        labelInfo4 = new javax.swing.JLabel();
        txtNoPol = new javax.swing.JLabel();
        panelOutput = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtOutput = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(1366, 768));
        setUndecorated(true);

        panelBase.setBackground(new java.awt.Color(153, 153, 153));

        jPanel2.setLayout(new java.awt.GridLayout(2, 1, 0, 10));

        labelCam1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelCam1.setText("Image Cam 1");
        jPanel2.add(labelCam1);

        labelCam2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelCam2.setText("Image Cam 2");
        jPanel2.add(labelCam2);

        javax.swing.GroupLayout panelFotoLayout = new javax.swing.GroupLayout(panelFoto);
        panelFoto.setLayout(panelFotoLayout);
        panelFotoLayout.setHorizontalGroup(
            panelFotoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFotoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelFotoLayout.setVerticalGroup(
            panelFotoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFotoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        txtbarcode.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        txtbarcode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtbarcode.setText("-");
        txtbarcode.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txtbarcodePropertyChange(evt);
            }
        });

        javax.swing.GroupLayout panelScanLayout = new javax.swing.GroupLayout(panelScan);
        panelScan.setLayout(panelScanLayout);
        panelScanLayout.setHorizontalGroup(
            panelScanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScanLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtbarcode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelScanLayout.setVerticalGroup(
            panelScanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScanLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtbarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.setMaximumSize(new java.awt.Dimension(570, 316));

        labelInfo1.setFont(new java.awt.Font("Tahoma", 1, 28)); // NOI18N
        labelInfo1.setText("-");

        txtJamMasuk.setFont(new java.awt.Font("Tahoma", 1, 28)); // NOI18N
        txtJamMasuk.setText("-");

        labelInfo2.setFont(new java.awt.Font("Tahoma", 1, 28)); // NOI18N
        labelInfo2.setText("-");

        txtGate.setFont(new java.awt.Font("Tahoma", 1, 28)); // NOI18N
        txtGate.setText("-");

        labelInfo3.setFont(new java.awt.Font("Tahoma", 1, 28)); // NOI18N
        labelInfo3.setText("-");

        txtJamBayar.setFont(new java.awt.Font("Tahoma", 1, 28)); // NOI18N
        txtJamBayar.setText("-");

        labelInfo4.setFont(new java.awt.Font("Tahoma", 1, 28)); // NOI18N
        labelInfo4.setText("-");

        txtNoPol.setFont(new java.awt.Font("Tahoma", 1, 28)); // NOI18N
        txtNoPol.setText("-");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(labelInfo1, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJamMasuk, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelInfo2, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtGate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelInfo4, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNoPol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelInfo3, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtJamBayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(10, 10, 10))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelInfo1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtJamMasuk, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelInfo2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtGate, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelInfo3, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtJamBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelInfo4, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNoPol, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout panelInfoLayout = new javax.swing.GroupLayout(panelInfo);
        panelInfo.setLayout(panelInfoLayout);
        panelInfoLayout.setHorizontalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelInfoLayout.setVerticalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        txtOutput.setEditable(false);
        txtOutput.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        txtOutput.setFocusable(false);
        jScrollPane1.setViewportView(txtOutput);

        javax.swing.GroupLayout panelOutputLayout = new javax.swing.GroupLayout(panelOutput);
        panelOutput.setLayout(panelOutputLayout);
        panelOutputLayout.setHorizontalGroup(
            panelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOutputLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        panelOutputLayout.setVerticalGroup(
            panelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOutputLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelBaseLayout = new javax.swing.GroupLayout(panelBase);
        panelBase.setLayout(panelBaseLayout);
        panelBaseLayout.setHorizontalGroup(
            panelBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBaseLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelFoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelScan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelBaseLayout.setVerticalGroup(
            panelBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBaseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelFoto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelBaseLayout.createSequentialGroup()
                        .addComponent(panelScan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelBase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelBase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtbarcodePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtbarcodePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_txtbarcodePropertyChange

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
            java.util.logging.Logger.getLogger(GateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GateOut().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelCam1;
    private javax.swing.JLabel labelCam2;
    private javax.swing.JLabel labelInfo1;
    private javax.swing.JLabel labelInfo2;
    private javax.swing.JLabel labelInfo3;
    private javax.swing.JLabel labelInfo4;
    private javax.swing.JPanel panelBase;
    private javax.swing.JPanel panelFoto;
    private javax.swing.JPanel panelInfo;
    private javax.swing.JPanel panelOutput;
    private javax.swing.JPanel panelScan;
    private javax.swing.JLabel txtGate;
    private javax.swing.JLabel txtJamBayar;
    private javax.swing.JLabel txtJamMasuk;
    private javax.swing.JLabel txtNoPol;
    private javax.swing.JTextPane txtOutput;
    public javax.swing.JLabel txtbarcode;
    // End of variables declaration//GEN-END:variables

    @Override
    public void barcodeListener() {
        if (scannerMode == 1) {
            //JIKA HASIL BARCODE SCANNER TIDAK MENGANDUNG KEY CODE ENTER
            this.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent k) {
                    barcode.append(k.getKeyChar());
                    if (barcode.length() == 16) {
                        try {
                            tkt = new queryTicket().getData(barcode.toString());
                            showDataTicket(barcode.toString());
                            if (checkExpiredTime() == 1) {
                                txtbarcode.setText(barcode.toString());
                                txtOutput.setText("Scan Berhasil\n \nSaat ini anda sudah melewati batas waktu untuk keluar, silahkan bayar kelebihan biaya pada staf parkir terdekat");
                                barcode.delete(0, barcode.length());
                            } else if (checkExpiredTime() == 2) {
                                txtbarcode.setText(barcode.toString());
                                txtOutput.setText("Scan Berhasil\n \nTerima kasih");
                                barcode.delete(0, barcode.length());
                                openGate();
                            } else {
                                txtbarcode.setText(barcode.toString());
                                txtOutput.setText("Scan Berhasil\n \nPembayaran belum dilakukan, silakan melakukan pembayaran terlebih dahulu");
                                barcode.delete(0, barcode.length());
                            }
                            showParkingPhotos();
                        } catch (Exception ex) {
                            Logger.getLogger(GateOut.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (k.getKeyCode() == KeyEvent.VK_F3) {

                    } else if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        clearInfo();
                    }
                }
            });
        } else {
            //JIKA HASIL SCAN DARI BARCODE READER MENGANDUNG KEY CODE "ENTER", GUNAKAN CODE DIBAWAH
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
                            clearInfo();
                            getDataTicket(barcode.toString());
                            if (checkExpiredTime() == 1) {
                                txtbarcode.setText(barcode.toString());
                                txtOutput.setText("Scan Berhasil\n \nSaat ini anda sudah melewati batas waktu untuk keluar, silahkan bayar kelebihan biaya pada staf parkir terdekat");
                            } else if (checkExpiredTime() == 2) {
                                txtbarcode.setText(barcode.toString());
                                txtOutput.setText("Scan Berhasil\n \nTerima kasih");
                            } else {
                                txtbarcode.setText(barcode.toString());
                                txtOutput.setText("Scan Berhasil\n \nPembayaran belum dilakukan, silakan melakukan pembayaran terlebih dahulu");
                            }
                            showImage("", labelCam1);
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

    }

    @Override
    public void cardListener() {

    }
}
