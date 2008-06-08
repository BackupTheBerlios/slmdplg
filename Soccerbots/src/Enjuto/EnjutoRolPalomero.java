package Enjuto;
import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.util.Vec2;


public class EnjutoRolPalomero extends EnjutoRol {
	
	double radio;
	double robot;
	
	Vec2 posicionCentroPorteria;
	Vec2 posicionArribaPorteria;
	Vec2 posicionAbajoPorteria;
	
	public EnjutoRolPalomero(EnjutoMojamuTeam jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 6;
		this.abstract_robot = robot;
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		//Ahora est� configurado para que haga lo mismo tanto atacando como defendiendo.
		
		if (estadoAtaqueODefensa == EnjutoMojamuTeam.ATACAR){
			abstract_robot.setDisplayString("Palomero Ataca!");			
			inicializaVariablesAtaque();
			actuarPalomero();
		} else {
			if (estadoAtaqueODefensa == EnjutoMojamuTeam.DEFENDER){
				this.jugador.cambiarRol(EnjutoMojamuTeam.DEFENSA);
			}
		}
	}
	
	private void actuarPalomero() {
		
		long curr_time = jugador.curr_time; 
		Vec2 balon = jugador.balon;
		int playerNumber = abstract_robot.getPlayerNumber(curr_time);
		
		int banda = -1; //0 banda superior, 1 banda inferior.
		
		if (playerNumber%2 == 0){
			abstract_robot.setDisplayString("Arriba");
			banda = 0;
		} else {
			abstract_robot.setDisplayString("Abajo");
			banda = 1;
		}
		
		boolean palomero = false;
		double distanciaASuPorteria = jugador.oponentGoal.r;
		//Puede estar en varios puntos del campo
		if (distanciaASuPorteria >= EnjutoMojamuTeam.LONGITUD_CAMPO/2) {
			//Ir a la banda.
			Vec2 puntoBanda;
			if (banda == 0){
				puntoBanda = new Vec2(jugador.oponentGoal.x + (-jugador.SIDE)*EnjutoMojamuTeam.LONGITUD_CAMPO/2, jugador.oponentGoal.y + EnjutoMojamuTeam.ANCHO_CAMPO/2 + 0.2);
				abstract_robot.setDisplayString("Yendo Arriba");
			} else {
				puntoBanda = new Vec2(jugador.oponentGoal.x + (-jugador.SIDE)*EnjutoMojamuTeam.LONGITUD_CAMPO/2, jugador.oponentGoal.y - EnjutoMojamuTeam.ANCHO_CAMPO/2 + 0.2);
				abstract_robot.setDisplayString("Yendo Abajo");
			}
			
			abstract_robot.setSpeed(curr_time, 0.4);
			abstract_robot.setSteerHeading(curr_time, puntoBanda.t);
			abstract_robot.setSpeed(curr_time, 1.0);
		} else {
			if (distanciaASuPorteria >= EnjutoMojamuTeam.LONGITUD_CAMPO/5) {
				//Ir a la banda.
				Vec2 puntoPalomero;
				if (banda == 0){
					puntoPalomero = new Vec2(jugador.oponentGoal.x + (-jugador.SIDE)*EnjutoMojamuTeam.LONGITUD_CAMPO/8, jugador.oponentGoal.y + EnjutoMojamuTeam.ANCHO_CAMPO/4 - 0.2);
					abstract_robot.setDisplayString("Yendo Palom Arriba");
				} else {
					puntoPalomero = new Vec2(jugador.oponentGoal.x + (-jugador.SIDE)*EnjutoMojamuTeam.LONGITUD_CAMPO/8, jugador.oponentGoal.y - EnjutoMojamuTeam.ANCHO_CAMPO/4 - 0.2 );
					abstract_robot.setDisplayString("Yendo Palom Abajo");
				}
				
				abstract_robot.setSpeed(curr_time, 0.4);
				abstract_robot.setSteerHeading(curr_time, puntoPalomero.t);
				abstract_robot.setSpeed(curr_time, 1.0);
				
			} else {
				
				if (distanciaASuPorteria < EnjutoMojamuTeam.LONGITUD_CAMPO/4.5){
					//Estoy en palomero
					abstract_robot.setSpeed(curr_time, 0.0);
					abstract_robot.setSteerHeading(curr_time, jugador.oponentGoal.t);
					abstract_robot.setDisplayString("Estoy en palomero");
					jugador.evitarColision(true);
					palomero = true;
				}
			}
		}
		
		//Modificador de la velocidad en base a la posici�n del bal�n en el eje X
		Vec2 posicionJugador = abstract_robot.getPosition(curr_time);
		if ( (posicionJugador.x - balon.x)*jugador.SIDE > 0 ) {
			
		}
		
		
		//Independiente a la colocaci�n
		double robotApoyo;
		
		if (palomero) {
			robotApoyo = 4*SocSmall.RADIUS;
		} else {
			robotApoyo = 3*SocSmall.RADIUS;
		}
		if (radio <= robotApoyo) {
			
								
			abstract_robot.setDisplayString("PUSHING");
			abstract_robot.setSpeed(curr_time, 0.3);
			if (playerNumber%2 == 0) {
				abstract_robot.setSteerHeading(curr_time, posicionArribaPorteria.t);
			} else {
				abstract_robot.setSteerHeading(curr_time, posicionAbajoPorteria.t);
			}
			abstract_robot.setSpeed(curr_time, 1.0);
		}
		
		double distancia = jugador.calcularDistancia(balon, jugador.oponentGoal);
		if (abstract_robot.canKick(curr_time) && distancia < 0.5)
		{
			abstract_robot.setSpeed(curr_time, 0.7);
			abstract_robot.kick(curr_time);
			abstract_robot.setSpeed(curr_time, 1.0);
		}
	}
	
	private void inicializaVariablesAtaque(){
		Vec2 balon = jugador.balon;
		radio = jugador.balon.r;
		robot = 1.2*SocSmall.RADIUS;
		
		posicionCentroPorteria = new Vec2(balon.x, balon.y);
		posicionCentroPorteria.sub(jugador.oponentGoal);
		posicionCentroPorteria.setr(SocSmall.RADIUS);
		posicionCentroPorteria.add(balon);
		
		posicionArribaPorteria = new Vec2(balon.x, balon.y);
		Vec2 parteArribaPorteria = new Vec2(jugador.oponentGoal.x,jugador.oponentGoal.y + SocSmall.RADIUS*3);
		posicionArribaPorteria.sub(parteArribaPorteria);
		posicionArribaPorteria.setr(SocSmall.RADIUS);
		posicionArribaPorteria.add(balon);
		
		posicionAbajoPorteria = new Vec2(balon.x, balon.y);
		Vec2 parteAbajoPorteria = new Vec2(jugador.oponentGoal.x,jugador.oponentGoal.y - SocSmall.RADIUS*3);			
		posicionAbajoPorteria.sub(parteAbajoPorteria);
		posicionAbajoPorteria.setr(SocSmall.RADIUS);
		posicionAbajoPorteria.add(balon);
	}
	
}
