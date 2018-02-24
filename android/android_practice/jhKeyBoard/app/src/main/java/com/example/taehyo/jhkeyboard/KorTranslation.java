package com.example.taehyo.jhkeyboard;

/**
 * Created by taehyo on 2018-02-12.
 */


public class KorTranslation {

    // mUnCodeBase-> 유니코드에서 한글은 코드값 0xAC00부터 시작 / 초성 19개, 중성 21개, 종성 28개의 조합으로 코드 배열되있음.
    private static String initialTbl = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
    private static String middleTbl = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
    private static String finalTbl = "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";
    private static double mUniCodeBase = 0xAC00;

    Token[] korval = new Token[500];
    int count = -1;

    char result;
    int state = -1;

    /**
     * 0: 초성 입력 상태
     * 1: 모음 입력 상태
     * 2: 종성 입력 상태
     * @param val : 입력받은 Token 값 .
     * @return : 초성 중성 종성 합친 문자열 반환.
     */
    public char korSplitData(Token val){
        korval[++count]=val;

        // 초성 입력 상태
        if(val.getStrType()==0){
            result=korval[count].getUnicodeValue().charAt(0);
            state=0;
        }


        // 모음 입력상태.
        else if(val.getStrType() == 1){
            if(state == 0){
                // 만일 이미 초성이 입력된 경우라면, 결과 스트링에서 초성을 삭제한 후 합친 완성 글자를 리턴해줌.
                if(MainActivity.resultString2.length() > 0){
                    MainActivity.resultString2 = MainActivity.resultString2.substring(0, MainActivity.resultString2.length()-1);
                }
                result = twoCombineKor(korval[count-1].getUnicodeValue(), korval[count].getUnicodeValue());
                state = 1;
            }
            else{
                result = korval[count].getUnicodeValue().charAt(0);
            }
        }

        // 종성 입력 상태.
        else if(val.getStrType() == 2){
            if(state == 1){
                if(MainActivity.resultString2.length() > 0){
                    MainActivity.resultString2 = MainActivity.resultString2.substring(0, MainActivity.resultString2.length()-1);
                }
                result = threeCombineKor(korval[count-2].getUnicodeValue(), korval[count-1].getUnicodeValue(), korval[count].getUnicodeValue());
                state = 2;
            }
            else{
                result = korval[count].getUnicodeValue().charAt(0);
            }
        }
        //아무것도 입력 안된 상태
        else{
            result = ' ';
        }

        return result;
    }

    /**
     * 초성 중성 입력받아서 합치는 함수
     * @param init : 초성
     * @param mid : 중성
     * @return
     */
    public char twoCombineKor(String init, String mid){
        int iinitial, imiddle;
        int iUniCode;

        iinitial = initialTbl.indexOf(init);   // 초성 위치
        imiddle = middleTbl.indexOf(mid);   // 중성 위치

        //초성 중성 합친 유니코드 값
        iUniCode = (int) (mUniCodeBase + ((iinitial * 21 + imiddle) * 28 ));

        //유니코드값을 문자로 변환하여 반환
        char temp = (char)iUniCode;
        return temp;
    }

    /**
     * 초성 중성 종성 입력받아서 합치는 함수
     * @param init : 초성
     * @param mid : 중성
     * @param fin : 종성
     * @return
     */
    public char threeCombineKor(String init, String mid, String fin){
        int iinitial, imiddle, ifinal;
        int iUniCode;

        iinitial = initialTbl.indexOf(init);
        imiddle=middleTbl.indexOf(mid);
        ifinal=finalTbl.indexOf(fin);

        //초성 중성 종성 합친 유니코드 값
        iUniCode = (int) (mUniCodeBase + ((iinitial * 21 + imiddle) * 28 )+ (ifinal + 1));

        //유니코드값을 문자로 변환하여 반환
        char temp=(char)iUniCode;
        return temp;
    }
}
