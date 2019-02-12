package mnt2cc.com.octango;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;


import mnt2cc.com.octango.util.TranslateClass;
import mnt2cc.com.octango.util.TranslateInterface;

public class ClipboardListenerService extends Service {

    private final static String CHANNEL_ID = "CHANNEL_RESULT";
    private ClipboardManager cm;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        final ClipboardListenerService _this = this;
        cm.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener(){

            @Override
            public void onPrimaryClipChanged(){
                final CharSequence _text = cm.getPrimaryClip().getItemAt(0).getText();
                Log.d("[Octango]", "Hello1");
                if(_text == null){
                    return;
                }
                final String text = _text.toString();
                TranslateClass.getInstance().translate(text, TranslateClass.LANG_EN, TranslateClass.LANG_JA, new TranslateInterface() {
                    @Override
                    public void onTranslateDone(String result) {

                        if(result.equals("An error occurred") || result.equals("")) {
                            return;
                        }

                        Log.d("[Octango]", text);

                        String shown = text + "→" + result;

                        NotificationCompat.Action action1 = new NotificationCompat.Action(
                                R.drawable.add_icon,
                                _this.getString(R.string.add),
                                createIntent(FileSaveService.ACTION_REGISTER, text, result)
                        );
                        NotificationCompat.Action action2 = new NotificationCompat.Action(
                                R.drawable.clear_icon,
                                _this.getString(R.string.cancel),
                                createIntent(FileSaveService.ACTION_NOTHING, text, result)
                        );

                        NotificationCompat.Builder builder =
                                new NotificationCompat.Builder(_this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.add_icon)
                                .setContentTitle(_this.getString(R.string.notification_title))
                                .setContentText(shown)
                                .addAction(action1)
                                .addAction(action2);

                        NotificationManagerCompat manager = NotificationManagerCompat.from(_this);
                        manager.notify(0, builder.build());
                    }
                });
            }

        });

        return super.onStartCommand(intent, flags, startId);
    }

    private PendingIntent createIntent(String action, String source, String target){
        Intent intent = new Intent(this, FileSaveService.class);
        intent.setAction(action);
        intent.putExtra(FileSaveService.EXTRA_SOURCE, source);
        intent.putExtra(FileSaveService.EXTRA_TARGET, target);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        return pendingIntent;
    }
}
