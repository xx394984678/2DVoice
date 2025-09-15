package com.live2d.demo.full.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class PCMAudioPlayer {
    private AudioTrack audioTrack;
    private int sampleRate = 16000; // 默认采样率
    private int channelConfig = AudioFormat.CHANNEL_OUT_MONO; // 默认立体声
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 默认16位
    private int bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private PlaybackThread playbackThread;
    private boolean isPlaying = false;

    public PCMAudioPlayer() {
        // 默认构造函数
    }

    // 设置采样率
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        updateBufferSize();
    }

    // 设置声道配置
    public void setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
        updateBufferSize();
    }

    // 设置音频格式
    public void setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
        updateBufferSize();
    }

    // 更新缓冲区大小
    private void updateBufferSize() {
        bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    }

    // 初始化AudioTrack
    public void initialize() {
        if (audioTrack != null) {
            audioTrack.release();
        }
        
        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM);
    }

    // 开始播放
    public void startPlayback() {
        if (audioTrack == null) {
            initialize();
        }
        
        if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrack.play();
            isPlaying = true;
        }
    }

    // 写入PCM数据
    public void writeData(byte[] data, int offset, int length) {
        if (audioTrack != null && isPlaying) {
            audioTrack.write(data, offset, length);
        }
    }

    // 停止播放
    public void stopPlayback() {
        if (audioTrack != null) {
            isPlaying = false;
            audioTrack.stop();
        }
    }

    // 释放资源
    public void release() {
        if (audioTrack != null) {
            isPlaying = false;
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    // 获取播放状态
    public boolean isPlaying() {
        return isPlaying;
    }

    // 设置循环播放模式（可选）
    public void setLooping(boolean looping) {
        if (audioTrack != null) {
            // 注意：MODE_STATIC模式下才支持setLoopPoints
            // 对于流模式，需要在应用层实现循环逻辑
        }
    }

    // 异步播放线程（可选，用于持续播放流数据）
    private class PlaybackThread extends Thread {
        private byte[] pcmData;
        
        public PlaybackThread(byte[] pcmData) {
            this.pcmData = pcmData;
        }
        
        @Override
        public void run() {
            startPlayback();
            if (pcmData != null) {
                writeData(pcmData, 0, pcmData.length);
            }
        }
    }

    // 使用线程播放一段PCM数据（可选）
    public void playPCMData(byte[] pcmData) {
        if (playbackThread != null) {
            playbackThread.interrupt();
        }
        playbackThread = new PlaybackThread(pcmData);
        playbackThread.start();
    }
}