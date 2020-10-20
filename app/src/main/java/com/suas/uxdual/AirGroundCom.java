package com.suas.uxdual;

import android.util.Log;

import java.nio.ByteBuffer;

public class AirGroundCom {
    public static final byte MaxMessageLength = 25;
    public static final byte MaxGPSMessageLength = 50;
    public static final byte GIMMODE_CHANNEL = 4;
    public static final byte TILT_CHANNEL = 5;
    public static final byte PAN_CHANNEL = 6;
    public static final byte FLIR4_CHANNEL = 7;
    public static final byte ZOOM_CHANNEL = 8;
    public static final byte REC_CHANNEL = 9;
    public static final byte PALETTE_CHANNEL = 10;
    public static final byte IR2D_CHANNEL = 11;
    public static final byte BATTERY_MESSAGE_CHANNEL = 12;
    public static final byte GPS_MESSAGE_CHANNEL = 13;
    public static final byte SDCARD_MESSAGE_CHANNEL = 14;
    public static final byte MAVLINK_CHANNEL = 15;
    public static final byte GPS_GLOBAL_POSITION_INFORMATION_CHANNEL = 16;
    public static final byte MAPPING_MODE_CHANNEL = 50;
    public static final byte UNIX_TIME_CHANNEL = 51;
    public static final byte THERMAL_GAINMODE_CHANNEL = 31;

    public static final boolean FAILED = false;
    public static final boolean PASSED = true;

    private static String vstring(final int value) {
        if (value < 10) return "00" + value;
        else if (value < 100) return "0" + value;
        else return "" + value;
    }

    static void sendG2Amessage(int value, int channel) {
        // BIG DIFFERENCE! in Java (Android), byte is signed 8-bit and ranges from -128 to 127)
        // In Arduino C++, byte (or uint8_t) is unsigned 8 bit, ranges from 0 to 255. Arduino's equivalence of Java's byte is int8_t, a signed variable type
        int[] message = new int[MaxMessageLength];
        int checksum;
        int i, messagelength, totallength;
        int sum;
        message[0] = 0x7E; //Start byte
        message[1] = 0x00; //Byte 1&2 to tell length of message, starting from byte 3 till end of message.
        message[2] = 0x0B; //0x000B (hexadecimal) = 11 (decimal, meaning 11 bytes total after byte 2.
        messagelength = intValue(message[1], message[2]);

        totallength = messagelength + 3;
        Log.d("totallength", "sendG2Amessage: totallength" + totallength);
        if (totallength > MaxMessageLength) return;

        message[3] = 0x01; //Frame type. 0x01 means typical gimbal command
        for (i = 4; i <= totallength - 5; i++) {
            message[i] = 0x00;
        }
        message[totallength - 4] = channel;
        //message[totallength - 3] = ((value >> 8) & 0xff);
        //message[totallength - 2] = (value & 0xff);
        int lobyte, hibyte;
        if (value > 127) {
            hibyte = ((value - 127) & 0xff);
            lobyte = 127 & 0xff;
        } else {
            hibyte = 0;
            lobyte = (value & 0xff);
        }
        message[totallength - 3] = hibyte;
        message[totallength - 2] = lobyte;

        sum = 0;
        for (i = 3; i <= totallength - 2; i++) {
            sum += message[i];
        }
        Log.d("sum", "sendG2Amessage: sum = " + sum);
        checksum = (0xff - (sum & 0xff));
        /*for (i = 0; i <= totallength; i++) {
            ////Serial.write(message[i]);
            ////Serial.print(message[i],HEX);
            ////Serial.print(", ");
            //B2BSerial.write(message[i]);
            //messageString += message[0];
            char c = (char) message[i];
            Log.d("message", "sendG2Amessage: message[" + i + "] = " + message[i] + " char = " + c);
        }*/

        if (checksum > 127) {
            hibyte = ((checksum - 127) & 0xff);
            lobyte = 127 & 0xff;
        } else {
            hibyte = 0;
            lobyte = (checksum & 0xff);
        }
        message[totallength - 1] = hibyte;
        message[totallength] = lobyte;
        String messageString = new String(message, 0, totallength + 1);

        Log.d("message", "sendG2Amessage: hiByte = " + hibyte + " lobyte = " + lobyte);
        /*try {
            ServiceBase.getServiceBase().getVideoService().SendG2AMessage1(messageString);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
        //CompleteWidgetActivity.mTcpClient.sendMessage(messageString);
        //new MainActivity.SendMessageTask().execute(messageString);
        Log.d("sendG2Amessage ", "sendG2Amessage: " + messageString);
    }

    static void sendGPSmessage(int sats, float alti, double lat, double lon, int channel) {
        // BIG DIFFERENCE! in Java (Android), byte is signed 8-bit and ranges from -128 to 127)
        // In Arduino C++, byte (or uint8_t) is unsigned 8 bit, ranges from 0 to 255. Arduino's equivalence of Java's byte is int8_t, a signed variable type
        int[] message = new int[MaxGPSMessageLength];
        int checksum;
        int i, messagelength, totallength;
        int sum;
        message[0] = 0x7E; //Start byte
        message[1] = 0x00; //Byte 1&2 to tell length of message, starting from byte 3 till end of message.
        message[2] = 0x19; //0x0019 (hexadecimal) = 25 (decimal, meaning 25 bytes total after byte 2.
        messagelength = intValue(message[1], message[2]);

        totallength = messagelength + 3;
        Log.d("totallength", "sendG2Amessage: totallength" + totallength);
        if (totallength > MaxGPSMessageLength) return;

        message[3] = 0x02; //Frame type. 0x01 means typical gimbal command, 0x02 is GPS information
        message[4] = (sats & 0xff);

        byte[] abyte = new byte[4];
        byte[] csumbyte = new byte[2];
        byte[] latbyte = new byte[8];
        byte[] lonbyte = new byte[8];

        ByteBuffer.wrap(abyte).putFloat(alti);
        ByteBuffer.wrap(latbyte).putDouble(lat);
        ByteBuffer.wrap(lonbyte).putDouble(lon);

        int ii;
        for(ii = 0; ii < 4; ii ++){
            message[5 + ii] = abyte[ii];
        }
        for(ii = 0; ii < 8; ii ++){
            message[9 + ii] = latbyte[ii];
            message[17 + ii] = lonbyte[ii];
        }

        message[25] = channel;
        sum = 0;
        for (i = 3; i <= totallength - 2; i++) {
            sum += message[i];
        }

        Log.d("sum", "sendG2Amessage: sum = " + sum);
        checksum = (0xff - (sum & 0xff));

        int lobyte, hibyte;

        if (checksum > 127) {
            hibyte = ((checksum - 127) & 0xff);
            lobyte = 127 & 0xff;
        } else {
            hibyte = 0;
            lobyte = (checksum & 0xff);
        }
        message[totallength - 1] = hibyte;
        message[totallength] = lobyte;
        String messageString = new String(message, 0, totallength + 1);

        Log.d("message", "sendG2Amessage: hiByte = " + hibyte + " lobyte = " + lobyte);
        /*try {
            ServiceBase.getServiceBase().getVideoService().SendG2AMessage1(messageString);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
        //CompleteWidgetActivity.mTcpClient.sendMessage(messageString);

        Log.d("sendG2Amessage ", "sendG2Amessage: " + messageString);
    }

    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static byte[] toByteArray(float value) {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putFloat(value);
        return bytes;
    }
    public static byte[] toByteArray(long value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(value);
        return bytes;
    }
    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static String packageG2AMessage(int value, int channel) {
        // BIG DIFFERENCE! in Java (Android), byte is signed 8-bit and ranges from -128 to 127)
        // In Arduino C++, byte (or uint8_t) is unsigned 8 bit, ranges from 0 to 255. Arduino's equivalence of Java's byte is int8_t, a signed variable type
        int[] message = new int[MaxMessageLength];
        int checksum;
        int i, messagelength, totallength;
        int sum;
        message[0] = 0x7E; //Start byte
        message[1] = 0x00; //Byte 1&2 to tell length of message, starting from byte 3 till end of message.
        message[2] = 0x0B; //0x000B (hexadecimal) = 11 (decimal, meaning 11 bytes total after byte 2.
        messagelength = intValue(message[1], message[2]);

        totallength = messagelength + 3;
        Log.d("totallength", "sendG2Amessage: totallength" + totallength);
        if (totallength > MaxMessageLength) return "";
        ////Serial.print(" ");
        ////Serial.println(totallength);
        ////Serial.println(" ");

        message[3] = 0x01; //Frame type. 0x01 means typical gimbal command
        for (i = 4; i <= totallength - 5; i++) {
            message[i] = 0x00;
        }
        message[totallength - 4] = channel;

        //TODO here and in Arduino end: make it work so can sent values > 127.
        // Right now, all values sent here are <= 100 so it work. With this Java android code, for
        // 127 < value < 256, we can do the trick as in the checksum below: byte0 = 127, byte1 = value - 127
        // when this two bytes are received, just do a simple addition to reconstruct the value: value = byte0 + byte1
        // but for value > 255, this is an issue. Java android will do some weird thing that messes up the
        // reconstruction if doing the byteshift as below.
        message[totallength - 3] = ((value >> 8) & 0xff);
        message[totallength - 2] = (value & 0xff);

        sum = 0;
        for (i = 3; i <= totallength - 2; i++) {
            sum += message[i];
        }
        Log.d("sum", "sendG2Amessage: sum = " + sum);
        checksum = (0xff - (sum & 0xff));
        /*for (i = 0; i <= totallength; i++) {
            ////Serial.write(message[i]);
            ////Serial.print(message[i],HEX);
            ////Serial.print(", ");
            //B2BSerial.write(message[i]);
            //messageString += message[0];
            //char c = (char) message[i];
            //Log.d("message", "sendG2Amessage: message[" + i + "] = " + message[i] + " char = " + c);
        }*/

        int lobyte, hibyte;

        if (checksum > 127) {
            hibyte = ((checksum - 127) & 0xff);
            lobyte = 127 & 0xff;
        } else {
            hibyte = 0;
            lobyte = (checksum & 0xff);
        }
        message[totallength - 1] = hibyte;
        message[totallength] = lobyte;
        String messageString = new String(message, 0, totallength + 1);
        Log.d("sendG2Amessage ", "sendG2Amessage: " + messageString);
        return messageString;
    }

    static int intValue(int Hibyte, int Lobyte) {
        //int Value = Hibyte;
        //Value <<= 8;
        //Value |= Lobyte;
        return (short) (((Hibyte) & 0xFF) << 8 | (Lobyte) & 0xFF);
        //return Value;
    }

    public static void readA2GMessage(String readmsgin, boolean[] readstatus, int[] signalValueout, int[] PINOUT) {
        //ByteBuffer bf = ByteBuffer.wrap(readmsg.getBytes());
        //TODO Thermal edit this after this work
        String readmsg = "";//MainActivity.MessageReceivedFromGimmera;
        int index = readmsg.indexOf(0x7E);
        int length = readmsg.length();
        //Log.i(TAG, "readA2GMessage: index = " + index + " message = " + readmsg + " length = "+ length);
        if (index < 0) {
            readstatus[0] = FAILED;
            return;
        }
        int[] message = new int[MaxMessageLength];
        int checksum, checksum_cal;
        int i, messagelength, totallength, signalValue;
        int sum;

        message[0] = readmsg.charAt(index);
        message[1] = readmsg.charAt(index + 1);
        message[2] = readmsg.charAt(index + 2);
        messagelength = intValue(message[1], message[2]);
        totallength = messagelength + 3;

        //Log.i(TAG, "readA2GMessage: messagelength = " + messagelength + " totallength = " + totallength);

        if (length < 14) {
            readstatus[0] = FAILED;
            return;
        }
        if ((totallength > MaxMessageLength) || (totallength < 4)) {
            //Wrong reading, not good to proceed further
            readstatus[0] = FAILED;
            return;
        } else if (totallength >= 4) {

            for (i = 3; i <= totallength; i++) {
                message[i] = readmsg.charAt(index + i);
            }
            sum = 0;
            for (i = 3; i <= totallength - 2; i++) {
                sum += message[i];
            }
            checksum_cal = 0xFF - (sum & 0xFF);
            int checksum_read = message[totallength - 1] + message[totallength];
            if ((checksum_cal == checksum_read) && (message[0] == 0x7E)) {
                //Serial.println(message[3], HEX);
                //Log.i(TAG, "readA2GMessage: checksum_cal = " + checksum_cal);
                if (message[3] == 0x01) {
                    signalValue = intValue(message[totallength - 3], message[totallength - 2]);
                    if (Math.abs(signalValue) > 127) {
                        readstatus[0] = FAILED;
                        return;
                    }
                    //Log.i(TAG, "readA2GMessage: Signal Value = " + signalValue);
                    readstatus[0] = PASSED;
                    signalValueout[0] = signalValue;
                    PINOUT[0] = message[totallength - 4];
                } else if (message[3] == 0x02) {
        /*byteToINT_32(message + 4, &(MAV_GPS.lat));
          byteToINT_32(message + 8, &(MAV_GPS.lon));
          byteToINT_32(message + 12, &(MAV_GPS.alt));
          byteToINT_32(message + 16, &(MAV_GPS.relative_alt));
          byteToINT_16(message + 20, &(MAV_GPS.vx));
          byteToINT_16(message + 22, &(MAV_GPS.vy));
          byteToINT_16(message + 24, &(MAV_GPS.vz));
          byteToUINT_16(message + 26, &(MAV_GPS.hdg));
          PINOUT = message[28];
          readstatus = PASSED;
          signalValueout = totallength;
          HIL_GPS.lat = MAV_GPS.lat;
          HIL_GPS.lon = MAV_GPS.lon;
          HIL_GPS.alt = MAV_GPS.alt;
          HIL_GPS.eph = MAV_GPS.lat % 1000; // record the 4th, 5th, 6th and 7th decimal places of Latitude
          HIL_GPS.epv = MAV_GPS.lon % 1000; // record the 4th, 5th, 6th and 7th decimal places of Longitude*/
                } else if (message[3] == 0x03) {
        /*byteToUINT_32(message + 4, &UNIX_TIME);
          PINOUT = message[8];
          readstatus = PASSED;
          signalValueout = totallength;
          //Serial.print("UNIX Time = "); Serial.println(UNIX_TIME);*/
                }
            } else {
                readstatus[0] = FAILED;
            }
        }
    }
}

