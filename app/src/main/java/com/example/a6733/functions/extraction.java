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
        int previous, now;
        int index;
        previous = raw_index[0];
        for (index = 1; index < raw_index.length; index++){
            now = raw_index[index];
            if (previous > now){
                break;
            }
            else{
                previous = now;
            }
        }
        length = index;

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
