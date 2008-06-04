import EDU.gatech.cc.is.abstractrobot.SocSmall;


public class EnjutoRolDefensa extends EnjutoRol {
	
	public EnjutoRolDefensa(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 0;
		this.abstract_robot = robot;
		
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		abstract_robot.setDisplayString("Defensa " + estadoAtaqueODefensa);
		if (estadoAtaqueODefensa == jugador.ATACAR){
			jugador.cubrirPase(jugador.calcularJugadorACubrir());
//			abstract_robot.setDisplayString("Defiendo!!");
			
		} else {
			if (estadoAtaqueODefensa == jugador.DEFENDER){
				jugador.cubrirPase(jugador.calcularJugadorACubrir());
//				abstract_robot.setDisplayString("Defiendo!!");
			}
		}
	}
}