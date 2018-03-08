package com.example.jarim.myapplication.USBConnector;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.jarim.myapplication.R;

/**
 * Created by lhc on 2018-01-05.
 */

public class RegisterDialog extends Dialog {
    private ImageView usb_female;
    private ImageView usb_male;
    private Animation animTransRight;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        animTransRight = AnimationUtils.loadAnimation(mContext, R.anim.translate);

        setCancelable(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.register_dialog);
        setComponent();
    }

    public RegisterDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public RegisterDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected RegisterDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private void setComponent() {
        usb_female = findViewById(R.id.usb_female);
        usb_female.setImageResource(R.drawable.usb_female);

        usb_male = findViewById(R.id.usb_male);
        usb_male.setImageResource(R.drawable.usb_male);

        usb_male.setAnimation(animTransRight);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return false;
    }
}
