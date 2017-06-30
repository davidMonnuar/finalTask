package it.unibo.qactors;

import it.unibo.qactors.action.ActorTimedAction;

public class ActionRegisterMessage {
public final  ActorTimedAction action;
public final boolean totegister;
public final String evId;

	public ActionRegisterMessage(String evId,ActorTimedAction action, boolean totegister){
		this.evId      = evId;
		this.action    = action;
		this.totegister= totegister;
	}
 }
