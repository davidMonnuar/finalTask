%====================================================================================
% Context ctx_demo2SonarA standalone= SYSTEM-configuration: file it.unibo.ctx_demo2SonarA.demo2Sys.pl 
%====================================================================================
context(ctx_demo2sonara, "172.20.10.2",  "TCP", "8099" ).  		 
%%% -------------------------------------------
qactor( qademo2sonara , ctx_demo2sonara, "it.unibo.qademo2sonara.MsgHandle_Qademo2sonara"   ). %%store msgs 
qactor( qademo2sonara_ctrl , ctx_demo2sonara, "it.unibo.qademo2sonara.Qademo2sonara"   ). %%control-driven 
