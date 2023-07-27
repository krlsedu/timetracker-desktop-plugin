package com.csctracker.desktoppluguin.core;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SqlLitle {

    private static final String DB_NAME = ".csctracker-desktop-plugin.db";
    private static final String BKPS = "bkps/";

    public static void createNewDatabase(String url) throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        Statement statement = conn.createStatement();
        statement.execute("CREATE TABLE error( json text)");
        statement.execute("CREATE TABLE sync( url text)");
        statement.execute("CREATE TABLE notify_error( json text)");
        statement.execute("CREATE TABLE configs( name text PRIMARY KEY, value text)");
        statement.execute("insert into configs (name, value) values ('lastArrivalTime', '0')");
        statement.execute("insert into configs (name, value) values ('subdomainActive', '1')");
        statement.execute("insert into configs (name, value) values ('urlCscTracker', '/backend/usage-info')");
        statement.execute("insert into configs (name, value) values ('dbNotificationsName', '\\AppData\\Local\\Microsoft\\Windows\\Notifications\\wpndatabase.db')");
        statement.execute("insert into configs (name, value) values ('urlNotifySync', '/notify-sync/message/')");
        statement.execute("insert into configs (name, value) values ('heartbeatMaxTimeSeconds', '60')");

        var tokenCscTracker = ConfigFile.get("settings", "tokenCscTracker");
        statement.execute("insert into configs (name, value) values ('tokenCscTracker', '" + tokenCscTracker + "')");

        var subdomains = ConfigFile.get("settings", "subdomains");
        if (subdomains == null) {
            subdomains = "gtw,back";
        }
        statement.execute("insert into configs (name, value) values ('subdomains', '" + subdomains + "')");

        var domain = ConfigFile.get("settings", "domain");
        if (domain == null) {
            domain = "https://subdomain.csctracker.com";
        }
        statement.execute("insert into configs (name, value) values ('domain','" + domain + "')");

        var debug = ConfigFile.get("settings", "debug");
        if (debug == null) {
            debug = "false";
        }
        statement.execute("insert into configs (name, value) values ('debug', '" + debug + "')");
        conn.close();
    }

    private static String getUrl() throws SQLException {
        String relativePath = ConfigFile.getResourcesLocation();
        File file = new File(relativePath + "\\" + DB_NAME);
        String url = "jdbc:sqlite:" + relativePath + "\\" + DB_NAME;

        if (!(file.exists() && !file.isDirectory())) {
            createNewDatabase(url);
        }

        return url;
    }

    private static String getUrlBkp() throws SQLException {
        String relativePath = ConfigFile.getResourcesLocation();
        String fileName = relativePath + "\\bkps\\.csctracker-desktop-plugin" + new SimpleDateFormat("-yyMMdd-hhmmss").format(new Date()) + ".db";
        File file = new File(fileName);
        String url = "jdbc:sqlite:" + fileName;

        if (!(file.exists() && !file.isDirectory())) {
            createNewDatabase(url);
        }

        return url;
    }

    public static void salvaUrl(String urlSalvar) throws Exception {
        String url = getUrl();
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement preparedStatement = conn.prepareStatement("insert into sync (url) values (?)");
        preparedStatement.setString(1, urlSalvar);
        preparedStatement.execute();
        conn.close();
    }

    public static void salva(String json) throws Exception {
        salva(json, "usage-info");
    }

    public static void salva(String json, String type) throws Exception {
        String url = getUrl();
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement preparedStatement = null;
        if (Objects.equals(type, "notify-error")) {
            preparedStatement = conn.prepareStatement("insert into notify_error (json) values (?)");
        } else {
            preparedStatement = conn.prepareStatement("insert into error (json) values (?)");
        }
        preparedStatement.setString(1, json);
        preparedStatement.execute();
        conn.close();
    }

    public static void salvaBkp(String json, String url) throws Exception {
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement preparedStatement = conn.prepareStatement("insert into error (json) values (?)");
        preparedStatement.setString(1, json);
        preparedStatement.execute();
        conn.close();
    }

    public static void syncBkps() throws Exception {
        var urls = getUrlsBkps();
        for (String url :
                urls) {
            if (!isUrlSync(url)) {
                var errors = getErrors(url);
                for (String erro :
                        errors) {
                    salva(erro);
                }
                salvaUrl(url);
            }
        }
        removeBkps();
    }

    public static void generateBackup() throws Exception {
        var url = getUrl();
        var urlBkp = getUrlBkp();
        var errors = getErrors(url);
        for (String erro :
                errors) {
            salvaBkp(erro, urlBkp);
        }
    }

    private static void removeBkps() throws Exception {

        String relativePath = ConfigFile.getResourcesLocation();
        File folder = new File(relativePath + "\\" + BKPS);
        File[] folderFiles = folder.listFiles();
        for (File file :
                folderFiles) {
            if (!file.delete()) {
                throw new Exception("Não foi possivel excluir! " + relativePath + "\\" + BKPS + file);
            }
        }
    }

    private static List<String> getUrlsBkps() throws SQLException {
        String relativePath = ConfigFile.getResourcesLocation();
        File folder = new File(relativePath + "\\" + BKPS);
        File[] folderFiles = folder.listFiles();
        var urls = new ArrayList<String>();

        for (File file :
                folderFiles) {
            String url = "jdbc:sqlite:" + relativePath + "\\" + BKPS + file.getName();
            urls.add(url);
        }

        return urls;
    }

    public static boolean isUrlSync(String urlCheck) throws SQLException {
        String url = getUrl();
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement preparedStatement = conn.prepareStatement("select * from sync where url = ?");

        preparedStatement.setString(1, urlCheck);
        ResultSet resultSet = preparedStatement.executeQuery();
        Boolean exist = resultSet.next();
        preparedStatement = conn.prepareStatement("delete from error");
        preparedStatement.execute();
        conn.close();
        return exist;
    }

    public static void getErrors(ConcurrentLinkedQueue<String> errors) throws SQLException {
        getErrors(errors, "usage-info");
    }

    public static void getErrors(ConcurrentLinkedQueue<String> errors, String type) throws SQLException {
        String url = getUrl();
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement preparedStatement = null;
        if (Objects.equals(type, "notify-error")) {
            preparedStatement = conn.prepareStatement("select * from notify_error");
        } else {
            preparedStatement = conn.prepareStatement("select * from error");
        }

        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            errors.add(resultSet.getString(1));
        }

        if (Objects.equals(type, "notify-error")) {
            preparedStatement = conn.prepareStatement("delete from notify_error");
        } else {
            preparedStatement = conn.prepareStatement("delete from error");
        }

        preparedStatement.execute();
        conn.close();
    }

    public static List<String> getErrors(String url) throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement preparedStatement = conn.prepareStatement("select * from error");

        ResultSet resultSet = preparedStatement.executeQuery();
        List<String> errors = new ArrayList<>();
        while (resultSet.next()) {
            errors.add(resultSet.getString(1));
        }
        preparedStatement = conn.prepareStatement("delete from error");
        preparedStatement.execute();
        conn.close();
        return errors;
    }

    public static String getConfig(String name) {
        try {
            Connection conn = DriverManager.getConnection(getUrl());
            PreparedStatement preparedStatement = conn.prepareStatement("select value from configs where name = ?");
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();
            var result = "0";
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
            conn.close();
            return result;
        } catch (Exception e) {
        }
        return "0";
    }


    public static void saveConfig(String name, String value) {
        try {
            Connection conn = DriverManager.getConnection(getUrl());
            PreparedStatement preparedStatement = conn.prepareStatement("insert into configs (name, value) values (?, ?)" +
                    " on conflict(name) do update set value = ?");
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, value);
            preparedStatement.setString(3, value);
            preparedStatement.execute();
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
