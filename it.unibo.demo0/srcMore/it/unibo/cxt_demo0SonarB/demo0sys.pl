%====================================================================================
% Context cxt_demo0SonarB standalone= SYSTEM-configuration: file it.unibo.cxt_demo0SonarB.demo0Sys.pl 
%====================================================================================
context(cxt_demo0sonarb, "172.20.10.2",  "TCP", "8069" ).  		 
%%% -------------------------------------------
qactor( qademo0sonarb , cxt_demo0sonarb, "it.unibo.qademo0sonarb.MsgHandle_Qademo0sonarb"   ). %%store msgs 
qactor( qademo0sonarb_ctrl , cxt_demo0sonarb, "it.unibo.qademo0sonarb.Qademo0sonarb"   ). %%control-driven 
