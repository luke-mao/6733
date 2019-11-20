package com.example.a6733.functions;

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
    int[] key;

    public reconciliation_alice(byte[] first_message, byte[] second_message, int[] L_x, int[] key_x){

        this.first_message = new String(first_message); // the input are in byte[], here change into string
        this.second_message = second_message;
        this.L_x = L_x;
        this.key_x = key_x;
    }


    public boolean decision(){

        /*first compute the intersection of keys
         * need to separate the strings first*/
        String string_int = reconciliation_function.split_acc_strings(first_message);

        int[] L_intersection_x = reconciliation_function.string_to_int_array(string_int);

        //now find the alice total key
        key = reconciliation_function.intersection_keys_one_direction(key_x, L_x, L_intersection_x);

        int[] window = L_intersection_x;

        // get the MAC byte[]
        byte[] alice_mac = reconciliation_function.MACencrypt(key,window);

        return Arrays.equals(alice_mac, second_message);
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
