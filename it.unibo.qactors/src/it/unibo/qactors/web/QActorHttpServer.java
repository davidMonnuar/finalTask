package it.unibo.qactors.web;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorMessage;
import it.unibo.qactors.QActorUtils;
import it.unibo.qactors.akka.QActor;
import it.unibo.qactors.platform.EventHandlerComponent;
import it.unibo.system.SituatedSysKb;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import fi.iki.elonen.IWebSocketFactory;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.WebSocket;
import fi.iki.elonen.WebSocketFrame;
import fi.iki.elonen.WebSocketFrame.CloseCode;
import fi.iki.elonen.WebSocketResponseHandler;

public class QActorHttpServer extends NanoHTTPD{
	public enum Mode{
		NAIVE,MESSAGE,EVENT 
	}

protected IOutputEnvView outEnvView;
protected WebSocketResponseHandler responseHandler;
protected IWebSocketFactory webSocketFactory;
//protected IContactEventPlatform platform ;
protected String dirPath;
protected Mode mode;
protected QActor actor;
protected QActorContext ctx;
	public QActorHttpServer(QActorContext ctx, IOutputEnvView outEnvView, String dirPath, int port) {
		super(port);
		this.ctx     = ctx;
		this.dirPath = dirPath;
		NanoHTTPD.SOCKET_READ_TIMEOUT=60*1000*10; //10 min;		 
		webSocketFactory = new IWebSocketFactory() {		
			@Override
			public WebSocket openWebSocket(IHTTPSession handshake) {
					return new Ws(handshake);
			}
		};
		this.outEnvView = outEnvView;
		responseHandler = new WebSocketResponseHandler(webSocketFactory);
  		outEnvView.addOutput("	*** QActorHttpServer starts dirPath=" + dirPath);
	}
	
	protected String uri = null;
	protected NanoHTTPD.Response response;
		
	@Override
	public Response serve(IHTTPSession session) {
//  		outEnvView.addOutput("		=== QActorHttpServer serve: "  );
		 
		response = responseHandler.serve(session);
		if(response == null){
			uri = session.getUri();
			return respondToServe();
		}
		return response;
	}
	
	protected Response respondToServe(){
   		outEnvView.addOutput("		=== QActorHttpServer serve: " + uri + " dirPath=" + dirPath);
	    try {
//			platform = ContactEventPlatform.getPlatform();
		    FileInputStream fis = null;
				if (uri.equals("/")){
			    fis = new FileInputStream(dirPath+"/QActorWebUI.html");
			    //new NanoHTTPD.Response(null, cmd, fis, count);
// 			    return newChunkedResponse(NanoHTTPD.Response.Status.OK, "text/html", fis);
 			    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/html", fis );
			    
			}else if (uri.equals("/QActorWebUI.js")){
		    	fis = new FileInputStream(dirPath+"/QActorWebUI.js");
//		    	return newChunkedResponse(NanoHTTPD.Response.Status.OK, "text/javascript", fis);
 		    	return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/javascript", fis);
	 		}else if (uri.equals("/QActorWebUI.css")){
		    	fis = new FileInputStream(dirPath+"/QActorWebUI.css");
//		    	return newChunkedResponse(NanoHTTPD.Response.Status.OK, "text/css", fis);
 		    	return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/css", fis);			
	 		}else
//	 			return newFixedLengthResponse("<html>NOT UDERSTAND</html>");
 	 			return new NanoHTTPD.Response("<html>NOT UDERSTAND</html>"); 
	    } catch (Exception e) {
//	    	outEnvView.addOutput("QActorHttpServe dirPath=" + dirPath + " ERROR " + e.getMessage() );
//			return new NanoHTTPD.Response("<html><body style='color:red;font-family: Consolas;'>hello, i am runing....</body></html>"); 
	    	return respondBuiltInToServe();
	    }		
	}
	
	protected Response respondBuiltInToServe(){
	    try {
	 		 InputStream fis = null;
	 		 String dirPath = "./srcWeb";
	    	 outEnvView.addOutput("QActorHttpServer respondBuiltInToServe dirPath=" + dirPath  );
				if (uri.equals("/")){
			    fis = //new FileInputStream(dirPath+"/QActorWebUI.html");
			      QActorHttpServer.class.getResourceAsStream("QActorWebUI.html") ;
//			    return newChunkedResponse(NanoHTTPD.Response.Status.OK, "text/html", fis);
 			    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/html", fis);
			}else if (uri.equals("/QActorWebUI.js")){
		    	fis = //new FileInputStream(dirPath+"/QActorWebUI.js");
		    			QActorHttpServer.class.getResourceAsStream("QActorWebUI.js") ;
//			    return newChunkedResponse(NanoHTTPD.Response.Status.OK, "text/javascript", fis);
 		    	return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/javascript", fis);
	 		}else if (uri.equals("/QActorWebUI.css")){
		    	fis = //new FileInputStream(dirPath+"/QActorWebUI.css");
		    			QActorHttpServer.class.getResourceAsStream("QActorWebUI.css") ;
//			    return newChunkedResponse(NanoHTTPD.Response.Status.OK, "text/css", fis);
 		    	return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/css", fis);			
	 		}else
//	 			return newFixedLengthResponse("<html>NOT UDERSTAND</html>");
 	 			return new NanoHTTPD.Response("<html>NOT UDERSTAND</html>"); 
	    } catch (Exception e) {
	    	outEnvView.addOutput("QActorHttpServer dirPath=" + dirPath + " ERROR " + e.getMessage() );
	    	String answer ="<html><body style='color:red;font-family: Consolas;'>hello, i am runing....</body></html>";
//	    	return newFixedLengthResponse(answer);
	    	return new NanoHTTPD.Response(answer);
 	    }
		
	}

	/*
	 * -------------------------------------------------------------
	 * Send a message to the actor sender specified in the msg
	 * -------------------------------------------------------------
	 */
	protected void handleMsg(String msg) throws Exception{
		//msg=msg( MSGID, MSGTYPE, SENDER, RECEIVER, message, 0 )
		QActorMessage mqa  = new QActorMessage(msg);
//		ActorContext ctx = null;//QActorUtils.getActorContext() ;//((ContactEventPlatform) platform).getActorContext();
		actor = QActorUtils.getQActor(mqa.msgReceiver());
		//String msgID, String destActorId, String msgType, String msg
		if(actor != null){
 			outEnvView.addOutput("		=== QActorHttpServer executeMsg " + mqa.getDefaultRep()  );
			actor.sendMsg(mqa.msgId(), mqa.msgReceiver(), mqa.msgType(), mqa.msgContent());
		}
	}
	protected void handleInputCommand(String cmd) throws Exception{
		String eventMsg = inputToEventMsg(cmd);
		outEnvView.addOutput("		=== QActorHttpServer executeInputCommand  emits " + GuiUiKb.inputCmd +":"+eventMsg  );
//		platform.raiseEvent("wsock", GuiUiKb.inputCmd, eventMsg);
		QActorUtils.raiseEvent(ctx,"wsock", GuiUiKb.inputCmd, eventMsg); //TO CHECK
		
	}
	protected void handleCmd(String cmd) throws Exception{
		Struct ct = (Struct) Term.createTerm(cmd);
		if( ct.getName().toString().startsWith("e")) {
// 			outEnvView.addOutput("		=== QActorHttpServer EVENT=" + cmd  );
 			Struct ast  = (Struct) Term.createTerm(cmd);
 			String alarm = ast.getArg(0).toString();
 			if( alarm.contains("cmd") &&  alarm.contains("start")){
 				outEnvView.addOutput("		=== QActorHttpServer raise event cmd(start) "    );
 				QActorUtils.raiseEvent(ctx,"wsock", "cmd", "cmd(start)");
 			}else if( alarm.contains("cmd") &&  alarm.contains("stop") ){
 				outEnvView.addOutput("		=== QActorHttpServer raise event cmd(stop) "    );
 				QActorUtils.raiseEvent(ctx,"wsock", "cmd", "cmd(stop)");
 			}else if( alarm.contains("obstacle")){
 	 			outEnvView.addOutput("		=== QActorHttpServer raise event obstacle(near)"    );
 				QActorUtils.raiseEvent(ctx,"wsock", "obstacle", "obstacle(near)");
 			}else{
 	 			outEnvView.addOutput("		=== QActorHttpServer raise event " + alarm   ); 				
 				QActorUtils.raiseEvent(ctx,"wsock", "alarm", alarm);  
 			}
		}else{
			String eventMsg = "usercmd(robotgui("+ct+"))";
			outEnvView.addOutput("		=== QActorHttpServer executeEvent " + GuiUiKb.terminalCmd +":"+eventMsg  );
//			platform.raiseEvent("wsock", GuiUiKb.terminalCmd, eventMsg);
			QActorUtils.raiseEvent(ctx,"wsock", GuiUiKb.terminalCmd, eventMsg);//TO CHECK
		}
	}

	/*
	 * This operations can be redefined by specialized classes
	 */
	protected void handleInputMsg(String msg) throws Exception{
   		outEnvView.addOutput("		=== QActorHttpServer handleUserCmd "+cmd);
			if( msg.startsWith("m-")){
 				handleMsg(msg.split("-")[1]);
 			}else if( msg.startsWith("i-")){ 
				handleInputCommand(msg.split("-")[1]);
 			}else{
				handleCmd(msg);
			}
 	}
	/*
	 * From input
	 */
	public static String inputToEventMsg(String input){
		String eventMsg = null;
		String moveToDo = GuiUiKb.buildCorrectPrologString(input);
//  			System.out.println("		=== QActorHttpServer inputToEventMsg moveToDo="+moveToDo);
 			try{
  				Term mt = Term.createTerm(moveToDo);
  				Struct ms;
 				if( mt instanceof Struct){
					ms = (Struct) mt;
// 				 	System.out.println("		=== QActorHttpServer inputToEventMsg ms=" + ms);
					if( ms.getName().equals(",")){
	 				 	Term guard = getGuard( ms );
						/* The term must have the form : 
 						 *  [ guard ] , goal 
						 *  [ guard ] , goal, duration , ... 
						 */
//	 				 	System.out.println("		=== QActorHttpServer inputToEventMsg guard=" + guard);
	 				 	if( guard == null ){
	 				 		//moveToDo="print('ERROR: missing guard')";
	 				 		moveToDo= "[true],"+moveToDo;
	 				 		mt = Term.createTerm(moveToDo);
	 				 		ms = (Struct) mt;
	 				 		guard = getGuard( ms );
	 				 	}	
	 				 	/*
	 				 	 * Syntax checking is done at Prolog level
	 				 	 */
 						moveToDo="do("+moveToDo+")";
 						eventMsg = "usercmd(executeInput("+moveToDo+"))";
					}else{		
						/* The term  has the form : 
						 *  f(...)
		 				 */ 				
						//System.out.println("		=== QActorHttpServer inputToEventMsg GOAL="+moveToDo + " " + (mt instanceof Struct) );
						//moveToDo="do("+moveToDo+")";
						eventMsg = "usercmd(executeInput("+moveToDo+"))";	
 						return eventMsg;
					}			
 			}else{
				/* The term  has the form : 
				 *  a
 				 */ 		
 				//System.out.println("		=== QActorHttpServer inputToEventMsg ATOM="+moveToDo);
 				eventMsg = "usercmd(executeInput("+moveToDo+"))";
 				return eventMsg;
			}
			}catch( Exception e1){
				/* The input  is NOT a Prolog term
  				 */ 				
				System.out.println("		=== QActorHttpServer  inputToEventMsg ERROR no term ="+moveToDo);
				//Not a Prolog term => Surround with ''
				try{
					moveToDo = moveToDo.replace("'", "\"");
					moveToDo = "'" + moveToDo + "'";
					eventMsg = "usercmd(executeInput("+moveToDo+"))";
					return eventMsg;
				}catch( Exception e2){
					//System.out.println("		=== QActorHttpServer inputToEventMsg ERROR ="+e2.getMessage());
					throw e2;
				}
			}
			return eventMsg;
	}
	
	protected static Term getGuard(Struct ms){
	//ms = a,b...
		Term t1 = ms.getArg(0);
		if( t1.isList() ){
//			Struct t1s = (Struct)t1;
//			Term g = t1s.getArg(0);
			return t1;
		}else return null;
	}
	
	protected static Term getDuration(Term t1){
		Term td = t1;
		if( t1 instanceof Struct && ((Struct)t1).getName().equals(",")){
			td = ((Struct)t1).getArg(0);
		}
 		if( td instanceof alice.tuprolog.Number ){
			return td;
		}else return null;		
	}
	protected static Term getE(Term t1){
		Term td = t1;
		if( t1 instanceof Struct && ((Struct)t1).getName().equals(",")){
			td = ((Struct)t1).getArg(0);
		}
 		if( td instanceof alice.tuprolog.Number ){
			return td;
		}else return null;		
	}
	
/*
 * 	
 */
	protected WebSocket myws = null;
	protected int count = 0;
	protected String cmd = null;
	
	public class Ws extends WebSocket{
		public Ws(IHTTPSession handshakeRequest) {
			super(handshakeRequest);
  			outEnvView.addOutput("		=== QActorHttpServer connected ");
  			myws = this; 			 
		}
		@Override
		protected void onPong(WebSocketFrame pongFrame) {
			outEnvView.addOutput("QActorHttpServer Ws  user pong.......");
		}
		@Override
		protected void onMessage(WebSocketFrame messageFrame) {
			try {
				cmd = messageFrame.getTextPayload();
				String eventMsg="noevent";
				try{
					handleInputMsg(cmd);
 				}catch(Exception e ){
 					//No platform available
 					outEnvView.addOutput("QActorHttpServer ERROR (Perhaps platform null) " + e.getMessage());
// 					platform = ContactEventPlatform.getPlatform();
 					handleInputMsg(cmd);
				}
// 				send("done " + cmd);				
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		@Override
		protected void onClose(CloseCode code, String reason,
				boolean initiatedByRemote) {
			outEnvView.addOutput("QActorHttpServer Ws  user disconnected.....");			
		}
		@Override
		protected void onException(IOException e) {
			outEnvView.addOutput("QActorHttpServer Ws  ERROR "+e.getMessage());
		}		
	}
	
/*
* 	
*/
		public class EvhQActorHttpServer extends EventHandlerComponent{
			protected int ccc = 0;
			
			public EvhQActorHttpServer(String name, QActorContext myctx, String eventId,
					IOutputEnvView view) throws Exception {
				super(name, myctx, new String[]{eventId}, view);
				println("EvhCb CREATED name=" + getName()  );
			}
			
			public String getCmd(){
				return cmd;
			}
			public WebSocket getWs(){
				return myws;
			}
 
			@Override
			public void doJob() throws Exception {
				IEventItem event = this.currentEvent; //getEventItem();
				if( event == null ){
					println("EvhCb doJob no event "  );
					return;
				}
				String msg = event.getEventId() + "|" + event.getMsg() + " from " + event.getSubj()   ;
				showMsg( msg );		
// 				println("EvhQActorHttpServer logo=" + cmd );
// 				myws.send("count=" + count++); 
 				
			}
			
		}
 	

/*
 * For fast demo / debug	
 */
	
	public  static void main(String[] args)  {
		try {
			IOutputEnvView outEnvView = SituatedSysKb.standardOutEnvView;
			/*
			 * Create a Context to give a platform to QActorHttpServer
			 */
 			QActorContext ctx = QActorContext.initQActorSystem(
					"ctxwebtest", "./src/it/unibo/qactors/web/sysWebTestKb.pl", 
					"./src/it/unibo/qactors/web/sysRules.pl",outEnvView,".");			
	 /*
	 * Create the server (done by the context)
	 */
//			QActorHttpServer server = new QActorHttpServer(ctx,outEnvView,".",8080);
//			server.start();
			Thread.sleep(60000);
		} catch (Exception e) {
 				e.printStackTrace();
		}
 	}//main
}
