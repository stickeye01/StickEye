package com.example.taehyo.jhkeyboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public int modeFlag = 0;
    private String[] modeStr = {"한글", "영어 소문자", "영어 대문자", "숫자"};

    private Scanner scan = new Scanner();
    private KorTranslation kt = new KorTranslation();

    private TextView tv, result;
    public static String resultString2 = "";
    private String one, two, three;
    private Token token;
    private int oneinput[] = new int[6];
    private int totalinput[] = new int[12];
    private boolean doublemode = false;
    private char value;


    public void init(int num[]) {
        for (int i = 0; i < 6; i++) {
            num[i] = 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textView1);
        result = (TextView) findViewById(R.id.textView2);

        tv.setText(modeStr[0]);

        char a=0b00001000;
        //테스트로 임의로 넣어봄
        int[] input = pasing(a);
        if(input[7]==1)
            modeFlag+=1;
        test(input);
        start(input,modeFlag);
    }

    public int[] pasing(char input) {
        int i=6,a;
        int[] input2= new int[8];
        for(i=0; i<8; i++) {
            a=input&1;
            input>>=1;
            input2[i]=a;
        }

        return input2;
    }


    public void test(int input[]) {
        if (input[6] == 1) {
            init(oneinput);
            for (int i = 0; i < 6; i++) {
                oneinput[i] = input[i];
            }
            doublemode = true;
            init(input);
        }
    }
    public void start(int input[], int modeFlag) {
        int flag = 0;
        for (int i = 0; i < 6; i++) {
            if (input[i] == 1) {
                flag = 1;
            }
        }

        tv.setText(modeStr[modeFlag]);



        if (flag == 1) {
            if (!doublemode) {
                token = scan.scanner(input, modeFlag);
                doublemode = false;
            } else {
                for (int i = 0; i < 6; i++) {
                    totalinput[i] = oneinput[i];
                }
                for (int i = 6; i < 12; i++) {
                    totalinput[i] = input[i - 6];
                }
                token = scan.scanner(totalinput, modeFlag);
                doublemode = false;
            }

            if (token != null) {
                if (modeFlag == 0) {
                    value = kt.korSplitData(token);

                    if (value != ' ') {
                        resultString2 += String.valueOf(value);
                    }
                } else if (modeFlag == 2) {
                    resultString2 += (token.getUnicodeValue()).toUpperCase();
                } else {
                    resultString2 += (token.getUnicodeValue());
                }

                result.setText(resultString2);
            } else {
                Toast toast;
                toast = Toast.makeText(MainActivity.this, "점자가 잘못입력되었습니다.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        init(input);
    }
}


