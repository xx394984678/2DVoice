package com.live2d.demo.full.entity;

public class ApiServerEntity {

    private String session_id;
    private String message_id;
    private boolean message_end_status;
    private String message;
    private String ai_tag;

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public boolean isMessage_end_status() {
        return message_end_status;
    }

    public void setMessage_end_status(boolean message_end_status) {
        this.message_end_status = message_end_status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAi_tag() {
        return ai_tag;
    }

    public void setAi_tag(String ai_tag) {
        this.ai_tag = ai_tag;
    }


}
