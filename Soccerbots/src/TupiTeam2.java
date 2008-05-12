import java.util.Enumeration;

import	EDU.gatech.cc.is.util.Vec2;
import	EDU.gatech.cc.is.abstractrobot.*;
import EDU.gatech.cc.is.communication.CommunicationException;
import EDU.gatech.cc.is.communication.StringMessage;



public class TupiTeam2 extends ControlSystemSS
	{
	private long	tiempo_actual;	
	private long	miNumero;		//Numero del jugador
	private double	miDireccion;	//Direccion hacia la que estoy mirando

	private Vec2	pelota;			//Donde esta la pelota
	private Vec2	miPorteria;
	private Vec2	suPorteria;
	private Vec2[]	compis;			//Donde estan mis compañeros de equipo
	private Vec2[]	enemigos;		//Donde estan los oponentes
	private int misGoles;			
	private int susGoles;

	private Vec2	compi_cercano;		//Mi compañero mas cercano
	private Vec2	enemigo_cercano;	//mi enemigo mas cercano
	private Vec2	cerca_pelota;		//Miembro de nuestro equipo mas cercano a la bola


	private Vec2	mover;	//mover el jugador hacia la direccion que indique move.t 
	//  con la velocidad que marca move.r
	private boolean	puedo_golpear;	//intenta golpear la pelota

	// lugar del campo en el que estoy
	private static int CUADRANTE;

	//un vector apuntandome a mi
	private static final Vec2 YO = new Vec2(0,0);
	
	//Radio de actuación del jugador
	private final double ROBOT_RADIUS = abstract_robot.RADIUS;
	
	private Entrenador ent;
	private Enumeration canalOrdenes;
	private int ordenesEntrenador;
	private boolean porteroBloqueado;
	
	/*
	 Configura el sistema de control. Este metodo se lanza
	 una vez en tiempo de inicializacion.
	 Lo vamos a utilizar para colocar a nuestros jugadores en
	 el campo.
	  - Que miren a la porteria contraria. 
	 */
	public void Configure()
		{
		porteroBloqueado=false;
		ent=new Entrenador();
		
		//Inicializamos el cuadrante del campo en el que estamos
		tiempo_actual = abstract_robot.getTime();
		//CUADRANTE = -1 es que estamos a la izquierda
		//CUADRANTE = 1 es que estamos a la derecha
		if(abstract_robot.getOurGoal(tiempo_actual).x < 0)
			CUADRANTE = -1;
		else
			CUADRANTE = 1;

		//se inicializa el vector mover aunque ahora mismo no vale para nada
		mover=new Vec2(0,0);
		canalOrdenes=abstract_robot.getReceiveChannel();
		
		//inicializamos el marcador 0 - 0
		misGoles = 0; 
		susGoles = 0;
		}
		
	public int TakeStep(){
		
		ent.captarEntorno();
		ent.definirEstrategia();
		
		StringMessage recvd=new StringMessage();
		while(canalOrdenes.hasMoreElements())
		{
			recvd =(StringMessage)canalOrdenes.nextElement();
			System.out.println(abstract_robot.getID() + " received:\n" + recvd);
		}
		/**
		 * Ordenes del entrenador
		 * 10 -> Portero
		 * 11 -> Defensa
		 * 12 -> CentroCampista
		 * 13 -> Delantero
		 * 14 -> Libero
		 **/
		ordenesEntrenador=Integer.parseInt(recvd.val);
		switch(ordenesEntrenador){
		case 10:
			estrategiaPortero();
			break;
		case 11:
			estrategiaDefensa();
			break;
		case 12:
			estrategiaCentrocampista();
			break;
		case 13:
			conducirPelota("Libero");
			break;
		case 14:
			conducirPelota("Libero");
			break;
		case 22:
			estrategiaDefensaArriba();
			break;
		default:
			estrategiaDefensaAbajo();
			break;
				

		}
		
		/* ACTUADORES */
		// Modificamos la dirección del jugador
		abstract_robot.setSteerHeading(tiempo_actual,mover.t);

		// Modificamos la velocidad del jugador
		abstract_robot.setSpeed(tiempo_actual,mover.r);

		// Comprobamos si el jugador está en condiciones de disparar la pelota
		if (puedo_golpear && abstract_robot.canKick(tiempo_actual))
			abstract_robot.kick(tiempo_actual);
		return 0;
	}
	
	
	
	/*
	 Este método calcula qué objeto está más derca del jugador YO.
	 Se usa en el método captarEntorno para que el jugador pueda conocer la situación
	 que le rodea (qué jugadores de mi equipo y del contrario tengo más cerca). 
	 */
	private Vec2 cercanoA(Vec2 pos, Vec2[] objetos)
	{
		double dist = Double.MAX_VALUE; //inicializamos el valor de la distancia al máximo
		Vec2 result = new Vec2(0, 0);
		Vec2 temp = new Vec2(0, 0);
		
		//para cada objeto
		for( int i=0; i < objetos.length; i++)
		{
			// encontrar la distancia del objeto a mi
			temp.sett(objetos[i].t);
			temp.setr(objetos[i].r);
			temp.sub(pos);

			//actualiza la distancia si hemos encontrado un objeto mas cercano
			if(temp.r < dist)
			{
				result = objetos[i];
				dist = temp.r;
			}
		}
		//devolvemos el objeto más cercano a YO
		return result;
	}
	
	
	
	private void estrategiaPortero()
	{
		abstract_robot.setDisplayString("Portero");
		// si la pelota está cerca del portero, intenta despejarla
		if(pelota.x * CUADRANTE > 0)
		{	mover.sett(pelota.t);
			mover.setr(1.0);
			puedo_golpear = true;
		}

		// si la pelota está lejos de la portería o fuera de sus límites ->
		// vuelve al centro de la misma lo antes posible
		else if( (Math.abs(miPorteria.x) > ROBOT_RADIUS * 1.4) ||
			 (Math.abs(miPorteria.y) > ROBOT_RADIUS * 4.25) )
		{	
			mover.sett(miPorteria.t);
			mover.setr( 1.0);
		}

		// si la pelota está detrás de la portería, corre hacia ella e
		// intenta despejarla
		else
		{
			if (pelota.y > 0) mover.sety(7);
			else mover.sety(-7);

			mover.setx((double)CUADRANTE);

			if(Math.abs(pelota.y) < ROBOT_RADIUS * 0.15) mover.setr(0.0);
			else mover.setr(1.0);
		}

		Vec2 aux=cercanoA(YO,enemigos);
		if(aux.r < ROBOT_RADIUS*1.4)
		{
			porteroBloqueado=true;
		}
	}
	
	private void estrategiaDelantero( )
	{
		
		// the other team's goalie is whoever is closest to the goal
		// el portero del equipo enemigo está más cerca de la portería
		Vec2 portero = cercanoA(suPorteria,enemigos);

		// find the point just behind the "goalie" 
		// in the way of their goal 
		suPorteria.sub(portero);
		suPorteria.setr(ROBOT_RADIUS);
		suPorteria.add(portero);

		mover.sett(suPorteria.t);
		mover.setr( 1.0);

		// si el portero del otro equipo es el más cercano 
		//-> lo bloqueamos, si la pelota está lejos de la portería
		if(portero != enemigo_cercano){
			avoidcollision();
			abstract_robot.setDisplayString("D->Buscando");
		}
		else abstract_robot.setDisplayString("D->Bloqueando");
	}
	
	private void estrategiaDefensa()
	{		
		Vec2 defender = new Vec2(0,0);

		// buscamos el centro del campo
		defender = abstract_robot.getPosition(tiempo_actual);
		//definimos la zona de defensa
		if(CUADRANTE>0){
			defender.setx(0.5);
		}
		else{
			defender.setx(-0.5);
		}
		
		// si yo soy el más cercano a la pelota, la conduzco
		if(cerca_pelota == YO) {
			conducirPelota("Def");
		}
		// si hay otro jugador más cerca de la pelota
		else {
			//si estoy fuera de mi sitio y no tengo la pelota
			if((Math.abs(abstract_robot.getPosition(tiempo_actual).x)>0.6)||(Math.abs(abstract_robot.getPosition(tiempo_actual).x)<0.49)){
				abstract_robot.setDisplayString("Def->Posicionando");
				//el jugador está en su campo
				if ((CUADRANTE * abstract_robot.getPosition(tiempo_actual).x > 0) && (Math.abs(abstract_robot.getPosition(tiempo_actual).x)>0.6)){
					mover.sett(suPorteria.t);
					mover.setr(1.0);
				}
				else if (Math.abs(abstract_robot.getPosition(tiempo_actual).x)>0.6){
					mover.sett(miPorteria.t);
					mover.setr(1.0);
				}
				
				else if ((CUADRANTE * abstract_robot.getPosition(tiempo_actual).x > 0) && (Math.abs(abstract_robot.getPosition(tiempo_actual).x)<0.49)){
						mover.sett(miPorteria.t);
						mover.setr(1.0);
					}
				else {
						mover.sett(suPorteria.t);
						mover.setr(-1.0);
				}
				avoidcollision();
			}
			else {
				//patrullo por la zona de defensa
				//no muevo la x y muevo la y hacia la pelota
				if(pelota.y>abstract_robot.getPosition(tiempo_actual).y){
					mover.sett(Math.PI/2);	
				}
				else{
					mover.sett((3/4)*Math.PI);	
				}
				mover.sety(pelota.y);
				mover.setr(1.0);
				Vec2 temp1=new Vec2(YO);
				temp1.sub(enemigo_cercano);
				Vec2 temp2=new Vec2(YO);
				temp2.sub(compi_cercano);
				if(temp2.r<temp1.r){
					//para no chocarme con mis compis
					avoidcollision();
				}
				abstract_robot.setDisplayString("Def->Patrullando");
			}
		}
		
		//System.out.println("Defensa: "+abstract_robot.getPosition(tiempo_actual).toString());
		//System.out.println("Vector defender: "+defender.toString());
	}private void estrategiaDefensaArriba()
	{		
		Vec2 defender = new Vec2(0,0);

		// buscamos el centro del campo
		defender = abstract_robot.getPosition(tiempo_actual);
		//definimos la zona de defensa
		if(CUADRANTE>0){
			defender.setx(0.5);
		}
		else{
			defender.setx(-0.5);
		}
		
		// si yo soy el más cercano a la pelota, la conduzco
		if(cerca_pelota == YO) {
			conducirPelota("Def1");
		}
		// si hay otro jugador más cerca de la pelota
		else {
			//si estoy fuera de mi sitio y no tengo la pelota
			if((Math.abs(abstract_robot.getPosition(tiempo_actual).x)>0.6)||(Math.abs(abstract_robot.getPosition(tiempo_actual).x)<0.49)){
				abstract_robot.setDisplayString("Def1->Posicionando");
				//el jugador está en su campo
				if ((CUADRANTE * abstract_robot.getPosition(tiempo_actual).x > 0) && (Math.abs(abstract_robot.getPosition(tiempo_actual).x)>0.6)){
					Vec2 v=new Vec2(suPorteria);
					v.sub(new Vec2(0,0.4));
					mover.sett(v.t);
					mover.setr(1.0);
				}
				else if (Math.abs(abstract_robot.getPosition(tiempo_actual).x)>0.6){
					Vec2 v=new Vec2(miPorteria);
					v.sub(new Vec2(0,0.4));
					mover.sett(v.t);
					mover.setr(1.0);
				}
				
				else if ((CUADRANTE * abstract_robot.getPosition(tiempo_actual).x > 0) && (Math.abs(abstract_robot.getPosition(tiempo_actual).x)<0.49)){
					Vec2 v=new Vec2(miPorteria);
					v.sub(new Vec2(0,0.4));
					mover.sett(v.t);
					mover.setr(1.0);
				}
				else {
					Vec2 v=new Vec2(suPorteria);
					v.sub(new Vec2(0,0.4));
					mover.sett(v.t);
					mover.setr(-1.0);
				}
				avoidcollision();
			}
			else {
				//patrullo por la zona de defensa
				//no muevo la x y muevo la y hacia la pelota
				if(pelota.y>0){
					if(pelota.y>abstract_robot.getPosition(tiempo_actual).y){
						mover.sett(Math.PI/2);	
					}
					else{
						mover.sett((3/4)*Math.PI);	
					}
					mover.setr(1.0);
				}else{
					mover.setr(0.4);
				}
				Vec2 temp1=new Vec2(YO);
				temp1.sub(enemigo_cercano);
				Vec2 temp2=new Vec2(YO);
				temp2.sub(compi_cercano);
				if(temp2.r<temp1.r){
					//para no chocarme con mis compis
					avoidcollision();
				}
				abstract_robot.setDisplayString("Def1->Patrullando");
			}
		}
		
		//System.out.println("Defensa: "+abstract_robot.getPosition(tiempo_actual).toString());
		//System.out.println("Vector defender: "+defender.toString());
	}
	private void estrategiaDefensaAbajo()
	{		
		Vec2 defender = new Vec2(0,0);

		// buscamos el centro del campo
		defender = abstract_robot.getPosition(tiempo_actual);
		//definimos la zona de defensa
		if(CUADRANTE>0){
			defender.setx(0.5);
		}
		else{
			defender.setx(-0.5);
		}
		
		// si yo soy el más cercano a la pelota, la conduzco
		if(cerca_pelota == YO) {
			conducirPelota("Def2");
		}
		// si hay otro jugador más cerca de la pelota
		else {
			//si estoy fuera de mi sitio y no tengo la pelota
			if((Math.abs(abstract_robot.getPosition(tiempo_actual).x)>0.6)||(Math.abs(abstract_robot.getPosition(tiempo_actual).x)<0.49)){
				abstract_robot.setDisplayString("Def2->Posicionando");
				//el jugador está en su campo
				if ((CUADRANTE * abstract_robot.getPosition(tiempo_actual).x > 0) && (Math.abs(abstract_robot.getPosition(tiempo_actual).x)>0.6)){
					Vec2 v=new Vec2(suPorteria);
					v.sub(new Vec2(0,-0.4));
					mover.sett(v.t);
					mover.setr(1.0);
				}
				else if (Math.abs(abstract_robot.getPosition(tiempo_actual).x)>0.6){
					Vec2 v=new Vec2(miPorteria);
					v.sub(new Vec2(0,-0.4));
					mover.sett(v.t);
					mover.setr(1.0);
				}
				
				else if ((CUADRANTE * abstract_robot.getPosition(tiempo_actual).x > 0) && (Math.abs(abstract_robot.getPosition(tiempo_actual).x)<0.49)){
					Vec2 v=new Vec2(miPorteria);
					v.sub(new Vec2(0,-0.4));
					mover.sett(v.t);
					mover.setr(1.0);
					}
				else {
					Vec2 v=new Vec2(suPorteria);
					v.sub(new Vec2(0,-0.4));
					mover.sett(v.t);
					mover.setr(-1.0);
				}
				avoidcollision();
			}
			else {
				//patrullo por la zona de defensa
				//no muevo la x y muevo la y hacia la pelota
				if(pelota.y<0){
					if(pelota.y>abstract_robot.getPosition(tiempo_actual).y){
						mover.sett(Math.PI/2);	
					}
					else{
						mover.sett((3/4)*Math.PI);	
					}
					mover.setr(1.0);
				}else{
					mover.setr(0.3);
				}
				Vec2 temp1=new Vec2(YO);
				temp1.sub(enemigo_cercano);
				Vec2 temp2=new Vec2(YO);
				temp2.sub(compi_cercano);
				if(temp2.r<temp1.r){
					//para no chocarme con mis compis
					avoidcollision();
				}
				abstract_robot.setDisplayString("Def2->Patrullando");
			}
		}
		
		//System.out.println("Defensa: "+abstract_robot.getPosition(tiempo_actual).toString());
		//System.out.println("Vector defender: "+defender.toString());
	}
		
	private void estrategiaCentrocampista()
	{
		Vec2 centro = new Vec2(0,0);

		// buscamos el centro del campo
		centro = abstract_robot.getPosition(tiempo_actual);
		centro.setr(-centro.r);
		
		// Si mi jugador es el más cercano a la pelota, conduzco la pelota
		if(cerca_pelota == YO) {
			abstract_robot.setDisplayString("CC->Conduciendo");
			conducirPelota("CC");
		}
		// Si hay otro jugador más cercano
		else
		// if i'm not closest to the ball stick around the center
		// and wait for a fast break
		{
			abstract_robot.setDisplayString("CC->Esperando");
			get_behind(centro,suPorteria);
			avoidcollision();
		}
	}
	
	private void conducirPelota(String jug)
	{
		String s=jug;
		s+="->Conduciendo";
		
		abstract_robot.setDisplayString(s);
		// Si estoy detras de la bola la llevo a la porteria
		if(behind_point(pelota,suPorteria) && pelota.t < ROBOT_RADIUS * 4)
		{
			mover.sett(suPorteria.t);
			mover.setr(1.0);

			// if i'm within 15x ROBOT_RADII away from and aiming
			// relatively at the goal try to kick the ball
			if( (Math.abs(miDireccion - suPorteria.t) < Math.PI/8) &&
			    (suPorteria.r < ROBOT_RADIUS * 15))
				{
				puedo_golpear = true;
				abstract_robot.setDisplayString(jug+"->Disparo");
				}
			
		}
		else

		// otherwise get behind the ball and avoid colliding with
		// other players
		{
			abstract_robot.setDisplayString(jug+"->BuscaPelota");
			get_behind(pelota,suPorteria);
			avoidcollision();
		}
		//System.out.println("Conducir pelota: x "+abstract_robot.getPosition(tiempo_actual).x+ " y "+ abstract_robot.getPosition(tiempo_actual).y);
	}
	
	private void get_behind( Vec2 point, Vec2 orient)
	{
		Vec2 behind_point = new Vec2(0,0);
		double behind = 0;
		double point_side = 0;

		// find a vector from the point, away from the orientation
		// you want to be
		behind_point.sett( orient.t);
		behind_point.setr( orient.r);

		behind_point.sub( point);
		behind_point.setr( -ROBOT_RADIUS*1.8);

		// determine if you are behind the object with respect
		// to the orientation
		behind = Math.cos( Math.abs( point.t - behind_point.t));

		// determine if you are on the left or right hand side
		// with respect to the orientation
		point_side = Math.sin( Math.abs( point.t - behind_point.t));

		// if you are in FRONT
		if( behind > 0)
		{
			// make the behind point more of a beside point
			// by rotating it depending on the side of the
			// orientation you are on
			if( point_side > 0)
				behind_point.sett( behind_point.t + Math.PI/2);
			else
				behind_point.sett( behind_point.t - Math.PI/2);
		}

		// move toward the behind point
		mover.sett( point.t);
		mover.setr( point.r);
		mover.add( behind_point);

		mover.setr( 1.0);

	}

	private boolean behind_point( Vec2 point, Vec2 orient)
	{

		// you are behind an object relative to the orientation
		// if your position relative to the point and the orientation
		// are approximately the same
		if( Math.abs( point.t - orient.t) < Math.PI/10) 
			return true;
		else
			return false;
	}

	private void avoidcollision( )
	{
		// an easy way to avoid collision

		// first keep out of your teammates way
		// if your closest teammate is too close, the move away from
		if(compi_cercano.r < ROBOT_RADIUS*1.4 )
		{
			mover.setx(-compi_cercano.x);
			mover.sety(-compi_cercano.y);
			mover.setr( 1.0);
		}

		// if the closest opponent is too close, move away to try to
		// go around
		else if(enemigo_cercano.r < ROBOT_RADIUS*1.4)
		{
			mover.setx(-enemigo_cercano.x);
			mover.sety(-enemigo_cercano.y);
			mover.setr( 1.0);
		}

	}
	
	class Entrenador{
		
		private int[] ordenesJugadores;
		private int numDefensas;
		
		public Entrenador(){
			ordenesJugadores=new int[5];
			for(int i=0;i<ordenesJugadores.length;i++)
				ordenesJugadores[i]=-1;
			numDefensas=1;
		}

		/*
		 Este metodo se encarga de actualizar la informacion que tiene cada
		 jugador de acuerdo con lo que captan los sensores
		 */
		private void captarEntorno(){
			Vec2 temp = new Vec2(0,0);
			Vec2 masCercanoPelota; //jugador de nuestro equipo más cercano a la pelota

			//Recoger todos los datos del entorno
			// coger el tiempo actual
			tiempo_actual = abstract_robot.getTime();

			// conseguir mi numero de jugador
			miNumero = abstract_robot.getPlayerNumber(tiempo_actual);

			//Recoger los datos de los objetos que hay en el campo
			// vector de la pelota
			pelota = abstract_robot.getBall(tiempo_actual);

			// vectores de nuestra porteria y la contraria
			miPorteria = abstract_robot.getOurGoal(tiempo_actual);
			suPorteria = abstract_robot.getOpponentsGoal(tiempo_actual);

			// conseguir las posiciones de nuestros compis de equipo
			compis = abstract_robot.getTeammates(tiempo_actual);

			// conseguir las posiciones de los oponentes
			enemigos = abstract_robot.getOpponents(tiempo_actual);

			// Recoger los datos de los objetos mas cercanos
			compi_cercano = cercanoA(YO,compis);
			enemigo_cercano = cercanoA(YO,enemigos);
			
			// Recogemos qué compañero de nuestro equipo está más cercano a la pelota
			masCercanoPelota = cercanoA(pelota,compis);

			temp.sett(masCercanoPelota.t);
			temp.setr(masCercanoPelota.r);
			
			temp.sub(pelota);
			
			// Damos al atributo cerca_pelota el valor que le corresponde
			if(temp.r > pelota.r) 
				cerca_pelota = YO; // yo soy el jugador más cercano a la pelota
			else
				 // otro jugador de mi equipo está más cerca de la pelota que YO
				cerca_pelota = masCercanoPelota;
			
			//obtenemos la orientación del robot en el momento actual
			miDireccion = abstract_robot.getSteerHeading(tiempo_actual);
			
			//modificamos la dirección y velocidad del jugador (inicialmente parado)
			mover.sett(0.0);
			mover.setr(0.0);
			
			//el jugador ha captado el entorno, pero aún no puede golpear la pelota 
			puedo_golpear = false;
			
			//actualizamos el marcador
			if (abstract_robot.getJustScored(tiempo_actual) == -CUADRANTE)
				misGoles++;
			else if (abstract_robot.getJustScored(tiempo_actual) == CUADRANTE)
				susGoles++;
			System.out.println("Mis goles: " + misGoles);
			System.out.println("Sus goles: " + susGoles);
		}
		
		/*
		 De acuerdo con lo que han recogido los sensores se elige una estrategia para
		 nuestros jugadores
		 */
		private int definirEstrategia()
		{	
			//inicialmente damos ordenes por defecto y si hay bloqueos
			//cambiamos la tactica
			ordenesJugadores[0]=10;
			ordenesJugadores[1]=22;
			ordenesJugadores[2]=23;
			ordenesJugadores[3]=13;
			ordenesJugadores[4]=14;
			
			StringMessage m = new StringMessage();
			
			/**
			 * Ordenes del entrenador
			 * 10 -> Portero
			 * 11 -> Defensa
			 * 22 -> Defensa arriba
			 * 23 -> Defensa abajo
			 * 12 -> CentroCampista
			 * 13 -> Delantero
			 * 14 -> Libero
			 * 99 -> Peticion de posicion
			 **/
			
			if (misGoles > susGoles) {
				//ponemos dos defensas para asegurar el resultado
				ordenesJugadores[0]=10;
				ordenesJugadores[1]=22;
				ordenesJugadores[2]=23;
				ordenesJugadores[3]=13;
				ordenesJugadores[4]=14;
			}
			else if (misGoles == susGoles)
			{
				//volvemos a la estrategia inicial
				ordenesJugadores[0]=10;
				ordenesJugadores[1]=22;
				ordenesJugadores[2]=23;
				ordenesJugadores[3]=13;
				ordenesJugadores[4]=14;
			}
			else{
				//vamos perdiendo y arriesgamos
				//centrocampista pasa a libero
				ordenesJugadores[0]=10;
				ordenesJugadores[1]=11;
				ordenesJugadores[2]=14;
				ordenesJugadores[3]=13;
				ordenesJugadores[4]=14;
			}
			
			if(porteroBloqueado){
				ordenesJugadores[0]=ordenesJugadores[1];
				ordenesJugadores[1]=10;
				porteroBloqueado=false;
			}
				
			for(int i=0;i<ordenesJugadores.length;i++){
				m.val = Integer.toString(ordenesJugadores[i]);
				try{abstract_robot.unicast(i,m);}
				catch(CommunicationException e){
					System.out.println(e.toString());
				}
			}
			// tell the parent we're OK
			return(CSSTAT_OK);
		}
	}
}
