package com.example.taehyo.jhkeyboard;

/**
 * Created by taehyo on 2018-02-12.
 */

public class Token {

    private String realPattern;			//  토큰의 패턴
    private String unicodeValue;		//  토큰의 문자
    private int     strType;


    public Token(){ }
    /**
     * 한글 외 문자 생성자
     * @param _realPattern
     * @param _unicodeValue
     */
    public Token(String _realPattern, String _unicodeValue){
        realPattern    = _realPattern;
        unicodeValue = _unicodeValue;
    }

    /**
     * 한글 문자 생성자
     * @param _realPattern
     * @param _unicodeValue
     * @param _strType
     */
    public Token(String _realPattern, String _unicodeValue, int _strType){
        realPattern    = _realPattern;
        unicodeValue = _unicodeValue;
        strType  		 = _strType;
    }

    public int getStrType() {
        return strType;
    }
    public void setStrType(int strType) {
        this.strType = strType;
    }
    public String getRealPattern() {
        return realPattern;
    }
    public void setRealPattern(String realPattern) {
        this.realPattern = realPattern;
    }
    public String getUnicodeValue() {
        return unicodeValue;
    }
    public void setUnicodeValue(String unicodeValue) {
        this.unicodeValue = unicodeValue;
    }
}
