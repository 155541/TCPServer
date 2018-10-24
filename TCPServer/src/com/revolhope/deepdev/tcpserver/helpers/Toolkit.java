package com.revolhope.deepdev.tcpserver.helpers;


import java.util.ArrayList;

import com.revolhope.deepdev.tcplibrary.model.Device;

public class Toolkit 
{
	ArrayList<Device> connectedDevices = new ArrayList<>();
	
	/***
	 * 
	 * @param dev
	 */
	public void addConnectedDevice(Device dev)
	{
		removeConnectedDevice(dev);
		connectedDevices.add(0, dev);
	}
	
	/***
	 * 
	 * @param dev
	 */
	public void removeConnectedDevice(Device dev)
	{
		for (Device d : connectedDevices)
		{
			if (d.getId() == dev.getId())
			{
				connectedDevices.remove(d);
			}
		}
	}
	
	/***
	 * 
	 * @return
	 */
	public ArrayList<Device> getConnectedDevices()
	{
		return this.connectedDevices;
	}
}
