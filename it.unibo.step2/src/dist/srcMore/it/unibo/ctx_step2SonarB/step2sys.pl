%====================================================================================
% Context ctx_step2SonarB standalone= SYSTEM-configuration: file it.unibo.ctx_step2SonarB.step2Sys.pl 
%====================================================================================
context(ctx_step2sonarb, "172.20.10.4",  "TCP", "8069" ).  		 
%%% -------------------------------------------
qactor( qastep2sonarb , ctx_step2sonarb, "it.unibo.qastep2sonarb.MsgHandle_Qastep2sonarb"   ). %%store msgs 
qactor( qastep2sonarb_ctrl , ctx_step2sonarb, "it.unibo.qastep2sonarb.Qastep2sonarb"   ). %%control-driven 
