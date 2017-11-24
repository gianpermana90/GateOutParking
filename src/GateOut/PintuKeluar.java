/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GateOut;

import cls.Ticket;
import config.Params;
import db.queryTicket;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class PintuKeluar extends javax.swing.JFrame {

    /**
     * Creates new form PintuKeluar
     */
    //initial value for barcode program
    private static final long THRESHOLD = 100;
    private static final int MIN_BARCODE_LENGTH = 8;
    private final StringBuffer barcode = new StringBuffer();
    private long lastEventTimeStamp = 0L;
    //tiket
    private Ticket tkt = new Ticket();
    //format date
    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    //initial for get picture
    private static final int BUFFER_SIZE = 4096;

    public PintuKeluar() {
        initComponents();
        this.setExtendedState(MAXIMIZED_BOTH);

        StyledDocument doc = txtOutput.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        txtOutput.setDisabledTextColor(Color.BLACK);

//      JIKA HASIL BARCODE SCANNER TIDAK MENGANDUNG KEY CODE ENTER
        this.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent k) {
                barcode.append(k.getKeyChar());
                if (barcode.length() == 16) {
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
                    } else {
                        txtbarcode.setText(barcode.toString());
                        txtOutput.setText("Scan Berhasil\n \nPembayaran belum dilakukan, silakan melakukan pembayaran terlebih dahulu");
                        barcode.delete(0, barcode.length());
                    }
                    showImage();
                } else if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    clearInfo();
                }
            }
        });

//        JIKA HASIL SCAN DARI BARCODE READER MENGANDUNG KEY CODE "ENTER", GUNAKAN CODE DIBAWAH
//        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
//            @Override
//            public boolean dispatchKeyEvent(KeyEvent e) {
//                if (e.getID() != KeyEvent.KEY_RELEASED) {
//                    return false;
//                }
//                
//                if (e.getWhen() - lastEventTimeStamp > THRESHOLD) {
//                    barcode.delete(0, barcode.length());
//                }
//                
//                lastEventTimeStamp = e.getWhen();
//                
//                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//                    if (barcode.length() >= MIN_BARCODE_LENGTH) {
//                        clearInfo();
//                        getDataTicket(barcode.toString());
//                        if (checkExpiredTime() == 1) {
//                            txtbarcode.setText(barcode.toString());
//                            txtOutput.setText("Scan Berhasil\n \nSaat ini anda sudah melewati batas waktu untuk keluar, silahkan bayar kelebihan biaya pada staf parkir terdekat");
//                        } else if (checkExpiredTime() == 2) {
//                            txtbarcode.setText(barcode.toString());
//                            txtOutput.setText("Scan Berhasil\n \nTerima kasih");
//                        } else {
//                            txtbarcode.setText(barcode.toString());
//                            txtOutput.setText("Scan Berhasil\n \nPembayaran belum dilakukan, silakan melakukan pembayaran terlebih dahulu");
//                        }
//                        showImage();
//                    }
//                    barcode.delete(0, barcode.length());
//                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//                    clearInfo();
//                } else {
//                    barcode.append(e.getKeyChar());
//                }
//                return false;
//            }
//        });
    }

    private void showImage() {
        BufferedImage image1 = null;
        BufferedImage image2 = null;
        Image cam1 = null;
        Image cam2 = null;
        String errorCode = "";
        try {
            URL url1 = new URL("http://192.168.43.149/giantlab/" + tkt.getBarcode() + "_1.jpg");
            URL url2 = new URL("http://192.168.43.149/giantlab/" + tkt.getBarcode() + "_2.jpg");
            image1 = ImageIO.read(url1);
            image2 = ImageIO.read(url2);
            cam1 = image1.getScaledInstance(labelCam1.getWidth(), labelCam1.getHeight(), Image.SCALE_SMOOTH);
            cam2 = image2.getScaledInstance(labelCam1.getWidth(), labelCam1.getHeight(), Image.SCALE_SMOOTH);
            labelCam1.setIcon(new ImageIcon(cam1));
            labelCam2.setIcon(new ImageIcon(cam2));
        } catch (IOException exp) {
            errorCode = "Gambar Tidak Ditemukan";

        }
        if (errorCode.equalsIgnoreCase("gambar tidak ditemukan")) {
            labelCam1.setText(errorCode);
            labelCam2.setText(errorCode);
        }else{
            labelCam1.setText("");
            labelCam2.setText("");
        }

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
            Logger.getLogger(gateOut.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            System.out.println("Pembayaran Belum Dilakukan");
            result = 3;
        }
        return result;
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
        txtGate.setText(Integer.toString(tkt.getEntranceGate()));
        txtJamMasuk.setText(tkt.getEntranceTime());
        txtJamBayar.setText(tkt.getPaymentTime());
        txtNoPol.setText(tkt.getLicenseNumber());
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

        panelBase = new javax.swing.JPanel();
        panelFoto = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        labelCam1 = new javax.swing.JLabel();
        labelCam2 = new javax.swing.JLabel();
        panelScan = new javax.swing.JPanel();
        txtbarcode = new javax.swing.JLabel();
        panelInfo = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtJamMasuk = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtGate = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtJamBayar = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtNoPol = new javax.swing.JLabel();
        panelOutput = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtOutput = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
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
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE)
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
        txtbarcode.setText("Scan tiket terlebih dahulu ...");

        javax.swing.GroupLayout panelScanLayout = new javax.swing.GroupLayout(panelScan);
        panelScan.setLayout(panelScanLayout);
        panelScanLayout.setHorizontalGroup(
            panelScanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScanLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtbarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelScanLayout.setVerticalGroup(
            panelScanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScanLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtbarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.setLayout(new java.awt.GridLayout(4, 2));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel2.setText("Jam Masuk");
        jPanel1.add(jLabel2);

        txtJamMasuk.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        txtJamMasuk.setText("-");
        jPanel1.add(txtJamMasuk);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel4.setText("Pintu Masuk");
        jPanel1.add(jLabel4);

        txtGate.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        txtGate.setText("-");
        jPanel1.add(txtGate);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel6.setText("Jam Bayar");
        jPanel1.add(jLabel6);

        txtJamBayar.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        txtJamBayar.setText("-");
        jPanel1.add(txtJamBayar);

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel8.setText("Nomor Polisi");
        jPanel1.add(jLabel8);

        txtNoPol.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        txtNoPol.setText("-");
        jPanel1.add(txtNoPol);

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
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addContainerGap())
        );

        txtOutput.setEditable(false);
        txtOutput.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        txtOutput.setText("Hello World");
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
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
                .addGroup(panelBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
            java.util.logging.Logger.getLogger(PintuKeluar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PintuKeluar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PintuKeluar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PintuKeluar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PintuKeluar().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelCam1;
    private javax.swing.JLabel labelCam2;
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
    private javax.swing.JLabel txtbarcode;
    // End of variables declaration//GEN-END:variables
}
