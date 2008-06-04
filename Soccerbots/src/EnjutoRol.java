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
	 * Método para que el rol realize una acción. 
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
