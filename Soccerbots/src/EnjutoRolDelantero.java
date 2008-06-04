import java.util.Random;

import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.util.Vec2;


public class EnjutoRolDelantero extends EnjutoRol {

	double radio;
	double robot;
	
	Vec2 posicionCentroPorteria;
	Vec2 posicionArribaPorteria;
	Vec2 posicionAbajoPorteria;
	
	int encasillarAtaque;
	
	
	public EnjutoRolDelantero(EnjutoMojamuTeamNachoConRoles jugador, SocSmall robot){
		this.jugador = jugador;
		this.identificadorRol = 0;
		this.abstract_robot = robot;
	}
	
	public void actuarRol(int estadoAtaqueODefensa){
		//!\ Ahora está configurado para que haga lo mismo tanto atacando como defendiendo.
		
		if (estadoAtaqueODefensa == jugador.ATACAR){
			inicializaVariablesAtaque();
			actuarDelantero();
		} else {
			if (estadoAtaqueODefensa == jugador.DEFENDER){
				//jugador.cubrir(jugador.calcularJugadorACubrir());
				
			}
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
	
	private void actuarDelantero(){
		
		long curr_time = jugador.curr_time; 
		Vec2 balon = jugador.balon;
		
		if (radio > robot)
		{
			// set heading behind ball
			abstract_robot.setSpeed(curr_time, 1.0);
			abstract_robot.setSteerHeading(curr_time, posicionCentroPorteria.t);
			
		}
		else
		{
			
			abstract_robot.setSpeed(curr_time, 0.7);
			int aDonde = encasillarAtaque;
			
			if (jugador.encasillado == false) {
				Random random = new Random();
				aDonde = random.nextInt(3);
				encasillarAtaque = aDonde;
				jugador.encasillado = true;
			}
			
			//System.out.println("A donde: " + aDonde);
			switch (aDonde){
				case 0: abstract_robot.setSteerHeading(curr_time, posicionArribaPorteria.t);
				abstract_robot.setDisplayString("D - Up");
				break;
				case 1: abstract_robot.setSteerHeading(curr_time, posicionCentroPorteria.t);
				abstract_robot.setDisplayString("D - Centro");
				break;
				case 2: abstract_robot.setSteerHeading(curr_time, posicionAbajoPorteria.t);
				abstract_robot.setDisplayString("D - Down");
				break;
				default: abstract_robot.setSteerHeading(curr_time, posicionCentroPorteria.t);
				abstract_robot.setDisplayString("D - Centro");
				break;
				
			}
			
			abstract_robot.setSpeed(curr_time, 1.0);
			//System.out.println("GIRA!!");
		}
		// set speed at maximum
		abstract_robot.setSpeed(curr_time, 1.0);

		// kick it if we can
		double distancia = jugador.calcularDistancia(balon, jugador.oponentGoal);
		double distanciaTiro = 0.5;
		if (abstract_robot.canKick(curr_time) && distancia < distanciaTiro)
		{
			abstract_robot.kick(curr_time);
			System.out.print("TIRA!!");
		}
	}
	
	
}
