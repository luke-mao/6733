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

    int[] L_Alice_x;
    int[] L_Alice_y;
    int[] L_Alice_z;

    int[] key;

    /* this is the initialization of the class */
    public reconciliation_bob(int[] L_x, int[] key_x,
                              int[] L_y, int[] key_y,
                              int[] L_z, int[] key_z,
                              int[] L_Alice_x, int[] L_Alice_y, int[] L_Alice_z){

        this.L_x = L_x;
        this.key_x = key_x;

        this.L_y = L_y;
        this.key_y = key_y;

        this.L_z = L_z;
        this.key_z = key_z;

        this.L_Alice_x = L_Alice_x;
        this.L_Alice_y = L_Alice_y;
        this.L_Alice_z = L_Alice_z;
    }


    /*bob decision returns a boolean
     * if true, then proceed*/

    int[] L_intersection_x;
    int[] L_intersection_y;
    int[] L_intersection_z;

    public boolean decision(){

        L_intersection_x = reconciliation_function.intersection_two_int_arrays(L_x, L_Alice_x);
        L_intersection_y = reconciliation_function.intersection_two_int_arrays(L_y, L_Alice_y);
        L_intersection_z = reconciliation_function.intersection_two_int_arrays(L_z, L_Alice_z);

        return reconciliation_function.bob_similarity_check_three_direction(
                L_x, L_intersection_x,
                L_z, L_intersection_y,
                L_z, L_intersection_z
        );
    }


    /*if the decision is true, then proceed with sending the message
     * first part is simply bytes[] of the message of L_intersection of three directions*/
    public byte[] first_part_message() {

        /*here, we have L_intersection_x/y/z as int[], need to combine them together and change to byte[]*/
        char[] return_char_array = new char[256];
        Arrays.fill(return_char_array, 'G'); // (int)'G' = 71
        int i = 0;
        for (; i < L_intersection_x.length; i++) {
            return_char_array[i] = (char) L_intersection_x[i];
        }
        return_char_array[i] = (char) 'H'; // (int)'H' = 72
        i++;
        for (int j = 0; j < L_intersection_y.length; j++) {
            return_char_array[j + i] = (char) L_intersection_y[j];
            i++;
        }
        return_char_array[i] = (char) 'H'; // (int)'H' = 72
        i++;
        for (int k = 0; k < L_intersection_z.length; k++) {
            return_char_array[k + i] = (char) L_intersection_z[k];
            i++;
        }
        return_char_array[i] = (char) 'H'; // (int)'H' = 72

        byte[] return_byte_array = new byte[256];
        for (i=0; i < 256; i++){
            return_byte_array[i] = (byte) return_char_array[i];
        }
        return return_byte_array;
    }


    /*second part of the message includes the Mac(key using the bob key after intersection,
    L_intersection)*/
    public byte[] second_part_message(){

        /*MACencrypt(int[] key, int[] window)*/
        // first combine three keys and three intersection windows
        key = reconciliation_function.intersection_keys_three_directions(
                key_x, L_x, L_intersection_x,
                key_y, L_y, L_intersection_y,
                key_z, L_z, L_intersection_z);

        int[] window = reconciliation_function.merge_three_int_array(L_intersection_x,L_intersection_y,L_intersection_z);

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
