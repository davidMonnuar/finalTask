package it.unibo.connector;

public interface IConnectorParser 
{
	// Parsing
	<T> void parseData(T data);
}
