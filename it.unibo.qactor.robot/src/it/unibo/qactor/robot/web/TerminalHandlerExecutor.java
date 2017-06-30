package it.unibo.qactor.robot.web;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.platform.EventHandlerComponent;

public class TerminalHandlerExecutor extends EventHandlerComponent{
private CmdUilInterpreter cmdInterpreter;
	public TerminalHandlerExecutor(String name, QActorContext myctx, String eventId,
			IOutputEnvView view) throws Exception {
		super(name, myctx, new String[]{eventId}, view);
		cmdInterpreter = new CmdUilInterpreter();
  	}
	@Override
	public void doJob() throws Exception {
		IEventItem event = this.currentEvent; //getEventItem();
 		String msg = event.getEventId() + "|" + event.getMsg() + " from " + event.getSubj()   ;
  		showMsg( "Robot TerminalHandlerExecutor "+ msg );		
		char cmd = event.getMsg().charAt(0);
		cmdInterpreter.execute(cmd);
//		showMsg( "TerminalHandlerExecutor endofjob" );
	}
}
