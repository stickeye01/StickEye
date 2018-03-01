package com.example.jarim.myapplication.BrailleKeyboard;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;


public class Scanner {

    private ArrayList<String> resultStr;

    /**
     * 생성자
     * 각 객체들을 초기화한다.
     * modeFlag는 임시로 초기화 되어있음.
     */
    public Scanner(){
        resultStr   = new ArrayList<String>();
    }

    /**
     * scanner()
     * summary : 문자열을 점자의 배열로 입력받고 그 배열을 문자로 변경하여 저장하는 역할 수행.
     * @param in : 입력한 문자열 배열
     * @param modeFlag : 입력한 모드
     * @return
     */
    public Token scanner(int in[], int modeFlag)
    {
        int i;
        int size=in.length;
        int input[] = new int[size];
        input=in;
        String ipPattern= "";

        for(i=0; i<size; i++){
            ipPattern += Integer.toString(input[i]);
        }

        Token ipToken	 = new Token();

        switch (modeFlag){
            case 0:
            case 1:
            case 2:
            case 3:
                ipToken=calcForValue(modeFlag, ipPattern);
                break;
            case 4:
                ipToken=null;
                break;
            default:
                break;
        }

        return ipToken;
    }


    /**
     * 1. HashTable을 먼저 검사한다.
     * 2. 검사 후 한 row를 LinkedList로 모두 받아온다.
     * 3. 그 LinkedList를 검사하여 내가 입력한 패턴과 맞는 올바른 token을 얻어오는 메소드
     * @param modeFlag : 언어 구별 플래그
     * @param _patternArr : 실제 입력받은 String Pattern을 가져온다.
     * @return
     */

    private Token calcForValue(int modeFlag, String _patternArr){
        int firstindex = 0;									// 첫번째 인덱스(첫번째 값)
        int secondindex	= 0;

        LinkedList targetList  = null;
        Token token = null;

        firstindex = getFirstIndex(_patternArr);
        secondindex	   = sumCalc(_patternArr);
        int i;
        try{
            switch(modeFlag){
                case 0:
                    targetList = new HashTable().getKorList()[firstindex][secondindex];
                    break;
                case 1:
                case 2:
                    targetList = new HashTable().getEngList()[firstindex][secondindex];
                    break;
                case 3:
                    targetList = new HashTable().getNumList()[firstindex][secondindex];
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




    /**
     * 처음으로 1이 나오는 인덱스를 찾아주는 메소드.
     * @param input
     * @return
     */
    private int getFirstIndex(String input){
        int i=0;
        int[] list= makeIntegerList(input);

        for(i=0; i<list.length; i++){
            if(list[i]==1){
                break;
            }
        }

        return (i+1);
    }

    /**
     * String 배열을 int형 배열로 변경해주는 메소드
     * @param _strEnum : int형 배열로 변경할 문자열 ( 여기서는 입력받는 패턴을 이야기한다.)
     * @return : int형으로 변경한 배열 리턴
     */
    private int[] makeIntegerList(String _strEnum){
        int size = _strEnum.length();
        int[] intList = new int[size];
        for(int i = 0; i < size; i++)
            intList[i] = (int)_strEnum.charAt(i) - 48;						// 스트링 변경
        return intList;
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
}
