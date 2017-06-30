package it.unibo.qactors.mqtt;
import it.unibo.qactors.akka.QActor;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;

public class MqttUtils implements MqttCallback{
private static MqttUtils myself = null;	
	protected String clientid = null;
	protected String eventId = "mqtt";
	protected String eventMsg = "";
	protected  QActor actor = null;
	protected  MqttClient client = null;
	
	public static MqttUtils getMqttSupport( ){
		if( myself == null )  myself = new MqttUtils();
		return myself  ;
	}
	public MqttUtils(){
		try {
			myself   = this;
			println("	%%% MqttUtils created "+ myself );
		} catch (Exception e) {
			println("	%%% MqttUtils WARNING: "+ e.getMessage() );
		} 
	}
 	public void connect(QActor actor, String clientid, String brokerAddr, String topic ) {
 		try{
			this.actor = actor;
			client = new MqttClient(brokerAddr, clientid);
//			println("	%%% MqttUtils connect/4 "+ clientid + " " + brokerAddr + " " + topic + " " + client);
			MqttConnectOptions options = new MqttConnectOptions();
			options.setKeepAliveInterval(480);
	//		options.setWill(topic, "crashed".getBytes(), 2, true);
			options.setWill("unibo/clienterrors", "crashed".getBytes(), 2, true);
			client.connect(options);		
 		}catch(Exception e){
 			actor.println("MqttUtils connect ERROR " + e.getMessage());
 		}
	}
	public void disconnect( )  {
		try{
			println("	%%% MqttUtils disconnect "+ client );
			if( client != null ) client.disconnect();
 		}catch(Exception e){
 			actor.println("MqttUtils disconnect ERROR " + e.getMessage());
 		}
	}	

	public void subscribe(QActor actor, String  clientid, String brokerAddr, String topic) throws Exception {
		try{
			this.actor = actor;
			client.setCallback(this);
 			client.subscribe(topic);  
 		}catch(Exception e){
				println("	%%% MqttUtils subscribe error "+  e.getMessage() );
				eventMsg = "mqtt(" + eventId +", failure)";
				println("	%%% MqttUtils subscribe error "+  eventMsg );
 				if( actor != null ) actor.sendMsg("mqttmsg", actor.getName(), "dispatch", "error");
	 			throw e;
		}
	}
	@Override
	public   void connectionLost(Throwable cause) {
		println("	%%% MqttUtils connectionLost  = "+ cause.getMessage() );
	}
	@Override
	public   void deliveryComplete(IMqttDeliveryToken token) {
//		println("			%%% MqttUtils deliveryComplete token= "+ token );
	}
	/*
	 * sends a tpoic content of the form
	 * 	         msg( MSGID, MSGTYPE, SENDER, RECEIVER, CONTENT, SEQNUM )
	 */
	public void publish(QActor actor, String clientid, String brokerAddr, String topic, String msg, 
			int qos, boolean retain) throws MqttException{
 		MqttMessage message = new MqttMessage();
		message.setRetained(retain);
		if( qos == 0 || qos == 1 || qos == 2){//qos=0 fire and forget; qos=1 at least once(default);qos=2 exactly once
			message.setQos(0);
		}
		message.setPayload(msg.getBytes());
		try{
			client.publish(topic, message);
//			println("			%%% MqttUtils published "+ message + " on topic=" + topic);
		}catch(MqttException e){
			println("	%%% MqttUtils publish ERROR  "+ e );
		}
	}	
	/*
	 * receives a message of the form
	 * 	         msg( MSGID, MSGTYPE, SENDER, RECEIVER, CONTENT, SEQNUM )
	 * and stores it in the RECEIVER msgqueue (TODO)
	 */
	private String msgID       = null;
	private String msgType     = null;
	private String msgSender   = null;
	private String dest        = null;
	private String msgcontent  = null;

	@Override //MqttCallback
	public void messageArrived(String topic, MqttMessage msg)   {
		try {
  			println("	%%% MqttUtils messageArrived on "+ topic + ": "+msg.toString());
			Struct msgt      = (Struct) Term.createTerm(msg.toString());
	//		println("messageArrived msgt "+ msgt + " actor=" + actor.getName() ); 
			 msgID      = msgt.getArg(0).toString();
			 msgType    = msgt.getArg(1).toString();
			 msgSender  = msgt.getArg(2).toString();
			 dest       = msgt.getArg(3).toString();
			 msgcontent = msgt.getArg(4).toString();
 	  		 if( actor != null ) //send a msg to itself (named without _ctrl)
				actor.sendMsg( msgSender, msgID,  actor.getName().replace("_ctrl", ""),  msgType,  msgcontent);
		} catch (Exception e) {
//			println("messageArrived ERROR "+e.getMessage() );
		}
	}	
	
	public String getSender(){ return (msgSender!=null) ? msgSender.replace("_ctrl", "") : "notyet"; }
	
	public void println(String msg){
		if( actor != null ) actor.println(msg);
		else System.out.println(msg);
	}
	
	/*
	 * =================================================================
	 * TESTING	
	 * =================================================================
	 */
		
		public void test() throws Exception{
			println("	%%% MqttUtils test " );
			connect(null,"qapublisher_mqtt", "tcp://m2m.eclipse.org:1883", "unibo/mqtt/qa");
			publish(null,"qapublisher_mqtt","tcp://m2m.eclipse.org:1883", "unibo/mqtt/qa", "sensordata(aaa)", 1, true);
			
			connect(  null,"observer_mqtt", "tcp://m2m.eclipse.org:1883", "unibo/mqtt/qa");
			subscribe(null,"observer_mqtt", "tcp://m2m.eclipse.org:1883", "unibo/mqtt/qa");
			for(int i=1; i<=3; i++)
			publish(null,"qapublisher_mqtt","tcp://m2m.eclipse.org:1883", "unibo/mqtt/qa", "distance("+ i +")", 1, true);
			
		}
		
		public static void main(String[] args) throws Exception{
			new MqttUtils().test();
		}	
}
