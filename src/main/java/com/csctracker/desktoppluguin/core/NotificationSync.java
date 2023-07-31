package com.csctracker.desktoppluguin.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.csctracker.desktoppluguin.desktop.Core.WAIT_TIME;
import static com.csctracker.desktoppluguin.desktop.Core.isAtivo;

@Slf4j
public class NotificationSync {

    private static final String DB_NAME = Configs.dbNotificationsName();
    private static final ConcurrentLinkedQueue<String> resincErrors = new ConcurrentLinkedQueue<>();


    public static List<Notification> getNotifications(Long lastArrivalTime) throws SQLException, ClassNotFoundException {
        var conn = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
        var preparedStatement = conn.prepareStatement("select Payload, ArrivalTime, * from Notification where Type = 'toast' and ArrivalTime > " + lastArrivalTime + " order by ArrivalTime asc ");

        var resultSet = preparedStatement.executeQuery();
        var notifications = new ArrayList<Notification>();
        while (resultSet.next()) {
            var notification = new Notification();
            notification.setText(resultSet.getString(1));
            notification.setArrivalTime(resultSet.getLong(2));
            notifications.add(notification);
        }
        conn.close();
        return notifications;
    }

    public static void notificationTracker() {
        var thread = new Thread(NotificationSync::notificationTrackerTread);
        thread.start();
    }

    public static void notificationTrackerTread() {
        Thread thread = new Thread(NotificationSync::resincErrors);
        thread.start();
        Long lastArrivalTime = Configs.lastArrivalTime();
        log.info("NotificationSync Started");
        do {
            lastArrivalTime = checkNotifications(lastArrivalTime);
        } while (isAtivo());
        log.warn("NotificationSync Stoped");
        if (isAtivo()) {
            notificationTracker();
        }
    }

    private static Long checkNotifications(Long lastArrivalTime) {
        var xmlMapper = new XmlMapper();
        var jsonMapper = new ObjectMapper();
        try {
            Thread.sleep(WAIT_TIME);
            var notifications = getNotifications(lastArrivalTime);
            for (Notification notification : notifications) {
                var node = xmlMapper.readTree(notification.getText());
                lastArrivalTime = notification.getArrivalTime();
                var json = jsonMapper.writeValueAsString(node);
                if (!json.contains("Notification incoming from")) {
                    sendJson(json);
                } else {
                    if (Configs.isDebug()) {
                        log.info("Notification descartada: " + json);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            Configs.lastArrivalTime(lastArrivalTime);
        }
        Configs.lastArrivalTime(lastArrivalTime);
        resincErrors();
        return lastArrivalTime;
    }

    private static void resincErrors() {
        do {
            try {
                Thread.sleep(WAIT_TIME * 10);
                try {
                    SqlLitle.getErrors(resincErrors, "notify-error");
                } catch (SQLException e) {
                    log.warn(e.getMessage());
                }

                while (true) {
                    String jsonString = resincErrors.poll();
                    if (jsonString == null) {
                        return;
                    }
                    send(jsonString);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (isAtivo());
    }

    private static void sendJson(String json) {
        var jsonSend = json;
        var message = new Message(json);
        try {
            var objectMapper = new ObjectMapper();
            jsonSend = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        send(jsonSend);
    }

    public static void send(String jsonSend) {

        String url = Configs.urlNotifySync();
        try {
            var urlProxy = Configs.urlProxy();
            var port = Configs.portProxy();
            HttpRequestWithBody post = Unirest.post(url);
            if (urlProxy != null && port != null) {
                post.proxy(urlProxy, port);
            }
            var response = post
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + Configs.tokenCscTracker())
                    .body(jsonSend).asString();
            if (response.getStatus() < 200 || response.getStatus() > 299) {
                Configs.changeSubDomain();
                SqlLitle.salva(jsonSend, "notify-error");
                log.info(response.toString());
            }
        } catch (Exception e) {
            Configs.changeSubDomain();
            try {
                SqlLitle.salva(jsonSend, "notify-error");
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
            log.error(e.getMessage());
        }
    }

}
