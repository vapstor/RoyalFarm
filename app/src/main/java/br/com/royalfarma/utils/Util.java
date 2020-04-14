package br.com.royalfarma.utils;

import android.os.SystemClock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

public abstract class Util {
    public final static String CONNECTION = "CONNECTION";
    public final static String CONFIG_FILE = "CONFIG_FILE";
    public final static String MY_LOG_TAG = "VAPSTOR";
    public final static String DB_PDT = "ws_products";

    public static long elapsedTime = 0;


    private static DecimalFormat DINHEIRO_REAL;

    public static String RSmask(double valor) {
        BigDecimal bigDecimal = new BigDecimal(valor).setScale(2, BigDecimal.ROUND_HALF_UP);
//        DINHEIRO_REAL = new BigDecimal("R$ ###,###,##0,00");
        return "R$ " + bigDecimal.toString().replace(".", ",");
    }

    public static String RSmask(BigDecimal bd) {
//        String valor = bd.toString();
        return "R$ " + bd.setScale(2, RoundingMode.UP).toString().replace(".", ",");
//        DINHEIRO_REAL = new DecimalFormat("¤ ###,###,##0.00");
//        return DINHEIRO_REAL.format(bd);
    }


    public static boolean confirmQuitApp() {
        if (SystemClock.elapsedRealtime() - elapsedTime < 1500) {
            elapsedTime = SystemClock.elapsedRealtime();
            return true;
        } else {
            elapsedTime = SystemClock.elapsedRealtime();
            return false;
        }
    }


    public static String getSha512FromString(String string) {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called from here
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(string.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
