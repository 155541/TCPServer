package com.revolhope.deepdev.tcpserver.main;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;

import com.revolhope.deepdev.tcplibrary.constants.Params;
import com.revolhope.deepdev.tcplibrary.helpers.server.OnReceive;
import com.revolhope.deepdev.tcplibrary.helpers.server.TcpServer;
import com.revolhope.deepdev.tcplibrary.model.DataFile;
import com.revolhope.deepdev.tcplibrary.model.Device;
import com.revolhope.deepdev.tcplibrary.model.Header;
import com.revolhope.deepdev.tcplibrary.model.Packet;
import com.revolhope.deepdev.tcplibrary.model.Token;
import com.revolhope.deepdev.tcplibrary.model.Code;
import com.revolhope.deepdev.tcpserver.helpers.Database;
import com.revolhope.deepdev.tcpserver.helpers.Toolkit;

public class Main {
	
	public static void main(String[] args) {
		// TEST
		try 
		{
			TcpServer.bind(Params.PORT);
			while(true)
			{
				TcpServer.listen(new OnReceive() {
					
					@SuppressWarnings("unchecked")
					@Override
					public Packet process(Packet obj, InetAddress clientAddr)
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
								Device dev = (Device) body;
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
								packetResponse.setHeader(headerResponse);
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
								
								dev.setCurrentInetAddress(clientAddr);
								Toolkit.addConnectedDevice(dev);
								
								headerResponse.setCode(Code.RES_OK);
								headerResponse.setToken(devToken);
								headerResponse.setTimestamp(Toolkit.timestamp());
								headerResponse.setDeviceId(Params.SERVER_ID);
								
								packetResponse.setHeader(headerResponse);
								packetResponse.setBody(Toolkit.getConnectedDevices());
								return packetResponse;
							}
							catch(SQLException exc)
							{
								headerResponse.setDeviceId(Params.SERVER_ID);
								headerResponse.setTimestamp(Toolkit.timestamp());
								headerResponse.setCode(Code.RES_ERROR_SQL);
								headerResponse.setToken(null);
								packetResponse.setHeader(headerResponse);
								packetResponse.setBody(exc.getMessage());
								return packetResponse;
							}
						
						case REQ_GET_DEV_CONN:
							
							try
							{
								Token reqToken = header.getToken();
								if (db.verifyToken(reqToken))
								{
									headerResponse.setCode(Code.RES_OK);
									headerResponse.setToken(reqToken);
									headerResponse.setTimestamp(Toolkit.timestamp());
									headerResponse.setDeviceId(Params.SERVER_ID);
									packetResponse.setBody(Toolkit.getConnectedDevices());
								}
								else
								{
									// TODO: WHAT? EH?
								}
								packetResponse.setHeader(headerResponse);
								return packetResponse;
							}
							catch(SQLException exc)
							{
								headerResponse.setDeviceId(Params.SERVER_ID);
								headerResponse.setTimestamp(Toolkit.timestamp());
								headerResponse.setCode(Code.RES_ERROR_SQL);
								headerResponse.setToken(null);
								packetResponse.setHeader(headerResponse);
								packetResponse.setBody(exc.getMessage());
								return packetResponse;
							}
							
							
						case REQ_CLOSE_SESSION:
							
							try
							{
								Device dev = (Device) body;
								Token reqToken = header.getToken();
								if (db.verifyToken(reqToken))
								{
									Toolkit.removeConnectedDevice(dev);
									headerResponse.setCode(Code.RES_OK);
									headerResponse.setToken(reqToken);
									headerResponse.setTimestamp(Toolkit.timestamp());
									headerResponse.setDeviceId(Params.SERVER_ID);
									packetResponse.setBody(null);
								}
								else
								{
									// TODO: WHAT? EH?
								}
								packetResponse.setHeader(headerResponse);
								return packetResponse;
							}
							catch(SQLException exc)
							{
								headerResponse.setDeviceId(Params.SERVER_ID);
								headerResponse.setTimestamp(Toolkit.timestamp());
								headerResponse.setCode(Code.RES_ERROR_SQL);
								headerResponse.setToken(null);
								packetResponse.setHeader(headerResponse);
								packetResponse.setBody(exc.getMessage());
								return packetResponse;
							}
							
						case REQ_TRANSMISSION:
							
							try
							{
								DataFile[] files = (DataFile[]) body;
								Token reqToken = header.getToken();
								Device origin = Toolkit.getById(header.getDeviceId());								
								
								if (db.verifyToken(reqToken))
								{
									
									if (!db.checkDevice(origin.getId())) return null; // TODO
									
									db.insertFile(files);
									
									headerResponse.setCode(Code.RES_OK);
									headerResponse.setToken(reqToken);
									headerResponse.setTimestamp(Toolkit.timestamp());
									headerResponse.setDeviceId(Params.SERVER_ID);
									
									packetResponse.setHeader(headerResponse);
									packetResponse.setBody(String.format("Files stored to be delivered: %d", files.length));
									
									return packetResponse;
								}
								else
								{
									// TODO: WHAT? EH?
									return null;
								}
							}
							catch(SQLException exc)
							{
								headerResponse.setDeviceId(Params.SERVER_ID);
								headerResponse.setTimestamp(Toolkit.timestamp());
								headerResponse.setCode(Code.RES_ERROR_SQL);
								headerResponse.setToken(null);
								packetResponse.setHeader(headerResponse);
								packetResponse.setBody(exc.getMessage());
								return packetResponse;
							}
						
						case REQ_PENDING_FILES:
							
							try
							{
								Token reqToken = header.getToken();
								Device origin = Toolkit.getById(header.getDeviceId());								
								
								if (db.verifyToken(reqToken))
								{
										
									
									ArrayList<DataFile> files = db.selectFiles(origin.getId(),
																			   body instanceof Long ? (Long)body : null);
									
									headerResponse.setCode(Code.RES_OK);
									headerResponse.setToken(reqToken);
									headerResponse.setTimestamp(Toolkit.timestamp());
									headerResponse.setDeviceId(Params.SERVER_ID);
									
									packetResponse.setHeader(headerResponse);
									packetResponse.setBody(files);
									
									return packetResponse;
								}
								else
								{
									// TODO: WHAT? EH?
									return null;
								}
							}
							catch(SQLException exc)
							{
								headerResponse.setDeviceId(Params.SERVER_ID);
								headerResponse.setTimestamp(Toolkit.timestamp());
								headerResponse.setCode(Code.RES_ERROR_SQL);
								headerResponse.setToken(null);
								packetResponse.setHeader(headerResponse);
								packetResponse.setBody(exc.getMessage());
								return packetResponse;
							}
							
						case REQ_DELETE_FILES:
							
							try
							{
								Token reqToken = header.getToken();								
								if (db.verifyToken(reqToken))
								{
									
									ArrayList<Long> ids = (ArrayList<Long>) body;
									db.deleteFiles(ids);
									
									headerResponse.setCode(Code.RES_OK);
									headerResponse.setToken(reqToken);
									headerResponse.setTimestamp(Toolkit.timestamp());
									headerResponse.setDeviceId(Params.SERVER_ID);
									
									packetResponse.setHeader(headerResponse);
									packetResponse.setBody(null);
									
									return packetResponse;
								}
								else
								{
									// TODO: WHAT? EH?
									return null;
								}
							}
							catch(SQLException exc)
							{
								headerResponse.setDeviceId(Params.SERVER_ID);
								headerResponse.setTimestamp(Toolkit.timestamp());
								headerResponse.setCode(Code.RES_ERROR_SQL);
								headerResponse.setToken(null);
								packetResponse.setHeader(headerResponse);
								packetResponse.setBody(exc.getMessage());
								return packetResponse;
							}
							
						default:
							//TODO: LOG THIS CASE 
							return null;
						}
					}
					
					@Override
					public void response(Packet obj, InetAddress addr, int port, ObjectOutputStream out)
					{
						try 
						{
							TcpServer.send(obj, addr, port, out);
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
					}
				});
			}		
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
