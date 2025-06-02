/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import main.Billing;
import model.ClientConnection;

/**
 *
 * @author hopel
 */
public class ClientConnectionDAO {
    
    private Connection con;
    
    public ClientConnectionDAO(){
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(Billing.getInstance().getConfiguration().getDbAccountUser());
        dataSource.setPassword(Billing.getInstance().getConfiguration().getDbAccountPassword());
        dataSource.setServerName(Billing.getInstance().getConfiguration().getDbAccountIP());
        dataSource.setDatabaseName(Billing.getInstance().getConfiguration().getDbAccountName());
        dataSource.setCharacterEncoding("UTF-8");
        dataSource.setCharacterSetResults("UTF-8");
        try {
            this.con = dataSource.getConnection();
        } catch (SQLException ex) {}
    }
    
    public void close() {
        try {
            this.con.close();
        } catch (SQLException ex) {
            
        }
    }
    
    public ClientConnection getClientConnectionByAccount(int serverId, String account) {
        ClientConnection macConnection = null;
        try {
            String sql = "SELECT mac_add, hardware_id, is_online FROM tbl_Connection WHERE server_id = ? AND account = ?";
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setInt(1, serverId);
            ps.setString(2, account);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String macAddress = rs.getString(1);
                String hardwareId = rs.getString(2);
                int isOnline = rs.getInt(3);
                macConnection = new ClientConnection(macAddress, hardwareId, account);
                macConnection.setIsOnline(isOnline);
            }
        } catch (SQLException ex) {
        }
        return macConnection;
    }
    
    public int getTotalClientConnectionByMacAddressOrHardwareId(int serverId, String macAddress, String hardwareId) {
        int totalConnections = 0;
        try {
            String sql = "SELECT COUNT(*) FROM tbl_Connection WHERE server_id = ? AND (hardware_id = ? OR mac_add = ?) AND is_online = ?";
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setInt(1, serverId);
            ps.setString(2, hardwareId);
            ps.setString(3, macAddress);
            ps.setInt(4, 1);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalConnections = rs.getInt(1);
            }
        } catch (SQLException ex) {
        }
        return totalConnections;
    }
    
    public boolean addNewClientConnection(int serverId, ClientConnection clientConnection) {
        try {
            String sql = "INSERT INTO tbl_Connection(mac_add, hardware_id, account, is_online, server_id) VALUES(?, ?, ?, ?, ?)";
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setString(1, clientConnection.getMacAddress());
            ps.setString(2, clientConnection.getHardwareId());
            ps.setString(3, clientConnection.getAccount());
            ps.setInt(4, clientConnection.getIsOnline());
            ps.setInt(5, serverId);
            
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
    
    public boolean deleteClientConnection(int serverId, ClientConnection clientConnection) {
        try {
            String sql = "DELETE FROM tbl_Connection WHERE server_id = ? AND account = ?";
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setInt(1, serverId);
            ps.setString(2, clientConnection.getAccount());
            
            return ps.execute();
        } catch (SQLException ex) {
            return false;
        }
    }
    
    public boolean updateClientConnection(int serverId, ClientConnection macConnection) {
        try {
            String sql = "UPDATE tbl_Connection SET is_online = ? WHERE server_id = ? AND account = ?";
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setInt(1, macConnection.getIsOnline());
            ps.setInt(2, serverId);
            ps.setString(3, macConnection.getAccount());
            
            ps.executeUpdate();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }
    
}
