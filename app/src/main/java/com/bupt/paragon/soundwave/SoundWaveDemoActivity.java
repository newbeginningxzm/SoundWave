package com.bupt.paragon.soundwave;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;

import com.bupt.paragon.sound_wave.SoundWave;

/**
 * Created by paragon on 16/10/30.
 */
public class SoundWaveDemoActivity extends Activity{

    private SoundWave mSoundWave;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.sound_wave_demo_activity);
        mSoundWave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mSoundWave.isPlaying()){
                    mSoundWave.stopPlay();
                }else{
                    mSoundWave.startPlay();
                }
            }
        });
    }
}
