import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.util.Vec2;


public class EnjutoRolUltimoDefensa extends EnjutoRol {

	public EnjutoRolUltimoDefensa(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 0;
		this.abstract_robot = robot;
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		//!\ Ahora está configurado para que haga lo mismo tanto atacando como defendiendo.
		
		if (estadoAtaqueODefensa == jugador.ATACAR){
			actuarUltimoDefensa();
		} else {
			if (estadoAtaqueODefensa == jugador.DEFENDER){
				//jugador.cubrir(jugador.calcularJugadorACubrir());
			}
		}
	}
	
	
	public void actuarUltimoDefensa() {
		
		long curr_time = jugador.curr_time; 
		Vec2 balon = jugador.balon;
		int playerNumber = abstract_robot.getPlayerNumber(curr_time);
		
		if (jugador.oponentes.length == 5){
			//abstract_robot.setSpeed(curr_time, 0);
			
			int enemigoMasPeligroso = jugador.calcularOponenteMasAdelantado(jugador.oponentes);
			//int enemigoMasPeligroso = calcularOponenteMasOfensivo(this.oponentes);
			
			if((abstract_robot.getPosition(curr_time).x)*jugador.SIDE < 0) {
				abstract_robot.setDisplayString("CAMPO ENEMIGO");
				Vec2 cercaDeMiCampo = new Vec2(jugador.ourGoal.x + (-jugador.SIDE)*jugador.LONGITUD_CAMPO/8, jugador.ourGoal.y);
				abstract_robot.setSteerHeading(curr_time, cercaDeMiCampo.t);
			} else {
				abstract_robot.setDisplayString("Mi CAMPO");
				jugador.cubrir(enemigoMasPeligroso);
			}
			
			
			/*Vec2 cercaCentroDelCampoEste = new Vec2(ourGoal.x+(LONGITUD_CAMPO/2)+0.10,ourGoal.y);
			Vec2 cercaCentroDelCampoOeste = new Vec2(ourGoal.x+(LONGITUD_CAMPO/2)-0.30,ourGoal.y);
			
			
			abstract_robot.setSteerHeading(curr_time, cercaCentroDelCampoOeste.t);*/
			
		}
	}
}
