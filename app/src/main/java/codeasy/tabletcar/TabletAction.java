package codeasy.tabletcar;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.KeyEvent;

/**
 * Created by wlisesrivas on 8/17/2016.
 */
public class TabletAction {

    private static final String CAMERA_APP = "com.easycap.viewer";
    private static final Instrumentation inst = new Instrumentation();

    /**
     * Take action with the command.
     * @param cmd
     */
    public static void handler(String cmd, Context context) {
        cmd = cmd.trim();
        switch (cmd){
            case "n": // Next
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_NEXT);
                break;
            case "p": // Prev
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                break;
            case "pl": // Play
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PLAY);
                break;
            case "pp": // Play - Plause
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                break;
            case "vu": // Volume Up
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP);
                break;
            case "vd": // Volume Down
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
                break;
            case "co": // Camera Open
                openCamera(context, false);
                break;
            case "cc": // Camera Close
                openCamera(context, true);
                break;
        }
    }

    private static void openCamera(Context context, boolean close) {
        // camera app

        // todo close camera app

        if(close == false) {
            PackageManager manager = context.getPackageManager();
            Intent i = manager.getLaunchIntentForPackage(CAMERA_APP);
            if (i == null) {
                return ;
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
        }
    }
}
