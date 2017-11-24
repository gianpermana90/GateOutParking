/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GateOut;

import acs.jni.ACR120S;
import java.util.Arrays;
import javax.smartcardio.CardException;

/**
 *
 * @author Hades
 */
public class RFIDreader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws CardException {
//        short readerPort = ACR120S.ACR120_COM2;
//        ACR120CardTerminal nfc = new ACR120CardTerminal(readerPort);
        Testing();
    }
    
    private static void Testing(){
        // TODO code application logic here
        short portCom = ACR120S.ACR120_COM2;
        short baud = ACR120S.ACR120_COM_BAUDRATE_9600;
        byte stationID = (byte) 1;
        byte sector = (byte) 1;
        byte keyType = (byte) ACR120S.AC_MIFARE_LOGIN_KEYTYPE_A;
        byte storedNo = (byte) ACR120S.AC_MIFARE_LOGIN_KEYTYPE_STORED_A;
        byte[] pKey = new byte[ACR120S.ACR120SJNI_MIFARE_KEY_LENGTH];
        byte[] pBlock = new byte[ACR120S.ACR120SJNI_MIFARE_BLOCK_LENGTH ];
        pBlock[0] = (byte) 0x48;
        pBlock[1] = (byte) 0x65;
        pBlock[2] = (byte) 0x6c;
        pBlock[3] = (byte) 0x6c;
        pBlock[4] = (byte) 0x6f;
        pBlock[5] = (byte) 0xff;
        pBlock[6] = (byte) 0xff;
        pBlock[7] = (byte) 0xff;
        pBlock[8] = (byte) 0xff;
        pBlock[9] = (byte) 0xff;
        pBlock[10] = (byte) 0xff;
        pBlock[11] = (byte) 0xff;
        pBlock[12] = (byte) 0xff;
        pBlock[13] = (byte) 0xff;
        pBlock[14] = (byte) 0xff;
        pBlock[15] = (byte) 0xff;
        
        pKey[0] = (byte) 0xff;
        pKey[1] = (byte) 0xff;
        pKey[2] = (byte) 0xff;
        pKey[3] = (byte) 0xff;
        pKey[4] = (byte) 0xff;
        pKey[5] = (byte) 0xff;
        
        System.out.println("PORT COM   = "+portCom);
        System.out.println("Baudrate   = "+baud);
        System.out.println("Station ID = "+stationID);
        System.out.println("Sector     = "+ sector);
        System.out.println("Key Type   = "+keyType);
        System.out.println("Stored No  = "+storedNo);
        System.out.println("Key"+Arrays.toString(pKey));

        ACR120S nfc = new ACR120S();
        byte rHandle = (byte) nfc.open(portCom, baud);
        System.out.println("Open Connection : "+rHandle);

        boolean[] pHaveTag = {true};
        byte[] pTAG = {(byte) 0xff};
        byte[] pRSN = new byte[4];
        byte[] pNumTagFound = {(byte) 0};
        
        System.out.println("Select Card : "+
                nfc.select(
                        rHandle,
                        stationID, 
                        pHaveTag, 
                        pTAG, 
                        pRSN));        
        
        System.out.println("Login : "+
                nfc.login(
                    rHandle,
                    stationID,
                    sector,
                    keyType,
                    storedNo,
                    pKey));
        
        System.out.println("Read: "+
                nfc.read(rHandle, stationID, sector, null)
        );
//        System.out.println("List Tag : "+
//                nfc.listTags(
//                        rHandle, 
//                        stationID, 
//                        pNumTagFound, 
//                        pHaveTag, 
//                        pTAG, 
//                        pRSN));
    }

}
