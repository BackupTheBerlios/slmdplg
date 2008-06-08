import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.communication.CommunicationException;
import EDU.gatech.cc.is.communication.Message;
import EDU.gatech.cc.is.communication.StringMessage;
import EDU.gatech.cc.is.util.Vec2;


public class EnjutoRolPortero extends EnjutoRol {
	
	private static int porteroBloqueado;
	private int estadoPortero;
	private int maxCiclosSeguirY;
	
	public EnjutoRolPortero(EnjutoMojamuTeam jugador, SocSmall robot){
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
		
		long curr_time = jugador.curr_time;
		Vec2 balon = jugador.balon;
		Vec2 ourGoal = jugador.ourGoal;
		int SIDE = jugador.SIDE;
		
		//Comprobar si est� sufriendo un bloqueo.
		
			//Si es as� intentar evitar colision, y posteriormente ir hacia la porter�a.
		
		//Permitirle salir del area, si el equipo rival est� muy defensivo.
		
		//En mano a mano no ir directamente a la bola, sin ir a la bola sin perder de vista la porter�a.
		
		if (jugador.curr_time<1000) {
			if (estadoPortero != 0)
				abstract_robot.setSpeed(jugador.curr_time, 0);
			abstract_robot.setSteerHeading(jugador.curr_time, jugador.ourGoal.t);
			
			abstract_robot.setSpeed(jugador.curr_time, 1.0);
			estadoPortero = 0;
		}
		else if (jugador.lejosDeTuArea() && !jugador.estaCercaDeVector(balon)) {
			if (estadoPortero != 1)
				abstract_robot.setSpeed(jugador.curr_time, 0);
			abstract_robot.setSteerHeading(jugador.curr_time, jugador.ourGoal.t);
			abstract_robot.setSpeed(jugador.curr_time, 1.0);
			estadoPortero = 1;
		}
		else if ( jugador.estasEnBandaDerecha() ) {
			if (estadoPortero != 11)
				abstract_robot.setSpeed(jugador.curr_time, 0);
			abstract_robot.setSteerHeading(jugador.curr_time, jugador.ourRightPost.t);
			abstract_robot.setSpeed(jugador.curr_time, 1.0);
			estadoPortero = 11;
		}
		else if ( jugador.estasEnBandaIzquierda() ) {
			if (estadoPortero != 111)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourLeftPost.t);
			abstract_robot.setSpeed(curr_time, 1.0);
			estadoPortero = 111;
		}
		else if (jugador.balonCercaAreaPropia() && jugador.balonAvanzandoCentrado()) {
			Vec2 balonConCorreccionOfensiva = new Vec2();
			if (SIDE==-1) {
				balonConCorreccionOfensiva.setx(balon.x-0.2*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);
			}
			else {
				balonConCorreccionOfensiva.setx(balon.x+0.02*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);
			}
			if (estadoPortero != 2)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balonConCorreccionOfensiva.t);
			abstract_robot.setSpeed(curr_time, 0.9); //Antes 0.75	
			if (balon.x>0 && abstract_robot.canKick(curr_time)) {
				abstract_robot.kick(curr_time);
			}
			estadoPortero = 2;
		}
		else if (jugador.balonCercaAreaPropia() && jugador.balonAvanzandoEscoradoBandaArriba()) {
			Vec2 balonConCorreccionOfensiva = new Vec2();
			if (SIDE==-1) {
				balonConCorreccionOfensiva.setx(balon.x-0.33*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);
			}
			else {
				balonConCorreccionOfensiva.setx(balon.x+0.50*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);
			}
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balonConCorreccionOfensiva.t);
			abstract_robot.setSpeed(curr_time, 0.9); //Antes 0.5
			estadoPortero = 3;
		}
		else if (jugador.balonCercaAreaPropia() && jugador.balonAvanzandoEscoradoBandaAbajo()) {
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
					balonConCorreccionOfensiva.setx(balon.x+0.48*balon.x);
					balonConCorreccionOfensiva.sety(balon.y);					
				}
				else {
					balonConCorreccionOfensiva.setx(balon.x+0.38*balon.x);
					balonConCorreccionOfensiva.sety(balon.y);
				}
			}
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			balonConCorreccionOfensiva.normalize(3);
			abstract_robot.setSteerHeading(curr_time, balonConCorreccionOfensiva.t);
			abstract_robot.setSpeed(curr_time, 0.9); //Antes 0.5
			estadoPortero = 4;
		}
		//A�adido ultimamente el balonDemasiadoLejosDeAreaPropia
		else if (!jugador.balonCercaAreaPropia() && jugador.balonDemasiadoLejosDeAreaPropia() && !jugador.estasEnElCentroVerticalDelCampo()) {
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 4;
		}
		else if (!jugador.balonCercaAreaPropia() && jugador.estasEnElCentroVerticalDelCampo() && jugador.demasiadoAdelantado()) {
			if (estadoPortero!=423)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoalAdelantado.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 423;
		}
		else if (!jugador.balonCercaAreaPropia() && jugador.estasEnElCentroVerticalDelCampo() && !jugador.demasiadoAdelantado()) {
			if (estadoPortero!=423)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
			estadoPortero = 423;
		}
		else if (!jugador.balonCercaAreaPropia() && !jugador.balonDemasiadoLejosDeAreaPropia() && jugador.balonAvanzandoEscoradoBandaArriba() && !jugador.estasEnOurCenterLeft()) {
			if (estadoPortero!=311)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoalCenterLeft.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 311;
		}
		else if (!jugador.balonCercaAreaPropia() && !jugador.balonDemasiadoLejosDeAreaPropia() && jugador.balonAvanzandoEscoradoBandaAbajo() && !jugador.estasEnOurCenterRight()) {
		
			if (estadoPortero!=312)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourGoalCenterRight.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 312;
		}
		else if (!jugador.balonCercaAreaPropia() && !jugador.balonDemasiadoLejosDeAreaPropia() && jugador.balonAvanzandoEscoradoBandaArriba() && jugador.estasEnOurCenterLeft()) {
			if (estadoPortero!=3)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
			estadoPortero = 311;
		}
		
		else if (jugador.balonCercaAreaPropia() && jugador.balonAvanzandoEscoradoBandaAbajo()) {
			if (estadoPortero!=4)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, jugador.ourRightPost.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero = 41;
		}
		//ANTES:
		else if (jugador.estasBajoPalos() && !jugador.estasEnElCentroVerticalDelCampo()) {
			if (estadoPortero!=89)
				abstract_robot.setSpeed(curr_time, 0);			
			abstract_robot.setSpeed(curr_time, 1);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			estadoPortero=89;
		}
		else if (!jugador.balonCercaAreaPropia() && !jugador.balonDemasiadoLejosDeAreaPropia() && !jugador.estasBajoPalosEnY()) {
			if (estadoPortero!=6)	
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
			estadoPortero=6;
		}
		else if (!jugador.balonCercaAreaPropia() && jugador.balonDemasiadoLejosDeAreaPropia()) {
			if (estadoPortero!=7)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 0.2); //Antes 0.5	
			estadoPortero=7;			
		}
		else if (jugador.estasBajoPalos() && jugador.balonDemasiadoLejosDeAreaPropia() && maxCiclosSeguirY==0) {
			if (estadoPortero!=921)
				abstract_robot.setSpeed(curr_time, 0);			
			abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			double rnd = Math.random();
			if (rnd >0.95) {
				maxCiclosSeguirY = 10;
			}
			estadoPortero=921;
		}
		else if (jugador.estasBajoPalos() && !jugador.balonDemasiadoLejosDeAreaPropia()) {
			if (estadoPortero!=922)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			estadoPortero=922;
		}
		else if (!jugador.estasBajoPalosEnY() && jugador.estasBajoPalosEnX()) {
			if (estadoPortero!=923)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			estadoPortero=923;
		}
		else if (!jugador.estasBajoPalos()) {
			if (estadoPortero!=924)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			estadoPortero=924;
		}
		else if (jugador.estasBajoPalos() && !jugador.estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			if (estadoPortero!=925)
				abstract_robot.setSpeed(curr_time, 925);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1);
			estadoPortero=925;
		}
		else if (jugador.estasBajoPalos() && jugador.estasEnElCentroVerticalDelCampo()) {
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
			if (estadoPortero!=926)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, balon.t);
			if (balon.x>0 && abstract_robot.canKick(curr_time)) {
				abstract_robot.kick(curr_time);
			}
			estadoPortero=926;
		}
		else {
			if (estadoPortero!=10000)
				abstract_robot.setSpeed(curr_time, 0);
			abstract_robot.setSteerHeading(curr_time, ourGoal.t);
			abstract_robot.setSpeed(curr_time, 1.0);	
			estadoPortero=10000;
			
			//No hace falta seguir movi�ndose hacia la porter�a y se puede tener el steerHeading mirando a la bola.
		}
		
		detectarPorteroBloqueadoPorRival();

		
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
				porteroBloqueado=0;
				int defensa = jugador.devolverDefensaMasDefensivoNoBloqueado();
				int defensaCierre = jugador.devolverDefensaCierreMasDefensivoNoBloqueado();		
				if (defensa>=0 || defensaCierre>=0) { 
					Message m1;
					int[] ids=new int[1];
					if (defensaCierre>=0) {
						m1 = new StringMessage("YO DEFENSACIERRE");
						m1.sender = abstract_robot.getPlayerNumber(jugador.curr_time);
						abstract_robot.broadcast(m1);
						ids[0] = defensaCierre;
						jugador.roles[abstract_robot.getPlayerNumber(jugador.curr_time)] = EnjutoMojamuTeam.DEFENSACIERRE;
						jugador.cambiarRol(EnjutoMojamuTeam.DEFENSACIERRE);
					} else {
						m1 = new StringMessage("YO DEFENSA");
						m1.sender = abstract_robot.getPlayerNumber(jugador.curr_time);
						abstract_robot.broadcast(m1);
						ids[0] = defensa;
						jugador.roles[abstract_robot.getPlayerNumber(jugador.curr_time)] = EnjutoMojamuTeam.DEFENSA;
						jugador.cambiarRol(EnjutoMojamuTeam.DEFENSA);
					}
					Message m2 = new StringMessage("TU PORTERO");
					m2.sender = abstract_robot.getPlayerNumber(jugador.curr_time);
					try {
						//Se le manda �nicamente al delantero que va a pasar a ser centrocampistaAprovechadorDeBloqueos, y ya ser� ese el que comunique al resto los cambios.
						abstract_robot.multicast(ids, m2);
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
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
