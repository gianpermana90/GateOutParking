/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rfid;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import org.nfctools.spi.acs.Acr122ReaderWriter;
import org.nfctools.utils.CardTerminalUtils;

/**
 *
 * @author Hades
 */
public class RFIDcommand {
    
    private Acr122ReaderWriter readerWriter;

    private TerminalFactory factory;
    private CardChannel channel;
    CardTerminal terminal;

    public RFIDcommand() {
        this.factory = TerminalFactory.getDefault();
        //this.terminal = CardTerminalUtils.getTerminalByName("ACR122");
        initTerminal();
    }

    public int checkCard() {
        int res = 0;
        try {
//            System.out.println(terminal.connect("*"));
            terminal.connect("*");
            res = 1;
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            if (ex.toString().contains("No card present")) {
                res = 0;
            }
        }
        return res;
    }

    public void initTerminal() {
        try {
            //save and show list of terminals
            List<CardTerminal> terminals = factory.terminals().list();
            System.out.println("Terminals : " + terminals);
            //Use first terminal
            terminal = terminals.get(0);
            //Connect with the card

        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            if (ex.toString().contains("No card present")) {
                System.out.println("No card present");
            }
        }
    }

    private void doAuth(int block) {
        ResponseAPDU result = null;
        try {
            Card card = terminal.connect("*");
//            System.out.println("Card : " + card);
            channel = card.getBasicChannel();
            result = channel.transmit(new CommandAPDU(new byte[]{
                (byte) 0xFF,
                (byte) 0x86,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x05,
                (byte) 0x01,
                (byte) 0x00,
                (byte) Byte.parseByte(Integer.toString(block)),
                (byte) 0x60,
                (byte) 0x00
            }));
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Authentication failed !");
            if (ex.toString().contains("No card present")) {
                System.out.println("No card present");
            }
        } catch (NullPointerException e) {
            System.out.println("No Card Found");
        }
//        System.out.println(result.toString());
    }

    public int readValueTAG(int block) {
        int hasil = 0;
        ResponseAPDU result = null;
        doAuth(block);
        try {
            result = channel.transmit(new CommandAPDU(new byte[]{
                (byte) 0xFF,
                (byte) 0xB1,
                (byte) 0x00,
                (byte) Byte.parseByte(Integer.toHexString(block)),
                (byte) 0x04
            }));
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Failed to read data !");
        }
//        System.out.println("Read Value Status : " + bin2hex(result.getData()));
        hasil = Integer.parseInt(bin2hex(result.getData()),16);
        return hasil;
    }

    public String readTAG(int block) {
        String hasil = "";
        ResponseAPDU result = null;
        doAuth(block);
        try {
            result = channel.transmit(new CommandAPDU(new byte[]{
                (byte) 0xFF,
                (byte) 0xB0,
                (byte) 0x00,
                (byte) Byte.parseByte(Integer.toString(block)),
                (byte) 0x10
            }));
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Failed to read data !");
        }
//        System.out.println("Read Status : " + convertHexToString(bin2hex(result.getData())));
        hasil = convertHexToString(bin2hex(result.getData()));
        return hasil;
    }

    public ResponseAPDU writeValueTAG(int block, int[] value) {
        ResponseAPDU result = null;
        doAuth(block);
        try {
            result = channel.transmit(new CommandAPDU(new byte[]{
                (byte) 0xFF,
                (byte) 0xD7,
                (byte) 0x00,
                (byte) Byte.parseByte(Integer.toString(block)),
                (byte) 0x05,
                (byte) 0x00,
                (byte) Byte.parseByte(Integer.toString(value[0])),
                (byte) Byte.parseByte(Integer.toString(value[1])),
                (byte) Byte.parseByte(Integer.toString(value[2])),
                (byte) Byte.parseByte(Integer.toString(value[3])),}));
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Failed to write value data !");
        }
//        System.out.println("Write ValueStatus : " + result.toString());
        return result;
    }

    public ResponseAPDU writeTAG(int block, int[] value) {
        ResponseAPDU result = null;
        doAuth(block);
        try {
            result = channel.transmit(new CommandAPDU(new byte[]{
                (byte) 0xFF,
                (byte) 0xD6,
                (byte) 0x00,
                (byte) Byte.parseByte(Integer.toString(block)),
                (byte) 0x10,
                (byte) Byte.parseByte(Integer.toString(value[0])),
                (byte) Byte.parseByte(Integer.toString(value[1])),
                (byte) Byte.parseByte(Integer.toString(value[2])),
                (byte) Byte.parseByte(Integer.toString(value[3])),
                (byte) Byte.parseByte(Integer.toString(value[4])),
                (byte) Byte.parseByte(Integer.toString(value[5])),
                (byte) Byte.parseByte(Integer.toString(value[6])),
                (byte) Byte.parseByte(Integer.toString(value[7])),
                (byte) Byte.parseByte(Integer.toString(value[8])),
                (byte) Byte.parseByte(Integer.toString(value[9])),
                (byte) Byte.parseByte(Integer.toString(value[10])),
                (byte) Byte.parseByte(Integer.toString(value[11])),
                (byte) Byte.parseByte(Integer.toString(value[12])),
                (byte) Byte.parseByte(Integer.toString(value[13])),
                (byte) Byte.parseByte(Integer.toString(value[14])),
                (byte) Byte.parseByte(Integer.toString(value[15]))
            }));
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Failed to write data !");
        }
//        System.out.println("Write ValueStatus : " + result.toString());
        return result;
    }

    public String bin2hex(byte[] data) {
        String res = "";
        try{
            res = String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
        }catch(Exception e){
            System.out.println("Coba Lagi");
        }
        return res;
    }
    
    public String convertHexToString(String hex){
	  StringBuilder sb = new StringBuilder();
	  StringBuilder temp = new StringBuilder();
	  //49204c6f7665204a617661 split into two characters 49, 20, 4c...
	  for( int i=0; i<hex.length()-1; i+=2 ){
	      //grab the hex in pairs
	      String output = hex.substring(i, (i + 2));
	      //convert hex to decimal
	      int decimal = Integer.parseInt(output, 16);
	      //convert the decimal to character
	      sb.append((char)decimal);
	      temp.append(decimal);
	  }
//	  System.out.println("Decimal : " + temp.toString());
	  return sb.toString();
  }

}
