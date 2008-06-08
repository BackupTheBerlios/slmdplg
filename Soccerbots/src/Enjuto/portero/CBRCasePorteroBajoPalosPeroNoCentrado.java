package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroBajoPalosPeroNoCentrado implements CBRCase {

	EnjutoRolPortero rolPropietario;

	public CBRCasePorteroBajoPalosPeroNoCentrado(EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if (rolPropietario.jugador.estasBajoPalos() && !rolPropietario.jugador.estasEnElCentroVerticalDelCampo())
			return true;
		else
			return false;
	}

	@Override
	public void proposeSolution() {
		long curr_time = rolPropietario.jugador.curr_time;
		if (rolPropietario.getEstadoPortero() != 13)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);			
		rolPropietario.abstract_robot.setSpeed(curr_time, 1);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, rolPropietario.jugador.ourGoal.t);
		rolPropietario.setEstadoPortero(13);
	}

}
