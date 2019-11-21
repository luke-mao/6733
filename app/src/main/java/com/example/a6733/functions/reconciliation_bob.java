package com.example.a6733.functions;

import com.example.a6733.functions.reconciliation_function;

import java.util.Arrays;

public class reconciliation_bob {

    /*change on the reconciliation bob
    * So that use one direction key only*/

    int[] L_x;
    int[] key_x;
    int[] L_y;
    int[] key_y;
    int[] L_z;
    int[] key_z;

    String message;

    int[] key;

    int[] L_Alice_z;

    /* this is the initialization of the class */
    public reconciliation_bob(int[] L_x, int[] key_x,
                              int[] L_y, int[] key_y,
                              int[] L_z, int[] key_z,
                              byte[] buf){

        this.L_x = L_x;
        this.key_x = key_x;

        this.L_y = L_y;
        this.key_y = key_y;

        this.L_z = L_z;
        this.key_z = key_z;

        this.message = new String(buf);
    }


    /*bob decision returns a boolean
     * if true, then proceed*/

    int[] L_intersection_x;
    int[] L_intersection_y;
    int[] L_intersection_z;

    public boolean decision(){

        String actual_message = reconciliation_function.split_acc_strings(message);
        L_Alice_z = reconciliation_function.string_to_int_array(actual_message);

        //L_intersection_x = reconciliation_function.intersection_two_int_arrays(L_x, L_Alice_x);
        //L_intersection_y = reconciliation_function.intersection_two_int_arrays(L_y, L_Alice_y);
        L_intersection_z = reconciliation_function.intersection_two_int_arrays(L_z, L_Alice_z);

        return reconciliation_function.bob_similarity_check(L_z, L_intersection_z);
    }


    /*if the decision is true, then proceed with sending the message
     * first part is simply bytes[] of the message of L_intersection of three directions*/
    public byte[] first_part_message() {

        String message = reconciliation_function.int_array_to_string(L_intersection_z);
        String result = reconciliation_function.final_acc_string(message);
        return result.getBytes();

    }


    /*second part of the message includes the Mac(key using the bob key after intersection,
    L_intersection)*/
    public byte[] second_part_message(){

        /*MACencrypt(int[] key, int[] window)*/
        // first combine three keys and three intersection windows
        key = reconciliation_function.intersection_keys_one_direction(key_z, L_z, L_intersection_z);

        int[] window = L_intersection_z;

        return reconciliation_function.MACencrypt(key,window);
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
