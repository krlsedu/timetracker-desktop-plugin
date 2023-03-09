package com.csctracker.desktoppluguin.core;

import com.csctracker.desktoppluguin.core.model.ApplicationDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

@Slf4j
public class Core {
    public static final int QUEUE_TIMEOUT_SECONDS = 10;
    public static final int QUEUE_ERRORS_TIMEOUT_SECONDS = 90;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final ScheduledExecutorService schedulerErrors = Executors.newScheduledThreadPool(1);
    private static final ConcurrentLinkedQueue<ApplicationDetail> heartbeatsQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> resincErrors = new ConcurrentLinkedQueue<>();
    private static boolean debug = true;
    private static ObjectMapper objectMapper = null;
    private static ScheduledFuture<?> scheduledFixture;
    private static ScheduledFuture<?> scheduledFixtureErrors;

    private Core() {
    }

    public static void init() {
        setupDebugging();
        setupQueueProcessor();
    }

    private static void setupQueueProcessor() {
        final Runnable handler = Core::processHeartbeatQueue;
        final Runnable handlerErros = Core::resincErrors;
        long delay = QUEUE_TIMEOUT_SECONDS;
        scheduledFixture = scheduler.scheduleAtFixedRate(handler, delay, delay, java.util.concurrent.TimeUnit.SECONDS);
        scheduledFixtureErrors = schedulerErrors.scheduleAtFixedRate(handlerErros, QUEUE_ERRORS_TIMEOUT_SECONDS, QUEUE_ERRORS_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
    }

    public static void stopQueue() {
        scheduledFixture.cancel(true);
        scheduledFixtureErrors.cancel(true);
    }

    private static void resincErrors() {
        if (!ConfigFile.isCscTrackerOffline()) {
            try {
                SqlLitle.getErrors(resincErrors);
            } catch (SQLException e) {
                log.warn("58", e.getMessage());
            }

            while (true) {
                String jsonString = resincErrors.poll();
                if (jsonString == null) {
                    return;
                }
                send(jsonString);
            }
        }
    }

    private static void processHeartbeatQueue() {

        List<ApplicationDetail> cscTrackerHeartbets = new ArrayList<>();
        while (true) {
            ApplicationDetail h = heartbeatsQueue.poll();
            if (h == null)
                break;

            cscTrackerHeartbets.add(h);
        }

        send(cscTrackerHeartbets);
    }

    public static void send(List<ApplicationDetail> heartbeats) {
        String jsonString = null;
        try {
            if (heartbeats != null && !heartbeats.isEmpty()) {
                jsonString = getObjectMapper().writeValueAsString(heartbeats);
                send(jsonString);
            }
        } catch (Exception e) {
            try {
                SqlLitle.salva(jsonString);
            } catch (Exception ex) {
                log.warn("96", ex.getMessage());
            }
            log.warn("98", e.getMessage());
        }
    }

    private static void send(String jsonString) {
        try {
            if (!ConfigFile.isCscTrackerOffline()) {
                System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

                var urlProxy = ConfigFile.urlProxy();
                var port = ConfigFile.portProxy();
                HttpRequestWithBody post = Unirest.post(ConfigFile.urlCscTracker());
                if (urlProxy != null && port != null) {
                    post.proxy(urlProxy, port);
                }
                var response = post
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + ConfigFile.tokenCscTracker())
                        .body(jsonString)
                        .asString();
                if (response.getStatus() < 200 || response.getStatus() > 299) {
                    SqlLitle.salva(jsonString);
                    log.warn(response.getBody());
                }
            } else {
                SqlLitle.salva(jsonString);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                SqlLitle.salva(jsonString);
            } catch (Exception ex) {
                log.warn("129", ex.getMessage());
            }
            log.warn("131", e.getMessage());
        }
    }

    public static void appendHeartbeat(final ApplicationDetail applicationDetail) {
        heartbeatsQueue.add(applicationDetail);
    }

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }

    public static void setupDebugging() {
        String debug = ConfigFile.get("settings", "debug");
        Core.debug = debug != null && debug.trim().equals("true");
        PrintStream fileStream = null;
        try {
            fileStream = new PrintStream(ConfigFile.getResourcesLocation() + "\\csctracker-desktop-plugin.log");
            System.setOut(fileStream);
            System.setErr(fileStream);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }
    }

    public static boolean isDebug() {
        return debug;
    }
}
