/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.Date;

/**
 *
 * @author hopel
 */
public class GiftCode {
    
    private final int id;
    private String acc_code;
    private int status;
    private Date startDate;
    private int type;
    private int serverId;
    private Date validateDate;
    private String code;
    private String ip;

    public GiftCode(int id, String acc_code, int status, Date startDate, int type, int serverId, Date validateDate, String code, String ip) {
        this.id = id;
        this.acc_code = acc_code;
        this.status = status;
        this.startDate = startDate;
        this.type = type;
        this.serverId = serverId;
        this.validateDate = validateDate;
        this.code = code;
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public String getAcc_code() {
        return this.acc_code;
    }

    public void setAcc_code(String acc_code) {
        this.acc_code = acc_code;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date receivedDate) {
        this.startDate = receivedDate;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getServerId() {
        return this.serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public Date getValidateDate() {
        return this.validateDate;
    }

    public void setValidateDate(Date validateDate) {
        this.validateDate = validateDate;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    
}
