package it.unibo.qactors.web;
 

public class GuiUiKb {
 

	public static final String terminalCmd = "usercmd";
 	public static final String inputCmd    = "inputcmd";
	public static final String speedCmd    = "speed";
	public static final String forwardCmd  = "w";
	public static final String backwardCmd = "s";
	public static final String leftCmd     = "a";
	public static final String rightCmd    = "d";
	public static final String stopCmd     = "h";

	
	public static String buildCorrectPrologString( String s ){
//		System.out.println("buildCorrectPrologString ... "+s);
		//a " within " or a ' within '
		if( s.startsWith("\"")   ){
			String s1 = s.substring(1,s.length()-1);
//			System.out.println(" ---- "+s1);
			s1 = s1.replace("\"", "'");
			s = "\""+s1+"\"";
		}
		else if( s.startsWith("\'")   ){
			String s1 = s.substring(1,s.length()-1);
//			System.out.println(" ---- "+s1);
			s1 = s1.replace("'", "\"");
			s = "'"+s1+"'";
		}
		if( ! s.contains("\"") && !  s.contains("'")  ) return s;
		if(  s.contains("\"") &&  ! s.contains("'") ) return s;
		if(  s.contains("'") &&  ! s.contains("\"") ){
			//This to allow Talk checking
			//s = s.replace("'", "\"");
			return s.replace("'", "\"");
		}
		if( s.indexOf('"') < s.indexOf("'")){
			//s is a msg  received form network
	 		s = s.replace("\"", "@");
			s = s.replace("'", "\"");
			s = s.replace("@", "'");
		}
		//else s is a input from local console
		return s;	
	}
}
