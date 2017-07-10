package it.unibo.qasprint1test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import interfaces.ICommandReceiver;
import interfaces.IConsole;

public class ConsoleTest {
private ICommandReceiver receiver;
private IConsole console;

	@Before
	public void setUp() throws Exception {
		console.setCommandReceiver(receiver);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetterSetterCommandReceiver() {
		assertEquals(console.getCommandReceiver(),receiver);
	}

	@Test
	public void testSendCommand(){
		String command="command";
		console.sendCommand(command);
		assertEquals(command,receiver.receiveCommand());
	}
}
