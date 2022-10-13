package com.csctracker.desktoppluguin.core;

import com.csctracker.desktoppluguin.desktop.SystemInfo;

public class Message {

    private String from;
    private String text;
    private String app;
    public Message(String text) {
        this.text = text;
        this.app = "CscTrackerDesktop";
        this.from = SystemInfo.getHostName();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}
