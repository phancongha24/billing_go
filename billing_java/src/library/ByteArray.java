/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package library;

import java.io.PrintWriter;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Steven Huang
 */
public class ByteArray {
    public static int getByteArrayLength(byte[] array) {
        int length = array.length;
        while (length > 0 && array[length - 1] == 0) {
            length--;
        }
        
        return length;
    }
    
    public static void printByteArrayToHexArray(byte[] array) {
        String[] hexArray = ByteArray.byteArrayToHexStringArray(array);
        ByteArray.printHexStringArray(hexArray);
    }
    
    public static void printHexStringArray(String[] hexArray) {
        String str = String.join(" ", hexArray);
        System.out.println(str);
    }
    
    public static String[] byteArrayToHexStringArray(byte[] array) {
        String str = DatatypeConverter.printHexBinary(array);
        int length = str.length();
        while (length > 0 && str.charAt(length - 1) == '0') {
            length--;
        }
        final String result[] = new String[length / 2];
        for (int i = 0; i < length - 1; i += 2) {
            result[i / 2] = str.substring(i, i + 2);
        }
        return result;
    }
    
    public static void printByteArray(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println("");
    }
    
    public static void printByteArrayToString(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print((char) array[i] + " ");
        }
        System.out.println("");
    }
}
