package com.csctracker.desktoppluguin.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.csctracker.desktoppluguin.desktop.Core.WAIT_TIME;
import static com.csctracker.desktoppluguin.desktop.Core.isAtivo;

@Slf4j
public class NotificationSync {

    private static final String DB_NAME = ConfigFile.dbNotificationsName();


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
        Long lastArrivalTime = ConfigFile.lastArrivalTime();
        do {
            var xmlMapper = new XmlMapper();
            var jsonMapper = new ObjectMapper();
            try {
                Thread.sleep(WAIT_TIME);
                var notifications = getNotifications(lastArrivalTime);
                for (Notification notification : notifications) {
                    var node = xmlMapper.readTree(notification.getText());
                    lastArrivalTime = notification.getArrivalTime();
                    var json = jsonMapper.writeValueAsString(node);
                    log.info(json);
                    send(json, "message");
                }
                ConfigFile.lastArrivalTime(lastArrivalTime);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                log.info(e.getMessage());
                break;
            }
        } while (isAtivo());
        if (com.csctracker.desktoppluguin.core.Core.isDebug() && !isAtivo()) {
            log.info("Stooped");
        }
    }

    public static void send(String json, String endpoint) {
        System.out.println(json);
        Thread thread = new Thread(() -> {
            String jsonSend = json;
            String uri = "";
            if ("message".equals(endpoint)) {
                Message message = new Message(json);
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    jsonSend = objectMapper.writeValueAsString(message);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                uri = ConfigFile.urlNotifySync();
            }
            HttpURLConnection connection = null;
            try {
                URL url = new URL(uri);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + ConfigFile.tokenCscTracker());
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.connect();

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
                bw.write(jsonSend);
                bw.flush();
                bw.close();

                int response = connection.getResponseCode();
                if (response != 201) {
                    log.error("Response " + response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
        thread.start();
    }

}
