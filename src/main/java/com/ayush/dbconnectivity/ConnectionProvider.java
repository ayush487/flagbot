package com.ayush.dbconnectivity;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionProvider {

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(
	                DBInfo.url,
	                DBInfo.user,
	                DBInfo.password
	            );
			return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
