package it.unibo.qactors.akka;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import akka.actor.ActorRef;
import akka.actor.UntypedAbstractActor;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.is.interfaces.protocols.IConnInteraction;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorMessage;
import it.unibo.qactors.QActorUtils;
 

public class ReceiverAgent extends UntypedAbstractActor{	
	
	private String autoMsgName="work";
	protected String name;
 	protected QActorContext ctx;
	protected IOutputEnvView outEnvView; 
	protected IConnInteraction conn;	
	protected Prolog prologEngine ;
	protected int nullCount = 0;
	protected static Vector<String> newCtxs = new Vector<String>(); 
	protected Vector<String> newFacts = new Vector<String>(); 
	protected Hashtable<IConnInteraction,Vector<String>> theoryOnConnTable;
	
	
	public ReceiverAgent(String name, QActorContext ctx, IOutputEnvView outEnvView, IConnInteraction conn){
		this.name 		= name;
		this.ctx 		= ctx;
 		this.conn   	= conn;
		this.outEnvView = outEnvView;
		
		prologEngine      = ctx.getEngine();
		theoryOnConnTable = new Hashtable<IConnInteraction,Vector<String>>();
	}

	@Override
	public void preStart() {
		getSelf().tell(autoMsgName, getSelf());		
	}
	@Override
	public void postStop(){
		try {
//			println(" %%%" + name + " closeConnection");
			conn.closeConnection();
		} catch (Exception e) {
 			e.printStackTrace();
		}
	}
	
	@Override
	public void onReceive(Object arg0) throws Throwable {
 		while(true){ 
 			try {
// 				    println("	%%%ReceiverAgent " + name + " WAITS FOR MESSAGES on conn=" + conn );
				    String msg = conn.receiveALine();
// 	      			println("	%%%ReceiverAgent " + name + " RECEIVED:" + msg  );
 				if( msg != null ){
 					if( msg.equals(CtxServerAgent.endOfSystem)) break;
 					
					if( msg.contains("updatesyskb") && msg.contains("remotectx") ){
 						updateLocalTheory(msg);
					} 
					else 
						elab(msg);			
				} else{
						println("	%%%ReceiverAgent received null on "  + conn  );
						nullCount++ ;
							if( nullCount> 5 ){
								conn.closeConnection();
								break;
							}
 				}
 			} catch (Exception e) {
// 	 			println( "	%%%ReceiverAgent: " + name + " ERROR "+ e.getMessage() );
// 				cleanLocalTheory();
 				break;	
 			}			
		}//while
// 		println( "	%%%ReceiverAgent " + name + " ENDS on connections " +  conn  );		
	}
/*
 * 
 */
	private int niter = 0;
	public  void elab(String msg ) throws Exception{
		try{
			//msg( MSGID, MSGTYPE, SENDER, RECEIVER, CONTENT, SEQNUM )
	  		msg = msg.replaceAll("&", ","); 
	//  		println( "	%%%ReceiverAgent " + name + " elab " +  msg  );
			if( QActorUtils.getMsgType(msg,ctx.getEngine()).contains("event")){
	 			QActorUtils.sendToEventLoopActor(ctx, msg, ctx.getSystemCreator() );	//The message is sent by the 'system'
	 			return;
			}
			/*
			 * The message is not an event
			 * WARNING: a message can arrive before that the actor receiver is started
			 */
	  		QActorMessage qamsg = new QActorMessage(msg);  	
	  		/*
	 		ActorSelection dest = null; 
			while( dest == null ){ //SHOULD BE but it could NOT BE !!
				dest = QActorUtils.getSelectionIfLocal(ctx, qamsg.msgReceiver());
				Thread.sleep(100);
				if( niter++ > 50 ){
					//println( "	%%%ReceiverAgent " + name + " SORRY for " +  msg  );
					println( "	%%% WARNING: ReceiverAgent SENDING " +  msg + " BEFORE THAT " + dest + " STARTED");
					niter = 0;
					return;
				}
	 		}
			dest.tell(qamsg, getSelf() );	//The message is sent by the 'system'
			*/
	  	//JUNE2017 after update to 2.5.2
	  		ActorRef aref = ctx.getActorRefInQActorContext(qamsg.msgReceiver());
	  		while( aref == null ){//SHOULD BE but it could NOT BE !!
	  			Thread.sleep(100);
	  			println( "	%%% WARNING: ReceiverAgent DOES NOT FIND " +  qamsg.msgReceiver());
	  			aref = ctx.getActorRefInQActorContext(qamsg.msgReceiver());
				if( niter++ > 50 ){
					//println( "	%%%ReceiverAgent " + name + " SORRY for " +  msg  );
					println( "	%%% WARNING: ReceiverAgent SENDING " +  msg + " BEFORE THAT " + aref + " STARTED");
					niter = 0;
					return;
				}
	  		}
//	  		println( "	%%%  ReceiverAgent tell  " +  qamsg  + " to " + aref);
	  		aref.tell(qamsg, getSelf() );	//The message is sent by the 'receiver agent (the system)'
//	  		Thread.sleep(5000);		
	//  		println( "	%%%ReceiverAgent " + name + " processing " +  dest  );
		}catch(Exception e){
//			println( "	%%%ReceiverAgent " + name + " ERROR   " +  e.getMessage()  );
		}
 	}
	
	/*
	 * 	--------------------------------------------------------
	 *  UPDATING THERORY
	 * 	--------------------------------------------------------
	 */
 	/*
	 * Dynamic MONOTONIC updating of the system knowledge base
	 */
	protected void updateLocalTheory(String msg)  {
		try{
			String thRep = QActorUtils.getContentMsg(msg,ctx.getEngine());
			
// 			println("	%%%ReceiverAgent updateLocalTheory   " + thRep ); 
			int curCtxNum = newCtxs.size();
			thRep = thRep.substring(1, thRep.length()-1); //delete ' '
			//println(" %%%updateLocalTheory  thRep : " + thRep   );
			String[] facts = thRep.split("@");
			for( int i=0; i<facts.length; i++){
				if( facts[i].length() > 0 ){
					boolean b = checkInsertFact( facts[i] );
					/*
					 * checkInsertFact returns false if the context is already present
					 */
				}
	 		}			 
			//MAY2017: we should check for the context names NOT for the context number
			if( newCtxs.size() > curCtxNum ){
				//There are new contexts: update the theory rep
//		 		println("	%%%ReceiverAgent updateLocalTheory cur/new=" + curCtxNum+"/"+newCtxs.size());
				ctx.updateLocalTheoryRep();
				createSupports();
				newCtxs = new Vector<String>();
	 		}else{
//	 			println("	%%%ReceiverAgent updateLocalTheory : no new context number   "  );
	 		}
		}catch(Exception e){
			println("	%%%ReceiverAgent UNABLE TO UPDATE THEORY"   );
		}
	}
	
	protected void checkReconnection(String msg)  {
		
	}

	/*
	 * Insert a new system fact if not already present
	 */
	protected boolean checkInsertFact(String fact) throws Exception{
//  		println(" %%%ReceiverAgent  checkInsertFact : " + fact   );
		Term tf = Term.createTerm(fact);
		Struct tfs = null;
		if( fact.startsWith("context")){
			tfs = (Struct) tf;
			String host = tfs.getArg(1).toString();
			fact = fact.replace(host, "ANYHOST");
		}
 		SolveInfo sol  = prologEngine.solve( fact + ".");
		if( sol.isSuccess() ) {
			return false; //already in 
		}
		else{
//  			println("	%%%ReceiverAgent checkInsertFact new fact : " + tf   );
			prologEngine.solve( "assertz(" + tf.toString() + ").");
			newFacts.add( tf.toString() );
			if( tfs != null ){
				//Dynamic MONOTONIC extension of the context interaction support
				String curCtx = tfs.getArg(0).toString();//fact.substring(fact.indexOf("(")+1, fact.indexOf(","));
 				println("	%%% ReceiverAgent added: " + tf   );
				newCtxs.add(curCtx);
			}
		}
		return true;
	}
	
	protected void cleanLocalTheory() {
		println("	%%%ReceiverAgent cleanLocalTheory  "   ) ;
		Vector<String> ctxs = theoryOnConnTable.get(conn);
		if( ctxs == null ) return;
		Iterator<String> it = ctxs.iterator();	
		try {
			while( it.hasNext() ){
				String curFact = it.next();
//				println("	%%%ReceiverAgent removing " + curFact ) ;
				prologEngine.solve( "retract(" + curFact + ").");
	 		}	
			ctx.updateLocalTheoryRep();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	protected void createSupports() throws Exception{
		//Create a sender connection to new contexts and propagate the theory rep
		Iterator<String> it = newCtxs.iterator();	
		while( it.hasNext() ){
			String curCtx = it.next();
			ctx.activateSenderToCtx( curCtx, true ); //The sender terminates if connection lost
 		}		
		theoryOnConnTable.put(conn, newFacts);
	}
	
	protected void println(String msg) {
  		ctx.getOutputEnvView().addOutput(msg);
	}

}
