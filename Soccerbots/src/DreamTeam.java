/*
 * DreamTeam.java
 * --------------
 * Carlos Olivar Escaja
 * David Fernández Ortega
 * Álvaro de los Reyes Guzmán
 */

import	EDU.gatech.cc.is.util.Vec2;
import	EDU.gatech.cc.is.abstractrobot.*;


/**
 * Ejemplo de equipo de reglas para SoccerBots
 * Este equipo utiliza algunas funciones de BasicTeam para su correcto
 * funcionamiento.
 */
public class DreamTeam extends ControlSystemSS {
	
	/**
	* Configura el sistema. Sirve de inicialización.
	*/
	public void Configure() {
		// no usado en este caso
	}
		
	
	/**
	* Método que se ejcuta cada vez que el sistema nos lo permite.
	* Decide qué debe hacer cada jugador en cada instante de tiempo en función
	* de distintos parámetros
	*/
	public int TakeStep() {
		// Guardamos los resultados
		//Res.Resultado.Guardar(this, abstract_robot);
		
		// Movimiento que vamos a hacer
		Vec2 result = new Vec2(0,0);

		// Obtenemos el tiempo actual
		long curr_time = abstract_robot.getTime();

		/*--- Obtenemos los sensores ---*/
		// Obtenemos el vector de la pelota
		Vec2 ball = abstract_robot.getBall(curr_time);
		
		// Obtenemos el vector de la posición
		Vec2 pos = abstract_robot.getPosition(curr_time);

		// Obtenemos los vectores de las porterías
		Vec2 ourgoal = abstract_robot.getOurGoal(curr_time);
		Vec2 theirgoal = abstract_robot.getOpponentsGoal(curr_time);

		// Obtenemos la lista de las posiciones de nuestros compañeros
		Vec2[] teammates = abstract_robot.getTeammates(curr_time);

		// Buscamos el compañero más cercano
		Vec2 closestteammate = new Vec2(99999,0);
		for (int i=0; i< teammates.length; i++) {
			if (teammates[i].r < closestteammate.r)
				closestteammate = teammates[i];
		}


		/*--- Estrategias ---*/
		// Zona de tiro
		Vec2 kickspot = new Vec2(ball.x, ball.y);
		kickspot.sub(theirgoal);
		kickspot.setr(abstract_robot.RADIUS);
		kickspot.add(ball);

		// Dirección para alejarse al compañero más cercano
		Vec2 awayfromclosest = new Vec2(closestteammate.x,
				closestteammate.y);
		awayfromclosest.sett(awayfromclosest.t + Math.PI);


		/*--- Número que nos indica la posición del jugador ---*/
		int mynum = abstract_robot.getPlayerNumber(curr_time);
		
		boolean equipoOeste = false;
		
		if (ourgoal.x < 0) {
			equipoOeste = true;
		}
		
		boolean chutar = false;
		
		                                       
		switch(mynum) {
		case 0: /*--- Portero ---*/
			/* 
			 * El portero nunca sale de la portería y lo único que hace es 
			 * mantenerse en ella y moverse hacia arriba o hacia abajo según la
			 * posición de la pelota
			 */
			result = ourgoal;
			if (ourgoal.y < 0.25 && ourgoal.y > -0.25) {
				result.sety(ball.y);	
			}
			
			chutar = true;
			break;
		
		case 1: /*--- Defensa ---*/
			/* 
			 * El defansa no pasa del centro del campo a no ser que la
			 * pelota esté muy cerca y defienda más allá del centro del campo
			 * o incluso suba a atacar
			 */
			if (equipoOeste) {
					if (pos.x  >= -0.1 && ball.r > 0.25) {
						result = ourgoal;
						result.sety(ball.y);
					}
					else {
						result = kickspot;
					}	
				}
				else {
					if (pos.x  <= 0.1 && ball.r > 0.25) {
						result = ourgoal;
						result.sety(ball.y);
					}
					else {
						result = kickspot;
					}
				}
			break;

		case 2: /*--- Carrilero norte ---*/
			/* 
			 * Este jugador siempre está en la zona norte del campo, a no ser que la
			 * pelota se encuentre en el centro (en cuanto a las x) y él esté muy
			 * cerca de la pelota.
			 * Si hay cerca un compañero intenta alejarse 
			 */
			if (closestteammate.r < 0.22) {
				result = awayfromclosest;
			}
			else {
				if (pos.y > 0) {
					result = kickspot;
				}
				else {  
					result.setx(ball.x);
					result.sety(pos.y + 1);
				}
			}
			if (theirgoal.r < 0.6) {
				chutar = true;
			}
			
			break;

		case 4: /*--- Carrilero sur ---*/
			/* 
			 * Este jugador siempre está en la zona sur del campo, a no ser que la
			 * pelota se encuentre en el centro (en cuanto a las x) y él esté muy
			 * cerca de la pelota.
			 * Si hay cerca un compañero intenta alejarse 
			 */
			if (closestteammate.r < 0.22) {
				result = awayfromclosest;
			}
			else {
				if (pos.y < 0) {
					result = kickspot;
				}
				else {  
					result.setx(ball.x);
					result.sety(pos.y - 1);
				}	
			}
			if (theirgoal.r < 0.6) {
				chutar = true;
			}
			
			break;

		case 3: /*--- Delantero ---*/
			/* 
			 * El delantero sólo pasa baja a su campo cuando está muy cerca de la
			 * pelota y ésta está cerca de su campo. Si está lejos de la pelota vuelve
			 * al campo del equipo contrario 
			 */
			if (equipoOeste) {
				if (pos.x > 0) {
					if (ball.r > 0.25) {
						result = kickspot;
					}
					else {
						result = ball;
					}
				}
				else {
					result.sety(ball.y);
					result.setx(pos.x + 1);
				}
			}
			else {
				if (pos.x < 0) {
					if (ball.r > 0.25) {
						result = kickspot;
					}
					else {
						result = ball;
					}
				}
				else {
					result.sety(ball.y);
					result.setx(pos.x - 1);
				}
			}
			if (theirgoal.r < 0.6) {
				chutar = true;
			}
			break;
		default:
			result = ball;
		}

		/*--- Envío de comandos y actuadores ---*/

		abstract_robot.setSteerHeading(curr_time, result.t);
		
		// chuta si se puede
		if (abstract_robot.canKick(curr_time) && chutar)
				abstract_robot.kick(curr_time);
				
		// fija la velocidad al máximo
		abstract_robot.setSpeed(curr_time, 1.0);
		
		// devuelve que todo ha ido bien
		return(CSSTAT_OK);
	}
}

