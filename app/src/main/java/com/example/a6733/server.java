package com.example.a6733;

import com.example.a6733.functions.DateUtil;
import com.example.a6733.functions.decryption;
import com.example.a6733.functions.encryption;
import com.example.a6733.functions.reconciliation_bob;
import com.example.a6733.functions.extraction;
import com.example.a6733.functions.reconciliation_function;

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
        implements View.OnClickListener, SensorEventListener {

    public int[] L_ALice_x = new int[70];
    public int[] L_ALice_y = new int[70];
    public int[] L_ALice_z = new int[70];
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

    public int[] L_Bob_x = new int[70];
    public int[] L_Bob_y = new int[70];
    public int[] L_Bob_z = new int[70];

    public int[] key_Bob_x = new int[70];
    public int[] key_Bob_y = new int[70];
    public int[] key_Bob_z = new int[70];

    public int[] f_L_Bob_x;
    public int[] f_L_Bob_y;
    public int[] f_L_Bob_z;

    public int[] f_key_Bob_x;
    public int[] f_key_Bob_y;
    public int[] f_key_Bob_z;

    String mykey;

    reconciliation_bob bob;     // define the class first

    // Temporary variables to store sensor data


    private SensorManager mSensorManager = null;
    // angular speeds from gyro
    private float[] gyro = new float[3];
    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];
    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];
    // magnetic field vector
    private float[] magnet = new float[3];
    // accelerometer vector
    private float[] accel = new float[3];
    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];
    // final orientation angles from sensor fusion
    private float[] fusedOrientation = new float[3];
    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];

    // accelerometer vector in global/earth frame
    private float[] accel_gl = new float[3];

    private float Zn_6 = NaN;
    private float Zn_5 = NaN;
    private float Zn_4 = NaN;
    private float Zn_3 = NaN;
    private float Zn_2 = NaN;
    private float Zn_1 = NaN;
    private float Zn = NaN;

    private boolean request_flag = false;
    private int sensor_sample_count = 0;

    public double[] acc_g = new double[750];
    public double[] acc_n = new double[750];
    public double[] acc_e = new double[750];
    public double[] smoothed_acc_g = new double[750];
    public double[] smoothed_acc_n = new double[750];
    public double[] smoothed_acc_e = new double[750];
    public int[] bits_g = new int[70];
    public int[] bits_n = new int[70];
    public int[] bits_e = new int[70];
    public int[] bits = new int[210];

    private double mean = 0;
    private double stdev = 0;
    private double upper_ts = 0;
    private double lower_ts = 0;

    // prev_peak time in millis
    private long t_peak = 0;

    private int steps = 0;

    public static final int TIME_CONSTANT = 33;
    public static final float FILTER_COEFFICIENT = 0.98f;
    private Timer fuseTimer = new Timer();

    public boolean start_recording_flag = false;

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

        // start listening the meessage, this is done in the mainActivity
        new Thread(new thread_udp_listen()).start();



        /* ------------------------------------------------------------------- */

        // ini gyro
        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;
        // ini gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;


        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        initListeners();

        // wait for 1 sec until sensors are initialised
        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                1000, TIME_CONSTANT);
    }

    public void initListeners(){
        /*100Hz*/
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                10000);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                10000);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                10000);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // count how many samples have been recorded
                if (start_recording_flag) {

                    sensor_sample_count++;
                    if (sensor_sample_count == 750) {
                        start_recording_flag = false;
                        generateKey();
                        extractBits();
                        Toast.makeText(this, "Finish sampling", Toast.LENGTH_SHORT).show();


                        extraction extract_x = new extraction(L_Bob_x, key_Bob_x);
                        extraction extract_y = new extraction(L_Bob_y, key_Bob_y);
                        extraction extract_z = new extraction(L_Bob_z, key_Bob_z);

                        f_L_Bob_x = new int[extract_x.find_length()];
                        f_key_Bob_x = new int[extract_x.find_length()];

                        f_L_Bob_x = extract_x.treat_index();
                        f_key_Bob_x = extract_x.treat_key();

                        f_L_Bob_y = new int[extract_y.find_length()];
                        f_key_Bob_y = new int[extract_y.find_length()];

                        f_L_Bob_y = extract_y.treat_index();
                        f_key_Bob_y = extract_y.treat_key();

                        f_L_Bob_z = new int[extract_z.find_length()];
                        f_key_Bob_z = new int[extract_z.find_length()];

                        f_L_Bob_z = extract_z.treat_index();
                        f_key_Bob_z = extract_z.treat_key();

                        Log.d(TAG, "x direction");
                        Log.d(TAG, reconciliation_function.int_array_to_string(f_L_Bob_x));
                        Log.d(TAG, reconciliation_function.int_array_to_string(f_key_Bob_x));

                        Log.d(TAG, "y direction");
                        Log.d(TAG,reconciliation_function.int_array_to_string(f_L_Bob_y));
                        Log.d(TAG, reconciliation_function.int_array_to_string(f_key_Bob_y));

                        Log.d(TAG, "z direction");
                        Log.d(TAG,reconciliation_function.int_array_to_string(f_L_Bob_z));
                        Log.d(TAG, reconciliation_function.int_array_to_string(f_key_Bob_z));

                        server_tv_3.append(reconciliation_function.int_array_to_string(f_L_Bob_x)+"\n");
                        server_tv_3.append(reconciliation_function.int_array_to_string(f_key_Bob_x)+"\n");

                        server_tv_3.append(reconciliation_function.int_array_to_string(f_L_Bob_y)+"\n");
                        server_tv_3.append(reconciliation_function.int_array_to_string(f_key_Bob_y)+"\n");

                        server_tv_3.append(reconciliation_function.int_array_to_string(f_L_Bob_z)+"\n");
                        server_tv_3.append(reconciliation_function.int_array_to_string(f_key_Bob_z)+"\n");

                        server_tv_1.append(DateUtil.getNowTime() + " Finish sampling");
                    } else if (sensor_sample_count < 750) {
                        acc_e[sensor_sample_count] = (double) accel_gl[0];
                        acc_n[sensor_sample_count] = (double) accel_gl[1];
                        acc_g[sensor_sample_count] = (double) accel_gl[2];
                    }
                }
                // copy new accelerometer data into accel array
                // then calculate new orientation
                System.arraycopy(event.values, 0, accel, 0, 3);
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                // process gyro data
                gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
        }

//        tv_value1.setText("azimuth：" +
//                Math.round(Math.toDegrees(fusedOrientation[0]) * 10f) / 10f + "\t\t||\t" +
//                Math.round(Math.toDegrees(accMagOrientation[0]) * 10f) / 10f);
//        tv_value2.setText("pitch：     " +
//                Math.round(Math.toDegrees(fusedOrientation[1]) * 10f) / 10f + "\t\t||\t" +
//                Math.round(Math.toDegrees(accMagOrientation[1]) * 10f) / 10f);
//        tv_value3.setText("roll：        " +
//                (Math.round(Math.toDegrees(fusedOrientation[2]) * 10f) / 10f) + "\t\t||\t" +
//                Math.round(Math.toDegrees(accMagOrientation[2]) * 10f) / 10f);

        // After find fusedOrientation[3], acc_gl can be estimated:
        float[] rot = getRotationMatrixFromOrientation(fusedOrientation);

        float accel_gl_x_raw = accel_gl[0] = accel[0] * rot[0] + accel[1] * rot[1] + accel[2] * rot[2];
        float accel_gl_y_raw = accel_gl[1] = accel[0] * rot[3] + accel[1] * rot[4] + accel[2] * rot[5];
        float accel_gl_z_raw = accel[0] * rot[6] + accel[1] * rot[7] + accel[2] * rot[8];

        // Then apply low pass filter to accel on z-axis:
        // A low-pass filter passes low-frequency signals/values and attenuates
        // (reduces the amplitude of) signals/values with frequencies higher than
        // the cutoff frequency.

        // To detect heel-strike, we ﬁrst apply a low-pass ﬁlter on acceleration along the
        // gravity direction to reduce noise.
        /* The cutoff frequency is chosen as 3Hz
           lpf_scaler = tau / (tau + t_PWM)
           FOR:
                tau = 1 / (2 * pi * cut_off_freq) = 0.05305164769
                t_PWM = 1 / freq_PWM = 0.01 (100 HZ)
           THUS:
                lpf_scaler = 0.8414f */
        // acceleration in earth/global frame after applying LPF:
        accel_gl[0] = 0.8414f * accel_gl[0] +  (1 - 0.8414f) * accel_gl_x_raw;
        accel_gl[1] = 0.8414f * accel_gl[1] +  (1 - 0.8414f) * accel_gl_y_raw;
        accel_gl[2] = 0.8414f * accel_gl[2] +  (1 - 0.8414f) * accel_gl_z_raw;

//        tv_value4.setText("accel_x：   " + Math.round(accel_gl[0] * 10f) / 10f);
//        tv_value5.setText("accel_y：   " + Math.round(accel_gl[1] * 10f) / 10f);
//        tv_value6.setText("accel_z：   " + Math.round(accel_gl[2] * 10f) / 10f);

        /*
        The peak of acceleration along the gravity direction indicates a heel-strike
        To detect peak, we find peak point Xn-2 which satisfy:
            "Zn_6 < Zn_5 < Zn_4 < Zn_3 > Zn_2 > Zn_1 > Zn" && Zn_3 > 9.81
        */
        Zn_6 = Zn_5;
        Zn_5 = Zn_4;
        Zn_4 = Zn_3;
        Zn_3 = Zn_2;
        Zn_2 = Zn_1;
        Zn_1 = Zn;
        Zn = accel_gl[2];

        if (Zn_6 < Zn_5 && Zn_5 < Zn_4 && Zn_4 < Zn_3
                && Zn_3 > Zn_2 && Zn_2 > Zn_1 && Zn_1 > Zn && Zn_3 > 11.2){
            if (System.currentTimeMillis() - t_peak > 250) {
                t_peak = System.currentTimeMillis();
                steps++;
            }
        }
//        else {
//            tv_value7.setText("Steps: " + steps);
//        }

    }

    public void extractBits() {
        int i;
        int j = 0;
        int k = 1;

        for (i=0; i < 70; i++) {
            if (bits_e[i] <= 1) {
                key_Bob_x[j] = bits_e[i];
                L_Bob_x[j] = k;

                j++;
            }
            k++;
        }

        j = 0;
        k = 1;
        for (i=0; i < 70; i++) {
            if (bits_n[i] <= 1) {
                key_Bob_y[j] = bits_n[i];
                L_Bob_y[j] = k;

                j++;
            }
            k++;
        }

        j = 0;
        k = 1;
        for (i=0; i < 70; i++) {
            if (bits_g[i] <= 1) {
                key_Bob_z[j] = bits_g[i];
                L_Bob_z[j] = k;

                j++;
            }
            k++;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void generateKey(){
        // copy 2000 LPF acc_g samples (start from 50 for avoiding initial sensor noise)
        // and get SGpoly filtered acc_g data
        CurveSmooth csm_g = new CurveSmooth(Arrays.copyOfRange(acc_g, 50, 750));
        csm_g.setSGpolyDegree(3);
        CurveSmooth csm_n = new CurveSmooth(Arrays.copyOfRange(acc_n, 50, 750));
        csm_n.setSGpolyDegree(3);
        CurveSmooth csm_e = new CurveSmooth(Arrays.copyOfRange(acc_e, 50, 750));
        csm_e.setSGpolyDegree(3);
        smoothed_acc_g = csm_g.savitzkyGolay(37);
        smoothed_acc_n = csm_n.savitzkyGolay(37);
        smoothed_acc_e = csm_e.savitzkyGolay(37);

        // get bits from smoothed curve
        bits_g = getBits(smoothed_acc_g); // z
        bits_n = getBits(smoothed_acc_n); // y
        bits_e = getBits(smoothed_acc_e); // x

//        // bits = bits_g + bits_n + bits_e
//        for (int i=0; i < 70; i++){
//            bits[i] = bits_g[i];
//        }
//        for (int i=70; i < 140; i++){
//            bits[i] = bits_n[i-70];
//        }
//        for (int i=140; i < 210; i++){
//            bits[i] = bits_e[i-140];
//        }

        // show key
//        for (int i=0; i < 210; i++) {
//            tv_value8.append("bit" + i + "：   " + bits[i]);
//            tv_value8.append("\n");
//        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int[] getBits(double[] smoothed_acc){
        // get 75 bits from 750 samples of data with a window size of 10
        double[] samplings = new double[70];
        for (int i = 0; i < 70; i++){
            samplings[i] = smoothed_acc[10 * i];
        }
        // quantization
        mean = Arrays.stream(samplings).average().orElse(Double.NaN);
        stdev = calculateSD(mean, samplings);
        upper_ts = mean + 0.9 * stdev;
        lower_ts = mean - 0.9 * stdev;
        // form bits as key
        int[] bits_= new int[70];
        for (int i = 0; i < 70; i++){
            if (samplings[i] > upper_ts){
                bits_[i] = 1;
            } else if (samplings[i] < lower_ts){
                bits_[i] = 0;
            } else {
                bits_[i] = 2;   // dropped
            }
        }
        return bits_;
    }

    public static double calculateSD(double mean, double[] numArray)
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;
        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.sqrt(standardDeviation/length);
    }

    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    public static final float EPSILON = 0.000000001f;

    private void getRotationVectorFromGyro
            (float[] gyroValues, float[] deltaRotationVector, float timeFactor)
    {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude = (float) Math.sqrt(gyroValues[0] * gyroValues[0] +
                gyroValues[1] * gyroValues[1] + gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private boolean initState = true;

    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if(initState) {
            float[] initMatrix;
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix;
        resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

            fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0]
                    + oneMinusCoeff * accMagOrientation[0];

            fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1]
                    + oneMinusCoeff * accMagOrientation[1];

            fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2]
                    + oneMinusCoeff * accMagOrientation[2];

            // overwrite gyro matrix and orientation with fused orientation
            // to comensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
                encryption new_encryption = new encryption(mykey, message);
                byte[] byte_message = new_encryption.encrypt();
                new Thread(new thread_udp_send(byte_message)).start();
            }
        }
        else if (v.getId() == R.id.server_sample_start){

            new Thread(new thread_timer()).start();
        }
    }


    class thread_timer implements Runnable {

        @Override
        public void run(){
            final int addition; //additional time added

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server_tv_1.append(
                            DateUtil.getNowTime() + " Prepare for sampling less than 1 minutes...\n"
                    );
                }
            });

            int minute = DateUtil.getNowMinute();

            if (DateUtil.getNowSecond() < 30){

                minute++;


                while (DateUtil.getNowMinute() != minute){
                    continue;
                }
            }
            else{
                while (DateUtil.getNowSecond() != 30){
                    continue;
                }
            }


            //server_tv_1.append(DateUtil.getNowTime() + " Start Sampling\n");
            //Toast.makeText(this, "start sampling", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sample start");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server_tv_1.append(DateUtil.getNowTime() + " Start Sampling\n");
                }
            });

            start_recording_flag = true;
            sensor_sample_count = 0;
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

                        ///////////////////////////////////////////////////////////////////////////////
                        /*name: L_ALice_x, L_Alice_y, L_ALice_z*/
                        int[] int_buf = new int[buf.length];
                        for (int i=0; i<buf.length; i++){
                            int_buf[i] = (int) buf[i];
                        }
                        // L_ALice_x:
                        int i = 0;
                        while(int_buf[i] != 71 && int_buf[i] != 72){
                            L_ALice_x[i] = int_buf[i];
                            i++;
                        }
                        i++;
                        // L_ALice_y:
                        int j = 0;
                        while(int_buf[i] != 71 && int_buf[i] != 72){
                            L_ALice_y[j] = int_buf[i];
                            j++;
                            i++;
                        }
                        i++;
                        // L_ALice_z:
                        j = 0;
                        while(int_buf[i] != 71 && int_buf[i] != 72){
                            L_ALice_z[j] = int_buf[i];
                            j++;
                            i++;
                        }
                        ///////////////////////////////////////////////////////////////////////////////

                        bob = new reconciliation_bob(
                                f_L_Bob_x, f_key_Bob_x,
                                f_L_Bob_y, f_key_Bob_y,
                                f_L_Bob_z, f_key_Bob_z,
                                L_ALice_x, L_ALice_y, L_ALice_z);

                        if (bob.decision()){

                            // if continue to connect, send the two meesages
                            byte[] first_message = bob.first_part_message();
                            new Thread(new thread_udp_send(first_message)).start();


                            byte[] second_message = bob.second_part_message();
                            new Thread(new thread_udp_send(second_message)).start();

                            mykey = bob.key_out();

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
                                    server_connection_status.setText("Pair fail");
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
                                server_tv_2.append(DateUtil.getNowTime() +"Receive: " + message + "\n");
                            }
                        });

                    }
                }

                // if break from the loop, then stop this thread
                Thread.currentThread().interrupt();

            } catch (Exception e) {
                Log.d(TAG, "Wrong client receiving thread");
                e.printStackTrace();
            }
        }
    }


    /*thread_udp_send
     * Task: send udp packet to the destinated server ip and port
     * Input: String message
     * note: here we do not need to implement encryption
     * the thread only accepts byte[]*/
    class thread_udp_send implements Runnable {

        private byte[] message;

        thread_udp_send(byte[] message) {
            this.message = message;
        }

        @Override
        public void run() {

            try {
                DatagramPacket packet = new DatagramPacket(
                        message,
                        message.length,
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
