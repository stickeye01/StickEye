package com.example.taehyo.jhkeyboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public int modeFlag = 0;
    private String[] modeStr = {"한글", "영어 소문자", "영어 대문자",  "숫자"};

    private Scanner scan = new Scanner();
    private KorTranslation kt = new KorTranslation();

    private TextView tv,result;
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
        result = (TextView) findViewById(R.id.textView2);

        tv.setText(modeStr[0]);

        ImageButton button1 = (ImageButton)findViewById(R.id.button1);
        ImageButton button2 = (ImageButton)findViewById(R.id.button2);
        ImageButton button3 = (ImageButton)findViewById(R.id.button3);
        ImageButton button4 = (ImageButton)findViewById(R.id.button4);
        ImageButton button5 = (ImageButton)findViewById(R.id.button5);
        ImageButton button6 = (ImageButton)findViewById(R.id.button6);
        Button ok = (Button) findViewById(R.id.button8);
        Button mode1 = (Button)findViewById(R.id.button9);
        Button cancel = (Button)findViewById(R.id.button7);
        Button reset = (Button)findViewById(R.id.button11);
        Button doublebutton = (Button)findViewById(R.id.button10);


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
    public void onClick(View v) {
        int flag = 0;
        v.setSoundEffectsEnabled(false);
        switch (v.getId()){
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
            /*
             * button7은 확인버튼
             * 배열중에 입력된 점자가 있으면
             * if 먼저 쌍자음/모음 상태인지 구별함
             * 쌍자음이면 12개의 점자를 입력받고 출력
             * else 쌍자음이나 모음이 아니면 6개 점자 입력받고 출력
             */
            case R.id.button7:
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

                    /*
                     * modeFlag
                     * 0 = 한글 , 1 = 영어, 2 = 숫자
                     * 0인 경우
                     * korSplitData함수에 token을 넣어 한글로 변환한다음 resultString2에 합친다음 출력
                     */
                    if(token!=null){
                        if(modeFlag==0){
                            value=kt.korSplitData(token);

                            if(value != ' '){
                                resultString2+=String.valueOf(value);
                            }
                        }
                        else if(modeFlag==2){
                            resultString2+=(token.getUnicodeValue()).toUpperCase();
                        }
                        else{
                            resultString2+=(token.getUnicodeValue());
                        }

                        result.setText(resultString2);
                    }

                    else{
                        Toast toast;
                        toast = Toast.makeText( MainActivity.this, "점자가 잘못입력되었습니다.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                init(input);
                break;
                // button8= 지우기
            case R.id.button8:
                if(resultString2.length()>0){
                    resultString2=resultString2.substring(0,resultString2.length()-1);
                }
                result.setText(resultString2);
                break;
                //button9 = 모드 선택
            case R.id.button9:
                modeFlag+=1;

                if(modeFlag==4)
                {
                    modeFlag=0;
                }
                tv.setText(modeStr[modeFlag]);
                break;
                // button10 = 대문자인경우 6개의 점자 더 입력받음
            case R.id.button10:
                init(oneinput);
                for(int i=0; i<6; i++){
                    oneinput[i]=input[i];
                }
                doublemode=true;
                init(input);
                break;
                // button11 = 초기화
            case R.id.button11:
                result.setText(" ");
                resultString2=" ";
                break;
                // button12 = spacebar
            case R.id.button12:
                result.setText("\t");
                break;
            default:
                break;

        }
    }
}
