package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCasePorDefecto implements CBRCase {

	EnjutoRolPortero rolPropietario;

	public CBRCasePorDefecto(EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	//Por ser el caso por defecto, siempre se cumple su descripción.
	public boolean acceptDescription() {
		return true;
	}

	@Override
	public void proposeSolution() {
		long curr_time = rolPropietario.jugador.curr_time;
		if (rolPropietario.getEstadoPortero() != 14)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, rolPropietario.jugador.ourGoal.t);
		rolPropietario.abstract_robot.setSpeed(curr_time, 1.0);	
		rolPropietario.setEstadoPortero(14);
	}
	
	
}
