import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.util.Vec2;


public class EnjutoRolPalomero extends EnjutoRol {
	
	double radio;
	double robot;
	
	Vec2 posicionCentroPorteria;
	Vec2 posicionArribaPorteria;
	Vec2 posicionAbajoPorteria;
	
	public EnjutoRolPalomero(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 0;
		this.abstract_robot = robot;
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		//!\ Ahora está configurado para que haga lo mismo tanto atacando como defendiendo.
		
		if (estadoAtaqueODefensa == jugador.ATACAR){
			
		} else {
			if (estadoAtaqueODefensa == jugador.DEFENDER){
				//jugador.cubrir(jugador.calcularJugadorACubrir());
			}
		}
	}
	
	private void actuarPalomero() {
		
		long curr_time = jugador.curr_time; 
		Vec2 balon = jugador.balon;
		int playerNumber = abstract_robot.getPlayerNumber(curr_time);
		
		
		Vec2 posicion = abstract_robot.getPosition(curr_time);
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
		if (distanciaASuPorteria >= jugador.LONGITUD_CAMPO/2) {
			//Ir a la banda.
			Vec2 puntoBanda;
			if (banda == 0){
				puntoBanda = new Vec2(jugador.oponentGoal.x + (-jugador.SIDE)*jugador.LONGITUD_CAMPO/2, jugador.oponentGoal.y + jugador.ANCHO_CAMPO/2 + 0.2);
				abstract_robot.setDisplayString("Yendo Arriba");
			} else {
				puntoBanda = new Vec2(jugador.oponentGoal.x + (-jugador.SIDE)*jugador.LONGITUD_CAMPO/2, jugador.oponentGoal.y - jugador.ANCHO_CAMPO/2 + 0.2);
				abstract_robot.setDisplayString("Yendo Abajo");
			}
			
			abstract_robot.setSpeed(curr_time, 0.4);
			abstract_robot.setSteerHeading(curr_time, puntoBanda.t);
			abstract_robot.setSpeed(curr_time, 1.0);
		} else {
			if (distanciaASuPorteria >= jugador.LONGITUD_CAMPO/5) {
				//Ir a la banda.
				Vec2 puntoPalomero;
				if (banda == 0){
					puntoPalomero = new Vec2(jugador.oponentGoal.x + (-jugador.SIDE)*jugador.LONGITUD_CAMPO/8, jugador.oponentGoal.y + jugador.ANCHO_CAMPO/4 - 0.2);
					abstract_robot.setDisplayString("Yendo Palom Arriba");
				} else {
					puntoPalomero = new Vec2(jugador.oponentGoal.x + (-jugador.SIDE)*jugador.LONGITUD_CAMPO/8, jugador.oponentGoal.y - jugador.ANCHO_CAMPO/4 - 0.2 );
					abstract_robot.setDisplayString("Yendo Palom Abajo");
				}
				
				abstract_robot.setSpeed(curr_time, 0.4);
				abstract_robot.setSteerHeading(curr_time, puntoPalomero.t);
				abstract_robot.setSpeed(curr_time, 1.0);
				
			} else {
				
				if (distanciaASuPorteria < jugador.LONGITUD_CAMPO/4.5){
				//if ((posicion.y >= oponentGoal.y + 0.4) || (posicion.y <= oponentGoal.y - 0.4) ) {
					//Estoy en palomero
					abstract_robot.setSpeed(curr_time, 0.0);
					abstract_robot.setSteerHeading(curr_time, jugador.oponentGoal.t);
					abstract_robot.setDisplayString("Estoy en palomero");
					jugador.evitarColision(true);
					palomero = true;
				} else {
					//Ir a palomero
					
				}
			}
		}
		
		
		//Independiente a la colocación
		double robotApoyo;
		
		if (palomero) {
			robotApoyo = 2*abstract_robot.RADIUS;
		} else {
			robotApoyo = 3*abstract_robot.RADIUS;
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
			System.out.print("TIRA!!");
		}
	}
	
	private void inicializaVariablesAtaque(){
		Vec2 balon = jugador.balon;
		radio = jugador.balon.r;
		robot = 1.2*abstract_robot.RADIUS;
		
		posicionCentroPorteria = new Vec2(balon.x, balon.y);
		posicionCentroPorteria.sub(jugador.oponentGoal);
		posicionCentroPorteria.setr(abstract_robot.RADIUS);
		posicionCentroPorteria.add(balon);
		
		posicionArribaPorteria = new Vec2(balon.x, balon.y);
		Vec2 parteArribaPorteria = new Vec2(jugador.oponentGoal.x,jugador.oponentGoal.y + abstract_robot.RADIUS*3);
		posicionArribaPorteria.sub(parteArribaPorteria);
		posicionArribaPorteria.setr(abstract_robot.RADIUS);
		posicionArribaPorteria.add(balon);
		
		posicionAbajoPorteria = new Vec2(balon.x, balon.y);
		Vec2 parteAbajoPorteria = new Vec2(jugador.oponentGoal.x,jugador.oponentGoal.y - abstract_robot.RADIUS*3);			
		posicionAbajoPorteria.sub(parteAbajoPorteria);
		posicionAbajoPorteria.setr(abstract_robot.RADIUS);
		posicionAbajoPorteria.add(balon);
	}
	
}
