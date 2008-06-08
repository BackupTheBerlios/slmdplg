package Enjuto.portero;

import Enjuto.EnjutoRolPortero;

public class CBRCaseBalonZonaIntermediaPorteroBienColocado implements CBRCase {

	EnjutoRolPortero rolPropietario;

	public CBRCaseBalonZonaIntermediaPorteroBienColocado(
			EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if (!rolPropietario.jugador.balonCercaAreaPropia() && !rolPropietario.jugador.balonDemasiadoLejosDeAreaPropia() && 
				 ( (rolPropietario.jugador.balonAvanzandoEscoradoBandaArriba() && rolPropietario.jugador.estasEnOurCenterLeft()) 
				|| (rolPropietario.jugador.balonAvanzandoEscoradoBandaAbajo() && rolPropietario.jugador.estasEnOurCenterRight()) ) )
			return true;
		else
			return false;
	}

	@Override
	public void proposeSolution() {
		long curr_time = rolPropietario.jugador.curr_time;
		if (rolPropietario.getEstadoPortero() != 12)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, rolPropietario.jugador.balon.t);
		rolPropietario.abstract_robot.setSpeed(curr_time, 0); //Antes 0.2
		rolPropietario.setEstadoPortero(12);
	}

}
