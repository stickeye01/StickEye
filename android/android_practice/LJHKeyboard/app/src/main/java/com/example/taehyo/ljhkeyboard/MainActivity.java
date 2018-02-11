package com.example.taehyo.ljhkeyboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public int modeFlag = 0;
    private String[] modeStr = {"한글", "영어 소문자", "영어 대문자",  "숫자"};

    private Scanner scan = new Scanner();
    private KorTranslation kt = new KorTranslation();

    private TextView tv , result, tv2;
    private String resultString ="";
    public static String resultString2 = "";
    private String one, two, three;
    private Token token;
    private int input[] = new int[6];
    private int oneinput[] = new int[6];
    private int totalinput[] = new int[12];
    private boolean doublemode = false;
    private char value;



    public void init(int num[]){
        for(int i =0; i<6; i++){
            num[i] = 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv = (TextView)findViewById(R.id.textView1);
        result = (TextView)findViewById(R.id.textView3);
        tv2 = (TextView)findViewById(R.id.textView2);

        tv.setText(modeStr[0]);

        ImageButton button1 = (ImageButton)findViewById(R.id.button1);
        ImageButton button2 = (ImageButton)findViewById(R.id.button2);
        ImageButton button3 = (ImageButton)findViewById(R.id.button3);
        ImageButton button4 = (ImageButton)findViewById(R.id.button4);
        ImageButton button5 = (ImageButton)findViewById(R.id.button5);
        ImageButton button6 = (ImageButton)findViewById(R.id.button6);
        ImageButton ok = (ImageButton)findViewById(R.id.button8);
        ImageButton mode1 = (ImageButton)findViewById(R.id.button9);
        ImageButton cancel = (ImageButton)findViewById(R.id.button7);
        ImageButton reset = (ImageButton)findViewById(R.id.button11);
        ImageButton doublebutton = (ImageButton)findViewById(R.id.button10);


        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button6.setOnClickListener(this);
        ok.setOnClickListener(this);
        mode1.setOnClickListener(this);
        cancel.setOnClickListener(this);
        reset.setOnClickListener(this);
        doublebutton.setOnClickListener(this);

    }



    @Override
    public void onClick(View v){
        int flag=0;
        v.setSoundEffectsEnabled(false);
        switch(v.getId()){
            case R.id.button1:
                input[0] = 1;
                break;
            case R.id.button2:
                input[1] = 1;
                break;
            case R.id.button3:
                input[2] = 1;
                break;
            case R.id.button4:
                input[3] = 1;
                break;
            case R.id.button5:
                input[4] = 1;
                break;
            case R.id.button6:
                input[5] = 1;
                break;
            case R.id.button8:
                for(int i=0; i<6; i++){
                    if(input[i]==1){
                        flag=1;
                    }
                }

                if(flag==1){
                    if(!doublemode){
                        token=scan.scanner(input,modeFlag);
                        doublemode=false;
                    }
                    else{
                        for(int i=0; i<6; i++){
                            totalinput[i]=oneinput[i];
                        }
                        for(int i=6; i<12; i++){
                            totalinput[i]=input[i-6];
                        }
                        token=scan.scanner(totalinput,modeFlag);
                        doublemode=false;
                    }

                    if(token!=null){
                        if(modeFlag==0){
                            resultString+=token.getUnicodeValue();
                            value=kt.splitdata(token);

                            if(value!= ' '){
                                resultString2+=String.valueOf(value);
                            }
                        }
                        else if(modeFlag==2){
                            resultString+=(token.getUnicodeValue()).toUpperCase();
                            resultString2+=(token.getUnicodeValue()).toUpperCase();
                        }
                        else{
                            resultString+=(token.getUnicodeValue());
                            resultString2+=(token.getUnicodeValue());
                        }

                        result.setText(resultString);
                        tv2.setText(resultString2);
                    }
                    else{
                        Toast toast;
                        toast = Toast.makeText( MainActivity.this, "점자가 잘못입력되었습니다.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                init(input);
                break;


            case R.id.button9:
                modeFlag+=1;

                if(modeFlag==4)
                {
                    modeFlag=0;
                }
                tv.setText(modeStr[modeFlag]);

                break;
            case R.id.button7:
                if(resultString2.length()>0){
                    resultString2=resultString2.substring(0,resultString2.length()-1);
                }
                tv2.setText(resultString2);
                break;
            case R.id.button11:
                tv2.setText(" ");
                result.setText(" ");
                resultString="";
                resultString2="";
                break;

            case R.id.button10:
                init(oneinput);
                for(int i=0; i<6; i++){
                    oneinput[i]=input[i];
                }
                doublemode=true;
                init(input);
                break;
            default:
                break;
        }

    }
}
