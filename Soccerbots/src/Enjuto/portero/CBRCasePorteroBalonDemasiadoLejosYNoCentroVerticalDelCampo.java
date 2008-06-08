package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroBalonDemasiadoLejosYNoCentroVerticalDelCampo implements
		CBRCase {

	EnjutoRolPortero rolPropietario;

	public CBRCasePorteroBalonDemasiadoLejosYNoCentroVerticalDelCampo(
			EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if (!rolPropietario.jugador.balonCercaAreaPropia() && rolPropietario.jugador.balonDemasiadoLejosDeAreaPropia() && !rolPropietario.jugador.estasEnElCentroVerticalDelCampo())
			return true;
		else
			return false;
	}

	@Override
	public void proposeSolution() {
		long curr_time = rolPropietario.jugador.curr_time;
		if (rolPropietario.getEstadoPortero() != 7)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, rolPropietario.jugador.ourGoal.t);
		rolPropietario.abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
		rolPropietario.setEstadoPortero(7);
	}
	

}
