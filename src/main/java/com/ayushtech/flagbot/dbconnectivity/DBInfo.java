package com.ayushtech.flagbot.dbconnectivity;

public class DBInfo {
	public static String url = null;
	public static String user = null;
	public static String password = null;
	public static String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

	private DBInfo() {
	}

	public static void setData(String url_, String username_, String password_) {
		if (url == null && user == null && password == null) {
			url = url_;
			user = username_;
			password = password_;
		}
	}

//    static String url = "jdbc:mysql://sql.freedb.tech:3306/freedb_flagbot";
//	static String user = "freedb_ayush487";
//	static String password = "&3gZJ@ewMkEZ@Mv";
//	static String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

}
