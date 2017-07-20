%====================================================================================
% Context cxt_demo0SonarA standalone= SYSTEM-configuration: file it.unibo.cxt_demo0SonarA.demo0Sys.pl 
%====================================================================================
context(cxt_demo0sonara, "172.20.10.2",  "TCP", "8099" ).  		 
%%% -------------------------------------------
qactor( qademo0sonara , cxt_demo0sonara, "it.unibo.qademo0sonara.MsgHandle_Qademo0sonara"   ). %%store msgs 
qactor( qademo0sonara_ctrl , cxt_demo0sonara, "it.unibo.qademo0sonara.Qademo0sonara"   ). %%control-driven 
