import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.util.Vec2;


public class EnjutoRolUltimoDefensa extends EnjutoRol {

	public EnjutoRolUltimoDefensa(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 0;
		this.abstract_robot = robot;
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		//Ahora está configurado para que haga lo mismo tanto atacando como defendiendo.
		
		if (estadoAtaqueODefensa == EnjutoMojamuTeamNachoConRoles.ATACAR){
			actuarUltimoDefensa();
		} else {
			if (estadoAtaqueODefensa == EnjutoMojamuTeamNachoConRoles.DEFENDER){
				jugador.cubrirPase(jugador.calcularJugadorACubrir());
			}
		}
	}
	
	
	public void actuarUltimoDefensa() {
		
		long curr_time = jugador.curr_time; 
		
		if (jugador.oponentes.length == 5){
			
			int enemigoMasPeligroso = jugador.calcularOponenteMasAdelantado(jugador.oponentes);
			
			if((abstract_robot.getPosition(curr_time).x)*jugador.SIDE < 0) {
				abstract_robot.setDisplayString("CAMPO ENEMIGO");
				Vec2 cercaDeMiCampo = new Vec2(jugador.ourGoal.x + (-jugador.SIDE)*EnjutoMojamuTeamNachoConRoles.LONGITUD_CAMPO/8, jugador.ourGoal.y);
				abstract_robot.setSteerHeading(curr_time, cercaDeMiCampo.t);
			} else {
				abstract_robot.setDisplayString("Mi CAMPO");
				jugador.cubrirPase(enemigoMasPeligroso);
			}
		}
	}
}
