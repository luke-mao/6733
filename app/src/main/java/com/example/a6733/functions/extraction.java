package com.example.a6733.functions;

public class extraction {

    int[] raw_key;
    int[] raw_index;
    int length;

    public extraction(int[] raw_index, int[] raw_key){
        this.raw_key = raw_key;
        this.raw_index = raw_index;
        find_length();
    }

    public int find_length(){

        for (int i = 0; i < raw_index.length; i++){
            if (raw_index[i] == 0){
                length = i;
                break;
            }
        }
        return length;
    }

    public int[] treat_index(){
        int[] result_index = new int[length];
        for (int i = 0; i < length; i++){
            result_index[i] = raw_index[i];
        }
        return result_index;
    }

    public int[] treat_key(){
        int[] result_key = new int[length];
        for (int i = 0; i < length; i++){
            result_key[i] = raw_key[i];
        }
        return result_key;
    }
}
