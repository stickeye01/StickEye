package com.example.jarim.myapplication.BrailleKeyboard;


import java.util.LinkedList;

/**
 * 한글 영어 문자 HashTable
 */

public class HashTable {
    static final public int maxFirstIndex = 7;
    static final public int maxSecondIndex = 27;
    private static LinkedList<Token> korList[][] = new LinkedList[maxFirstIndex][maxSecondIndex];
    private static LinkedList<Token> engList[][] = new LinkedList[maxFirstIndex][maxSecondIndex];
    private static LinkedList<Token> numList[][] = new LinkedList[maxFirstIndex][maxSecondIndex];

    public HashTable(){
        initTable();
        //한글 0: 초성, 1: 모음, 2: 종성
        korList[1][1].add(new Token("100000", "ㄱ", 2));
        korList[1][3].add(new Token("110000", "ㅂ", 2));
        korList[1][4].add(new Token("101000", "ㅈ", 2));
        korList[1][5].add(new Token("100100", "ㄴ", 0));
        korList[1][6].add(new Token("100010", "ㅁ", 0));
        korList[1][7].add(new Token("110100", "ㅋ", 0));
        korList[1][8].add(new Token("110010", "ㅌ", 0));
        korList[1][10].add(new Token("100110", "ㅍ", 0));
        korList[1][12].add(new Token("110110", "ㅇ", 0));
        korList[2][2].add(new Token("010000", "ㄹ", 2));
        korList[2][5].add(new Token("011000", "ㅊ", 2));
        korList[2][6].add(new Token("010100", "ㄷ", 0));
        korList[2][7].add(new Token("010010", "ㄴ", 2));
        korList[2][8].add(new Token("010001", "ㅁ", 2));
        korList[2][10].add(new Token("011010", "ㅋ", 2));
        korList[2][11].add(new Token("010110", "ㅎ", 0));
        korList[2][11].add(new Token("011001", "ㅌ", 2));
        korList[2][13].add(new Token("010011", "ㅍ", 2));
        korList[2][16].add(new Token("011011", "ㅇ", 2));
        korList[3][3].add(new Token("001000", "ㅅ", 2));
        korList[3][8].add(new Token("001010", "ㄷ", 2));
        korList[3][14].add(new Token("001011", "ㅎ", 2));
        korList[4][4].add(new Token("000100", "ㄱ", 0));
        korList[4][9].add(new Token("000110", "ㅂ", 0));
        korList[4][10].add(new Token("000101", "ㅈ", 0));
        korList[5][5].add(new Token("000010", "ㄹ", 0));
        korList[5][11].add(new Token("000011", "ㅊ", 0));
        korList[6][6].add(new Token("000001", "ㅅ", 0));

        korList[4][10].add(new Token("000100000001", "ㄲ", 0));
        korList[2][12].add(new Token("010100000001", "ㄸ", 0));
        korList[4][15].add(new Token("000110000001", "ㅃ", 0));
        korList[6][12].add(new Token("000001000001", "ㅆ", 0));
        korList[4][16].add(new Token("000101000001", "ㅉ", 0));
        korList[3][10].add(new Token("001000001100", "ㅆ", 2));
        korList[1][4].add(new Token("100000001000", "ㄳ", 2));
        korList[2][11].add(new Token("010010101000", "ㄵ", 2));
        korList[2][21].add(new Token("010010001011", "ㄶ", 2));
        korList[2][3].add(new Token("010000100000", "ㄺ", 2));
        korList[2][10].add(new Token("010000010001", "ㄻ", 2));
        korList[2][10].add(new Token("010000110000", "ㄼ", 2));
        korList[2][5].add(new Token("010000001000", "ㄽ", 2));
        korList[2][13].add(new Token("010000011001", "ㄾ", 2));
        korList[2][15].add(new Token("010000010011", "ㄿ", 2));
        korList[2][16].add(new Token("010000001011", "ㅀ", 2));
        korList[1][6].add(new Token("110000001000", "ㅄ", 2));

        //모음
        korList[1][8].add(new Token("101100", "ㅜ", 1));
        korList[1][9].add(new Token("110001", "ㅏ", 1));
        korList[1][9].add(new Token("101010", "ㅣ", 1));
        korList[1][10].add(new Token("101001", "ㅗ", 1));
        korList[1][11].add(new Token("100101", "ㅠ", 1));
        korList[1][12].add(new Token("100011", "ㅕ", 1));
        korList[1][13].add(new Token("101110", "ㅔ", 1));
        korList[1][19].add(new Token("101111", "ㅚ", 1));
        korList[2][9].add(new Token("011100", "ㅓ", 1));
        korList[2][12].add(new Token("010101", "ㅡ", 1));
        korList[2][17].add(new Token("010111", "ㅢ", 1));
        korList[3][7].add(new Token("001100", "ㅖ", 1));
        korList[3][12].add(new Token("001110", "ㅑ", 1));
        korList[3][13].add(new Token("001101", "ㅛ", 1));
        korList[1][11].add(new Token("111010", "ㅐ", 1));
        korList[1][12].add(new Token("111001", "ㅘ", 1));
        korList[1][10].add(new Token("111100", "ㅝ", 1));
        korList[3][23].add(new Token("001110111010", "ㅒ", 1));
        korList[1][23].add(new Token("111001111010", "ㅙ", 1));
        korList[1][21].add(new Token("111100111010", "ㅞ", 1));
        korList[1][19].add(new Token("101100111010", "ㅟ", 1));




        // 영어
        engList[1][1].add(new Token("100000", "a", 3));
        engList[1][3].add(new Token("110000", "b", 3));
        engList[1][5].add(new Token("100100", "c", 3));
        engList[1][10].add(new Token("100110", "d", 3));
        engList[1][6].add(new Token("100010", "e", 3));
        engList[1][7].add(new Token("110100", "f", 3));
        engList[1][12].add(new Token("110110", "g", 3));
        engList[1][8].add(new Token("110010", "h", 3));
        engList[2][6].add(new Token("010100", "i", 3));
        engList[2][11].add(new Token("010110", "j", 3));
        engList[1][4].add(new Token("101000", "k", 3));
        engList[1][6].add(new Token("111000", "l", 3));
        engList[1][8].add(new Token("101100", "m", 3));
        engList[1][13].add(new Token("101110", "n", 3));
        engList[1][9].add(new Token("101010", "o", 3));
        engList[1][10].add(new Token("111100", "p", 3));
        engList[1][15].add(new Token("111110", "q", 3));
        engList[1][11].add(new Token("111010", "r", 3));
        engList[2][9].add(new Token("011100", "s", 3));
        engList[2][14].add(new Token("011110", "t", 3));
        engList[1][10].add(new Token("101001", "u", 3));
        engList[1][12].add(new Token("111001", "v", 3));
        engList[2][17].add(new Token("010111", "w", 3));
        engList[1][14].add(new Token("101101", "x", 3));
        engList[1][19].add(new Token("101111", "y", 3));
        engList[1][15].add(new Token("101011", "z", 3));


        // 숫자
        numList[1][1].add(new Token("100000", "1", 4));
        numList[1][3].add(new Token("110000", "2", 4));
        numList[1][5].add(new Token("100100", "3", 4));
        numList[1][10].add(new Token("100110", "4", 4));
        numList[1][6].add(new Token("100010", "5", 4));
        numList[1][7].add(new Token("110100", "6", 4));
        numList[1][12].add(new Token("110110", "7", 4));
        numList[1][8].add(new Token("110010", "8", 4));
        numList[2][6].add(new Token("010100", "9", 4));
        numList[2][11].add(new Token("010110", "0", 4));

    }


    /// 각 List를 초기화 시키는 method
    public void initTable()
    {

        // 한글 List 초기화
        for(int i = 0; i < maxFirstIndex ; i++)
            for(int j = 0; j < maxSecondIndex ; j++)
                korList[i][j] = new LinkedList();

        // 영어 List 초기화
        for(int i = 0; i < maxFirstIndex ; i++)
            for(int j = 0; j < maxSecondIndex ; j++)
                engList[i][j] = new LinkedList();

        // 숫자 List 초기화
        for(int i = 0; i < 3; i++)
            for(int j = 0; j < 13; j++)
                numList[i][j] = new LinkedList();
    }

    public static LinkedList<Token>[][] getKorList() {
        return korList;
    }
    public static void setKorList(LinkedList<Token>[][] koreList) {
        HashTable.korList = koreList;
    }


    public static LinkedList<Token>[][] getEngList() {
        return engList;
    }
    public static void setEngList(LinkedList<Token>[][] engList) {
        HashTable.engList = engList;
    }



    public static LinkedList<Token>[][] getNumList() {
        return numList;
    }
    public static void setNumList(LinkedList<Token>[][] numList) {
        HashTable.numList = numList;
    }

}