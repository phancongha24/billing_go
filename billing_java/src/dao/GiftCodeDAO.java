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
import java.util.Date;
import main.Billing;
import model.GiftCode;

/**
 *
 * @author Steven Huang
 */
public class GiftCodeDAO {
    
    private Connection con;
    
    public GiftCodeDAO(){
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
    
    public GiftCode findGiftCode(String code) {
        GiftCode giftcode = null;
        try {
            String sql = "SELECT id, acc_code, status, recv_date, type_giftcode, server_id, date_add, ip FROM giftcode WHERE gift_code = ?";
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setString(1, code);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                String acc_code = rs.getString(2);
                int status = rs.getInt(3);
                Date startDate = rs.getDate(4);
                int type = rs.getInt(5);
                int serverId = rs.getInt(6);
                Date endDate = rs.getDate(7);
                String ip = rs.getString(8);
                
                giftcode = new GiftCode(id, acc_code, status, startDate, type, serverId, endDate, code, ip);
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
        }
        return giftcode;
    }
    
    public void updateGiftCode(GiftCode code) {
        try {
            String sql = "UPDATE giftcode SET acc_code = ?, ip = ?, status = ?, type_giftcode = ? WHERE id = ?";
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setString(1, code.getAcc_code());
            ps.setString(2, code.getIp());
            ps.setInt(3, code.getStatus());
            ps.setInt(4, code.getType());
            ps.setInt(5, code.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            //ex.printStackTrace();
        }
    }
    
}
