package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroBalonZonaIntermediaArribaYNoEnOurCenterLeft implements CBRCase {

	EnjutoRolPortero rolPropietario;

	public CBRCasePorteroBalonZonaIntermediaArribaYNoEnOurCenterLeft(
			EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if (!rolPropietario.jugador.balonCercaAreaPropia() && !rolPropietario.jugador.balonDemasiadoLejosDeAreaPropia() && rolPropietario.jugador.balonAvanzandoEscoradoBandaArriba() && !rolPropietario.jugador.estasEnOurCenterLeft())
			return true;
		else
			return false;
	}

	@Override
	public void proposeSolution() {
		long curr_time = rolPropietario.jugador.curr_time;
		if (rolPropietario.getEstadoPortero() != 10)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, rolPropietario.jugador.ourGoalCenterLeft.t);
		rolPropietario.abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
		rolPropietario.setEstadoPortero(10);
	}
}
