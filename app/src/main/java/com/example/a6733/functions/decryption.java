package com.example.a6733.functions;

import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class decryption {

    String keystring;
    byte[] message;

    public decryption(String keystring, byte[] message){
        this.keystring = keystring;
        this.message = message;
    }

    public String decrypt(){

        String ALGORITHM = "AES";
        String final_result = "";

        /*so that this part encrypts the message*/
        if (keystring.length() != 16){
            Log.d("Decryption error", "key string is not 16");
        }
        else{
            try{

                // get the key in string
                byte[] rawkey = keystring.getBytes();
                // get the real secretkey
                SecretKeySpec key = new SecretKeySpec(rawkey, ALGORITHM);

                Cipher cipher = Cipher.getInstance(ALGORITHM);

                cipher.init(Cipher.DECRYPT_MODE, key);

                cipher.update(message);

                byte[] result = cipher.doFinal();

                final_result = new String(result);

            }
            catch (Exception e){
                Log.d("Encryption error", "error in try and catch");
                e.printStackTrace();
            }
        }
        return final_result;
    }

}
