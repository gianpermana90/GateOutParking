/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import GateOut.PintuKeluar;
import cls.RFID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hades
 */
public class ThreadRFID extends Thread {

    private PintuKeluar ui;
    private RFID card = new RFID();
    private int memberID;

    public ThreadRFID(PintuKeluar ui) {
        this.ui = ui;
    }
//UNDER DEVELOPMENT
    @Override
    public void run() {
        int stat = 0;
        while (true) {
            try {
                stat = card.checkCard();
                if (stat != 0) {
                    ui.txtbarcode.setText("Member");
                    memberID = card.readValueTAG(9);
                    System.out.println(memberID);
                    ui.getDataMember(memberID);
                }
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ThreadRFID.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
