package com.example.jarim.myapplication.AndroidSide;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.EditText;

import com.example.jarim.myapplication.Constants;
import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by lhc on 2018-02-12.
 */

public class MessageBookBean extends AppBean {
    private EditText input_etext;
    private ArrayList<SMSMessage> msgLists;
    private int horizontal_index = 0;
    private int MSG_SIZE = 10;

    public MessageBookBean(String _name, String _intentName, TtsService _tts, Context _ctx) {
        super(_name, _intentName, _tts, _ctx);
        input_etext = (EditText) mActivity.findViewById(R.id.test_input);
        msgLists = new ArrayList<SMSMessage>();
    }


    @Override
    public boolean start(Object o) {
        tts.sspeak("문자 메시지 읽기입니다. 조이스틱을 좌우로 움직여주세요.");
        MultiDimensionMenu.MENU_LEVEL = Constants.SUB_MENU_MODE;
        input_etext.setText("");
        input_etext.requestFocus();
        return true;
    }

    /**
     *  Get and fill the array lists for SMS
     */
    private void readSMSMessage() {
        Uri allMessages = Uri.parse("content://sms");
        ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(allMessages,
                new String[] {"_id", "thread_id", "address", "person", "date", "body"},
                null, null, "date DESC");

        while (c.moveToNext()) {
            SMSMessage msg = new SMSMessage();

            msg.setMessageId(String.valueOf(c.getLong(0)));
            msg.setThreadId(String.valueOf(c.getLong(1)));
            msg.setAddress(c.getString(2));
            msg.setContactId(String.valueOf(c.getLong(3)));
            msg.setContactId_string(String.valueOf(c.getLong(3)));
            msg.setTimestamp(String.valueOf(c.getLong(4)));
            msg.setBody(c.getString(5));

            msgLists.add(msg);
        }

        // order by the date
        // Collections.sort(msgLists);
    }


    @Override
    public void left() {
        horizontal_index --;
        if (horizontal_index < 0) horizontal_index = MSG_SIZE - 1;
        readSMSMessage();
        tts.ispeak(Integer.toString(horizontal_index)+"번째 메시지");
        readCurMessage();
    }

    @Override
    public void right() {
        horizontal_index ++;
        readSMSMessage();
        if (horizontal_index == MSG_SIZE)
            tts.ispeak("메인 메뉴로 돌아가기");
        else {
            if (horizontal_index > MSG_SIZE) horizontal_index = 0;
            tts.ispeak(Integer.toString(horizontal_index) + "번째 메시지");
            readCurMessage();
        }
    }

    @Override
    public void click() {
        readSMSMessage();
        if (horizontal_index == MSG_SIZE) {
            tts.ispeak("메인 메뉴로 돌아갑니다.");
            MultiDimensionMenu.MENU_LEVEL = Constants.MAIN_MENU_MODE;
        } else {
            tts.ispeak(Integer.toString(horizontal_index)+"번째 메시지를 읽습니다.");
            readCurMessage();
            // 사실 여기서 클릭 버튼을 한번 더 누르면 문자 답장도 가능하게 했으면 좋을듯.
        }
    }

    /**
     *  read the message that is selected currently.
     */
    private void readCurMessage() {
        SMSMessage selMsg = msgLists.get(horizontal_index);
        tts.ispeak(selMsg.getAddress()+"에게 온 문자입니다.");
        tts.ispeak(selMsg.getBody());
    }

    public class SMSMessage implements  Comparable {
        String messageId;
        String threadId;
        String address; //휴대폰번호
        String contactId;
        String contactId_string;
        String timestamp; //시간
        String body; //문자내용

        public SMSMessage(){}

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getThreadId() {
            return threadId;
        }

        public void setThreadId(String threadId) {
            this.threadId = threadId;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getContactId() {
            return contactId;
        }

        public void setContactId(String contactId) {
            this.contactId = contactId;
        }

        public String getContactId_string() {
            return contactId_string;
        }

        public void setContactId_string(String contactId_string) {
            this.contactId_string = contactId_string;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        @Override
        public int compareTo(@NonNull Object o) {
            SMSMessage msg = (SMSMessage) o;
            return this.timestamp.compareTo(msg.getTimestamp());
        }
    }
}
