package mnt2cc.com.octango;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import mnt2cc.com.octango.util.TranslateClass;
import mnt2cc.com.octango.util.TranslateInterface;

public class ClipboardListenerService extends Service {

    private final static String CHANNEL_ID = "CHANNEL_RESULT";
    public final static int ID = 0x2cc;
    private ClipboardManager cm;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        this.createChannel();
        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener(){

            @Override
            public void onPrimaryClipChanged(){
                if(cm.getPrimaryClip() != null){
                    if (cm.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || cm.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)){
                        CharSequence _text = cm.getPrimaryClip().getItemAt(0).getText();

                        if(_text == null) return;

                        String text = _text.toString();
                        new TranslateClass(text, TranslateClass.LANG_EN, TranslateClass.LANG_JA, new TranslateInterface() {
                            @Override
                            public void onTranslateDone(String source, String target) {

                                if(target.equals("")) return;

                                notification(source, target);
                            }
                        }).execute(0);
                    }
                }
            }

        });
    }

    private String DEFAULT_IMAGE(){
        BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(R.drawable.no_image);
        return FileSaveService.base64encode(bd.getBitmap());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return super.onStartCommand(intent, flags, startId);
    }

    private PendingIntent createIntent(String action, String source, String target){
        Intent intent = new Intent(getApplicationContext(), FileSaveService.class)
                .setAction(action)
                .putExtra(FileSaveService.EXTRA_SOURCE, source)
                .putExtra(FileSaveService.EXTRA_TARGET, target)
                .putExtra(FileSaveService.EXTRA_UUID, "")
                .putExtra(FileSaveService.EXTRA_IMAGE, DEFAULT_IMAGE())
                .putExtra(FileSaveService.EXTRA_FROM_NOTIFICATION, true);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private void notification(String source, String target){
        String shown = source + "→" + target;

        //Toast.makeText(getApplicationContext(), shown, Toast.LENGTH_LONG).show();

        NotificationCompat.Action action1 = new NotificationCompat.Action(
                R.drawable.add_icon,
                getString(R.string.add),
                createIntent(FileSaveService.ACTION_REGISTER, source, target)
        );
        NotificationCompat.Action action2 = new NotificationCompat.Action(
                R.drawable.clear_icon,
                getString(R.string.cancel),
                createIntent(FileSaveService.ACTION_NOTHING, source, target)
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.add_icon)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(shown)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .addAction(action1)
                        .addAction(action2);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(ID, builder.build());
        Log.d("[Octango Debugger]", shown);
    }

    private void createChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.notification_title), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.notification_description));
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }
}
