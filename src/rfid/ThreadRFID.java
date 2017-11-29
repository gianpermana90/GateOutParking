/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rfid;

import GateOut.GateOut;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nfctools.mf.MfCardListener;
import org.nfctools.mf.MfReaderWriter;
import org.nfctools.mf.card.MfCard;

/**
 *
 * @author Hades
 */
public class ThreadRFID extends Thread {

    private GateOut ui;
    private RFIDcommand card = new RFIDcommand();
    private int memberID;

    public ThreadRFID(GateOut ui) {
        this.ui = ui;
    }
//UNDER DEVELOPMENT
    @Override
    public void run() {
        MfCardListener listener;
        listener = new MfCardListener() {
            @Override
            public void cardDetected(MfCard mfCard, MfReaderWriter mfReaderWriter) throws IOException {
                //printCardInfo(mfCard);
                memberID = card.readValueTAG(9);
                ui.getDataMember(memberID);
            }
        };

        try {
            // Start listening
            listen(listener);
        } catch (IOException ex) {
            Logger.getLogger(ThreadRFID.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Prints information about a card.
     * @param card a card
     */
    private static void printCardInfo(MfCard card) {
        System.out.println("Card detected: "
                + card.getTagType().toString() + " "
                + card.toString());
    }
    
    /**
     * Listens for cards using the provided listener.
     * @param listener a listener
     */
    private static void listen(MfCardListener listener) throws IOException {
        Acr122Device acr122;
        try {
            acr122 = new Acr122Device();
        } catch (RuntimeException re) {
            System.out.println("No ACR122 reader found.");
            return;
        }
        acr122.open();
        
        acr122.listen(listener);
        System.out.println("Press ENTER to exit");
        System.in.read();
        
        acr122.close();
    }
}
