package mnt2cc.com.octango;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootStarter extends BroadcastReceiver {

    //Automatically start the service.
    @Override
    public void onReceive(Context context, Intent arg){
        Intent intent = new Intent(context, ClipboardListenerService.class);
        context.startService(intent);
    }
}
