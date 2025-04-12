package com.g.media.uploader.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class SleepUtils {
    public static void sleepSecond(Integer second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException ex) {
            log.warn("sleep error", ex);
        }
    }
}
