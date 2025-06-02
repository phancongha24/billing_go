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
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Billing;
import model.Account;

/**
 *
 * @author Steven Huang
 */
public class AccountDAO {
    
    private Connection con;
    
    public AccountDAO(){
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(Billing.getInstance().getConfiguration().getDbAccountUser());
        dataSource.setPassword(Billing.getInstance().getConfiguration().getDbAccountPassword());
        dataSource.setServerName(Billing.getInstance().getConfiguration().getDbAccountIP());
        dataSource.setDatabaseName(Billing.getInstance().getConfiguration().getDbAccountName());
        dataSource.setCharacterEncoding("UTF-8");
        dataSource.setCharacterSetResults("UTF-8");
        try {
            this.con = dataSource.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void close() {
        try {
            this.con.close();
        } catch (SQLException ex) {
            
        }
    }
    
    public Account findAccountByUsernameAndPassword(String username, String password) {
        Account account = null;
        try {
            String sql = "SELECT id, question, answer, email, locked, point FROM account WHERE name = ? AND password = ?";
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                String secretQuestion = rs.getString(2);
                String secretAnswert = rs.getString(3);
                String email = rs.getString(4);
                int lockStatus = rs.getInt(5);
                int point = rs.getInt(6);
                
                account = new Account(id, username, password, secretQuestion, secretAnswert, email, lockStatus, point);
            }
        } catch (SQLException ex) {}
        return account;
    }
    
    public Account findAccountByUsername(String username) {
        Account account = null;
        try {
            String sql = "SELECT id, password, question, answer, email, locked, point FROM account WHERE name = ?";
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setString(1, username);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                String password = rs.getString(2);
                String secretQuestion = rs.getString(3);
                String secretAnswert = rs.getString(4);
                String email = rs.getString(5);
                int lockStatus = rs.getInt(6);
                int point = rs.getInt(7);
                
                account = new Account(id, username, password, secretQuestion, secretAnswert, email, lockStatus, point);
            }
        } catch (SQLException ex) {}
        return account;
    }
    
    public boolean updateAccount(Account account) {
        try {
            this.con.setAutoCommit(false);
            String sql = "UPDATE account SET question = ?, answer = ?, email = ?, locked = ?, point = ? WHERE id = ?";
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setString(1, account.getSecretQuestion());
            ps.setString(2, account.getSecretAnswer());
            ps.setString(3, account.getEmail());
            ps.setInt(4, account.getLockStatus());
            ps.setInt(5, account.getPoint());
            ps.setInt(6, account.getID());
            
            ps.executeUpdate();
            this.con.commit();
        } catch (SQLException ex) {
            try {
                this.con.rollback();
            } catch (SQLException e) {}
            return false;
        } finally {
            try {
                this.con.setAutoCommit(true);
            } catch (SQLException e) {}
        }
        return true;
    }
    
}
