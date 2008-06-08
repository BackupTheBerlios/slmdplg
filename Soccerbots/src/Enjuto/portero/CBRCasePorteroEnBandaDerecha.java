package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroEnBandaDerecha implements CBRCase {

	EnjutoRolPortero rolPropietario;
	
	public CBRCasePorteroEnBandaDerecha(EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if ( rolPropietario.jugador.estasEnBandaDerecha() ) 
			return true;
		else
			return false;
	}

	@Override
	public void proposeSolution() {
		if (rolPropietario.getEstadoPortero() != 2)
			rolPropietario.abstract_robot.setSpeed(rolPropietario.jugador.curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(rolPropietario.jugador.curr_time, rolPropietario.jugador.ourRightPost.t);
		rolPropietario.abstract_robot.setSpeed(rolPropietario.jugador.curr_time, 1.0);
		rolPropietario.setEstadoPortero(2);
	}

}
