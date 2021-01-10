package com.cidtepole.tcpclientbridge.data;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.cidtepole.tcpclientbridge.R;
import com.cidtepole.tcpclientbridge.model.Server;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class Tools {

    /// <summary> Converts an array of bytes into a formatted string of hex digits (ex: E4 CA B2)</summary>
    /// <param name="data"> The array of bytes to be translated into a string of hex digits. </param>
    /// <returns> Returns a well formatted string of hex digits with spacing. </returns>
    public static  String ByteArrayToHexString(byte[] data)
    {
        StringBuilder sb = new StringBuilder(data.length * 3);

        for ( int i = 0; i < data.length; i++)
            sb.append(Integer.toHexString(data[i]).concat("  "));
        return sb.toString().toUpperCase();
    }

    private static float getAPIVerison() {

       Float f = null;

        try {
            StringBuilder strBuild = new StringBuilder();
            strBuild.append(android.os.Build.VERSION.RELEASE.substring(0, 1));
            f = new Float(strBuild.toString());
        } catch (NumberFormatException e) {
            Log.e("", "erro ao recuperar a versão da API" + e.getMessage());
        }



        return f.floatValue();


    }


    public static  byte[] decodeHexString(String hexString) {
        if (hexString.length() % 2 == 1) {
            throw new IllegalArgumentException(
                    "Invalid hexadecimal String supplied.");
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
        }
        return bytes;
    }

    public static byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    private static int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if(digit == -1) {
            throw new IllegalArgumentException(
                    "Invalid Hexadecimal Character: "+ hexChar);
        }
        return digit;
    }







    public static void systemBarLolipop(Activity act){
        if (getAPIVerison() >= 5.0) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public static int getGridSpanCount(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth  = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.recycler_item_size);
        return Math.round(screenWidth / cellWidth);
    }


    public static boolean checkServerList(ArrayList<Server> servers, Server server){


        for(Server server_l: servers){
            if (server_l.equals(server)) {
                return true;
            }
        }

        return false;
    }

}
