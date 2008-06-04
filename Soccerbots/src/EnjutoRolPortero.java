import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.communication.CommunicationException;
import EDU.gatech.cc.is.communication.Message;
import EDU.gatech.cc.is.communication.StringMessage;
import EDU.gatech.cc.is.util.Vec2;


public class EnjutoRolPortero extends EnjutoRol {
	
	private static int porteroBloqueado;
	private int estadoPortero;
	private int maxCiclosSeguirY;
	
	public EnjutoRolPortero(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 0;
		this.abstract_robot = robot;
		estadoPortero=0;
	    maxCiclosSeguirY = 10;
		porteroBloqueado = 0;
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		actuarPortero();
	}
	
	private void actuarPortero() {
		
		int estadoPorteroAnterior = estadoPortero;
		
		long curr_time = jugador.curr_time;
		Vec2 balon = jugador.balon;
		Vec2 ourGoal = jugador.ourGoal;
		int SIDE = jugador.SIDE;
		
		//Comprobar si está sufriendo un bloqueo.
		
			//Si es así intentar evitar colision, y posteriormente ir hacia la portería.
		
		//Permitirle salir del area, si el equipo rival está muy defensivo.
		
		//En mano a mano no ir directamente a la bola, sin ir a la bola sin perder de vista la portería.

// Antes:
		if (jugador.curr_time<1800) {
			if (estadoPortero != 0)
				abstract_robot.setSpeed(jugador.curr_time, 0);
			abstract_robot.setSteerHeading(jugador.curr_time, jugador.ourGoal.t);
			
			// set speed at maximum
			abstract_robot.setSpeed(jugador.curr_time, 1.0);
			//System.out.println("Curr_time menos que 100");
			//System.out.println("PORTERO: estadoInicial");
			estadoPortero = 0;
		}
		else if ( /*estasEnBanda() ||*/ jugador.lejosDeTuArea() && !jugador.estaCercaDeVector(balon) /*|| !estasBajoPalosEnY()*/ ) {
			//System.out.println("PORTERO: estasEnBanda o lejosDeTuArea");
			// set speed at minimum
			if (estadoPortero != 1)
				abstract_robot.setSpeed(jugador.curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(jugador.curr_time, jugador.ourGoal.t);
			// set speed at maximum
			abstract_robot.setSpeed(jugador.curr_time, 1.0);
			estadoPortero = 1;
		}
		else if ( jugador.estasEnBandaDerecha() ) {
			System.out.println("PORTERO: estas en banda derecha");
			// set speed at minimum
			if (estadoPortero != 11)
				abstract_robot.setSpeed(jugador.curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(jugador.curr_time, jugador.ourRightPost.t);
			// set speed at maximum
			abstract_robot.setSpeed(jugador.curr_time, 1.0);
			estadoPortero = 11;
		}
		else if ( jugador.estasEnBandaIzquierda() ) {
			System.out.println("PORTERO: estas en banda izquierda");
			// set speed at minimum
			if (estadoPortero != 111)
				abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, jugador.ourLeftPost.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
			estadoPortero = 111;
		}
		else if ( (jugador.balonCercaAreaPropia() /*|| tienesBalonPorDelanteYCerca()*/ ) && jugador.balonAvanzandoCentrado()) {
			//System.out.println("PORTERO: balonCercaAreaPropia y balón avanzando centrado");
			Vec2 balonConCorreccionOfensiva = new Vec2();
			if (SIDE==-1) {
				balonConCorreccionOfensiva.setx(balon.x-0.2*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);
			}
			else {
				balonConCorreccionOfensiva.setx(balon.x+0.2*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);
			}
			if (estadoPortero != 2)
				abstract_robot.setSpeed(curr_time, 0);
			//abstract_robot.setSpeed(curr_time, 0);
			//Antes
			//abstract_robot.setSteerHeading(curr_time, balon.t);
			//Ahora
			//balonConCorreccionOfensiva.normalize(1);
			abstract_robot.setSteerHeading(curr_time, balonConCorreccionOfensiva.t);
			abstract_robot.setSpeed(curr_time, 0.9); //Antes 0.75	
			if (balon.x>0 && abstract_robot.canKick(curr_time)) {
				System.out.println("Portero: ¡¡Despejo!!");
				abstract_robot.kick(curr_time);
			}
			else {
				//System.out.println("CUIDADO CON GOL EN PROPIA");
			}
			estadoPortero = 2;
			
			//System.out.println("EN CORRECCIÓN OFENSIVA!!!!!!!!!");
		}
		else if (jugador.balonCercaAreaPropia() && jugador.balonAvanzandoEscoradoBandaArriba()) {
			Vec2 balonConCorreccionOfensiva = new Vec2();
			if (SIDE==-1) {
				balonConCorreccionOfensiva.setx(balon.x-0.33*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);
			}
			else {
				balonConCorreccionOfensiva.setx(balon.x+0.33*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);
			}
			//System.out.println("PORTERO: balonCercaAreaPropia y balón avanzando escorado banda arriba");
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			//ANTES:
			//abstract_robot.setSteerHeading(curr_time, ourLeftPost.t);
			//AHORA (con corrección):
			abstract_robot.setSteerHeading(curr_time, balonConCorreccionOfensiva.t);
			abstract_robot.setSpeed(curr_time, 0.9); //Antes 0.5
			estadoPortero = 3;
		}
		else if (jugador.balonCercaAreaPropia() && jugador.balonAvanzandoEscoradoBandaAbajo()) {
			//System.out.println("PORTERO: balonCercaAreaPropia y balón avanzando escorado banda abajo");
			Vec2 balonConCorreccionOfensiva = new Vec2();
			if (SIDE==-1) {
				if (balon.y >0.2) {
					balonConCorreccionOfensiva.setx(balon.x-0.45*balon.x);
					balonConCorreccionOfensiva.sety(balon.y);					
				}
				else {
					balonConCorreccionOfensiva.setx(balon.x-0.33*balon.x);
					balonConCorreccionOfensiva.sety(balon.y);
				}
			}
			else {
				if (balon.y >0.2) {
					balonConCorreccionOfensiva.setx(balon.x+0.45*balon.x);
					balonConCorreccionOfensiva.sety(balon.y);					
				}
				else {
					balonConCorreccionOfensiva.setx(balon.x+0.33*balon.x);
					balonConCorreccionOfensiva.sety(balon.y);
				}
			}
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			balonConCorreccionOfensiva.normalize(3);
			//ANTES:
			//abstract_robot.setSteerHeading(curr_time, ourRightPost.t);
			//AHORA (con corrección):
			abstract_robot.setSteerHeading(curr_time, balonConCorreccionOfensiva.t);
			abstract_robot.setSpeed(curr_time, 0.9); //Antes 0.5
			estadoPortero = 4;
		}
		//Añadido ultimamente el balonDemasiadoLejosDeAreaPropia
		else if (!jugador.balonCercaAreaPropia() && jugador.balonDemasiadoLejosDeAreaPropia() && !jugador.estasEnElCentroVerticalDelCampo()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && !estasEnElCentroVerticalDelCampo()");
			System.out.println("Mirando a OurGoal");
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 4;
		}
		else if (!jugador.balonCercaAreaPropia() && jugador.estasEnElCentroVerticalDelCampo() && jugador.demasiadoAdelantado()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && estasEnElCentroVerticalDelCampo()");
			System.out.println("OUR GOAL ADELANTADO");
			if (estadoPortero!=423)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoalAdelantado.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 423;
		}
		else if (!jugador.balonCercaAreaPropia() && jugador.estasEnElCentroVerticalDelCampo() && !jugador.demasiadoAdelantado()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && estasEnElCentroVerticalDelCampo()");
//			System.out.println("Portero Parado");
			if (estadoPortero!=423)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
			estadoPortero = 423;
		}
		else if (!jugador.balonCercaAreaPropia() && !jugador.balonDemasiadoLejosDeAreaPropia() && jugador.balonAvanzandoEscoradoBandaArriba() && !jugador.estasEnOurCenterLeft()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && !estasEnOurCenterLeft()");
			if (estadoPortero!=311)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoalCenterLeft.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 311;
		}
		else if (!jugador.balonCercaAreaPropia() && !jugador.balonDemasiadoLejosDeAreaPropia() && jugador.balonAvanzandoEscoradoBandaAbajo() && !jugador.estasEnOurCenterRight()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && !estasEnOurCenterLeft()");
			if (estadoPortero!=312)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoalCenterRight.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 312;
		}
		else if (!jugador.balonCercaAreaPropia() && !jugador.balonDemasiadoLejosDeAreaPropia() && jugador.balonAvanzandoEscoradoBandaArriba() && jugador.estasEnOurCenterLeft()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && balonAvanzandoEscoradoBandaArriba() && ya estasEnOurCenterLeft()");
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
			estadoPortero = 311;
		}
		
		else if (jugador.balonCercaAreaPropia() && jugador.balonAvanzandoEscoradoBandaAbajo()) {
			//System.out.println("PORTERO: balonCercaAreaPropia y balón avanzando escorado banda abajo");
			System.out.println("AUN ACTIVO!!!!!!!!!!!!!!!!!!!!!");
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourRightPost.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 41;
		}
		//ANTES:
		else if (jugador.estasBajoPalos() && !jugador.estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Estás bajo palos pero no en el centro vertical del campo");
			if (estadoPortero!=89)
				abstract_robot.setSpeed(curr_time, 0);			
			abstract_robot.setSpeed(curr_time, 1);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			estadoPortero=89;
		}
		
		// 03/06/2008  -> 21:36 Estaba puesto, ahora comentado.
		
		/*else if (!balonCercaAreaPropia() && !balonDemasiadoLejosDeAreaPropia() && estasBajoPalosEnY()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia y !balonDemasiadoLejosDeAreaPropia");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
			estadoPortero=5;
		}*/
		else if (!jugador.balonCercaAreaPropia() && !jugador.balonDemasiadoLejosDeAreaPropia() && !jugador.estasBajoPalosEnY()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia y !balonDemasiadoLejosDeAreaPropia");
			if (estadoPortero!=6)	
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero=6;
		}
		else if (!jugador.balonCercaAreaPropia() && jugador.balonDemasiadoLejosDeAreaPropia()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia y sí balonDemasiadoLejosDeAreaPropia");
			if (estadoPortero!=7)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 0.2); //Antes 0.5	
			estadoPortero=7;			
		}
		//else if (estasBajoPalos() && estasEnElCentroVerticalDelCampo()) {
		//	//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
		//	//System.out.println("PORTERO: Estás bajo palos y en el centro vertical del campo");
		//	abstract_robot.setSpeed(curr_time, 0);
		//	abstract_robot.setSteerHeading(curr_time, balon.t);
		//}
		
		/*else if (estasBajoPalos() && balonDemasiadoLejosDeAreaPropia() && maxCiclosSeguirY>0) {
			maxCiclosSeguirY--;
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Estás bajo palos y el balon demasiado lejos del area propia. Sigues la y de la bola como maximo " + maxCiclosSeguirY +" ciclos más.");
			abstract_robot.setSpeed(curr_time, 0);
			Vec2 nuevaDireccion= new Vec2(0,ballMenosOurGoal.y);
			//nuevaDireccion.normalize(1);
			abstract_robot.setSteerHeading(curr_time, nuevaDireccion.t);
			abstract_robot.setSpeed(curr_time, 1);
			//maximoSeguirYBola;
		}*/
		else if (jugador.estasBajoPalos() && jugador.balonDemasiadoLejosDeAreaPropia() && maxCiclosSeguirY==0) {
			if (estadoPortero!=921)
				abstract_robot.setSpeed(curr_time, 0);			
			//System.out.println("PORTERO: Te cansas de seguir y, y simplemente miras quieto.");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			double rnd = Math.random();
			if (rnd >0.95) {
				maxCiclosSeguirY = 10;
				//System.out.println("El siguiente paso puede que vuelvas a seguir y.");
			}
			estadoPortero=921;
		}
		else if (jugador.estasBajoPalos() && !jugador.balonDemasiadoLejosDeAreaPropia()) {
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Estás bajo palos y el balon demasiado lejos del area propia. Sigues la y de la bola como maximo unos ciclos más.");
			if (estadoPortero!=922)
				abstract_robot.setSpeed(curr_time, 0);
			//abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			estadoPortero=922;
			//maximoSeguirYBola;
		}
		else if (!jugador.estasBajoPalosEnY() && jugador.estasBajoPalosEnX()) {
			//System.out.println("PORTERO: Estás bajo palos en X pero no en y.");
			if (estadoPortero!=923)
				abstract_robot.setSpeed(curr_time, 0);
			//abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			estadoPortero=923;
		}
		/*else if (true) {
			System.out.println("estaba entrando aqui!!!");
		}*/
		//Prueba
		else if (!jugador.estasBajoPalos()) {
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Estás bajo palos pero no en el centro vertical del campo");
			if (estadoPortero!=924)
				abstract_robot.setSpeed(curr_time, 0);
			//abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			estadoPortero=924;
		}
		else if (jugador.estasBajoPalos() && !jugador.estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Estás bajo palos pero no en el centro vertical del campo");
			if (estadoPortero!=925)
				abstract_robot.setSpeed(curr_time, 925);
			//abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			estadoPortero=925;
		}
		else if (jugador.estasBajoPalos() && jugador.estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Estás bajo palos y en el centro vertical del campo");
			if (estadoPortero!=926)
				abstract_robot.setSpeed(curr_time, 0);
			//abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			if (balon.x>0 && abstract_robot.canKick(curr_time)) {
				System.out.println("Portero: ¡¡Despejo!!");
				abstract_robot.kick(curr_time);
			}
			else {
				System.out.println("CUIDADO CON GOL EN PROPIA");
			}
			estadoPortero=926;
		}
		else {
			System.out.println("\nºDEFAULT\n\n\n");
			//System.out.println("PORTERO: default: ir a tu portería...");
			if (estadoPortero!=10000)
				abstract_robot.setSpeed(curr_time, 0);
			//abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);	
			estadoPortero=10000;
			
			//No hace falta seguir moviéndose hacia la portería y se puede tener el steerHeading mirando a la bola.
/*
			//System.out.println("PORTERO: Por defecto, estás parado.");
			////System.out.println("PORTERO: Por defecto, estás bajo palos y sigues la y de la bola.");
			abstract_robot.setSpeed(curr_time, 0);
			//Importante: Para saber si está escorado o no se utiliza ballMenosOurGoal, pero para seguir la dirección de la y se utiliza ball.y.
//			Vec2 nuevaDireccion= new Vec2(0,balon.y);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0);*/
		}
		
		if (estadoPorteroAnterior!=estadoPortero) {
			//System.out.println("******* TRANSICIÓN *******");
		}
		
		boolean bloqueado = detectarPorteroBloqueadoPorRival();
		
		if (bloqueado) {
			System.out.println("Atención: Portero BLOQUEADO. Actuar!!");
		}
		
	}
	
	private boolean detectarPorteroBloqueadoPorRival() {
		Vec2 yo = new Vec2(0,0);
		Vec2 oponenteMasCercano = jugador.calcularMasCercano(yo, jugador.oponentes);
		if( oponenteMasCercano.r < SocSmall.RADIUS*1.1)
		{
			porteroBloqueado++;
			if (porteroBloqueado<=225 && !jugador.estasEnBanda()) {
				return false;
			}
			else {
				System.out.println("**********\n\n\nPortero BLOQUEADO!!!\n\n\n****");
				porteroBloqueado=0;
				int defensa = jugador.devolverDefensaMasDefensivoNoBloqueado();
				int defensaCierre = jugador.devolverDefensaCierreMasDefensivoNoBloqueado();		
				if (defensa>=0 || defensaCierre>=0) { 
					
					//roles[delantero] = CENTROCAMPISTAAPROVECHADORDEBLOQUEOS;
					//Se le manda a todos que este rol va a ser delantero. 
					Message m1;
					int[] ids=new int[1];
					if (defensaCierre>=0) {
						m1 = new StringMessage("YO DEFENSACIERRE");
						m1.sender = abstract_robot.getPlayerNumber(jugador.curr_time);
						abstract_robot.broadcast(m1);
						ids[0] = defensaCierre;
						System.out.println("Cambio de Rol: DefensaCierre "+defensaCierre+" por Portero "+abstract_robot.getPlayerNumber(jugador.curr_time));
						jugador.roles[abstract_robot.getPlayerNumber(jugador.curr_time)] = jugador.DEFENSACIERRE;
						jugador.cambiarRol(jugador.DEFENSACIERRE);
					} else {
						m1 = new StringMessage("YO DEFENSA");
						m1.sender = abstract_robot.getPlayerNumber(jugador.curr_time);
						abstract_robot.broadcast(m1);
						ids[0] = defensa;
						System.out.println("Cambio de Rol: Defensa "+defensa+" por Portero "+abstract_robot.getPlayerNumber(jugador.curr_time));
						jugador.roles[abstract_robot.getPlayerNumber(jugador.curr_time)] = jugador.DEFENSA;
						jugador.cambiarRol(jugador.DEFENSA);
					}
					Message m2 = new StringMessage("TU PORTERO");
					m2.sender = abstract_robot.getPlayerNumber(jugador.curr_time);
					try {
						//Se le manda únicamente al delantero que va a pasar a ser centrocampistaAprovechadorDeBloqueos, y ya será ese el que comunique al resto los cambios.
						abstract_robot.multicast(ids, m2);
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
					//if (roles[delantero] == CENTROCAMPISTAAPROVECHADORDEBLOQUEOS)
					//	System.out.println("Menos mal");
				}
				return true;
			}
		}
		else {
			porteroBloqueado=0;
			return false;
		}
	}
}
