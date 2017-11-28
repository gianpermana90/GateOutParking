/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

/**
 *
 * @author Debam
 */
public class Params {
    public static boolean debug = true;
//    public static String pathFoto = "/home/pi/Documents/PintuKeluar";
    public static String pathFoto = "D:\\Gian Permana\\Projects\\Hexapark\\Program\\GateOut\\dist";
//    public static String pathFoto = "/home/administrator/img";

    public static String pathFotoServer = "img";
//    public static String pathFotoServer = "/var/www/html/hexapark/web/assets/uploads";

    public static String serverIp = "192.168.1.1";
    public static String username = "hexadata";
    public static String password = "restartsmb2palembang";
    
    public static String properties = "config.properties";
    public static String fRun = "firstRun";
    public static String plat = "BG";
    
    //Scanner without enter key code
    public static int ScannerMode = 1;
    //Scanner with enter key code
    //public static int ScannerMode = 2;
    
    //URL Database
    public static String DBurl = "jdbc:mysql://192.168.43.149:3306/hexapark";
    
}
