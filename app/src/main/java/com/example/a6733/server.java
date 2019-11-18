package com.example.a6733;

import com.example.a6733.functions.DateUtil;
import com.example.a6733.functions.reconciliation_bob;

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

// Libraries for bit generator
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ScaleGestureDetectorCompat;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Build;
import android.os.Bundle;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import flanagan.analysis.CurveSmooth;

import static java.lang.Float.NaN;


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

    /*TAG*/
    String TAG = "Server: ";

    int[] L_Bob_x;
    int[] L_Bob_y;
    int[] L_Bob_z;

    int[] key_Bob_x;
    int[] key_Bob_y;
    int[] key_Bob_z;

    reconciliation_bob bob;     // define the class first



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

        try {
            server_ip = getLocalIpAddress();
            server_port = 8080;

            server_tv_ip.append(server_ip);
            server_tv_port.append("8080");
        } catch (Exception e) {
            Log.d(TAG, "Error get local ip address");
            e.printStackTrace();
        }

        /*Get the two buttons with click listener, details defined later in function onClick*/
        findViewById(R.id.server_send).setOnClickListener(this);

        findViewById(R.id.server_sample_start).setOnClickListener(this);

        try {
            socket = new DatagramSocket(server_port);
            socket.setReuseAddress(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "error open the socket");
        }

        //new Thread(new thread_udp_listen()).start();


    }


    /*onClick function: for buttons*/
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.server_send) {

            /*Send the message using the thread
             * Clean the input box*/
            String message = server_et.getText().toString();
            server_et.setText("");

            if (!message.isEmpty()) {
                new Thread(new thread_udp_send(message.getBytes())).start();
            }
        }
        else if (v.getId() == R.id.server_sample_start){

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
                /*messenge_counter, count the message*/
                int messenge_counter = 0;

                while (true) {

                    byte[] buffer = new byte[256];

                    DatagramPacket packet = new DatagramPacket(
                            buffer,
                            buffer.length
                    );

                    socket.receive(packet);
                    messenge_counter++;     //add one to the messenge_counter

                    // for the first message:
                    // for the second message:
                    // for the third message:
                    // if there is number 4 message and later on, use encryption channel

                    // at the first time, the message comes with a word  "connect"
                    // use this to determine the inet address of sender and port number'
                    byte[] buf = packet.getData();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            server_tv_2.append(DateUtil.getNowTime() + "\n" + "receive message\n");
                        }
                    });


                    inet_client_address = packet.getAddress();
                    client_port = packet.getPort();


                    if (messenge_counter == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                server_connection_status.setText("Connected");
                            }
                        });
                        new Thread(new thread_udp_send("connect".getBytes())).start();
                        new Thread(new thread_timer()).start();     // start the timer

                    } else if (messenge_counter == 2) {
                        // this time, client sends L_Alice to the server （me）
                        //need to run the reconciliation

                        bob = new reconciliation_bob(
                                buf,  // this is the alice message in byte, but the programme will convert into a string
                                L_Bob_x, L_Bob_y, L_Bob_z,
                                key_Bob_x, key_Bob_y, key_Bob_z
                        );

                        if (bob.decision()){

                            // if continue to connect, send the two meesages
                            byte[] first_message = bob.first_part_message();
                            new Thread(new thread_udp_send(first_message)).start();


                            byte[] second_message = bob.second_part_message();
                            new Thread(new thread_udp_send(second_message)).start();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    server_tv_2.append("\n"+ DateUtil.getNowTime() +"\nPair Success !!!");
                                }
                            });
                        }
                        else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    server_tv_2.append("\n"+ DateUtil.getNowTime() +"\nPair fail !!!");
                                }
                            });

                        }

                    } else if (messenge_counter >= 3) {

                        // so now you should use the encryption now
                        final String message = new String(buf);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                server_tv_1.append(DateUtil.getNowTime() + " receive...\n" + message + "\n");
                            }
                        });
                    }

                }
            } catch (Exception e) {
                Log.d(TAG, "Wrong client receiving thread");
                e.printStackTrace();
            }
        }
    }


    /*thread_udp_send
     * Task: send udp packet to the destinated server ip and port
     * Input: String message*/
    class thread_udp_send implements Runnable {

        private byte[] message;

        thread_udp_send(byte[] message) {
            this.message = message;
        }

        @Override
        public void run() {

            try {
                byte[] buf = message;
                DatagramPacket packet = new DatagramPacket(
                        buf,
                        buf.length,
                        inet_client_address,
                        client_port
                );

                socket.send(packet);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        server_tv_2.append(DateUtil.getNowTime() +"\nserver send message\n");
                    }
                });


            } catch (Exception e) {
                Log.d(TAG, "error: thread udp send");
                e.printStackTrace();
            }
        }
    }

    class thread_timer implements Runnable {

        @Override
        public void run() {

            final int addition; //additional time added

            int minute = DateUtil.getNowMinute();

            if (DateUtil.getNowSecond() > 30) {
                minute = minute + 2;
                addition = 2;
            } else {
                minute += 1;
                addition = 1;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server_tv_1.append(
                            DateUtil.getNowTime() + " Prepare for sampling less than " + addition + " minutes...\n"
                    );
                }
            });

            while (DateUtil.getNowMinute() != minute) {
                continue;
            }
            new Thread(new sampling()).start();     // straight after the time is reached, start the sampling

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server_tv_1.append(DateUtil.getNowTime() + " Start Sampling\n");
                }
                /*Here you can call your thread for the sampling*/
            });

        }
    }

    class sampling implements Runnable {

        /*this part is the sampling, it should return L int[] and key int[]*/
        @Override
        public void run() {
            //////// insert Bit generator here

            L_Bob_x = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
            L_Bob_y = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
            L_Bob_z = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

            key_Bob_x = new int[]{1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1};
            key_Bob_y = new int[]{1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1};
            key_Bob_z = new int[]{1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1};

            /*now start the reconciliation
            * the above code has defined the variable bob, now define its class
            * alice will send the message first, so now is ok*/
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server_tv_1.append(DateUtil.getNowTime() + " finish Sampling\n");
                }
                /*Here you can call your thread for the sampling*/
            });
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
    protected void onDestroy() {

        socket.close();
        super.onDestroy();
    }

}
