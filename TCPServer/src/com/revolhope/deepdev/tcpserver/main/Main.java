package com.revolhope.deepdev.tcpserver.main;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Calendar;

import com.revolhope.deepdev.tcplibrary.constants.Params;
import com.revolhope.deepdev.tcplibrary.helpers.TcpServer;
import com.revolhope.deepdev.tcplibrary.model.Device;
import com.revolhope.deepdev.tcplibrary.model.Header;
import com.revolhope.deepdev.tcplibrary.model.Packet;
import com.revolhope.deepdev.tcplibrary.model.Token;
import com.revolhope.deepdev.tcplibrary.model.Type;
import com.revolhope.deepdev.tcpserver.helpers.Database;
import com.revolhope.deepdev.tcpserver.helpers.Toolkit;

public class Main {

	public static void main(String[] args) {
				
		try 
		{	
			TcpServer.listen(Params.PORT, new TcpServer.OnReceive() {
				
				@Override
				public Packet process(Packet obj)
				{
					Database db = Database.GetInstance();
					Header header = obj.getHeader();
					Object body = obj.getBody();
					
					Packet packetResponse = new Packet();
					Header headerResponse = new Header();
					
					switch(header.getType())
					{
					case REQ_INIT:
						try
						{
							Device dev = db.insertDevice((Device) body);
							dev = db.insertDevice(dev);
							
							headerResponse.setDeviceId(Params.SERVER_ID);
							headerResponse.setTimestamp(Toolkit.timestamp());
							
							if (dev != null)
							{
								headerResponse.setType(Type.RES_OK);
								headerResponse.setToken(new Token(dev.getId()));
								packetResponse.setBody(dev);
								
								db.insertToken(headerResponse.getToken());
							}
							else
							{
								headerResponse.setType(Type.RES_ERROR_SQL);
								headerResponse.setToken(null);
								packetResponse.setBody("ERROR_SQL: Device returned from database is null..");
							}
							
							packetResponse.setHeader(headerResponse);
							return packetResponse;
						}
						catch(SQLException exc)
						{
							headerResponse.setType(Type.RES_ERROR_SQL);
							headerResponse.setToken(null);
							packetResponse.setBody(exc.getMessage());
							return packetResponse;
						}
						
					case REQ_OPEN_SESSION:
						
						try
						{
							Device dev = db.selectDeviceByMac((String) body);
							
							if (dev != null)
							{
								Token devToken = db.selectToken(dev.getId());
								if (!devToken.isValid())
								{
									devToken.refresh();
									db.updateToken(devToken);
								}
								
								Toolkit.addConnectedDevice(dev);
								
								headerResponse.setType(Type.RES_OK);
								headerResponse.setToken(devToken);
								headerResponse.setTimestamp(Toolkit.timestamp());
								headerResponse.setDeviceId(Params.SERVER_ID);
							}
						}
						catch(SQLException exc)
						{
							
						}
						
						break;
					case REQ_CLOSE_SESSION:
						break;
					case REQ_TRANSMISSION:
						break;
					default:
						break;
					}
					
					
					
					return null;
				}
				
				@Override
				public void response(Packet obj, InetAddress addr, int port)
				{
					
				}
			});				
		} 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
