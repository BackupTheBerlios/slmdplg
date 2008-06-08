package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroBalonNoCercaPorteroCentroVerticalYDemasiadoAdelantado
		implements CBRCase {

	EnjutoRolPortero rolPropietario;
	
	public CBRCasePorteroBalonNoCercaPorteroCentroVerticalYDemasiadoAdelantado(
			EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if (!rolPropietario.jugador.balonCercaAreaPropia() && rolPropietario.jugador.estasEnElCentroVerticalDelCampo() && rolPropietario.jugador.demasiadoAdelantado()) 
			return true;
		else
			return false;
	}

	@Override
	public void proposeSolution() {
		long curr_time = rolPropietario.jugador.curr_time;
		if (rolPropietario.getEstadoPortero() != 8)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, rolPropietario.jugador.ourGoalAdelantado.t);
		rolPropietario.abstract_robot.setSpeed(curr_time, 1); //Antes 0.5
		rolPropietario.setEstadoPortero(8);
	}

}
