package codeasy.radiocontrolbt;

import android.app.Instrumentation;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by wlisesrivas on 7/3/2016.
 */
public class AppService extends IntentService implements Runnable {

    Thread thread;
    Instrumentation inst;

    public AppService() {
        super("TabletCar");
        inst = new Instrumentation();
        thread = new Thread(this);
//        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        thread.start();
    }

    @Override
    public void run() {
//        MainActivity.getInstance().msg("El Servicio ha iniciado");
//        while(true) {        }
    }
}
