package it.unibo.qactors.action;
import it.unibo.qactors.QActorMessage;

public interface IMsgQueue {

	public QActorMessage getMsgFromQueue( );
	public int getSizeOfMsgQueue();
}
