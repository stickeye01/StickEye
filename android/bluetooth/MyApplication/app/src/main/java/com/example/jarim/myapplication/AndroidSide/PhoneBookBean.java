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
    // ��, ��, ��, ��, ��, ��, ��, ��, ��, ��, ��, ��, ��, ��, ��Ÿ.
    private ArrayList<ContactInfo>[] charPerConcats;
    private String[] subMenu;
    private int horizontal_index;
    private int vertical_index;

    public PhoneBookBean(String _name, String _intentName, TtsService _tts, Context _ctx) {
        super(_name, _intentName, _tts, _ctx);
        input_etext = (EditText) mActivity.findViewById(R.id.test_input);

        charPerConcats = new ArrayList[15];
        for (int i = 0; i < charPerConcats.length; i++) {
            charPerConcats[i] = new ArrayList<ContactInfo>();
        }

        subMenu = new String[Constants.INITIAL_SOUND.length+2];
        for (int i = 0; i < Constants.INITIAL_SOUND.length; i++) {
            subMenu[i] = Character.toString(Constants.INITIAL_SOUND[i]);
        }
        subMenu[14] = "��Ÿ";
        subMenu[15] = "���� �޴��� ���ư���";
        horizontal_index = 0;
        vertical_index = -1;
    }

    @Override
    public boolean start(Object o) {
        tts.ispeak("��ȭ��ȣ���Դϴ�. �¿�� ������ �� ������ ������ �� �ְ�, " +
                " ���Ϸ� ������ �� �������� �����ϴ� �̸��� ������ �� �ֽ��ϴ�.");
        ArrayList<ContactInfo> contacts = getUserContactsList();
        MultiDimensionMenu.MENU_LEVEL = Constants.SUB_MENU_MODE;
        // @{ �ѱ� ���� ����
        final Comparator<ContactInfo> comparator = new Comparator<ContactInfo>() {
            @Override
            public int compare(ContactInfo contactInfo, ContactInfo t1) {
                String srcKey = MediaStore.Audio.keyFor(contactInfo.getName());
                String destKey = MediaStore.Audio.keyFor(t1.getName());
                return srcKey.compareTo(destKey);
            }
        };
        Collections.sort(contacts, comparator);
        // @} �ѱ� ���� ���� ����

        int no = 0;
        for (ContactInfo cInfo : contacts) {
            char c = getChosung(cInfo.getName().charAt(0));
            Log.e("LHC", "CHAR:"+Character.toString(c));
            int index = getIndexOfHangul(c);
            Log.e("LHC","<"+Integer.toString(no++)+"> "+"�̸�: "+cInfo.getName()+", ��ȣ:"+cInfo.getPhoneNum()+"-->"+Integer.toString(index));
            charPerConcats[index].add(cInfo);
        }

        return true;
    }

    @Override
    public void clicked(Object _v) {

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
            case '��':case '��':
                return 0;
            case '��': return 1;
            case '��':case '��':
                return 2;
            case '��': return 3;
            case '��': return 4;
            case '��':case '��':
                return 5;
            case '��':case '��':
                return 6;
            case '��': return 7;
            case '��':case '��':
                return 8;
            case '��': return 9;
            case '��': return 10;
            case '��': return 11;
            case '��': return 12;
            case '��': return 13;
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
        }
    }

    @Override
    public void down() {
        if (horizontal_index < 15) {
            vertical_index --;
            if (vertical_index < 0)
                vertical_index = charPerConcats[horizontal_index].size()-1;
            tts.ispeak(charPerConcats[horizontal_index].get(vertical_index).getName());
        }
    }

    @Override
    public void left() {
        horizontal_index --;
        vertical_index = -1;
        if (horizontal_index < 0) horizontal_index = subMenu.length - 1;
        tts.ispeak(subMenu[horizontal_index]);
    }

    @Override
    public void right() {
        horizontal_index ++;
        vertical_index = -1;
        if (horizontal_index >= subMenu.length) horizontal_index = 0;
        tts.ispeak(subMenu[horizontal_index]);
    }

    @Override
    public void click() {
        if (horizontal_index == 15) {
            tts.ispeak("���� �޴��� ���ư��ϴ�.");
            MultiDimensionMenu.MENU_LEVEL = Constants.MAIN_MENU_MODE;
        } else if (vertical_index > -1) {
            // ���� ��ȭ��ȣ�� ���õ� ������ ���.
            // vertical_index�� -1�̶�� ����, ���� ���õ� ��ȣ�� ���� �޴� ���¶�� ���̴�.
            tts.sspeak("��ȭ�� �̴ϴ�.");
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