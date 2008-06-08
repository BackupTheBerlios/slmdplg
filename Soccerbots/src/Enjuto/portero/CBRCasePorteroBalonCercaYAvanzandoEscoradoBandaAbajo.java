package Enjuto.portero;

import EDU.gatech.cc.is.util.Vec2;
import Enjuto.EnjutoRolPortero;

public class CBRCasePorteroBalonCercaYAvanzandoEscoradoBandaAbajo implements
		CBRCase {

	EnjutoRolPortero rolPropietario;
	
	public CBRCasePorteroBalonCercaYAvanzandoEscoradoBandaAbajo(
			EnjutoRolPortero rolPropietario) {
		super();
		this.rolPropietario = rolPropietario;
	}
	
	@Override
	public boolean acceptDescription() {
		if (rolPropietario.jugador.balonCercaAreaPropia() && rolPropietario.jugador.balonAvanzandoEscoradoBandaAbajo())
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
			if (balon.y >0.2) {
				balonConCorreccionOfensiva.setx(balon.x-0.45*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);					
			}
			else {
				balonConCorreccionOfensiva.setx(balon.x-0.33*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);
			}
		}
		else {
			if (balon.y >0.2) {
				balonConCorreccionOfensiva.setx(balon.x+0.48*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);					
			}
			else {
				balonConCorreccionOfensiva.setx(balon.x+0.38*balon.x);
				balonConCorreccionOfensiva.sety(balon.y);
			}
		}
		if (rolPropietario.getEstadoPortero() != 6)
			rolPropietario.abstract_robot.setSpeed(curr_time, 0);
		balonConCorreccionOfensiva.normalize(3);
		rolPropietario.abstract_robot.setSteerHeading(curr_time, balonConCorreccionOfensiva.t);
		rolPropietario.abstract_robot.setSpeed(curr_time, 0.9); //Antes 0.5
		rolPropietario.setEstadoPortero(6);
	}

}
