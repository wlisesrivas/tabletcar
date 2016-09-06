package codeasy.tabletcar;

import android.app.Instrumentation;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by wlisesrivas on 7/3/2016.
 */
public class AppService extends IntentService {

    Thread thread;
    Instrumentation inst;
    private static ConnectThread connectThread;

    public AppService() {
        super("TabletCar");
        inst = new Instrumentation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("DOBLE U", "Servicio iniciadooooo");
        if(connectThread == null) {
            connectThread = new ConnectThread(MainActivity.device, MainActivity.mBluetoothAdapter, this);
            connectThread.start();
        }
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("DOBLE U", "Servicio handle intent");
    }

}
