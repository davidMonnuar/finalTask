/* Generated by AN DISI Unibo */ 
package it.unibo.qademo1console;
import java.util.Vector;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorMessage;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.action.AsynchActionResult;
import it.unibo.qactors.action.IMsgQueue;
import it.unibo.qactors.akka.QActor;

public class MsgHandle_Qademo1console extends QActor implements IMsgQueue{ 
	protected AsynchActionResult aar = null;
	protected boolean actionResult = true;
	protected alice.tuprolog.SolveInfo sol;
  	protected Vector<QActorMessage> msgQueue = new Vector<QActorMessage>();

	public MsgHandle_Qademo1console(String actorId, QActorContext myCtx, IOutputEnvView outEnvView )  throws Exception{
		super(actorId, myCtx, null,outEnvView, null);
	}
	@Override
	protected void doJob() throws Exception {} 

	protected void handleQActorMessage(QActorMessage msg) {
		//println(getName() + " RECEIVES QActorMessage " + msg.getDefaultRep() );	
 		insertMsgInQueue(msg);
	}	
	protected synchronized void insertMsgInQueue(QActorMessage msg){
		msgQueue.add( msg );
		//println(getName() + " INSERTED msg in queue "   );	
		this.notifyAll();
	}
	//Called by MsgHandle_Qademo1console_ctrl
	public synchronized QActorMessage getMsgFromQueue( ){
		while( msgQueue.size() == 0 ){
			try {
//				println(getName() + " WAITS "   );	
				wait();
			} catch (InterruptedException e) {
 				//println(getName() + " getMsgFromQueue INTERRUPTED "   );	
 				return null;
			}	
//			println(getName() + " RESUMES "   );				
		}//while
		QActorMessage msg = msgQueue.remove(0);
//		println(getName() + " getMsgFromQueue: " + msg  );
		return msg;
	}
	@Override
	public int  getSizeOfMsgQueue( ){
		return msgQueue.size();
	}
}
