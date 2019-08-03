package com.hofill.mastermindTest.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {

	public Connection conn = null;
	private final String user;
	private final String database;
	private final String password;
	private final String port;
	private final String hostname;

	public MySQL() {
		hostname = DatabaseValues.hostname;
		port = DatabaseValues.port;
		database = DatabaseValues.database;
		user = DatabaseValues.username;
		password = DatabaseValues.password;
	}

	public void openConnection() throws SQLException, ClassNotFoundException {
		if (conn != null && !conn.isClosed()) {
			return;
		}

		String connectionURL = "jdbc:mysql://" + hostname + ":" + port;
		if (database != null) {
			connectionURL = connectionURL + "/" + database;
		}

		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(connectionURL, user, password);
		
	}
	
	public Connection getConnection() throws ClassNotFoundException, SQLException {
		if (conn != null && !conn.isClosed()) {
			return conn;
		}
		openConnection();
		return conn;
	}

	public void closeConnection() throws SQLException {
		if (conn != null && !conn.isClosed()) {
			conn.close();
		}
	}

}
