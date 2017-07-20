%====================================================================================
% Context cxt_demo1SonarA standalone= SYSTEM-configuration: file it.unibo.cxt_demo1SonarA.demo1Sys.pl 
%====================================================================================
context(cxt_demo1sonara, "172.20.10.5",  "TCP", "8099" ).  		 
%%% -------------------------------------------
qactor( qademo1sonara , cxt_demo1sonara, "it.unibo.qademo1sonara.MsgHandle_Qademo1sonara"   ). %%store msgs 
qactor( qademo1sonara_ctrl , cxt_demo1sonara, "it.unibo.qademo1sonara.Qademo1sonara"   ). %%control-driven 
