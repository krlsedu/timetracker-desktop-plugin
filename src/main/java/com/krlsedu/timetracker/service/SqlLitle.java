package com.krlsedu.timetracker.service;

import com.krlsedu.timetracker.model.Error;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlLitle {
	public static void createNewDatabase(String url) {
		
		
		try (Connection conn = DriverManager.getConnection(url)) {
			if (conn != null) {
				DatabaseMetaData meta = conn.getMetaData();
				Statement statement = conn.createStatement();
				statement.executeQuery("CREATE TABLE error( url text null, json text)").close();
				System.out.println("The driver name is " + meta.getDriverName());
				System.out.println("A new database has been created.");
			}
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static String getUrl(){
		File f = new File("timeTracker.db");
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		String url = "jdbc:sqlite:" + s + "\\" + "timeTracker.db";
		
		if (!(f.exists() && !f.isDirectory())) {
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
	
	public static List<Error> getErros(){
		String url = getUrl();
		try {
			Connection conn = DriverManager.getConnection(url);
			PreparedStatement preparedStatement = conn.prepareStatement("select * from error");
			
			ResultSet resultSet = preparedStatement.executeQuery();
			List<Error> errors = new ArrayList<>();
			while (resultSet.next()) {
				Error error = new Error();
				error.setUrl(resultSet.getString(1));
				error.setJson(resultSet.getString(2));
				errors.add(error);
			}
			preparedStatement.execute("delete from error");
			conn.close();
			return errors;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
}
