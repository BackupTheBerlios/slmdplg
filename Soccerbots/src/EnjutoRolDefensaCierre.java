import EDU.gatech.cc.is.abstractrobot.SocSmall;


public class EnjutoRolDefensaCierre extends EnjutoRol 
{
	public EnjutoRolDefensaCierre(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 5;
		this.abstract_robot = robot;
		
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		if (estadoAtaqueODefensa == jugador.ATACAR){
			jugador.cubrirContra(jugador.calcularJugadorACubrir());
//			abstract_robot.setDisplayString("Defiendo!!");
			
		} else {
			if (estadoAtaqueODefensa == jugador.DEFENDER){
				jugador.cubrirContra(jugador.calcularJugadorACubrir());
//				abstract_robot.setDisplayString("Defiendo!!");
			}
		}
	}
}
