/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author Steven Huang
 */
public class Packet {
    
    private String type;
    private byte packet[];
    
    public Packet() {}

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getPacket() {
        return this.packet;
    }

    public void setPacket(byte[] packet) {
        this.packet = packet;
    }
    
}
