package mnt2cc.com.octango;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View;
import android.graphics.Typeface;

import android.net.Uri;

import android.annotation.SuppressLint;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    private Uri mCropImageUri = null;

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
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.content_view);
        linearLayout.removeViews(0, linearLayout.getChildCount());
        OctangoCardView v;
        v = new OctangoCardView(this, "Hello", "こんにちは", "0xffffffff", null);
        linearLayout.addView(v);

        //font
        Typeface iconFont = FontAwesome.getTypeface(getApplicationContext(), FontAwesome.FONTAWESOME);
        FontAwesome.markAsIconContainer(findViewById(R.id.main_activity), iconFont);
    }


    public void onSelectImageClick(View view) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Bitmap bitmap = result.getBitmap();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }
}
