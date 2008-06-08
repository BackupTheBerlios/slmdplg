package Enjuto.portero;

import EDU.gatech.cc.is.util.Vec2;
import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroBalonCercaYAvanzandoCentrado implements CBRCase {

	EnjutoRolPortero rolPropietario;
	
	public CBRCasePorteroBalonCercaYAvanzandoCentrado(
			EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}

	@Override
	public boolean acceptDescription() {
		if (rolPropietario.jugador.balonCercaAreaPropia() && rolPropietario.jugador.balonAvanzandoCentrado())
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
			balonConCorreccionOfensiva.setx(balon.x-0.2*balon.x);
			balonConCorreccionOfensiva.sety(balon.y);
		}
		else {
			balonConCorreccionOfensiva.setx(balon.x+0.02*balon.x);
			balonConCorreccionOfensiva.sety(balon.y);
		}
		if (rolPropietario.getEstadoPortero() != 4)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, balonConCorreccionOfensiva.t);
		rolPropietario.abstract_robot.setSpeed(curr_time, 0.9); //Antes 0.75	
		if (balon.x>0 && rolPropietario.abstract_robot.canKick(curr_time)) {
			rolPropietario.abstract_robot.kick(curr_time);
		}
		rolPropietario.setEstadoPortero(4);

	}

}
