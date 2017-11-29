/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testcode;

import cls.RFID;
import interfaces.readerBarcode;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ResponseAPDU;
import org.nfctools.mf.MfCardListener;
import org.nfctools.spi.acs.Acr122ReaderWriter;

/**
 *
 * @author Hades
 */
public class test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        RFID nfc = new RFID();
        //Read ID Card
        //nfc.readValueTAG(8).toString();
        int t = nfc.readValueTAG(9);
        System.out.println(t);
        //write id card
//        int[] value = {0, 0, 0, 2};
//        nfc.writeValueTAG(8, value);
        //read ID Card
        //nfc.readValueTAG(8).toString();
        //Write Member Name        
//        int[] name = {0x48, 0x69, 0x61, 0x6E, 0x20, 0x50, 0x65, 0x72, 0x6D, 0x61, 0x6E, 0x61, 0x20, 0x20, 0x20, 0x20};
//        nfc.writeTAG(12, name);
//        //Read Member Name
//        nfc.readTAG(12);                        
    }    
}
