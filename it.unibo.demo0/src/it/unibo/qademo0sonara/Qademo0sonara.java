/* Generated by AN DISI Unibo */ 
/*
This code is generated only ONCE
*/
package it.unibo.qademo0sonara;
import it.unibo.is.interfaces.IActivity;
import it.unibo.is.interfaces.IIntent;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;

public class Qademo0sonara extends AbstractQademo0sonara implements IActivity { 
	public Qademo0sonara(String actorId, QActorContext myCtx, IOutputEnvView outEnvView )  throws Exception{
		super(actorId, myCtx, outEnvView);
	}
	protected void addInputPanel(int size){
	}

	public void addCmd()
	{
		outEnvView.getEnv().addCmdPanel("50", new String [] {"50"}, this);
		outEnvView.getEnv().addCmdPanel("100", new String [] {"100"}, this);
		outEnvView.getEnv().addCmdPanel("150", new String [] {"150"}, this);
		outEnvView.getEnv().addCmdPanel("200", new String [] {"200"}, this);
	}
	
	public void execAction(String cmd) {
		
		 this.emit("local_sonara", "sonara("+cmd+")");
	 }
	@Override
	public void execAction() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void execAction(IIntent input) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String execActionWithAnswer(String cmd) {
		// TODO Auto-generated method stub
		return null;
	}
}
