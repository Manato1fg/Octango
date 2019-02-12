package mnt2cc.com.octango;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

public class OctangoCardView extends LinearLayout {

    public String source, target, uid;
    private Bitmap bitmap;

    public OctangoCardView(Context context) {
        this(context, null);
    }

    public OctangoCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OctangoCardView(Context context, String source, String target, String uid, Bitmap bitmap){
        super(context, null);

        this.source = source;
        this.target = target;
        this.uid = uid;
        this.bitmap = bitmap;

        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.octangocard, this);

        TextView sourceView = (TextView) findViewById(R.id.source);
        sourceView.setText(this.source);
        TextView targetView = (TextView) findViewById(R.id.target);
        targetView.setText(this.target);
        ImageView imageView = (ImageView) findViewById(R.id.card_image);
        if(this.bitmap == null){
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.no_image));
        }else{
            imageView.setImageBitmap(this.bitmap);
        }

        final String _uid = this.uid;

        TextView deleteBtn = (TextView) findViewById(R.id.deleteButton);

        deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("[Octango]", _uid);
            }
        });

    }

    public void onEditButtonClicked(View view){

    }

    public void onDeleteButtonClicked(View view){

    }
}
