package com.example.a6733;

import com.example.a6733.functions.DateUtil;

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
    boolean first_time_message = true;

    /*Use for the log.d printout, use this TAG for client interface issues*/
    String TAG = "Client: ";


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
                new Thread(new thread_udp_send("connect", false)).start();


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
                new Thread(new thread_udp_send(message, true)).start();
            }
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

                while(true){

                    byte[] buf = new byte[256];

                    DatagramPacket packet = new DatagramPacket(
                            buf,
                            buf.length
                    );

                    socket.receive(packet);

                    buf = packet.getData();

                    final String message = new String(buf, "UTF-8");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            client_connection_status.setText("Connected");
                        }
                    });

                    /*If the client first receive message, do not show
                    * Other cases show the message*/
                    if (first_time_message){

                        first_time_message = false;
                        /*start the timer thread*/
                        new Thread(new thread_timer()).start();
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                client_tv_1.append(DateUtil.getNowTime()+" receive...\n" + message + "\n");
                            }
                        });
                    }
                }
            }
            catch (Exception e) {
                Log.d(TAG, "Wrong client receiving thread");
                e.printStackTrace();
            }
        }
    }


    /*thread_udp_send
    * Task: send udp packet to the destinated server ip and port
    * Input: String message*/
    class thread_udp_send implements Runnable {
        private String message;
        private boolean displayBoolean;

        thread_udp_send(String message, boolean displayBoolean) {
            this.message = message;
            this.displayBoolean = displayBoolean;
        }

        @Override
        public void run() {

            try{
                byte[] buf = message.getBytes("UTF-8");
                DatagramPacket packet = new DatagramPacket(
                        buf,
                        buf.length,
                        inet_server_address,
                        server_port
                );

                socket.send(packet);

                if (displayBoolean){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            client_tv_1.append(DateUtil.getNowTime()+" send...\n" + message + "\n");
                        }
                    });
                }
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

            if (DateUtil.getNowSecond() > 30){
                minute += 2;
            }
            else{
                minute += 1;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    client_tv_2.setText(DateUtil.getNowTime()+" Prepare for sampling...\n");
                }
            });

            while (DateUtil.getNowMinute() != minute){
                continue;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    client_tv_2.append(DateUtil.getNowTime() + " Start Sampling");
                }

                /*At here call your thread for sampling */
            });
        }
    }


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
