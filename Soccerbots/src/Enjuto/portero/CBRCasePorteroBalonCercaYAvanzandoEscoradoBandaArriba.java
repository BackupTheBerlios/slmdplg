package Enjuto.portero;

import EDU.gatech.cc.is.util.Vec2;
import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroBalonCercaYAvanzandoEscoradoBandaArriba implements
		CBRCase {

	EnjutoRolPortero rolPropietario;
	
	public CBRCasePorteroBalonCercaYAvanzandoEscoradoBandaArriba(
			EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if (rolPropietario.jugador.balonCercaAreaPropia() && rolPropietario.jugador.balonAvanzandoEscoradoBandaArriba())
			return true;
		else
			return false;
	}

	@Override
	public void proposeSolution() {
		long curr_time = rolPropietario.jugador.curr_time;
		Vec2 balon = rolPropietario.jugador.balon;
		Vec2 balonConCorreccionOfensiva = new Vec2();
		if (rolPropietario.jugador.SIDE==-1) {
			balonConCorreccionOfensiva.setx(balon.x-0.33*balon.x);
			balonConCorreccionOfensiva.sety(balon.y);
		}
		else {
			balonConCorreccionOfensiva.setx(balon.x+0.50*balon.x);
			balonConCorreccionOfensiva.sety(balon.y);
		}
		if (rolPropietario.getEstadoPortero() != 5)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, balonConCorreccionOfensiva.t);
		rolPropietario.abstract_robot.setSpeed(curr_time, 0.9); //Antes 0.5
		rolPropietario.setEstadoPortero(5);
	}

}
