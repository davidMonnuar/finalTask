package it.unibo.qasprint1test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SonarTest {
private ISonar sonar;
private IMessageReceiver receiver;

	@Before
	public void setUp() throws Exception {
		sonar.setMessageReceiver(receiver);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void GetterSetterMessageReceiver() {
		assertEquals(sonar.getMessageReceiver(), receiver);
	}

	@Test
	public void testSendCommand(){
		String command="command";
		sonar.sendCommand(command);
		assertEquals(command,sonar.receiveCommand());
	}
	
}
