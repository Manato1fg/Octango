package mnt2cc.com.octango;

import android.content.Intent;
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

    public String source, target, uuid;
    private Bitmap bitmap;
    private Context c;

    public OctangoCardView(Context context) {
        this(context, null);
    }

    public OctangoCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OctangoCardView(Context context, String source, String target, String uuid, Bitmap bitmap){
        super(context, null);

        this.c = context;

        this.source = source;
        this.target = target;
        this.uuid = uuid;
        this.bitmap = bitmap;

        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.octangocard, this);

        TextView sourceView = findViewById(R.id.source);
        sourceView.setText(this.source);
        TextView targetView = findViewById(R.id.target);
        targetView.setText(this.target);
        ImageView imageView = findViewById(R.id.card_image);
        if(this.bitmap == null){
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.no_image));
        }else {
            imageView.setImageBitmap(this.bitmap);
        }

        this.setClickable(true);
        final Context c = this.c;
        final String uuid = this.uuid;
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c, CardActivity.class)
                        .putExtra(FileSaveService.EXTRA_UUID, uuid);
                c.startActivity(intent);
            }
        });

    }
}
