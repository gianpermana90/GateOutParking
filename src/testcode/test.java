/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testcode;

import cls.RFID;
import interfaces.readerBarcode;
import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author Hades
 */
public class test implements readerBarcode{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        // TODO code application logic here
        RFID nfc = new RFID();
        nfc.readValueTAG(1).toString();
        int[] value = {0, 0, 0, 3};
        nfc.writeValueTAG(1, value);
        nfc.readValueTAG(1).toString();
        
    }

    @Override
    public void barcodeListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
