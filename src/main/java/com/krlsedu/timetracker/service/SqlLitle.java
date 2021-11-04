package com.krlsedu.timetracker.service;

import com.krlsedu.timetracker.model.Error;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlLitle {
	
	private static final String DB_NAME = ".wakatime-desktop-plugin.db";
	
	public static void createNewDatabase(String url) {
		try {
			Connection conn = DriverManager.getConnection(url);
			Statement statement = conn.createStatement();
			statement.executeQuery("CREATE TABLE processs( name text null, json text)").close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static String getUrl() {
		File file = new File(DB_NAME);
		String relativePath = Dependencies.getResourcesLocation();
		String url = "jdbc:sqlite:" + relativePath + "\\" + DB_NAME;
		
		if (!(file.exists() && !file.isDirectory())) {
			createNewDatabase(url);
		}
		
		return url;
	}
	
	public static void salva(Error error) throws Exception {
		String url = getUrl();
		try {
			Connection conn = DriverManager.getConnection(url);
			PreparedStatement preparedStatement = conn.prepareStatement("insert into error (url,json) values (?,?)");
			preparedStatement.setString(1, error.getUrl());
			preparedStatement.setString(2, error.getJson());
			preparedStatement.execute();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static List<Error> getErrors() {
		String url = getUrl();
		List<Error> errors = new ArrayList<>();
		try {
			Connection conn = DriverManager.getConnection(url);
			PreparedStatement preparedStatement = conn.prepareStatement("select * from error");
			
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Error error = new Error();
				error.setUrl(resultSet.getString(1));
				error.setJson(resultSet.getString(2));
				errors.add(error);
			}
			preparedStatement = conn.prepareStatement("delete from error");
			preparedStatement.execute();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return errors;
	}
}
