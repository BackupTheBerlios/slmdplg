import EDU.gatech.cc.is.abstractrobot.SocSmall;


/**
 * Clase que representa un rol dentro de nuestro equipo
 * @author EnjutoMojamuTeam
 *
 */
public class EnjutoRol {
	protected EnjutoMojamuTeamNachoConRoles jugador;
	protected int identificadorRol;
	protected SocSmall abstract_robot;
	
	/**
	 * M�todo para que el rol realize una acci�n. 
	 */
	public void actuarRol(int estadoAtaqueODefensa){
		
	}

	public int getIdentificadorRol() {
		return identificadorRol;
	}

	public void setIdentificadorRol(int identificadorRol) {
		this.identificadorRol = identificadorRol;
	}
	
	
}
