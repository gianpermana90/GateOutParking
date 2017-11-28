/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testcode;

/**
 *
 * @author Hades
 */
import java.util.List;
import java.math.BigInteger;
import javax.smartcardio.*;

public class tagscan {

    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    public static void main(String[] args) {

        try {

            // Display the list of terminals
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            System.out.println("Terminals: " + terminals);

            // Use the first terminal
            CardTerminal terminal = terminals.get(0);

            // Connect wit hthe card
            Card card = terminal.connect("*");
            System.out.println("Card: " + card);
            CardChannel channel = card.getBasicChannel();

            // Send test command
            ResponseAPDU response = channel.transmit(new CommandAPDU(new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00}));
            System.out.println("Response: " + response.toString());
            if (response.getSW1() == 0x63 && response.getSW2() == 0x00) {
                System.out.println("Failed");
            }
            System.out.println("UID: " + bin2hex(response.getData()));

            //Auth
            ResponseAPDU auth = channel.transmit(new CommandAPDU(new byte[]{
            (byte) 0xFF, 
            (byte) 0x00, 
            (byte) 0x00, 
            (byte) 0x00, 
            (byte) 0x02, 
            (byte) 0xD4, 
            (byte) 0x04}));
            System.out.println("UID: " + bin2hex(auth.getData()));
            
            ResponseAPDU authen = channel.transmit(new CommandAPDU(new byte[]{
                (byte) 0xFF, 
                (byte) 0x86, 
                (byte) 0x00, 
                (byte) 0x00, 
                (byte) 0x05, 
                (byte) 0x01, 
                (byte) 0x00, 
                (byte) 0x01, 
                (byte) 0x60, 
                (byte) 0x00}));
            System.out.println("Response: " + authen.toString());
            
            
            if (authen.getSW1() == 0x63 && authen.getSW2() == 0x00) {
                System.out.println("Failed");
            }else{
                System.out.println("Success");
            }
            
//            Write Data
            ResponseAPDU write = channel.transmit(new CommandAPDU(new byte[]{
                (byte) 0xFF, 
                (byte) 0xD7, 
                (byte) 0x00, 
                (byte) 0x01, 
                (byte) 0x05, 
                (byte) 0x00, 
                (byte) 0x00, 
                (byte) 0x00, 
                (byte) 0x00, 
                (byte) 0x08}));
            System.out.println("Response: " + write.toString());
            if (write.getSW1() == 0x63 && write.getSW2() == 0x00) {
                System.out.println("Failed");
            }else{
                System.out.println("Success");
            }
            
//            Read Data
            ResponseAPDU readVal = channel.transmit(new CommandAPDU(new byte[]{
                (byte) 0xFF, 
                (byte) 0xB1, 
                (byte) 0x00, 
                (byte) 0x01, 
                (byte) 0x04}));
            System.out.println("Response: " + readVal.toString());
            System.out.println("UID: " + bin2hex(readVal.getData()));
            if (readVal.getSW1() == 0x63 && readVal.getSW2() == 0x00) {
                System.out.println("Failed");
            }else{
                System.out.println("Success");
            }
            
            // Disconnect the card
            card.disconnect(false);

        } catch (Exception e) {

            System.out.println("Ouch: " + e.toString());

        }
    }
}
