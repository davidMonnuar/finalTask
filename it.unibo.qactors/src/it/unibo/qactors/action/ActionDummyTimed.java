package it.unibo.qactors.action;
 
import java.util.concurrent.Callable;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.akka.QActor;

public class ActionDummyTimed extends ActorTimedAction  {

public ActionDummyTimed( 
		String name,   QActor actor,  QActorContext ctx, String teminationEvId, String[] alarms, IOutputEnvView outView, int maxduration ) throws Exception {
	super(name, actor, ctx, false, teminationEvId, alarms, outView, maxduration); 
}

	@Override
	protected Callable<String> getActionBodyAsCallable() {
 		return new Callable<String>(){
			@Override
			public String call() throws Exception {
				Thread.sleep(maxduration+10); //EXTRA TIME TO PRIVILEGE TIMEOUT INTERRUPT
//				println("%%%%%%%%%%%%%%%%%%%% ActionDummyTimed ENDS NORMALLY");
				return "wait done";
			}		
		};
	}
	
	@Override
	protected String getApplicationResult() throws Exception {
		if( this.suspendevent == null )
			return name+"(" + maxduration + ", timeremained(" + timeRemained +"))";
		else  
			return name+"("+ maxduration + "," + suspendevent + ",timeRemained("+timeRemained+"))";
	}
	
	@Override
	public String toString(){
		return "ActionDummyTimed " + name + "(" + this.maxduration +")";
	}

}
