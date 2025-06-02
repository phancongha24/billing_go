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
public class ClientConnection {
    
    private final String macAddress;
    private final String hardwareId;
    private final String account;
    private int isOnline;

    public ClientConnection(String macAddress, String hardwareId, String account) {
        this.macAddress = macAddress;
        this.hardwareId = hardwareId;
        this.account = account;
        this.isOnline = 0;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public String getHardwareId() {
        return this.hardwareId;
    }

    public String getAccount() {
        return this.account;
    }

    public int getIsOnline() {
        return this.isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }
    
}
