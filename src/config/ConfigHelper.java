/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Debam
 */
public class ConfigHelper {

    public static String getValue(String key) throws IOException {
        try {
            FileInputStream in = new FileInputStream(Params.properties);
            Properties p = new Properties();
            p.load(in);

            return p.getProperty(key, "null");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigHelper.class.getName()).log(Level.SEVERE, null, ex);
            return "null";
        }

    }

    public static String getFirstRun() throws IOException {
        try {
            FileInputStream in = new FileInputStream(Params.properties);
            Properties p = new Properties();
            p.load(in);

            return p.getProperty(Params.fRun, "0");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigHelper.class.getName()).log(Level.SEVERE, null, ex);
            return "0";
        }

    }
}
