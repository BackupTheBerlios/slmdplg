import EDU.gatech.cc.is.abstractrobot.SocSmall;


public class EnjutoRolDefensa extends EnjutoRol {
	
	public EnjutoRolDefensa(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 1;
		this.abstract_robot = robot;
		
	}
	
	public void actuarRol(int estadoAtaqueODefensa)
	{
		abstract_robot.setDisplayString("Defensa " + estadoAtaqueODefensa);

		if (estadoAtaqueODefensa == EnjutoMojamuTeamNachoConRoles.ATACAR){

			abstract_robot.setDisplayString("Defensa ataca!");
			//Si anteriormente ya era defensa, pues no sigo defendiendo.
			if (jugador.ultimoEstado == identificadorRol){
				if (jugador.companeros.length >= 4) {
					jugador.cubrirPase(jugador.calcularJugadorACubrir());
				}
			} else {
				jugador.volverAlAnteriorRol();
			}
			jugador.cubrirPase(jugador.calcularJugadorACubrir());

			
		} else {
			if (estadoAtaqueODefensa == EnjutoMojamuTeamNachoConRoles.DEFENDER){

				abstract_robot.setDisplayString("Defensa defiende!");
				jugador.cubrirPase(jugador.calcularJugadorACubrir());
			}
		}
	}
}
