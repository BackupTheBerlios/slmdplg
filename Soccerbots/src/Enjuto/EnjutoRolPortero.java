package Enjuto;
import java.util.ArrayList;
import java.util.Iterator;

import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.communication.CommunicationException;
import EDU.gatech.cc.is.communication.Message;
import EDU.gatech.cc.is.communication.StringMessage;
import EDU.gatech.cc.is.util.Vec2;
import Enjuto.portero.CBRCase;
import Enjuto.portero.CBRCaseBalonZonaIntermediaPorteroBienColocado;
import Enjuto.portero.CBRCaseNoBajoPalosNiCondicionesAnteriores;
import Enjuto.portero.CBRCasePorDefecto;
import Enjuto.portero.CBRCasePorteroBalonCercaYAvanzandoCentrado;
import Enjuto.portero.CBRCasePorteroBalonCercaYAvanzandoEscoradoBandaAbajo;
import Enjuto.portero.CBRCasePorteroBalonCercaYAvanzandoEscoradoBandaArriba;
import Enjuto.portero.CBRCasePorteroBalonDemasiadoLejosYNoCentroVerticalDelCampo;
import Enjuto.portero.CBRCasePorteroBalonNoCercaPorteroCentroVerticalYDemasiadoAdelantado;
import Enjuto.portero.CBRCasePorteroBalonNoCercaPorteroCentroVerticalYNoDemasiadoAdelantado;
import Enjuto.portero.CBRCasePorteroBalonZonaIntermediaAbajoYNoEnOurCenterRight;
import Enjuto.portero.CBRCasePorteroBalonZonaIntermediaArribaYNoEnOurCenterLeft;
import Enjuto.portero.CBRCasePorteroEnBandaDerecha;
import Enjuto.portero.CBRCasePorteroEnBandaIzquierda;
import Enjuto.portero.CBRCasePorteroInicial;
import Enjuto.portero.CBRCasePorteroLejosAreaYNoCercaDeBalon;


public class EnjutoRolPortero extends EnjutoRol {
	
	private static int porteroBloqueado;
	private int estadoPortero;
	private int maxCiclosSeguirY;
	ArrayList<CBRCase> casos;
	
	public EnjutoRolPortero(EnjutoMojamuTeam jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 0;
		this.abstract_robot = robot;
		estadoPortero=0;
	    maxCiclosSeguirY = 10;
		porteroBloqueado = 0;
		
		//Generar vector de casos, en orden preferente.
		casos = new ArrayList<CBRCase>();
		casos.add(new CBRCasePorteroInicial(this));
		casos.add(new CBRCasePorteroLejosAreaYNoCercaDeBalon(this));
		casos.add(new CBRCasePorteroEnBandaDerecha(this));
		casos.add(new CBRCasePorteroEnBandaIzquierda(this));
		casos.add(new CBRCasePorteroBalonCercaYAvanzandoCentrado(this));
		casos.add(new CBRCasePorteroBalonCercaYAvanzandoEscoradoBandaArriba(this));
		casos.add(new CBRCasePorteroBalonCercaYAvanzandoEscoradoBandaAbajo(this));
		casos.add(new CBRCasePorteroBalonDemasiadoLejosYNoCentroVerticalDelCampo(this));
		casos.add(new CBRCasePorteroBalonNoCercaPorteroCentroVerticalYDemasiadoAdelantado(this));
		casos.add(new CBRCasePorteroBalonNoCercaPorteroCentroVerticalYNoDemasiadoAdelantado(this));
		casos.add(new CBRCasePorteroBalonZonaIntermediaArribaYNoEnOurCenterLeft(this));
		casos.add(new CBRCasePorteroBalonZonaIntermediaAbajoYNoEnOurCenterRight(this));
		casos.add(new CBRCaseBalonZonaIntermediaPorteroBienColocado(this));
		casos.add(new CBRCaseNoBajoPalosNiCondicionesAnteriores(this));
		casos.add(new CBRCasePorDefecto(this));
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		actuarPortero();
	}
	
	private void actuarPortero() {
				
		//Forma de proceder: Se lee la lista de todos lso casos posibles en orden hasta encontrar uno cuya descripción 
		//sea compatible con el estado actual del juego. Solo 1 será aplicable, y siempre será al menos uno pro haber un 
		//caso por defecto.
		Iterator<CBRCase> it = casos.iterator();
		CBRCase caso;
		boolean aplicadoCaso = false;
		while (it.hasNext() && !aplicadoCaso) {
			caso = it.next();
			if (caso.acceptDescription()) {
				caso.proposeSolution();
				aplicadoCaso = true;
			}
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
						//Se le manda ï¿½nicamente al delantero que va a pasar a ser centrocampistaAprovechadorDeBloqueos, y ya serï¿½ ese el que comunique al resto los cambios.
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

	public static int getPorteroBloqueado() {
		return porteroBloqueado;
	}

	public static void setPorteroBloqueado(int porteroBloqueado) {
		EnjutoRolPortero.porteroBloqueado = porteroBloqueado;
	}

	public int getEstadoPortero() {
		return estadoPortero;
	}

	public void setEstadoPortero(int estadoPortero) {
		this.estadoPortero = estadoPortero;
	}
}
