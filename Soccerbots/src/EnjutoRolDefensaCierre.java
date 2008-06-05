import EDU.gatech.cc.is.abstractrobot.SocSmall;


public class EnjutoRolDefensaCierre extends EnjutoRol 
{
	public EnjutoRolDefensaCierre(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 5;
		this.abstract_robot = robot;
		
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		if (estadoAtaqueODefensa == EnjutoMojamuTeamNachoConRoles.ATACAR){
			jugador.cubrirContra(jugador.calcularJugadorACubrir());
			
		} else {
			if (estadoAtaqueODefensa == EnjutoMojamuTeamNachoConRoles.DEFENDER){
				jugador.cubrirContra(jugador.calcularJugadorACubrir());
			}
		}
	}
}
