%====================================================================================
% Context ctx_step2SonarA standalone= SYSTEM-configuration: file it.unibo.ctx_step2SonarA.step2Sys.pl 
%====================================================================================
context(ctx_step2sonara, "172.20.10.5",  "TCP", "8099" ).  		 
%%% -------------------------------------------
qactor( qastep2sonara , ctx_step2sonara, "it.unibo.qastep2sonara.MsgHandle_Qastep2sonara"   ). %%store msgs 
qactor( qastep2sonara_ctrl , ctx_step2sonara, "it.unibo.qastep2sonara.Qastep2sonara"   ). %%control-driven 
