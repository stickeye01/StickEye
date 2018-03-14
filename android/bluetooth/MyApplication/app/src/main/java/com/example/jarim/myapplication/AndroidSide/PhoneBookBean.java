package com.example.jarim.myapplication.AndroidSide;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.EditText;

import com.example.jarim.myapplication.BrailleKeyboard.BrailleKeyboard;
import com.example.jarim.myapplication.Constants;
import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lhc on 2018-02-12.
 */

public class PhoneBookBean extends AppBean {
    private EditText input_etext;
    // ㄱ, ㄴ, ㄷ, ㄹ, ㅁ, ㅂ, ㅅ, ㅇ, ㅈ, ㅊ, ㅋ, ㅌ, ㅍ, ㅎ, 기타.
    private ArrayList<ContactInfo>[] charPerConcats;
    private String[] subMenu;
    private int horizontal_index;
    private int vertical_index;

    public PhoneBookBean(String _name, String _intentName, TtsService _tts, Context _ctx,
                         BrailleKeyboard _bKey) {
        super(_name, _intentName, _tts, _ctx, _bKey);
        input_etext = (EditText) mActivity.findViewById(R.id.test_input);

        charPerConcats = new ArrayList[15];
        for (int i = 0; i < charPerConcats.length; i++) {
            charPerConcats[i] = new ArrayList<ContactInfo>();
        }

        subMenu = new String[Constants.INITIAL_SOUND.length+2];
        for (int i = 0; i < Constants.INITIAL_SOUND.length; i++) {
            subMenu[i] = Character.toString(Constants.INITIAL_SOUND[i]);
        }
        subMenu[14] = "기타";
        subMenu[15] = "메인 메뉴로 돌아가기";
        horizontal_index = 0;
        vertical_index = -1;
    }

    @Override
    public boolean start(Object o) {
        tts.ispeak("전화번호부입니다. 좌우로 움직일 때 자음을 선택할 수 있고, " +
                " 상하로 움직일 때 자음으로 시작하는 이름을 선택할 수 있습니다.");
        ArrayList<ContactInfo> contacts = getUserContactsList();
        Constants.MENU_LEVEL = Constants.SUB_MENU_MODE;
        // @{ 한글 자음 정렬
        final Comparator<ContactInfo> comparator = new Comparator<ContactInfo>() {
            @Override
            public int compare(ContactInfo contactInfo, ContactInfo t1) {
                String srcKey = MediaStore.Audio.keyFor(contactInfo.getName());
                String destKey = MediaStore.Audio.keyFor(t1.getName());
                return srcKey.compareTo(destKey);
            }
        };
        Collections.sort(contacts, comparator);
        // @} 한글 자음 정렬 종료

        int no = 0;
        for (ContactInfo cInfo : contacts) {
            char c = getChosung(cInfo.getName().charAt(0));
            Log.e("LHC", "CHAR:"+Character.toString(c));
            int index = getIndexOfHangul(c);
            Log.e("LHC","<"+Integer.toString(no++)+"> "+"이름: "+cInfo.getName()+", 번호:"+cInfo.getPhoneNum()+"-->"+Integer.toString(index));
            charPerConcats[index].add(cInfo);
        }

        return true;
    }

    private ArrayList<ContactInfo> getUserContactsList() {
        ArrayList<ContactInfo> contacts = new ArrayList<ContactInfo>();
        ContactInfo cInfo;

        String [] arrProjection = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        String [] arrPhoneProjection = {
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        // get user list
        Cursor clsCursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, arrProjection,
                ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1" ,
                null, null
        );

        while (clsCursor.moveToNext()) {
            cInfo = new ContactInfo();
            String contactId = clsCursor.getString(0);
            // Name
            cInfo.setName(clsCursor.getString(1));

            Cursor clsPhoneCursor = mContext.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrPhoneProjection,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = "+contactId,
                    null, null
            );
           clsPhoneCursor.moveToNext();
           // Phone number
           cInfo.setPhoneNum(clsPhoneCursor.getString(0));
           clsPhoneCursor.close();
           contacts.add(cInfo);
        }
        clsCursor.close();

        return contacts;
    }


    /**
     * Check whether it is Hangul or not.
     * @param c
     * @return
     */
    private boolean isHangul(char c) {
        return Constants.HANGUL_BEGIN_UNICODE <= c && c <= Constants.HANGUL_LAST_UNICODE;
    }

    /**
     * Get initial alphabet of Hangul character.
     * @param c
     * @return
     */
    private char getChosung(char c) {
        if (isHangul(c)) {
            int hanBeigin = (c - Constants.HANGUL_BEGIN_UNICODE);
            int index = hanBeigin / Constants.HANGUL_BASE_UNIT;
            return Constants.HANGUL_CONSONANT[index];
        } else {
            return 14;
        }
    }

    /**
     * Get index of Hangul alphabet.
     * @param c
     * @return
     */
    private int getIndexOfHangul(char c) {
        switch (c) {
            case 'ㄱ':case 'ㄲ':
                return 0;
            case 'ㄴ': return 1;
            case 'ㄷ':case 'ㄸ':
                return 2;
            case 'ㄹ': return 3;
            case 'ㅁ': return 4;
            case 'ㅂ':case 'ㅃ':
                return 5;
            case 'ㅅ':case 'ㅆ':
                return 6;
            case 'ㅇ': return 7;
            case 'ㅈ':case 'ㅉ':
                return 8;
            case 'ㅊ': return 9;
            case 'ㅋ': return 10;
            case 'ㅌ': return 11;
            case 'ㅍ': return 12;
            case 'ㅎ': return 13;
        }
        return 14;
    }

    private class ContactInfo {
        private String name;
        private String phone_num;

        public String getName() {return name;}
        public void setName(String _name) {name = _name;}
        public String getPhoneNum() {return phone_num;}
        public void setPhoneNum(String _phone_num) {phone_num = _phone_num;}
    }

    @Override
    public void top() {
        if (horizontal_index < 15) {
            if (vertical_index == -1) vertical_index = 0;
            vertical_index ++;
            if (vertical_index >= charPerConcats[horizontal_index].size())
                vertical_index = 0;
            Log.e("LHC","VERTICAL :"+Integer.toString(vertical_index)+", HORIZONTAL:"+Integer.toString(horizontal_index));
            tts.ispeak(charPerConcats[horizontal_index].get(vertical_index).getName());
            menu_txt.setText("전화번호부:"+charPerConcats[horizontal_index].get(vertical_index).getName());
        }
    }

    @Override
    public void down() {
        if (horizontal_index < 15) {
            vertical_index --;
            if (vertical_index < 0)
                vertical_index = charPerConcats[horizontal_index].size()-1;
            tts.ispeak(charPerConcats[horizontal_index].get(vertical_index).getName());
            menu_txt.setText("전화번호부:"+charPerConcats[horizontal_index].get(vertical_index).getName());
        }
    }

    @Override
    public void left() {
        horizontal_index --;
        vertical_index = -1;
        if (horizontal_index < 0) horizontal_index = subMenu.length - 1;
        tts.ispeak(subMenu[horizontal_index]);
        if (horizontal_index != 15)
            menu_txt.setText("전화번호부:"+
                    subMenu[horizontal_index]);
        else
            menu_txt.setText("메인 메뉴로 돌아가기");
    }

    @Override
    public void right() {
        horizontal_index ++;
        vertical_index = -1;
        if (horizontal_index >= subMenu.length) horizontal_index = 0;
        tts.ispeak(subMenu[horizontal_index]);
        if (horizontal_index != 15)
            menu_txt.setText("전화번호부:"+
                    subMenu[horizontal_index]);
        else
            menu_txt.setText("메인 메뉴로 돌아가기");
    }

    @Override
    public void click() {
        if (horizontal_index == 15) {
            tts.ispeak("메인 메뉴로 돌아갑니다.");
            Constants.MENU_LEVEL = Constants.MAIN_MENU_MODE;
        } else if (vertical_index > -1) {
            // 만약 전화번호가 선택된 상태일 경우.
            // vertical_index가 -1이라는 것은, 아직 선택된 번호가 없는 메뉴 상태라는 뜻이다.
            tts.sspeak("전화를 겁니다.");
            ContactInfo cInfo = charPerConcats[horizontal_index].get(vertical_index);
            Intent intent = new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:" + cInfo.getPhoneNum()));

            // Check permissions
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                mContext.startActivity(intent);
            }
        }
    }
}