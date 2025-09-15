package com.live2d.demo.full.entity;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtitleWrapEntity {

    private List<SubtitleMsgData> data;
    private String type;

    public List<SubtitleMsgData> getData() {
        return data;
    }

    public void setData(List<SubtitleMsgData> data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    // 拆包校验
    public static boolean unpack(ByteBuffer message, StringBuilder subtitles) {
        final int kSubtitleHeaderSize = 8;
        if (message.remaining() < kSubtitleHeaderSize) {
            return false;
        }

        // 魔法数字 "subv"
        int magicNumber = (message.get() << 24) | (message.get() << 16) | (message.get() << 8) | (message.get());
        if (magicNumber != 0x73756276) {
            return false;
        }

        int length = message.getInt();

        if (message.remaining() != length) {
            return false;
        }

        // 读取字幕内容
        byte[] subtitleBytes = new byte[length];
        message.get(subtitleBytes);
        subtitles.append(new String(subtitleBytes, StandardCharsets.UTF_8));

        return true;
    }
}
