package it.unibo.qactors;

public class ActorSelectionMessage {
private String name;
	public ActorSelectionMessage(String name){
		this.name = name;
	}
	public String getName(){
		return name;
	}
}
