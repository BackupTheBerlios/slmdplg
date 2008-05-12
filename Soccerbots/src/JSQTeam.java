// CARPETA RES para guardar datos

/*
 * JSQTeam.java
 * Jose -
 * Sergio -
 * Quique -
 * Team
 *
 * Desarrollado a partir de la clase BasicTeam para los movimientos
 * y de la clase CommTeam para las comunicaciones del Entrenador 
 * con todos los jugadores.
 * De la clase BriSpec he cogido la información para saber si un jugador 
 * es un defensa o es un delantero.
 * Para mejorar las ultimas versiones se han usado ideas sacadas de las
 * clases DTeam, AIKHomoG y Kechze
 */

import	EDU.gatech.cc.is.util.Vec2;
import	EDU.gatech.cc.is.abstractrobot.*;
//import 	EntrenadorJSQTeam;
import java.lang.Integer;
import	java.lang.Math;
// imports para las comunicaciones
import	java.util.Enumeration;
import	EDU.gatech.cc.is.communication.*;
//Clay not used

/**
 * <p>Title: JSQTeam</p>
 * <p>Description: Equipo realizado por Jose, Sergio y Quique para el
 * campeonato de SoccerBots</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * @author José María Sobrinos García, Sergio Díaz Jubera, Enrique Aguilera
 * @version 1.0
 */


public class JSQTeam extends ControlSystemSS
{
	// CONSTANTES -------------------------------------------------------------
	// ------------------------------------------------------------------------
	//
	// indica si se desean mostrar mensajes por pantalla o no de 
	// información del sistema (com las comunicaciones broadcast y la 
	// posicion de determinados elementos en la pantalla)
	public static final boolean MOSTRAR_MENSAJES = false;
	
	// ATRIBUTOS -------------------------------------------------------------
	// ------------------------------------------------------------------------
	//
	// táctica que está jugando el equipo. Por defecto la táctica es 2 def 2 del.
	private static int tactica = EntrenadorJSQTeam.TACTICA22;
	// marcajes que está realizando el equipo. Por defecto la táctica es 
	// el marcaje en zona por parte de todos los miembros del equipo
	private static int marcaje = EntrenadorJSQTeam.MARCAJECC0Z4;
	//
	// el EntrenadorJSQTeam es creado por el jugador portero (identificador = 0)
	// y este se encarga de mirar a ver lo que ha decidido el entrenador 
	// (hacerle que piense) en cada turno de juego
	private static EntrenadorJSQTeam entrenador = null;
	//
	// identificador del jugador
	private static int mynum = -1;
	//
	// tacticas validas
	private static int[] tacticasValidas = {
				EntrenadorJSQTeam.TACTICA40,
				EntrenadorJSQTeam.TACTICA31,
				EntrenadorJSQTeam.TACTICA22,
				EntrenadorJSQTeam.TACTICA13,
				EntrenadorJSQTeam.TACTICA04};
	//
	// marcajes válidos
	private static int[] marcajesValidos = {
				EntrenadorJSQTeam.MARCAJECC0Z4,
				EntrenadorJSQTeam.MARCAJECC1Z3,
				EntrenadorJSQTeam.MARCAJECC2Z2,
				EntrenadorJSQTeam.MARCAJECC3Z1,
				EntrenadorJSQTeam.MARCAJECC4Z0};

	// posiciones relacionadas con la táctica que pueden adoptar los jugadores
	private static Vec2 def0 = null;	// posicion defensiva en el centro de la defensa
	private static Vec2 def1 = null;	// posicion defensiva en el banda izquierda de la defensa (arriba en lado izquierdo)
	private static Vec2 def2 = null;	// posicion defensiva en el banda derecha de la defensa (abajo en lado izquierdo)
	private static Vec2 def3 = null;	// posicion defensiva adelantada de la defensa
	private static Vec2 def4 = null;	// posicion defensiva de dos defensas banda izquierda
	private static Vec2 def5 = null;	// posicion defensiva de dos defensas banda derecha
	private static Vec2 at0 = null;	// posicion ofensiva en el centro del ataque
	private static Vec2 at1 = null;	// posicion ofensiva en el banda izquierda del ataque (arriba en lado izquierdo)
	private static Vec2 at2 = null;	// posicion ofensiva en el banda derecha del ataque (abajo en lado izquierdo)
	private static Vec2 at3 = null;	// posicion ofensiva retrasada del ataque
	private static Vec2 at4 = null;	// posicion ofensiva de dos delanteros banda izquierda
	private static Vec2 at5 = null;	// posicion ofensiva de dos delanteros banda derecha



	/**
	 * This enumeration buffers incoming messages.
	 */
	private Enumeration messagesin;	//COMMUNICATION	
	
  /**
   Configure the control system.  This method is
   called once at initialization time.  You can use it
   to do whatever you like.
   */
  public void Configure()
  {
  	// get the current time for timestamps
  	long	curr_time = abstract_robot.getTime();

    // obtener el número del identificador del jugador
  	mynum = abstract_robot.getPlayerNumber(curr_time);
  	if (mynum==0){
  		if (entrenador == null)
  			entrenador = new EntrenadorJSQTeam(this);
  		}
  	
  	/*--- Instantiate the message buffer ----*/
		messagesin = abstract_robot.getReceiveChannel();//COMMUNICATION
		
	// inicializamos las posiciones de referencia para las tácticas
	Vec2 theirgoal = abstract_robot.getOpponentsGoal(curr_time);
        if (theirgoal.x < 0.0) {
        	// porteria en el lado derecho
			def0 = new Vec2(0.8,0.0);
			def1 = new Vec2(0.6,0.6);
			def2 = new Vec2(0.6,-0.6);
			def3 = new Vec2(0.2,0.2);
			def4 = new Vec2(0.9,0.3);
			def5 = new Vec2(0.9,-0.3);
			at0 = new Vec2(-0.8,0.0);
			at1 = new Vec2(-0.6,0.6);
			at2 = new Vec2(-0.6,-0.6);
			at3 = new Vec2(0.2,-0.2);
			at4 = new Vec2(-0.9,0.3);
			at5 = new Vec2(-0.9,-0.3);
		}
		else{
			// porteria en el lado izquierdo
			def0 = new Vec2(-1.4,0.0);
			def1 = new Vec2(-1.1,0.5);
			def2 = new Vec2(-1.1,-0.5);
			def3 = new Vec2(-0.4,0.0);
			def4 = new Vec2(-1.0,0.3);
			def5 = new Vec2(-1.0,-0.3);
			at0 = new Vec2(1.4,0.0);
			at1 = new Vec2(1.1,0.5);
			at2 = new Vec2(1.1,-0.5);
			at3 = new Vec2(-0.2,0.1);
			at4 = new Vec2(0.4,0.3);
			at5 = new Vec2(0.4,-0.3);
			}
  } // fin Configure


  /**
   Called every timestep to allow the control system to
   run.
   */
  public int TakeStep()
  {
  // para guardar el resultado
  //Res.Resultado.Guardar(this,abstract_robot);

  // the eventual movement command is placed here
  Vec2	result = new Vec2(0,0);
	
	// para guardar el resultado
	//Res.Resultado.Guardar(this,abstract_robot);
	
	// SENSORES -----------------------------------------------------------
	// --------------------------------------------------------------------
	
  // get the current time for timestamps
  long	curr_time = abstract_robot.getTime();
  
  // distancia al area
  Vec2 dist_area = null;
  
  /*--- Get some sensor data ---*/
  // get vector to the ball
  Vec2 ball = abstract_robot.getBall(curr_time);

  // get vector to our and their goal
  Vec2 ourgoal = abstract_robot.getOurGoal(curr_time);
  Vec2 theirgoal = abstract_robot.getOpponentsGoal(curr_time);

  // get a list of the positions of our teammates
  Vec2[] teammates = abstract_robot.getTeammates(curr_time);

  /*--- check get opponents routine ---*/
  Vec2[] opponents = abstract_robot.getOpponents(curr_time);

  // find the closest teammate
  Vec2 closestteammate = new Vec2(99999,0);
  for (int i=0; i< teammates.length; i++)
  {
    if (teammates[i].r < closestteammate.r)
      closestteammate = teammates[i];
  }

  /*--- now compute some strategic places to go ---*/
  // compute a point one robot radius
  // behind the ball.
  Vec2 kickspot = new Vec2(ball.x, ball.y);
  kickspot.sub(theirgoal);
  kickspot.setr(abstract_robot.RADIUS);
  kickspot.add(ball);

  // compute a point three robot radii
  // behind the ball.
  Vec2 backspot = new Vec2(ball.x, ball.y);
  backspot.sub(theirgoal);
  backspot.setr(abstract_robot.RADIUS*5);
  backspot.add(ball);

  // compute a north and south spot
  Vec2 northspot = new Vec2(backspot.x,backspot.y+0.7);
  Vec2 southspot = new Vec2(backspot.x,backspot.y-0.7);

  // compute a position between the ball and defended goal
  Vec2 goaliepos = new Vec2(ourgoal.x + ball.x,
                            ourgoal.y + ball.y);
  goaliepos.setr(goaliepos.r*0.5);

  // a direction away from the closest teammate.
  Vec2 awayfromclosest = new Vec2(closestteammate.x,
                                  closestteammate.y);
  awayfromclosest.sett(awayfromclosest.t + Math.PI);
  
  // obtenemos las posiciones de los rivales para transmitirselas al 
  // entrenador y pueda pensar la tactica siguiente
  //Vec2[] opponents = abstract_robot.getOpponents(curr_time);
  
  // SACADOS DE BRISPEC.JAVA
  Vec2 me, ego_theirgoal,ego_opponents[];
  // get a list of the positions of our ego_teammates
  ego_opponents = abstract_robot.getOpponents(curr_time);
	//should create non-egos, too
  me = abstract_robot.getPosition(curr_time);
  ego_theirgoal = abstract_robot.getOpponentsGoal(curr_time);
  theirgoal = new Vec2( ego_theirgoal.x, ego_theirgoal.y );
  theirgoal.add( me );
  // distancia jugador al area del portero
  dist_area = new Vec2(me.x,me.y);
  dist_area.sub(ourgoal);
  
  // contiene información sobre los puestos
  int[] puesto = {-1,-1,-1,-1};
  for (int i=1;i<opponents.length;i++)
  {
  	// no contamos al portero para mayor eficiencia
    if( ((theirgoal.x>0) && (opponents[i].x<0)) || 
             ((theirgoal.x<=0) && (opponents[i].x>0))){
  		puesto[i-1] = 0;
  		// es delantero  
    }
    else {
    	puesto[i-1] = 1;
    	// es defensa
    	}
	}
	
	// EL ENTRENADOR PIENSA LA SIGUIENTE JUGADA DEL EQUIPO
	if (mynum == 0 && entrenador != null){
		entrenador.siguienteDecision(puesto);
		}
	
	// SI SE HA MARCADO GOL ENTONCES LLAMAMOS AL ENTRENADOR PARA QUE ACTUALICE
	// LAS TÁCTICAS
	int golMetido = abstract_robot.getJustScored(curr_time);
	if (golMetido!=0)
		entrenador.intentaAprender(golMetido);

	// COMUNICACIONES --------------------------------------------------
	// -----------------------------------------------------------------
	/*--- Look for incoming messages ---*/
	while (messagesin.hasMoreElements())
		{
		StringMessage recvd = 
				(StringMessage) messagesin.nextElement();
		if (MOSTRAR_MENSAJES){
			System.out.println(mynum + " received:\n" + recvd);
			}
		String mens = recvd.val;
		// comprobamos si es un mensjae válido de táctica y marcajes del capitan
		// y si lo es actualizamos la táctica o el marcaje segun corresponda
		boolean encontradoTact = false, encontradoMar = false;
		for (int j = 0; j < 5; j++){
			if (Integer.parseInt(mens) == tacticasValidas[j] && !encontradoTact){
				tactica = tacticasValidas[j];
				encontradoTact = true;
				}
			if (Integer.parseInt(mens) == marcajesValidos[j] && !encontradoMar)
				marcaje = marcajesValidos[j];
				encontradoMar = true;
			}
		}

	// ACCIONES A REALIZAR -----------------------------------------------
	// -------------------------------------------------------------------
  /*--- go to one of the places depending on player num ---*/
  mynum = abstract_robot.getPlayerNumber(curr_time);
 
  opponents = abstract_robot.getOpponents(curr_time);
  /*--- Goalie ---*/
  if (mynum == 0)
  {
  	// si el portero está demasiado lejos de su porteria 
  	// entonces debe volver a su porteria
  	//if (dist_area.r > 0.7)
  	//	result = ourgoal;
    // sino acercarse a la pelota si está lejos
    if (ball.r > 0.5)
      result = goaliepos;
    // si está cerca darle una patada
    else if (ball.r > 0.1)
      result = kickspot;
    else
      result = goaliepos;
    // apartarse de los demás compañeros
    if (closestteammate.r < 0.3)
    {
      result = awayfromclosest;
    }
  }

  /*--- midback ---*/
  else if (mynum == 1)
  {
  	Vec2 porDefecto = null;
  	switch (tactica){
		case EntrenadorJSQTeam.TACTICA40: porDefecto = def0;
			break;
		case EntrenadorJSQTeam.TACTICA31: porDefecto = def0;
			break;
		case EntrenadorJSQTeam.TACTICA22: porDefecto = def4;
			break;
		case EntrenadorJSQTeam.TACTICA13: porDefecto = def0;
			break;
		case EntrenadorJSQTeam.TACTICA04: porDefecto = at3;
			break;
				} // switch   		
  	if ((marcaje != EntrenadorJSQTeam.MARCAJECC0Z4) && (opponents.length >=5))
  		// si tenemos que marcar al jugador 4
  		if (opponents[4] != null)
  			result = opponents[4];
  		else 
  			result = porDefecto;
  	// si no tenemos que marcar a nadie
  	else if (tactica == EntrenadorJSQTeam.TACTICA04){
  		// si nos tenemos que comportar como delanteros
  		
  		if (((theirgoal.x > 0.0) && (me.x < 0.0))||
  		((theirgoal.x <= 0.0) && (me.x >= 0.0)))
  		// si nos tenemos que comportar como delanteros
  		// si nos hemos salido del area de los de los delanteros
  		// 	volver al area de los delanteros
  		result = porDefecto;
  		else if(ball.r > 0.5)
  			// si el balon está en el area de los delanteros
  			// y esta lejos entocnes correr tras el
  			result = backspot;
  		else if (ball.r > 0.30)
  			// si está cerca entonces disponerse a chutar
  			result = kickspot;
    	else
      		result = ball;
    	// keep away from others
    	if (closestteammate.r < 0.3)
    	{
      	result = awayfromclosest;
    	}
  	} // fin comportarse como delanteros
  	else if (((theirgoal.x > 0.0) && (me.x > 0.0))||
  		((theirgoal.x <= 0.0) && (me.x < 0.0)))
  		// si nos tenemos que comportar como defensas
  		// si nos hemos salido del areade los defensas
  		// 	volver al area de los defensas
  		result = porDefecto;
  	else if (((theirgoal.x > 0.0) && !(ball.x > 0.0))||
  		((theirgoal.x <= 0.0) && !(ball.x < 0.0))) {
  		// si estamos en el area de los defensas 
  		// y el balon no está en el area de los defensas
  		// entonces debemos ir a la posicion por defecto
  		result = porDefecto;
  	}
  	else if(ball.r > 0.5)
  		// si el balon está en el area de los defensas
  		// y esta lejos entocnes correr tras el
  		result = backspot;
  	else if (ball.r > 0.30)
  		// si está cerca entonces disponerse a chutar
  		result = kickspot;
    else
      result = ball;
    // keep away from others
    if (closestteammate.r < 0.3)
    {
      result = awayfromclosest;
    }
}

  if (mynum == 2)
  {
  	Vec2 porDefecto = null;
  	switch (tactica){
		case EntrenadorJSQTeam.TACTICA40: porDefecto = def1;
			break;
		case EntrenadorJSQTeam.TACTICA31: porDefecto = def1;
			break;
		case EntrenadorJSQTeam.TACTICA22: porDefecto = def5;
			break;
		case EntrenadorJSQTeam.TACTICA13: porDefecto = at2;
			break;
		case EntrenadorJSQTeam.TACTICA04: porDefecto = at2;
			break;
				} // switch   		
  	if ((marcaje != EntrenadorJSQTeam.MARCAJECC0Z4)&& (opponents.length >=4)){
  		// si tenemos que marcar al jugador 3
  		if (opponents[3] !=null)
  			result = opponents[3];
  		else 
  			result = porDefecto;}
  	// si no tenemos que marcar a nadie
  	else if (tactica == EntrenadorJSQTeam.TACTICA04){
  		// si nos tenemos que comportar como delanteros
  		
  		if (((theirgoal.x > 0.0) && (me.x < 0.0))||
  		((theirgoal.x <= 0.0) && (me.x >= 0.0)))
  		// si nos tenemos que comportar como delanteros
  		// si nos hemos salido del area de los de los delanteros
  		// 	volver al area de los delanteros
  		result = porDefecto;
  		else if(ball.r > 0.5)
  			// si el balon está en el area de los delanteros
  			// y esta lejos entocnes correr tras el
  			result = backspot;
  		else if (ball.r > 0.30)
  			// si está cerca entonces disponerse a chutar
  			result = kickspot;
    	else
      		result = ball;
    	// keep away from others
    	if (closestteammate.r < 0.3)
    	{
      	result = awayfromclosest;
    	}
  	} // fin comportarse como delanteros
  	else if (((theirgoal.x > 0.0) && (me.x > 0.0))||
  		((theirgoal.x <= 0.0) && (me.x < 0.0)))
  		// si nos tenemos que comportar como defensas
  		// si nos hemos salido del areade los defensas
  		// 	volver al area de los defensas
  		result = porDefecto;
  	else if (((theirgoal.x > 0.0) && !(ball.x > 0.0))||
  		((theirgoal.x <= 0.0) && !(ball.x < 0.0))) {
  		// si estamos en el area de los defensas 
  		// y el balon no está en el area de los defensas
  		// entonces debemos ir a la posicion por defecto
  		result = porDefecto;
  	}
  	else if(ball.r > 0.5)
  		// si el balon está en el area de los defensas
  		// y esta lejos entocnes correr tras el
  		result = backspot;
  	else if (ball.r > 0.30)
  		// si está cerca entonces disponerse a chutar
  		result = kickspot;
    else
      result = ball;
    // keep away from others
    if (closestteammate.r < 0.3)
    {
      result = awayfromclosest;
    }
  }

  if (mynum == 4)
  {
    Vec2 porDefecto = null;
  	switch (tactica){
		case EntrenadorJSQTeam.TACTICA40: porDefecto = def2;
			break;
		case EntrenadorJSQTeam.TACTICA31: porDefecto = def2;
			break;
		case EntrenadorJSQTeam.TACTICA22: porDefecto = at1;
			break;
		case EntrenadorJSQTeam.TACTICA13: porDefecto = at1;
			break;
		case EntrenadorJSQTeam.TACTICA04: porDefecto = at1;
			break;
				} // switch   		
  	if ((marcaje != EntrenadorJSQTeam.MARCAJECC0Z4)&& (opponents.length >=3)){
  		// si tenemos que marcar al jugador 2
  		if (opponents[2] !=null)
  			result = opponents[2];
  		else 
  			result = porDefecto;}
  	// si no tenemos que marcar a nadie
  	else if (tactica == EntrenadorJSQTeam.TACTICA04){
  		// si nos tenemos que comportar como delanteros
  		
  		if (((theirgoal.x > 0.0) && (me.x < 0.0))||
  		((theirgoal.x <= 0.0) && (me.x >= 0.0)))
  		// si nos tenemos que comportar como delanteros
  		// si nos hemos salido del area de los de los delanteros
  		// 	volver al area de los delanteros
  		result = porDefecto;
  		else if(ball.r > 0.5)
  			// si el balon está en el area de los delanteros
  			// y esta lejos entocnes correr tras el
  			result = backspot;
  		else if (ball.r > 0.30)
  			// si está cerca entonces disponerse a chutar
  			result = kickspot;
    	else
      		result = ball;
    	// keep away from others
    	if (closestteammate.r < 0.3)
    	{
      	result = awayfromclosest;
    	}
  	} // fin comportarse como delanteros
  	else if (((theirgoal.x > 0.0) && (me.x > 0.0))||
  		((theirgoal.x <= 0.0) && (me.x < 0.0)))
  		// si nos tenemos que comportar como defensas
  		// si nos hemos salido del areade los defensas
  		// 	volver al area de los defensas
  		result = porDefecto;
  	else if (((theirgoal.x > 0.0) && !(ball.x > 0.0))||
  		((theirgoal.x <= 0.0) && !(ball.x < 0.0))) {
  		// si estamos en el area de los defensas 
  		// y el balon no está en el area de los defensas
  		// entonces debemos ir a la posicion por defecto
  		result = porDefecto;
  	}
  	else if(ball.r > 0.5)
  		// si el balon está en el area de los defensas
  		// y esta lejos entocnes correr tras el
  		result = backspot;
  	else if (ball.r > 0.30)
  		// si está cerca entonces disponerse a chutar
  		result = kickspot;
    else
      result = ball;
    // keep away from others
    if (closestteammate.r < 0.3)
    {
      result = awayfromclosest;
    }
  }

  if (mynum == 3)
  {
    Vec2 porDefecto = null;
  	switch (tactica){
		case EntrenadorJSQTeam.TACTICA40: porDefecto = def3;
			break;
		case EntrenadorJSQTeam.TACTICA31: porDefecto = at0;
			break;
		case EntrenadorJSQTeam.TACTICA22: porDefecto = at0;
			break;
		case EntrenadorJSQTeam.TACTICA13: porDefecto = at0;
			break;
		case EntrenadorJSQTeam.TACTICA04: porDefecto = at0;
			break;
				} // switch   		
  	if ((marcaje != EntrenadorJSQTeam.MARCAJECC0Z4)&& (opponents.length >=2)){
  		// si tenemos que marcar al jugador 1
  		if (opponents[1] !=null)
  			result = opponents[1];
  		else 
  			result = porDefecto;}
  	// si no tenemos que marcar a nadie
  	else if (tactica == EntrenadorJSQTeam.TACTICA04){
  		// si nos tenemos que comportar como delanteros
  		
  		if (((theirgoal.x > 0.0) && (me.x < 0.0))||
  		((theirgoal.x <= 0.0) && (me.x >= 0.0)))
  		// si nos tenemos que comportar como delanteros
  		// si nos hemos salido del area de los de los delanteros
  		// 	volver al area de los delanteros
  		result = porDefecto;
  		else if(ball.r > 0.5)
  			// si el balon está en el area de los delanteros
  			// y esta lejos entocnes correr tras el
  			result = backspot;
  		else if (ball.r > 0.30)
  			// si está cerca entonces disponerse a chutar
  			result = kickspot;
    	else
      		result = ball;
    	// keep away from others
    	if (closestteammate.r < 0.3)
    	{
      	result = awayfromclosest;
    	}
  	} // fin comportarse como delanteros
  	else if (((theirgoal.x > 0.0) && (me.x > 0.0))||
  		((theirgoal.x <= 0.0) && (me.x < 0.0)))
  		// si nos tenemos que comportar como defensas
  		// si nos hemos salido del areade los defensas
  		// 	volver al area de los defensas
  		result = porDefecto;
  	else if (((theirgoal.x > 0.0) && !(ball.x > 0.0))||
  		((theirgoal.x <= 0.0) && !(ball.x < 0.0))) {
  		// si estamos en el area de los defensas 
  		// y el balon no está en el area de los defensas
  		// entonces debemos ir a la posicion por defecto
  		result = porDefecto;
  	}
  	else if(ball.r > 0.5)
  		// si el balon está en el area de los defensas
  		// y esta lejos entocnes correr tras el
  		result = backspot;
  	else if (ball.r > 0.30)
  		// si está cerca entonces disponerse a chutar
  		result = kickspot;
    else
      result = ball;
    // keep away from others
    if (closestteammate.r < 0.3)
    {
      result = awayfromclosest;
    }
  }

	// ACTUADORES -------------------------------------------------------
	// ------------------------------------------------------------------

  /*--- Send commands to actuators ---*/
  // set the heading
  if ((mynum==0)&&( (Math.abs(me.x) < 1.0) ||
	(Math.abs(me.y) > 0.5) ))
  	abstract_robot.setSteerHeading(curr_time, ourgoal.t);
  else
  	abstract_robot.setSteerHeading(curr_time, result.t);
	if (MOSTRAR_MENSAJES){
		System.out.println("x= " + me.x + ", y= " + me.y );	
	}
  // set speed at maximum
  abstract_robot.setSpeed(curr_time, 1.0);

  // kick it if we can
  Vec2 tiro = theirgoal;
  tiro.sub(me);
  Vec2 pase = closestteammate;
  pase.sub(me);
  
  if (abstract_robot.canKick(curr_time)&& ((tiro.r < 1.0)
  	|| (pase.r <3.0)))
    abstract_robot.kick(curr_time);

  // tell the parent we're OK
  return(CSSTAT_OK);
  }
  
  /**
   * transmite a los demás compañeros del equipo las ordenes que le 
   * ha dado el entrenador
   */
  public void ordenesDelEntrenador(int tacticaActual, int marcajeActual){
  	if (mynum == 0){
  	// actualizamos la tacticaActual y el marcaje Actual del capitan
  	tactica = tacticaActual;
  	marcaje = marcajeActual;
  	// mandamos un mensaje al resto del equipo para que empiecen a jugar 
  	// con la nueva tactica y marcaje
  	
  	// creamos el mensaje
  	StringMessage m = new StringMessage();
  	/*for (int i = 1; i < 5; i++){
			m = new StringMessage();
			m.val = (new Integer(tacticaActual)).toString();
			// enviar el mensaje al robot i (i distinto del capitan) 
			try{
				abstract_robot.unicast(i,m);
				} catch(CommunicationException e){}
  	} // for tactica
  	for (int i = 1; i < 5; i++){
			m = new StringMessage();
			m.val = (new Integer(marcajeActual)).toString();
			// enviar el mensaje al robot i (i distinto del capitan) 
			try{
				abstract_robot.unicast(i,m);
				} catch(CommunicationException e){}
  	} // for marcaje
  	*/
  	// alternativa con broadcast
  	m = new StringMessage();
		m.val = (new Integer(tacticaActual)).toString();
		try{
				abstract_robot.broadcast(m);
		} catch (Exception e){}
		m = new StringMessage();
		m.val = (new Integer(marcajeActual)).toString();
		try{
				abstract_robot.broadcast(m);
		} catch (Exception e){}
  	} // if
  } // fin ordenesDelEntrenador
}
