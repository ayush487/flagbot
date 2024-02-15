package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionProvider {

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection==null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(
                        DBInfo.url,
                        DBInfo.user,
                        DBInfo.password
                    );
                return connection;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return connection;
        }
    }
}
