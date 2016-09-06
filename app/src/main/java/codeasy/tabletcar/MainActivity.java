package codeasy.tabletcar;

import android.app.Instrumentation;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothDevice device;
    Set<BluetoothDevice> pairedDevices;
    private Intent intent;
    Spinner devicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            msg("This Devices does not support Bluetooth");
            System.exit(0);
        }

        devicesList = (Spinner) findViewById(R.id.devicesList);
        devicesList.setOnItemSelectedListener(this);
        refreshDevices(null);
    }

    public void startServiceReady() {
        startService(new Intent(this, AppService.class));
    }

    public void refreshDevices(View view) {
        // enable bluetooth if not
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() == 0) {
            Toast.makeText(this, "There are no paired devices.", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> list = new ArrayList<String>();
        for (BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list);
        devicesList.setAdapter(adapter);

    }

    /**
     * Toast message.
     * @param str
     */
    public void msg(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selected = adapterView.getItemAtPosition(i).toString();
        for (BluetoothDevice bt : mBluetoothAdapter.getBondedDevices()) {
            if(bt.getName().equals(selected)) {
                device = bt;
                break;
            }
        }
        if(device == null) return;
        // Connect to device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if(!device.createBond())
                msg("Error trying to connect, not devices bond");
        }

        msg(String.format("Connected to [%s].", selected));
        startServiceReady();
//        new ConnectThread(device, mBluetoothAdapter, this).start();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
