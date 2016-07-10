package codeasy.radiocontrolbt;

import android.app.Instrumentation;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by wlisesrivas on 7/3/2016.
 */
public class AppService extends IntentService {

    Instrumentation inst;

    public AppService() {
        super("RadioControlBT");
        inst = new Instrumentation();
//        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("RadioControl ::", "El Servicio ha iniciado");
        while(true) {
            try {
                Log.i("RadioControl ::", "Servicio corriendo...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
