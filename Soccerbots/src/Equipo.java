import EDU.gatech.cc.is.abstractrobot.ControlSystemSS;
import EDU.gatech.cc.is.util.Vec2;

public class Equipo extends ControlSystemSS {
	private long curr_time;
	private long mynum; 
	private double rotation; 
	private Vec2 ball;
	private Vec2 ourgoal; 
	private Vec2 theirgoal; 
	private Vec2[] teammates; 
	private Vec2[] opponents; 
	private Vec2 closest;  
	private Vec2 closest_to_ball;
	private Vec2 move;
	private boolean kickit;
	private int side;
	private boolean puedeCambiar = false;

	public void Configure() {
		curr_time = abstract_robot.getTime();
		if (abstract_robot.getOurGoal(curr_time).x < 0)
			side = -1;
		else
			side = 1;

		move = new Vec2(0, 0);
	}
	
	private double ang(double a) {
		if (a > 2 * Math.PI)
			return ang(a - 2 * Math.PI);
		if (a < 0)
			return ang(a + 2 * Math.PI);
		return a;
	}
	 
	public int takeStep()
	{
		//Res.Resultado.Guardar(this,abstract_robot);
		
		Vec2 closest = new Vec2(0,0);
		update_env();
		Vec2 pos = abstract_robot.getPosition(curr_time);
		/*--- Goalie ---*/
		if( mynum == 0)
		{
			portero();
		}

		/*--- Backup ---*/
		else if( mynum == 1)
		{
			
			
			return defensa();
		}

		/*--- Offside ---*/
		else if (mynum == 2) {
			if (puedeCambiar  && (ball.x + pos.x) == 0
					&& (ball.x + pos.x) == 0) {

			
				abstract_robot.setID(3);
				puedeCambiar = false;
				conductor();
			} else if (!puedeCambiar && (ball.x + pos.x) != 0
					&& (ball.x + pos.x) != 0) {
				puedeCambiar = true;
			}
			return delantero();
		}

		/*--- Designated Driver ---*/
		else if( mynum == 3)
		{
			if(puedeCambiar && (ball.x + pos.x) == 0 && (ball.x + pos.x) ==0 ){
				
				abstract_robot.setID(2);
				puedeCambiar = false;
				return delantero();
				
			}else if(!puedeCambiar && (ball.x + pos.x) != 0 && (ball.x + pos.x)  !=0 ){
				puedeCambiar = true;
			}
			conductor();
		}

		/*--- Center ---*/
		else
			return delantero();
		
		

		// tell the parent we're OK
		return(CSSTAT_OK);
	}
	
	
	private void update_env() {
		/*--- bookkeeping data ---*/
		// get the current time for timestamps
		curr_time = abstract_robot.getTime();

		// get my player id
		mynum = abstract_robot.getPlayerNumber(curr_time);

		/*--- sensor data ---*/
		// get vector to the ball
		ball = abstract_robot.getBall(curr_time);
		ball.t = ang(ball.t);

		// get vector to our and their goal
		ourgoal = abstract_robot.getOurGoal(curr_time);
		ourgoal.t = ang(ourgoal.t);
		theirgoal = abstract_robot.getOpponentsGoal(curr_time);
		theirgoal.t = ang(theirgoal.t);

		// get a list of the positions of our teammates
		teammates = abstract_robot.getTeammates(curr_time);

		// get a list of the positions of the opponents
		opponents = abstract_robot.getOpponents(curr_time);

		rotation = abstract_robot.getSteerHeading(curr_time);

		/*--- default actuator control ---*/
		// set movement data: rotation and speed;
		move.sett(0.0);
		move.setr(0.0);

		// set kicking
		kickit = false;

	}
	
	private void portero()
	{
		// if the ball is behind me try to kick it out
		if( ball.x * side > 0)
		{
			move.sett( ball.t);
			move.setr( 1.0);
			kickit = true;
		}

		// if i'm outside the goal area go back toward the goal
		else if( (Math.abs(ourgoal.x) > abstract_robot.RADIUS * 1.4) ||
			 (Math.abs(ourgoal.y) > abstract_robot.RADIUS * 4.25) )

		{
			move.sett( ourgoal.t);
			move.setr( 1.0);
		}

		// stay between the ball and the goal
		else
		{
			if( ball.y > 0)
				move.sety( 7);
			else
				move.sety( -7);

			move.setx( (double)side);

			if( Math.abs( ball.y) < abstract_robot.RADIUS * 0.15)
				move.setr( 0.0);
			else
				move.setr( 1.0);
		}
		/*--- Send commands to actuators ---*/
		// set the heading
		abstract_robot.setSteerHeading(curr_time, move.t);

		// set the speed
		abstract_robot.setSpeed(curr_time, move.r);

		// maybe kick it
		if (kickit && abstract_robot.canKick( curr_time))
			abstract_robot.kick(curr_time);
		
	}
	
	protected static double escalar(Vec2 v, Vec2 u) {
        return v.x * u.x + v.y * u.y;
    }
	
	private void empujarHaciaPorteria()
	{
		Vec2 tmp1 = new Vec2(this.theirgoal);
//		tmp1.sub(ball);
		Vec2 pos = abstract_robot.getPosition(curr_time);
		if (pos.y>0)
			tmp1.sety(tmp1.y+abstract_robot.RADIUS*4);
		else 
			tmp1.sety(tmp1.y-abstract_robot.RADIUS*4);
		Vec2 tmp2 = new Vec2(this.ball);
		double ang1 = ang(tmp2.t-tmp1.t);
		double act = abstract_robot.getSteerHeading(curr_time);
		abstract_robot.setSteerHeading(curr_time, act+ang1*2);
		abstract_robot.setSpeed(curr_time, 1);
	}
	private void siCercaChutar()
	{
		Vec2 pos = abstract_robot.getPosition(curr_time);
		Vec2 tmp1 = new Vec2(this.theirgoal);
		tmp1.sub(ball);
		Vec2 tmp2 = new Vec2(this.ball);
		double ang1 = ang(tmp2.t-tmp1.t);
		double ang2 = ((1+side)/2)*Math.PI/2;
		double ang3;
		if (side==-1) 
			ang3 = pos.y>0?Math.PI*2-0.6:0.6;
		else
			ang3 = pos.y>0?Math.PI+0.6:Math.PI - 0.6;		
		if 
//		(dif(abstract_robot.getSteerHeading(curr_time),ang2)<0.7&&
//			ang1<0.1&&theirgoal.r<abstract_robot.RADIUS*8)
		(dif(ang(abstract_robot.getSteerHeading(curr_time)),ang3)<0.2
				&&theirgoal.r<abstract_robot.RADIUS*10)
		{
			abstract_robot.setDisplayString("chutando");
			abstract_robot.kick(curr_time);
		}
		
	}
	
	private double dif(double a1,double a2)
	{
		double diff = ang(a1-a2);
		if (diff>Math.PI) 
			diff = (2*Math.PI-diff);  
		return diff;
	}

	private Vec2 dirigirAlMedio(Vec2 vec1, Vec2 vec2) {
		Vec2 dir = new Vec2(vec2);
		dir.sub(vec1);
		double BM = ((vec1.r * vec1.r) + (dir.r * dir.r) - (vec2.r * vec2.r))
				/ (2 * dir.r);
		if(Math.abs(BM) <= dir.r ) dir.normalize(Math.abs(BM));
		dir.add(vec1);
		return new Vec2(dir);
	}	
	
	public void conductor()
	{
		move.r=1.0;
		move.t=0;
		Vec2 pos = abstract_robot.getPosition(curr_time);
		if (ball.x*side>0)
		{
			abstract_robot.setDisplayString("hacia atrás");
			Vec2 v=new Vec2(ball);
			v.sub(theirgoal);
			v.setr(8*abstract_robot.RADIUS);
			v.add(ball);
			abstract_robot.setSteerHeading(curr_time, v.t);
			abstract_robot.setSpeed(curr_time, 1);
		}
		else if (ball.r<abstract_robot.KICKER_SPOT_RADIUS+1.1*abstract_robot.RADIUS)
		{
				abstract_robot.setDisplayString("empujando");
				empujarHaciaPorteria();
				siCercaChutar();
		}
		else
		{
			abstract_robot.setDisplayString("a por la bola");
			abstract_robot.setSteerHeading(curr_time, ball.t);
			abstract_robot.setSpeed(curr_time, move.r);
		}
	}
	
	public int defensa() {
		update_env();
		
		//si la pelota esta en nuestro radio de tiro
		if(ball.r <= abstract_robot.RADIUS + abstract_robot.KICKER_SPOT_RADIUS ){
			double actDir =ang(abstract_robot.getSteerHeading(curr_time));
			//miro si mi direccion es hacia adelante
			//y despejo la pelota
			if (actDir < Math.PI ){
				actDir -=(Math.PI/2);
				if((actDir * side )>0) abstract_robot.kick(curr_time);
				
			}else{
				actDir -=((3*Math.PI)/2);
				if((actDir * side )<0) abstract_robot.kick(curr_time);
				
			}
			return (CSSTAT_OK);
		}
		
		Vec2 pos = abstract_robot.getPosition(curr_time);
		
		//si la pelota pasa el medio campo esperamos en el centro
		if ((ball.x + pos.x) * side < 0) {
			move = new Vec2(-pos.x + (0.35*side), -pos.y);
			closest = seChocaConAlguien() ;
			if( closest != null && closest.r < abstract_robot.RADIUS*1.1 )
			{
				if((closest.t - Math.PI/2)< move.t && move.t < (closest.t + Math.PI/2)){
					if(closest.t < move.t){
						move.sett( move.t+Math.PI /2);
					}
					else{
						move.sett( move.t-Math.PI /2);
					}
						
					
				}
			}
				move.setr(1.0);

			abstract_robot.setSpeed(curr_time, move.r);
			abstract_robot.setSteerHeading(curr_time, move.t);
			return (CSSTAT_OK);

		}
		
		
		move.r = 1.0;
		move.t = 0;
		
		Vec2 dir = new Vec2(ourgoal);
		dir.sub(ball);
		abstract_robot.setSpeed(curr_time, move.r);
		//mira que este por delante de la pelota y alineado
		if (((ball.x * side)<0)&&
		((ball.t - 0.018 < dir.t && dir.t < ball.t + 0.018)
				|| (ourgoal.t - 0.018 < dir.t && dir.t < ourgoal.t + 0.018))) {
			
			//se situa en la distancia media entre la pelota y nuestra porteria
			if(ball.r < ourgoal.r){
				move = new Vec2(ourgoal);
			}else if ( ourgoal.r < ball.r){
				move = new Vec2(ball);
			}
			


			
		
		} 
		else {	
			
			Vec2 ourgoal2 = new Vec2(ourgoal);
			
			move = dirigirAlMedio(ball,ourgoal2);
			
			
			// comprobamos que la pelota no este por en medio
			// de la direccion que nos dirigimos
			if ((ball.x * side)>0){
				Vec2 dir2 = new Vec2(move);
				dir2.sub(ball);
				Vec2 dir3= new Vec2(0,0);
				dir3.sub(ball);
				dir2 = dirigirAlMedio(dir3, dir2);
				if (dir2.r <= abstract_robot.RADIUS && ball.r < 0.14) {
					if (dir2.y < 0) {
						move = new Vec2(ball.x + pos.x, ball.y + pos.y
								- abstract_robot.RADIUS);
					} else {
						move = new Vec2(ball.x + pos.x, ball.y + pos.y
								+ abstract_robot.RADIUS);
					}
				}
			}
//			mira si se choca con alguien
			closest = seChocaConAlguien() ;
			if( closest != null && closest.r < abstract_robot.RADIUS*1.1 )
			{
				if((closest.t - Math.PI/2)< move.t && move.t < (closest.t + Math.PI/2)){
					if(closest.t < move.t){
						move.sett( move.t+Math.PI /2);
					}
					else{
						move.sett( move.t-Math.PI /2);
					}
						
					
				}
				move.setr( 1.0);
				abstract_robot.setSteerHeading(curr_time, move.t);
				return (CSSTAT_OK);
			}
			}
			

		
		abstract_robot.setSteerHeading(curr_time, move.t);
		return (CSSTAT_OK);
	}
	
	private void goToOutGoal(){
		abstract_robot.setSteerHeading(curr_time, ourgoal.t);
		abstract_robot.setSpeed(curr_time, 1.0);
	}
	
	private Vec2 seChocaConAlguien(){

		for(int i = 0; i< teammates.length;i++){
			Vec2 jug = new Vec2(teammates[i]);
			if(jug.r <= abstract_robot.RADIUS*2){
				return new Vec2(jug);
			}
		}
		for(int i = 0; i< opponents.length;i++){
			Vec2 jug = new Vec2(opponents[i]);
			if(jug.r <= abstract_robot.RADIUS*2){
				return new Vec2(jug);
			}
		}
		return null;
	}
	
	public int delantero() {
		update_env();
		// mira si se choca con alguien
		closest = seChocaConAlguien();
		if (closest != null && closest.r < abstract_robot.RADIUS * 1.4) {
			move.setx(-closest.x);
			move.sety(-closest.y);
			move.setr(1.0);
			abstract_robot.setSteerHeading(curr_time, move.t);
			return (CSSTAT_OK);
		}

		Vec2 pos = abstract_robot.getPosition(curr_time);
//		si la pelota pasa el medio campo esperamos en el centro
		if ((ball.x + pos.x) * side > 0 && (mynum/3)==0) {
			move = new Vec2(-pos.x + (0.2*side), -pos.y);
			
				move.setr(1.0);

			abstract_robot.setSpeed(curr_time, move.r);
			abstract_robot.setSteerHeading(curr_time, move.t);
			return (CSSTAT_OK);

		}
		move.r = 1.0;
		move.t = 0;
		Vec2 dir = new Vec2(ourgoal);
		// apuntamos hacia una esquina de nuestro campo
		dir.sety(1.522 / 2);
		dir.sub(ball);
		abstract_robot.setSpeed(curr_time, move.r);
		// si estamos por delante de la pelota y alineados con la porteria
		// y la pelota entonces vamos hacia la pelota
		// y nos encontramos en el campo contrario

		if (((ball.x * side) < 0 )
				&& ((ball.t - 0.01 < dir.t && dir.t < ball.t + 0.01)
						|| (ourgoal.t - 0.01 < dir.t && dir.t < ourgoal.t + 0.01) || (ball.r > 0.25))) {

			goToBall();
		}
		// si nos encontramos en el campo contrario
		// y la pelota tambien
		else if (((pos.x * side) < 0) && ((ball.x * side) < 0)) {
			Vec2 punt = new Vec2(ourgoal);
			// apuntamos hacia una esquina de nuestro campo
			if ((mynum /3 ) == 0)
				punt.sety(punt.y + 1.522 / 2);
			else
				punt.sety(punt.y - 1.522 / 2);

			// no movemos hacia el punto más cercano que se encuantra en la
			// recta
			// entre la pelota y nuestra esquina
			move = dirigirAlMedio(ball, punt);

			// comprobamos que la pelota no este por en medio
			// de la direccion que nos dirigimos
			if ((ball.x * side) > 0) {
				Vec2 dir2 = new Vec2(move);
				dir2.sub(ball);
				Vec2 dir3 = new Vec2(0, 0);
				dir3.sub(ball);
				dir2 = dirigirAlMedio(dir3, dir2);
				if (dir2.r <= abstract_robot.RADIUS && ball.r < 0.14) {
					if (dir2.y < 0) {
						move = new Vec2(ball.x + pos.x, ball.y + pos.y
								- abstract_robot.RADIUS);
					} else {
						move = new Vec2(ball.x + pos.x, ball.y + pos.y
								+ abstract_robot.RADIUS);
					}
				}

			}
			abstract_robot.setSteerHeading(curr_time, move.t);
		}
		// si la pelota no esta en el campo contrario
		else {
			Vec2 punt = new Vec2(ourgoal);
			// apuntamos hacia una esquina de nuestro campo
			
			if ((mynum /3 ) == 0)
				punt.sety(punt.y + 1.522 / 2);
			else
				punt.sety(punt.y - 1.522 / 2);

			// no movemos hacia el punto más cercano que se encuantra en la
			// recta
			// entre la porteria contraria y nuestra esquina
			move = dirigirAlMedio(theirgoal, punt);
			// se situa en la distancia media entre la pelota y nuestra porteria
			if (theirgoal.r < punt.r) {
				move = new Vec2(punt);
			} else if (punt.r < theirgoal.r) {
				move = new Vec2(theirgoal);
			}
			abstract_robot.setSteerHeading(curr_time, move.t);
		}

		return (CSSTAT_OK);
	}
	
	private void goToBall() {
		abstract_robot.setSteerHeading(curr_time, ball.t);
		abstract_robot.setSpeed(curr_time, 1.0);

		if (abstract_robot.canKick(curr_time))
			abstract_robot.kick(curr_time);

	}

}
