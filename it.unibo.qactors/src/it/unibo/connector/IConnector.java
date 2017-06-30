package it.unibo.connector;

public interface IConnector 
{
	// Network setup
	boolean connect();
	boolean close();
	
	// Properties
	boolean isConnected();
	
	// Communication
	<T> void send(T data);
	boolean receive();
	
	// Actor's handler utility management
	void setupActorSimulatorName();
	void setupCustomSimulatorName(String newName);
	void setupCustomSimulatorName(String oldName, String newName);
	
	// Parsing
	void setConnectorParser(IConnectorParser parser);
	IConnectorParser getConnectorParser();
}
