package it.unibo.qactors.action;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.qactors.QActorUtils;

public interface IActorAction extends IObservableActionGeneric<String>{	
 	public int  getMaxDuration();
	public boolean isSuspended();
	public IEventItem getInterruptEvent( );
 	public void setMaxDuration(int d);		
	public final String  endBuiltinEvent = QActorUtils.locEvPrefix+"end"; 
	public final boolean suspendPlan  = false;
	public final boolean continuePlan = true;
 	public enum ActionExecMode{
		synch(0, "synch"),
		asynch(1, "aynch");
		private int value;
		private String name;	
 		private ActionExecMode(int value, String name) {
			this.value = value;
			this.name = name;
 		}
	}
}