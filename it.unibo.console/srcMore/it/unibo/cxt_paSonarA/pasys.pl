%====================================================================================
% Context cxt_paSonarA standalone= SYSTEM-configuration: file it.unibo.cxt_paSonarA.paSys.pl 
%====================================================================================
context(cxt_pasonara, "192.168.137.1",  "TCP", "8099" ).  		 
%%% -------------------------------------------
qactor( qapasonara , cxt_pasonara, "it.unibo.qapasonara.MsgHandle_Qapasonara"   ). %%store msgs 
qactor( qapasonara_ctrl , cxt_pasonara, "it.unibo.qapasonara.Qapasonara"   ). %%control-driven 
