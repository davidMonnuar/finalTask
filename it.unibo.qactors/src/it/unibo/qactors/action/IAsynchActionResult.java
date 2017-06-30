package it.unibo.qactors.action;
import it.unibo.contactEvent.interfaces.IEventItem;

public interface IAsynchActionResult {
	//An action can work for a prefixed amount of time DT
	public boolean getInterrupted(); //true if the action has been interrupted by some event
	public IEventItem getEvent();    //gives the event that has interrupted the action
	public long getTimeRemained();   //gives the time TR=DT-TE where TE is the execution time before the interruption
	public String getResult();	     //gives the result of the action
	public boolean getGoon();		 //returns true if the system can continue
	public void  setResult(String result);
	public void  setGoon(boolean goon);
}
