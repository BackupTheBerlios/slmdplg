import  EDU.gatech.cc.is.util.Vec2;
import  EDU.gatech.cc.is.abstractrobot.*;
//Clay not used

/**
 * Cambiado portero respecto a v1 (MattiHetero)
 * Semiimplementada la estrategia de Defensa
 * Falta añadir comunicacion entre los 2 defensas
 * 
 */


public class RibeiroTeam extends ControlSystemSS
{	
	// Current state variables
    Vec2 CurMyPos, CurBallPos, CurBallPosEgo;
    long CurTime;
    int CurMode;
    boolean KickIt = false;
    boolean bienOrientado = false;
    int MyNum;
    Vec2 CurTeammates[], CurOpponents[];
    Vec2 CurOurGoal, CurOpponentsGoal;
    int StuckCount = 0;

    // Previous step state variables
    Vec2 PrevMyPos, PrevBallPos, PrevBallPosEgo;
    long PrevTime;
    int PrevMode;
    Vec2 PrevTeammates[], PrevOpponents[];
  
    // Modes
    final int MODE_ATTACK = 0;
    final int MODE_GOON = 1;
    final int MODE_GOALIE = 2;

    // Useful constants
    final double PI = Math.PI;
    final double DEFAULT_SPEED = 1.0;
    final double DEFENDED_DISTANCE = 0.03;
    final double BETWEEN_GOAL_ANGLE = PI/6;
    final double ROBOT_RADIUS = 0.06;
    final double BALL_RADIUS = 0.02;
    final double GOAL_WIDTH = .4;
    final double STUCK_LIMIT = 50;
    final double KICK_DISTANCE = 1.0;
   	final double DEFENSE_RADIUS = 0.6;
   	
	// what side of the field are we on? -1 for west +1 for east
	private int SIDE;

    // Initialize the important previous values
    public void Configure()
    {
        CurTime = abstract_robot.getTime();
		if( abstract_robot.getOurGoal(CurTime).x < 0)
			SIDE = -1;
		else
			SIDE = 1;
       PrevBallPos = new Vec2(0,0);
	}
	
	public int TakeStep()
    {
        //Res.Resultado.Guardar(this,abstract_robot);
        MyNum = abstract_robot.getPlayerNumber(CurTime);
        CurTime = abstract_robot.getTime();
        // Get curret position
        CurMyPos = abstract_robot.getPosition(CurTime);
        // Get egocentric ball position, then convert to absolute position
        CurBallPosEgo = abstract_robot.getBall(CurTime);
        CurBallPos = new Vec2(CurBallPosEgo.x, CurBallPosEgo.y);
        CurBallPos.add(CurMyPos);
        // Get goal positions
        CurOurGoal = abstract_robot.getOurGoal(CurTime);
        CurOpponentsGoal = abstract_robot.getOpponentsGoal(CurTime);
        // Get team positions
        CurTeammates = abstract_robot.getTeammates(CurTime);
        CurOpponents = abstract_robot.getOpponents(CurTime);
        PrevBallPos = new Vec2(CurBallPos.x, CurBallPos.y);


        Vec2 destino = DistribuyeRoles();

		abstract_robot.setSteerHeading(CurTime,destino.t);
		
        if(soyDelantero()) {
        	//Esta parte de código en principio iba en TakeStep
	        if(KickIt)
 	       {	
            	if(abstract_robot.canKick(CurTime)){
	            	/*try{
						java.lang.Thread.sleep(3000);
        			} catch(Exception e){}*/
            		abstract_robot.setSteerHeading(CurTime,abstract_robot.getBall(CurTime).t);	
            		abstract_robot.kick(CurTime);
            		
            	}
            	abstract_robot.setSteerHeading(CurTime,abstract_robot.getBall(CurTime).t);
	        }
        }
        //No soy delantero
	    abstract_robot.setSpeed(CurTime,1.0);
        // tell the parent we're OK
        return(CSSTAT_OK);

	}
	

	boolean soyDelantero() {
		return (MyNum==3 || MyNum==4);
	}
	
	Vec2 DistribuyeRoles() {
		//el vector destino guardara el resultado a devolver
		Vec2 destino = new Vec2(99999,0);
		switch (MyNum) {
            case 0:  destino = Portero(); break;
            case 1:  destino = Defensa(); break;
            case 2:  destino = Ataque2(); break;
            case 3:  destino = Ataque2(); break;
            case 4:  destino = BloqueaPortero(); break;
            default: System.out.println("Error: jugador sin táctica! " + MyNum);break;
        }
        return destino;
	}
	
	boolean Undefended(Vec2 opponent)
    {
        Vec2 AbsOpp = EgoToAbs(opponent);
        // return true if there is no teammate within DEFENDED_DISTANCE of opponent.
        for(int i = 0; i < CurTeammates.length; i++)
            {
                Vec2 AbsPos = EgoToAbs(CurTeammates[i]);
                Vec2 DiffPos = new Vec2(AbsOpp.x - AbsPos.x, AbsOpp.y - AbsPos.y);
      
                if(DiffPos.r < 2 * ROBOT_RADIUS + DEFENDED_DISTANCE) return(false);
            }
        return(true);
    }

	//Devuelve una posición de Defensa óptima.
	//Basado en GoonMode de DoogHomoG
    Vec2 Defensa()
    {
        abstract_robot.setDisplayString("Defensa");
        Vec2 Victim = null;
        if (SIDE==-1)
        	Victim=new Vec2(-99999.0,0.0);
        else
        	Victim=new Vec2(99999.0,0.0);
        Vec2[] OrderedOpponents = new Vec2[CurOpponents.length];
        OrderedOpponents = OrdenaOponentes();
        for(int i=0; i < OrderedOpponents.length; i++)
            {
                if(Undefended(OrderedOpponents[i]) && (OrderedOpponents[i].r < Victim.r))
                    Victim = OrderedOpponents[i];
            }

		Vec2 Opp2OurGoal = CurOurGoal;
        Opp2OurGoal.sub(Victim);
        Opp2OurGoal.setr(2*ROBOT_RADIUS);
        Victim.add(Opp2OurGoal); 
        boolean mirandoalfrente = false;
        double orientacion = abstract_robot.getSteerHeading(CurTime);
        if (SIDE==-1)
        	mirandoalfrente = (-(PI/2.0) < orientacion) && (orientacion < (PI/2.0));
        else
        	mirandoalfrente = (orientacion> PI/2.0) || (orientacion < -(PI/2.0));
        if (abstract_robot.canKick(CurTime) && mirandoalfrente) {
        	abstract_robot.kick(CurTime);
        }
        return(Victim);
    }

	private Vec2 BloqueaPortero( )
	{
		abstract_robot.setDisplayString("Bloqueador");
		Vec2 portcontrario = closest_to( CurOpponentsGoal, CurOpponents);
		Vec2 destino = CurOpponentsGoal;
		destino.sub( portcontrario);
		destino.setr( ROBOT_RADIUS );
		destino.add( portcontrario);

		return destino;
	}

	//Ataque basado en el fichero nuevo2 de Maria
	Vec2 Ataque2()
    {	
    	//boolean veteAPorBola = masCercaBola();
    	//if(veteAPorBola){
			KickIt = false;
		  
			Vec2 TargetSpot;
			Vec2 PorteriaContraria = new Vec2(abstract_robot.getOpponentsGoal(CurTime));
			if ((bienOrientado)&& (Math.abs(PorteriaContraria.r) < KICK_DISTANCE)) {						
				KickIt = true;
				TargetSpot = new Vec2(abstract_robot.getBall(CurTime));
				bienOrientado = false;
			}
	        else KickIt = false;
	        
	        if(!KickIt){ //si estaba bien orientado, no necesito recalcular en el siguiente step
		        abstract_robot.setDisplayString("Delantero");
		        TargetSpot = new Vec2(abstract_robot.getBall(CurTime));
		        Vec2 GoalSpot = new Vec2(abstract_robot.getOpponentsGoal(CurTime));
		        if(CurMyPos.y > 0) GoalSpot.y += 0.9 * (GOAL_WIDTH / 2.0);
		        if(CurMyPos.y < 0) GoalSpot.y -= 0.9 * (GOAL_WIDTH / 2.0);
		        TargetSpot.sub(GoalSpot);
		        TargetSpot.setr(ROBOT_RADIUS);
		        TargetSpot.add(abstract_robot.getBall(CurTime));
		
				Vec2 posteArriba = new Vec2(0,0.40/2.0);
				Vec2 posteAbajo = new Vec2(0,-(0.40/2.0));
				posteArriba.add(PorteriaContraria);
				posteAbajo.add(PorteriaContraria);
				Vec2 aux = new Vec2(abstract_robot.getBall(CurTime));
				
				
				//vemos si la pelota está bien orientada con la portería para chutar
				if (PorteriaContraria.x>0)
				{
				// atacando a la derecha corregimos ángulos porque hay angulos negativos
				// convertimos a lo que queremos
					if (posteAbajo.t>Math.PI/2.0)
						posteAbajo.t -=2.0* Math.PI;
					if (aux.t>Math.PI/2.0)
						aux.t -= 2*Math.PI;
					if (posteArriba.t>Math.PI/2.0)
						posteArriba.t -=2.0* Math.PI;
					bienOrientado = (aux.t<posteArriba.t-0.1) && (aux.t>posteAbajo.t+0.1);
					if (bienOrientado) {
						double uno,dos,tres;
						uno = Math.round(posteArriba.t*100.0)/100.0; 
						dos = Math.round(posteAbajo.t*100.0)/100.0; 
						tres = Math.round(aux.t*100.0)/100.0; 
						abstract_robot.setDisplayString(uno + " " + dos + " " + tres); 
						//TargetSpot = abstract_robot.getBall(CurTime);
					}
				}
				else {
					//atacamos a la izquierda
					if (posteAbajo.t<0.0)
							posteAbajo.t +=2.0* Math.PI;
					if (aux.t<0.0)
							aux.t += 2.0*Math.PI;
					if (posteArriba.t<0.0)
							posteArriba.t +=2.0* Math.PI;
					
					bienOrientado = (aux.t>posteArriba.t+0.1) && (aux.t<posteAbajo.t-0.1);
					
				}
					
				
			}
			else TargetSpot = new Vec2(abstract_robot.getBall(CurTime));
	/*}
	else{//bloquea al otro portero
		TargetSpot
	}*/
       return(TargetSpot);
    }

	public Vec2 BuenTiro(long timestamp) 
	{
		Vec2 ball = abstract_robot.getBall(timestamp);
		Vec2 goal = abstract_robot.getOpponentsGoal(timestamp);
		Vec2 last_spot = new Vec2(ball.x, ball.y);
		last_spot.sub(goal);
		last_spot.setr(abstract_robot.RADIUS);
		last_spot.add(ball);
		return(last_spot);
	}
    
    Vec2 EgoToAbs(Vec2 EgoPos)
    {
        Vec2 AbsPosition = new Vec2(EgoPos.x, EgoPos.y);
        AbsPosition.add(CurMyPos);
        return(AbsPosition);
    }
    
	//Portero de MattiHetero
	Vec2 Portero() {
		abstract_robot.setDisplayString("Portero");
		Vec2 temp=new Vec2(CurBallPosEgo);
		temp.sub(CurOurGoal);	// ball to goal
		if (temp.x*SIDE<-2*DEFENSE_RADIUS)
		{	// to allow left and right to take their role
			temp.setr(DEFENSE_RADIUS);
			temp.add(CurOurGoal);
			temp.sety(CurBallPosEgo.y);	/* follow the ball, so the goalie doesnt become left-
or									rightmost, preventing the others to choose this role */
		}
		else if (temp.r>DEFENSE_RADIUS)
		{ // move out a bit if the ball is far from our goal
		  // prevents some of those mean goalie blocking behaviors
			temp.setr(Math.min(temp.r-DEFENSE_RADIUS,DEFENSE_RADIUS));
			temp.add(CurOurGoal);
		}
		else
		{	// stay inside the goal
			if (temp.y>0.25) 
				temp.sety(0.25);
			if (temp.y<-0.25)
				temp.sety(-0.25);
			temp.setx(0);
			temp.add(CurOurGoal);
		}
		return temp;
	}

    Vec2 PorteroDoog() {
            abstract_robot.setDisplayString("Goalie");
        Vec2 ReturnCmd = new Vec2(CurOurGoal.x, CurOurGoal.y);
        // If we're too far out of goal in x dir, get back in!
        Vec2 OurGoalAbs = new Vec2(CurOurGoal.x, CurOurGoal.y);
        OurGoalAbs.add(CurMyPos);
        if(Math.abs(CurMyPos.x) < Math.abs(OurGoalAbs.x * 0.9))
            {
                return(CurOurGoal);
            }

        // Otherwise, calculate projected ball trajectory
        Vec2 BallDir = new Vec2(CurBallPos.x, CurBallPos.y);
        BallDir.sub(PrevBallPos);
        // If ball is headed into goal, block it!
        ReturnCmd.setx(0);
    
        boolean MoveUp = false;
        boolean MoveDown = false;

        if(CurMyPos.y < CurBallPos.y) MoveUp = true;
        if(CurMyPos.y > CurBallPos.y) MoveDown = true;
        if(CurMyPos.y > GOAL_WIDTH/2.0) MoveUp = false;
        if(CurMyPos.y < -GOAL_WIDTH/2.0) MoveDown = false;

        if(MoveDown && MoveUp)
            {
                ReturnCmd.sety(0);
                //      System.out.println("Both " + CurMyPos.y + " " + CurBallPos.y);
            }
        else if(MoveDown) 
            {
                ReturnCmd.sety(-1);
                //      System.out.println("Down");
            }
        else if(MoveUp) 
            {
                ReturnCmd.sety(1);
                //      System.out.println("Up");
            }
        else 
            {
                ReturnCmd.sety(0);
            }

        return(ReturnCmd);
    }

	Vec2[] OrdenaOponentes() {
		Vec2 aux = null;
		int posMejor = 0;
		double mejorX = 0;
		if (SIDE == -1)
			mejorX = 999.0;
		else
			mejorX = -999.0;

		Vec2[] oponentesOrdenados = CurOpponents;
		for (int j=0; j<oponentesOrdenados.length; j++) {
			for (int i =j; i<oponentesOrdenados.length; i++) {
				if (SIDE == -1) {
					if (oponentesOrdenados[i].x<mejorX) { 
						mejorX=oponentesOrdenados[i].x;
						posMejor = i;
					}
				}
				else {//SIDE == 1 
					if (oponentesOrdenados[i].x>mejorX) {
						mejorX=oponentesOrdenados[i].x;
						posMejor = i;
					}
				}
			}
			aux = oponentesOrdenados[j];
			oponentesOrdenados[j] = oponentesOrdenados[posMejor];
			oponentesOrdenados[posMejor] = aux;
			//System.out.println(oponenteMasCercano.x);
		}
		return oponentesOrdenados;
	}
	public boolean masCercaBola(){
		Vec2 ball = abstract_robot.getBall(CurTime);
		Vec2 [] teammates = abstract_robot.getTeammates(CurTime);
		//consideramos el otro atacante la posicion 0
		Vec2 bolaOtroDelantero = new Vec2(ball);
		bolaOtroDelantero.add(teammates[0]);
		if(ball.r<bolaOtroDelantero.r)
			return true;
		else
			return false;
	
	}
	
	private Vec2 closest_to( Vec2 point, Vec2[] objects)
	{
		double dist = 9999;
		Vec2 result = new Vec2(0, 0);
		Vec2 temp = new Vec2(0, 0);

		for( int i=0; i < objects.length; i++)
		{

			// find the distance from the point to the current
			// object
			temp.sett( objects[i].t);
			temp.setr( objects[i].r);
			temp.sub( point);

			// if the distance is smaller than any other distance
			// then you have something closer to the point
			if(temp.r < dist)
			{
				result = objects[i];
				dist = temp.r;
			}
		}

		return result;
	}
}
 