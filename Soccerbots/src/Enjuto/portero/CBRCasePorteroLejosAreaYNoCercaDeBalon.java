package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroLejosAreaYNoCercaDeBalon implements CBRCase {

	EnjutoRolPortero rolPropietario;
	
	public CBRCasePorteroLejosAreaYNoCercaDeBalon(
			EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	public EnjutoRolPortero getRolPropietario() {
		return rolPropietario;
	}

	public void setRolPropietario(EnjutoRolPortero rolPropietario) {
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if (rolPropietario.jugador.lejosDeTuArea() && !rolPropietario.jugador.estaCercaDeVector(rolPropietario.jugador.balon))
			return true;
		else
			return false;
	}

	@Override
	public void proposeSolution() {
		long curr_time = rolPropietario.jugador.curr_time;
		if (rolPropietario.getEstadoPortero() != 1)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, rolPropietario.jugador.ourGoal.t);
		rolPropietario.abstract_robot.setSpeed(curr_time, 1.0);
		rolPropietario.setEstadoPortero(1);
	}

}
