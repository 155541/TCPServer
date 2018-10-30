package com.revolhope.deepdev.tcpserver.helpers;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import com.revolhope.deepdev.tcplibrary.model.Device;

public class Toolkit 
{
	private static ArrayList<Device> connectedDevices = new ArrayList<>();
	
	/***
	 * 
	 * @param dev
	 */
	public static void addConnectedDevice(Device dev)
	{
		removeConnectedDevice(dev);
		connectedDevices.add(0, dev);
	}
	
	/***
	 * 
	 * @param dev
	 */
	public static void removeConnectedDevice(Device dev)
	{
		Device d;
		Iterator<Device> it = connectedDevices.iterator();
		while(it.hasNext())
		{
			d = it.next();
			if (d.getId() == dev.getId())
			{
				it.remove();
				return;
			}
		}
	}
	
	/***
	 * 
	 * @return
	 */
	public static ArrayList<Device> getConnectedDevices()
	{
		return Toolkit.connectedDevices;
	}
	
	public static long timestamp()
	{
		return Calendar.getInstance().getTimeInMillis();
	}
}
