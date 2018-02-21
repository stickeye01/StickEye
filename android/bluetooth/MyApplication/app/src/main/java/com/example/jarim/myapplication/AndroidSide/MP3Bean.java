package com.example.jarim.myapplication.AndroidSide;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.widget.EditText;

import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by hochan on 2018-02-07.
 */

public class MP3Bean extends AppBean {
    private ArrayList<MusicDto> list;
    public MP3Bean(String _name, String _intentName, TtsService _tts, Context _ctx) {
        super(_name, _intentName, _tts, _ctx);
        getMusicList();

    }


    @TargetApi(Build.VERSION_CODES.O)
    private void getMusicList() {
        list = new ArrayList<>();

        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST};
        Cursor cursor =
                mContext.getContentResolver().query((Uri)MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null);

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

