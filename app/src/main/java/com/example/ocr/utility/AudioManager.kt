package com.example.ocr.utility

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast


class AudioManager{
    private var mediaPlayer = MediaPlayer()
    private var currentFileName : String? = null

    fun playAudioFile(context: Context,fileName:String){

        try {
            if(fileName!=currentFileName) {

                mediaPlayer = MediaPlayer()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                );

                val audioFile = Uri.parse(fileName)
                mediaPlayer.setDataSource(context, audioFile)
                mediaPlayer.prepare()
                mediaPlayer.start()
                currentFileName = fileName
            }else{
                mediaPlayer.start()
            }

        }catch (ex:Exception){
            Toast.makeText(context,"Unable to play audio", Toast.LENGTH_SHORT).show()
        }

    }

}