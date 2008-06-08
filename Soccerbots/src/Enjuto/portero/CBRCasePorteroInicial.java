package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroInicial implements CBRCase {
	
	EnjutoRolPortero rolPropietario;
	//EnjutoMojamuTeam jugador;

	public CBRCasePorteroInicial(EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	public boolean acceptDescription() {
		if (rolPropietario.jugador.curr_time<1000)
			return true;
		else
			return false;
	}

	public void proposeSolution() {
		long curr_time = rolPropietario.jugador.curr_time;
		if (rolPropietario.getEstadoPortero() != 0)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, rolPropietario.jugador.ourGoal.t);
		
		rolPropietario.abstract_robot.setSpeed(curr_time, 1.0);
		rolPropietario.setEstadoPortero(0);
	}

}
