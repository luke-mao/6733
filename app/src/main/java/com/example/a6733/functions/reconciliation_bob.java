package com.example.a6733.functions;

import com.example.a6733.functions.reconciliation_function;

public class reconciliation_bob {

    /*change on the reconciliation bob
    * So that use one direction key only*/

    String L_Alice;
    int[] L_x;
    int[] key_x;

    int[] key;

    /* this is the initialization of the class */
    public reconciliation_bob(byte[] L_Alice, int[] L_x, int[] key_x){
        this.L_Alice = new String(L_Alice);
        this.L_x = L_x;
        this.key_x = key_x;
    }


    /*bob decision returns a boolean
     * if true, then proceed*/

    int[] L_intersection_x;

    public boolean decision(){

        String str_Alice = reconciliation_function.split_acc_strings(L_Alice);

        int[] L_Alice_x = reconciliation_function.string_to_int_array(str_Alice);

        L_intersection_x = reconciliation_function.intersection_two_int_arrays(L_x, L_Alice_x);

        return reconciliation_function.bob_similarity_check(L_x, L_intersection_x);
    }


    /*if the decision is true, then proceed with sending the message
     * first part is simply bytes[] of the message of L_intersection of three directions*/
    public byte[] first_part_message(){

        String x = reconciliation_function.int_array_to_string(L_intersection_x);

        String total = reconciliation_function.final_acc_string(x);
        return total.getBytes();
    }


    /*second part of the message includes the Mac(key using the bob key after intersection,
    L_intersection)*/
    public byte[] second_part_message(){

        /*MACencrypt(int[] key, int[] window)*/
        // first combine three keys and three intersection windows
        key = reconciliation_function.intersection_keys_one_direction(key_x, L_x, L_intersection_x);

        int[] window = L_intersection_x;

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
