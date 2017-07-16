%====================================================================================
% Context cxt_paRobot  SYSTEM-configuration: file it.unibo.cxt_paRobot.paSys.pl 
%====================================================================================
context(cxt_parobot, "192.168.137.1",  "TCP", "8079" ).  		 
context(cxt_paremoteconsole, "192.168.137.1",  "TCP", "8089" ).  		 
context(cxt_pasonara, "192.168.137.1",  "TCP", "8099" ).  		 
context(cxt_pasonarb, "192.168.137.2",  "TCP", "8069" ).  		 
%%% -------------------------------------------
qactor( qapaconsole , cxt_paremoteconsole, "it.unibo.qapaconsole.MsgHandle_Qapaconsole"   ). %%store msgs 
qactor( qapaconsole_ctrl , cxt_paremoteconsole, "it.unibo.qapaconsole.Qapaconsole"   ). %%control-driven 
qactor( qapasonara , cxt_pasonara, "it.unibo.qapasonara.MsgHandle_Qapasonara"   ). %%store msgs 
qactor( qapasonara_ctrl , cxt_pasonara, "it.unibo.qapasonara.Qapasonara"   ). %%control-driven 
qactor( qapasonarb , cxt_pasonarb, "it.unibo.qapasonarb.MsgHandle_Qapasonarb"   ). %%store msgs 
qactor( qapasonarb_ctrl , cxt_pasonarb, "it.unibo.qapasonarb.Qapasonarb"   ). %%control-driven 
%%% -------------------------------------------
%%% -------------------------------------------
qactor( qrparobot , cxt_parobot, "it.unibo.qrparobot.MsgHandle_Qrparobot" ). 
qactor( qrparobot_ctrl , cxt_parobot, "it.unibo.qrparobot.Qrparobot" ). 

