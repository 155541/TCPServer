package com.revolhope.deepdev.tcpserver.helpers;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import com.revolhope.deepdev.tcplibrary.model.Device;

public class Toolkit 
{
	private static ArrayList<Device> connectedDevices = new ArrayList<>();
	
	/***
	 * TODO
	 * @param dev
	 */
	public static void addConnectedDevice(Device dev)
	{
		removeConnectedDevice(dev);
		connectedDevices.add(0, dev);
	}
	
	/***
	 * TODO
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
	 * TODO
	 * @return
	 */
	public static ArrayList<Device> getConnectedDevices()
	{
		return Toolkit.connectedDevices;
	}
	
	/**
	 * TODO
	 * @param dev
	 * @return
	 */
	public static boolean isOnline(Device dev)
	{
		for (Device d : connectedDevices)
		{
			if (d.getId() == dev.getId()) return true;
		}
		return false;
	}
	
	/**
	 * TODO
	 * @param id
	 * @return
	 */
	public static Device getById(long id)
	{
		for (Device d : connectedDevices)
		{
			if (d.getId() == id) return d;
		}
		return null;
	}
	
	
	// =========================================
	
	/**
	 * TODO
	 * @return
	 */
	public static long timestamp()
	{
		return Calendar.getInstance().getTimeInMillis();
	}
}
