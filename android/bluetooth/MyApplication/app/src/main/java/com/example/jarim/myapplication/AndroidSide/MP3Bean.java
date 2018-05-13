package com.example.jarim.myapplication.AndroidSide;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.EditText;

import com.example.jarim.myapplication.BrailleKeyboard.BrailleKeyboard;
import com.example.jarim.myapplication.Constants;
import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by hochan on 2018-02-07.
 */

public class MP3Bean extends AppBean {
    private ArrayList<MusicDto> list;
    private MediaPlayer mediaPlayer;
    private int horizontal_index = 0;
    private int wantToBackMain = 0;
    private int isMusicOn = 1;

    public MP3Bean(String _name, String _intentName, TtsService _tts, Context _ctx,
                   BrailleKeyboard _bKey) {
        super(_name, _intentName, _tts, _ctx, _bKey);
        mediaPlayer = new MediaPlayer();
        list = new ArrayList<>();
        getMusicList();
    }


    @Override
    public boolean start(Object o) {
        tts.ispeak("MP3가 실행되었습니다.");
        Constants.MENU_LEVEL = Constants.SUB_MENU_MODE;
        if (isMusicOn == 1 && horizontal_index < list.size()) playMusic(list.get(horizontal_index));
        return true;
    }

    @Override
    public void left() {
        horizontal_index --;
        wantToBackMain = 0;
        if (horizontal_index < 0) horizontal_index = list.size();
        if (horizontal_index == list.size()) {
            tts.ispeak("메인 메뉴로 돌아가기");
            menu_txt.setText("메인 메뉴로 돌아가기");
        } else {
            tts.ispeak(list.get(horizontal_index).getTitle());
            menu_txt.setText("MP3:"+list.get(horizontal_index).getTitle());
            if (isMusicOn == 1) playMusic(list.get(horizontal_index));
        }
    }

    @Override
    public void right() {
        wantToBackMain = 0;
        horizontal_index ++;
        if (horizontal_index == list.size()) {
            tts.ispeak("메인 메뉴로 돌아가기");
        } else {
            if (horizontal_index > list.size()) horizontal_index = 0;
            tts.ispeak(list.get(horizontal_index).getTitle());
            menu_txt.setText("MP3:"+list.get(horizontal_index).getTitle());
            if (isMusicOn == 1) playMusic(list.get(horizontal_index));
        }
    }

    @Override
    public void top() {
        wantToBackMain = 1;
        tts.ispeak("메인 메뉴로 돌아가기");
    }

    @Override
    public void down() {
        wantToBackMain = 1;
        tts.ispeak("메인 메뉴로 돌아가기");
    }

    @Override
    public void click() {
        if (horizontal_index == list.size() || wantToBackMain == 1) {
            tts.ispeak("메인 메뉴로 돌아갑니다.");
            wantToBackMain = 1;
            Constants.MENU_LEVEL = Constants.MAIN_MENU_MODE;
            if (isMusicOn == 1) {
                isMusicOn = 0;
                stopMusic();
            }
        } else {
            if (!list.isEmpty() && horizontal_index < list.size()) {
                // 종료 혹은 실행.
                if (isMusicOn == 1) { // 이미 실행되고 있으므로 멈춘다.
                    isMusicOn = 0;
                    stopMusic();
                } else {
                    isMusicOn = 1;
                    playMusic(list.get(horizontal_index));
                }
            }
        }
    }

    /**
     * get music lists that are saved in the phone.
     */
    private void getMusicList() {
        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST};

        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        while (cursor.moveToNext()) {
            MusicDto musicDto = new MusicDto();
            musicDto.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            musicDto.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
            musicDto.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            musicDto.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            list.add(musicDto);
        }
        cursor.close();
    }

    /**
     * play music.
     * @param musicDto
     */
    private void playMusic(MusicDto musicDto) {
        Uri musicURI = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                    ""+musicDto.getId());
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mContext, musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        mediaPlayer.pause();
    }


    private class MusicDto implements Serializable {
        private String id;
        private String albumID;
        private String title;
        private String artist;

        public MusicDto() {}

        public MusicDto(String id, String albumID, String title, String artist) {
            this.id = id;
            this.albumID = albumID;
            this.title = title;
            this.artist = artist;
        }
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAlbumId() {
            return albumID;
        }

        public void setAlbumId(String albumId) {
            this.albumID = albumId;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return "MusicDto{" +
                    "id='" + id + '\'' +
                    ", albumId='" + albumID + '\'' +
                    ", title='" + title + '\'' +
                    ", artist='" + artist + '\'' +
                    '}';
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }
    }
}

