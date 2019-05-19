package com.rideaustin.manager;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.schedulers.RxSchedulers;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by supreethks on 20/09/16.
 */
public class DefaultSoundManager implements SoundManager {

    private static final float MIN_VOLUME_LEVEL_FACTOR = 0.5f;

    public static final int NO_VALUE = -1;
    public static final int LOW_MUSIC_VOLUME = 1;
    public static final int NO_FLAG = 0;
    @NonNull
    private final AudioManager audioManager;

    private SoundPool soundPool;
    private int streamId;
    private int mediaSoundVolume = NO_VALUE;
    private int alarmSoundVolume = NO_VALUE;

    public DefaultSoundManager(@NonNull AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void playAcceptPendingTone() {
        Timber.d("::playAcceptPendingTone::");
        prepareForPendingTone();
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        soundForcedAlarm(R.raw.ride_request_notification, volume, volume);

    }

    @Override
    public void stopAcceptPendingTone() {
        Timber.d("::stopAcceptPendingTone::");
        stopAlertAlarm();
        restoreAfterPendingTone();
    }

    private void prepareForPendingTone() {
        mediaSoundVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        alarmSoundVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        Timber.d(":: saving music volume: " + mediaSoundVolume);
        int alarmSoundVolumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, LOW_MUSIC_VOLUME, NO_FLAG);
        float currentFactor = alarmSoundVolume / (float) alarmSoundVolumeMax;
        float acceptPendingSoundLevelFactor = currentFactor < MIN_VOLUME_LEVEL_FACTOR ? MIN_VOLUME_LEVEL_FACTOR : currentFactor;
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (int) (acceptPendingSoundLevelFactor * alarmSoundVolumeMax), NO_FLAG);
    }

    private void restoreAfterPendingTone() {
        RxSchedulers.schedule(() -> {
            if (alarmSoundVolume != NO_VALUE) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmSoundVolume, NO_FLAG);
                alarmSoundVolume = NO_VALUE;
            }
            if (mediaSoundVolume != NO_VALUE) {
                Timber.d(":: restoring music volume: " + mediaSoundVolume + " is music on: " + audioManager.isMusicActive());
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaSoundVolume, NO_FLAG);
                mediaSoundVolume = NO_VALUE;
            }
        }, 1, TimeUnit.SECONDS);
    }

    private void soundForcedAlarm(@RawRes int res, final float leftVolume, final float rightVolume) {
        Timber.d("::soundForcedAlarm:: Volume (%f,%f) Sound pool: %s", leftVolume, rightVolume, soundPool);
        if (soundPool != null) {
            return;
        }
        soundPool = createSoundPool();
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            final int VOLUME_MAX = 1;
            pool.play(streamId, VOLUME_MAX, VOLUME_MAX, 1, -1, 1);
        });
        streamId = soundPool.load(App.getInstance(), res, 1);
    }

    private SoundPool createSoundPool() {
        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(1)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                            .build());
            return builder.build();
        } else {
            return new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        }
    }

    private void stopAlertAlarm() {
        Timber.d("::stopAlertAlarm::");
        if (soundPool != null) {
            soundPool.stop(streamId);
            soundPool.release();
            soundPool = null;
        }
    }
}
