package it.unibo.connector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;

import it.unibo.qactors.akka.QActor;

/**
 * 
 * @author Federico Ruggeri
 * 
 * Simple TCP client that sends and reads strings.
 * Moreover, this class supports some utility functions
 * that are compliant with UnityActorSimulator.
 *
 * @todo Rework socket usage -> rx / asynchronous
 */
public class UnityConnector implements IConnector
{
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	
	private IConnectorParser parser;
	
	private String address;
	private int port;
	private QActor actor;
	private boolean isConnected;
	
	public UnityConnector(int port, QActor actor)
	{
		this("127.0.0.1", port, actor);
	}
	
	public UnityConnector(String address, int port, QActor actor)
	{
		this.address = address;
		this.port = port;
		this.actor = actor;
		this.isConnected = false;
	}
	
	/**
	 * Configuration from property file.
	 * 
	 * @param configFile
	 */
	public UnityConnector(String configFile)
	{
		System.out.println("######### PLACEHOLDER UNITY CONNECTOR #########");
		configure(configFile);
	}
	
	private void configure(String configFile)
	{
		// Property file
		Properties prop = new Properties();
		try 
		{
			prop.load(new FileInputStream(new File(configFile)));
			
			address = prop.getProperty("Address");
			port = Integer.parseInt(prop.getProperty("Port"));
		} catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void setConnectorParser(IConnectorParser parser) 
	{
		this.parser = parser;
	}

	@Override
	public IConnectorParser getConnectorParser() 
	{
		return parser;
	}
	
	@Override
	public boolean isConnected() 
	{
		return isConnected;
	}

	@Override
	public <T> void send(T data) 
	{
		if (!isConnected)
		{
			actor.println("[UnityConnector] Can't send messages when not connected!");
			return;
		}
		
		try 
		{
            String encoded = URLEncoder.encode(data.toString(),"UTF-8");
            output.println(encoded);
            output.flush();
			actor.println("[UnityConnector] Sending encoded data: " + encoded);
		}
		catch (IOException e)
		{
			close();
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean receive() 
	{
		if (!isConnected)
		{
			actor.println("[UnityConnector] Can't received messages when not connected!");
			return false;
		}
		
		try 
		{
			String message = input.readLine();
			String decodedMsg = URLDecoder.decode(message, "UTF-8");
			actor.println("[UnityConnector] Received data: " + decodedMsg);
			parser.parseData(decodedMsg);
		} catch (IOException e) 
		{
			close();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean connect() 
	{
		try 
		{
			// Configuring parser
			if (parser == null)
			{
				System.out.println("[UnityConnector] No custom parser has been assigned! Using default one..");
				parser = new DefaultConnectorParser(actor);
			}
			
			socket = new Socket(address, port);
			
			output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			output.flush();
			input = new BufferedReader(new InputStreamReader((socket.getInputStream())));
			
			// Starts the reading thread
			Thread reader = new Thread(new Runnable()
			{
				public void run()
				{
					actor.println("[UnityConnector] Reader thread is up!");
					while (receive());
				}
			});
			reader.start();
			isConnected = true;
			
			System.out.println("[UnityConnector] Connected to:" + address + "/" + port);
		} catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		return true;

	}

	@Override
	public boolean close() 
	{
		try 
		{
			actor.println("[UnityConnector] Closing communication!");
			isConnected = false;
			output.close();
			input.close();
			socket.close();
		} catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Simple method that configures the network client
	 * handler's logic name to the actor's name.
	 * Use this for initial actor simulator configuration.
	 */
	@Override
	public void setupActorSimulatorName()
	{
		if (!isConnected)
		{
			System.out.println("[UnityConnector] Can't execute method since no connection is established.");
			return;
		}
	
		String oldName = socket.getLocalAddress().getHostAddress() + "/" + port;
		String newName = actor.getName().replace("_ctrl", "");
		String command = "changeID(\"" + oldName + "\",\"" + newName + "\")";
		send(command);
	}
	
	/**
	 * Changes the default client handler's ID to the given one.
	 * Use this for custom initial configuration.
	 * @param newName
	 */
	@Override
	public void setupCustomSimulatorName(String newName)
	{
		if (!isConnected)
		{
			System.out.println("[UnityConnector] Can't execute method since no connection is established.");
			return;
		}
		
		String oldName = socket.getLocalAddress().getHostAddress() + "/" + port;
		String command = "changeID(\"" + oldName + "\",\"" + newName + "\")";
		send(command);
	}
	
	/**
	 * Changes the client handler's ID (oldName) to the given one (newName).
	 * Suitable for a dynamic configuration.
	 * @param oldName
	 * @param newName
	 */
	@Override
	public void setupCustomSimulatorName(String oldName, String newName)
	{
		if (!isConnected)
		{
			System.out.println("[UnityConnector] Can't execute method since no connection is established.");
			return;
		}
		
		String command = "changeID(\"" + oldName + "\",\"" + newName + "\")";
		send(command);
	}

}
