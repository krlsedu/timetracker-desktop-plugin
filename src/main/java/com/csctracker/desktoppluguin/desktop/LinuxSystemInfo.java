package com.csctracker.desktoppluguin.desktop;


import lombok.extern.slf4j.Slf4j;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.csctracker.desktoppluguin.desktop.SystemInfo.SECONDS_TO_IDLE;

@Slf4j
public class LinuxSystemInfo implements NativeMouseInputListener, NativeKeyListener {

    private SystemInfo.State state = SystemInfo.State.UNKNOWN;

    public Long timerInit = new Date().getTime();

    public void nativeMouseClicked(NativeMouseEvent e) {
        resetTimer();
    }

    public void nativeMousePressed(NativeMouseEvent e) {
        resetTimer();
    }

    public void nativeMouseReleased(NativeMouseEvent e) {
        resetTimer();
    }

    public void nativeMouseMoved(NativeMouseEvent e) {
        resetTimer();
    }

    public void nativeMouseDragged(NativeMouseEvent e) {
        resetTimer();
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        resetTimer();
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        resetTimer();
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        resetTimer();
    }

    public void resetTimer() {
        timerInit = new Date().getTime();
    }

    public boolean isChangedState() {
        SystemInfo.State newState = getState();
        if (newState != state) {
            state = newState;
            log.info(state.toString());
            return true;
        }
        return false;
    }

    private SystemInfo.State getState() {
        var idleSec = (int) ((new Date().getTime() - timerInit) / 1000);
        return idleSec < SECONDS_TO_IDLE ? SystemInfo.State.ONLINE : SystemInfo.State.IDLE;
    }

    public boolean isOnline() {
        return getState().equals(SystemInfo.State.ONLINE);
    }

    public void start() {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            log.error("There was a problem registering the native hook.");
            log.error(ex.getMessage());
        }
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
		GlobalScreen.addNativeKeyListener(this);
    }
}