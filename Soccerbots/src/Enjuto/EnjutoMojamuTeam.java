package Enjuto;

 import java.util.Enumeration;

import EDU.gatech.cc.is.abstractrobot.ControlSystemSS;
import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.communication.Message;
import EDU.gatech.cc.is.communication.StringMessage;
import EDU.gatech.cc.is.util.Vec2;
import Enjuto.EnjutoRol;
import Enjuto.EnjutoRolCentrocampistaBloqueador;
import Enjuto.EnjutoRolDefensa;
import Enjuto.EnjutoRolDefensaCierre;
import Enjuto.EnjutoRolDelantero;
import Enjuto.EnjutoRolPalomero;
import Enjuto.EnjutoRolPortero;

/**
 * This is about the simplest possible soccer strategy, just go to the ball.
 * <P>
 * <A HREF="../COPYRIGHT.html">Copyright</A>
 * (c)1997 Georgia Tech Research Corporation
 *
 * @author Tucker Balch
 * @version $Revision: 1.1 $
 */


public class EnjutoMojamuTeam extends ControlSystemSS
{
	  static final int PORTERO = 0;
	  static final int DEFENSA = 1;
	  static final int CENTRO = 2;
	  static final int DELANTERO = 3;
	  static final int CENTROCAMPISTAAPROVECHADORDEBLOQUEOS = 4;
	  static final int DEFENSACIERRE = 5;
	  static final int PALOMERO = 6;
	  

	  
	  static final int SINPOSESION = 0;
	  static final int POSESION = 1;
	  static final int BALONSUELTO = 2;
	  
	  static final int DEFENDER = 0;
	  static final int ATACAR = 1;
	  
	  static final int CICLOSPARACAMBIAR = 75;

	  // Dimensiones del campo y de las porter�as.
	  static final double ANCHO_CAMPO = 1.525;
	  static final double LONGITUD_CAMPO = 2.74;
	  static final double ANCHO_PORTERIA = 0.48; //En teor�a 0.5, perp tal vez nos interese esta distancia.

	  static int golesMarcados;
	  static int golesEncajados;
	  static int resultadoActual;
	  
	  // Indica en que lado del campo estamos jugando: -1 oeste (west team), +1 este (east team).
	  public int SIDE;
	  
	 //Estado del equipo de cara a si ataca o defiende.
	  public int estadoAtaqueODefensa;

	 
	  
	  //Rol que posee este jugador.
	  private EnjutoRol rol;
	  
	  private int ultimoRol;
	  
	/**
	Configure the Avoid control system.  This method is
	called once at initialization time.  You can use it
	to do whatever you like.
	*/
	public boolean miPosesion;
	public Vec2[] oponentes, oponentesAncho, companeros;
	public Vec2 balon, ourGoal, oponentGoal, ballMenosOurGoal;
	public Vec2 theirLeftPost,theirRightPost, ourLeftPost, ourRightPost, ourGoalCenterLeft, ourGoalCenterRight;
	public Vec2 ourGoalAdelantado, cercaCentroDelCampoEste, cercaCentroDelCampoOeste;
	public long curr_time;
	public int[] roles;
	
	public Enumeration mensajesRecibidos;
	
	//Como estuvo en el anterior tiempo -> "atacando", "defendiendo" 
	int ultimoEstado;
	//Contador para que no se produzcan cambios de estado repentinos.
	int contadorCambioEstado;
	
	//Prueba de ataque.
	
	boolean encasillado;
	
	public void Configure()
	{
		if( abstract_robot.getOurGoal(curr_time).x < 0)
			SIDE = -1;
		else
			SIDE = 1;
		
		if (SIDE==-1) {
			int numRobot = abstract_robot.getPlayerNumber(abstract_robot.getTime());
			if (numRobot == 0) 
			{
				abstract_robot.setDisplayString("Casillas");
				this.rol = new EnjutoRolPortero(this, this.abstract_robot);
				ultimoRol = PORTERO;
			}
			else if (numRobot == 1)
			{
				abstract_robot.setDisplayString("Ramos");
				this.rol = new EnjutoRolDefensaCierre(this, this.abstract_robot);
				ultimoRol = DEFENSACIERRE;
				/*this.rol = new EnjutoRolDefensa(this, this.abstract_robot);
				ultimoRol = DEFENSA;*/
				//abstract_robot.setDisplayString("Raul");
				//this.rol = new EnjutoRolDelantero(this, this.abstract_robot);
				//ultimoRol = DELANTERO;
			}
			else if (numRobot == 2) 
			{
				/*abstract_robot.setDisplayString("Pepe");
				this.rol = new EnjutoRolDefensa(this, this.abstract_robot);
				ultimoRol = DEFENSA;*/
				abstract_robot.setDisplayString("Raul");
				this.rol = new EnjutoRolPalomero(this, this.abstract_robot);
				ultimoRol = PALOMERO;
				/*abstract_robot.setDisplayString("Raul");
				this.rol = new EnjutoRolDelantero(this, this.abstract_robot);
				ultimoRol = DELANTERO;	*/	
			}
			else if (numRobot == 3)
			{
				abstract_robot.setDisplayString("Guti");
				this.rol = new EnjutoRolCentrocampistaBloqueador(this, this.abstract_robot);
				ultimoRol = CENTROCAMPISTAAPROVECHADORDEBLOQUEOS;
			}
			else if (numRobot == 4) 
			{
				abstract_robot.setDisplayString("Raul");
				this.rol = new EnjutoRolDelantero(this, this.abstract_robot);
				ultimoRol = DELANTERO;
			}
				
			mensajesRecibidos = abstract_robot.getReceiveChannel();//COMMUNICATION
			
			//Gracias a este vector conozco que roles desempe�an mis compa�eros.
			roles = new int[5];
	
			roles[0] = PORTERO;
			roles[1] = DEFENSACIERRE;
			roles[2] = PALOMERO;
			roles[3] = CENTROCAMPISTAAPROVECHADORDEBLOQUEOS;
			roles[4] = DELANTERO;
		
		}
		else { //SIDE=-1
			int numRobot = abstract_robot.getPlayerNumber(abstract_robot.getTime());
			if (numRobot == 0) 
			{
				abstract_robot.setDisplayString("Casillas");
				this.rol = new EnjutoRolPortero(this, this.abstract_robot);
				ultimoRol = PORTERO;
			}
			else if (numRobot == 1)
			{
				abstract_robot.setDisplayString("Ramos");
				/*this.rol = new EnjutoRolDefensaCierre(this, this.abstract_robot);
				ultimoRol = DEFENSACIERRE;*/
				this.rol = new EnjutoRolDefensa(this, this.abstract_robot);
				ultimoRol = DEFENSA;
				//abstract_robot.setDisplayString("Raul");
				//this.rol = new EnjutoRolDelantero(this, this.abstract_robot);
				//ultimoRol = DELANTERO;
			}
			else if (numRobot == 2) 
			{
				abstract_robot.setDisplayString("Pepe");
				this.rol = new EnjutoRolDefensa(this, this.abstract_robot);
				ultimoRol = DEFENSA;
				/*abstract_robot.setDisplayString("Raul");
				this.rol = new EnjutoRolPalomero(this, this.abstract_robot);
				ultimoRol = PALOMERO;*/
				/*abstract_robot.setDisplayString("Raul");
				this.rol = new EnjutoRolDelantero(this, this.abstract_robot);
				ultimoRol = DELANTERO;	*/	
			}
			else if (numRobot == 3)
			{
				abstract_robot.setDisplayString("Guti");
				this.rol = new EnjutoRolCentrocampistaBloqueador(this, this.abstract_robot);
				ultimoRol = CENTROCAMPISTAAPROVECHADORDEBLOQUEOS;
			}
			else if (numRobot == 4) 
			{
				abstract_robot.setDisplayString("Raul");
				this.rol = new EnjutoRolDelantero(this, this.abstract_robot);
				ultimoRol = DELANTERO;
			}
				
			mensajesRecibidos = abstract_robot.getReceiveChannel();//COMMUNICATION
			
			//Gracias a este vector conozco que roles desempe�an mis compa�eros.
			roles = new int[5];

			roles[0] = PORTERO;
			roles[1] = DEFENSA;
			roles[2] = DEFENSA;
			roles[3] = CENTROCAMPISTAAPROVECHADORDEBLOQUEOS;
			roles[4] = DELANTERO;
			
		}

/*		roles[0] = PORTERO;
		roles[1] = DEFENSA;
		roles[2] = PALOMERO;
		roles[3] = CENTROCAMPISTAAPROVECHADORDEBLOQUEOS;
		roles[4] = DELANTERO;*/
		
		oponentesAncho = new Vec2[5];
		
		ultimoEstado = ATACAR;
		contadorCambioEstado = CICLOSPARACAMBIAR;
		
		encasillado = false;
		
		curr_time = abstract_robot.getTime();

		
		curr_time = abstract_robot.getTime();
		companeros = abstract_robot.getTeammates(curr_time);
		balon = abstract_robot.getBall(curr_time);
		ourGoal = abstract_robot.getOurGoal(curr_time);
		oponentGoal = abstract_robot.getOpponentsGoal(curr_time);
		oponentes = abstract_robot.getOpponents(curr_time);
		
		if (SIDE==-1) {
			theirLeftPost = new Vec2(oponentGoal.x,oponentGoal.y-ANCHO_PORTERIA/2);
		    theirRightPost = new Vec2(oponentGoal.x,oponentGoal.y+ANCHO_PORTERIA/2);
		    ourLeftPost = new Vec2(ourGoal.x,ourGoal.y+ANCHO_PORTERIA/2);
		    ourRightPost = new Vec2(ourGoal.x,ourGoal.y-ANCHO_PORTERIA/2);
		    ourGoalCenterLeft = new Vec2(ourGoal.x,ourGoal.y+ANCHO_PORTERIA/4);
		    ourGoalCenterRight = new Vec2(ourGoal.x,ourGoal.y-ANCHO_PORTERIA/4);
		}
		else {
			theirLeftPost = new Vec2(oponentGoal.x,oponentGoal.y+ANCHO_PORTERIA/2);
		    theirRightPost = new Vec2(oponentGoal.x,oponentGoal.y-ANCHO_PORTERIA/2);
		    ourLeftPost = new Vec2(ourGoal.x,ourGoal.y-ANCHO_PORTERIA/2);
		    ourRightPost = new Vec2(ourGoal.x,ourGoal.y+ANCHO_PORTERIA/2);
		    ourGoalCenterLeft = new Vec2(ourGoal.x,ourGoal.y-ANCHO_PORTERIA/4);
		    ourGoalCenterRight = new Vec2(ourGoal.x,ourGoal.y+ANCHO_PORTERIA/4);			
		}
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
		
		boolean balonSuelto = (distanciaJugadorMasCercanoBalon < SocSmall.RADIUS*4) && (calcularDistancia(oponentes[oponenteMasCercano], balon)<SocSmall.RADIUS*3);
		if (distanciaJugadorMasCercanoBalon < distanciaOponenteMasCercanoBalon)
			return true;
		else
			if (balonSuelto)
				return true;
			else
				return false;
	}


	public double calcularDistancia(Vec2 posBalon, Vec2 vec2) 
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
	    int minumero = abstract_robot.getPlayerNumber(curr_time);


	    if (SIDE==-1) {
		    //Modificando para que no haya objetos de m�s y para que se retarde.	    
		    ourLeftPost.setx(ourGoal.x);
		    ourLeftPost.sety(ourGoal.y+ANCHO_PORTERIA/2);
		    ourRightPost.setx(ourGoal.x);
		    ourRightPost.sety(ourGoal.y-ANCHO_PORTERIA/2);
		    ourGoalCenterLeft = new Vec2(ourGoal.x,ourGoal.y+ANCHO_PORTERIA/4);
		    ourGoalCenterRight = new Vec2(ourGoal.x,ourGoal.y-ANCHO_PORTERIA/4);
	    }
	    else {
		    ourLeftPost.setx(ourGoal.x);
		    ourLeftPost.sety(ourGoal.y-ANCHO_PORTERIA/2);
		    ourRightPost.setx(ourGoal.x);
		    ourRightPost.sety(ourGoal.y+ANCHO_PORTERIA/2);
		    ourGoalCenterLeft = new Vec2(ourGoal.x,ourGoal.y-ANCHO_PORTERIA/4);
		    ourGoalCenterRight = new Vec2(ourGoal.x,ourGoal.y+ANCHO_PORTERIA/4);	    	
	    }
		
	    if (SIDE==-1) {
	    	ourGoalAdelantado = new Vec2(ourGoal.x+0.10,ourGoal.y);
			cercaCentroDelCampoEste = new Vec2(ourGoal.x+(LONGITUD_CAMPO/2)+0.14,ourGoal.y);
			cercaCentroDelCampoOeste = new Vec2(ourGoal.x+(LONGITUD_CAMPO/2)-0.14,ourGoal.y);
	    } else {
	    	ourGoalAdelantado = new Vec2(ourGoal.x-0.10,ourGoal.y);
	    	
			cercaCentroDelCampoEste = new Vec2(ourGoal.x-(LONGITUD_CAMPO/2)+0.14,ourGoal.y);
			cercaCentroDelCampoOeste = new Vec2(ourGoal.x-(LONGITUD_CAMPO/2)-0.14,ourGoal.y);
	    }
	 
	    //Partes que solo hay que calcular una vez (solo se calcular�n para el jugador 0).
	    if (this.rol.getIdentificadorRol() == PORTERO) {
		      // Actualiza los goles a favor y en contra (si se ha marcado alg�n gol)
		    if (abstract_robot.getJustScored(curr_time) == 1) {
		        golesMarcados++;
		        actualizarResultadoPartido();
		    }
		    else if (abstract_robot.getJustScored(curr_time) == -1) {
		        golesEncajados++;
		        actualizarResultadoPartido();
		    }
	    }
	    
	    Vec2 evitaColision = null;

		
		if (contadorCambioEstado > 0) {
			contadorCambioEstado--;
		}
		
		if (oponentes.length == 5) //Ya est�n los 5 oponentes creados.
			ordenarOponentes();
			
		//Realizamos los c�lculos de la posesi�n.
		boolean pos = calcularPosesion();
		
		
		if ((!miPosesion && pos) || (miPosesion && !pos))	//Ha habido cambio de posesi�n.	
			miPosesion = pos;
		
		int estadoNuevo = -1;
		
		if (miPosesion) {
			estadoNuevo = ATACAR;
		} else {
			estadoNuevo = DEFENDER;
		}
		
		if (estadoNuevo != ultimoEstado) {
				
			//Si permitimos realizar el cambio (el contador es 0).
			if (contadorCambioEstado == 0) {
				contadorCambioEstado = CICLOSPARACAMBIAR;
				evitaColision = evitarColision(estadoNuevo);
				encasillado = false;
				this.ultimoEstado = estadoNuevo;
				estadoAtaqueODefensa = estadoNuevo;
			} else {
				evitaColision = evitarColision(ultimoEstado);
				estadoAtaqueODefensa = ultimoEstado;
			}
		} else {
			evitaColision = evitarColision(ultimoEstado);
			contadorCambioEstado = CICLOSPARACAMBIAR;
			this.ultimoEstado = estadoNuevo;
			estadoAtaqueODefensa = ultimoEstado;
		}

		/*--- Comprobando si tengo mensajes sin leer de mis compa�eros ---*/
		//COMMUNICATION
		while (mensajesRecibidos.hasMoreElements()) {
			StringMessage recvd = (StringMessage)mensajesRecibidos.nextElement();
			if (recvd.val.equals("YO CENTROCAMPISTAAPROVECHADORDEBLOQUEOS")) {
				roles[recvd.sender]=CENTROCAMPISTAAPROVECHADORDEBLOQUEOS;
			}
			else if (recvd.val.equals("YO DELANTERO")) {
				roles[recvd.sender]=DELANTERO;
			}
			else if (recvd.val.equals("YO PORTERO")) {
				roles[recvd.sender]=PORTERO;
			}
			else if (recvd.val.equals("YO DEFENSA")) {
				roles[recvd.sender]=DEFENSA;
			}
			else if (recvd.val.equals("YO DEFENSACIERRE")) {
				roles[recvd.sender]=DEFENSACIERRE;
			}
			else if (recvd.val.equals("TU CENTROCAMPISTAAPROVECHADORDEBLOQUEOS")) {
				roles[minumero]=CENTROCAMPISTAAPROVECHADORDEBLOQUEOS;
				this.cambiarRol(CENTROCAMPISTAAPROVECHADORDEBLOQUEOS);
				Message m1 = new StringMessage("YO CENTROCAMPISTAAPROVECHADORDEBLOQUEOS");
				m1.sender=minumero;
				abstract_robot.broadcast(m1);
			}
			else if (recvd.val.equals("TU DELANTERO")) {
				roles[minumero]=DELANTERO;
				this.cambiarRol(DELANTERO);
				Message m1 = new StringMessage("YO DELANTERO");
				m1.sender=minumero;
				abstract_robot.broadcast(m1);
			}
			else if (recvd.val.equals("TU DEFENSA")) {
				roles[minumero]=DEFENSA;
				this.cambiarRol(DEFENSA);
				Message m1 = new StringMessage("YO DEFENSA");
				m1.sender=minumero;
				abstract_robot.broadcast(m1);
			}
			else if (recvd.val.equals("TU DEFENSACIERRE")) {
				roles[minumero]=DEFENSACIERRE;
				this.cambiarRol(DEFENSACIERRE);
				Message m1 = new StringMessage("YO DEFENSACIERRE");
				m1.sender=minumero;
				abstract_robot.broadcast(m1);
			}
			else if (recvd.val.equals("TU PORTERO")) {
				roles[minumero]=PORTERO;
				this.cambiarRol(PORTERO);
				Message m1 = new StringMessage("YO PORTERO");
				m1.sender=minumero;
				abstract_robot.broadcast(m1);
			}
		}
		
		rol.actuarRol(estadoAtaqueODefensa);

		//Se hace despu�s de actuar, si no no se evita la colisi�n.
		if (evitaColision != null && roles[minumero]!=PORTERO)
		{
			if (estadoAtaqueODefensa!=DEFENDER) {
				abstract_robot.setSteerHeading(curr_time, evitaColision.t);
				abstract_robot.setSpeed(curr_time, evitaColision.r);
			}
		}
			
		abstract_robot.setDisplayString("R=" + roles[abstract_robot.getPlayerNumber(abstract_robot.getTime())]);	
		
		return(CSSTAT_OK);
	}
		
	private void actualizarResultadoPartido() {
		resultadoActual= golesMarcados - golesEncajados;
	}


	private Vec2 evitarColision(int estrategia){
		if (estrategia == ATACAR){
			return evitarColision(true); //Tambi�n evitamos colisiones con los oponentes.
		}
		else if (estrategia == DEFENDER){
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
		double distancias[] = new double[5];
		
		for (int i=0; i < 5; i++)
			distancias[i] = calcularDistancia(ourGoal, oponentes[i]);
		
		for (int i = 0; i<5; i++)
		{
			double maxNoAsig = -1.0;
			int maxID = -1;
			for (int j=0; j<5; j++)
			{
				if (!asignados[j] && distancias[j]>maxNoAsig)
				{
					maxNoAsig = distancias[j];
					maxID = j;
				}
			}
			asignados[maxID] = true;
			opOrdenados[i]=oponentes[maxID];
		}
		oponentes = opOrdenados;
	}


	public int calcularJugadorACubrir() 
	{	
		int elegido = 4; //Inicialmente el m�s ofensivo de los oponentes.
		
		int numJugador = abstract_robot.getPlayerNumber(curr_time);
		for (int i = 0; i < numJugador; i++)
			if ((roles[i] == DEFENSA) || (roles[i] == DEFENSACIERRE))
				elegido--;
		return elegido;
	}


	public void cubrirPase(int i)
	{
		if (oponentes.length >= i)
		{
			Vec2 vOponenteBalon = (Vec2)balon.clone(); 
			vOponenteBalon.sub(oponentes[i]);
			
			vOponenteBalon.normalize(1.0);
			double pX = (2*SocSmall.RADIUS)*vOponenteBalon.x;
			double pY = (2*SocSmall.RADIUS)*vOponenteBalon.y;

			//Entre la bola y el jugador:
			Vec2 vJugadorPosicion = new Vec2((pX + oponentes[i].x),(pY + oponentes[i].y));

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
	
	public void cubrirContra(int i)
	{
		if (oponentes.length >= i)
		{
			Vec2 vOponentePorteria = (Vec2)ourGoal.clone(); 
			vOponentePorteria.sub(oponentes[i]);
			boolean estaLejosDePorteria = vOponentePorteria.r > 2*ANCHO_CAMPO/3;
			boolean oponenteALaDerecha = oponentes[i].x > 0;
			boolean estoyEntreOponentePorteria = ((SIDE == -1 && oponenteALaDerecha) || (SIDE == 1 && !oponenteALaDerecha));

			if (estaLejosDePorteria || !estoyEntreOponentePorteria) //Recular al punto medio entre el oponente y la porter�a.
			{
				double mitadDistancia = vOponentePorteria.r/2;
				vOponentePorteria.normalize(1.0);

				double pX = mitadDistancia*vOponentePorteria.x;
				double pY = mitadDistancia*vOponentePorteria.y;

				//Entre la bola y el jugador:
				Vec2 vJugadorPosicion = new Vec2((pX + oponentes[i].x),(pY + oponentes[i].y));

				double distanciaJugPos = vJugadorPosicion.r;
				if (distanciaJugPos > 0.5*SocSmall.RADIUS)
				{
					abstract_robot.setSpeed(curr_time, 0.0);
					abstract_robot.setSteerHeading(curr_time, vJugadorPosicion.t);
					abstract_robot.setSpeed(curr_time, 0.5);
				}
				else
				{
					abstract_robot.setSteerHeading(curr_time, balon.t);
					abstract_robot.setSpeed(curr_time, 0.0);
				}
			}
			else //Est� cerca, y yo entre �l y la porter�a.
			{
				Vec2 vOponenteBalon = (Vec2)balon.clone();
				vOponenteBalon.sub(oponentes[i]);
				if (vOponenteBalon.r < 1.5*SocSmall.RADIUS) //Al que hay que cubrir, lleva el bal�n (o casi).
				{
					double angulo = abstract_robot.getSteerHeading(curr_time);
					boolean mirandoAIzquierda = angulo > Math.PI/4 && angulo < 3*Math.PI/4;
					if (abstract_robot.canKick(curr_time) && ((!mirandoAIzquierda && SIDE == -1) || (mirandoAIzquierda && SIDE == 1)))
						abstract_robot.kick(curr_time);
					else
					{
						abstract_robot.setSpeed(curr_time, 0.2);
						abstract_robot.setSteerHeading(curr_time, balon.t);
						abstract_robot.setSpeed(curr_time, 0.7);
					}
				}
				else
					cubrirPase(i);
			}
		}
	}
	
	public void showDatos() {
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
	}
	
	public void printVectorVec2(Vec2[] vector) {
		for (int i=0; i< vector.length; i++)	
			System.out.println("Vector Op[" + i + "] --> (" +vector[i].x + "," + vector[i].y + "). R=" + vector[i].r + " . T=" + vector[i].t + ". ");
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
	 * Devuelve el jugador del equipo rival que est� m�s atr�s en el campo (el que har�a las veces de portero).
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
	
	public void goToBall() {
		abstract_robot.setSteerHeading(curr_time, balon.t);
		abstract_robot.setSpeed(curr_time, 1.0);

		if (abstract_robot.canKick(curr_time))
			abstract_robot.kick(curr_time);

	}
	
	public int devolverDefensaMasDefensivoNoBloqueado() {
		//Si no hay cierre, que remedio, se cambia por un defensa.
		for (int i=0;i<5;i++) {
			if (roles[i] == DEFENSA) {
				return i;
			}
		}
		return -1; //Si sale del for devolver -1, no encontrado
	}

	public int devolverDefensaCierreMasDefensivoNoBloqueado() {
		//int deMomentoPlayer=-1;
		//int deMomentoDistancia=1000;
		for (int i=0;i<5;i++) {
			if (roles[i] == DEFENSACIERRE) {
				return i;
			}
		}
		return -1; //Si sale del for devolver -1, no encontrado
	}
	
	public Vec2 evitarColision(boolean evitarOponentes )
	{
		//Comprobamos cercan�a a nuestros compa�eros y actualizamos.
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
		else 
			if (evitarOponentes) //Tambi�n queremos evitar colisiones con los oponentes.
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

	public Vec2 calcularMasCercano(Vec2 posicion, Vec2[] candidatos) 
	{
		double dist = 9999;
		Vec2 result = new Vec2(0, 0);
		Vec2 temp = new Vec2(0, 0);

		for( int i=0; i < candidatos.length; i++)
		{
			temp.sett( candidatos[i].t);
			temp.setr( candidatos[i].r);
			temp.sub(posicion);
			
			if(temp.r < dist)
			{
				result = candidatos[i];
				dist = temp.r;
			}
		}
		return result;
	}
	
	public boolean lejosDeTuArea() {
		if (SIDE==-1) {
			if ( ourGoal.x < -0.45)
				return true;
			else
				return false;
		} else {
			if ( ourGoal.x > 0.45)
				return true;
			else
				return false;			
		}
	}

	
	public boolean estasEnBanda() {
		//Igual para este y oeste.
		if ( ourGoal.y > 0.35 || ourGoal.y <-0.35)
			return true;
		else
			return false;
	}

	//Derecha = Abajo (si fuera siempre Oeste) en otras funciones.
	public boolean estasEnBandaDerecha() {
		if (SIDE==-1) {
			if ( ourGoal.y > 0.35)
				return true;
			else
				return false;
		}
		else {
			if ( ourGoal.y < -0.35)
				return true;
			else
				return false;			
		}
	}
	
	//Izquierda = Arriba (si fuera siempre Este) en otras funciones.
	public boolean estasEnBandaIzquierda() {
		if (SIDE==-1) {
			if ( ourGoal.y < -0.35) 
				return true;
			else
				return false;
		}
		else {
			if ( ourGoal.y > 0.35)
				return true;
			else
				return false;			
		}
	}
	
	public boolean estasMuyEnBanda() {
		if ( ourGoal.y > 0.45 || ourGoal.y <-0.45)
			return true;
		else
			return false;
	}
	
	//Para este y oeste.
	public boolean balonCercaAreaPropia() {
		if (SIDE==-1) {
			if (ballMenosOurGoal.x < 0.65) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if (ballMenosOurGoal.x > -0.65) {
				return true;
			}
			else {
				return false;
			}			
		}
	}
	
	//Para este y oeste.
	public boolean balonMuyCercaAreaPropia() {
		if (SIDE==-1) {
			if (ballMenosOurGoal.x < 0.35) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if (ballMenosOurGoal.x < -0.35) {
				return true;
			}
			else {
				return false;
			}			
		}
	}

	public boolean balonDemasiadoLejosDeAreaPropia() {
		if (SIDE==-1) {
			if (ballMenosOurGoal.x < 2.05) {
				return false;
			}
			else {
				return true;
			}
		}
		else {
			if (ballMenosOurGoal.x < -2.05) {
				return false;
			}
			else {
				return true;
			}			
		}
	}
	
	public boolean balonAvanzandoEscorado() {
		if (ballMenosOurGoal.y > 0.25 || ballMenosOurGoal.y < -0.25) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean balonAvanzandoEscoradoBandaAbajo() {
		if (SIDE==-1) {
			if (ballMenosOurGoal.y < -0.25) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if (ballMenosOurGoal.y > 0.25) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	public boolean balonAvanzandoEscoradoBandaArriba() {
		if (SIDE==-1) {
			if (ballMenosOurGoal.y > 0.25) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if (ballMenosOurGoal.y < -0.25) {
				return true;
			}
			else {
				return false;
			}			
		}
	}
	
	public boolean balonAvanzandoCentrado() {
		// Vale para Este y Oeste.
		if (ballMenosOurGoal.y < 0.25 && ballMenosOurGoal.y > -0.25) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public Vec2 getVectorResta(Vec2 vector1, Vec2 vector2) 
	{
		Vec2 v = (Vec2)vector1.clone();
		v.sub(vector2);
		return v;
	}

	
	public boolean estasBajoPalos() {
		if (ourGoal.r<0.15) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean estasBajoPalosEnX() {
		if (SIDE==-1) {
			if (ourGoal.x > -0.15) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if (ourGoal.x < 0.15) {
				return true;
			}
			else {
				return false;
			}			
		}
	}

	public boolean estasBajoPalosEnY() {
		if (ourGoal.y>-0.5 && ourGoal.y<0.5) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean estasEnElCentroVerticalDelCampo() {
		if (ourGoal.y<0.05 && ourGoal.y>-0.05) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean estasEnOurCenterLeft() {
		if (ourGoalCenterLeft.r <0.05)
			return true;
		else
			return false;
	}
	
	public boolean estasEnOurCenterRight() {
		if (ourGoalCenterRight.r <0.05)
			return true;
		else
			return false;
	}
	
	public boolean estaEnVector(Vec2 vector) {
		//Tanto para este como para oeste.
		if (vector.r <0.07)
			return true;
		else
			return false;
	}

	public boolean estaCercaDeVector(Vec2 vector) {
		//Tanto para este como para oeste.
		if (vector.r <0.17)
			return true;
		else
			return false;
	}
	
	public boolean tienesBalonPorDelanteYCerca() {
		//Tanto para este como para oeste.
		if (SIDE==-1) {
			if (balon.x > -0.05 && balon.r < 0.25)
				return true;
			else
				return false;
		}
		else {
			//>0... o >-0.05 cuando est� casi.
			if (balon.x < 0.05 && balon.r < 0.25)
				return true;
			else
				return false;
		}
	}
	
	
	public int devolverDelanteroMenosOfensivo() {
		for (int i=0;i<5;i++) {
			if (roles[i] == DELANTERO)
				return i;
		}
		return -1; //Si sale del for devolver -1, no encontrado
	}
	
	public void cambiarRol(int nuevoRol){
		ultimoRol = rol.identificadorRol;
		
		switch (nuevoRol) {
		case PORTERO:
			this.rol = new EnjutoRolPortero(this, this.abstract_robot);
			break;
		case DEFENSA:
			this.rol = new EnjutoRolDefensa(this, this.abstract_robot);
			break;
		case DEFENSACIERRE:
			this.rol = new EnjutoRolDefensaCierre(this, this.abstract_robot);
			break;
		case CENTRO:
			//this.rol = new EnjutoRolCentro();
			break;
		case DELANTERO:
			this.rol = new EnjutoRolDelantero(this, this.abstract_robot);
			break;
		case CENTROCAMPISTAAPROVECHADORDEBLOQUEOS:
			this.rol = new EnjutoRolCentrocampistaBloqueador(this, this.abstract_robot);
			break;
		case PALOMERO:
			this.rol = new EnjutoRolPalomero(this, this.abstract_robot);
			break;
		}
	}
	
	public void volverAlAnteriorRol(){
		
		if (ultimoRol == rol.identificadorRol) {
		} else {

			switch (ultimoRol) {
			case PORTERO:
				ultimoRol = rol.identificadorRol;
				this.rol = new EnjutoRolPortero(this, this.abstract_robot);
				break;
			case DEFENSA:
				ultimoRol = rol.identificadorRol;
				this.rol = new EnjutoRolDefensa(this, this.abstract_robot);
				break;
			case DEFENSACIERRE:
				ultimoRol = rol.identificadorRol;
				this.rol = new EnjutoRolDefensaCierre(this, this.abstract_robot);
				break;
			case CENTRO:
				ultimoRol = rol.identificadorRol;
				//this.rol = new EnjutoRolCentro();
				break;
			case DELANTERO:
				ultimoRol = rol.identificadorRol;
				this.rol = new EnjutoRolDelantero(this, this.abstract_robot);
				break;
			case CENTROCAMPISTAAPROVECHADORDEBLOQUEOS:
				ultimoRol = rol.identificadorRol;
				this.rol = new EnjutoRolCentrocampistaBloqueador(this, this.abstract_robot);
				break;
			case PALOMERO:
				ultimoRol = rol.identificadorRol;
				this.rol = new EnjutoRolPalomero(this, this.abstract_robot);
				break;
			}
		}
		
	}
	
	public boolean demasiadoAdelantado() {
		if (SIDE==-1) {
			if (ourGoal.x<-0.15)
				return true;
			else 
				return false;
		}
		else {
			if (ourGoal.x>0.15)
				return true;
			else 
				return false;		
		}
	}
	
	public void conducirBalon()
	{    
	     Vec2 trayectoria;
	     trayectoria = new Vec2(balon);
	     trayectoria.sub(oponentGoal);
	     trayectoria.setr(0.054);
	     trayectoria.add(balon);
	            
	     trayectoria.normalize(1.0);
	     abstract_robot.setSteerHeading(curr_time, trayectoria.t);
	     
	     if(!abstract_robot.canKick(curr_time))
	     {
	       Vec2 resultado = new Vec2();
	       Vec2 result = evitarColision(true);
	       if (result != null)
	    	   abstract_robot.setSteerHeading(curr_time,resultado.t);
	       abstract_robot.setSpeed(curr_time, 1.0);
	     }
	  }

}