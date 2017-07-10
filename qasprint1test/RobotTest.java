package it.unibo.qasprint1test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.unibo.iot.executors.baseRobot.IBaseRobot;
import it.unibo.iot.models.commands.baseRobot.BaseRobotAngle;
import it.unibo.iot.models.commands.baseRobot.BaseRobotForward;
import it.unibo.iot.models.commands.baseRobot.BaseRobotSpeed;
import it.unibo.iot.models.commands.baseRobot.BaseRobotSpeedValue;
import it.unibo.iot.models.commands.baseRobot.IBaseRobotCommand;

public class RobotTest {
	private IBaseRobot baseRobot;
	private IBaseRobotCommand robotCommand;
	
	@Before
	public void setUp() throws Exception {
		robotCommand = new BaseRobotForward(new BaseRobotSpeed(BaseRobotSpeedValue.ROBOT_SPEED_HIGH),new BaseRobotAngle(60));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void GetterSpeedIbaseRobotCommand() {
		assertEquals(robotCommand.getSpeed(), BaseRobotSpeedValue.ROBOT_SPEED_HIGH);
		assertEquals(60,robotCommand.getAngle());
	}
	
	@Test
	public void testGetterDefStringAndExecute(){
		baseRobot.execute(robotCommand);
		assertEquals("robotCommand(baseRobotForward,robotSpeed(robot_speed_high),robotAngle(60))",robotCommand.getDefStringRep());
	}
	
}
