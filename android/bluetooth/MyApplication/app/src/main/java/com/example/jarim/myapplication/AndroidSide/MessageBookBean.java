package com.example.jarim.myapplication.AndroidSide;

import android.content.Context;
import android.widget.EditText;

import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

/**
 * Created by lhc on 2018-02-12.
 */

public class MessageBookBean extends AppBean {
    EditText input_etext;

    public MessageBookBean(String _name, String _intentName, TtsService _tts, Context _ctx) {
        super(_name, _intentName, _tts, _ctx);
        input_etext = (EditText) mActivity.findViewById(R.id.test_input);
    }

}
