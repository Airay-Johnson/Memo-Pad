package org.example.entity;

public class AiRequest {
    private String text;
    private String action;
    private String userRequest;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserRequest() {
        return userRequest;
    }

    public void setUserRequest(String userRequest) {
        this.userRequest = userRequest;
    }
}
