%====================================================================================
% Context cxt_paSonarB standalone= SYSTEM-configuration: file it.unibo.cxt_paSonarB.paSys.pl 
%====================================================================================
context(cxt_pasonarb, "192.168.137.2",  "TCP", "8069" ).  		 
%%% -------------------------------------------
qactor( qapasonarb , cxt_pasonarb, "it.unibo.qapasonarb.MsgHandle_Qapasonarb"   ). %%store msgs 
qactor( qapasonarb_ctrl , cxt_pasonarb, "it.unibo.qapasonarb.Qapasonarb"   ). %%control-driven 
