package com.revolhope.deepdev.tcpserver.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.revolhope.deepdev.tcplibrary.model.Device;

public class Database {
	
	
	private static final String DB_URL = "localhost:3306/";
	private static final String DB_NAME = "FTP_Server";
	private static final String DB_USR = "ftp_server";
	private static final String DB_PWD = "b1ql83dD$;Reka!lPOd80_234";
	
	private static Database INSTANCE;
	private static Connection conn;
	
	/***
	 * Private constructor
	 */
	private Database () {}
	
	/***
	 * Thread Safe Singleton
	 * @return Database instance
	 */
	public static Database GetInstance()
	{
		if (INSTANCE == null)
		{
			synchronized (Database.class) 
			{
				if (INSTANCE == null)
				{
					INSTANCE = new Database();
				}
			}
		}
		return INSTANCE;
	}
	
	/***
	 * Save device into database
	 * @param dev Object to be stored
	 * @return True if all was right, false if not
	 * @throws SQLException
	 */
	public Device insertDevice(Device dev) throws SQLException
	{
		openConnection();
		String query = "INSERT INTO DEVICE(DEV_NAME, DEV_MAC_ADDRESS, DATE_CREATED) VALUES(?,?,?)";
		PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		
		ps.setString(1, dev.getName());
		ps.setString(2, dev.getMacAddress());
		ps.setLong(3, System.currentTimeMillis());
		
		int affected = ps.executeUpdate();
		ResultSet result = ps.getGeneratedKeys();
		
		if (affected == 1 && result.first())
		{
			dev.setId(result.getLong(0));
			ps.close();
			conn.close();
			return dev;
		}
		else
		{
			ps.close();
			conn.close();
			return null;
		}
	}
	
	/***
	 * 
	 * @param deviceMac
	 * @return
	 * @throws SQLException
	 */
	public Device selectDeviceByMac(String deviceMac) throws SQLException
	{
		openConnection();
		String query = "SELECT DEV_ID, DEV_NAME, DEV_MAC_ADDRESS, DATE_CREATED FROM DEVICE WHERE DEV_MAC_ADDRESS = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		ps.setString(1, deviceMac);
		ResultSet result = ps.executeQuery();
		if (result.first())
		{
			Device dev = new Device();
			dev.setId(result.getLong(0));
			dev.setName(result.getString(1));
			dev.setMacAddress(result.getString(2));
			dev.setCreatedDate(result.getLong(3));
			
			ps.close();
			conn.close();
			return dev;
		}
		else
		{
			ps.close();
			conn.close();
			throw new SQLException("Device name not found in database..");
		}
	}
	
	/***
	 * Open connection for the URL, user and password set
	 * @throws SQLException
	 */
	private static void openConnection() throws SQLException
	{
		conn = DriverManager.getConnection("jdbc:mysql://"+DB_URL+DB_NAME, DB_USR, DB_PWD);
	}
}
