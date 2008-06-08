package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroEnBandaIzquierda implements CBRCase {

	EnjutoRolPortero rolPropietario;
	
	public CBRCasePorteroEnBandaIzquierda(EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if ( rolPropietario.jugador.estasEnBandaIzquierda() ) 
			return true;
		else
			return false;
	}

	@Override
	public void proposeSolution() {
		if (rolPropietario.getEstadoPortero() != 3)
			rolPropietario.abstract_robot.setSpeed(rolPropietario.jugador.curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(rolPropietario.jugador.curr_time, rolPropietario.jugador.ourLeftPost.t);
		rolPropietario.abstract_robot.setSpeed(rolPropietario.jugador.curr_time, 1.0);
		rolPropietario.setEstadoPortero(3);
	}

}
