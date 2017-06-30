package it.unibo.qactors;

public class ActorTerminationMessage {
private String name;
private boolean testing;
	public ActorTerminationMessage(String name, boolean testing){
		this.name    = name;
		this.testing = testing;
	}
	public String getName(){
		return name;
	}
	public boolean testing(){
		return testing;
	}
	public String toString(){
		return "terminate(N,T)".replace("N", name).replace("T",""+testing);
	}
}
