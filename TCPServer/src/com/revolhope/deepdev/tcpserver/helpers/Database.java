package com.revolhope.deepdev.tcpserver.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.revolhope.deepdev.tcplibrary.model.Device;
import com.revolhope.deepdev.tcplibrary.model.Token;

public class Database {
	
	
	private static final String DB_URL = "localhost:3306/";
	private static final String DB_NAME = "FTP_Server";
	private static final String DB_USR = "ftp_server";
	private static final String DB_PWD = "44rl9:7g4nGve$_jDCaj32n<";
	private static final String DB_PARAMS = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	
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
			dev.setId(result.getLong(1));
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
	 * Method to insert new Token into database
	 * @param token Object to be stored
	 * @throws SQLException
	 */
	public void insertToken(Token token) throws SQLException
	{
		openConnection();
		String query = "INSERT INTO TOKEN(DEVICE_ID, _TOKEN, EXPIRATION_DATE) VALUES(?,?,?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		ps.setLong(1, token.getDeviceId());
		ps.setString(2, token.toString());
		ps.setLong(3, token.getExpirationDate());
		
		int affected = ps.executeUpdate();
		
		if (affected != 1)
		{
			ps.close();
			conn.close();
			throw new SQLException("Error in sql method. Affected rows != 1 before insert");
		}
	}
	
	/***
	 * Method to update a Token
	 * @param token Object to be updated
	 * @throws SQLException
	 */
	public void updateToken(Token token) throws SQLException
	{
		openConnection();
		String query = "UPDATE FROM TOKEN SET _TOKEN = ?, EXPIRATION_DATE = ? WHERE DEVICE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		ps.setString(1, token.toString());
		ps.setLong(2, token.getExpirationDate());
		ps.setLong(3, token.getDeviceId());
		
		int affected = ps.executeUpdate();
		
		if (affected != 1)
		{
			ps.close();
			conn.close();
			throw new SQLException("Error in sql method. Affected rows != 1 before update");
		}
	}
	
	/***
	 * Method to select a Token given it's device id
	 * @param deviceId Long identifier of the token requested
	 * @return Token object
	 * @throws SQLException
	 */
	public Token selectToken(long deviceId) throws SQLException
	{
		openConnection();
		String query = "SELECT _TOKEN, EXPIRATION_DATE FROM TOKEN WHERE DEVICE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		ps.setLong(1, deviceId);
		ResultSet result = ps.executeQuery();
		if (result.first())
		{
			String token = result.getString(1);
			long expiration = result.getLong(2);
			
			ps.close();
			conn.close();
			
			return new Token(deviceId, token, expiration);
		}
		else
		{
			ps.close();
			conn.close();
			throw new SQLException("Token associated to given deviceId not found in database..");
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
			dev.setId(result.getLong(1));
			dev.setName(result.getString(2));
			dev.setMacAddress(result.getString(3));
			dev.setCreatedDate(result.getLong(4));
			
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
	 * Method to verify if token given is valid or not
	 * @param token Object to be verified
	 * @return True if token is equals than stored in database, false otherwise
	 * @throws SQLException
	 */
	public boolean verifyToken(Token token) throws SQLException
	{
		openConnection();
		String query = "SELECT * FROM TOKEN WHERE DEVICE_ID = ? AND _TOKEN = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		ps.setLong(1, token.getDeviceId());
		ps.setString(2, token.toString());
		
		ResultSet result = ps.executeQuery();
		
		boolean verified = result.first() && !result.next();
		ps.close();
		conn.close();
		
		return verified;
	}
	
	/***
	 * Open connection for the URL, user and password set
	 * @throws SQLException
	 */
	private static void openConnection() throws SQLException
	{
		conn = DriverManager.getConnection("jdbc:mysql://"+DB_URL+DB_NAME+DB_PARAMS, DB_USR, DB_PWD);
	}
}
