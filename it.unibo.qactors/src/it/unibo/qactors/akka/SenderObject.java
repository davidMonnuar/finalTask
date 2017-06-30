/*
  * Provides a method to send messages to a remote context
  * (by creating a connection with that remote context)
 */
package it.unibo.qactors.akka;
import java.util.Hashtable;

import alice.tuprolog.Term;
import it.unibo.is.interfaces.IOutputView;
import it.unibo.is.interfaces.protocols.IConnInteraction;
import it.unibo.qactors.QActorContext;
import it.unibo.supports.FactoryProtocol;
import it.unibo.system.SituatedPlainObject;

public class SenderObject extends SituatedPlainObject{	
	protected IConnInteraction conn = null;
	protected String hostName;
	protected int port;
 	protected FactoryProtocol factoryP;
 	protected QActorContext ctx;
	protected String protocol;
	protected SenderObject myself;
	protected boolean tryingToConnect = true;
	
	protected  Hashtable<String,IConnInteraction> connectionTable;	
	
	public SenderObject(String name, QActorContext ctx, IOutputView outView,  
			String protocol, String hostName, int port ) {
		super(name, outView);
		this.ctx = ctx;
		this.hostName 	= hostName;
		this.port     	= port;
		this.protocol	= protocol;
		factoryP 		= new FactoryProtocol(outView, protocol, "fp");
		myself 			= this;
		
		connectionTable	= new Hashtable<String,IConnInteraction>();
		tryTheConnection();
		
    } 
	protected void tryTheConnection(){
		//Attempt to connect with the remote node
		new Thread(){
			public void run(){
				boolean res = false;
				while( ! res ){
					res = setConn( );
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
							e.printStackTrace();
					}
				}//while
				tryingToConnect = true;
				try {
					myself.updateWaiting();
				} catch (Exception e) {
 					e.printStackTrace();
				}
//				println("      %%%SenderObject TRYTHECONNECTION DONE  conn=" + conn );

			}
		}.start();		
	}
	protected synchronized void updateWaiting() throws Exception{
		conn = connectionTable.get(hostName+port);
//		println("      %%%SenderObject updateWaiting CONNECTED to " + hostName + ":" + port  + " conn=" + conn );
		this.notifyAll();
	}
	//May 2017 after Mqtt
	public synchronized void sendMsg(String senderName, String receiverName, 
			String msgId, String msgType, String m) throws Exception {
 		conn = connectionTable.get(hostName+port);
		sendTheMsg(  senderName, receiverName,  msgId, msgType, m) ;
//		while( conn == null ){
//// 			println("      %%%SenderObject " + this.getName() + " waits since NO CONNECTION to " + hostName + port  );
//			wait();
//		}
//		if( conn != null ){
//			sendTheMsg(  senderName, receiverName,  msgId, msgType, m) ;	
//		} else{
//			println("      %%%SenderObject NO CONNECTION to " + hostName + ":" + port   );
// 		}	
	}
 	public  void sendMsg(QActor sender, String receiverName, 
 			String msgId, String msgType, String m) throws Exception {
 		conn = connectionTable.get(hostName+port);
 		sendTheMsg(sender.getName(), receiverName, msgId,   msgType,   m);
//		while( conn == null ){
// 			println("      %%%SenderObject waits (QActor) since NO CONNECTION to " + hostName + port  );
//			wait();
//		}
//		if( conn != null ){
//			sendTheMsg(  sender, receiverName,  msgId, msgType, m) ;	
//		} else{
//			println("      %%%SenderObject NO CONNECTION to " + hostName + ":" + port   );
// 		}
   	}
	protected void sendTheMsg(QActor sender, String receiverName,  String msgId, String msgType, String m) throws Exception {
		sendTheMsg( sender.getName(), receiverName,   msgId,  msgType,  m);								
	}
	//May 2017 after Mqtt
	protected synchronized void sendTheMsg(String senderName, String receiverName,  
			String msgId, String msgType, String m) throws Exception {
		try {
			int msgNum = ctx.newMsgnum();
			//msg( MSGID, MSGTYPE SENDER, RECEIVER, CONTENT, SEQNUM )
			
   	 		String mout = "msg(" + msgId +","+ msgType + ","+ senderName +","+ receiverName +","+ m+","+ msgNum +")";
 //JUNE2017  	 	if( m.contains("value(24)")) println("      %%%SenderObject " + this.getName() + " sends " + mout + " TO " + hostName + ":" + port  + " conn=" + conn  );
 			//If the connection has been rest we block forever
			while( conn == null ){
//				println("      %%%SenderObject waits since NO CONNECTIONNNN to " + hostName + port   );
				if( ! tryingToConnect ) tryTheConnection();
				wait();
//				println("      %%%SenderObject RESUMES " + conn   );
			}
	 		conn.sendALine( mout );
//JUNE2017  			if( m.contains("value(24)"))  println("      %%%SenderObject " + this.getName() + " has sent " + mout   );
		} catch (Exception e) {
			println("      %%%SenderObject " + name + " send/5 ERROR " + e.getMessage()   );
			connectionTable.remove(hostName+port);
//			ctx.ctxTable.remove(name);
			conn = null;
  			throw e;
		}											
	}
	public synchronized void sendMsg(String mout) throws Exception {
		try {
			conn = connectionTable.get(hostName+port);			 
			//If the connection has been reset, we block forever
			while( conn == null ){
// 				println("      %%%SenderObject waits since NO CONNECTION to " + hostName + port   );
				if( ! tryingToConnect ) tryTheConnection();
				wait();
  			}			 
			if( conn != null ){
// 				println("      %%%SenderObject sendALine " + mout    );
				conn.sendALine( mout );	
			} else{
				println("      %%%SenderObject NO CONNECTION to " + hostName + ":" + port   );
  		}
		} catch (Exception e) {
//			println("      %%%SenderObject " + name + " send/1 ERROR " + e.getMessage()   );
			connectionTable.remove(hostName+port);
//			ctx.ctxTable.remove(name);
			conn = null;
 			throw e;
		}									
	}
	protected boolean setConn( ){
		try { 
//  			println("      %%%SenderObject " + name + " try conn " +  protocol + " " + hostName + " " + port);
			conn = factoryP.createClientProtocolSupport(hostName, port);
//   			println("      %%%SenderObject " + name + " SETTTTTT CONN TO " +   hostName + port + " conn=" + conn);
			//TEST
// 			conn.sendALine( ctx.getName() + " xxxxxxxxxxx connected to --->" + port );	
			//ENDTEST
			connectionTable.put(hostName+port, conn);
			return true;
		} catch (Exception e) {
			 println("      %%%SenderObject " + name + " setConn "  + e.getMessage() );
		}				
		return false;
 	}
	/*
	 * Called by ActorContext
	 */
	public String receiveWakeUpAnswer() throws Exception{
		String line = conn.receiveALine();
		println("      %%%SenderObject receiveWakeUpAnswer " + line );
		return line;
	}
}
