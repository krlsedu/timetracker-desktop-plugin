package com.krlsedu.timetracker.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krlsedu.timetracker.core.model.Heartbeat;
import com.krlsedu.timetracker.desktop.Applications;
import com.krlsedu.timetracker.model.ApplicationDetail;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final ConcurrentLinkedQueue<Heartbeat> heartbeatsQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> resincErrors = new ConcurrentLinkedQueue<>();
    private static boolean debug = true;
    private static ObjectMapper objectMapper = null;
    private static ScheduledFuture<?> scheduledFixture;
    private static ScheduledFuture<?> scheduledFixtureErrors;

    private TimeTrackerCore() {
    }

    public static void init() {
        Applications.init();
        setupDebugging();
        setupQueueProcessor();
    }

    private static String[] buildCliCommand(Heartbeat heartbeat, ArrayList<Heartbeat> extraHeartbeats) {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add(Dependencies.getCLILocation());
        cmds.add("--entity");
        cmds.add(heartbeat.getEntity());
        cmds.add("--time");
        cmds.add(heartbeat.getTimestamp().toPlainString());
        cmds.add("--key");
        cmds.add(ConfigFile.getApiKey());
        if (heartbeat.getProject() != null) {
            cmds.add("--project");
            cmds.add(heartbeat.getProject());
        }
        if (heartbeat.getLanguage() != null) {
            cmds.add("--alternate-language");
            cmds.add(heartbeat.getLanguage());
        }
        if (heartbeat.getEntityType() != null) {
            cmds.add("--entity-type");
            cmds.add(heartbeat.getEntityType());
        }
        if (heartbeat.getCategory() != null) {
            cmds.add("--category");
            cmds.add(heartbeat.getCategory());
        }
        if (heartbeat.getHostName() != null) {
            cmds.add("--hostname");
            cmds.add(heartbeat.getHostName());
        }
        cmds.add("--plugin");
        cmds.add(heartbeat.getIdeName() + "/" + heartbeat.getIdeVersion() + " " + "desktop-wakatime/" + "0.1");
        if (heartbeat.isWrite())
            cmds.add("--write");
        if (!extraHeartbeats.isEmpty())
            cmds.add("--extra-heartbeats");
        return cmds.toArray(new String[0]);
    }

    private static void sendHeartbeat(final Heartbeat heartbeat, final ArrayList<Heartbeat> extraHeartbeats) {
        final String[] cmds = buildCliCommand(heartbeat, extraHeartbeats);
        if (TimeTrackerCore.debug) {
            log.debug("Executing CLI: " + Arrays.toString(obfuscateKey(cmds)));
        }
        try {
            Process proc = Runtime.getRuntime().exec(cmds);
            if (!extraHeartbeats.isEmpty()) {
                String json = toJSON(extraHeartbeats);

                if (TimeTrackerCore.debug) {
                    log.debug(json);
                }

                try {
                    BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
                    stdin.write(json);
                    stdin.write("\n");
                    try {
                        stdin.flush();
                        stdin.close();
                    } catch (IOException e) { /* ignored because wakatime-cli closes pipe after receiving \n */ }
                } catch (IOException e) {
                    log.warn(e);
                }
            }
            if (TimeTrackerCore.debug) {
                BufferedReader stdout = new BufferedReader(new
                        InputStreamReader(proc.getInputStream()));
                BufferedReader stderr = new BufferedReader(new
                        InputStreamReader(proc.getErrorStream()));
                proc.waitFor();
                String s;
                while ((s = stdout.readLine()) != null) {
                    log.debug(s);
                }
                while ((s = stderr.readLine()) != null) {
                    log.debug(s);
                }
                log.debug("Command finished with return value: " + proc.exitValue());
            }
        } catch (Exception e) {
            log.warn(e);
        }
    }


    private static String[] obfuscateKey(String[] cmds) {
        ArrayList<String> newCmds = new ArrayList<>();
        String lastCmd = "";
        for (String cmd : cmds) {
            if ("--key".equals(lastCmd))
                newCmds.add(obfuscateKey(cmd));
            else
                newCmds.add(cmd);
            lastCmd = cmd;
        }
        return newCmds.toArray(new String[0]);
    }

    private static String obfuscateKey(String key) {
        String newKey = null;
        if (key != null) {
            newKey = key;
            if (key.length() > 4)
                newKey = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXX" + key.substring(key.length() - 4);
        }
        return newKey;
    }

    private static String toJSON(ArrayList<Heartbeat> extraHeartbeats) throws JsonProcessingException {
        return getObjectMapper().writeValueAsString(extraHeartbeats);
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

    private static void processHeartbeatQueue() {

        // get single heartbeat from queue
        Heartbeat heartbeat = heartbeatsQueue.poll();
        if (heartbeat == null)
            return;

        // get all extra heartbeats from queue
        ArrayList<Heartbeat> extraHeartbeats = new ArrayList<>();
        List<Heartbeat> timeTrackerHeartbets = new ArrayList<>();
        timeTrackerHeartbets.add(heartbeat);
        while (true) {
            Heartbeat h = heartbeatsQueue.poll();
            if (h == null)
                break;

            timeTrackerHeartbets.add(h);
            if (!heartbeat.isSent()) {
                heartbeat = h;
                continue;
            }
            if (h.isSent()) {
                extraHeartbeats.add(h);
            }
        }

        send(timeTrackerHeartbets);
        if (Dependencies.isTimeTrackerToWakatime()) {
            sendHeartbeat(heartbeat, extraHeartbeats);
        }
    }

    public static void send(List<Heartbeat> heartbeats) {
        String jsonString = null;
        try {
            jsonString = getObjectMapper().writeValueAsString(heartbeats);
            send(jsonString);
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
            if (!Dependencies.isTimeTrackerOffline()) {
                System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
                CloseableHttpClient client = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(Dependencies.urlTimeTracker());

                post.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

                var response = client.execute(post);
                if (response.getStatusLine().getStatusCode() != 201) {
                    SqlLitle.salva(jsonString);
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
        Applications.checkApplicationDetail(applicationDetail);
        heartbeatsQueue.add(applicationDetail.getHeartbeat());
    }

    public static BigDecimal getCurrentTimestamp(ApplicationDetail applicationDetail) {
        return new BigDecimal(String.valueOf(applicationDetail.getDateEnd().getTime() / 1000.0)).setScale(4, RoundingMode.HALF_UP);
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

    public static void setDebug(boolean debug) {
        TimeTrackerCore.debug = debug;
    }
}
