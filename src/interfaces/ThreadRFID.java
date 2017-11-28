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
    
    @Override
    public void run(){
        while(true){
            try {
                int stat = card.checkCard();
                if(stat != 0){
                    System.out.println("Ada Card");
                }
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ThreadRFID.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
