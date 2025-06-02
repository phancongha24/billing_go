/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package library;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 *
 * @author Steven Huang
 */
public class PacketResolver {
    
    public static boolean isValidChar(String str) {
        return str.matches("[A-Za-z0-9_]*");
    }
    
    public static String getString(byte input[], int from, int to) {
        if (from > to || to >= input.length) return null;
        byte b[] = new byte[to - from + 1];
        System.arraycopy(input, from, b, 0, to - from + 1);
        
        try {
            return new String(b, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    
    public static int getInt(byte input[], int from, int to) {
        if (from > to || to >= input.length || to - from + 1 > 4) return Integer.MIN_VALUE;
        byte b[] = new byte[] {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };
        int j = b.length - 1;
        for (int i = to; i>= from; i--) {
            b[j--] = input[i];
        }
        
        return ByteBuffer.wrap(b).getInt();
    }
    
    public static byte[] getByte(int x) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(x).array();
    }
    
}
