%====================================================================================
% Context cxt_demo1SonarB standalone= SYSTEM-configuration: file it.unibo.cxt_demo1SonarB.demo1Sys.pl 
%====================================================================================
context(cxt_demo1sonarb, "172.20.10.4",  "TCP", "8069" ).  		 
%%% -------------------------------------------
qactor( qademo1sonarb , cxt_demo1sonarb, "it.unibo.qademo1sonarb.MsgHandle_Qademo1sonarb"   ). %%store msgs 
qactor( qademo1sonarb_ctrl , cxt_demo1sonarb, "it.unibo.qademo1sonarb.Qademo1sonarb"   ). %%control-driven 
