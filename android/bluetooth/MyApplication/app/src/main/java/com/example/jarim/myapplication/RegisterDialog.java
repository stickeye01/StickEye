package com.example.jarim.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.ImageView;

/**
 * Created by lhc on 2018-01-05.
 */

public class RegisterDialog extends Dialog {
    private ImageView usb_on_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.register_dialog);
        setComponent();
    }

    public RegisterDialog(@NonNull Context context) {
        super(context);
    }

    public RegisterDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected RegisterDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private void setComponent() {
        usb_on_img = findViewById(R.id.usb_img);
        usb_on_img.setImageResource(R.drawable.usb_connection);
    }

}
