%====================================================================================
% Context ctx_demo2SonarB standalone= SYSTEM-configuration: file it.unibo.ctx_demo2SonarB.demo2Sys.pl 
%====================================================================================
context(ctx_demo2sonarb, "172.20.10.2",  "TCP", "8069" ).  		 
%%% -------------------------------------------
qactor( qademo2sonarb , ctx_demo2sonarb, "it.unibo.qademo2sonarb.MsgHandle_Qademo2sonarb"   ). %%store msgs 
qactor( qademo2sonarb_ctrl , ctx_demo2sonarb, "it.unibo.qademo2sonarb.Qademo2sonarb"   ). %%control-driven 
