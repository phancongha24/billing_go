/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import control.BillingSocket;
import java.io.IOException;
import library.IniFile;
import model.Configuration;

/**
 *
 * @author hopel
 */
public class Billing {

    /* Singleton - Instance */
    private static Billing INSTANCE;
    public static Billing getInstance() {
        return Billing.INSTANCE;
    }
    
    private Billing() {
        this.init();
    }
    /* End */
    
    private Configuration config;
    private Configuration readConfig() {
        try {
            IniFile iniFile = new IniFile("Config.ini");
            Configuration configuration = new Configuration();
            configuration.setBillingIp(iniFile.getString("Billing", "IP", "127.0.0.1"));
            configuration.setBillingPort(iniFile.getInt("Billing", "Port", 12680));
            
            configuration.setDbAccountIP(iniFile.getString("Account_Database", "IP", "192.168.1.3"));
            configuration.setDbAccountUser(iniFile.getString("Account_Database", "User", "tlbb"));
            configuration.setDbAccountPassword(iniFile.getString("Account_Database", "Password", "tlbb1234"));
            configuration.setDbAccountName(iniFile.getString("Account_Database", "Name", "web"));
            
            configuration.setMaxConnection(iniFile.getInt("Extend", "MaxConnection", 1));
            configuration.setServerId(iniFile.getInt("Extend", "ServerId", 1));
            return configuration;
        } catch (IOException ex) {
            System.out.println("No configuration file was found!");
            System.exit(0);
        }
        return null;
    }
    
    public Configuration getConfiguration() {
        return this.config;
    }
    
    private void init() {
        Billing.INSTANCE = this;
        this.config = this.readConfig();
    }
    
    public void start() {
        BillingSocket billingSocket = new BillingSocket();
        billingSocket.start();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Billing billing = new Billing();
        billing.start();
    }
    
}
