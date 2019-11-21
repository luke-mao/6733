package com.example.a6733.functions;

import android.util.Log;

import java.security.Key;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class reconciliation_function {

    /*convert int[] to string
     * this one is mainly used for bit conversion
     * output a string array from  [ 1,0,1,0] to 1010
     * used in the MAC encrypt*/
    public static String int_array_to_string_no_comma(int[] array) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            builder.append(String.valueOf(array[i]));
        }
        return builder.toString();
    }


    /*input an int array, return a string, separator is comma
     * this is to combine the window index of acceleration data
     * for example, window L for x direction is [1, 2, 3, 6, 10]
     * the output value will be String: 1,2,3,6,10*/
    public static String int_array_to_string(int[] array) {

        StringBuilder builder = new StringBuilder();

        builder.append(String.valueOf(array[0]));

        for (int i = 1; i < array.length; i++) {
            builder.append(",");
            builder.append(String.valueOf(array[i]));
        }
        return builder.toString();
    }


    /*input a string (string of acceleration data in one direction)
     * the output is interger array in this direction
     * essentially the reverse of int_array_to_string*/
    public static int[] string_to_int_array(String input) {

        String[] string_array = input.split(",");
        //convert each element into integer
        int[] int_array = new int[string_array.length];

        for (int i = 0; i < int_array.length; i++) {

            int_array[i] = Integer.parseInt(string_array[i]);
        }
        return int_array;
    }


    /*combine acceleration string of three direction together
     * use separator symbol "|"*/
    public static String final_acc_string(String accX) {

        String result = accX + "@";
        return result;
    }


    /*a reverse function of the combine_three_acc_strings
     * return a string array: [string accX, string accY, string accZ]*/
    public static String split_acc_strings(String input) {

        String[] result = input.split("@", 2);
        return result[0];
    }


    /*find the intersection set of keys based on each direction
     * So that for three directions, we need to run the function for three times
     * mykey: int[] of my original key in this direction
     * mywindow: int[] of the my window index that generate this key
     * intersection: int[] of the window index intersection using my window index and the incoming message*/
    public static int[] intersection_keys_one_direction(int[] mykey, int[] mywindow, int[] intersection) {

        int[] newkey = new int[intersection.length];

        int mywindow_index = 0;
        for (int i = 0; i < intersection.length; i++) {

            for (int mywindow_i = mywindow_index; mywindow_i < mywindow.length; mywindow_i++) {
                if (intersection[i] == mywindow[mywindow_i]) {
                    newkey[i] = mykey[mywindow_i];
                    mywindow_index = mywindow_i + 1;
                    break;
                }
            }
        }
        return newkey;
    }


    /*find the intersection of keys in three direction
     * notice the order of input*/
    public static int[] intersection_keys_three_directions(int[] mykeyX, int[] mywindowX, int[] intersectionX,
                                                           int[] mykeyY, int[] mywindowY, int[] intersectionY,
                                                           int[] mykeyZ, int[] mywindowZ, int[] intersectionZ) {

        int[] newkeyX = intersection_keys_one_direction(mykeyX, mywindowX, intersectionX);
        int[] newkeyY = intersection_keys_one_direction(mykeyY, mywindowY, intersectionY);
        int[] newkeyZ = intersection_keys_one_direction(mykeyZ, mywindowZ, intersectionZ);

        return merge_three_int_array(newkeyX,newkeyY,newkeyZ);

    }


    /*similarity check, bob function
     * The cut off is 80%. Less than 80% same, then cancel the transction*/
    public static boolean bob_similarity_check(int[] mywindowX, int[] intersectionX){

        double cutoff = 0.1;

        double simiX = (double) intersectionX.length / (double) mywindowX.length;

        Log.d("Similarity", String.valueOf(simiX));

        return simiX > cutoff;
    }

    public static boolean bob_similarity_check_three_direction(
            int[] mywindowX, int[] intersectionX,
            int[] mywindowY, int[] intersectionY,
            int[] mywindowZ, int[] intersectionZ){

        double cutoff = 0.1;

        double simiX = (double) intersectionX.length / (double) mywindowX.length;
        double simiY = (double) intersectionY.length / (double) mywindowY.length;
        double simiZ = (double) intersectionZ.length / (double) mywindowZ.length;

        Log.d("SimilarityX", String.valueOf(simiX));
        Log.d("SimilarityY", String.valueOf(simiY));
        Log.d("SimilarityZ", String.valueOf(simiZ));


        return ((simiX + simiY + simiZ)/3) > cutoff;
    }


    /*use the MAC method to encrypt the window int array using the key int_key*/
    public static byte[] MACencrypt(int[] int_key, int[] window){

        /*generate mac message, return bytes*/
        //first make the int[] key into string
        String keystring = int_array_to_string_no_comma(int_key);
        String data = int_array_to_string_no_comma(window);

        byte[] message = null;
        try{
            Key key = new SecretKeySpec(keystring.getBytes(), "");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(key);
            message = mac.doFinal(data.getBytes());
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return message;
    }


    /*find the intersection array of two integer array
     * main means my window index, guest means incoming window index
     * so that the intersection needs to run for three directions
     * this should run for three direction, the key function one direction is enough*/
    public static int[] intersection_two_int_arrays(int[] main, int[] guest){

        /*find how many elements in guest is in main
         * Take element from guest, and compare with each element in main
         * stop until the main element > guest element*/

        if (guest.length < main.length){

            int[] intersection = new int[guest.length];

            int main_index = 0;
            int intersection_index = 0;

            for (int guest_i = 0; guest_i < guest.length; guest_i++){

                for (int main_i = main_index; main_i < main.length; main_i++){

                    if (guest[guest_i] == main[main_i]){
                        intersection[intersection_index] = guest[guest_i];

                        intersection_index++;
                        main_index = main_i + 1;
                        break;
                    }

                    if (guest[guest_i] < main[main_i]){
                        break;
                    }
                }
            }

            // now the result int array has result_index numbers
            int[] result = new int[intersection_index];
            for (int i = 0; i < result.length; i++){
                result[i] = intersection[i];
            }

            return result;
        }
        else {

            // now the guest int array has more elements than the main int array
            // find the intersection
            int[] intersection = new int[main.length];

            int guest_index = 0;
            int intersection_index = 0;

            for (int main_i = 0; main_i < main.length; main_i++) {

                for (int guest_i = guest_index; guest_i < guest.length; guest_i++) {

                    if (guest[guest_i] == main[main_i]) {
                        intersection[intersection_index] = guest[guest_i];

                        intersection_index++;
                        guest_index = main_i + 1;
                        break;
                    }

                    if (main[main_i] < guest[guest_i]) {
                        break;
                    }
                }
            }

            // now the result int array has result_index numbers
            int[] result = new int[intersection_index];
            for (int i = 0; i < result.length; i++) {
                result[i] = intersection[i];
            }

            return result;
        }
    }


    /*merge three int[] into one int[]*/
    public static int[] merge_three_int_array(int[] a, int[] b, int[] c){

        int[] result = new int[a.length + b.length + c.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        System.arraycopy(c, 0, result, b.length, c.length);

        return result;
    }

}
