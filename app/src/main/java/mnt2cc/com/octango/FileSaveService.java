package mnt2cc.com.octango;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.UUID;


public class FileSaveService extends IntentService {
    public static final String ACTION_REGISTER = "mnt2cc.com.octango.action.REGISTER";
    public static final String ACTION_NOTHING = "mnt2cc.com.octango.action.NOTHING";
    public static final String ACTION_READ = "mnt2cc.com.octango.action.READ";
    public static final String ACTION_DELETE = "mnt2cc.com.octango.action.DELETE";
    public static final String ACTION_GET = "mnt2cc.com.octango.action.GET";

    public static final String EXTRA_SOURCE = "mnt2cc.com.octango.extra.SOURCE_TEXT";
    public static final String EXTRA_TARGET = "mnt2cc.com.octango.extra.TARGET_TEXT";
    public static final String EXTRA_IMAGE = "mnt2cc.com.octango.extra.IMAGE";
    public static final String EXTRA_UUID = "mnt2cc.com.octango.extra.UUID";
    public static final String EXTRA_FROM_NOTIFICATION = "from.notification";

    public static final String KEY_DATA_FILE = "key.data.file";
    public static final String KEY_DATA = "key.data";

    public FileSaveService() {
        super("FileSaveService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REGISTER.equals(action)) {
                final String source = intent.getStringExtra(FileSaveService.EXTRA_SOURCE);
                final String target = intent.getStringExtra(FileSaveService.EXTRA_TARGET);
                final String uuid = intent.getStringExtra(FileSaveService.EXTRA_UUID).equals("") ? UUID.randomUUID().toString() : intent.getStringExtra(FileSaveService.EXTRA_UUID);
                if(!intent.getStringExtra(FileSaveService.EXTRA_UUID).equals("")){
                    this.delete(uuid);
                }
                final String image = intent.getStringExtra(FileSaveService.EXTRA_IMAGE);
                this.appendData(source, target, loadImage(image), uuid);

                Intent intent1 = new Intent()
                        .setAction(CardActivity.FinishSavingReceiver.ACTION);

                sendBroadcast(intent1);
            } else if (ACTION_NOTHING.equals(action)) {
                return;
            } else if (ACTION_READ.equals(action)) {
                updateUI();
            } else if (ACTION_DELETE.equals(action)) {
                final String uuid = intent.getStringExtra(FileSaveService.EXTRA_UUID);
                this.delete(uuid);
                Intent intent1 = new Intent()
                        .setAction(CardActivity.FinishSavingReceiver.ACTION);

                sendBroadcast(intent1);
            } else if (ACTION_GET.equals(action)){
                final String uuid = intent.getStringExtra(FileSaveService.EXTRA_UUID);
                JSONObject jObj = this.get(uuid);
                if(jObj == null){
                    Intent intent1 = new Intent()
                            .setAction(CardActivity.ErrorReceiver.ACTION);
                    sendBroadcast(intent1);
                    return;
                }else{
                    try{
                        String source = jObj.getString(EXTRA_SOURCE);
                        String target = jObj.getString(EXTRA_TARGET);
                        String encoded = jObj.getString(EXTRA_IMAGE);

                        Intent intent1 = new Intent()
                                .setAction(CardActivity.SearchReceiver.ACTION)
                                .putExtra(EXTRA_SOURCE, source)
                                .putExtra(EXTRA_TARGET, target)
                                .putExtra(EXTRA_IMAGE, encoded);
                        sendBroadcast(intent1);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            if(intent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)){
                NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
                nm.cancel(ClipboardListenerService.ID);
            }
        }
    }

    private JSONObject get(String uuid){
        try{
            JSONArray jAry = read();
            for(int i = 0; i < jAry.length(); i++){
                JSONObject jsonObject = jAry.getJSONObject(i);
                if(jsonObject.getString(EXTRA_UUID).equals(uuid)){
                    return jsonObject;
                }
            }
            return null;
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void delete(String uuid){
        try{
            JSONArray jAry = read();
            for(int i = 0; i < jAry.length(); i++){
                JSONObject jsonObject = jAry.getJSONObject(i);
                if(jsonObject.getString(EXTRA_UUID).equals(uuid)){
                    jAry.remove(i);
                    break;
                }
            }
            this.save(jAry);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void save(JSONArray jsonArray) {
        SharedPreferences sp = getSharedPreferences(KEY_DATA_FILE, MODE_PRIVATE);
        sp.edit().putString(KEY_DATA, jsonArray.toString()).apply();
    }

    private void appendData(String source, String target, Bitmap bitmap, String uuid){

        try{
            JSONArray jAry = read();
            JSONObject jObj = new JSONObject()
                    .put(EXTRA_SOURCE, source)
                    .put(EXTRA_TARGET, target)
                    .put(EXTRA_UUID, uuid)
                    .put(EXTRA_IMAGE, base64encode(bitmap));

            jAry = jAry.put(jObj);
            this.save(jAry);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public static String base64encode(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encoded = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        return encoded;
    }

    public static Bitmap loadImage(String encoded){
        byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void updateUI(){
        try{
            JSONArray jAry = read();
            if (jAry.length() == 0){
                Intent intent = new Intent().setAction(MainActivity.EmptyReceiver.ACTION);
                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                return;
            }
            String source, target, uuid, encoded;
            for(int i = 0; i < jAry.length(); i++){
                JSONObject jObj = jAry.getJSONObject(i);
                source = jObj.getString(EXTRA_SOURCE);
                target = jObj.getString(EXTRA_TARGET);
                uuid = jObj.getString(EXTRA_UUID);
                encoded = jObj.getString(EXTRA_IMAGE);

                Intent intent = new Intent()
                        .setAction(MainActivity.AddListReceiver.ACTION)
                        .putExtra(EXTRA_SOURCE, source)
                        .putExtra(EXTRA_TARGET, target)
                        .putExtra(EXTRA_UUID, uuid)
                        .putExtra(EXTRA_IMAGE, encoded);
                sendBroadcast(intent);
            }
        }catch (JSONException e){
            JSONArray jsonArray = new JSONArray();
            this.save(jsonArray);
            Intent intent = new Intent().setAction(MainActivity.EmptyReceiver.ACTION);
            sendBroadcast(intent);
        }finally {
            Intent intent = new Intent().setAction(MainActivity.FinishReceiver.ACTION);
            sendBroadcast(intent);
        }
    }

    private JSONArray read() throws JSONException{
        return new JSONArray(getSharedPreferences(KEY_DATA_FILE, MODE_PRIVATE).getString(KEY_DATA, ""));
    }
}
