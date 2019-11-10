package com.example.a6733;

import com.example.a6733.functions.DateUtil;

import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;


public class server
        extends AppCompatActivity
        implements View.OnClickListener {


    /*This is the main coding part for server_service
     * Most code for udp communication are similar
     * Difference is that client input the IP and port to connect to the server
     * The server page has 3 text view parts to manipulate
     * They will be detailed below*/

    /*Define all the thread here*/


    /*These three text view are designed for your use.
     * Do not output anything outside these three text views
     * Note that the height is "wrap content", convenient to use*/
    TextView server_tv_1, server_tv_2, server_tv_3;

    TextView server_tv_ip, server_tv_port;


    /*This is the input message edittext for the client
     * We can set text at here for information transfer
     * So once click send, the heart beat or other information can be sent*/
    EditText server_et;

    /*Connection status: Not connected / Connected*/
    TextView server_connection_status;


    /*Define some strings and integer for port number
     * These are placed as global variables*/
    String server_ip = "";
    int client_port, server_port;
    InetAddress inet_client_address;
    DatagramSocket socket;
    boolean first_time_message = true;

    /*TAG*/
    String TAG = "Server: ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);

        /*Get textview, edit text done*/
        server_tv_1 = findViewById(R.id.server_tv_1);
        server_tv_1.setText("");

        server_tv_2 = findViewById(R.id.server_tv_2);
        server_tv_3 = findViewById(R.id.server_tv_3);
        server_connection_status = findViewById(R.id.server_connection_status);
        server_et = findViewById(R.id.server_et);

        server_tv_ip = findViewById(R.id.server_ip);
        server_tv_port = findViewById(R.id.server_port);

        try{
            server_ip = getLocalIpAddress();
            server_port = 8080;

            server_tv_ip.append(server_ip);
            server_tv_port.append("8080");
        }
        catch (Exception e){
            Log.d(TAG, "Error get local ip address");
            e.printStackTrace();
        }

        /*Get the two buttons with click listener, details defined later in function onClick*/
        findViewById(R.id.server_send).setOnClickListener(this);

        try{
            socket = new DatagramSocket(server_port);
            socket.setReuseAddress(true);
        }
        catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "error open the socket");
        }

        new Thread(new thread_udp_listen()).start();
    }


    /*onClick function: for buttons*/
    @Override
    public void onClick(View v){

        if (v.getId() == R.id.server_send){

            /*Send the message using the thread
             * Clean the input box*/
            String message = server_et.getText().toString();
            server_et.setText("");

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

                while(true) {

                    byte[] buf = new byte[256];

                    DatagramPacket packet = new DatagramPacket(
                            buf,
                            buf.length
                    );

                    socket.receive(packet);




                    // at the first time, the message comes with a word  "connect"
                    // use this to determine the inet address of sender and port number'
                    buf = packet.getData();
                    final String message = new String(buf, "UTF-8");

                    Log.d(TAG, "Message:"+message);


                    inet_client_address = packet.getAddress();
                    client_port = packet.getPort();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            server_connection_status.setText("Connected");

                        }
                    });

                    /*if this is the first time message, then return it*/
                    if (first_time_message){
                        // send the response "connect" for this particular message
                        new Thread(new thread_udp_send("connect", false)).start();
                        first_time_message = false;
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                server_tv_1.append(DateUtil.getNowTime()+" receive...\n" + message + "\n");
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
                        inet_client_address,
                        client_port
                );

                socket.send(packet);


                if (displayBoolean){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            server_tv_1.append(DateUtil.getNowTime()+" send...\n" + message + "\n");
                            Log.d(TAG, "send"+message);
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



    /*Get the local host ip address*/
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
