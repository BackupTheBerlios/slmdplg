package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroBalonNoCercaPorteroCentroVerticalYNoDemasiadoAdelantado
		implements CBRCase {

	EnjutoRolPortero rolPropietario;

	public CBRCasePorteroBalonNoCercaPorteroCentroVerticalYNoDemasiadoAdelantado(
			EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if (!rolPropietario.jugador.balonCercaAreaPropia() && rolPropietario.jugador.estasEnElCentroVerticalDelCampo() && !rolPropietario.jugador.demasiadoAdelantado())
			return true;
		else
			return false;
	}

	@Override
	public void proposeSolution() {
		long curr_time = rolPropietario.jugador.curr_time;
		if (rolPropietario.getEstadoPortero() != 9)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, rolPropietario.jugador.balon.t);
		rolPropietario.abstract_robot.setSpeed(curr_time, 0); //Antes 0.5
		rolPropietario.setEstadoPortero(9);
	}

}
