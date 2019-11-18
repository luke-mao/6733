package com.example.a6733.functions;

import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class encryption {
    String keystring;
    String message;

    public encryption(String keystring, String message){
        this.keystring = keystring;
        this.message = message;
    }

    public byte[] encrypt(){
        String ALGORITHM = "AES";
        byte[] result = new byte[]{};

        /*so that this part encrypts the message*/
        if (keystring.length() != 16){
            Log.d("Encryption error", "key string is not 16");
        }
        else{
            try{
                // get the key in string
                byte[] rawkey = keystring.getBytes();
                // get the real secretkey
                SecretKeySpec key = new SecretKeySpec(rawkey, ALGORITHM);

                Cipher cipher = Cipher.getInstance(ALGORITHM);

                cipher.init(Cipher.ENCRYPT_MODE, key);

                cipher.update(message.getBytes());

                result = cipher.doFinal();


            }
            catch (Exception e){
                Log.d("Encryption error", "error in try and catch");
                e.printStackTrace();
            }
        }
        return result;
    }
}
