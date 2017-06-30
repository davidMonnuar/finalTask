package it.unibo.qactors.platform;
 
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorUtils;
//import it.unibo.qactors.akka.SystemCreationActor;
import java.util.Vector;
import akka.actor.ActorRef;
import akka.actor.Props;
import it.unibo.contactEvent.interfaces.IEventItem;

/*
 * The platform calls the onReceive operation when an expected event is detected
 */
public  class EventHandlerComponent extends EventAbstractComponent {
protected Vector<IEventItem> curEventItems   = new Vector<IEventItem>();
protected  String[] events;
/*
 * FACTORY METHOD
 */
	public static EventHandlerComponent createEventHandler(
			String name, QActorContext ctx, String[] events, IOutputEnvView view){
		ActorRef h = ctx.getAkkaContext().actorOf( Props.create(  
				EventHandlerComponent.class,  name, ctx, events,view  ),  name );
		return (EventHandlerComponent) QActorUtils.getQActor(name);
 	}
 	public EventHandlerComponent( String name, QActorContext myctx, String[] events, IOutputEnvView view ) throws Exception { 
 		super(name,myctx,view); 
 		this.events = events; 
   		if( events == null ) throw new Exception("	*** EventHandlerComponent no events array");
//  		println("EventHandlerComponent " + getName() + " event n=" + events.length );
		for( int i=0; i<events.length; i++){
			if( events[i] == null ) break;		//defensive
			if( events[i].trim().length() > 0 ){
//  				println("	*** EventHandlerComponent " + getName() + " register for " +  events[i] );
				registerForEvent(  events[i], 1000 );
			}
		}
 		this.isStarted = true;
 	}
	@Override
	public void doJob() throws Exception { 	}
// 	@Override
  	public boolean isPassive(){return true;}
 
 
	protected void loadWorldTheory() throws Exception{
		//WE DO NOT LOAD TEH WORLD THEORY in a EventHandler
	}
 
	@Override
	protected void handleQActorEvent(IEventItem ev) {
//  		println(getName() + " *** (EventHandlerComponent) RECEIVES EVENT " + ev.getDefaultRep() );	
		this.setCurrentEvent(ev);
	}
  
}
