package com.example.jarim.myapplication.AndroidSide;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.jarim.myapplication.BrailleKeyboard.BrailleKeyboard;
import com.example.jarim.myapplication.Constants;
import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

import java.util.ArrayList;

/**
 * Created by lhc on 2018-02-09.
 */

public class PhoneRegisterBean extends AppBean {
    private EditText input_etext;
    private int no_degree = Constants.NAME_REGISTER_STAGE;

    private ArrayList <ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

    private String name;
    private String phoneNum;

    public PhoneRegisterBean(String _name, String _intentName, TtsService _tts, Context _ctx,
                             BrailleKeyboard _bKey) {
        super(_name, _intentName, _tts, _ctx, _bKey);
        input_etext = (EditText) mActivity.findViewById(R.id.test_input);
    }

    @Override
    public boolean start(Object o) {
        Constants.MENU_LEVEL = Constants.BRAILLE_CLICK_MODE;
        bKey.turnOnBrailleKB();
        bKey.clearString();
        tts.ispeak("전화번호 등록입니다. 이름을 입력하세요.");
        input_etext.setText("");
        input_etext.requestFocus();
        return true;
    }

    @Override
    public void click() {
        if (no_degree == Constants.NAME_REGISTER_STAGE) {
            name = input_etext.getText().toString();
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            // NAMES
            if (name != null) {
                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                name).build());
            }
            tts.ispeak("번호를 입력하세요.");
            input_etext.setText("");
            input_etext.requestFocus();
            bKey.changeMode(Constants.B_NUM_MODE);
            bKey.TOnModeLock();
            bKey.clearString();
            no_degree = Constants.PHONE_NUM_REG_STAGE;
        } else if (no_degree == Constants.PHONE_NUM_REG_STAGE) {
            phoneNum = input_etext.getText().toString();
            // Phone Numbers
            if (phoneNum != null) {
                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                         ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNum)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            }
            tts.ispeak("등록하시겠습니까? 등록하시려면 click button을 누르세요.");
            no_degree = Constants.REGISTER_FINAL_STAGE;
        } else if (no_degree == Constants.REGISTER_FINAL_STAGE) {
            try {
                mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                tts.ispeak("번호가 등록되었습니다!");
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
            no_degree = Constants.NAME_REGISTER_STAGE;
            Constants.MENU_LEVEL = Constants.MAIN_MENU_MODE;
            bKey.TOffModeLock();
        }
    }
}
