package it.unibo.connector;

import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import it.unibo.qactors.akka.QActor;

/**
 * 
 * @author Federico Ruggeri
 * 
 * Basic string parser that is compliant with QActors' environment.
 *
 */
public class DefaultConnectorParser implements IConnectorParser
{
	// these values match UnityPrologUtility's build functions identifiers
	protected static final String EVENT_IDENTIFIER = "event";
	protected static final String MESSAGE_IDENTIFIER = "msg";
	
	// necessary in order to attach to the Qactors' environment
	private QActor actor;
	
	public DefaultConnectorParser(QActor actor)
	{
		this.actor = actor;
	}

	/**
	 * Simple method that parses strings in prolog-like syntax.
	 * Moreover, it is compliant to the UnityActorSimulator's communication format for QActors.
	 * Thus, any incoming event or message request is detected and mapped into Qactors' environment. 
	 */
	@Override
	public <T> void parseData(T data) 
	{
		String dataStr = data.toString();
		actor.println("[DefaultConnectorParser] Parsing data : " +dataStr);
		Struct ts = (Struct) Term.createTerm( dataStr );
		actor.println("[DefaultConnectorParser] ts : " +ts);
		String evId      = ts.getArg(0).toString();
		String evContent = ts.getArg(4).toString();
		actor.emit(evId, evContent);
	}
	/*
	@Override
	public <T> void parseData(T data) 
	{
		actor.println("[DefaultConnectorParser] Parsing data : " + data.toString());
		
		String prologData = data.toString();
		
		if (prologData.startsWith(MESSAGE_IDENTIFIER))
		{
			String type = prologData.split(",")[1].trim();
			
			if (type.equalsIgnoreCase(EVENT_IDENTIFIER)) // event
			{
	
				String eventID = prologData.split(",")[0].split("\\(")[1].trim();
				String emitter = prologData.split(",")[3].trim();
				int emitterIndex = prologData.indexOf(emitter);
				int payloadStartIndex = emitterIndex + emitter.length() + 1; // ','
				String[] tokens = prologData.split(",");
				String seqNum = tokens[tokens.length - 1];
				int seqNumIndex = prologData.indexOf(seqNum);
				String payload = prologData.substring(payloadStartIndex, seqNumIndex - 1).trim();
				
				actor.println("[DefaultConnectorParser] Detected event! Emitting.." + payload );
				actor.emit(eventID, payload);
			}
			else // message
			{
				actor.println("[DefaultConnectorParser] Detected message! Sending..");
				
				String messageID = prologData.split(",")[0].split("\\(")[1].trim();
				String messageType = prologData.split(",")[1].trim();
				String dest = prologData.split(",")[2].trim();
				
				String emitter = prologData.split(",")[3];
				int emitterIndex = prologData.indexOf(emitter);
				int payloadStartIndex = emitterIndex + emitter.length() + 1; // ','
				String[] tokens = prologData.split(",");
				String seqNum = tokens[tokens.length -1];
				int seqNumIndex = prologData.indexOf(seqNum);
				String payload = prologData.substring(payloadStartIndex, seqNumIndex - 1).trim();
				
				try 
				{
					actor.sendMsg(messageID, dest, messageType, payload);
				} catch (Exception e) 
				{
					e.printStackTrace();
				}
			}

		}
		else // prolog goal?
		{
			actor.println("[DefaultConnectorParser] Custom prolog data detected! Trying to unify goal with actor's loaded theory..");
			actor.solveGoal(prologData);
		}
	}
	*/

}
