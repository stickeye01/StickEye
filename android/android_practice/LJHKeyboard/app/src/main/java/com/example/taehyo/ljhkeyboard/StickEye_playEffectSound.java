package com.example.taehyo.ljhkeyboard;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by taehyo on 2018-02-11.
 */

public class StickEye_playEffectSound  {
    private int resID = -1;
    private Context context;
    private MediaPlayer player;
    private boolean bRepeat = false;

    public StickEye_playEffectSound(Context context) {
        this.context = context;
    }

    //리소스 설정
    public void setResID(int resID) {
        this.resID = resID;
    }
    public int getResID() {
        return resID;
    }

    //반복설정
    public void setRepeat(boolean bRepeat){
        this.bRepeat = bRepeat;
    }
    public boolean getRepeat() {
        return bRepeat;
    }

    //리소스 설정 해제
    public void clear() {
        this.resID = -1;
    }

    //중지
    public void stopEffect(){
        if(player==null) return;
        if(player.isPlaying()==true) player.stop();
    }

    //리소스 재생, 리소스ID가 -1이면 재생하지 않는다.
    public void play() {
        if (context == null) return;
        if (this.resID == -1) return;
        stopEffect();
        try {
            player = MediaPlayer.create(context, this.resID);
            player.setLooping(bRepeat);
            player.start();
        } catch (NullPointerException e) {
        }
    }
}
