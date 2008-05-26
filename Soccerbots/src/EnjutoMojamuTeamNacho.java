

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


public class EnjutoMojamuTeamNacho extends ControlSystemSS
{
	  static final int PORTERO = 0;
	  static final int DEFENSA = 1;
	  static final int CENTRO = 2;
	  static final int DELANTERO = 3;
	  
	  static final int SINPOSESION = 0;
	  static final int POSESION = 1;
	  static final int BALONSUELTO = 2;

	  // Dimensiones del campo y de las porterías (Del equipo PROFES1, comprobar, porque alguno utiliza otros valores porque tal vez interese)
	  static final double ANCHO_CAMPO = 1.525;
	  static final double LONGITUD_CAMPO = 2.74;
	  static final double ANCHO_PORTERIA = 0.45; //En teoría 0.5, peor tal vez nos interese esta distancia.
	  
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
	private Vec2 balon, ourGoal, oponentGoal, ballMenosOurGoal ;
	private Vec2 theirLeftPost,theirRightPost, ourLeftPost, ourRightPost, ourGoalCenterLeft, ourGoalCenterRight;
	private long curr_time;
	private int[] roles;
	
	//Prueba para defender.
	private int numNiCaso;
	
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
			abstract_robot.setDisplayString("Raúl");
		else if (numRobot == 4)
			abstract_robot.setDisplayString("Guti.Haz");
		
		numNiCaso = 0;
		
		roles = new int[5];
		roles[0] = PORTERO;
		roles[1] = DEFENSA;
		roles[2] = DEFENSA;
		roles[3] = DEFENSA;
		roles[4] = DEFENSA;
		
		oponentesAncho = new Vec2[5];
		
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
		if (oponentes.length == 5) //Ya están los 5 oponentes creados.
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
			if (true) {
				tacticaAbsurda();
			}
			else {			
				boolean pos = calcularPosesion();
				if (!miPosesion && pos)
				{
					miPosesion = pos;
				}
				else if (miPosesion && !pos)
				{
					miPosesion = pos;
				}
				if (miPosesion)
					atacar();
				else if (!miPosesion)
					defender();
			}
		}
		
		//System.out.println(abstract_robot.getOpponents(curr_time).toString());
		//showDatos();
		// tell the parent we're OK
		return(CSSTAT_OK);
	}

	/**
	 * Ordena el vector de oponentes desde 
	 * el más lejano a nuestra portería, al más cercano.
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
			System.out.println("Avanzando centrado, con y = " + ballMenosOurGoal.y);
			return true;
		}
		else {
			System.out.println("Avanzando escorado, con y = " + ballMenosOurGoal.y);
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
		if (ourGoalCenterLeft.r <0.15)
			return true;
		else
			return false;
	}
	
	private boolean estasEnOurCenterRight() {
		if (ourGoalCenterRight.r <0.15)
			return true;
		else
			return false;
	}
	
	//Método a mejorar.
	private void actuarPortero() {
	
		//Comprobar si está sufriendo un bloqueo.
		
			//Si es así intentar evitar colision, y posteriormente ir hacia la portería.
		
		//Permitirle salir del area, si el equipo rival está muy defensivo.
		
		//En mano a mano no ir directamente a la bola, sin ir a la bola sin perder de vista la portería.

// Antes:
		/*if (curr_time<100) {
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
			System.out.println("Curr_time menos que 100");
			System.out.println("PORTERO: estadoInicial");
			estadoPortero = 0;
		}*/
		if ( estasEnBanda() || lejosDeTuArea() /*|| !estasBajoPalosEnY()*/ ) {
			System.out.println("PORTERO: estasEnBanda o lejosDeTuArea");
			// set speed at minimum
			if (estadoPortero != 1)
				abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
			estadoPortero = 1;
		}
		else if (balonCercaAreaPropia() && balonAvanzandoCentrado()) {
			System.out.println("PORTERO: balonCercaAreaPropia y balón avanzando centrado");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.75	
			estadoPortero = 2;
		}
		else if (balonCercaAreaPropia() && balonAvanzandoEscoradoBandaArriba()) {
			System.out.println("PORTERO: balonCercaAreaPropia y balón avanzando escorado banda arriba");
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourLeftPost.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 3;
		}
		else if (balonCercaAreaPropia() && balonAvanzandoEscoradoBandaAbajo()) {
			System.out.println("PORTERO: balonCercaAreaPropia y balón avanzando escorado banda abajo");
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourRightPost.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 4;
		}
		else if (!balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && !estasEnOurCenterLeft()) {
			System.out.println("PORTERO: !balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && !estasEnOurCenterLeft()");
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoalCenterLeft.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 31;
		}
		else if (!balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && !estasEnOurCenterLeft()) {
			System.out.println("PORTERO: !balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && !estasEnOurCenterLeft()");
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoalCenterLeft.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 31;
		}
		/*
		else if (balonCercaAreaPropia() && balonAvanzandoEscoradoBandaAbajo()) {
			System.out.println("PORTERO: balonCercaAreaPropia y balón avanzando escorado banda abajo");
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourRightPost.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 41;
		}
		//ANTES:
		//else if (estasBajoPalos() && !estasEnElCentroVerticalDelCampo()) {
		//	//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
		//	System.out.println("PORTERO: Estás bajo palos pero no en el centro vertical del campo");
		//	abstract_robot.setSpeed(curr_time, 1);
		//	abstract_robot.setSteerHeading(curr_time, ourGoal.t);
		//}
		else if (!balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && estasBajoPalosEnY()) {
			System.out.println("PORTERO: !balonCercaAreaPropia y !balonDemasiadoLejosDeAreaPropia");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
			estadoPortero=5;
		}
		else if (!balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && !estasBajoPalosEnY()) {
			System.out.println("PORTERO: !balonCercaAreaPropia y !balonDemasiadoLejosDeAreaPropia");
			if (estadoPortero!=6)	
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero=6;
		}
		else if (!balonCercaAreaPropia() && balonDemasiadoLejosDeAreaPropia()) {
			System.out.println("PORTERO: !balonCercaAreaPropia y sí balonDemasiadoLejosDeAreaPropia");
			if (estadoPortero!=7)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5	
			estadoPortero=7;			
		}
		//else if (estasBajoPalos() && estasEnElCentroVerticalDelCampo()) {
		//	//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
		//	System.out.println("PORTERO: Estás bajo palos y en el centro vertical del campo");
		//	abstract_robot.setSpeed(curr_time, 0);
		//	abstract_robot.setSteerHeading(curr_time, balon.t);
		//}
		else if (estasBajoPalos() && balonDemasiadoLejosDeAreaPropia() && maxCiclosSeguirY>0) {
			maxCiclosSeguirY--;
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			System.out.println("PORTERO: Estás bajo palos y el balon demasiado lejos del area propia. Sigues la y de la bola como maximo " + maxCiclosSeguirY +" ciclos más.");
			abstract_robot.setSpeed(curr_time, 0);
			Vec2 nuevaDireccion= new Vec2(0,ballMenosOurGoal.y);
			//nuevaDireccion.normalize(1);
			abstract_robot.setSteerHeading(curr_time, nuevaDireccion.t);
			abstract_robot.setSpeed(curr_time, 1);
			//maximoSeguirYBola;
		}
		else if (estasBajoPalos() && balonDemasiadoLejosDeAreaPropia() && maxCiclosSeguirY==0) {
			System.out.println("PORTERO: Te cansas de seguir y, y simplemente miras quieto.");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			double rnd = Math.random();
			if (rnd >0.95) {
				maxCiclosSeguirY = 10;
				System.out.println("El siguiente paso puede que vuelvas a seguir y.");
			}
		}
		else if (estasBajoPalos() && !balonDemasiadoLejosDeAreaPropia()) {
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			System.out.println("PORTERO: Estás bajo palos y el balon demasiado lejos del area propia. Sigues la y de la bola como maximo unos ciclos más.");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			//maximoSeguirYBola;
		}
		else if (!estasBajoPalosEnY() && estasBajoPalosEnX()) {
			System.out.println("PORTERO: Estás bajo palos en X pero no en y.");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
		}
		//Prueba
		else if (!estasBajoPalos()) {
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			System.out.println("PORTERO: Estás bajo palos pero no en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
		}
		else if (estasBajoPalos() && !estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			System.out.println("PORTERO: Estás bajo palos pero no en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
		}
		else if (estasBajoPalos() && estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			System.out.println("PORTERO: Estás bajo palos y en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
		}*/
		else {
			
			System.out.println("PORTERO: default: ir a tu portería...");
			abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);		
			
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
/*
			System.out.println("PORTERO: Por defecto, estás parado.");
			//System.out.println("PORTERO: Por defecto, estás bajo palos y sigues la y de la bola.");
			abstract_robot.setSpeed(curr_time, 0);
			//Importante: Para saber si está escorado o no se utiliza ballMenosOurGoal, pero para seguir la dirección de la y se utiliza ball.y.
//			Vec2 nuevaDireccion= new Vec2(0,balon.y);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0);*/
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
					System.out.println("**** KEEPER -->  ourGoal.x = " + ourGoal.x);
				}
				else {
					abstract_robot.setSpeed(curr_time, 0);
					abstract_robot.setSteerHeading(curr_time, balon.t);
				}
			}
		}*/
	}


	private void defender() 
	{
		
		//Incluso comentando esto se ve más gráfico, con el portero cubriendo también.
			//Variante: Solo cubrir a su jugador más ofensivo.
/*			if ( calcularOponenteMasOfensivo(oponentes) == abstract_robot.getPlayerNumber(curr_time)) {
				numNiCaso = abstract_robot.getPlayerNumber(curr_time);
				System.out.println("Orden recibida: Voy a por su jugador más ofensivo: " + numNiCaso + ".");
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
			candidatos[i] = oponentes[4-i]; //Candidatos queda ordenado por cercanía a la portería.
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
		
		return numerosVectorFin[masAltos];
	}


	private void cubrir(int i)
	{
		abstract_robot.setDisplayString("al " + i);
		//Nunca entendido, de hecho he puesto ahora >= para que cubra también el jugador 4.
		if (oponentes.length >= i)
		{
			//Vec2 oponente = dameOponenteID(i);
		
			Vec2 vOponenteBalon = (Vec2)balon.clone(); 
			vOponenteBalon.setx(vOponenteBalon.x - oponentes[i].x);
			vOponenteBalon.sety(vOponenteBalon.y - oponentes[i].y);
			//Hacemos que el vector sea unitario, porque se multiplicará después por el 2*radio (Menuda diferencia!!).
			vOponenteBalon.normalize(1.0);
			
			//System.out.println("La medida de 2*Radius es = " + (2*SocSmall.RADIUS));
			
			//Sí, multiplicar, porque lo que quieres es una parte del vector, para hacer la corrección.
			double pX = (2*SocSmall.RADIUS)*vOponenteBalon.x;
			double pY = (2*SocSmall.RADIUS)*vOponenteBalon.y;
			Vec2 posicionJugador = abstract_robot.getPosition(curr_time);
			//Antes
				//Vec2 vJugadorPosicion = new Vec2((pX - posicionJugador.x),(pY - posicionJugador.y));
			//Ahora
			
			//No vale, prueba inicial..
			//Vec2 vJugadorPosicion = new Vec2((pX + posicionJugador.x),(pY + posicionJugador.y));
			
			//Yendo a la bola, pero con corrección de ir "por donde vaya el rival" (poco útil) 
			//Vec2 vJugadorPosicion = new Vec2((pX + balon.x),(pY + balon.y));

			//Directamente al jugador rival (bloqueas más):
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
		if (abstract_robot.getPlayerNumber(curr_time) != 1)
		{
			double radio = balon.r;
			double robot = 1.2*abstract_robot.RADIUS;
			if (radio > robot)
			{
			// set heading towards it
				abstract_robot.setSpeed(curr_time, 1.0);
				abstract_robot.setSteerHeading(curr_time, balon.t);
			}
			else
			{
				abstract_robot.setSpeed(curr_time, 0.7);
				abstract_robot.setSteerHeading(curr_time, oponentGoal.t);
				abstract_robot.setSpeed(curr_time, 1.0);
				System.out.println("GIRA!!");
			}
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);

			// kick it if we can
			double distancia = calcularDistancia(balon, oponentGoal);
			if (abstract_robot.canKick(curr_time) && distancia < 0.5)
			{
				abstract_robot.kick(curr_time);
				System.out.print("TIRA!!");
			}
		}
		else
		{
			cubrir(4);
		}
	}
	
	public void showDatos() {
//		if (abstract_robot.getPlayerNumber(curr_time) == 0) {
			long curr_timeAux = abstract_robot.getTime();
			Vec2[] oponentesAux = abstract_robot.getOpponents(curr_time);
			Vec2[] companerosAux = abstract_robot.getTeammates(curr_time);
			Vec2 balonAux = abstract_robot.getBall(curr_time);
			
			System.out.println("Listado de datos disponibles, desde jugador: " + (abstract_robot.getPlayerNumber(curr_time)) + ".");
			System.out.println("Compañeros:");
			printVectorVec2(companerosAux);
			System.out.println("Oponentes");
			printVectorVec2(oponentesAux);
			System.out.println("Posicion del balón: (" +balonAux.x + "," + balonAux.y + "). R=" + balonAux.r + " . T=" + balonAux.t + ". ");			
			System.out.println("Jugador más defensivo de nuestro equipo: "+calcularCompaneroMasDefensivo(companerosAux));
			System.out.println("Jugador más ofensivo de nuestro equipo: "+calcularCompaneroMasOfensivo(companerosAux));
			System.out.println("Jugador más defensivo del oponente: "+calcularOponenteMasDefensivo(oponentesAux));
			System.out.println("Jugador más ofensivo del oponente: "+calcularOponenteMasOfensivo(oponentesAux));
			System.out.println("__________________________________________________________________________");

//		}
	}
	
	public void printVectorVec2(Vec2[] vector) {
		for (int i=0; i< vector.length; i++)		
		{
			System.out.println("Vector Op[" + i + "] --> (" +vector[i].x + "," + vector[i].y + "). R=" + vector[i].r + " . T=" + vector[i].t + ". ");
		}
		//System.out.println("__________________________________________________________________________");
	}
	
	/**
	 * Devuelve el jugador de EnjutoMojamuteam que está más atrás en el campo (el que haría las veces de portero).
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
	 * Devuelve el jugador de EnjutoMojamuteam que está más adelantado en el campo (el que haría las veces de delantero).
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
	 * Devuelve el jugador de EnjutoMojamuteam que está más atrás en el campo (el que haría las veces de portero).
	 */
	public int calcularOponenteMasDefensivo(Vec2[] vectorOponentes) {
		int masDefensivo=0; //Se inicia a 0 el más defensivo, y se comapra respecto al resto.
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
	 * Devuelve el jugador del equipo rival que está más adelantado en el campo (el que haría las veces de delantero).
	 */
	public int calcularOponenteMasOfensivo(Vec2[] vectorOponentes) {
		int masOfensivo=0; //Se inicia a 0 el más defensivo, y se comapra respecto al resto.
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
	
	/**
	 * Devuelve el jugador de EnjutoMojamuteam que está más atrás en el campo (el que haría las veces de portero).
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
	 * Devuelve el jugador del equipo rival que está más adelantado en el campo (el que haría las veces de delantero).
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
}
