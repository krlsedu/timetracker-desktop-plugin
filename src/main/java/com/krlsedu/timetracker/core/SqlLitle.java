package com.krlsedu.timetracker.core;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SqlLitle {

    private static final String DB_NAME = ".timetracker-desktop-plugin.db";
    private static final String BKPS = "bkps/";

    public static void createNewDatabase(String url) throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        Statement statement = conn.createStatement();
        statement.execute("CREATE TABLE error( json text)");
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
        String fileName = relativePath + "\\bkps\\.timetracker-desktop-plugin" + new SimpleDateFormat("-yyMMdd-hhmmss").format(new Date()) + ".db";
        File file = new File(fileName);
        String url = "jdbc:sqlite:" + fileName;

        if (!(file.exists() && !file.isDirectory())) {
            createNewDatabase(url);
        }

        return url;
    }

    public static void salva(String json) throws Exception {
        String url = getUrl();
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement preparedStatement = conn.prepareStatement("insert into error (json) values (?)");
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
            var errors = getErrors(url);
            for (String erro :
                    errors) {
                salva(erro);
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

    public static void getErrors(ConcurrentLinkedQueue<String> errors) throws SQLException {
        String url = getUrl();
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement preparedStatement = conn.prepareStatement("select * from error");

        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            errors.add(resultSet.getString(1));
        }
        preparedStatement = conn.prepareStatement("delete from error");
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
}
