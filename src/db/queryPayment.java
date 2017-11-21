/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import cls.Member;
import cls.Ticket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hades
 */
public class queryPayment {

    SimpleDateFormat entranceTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    SimpleDateFormat exitTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    public int[] getPriceInfo(Ticket pym) {
        int[] res = new int[4];
        String query = "select * from tariff where types = '" + pym.getTarifTypes()+ "';";
        Koneksi connect = new Koneksi();
        Connection con = connect.logOn();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                res[0] = rs.getInt(3); //initTime
                res[1] = rs.getInt(4); //initPrice
                res[2] = rs.getInt(5); //nextHourPrice
                res[3] = rs.getInt(6); //12dayPrice
            }
        } catch (SQLException ex) {
            Logger.getLogger(queryPayment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    public Member getMemberDetails(String licenseNum, Connection con) {
        Member res = new Member();
        //Get Data Member
        String queryGetMember = "SELECT b.nopol, a.mdalamat, a.mdnama, a.mdid FROM memberdetails a, membernopol b WHERE b.nopol = '" + licenseNum + "' AND b.Memberid = a.mdid; ";
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(queryGetMember);
            res.setLicenseNumber(rs.getString(1));
            res.setInstansi(rs.getString(2));
            res.setName(rs.getString(3));
            res.setMemberID(rs.getString(4));

            //Cek Status dan Tanggal Masa Berlaku Akun Member
            Date validDate;
            String queryMasaBerlaku = "select validuntil from memberdetails where mdid = '" + res.getMemberID() + "'";
            Statement stm2 = con.createStatement();
            ResultSet rs2 = stm2.executeQuery(queryMasaBerlaku);
            if (rs2.next()) {
                validDate = rs2.getDate(1);
                System.out.println("Masa Berlaku Hingga : " + validDate);

                Calendar dateNow = Calendar.getInstance();
                Calendar dateValid = Calendar.getInstance();
                Date skr = new Date();
                dateNow.setTime(skr);
                dateValid.setTime(validDate);
                if (dateNow.before(dateValid)) {
                    res.setStatus("Aktif");
                    //System.out.println(res.getStatus());
                } else {
                    res.setStatus("Tidak Aktif");
                }
            }
            System.out.println("Status Member : " + res.getStatus());

        } catch (SQLException ex) {
            Logger.getLogger(queryPayment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    public String cekMember(String licenseNum){
        String res = "";
        Koneksi connect = new Koneksi();
        Connection con = connect.logOn();
        String query = "select * from membernopol where nopol = '" + licenseNum + "'";
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            if(rs.next()){
                res = "YES";
            }else{
                res = "NO";
            }
        } catch (SQLException ex) {
            Logger.getLogger(queryPayment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    public int determinePrice(String entranceTime, int initPrice, int nextHourPrice, int hTarif){
        int res = 0;
        String dateStart = entranceTime;
        System.out.println(dateStart);
        SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = new Date();
        try {
            date1 = dFormat.parse(dateStart); //Parsing to date (entrance Time)
        } catch (ParseException ex) {
            Logger.getLogger(queryPayment.class.getName()).log(Level.SEVERE, null, ex);
        }
        //determine parking duration
        long milis1 = date1.getTime(); //entranceTime
        long milis2 = (System.currentTimeMillis()); //currentTime
        long dif = milis2 - milis1;
        long m = TimeUnit.MILLISECONDS.toMinutes(dif);
        long h = TimeUnit.MILLISECONDS.toHours(dif);
        long s = TimeUnit.MILLISECONDS.toSeconds(dif);
        long d = TimeUnit.MILLISECONDS.toDays(dif);
        //Parking Duration
        int mDif = (int) (m % 60);
        int sDif = (int) (s % 60);
        int hDif = (int) (h & 24);
        System.out.println(hDif+" "+mDif+" "+sDif);
        
        if(h == 0){
            res = initPrice;
        }else if(h >= 12 && hTarif != 0){
            res = (int) ((nextHourPrice * (h-11)) + (int) hTarif);
        }else{
            res = (int) ((nextHourPrice * (h)) + (int) initPrice);
        }
        return res;
    }
    
    public int determinePriceInap(String entranceTime, String exitTime, int initPrice, int nextHourPrice, int initTime){
        int res = 0;
        String dateStart = entranceTime;
        String dateFinish = exitTime;
        SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = new Date();
        Date date2 = new Date();
        try {
            date1 = dFormat.parse(dateStart); //Parsing to date (entrance Time)
            date2 = dFormat.parse(dateFinish); //Parsing to date (entrance Time)
        } catch (ParseException ex) {
            Logger.getLogger(queryPayment.class.getName()).log(Level.SEVERE, null, ex);
        }
        //determine parking duration
        long milis1 = date1.getTime(); //entranceTime
        long milis2 = date2.getTime(); //currentTime
        //long milis2 = (System.currentTimeMillis()); //currentTime
        long dif = milis2 - milis1;
        long m = TimeUnit.MILLISECONDS.toMinutes(dif);
        long h = TimeUnit.MILLISECONDS.toHours(dif);
        long s = TimeUnit.MILLISECONDS.toSeconds(dif);
        long d = TimeUnit.MILLISECONDS.toDays(dif);
        //Parking Duration
        int mDif = (int) (m % 60);
        int sDif = (int) (s % 60);
        int hDif = (int) (h & 24);
        
        System.out.println(dateStart);
        System.out.println(dateFinish);
        System.out.println(hDif+" "+mDif+" "+sDif);
        
        if(initTime == 0){
            res = (int) (initPrice * (d+1));
        }else if(h <= initTime){
            res = initPrice;
        }else{
            res = (int) ((nextHourPrice * (h - initTime + 1)) + (int) initPrice);
        }
        return res;
    }
}
