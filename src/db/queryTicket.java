/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import cls.Ticket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hades
 */
public class queryTicket {
    
    //SimpleDateFormat entranceTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    SimpleDateFormat entranceTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat exitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //SimpleDateFormat exitTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    public Ticket getData(String barcode) {
        Ticket res = new Ticket();
        Koneksi connect = new Koneksi();
        Connection con = connect.logOn();
        String query = "select * from parkingtrx where trxid = '" +barcode+ "'";
        try{
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                res.setBarcode(barcode);
                res.setEntranceGate(rs.getInt("entrancegate"));
                res.setEntranceTime(entranceTime.format(rs.getTimestamp("entrancetime")).toString());
                res.setExitGate(rs.getInt("exitgate"));
                res.setExitTime(exitTime.format(rs.getTimestamp("exittime")).toString());
                res.setPrice(rs.getInt("amounttopay"));
                res.setPaymentTime(exitTime.format(rs.getTimestamp("paymenttime")).toString());
                res.setPaymentMethods(rs.getString("paymentmethods"));
                res.setLicenseNumber(rs.getString("numberplate"));
                res.setVehicleTypes(rs.getString("vehicletypes"));
                res.setTarifTypes(rs.getString("tarifftypes"));
                res.setOverNightParking(rs.getString("overnightparking"));
//                res.setGateInPicture1(rs.getString("gateinpicone"));
//                res.setGateInPicture2(rs.getString("gateinpictwo"));
//                res.setGateOutPicture1(rs.getString("gateoutpic"));
//                res.setGateOutPicture2(rs.getString("gateoutpictwo"));
            }
        }catch(SQLException e){   
            String errorCode = e.toString();
            if(errorCode.contains("can not be represented as java.sql.Timestamp")){
                System.out.println(errorCode);
                System.out.println("There is no payment process has not been made for this barcode");
            }else{
                Logger.getLogger(queryTicket.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        connect.logOff();
        return res;
    }
    
    public int updatePolNum(String idTicket, String number){
        int res = 0;
        Koneksi conn = new Koneksi();
        Connection con = conn.logOn();
        String query = "update parkingtrx set numberplate = '"+number+"' where trxid = '"+idTicket+"'";
        try {
            Statement stm = con.createStatement();
            res = stm.executeUpdate(query);
            System.out.println("Number Plate successfully updated");
        } catch (SQLException ex) {
            Logger.getLogger(queryTicket.class.getName()).log(Level.SEVERE, null, ex);
        }
        conn.logOff();
        return res;
    }
    
    public int insertExitDate(String barcode, String date){
        int res = 0;
        Koneksi conn = new Koneksi();
        Connection con = conn.logOn();
        String query = "UPDATE parkingtrx set exittime = '"+date+"' where trxid = '"+barcode+"'";
        try {
            Statement stm = con.createStatement();
            res = stm.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(queryTicket.class.getName()).log(Level.SEVERE, null, ex);
        }
        conn.logOff();
        return res;
    }

}
