package com.live2d.demo.full.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavUtils {
    
    // WAV 文件头结构
    private static final String RIFF_HEADER = "RIFF";
    private static final String WAVE_HEADER = "WAVE";
    private static final String FMT_HEADER = "fmt ";
    private static final String DATA_HEADER = "data";
    
    private static final int HEADER_SIZE = 44; // WAV头固定44字节
    
    /**
     * 将PCM文件转换为WAV文件
     * @param pcmFilePath 输入PCM文件路径
     * @param wavFilePath 输出WAV文件路径
     * @param sampleRate 采样率（如44100）
     * @param channels 声道数（1-单声道，2-立体声）
     * @param bitDepth 位深度（8, 16, 24, 32）
     */
    public static void convertPcmToWav(String pcmFilePath, String wavFilePath, 
                                     int sampleRate, int channels, int bitDepth) throws IOException {
        
        File pcmFile = new File(pcmFilePath);
        if (!pcmFile.exists()) {
            throw new IOException("PCM文件不存在: " + pcmFilePath);
        }
        
        long pcmDataSize = pcmFile.length();
        long wavFileSize = pcmDataSize + HEADER_SIZE;
        
        try (FileInputStream pcmStream = new FileInputStream(pcmFile);
             FileOutputStream wavStream = new FileOutputStream(wavFilePath)) {
            
            // 1. 写入WAV文件头
            writeWavHeader(wavStream, sampleRate, channels, bitDepth, pcmDataSize);
            
            // 2. 写入PCM数据
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = pcmStream.read(buffer)) != -1) {
                wavStream.write(buffer, 0, bytesRead);
            }
        }
    }
    
    /**
     * 写入WAV文件头
     */
    private static void writeWavHeader(FileOutputStream out, int sampleRate, 
                                     int channels, int bitDepth, long dataSize) throws IOException {
        
        int byteRate = sampleRate * channels * bitDepth / 8;
        int blockAlign = channels * bitDepth / 8;
        
        ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
        header.order(ByteOrder.LITTLE_ENDIAN); // WAV使用小端字节序
        
        // RIFF chunk
        header.put(RIFF_HEADER.getBytes()); // ChunkID: "RIFF"
        header.putInt((int) (dataSize + HEADER_SIZE - 8)); // ChunkSize: 文件总大小 - 8
        header.put(WAVE_HEADER.getBytes()); // Format: "WAVE"
        
        // fmt sub-chunk
        header.put(FMT_HEADER.getBytes()); // Subchunk1ID: "fmt "
        header.putInt(16); // Subchunk1Size: PCM格式为16
        header.putShort((short) 1); // AudioFormat: PCM = 1
        header.putShort((short) channels); // NumChannels
        header.putInt(sampleRate); // SampleRate
        header.putInt(byteRate); // ByteRate
        header.putShort((short) blockAlign); // BlockAlign
        header.putShort((short) bitDepth); // BitsPerSample
        
        // data sub-chunk
        header.put(DATA_HEADER.getBytes()); // Subchunk2ID: "data"
        header.putInt((int) dataSize); // Subchunk2Size: PCM数据大小
        
        out.write(header.array());
    }
    
    /**
     * 直接为已存在的PCM文件添加WAV头（原地转换）
     */
    public static void addWavHeaderToPcm(String pcmFilePath, String wavFilePath,
                                       int sampleRate, int channels, int bitDepth) throws IOException {
        
        File pcmFile = new File(pcmFilePath);
        if (!pcmFile.exists()) {
            throw new IOException("PCM文件不存在: " + pcmFilePath);
        }
        
        long pcmDataSize = pcmFile.length();
        
        try (RandomAccessFile wavFile = new RandomAccessFile(wavFilePath, "rw")) {
            // 写入WAV头
            writeWavHeader(wavFile, sampleRate, channels, bitDepth, pcmDataSize);
            
            // 追加PCM数据
            try (FileInputStream pcmStream = new FileInputStream(pcmFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                while ((bytesRead = pcmStream.read(buffer)) != -1) {
                    wavFile.write(buffer, 0, bytesRead);
                }
            }
        }
    }
    
    /**
     * 使用RandomAccessFile写入WAV头
     */
    private static void writeWavHeader(RandomAccessFile file, int sampleRate, 
                                     int channels, int bitDepth, long dataSize) throws IOException {
        
        int byteRate = sampleRate * channels * bitDepth / 8;
        int blockAlign = channels * bitDepth / 8;
        
        // RIFF chunk
        file.write(RIFF_HEADER.getBytes());
        file.writeInt(Integer.reverseBytes((int) (dataSize + HEADER_SIZE - 8)));
        file.write(WAVE_HEADER.getBytes());
        
        // fmt sub-chunk
        file.write(FMT_HEADER.getBytes());
        file.writeInt(Integer.reverseBytes(16));
        file.writeShort(Short.reverseBytes((short) 1));
        file.writeShort(Short.reverseBytes((short) channels));
        file.writeInt(Integer.reverseBytes(sampleRate));
        file.writeInt(Integer.reverseBytes(byteRate));
        file.writeShort(Short.reverseBytes((short) blockAlign));
        file.writeShort(Short.reverseBytes((short) bitDepth));
        
        // data sub-chunk
        file.write(DATA_HEADER.getBytes());
        file.writeInt(Integer.reverseBytes((int) dataSize));
    }
    
    /**
     * 实时流式封装：将PCM数据块封装为WAV格式
     */
    public static class WavStreamEncoder {
        private FileOutputStream out;
        private long dataSize = 0;
        private final int sampleRate;
        private final int channels;
        private final int bitDepth;
        private final String filePath;
        private boolean headerWritten = false;
        
        public WavStreamEncoder(String filePath, int sampleRate, int channels, int bitDepth) {
            this.filePath = filePath;
            this.sampleRate = sampleRate;
            this.channels = channels;
            this.bitDepth = bitDepth;
        }
        
        public void open() throws IOException {
            out = new FileOutputStream(filePath);
            // 先预留44字节的头部空间
            out.write(new byte[HEADER_SIZE]);
        }
        
        public void write(byte[] data) throws IOException {
            write(data, 0, data.length);
        }
        
        public void write(byte[] data, int offset, int length) throws IOException {
            if (out == null) {
                open();
            }
            out.write(data, offset, length);
            dataSize += length;
        }
        
        public void close() throws IOException {
            if (out != null) {
                out.close();
                // 现在回填正确的WAV头
                updateWavHeader();
            }
        }
        
        private void updateWavHeader() throws IOException {
            try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
                int byteRate = sampleRate * channels * bitDepth / 8;
                int blockAlign = channels * bitDepth / 8;
                
                // RIFF chunk
                file.seek(0);
                file.write(RIFF_HEADER.getBytes());
                file.writeInt(Integer.reverseBytes((int) (dataSize + HEADER_SIZE - 8)));
                file.write(WAVE_HEADER.getBytes());
                
                // fmt sub-chunk
                file.write(FMT_HEADER.getBytes());
                file.writeInt(Integer.reverseBytes(16));
                file.writeShort(Short.reverseBytes((short) 1));
                file.writeShort(Short.reverseBytes((short) channels));
                file.writeInt(Integer.reverseBytes(sampleRate));
                file.writeInt(Integer.reverseBytes(byteRate));
                file.writeShort(Short.reverseBytes((short) blockAlign));
                file.writeShort(Short.reverseBytes((short) bitDepth));
                
                // data sub-chunk
                file.write(DATA_HEADER.getBytes());
                file.writeInt(Integer.reverseBytes((int) dataSize));
            }
        }
    }
}