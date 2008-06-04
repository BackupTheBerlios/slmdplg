import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.util.Vec2;


public class EnjutoRolPortero extends EnjutoRol {
	
	private int estadoPortero;
	private int maxCiclosSeguirY;
	
	public EnjutoRolPortero(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 0;
		this.abstract_robot = robot;
		estadoPortero=0;
	    maxCiclosSeguirY = 10;
		
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		actuarPortero();
	}
	
	private void actuarPortero() {
		
		int estadoPorteroAnterior = estadoPortero;
		
		long curr_time = jugador.curr_time; 
		Vec2 balon = jugador.balon;
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
		if ( /*estasEnBanda() ||*/ jugador.lejosDeTuArea() /*|| !estasBajoPalosEnY()*/ ) {
			//System.out.println("PORTERO: estasEnBanda o lejosDeTuArea");
			// set speed at minimum
			if (estadoPortero != 1)
				abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoal.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
			estadoPortero = 1;
		}
		else if ( jugador.estasEnBandaDerecha() ) {
			//System.out.println("PORTERO: estas en banda derecha");
			// set speed at minimum
			if (estadoPortero != 11)
				abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, jugador.ourRightPost.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
			estadoPortero = 11;
		}
		else if ( jugador.estasEnBandaIzquierda() ) {
			//System.out.println("PORTERO: estas en banda izquierda");
			// set speed at minimum
			if (estadoPortero != 111)
				abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, jugador.ourLeftPost.t);
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
			estadoPortero = 111;
		}
		else if (jugador.balonCercaAreaPropia() && jugador.balonAvanzandoCentrado()) {
			//System.out.println("PORTERO: balonCercaAreaPropia y bal�n avanzando centrado");
			Vec2 balonConCorreccionOfensiva = new Vec2();
			if (jugador.SIDE==-1) {
				balonConCorreccionOfensiva.x = (balon.x-0.1*balon.x);
				balonConCorreccionOfensiva.y = (balon.y);
			}
			else {
				balonConCorreccionOfensiva.x = (balon.x+0.1*balon.x);
				balonConCorreccionOfensiva.y = (balon.y);
			}
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.75	
			if (balon.x>0 && abstract_robot.canKick(curr_time)) {
				System.out.println("Portero: ��Despejo!!");
				abstract_robot.kick(curr_time);
			}
			estadoPortero = 2;
		}
		else if (jugador.balonCercaAreaPropia() && jugador.balonAvanzandoEscoradoBandaArriba()) {
			//System.out.println("PORTERO: balonCercaAreaPropia y bal�n avanzando escorado banda arriba");
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourLeftPost.t);
			abstract_robot.setSpeed(curr_time, 0.7); //Antes 0.5
			estadoPortero = 3;
		}
		else if (jugador.balonCercaAreaPropia() && jugador.balonAvanzandoEscoradoBandaAbajo()) {
			//System.out.println("PORTERO: balonCercaAreaPropia y bal�n avanzando escorado banda abajo");
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourRightPost.t);
			abstract_robot.setSpeed(curr_time, 0.7); //Antes 0.5
			estadoPortero = 4;
		}
		//A�adido ultimamente el balonDemasiadoLejosDeAreaPropia
		else if (!jugador.balonCercaAreaPropia() && jugador.balonDemasiadoLejosDeAreaPropia() && !jugador.estasEnElCentroVerticalDelCampo()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && !estasEnElCentroVerticalDelCampo()");
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 4;
		}
		else if (!jugador.balonCercaAreaPropia() && jugador.estasEnElCentroVerticalDelCampo()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia() && estasEnElCentroVerticalDelCampo()");
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
			//System.out.println("PORTERO: balonCercaAreaPropia y bal�n avanzando escorado banda abajo");
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourRightPost.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 41;
		}
		//ANTES:
		else if (jugador.estasBajoPalos() && !jugador.estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos pero no en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 1);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoal.t);
		}
		else if (!jugador.balonCercaAreaPropia() && !jugador.balonDemasiadoLejosDeAreaPropia() && jugador.estasBajoPalosEnY()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia y !balonDemasiadoLejosDeAreaPropia");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
			estadoPortero=5;
		}
		else if (!jugador.balonCercaAreaPropia() && !jugador.balonDemasiadoLejosDeAreaPropia() && !jugador.estasBajoPalosEnY()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia y !balonDemasiadoLejosDeAreaPropia");
			if (estadoPortero!=6)	
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero=6;
		}
		else if (!jugador.balonCercaAreaPropia() && jugador.balonDemasiadoLejosDeAreaPropia()) {
			//System.out.println("PORTERO: !balonCercaAreaPropia y s� balonDemasiadoLejosDeAreaPropia");
			if (estadoPortero!=7)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoal.t);
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
		else if (jugador.estasBajoPalos() && jugador.balonDemasiadoLejosDeAreaPropia() && maxCiclosSeguirY==0) {
			//System.out.println("PORTERO: Te cansas de seguir y, y simplemente miras quieto.");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			double rnd = Math.random();
			if (rnd >0.95) {
				maxCiclosSeguirY = 10;
				//System.out.println("El siguiente paso puede que vuelvas a seguir y.");
			}
		}
		else if (jugador.estasBajoPalos() && !jugador.balonDemasiadoLejosDeAreaPropia()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos y el balon demasiado lejos del area propia. Sigues la y de la bola como maximo unos ciclos m�s.");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			//maximoSeguirYBola;
		}
		else if (!jugador.estasBajoPalosEnY() && jugador.estasBajoPalosEnX()) {
			//System.out.println("PORTERO: Est�s bajo palos en X pero no en y.");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
		}
		//Prueba
		else if (!jugador.estasBajoPalos()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos pero no en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
		}
		else if (jugador.estasBajoPalos() && !jugador.estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos pero no en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
		}
		else if (jugador.estasBajoPalos() && jugador.estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			//System.out.println("PORTERO: Est�s bajo palos y en el centro vertical del campo");
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			if (balon.x>0 && abstract_robot.canKick(curr_time)) {
				System.out.println("Portero: ��Despejo!!");
				abstract_robot.kick(curr_time);
			}
		}
		else {
			
			//System.out.println("PORTERO: default: ir a tu porter�a...");
			abstract_robot.setSpeed(curr_time, 0);
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoal.t);
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
