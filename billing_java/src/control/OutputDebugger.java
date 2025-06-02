/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Steven Huang
 */
public class OutputDebugger {
    
    private static String getCurrentDateTime() {
        return new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    }
    
    public static PrintWriter getDebugWriter() {
        File file = new File("log");
        if (!file.exists()) {
            file.mkdir();
        }
        
        try {
            return new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("log/" + OutputDebugger.getCurrentDateTime() + "_debug.log", true), "UTF-8")));
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            return null;
        }
    }
    
    public static PrintWriter getPacketReceivedWriter() {
        File file = new File("log");
        if (!file.exists()) {
            file.mkdir();
        }
        
        try {
            return new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("log/" + OutputDebugger.getCurrentDateTime() + "_recv_packet.log", true), "UTF-8")));
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            return null;
        }
    }
    
}
