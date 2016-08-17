package codeasy.radiocontrolbt;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import tw.com.prolific.driver.pl2303.PL2303Driver;

public class MainActivity extends AppCompatActivity {

    private Intent intent;

    private Button btnConnect;
    private TextView lblOutput;
    private static final String ACTION_USB_PERMISSION = "codeasy.radiocontrolbt.USB_PERMISSION";
    private PL2303Driver.BaudRate mBaudrate = PL2303Driver.BaudRate.B9600;
    private PL2303Driver mSerial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        lblOutput = (TextView) findViewById(R.id.lblOutput);

        // get service
        mSerial = new PL2303Driver((UsbManager) getSystemService(Context.USB_SERVICE),
                this, ACTION_USB_PERMISSION);

        // check USB host function.
        if (!mSerial.PL2303USBFeatureSupported()) {
            msg("No Support USB host API");
            mSerial = null;
            return;
        }

        // Start service
//        intent = new Intent(this, AppService.class);
//        startService(intent);
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

    /**
     * Toast message.
     * @param str
     */
    public void msg(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public void connectAction(View view) {
        btnConnect.setText("Connecting...");

        if(mSerial.isConnected()) {
            if (!mSerial.InitByBaudRate(mBaudrate, 700)) {
                if(!mSerial.PL2303Device_IsHasPermission()) {
                    msg("cannot open, maybe no permission");
                }
                if(mSerial.PL2303Device_IsHasPermission() && (!mSerial.PL2303Device_IsSupportChip())) {
                    msg("cannot open, maybe this chip has no support, please use PL2303HXD / RA / EA chip.");
                }
            } else {
                msg("Connected ...");
                btnConnect.setText("[Disconnect]");
                return;
            }

        } else {
            msg("Error trying to connect");
            btnConnect.setText("Connect");
        }
    }

    private void readDataFromSerial() {
        int len;
        byte[] rbuf = new byte[20];
        StringBuffer sbHex=new StringBuffer();
        if(null==mSerial)
            return;
        if(!mSerial.isConnected())
            return;
        len = mSerial.read(rbuf);
        if(len<0) {
            return;
        }

        if (len > 0) {
            for (int j = 0; j < len; j++) {
                sbHex.append((char) (rbuf[j] & 0x000000FF));
            }
            lblOutput.setText(sbHex.toString());
            Toast.makeText(this, "len="+len, Toast.LENGTH_SHORT).show();
        }
        else {
            lblOutput.setText("[empty]");
            return;
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void writeDataToSerial(String strWrite) {
        if(null==mSerial)
            return;
        if(!mSerial.isConnected())
            return;

        int res = mSerial.write(strWrite.getBytes(), strWrite.length());
        if( res<0 )
            return;

        msg("Write length: "+strWrite.length()+" bytes");
    }

    @Override
    protected void onDestroy() {
        msg("Enter onDestroy");
        if(mSerial!=null) {
            mSerial.end();
            mSerial = null;
        }
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
        //if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
        if(!mSerial.isConnected()) {
            msg("New instance : " + mSerial);
            if( !mSerial.enumerate() ) {
                msg("no more devices found");
                return;
            } else {
                msg("onResume:enumerate succeeded!");
            }
        }//if isConnected
        msg("attached");
    }
}
