package it.unibo.qactors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import alice.tuprolog.Library;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import alice.tuprolog.Var;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.action.ActionSolveTimed;
import it.unibo.qactors.action.AsynchActionResult;
import it.unibo.qactors.action.IActorAction;
import it.unibo.qactors.akka.QActor;
import it.unibo.qactors.akka.SenderObject;
import it.unibo.qactors.akka.SystemCreationActor;
import it.unibo.qactors.platform.EventItem;
import it.unibo.qactors.platform.EventLoopActor;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class QActorUtils {
	public static final String guardVolatile  	 = "volatile";
	public static final String guardPermanent 	 = "permanent";
	private static int nameCount = 1;
	private static Hashtable<String,QActor> qActorTable = new Hashtable<String,QActor>();
	public static final String locEvPrefix   ="local_";
	public static final String eventLoopNamePrefix = "evlpa_";
	public static String robotBase = "mock";
	
	public static String getNewName(String prefix){
  		return prefix+nameCount++;
  	}

  	public static void resetActorTable(){
  		qActorTable = new Hashtable<String,QActor>();
//  		System.out.println("qActorTable reset "  );
  	}
	/*
	 * LEAVE HERE (line 63) since refenced by Latex	
	 */
	public static String adjust(String fname ){
		return fname.replace("\\", "/").replace("'", "");
	}
	public static String getEventLoopActorName(String ctxName){
		return eventLoopNamePrefix+ctxName;
	}
	public static synchronized void memoQActor( QActor actor ){
		qActorTable.put(actor.getName(), actor);
// 		System.out.println("memoQActor " + actor.getName() );
	}
	public static synchronized void forgetQActor( QActor actor ){
		qActorTable.remove( actor.getName() );
//		System.out.println("forgetQActor " + actor.getName() );
	}
	
	public static void setRobotBase( String name){
		robotBase = name;
	}
	public static String getRobotBase(  ){
		return robotBase ;
	}
 
	public static QActor getQActor( String name ){
		QActor qa = qActorTable.get(name);
		if( qa != null ) return qa;
		else return waitUntilQActorIsOn(   name );
	}
	public static QActor waitUntilQActorIsOn( String name )  {
// 			System.out.println("	%%% QActorUtils waitUntilQActorIsOn " + name    );
  			QActor qa = null;
			while( qa == null ){
				qa = qActorTable.get(name);
				if( qa == null){
    				//System.out.println("	%%% QActorUtils " + name + "  not yet created ..."  );
					try {
 						Thread.sleep(50);
					} catch (InterruptedException e) {
 						e.printStackTrace();
					}
 				}
			}
//  			System.out.println("	%%% QActorUtils " + name + " created " + qa  );
			return qa;
 	}
	
//	public static void terminateTheQActorSystem( boolean testing ){
//// 		System.out.println("	%%% ActorUtils terminateTheQActorSystem "   );
//		resetActorTable();
//		ActorTerminationMessage msg = new ActorTerminationMessage("all", testing);
//		actor.getQActorContext().getSystemCreator().tell(msg, systemCreator );
//	}
	public static void terminateTheQActor(QActor actor, boolean testing){ //MARCH2017
//   		System.out.println("	%%% ActorUtils terminateTheQActor " + actor.getName() + " testing=" + testing  );
 		ActorTerminationMessage msg = new ActorTerminationMessage( actor.getName(), testing );
 		/*
		ActorSelection sel = getSelectionIfLocal(  actor.getQActorContext(),   actor.getName() );
//   		System.out.println("	%%% ActorUtils terminateTheQActor sel=" + sel  );
    	if( sel != null ){
//      		qActorTable.remove(actor.getName());
  			actor.getQActorContext().getSystemCreator().tell(msg, getActorRefFromActorSelection(sel) );
   		}
   		*/
 		//JUNE2017 after update to 2.5.2
 		ActorRef actorRef = actor.getQActorContext().getActorRefInQActorContext(actor.getName());
 		if( actorRef != null )
 			actor.getQActorContext().getSystemCreator().tell(msg, actorRef );
 		
	}
	public static void terminateTheQActorSystem(ActorRef sysCreator, boolean testing){ //MARCH2017
    System.out.println("	%%% ActorUtils terminateTheQActorSystem testing=" + testing  );
//	ActorTerminationMessage msg = new ActorTerminationMessage( sysCreator.getName(), testing );
//	ActorSelection sel = getSelectionIfLocal(  sysCreator.getQActorContext(),   sysCreator.getName() );
		
//		while( ! qActorTable.isEmpty() ){
			 Iterator<QActor> itqa = qActorTable.values().iterator();
			 while( itqa.hasNext() ){
				 QActor qa = itqa.next();
//				 if( ! ( qa.getName().contains("evlpa") || qa.getName().contains("ctrl")) ){
				 if( ! ( qa.getName().contains("evlpa")  ) ){
					 System.out.println("	%%% QActorUtils terminateTheQActorSystem qa=" + qa.getName()  );
					 /*
					 ActorSelection sel = getSelectionIfLocal(  qa.getQActorContext(),   qa.getName() );
					 ActorTerminationMessage msg = new ActorTerminationMessage( qa.getName(), testing );
					 if( sel != null )
						 sysCreator.tell(msg, getActorRefFromActorSelection(sel) );
					*/
					//JUNE2017 after update to 2.5.2
					ActorRef actorRef = qa.getQActorContext().getActorRefInQActorContext(qa.getName());
					if( actorRef != null ){
						ActorTerminationMessage msg = new ActorTerminationMessage( qa.getName(), testing );
						sysCreator.tell(msg, actorRef );
					}
				 
				 }
			 }
			 
//		}
 		
	}
//	public static void terminateTheQActorSystem(QActor actor, boolean testing){ //MARCH2017
//   		while( ! qActorTable.isEmpty() ){
//   		 System.out.println("	%%% ActorUtils WAITS FOR terminateTheQActorSystem " + actor.getName() + " testing=" + testing  );
////   		 resetActorTable();	//MARCH2017
//   		 try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//   		}
//  		 System.out.println("	%%% ActorUtils terminateTheQActorSystem " + actor.getName() + " testing=" + testing  );
//  			
//   		 
////		ActorTerminationMessage msg = new ActorTerminationMessage( actor.getName(), testing );
////		ActorSelection sel = getSelectionIfLocal(  actor.getQActorContext(),   actor.getName() );
////   		System.out.println("	%%% ActorUtils terminateTheQActorSystem sel=" + sel  );
////   		if( sel != null )
////   			actor.getQActorContext().getSystemCreator().tell(msg, getActorRefFromActorSelection(sel) );
//	}
  	public static int getCtxPort(Prolog prologEngine, String ctxId) throws Exception{
// 		System.out.println("QActorUtils getCtxPort  "+ctxId);
		SolveInfo sol = prologEngine.solve("getCtxPort( " + ctxId +", PORTNAME ).");	
		if( ! sol.isSuccess() ) throw new Exception("context port not found");
		String portStr = ""+sol.getVarValue("PORTNAME");
		portStr = portStr.replaceAll("'", "");
		int port = Integer.parseInt(portStr);
		return port;
 	}

 	public static boolean checkIfAkkaImplementation( Prolog prologEngine ) throws Exception{
		SolveInfo sol = prologEngine.solve("using( akka ).");	
		if(  sol.isSuccess() ) return true;
		else return false;
 	}
	 
 	/*
 	 * Assumption: theory loaded
 	 */
	public static String createLocalTheoryRep( QActorContext ctx, Prolog prologEngine, IOutputEnvView outEnvView) throws Exception{
		String localTheoryRep = "\"";
		String contextRep = repOfContetxts(ctx,prologEngine,outEnvView,false);
		String actorRep   = repOfActors(prologEngine,outEnvView,false); //
		localTheoryRep    = localTheoryRep + contextRep + actorRep + "\"";
 		return localTheoryRep;
	} 	
	public static String repOfContetxts(QActorContext myctx, Prolog prologEngine, IOutputEnvView outEnvView, boolean show) throws Exception{
		String contextRep = "";
		SolveInfo sol   = prologEngine.solve("getTheContexts(CTXS).");	
		if( ! sol.isSuccess() ) {
			throw new Exception("no context found in context") ;
		}else{
			Struct ctxList  = (Struct) sol.getVarValue("CTXS");
			 
			Iterator<? extends Term> it = ctxList.listIterator();
			while( it.hasNext() ){
				String ctx = ""+it.next();
 				if(show) outEnvView.addOutput("	+++ " + ctx);
				contextRep = contextRep + "@" + ctx  ;
				myctx.incNumOfContexts();
			}
		}
		return contextRep;
	}
	
	public static String repOfActors(Prolog prologEngine, IOutputEnvView outEnvView, boolean show) throws Exception{
		String actorRep ="";
		SolveInfo sol   = prologEngine.solve("getTheActors(ACTORS).");	
		if( ! sol.isSuccess() ) {
			throw new Exception("no actor found in context");			
		}else{
			Struct actorList  = (Struct) sol.getVarValue("ACTORS");
			Iterator<? extends Term> ita = actorList.listIterator();
			while( ita.hasNext() ){
				String actor = ""+ita.next();
				if(!  actor.contains("evlpa")){	//do not include  qactor( evlpaxxx, ... )
 					if(show) outEnvView.addOutput("	+++ " + actor);
					actorRep = actorRep + "@" + actor  ;
				}
			}
		}
		return actorRep;
	}
	
	public static void activateAkkaSystem(String name, QActorContext ctx){
		if( ctx.getActorSystem() != null ){
			System.out.println("activateAkkaSystem ALREADY DONE");
			return;
		}
 //	    String configFile= name+".conf";
 	    Config config       = ConfigFactory.parseFile(new File("akkaConfig.conf"));
	    ctx.getOutputEnvView().addOutput("	%%% QActorUtils activateAkkaSystem in progress ... "  + name + " config=" + config);
	    /*
 	     * An actor system is a hierarchical group of actors which share common configuration, 
 	     * e.g. dispatchers, deployments, remote capabilities and addresses. 
 	     * It is also the entry point for creating or looking up actors.
 	     */
  	    ActorSystem system  = ActorSystem.create( name, config  ); //ActorSystem.create( name, config ); //MAY2017
// 	    ActorSystem system  = ActorSystem.create( name , null, null, null ); //JUNE2017
// 	    ExecutionContext ex = system.dispatchers().lookup("akka.actor.default-dispatcher");  //JUNE2017
// 	    System.out.println( "	%%% QActorUtils ExecutionContext " + ex    );
//	    ctx.setActorSystem(system);
	    ActorRef systemCreator = system.actorOf( Props.create(  SystemCreationActor.class,  name, ctx  ),  name );
	    ctx.setSystemCreator( systemCreator  );
 	    ctx.setActorSystem( system );
 	    QActorUtils.waitUntilQActorIsOn(name);
 	    ctx.actorCtxTab.put(name, systemCreator);
   	}
	 
//	public static int activateAkkaActorsInContext( 
//			UntypedActorContext actorCtx, ActorContext ctx,Prolog ctxPrologEngine, IOutputEnvView outEnvView) throws Exception{
//		int n3 = activateActorsInContext(actorCtx,  ctx, "robot" , ctxPrologEngine,  outEnvView);
//		int n2 = activateActorsInContext(actorCtx,  ctx, "eventhandler" , ctxPrologEngine,  outEnvView);
//		int n1 = activateActorsInContext(actorCtx,  ctx, "qactor" , ctxPrologEngine,  outEnvView);
//		return n1 + n3 ; //we do not return n2 to allow strong termination when all the actors die
// 	}
//	public static  int activateActorsInContext( UntypedActorContext actorCtx, ActorContext ctx, 
//			String actorType, Prolog ctxPrologEngine, IOutputEnvView outEnvView) throws Exception{
//		int numOfActors=0;
//		String goal="";
//		if( actorType.equals("qactor"))
//			goal =  "qactor(ACTOR, CTX, CLASS )."  ;
//		else if( actorType.equals("eventhandler"))
//			goal= "eventhandler(ACTOR, CTX, CLASS, EVENTS )." ;
//		else if( actorType.equals("robot"))
//			goal= "qactor(ACTOR, CTX, CLASS, robot )." ;
//		goal = goal.replace("CTX", ctx.getName());
//		ctx.getOutputEnvView().addOutput("	activateActorsInContext GOAL=" + goal  );
//	 	SolveInfo actorSol = ctxPrologEngine.solve(goal);
//		if( actorSol.isSuccess() ){
//			activateTheProperActor(ctx,actorType,actorSol,outEnvView);
// 			numOfActors = 1;
//			ctx.getOutputEnvView().addOutput("	activateActorsInContext actorSol =" + actorSol  );
//			while( actorSol.hasOpenAlternatives() ){
//				ctx.getOutputEnvView().addOutput("	activateActorsInContext OPEN for" + goal  );
//				actorSol = ctxPrologEngine.solveNext();
//				if( actorSol.isSuccess() ){ 
//					activateTheProperActor(ctx,actorType,actorSol,outEnvView);
//					numOfActors++;
//				}					 
//			}
//		}
//		return numOfActors;
// 	}
	
//	protected static  void activateTheProperActor(ActorContext ctx, String actorType, 
//			SolveInfo actorSol,IOutputEnvView outEnvView) throws Exception{
//		String actorName = actorSol.getVarValue("ACTOR").toString();
//		String className = actorSol.getVarValue("CLASS").toString().replaceAll("'", "");
// 		ctx.getOutputEnvView().addOutput("	activateTheProperActor " + actorName + " " + actorType );
//		if( actorType.equals("qactor") ){ 
//			activateAkkaQActor(ctx,className,actorName,outEnvView);
//		}else if( actorType.equals("robot") ){
//			activateAkkaRobot(ctx,className,actorName,outEnvView);
//		}
//		else if( actorType.equals("eventhandler") ){
//			String events = actorSol.getVarValue("EVENTS").toString();
//			activateAkkaEventHandler(ctx,className,actorName,outEnvView,events);
//		}
//	}
	
 
	
	public static ActorRef activateAkkaQActor( 
			QActorContext ctx,String className, String actorName, IOutputEnvView outEnvView ) throws Exception{
 		System.out.println( "	%%% QActorUtils activateAkkaQActor " + actorName + " " + className );
 		ActorRef aref = ctx.getAkkaContext().actorOf( 
					Props.create(Class.forName(className), actorName, ctx, outEnvView), actorName );
		waitUntilQActorIsOn(actorName);
  		return aref;
	}
	public static ActorRef activateAkkaRobot( 
			QActorContext ctx,String className, String actorName, IOutputEnvView outEnvView ) throws Exception{
// 		System.out.println( "	%%% QActorUtils activateAkkaRobot " + actorName  );
 		ActorRef aref = ctx.getAkkaContext().actorOf( 
					Props.create(Class.forName(className), actorName, ctx, outEnvView, ctx.getRobotBase() ), actorName );
		waitUntilQActorIsOn(actorName);
  		return aref;
	}
	public static ActorRef activateAkkaEventHandler( 
			QActorContext ctx,String className, String actorName, IOutputEnvView outEnvView,String events) throws Exception{
 	 	System.out.println( "	%%% QActorUtils activateAkkaEventHandler " + actorName + " events=" + events + " className=" + className);
  	 	String[] eventArray =  createArray( events.replaceAll("'", "") );
	 	ActorRef aref = ctx.getAkkaContext().actorOf( 					
					Props.create(Class.forName(className), actorName, ctx, outEnvView, eventArray), actorName );
		waitUntilQActorIsOn(actorName);			
 		return aref;
	}

	
	
	
//	public static ActorRef activateEventLoopActor( 
//			ActorContext ctx,String className, String actorName, IOutputEnvView outEnvView) throws Exception{
//		System.out.println( "	%%% activateEventLoopActor " + actorName );
//		ActorRef aref = creatorAkkaContext.actorOf( 
//				Props.create(Class.forName(className), actorName, ctx, outEnvView), actorName );
//		waitUntilQActorIsOn(actorName);
//		evlpActorRef = aref;
//		return aref;
//	}
	
	
//	public static void activateJavaActorsInContext( 
//			ActorContext ctx, IOutputEnvView outEnvView) throws Exception{
//		Prolog ctxPrologEngine = ctx.getEngine();
//	 	SolveInfo actorSol = ctxPrologEngine.solve("qactor( ACTOR, CTX, CLASS ).");
//		if( actorSol.isSuccess() ){
// 			String actorName = actorSol.getVarValue("ACTOR").toString();
// 			String className = actorSol.getVarValue("CLASS").toString().replaceAll("'", "");
//  			createJavaActorDynamically(actorName,className,ctx,outEnvView);			 
//			while( actorSol.hasOpenAlternatives() ){
//				actorSol = ctxPrologEngine.solveNext();
//				if( actorSol.isSuccess() ){ 
//					actorName = actorSol.getVarValue("ACTOR").toString() ;
//					className = actorSol.getVarValue("CLASS").toString().replaceAll("'", "");					
//					createJavaActorDynamically(actorName,className,ctx,outEnvView);
//				}					 
//			}
//		}		
//	}
	
	public static void  startEventLoopActor(QActorContext ctx) throws Exception{
 		String eventLoopName = getEventLoopActorName(ctx);
// 		System.out.println( "	%%% startEventLoopActor STARTS:" + eventLoopName );
		ActorRef aref = ctx.getAkkaContext().actorOf( 
				Props.create(Class.forName("it.unibo.qactors.platform.EventLoopActor"), 
						eventLoopName, ctx, ctx.getOutputEnvView()), eventLoopName );
		waitUntilQActorIsOn(eventLoopName);
//		evlpActorRef = aref;
		ctx.setEvlpActorRef( aref );
		ctx.setEventLoopActor(  (EventLoopActor) getQActor(eventLoopName) );
		ctx.actorCtxTab.put(eventLoopName, aref);
		System.out.println( "	%%% QActorUtils startEventLoopActor ENDS aref=" + aref + " " + ctx.getEvlpActorRef()  );		 	
 	}
	
	public static void createJavaActorDynamically(
			String actorName, String className, QActorContext myCtx,IOutputEnvView outEnvView) throws Exception{
		Class<?> clazz = Class.forName(className);
		Constructor<?> ctor = clazz.getConstructor(String.class, QActorContext.class, IOutputEnvView.class);
		Thread object = (Thread) ctor.newInstance(new Object[] { actorName, myCtx, outEnvView });
		object.start();		
	}

	public static  QActor createActionTerminationEvHandler( 
  			QActorContext myCtx, IOutputEnvView outEnvView, String clazz, String teminationevid ) throws Exception{
  	 	String name       =  QActorUtils.getNewName("evh_");
 	 	String[] alarmIds = new String[]{teminationevid};
//	 	ActorRef aref = 
	 			myCtx.getAkkaContext().actorOf( Props.create(
	 					Class.forName(clazz), 
	 					name, myCtx,  outEnvView, alarmIds), name );	
	 	/*
	 	* The action must be activated AFTER that the handler has been registered
	 	*/
	 	return QActorUtils.waitUntilQActorIsOn(name); 	 
 	}


	public static String buildStringMsg(QActorContext ctx, String senderId, String msgID, String destActorId, String msgType, String msg){
		return "msg(" + msgID + ","+ msgType + "," + senderId + ","+ destActorId +","+ msg+","+ ctx.newMsgnum() + ")";
	}
	
	public static QActorMessage buildMsg(
			QActorContext ctx, String senderId, String msgID, String destActorId, String msgType, String msg) throws Exception{
		return new QActorMessage(msgID, msgType,senderId, destActorId, msg, ""+ctx.newMsgnum());
	}
//	public static QActorMessage buildStructMsg(String msg) throws Exception{
//		return new QActorMessage(msg);
//	}
	
	public static String getEventLoopActorName(QActorContext ctx){
		return getEventLoopActorName(ctx.getName());
	}

	public static void raiseEvent( QActorContext ctx, String emitter, String evId, String evContent) throws Exception {
		check_ifexists_evlpActor(ctx);
		IEventItem ev = buildEventItem(  emitter, evId, evContent  );
//		System.out.println("QActorUtils raiseEvent  "+ ev );
		ctx.getEvlpActorRef().tell(ev, ActorRef.noSender() );
   		if( ! evId.startsWith(QActorUtils.locEvPrefix)){
  			String evRep = ev.getPrologRep();
  			propagateEvent(ctx,evRep); 
  		}		
	}
	
	public static void raiseEvent( QActor actor, String emitter, String evId, String evContent) throws Exception {
		if( actor == null ) throw new Exception("Actor null. Please use context");
		QActorContext ctx = actor.getQActorContext();
		check_ifexists_evlpActor(ctx);
		IEventItem ev = buildEventItem(  emitter, evId, evContent  );
//		System.out.println("QActorUtils raiseEvent " + ev.getEventId() + " evlpActorRef=" + evlpActorRef);
		ctx.getEvlpActorRef().tell(ev, actor.getSelf());
   		if( ! evId.startsWith(QActorUtils.locEvPrefix)){
  			String evRep = ev.getPrologRep();
  			propagateEvent(ctx,evRep); 
  		}		
	}

	protected static void  check_ifexists_evlpActor( QActorContext ctx ){ 
		if( ctx.getEvlpActorRef() != null ) return;
		String evlpName = getEventLoopActorName(ctx);
/*
		ActorSelection evlpActorSel = getSelectionIfLocal(ctx, getEventLoopActorName(ctx));
		ctx.setEvlpActorSel( evlpActorSel ); 
		ctx.setEvlpActorRef( getActorRefFromActorSelection(evlpActorSel) );
		ctx.setEventLoopActor(  (EventLoopActor) getQActor(evlpName) );
*/
		//JUNE2017 after update to 2.5.2
		ActorRef aref = ctx.getActorRefInQActorContext(evlpName);
		if( aref == null ){
			System.out.println("WARNING ABOUT " + evlpName );
		}
//		ctx.setEvlpActorRef( aref );
//		ctx.setEventLoopActor(  (EventLoopActor) getQActor(evlpName) );
	}

//	public static EventLoopActor getEventLoopActor(){
//		return evlpActor;
//	}
	public static ActorRef getActorRefFromActorSelection(ActorSelection sel){
		Timeout timeout = new Timeout(Duration.create(2, "seconds"));
		Future<ActorRef> future = sel.resolveOne(timeout);
		ActorRef result = null;
		try {
			result = (ActorRef) Await.result(future, timeout.duration());
 		} catch (Exception e) {
 		}
		return result;
	}

/*	
	public static  ActorSelection getSelectionIfLocal(  QActorContext ctx, String destActorId){
// 		System.out.println( "		%%%* getSelectionIfLocal " + destActorId + " in " + ctx.getName() + " akkaCtx=" + ctx.getAkkaContext());
		ActorSelection asel = ctx.getAkkaContext().actorSelection("/user/"+ctx.getName()+"/"+destActorId);
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<ActorRef> future = asel.resolveOne(timeout);
		boolean actorIsLocal    = ! future.toString().contains("Failure");
  		System.out.println( "		%%%* f=" + actorIsLocal + " future=" + future.toString());
		if( actorIsLocal ) return asel;
		else return null;
	}
*/
	
/*
* (SYSTEM) MESSAGES	
*/	
	
	public static String buildEmitEventMsg( String evId, String msg ){
		return "emit(" + evId + "," + msg + ")";
	}
	public static String getMsgReceiverActorId( String msg, Prolog prologEngine ) throws Exception{
//		println("getMsgReceiverActorId of " + msg  );
		SolveInfo sol  = prologEngine.solve( "getMsgReceiverId("+ msg +", ACTORID ).");	
		if( ! sol.isSuccess() ) return "unknown";
		String actorId = ""+sol.getVarValue("ACTORID");
 		return actorId;		
	}
	public static String getMsgSenderActorId( String msg, Prolog prologEngine ) throws Exception{
//		System.out.println("getMsgSenderActorId of " + msg  );
		SolveInfo sol  = prologEngine.solve( "getMsgSenderId("+ msg +", ACTORID ).");	
		if( ! sol.isSuccess() ) return "unknown";
		String actorId = ""+sol.getVarValue("ACTORID");
		//println("actorId of " + msg + " = " + actorId);
		return actorId;		
	}
	public static String getMsgId( String msg, Prolog prologEngine ) throws Exception{
		//println("getMsgId of " + msg  );
		SolveInfo sol  = prologEngine.solve( "getMsgId("+ msg +", MSGID ).");	
		String msgId = ""+sol.getVarValue("MSGID");
		//println("msgType of " + msg + " = " + msgId);
		return msgId;		
	}
	public static String getMsgType( String msg, Prolog prologEngine ) throws Exception{
//		println("getMsgType of " + msg + " prologEngine=" + prologEngine );
		SolveInfo sol  = prologEngine.solve( "getMsgType("+ msg +", MSGTYPE ).");	
		if( ! sol.isSuccess() ) return "unknown";
		String msgType = ""+sol.getVarValue("MSGTYPE");
//		println("msgType of " + msg + " = " + msgType);
		return msgType;		
	}
//	public static ActorSelection getReceiverActor( String msg, QActorContext ctx ) throws Exception{
//		String actorId = getMsgReceiverActorId(   msg, ctx.getEngine() );
//  		System.out.println("receiveactorId of " + msg + " = " + actorId    );
//		return getSelectionIfLocal(ctx,actorId);
//	}
	
	public static String getContentMsg( String msg, Prolog prologEngine ) throws Exception{
 		SolveInfo sol  = prologEngine.solve( "getMsgContent("+ msg +", MSGCONTENT ).");	
		if( ! sol.isSuccess() ) return "unknown";
		String msgContent = ""+sol.getVarValue("MSGCONTENT");
 		return msgContent;		
	}
 	
	public static String getActorCtx(String actorId, Prolog prologEngine) throws Exception{
  		if( actorId.startsWith("evlpa_")) actorId = actorId.replace("evlpa_", ""); //OCT2016
// 		System.out.println("			getActorCtx of " + actorId  );
		SolveInfo sol = prologEngine.solve("qactor( " + actorId +", CTX , CLASS ).");	
		if( ! sol.isSuccess() ) return "unknown";
		String ctxName = ""+sol.getVarValue("CTX");
		//println("ctxName of " + actorId + " = " + ctxName);
		return ctxName;
 	}
	public static  String getCtxProtocol(String ctxId, Prolog prologEngine) throws Exception{
		SolveInfo sol = prologEngine.solve("getCtxProtocol( " + ctxId +", PROTOCOL ).");	
		if( ! sol.isSuccess() ) return "unknown";
		String protocolName = ""+sol.getVarValue("PROTOCOL");		
		//println("protocolName of " + ctxId + " = " + protocolName);
		return protocolName;
 	}
	public static  String getCtxHost(String ctxId, Prolog prologEngine) throws Exception{
		SolveInfo sol = prologEngine.solve("getCtxHost( " + ctxId +", HOSTNAME ).");	
		if( ! sol.isSuccess() ) return "unknown";
		String hostName = ""+sol.getVarValue("HOSTNAME");
		//println("hostName of " + ctxId + " = " + hostName);
		return hostName;
 	}
 	public static  int getCtxPort(String ctxId, Prolog prologEngine) throws Exception{
// 		println("getCtxPort of " + ctxId  );
		SolveInfo sol = prologEngine.solve("getCtxPort( " + ctxId +", PORTNAME ).");	
		if( ! sol.isSuccess() ) return 8010;
		String portStr = ""+sol.getVarValue("PORTNAME");
		portStr = portStr.replaceAll("'", "");
		int port = Integer.parseInt(portStr);
//		println("port of " + ctxId + " = " + port);
		return port;
 	}
 	

/*
 * 	
 */
 	public static void sendToEventLoopActor(QActorContext ctx, String msg, ActorRef sender){
		String eventLoopActorName = QActorUtils.getEventLoopActorName(ctx);
		/*
		ActorSelection dest       = getSelectionIfLocal(ctx,eventLoopActorName);
//		System.out.println("QActorUtils sends event " + msg +" to dest="  + dest  );
		//SEND
		if( dest != null )  dest.tell(msg, sender);
		*/
		//JUNE2017 after update to 2.5.2
		ActorRef dest = ctx.getActorRefInQActorContext(eventLoopActorName);
//		System.out.println("QActorUtils sends event " + msg +" to dest="  + dest  );
		//SEND
		if( dest != null )  dest.tell(msg, sender);
		
 	}
 	
/*
 * EVENTS	
 */
  public static IEventItem buildEventItem(String subj, String evId, String evContent ){
	  IEventItem event = null;
	  try {
		  event = new EventItem( evId, evContent, EventLoopActor.getLocalTime() , subj );
	} catch (Exception e) {
 		e.printStackTrace();
	}
	  return event;
  }
  
  public static void propagateEvent(QActorContext ctx, String evRep) throws Exception{
//  	    ctx.getOutputEnvView().addOutput("QActorUtils propagateEvent evRep: " + evRep );
		Prolog prologEngine = ctx.getEngine();
		SolveInfo sol       = prologEngine.solve("getCtxNames( CTXNAMES ).");	
		Struct ctxList      = (Struct) sol.getVarValue("CTXNAMES");
		Iterator<? extends Term> it = ctxList.listIterator();
		String curOtherCtx="";
		while( it.hasNext() ){ 
			try {
				curOtherCtx       = ""+it.next();
		  	    //ctx.getOutputEnvView().addOutput("QActorUtils curOtherCtx: " + curOtherCtx );
//				String evLoppName = eventLoopNamePrefix+curOtherCtx;
				SenderObject sa   = ctx.getSenderObject(curOtherCtx); //evLoppName pct2016
				if( sa != null ) {
 					//ctx.getOutputEnvView().addOutput("QActorUtils propagateEvent FOUND sa for " + "evlpa"+curOtherCtx + " " + evRep);
					sa.sendMsg(evRep);
				}
			} catch (Exception e) {
 				ctx.getOutputEnvView().addOutput("propagateEvent " + curOtherCtx + " " + evRep + " ERROR " + e.getMessage() );
				//ctx.clearSenderAgent(eventLoopNamePrefix+curOtherCtx);
 				ctx.activateSenderToCtx( curOtherCtx, true );  //MAY2017
			}
		}
	}

/*
 * REGENERATE
 */
  
	/*
	 * HORRIBLE: this should be redesigned
	 */
	public static String replaceVarInStruct(String pstructStr, String varin, String valin){
		Struct ps = (Struct) Term.createTerm(pstructStr);
		int arity = ps.getArity();
		String newttStr = "";
//		pstructStr = pstructStr.replace("\"", "?");//May 2017
//		pstructStr = pstructStr.replace("'", "?");//May 2017
// 		System.out.println("	%%% QActorUtils replaceVarInStructENTRY in " + pstructStr +" varin=" +  varin + " val=" + valin);
		for( int i=0; i<arity; i++ ){
			Term argi    = ps.getArg(i);
			String ttStr = argi.toString();
// 			System.out.println("	replaceVarInStruct ttStr=" + ttStr + " i=" + i + " argi=" + argi);
	 		if( argi.isAtom() ) continue;
	 		if( argi instanceof Var ){
				if( varin.equals( ttStr ) ){
					newttStr  = ttStr.replace(varin,valin);
					int indexoflp = pstructStr.indexOf("(");	//index of "("						
					int predCharIndex = pstructStr.indexOf(ttStr) - 1;
//					System.out.println("replaceVarInStruct indexoflp=" + indexoflp + " predCharIndex="+  predCharIndex);
					if( predCharIndex < 0 ) return pstructStr; //already all done
					if( predCharIndex < indexoflp )  //we must skip the functor
						predCharIndex = indexoflp + pstructStr.substring(indexoflp,pstructStr.length()).indexOf(ttStr) - 1;
					String predChar = ""+pstructStr.charAt(predCharIndex);
//					System.out.println("replaceVarInStruct predChar=" + predChar  );
					while( ! predChar.equals(",") && ! predChar.equals("(") && predChar.toUpperCase().equals(predChar) ){
						//substring in var:do nothing
						predCharIndex = pstructStr.indexOf(ttStr,predCharIndex+2) - 1;
						predChar      = ""+pstructStr.charAt(predCharIndex);
//						System.out.println("replaceVarInStruct predChar=" + predChar + "var=" + ttStr + " " + predCharIndex);
					}		
//  					System.out.println("	replaceVarInStruct FOUND " + pstructStr.substring(predCharIndex) + "var=" + ttStr + " " + newttStr);
					String ss  = pstructStr.substring(predCharIndex).replace( ttStr,newttStr );
//					System.out.println("replaceVarInStruct pstructStr 1 " + ss     );
					pstructStr = pstructStr.substring(0,predCharIndex)+ss;
// 					System.out.println("replaceVarInStruct pstructStr 2 " + pstructStr     );
				}
			}else if( argi instanceof Struct){
//	   			System.out.println("replaceVarInStruct for " + tt +" varin=" +  varin + " val=" + valin);	
				newttStr = replaceVarInStruct(ttStr,varin,valin);
//				ttStr = ttStr.replace("'", "\"");  //?????? APRIL 2017
// 				System.out.println("replaceVarInStruct recursive pstructStr=" + pstructStr);	
// 				System.out.println("replaceVarInStruct recursive ttStr=" + ttStr  );	
// 				System.out.println("replaceVarInStruct recursive newttStr=" + newttStr );	
				String ss = ttStr.replace( ttStr , newttStr );
//				ss = ss.replace("'", "\""); //?????? APRIL 2017
//				System.out.println("replaceVarInStruct recursive ss=" + ss   );	
//				System.out.println("replaceVarInStruct recursive ttStr="+ttStr );	
//				System.out.println("replaceVarInStruct recursive index ="+pstructStr.indexOf(ttStr) );	
				pstructStr = pstructStr.replace(ttStr, ss);
				//System.out.println("	replaceVarInStruct recursive ss=" + ss   );		
//				System.out.println("	replaceVarInStruct recursive pstructStr="+pstructStr );		
			}
		}
//		if( actorop ) return"actorOpDone("+pstructStr+")";
//		else 
//			System.out.println("replaceVarInStruct return pstructStr= " + pstructStr     );
//			pstructStr = pstructStr.replace("?", "'");//May 2017
			return pstructStr;
	}  
	
	public static String substituteVars( Hashtable<String, String> guardVars, String parg){
//  	System.out.println("	%%% QActorUtils substituteVars in " + parg + " guardVars=" + guardVars);
 	if( parg.length()== 0 ) return parg;
	if( guardVars!= null ){ 
		java.util.Iterator<String> it = guardVars.keySet().iterator();
	    while( it.hasNext() ){
	    	String varin = it.next() ; 
	    	String valin = guardVars.get( varin );
//			System.out. println("		varin=" +  varin + " valin=" + valin + " parg=" + parg) ;
			if( varin.equals( parg ) ) parg = parg.replace( varin,valin );
			else{
		 		try{
		 			Term pargt = Term.createTerm(parg);
			 		if( pargt instanceof Struct ){
			 			parg = replaceVarInStruct(""+pargt,varin,valin);  
			 			//we transfer the prolog rep of struct to avoid the ' "" problem
			 		}						
		 		}catch(Exception e){
		 			System.out.println("substituteVars ERROR " + e.getMessage() );
		 			return parg;
		 		}		 		
			}				  
		}
	}
//	System.out.println("substituteVars parg= " + parg );
	return parg;
	}
	
public static Term unifyMsgContent(Prolog pengine, String argStr, String msg, Hashtable<String, String> guardVars){
//	println("unifyMsgContent " + argStr + " " + msg);
		if( guardVars!=null ) msg = substituteVars(guardVars,msg);
//    println("unifyMsgContent " + msg);
		 Term arg = Term.createTerm( argStr );
	     Term msgt = Term.createTerm(msg);	 
		  boolean b = pengine.unify(arg,msgt);
		  if( ! b ){
		  	 System.out.println("ERROR: msg content does not match msg-specification");
		  	 return null ;
		  }
		  else{
//			println("ASNWER: " + arg.toString());
			return arg; //unified with msg-specification 
		  } 
}




//public static String updateVars(
//	Prolog pengine, Hashtable<String, String> guardVars, Hashtable<String,String> myguardVars, 
//	Term tmsgdef, Term tmsguser, Term tmsg, String swithvar) throws Exception{
//		Hashtable<String,String> htss = new Hashtable<String,String>();
//		//1) Check msg templates
//// 	println("%%% updateVars tmsguser=" + tmsguser + " tmsg=" + tmsg + " tmsgdef=" + tmsgdef);
//	SolveInfo sol = pengine.solve( tmsgdef + "  = "+ tmsguser +".");
//		if( sol.isSuccess()){
//			memoVars(sol,htss);  				
//		}else new Exception("msg template do not match");
//	//2) Check msg payload
//	sol = pengine.solve( tmsguser + "  = "+ tmsg +".");
//	if( sol.isSuccess())  memoVars(sol,htss); else{
////	    	println("%%% no match between tmsguser=" + tmsguser + " and tmsg=" + tmsg);
//		return null; //the msg payload does not macth
//	}
////  	println("%%% updateVars/5 (1) swithvar=" + swithvar );
//	if( myguardVars != null ) swithvar = substituteVars(myguardVars,swithvar);
//// 	println("%%% updateVars/5 (2) " + swithvar );
//	swithvar = substituteVars(htss,swithvar);
////   	println("%%% updateVars/5 (3) " + swithvar + " " + myguardVars);
//  	if( myguardVars == null ){
//  		//Copy variables in guardVars, otherwise guardVars.get(..) does not work
////  		println("%%% updateVars/5 (4) myguardVars null"   );
//  		guardVars = new Hashtable<String,String>();
//  		Enumeration<String> es = htss.keys();
//  		while(es.hasMoreElements()){
//  			String key=es.nextElement();
////  			println("%%% updateVars/5 (4) " + key + "  " + htss.get(key) );
//  			guardVars.put(key, htss.get(key));
//  		}
//  	}
//	return swithvar;
//}

public static void memoVars(SolveInfo sol, Hashtable<String,String> htss) throws Exception{
	if( ! sol.isSuccess()) return;
	ListIterator<Var> bvit= sol.getBindingVars().listIterator();
	while( bvit.hasNext() ){
		Var v = bvit.next();
		String varName = v.getName();
		String val = v.getTerm().toString(); 
//			println("%%% memoVars var " + varName + " val=" + val );
			htss.put(varName, val);
	}	    	
}

public static boolean loadTheoryFromFile(Prolog pengine, String filePath){
	try{
		Theory theory = new Theory( new FileInputStream(filePath) );
  		pengine.addTheory(theory);
 	  	System.out.println( " loadTheoryFromFile done " + filePath  );
 	  	return true;
	}catch( Exception e){
		System.out.println(" loadTheoryFromFile WARNING: "  + e.getMessage() );
		return false;
	}		
	
}

public static boolean solveGoal(Prolog pengine, String goal){
	SolveInfo sol;
	try {
//		System.out.println(" %%% QActorUtils  solveGoal "  + goal + " dir= "  + pengine.getCurrentDirectory() );				
//  		System.out.println(" %%% QActorUtils  solveGoal goal: "  + goal + " pengine=" + pengine);
		sol = pengine.solve(goal+"."); 
		if( sol != null ) pengine.solve("setPrologResult(SOL).".replace("SOL", sol.getSolution().toString() ));
		else pengine.solve("setPrologResult(failure)." );
//		System.out.println(" %%% QActorUtils  solveGoal sol: "  + sol );
//		System.out.println(" %%% QActorUtils  solveGoal dir= "  + pengine.getCurrentDirectory() );				
		return sol.isSuccess();
	} catch (Exception e) {
		System.out.println(" %%% QActorUtils  solveGoal " + goal + " WARNING: "  + e.getMessage() );
		return false;
	}	
}
public static SolveInfo solveGoal(String goal,Prolog pengine){
	SolveInfo sol = null;
	try {
//		System.out.println(" %%% QActorUtils  solveGoal SolveInfo "  + goal + " dir= "  + pengine.getCurrentDirectory() );				
		sol = pengine.solve(goal+".");
		/*
		 * The following code does not work if ...
		 */
		if( sol != null && sol.isSuccess()){ 
//			System.out.println(" %%% QActorUtils  solveGoal GOING TO setPrologResult  "    );	
			pengine.solve("setPrologResult(SOL).".replace("SOL", sol.getSolution().toString() ));
		}else pengine.solve("setPrologResult(failure)." );
//		System.out.println(" %%% QActorUtils  solveGoal SolveInfo dir= "  + pengine.getCurrentDirectory() );				
 	} catch (Exception e) {
		System.out.println(" %%% QActorUtils  solveGoal sol WARNING: "  + e.getMessage() );
 	}	
	return sol;
}
public static AsynchActionResult solveGoal(  QActor actor,  QActorContext ctx, Prolog pengine, String goal, String alarms, IOutputEnvView outEnvView,
		int maxduration) throws Exception{
	return solveGoal(actor, ctx,pengine,goal,new String[]{alarms},outEnvView,maxduration );
}

public static AsynchActionResult solveGoal(  QActor actor,  QActorContext ctx, Prolog pengine, String goal, String[] alarms, IOutputEnvView outEnvView,
		int maxduration) throws Exception{
	String name = getNewName("solve_");
	String terminationEvId = getNewName(locEvPrefix+"endSolve_");
	
	IActorAction action = new ActionSolveTimed(
			name, actor, ctx, pengine, goal, terminationEvId, alarms, outEnvView, maxduration);		//synch - reactive
// 	System.out.println("QActorUtils ActionSolveTimed= " +action);
	action.execSynch();
 	String result = action.getResultRep();
// 	System.out.println("QActorUtils ActionSolveTimed result= " +result);
	
	AsynchActionResult aar = new AsynchActionResult(
 		action,action.getExecTime(),action.isSuspended(),true,result,action.getInterruptEvent());
	return aar;
}

	public static void emitEventAfterTime(QActor actor, String emitter, String evId, String evContent, int time){
		emitEventAfterTime( actor.getQActorContext(),emitter,  evId,  evContent,  time );
    }
	public static void emitEventAfterTime(QActorContext ctx, String emitter, String evId, String evContent, int time){
  		new Thread(){
  			public void run(){
  				try {
 					System.out.println("			||| " + emitter + " WILL EMIT event:" + evId + " after " +time );
					Thread.sleep(time);
 					raiseEvent(ctx,emitter, evId, evContent);
  					System.out.println("			||| " + emitter + " EMITTED event:" + evId  );
				} catch (Exception e) {
 					e.printStackTrace();
				}
  			}
  		}.start();
  	}


//public static void solveGoal(String name, Prolog pengine, String goal,
//		String terminationEvId, String answerEvId, String[] alarms, IOutputEnvView outEnvView,
//		int maxduration) throws Exception{
//	IActorAction action = new ActionSolveTimed(
//			"solveReactive", pengine, "fibo(28,V)",
//			terminationEvId, "", alarms, outEnvView, maxduration);		
//	QActor actionTerminationHandler = 
//			QActorUtils.createActionTerminationEvHandler(actorContext, outEnvView, terminationEvId);
//	IEventItem evActionTerminate = actionTerminationHandler.waitForCurentEvent();
//	
//}
 
		
public static void registerActorInProlog18(Prolog pengine, QActor a) throws Exception{  
//	println("QActorUtils Regsitering in TuProlog ... " + this.getName()  ); 
	Library lib = pengine.getLibrary("alice.tuprolog.lib.OOLibrary");
//	println("QActorUtils Registering in TuProlog18 ... " + lib ); 
	Struct internalName = new Struct( "qatu"+a.getName() );
	((alice.tuprolog.lib.OOLibrary)lib).register( internalName, a); 
//	System.out.println("QActorUtils Registered in TuProlog18 " + internalName ); 
}

public static String[] createArray(String events) {
	if (events == null)
		return new String[] {};
	if (events.length() == 0)
		return new String[0];
	if (events.contains("[")) { // it happens for user input via GUI or Web
		events = events.replace("[", "").replace("]", "");
	}
	// System.out.println("  createEventArray " + events );
	Vector<String> vs = new Vector<String>();
	if (events.contains(",")) {
		StringTokenizer st = new StringTokenizer(events, ",");
		while (st.hasMoreTokens()) {
			String t = st.nextToken();
			t.replaceAll("'", "");
//			System.out.println(" createEventArray adding token " + t );
			vs.add(t);
		}
	} else{ vs.add(events); }
	String[] vsa = new String[vs.size()];
	vs.toArray(vsa);
 //	System.out.println(" createEventArray vsa " + vsa.length );
	return vsa;
}

	public static List<Var> evalTheGuard( QActor actor, String guard, String guardType ) throws Exception{
//		System.out.println("		### QActorPlanned evalTheGuard=" + guard + " of type=" + guardType );	
		
		/*
		 * If the pengine is engaged in solving a goal: the caller Thread waits
		 */
		SolveInfo sol = actor.getPrologEngine().solve("evalGuard("+guard+").");
// 		System.out.println("### QActorPlanned executePlanAction evalTheGuard " + guard + " sol=" + sol);	
		if( sol.isSuccess() ){
			if( guardType.equals(guardVolatile)){
				actor.removeRule(guard);
			}
			return sol.getBindingVars();
		}
		else return null; 
	}
	
	public static  Hashtable<String,String> evalTheGuard( QActor actor,  String guard ) throws Exception{
		Prolog pengine = actor.getPrologEngine();
		Hashtable<String,String> htss = new Hashtable<String,String>();
// 	   			System.out.println("evalTheGuard " + guard    );
	 	  		if( guard.equals("true") ) return htss;
	 			boolean toremove = true;
	 			boolean hasNot   = false;
	 			guard = guard.trim();
	 			if( guard.startsWith("not")){
	 				hasNot = true;
	 				guard = guard.substring(3).trim();
//	  				System.out.println("evalGuard=" + guard    );
	 			}
	 			if( guard.startsWith("??")){
	 				guard = guard.substring(2).trim();
	 				toremove = true;
	 			}else if( guard.startsWith("!?")){
	 				guard = guard.substring(2).trim();
	 				toremove = false; 				
				}
//	 			else if( guard.startsWith("!!")){
//					guard = guard.substring(2);
//					AsynchActionResult res = execDummyActionForGuardWait( guard );
//					//We should insert the event args in the hss
//					return htss;	//
//	 			}
	 			else{
//	 				System.out.println("evalGuard guard prefix wrong"    );
	 				throw new Exception("guard prefix wrong");
	 			}
//	 			System.out.println("evalGuard=" + guard    );
	  			SolveInfo sol = pengine.solve(  "evalGuard("+guard +").");
//	     		System.out.println("evalTheGuard " + guard + " sol=" + sol    );
	 			if( sol.isSuccess() ){
//  	     			System.out.println("evalTheGuard " + guard + " sol=" + sol.getSolution()  + " toremove=" + toremove );
					if(toremove){
			  			//The guard is removed, once evaluated true  
						actor.removeRule( guard ); //we remove the solution sol.getSolution().toString()
	 				} 
					if( hasNot ) return null;
	  				ListIterator<Var> bvit= sol.getBindingVars().listIterator();
					while( bvit.hasNext() ){
						Var v = bvit.next();
						String varName = v.getName();
//						Term t =  v.getTerm();
//						System.out.println("evalTheGuard  " + varName + " t=" + t );
	 					String val = v.getTerm().toString(); 	//Any var is converted in String
// 	 					System.out.println("evalTheGuard  " + varName + " val=" + val );
	 					htss.put(varName, val);
					}
	 				return htss;
				}
				//guard not found
//	  			System.out.println("evalGuard " + guard + " failure with hasNot=" + hasNot  + " htss=" + htss  );
				if( hasNot ) return htss;
				else return null;
	  	}	
	
	 	public static boolean checkExceptionMsg( String err ){
 	 		return err.contains("Task java.util.concurrent.ScheduledThreadPoolExecutor");
 	 	}

		public static void consultFromFile( Prolog pengine, String fName){
			String inputS="";
			try {				 
				System.out.println(" %%% QActorUtils  consultFromFile " + fName + " pengine=" + pengine );
	 			InputStream fs          = new java.io.FileInputStream(fName);
				InputStreamReader inpsr = new InputStreamReader(fs);
				BufferedReader br       = new BufferedReader(inpsr);
			 
				Iterator<String> lsit   = br.lines().iterator();
//				System.out.println("QActorUtils  consultFromFile " + lsit.hasNext() );
				while(lsit.hasNext()){
					inputS = lsit.next();
// 	  				System.out.println(" %%% QActorUtils inputS " + inputS);
	  				if( inputS == null ) break;
	  				if( inputS.endsWith(".") ) inputS= inputS.substring(0, inputS.length()-1);
					String g =  "addRule(X)".replace("X", inputS);
// 					System.out.println(" %%% QActorUtils   g= " + g);
					SolveInfo sol = pengine.solve( g +"." );  
				}
				br.close();
	 		} catch (Exception e) {
				System.out.println(" %%% QActorUtils   consultFromFile ERROR " + e.getMessage());
			}			
		}

		public static String readFile(String fName){
			String inputS="";
			try {
				System.out.println("QActor  readFile " + fName);
				InputStream fs          = new java.io.FileInputStream(fName);
				InputStreamReader inpsr = new InputStreamReader(fs);
				BufferedReader br       = new BufferedReader(inpsr);
				Iterator<String> lsit   = br.lines().iterator();
//				outS="\"";
				while(lsit.hasNext()){
					inputS = inputS + lsit.next();
					if( lsit.hasNext() ) inputS = inputS +"\n";
				}
				br.close();
//				outS= outS + "\"";
			} catch (Exception e) {
				System.out.println("QActor  ERROR " + e.getMessage());
//	 			e.printStackTrace();
			}
			return inputS ;
		}

		public static void writeInFile(String fName, String content){
			try {
				System.out.println("QActor  writeInFile " + fName + " : " + content);
				FileOutputStream fsout = new FileOutputStream( new File(fName) );
				fsout.write(content.getBytes());
				fsout.close();
			} catch (Exception e) {
				System.out.println("QActor  ERROR " + e.getMessage());
	 		}
			
		}
		public static void writeListInFile(String fName, String content ){
	 		Struct t = (Struct) Term.createTerm(content);
	 		System.out.println("QActor  writeListInFile obj=" + t.getClass().getName() );
			String outS ="";
			if( t.isList() ){
				while( ! t.isEmptyList() ){
					outS = outS + t.listHead()+"\n";
					t = t.listTail();
				}
//	  			println( outS );
				writeInFile(fName,outS);
			}		
		}
/*
 * MQTT (2017)
 */
		
//	 	public static IConnInteraction connectMQTTAsReceiver( String clientid, String brokerAddr, String topic ) throws Exception{
//	 		System.out.println("			%%%  connectMQTTAsReceiver to:" + brokerAddr + " topic=" + topic);
// 			MqttSupport mqttsupport = new MqttSupport();
// 			IConnInteraction connSend = mqttsupport.connectAsReceiver(clientid, brokerAddr, topic);	 			 		
// 			System.out.println("			%%%  connectMQTTAsReceiver done "  );
// 			return connSend;
//	 	}
//	 	public static IConnInteraction connectMQTTAsSender( String clientid, String brokerAddr, String topic ) throws Exception{
//	 		System.out.println("			%%%  connectMQTTAsSender to:" + brokerAddr + " topic=" + topic);
// 			MqttSupport mqttsupport = new MqttSupport();
// 			IConnInteraction connSend = mqttsupport.connectAsSender(clientid, brokerAddr, topic);	 			 		
// 			System.out.println("			%%%  connectMQTTAsSender done "  );
// 			return connSend;
//	 	}
//		
//	 	public static String receiveMQTT(IConnInteraction conn) throws Exception{
//	 		return conn.receiveALine();
//	 	}
//	 	public static void sendMQTT(IConnInteraction conn, String msg) throws Exception{
//	 		conn.sendALine(msg);
//	 	}
		
}
