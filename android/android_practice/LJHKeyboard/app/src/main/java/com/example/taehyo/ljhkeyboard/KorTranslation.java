package com.example.taehyo.ljhkeyboard;

/**
 * Created by taehyo on 2018-02-10.
 */
 // mUnCodeBase-> 유니코드에서 한글은 코드값 0xAC00부터 시작 / 초성 19개, 중성 21개, 종성 28개의 조합으로 코드 배열되있음.
public class KorTranslation {
    private static String initialTbl = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
    private static String middleTbl = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
    private static String finalTbl = "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";
    private static double mUniCodeBase = 0xAC00;

    Token[] korval = new Token[500];
    int count = -1;

    char result;
    int state = -1;
    // 0 : 초성 입력 상태, 1 : 모음입력 상태 : 2 : 종성입력 상태.
    public char splitdata(Token val){
        korval[++count] = val;
        if(val.getStrType() == 0){ // 초성 입력 상태.
            result = korval[count].getUnicodeValue().charAt(0);
            state = 0;
        }
        else if(val.getStrType() == 1){ // 모음 입력상태.
            if(state == 0){
                // 만일 이미 초성이 입력된 경우라면, 결과 스트링에서 초성을 삭제한 후 합친 완성 글자를 리턴해줌.
                if(MainActivity.resultString2.length() > 0){
                    MainActivity.resultString2 = MainActivity.resultString2.substring(0, MainActivity.resultString2.length()-1);
                }
                result = twocombineKor(korval[count-1].getUnicodeValue(), korval[count].getUnicodeValue());
                state = 1;
            }
            else{
                result = korval[count].getUnicodeValue().charAt(0);
            }
        }
        else if(val.getStrType() == 2){ // 모음 입력 상태.
            if(state == 1){
                if(MainActivity.resultString2.length() > 0){
                    MainActivity.resultString2 = MainActivity.resultString2.substring(0, MainActivity.resultString2.length()-1);
                }
                result = threecombineKor(korval[count-2].getUnicodeValue(), korval[count-1].getUnicodeValue(), korval[count].getUnicodeValue());
                state = 2;
            }
            else{
                result = korval[count].getUnicodeValue().charAt(0);
            }
        }
        else{
            result = ' ';
        }

        return result;
    }

    public char twocombineKor(String one, String two){
        int iinitial, imiddle;
        int iUniCode;

        iinitial = initialTbl.indexOf(one);   // 초성 위치
        imiddle = middleTbl.indexOf(two);   // 중성 위치

        iUniCode = (int) (mUniCodeBase + ((iinitial * 21 + imiddle) * 28 ));
        char temp = (char)iUniCode;
        return temp;
    }

        public char threecombineKor(String one, String two, String three){
            int iinitial, imiddle, ifinal;
            int iUniCode;
            iinitial = initialTbl.indexOf(one);   // 초성 위치
            imiddle = middleTbl.indexOf(two);   // 중성 위치
            ifinal = finalTbl.indexOf(three);   // 종성 위치
            // 앞서 만들어 낸 계산식
            iUniCode = (int) (mUniCodeBase + ((iinitial * 21 + imiddle) * 28 )+ (ifinal + 1));
            // 코드값을 문자로 변환
            char temp = (char)iUniCode;
            return temp;
        }
}