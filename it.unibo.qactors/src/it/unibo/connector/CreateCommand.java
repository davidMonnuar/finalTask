package it.unibo.connector;

public class CreateCommand {

private String name, prefabs;
private float  x,y,z,qx,qy,qz,v;

	public CreateCommand(String name, String prefabs, 
			float x, float y, float z, float qx, float qy, float qz, float v){
		this.name    = name;
		this.prefabs = "'"+prefabs+"'";
		this.x=x;
		this.y=y;
		this.z=z;
		this.qx=qx;
		this.qy=qy;
		this.qz=qz;
		this.v=v;
		
	}
@Override
	public String toString(){
		return "create(NAME,PREFAB,PX,PY,PZ,QX,QY,QZ,V)"
				.replace("NAME", name) 
				.replace("PREFAB", prefabs) 
				.replace("PX", ""+x) 
				.replace("PY", ""+y) 
				.replace("PZ", ""+z) 
				.replace("QX", ""+qx) 
				.replace("QY", ""+qy) 
				.replace("QZ", ""+qz) 
				.replace("V", ""+v) ;
	}
}
