package br.com.royalfarma.utils;

import android.os.SystemClock;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Util {
    public final static String CONNECTION = "CONNECTION";
    public final static String CONFIG_FILE = "CONFIG_FILE";
    public final static String MY_LOG_TAG = "VAPSTOR";
    public static long elapsedTime = 0;

    public static void confirmQuitApp(AppCompatActivity activity) {
        if (SystemClock.elapsedRealtime() - elapsedTime < 1500) {
            activity.finishAffinity();
        } else {
            Toast.makeText(activity, "Toque novamente para sair", Toast.LENGTH_SHORT).show();
        }
        elapsedTime = SystemClock.elapsedRealtime();
    }

    public static String getSha512FromString(String string) {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
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
