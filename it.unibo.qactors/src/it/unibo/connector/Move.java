package it.unibo.connector;

public class Move {
private String name,  direction;
private float speed,   duration,   angle;

	public Move( String name, String direction, float speed, float duration, float angle){
		this.name      =  name ;
		this.direction = "'"+direction+"'";
		this.speed     = speed;
		this.duration  = duration;
		this.angle     = angle;

	}
	@Override
	public String toString(){
		return "move(NAME,DIRECTION,SPEED,TIME,ANGLE)"
				.replace("NAME", name)
				.replace("DIRECTION", direction)
				.replace("SPEED", ""+speed)
				.replace("TIME", ""+duration)
				.replace("ANGLE", ""+angle);
	}
}
