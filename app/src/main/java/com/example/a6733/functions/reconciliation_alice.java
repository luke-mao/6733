package com.example.a6733.functions;

import android.util.Base64;
import android.util.Log;

import com.example.a6733.functions.reconciliation_function;
import java.util.Arrays;

public class reconciliation_alice{

    /* CLIENT is the alice, send the message first*/
    /* CLIENT is the alice, send the message first*/
    /* CLIENT is the alice, send the message first*/

    /*use key from one direction only*/

    /*this is the alice part, alice needs to receive two messages from bob
     * First message is L_bar, second message is the MAC encryption
     * In addition, other inputs are alice key three axis, alice window index three axis*/

    String first_message;
    byte[] second_message;
    int[] L_x;
    int[] key_x;
    int[] L_y;
    int[] key_y;
    int[] L_z;
    int[] key_z;
    int[] L_intersection_x;
    int[] L_intersection_y;
    int[] L_intersection_z;
    int[] key;

    public reconciliation_alice(byte[] first_message,
                                byte[] second_message,
                                int[] L_x, int[] key_x,
                                int[] L_y, int[] key_y,
                                int[] L_z, int[] key_z){

        this.first_message = new String(first_message);
        this.second_message = second_message;
        this.L_x = L_x;
        this.key_x = key_x;
        this.L_y = L_y;
        this.key_y = key_y;
        this.L_z = L_z;
        this.key_z = key_z;
    }


    public boolean decision(){

        /*first compute the intersection of keys
         * need to separate the strings first*/
        String message = reconciliation_function.split_acc_strings(first_message);
        L_intersection_z = reconciliation_function.string_to_int_array(message);

        //now find the alice total key
        key = reconciliation_function.intersection_keys_one_direction(key_z, L_z, L_intersection_z);

        int[] window = L_intersection_z;

        // get the MAC byte[]
        byte[] alice_mac = reconciliation_function.MACencrypt(key,window);

        //check the byte array length of alice_mac, and copy the exact length from the second_message
        // to avoid the padding
        byte[] exact_second_message = Arrays.copyOfRange(second_message, 0, alice_mac.length);

        String s_second_message = Base64.encodeToString(second_message, Base64.DEFAULT);
        String s_alice_mac = Base64.encodeToString(alice_mac, Base64.DEFAULT);
        String s_exact_second_message = Base64.encodeToString(exact_second_message, Base64.DEFAULT);

        Log.d("Alice", s_second_message);
        Log.d("Alice", s_alice_mac);
        Log.d("Alice", s_exact_second_message);

        return s_alice_mac.equals(s_exact_second_message);
    }


    public String key_out(){
        // so now we have the int key, check the length,
        // if less than 16 digits, make it to 16 digits, if more, then extract the first 16 digits
        String potential_key = reconciliation_function.int_array_to_string_no_comma(key);
        String final_key = "";

        if (potential_key.length() > 16){
            final_key = potential_key.substring(0,16);
        }
        else if (potential_key.length() < 16){

            // copy the key again and again
            while (potential_key.length() < 16){
                potential_key += potential_key;
            }
            final_key = potential_key.substring(0,16);
        }

        return final_key;
    }
}
