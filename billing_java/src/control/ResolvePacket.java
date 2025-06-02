/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import dao.AccountDAO;
import dao.GiftCodeDAO;
import dao.ClientConnectionDAO;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import library.ByteArray;
import library.PacketResolver;
import main.Billing;
import model.Account;
import model.GiftCode;
import model.ClientConnection;

/**
 *
 * @author Steven Huang
 */
class ResolvePacket {

    private static String getCurrentTimeStamp() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    /*Check type of packet*/
    public static boolean isOpeningSocketPacket(byte packet[]) {
        if (packet.length < 5) {
            return false;
        }
        return packet[4] == (byte) 0xA0;
    }

    public static boolean isHandlingPacket(byte packet[]) {
        if (packet.length < 5) {
            return false;
        }
        return packet[4] == (byte) 0xA1;
    }

    public static boolean isLoginPacket(byte packet[]) {
        if (packet.length < 5) {
            return false;
        }
        return packet[4] == (byte) 0xA2;
    }

    public static boolean isSelectCharacterPacket(byte packet[]) {
        if (packet.length < 5) {
            return false;
        }
        return packet[4] == (byte) 0xA3;
    }

    public static boolean isOutGamePacket(byte packet[]) {
        if (packet.length < 5) {
            return false;
        }
        return packet[4] == (byte) 0xA4;
    }

    public static boolean isAskPointPacket(byte packet[]) {
        if (packet.length < 5) {
            return false;
        }
        return packet[4] == (byte) 0xE2;
    }

    public static boolean isExchangePointPacket(byte packet[]) {
        if (packet.length < 5) {
            return false;
        }
        return packet[4] == (byte) 0xE1;
    }

    public static boolean isActivateCodePacket(byte packet[]) {
        if (packet.length < 5) {
            return false;
        }
        return packet[4] == (byte) 0xC6;
    }

    public static boolean isCheckCharacterOnlinePacket(byte packet[]) {
        if (packet.length < 5) {
            return false;
        }
        return packet[4] == (byte) 0xA6;
    }

    public static boolean isGetCodeRewardPacket(byte packet[]) {
        if (packet.length < 5) {
            return false;
        }
        return packet[4] == (byte) 0xC1;
    }

    /*End*/

 /*Solve packet*/
    public static byte[] solveOpeningSocketPacket(byte packet[]) {
        return new byte[]{
            (byte) 0xAA, (byte) 0x55, (byte) 0x00, (byte) 0x05, (byte) 0xA0, (byte) 0x11, (byte) 0x98, (byte) 0x01, (byte) 0x00, (byte) 0x55, (byte) 0xAA
        };
    }

    public static byte[] solveHandlingPacket(byte packet[]) {
        return new byte[]{
            (byte) 0xAA, (byte) 0x55, (byte) 0x00, (byte) 0x05, (byte) 0xA1, (byte) 0xA8, (byte) 0xD8, (byte) 0x06, (byte) 0x00, (byte) 0x55, (byte) 0xAA
        };
    }

    public static byte[] solveLoginPacket(byte packet[]) {
        int packetLength = ByteArray.getByteArrayLength(packet);

        int accountLength = (int) packet[7];
        String account = PacketResolver.getString(packet, 8, 8 + accountLength - 1);

        int passMD5Length = (int) packet[8 + accountLength];
        String passMD5 = PacketResolver.getString(packet, 8 + accountLength + 1, 8 + accountLength + 1 + passMD5Length - 1);
        String macAddress = PacketResolver.getString(packet, packetLength - 1 - 70, packetLength - 1 - 38 - 1);
        String hardwareId = PacketResolver.getString(packet, packetLength - 1 - 36, packetLength - 1 - 2 - 1);

        /*
            1: Succeeded
            2: Wrong username
            3: Wrong password
            4: Has character that's online
            5: Account is already used
            6: Failed again, try again later
            7: Account blocked
            8: Not enough point
         */
        int res = 1;
        String response = "Succeeded";

        AccountDAO aDAO = new AccountDAO();
        ClientConnectionDAO cDAO = new ClientConnectionDAO();
        Account ac = aDAO.findAccountByUsernameAndPassword(account, passMD5);
        if (ac == null) {
            res = 2;
            response = "Wrong username or password";
        } else if (ac.getLockStatus() != 0) {
            res = 7;
            response = "Account is locked";
        }

        ClientConnection cConnect = cDAO.getClientConnectionByAccount(Billing.getInstance().getConfiguration().getServerId(), account);
        if (res == 1 && cConnect != null && cConnect.getIsOnline() != 0) {
            res = 4;
            response = "Online -> Kick out";
        }

        if (res == 1) {
            if (cDAO.getTotalClientConnectionByMacAddressOrHardwareId(Billing.getInstance().getConfiguration().getServerId(), macAddress, hardwareId) >= Billing.getInstance().getConfiguration().getMaxConnection()) {
                res = 5; //6 - Do not work with Auto Tinh Kiem
                response = "Maximum connections from this Client";
            } else {
                cDAO.deleteClientConnection(Billing.getInstance().getConfiguration().getServerId(), new ClientConnection(macAddress, hardwareId, account));       //Delete old one
                cDAO.addNewClientConnection(Billing.getInstance().getConfiguration().getServerId(), new ClientConnection(macAddress, hardwareId, account));
                /*ClientConnection clientConnection = cDAO.getClientConnectionByHardwareIdAndAccount(hardwareId, account);
                if (clientConnection == null) {
                    System.out.println("======ERROR NEW MAC IS SET TO NULL======");
                }*/
            }
        } else if (res == 4 || res == 7) {
            cDAO.deleteClientConnection(Billing.getInstance().getConfiguration().getServerId(), new ClientConnection(macAddress, hardwareId, account));           //Delete old one
        }
        aDAO.close();
        cDAO.close();

        System.out.println(ResolvePacket.getCurrentTimeStamp() + "\t" + "Login" + "\n\t" + "Account: " + account + "\n\t" + "Response: " + response);
        System.out.println("\t" + "MAC Address = " + macAddress);
        System.out.println("\t" + "Hardware ID = " + hardwareId);

        int val = 0;
        if (res != 1) {
            val = 1;
        }

        byte status = (byte) res;
        byte synBits[] = {packet[5], packet[6]};

        int totalBits = 31 - 20 * val + account.length();

        byte result[] = new byte[totalBits];
        result[0] = (byte) 0xAA;
        result[1] = (byte) 0x55;
        int fullPacketContentLength = 27 - 20 * val + account.length() - 2;
        byte packetLengthByte[] = PacketResolver.getByte(fullPacketContentLength);

        result[2] = packetLengthByte[2];
        result[3] = packetLengthByte[3];
        result[4] = (byte) 0xA2;
        result[5] = synBits[0];
        result[6] = synBits[1];
        result[7] = (byte) account.length();
        for (int i = 8; i < 8 + account.length(); i++) {
            result[i] = (byte) account.charAt(i - 8);
        }
        result[8 + account.length()] = status;
        for (int i = 8 + account.length() + 1; i < totalBits - 2; i++) {
            result[i] = (byte) 0x00;
        }
        result[totalBits - 2] = (byte) 0x55;
        result[totalBits - 1] = (byte) 0xAA;

        return result;
    }

    public static byte[] solveSelectCharacterPacket(byte packet[]) {
        int accountLength = packet[7];
        String account = PacketResolver.getString(packet, 8, 8 + accountLength - 1);

        int charNameLength = packet[8 + accountLength];
        String charName = PacketResolver.getString(packet, 8 + accountLength + 1, 8 + accountLength + 1 + charNameLength - 1);

        System.out.println(ResolvePacket.getCurrentTimeStamp() + "\t" + "Select character" + "\n\t" + "Account: " + account + "\n\t" + "Character: " + charName);

        int status = 1;                   //Failed = 0, Succeeded = 1

        ClientConnectionDAO cDAO = new ClientConnectionDAO();
        ClientConnection client = cDAO.getClientConnectionByAccount(Billing.getInstance().getConfiguration().getServerId(), account);
        if (client != null && client.getIsOnline() != 0) {
            status = 0;
            client = null;
        }
        if (client != null) {
            String macAddress = client.getMacAddress();
            String hardwareId = client.getHardwareId();
            if (cDAO.getTotalClientConnectionByMacAddressOrHardwareId(Billing.getInstance().getConfiguration().getServerId(), macAddress, hardwareId) >= Billing.getInstance().getConfiguration().getMaxConnection()) {
                status = 0;
            } else {
                client.setIsOnline(1);
                boolean result = cDAO.updateClientConnection(Billing.getInstance().getConfiguration().getServerId(), client);
                if (!result) {
                    System.out.println("Update character online status failed.");
                    status = 0;
                }
            }
        }
        cDAO.close();

        byte synBits[] = {packet[5], packet[6]};
        int totalBits = 25 + account.length();

        byte result[] = new byte[totalBits];
        result[0] = (byte) 0xAA;
        result[1] = (byte) 0x55;
        int fullPacketContentLength = 21 + account.length() - 2;
        byte fullPacketLengthByte[] = PacketResolver.getByte(fullPacketContentLength);
        result[2] = fullPacketLengthByte[2];
        result[3] = fullPacketLengthByte[3];
        result[4] = (byte) 0xA3;
        result[5] = synBits[0];
        result[6] = synBits[1];
        result[7] = (byte) account.length();
        for (int i = 8; i < 8 + account.length(); i++) {
            result[i] = (byte) account.charAt(i - 8);
        }
        result[8 + account.length()] = (byte) status;
        result[8 + account.length() + 1] = (byte) 0x00;
        result[8 + account.length() + 2] = (byte) 0x00;
        result[8 + account.length() + 3] = (byte) 0x00;
        result[8 + account.length() + 4] = (byte) 0x27;
        result[8 + account.length() + 5] = (byte) 0x10;
        result[8 + account.length() + 6] = (byte) 0x00;
        result[8 + account.length() + 7] = (byte) 0x00;
        result[8 + account.length() + 8] = (byte) 0x27;
        result[8 + account.length() + 9] = (byte) 0x0F;
        result[8 + account.length() + 10] = (byte) 0x00;
        result[8 + account.length() + 11] = (byte) 0x00;
        result[8 + account.length() + 12] = (byte) 0x22;
        result[8 + account.length() + 13] = (byte) 0xB8;
        result[8 + account.length() + 14] = (byte) 0x00;
        result[8 + account.length() + 15] = (byte) 0x55;
        result[8 + account.length() + 16] = (byte) 0xAA;

        if (status == 0) {
            return null;
        }
        return result;
    }

    public static byte[] solveOutGamePacket(byte packet[]) {
        int accountLength = packet[7];
        String account = PacketResolver.getString(packet, 8, 8 + accountLength - 1);

        System.out.println(ResolvePacket.getCurrentTimeStamp() + "\t" + "Logout" + "\n\t" + "Account: " + account);
        ClientConnectionDAO cDAO = new ClientConnectionDAO();
        ClientConnection client = cDAO.getClientConnectionByAccount(Billing.getInstance().getConfiguration().getServerId(), account);
        if (client != null) {
            cDAO.deleteClientConnection(Billing.getInstance().getConfiguration().getServerId(), client);
        }
        cDAO.close();

        byte synBits[] = {packet[5], packet[6]};
        int totalBits = 11 + account.length();

        byte result[] = new byte[totalBits];
        result[0] = (byte) 0xAA;
        result[1] = (byte) 0x55;
        int fullPacketContentLength = 7 + account.length() - 2;
        byte fullPacketLengthByte[] = PacketResolver.getByte(fullPacketContentLength);
        result[2] = fullPacketLengthByte[2];
        result[3] = fullPacketLengthByte[3];
        result[4] = (byte) 0xA4;
        result[5] = synBits[0];
        result[6] = synBits[1];
        result[7] = (byte) account.length();
        for (int i = 8; i < 8 + account.length(); i++) {
            result[i] = (byte) account.charAt(i - 8);
        }
        result[8 + account.length()] = (byte) 0x00;
        result[8 + account.length() + 1] = (byte) 0x55;
        result[8 + account.length() + 2] = (byte) 0xAA;

        return result;
    }

    public static byte[] solveAskPointPacket(byte packet[]) {
        int accountLength = packet[7];

        String account = PacketResolver.getString(packet, 8, 8 + accountLength - 1);

        byte synBits[] = {packet[5], packet[6]};
        int totalBits = 14 + account.length();

        byte result[] = new byte[totalBits];
        result[0] = (byte) 0xAA;
        result[1] = (byte) 0x55;
        int fullPacketContentLength = 10 + account.length() - 2;
        byte packetContentLengthByte[] = PacketResolver.getByte(fullPacketContentLength);
        result[2] = packetContentLengthByte[2];
        result[3] = packetContentLengthByte[3];
        result[4] = (byte) 0xE2;
        result[5] = synBits[0];
        result[6] = synBits[1];
        result[7] = (byte) account.length();
        for (int i = 8; i < 8 + account.length(); i++) {
            result[i] = (byte) account.charAt(i - 8);
        }

        //Number of point = 4 hexa numbers XX XX XX XX
        AccountDAO aDAO = new AccountDAO();
        Account a = aDAO.findAccountByUsername(account);
        aDAO.close();

        if (a == null) {
            result[8 + account.length()] = (byte) 0x00;
            result[8 + account.length() + 1] = (byte) 0x00;
            result[8 + account.length() + 2] = (byte) 0x00;
            result[8 + account.length() + 3] = (byte) 0x00;
        } else {
            byte hexByte[] = PacketResolver.getByte(a.getPoint());
            result[8 + account.length()] = hexByte[0];
            result[8 + account.length() + 1] = hexByte[1];
            result[8 + account.length() + 2] = hexByte[2];
            result[8 + account.length() + 3] = hexByte[3];

            System.out.println(ResolvePacket.getCurrentTimeStamp() + "\t" + "Check point" + "\n\t" + "Account: " + account + "\n\t" + "Point: " + a.getPoint());
        }
        //End

        result[8 + account.length() + 4] = (byte) 0x55;
        result[8 + account.length() + 5] = (byte) 0xAA;

        return result;
    }

    public static byte[] solveExchangePointPacket(byte packet[]) {
        int nLength = ByteArray.getByteArrayLength(packet);

        int accountLength = packet[7];
        String account = PacketResolver.getString(packet, 8, 8 + accountLength - 1);
        int productValue = PacketResolver.getInt(packet, nLength - 1 - 7, nLength - 1 - 6);
        int costValue = PacketResolver.getInt(packet, nLength - 1 - 5, nLength - 1 - 2);

        byte errorBit = (byte) 0x00;
        AccountDAO aDAO = new AccountDAO();
        Account acc = aDAO.findAccountByUsername(account);
        int totalPointsLeft = 0;
        if (acc != null) {
            int pre = acc.getPoint();
            int nPoint = pre - costValue;

            if (nPoint >= 0) {
                acc.setPoint(nPoint);
                aDAO.updateAccount(acc);

                totalPointsLeft = acc.getPoint();
                System.out.println("Exchange point" + "\n\t" + "Account: " + account + "\n\t" + "Point: " + pre + "\n\t" + "Exchanged point: " + costValue + "\n\t" + "Point left: " + totalPointsLeft);
            } else {
                errorBit = (byte) 0x02;
                productValue = 0;
                System.out.println("Exchange point" + "\n\t" + "Account: " + account + "\n\t" + "Point: " + pre + "\n\t" + "Exchanged point: " + costValue + "\n\t" + "NOT ENOUGH POINT => Exchange failed");
            }
        }
        aDAO.close();

        byte tokenHexByte[] = PacketResolver.getByte(productValue);
        byte pointHexByte[] = PacketResolver.getByte(totalPointsLeft);

        byte synBits[] = {packet[5], packet[6]};
        int totalBits = 44 + account.length();

        byte result[] = new byte[totalBits];
        result[0] = (byte) 0xAA;
        result[1] = (byte) 0x55;
        int fullPacketContentLength = 40 + account.length() - 2;
        byte fullPacketLengthByte[] = PacketResolver.getByte(fullPacketContentLength);
        result[2] = fullPacketLengthByte[2];
        result[3] = fullPacketLengthByte[3];
        result[4] = (byte) 0xE1;
        result[5] = synBits[0];
        result[6] = synBits[1];
        result[7] = (byte) account.length();
        for (int i = 8; i < 8 + account.length(); i++) {
            result[i] = (byte) account.charAt(i - 8);
        }

        //Synchronize bits
        /*
          21 bits synchronize key with input received
         */
        for (int i = 0; i < 21; i++) {
            result[8 + account.length() + i] = packet[nLength - 34 + i];
        }
        //End

        //Error bit
        /*
            00 -> Succeeded => Call function BuyRet
            02 -> Not enough point
         */
        result[8 + account.length() + 21] = errorBit;
        //End

        //Info bit
        /*
          4 bits points number after exchanging
         */
        result[8 + account.length() + 22] = pointHexByte[0];
        result[8 + account.length() + 23] = pointHexByte[1];
        result[8 + account.length() + 24] = pointHexByte[2];
        result[8 + account.length() + 25] = pointHexByte[3];

        /*
          2 bits exchanged time -> default to 1
         */
        result[8 + account.length() + 26] = (byte) 0x00;
        result[8 + account.length() + 27] = (byte) 0x01;

        /*
          4 bits request type -> Similar to input received
         */
        result[8 + account.length() + 28] = packet[nLength - 11];
        result[8 + account.length() + 29] = packet[nLength - 10];
        result[8 + account.length() + 30] = packet[nLength - 9];
        result[8 + account.length() + 31] = packet[nLength - 8];

        /*
          2 bits number of tokens added to game
         */
        result[8 + account.length() + 32] = tokenHexByte[2];
        result[8 + account.length() + 33] = tokenHexByte[3];
        //End

        result[totalBits - 2] = (byte) 0x55;
        result[totalBits - 1] = (byte) 0xAA;

        return result;
    }

    public static byte[] solveActivateCodePacket(byte packet[]) {
        int giftCodeLength = packet[7];
        String code = PacketResolver.getString(packet, 8, 8 + giftCodeLength - 1);
        //System.out.println("Code = " + code);

        int accountLength = packet[8 + giftCodeLength];
        String account = PacketResolver.getString(packet, 9 + giftCodeLength, 9 + giftCodeLength + accountLength - 1);
        //System.out.println("Account = " + account);

        int ipLength = packet[9 + giftCodeLength + accountLength];
        String ip = PacketResolver.getString(packet, 10 + giftCodeLength + accountLength, 10 + giftCodeLength + accountLength + ipLength - 1);
        //System.out.println("IP = " + ip);

        int charLength = packet[10 + giftCodeLength + accountLength + ipLength];
        String charName = PacketResolver.getString(packet, 11 + giftCodeLength + accountLength + ipLength, 11 + giftCodeLength + accountLength + ipLength + charLength - 1);
        //System.out.println("Char name = " + charName);

        // TODO check database
        int isOK = 0;
        int resultBit = 0;
        GiftCodeDAO gDAO = new GiftCodeDAO();
        GiftCode gc = gDAO.findGiftCode(code);
        String response = "Succeeded";
        if (gc != null && gc.getAcc_code() != null && gc.getAcc_code().compareTo(charName) == 0) {
            resultBit = 2;
            response = "That character was just received that code";
            isOK = 1;
        } else if (gc == null || gc.getStatus() != 1 || gc.getValidateDate().before(new Date()) || gc.getStartDate().after(new Date())) {
            resultBit = 1;
            response = "Not in time of giftcode";
            isOK = 1;
        } else if ((gc.getAcc_code() != null && !gc.getAcc_code().isEmpty())) {
            resultBit = 9;
            response = "Giftcode was used by another character";
            isOK = 1;
        }
        gDAO.close();

        if (isOK == 1) {
            System.out.println(ResolvePacket.getCurrentTimeStamp() + "\t" + "Check giftcode" + "\n\t" + "Account: " + account + "\n\t" + "Character: " + charName + "\n\t" + "Code: " + code + "\n\t" + "Response: " + response);
        }

        byte synBits[] = {packet[5], packet[6]};
        int totalBits = 33 + account.length() - 22 * isOK;

        byte result[] = new byte[totalBits];
        result[0] = (byte) 0xAA;
        result[1] = (byte) 0x55;
        int fullPacketContentLength = 29 + account.length() - 2 - 22 * isOK;
        byte[] fullPacketLengthByte = PacketResolver.getByte(fullPacketContentLength);
        result[2] = fullPacketLengthByte[2];
        result[3] = fullPacketLengthByte[3];
        result[4] = (byte) 0xC6;
        result[5] = synBits[0];
        result[6] = synBits[1];
        result[7] = (byte) account.length();
        for (int i = 8; i < 8 + account.length(); i++) {
            result[i] = (byte) account.charAt(i - 8);
        }
        result[8 + account.length()] = (byte) resultBit;                        //Result failed = 1, succeeded = 0, 2 = Account has received that gift.
        /*
        
         */
        if (isOK == 0) {
            result[8 + account.length() + 1] = (byte) 0x01;
            result[8 + account.length() + 2] = (byte) 0x33;
            result[8 + account.length() + 3] = (byte) 0x30;
            result[8 + account.length() + 4] = (byte) 0x33;
            result[8 + account.length() + 5] = (byte) 0x30;
            result[8 + account.length() + 6] = (byte) 0x39;
            result[8 + account.length() + 7] = (byte) 0x30;
            result[8 + account.length() + 8] = (byte) 0x35;
            result[8 + account.length() + 9] = (byte) 0x32;

            result[8 + account.length() + 10] = (byte) 0x00;
            result[8 + account.length() + 11] = (byte) 0x01;
            result[8 + account.length() + 12] = (byte) 0x03;
            result[8 + account.length() + 13] = (byte) 0x04;
            result[8 + account.length() + 14] = (byte) 0x05;
            result[8 + account.length() + 15] = (byte) 0x06;
            result[8 + account.length() + 16] = (byte) 0x07;
            result[8 + account.length() + 17] = (byte) 0x08;
            result[8 + account.length() + 18] = (byte) 0x09;
            result[8 + account.length() + 19] = (byte) 0x01;
            result[8 + account.length() + 20] = (byte) 0x02;
            result[8 + account.length() + 21] = (byte) 0x03;
            result[8 + account.length() + 22] = (byte) 0x01;
        }
        //End

        result[totalBits - 2] = (byte) 0x55;
        result[totalBits - 1] = (byte) 0xAA;

        return result;
    }

    //A6
    public static byte[] solveCheckCharacterOnlinePacket(byte packet[]) {
        return null;
    }

    public static byte[] solveGetCodeRewardPacket(byte packet[]) {
        int giftCodeLength = packet[7];
        String code = PacketResolver.getString(packet, 8, 8 + giftCodeLength - 1);
        //System.out.println("Code = " + code);

        int accountLength = packet[8 + giftCodeLength];
        String account = PacketResolver.getString(packet, 9 + giftCodeLength, 9 + giftCodeLength + accountLength - 1);
        //System.out.println("Account = " + account);

        int ipLength = packet[9 + giftCodeLength + accountLength];
        String ip = PacketResolver.getString(packet, 10 + giftCodeLength + accountLength, 10 + giftCodeLength + accountLength + ipLength - 1);
        //System.out.println("IP = " + ip);

        int charLength = packet[10 + giftCodeLength + accountLength + ipLength];
        String charName = PacketResolver.getString(packet, 11 + giftCodeLength + accountLength + ipLength, 11 + giftCodeLength + accountLength + ipLength + charLength - 1);
        //System.out.println("Char name = " + charName);

        // TODO check database
        int isOK = 0;
        byte resultBit = (byte) 0x00;
        GiftCodeDAO gDAO = new GiftCodeDAO();
        GiftCode gc = gDAO.findGiftCode(code);
        String response = "Succeeded";
        if (gc != null && gc.getAcc_code() != null && gc.getAcc_code().compareTo(charName) == 0) {
            resultBit = (byte) 0x02;
            response = "That character was just received that code";
            isOK = 1;
        } else if (gc == null || gc.getStatus() != 1 || gc.getValidateDate().before(new Date()) || gc.getStartDate().after(new Date())) {
            resultBit = (byte) 0x01;
            response = "Not in time of giftcode";
            isOK = 1;
        } else if ((gc.getAcc_code() != null && !gc.getAcc_code().isEmpty())) {
            resultBit = (byte) 0x09;
            response = "Giftcode was used by another character";
            isOK = 1;
        }

        if (isOK == 1) {
            System.out.println(ResolvePacket.getCurrentTimeStamp() + "\t" + "Check giftcode" + "\n\t" + "Account: " + account + "\n\t" + "Character: " + charName + "\n\t" + "Code: " + code + "\n\t" + "Response: " + response);
        }

        byte giftCodeNum = (byte) 0x00;
        if (gc != null) {
            gc.setAcc_code(charName);
            gc.setIp(ip);
            gDAO.updateGiftCode(gc);
            giftCodeNum = (byte) gc.getType();
        }
        gDAO.close();

        byte synBits[] = {packet[5], packet[6]};
        int totalBits = 33 + account.length() - 22 * isOK;

        byte result[] = new byte[totalBits];
        result[0] = (byte) 0xAA;
        result[1] = (byte) 0x55;
        int fullPacketContentLength = 29 + account.length() - 2 - 22 * isOK;
        byte[] fullPacketLengthByte = PacketResolver.getByte(fullPacketContentLength);
        result[2] = fullPacketLengthByte[2];
        result[3] = fullPacketLengthByte[3];
        result[4] = (byte) 0xC1;
        result[5] = synBits[0];
        result[6] = synBits[1];
        result[7] = (byte) account.length();
        for (int i = 8; i < 8 + account.length(); i++) {
            result[i] = (byte) account.charAt(i - 8);
        }
        result[8 + account.length()] = resultBit;                               //Result failed = 1, succeeded = 0, 2 = Account has received that gift.

        if (isOK == 0) {
            result[8 + account.length() + 1] = (byte) 0x01;
            result[8 + account.length() + 2] = (byte) 0x33;
            result[8 + account.length() + 3] = (byte) 0x30;
            result[8 + account.length() + 4] = (byte) 0x33;
            result[8 + account.length() + 5] = (byte) 0x30;
            result[8 + account.length() + 6] = (byte) 0x39;
            result[8 + account.length() + 7] = (byte) 0x30;
            result[8 + account.length() + 8] = (byte) 0x35;
            result[8 + account.length() + 9] = (byte) 0x32;

            result[8 + account.length() + 10] = (byte) 0x00;
            result[8 + account.length() + 11] = (byte) 0x01;
            result[8 + account.length() + 12] = (byte) 0x03;
            result[8 + account.length() + 13] = (byte) 0x04;
            result[8 + account.length() + 14] = (byte) 0x05;
            result[8 + account.length() + 15] = (byte) 0x06;
            result[8 + account.length() + 16] = (byte) 0x07;
            result[8 + account.length() + 17] = (byte) 0x08;
            result[8 + account.length() + 18] = (byte) 0x09;
            result[8 + account.length() + 19] = (byte) 0x01;
            result[8 + account.length() + 20] = (byte) 0x02;
            result[8 + account.length() + 21] = (byte) 0x03;
            result[8 + account.length() + 22] = giftCodeNum;                    //Catch number in LUA -> corresponding with 256 types of giftcode
        }
        //End

        result[totalBits - 2] = (byte) 0x55;
        result[totalBits - 1] = (byte) 0xAA;

        return result;
    }
    /*End*/

}
