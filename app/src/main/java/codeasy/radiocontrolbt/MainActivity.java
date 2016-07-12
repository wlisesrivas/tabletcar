package codeasy.radiocontrolbt;

import android.app.Instrumentation;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    Spinner devicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "This Devices does not support Bluetooth", Toast.LENGTH_LONG).show();
            System.exit(0);
        }

        devicesList = (Spinner) findViewById(R.id.devicesList);
        refreshDevices();
        // Start service
        Intent intent = new Intent(this, AppService.class);
        startService(intent);
    }

    public void refreshDevices() {
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

        // todo fill show dropdown with available bluetooth

        
        List<String> list = new ArrayList<String>();
        for (BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName());
            //adapter.add(bt.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list);
        devicesList.setAdapter(adapter);
        
    }

    public void testAction(View view) {
        // Do the test action click
        takeControl();
    }

    private void takeControl() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Instrumentation inst = new Instrumentation();
                try {
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
                    Thread.sleep(500);
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
                    Thread.sleep(500);
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP);
                    Thread.sleep(500);
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP);
                    Thread.sleep(500);
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PLAY);
                    Thread.sleep(500);
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_NEXT);
                    Thread.sleep(500);
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
