package mnt2cc.com.octango;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class CardActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 0x2cc;

    public static final int IMAGE_SIZE = 150;

    public FinishSavingReceiver finishSavingReceiver = new FinishSavingReceiver();
    public ErrorReceiver errorReceiver = new ErrorReceiver();
    public SearchReceiver searchReceiver = new SearchReceiver();

    private String uuid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.registerReceivers();

        Intent intent = getIntent();
        if(!intent.getStringExtra(FileSaveService.EXTRA_UUID).equals("")){
            String uuid = intent.getStringExtra(FileSaveService.EXTRA_UUID);
            Intent i = new Intent(this, FileSaveService.class)
                    .setAction(FileSaveService.ACTION_GET)
                    .putExtra(FileSaveService.EXTRA_UUID, uuid);
            this.uuid = uuid;
            this.startService(i);
        }
    }

    public void pickImage(View view){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public void onDone(View view){
        TextView sourceView = findViewById(R.id.sourceEdit);
        TextView targetView = findViewById(R.id.targetEdit);
        ImageView imageView = findViewById(R.id.card_image_view);

        if(sourceView.getText().toString().equals("") || targetView.getText().toString().equals("")){
            Toast.makeText(this, getText(R.string.fill_inputs), Toast.LENGTH_LONG).show();
            return;
        }

        String source = sourceView.getText().toString();
        String target = targetView.getText().toString();
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, false);

        Intent intent = new Intent(this, FileSaveService.class)
                .setAction(FileSaveService.ACTION_REGISTER)
                .putExtra(FileSaveService.EXTRA_SOURCE, source)
                .putExtra(FileSaveService.EXTRA_TARGET, target)
                .putExtra(FileSaveService.EXTRA_UUID, this.uuid)
                .putExtra(FileSaveService.EXTRA_IMAGE, FileSaveService.base64encode(bitmap));

        Log.d("[Octango Debugger]", "hi1");
        this.startService(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent i){
        super.onActivityResult(requestCode, resultCode, i);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            if(i != null){
                CropImage.activity(i.getData())
                        .setAspectRatio(1, 1)
                        .start(this);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(i);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try{
                    InputStream inputStream = getContentResolver().openInputStream(resultUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ImageView imageView = findViewById(R.id.card_image_view);
                    imageView.setImageBitmap(bitmap);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }
        }
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

    public void registerReceivers(){
        this.registerReceiver(finishSavingReceiver, new IntentFilter(FinishSavingReceiver.ACTION));
        this.registerReceiver(errorReceiver, new IntentFilter(ErrorReceiver.ACTION));
        this.registerReceiver(searchReceiver, new IntentFilter(SearchReceiver.ACTION));
    }

    public void unregisterReceivers(){
        this.unregisterReceiver(finishSavingReceiver);
        this.unregisterReceiver(errorReceiver);
        this.unregisterReceiver(searchReceiver);
    }

    class FinishSavingReceiver extends BroadcastReceiver{

        public static final String ACTION = "finish_saving_receiver";
        @Override
        public void onReceive(Context ctx, Intent i){
            finish();
        }
    }

    class ErrorReceiver extends BroadcastReceiver{

        public static final String ACTION = "error_receiver";
        @Override
        public void onReceive(Context ctx, Intent i){
            Toast.makeText(CardActivity.this, getText(R.string.error_occurred), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    class SearchReceiver extends BroadcastReceiver{

        public static final String ACTION = "search_receiver";
        @Override
        public void onReceive(Context ctx, Intent i){
            String source = i.getStringExtra(FileSaveService.EXTRA_SOURCE);
            String target = i.getStringExtra(FileSaveService.EXTRA_TARGET);
            Bitmap bitmap = FileSaveService.loadImage(i.getStringExtra(FileSaveService.EXTRA_IMAGE));

            TextView sourceEdit = findViewById(R.id.sourceEdit);
            TextView targetEdit = findViewById(R.id.targetEdit);
            ImageView imageView = findViewById(R.id.card_image_view);

            sourceEdit.setText(source);
            targetEdit.setText(target);
            imageView.setImageBitmap(bitmap);
        }
    }

}
