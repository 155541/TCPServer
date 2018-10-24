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
import com.revolhope.deepdev.tcplibrary.model.Code;
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
					
					switch(header.getCode())
					{
					case REQ_INIT:
						try
						{
							Device dev = db.insertDevice((Device) body);
							dev = db.insertDevice(dev);
							
							headerResponse.setDeviceId(Params.SERVER_ID);
							headerResponse.setTimestamp(Toolkit.timestamp());
							headerResponse.setCode(Code.RES_OK);
							headerResponse.setToken(new Token(dev.getId()));
							packetResponse.setBody(dev);
							
							db.insertToken(headerResponse.getToken());
							
							packetResponse.setHeader(headerResponse);
							return packetResponse;
						}
						catch(SQLException exc)
						{
							headerResponse.setDeviceId(Params.SERVER_ID);
							headerResponse.setTimestamp(Toolkit.timestamp());
							headerResponse.setCode(Code.RES_ERROR_SQL);
							headerResponse.setToken(null);
							packetResponse.setBody(exc.getMessage());
							return packetResponse;
						}
						
					case REQ_OPEN_SESSION:
						
						try
						{
							Device dev = db.selectDeviceByMac((String) body);
							
							Token devToken = db.selectToken(dev.getId());
							if (!devToken.isValid())
							{
								devToken.refresh();
								db.updateToken(devToken);
							}
							
							Toolkit.addConnectedDevice(dev);
							
							headerResponse.setCode(Code.RES_OK);
							headerResponse.setToken(devToken);
							headerResponse.setTimestamp(Toolkit.timestamp());
							headerResponse.setDeviceId(Params.SERVER_ID);
							
							packetResponse.setBody(Toolkit.getConnectedDevices());
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
