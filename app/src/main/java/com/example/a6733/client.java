package com.example.a6733;

import com.example.a6733.functions.DateUtil;
import com.example.a6733.functions.decryption;
import com.example.a6733.functions.encryption;
import com.example.a6733.functions.reconciliation_alice;
import com.example.a6733.functions.reconciliation_function;

import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.EditText;
import android.widget.TextView;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class client
        extends AppCompatActivity
        implements View.OnClickListener {

    /*This is the main coding part for client_service
    * Most code for udp communication are similar
    * Difference is that client input the IP and port to connect to the server
    * The client page has 3 text view parts to manipulate
    * They will be detailed below*/

    /*Define all the thread here*/


    /*These three text view are designed for your use.
    * Do not output anything outside these three text views
    * Note that the height is "wrap content", convenient to use*/
    TextView client_tv_1, client_tv_2, client_tv_3;
    TextView client_tv_ip, client_tv_port;

    /*This is the input message edittext for the client
    * We can set text at here for information transfer
    * So once click send, the heart beat or other information can be sent*/
    EditText client_et, client_input_server_ip, client_input_server_port;

    /*Connection status: Not connected / Connected*/
    TextView client_connection_status;

    /*Define some strings and integer for port number
    * These are placed as global variables*/
    String client_ip = "";
    String server_ip = "";
    int client_port, server_port;
    InetAddress inet_server_address;
    DatagramSocket socket;

    /*Use for the log.d printout, use this TAG for client interface issues*/
    String TAG = "Client: ";


    int[] L_Alice_x;
    int[] L_Alice_y;
    int[] L_Alice_z;

    int[] key_Alice_x;
    int[] key_Alice_y;
    int[] key_Alice_z;

    String mykey;

    reconciliation_alice alice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);

        /*Get textview, edit text done*/
        client_tv_1 = findViewById(R.id.client_tv_1);
        client_tv_1.setText("");
        client_tv_2 = findViewById(R.id.client_tv_2);
        client_tv_3 = findViewById(R.id.client_tv_3);
        client_connection_status = findViewById(R.id.client_connection_status);
        client_et = findViewById(R.id.client_et);

        client_tv_ip = findViewById(R.id.client_ip);
        client_tv_port = findViewById(R.id.client_port);

        try{
            client_ip = getLocalIpAddress();
            client_port = 8080;
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "error get local ip address");
        }

        client_tv_ip.setText(client_ip);
        client_tv_port.setText("8080");


        client_input_server_ip = findViewById(R.id.client_input_server_ip);
        client_input_server_ip.setText("10.0.0.41");

        client_input_server_port = findViewById(R.id.client_input_server_port);
        client_input_server_port.setText("8080");

        /*Get the two buttons with click listener, details defined later in function onClick*/
        findViewById(R.id.client_button_connect).setOnClickListener(this);
        findViewById(R.id.client_send).setOnClickListener(this);
        findViewById(R.id.client_sample_start).setOnClickListener(this);
    }


    /*onClick function: for buttons*/
    @Override
    public void onClick(View v){

        if (v.getId() == R.id.client_button_connect){

            /*click the connect button, several tasks
            * 1. Get the input from edittext, try to connect by send string "connect"
            * 2. Wait for reply
            * 3. If reply string "connect", then display connection status as "connected"*/
            try{

                // get hte server ip from input
                server_ip = client_input_server_ip.getText().toString().trim();
                server_port = Integer.parseInt(client_input_server_port.getText().toString().trim());

                Log.d(TAG, server_ip);

                // send the "connect" string
                inet_server_address =InetAddress.getByName(server_ip);


                // start the sending thread, send message "connect"
                socket = new DatagramSocket(client_port);
                socket.setReuseAddress(true);


                // prepare for the Alice message
                String alice_message = reconciliation_function.combine_three_acc_strings(
                        reconciliation_function.int_array_to_string(L_Alice_x),
                        reconciliation_function.int_array_to_string(L_Alice_y),
                        reconciliation_function.int_array_to_string(L_Alice_z)
                );


                new Thread(new thread_udp_send(alice_message.getBytes())).start();

                // start the sending thread, send message "connect"
                new Thread(new thread_udp_listen()).start();

            }
            catch (Exception e){
                Log.d(TAG, "Exception in socket connection");
                e.printStackTrace();
            }
        }
        else if(v.getId() == R.id.client_send){

            /*Send the message using the thread
            * Clean the input box*/
            String message = client_et.getText().toString();
            client_et.setText("");

            if (! message.isEmpty()){
                encryption new_encryption = new encryption(mykey, message);
                byte[] byte_message = new_encryption.encrypt();
                new Thread(new thread_udp_send(byte_message)).start();
            }
        }
        else if (v.getId() == R.id.client_sample_start){
            new Thread(new thread_timer()).start();
        }
    }



    /*thread_udp_listen
    * No input, listen on the port 8080
    * Check the message inet address to the input server address
    * Ignore message if not the right one*/
    class thread_udp_listen implements Runnable {

        @Override
        public void run() {
            try {

                /*define variable for the counting of messages*/
                int messenge_counter = 0;
                byte[] first_message = new byte[] {};
                byte[] second_message;
                //boolean start_encryption = false;

                while(true){

                    byte[] buf = new byte[256];

                    DatagramPacket packet = new DatagramPacket(
                            buf,
                            buf.length
                    );

                    socket.receive(packet);
                    messenge_counter++;     // add one to the messenger counter

                    buf = packet.getData();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            client_tv_2.append(DateUtil.getNowTime() + "\n" + "receive message\n");
                        }
                    });


                    /*If the client first receive message, do not show
                    * Other cases show the message*/
                    if (messenge_counter == 1){
                        first_message = buf;

                    }
                    else if (messenge_counter == 2){
                        /*bob will send alice two pieces of message
                        * the first is the L_bar, the second is the MAC message
                        * put it this way, bob will not cancel any transction, only alice will */
                        /*this message is not encrypted*/
                        second_message = buf;

                        /*now alice can run the reconciliation
                         * simply run it here*/
                        alice = new reconciliation_alice(
                                first_message, second_message,
                                L_Alice_x, L_Alice_y, L_Alice_z,
                                key_Alice_x, key_Alice_y, key_Alice_z
                        );

                        if (alice.decision()){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    client_tv_2.append("\n" + DateUtil.getNowTime() + "\nPaired success !!");
                                }
                            });

                            mykey = alice.key_out();

                        }
                        else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    client_tv_2.append("\n" + DateUtil.getNowTime() + "\nPaired fail !!");
                                    client_connection_status.setText("Pair fail !!");
                                }
                            });

                            break;
                        }
                    }
                    else{
                        // if the programme reach here, now is time to use the decryption method
                        decryption new_decryption = new decryption(mykey, buf);
                        final String message = new_decryption.decrypt();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                client_tv_2.append(DateUtil.getNowTime() +"Receive: " + message + "\n");
                            }
                        });
                    }
                }

                // if break from the loop, then stop the thread
                Thread.currentThread().interrupt();
            }
            catch (Exception e) {
                Log.d(TAG, "Wrong client receiving thread");
                e.printStackTrace();
            }
        }
    }


    /*thread_udp_send
    * Task: send udp packet to the destinated server ip and port
    * Input: byte[] message
    * Note that everything is in byte[] only !!!*/
    class thread_udp_send implements Runnable {

        private byte[] message;

        thread_udp_send(byte[] message) {
            this.message = message;
        }

        @Override
        public void run() {

            try{
                byte[] buf = message;
                DatagramPacket packet = new DatagramPacket(
                        buf,
                        buf.length,
                        inet_server_address,
                        server_port
                );

                socket.send(packet);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        client_tv_2.append(DateUtil.getNowTime() +" client send message\n");
                    }
                });
            }
            catch (Exception e){
                Log.d(TAG, "error: thread udp send");
                e.printStackTrace();
            }
        }
    }

    class thread_timer implements Runnable{

        @Override
        public void run(){

            int minute = DateUtil.getNowMinute();
            final int addition;     // additional time added

            if (DateUtil.getNowSecond() > 30){
                minute += 2;
                addition = 2;
            }
            else{
                minute += 1;
                addition = 1;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    client_tv_1.append(
                            DateUtil.getNowTime()+" Prepare for sampling less than " + addition + " minutes...\n"
                    );
                }
            });

            while (DateUtil.getNowMinute() != minute){
                continue;
            }

            // start the sampling
            new Thread(new sampling()).start();     // straight after the time is reached, start the sampling

            // report on the main screen
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    client_tv_1.append(DateUtil.getNowTime() + " Start Sampling\n");
                }

                /*At here call your thread for sampling */
            });
        }
    }

    class sampling implements Runnable{

        /*this part is the sampling, it should return L int[] and key int[]*/
        @Override
        public void run(){

            ///////
            /*the code here should derive the following variables
            * they are decleared at the top of the page*/
            L_Alice_x = new int[] {1,2,3,5,6,7,9,10};
            L_Alice_y = new int[]{1,2,3,5,6,7,9,10};
            L_Alice_z = new int[]{1,2,3,5,6,7,9,10};

            key_Alice_x =new int[] {1,0,1,1,0,1,1,0};
            key_Alice_y = new int[]{1,0,1,1,0,1,1,0};
            key_Alice_z = new int[]{1,0,1,1,0,1,1,0};

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    client_tv_1.append(DateUtil.getNowTime() + " Sampling finish\n");
                }

                /*At here call your thread for sampling */
            });

        }
    }


/*    *//*now goes the reconciliation, this is Alice, send message first*//*
    String alice_message = reconciliation_function.combine_three_acc_strings(
            reconciliation_function.int_array_to_string(L_Alice_x),
            reconciliation_function.int_array_to_string(L_Alice_y),
            reconciliation_function.int_array_to_string(L_Alice_z)
    );

    *//*alice sends the L_Alice first to bob*//*
            new Thread(new thread_udp_send(alice_message.getBytes())).start();

            Log.d("Client Alice message:", alice_message);

    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            client_tv_2.append(DateUtil.getNowTime() + " Alice send message\n");
        }

        *//*At here call your thread for sampling *//*
    });*/


    /*function: get the local host ip address
    * return in String*/
    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array())
                .getHostAddress();
    }


    @Override
    protected void onDestroy(){

        socket.close();
        super.onDestroy();
    }

}
