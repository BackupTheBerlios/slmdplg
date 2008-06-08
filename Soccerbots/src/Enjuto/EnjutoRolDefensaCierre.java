package Enjuto;
import EDU.gatech.cc.is.abstractrobot.SocSmall;


public class EnjutoRolDefensaCierre extends EnjutoRol 
{
	public EnjutoRolDefensaCierre(EnjutoMojamuTeam jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 5;
		this.abstract_robot = robot;
		
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		if (estadoAtaqueODefensa == EnjutoMojamuTeam.ATACAR){
			jugador.cubrirContra(jugador.calcularJugadorACubrir());
			
		} else {
			if (estadoAtaqueODefensa == EnjutoMojamuTeam.DEFENDER){
				jugador.cubrirContra(jugador.calcularJugadorACubrir());
			}
		}
	}
}
