/*
 * Copyright (C) Jenly, CameraScan Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.king.camera.scan.manager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import com.king.camera.scan.R;
import com.king.logx.LogX;

import java.io.Closeable;

/**
 * 音效管理器：主要用于播放蜂鸣提示音和振动效果
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
public final class BeepManager implements Closeable {

    private static final long VIBRATE_DURATION = 50L;

    private final Context context;
    private SoundPool mSoundPool;
    private int soundId = 0;
    private Vibrator vibrator;
    private boolean playBeep;
    private boolean vibrate;

    public BeepManager(Context context) {
        this.context = context;
        updatePrefs();
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public void setPlayBeep(boolean playBeep) {
        this.playBeep = playBeep;
    }

    private void updatePrefs() {
        if (mSoundPool == null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
            mSoundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

            soundId = mSoundPool.load(context, R.raw.camera_scan_beep, 1);
        }
        if (vibrator == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibrator = ((VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)).getDefaultVibrator();
            } else {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }
        }
    }

    public void playBeepSoundAndVibrate() {
        if (playBeep && mSoundPool != null) {
            mSoundPool.play(soundId, 1f, 1f, 1, 0, 1f);
        }
        if (vibrate && vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(VIBRATE_DURATION);
            }
        }
    }

    @Override
    public void close() {
        try {
            if (mSoundPool != null) {
                mSoundPool.release();
                mSoundPool = null;
            }
        } catch (Exception e) {
            LogX.w(e);
        }
    }

}
