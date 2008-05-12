

import java.util.Enumeration;

import	EDU.gatech.cc.is.util.Vec2;
import	EDU.gatech.cc.is.abstractrobot.*;
import EDU.gatech.cc.is.communication.CommunicationException;
import EDU.gatech.cc.is.communication.StringMessage;

import javax.swing.JOptionPane;
//Clay not used

/**
 * Example of a simple strategy for a robot
 * soccer team without using Clay.
 * It illustrates how to use many of the sensor and
 * all of the motor methods of a SocSmall robot.
 * <P>
 * For detailed information on how to configure behaviors, see the
 * <A HREF="../clay/docs/index.html">Clay page</A>.
 * <P>
 * <A HREF="../COPYRIGHT.html">Copyright</A>
 * (c)1997 Georgia Tech Research Corporation
 *
 * @author Tucker Balch
 * @version $Revision: 1.1 $
 */


public class DcIbJr extends ControlSystemSS
	{
	private Enumeration messagesin;	//COMMUNICATION
	/**
	Configure the control system.  This method is
	called once at initialization time.  You can use it
	to do whatever you like.
	*/
	EntrenadorDIJ trainer;
	
	public void Configure()
		{
			if(abstract_robot.getPlayerNumber(abstract_robot.getTime())==0){
				trainer=new EntrenadorDIJ();
			}
			messagesin = abstract_robot.getReceiveChannel();
			abstract_robot.setBaseSpeed(abstract_robot.MAX_TRANSLATION);
		}
		
	
	/**
	Called every timestep to allow the control system to
	run.
	*/
	public int TakeStep()
		{
		//para indicar la estaregia a seguri de las 10 definidas
		int estrategia=4;		
		
		// the eventual movement command is placed here
		Vec2	result = new Vec2(0,0);

		// get the current time for timestamps
		long	curr_time = abstract_robot.getTime();


		 Vec2    vectorPosicion = new Vec2(abstract_robot.getPosition(-1));
		/*--- Get some sensor data ---*/
		// get vector to the ball
		Vec2 ball = abstract_robot.getBall(curr_time);

		// get vector to our and their goal
		Vec2 ourgoal = abstract_robot.getOurGoal(curr_time);
		Vec2 theirgoal = abstract_robot.getOpponentsGoal(curr_time);

		// get a list of the positions of our teammates
		Vec2[] teammates;
		Vec2[] opponents;
		if(curr_time>0){
			teammates = abstract_robot.getTeammates(curr_time);

			opponents = abstract_robot.getOpponents(curr_time);}
		else{
			teammates = abstract_robot.getTeammates(-1);

			opponents = abstract_robot.getOpponents(-1);
		}
		/*--- now compute some strategic places to go ---*/
		// compute a point one robot radius
		// behind the ball.
		Vec2 kickspot = new Vec2(ball.x, ball.y);
		kickspot.sub(theirgoal);
		kickspot.setr(abstract_robot.RADIUS);
		kickspot.add(ball);
		
		//posicion en el area del contrario
		Vec2 punta = new Vec2(10,0);
		for(int z=0;z<10;z++){
			punta.sub(vectorPosicion);
		}
		
		Vec2 puntaI = new Vec2(10,4.5);
		for(int z=0;z<10;z++){
			puntaI.sub(vectorPosicion);
		}
	    Vec2 puntaD = new Vec2(10,-4.5);
		for(int z=0;z<10;z++){
			puntaD.sub(vectorPosicion);
		}
		//posicion en ataque
		Vec2 ataque = new Vec2(7,0);
		for(int z=0;z<10;z++){
			ataque.sub(vectorPosicion);
		}
		Vec2 ataqueD = new Vec2(7,-4.5);
		for(int z=0;z<10;z++){
			ataqueD.sub(vectorPosicion);
		}
		Vec2 ataqueI  = new Vec2(7,4.5);
		for(int z=0;z<10;z++){
			ataqueI.sub(vectorPosicion);
		}
	    //posicion enel centro del campo
		Vec2 centroCampista = new Vec2(0,0);
		for(int z=0;z<10;z++){
			centroCampista.sub(vectorPosicion);
		}
		Vec2 centroCampistaD = new Vec2(0,-4.5);
		for(int z=0;z<10;z++){
			centroCampistaD.sub(vectorPosicion);
		}
		Vec2 centroCampistaI = new Vec2(0,4.5);
		for(int z=0;z<10;z++){
			centroCampistaI.sub(vectorPosicion);
		}
       //posicion en defensa
		Vec2 defensa = new Vec2(-7,0);
		for(int z=0;z<10;z++){
			defensa.sub(vectorPosicion);
		}
		Vec2 defensaD = new Vec2(-7,-4.5);
		for(int z=0;z<10;z++){
			defensaD.sub(vectorPosicion);
		}
		Vec2 defensaI  = new Vec2(-7,4.5);
		for(int z=0;z<10;z++){
			defensaI.sub(vectorPosicion);
		}
		
		
	   //posicion de portero
		double y = ball.y;
	    if (ball.y > 0.0 && vectorPosicion.y > 0.1) y = 0.0;
	    else if (ball.y < 0.0 && vectorPosicion.y < -0.1) y = 0.0;
	    Vec2 portero = new Vec2(ourgoal.x, y);

		// a direction away from the closest teammate.
		/*--- go to one of the places depending on player num ---*/
		int mynum = abstract_robot.getPlayerNumber(curr_time);

		//Cada 2 segundos se llama al entrenador
		//se indican las posiciones de todos los jugadores en ese momento
		//y la posicion del balon
		//se toma la estrategia necesaria
		if((curr_time%2000==0&&mynum==0)){
			//reiniciamos el caso que explica la situacion anterior
			//para definir en el la situacion actual
			trainer.reiniciaCaso();
			//Metemos en el caso la situacion actual del partido
			trainer.aniadePosicionLocal(trainer.xPosicion(ourgoal),trainer.yPosicion(ourgoal),0);
			trainer.aniadePosicionLocal(trainer.xPosicion(theirgoal,teammates[0]),trainer.yPosicion(theirgoal,teammates[0]),1);
			trainer.aniadePosicionLocal(trainer.xPosicion(theirgoal,teammates[1]),trainer.yPosicion(theirgoal,teammates[1]),2);
			trainer.aniadePosicionLocal(trainer.xPosicion(theirgoal,teammates[2]),trainer.yPosicion(theirgoal,teammates[2]),3);
			trainer.aniadePosicionLocal(trainer.xPosicion(theirgoal,teammates[3]),trainer.yPosicion(theirgoal,teammates[3]),4);
			trainer.aniadePosicionVisitante(trainer.xPosicion(theirgoal,opponents[4]),trainer.yPosicion(theirgoal,opponents[4]),0);
			trainer.aniadePosicionVisitante(trainer.xPosicion(theirgoal,opponents[0]),trainer.yPosicion(theirgoal,opponents[0]),1);
			trainer.aniadePosicionVisitante(trainer.xPosicion(theirgoal,opponents[1]),trainer.yPosicion(theirgoal,opponents[1]),2);
			trainer.aniadePosicionVisitante(trainer.xPosicion(theirgoal,opponents[2]),trainer.yPosicion(theirgoal,opponents[2]),3);
			trainer.aniadePosicionVisitante(trainer.xPosicion(theirgoal,opponents[3]),trainer.yPosicion(theirgoal,opponents[3]),4);
			trainer.aniadePosicionBalon(trainer.xPosicion(theirgoal,ball),trainer.yPosicion(theirgoal,ball));
			//hacemos la similitud de ese caso con todos 
			//los que tenemos en la base de casos
			//quedandonos con lso que superen un umbral de similitud
			//y cojemos el de mayor resultado
			trainer.estrategia=trainer.dameEstrategia(5);
			//aplicamos la estrategia a cada jugador
			//y se la añadimos al caso
			trainer.aniadeEstrategia(trainer.estrategia);
		}
		//Cojemos el ultimo mensaje
		//Porque mandamos al estrategia a lso demas por mensajes
		StringMessage msj= new StringMessage();
		if(messagesin.hasMoreElements()){
			while (messagesin.hasMoreElements()){
				msj = (StringMessage)messagesin.nextElement();
			}
			estrategia=Integer.parseInt(msj.val);
		}
		System.out.println(mynum+"-->"+estrategia);
		/*--- Goalie ---*/
		if (mynum == 0)
			{	
				//mandamos a todo los robots la estrategia a seguir
				StringMessage msjE = new StringMessage();
				msjE.val = Integer.toString(trainer.estrategia);
				abstract_robot.broadcast(msjE);
				//asginamos la estrategia
				estrategia=trainer.estrategia;
				System.out.println("0000"+estrategia);
				switch (estrategia) {
				case 0: 
						if(ball.r>0.5){
							result=ataqueI;
						}else
							result=kickspot;
							break;
				case 1: 
						if(ball.r>0.5){
							result=centroCampistaI;
						}else
							result=kickspot;
						break;
				case 2:  
						if(ball.r>0.5){
							result=portero;
						}else
							result=kickspot;
							break;
				case 3: 
						if(ball.r>0.5){
							result=portero;
				}else
						result=kickspot;
				break;
				case 4:  
						if(ball.r>0.5){
							result=portero;
				}else
						result=kickspot;
				break;
				case 5:
						if(ball.r>0.5){
							result=portero;
				}else
						result=kickspot;
				break;
				case 6: 
						if(ball.r>0.5){
							result=portero;
						}else
							result=kickspot;
				break;
				case 7:  
						if(ball.r>0.5){
							result=portero;
						}else
							result=kickspot;
							break;
				case 8:  
						if(ball.r>0.5){
							result=portero;
						}else
						result=kickspot;
						break;
				case 9:  
						if(ball.r>0.5){
							result=portero;
						}else
						result=kickspot;
						break;
				}


			}

		/*--- midback ---*/
		else if (mynum == 1)
			{
			switch (estrategia) {
			case 0: 
					if(ball.r>0.5){
						result=ataque;
					}else
						result=kickspot;
						break;
			case 1: 
					if(ball.r>0.5){
						result=centroCampista;
					}else
						result=kickspot;
					break;
			case 2:  
					if(ball.r>0.5){
						result=centroCampistaI;
					}else
						result=kickspot;
						break;
			case 3: 
					if(ball.r>0.5){
						result=defensa;
			}else
					result=kickspot;
			break;
			case 4:  
					if(ball.r>0.5){
						result=defensa;
			}else
					result=kickspot;
			break;
			case 5:
					if(ball.r>0.5){
						result=defensaI;
			}else
					result=kickspot;
			break;
			case 6: 
					if(ball.r>0.5){
						result=defensaI;
					}else
						result=kickspot;
			break;
			case 7:  
					if(ball.r>0.5){
						result=defensaI;
					}else
						result=kickspot;
						break;
			case 8:  
					if(ball.r>0.5){
						result=defensaI;
					}else
					result=kickspot;
					break;
			case 9:  
					if(ball.r>0.5){
						result=defensa;
					}else
					result=kickspot;
					break;
			}
			}

		else if (mynum == 2)
			{
			switch (estrategia) {
			case 0: 
					if(ball.r>0.5){
						result=ataqueD;
					}else
						result=kickspot;
						break;
			case 1: 
					if(ball.r>0.5){
						result=centroCampistaD;
					}else
						result=kickspot;
					break;
			case 2:  
					if(ball.r>0.5){
						result=centroCampistaD;
					}else
						result=kickspot;
						break;
			case 3: 
					if(ball.r>0.5){
						result=ataqueI;
			}else
					result=kickspot;
			break;
			case 4:  
					if(ball.r>0.5){
						result=centroCampista;
			}else
					result=kickspot;
			break;
			case 5:
					if(ball.r>0.5){
						result=defensaD;
			}else
					result=kickspot;
			break;
			case 6: 
					if(ball.r>0.5){
						result=defensa;
					}else
						result=kickspot;
			break;
			case 7:  
					if(ball.r>0.5){
						result=defensaI;
					}else
						result=kickspot;
						break;
			case 8:  
					if(ball.r>0.5){
						result=defensaD;
					}else
					result=kickspot;
					break;
			case 9:  
					if(ball.r>0.5){
						result=centroCampistaI;
					}else
					result=kickspot;
					break;
			}
			}

		else if (mynum == 3)
			{
			switch (estrategia) {
			case 0: 
					if(ball.r>0.5){
						result=puntaI;
					}else
						result=kickspot;
						break;
			case 1: 
					if(ball.r>0.5){
						result=ataqueI;
					}else
						result=kickspot;
					break;
			case 2:  
					if(ball.r>0.5){
						result=ataqueI;
					}else
						result=kickspot;
						break;
			case 3: 
					if(ball.r>0.5){
						result=ataque;
			}else
					result=kickspot;
			break;
			case 4:  
					if(ball.r>0.5){
						result=ataqueI;
			}else
					result=kickspot;
			break;
			case 5:
					if(ball.r>0.5){
						result=centroCampista;
			}else
					result=kickspot;
			break;
			case 6: 
					if(ball.r>0.5){
						result=defensaD;
					}else
						result=kickspot;
			break;
			case 7:  
					if(ball.r>0.5){
						result=defensaD;
					}else
						result=kickspot;
						break;
			case 8:  
					if(ball.r>0.5){
						result=centroCampistaI;
					}else
					result=kickspot;
					break;
			case 9:  
					if(ball.r>0.5){
						result=centroCampista;
					}else
					result=kickspot;
					break;
			}
			}

		/*---Lead Forward ---*/
		else if (mynum == 4)
			{
			switch (estrategia) {
			case 0: 
					if(ball.r>0.5){
						result=puntaD;
					}else
						result=kickspot;
						break;
			case 1: 
					if(ball.r>0.5){
						result=ataqueD;
					}else
						result=kickspot;
					break;
			case 2:  
					if(ball.r>0.5){
						result=ataqueD;
					}else
						result=kickspot;
						break;
			case 3: 
					if(ball.r>0.5){
						result=ataqueD;
			}else
					result=kickspot;
			break;
			case 4:  
					if(ball.r>0.5){
						result=ataqueD;
			}else
					result=kickspot;
			break;
			case 5:
					if(ball.r>0.5){
						result=ataque;
			}else
					result=kickspot;
			break;
			case 6: 
					if(ball.r>0.5){
						result=centroCampista;
					}else
						result=kickspot;
			break;
			case 7:  
					if(ball.r>0.5){
						result=defensaD;
					}else
						result=kickspot;
						break;
			case 8:  
					if(ball.r>0.5){
						result=centroCampistaD;
					}else
					result=kickspot;
					break;
			case 9:  
					if(ball.r>0.5){
						result=centroCampistaD;
					}else
					result=kickspot;
					break;
			}
			}
		
		//Miramos si con la estrategia que tenemos se marca o no un gol
		// y en que campo para añadir esta informacion al caso antes de añadirle a la base de casos
		if(mynum==0){
			if(abstract_robot.getJustScored(curr_time)==1){
				trainer.gol++;
			}else if(abstract_robot.getJustScored(curr_time)==-1){
				trainer.gol--;
			}
			if((curr_time+20)%2000==0){
			//añadimos el resultado
			trainer.aniadeResultado(trainer.gol);
			//añadir caso a base de casos
			//y la base de casos pasaral a archivo
			trainer.actualizaBC();		
			}
		}
		/*--- Send commands to actuators ---*/
		// set the heading
		abstract_robot.setSteerHeading(curr_time, result.t);

		// set speed at maximum
		abstract_robot.setSpeed(curr_time, 1.0);

		// kick it if we can
		if (abstract_robot.canKick(curr_time))
			abstract_robot.kick(curr_time);

		// tell the parent we're OK  
		return(CSSTAT_OK);
		}
	}