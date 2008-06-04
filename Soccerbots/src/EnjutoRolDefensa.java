import EDU.gatech.cc.is.abstractrobot.SocSmall;


public class EnjutoRolDefensa extends EnjutoRol {
	
	public EnjutoRolDefensa(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 0;
		this.abstract_robot = robot;
		
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		abstract_robot.setDisplayString("Actúo??!!" + estadoAtaqueODefensa);
		if (estadoAtaqueODefensa == jugador.ATACAR){
			
		} else {
			if (estadoAtaqueODefensa == jugador.DEFENDER){
				jugador.cubrir(jugador.calcularJugadorACubrir());
				abstract_robot.setDisplayString("Defiendo!!");
			}
		}
	}
}
