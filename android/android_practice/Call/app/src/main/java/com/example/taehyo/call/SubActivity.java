package com.example.taehyo.call;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by taehyo on 2018-01-16.
 */

public class SubActivity extends Activity implements View.OnClickListener {
    private static final int REQUEST_CALL = 1;
    EditText editText;
    Intent intent;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subactivity);

        //에딧텍스트 객체
        editText = (EditText) findViewById(R.id.edtNumber);

        //버튼 객체
        Button button = (Button) findViewById(R.id.btnCall);

        //버튼 클릭
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        //텍스트 객체의 데이터 값을 가져온다.
        String receiver = editText.getText().toString();
        //인텐트 객체 생성
        //인텐트로 전화 걸기 옵션 선언
        //위에서 받은 전화번호 데이터 넣어주기
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + receiver));

        //권한보유 확인 코드
        //
        if(ContextCompat.checkSelfPermission(SubActivity.this,Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SubActivity.this,new String[]{Manifest.permission.CALL_PHONE},REQUEST_CALL);
        }
        else {
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
