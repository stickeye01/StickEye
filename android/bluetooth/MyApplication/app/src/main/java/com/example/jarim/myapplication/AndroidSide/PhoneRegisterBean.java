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

import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

import java.util.ArrayList;

/**
 * Created by lhc on 2018-02-09.
 */

public class PhoneRegisterBean extends AppBean implements View.OnClickListener {
    private EditText input_etext;
    private Button click_button;
    private final int NAME_REGISTER_STAGE = 0;
    private final int PHONE_NUM_REG_STAGE = 1;
    private final int REGISTER_FINAL_STAGE = 2;
    private int no_degree = NAME_REGISTER_STAGE;

    private ArrayList <ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

    private String name;
    private String phoneNum;

    public PhoneRegisterBean(String _name, String _intentName, TtsService _tts, Context _ctx) {
        super(_name, _intentName, _tts, _ctx);
        input_etext = (EditText) mActivity.findViewById(R.id.test_input);
        click_button = (Button) mActivity.findViewById(R.id.click2);
        click_button.setOnClickListener(this);
    }

    @Override
    public boolean start(Object o) {
        tts.sspeak("이름을 입력하세요.");
        input_etext.setText("");
        input_etext.requestFocus();
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.click2) {
            if (no_degree == NAME_REGISTER_STAGE) {
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
                tts.sspeak("번호를 입력하세요.");
                no_degree = PHONE_NUM_REG_STAGE;
            } else if (no_degree == PHONE_NUM_REG_STAGE) {
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
                tts.sspeak("등록하시겠습니까? 등록하시려면 click button을 누르세요.");
                no_degree = REGISTER_FINAL_STAGE;
            } else if (no_degree == REGISTER_FINAL_STAGE) {
                try {
                    mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                no_degree = NAME_REGISTER_STAGE;
            }
        }
    }
}
