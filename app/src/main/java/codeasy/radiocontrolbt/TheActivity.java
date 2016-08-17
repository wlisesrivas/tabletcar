package codeasy.radiocontrolbt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TheActivity extends Activity {

    private static final int RESULT_SETTINGS = 1;
    private static String logtag = "TabletCar";

    private String moduleName;
    private String moduleAddress;

    private Handler h;

    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<String> mArrayAdapterName = new ArrayList<String>();
    private ArrayList<String> mArrayAdapterAddress = new ArrayList<String>();

    final int RECIEVE_MESSAGE = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    private ConnectedThread mConnectedThread;
    private final String defaultModuleName = "WilisesMobile";
    private boolean isConnected;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readModuleName();

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:
                        byte[] readBuf = (byte[]) msg.obj;
                        if (msg.arg1 > 0) {
                            String answer = bytesToHexString(readBuf);
                            answer = answer.substring(0, msg.arg1 * 2);
                            Log.d(logtag, "Answer: " + answer);
                        }
                        Log.d(logtag, "...String:" + sb.toString() + "Byte:" + msg.arg1 + "...");
                        break;
                }
            }

        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        connectTo();
        connectToModule();

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
    }

    // Establish the connection
    private void connectTo() {
        Log.d(logtag, "Connect Button");

        if (btAdapter != null && btAdapter.isEnabled()) {
            pairedDevices = btAdapter.getBondedDevices();

            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    mArrayAdapterName.add(device.getName());
                    mArrayAdapterAddress.add(device.getAddress());
                }
            }

            if (!mArrayAdapterName.contains(moduleName)) {
                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }
                btAdapter.startDiscovery();

                // Register the BroadcastReceiver
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
            } else {
                int index = mArrayAdapterName.indexOf(moduleName);
                moduleAddress = mArrayAdapterAddress.get(index);
                connectToModule();
            }

        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(logtag, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Throwable e) {
                Log.e(logtag, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }


    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void readModuleName() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        moduleName = sharedPrefs.getString("moduleID", defaultModuleName);
    }

    /**
     * Bytes to Hexadecimal.
     * @param bytes
     * @return
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    protected void connectToModule() {
        Log.d(logtag, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(moduleAddress);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (Throwable e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive. Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(logtag, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(logtag, "....Connection ok...");
        } catch (Throwable e) {
            try {
                btSocket.close();
            } catch (Throwable e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(logtag, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        if (btSocket != null) {
            isConnected = true;
        }
    }


    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView

                if (device.getName().equals(moduleName)) {
                    moduleAddress = device.getAddress();
                    btAdapter.cancelDiscovery();
                    connectToModule();
                }

            }
        }
    };

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (Throwable e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];    // buffer store for the stream
            int bytes;                            // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (Throwable e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void writeBytes(byte[] message) {
            Log.d(logtag, "...Data to send: " + message + "...");
            try {
                mmOutStream.write(message);
            } catch (Throwable e)            //IOException e
            {
                Log.d(logtag, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                readModuleName();
                break;
        }
    }


    @Override
    protected void onStart() //activity is started and visible to the user
    {
        Log.d(logtag, "onStart() called");
        super.onStart();
    }

    @Override
    protected void onResume() //activity was resumed and is visible again
    {
        Log.d(logtag, "onResume() called");

        if (isConnected) // RECONNECT ON RESUME
        {
            // Set up a pointer to the remote node using it's address.
            BluetoothDevice device = btAdapter.getRemoteDevice(moduleAddress);

            // Two things are needed to make a connection:
            //   A MAC address, which we got above.
            //   A Service ID or UUID.  In this case we are using the
            //     UUID for SPP.

            try {
                btSocket = createBluetoothSocket(device);
            } catch (Throwable e) {
                errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
            }

            // Discovery is resource intensive.  Make sure it isn't going on
            // when you attempt to connect and pass your message.
            btAdapter.cancelDiscovery();

            // Establish the connection.  This will block until it connects.
            Log.d(logtag, "...Connecting...");
            try {
                btSocket.connect();
                Log.d(logtag, "....Connection ok...");
            } catch (Throwable e) {
                try {
                    btSocket.close();
                } catch (Throwable e2) {
                    errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                }
            }

            // Create a data stream so we can talk to server.
            Log.d(logtag, "...Create Socket...");

            mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();

        }
        super.onResume();
    }

    @Override
    protected void onPause() //device goes to sleep or another activity appears
    {
        Log.d(logtag, "onPause() called");//another activity is currently running (or user has pressed Home)
        if (btAdapter != null) {
            if (btAdapter.isDiscovering()) {
                unregisterReceiver(mReceiver);
                btAdapter.cancelDiscovery();
            }
        }

        try {
            if (btSocket != null) {
                btSocket.close();
            }
        } catch (Throwable t) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + t.getMessage() + ".");
        }
        super.onPause();
    }

    @Override
    protected void onStop() //the activity is not visible anymore
    {
        Log.d(logtag, "onStop() called");
        if (btAdapter != null) {
            if (btAdapter.isDiscovering()) {
                unregisterReceiver(mReceiver);
                btAdapter.cancelDiscovery();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() //android has killed this activity
    {
        Log.d(logtag, "onDestroy() called");
        if (btAdapter != null) {
            if (btAdapter.isDiscovering()) {
                unregisterReceiver(mReceiver);
                btAdapter.cancelDiscovery();
            }
        }
        super.onDestroy();
    }
}