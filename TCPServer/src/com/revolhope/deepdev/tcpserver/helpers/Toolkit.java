package com.revolhope.deepdev.tcpserver.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.revolhope.deepdev.tcplibrary.model.Device;

public class Toolkit 
{
	private static Map<Long, String> mapTokens = new HashMap<>();
	
	/***
	 * Private constructor
	 */
	private Toolkit() {}
	
	/***
	 * Method to generate and get a new Token for given Device
	 * @param device Device to assign the new generated token
	 * @return String representing the new token
	 */
	public static String generateToken(Device device)
	{
		String token = UUID.randomUUID().toString().replace("-", "");
		mapTokens.put(device.getId(), token);
		return token;
	}
	
	/***
	 * Method to verify if token given is correct
	 * @param dev Device who send the token
	 * @param token Token (String) to verify
	 * @return True if token given is equals to the token stored
	 */
	public static boolean verifyToken(Device dev, String token)
	{
		if (mapTokens.containsKey(dev.getId()))
		{
			return mapTokens.get(dev.getId()).equals(token);
		}
		return false;
	}
}
