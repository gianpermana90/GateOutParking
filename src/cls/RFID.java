/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cls;

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

/**
 *
 * @author Hades
 */
public class RFID {
    
    private TerminalFactory factory;
    private CardChannel channel;
    CardTerminal terminal;

    public RFID() {
        this.factory = TerminalFactory.getDefault();;
        initTerminal();
    }
    
    public int checkCard(){
        int res = 0;
        try {
            System.out.println(terminal.connect("*"));
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            if(ex.toString().contains("No card present")){
                res = 0;
            }else{
                res = 1;
            }
        }
        return res;
    }
    
    public String bin2hex(byte[] data){
        return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data));
    }
    
    public void initTerminal(){
        try {
            //save and show list of terminals
            List<CardTerminal> terminals = factory.terminals().list();
            System.out.println("Terminals : " + terminals);
            //Use first terminal
            terminal = terminals.get(0);
            //Connect with the card
            Card card = terminal.connect("*");
            System.out.println("Card : "+ card);
            channel = card.getBasicChannel();
            
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            if(ex.toString().contains("No card present")){
                System.out.println("No card present");
            }
        }      
    }
    
    private void doAuth(){
        ResponseAPDU result = null;
        try {
            result = channel.transmit(new CommandAPDU(new byte[]{
            (byte) 0xFF, 
            (byte) 0x86, 
            (byte) 0x00, 
            (byte) 0x00, 
            (byte) 0x05, 
            (byte) 0x01, 
            (byte) 0x00, 
            (byte) 0x01, 
            (byte) 0x60, 
            (byte) 0x00
            }));
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Authentication failed !");
        }catch(NullPointerException e){
            System.out.println("No Card Found");
        }
        System.out.println(result.toString());
    }
    
    public ResponseAPDU readValueTAG(int block){
        ResponseAPDU result = null;
        doAuth();
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
        System.out.println("Read Value Status : "+bin2hex(result.getData()));
        return result;
    }
    
    public ResponseAPDU readTAG(int block){
        ResponseAPDU result = null;
        doAuth();
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
        System.out.println("Read Status : "+bin2hex(result.getData()));
        return result;
    }
    
    public ResponseAPDU writeValueTAG(int block, int[] value){
        ResponseAPDU result = null;
        doAuth();
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
            (byte) Byte.parseByte(Integer.toString(value[3])),  
            }));
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Failed to write value data !");
        }
        System.out.println("Write ValueStatus : " + result.toString());
        return result;
    }
    
    public ResponseAPDU writeTAG(int block, int[] value){
        ResponseAPDU result = null;
        doAuth();
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
            (byte) Byte.parseByte(Integer.toString(value[3])),  
            }));
        } catch (CardException ex) {
            //Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Failed to write data !");
        }
        System.out.println("Write ValueStatus : " + result.toString());
        return result;
    }
    
    
}
