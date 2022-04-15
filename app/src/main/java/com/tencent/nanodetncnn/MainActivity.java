// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

package com.tencent.nanodetncnn;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends Activity implements SurfaceHolder.Callback
{
    public static final int REQUEST_CAMERA = 100;

    private NanoDetNcnn nanodetncnn = new NanoDetNcnn();
    private int facing = 0;

    private Spinner spinnerModel;
    private Spinner spinnerCPUGPU;
    private int current_model = 0;
    private int current_cpugpu = 0;

    private SurfaceView cameraView;
    //------
    private static final String TAG = "MainActivity";
    public static final boolean D = BuildConfig.DEBUG; // This is automatically set when building

    public static Activity activity;
    public static Context context;
    private static Toast mToast;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_DISCONNECTED = 4;
    public static final int MESSAGE_RETRY = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothHandler mBluetoothHandler = null;
    // Member object for the chat services
    public static BluetoothChatService mChatService = null;


    private BluetoothDevice btDevice; // The BluetoothDevice object 蓝牙设备
    private boolean btSecure; // If it's a new device we will pair with the device
    public static boolean stopRetrying;

    public static int currentTabSelected;

    public static String accValue = "";
    public static String gyroValue = "";
    public static String kalmanValue = "";
    public static boolean newIMUValues;

    public static String Qangle = "";
    public static String Qbias = "";
    public static String Rmeasure = "";
    public static boolean newKalmanValues;

    public static String pValue = "";
    public static String iValue = "";
    public static String dValue = "";
    public static String targetAngleValue = "";
    public static boolean newPIDValues;

    public static boolean backToSpot;
    public static int maxAngle = 8; // Eight is the default value
    public static int maxTurning = 20; // Twenty is the default value

    public static String appVersion;
    public static String firmwareVersion;
    public static String eepromVersion;
    public static String mcu;
    public static boolean newInfo;

    public static String batteryLevel;
    public static double runtime;
    public static boolean newStatus;

    public static boolean pairingWithDevice;

    public static boolean buttonState;
    public static boolean joystickReleased;

    public final static String getPIDValues = "GP;";
    public final static String getSettings = "GS;";
    public final static String getInfo = "GI;";
    public final static String getKalman = "GK;";

    public final static String setPValue = "SP,";
    public final static String setIValue = "SI,";
    public final static String setDValue = "SD,";
    public final static String setKalman = "SK,";
    public final static String setTargetAngle = "ST,";
    public final static String setMaxAngle = "SA,";
    public final static String setMaxTurning = "SU,";
    public final static String setBackToSpot = "SB,";

    public final static String imuBegin = "IB;";
    public final static String imuStop = "IS;";

    public final static String statusBegin = "RB;";
    public final static String statusStop = "RS;";

    public final static String sendStop = "CS,;";
    public final static String sendIMUValues = "CM,";
    public final static String sendJoystickValues = "CJ,";
    public final static String sendPairWithWii = "CPW;";
    public final static String sendPairWithPS4 = "CPP;";

    public final static String restoreDefaultValues = "CR;";

    public final static String responsePIDValues = "P";
    public final static String responseKalmanValues = "K";
    public final static String responseSettings = "S";
    public final static String responseInfo = "I";
    public final static String responseIMU = "V";
    public final static String responseStatus = "R";
    public final static String responsePairConfirmation = "PC";

    public final static int responsePIDValuesLength = 5;
    public final static int responseKalmanValuesLength = 4;
    public final static int responseSettingsLength = 4;
    public final static int responseInfoLength = 4;
    public final static int responseIMULength = 4;
    public final static int responseStatusLength = 3;
    public final static int responsePairConfirmationLength = 1;
    //---------

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraView = (SurfaceView) findViewById(R.id.cameraview);

        cameraView.getHolder().setFormat(PixelFormat.RGBA_8888);
        cameraView.getHolder().addCallback(this);

        Button buttonSwitchCamera = (Button) findViewById(R.id.buttonSwitchCamera);
        buttonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                int new_facing = 1 - facing;

                nanodetncnn.closeCamera();

                nanodetncnn.openCamera(new_facing);

                facing = new_facing;
            }
        });
        Button b=(Button)findViewById(R.id.buttonobjinfo);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView text_height=(TextView)findViewById(R.id.objText);
                int[]a=nanodetncnn.getObjInfo(0,4);
                text_height.setText("矩阵  x:"+a[0]+" y:"+a[1]+" h:"+a[2]+" w:"+a[3]);
                Log.e("TAG", "on: "+ a[0] );
                Log.e("TAG", "on: "+ a[3] );
                String stop_command = "GMC,w,0;";
                String spin = "GMC,a,60;";
                String foward = "GMC,w,60;";
                Log.e("TAG", "on: "+ foward );
//                MainActivity.mChatService.write(foward);
                if(a[0]!=-1&&a[0]!=0)
                {
                    if(a[0]+a[3]/2 < 180)
                    {
                        String msg1 = "GMC,a,60;";
                        Log.e("TAG", "onpClick: "+ msg1 );
                        MainActivity.mChatService.write(msg1);
                    }
                    else if(a[0]+a[3]/2 > 180)
                    {
                        String msg2 = "GMC,d,60;";
                        Log.e("TAG", "onpClick: "+ msg2 );
                        MainActivity.mChatService.write(msg2);
                    }
                    a=nanodetncnn.getObjInfo(0,4);
                    while((a[0]+a[3]/2) <= 160||(a[0]+a[3]/2) >=200)
                    {
                        Log.e("TAG", "onpspin: "+ a[0] );
                        Log.e("TAG", "onpspin: "+ a[3] );
//                      MainActivity.mChatService.write(stop_command);
                        a=nanodetncnn.getObjInfo(0,4);
                    }
                    MainActivity.mChatService.write(stop_command);
                    a=nanodetncnn.getObjInfo(0,4);
                    MainActivity.mChatService.write(foward);
                    while((a[0]+a[3])>=10&&a[0]<=320)
                    {
//                      MainActivity.mChatService.write(stop_command);
                        a=nanodetncnn.getObjInfo(0,4);
                        Log.e("TAG", "onphead: "+ a[0] );
                        Log.e("TAG", "onphead: "+ a[3] );
                    }
                    MainActivity.mChatService.write(stop_command);
                }
                else
                {
                    MainActivity.mChatService.write(spin);
                    a=nanodetncnn.getObjInfo(0,4);
                    while(a[0]==-1||a[0]==0)
                    {
//                      MainActivity.mChatService.write(spin);
                        a=nanodetncnn.getObjInfo(0,4);
                    }
                    MainActivity.mChatService.write(stop_command);
                    a=nanodetncnn.getObjInfo(0,4);
                    if(a[0]!=-1&&a[0]!=0)
                    {
                        if(a[0]+a[3]/2 < 180)
                        {
                            String msg1 = "GMC,a,60;";
                            Log.e("TAG", "on: "+ msg1 );
                            MainActivity.mChatService.write(msg1);
                        }
                        else if(a[0]+a[3]/2 > 180)
                        {
                            String msg2 = "GMC,d,60;";
                            MainActivity.mChatService.write(msg2);
                        }
                        a=nanodetncnn.getObjInfo(0,4);
                        while((a[0]+a[3]/2) <= 160||(a[0]+a[3]/2) >200)
                        {
//                        MainActivity.mChatService.write(stop_command);
                            a=nanodetncnn.getObjInfo(0,4);
                        }
                        MainActivity.mChatService.write(stop_command);
                        MainActivity.mChatService.write(foward);
                        a=nanodetncnn.getObjInfo(0,4);
                        while((a[0]+a[3])>=10&&a[0]<=320)
                        {
//                        MainActivity.mChatService.write(stop_command);
                            a=nanodetncnn.getObjInfo(0,4);
                        }
                        MainActivity.mChatService.write(stop_command);
                    }
                }
                // *********************蓝牙传递*********************
//                String message = MainActivity.sendJoystickValues + 'w' + ',' + a[1] + ";";
//                MainActivity.mChatService.write(message);
            }
        });
        spinnerModel = (Spinner) findViewById(R.id.spinnerModel);
        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                if (position != current_model)
                {
                    current_model = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

        spinnerCPUGPU = (Spinner) findViewById(R.id.spinnerCPUGPU);
        spinnerCPUGPU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                if (position != current_cpugpu)
                {
                    current_cpugpu = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

        reload();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        else
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            showToast("Bluetooth is not available", Toast.LENGTH_LONG);
            finish();
            return;
        }

        mBluetoothHandler = new BluetoothHandler(this);
        mChatService = new BluetoothChatService(mBluetoothHandler, mBluetoothAdapter);
        findViewById(R.id.menu_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

            }
        });
    }

    private void reload()
    {
        boolean ret_init = nanodetncnn.loadModel(getAssets(), current_model, current_cpugpu);
        if (!ret_init)
        {
            Log.e("MainActivity", "nanodetncnn loadModel failed");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        nanodetncnn.setOutputWindow(holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        nanodetncnn.openCamera(facing);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        nanodetncnn.closeCamera();
    }


    // The Handler class that gets information back from the BluetoothChatService
    // 主活动信息处理器，处理蓝牙返回数据
    static class BluetoothHandler extends Handler
    {
        private final MainActivity mMainActivity;
        private String mConnectedDeviceName; // Name of the connected device

        BluetoothHandler(MainActivity mMainActivity)
        {
            this.mMainActivity = mMainActivity;
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
//                    mMainActivity.supportInvalidateOptionsMenu();
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            MainActivity.showToast(mMainActivity.getString(R.string.connected_to) + " " + mConnectedDeviceName, Toast.LENGTH_SHORT);
                            if (mChatService == null)
                                return;
                            Handler mHandler = new Handler();
                            mHandler.postDelayed(new Runnable()
                            {
                                public void run()
                                {
                                    mChatService.write(getPIDValues + getSettings + getInfo + getKalman);
                                }
                            }, 1000); // Wait 1 second before sending the message
//                            if (GraphFragment.mToggleButton != null) {
//                                if (GraphFragment.mToggleButton.isChecked() && checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
//                                    mHandler.postDelayed(new Runnable()
//                                    {
//                                        public void run()
//                                        {
//                                            mChatService.write(imuBegin); // Request data
//                                        }
//                                    }, 1000); // Wait 1 second before sending the message
//                                } else {
//                                    mHandler.postDelayed(new Runnable()
//                                    {
//                                        public void run()
//                                        {
//                                            mChatService.write(imuStop); // Stop sending data
//                                        }
//                                    }, 1000); // Wait 1 second before sending the message
//                                }
//                            }
//                            if (checkTab(ViewPagerAdapter.INFO_FRAGMENT)) {
//                                mHandler.postDelayed(new Runnable()
//                                {
//                                    public void run()
//                                    {
//                                        mChatService.write(statusBegin); // Request data
//                                    }
//                                }, 1000); // Wait 1 second before sending the message
//                            }
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                    }
//                    PIDFragment.updateButton();
                    break;
//                case MESSAGE_READ:
//                    if (newPIDValues) {
//                        newPIDValues = false;
//                        PIDFragment.updateView();
//                    }
//                    if (newInfo || newStatus) {
//                        newInfo = false;
//                        newStatus = false;
//                        InfoFragment.updateView();
//                    }
//                    if (newIMUValues) {
//                        newIMUValues = false;
//                        GraphFragment.updateIMUValues();
//                    }
//                    if (newKalmanValues) {
//                        newKalmanValues = false;
//                        GraphFragment.updateKalmanValues();
//                    }
//                    if (pairingWithDevice) {
//                        pairingWithDevice = false;
//                        MainActivity.showToast("Now enable discovery of your device", Toast.LENGTH_LONG);
//                    }
//                    break;
                case MESSAGE_DEVICE_NAME:
                    // Save the connected device's name
                    if (msg.getData() != null)
                        mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    break;
//                case MESSAGE_DISCONNECTED:
//                    mMainActivity.supportInvalidateOptionsMenu();
//                    PIDFragment.updateButton();
//                    if (msg.getData() != null)
//                        MainActivity.showToast(msg.getData().getString(TOAST), Toast.LENGTH_SHORT);
//                    break;
                case MESSAGE_RETRY:
                    if (D)
                        Log.d(TAG, "MESSAGE_RETRY");
                    mMainActivity.connectDevice(null, true);
                    break;
            }
        }
    }

    // 监听蓝牙服务事项
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect to
                if (resultCode == Activity.RESULT_OK)
                    connectDevice(data, false);
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK)
                    setupBTService(); // Bluetooth is now enabled, so set up a chat session
                else {
                    // User did not enable Bluetooth or an error occured
                    if (D)
                        Log.d(TAG, "BT not enabled");
                    showToast(getString(R.string.bt_not_enabled_leaving), Toast.LENGTH_SHORT);
                    finish();
                }
        }
    }

    public static void showToast(String message, int duration)
    {
        if (duration != Toast.LENGTH_SHORT && duration != Toast.LENGTH_LONG)
            throw new IllegalArgumentException();
        if (mToast != null)
            mToast.cancel(); // Close the toast if it's already open
        mToast = Toast.makeText(context, message, duration);
        mToast.show();
    }

    private void setupBTService()
    {
        if (mChatService != null)
            return;

        if (D)
            Log.d(TAG, "setupBTService()");
        if (mBluetoothHandler == null)
            mBluetoothHandler = new BluetoothHandler(this);
        mChatService = new BluetoothChatService(mBluetoothHandler, mBluetoothAdapter); // Initialize the BluetoothChatService to perform Bluetooth connections
    }
    private void connectDevice(Intent data, boolean retry)
    {
        if (retry) {
            if (btDevice != null && !stopRetrying) {
                mChatService.start(); // This will stop all the running threads
                mChatService.connect(btDevice, btSecure); // Attempt to connect to the device
            }
        } else { // It's a new connection
            stopRetrying = false;
            mChatService.newConnection = true;
            mChatService.start(); // This will stop all the running threads
            if (data.getExtras() == null)
                return;

            // 获取设备蓝牙地址
            String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS); // Get the device Bluetooth address

            btSecure = data.getExtras().getBoolean(DeviceListActivity.EXTRA_NEW_DEVICE); // If it's a new device we will pair with the device
            btDevice = mBluetoothAdapter.getRemoteDevice(address); // Get the BluetoothDevice object
            Log.d(TAG, "connectDevice: " +btSecure +btDevice.getName() +  address);

            mChatService.nRetries = 0; // Reset retry counter
            mChatService.connect(btDevice, btSecure); // Attempt to connect to the device
            showToast(getString(R.string.connecting), Toast.LENGTH_SHORT);

            String msg = "GMC,s,80;";
            MainActivity.mChatService.write(msg);
        }
    }

}
