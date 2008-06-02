

import java.util.Random;

import EDU.gatech.cc.is.abstractrobot.ControlSystemSS;
import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.util.Vec2;

/**
 * This is about the simplest possible soccer strategy, just go to the ball.
 * <P>
 * <A HREF="../COPYRIGHT.html">Copyright</A>
 * (c)1997 Georgia Tech Research Corporation
 *
 * @author Tucker Balch
 * @version $Revision: 1.1 $
 */


public class EnjutoMojamuTeamEnsayoAlex2 extends ControlSystemSS
{
	  static final int PORTERO = 0;
	  static final int DEFENSA = 1;
	  static final int CENTRO = 2;
	  static final int DELANTERO = 3;
	  
	  static final int SINPOSESION = 0;
	  static final int POSESION = 1;
	  static final int BALONSUELTO = 2;
	  
	  static final int CICLOSPARACAMBIAR = 75;

	  // Dimensiones del campo y de las porter�as (Del equipo PROFES1, comprobar, porque alguno utiliza otros valores porque tal vez interese)
	  static final double ANCHO_CAMPO = 1.525;
	  static final double LONGITUD_CAMPO = 2.74;
	  static final double ANCHO_PORTERIA = 0.45; //En teor�a 0.5, peor tal vez nos interese esta distancia.
	  
	  // Indica en que lado del campo estamos jugando: -1 oeste (west team), +1 este (east team).
	  private int SIDE;
	  
	  private int maxCiclosSeguirY;

	  private int estadoPortero;
	  
	/**
	Configure the Avoid control system.  This method is
	called once at initialization time.  You can use it
	to do whatever you like.
	*/
	private boolean miPosesion;
	private Vec2[] oponentes, oponentesAncho, companeros;
	private Vec2 balon, ourGoal, oponentGoal, ballMenosOurGoal;
	private Vec2 theirLeftPost,theirRightPost, ourLeftPost, ourRightPost, ourGoalCenterLeft, ourGoalCenterRight;
	private long curr_time;
	private int[] roles;
	
	//Como estuvo en el anterior tiempo -> "atacando", "defendiendo" 
	String ultimoEstado;
	//Contador para que no se produzcan cambios de estado repentinos.
	int contadorCambioEstado;
	
	//Prueba para defender.
	private int numNiCaso;
	
	//Prueba de ataque.
	int encasillarAtaque;
	boolean encasillado;
	
	public void Configure()
	{
		int numRobot = abstract_robot.getPlayerNumber(abstract_robot.getTime());
		if (numRobot == 0)
			abstract_robot.setDisplayString("Casillas");
		else if (numRobot == 1)
			abstract_robot.setDisplayString("S.Ramos");
		else if (numRobot == 2)
			abstract_robot.setDisplayString("Pajares");
		else if (numRobot == 3)
			abstract_robot.setDisplayString("Ra�l");
		else if (numRobot == 4)
			abstract_robot.setDisplayString("Guti.Haz");
		
		numNiCaso = 0;
		
		roles = new int[5];
		roles[0] = PORTERO;
		roles[1] = DEFENSA;
		roles[2] = DEFENSA;
		roles[3] = DEFENSA;
		roles[4] = DELANTERO;
		
		oponentesAncho = new Vec2[5];
		
		ultimoEstado = "atacando";
		contadorCambioEstado = CICLOSPARACAMBIAR;
		
		encasillado = false;
		
		curr_time = abstract_robot.getTime();
		if( abstract_robot.getOurGoal(curr_time).x < 0)
			SIDE = -1;
		else
			SIDE = 1;

		curr_time = abstract_robot.getTime();
		companeros = abstract_robot.getTeammates(curr_time);
		balon = abstract_robot.getBall(curr_time);
		ourGoal = abstract_robot.getOurGoal(curr_time);
		oponentGoal = abstract_robot.getOpponentsGoal(curr_time);
		oponentes = abstract_robot.getOpponents(curr_time);	
		theirLeftPost = new Vec2(oponentGoal.x,oponentGoal.y+ANCHO_PORTERIA/2);
	    theirRightPost = new Vec2(oponentGoal.x,oponentGoal.y-ANCHO_PORTERIA/2);
	    ourLeftPost = new Vec2(ourGoal.x,ourGoal.y+ANCHO_PORTERIA/2);
	    ourRightPost = new Vec2(ourGoal.x,ourGoal.y-ANCHO_PORTERIA/2);
	    ourGoalCenterLeft = new Vec2(ourGoal.x,ourGoal.y+ANCHO_PORTERIA/4);
	    ourGoalCenterRight = new Vec2(ourGoal.x,ourGoal.y-ANCHO_PORTERIA/4);
	    
	    maxCiclosSeguirY = 10;
	    
	    estadoPortero=0;
	}
		
	
	private boolean calcularPosesion() 
	{
		double distanciaOponenteMasCercanoBalon = 99999;
		Vec2 posBalon = abstract_robot.getBall(curr_time);
		double distanciaOponenteBalon;
		int oponenteMasCercano = -1;
		for (int i=0; i< oponentes.length; i++)
		{
			distanciaOponenteBalon = calcularDistancia(posBalon, oponentes[i]);
			if (distanciaOponenteBalon < distanciaOponenteMasCercanoBalon)
			{
				distanciaOponenteMasCercanoBalon = distanciaOponenteBalon;
				oponenteMasCercano = i;
			}
		}
		
		double distanciaJugadorMasCercanoBalon = calcularDistancia(posBalon, abstract_robot.getPosition(curr_time));
		double distanciaCompaneroBalon;
		for (int i=0; i< companeros.length; i++)
		{
			distanciaCompaneroBalon = calcularDistancia(posBalon, companeros[i]);
			if (distanciaCompaneroBalon < distanciaJugadorMasCercanoBalon)
				distanciaJugadorMasCercanoBalon = distanciaCompaneroBalon;
		}
		
		boolean balonSuelto = (distanciaJugadorMasCercanoBalon < abstract_robot.RADIUS*4) && (calcularDistancia(oponentes[oponenteMasCercano], balon)<abstract_robot.RADIUS*3);
		if (distanciaJugadorMasCercanoBalon < distanciaOponenteMasCercanoBalon)
			return true;
		else
			if (balonSuelto)
				return true;
			else
				return false;
	}


	private double calcularDistancia(Vec2 posBalon, Vec2 vec2) 
	{
		Vec2 v = (Vec2)posBalon.clone();
		v.sub(vec2);
		return v.r;
	}


	/**
	Called every timestep to allow the control system to
	run.
	*/
	public int TakeStep()
	{
		curr_time = abstract_robot.getTime();
		companeros = abstract_robot.getTeammates(curr_time);
		balon = abstract_robot.getBall(curr_time);
		ourGoal = abstract_robot.getOurGoal(curr_time);
		oponentGoal = abstract_robot.getOpponentsGoal(curr_time);
		oponentes = abstract_robot.getOpponents(curr_time);
		ballMenosOurGoal = getVectorResta(balon,ourGoal);		
		//New
		theirLeftPost = new Vec2(oponentGoal.x,oponentGoal.y+ANCHO_PORTERIA/2);
	    theirRightPost = new Vec2(oponentGoal.x,oponentGoal.y-ANCHO_PORTERIA/2);
	    ourLeftPost = new Vec2(ourGoal.x,ourGoal.y+ANCHO_PORTERIA/2);
	    ourRightPost = new Vec2(ourGoal.x,ourGoal.y-ANCHO_PORTERIA/2);
	    ourGoalCenterLeft = new Vec2(ourGoal.x,ourGoal.y+ANCHO_PORTERIA/4);
	    ourGoalCenterRight = new Vec2(ourGoal.x,ourGoal.y-ANCHO_PORTERIA/4);
		
		Vec2 evitaColision = null;
		
		if (contadorCambioEstado > 0) {
			contadorCambioEstado--;
		}
		
		if (oponentes.length == 5) //Ya est�n los 5 oponentes creados.
			ordenarOponentes();
			
		if (roles[abstract_robot.getPlayerNumber(curr_time)] == PORTERO)
		{
			actuarPortero();
			
			boolean pos = calcularPosesion();
			if (!miPosesion && pos)
			{
				miPosesion = pos;
				System.out.println("RECUPERO");
			}
			else if (miPosesion && !pos)
			{
				miPosesion = pos;
				System.out.println("PIERDO");
			}
		}
		else
		{
			boolean pos = calcularPosesion();
			if (!miPosesion && pos)
			{
				miPosesion = pos;
			}
			else if (miPosesion && !pos)
			{
				miPosesion = pos;
			}
			
			String estadoNuevo = "";
			
			if (miPosesion) {
				estadoNuevo = "atacando";
			} else {
				estadoNuevo = "defendiendo";
			}
			
			if (estadoNuevo.compareTo(ultimoEstado) != 0) {
				
				if (abstract_robot.getPlayerNumber(abstract_robot.getTime())==4){ 
					System.out.println("Debo cambiar??" + contadorCambioEstado);
				}
					
				//Si permitimos realizar el cambio (el contador es 0).
				if (contadorCambioEstado == 0) {
					if (abstract_robot.getPlayerNumber(abstract_robot.getTime())==4){ 
						System.out.println("CAMBIO DE ESTRATEGIA!!");
					}
					contadorCambioEstado = CICLOSPARACAMBIAR;
					evitaColision = realizarEstrategia(estadoNuevo);
					encasillado = false;
					this.ultimoEstado = estadoNuevo;
				} else {
					evitaColision = realizarEstrategia(ultimoEstado);
				}
			} else {
				evitaColision = realizarEstrategia(ultimoEstado);
				contadorCambioEstado = CICLOSPARACAMBIAR;
				this.ultimoEstado = estadoNuevo;
			}
			
			if (evitaColision != null)
			{
				abstract_robot.setSteerHeading(curr_time, evitaColision.t);
				abstract_robot.setSpeed(curr_time, evitaColision.r);
			}
			
		}	
			
			/*if ((miPosesion && (ultimoEstado.compareTo("atacando") == 0)) || (miPosesion && contadorCambioEstado==0 && ultimoEstado.compareTo("defendiendo") == 0))
				atacar();
			else if (!miPosesion)
				defender();
		}
		
		if (contadorCambioEstado == 0){
			contadorCambioEstado = 50;
		}*/
		
		//System.out.println(abstract_robot.getOpponents(curr_time).toString());
		//showDatos();
		// tell the parent we're OK
		return(CSSTAT_OK);
	}
		
	private Vec2 realizarEstrategia(String estrategia){
		if (estrategia.compareTo("atacando") == 0){
			atacar();
			return evitarColision(true); //Tambi�n evitamos colisiones con los oponentes.
		}
		else if (estrategia.compareTo("defendiendo") == 0){
			defender();
			return evitarColision(false); //No se evitan colisiones con oponentes.
		}
		else return null;
	}

	/**
	 * Ordena el vector de oponentes desde 
	 * el m�s lejano a nuestra porter�a, al m�s cercano.
	 */
	private void ordenarOponentes() 
	{
		Vec2[] opOrdenados = new Vec2[5];
		boolean[] asignados = {false, false, false, false, false};
		//boolean[] asignadosAncho = {false, false, false, false, false};
		double distancias[] = new double[5];
		
		for (int i=0; i < 5; i++)
			distancias[i] = calcularDistancia(ourGoal, oponentes[i]);
		
		for (int i = 0; i<5; i++)
		{
			double maxNoAsig = -1.0;
			//double maxNoAsigAncho = -1.0;
			int maxID = -1;
			//int maxIDAncho = -1;
			for (int j=0; j<5; j++)
			{
				if (!asignados[j] && distancias[j]>maxNoAsig)
				{
					maxNoAsig = distancias[j];
					maxID = j;
				}
//				if (!asignadosAncho[j] && oponentes[j].y>maxNoAsigAncho)
//				{
//					maxNoAsigAncho = distancias[j];
//					maxIDAncho = j;
//				}
			}
			asignados[maxID] = true;
			opOrdenados[i]=oponentes[maxID];

//			asignadosAncho[maxIDAncho] = true;
//			oponentesAncho[i]=oponentes[maxIDAncho];
		}
		oponentes = opOrdenados;
	}


/*	private void actuarPortero() {
	
		if (ourGoal.y > 0.25 || ourGoal.y <-0.25 || ourGoal.x < -0.25)
		{
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
	
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
		} 
		else 
			if (balon.x < 0.25)
			{
				//ToDo
				//balon.normalize(1.0);
				abstract_robot.setSteerHeading(curr_time, balon.t);
				abstract_robot.setSpeed(curr_time, 0.75);					
			}
			else
			{
				abstract_robot.setSteerHeading(curr_time, ourGoal.t);
				abstract_robot.setSpeed(curr_time, 0.5);
			}
	}*/


	private void defender() 
	{
		//System.out.println("defiendo");
		//Incluso comentando esto se ve m�s gr�fico, con el portero cubriendo tambi�n.
			//Variante: Solo cubrir a su jugador m�s ofensivo.
/*			if ( calcularOponenteMasOfensivo(oponentes) == abstract_robot.getPlayerNumber(curr_time)) {
				numNiCaso = abstract_robot.getPlayerNumber(curr_time);
				System.out.println("Orden recibida: Voy a por su jugador m�s ofensivo: " + numNiCaso + ".");
				cubrir(abstract_robot.getPlayerNumber(curr_time));
			}
			else {
				if (numNiCaso!=abstract_robot.getPlayerNumber(curr_time)) {
					abstract_robot.setSteerHeading(curr_time, balon.t);
					abstract_robot.setSpeed(curr_time, 0.8);
				}
			}*/

		int playerNumber = abstract_robot.getPlayerNumber(curr_time);
		int rol = roles[playerNumber];
		if (rol == DEFENSA)
			cubrir(calcularJugadorACubrir());
		else if (rol == CENTRO)
		{
			
		}
		else if (rol == DELANTERO)
		{
			abstract_robot.setDisplayString("Guti - Defiende");
			atacar();
			//goToBall();
		}
	}


	private int calcularJugadorACubrir() 
	{	
		int numDefensas = 0;
		int masAltos = 0;
		Vec2 vCentroCompanero;
		for (int i = 0; i < 5; i++)
			if (roles[i] == DEFENSA)
			{
				numDefensas++;
				if (i < 4 && companeros[i].y>0)
					masAltos++;
			}
		Vec2[] candidatos = new Vec2[numDefensas];
		int[] numerosVectorIni = new int[numDefensas];
		int[] numerosVectorFin = new int[numDefensas];
		for (int i = 0; i < numDefensas; i++)
		{
			candidatos[i] = oponentes[4-i]; //Candidatos queda ordenado por cercan�a a la porter�a.
			numerosVectorIni[i] = 4-i;
		}
		Vec2[] porAltura = new Vec2[candidatos.length];
		boolean[] asignados = new boolean[candidatos.length];
		for (int i = 0; i < candidatos.length; i++)
			asignados[i] = false;
		for (int i = 0; i < candidatos.length; i++)
		{
			double minAncho = -9999.9;
			int minID = -1;
			for (int j=0; j<candidatos.length; j++)
			{
				if (!asignados[j] && candidatos[j].y > minAncho)
				{
					minAncho = candidatos[j].y;
					minID = j;
				}
			}
			asignados[minID] = true;
			porAltura[i]=candidatos[minID];
			numerosVectorFin[i]=numerosVectorIni[minID];
		}
		masAltos = 0;
		return numerosVectorFin[masAltos];
	}


	private void cubrir(int i)
	{
		abstract_robot.setDisplayString("al " + i);
		//Nunca entendido, de hecho he puesto ahora >= para que cubra tambi�n el jugador 4.
		if (oponentes.length >= i)
		{
			//Vec2 oponente = dameOponenteID(i);
		
			Vec2 vOponenteBalon = (Vec2)balon.clone(); 
			vOponenteBalon.setx(vOponenteBalon.x - oponentes[i].x);
			vOponenteBalon.sety(vOponenteBalon.y - oponentes[i].y);
			//Hacemos que el vector sea unitario, porque se multiplicar� despu�s por el 2*radio (Menuda diferencia!!).
			vOponenteBalon.normalize(1.0);
			
			//System.out.println("La medida de 2*Radius es = " + (2*SocSmall.RADIUS));
			
			//S�, multiplicar, porque lo que quieres es una parte del vector, para hacer la correcci�n.
			double pX = (2*SocSmall.RADIUS)*vOponenteBalon.x;
			double pY = (2*SocSmall.RADIUS)*vOponenteBalon.y;
			Vec2 posicionJugador = abstract_robot.getPosition(curr_time);
			//Antes
				//Vec2 vJugadorPosicion = new Vec2((pX - posicionJugador.x),(pY - posicionJugador.y));
			//Ahora
			
			//No vale, prueba inicial..
			//Vec2 vJugadorPosicion = new Vec2((pX + posicionJugador.x),(pY + posicionJugador.y));
			
			//Yendo a la bola, pero con correcci�n de ir "por donde vaya el rival" (poco �til) 
			//Vec2 vJugadorPosicion = new Vec2((pX + balon.x),(pY + balon.y));

			//Directamente al jugador rival (bloqueas m�s):
			//Vec2 vJugadorPosicion = new Vec2(oponentes[i].x,oponentes[i].y);
			
			//Entre la bola y el jugador:
			Vec2 vJugadorPosicion = new Vec2((pX + oponentes[i].x),(pY + oponentes[i].y));

			//Como siempre.
			double distanciaJugPos = vJugadorPosicion.r;
			if (distanciaJugPos > 0.5*SocSmall.RADIUS)
			{
				abstract_robot.setSpeed(curr_time, 0.5);
				abstract_robot.setSteerHeading(curr_time, vJugadorPosicion.t);
				abstract_robot.setSpeed(curr_time, 1.0);
			}
			else
			{
				abstract_robot.setSteerHeading(curr_time, balon.t);
				abstract_robot.setSpeed(curr_time, 0.0);
			}
		}
	}


	private void atacar() 
	{
		//System.out.println("atacooo");
		
		
		//Todos menos el primer jugador
		if (abstract_robot.getPlayerNumber(curr_time) != 1)
		{
			double radio = balon.r;
			double robot = 1.2*abstract_robot.RADIUS;
			
			Vec2 posicionCentroPorteria = new Vec2(balon.x, balon.y);
			posicionCentroPorteria.sub(oponentGoal);
			posicionCentroPorteria.setr(abstract_robot.RADIUS);
			posicionCentroPorteria.add(balon);
			
			Vec2 posicionArribaPorteria = new Vec2(balon.x, balon.y);
			Vec2 parteArribaPorteria = new Vec2(oponentGoal.x,oponentGoal.y + abstract_robot.RADIUS*3);
			posicionArribaPorteria.sub(parteArribaPorteria);
			posicionArribaPorteria.setr(abstract_robot.RADIUS);
			posicionArribaPorteria.add(balon);
			
			Vec2 posicionAbajoPorteria = new Vec2(balon.x, balon.y);
			Vec2 parteAbajoPorteria = new Vec2(oponentGoal.x,oponentGoal.y - abstract_robot.RADIUS*3);			
			posicionAbajoPorteria.sub(parteAbajoPorteria);
			posicionAbajoPorteria.setr(abstract_robot.RADIUS);
			posicionAbajoPorteria.add(balon);
			
			
			int playerNumber = abstract_robot.getPlayerNumber(curr_time);
			int rol = roles[playerNumber];
			
			//If the player is "DELANTERO", he goes behind the ball and tries to score.
			if (rol == DELANTERO) {
			
				if (radio > robot)
				{
					// set heading behind ball
					abstract_robot.setSpeed(curr_time, 1.0);
					abstract_robot.setSteerHeading(curr_time, posicionCentroPorteria.t);
					
				}
				else
				{
					
					abstract_robot.setSpeed(curr_time, 0.7);
					int aDonde = encasillarAtaque;
					
					if (encasillado == false) {
						Random random = new Random();
						aDonde = random.nextInt(3);
						encasillarAtaque = aDonde;
						encasillado = true;
					}
					
					//System.out.println("A donde: " + aDonde);
					switch (aDonde){
						case 0: abstract_robot.setSteerHeading(curr_time, posicionArribaPorteria.t);
						abstract_robot.setDisplayString("D - Up");
						break;
						case 1: abstract_robot.setSteerHeading(curr_time, posicionCentroPorteria.t);
						abstract_robot.setDisplayString("D - Centro");
						break;
						case 2: abstract_robot.setSteerHeading(curr_time, posicionAbajoPorteria.t);
						abstract_robot.setDisplayString("D - Down");
						break;
						default: abstract_robot.setSteerHeading(curr_time, posicionCentroPorteria.t);
						abstract_robot.setDisplayString("D - Centro");
						break;
						
					}
					
					abstract_robot.setSpeed(curr_time, 1.0);
					//System.out.println("GIRA!!");
				}
				// set speed at maximum
				abstract_robot.setSpeed(curr_time, 1.0);
	
				// kick it if we can
				double distancia = calcularDistancia(balon, oponentGoal);
				double distanciaTiro = 0.5;
				if (abstract_robot.canKick(curr_time) && distancia < distanciaTiro)
				{
					abstract_robot.kick(curr_time);
					System.out.print("TIRA!!");
				}
				
			//If the player is not DELANTERO, he will support the attack
			} else {
				Vec2 posicion = abstract_robot.getPosition(curr_time);
				int banda = -1; //0 banda superior, 1 banda inferior.
				
				if (playerNumber%2 == 0){
					abstract_robot.setDisplayString("Arriba");
					banda = 0;
				} else {
					abstract_robot.setDisplayString("Abajo");
					banda = 1;
				}
				
				boolean palomero = false;
				double distanciaASuPorteria = this.oponentGoal.r;
				//Puede estar en varios puntos del campo
				if (distanciaASuPorteria >= LONGITUD_CAMPO/2) {
					//Ir a la banda.
					Vec2 puntoBanda;
					if (banda == 0){
						puntoBanda = new Vec2(oponentGoal.x + (-SIDE)*LONGITUD_CAMPO/2, oponentGoal.y + ANCHO_CAMPO/2 + 0.2);
						abstract_robot.setDisplayString("Yendo Arriba");
					} else {
						puntoBanda = new Vec2(oponentGoal.x + (-SIDE)*LONGITUD_CAMPO/2, oponentGoal.y - ANCHO_CAMPO/2 + 0.2);
						abstract_robot.setDisplayString("Yendo Abajo");
					}
					
					abstract_robot.setSpeed(curr_time, 0.4);
					abstract_robot.setSteerHeading(curr_time, puntoBanda.t);
					abstract_robot.setSpeed(curr_time, 1.0);
				} else {
					if (distanciaASuPorteria >= LONGITUD_CAMPO/5) {
						//Ir a la banda.
						Vec2 puntoPalomero;
						if (banda == 0){
							puntoPalomero = new Vec2(oponentGoal.x + (-SIDE)*LONGITUD_CAMPO/8, oponentGoal.y + ANCHO_CAMPO/4 - 0.2);
							abstract_robot.setDisplayString("Yendo Palom Arriba");
						} else {
							puntoPalomero = new Vec2(oponentGoal.x + (-SIDE)*LONGITUD_CAMPO/8, oponentGoal.y - ANCHO_CAMPO/4 - 0.2 );
							abstract_robot.setDisplayString("Yendo Palom Abajo");
						}
						
						abstract_robot.setSpeed(curr_time, 0.4);
						abstract_robot.setSteerHeading(curr_time, puntoPalomero.t);
						abstract_robot.setSpeed(curr_time, 1.0);
						
					} else {
						
						if (distanciaASuPorteria < LONGITUD_CAMPO/4.5){
						//if ((posicion.y >= oponentGoal.y + 0.4) || (posicion.y <= oponentGoal.y - 0.4) ) {
							//Estoy en palomero
							abstract_robot.setSpeed(curr_time, 0.0);
							abstract_robot.setSteerHeading(curr_time, oponentGoal.t);
							abstract_robot.setDisplayString("Estoy en palomero");
							evitarColision(true);
							palomero = true;
						} else {
							//Ir a palomero
							
						}
					}
				}
				
				
				//Independiente a la colocación
				double robotApoyo;
				
				if (palomero) {
					robotApoyo = 2*abstract_robot.RADIUS;
				} else {
					robotApoyo = 3*abstract_robot.RADIUS;
				}
				if (radio <= robotApoyo) {
					
										
					abstract_robot.setDisplayString("PUSHING");
					abstract_robot.setSpeed(curr_time, 0.3);
					if (playerNumber%2 == 0) {
						abstract_robot.setSteerHeading(curr_time, posicionArribaPorteria.t);
					} else {
						abstract_robot.setSteerHeading(curr_time, posicionAbajoPorteria.t);
					}
					abstract_robot.setSpeed(curr_time, 1.0);
				}
				
				double distancia = calcularDistancia(balon, oponentGoal);
				if (abstract_robot.canKick(curr_time) && distancia < 0.5)
				{
					abstract_robot.setSpeed(curr_time, 0.7);
					abstract_robot.kick(curr_time);
					abstract_robot.setSpeed(curr_time, 1.0);
					System.out.print("TIRA!!");
				}
								
			}
		}
		else
		{
			int enemigoMasPeligroso = calcularOponenteMasAdelantado(this.oponentes);
			//int enemigoMasPeligroso = calcularOponenteMasOfensivo(this.oponentes);
			if (oponentes.length == 5){
				//abstract_robot.setSpeed(curr_time, 0);
				
				if((abstract_robot.getPosition(curr_time).x)*SIDE < 0) {
					abstract_robot.setDisplayString("CAMPO ENEMIGO");
					Vec2 cercaDeMiCampo = new Vec2(ourGoal.x + (-SIDE)*LONGITUD_CAMPO/8, ourGoal.y);
					abstract_robot.setSteerHeading(curr_time, cercaDeMiCampo.t);
				} else {
					abstract_robot.setDisplayString("Mi CAMPO");
					cubrir(enemigoMasPeligroso);
				}
				
				
				/*Vec2 cercaCentroDelCampoEste = new Vec2(ourGoal.x+(LONGITUD_CAMPO/2)+0.10,ourGoal.y);
				Vec2 cercaCentroDelCampoOeste = new Vec2(ourGoal.x+(LONGITUD_CAMPO/2)-0.30,ourGoal.y);
				
				
				abstract_robot.setSteerHeading(curr_time, cercaCentroDelCampoOeste.t);*/
				
			}
		}
	}

	
	public void showDatos() {
//		if (abstract_robot.getPlayerNumber(curr_time) == 0) {
			long curr_timeAux = abstract_robot.getTime();
			Vec2[] oponentesAux = abstract_robot.getOpponents(curr_time);
			Vec2[] companerosAux = abstract_robot.getTeammates(curr_time);
			Vec2 balonAux = abstract_robot.getBall(curr_time);
			
			System.out.println("Listado de datos disponibles, desde jugador: " + (abstract_robot.getPlayerNumber(curr_time)) + ".");
			System.out.println("Compa�eros:");
			printVectorVec2(companerosAux);
			System.out.println("Oponentes");
			printVectorVec2(oponentesAux);
			System.out.println("Posicion del bal�n: (" +balonAux.x + "," + balonAux.y + "). R=" + balonAux.r + " . T=" + balonAux.t + ". ");			
			System.out.println("Jugador m�s defensivo de nuestro equipo: "+calcularCompaneroMasDefensivo(companerosAux));
			System.out.println("Jugador m�s ofensivo de nuestro equipo: "+calcularCompaneroMasOfensivo(companerosAux));
			System.out.println("Jugador m�s defensivo del oponente: "+calcularOponenteMasDefensivo(oponentesAux));
			System.out.println("Jugador m�s ofensivo del oponente: "+calcularOponenteMasOfensivo(oponentesAux));
			System.out.println("__________________________________________________________________________");

//		}
	}
	
	public void printVectorVec2(Vec2[] vector) {
		for (int i=0; i< vector.length; i++)		
		{
			//System.out.println("Vector Op[" + i + "] --> (" +vector[i].x + "," + vector[i].y + "). R=" + vector[i].r + " . T=" + vector[i].t + ". ");
		}
		////System.out.println("__________________________________________________________________________");
	}
	
	/**
	 * Devuelve el jugador de EnjutoMojamuteam que est� m�s atr�s en el campo (el que har�a las veces de portero).
	 */
	public int calcularCompaneroMasDefensivo(Vec2[] vectorCompaneros) {
		int masDefensivo=-1; //-1 se considera uno mismo.
		double valorMasDefensivo=0;
		for (int i=0; i< vectorCompaneros.length; i++) {
			if (vectorCompaneros[i].x < valorMasDefensivo) {
				masDefensivo=i;
				valorMasDefensivo = vectorCompaneros[i].x;
			}
		}
		return masDefensivo;
	}
	
	/**
	 * Devuelve el jugador de EnjutoMojamuteam que est� m�s adelantado en el campo (el que har�a las veces de delantero).
	 */
	public int calcularCompaneroMasOfensivo(Vec2[] vectorCompaneros) {
		int masOfensivo=-1; //-1 se considera uno mismo.
		double valorMasOfensivo=0;
		for (int i=0; i< vectorCompaneros.length; i++) {
			if (vectorCompaneros[i].x > valorMasOfensivo) {
				masOfensivo=i;
				valorMasOfensivo = vectorCompaneros[i].x;
			}
		}
		return masOfensivo;		
	}
	
	/**
	 * Devuelve el jugador de EnjutoMojamuteam que est� m�s atr�s en el campo (el que har�a las veces de portero).
	 */
	public int calcularOponenteMasDefensivo(Vec2[] vectorOponentes) {
		int masDefensivo=0; //Se inicia a 0 el m�s defensivo, y se comapra respecto al resto.
		double valorMasDefensivo = 0;
		if (vectorOponentes.length>0) {
			valorMasDefensivo=vectorOponentes[0].x;
		}
		for (int i=0; i< vectorOponentes.length; i++) {
			if (vectorOponentes[i].x < valorMasDefensivo) {
				masDefensivo=i;
				valorMasDefensivo = vectorOponentes[i].x;
			}
		}
		return masDefensivo;	
	}
	
	/**
	 * Devuelve el jugador del equipo rival que est� m�s adelantado en el campo (el que har�a las veces de delantero).
	 */
	public int calcularOponenteMasOfensivo(Vec2[] vectorOponentes) {
		int masOfensivo=0; //Se inicia a 0 el m�s defensivo, y se comapra respecto al resto.
		double valorMasOfensivo = 0;
		if (vectorOponentes.length>0) {
			valorMasOfensivo=vectorOponentes[0].x;
		}
		for (int i=0; i< vectorOponentes.length; i++) {
			if (vectorOponentes[i].x > valorMasOfensivo) {
				masOfensivo=i;
				valorMasOfensivo = vectorOponentes[i].x;
			}
		}
		return masOfensivo;	
	}
	
	public int calcularOponenteMasAdelantado(Vec2[] vectorOponentes) {
		double distanciaANuestraPorteria = 999;
		double distanciaTemp = 999;
		int masAdelantado = 0;
		
		for (int i=0; i< vectorOponentes.length; i++) {
			distanciaTemp = calcularDistancia(vectorOponentes[i], this.ourGoal);
			if (distanciaTemp < distanciaANuestraPorteria) {
				masAdelantado = i;
				distanciaANuestraPorteria = distanciaTemp;
			}
		}
		
		return masAdelantado;
	}
	
	private void goToBall() {
		abstract_robot.setSteerHeading(curr_time, balon.t);
		abstract_robot.setSpeed(curr_time, 1.0);

		if (abstract_robot.canKick(curr_time))
			abstract_robot.kick(curr_time);

	}
	
	/**
	 * Devuelve el jugador de EnjutoMojamuteam que est� m�s atr�s en el campo (el que har�a las veces de portero).
	 */
	/*public int calcularMasDefensivo(Vec2[] vector) {
		int masDefensivo=-1; //-1 se considera uno mismo.
		double valorMasDefensivo=0;
		for (int i=0; i< vector.length; i++) {
			if (vector[i].x < valorMasDefensivo) {
				masDefensivo=i;
				valorMasDefensivo = vector[i].x;
			}
		}
		return masDefensivo;
	}*/
	
	/**
	 * Devuelve el jugador del equipo rival que est� m�s adelantado en el campo (el que har�a las veces de delantero).
	 * @param evitarOponentes 
	 */
	/*public int calcularMasOfensivo(Vec2[] vector) {
		int masOfensivo=-1; //-1 se considera uno mismo.
		double valorMasOfensivo=0;
		for (int i=0; i< vector.length; i++) {
			if (vector[i].x > valorMasOfensivo) {
				masOfensivo=i;
				valorMasOfensivo = vector[i].x;
			}
		}
		return masOfensivo;	
	}*/
	
	//Fusilado del DTeam.
	private Vec2 evitarColision(boolean evitarOponentes )
	{
		// an easy way to avoid collision

		// first keep out of your teammates way
		// if your closest teammate is too close, the move away from
		Vec2 yo = new Vec2(0,0);
		Vec2 compiMasCercano = calcularMasCercano(yo, companeros);
		if( compiMasCercano.r < SocSmall.RADIUS*1.1 )
		{
			Vec2 nuevaDireccion = new Vec2(0,0);
			nuevaDireccion.setx( -compiMasCercano.x);
			nuevaDireccion.sety( -compiMasCercano.y);
			nuevaDireccion.setr( 1.0);
			return nuevaDireccion;
		}

		// if the closest opponent is too close, move away to try to
		// go around
		else 
			if (evitarOponentes)
			{
				Vec2 oponenteMasCercano = calcularMasCercano(yo, oponentes);
				if( oponenteMasCercano.r < SocSmall.RADIUS*1.1)
				{
					Vec2 nuevaDireccion = new Vec2(0,0);
					nuevaDireccion.setx( -oponenteMasCercano.x);
					nuevaDireccion.sety( -oponenteMasCercano.y);
					nuevaDireccion.setr( 1.0);
					return nuevaDireccion;
				}
				else return null;
			}
			else return null;
	}

	//Fusilado del DTeam.
	private Vec2 calcularMasCercano(Vec2 posicion, Vec2[] candidatos) 
	{
		double dist = 9999;
		Vec2 result = new Vec2(0, 0);
		Vec2 temp = new Vec2(0, 0);

		for( int i=0; i < candidatos.length; i++)
		{

			// find the distance from the point to the current
			// object
			temp.sett( candidatos[i].t);
			temp.setr( candidatos[i].r);
			temp.sub(posicion);

			// if the distance is smaller than any other distance
			// then you have something closer to the point
			if(temp.r < dist)
			{
				result = candidatos[i];
				dist = temp.r;
			}
		}
		return result;
	}
	
	//A�adido por Nach
	
	private void tacticaAbsurda() {
		//Go to corner.

		// set heading towards it
		abstract_robot.setSteerHeading(curr_time, oponentGoal.t+0.9);

		// set speed at maximum
		abstract_robot.setSpeed(curr_time, 1.0);
	}
	
	private boolean lejosDeTuArea() {
		if ( ourGoal.x < -0.45)
			return true;
		else
			return false;
	}

	private boolean estasEnBanda() {
		if ( ourGoal.y > 0.35 || ourGoal.y <-0.35)
			return true;
		else
			return false;
	}

	//Derecha = Abajo (si fuera siempre Este) en otras funciones.
	private boolean estasEnBandaDerecha() {
		if ( ourGoal.y > 0.35)
			return true;
		else
			return false;
	}
	
	//Izquierda = Arriba (si fuera siempre Este) en otras funciones.
	private boolean estasEnBandaIzquierda() {
		if ( ourGoal.y < -0.35)
			return true;
		else
			return false;
	}
	
	private boolean estasMuyEnBanda() {
		if ( ourGoal.y > 0.45 || ourGoal.y <-0.45)
			return true;
		else
			return false;
	}
	
	//Comprobar que funcione para este y oeste.
	private boolean balonCercaAreaPropia() {
//		if (balon.x < 0.25) {
		if (ballMenosOurGoal.x < 0.35) {
			return true;
		}
		else {
			return false;
		}
	}

	private boolean balonDemasiadoLejosDeAreaPropia() {
//		if (balon.x < 0.25) {
		if (ballMenosOurGoal.x < 2.05) {
			return false;
		}
		else {
			return true;
		}
	}
	
	private boolean balonAvanzandoEscorado() {
//		if (balon.y > 0.25 || balon.y < -0.25) {
		if (ballMenosOurGoal.y > 0.25 || ballMenosOurGoal.y < -0.25) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean balonAvanzandoEscoradoBandaAbajo() {
//		if (balon.y < -0.25) {
		if (ballMenosOurGoal.y < -0.25) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean balonAvanzandoEscoradoBandaArriba() {
//		if (balon.y > 0.25) {
		if (ballMenosOurGoal.y > 0.25) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean balonAvanzandoCentrado() {
		/*if (balon.y < 0.25 || balon.y > -0.25) {
			return true;
		}*/
		//Vec2 ballMenosOurGoal = getVectorResta(balon,ourGoal);
		if (ballMenosOurGoal.y < 0.25 && ballMenosOurGoal.y > -0.25) {
			//System.out.println("Avanzando centrado, con y = " + ballMenosOurGoal.y);
			return true;
		}
		else {
			//System.out.println("Avanzando escorado, con y = " + ballMenosOurGoal.y);
			return false;
		}
	}
	
	private Vec2 getVectorResta(Vec2 vector1, Vec2 vector2) 
	{
		Vec2 v = (Vec2)vector1.clone();
		v.sub(vector2);
		return v;
	}

	
	private boolean estasBajoPalos() {
		if (ourGoal.r<0.15) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean estasBajoPalosEnX() {
		if (ourGoal.x<-0.15) {
			return true;
		}
		else {
			return false;
		}
	}

	private boolean estasBajoPalosEnY() {
		if (ourGoal.y>-0.5 && ourGoal.y<0.5) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean estasEnElCentroVerticalDelCampo() {
		if (ourGoal.y<0.05 && ourGoal.y>-0.05) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean estasEnOurCenterLeft() {
		if (ourGoalCenterLeft.r <0.05)
			return true;
		else
			return false;
	}
	
	private boolean estasEnOurCenterRight() {
		if (ourGoalCenterRight.r <0.05)
			return true;
		else
			return false;
	}
	
	//M�todo a mejorar.
	private void actuarPortero() {
	
		int estadoPorteroAnterior = estadoPortero;
		
		//Comprobar si est� sufriendo un bloqueo.
		
			//Si es as� intentar evitar colision, y posteriormente ir hacia la porter�a.
		
		//Permitirle salir del area, si el equipo rival est� muy defensivo.
		
		//En mano a mano no ir directamente a la bola, sin ir a la bola sin perder de vista la porter�a.

// Antes:
		/*if (curr_time<100) {
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
			//System.out.println("Curr_time menos que 100");
			//System.out.println("PORTERO: estadoInicial");
			estadoPortero = 0;
		}*/
		if ( /*estasEnBanda() ||*/ lejosDeTuArea() /*|| !estasBajoPalosEnY()*/ ) {
			//System.out.println("PORTERO: estasEnBanda o lejosDeTuArea");
			// set speed at minimum
			if (estadoPortero != 1)
				abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
			estadoPortero = 1;
		}
		else if ( estasEnBandaDerecha() ) {
			//System.out.println("PORTERO: estas en banda derecha");
			// set speed at minimum
			if (estadoPortero != 11)
				abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, ourRightPost.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
			estadoPortero = 11;
		}
		else if ( estasEnBandaIzquierda() ) {
			//System.out.println("PORTERO: estas en banda izquierda");
			// set speed at minimum
			if (estadoPortero != 111)
				abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, ourLeftPost.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
			estadoPortero = 111;
		}
		else if (balonCercaAreaPropia() && balonAvanzandoCentrado()) {
			//System.out.println("PORTERO: balonCercaAreaPropia y bal�n avanzando centrado");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.75	
			estadoPortero = 2;
		}
		else if (balonCercaAreaPropia() && balonAvanzandoEscoradoBandaArriba()) {
			//System.out.println("PORTERO: balonCercaAreaPropia y bal�n avanzando escorado banda arriba");
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourLeftPost.t);
			abstract_robot.setSpeed(curr_time, 0.7); //Antes 0.5
			estadoPortero = 3;
		}
		else if (balonCercaAreaPropia() && balonAvanzandoEscoradoBandaAbajo()) {
			//System.out.println("PORTERO: balonCercaAreaPropia y bal�n avanzando escorado banda abajo");
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourRightPost.t);
			abstract_robot.setSpeed(curr_time, 0.7); //Antes 0.5
			estadoPortero = 4;
		}
		else if (!balonCercaAreaPropia() && !estasEnElCentroVerticalDelCampo()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && !estasEnElCentroVerticalDelCampo()");
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 4;
		}
		else if (!balonCercaAreaPropia() && estasEnElCentroVerticalDelCampo()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && estasEnElCentroVerticalDelCampo()");
			if (estadoPortero!=423)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
			estadoPortero = 423;
		}
		else if (!balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && !estasEnOurCenterLeft()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && !estasEnOurCenterLeft()");
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoalCenterLeft.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 31;
		}
		else if (!balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && !estasEnOurCenterRight()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && !estasEnOurCenterLeft()");
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoalCenterLeft.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 31;
		}
		else if (!balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && estasEnOurCenterLeft()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && ya estasEnOurCenterLeft()");
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
			estadoPortero = 311;
		}
		
		else if (balonCercaAreaPropia() && balonAvanzandoEscoradoBandaAbajo()) {
			//System.out.println("PORTERO: balonCercaAreaPropia y bal�n avanzando escorado banda abajo");
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourRightPost.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 41;
		}
		//ANTES:
		else if (estasBajoPalos() && !estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos pero no en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 1);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
		}
		else if (!balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && estasBajoPalosEnY()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia y !balonDemasiadoLejosDeAreaPropia");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
			estadoPortero=5;
		}
		else if (!balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && !estasBajoPalosEnY()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia y !balonDemasiadoLejosDeAreaPropia");
			if (estadoPortero!=6)	
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero=6;
		}
		else if (!balonCercaAreaPropia() && balonDemasiadoLejosDeAreaPropia()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia y s� balonDemasiadoLejosDeAreaPropia");
			if (estadoPortero!=7)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5	
			estadoPortero=7;			
		}
		//else if (estasBajoPalos() && estasEnElCentroVerticalDelCampo()) {
		//	//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
		//	//System.out.println("PORTERO: Est�s bajo palos y en el centro vertical del campo");
		//	abstract_robot.setSpeed(curr_time, 0);
		//	abstract_robot.setSteerHeading(curr_time, balon.t);
		//}
		
		/*else if (estasBajoPalos() && balonDemasiadoLejosDeAreaPropia() && maxCiclosSeguirY>0) {
			maxCiclosSeguirY--;
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos y el balon demasiado lejos del area propia. Sigues la y de la bola como maximo " + maxCiclosSeguirY +" ciclos m�s.");
			abstract_robot.setSpeed(curr_time, 0);
			Vec2 nuevaDireccion= new Vec2(0,ballMenosOurGoal.y);
			//nuevaDireccion.normalize(1);
			abstract_robot.setSteerHeading(curr_time, nuevaDireccion.t);
			abstract_robot.setSpeed(curr_time, 1);
			//maximoSeguirYBola;
		}*/
		else if (estasBajoPalos() && balonDemasiadoLejosDeAreaPropia() && maxCiclosSeguirY==0) {
			//System.out.println("PORTERO: Te cansas de seguir y, y simplemente miras quieto.");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			double rnd = Math.random();
			if (rnd >0.95) {
				maxCiclosSeguirY = 10;
				//System.out.println("El siguiente paso puede que vuelvas a seguir y.");
			}
		}
		else if (estasBajoPalos() && !balonDemasiadoLejosDeAreaPropia()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos y el balon demasiado lejos del area propia. Sigues la y de la bola como maximo unos ciclos m�s.");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			//maximoSeguirYBola;
		}
		else if (!estasBajoPalosEnY() && estasBajoPalosEnX()) {
			//System.out.println("PORTERO: Est�s bajo palos en X pero no en y.");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
		}
		//Prueba
		else if (!estasBajoPalos()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos pero no en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
		}
		else if (estasBajoPalos() && !estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos pero no en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
		}
		else if (estasBajoPalos() && estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos y en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
		}
		else {
			
			//System.out.println("PORTERO: default: ir a tu porter�a...");
			abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);		
			
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
/*
			//System.out.println("PORTERO: Por defecto, est�s parado.");
			////System.out.println("PORTERO: Por defecto, est�s bajo palos y sigues la y de la bola.");
			abstract_robot.setSpeed(curr_time, 0);
			//Importante: Para saber si est� escorado o no se utiliza ballMenosOurGoal, pero para seguir la direcci�n de la y se utiliza ball.y.
//			Vec2 nuevaDireccion= new Vec2(0,balon.y);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0);*/
		}
		
		if (estadoPorteroAnterior!=estadoPortero) {
			//System.out.println("******* TRANSICI�N *******");
		}
		
		/*else {
		if ( estasEnBanda() || lejosDelArea() )
//		if ( (ourGoal.y > 0.25 || ourGoal.y <-0.25) && (ourGoal.x < -0.25))
		{

			// set speed at minimum
			//abstract_robot.setSpeed(curr_time, 0.1);
			
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
	
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
		} 
		else 
			if (balon.x < 0.25)
			{
				//ToDo
				//balon.normalize(1.0);
				abstract_robot.setSpeed(curr_time, 0);
				abstract_robot.setSteerHeading(curr_time, balon.t);
				abstract_robot.setSpeed(curr_time, 1); //Antes 0.75					
			}
			else
			{
				if (ourGoal.x > 0.2) {
					abstract_robot.setSteerHeading(curr_time, ourGoal.t);
					abstract_robot.setSpeed(curr_time, 1);
					//System.out.println("**** KEEPER -->  ourGoal.x = " + ourGoal.x);
				}
				else {
					abstract_robot.setSpeed(curr_time, 0);
					abstract_robot.setSteerHeading(curr_time, balon.t);
				}
			}
		}*/
	}
}
