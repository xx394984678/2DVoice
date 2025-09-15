package com.live2d.demo.full.entity;

public class CreateRoomEntity {

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
        private String room_id;
        private String room_token;

        public String getRoom_id() {
            return room_id;
        }

        public void setRoom_id(String room_id) {
            this.room_id = room_id;
        }

        public String getRoom_token() {
            return room_token;
        }

        public void setRoom_token(String room_token) {
            this.room_token = room_token;
        }
    }
}
