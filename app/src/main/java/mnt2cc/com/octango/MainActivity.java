package mnt2cc.com.octango;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.graphics.Typeface;

import android.net.Uri;

public class MainActivity extends AppCompatActivity {

    private Uri mCropImageUri = null;

    private AddListReceiver addListReceiver = new AddListReceiver();
    private EmptyReceiver emptyReceiver = new EmptyReceiver();
    private FinishReceiver finishReceiver = new FinishReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Service開始

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ClipboardListenerService.class.getName().equals(serviceInfo.service.getClassName())) {
                // 実行中なら起動しない
                return;
            }
        }
        Intent intent = new Intent(this, ClipboardListenerService.class);
        this.startService(intent);

    }

    @Override
    public void onStart(){
        super.onStart();

        //Design
        this.initDesign();
    }

    private void initDesign(){
        LinearLayout linearLayout = findViewById(R.id.content_view);
        linearLayout.removeViews(0, linearLayout.getChildCount());

        this.registerReceivers();

        Intent intent = new Intent(this, FileSaveService.class)
                .setAction(FileSaveService.ACTION_READ);
        this.startService(intent);
    }

    public void onAddBtnClick(View view){
        Intent intent = new Intent(this, CardActivity.class)
                .putExtra(FileSaveService.EXTRA_UUID, "");
        this.startActivity(intent);
    }



    @Override
    public void onResume(){
        super.onResume();
        this.registerReceivers();
    }

    @Override
    public void onPause(){
        super.onPause();
        this.unregisterReceivers();
    }

    private void registerReceivers(){
        this.registerReceiver(addListReceiver, new IntentFilter(AddListReceiver.ACTION));
        this.registerReceiver(emptyReceiver, new IntentFilter(EmptyReceiver.ACTION));
        this.registerReceiver(finishReceiver, new IntentFilter(FinishReceiver.ACTION));
    }

    private void unregisterReceivers(){
        this.unregisterReceiver(addListReceiver);
        this.unregisterReceiver(emptyReceiver);
        this.unregisterReceiver(finishReceiver);
    }


    class AddListReceiver extends BroadcastReceiver{

        public static final String ACTION = "add_list_receiver";
        @Override
        public void onReceive(Context ctx, Intent i){
            LinearLayout linearLayout = findViewById(R.id.content_view);

            String source = i.getStringExtra(FileSaveService.EXTRA_SOURCE);
            String target = i.getStringExtra(FileSaveService.EXTRA_TARGET);
            String uuid = i.getStringExtra(FileSaveService.EXTRA_UUID);
            Bitmap bitmap = FileSaveService.loadImage(i.getStringExtra(FileSaveService.EXTRA_IMAGE));

            OctangoCardView v = new OctangoCardView(MainActivity.this, source, target, uuid, bitmap);
            linearLayout.addView(v);
        }
    }

    class EmptyReceiver extends BroadcastReceiver{

        public static final String ACTION = "empty_receiver";

        @Override
        public void onReceive(Context ctx, Intent i){
            LinearLayout linearLayout = findViewById(R.id.content_view);
            TextView textView = new TextView(ctx);
            textView.setText(getText(R.string.empty_message));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(layoutParams);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(30.0f);
            linearLayout.addView(textView);
        }
    }

    class FinishReceiver extends BroadcastReceiver{

        public static final String ACTION = "finish_receiver";

        @Override
        public void onReceive(Context ctx, Intent i){
            //font
            Typeface iconFont = FontAwesome.getTypeface(getApplicationContext(), FontAwesome.FONTAWESOME);
            FontAwesome.markAsIconContainer(findViewById(R.id.main_activity), iconFont);
        }
    }
}
