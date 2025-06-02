/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author hopel
 */
public class Configuration {
    private String billingIp;
    private int billingPort;

    private String dbAccountIP;
    private String dbAccountUser;
    private String dbAccountPassword;
    private String dbAccountName;

    private int maxConnection;
    private int serverId;

    public Configuration() {
    }

    public String getBillingIp() {
        return this.billingIp;
    }

    public void setBillingIp(String billingIp) {
        this.billingIp = billingIp;
    }

    public int getBillingPort() {
        return this.billingPort;
    }

    public void setBillingPort(int billingPort) {
        this.billingPort = billingPort;
    }

    public String getDbAccountName() {
        return this.dbAccountName;
    }

    public void setDbAccountName(String dbAccountName) {
        this.dbAccountName = dbAccountName;
    }

    public int getMaxConnection() {
        return this.maxConnection;
    }

    public void setMaxConnection(int maxConnection) {
        this.maxConnection = maxConnection;
    }

    public String getDbAccountIP() {
        return this.dbAccountIP;
    }

    public void setDbAccountIP(String dbAccountIP) {
        this.dbAccountIP = dbAccountIP;
    }

    public String getDbAccountUser() {
        return this.dbAccountUser;
    }

    public void setDbAccountUser(String dbAccountUser) {
        this.dbAccountUser = dbAccountUser;
    }

    public String getDbAccountPassword() {
        return this.dbAccountPassword;
    }

    public void setDbAccountPassword(String dbAccountPassword) {
        this.dbAccountPassword = dbAccountPassword;
    }

    public int getServerId() {
        return this.serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }
}
