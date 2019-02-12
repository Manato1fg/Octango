package mnt2cc.com.octango;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.UUID;


public class FileSaveService extends IntentService {
    public static final String ACTION_REGISTER = "mnt2cc.com.octango.action.REGISTER";
    public static final String ACTION_NOTHING = "mnt2cc.com.octango.action.NOTHING";
    public static final String ACTION_READ = "mnt2cc.com.octango.action.READ";
    public static final String ACTION_DELETE = "mnt2cc.com.octango.action.READ";

    public static final String EXTRA_SOURCE = "mnt2cc.com.octango.extra.SOURCE_TEXT";
    public static final String EXTRA_TARGET = "mnt2cc.com.octango.extra.TARGET_TEXT";
    public static final String EXTRA_IMAGE = "mnt2cc.com.octango.extra.IMAGE";
    public static final String EXTRA_VIEW = "mnt2cc.com.octango.extra.VIEW";
    public static final String EXTRA_UUID = "mnt2cc.com.octango.extra.UUID";

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
                final String uuid = UUID.randomUUID().toString();
                final Bitmap image = (Bitmap) intent.getExtras().get(FileSaveService.EXTRA_IMAGE);
                this.appendData(source, target, image, uuid);
            } else if (ACTION_NOTHING.equals(action)) {
                return;
            } else if (ACTION_READ.equals(action)) {
                Bundle b = intent.getExtras();
                LinearLayout parent = (LinearLayout) b.get(EXTRA_VIEW);
                updateUI(parent);
            } else if (ACTION_READ.equals(action)) {
                final String uuid = intent.getStringExtra(FileSaveService.EXTRA_UUID);
                this.delete(uuid);
            }
        }
    }

    private void delete(String uuid){
        try{
            JSONArray jAry = read();
            for(int i = 0; i < jAry.length(); i++){
                JSONObject jsonObject = jAry.getJSONObject(i);
                if(jsonObject.getString("uuid").equals(uuid)){
                    jAry.remove(i);
                    break;
                }
            }
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
            JSONObject jObj = new JSONObject();
            jObj.put("UUID", uuid);
            jObj.put("source", source);
            jObj.put("target", target);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            jObj.put("bitmap", Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT));

            jAry.put(jObj);

            this.save(jAry);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private Bitmap loadImage(String base64encoded){
        byte[] bytes = Base64.decode(base64encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void updateUI(LinearLayout parent){
        try{
            JSONArray jAry = read();
            OctangoCardView v;
            String source, target, uuid;
            Bitmap bitmap;
            for(int i = 0; i < jAry.length(); i++){
                JSONObject jObj = jAry.getJSONObject(i);
                source = jObj.getString("source");
                target = jObj.getString("target");
                uuid = jObj.getString("UUID");
                bitmap = this.loadImage(jObj.getString("bitmap"));
                v = new OctangoCardView(this, source, target, uuid, bitmap);
                parent.addView(v);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private JSONArray read() throws JSONException{
        return new JSONArray(getSharedPreferences(KEY_DATA_FILE, MODE_PRIVATE).getString(KEY_DATA, ""));
    }
}
