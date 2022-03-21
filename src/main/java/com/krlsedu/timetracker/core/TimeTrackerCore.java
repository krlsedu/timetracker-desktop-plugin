package com.krlsedu.timetracker.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krlsedu.timetracker.core.model.ApplicationDetail;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class TimeTrackerCore {
    public static final Logger log = Logger.getLogger(TimeTrackerCore.class);
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

    private TimeTrackerCore() {
    }

    public static void init() {
        setupDebugging();
        setupQueueProcessor();
    }

    private static void setupQueueProcessor() {
        final Runnable handler = TimeTrackerCore::processHeartbeatQueue;
        final Runnable handlerErros = TimeTrackerCore::resincErrors;
        long delay = QUEUE_TIMEOUT_SECONDS;
        scheduledFixture = scheduler.scheduleAtFixedRate(handler, delay, delay, java.util.concurrent.TimeUnit.SECONDS);
        scheduledFixtureErrors = schedulerErrors.scheduleAtFixedRate(handlerErros, QUEUE_ERRORS_TIMEOUT_SECONDS, QUEUE_ERRORS_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
    }

    public static void stopQueue() {
        scheduledFixture.cancel(true);
        scheduledFixtureErrors.cancel(true);
    }

    private static void resincErrors() {
        if (!ConfigFile.isTimeTrackerOffline()) {
            try {
                SqlLitle.getErrors(resincErrors);
            } catch (SQLException e) {
                log.warn(e);
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

        List<ApplicationDetail> timeTrackerHeartbets = new ArrayList<>();
        while (true) {
            ApplicationDetail h = heartbeatsQueue.poll();
            if (h == null)
                break;

            timeTrackerHeartbets.add(h);
        }

        send(timeTrackerHeartbets);
    }

    public static void send(List<ApplicationDetail> heartbeats) {
        String jsonString = null;
        try {
            if (heartbeats != null && heartbeats.isEmpty()) {
                jsonString = getObjectMapper().writeValueAsString(heartbeats);
                send(jsonString);
            }
        } catch (Exception e) {
            try {
                SqlLitle.salva(jsonString);
            } catch (Exception ex) {
                log.warn(ex);
            }
            log.warn(e);
        }
    }

    private static void send(String jsonString) {
        try {
            if (!ConfigFile.isTimeTrackerOffline()) {
                System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
                CloseableHttpClient client = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(ConfigFile.urlTimeTracker());

                post.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

                var response = client.execute(post);
                if (response.getStatusLine().getStatusCode() != 201) {
                    SqlLitle.salva(jsonString);
                    log.warn(response);
                }
            } else {
                SqlLitle.salva(jsonString);
            }
        } catch (Exception e) {
            try {
                SqlLitle.salva(jsonString);
            } catch (Exception ex) {
                log.warn(ex);
            }
            log.warn(e);
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
        TimeTrackerCore.debug = debug != null && debug.trim().equals("true");
    }

    public static boolean isDebug() {
        return debug;
    }
}
