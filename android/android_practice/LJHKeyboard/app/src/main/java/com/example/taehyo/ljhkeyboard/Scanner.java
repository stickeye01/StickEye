package com.example.taehyo.ljhkeyboard;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by taehyo on 2018-02-10.
 */

public class Scanner {
    // 한글, 영어, 숫자 인지 구별해주는 플래그
    // 0 : 한글			 1 : 영어 소문자
    // 2 : 영어 대문자	 3 : 숫자
    // 4 : 특수문자
    private ArrayList<String> resultStr;
    TextView tv;

    /***
     * 생성자
     *	각 객체들을 초기화 한다.
     *  +) modeFlag는 현재 임시로 초기화 되어있음.
     */
    public Scanner(){
        resultStr   = new ArrayList<String>();
    }

    /***
     * scanner()
     * summary : touchPad로 문자열을 점자의 배열로 입력받고 그 배열을 문자로 변경(incode/decode)하여
     * 저장해주는 역할을 수행한다.
     *
     *  return : X
     *  param : X
     */
    public Token scanner(int in[], int modeFlag)
    {
        int i ;
        int size = in.length;
        int input[] = new int[size];
        input = in;
        String ipPattern = "";

        for(i = 0; i<size ;i++){
            ipPattern += Integer.toString(input[i]);
        }

        Token ipToken	 = new Token();

        switch(modeFlag){
            case 0:

            case 1:
            case 2:
            case 3:
                ipToken = hash(modeFlag, ipPattern);
                break;
            case 4:
                ipToken = null;// 특수 문자
                break;
            default:
                ipToken = null;// 에러 처리
                break;
        }

        return ipToken;
    }

    /**
     *  해시 값을 얻어오는 메소드
     * @param _modeFlag   :
     * @param _patternArr
     * @return
     */
    private Token hash(int _modeFlag, String _patternArr){
        int firstNum = 0;									// 첫번째 인덱스(첫번째 값)
        int allSum	= 0;									// 두번째 인덱스(두번째 값)
        Token token = new Token();						// 토큰 값

        firstNum = getFirstindex(_patternArr);
        allSum	   = sumCalc(_patternArr);

        switch(_modeFlag){
            case 0:

            case 1:
            case 2:
            case 3:
                token = calcForValue(_modeFlag,  firstNum,  allSum, _patternArr);
                break;
            case 4:
                break;
            default:
                break;
        }

        return token;
    }
    // 처음으로 1이나오는 인덱스를 찾아주는 메소드.
    private int getFirstindex(String params){
        int i;
        int[] list = makeIntegerList(params);

        for(i = 0; i < list.length; i++){
            if(list[i] == 1){
                break;
            }
        }
        return (i+1);
    }
    /**
     * 입력 받은 패턴(0,1로 이루어진 배열)을 모두 더해서
     * hash Table에 사용할 index로 사용한다.
     * @param paramLIst : 입력받은 패턴 배열
     * @return : 더한 값
     */
    private int sumCalc(String paramLIst){
        int sumResult = 0;
        int[] integerList = makeIntegerList(paramLIst);
        int i;

        if(integerList.length > 6){
            for(i = 0; i < integerList.length; i++){

                if(i >= 6){
                    if(integerList[i] == 1){
                        sumResult += (((i-6)+1));
                        System.out.println(i + " " + ((i-6)+1));
                    }
                }
                else{
                    if(integerList[i] == 1){
                        sumResult += (i+1);
                        System.out.println(i + " " + (i+1));
                    }
                }
            }
        }
        else{
            for(i = 0; i < integerList.length; i++){
                if(integerList[i] == 1){
                    sumResult += (i+1);
                }
            }
        }
        System.out.println(sumResult);
        return sumResult;
    }

    /**
     *  String 값을 Int형 배열로 변경해주는 메소드
     * @param _strEnum : Int 배열로 변경할 문자열 (보통 입력받는 패턴을 이야기한다.)
     * @return int형으로 변경한 배열 리턴
     */
    private int[] makeIntegerList(String _strEnum){
        int size = _strEnum.length();
        int[] intList = new int[size];
        for(int i = 0; i < size; i++)
            intList[i] = (int)_strEnum.charAt(i) - 48;						// 스트링 변경
        return intList;
    }

    /**
     * 1. HashTable을 먼저 검사하여
     * 한 row를 LinkedList로 모두 받아온뒤.
     * 2. 그 LinkedList를 검사하여 올바른 토큰을 얻어오는 메소드
     * @param modeFlag : 언어 구별 플래그
     * @param firstindex  : 첫번째 인덱스, 패턴(0,1)을 모두 더한 값을 가져온다.
     * @param second	   : 두번째 인덱스,  패턴의 첫번째 값을 가져온다.
     * @param _patternArr: 실제로 입력받은 String Pattern을 가져온다.
     * @return  : 검사된 토큰
     */
    private Token calcForValue(int modeFlag, int firstindex, int second, String _patternArr){
        LinkedList targetList  = null;
        Token token = null;
        int i;
        try{
            switch(modeFlag){
                case 0:
                    targetList = new HashTable().getKoreList()[firstindex][second];
                    break;
                case 1:
                case 2:
                    targetList = new HashTable().getEngList()[firstindex][second];
                    break;
                case 3:
                    targetList = new HashTable().getNumList()[firstindex][second];
                    break;
                case 4:
                    break;
                default:
                    break;
            }

            if(targetList!=null){

                if(targetList.size() == 1){
                    token = (Token) targetList.getFirst();
                }else if(targetList.size() == 0){
                    token = null;
                }else{
                    for(i = 0; i < targetList.size(); i++){
                        if((token = (Token) targetList.get(i)).getRealPattern().equals(_patternArr)){
                            break;
                        }
                    }
                }
            }
            else{
                System.out.println("error");
            }
        }catch(Exception e){}
        return token;
    }
}
