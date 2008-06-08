package Enjuto;
import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.communication.CommunicationException;
import EDU.gatech.cc.is.communication.Message;
import EDU.gatech.cc.is.communication.StringMessage;


public class EnjutoRolCentrocampistaBloqueador extends EnjutoRol {

	public EnjutoRolCentrocampistaBloqueador(EnjutoMojamuTeam jugador, SocSmall robot) {
		this.abstract_robot = robot;
		this.jugador = jugador;
		this.identificadorRol = 4;
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		abstract_robot.setDisplayString("Centro Campista!");
		actuarCentrocampistaAprovechadorDeBloqueos();
	}
	
	private void actuarCentrocampistaAprovechadorDeBloqueos() {
		
		long curr_time = jugador.curr_time; 
		
		if (jugador.SIDE==-1) 
		{ //Oeste
			if (!jugador.estaEnVector(jugador.cercaCentroDelCampoOeste)) 
			{
				abstract_robot.setSteerHeading(curr_time, jugador.cercaCentroDelCampoOeste.t);
				abstract_robot.setSpeed(curr_time, 1.0);			
			} else if (jugador.estaEnVector(jugador.cercaCentroDelCampoOeste) && !jugador.tienesBalonPorDelanteYCerca()) 
			{
				abstract_robot.setSteerHeading(curr_time, jugador.oponentGoal.t);
				abstract_robot.setSpeed(curr_time, 0);
			} else 
			{ //estaEnVector(cercaCentroDelCampoOeste) && tienesBalonPorDelanteYCerca()
				//Al ataque, y que otro delantero sea el que le supla.
				int delantero = jugador.devolverDelanteroMenosOfensivo();
				if (delantero>=0) 
				{ 
					jugador.roles[abstract_robot.getPlayerNumber(curr_time)] = 3; // 3 = DELANTERO
					Message m1 = new StringMessage("YO DELANTERO");
					m1.sender = abstract_robot.getPlayerNumber(curr_time);
					abstract_robot.broadcast(m1);
					Message m2 = new StringMessage("TU CENTROCAMPISTAAPROVECHADORDEBLOQUEOS");
					m2.sender = abstract_robot.getPlayerNumber(curr_time);
					int[] ids=new int[1];
					ids[0] = delantero;
					try {
						abstract_robot.multicast(ids, m2);
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
					jugador.cambiarRol(3);
				}
			}
		}
		else { 
			
			if (!jugador.estaEnVector(jugador.cercaCentroDelCampoEste)) {
				abstract_robot.setSteerHeading(curr_time, jugador.cercaCentroDelCampoEste.t);
				abstract_robot.setSpeed(curr_time, 1.0);	
			} else if (jugador.estaEnVector(jugador.cercaCentroDelCampoEste) && !jugador.tienesBalonPorDelanteYCerca()) {
				abstract_robot.setSteerHeading(curr_time, jugador.oponentGoal.t);
				abstract_robot.setSpeed(curr_time, 0);
			} else { //estaEnVector(cercaCentroDelCampoOeste) && tienesBalonPorDelanteYCerca()
				//Al ataque, y que otro delantero sea el que le supla.
				int delantero = jugador.devolverDelanteroMenosOfensivo();
				if (delantero>=0) 
				{ 
					jugador.roles[abstract_robot.getPlayerNumber(curr_time)] = 3; // 3 = DELANTERO
					//Se le manda a todos que este rol va a ser delantero. 
					Message m1 = new StringMessage("YO DELANTERO");
					m1.sender = abstract_robot.getPlayerNumber(curr_time);
					abstract_robot.broadcast(m1);
					Message m2 = new StringMessage("TU CENTROCAMPISTAAPROVECHADORDEBLOQUEOS");
					m2.sender = abstract_robot.getPlayerNumber(curr_time);
					int[] ids=new int[1];
					ids[0] = delantero;
					try {
						abstract_robot.multicast(ids, m2);
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
					jugador.cambiarRol(3);
				}
			}
		}
	}
	
	
}
