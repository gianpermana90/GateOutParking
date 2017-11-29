/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

/**
 *
 * @author rakha
 */

import config.Params;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private Connection con;
    private String driver = "com.mysql.jdbc.Driver";
    private String url = Params.DBurl; // url + nama database
    private String username = "root";// user name dbms
    private String password = "";// pswd dbms
    
    public Connection logOn(){
        try{
            //load JDBC Driver
            Class.forName(driver).newInstance();
            //Buat object Connection
            con = DriverManager.getConnection(url, username, password);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return con;
    }
    
    public void logOff(){
        try{
            //Tutup Koneksi
            con.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
