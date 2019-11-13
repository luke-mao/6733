package com.example.a6733.functions;

import com.example.a6733.functions.reconciliation_function;
import java.util.Arrays;

public class reconciliation_alice {

    /* CLIENT is the alice, send the message first*/

    /*this is the alice part, alice needs to receive two messages from bob
     * First message is L_bar, second message is the MAC encryption
     * In addition, other inputs are alice key three axis, alice window index three axis*/

    String first_message;
    byte[] second_message;
    int[] L_x, L_y, L_z;
    int[] key_x, key_y, key_z;

    public reconciliation_alice(byte[] first_message, byte[] second_message,
                                int[] L_x, int[] L_y, int[] L_z,
                                int[] key_x, int[] key_y, int[] key_z){

        this.first_message = new String(first_message); // the input are in byte[], here change into string
        this.second_message = second_message;
        this.L_x = L_x;
        this.L_y = L_y;
        this.L_z = L_z;
        this.key_x = key_x;
        this.key_y = key_y;
        this.key_z = key_z;
    }


    public boolean decision(){

        /*first compute the intersection of keys
         * need to separate the strings first*/
        String[] string_window = reconciliation_function.split_three_acc_window_strings(first_message);

        int[] L_intersection_x = reconciliation_function.string_to_int_array(string_window[0]);
        int[] L_intersection_y = reconciliation_function.string_to_int_array(string_window[1]);
        int[] L_intersection_z = reconciliation_function.string_to_int_array(string_window[2]);

        //now find the alice total key
        int[] key = reconciliation_function.intersection_keys_three_directions(
                key_x, L_x, L_intersection_x,
                key_y, L_y, L_intersection_y,
                key_z, L_z, L_intersection_z
        );

        int[] window = reconciliation_function.merge_three_int_array(
                L_intersection_x, L_intersection_y, L_intersection_z
        );

        // get the MAC byte[]
        byte[] alice_mac = reconciliation_function.MACencrypt(key,window);

        return Arrays.equals(alice_mac, second_message);
    }
}
