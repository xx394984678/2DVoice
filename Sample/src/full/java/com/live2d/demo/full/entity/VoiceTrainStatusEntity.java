package com.live2d.demo.full.entity;

public class VoiceTrainStatusEntity {

    private String error_code;
    private Object error_info;
    private String message;
    private String date;
    private ContentDTO content;

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public Object getError_info() {
        return error_info;
    }

    public void setError_info(Object error_info) {
        this.error_info = error_info;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ContentDTO getContent() {
        return content;
    }

    public void setContent(ContentDTO content) {
        this.content = content;
    }

    public static class ContentDTO {
        private int status;
        private String voice_id;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getVoice_id() {
            return voice_id;
        }

        public void setVoice_id(String voice_id) {
            this.voice_id = voice_id;
        }
    }
}
