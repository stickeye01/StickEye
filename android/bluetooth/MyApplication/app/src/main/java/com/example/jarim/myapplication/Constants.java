package com.example.jarim.myapplication;

/**
 * Created by hochan on 2018-01-06.
 */

public class Constants {
    public static final int MSG_DEVICE_COUNT = 1;
    public static final int MSG_DEVICD_INFO = 11;
    public static final int MSG_READ_DATA_COUNT = 21;
    public static final int MSG_READ_DATA = 22;
    public static final int MSG_DIALOG_HIDE = 23;
    public static final int MSG_USB_CONN_SUCCESS = 24;
    public static final int MSG_USB_NOTIFY = 25;
    public static final int MSG_SERIAL_ERROR = -1;
    public static final int MSG_FATAL_ERROR_FINISH_APP = -2;
    public static final int MSG_CONN_FAIL = -3;
    public static final String ACTION_USB_PERMISSION ="com.example.jarim.USB_PERMISSION";
    public static String macAddr = "";
    public static final int TIMEOUT = 30000;

    public static final int NAME_REGISTER_STAGE = 0;
    public static final int PHONE_NUM_REG_STAGE = 1;
    public static final int REGISTER_FINAL_STAGE = 2;
    public static final int PHONE_NUM_WRITE_STAGE = 3;
    public static final int CALLING_STAGE = 4;
    public static final int MESSAGE_WRITE_STAGE = 5;
    public static final int SEND_MESSAGE_STAGE = 6;

    public static final char HANGUL_BEGIN_UNICODE = 44032; // 가
    public static final char HANGUL_LAST_UNICODE = 55203; // 힣
    public static final char HANGUL_BASE_UNIT = 588; // 각 자음마다 갖는 글자수.
    public static final char[] HANGUL_CONSONANT = { 'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ',
                                                        'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ',
                                                        'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ',
                                                        'ㅎ' };
    public static final char[] INITIAL_SOUND = {'ㄱ', 'ㄴ', 'ㄷ', 'ㄹ', 'ㅁ', 'ㅂ',
                                                'ㅅ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

    public static final int MAIN_MENU_MODE = 0;
    public static final int SUB_MENU_MODE = 1;
    public static final int BRAILLE_CLICK_MODE = 2;
    public static int MENU_LEVEL = Constants.MAIN_MENU_MODE;

    public static final int BRAILLE_KEYBOARD_OFF = 0;
    public static final int BRAILLE_KEYBOARD_ON = 1;
    public static int KEYBOARD_MODE = Constants.BRAILLE_KEYBOARD_OFF;
    public static int B_KOR_MODE = 0;
    public static int B_ENG_I_MODE = 1;
    public static int B_ENG_U_MODE = 2;
    public static int B_NUM_MODE = 3;
}
