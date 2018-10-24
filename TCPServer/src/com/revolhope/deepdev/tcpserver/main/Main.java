package com.revolhope.deepdev.tcpserver.main;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;

import com.revolhope.deepdev.tcplibrary.constants.Params;
import com.revolhope.deepdev.tcplibrary.helpers.Database;
import com.revolhope.deepdev.tcplibrary.helpers.TcpServer;
import com.revolhope.deepdev.tcplibrary.model.Device;
import com.revolhope.deepdev.tcplibrary.model.Header;
import com.revolhope.deepdev.tcplibrary.model.Packet;

public class Main {

	public static void main(String[] args) {
				
		try 
		{	
			TcpServer.listen(Params.PORT, new TcpServer.OnReceive() {
				
				@Override
				public Packet process(Packet obj)
				{
					Header header = obj.getHeader();
					switch(header.getType())
					{
					case REQ_INIT:
						try
						{
							Database db = Database.GetInstance();
							Device dev = db.insertDevice((Device) obj.getBody());
							
							 
						}
						catch(SQLException exc)
						{
							
						}
						
						break;
					case REQ_OPEN_SESSION:
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
