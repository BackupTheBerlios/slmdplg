import java.util.Vector;

import EDU.gatech.cc.is.abstractrobot.ControlSystemSS;
import EDU.gatech.cc.is.util.Vec2;


public class Jugador_Cool_Def extends ControlSystemSS {

	private final double RADIO_JUGADOR = abstract_robot.RADIUS;

	private long tiempo;

	private Vec2 jugador_actual;

	// private Vec2 jugador_actual_desde_centro;
	private Vec2 nuestra_porteria_polares;

	private Vec2 su_porteria_polares;

	private Vec2 nuestra_porteria;

	private Vec2 su_porteria;

	private boolean derecha;

	private Vec2 pelota;

	private Vec2 pelota_polares;

	// 1 si estás a la derecha o -1 si estás a la izquierda
	private double const_mult = 1;

	private Vec2 direccion = new Vec2();

	private Vec2[] contrarios_polares;

	private boolean tira;

	private long curr_time; //What time is it?

	private long mynum; //Who am I?

	private double rotation; //What direction am I pointing?

	private Vec2 ball; //Where is the ball?

	private Vec2 ourgoal; //Where is our goal?

	private Vec2 theirgoal; //Where is their goal?

	private Vec2[] teammates; //Where are my teammates?

	private Vec2[] opponents; //Where are my opponents?

	//Where is the closest...
	private Vec2 closest_team; //Teammate?

	private Vec2 closest_opp; //Opponent?

	private Vec2 closest_to_ball; //Teammate to the Ball?

	private Vec2 move; //Move in move.t direction

	//  with speed move.r
	private boolean kickit; //Try to kick it

	// what side of the field are we on? -1 for west +1 for east
	private int SIDE;

	// a vector pointing to me.
	private static final Vec2 ME = new Vec2(0, 0);

	// restated here for convenience
	private final double ROBOT_RADIUS = abstract_robot.RADIUS;

	private static final boolean DEBUG = false;

	private static final double FIELD_WIDTH = 1.525;

	private static final double FIELD_LENGTH = 2.74;

	private static final double GOAL_WIDTH = 0.5;

	private static final double MARGIN = 0.02;

	private static final double RANGE = 0.3;

	private static final double TEAMMATE_G = 1.0;

	private static final double WALL_G = 1.0;

	private static final double GOALIE_G = 2.0;

	private static final double FORCE_LIMIT = 1.0;

	// Relativas a la esquina inferior izquierda del campo
	private Vec2 convierte_a_cartesianas(Vec2 polares) {
		Vec2 cartesianas = new Vec2();
		cartesianas.setx(Math.abs(Math.min(nuestra_porteria_polares.x,
				su_porteria_polares.x)));
		cartesianas.sety((-1) * nuestra_porteria_polares.y + .8625);
		// cartesianas.sety(polares.y + (-1)*nuestra_porteria_polares.y +
		// .8625);
		return cartesianas;

	}

	private Vec2 cambia_eje_refencia(Vec2 desde_jugador) {
		Vec2 jugador_actual_2 = (Vec2) jugador_actual.clone();

		Vec2 desde_centro_campo = (Vec2) desde_jugador.clone();
		/* Hacer copia de los vectores porq sino add peta */

		desde_centro_campo.add(jugador_actual_2);
		return desde_centro_campo;
	}

	public synchronized void actualiza_valores() {
		tiempo = abstract_robot.getTime();
		nuestra_porteria_polares = abstract_robot.getOurGoal(tiempo);
		su_porteria_polares = abstract_robot.getOpponentsGoal(tiempo);
		jugador_actual = abstract_robot.getPosition(tiempo);
		contrarios_polares = abstract_robot.getOpponents(tiempo);
		pelota_polares = abstract_robot.getBall(tiempo);
		pelota = cambia_eje_refencia(pelota_polares);
		nuestra_porteria = cambia_eje_refencia(nuestra_porteria_polares);
		su_porteria = cambia_eje_refencia(su_porteria_polares);
		tira = false;
		Vec2 closest;
		Vec2 temp = new Vec2(0, 0);

		/*--- bookkeeping data ---*/
		// get the current time for timestamps
		curr_time = abstract_robot.getTime();

		// get my player id
		mynum = abstract_robot.getPlayerNumber(curr_time);

		/*--- sensor data ---*/
		// get vector to the ball
		ball = abstract_robot.getBall(curr_time);

		// get vector to our and their goal
		ourgoal = abstract_robot.getOurGoal(curr_time);
		theirgoal = abstract_robot.getOpponentsGoal(curr_time);

		// get a list of the positions of our teammates
		teammates = abstract_robot.getTeammates(curr_time);

		// get a list of the positions of the opponents
		opponents = abstract_robot.getOpponents(curr_time);

		// get closest data
		closest_team = closest_to(ME, teammates);
		closest_opp = closest_to(ME, opponents);

		closest = closest_to(ball, teammates);

		temp.sett(closest.t);
		temp.setr(closest.r);

		temp.sub(ball);

		if (temp.r > ball.r)
			closest_to_ball = ME;
		else
			closest_to_ball = closest;

		rotation = abstract_robot.getSteerHeading(curr_time);

		/*--- default actuator control ---*/
		// set movement data: rotation and speed;
		move.sett(0.0);
		move.setr(0.0);

		// set kicking
		kickit = false;
	}

	public void Configure() {
		tiempo = abstract_robot.getTime();
		nuestra_porteria_polares = abstract_robot.getOurGoal(tiempo);
		su_porteria_polares = abstract_robot.getOpponentsGoal(tiempo);
		jugador_actual = abstract_robot.getPosition(tiempo);
		contrarios_polares = abstract_robot.getOpponents(tiempo);
		pelota_polares = abstract_robot.getBall(tiempo);
		nuestra_porteria = cambia_eje_refencia(nuestra_porteria_polares);
		su_porteria = cambia_eje_refencia(su_porteria_polares);
		if (nuestra_porteria.x < 0) {
			const_mult = -1;
		} else {
			const_mult = 1;

		}
		curr_time = abstract_robot.getTime();
		if (abstract_robot.getOurGoal(curr_time).x < 0)
			SIDE = -1;
		else
			SIDE = 1;

		move = new Vec2(0, 0);
	}

	public int TakeStep() {
		//Resultado.Guardar(this,abstract_robot);
		actualiza_valores();
		move.setr(1);
		boolean es_portero = false;
		if (abstract_robot.getPlayerNumber(curr_time)==0)
		{
			abstract_robot.setSpeed(curr_time,1);
			this.actua_Portero();			
			es_portero = true;
		}
		if (abstract_robot.getPlayerNumber(curr_time)==3)
		{
			this.juega_delantero();		
		}
		else if (abstract_robot.getPlayerNumber(curr_time)==4)
		{
			bloquea_portero();			
		}
		else if (abstract_robot.getPlayerNumber(curr_time)==1)
			this.play_backup();
		else if (abstract_robot.getPlayerNumber(curr_time)==2)
			
		{
			this.play_backup2();			
		}
		
		if (!es_portero)
		{
		abstract_robot.setSteerHeading(curr_time, move.t);
		abstract_robot.setSpeed(curr_time, move.r);
		if (kickit && abstract_robot.canKick( curr_time))
			abstract_robot.kick(curr_time);
		}	
		
		return(CSSTAT_OK);
	}

	private Vec2 calcula_pos_relativa_centro_campo(Vec2 v){
Vec2 vec_jugador = (Vec2)abstract_robot.getPosition(curr_time).clone();
Vec2 absoluta = (Vec2)v.clone();
absoluta.add(vec_jugador);
		return absoluta;
	}
	
	
	private void actua_Portero() {
		abstract_robot.setDisplayString("Portero");
		Vec2 pelota_abs = calcula_pos_relativa_centro_campo(ball);
		Vec2 porteria_abs = calcula_pos_relativa_centro_campo(ourgoal);
		pelota_abs.sub(porteria_abs);
		
		//en pelota abs tenemos un vector de la porteria a la pelota
		
		pelota_abs.normalize(0.3);
		Vec2 destino = (Vec2)porteria_abs.clone();
		destino.add(pelota_abs);
		
		Vec2 pos = abstract_robot.getPosition(curr_time);
		Vec2 ir_a = new Vec2(porteria_abs.x,destino.y);
		ir_a.sub(pos);
		abstract_robot.setSteerHeading(curr_time, ir_a.t);
		
		if(ir_a.r >  0.05){
			abstract_robot.setSpeed(curr_time, 1.0);
		}
		else{
			abstract_robot.setSpeed(curr_time, 0.0);
		}
	}
	private void play_backup2() {

		if (soy_el_mas_cercano_a_la_pelota_de_mi_equipo()) {
			if ((ourgoal.r > 0.75)) {
				ataca();
				kickit = true;
			} else {
				kickit = true;
				abstract_robot.setDisplayString("a por la pelota");
				if (ball.r < 0.2)
					drive_ball();
				else
					this.get_behind(ball, theirgoal);
			}
		} else {
			if ((ourgoal.r > 1.45)) {
				abstract_robot
						.setDisplayString("me voy al medio campo nuestro");
				move.sett(ourgoal.t);
				if (ourgoal.r < 0.3) {
					move.sett(ball.t);
					move.setr(0);
				}
				avoidcollision();

			} else {
				abstract_robot.setDisplayString("cubro");
				Vec2 backup = contrario_no_cubierto_mas_cercano_a_mi();
				if (backup == null)
					backup = ball;
				move.sett(backup.t);
				move.setr(backup.r);
				avoidcollision();
			}
		}

	}

	private void juega_delantero() {


			//Soy el más cercano a la bola
			if (soy_el_mas_cercano_a_la_pelota_de_mi_equipo()) {
				abstract_robot.setDisplayString("ataco");
				ataca();
			}
			//No es el más cercano a la pelota
			else {
				if (tienen_la_pelota()) {
					get_behind(ball, theirgoal);
					this.avoidcollision();
				} else

				{
					move.sett(ball.t);
					move.setr(0);
				}
			}
		}

	

	private void play_backup() {
		if ((soy_el_mas_cercano_a_la_pelota_de_mi_equipo())) {
			if (!nos_atacan()) {
				if ((ourgoal.r < 1)) {
					abstract_robot
							.setDisplayString("estoy cerca y tiro a la otra portería");
					this.get_behind(ball, theirgoal);
					//kickit = true;
				} else {
					abstract_robot.setDisplayString("a por la pelota");
					drive_ball();
				}
			} else {
				abstract_robot.setDisplayString("nos atacan y estoy más cerca");
				Vec2 backup = new Vec2();
				backup.sett(ball.t);
				backup.setr(ball.r);
				backup.setx(backup.x + ROBOT_RADIUS * SIDE);
				get_behind(backup, theirgoal);
				//move.sett(this.GoBehindBall());
			}
		}

		else

		{
			{
				Vec2 cubrir = contrario_no_cubierto_mas_cercano_a_mi_dentro_de_mi_campo();
				if (cubrir != null) {
					abstract_robot.setDisplayString("cubro"
							+ abstract_robot.getPlayerNumber(curr_time)
							+ "a un contrario");
					move.sett(cubrir.t);
					if (cubrir.r > 0.3)
						avoidcollision();
				} else {
					if (ourgoal.r > 0.3) {
						move.sett(ourgoal.t);
						this.avoidcollision();
					} else {
						this.get_behind(ball, theirgoal);
						//move.sett(this.GoBehindBall());
					}

				}
			}

		}

	}

	private void bloquea_portero() {
		Vec2 su_portero;
		double d_porteria = 9999;
		int min = -1;
		opponents = abstract_robot.getOpponents(curr_time);
		for (int i = 0; i < opponents.length; i++) {
			double d = distancia(theirgoal, opponents[i]);
			//System.out.println(d);
			if (d < d_porteria) {
				min = i;
				d_porteria = d;
			}
		}
		//System.out.println(min+ " " + d_porteria);
		if (min != -1) {
			move.sett(opponents[min].t);
			move.setr(1);
			if (theirgoal.r > 0.6)
				avoidcollision();
		}
		//else
		//	System.out.println("me quedo");

	}

	private boolean tienen_la_pelota() {
		double dist_nosotros = 9999;
		for (int i = 0; i < this.teammates.length; i++) {

			dist_nosotros = Math.min(distancia(ball, teammates[i]),
					dist_nosotros);
		}
		double dist_suya = 9999;
		for (int i = 0; i < this.teammates.length; i++) {

			dist_nosotros = Math.min(distancia(ball, teammates[i]), dist_suya);
		}
		return dist_suya > dist_nosotros;
	}

	// Devuelve nulo si no hay ninguno interesante
	private Vec2 contrario_mas_cercano_al_balon_en_polares() {
		int p_mejor = -1;
		double distancia_contrario = 9999;
		for (int i = 0; i < contrarios_polares.length; i++) {
			double ox = contrarios_polares[i].x;
			double oy = contrarios_polares[i].y;
			double bx = pelota_polares.x;
			double by = pelota_polares.y;
			double d_o_b = Math.sqrt((ox - bx) * (ox - bx) + (oy - by)
					* (oy - by));
			if (d_o_b < this.RADIO_JUGADOR * 4) {
				p_mejor = i;
			}
		}
		if (p_mejor != -1)
			return contrarios_polares[p_mejor];
		else
			return null;

	}

	private double distancia(Vec2 p1, Vec2 p2) {
		return (Math.sqrt((p1.x * p2.x) + (p1.y * p1.y)));
	}

	private Vec2 closest_to(Vec2 point, Vec2[] objects) {
		double dist = 9999;
		Vec2 result = new Vec2(0, 0);
		Vec2 temp = new Vec2(0, 0);

		for (int i = 0; i < objects.length; i++) {

			// find the distance from the point to the current
			// object
			temp.sett(objects[i].t);
			temp.setr(objects[i].r);
			temp.sub(point);

			// if the distance is smaller than any other distance
			// then you have something closer to the point
			if (temp.r < dist) {
				result = objects[i];
				dist = temp.r;
			}
		}

		return result;
	}

	private void get_behind(Vec2 point, Vec2 orient) {
		Vec2 behind_point = new Vec2(0, 0);
		double behind = 0;
		double point_side = 0;

		// find a vector from the point, away from the orientation
		// you want to be
		behind_point.sett(orient.t);
		behind_point.setr(orient.r);

		behind_point.sub(point);
		behind_point.setr(-ROBOT_RADIUS * 1.8);

		// determine if you are behind the object with respect
		// to the orientation
		behind = Math.cos(Math.abs(point.t - behind_point.t));

		// determine if you are on the left or right hand side
		// with respect to the orientation
		point_side = Math.sin(Math.abs(point.t - behind_point.t));

		// if you are in FRONT
		if (behind > 0) {
			// make the behind point more of a beside point
			// by rotating it depending on the side of the
			// orientation you are on
			if (point_side > 0)
				behind_point.sett(behind_point.t + Math.PI / 2);
			else
				behind_point.sett(behind_point.t - Math.PI / 2);
		}

		// move toward the behind point
		move.sett(point.t);
		move.setr(point.r);
		move.add(behind_point);

		move.setr(1.0);

	}

	private boolean behind_point(Vec2 point, Vec2 orient) {

		// you are behind an object relative to the orientation
		// if your position relative to the point and the orientation
		// are approximately the same
		if (Math.abs(point.t - orient.t) < Math.PI / 10)
			return true;
		else
			return false;
	}

	double GoBehindBall()
	// which theta to goto to go behind the ball (me->ball->goal)
	{
		Vec2 result;
		result = ball;
		//  if(CloseToBall(me) && !HaveBall(me))
		{
			Vec2 kickspot = new Vec2(ball.x, ball.y);
			kickspot.sub(theirgoal);
			kickspot.sety(kickspot.y + ROBOT_RADIUS * 2);
			//   kickspot.sub(BottomGoalPost());
			Vec2 B = new Vec2(kickspot.x, kickspot.y);
			Vec2 C = new Vec2(kickspot.x, kickspot.y);
			Vec2 D = new Vec2(kickspot.x, kickspot.y);
			kickspot.setr(ROBOT_RADIUS);// + BALL_RADIUS);
			B.setr(ROBOT_RADIUS * 2);
			C.setr(ROBOT_RADIUS * 2);
			D.setr(ROBOT_RADIUS * 1.5);

			B.sett(B.t - Vec2.PI / 2);
			C.sett(C.t + Vec2.PI / 2);
			kickspot.add(ball);
			B.add(ball);
			C.add(ball);
			D.add(ball);
			// figure out wich quadrant w/ respect to ball

			int quad = Quadrant(ball.t - theirgoal.t);
			if ((quad == 1) || (quad == 4)) {
				if (Math.abs(ball.t - theirgoal.t) < Vec2.PI / 6) {
					result = kickspot;
					result.setr(1.0);
				} else {
					result = D;
					//     result.setr(1.0);
				}
			} else if (quad == 3) {
				result = B;
				result.setr(1.0);
			} else // quad = 2
			{
				result = C;
				result.setr(1.0);
			}
		}
		return result.t;
	}

	private void avoidcollision() {
		// an easy way to avoid collision

		// first keep out of your teammates way
		// if your closest teammate is too close, the move away from
		if (closest_team.r < ROBOT_RADIUS * 1.4) {
			move.setx(-closest_team.x);
			move.sety(-closest_team.y);
			move.setr(1.0);
		}

		// if the closest opponent is too close, move away to try to
		// go around
		else if (closest_opp.r < ROBOT_RADIUS * 1.4) {
			move.setx(-closest_opp.x);
			move.sety(-closest_opp.y);
			move.setr(1.0);
		}

	}

	private void ataca() {

		if (!((behind_point(ball, theirgoal)) && (ball.r < this.ROBOT_RADIUS * 1.5))) {
			abstract_robot.setDisplayString("Voy porque soy");
			//move.sett(this.GoBehindBall());
			drive_ball();
			//avoidcollision();
		} else {
			//Si estoy cerca
			if (theirgoal.r <= 1) {
				abstract_robot.setDisplayString("tengo el balon y he de tirar");

				Vec2[] obstaculos = abstract_robot.getOpponents(curr_time);

				Vec2 disparo = (Vec2) theirgoal.clone();
				double dif = 0.25;
				disparo.sett(disparo.t - dif);
				Vec2 max_ang = new Vec2();
				max_ang.sett(disparo.t + dif);
				int i = 0;
				boolean tengo_tiro = false;

				while (disparo.t < max_ang.t) {
					tengo_tiro = true;
					for (int j = 0; j < obstaculos.length; j++) {
						//System.out.print(Math.abs(Math.abs(disparo.t) - Math.abs(obstaculos[i].t)));
						if ((Math.abs(Math.abs(disparo.t)
								- Math.abs(obstaculos[i].t)) < 0.2)) {
							disparo.sett(disparo.t + 0.05);
							//System.out.print(" " + disparo.t);
							tengo_tiro = false;
							break;
						}
					}
					if (tengo_tiro)
						break;
				}
				//System.out.print(tengo_tiro);
				obstaculos = teammates;
				i = 0;
				while (disparo.t < max_ang.t) {
					tengo_tiro = true;
					for (int j = 0; j < obstaculos.length; j++) {
						//System.out.print(Math.abs(Math.abs(disparo.t) - Math.abs(obstaculos[i].t)));
						if ((Math.abs(Math.abs(disparo.t)
								- Math.abs(obstaculos[i].t)) < 0.2)) {
							disparo.sett(disparo.t + 0.05);
							System.out.print(" " + disparo.t);
							tengo_tiro = false;
							break;
						}
					}
					if (tengo_tiro)
						break;
				}
				//	System.out.println(tengo_tiro);
				//Hemos encontrado hueco
				if (tengo_tiro) {
					//	System.out.println("DEF" + Math.abs(Math.abs(disparo.t) - Math.abs(theirgoal.t)));
					//	if (Math.abs(Math.abs(disparo.t) - Math.abs(theirgoal.t))<0.25)
					{
						move.sett(disparo.t);
						if (abstract_robot.canKick(curr_time)) {
							abstract_robot.kick(curr_time);
						}
					}
					//	else
					{
						//	get_behind(ball,disparo);
					}

					abstract_robot.setDisplayString("tengo tiro");
					//					abstract_robot.setDisplayString("no hay obstaculo");
					//	get_behind(ball,disparo);
					//move.sett(disparo.t);

					//	this.avoidcollision();

					//abstract_robot.setSteerHeading(curr_time,disparo.t);

				} else {

					abstract_robot
							.setDisplayString("hay obstaculo,sigo hacia su porteria");
					//Buscamos una dirección sin contrarios
					Vec2 direccion = (Vec2) theirgoal.clone();
					direccion.sett(direccion.t - dif);
					max_ang = new Vec2();
					max_ang.sett((-1) * direccion.t);
					i = 0;
					obstaculos = opponents;
					for (int k = 0; k < 15; k++) {
						boolean ok = true;
						for (int j = 0; j < obstaculos.length; j++) {
							if ((Math.abs(disparo.t - obstaculos[i].t) < 0.3)) {
								disparo.sett(disparo.t + 0.40);
								ok = false;
								break;
							}
						}
						if (ok)
							break;
					}
					move.sett(direccion.t);
					//avoidcollision();
				}

			}
			//Si no puedo tirar corrijo
			else {
				abstract_robot.setDisplayString("no puedo tirar");
				//get_behind(ball,theirgoal);
				move.sett(theirgoal.t);
				move.setr(1);
				//avoidcollision();
				//Si no a ver que hacemos

			}
		}
	}

	public boolean nos_atacan() {
		return (distancia(ourgoal, ball) < 1.2);
	}

	public boolean soy_el_mas_cercano_a_la_pelota_de_mi_equipo() {
		double d = ball.r;
		for (int i = 0; i < teammates.length; i++) {
			if (distancia(teammates[i], ball) < d)
				return false;
		}
		return true;
	}

	private void drive_ball() {
		// if i'm behind the ball (oriented toward the goal) then
		// start charging the goal
		if (behind_point(ball, theirgoal) && ball.t < ROBOT_RADIUS * 1.5) {
			move.sett(theirgoal.t);
			move.setr(1.0);

			if ((Math.abs(rotation - theirgoal.t) < Math.PI / 8)
					&& (theirgoal.r < ROBOT_RADIUS * 15)) {
				move = getKickPosition(ball, theirgoal, curr_time);
				kickit = true;

			}

		} else {
			get_behind(ball, theirgoal);
			//avoidcollision();
		}
	}

	public Vec2 contrario_no_cubierto_mas_cercano_a_mi_dentro_de_mi_campo() {

		int p = -1;
		double d = 9999;
		Vec2[] no_cubiertos = contrario_no_cubiertos();
		for (int i = 0; i < no_cubiertos.length; i++) {
			if ((no_cubiertos[i] != null) && (no_cubiertos[i].r < d)
					&& (distancia(no_cubiertos[i], ourgoal) < 1)) {
				p = i;
				d = distancia(no_cubiertos[i], ball);
			}
		}
		//System.out.println("Cubro a " + p);
		if (p != -1)
			return no_cubiertos[p];
		else
			return null;
	}

	public Vec2 contrario_no_cubierto_mas_cercano_a_mi() {

		int p = -1;
		double d = 9999;
		Vec2[] no_cubiertos = contrario_no_cubiertos();
		for (int i = 0; i < no_cubiertos.length; i++) {
			if ((no_cubiertos[i] != null) && (no_cubiertos[i].r < d)) {
				p = i;
				d = distancia(no_cubiertos[i], ball);
			}
		}
		//System.out.println("Cubro a " + p);
		if (p != -1)
			return no_cubiertos[p];
		else
			return null;
	}

	static int Quadrant(double theta) {
		double t = NormalizePI(theta);
		if ((t > 0) && (t < Vec2.PI * .5))
			return 1;
		else if ((t >= Vec2.PI * .5) && (t < Vec2.PI))
			return 2;
		else if ((t >= Vec2.PI) && (t < Vec2.PI * 1.5))
			return 3;
		else
			//if ((t >= PI * 1.5) && (t < PI2))
			return 4;
	}

	private Vec2 companero_que_bloquea() {
		Vec2[] companeros = abstract_robot.getTeammates(curr_time);
		int bloquea = -1;
		double dist = 9999;
		for (int i = 0; i < companeros.length; i++) {
			Vec2 dist_comp_port = (Vec2) theirgoal.clone();
			dist_comp_port.sub(companeros[i]);
			if (dist_comp_port.x < dist) {
				dist = dist_comp_port.x;
				bloquea = i;
			}
		}
		if (bloquea != -1) {
			return companeros[bloquea];
		} else
			return null;
	}

	private Vec2 getKickPosition(Vec2 ball, Vec2 target, long time) {
		Vec2 v = new Vec2(target);
		v.sub(ball);
		double alpha = v.t + Math.PI;
		v = new Vec2((1.0 / Math.sqrt(2)) * abstract_robot.RADIUS
				* Math.cos(alpha), (1.0 / Math.sqrt(2)) * abstract_robot.RADIUS
				* Math.sin(alpha));
		alpha = angle(ball, v);
		if (alpha < Math.PI / 2.0) {
			if (cross(ball, v) > 0.0) {
				v.sett(v.t + (Math.PI / 2.0 - alpha));
			} else {
				v.sett(v.t - (Math.PI / 2.0 - alpha));
			}
		}
		v.add(ball);
		return v;
	}

	// Should also consider walls!!!
	private double getFreeDirection(Vec2 goal, double range, long time) {
		ObstacleList obstacles = new ObstacleList();
		for (int k = 0; k < 2; k++) {
			Vec2[] ps = (k == 0) ? abstract_robot.getOpponents(time)
					: abstract_robot.getTeammates(time);
			Vec2[] players = new Vec2[ps.length];
			for (int i = 0; i < players.length; i++) {
				players[i] = new Vec2(ps[i]);
				players[i].setr(players[i].r + abstract_robot.RADIUS);
				Vec2 diff = new Vec2(goal);
				diff.sub(players[i]);
				if ((players[i].r < 2 * abstract_robot.RADIUS + MARGIN)
						|| (players[i].r < range
								&& players[i].r < goal.r
										+ abstract_robot.RADIUS && diff.r < goal.r)) {
					obstacles.add(new Obstacle(goal, players[i],
							abstract_robot.RADIUS, abstract_robot.RADIUS));
				}
			}

		}
		double dir = goal.t;
		if (obstacles.size() > 0) {
			Obstacle bound = obstacles.getBoundaries();
			if (bound.obscures(dir)) {
				for (int i = 0; i < obstacles.size(); i++) {
					Obstacle o = obstacles.get(0);
					if (o.obscures(dir)) {
						if (angle(dir, o.getLeft()) < angle(o.getRight(), dir)) {
							dir = o.getLeft();
						} else {
							dir = o.getRight();
						}
						break;
					}
				}
			}
		}
		StringBuffer sb = new StringBuffer();
		sb.append(rad2deg(goal.t));
		sb.append(' ').append(obstacles);
		sb.append(" -> ").append(rad2deg(dir));
		abstract_robot.setDisplayString(sb.toString());
		return dir;
	}

	private Vec2[] contrario_no_cubiertos() {

		Vec2[] dev = new Vec2[5];
		int p = 0;
		for (int i = 0; i < opponents.length; i++) {
			boolean cubierto = false;
			int j = 0;
			//System.out.print("distancia de mis compis ");
			while ((j < teammates.length) && (!cubierto)) {
				//System.out.print(distancia(opponents[i],teammates[j]) + " ");
				if (distancia(opponents[i], teammates[j]) < 0.2) {
					//	System.out.println("Está cubierto " + distancia(opponents[i],teammates[j]));
					cubierto = true;
				} else
					j++;
			}
			//System.out.println();
			if (!cubierto) {
				dev[p] = opponents[i];
				p++;
			}

		}

		return dev;
	}

	public Vec2 contrario_no_cubierto_mas_cercano_a_la_pelota() {

		int p = -1;
		double d = 9999;
		Vec2[] no_cubiertos = contrario_no_cubiertos();
		for (int i = 0; i < no_cubiertos.length; i++) {
			if ((no_cubiertos[i] != null)
					&& (distancia(no_cubiertos[i], ball) < d)) {
				p = i;
				d = distancia(no_cubiertos[i], ball);
			}
		}
		//System.out.println("Cubro a " + p);
		if (p != -1)
			return no_cubiertos[p];
		else
			return null;
	}

	static double NormalizePI(double t) {
		while (t > Vec2.PI2)
			t -= Vec2.PI2;
		while (t < 0)
			t += Vec2.PI2;
		return t;
	}

	protected static double dot(Vec2 v, Vec2 u) {
		return v.x * u.x + v.y * u.y;
	}

	protected static double angle(Vec2 v, Vec2 u) {
		return Math.acos(dot(v, u) / (v.r * u.r));
	}

	protected static double cross(Vec2 v, Vec2 u) {
		return v.x * u.y - v.y * u.x;
	}

	protected static int rad2deg(double alpha) {
		return (int) (180.0 * alpha / Math.PI);
	}

	/**
	 * @return the angle, in the range 0 through <I>pi</I>, between
	 * the two given angles.
	 */
	protected static double angle(double alpha, double beta) {
		return Math.acos(Math.cos(alpha - beta));
	}

	class Obstacle extends Object {
		/**
		 * The left angle.
		 */
		private double left;

		/**
		 * The right angle.
		 */
		private double right;

		/**
		 * Creates an obstacle with the given boundaries.
		 *
		 * @param left left angle given in radians.
		 * @param right right anfle given in radians.
		 */
		protected Obstacle(double left, double right) {
			this.left = left;
			this.right = right;
		}

		protected Obstacle(Vec2 g, Vec2 p, double r, double ownR) {
			double cp = cross(g, p);
			double d = ownR + r + MARGIN;
			double t = g.t;
			if (cp >= 0.0) {
				if (p.r <= d) {
					t = p.t - 1.1 * Math.PI / 2.0;
				} else {
					t -= Math.PI / 2.0;
				}
			} else {
				if (p.r <= d) {
					t = p.t + 1.1 * Math.PI / 2.0;
				} else {
					t += Math.PI / 2.0;
				}
			}
			Vec2 v1 = new Vec2(d * Math.cos(t), d * Math.sin(t));
			if (p.r > d) {
				v1.add(p);
			}
			if (p.r <= d) {
				if (cp >= 0) {
					t = p.t + 1.1 * Math.PI / 2.0;
				} else {
					t = p.t - 1.1 * Math.PI / 2.0;
				}
			} else {
				t = g.t + Math.PI;
			}
			Vec2 v2 = new Vec2(d * Math.cos(t), d * Math.sin(t));
			if (p.r > d) {
				v2.add(p);
			}
			if (cp >= 0.0) {
				left = v2.t;
				right = v1.t;
			} else {
				left = v1.t;
				right = v2.t;
			}
		}

		/**
		 * @return the left boundary, in radians, of this obstacle.
		 */
		public double getLeft() {
			return left;
		}

		/**
		 * @return the right boundary, in radians, of this obstacle.
		 */
		public double getRight() {
			return right;
		}

		/**
		 * @param alpha an angle to check given in radians.
		 *
		 * @return <CODE>true</CODE> if this obstacle obscures the
		 * given angle; <CODE>false</CODE> otherwise.
		 */
		protected boolean obscures(double alpha) {
			if (left * right < 0.0) {
				if (left > 0.0) {
					return left > alpha && alpha > right;
				} else {
					return left > alpha || alpha > right;
				}
			} else if (left > right) {

				return left > alpha && alpha > right;
			} else {
				return left > alpha || alpha > right;
			}
		}

		/**
		 * @param o an obstacle to compare with.
		 *
		 * @return -1 if this obstacle is completely to the left of
		 * <CODE>o</CODE>, 1 if it is completely to the right, and 0
		 * otherwise.
		 */
		protected int compare(Obstacle o) {
			if (obscures(o.left) || obscures(o.right) || o.obscures(left)
					|| o.obscures(right)) {
				return 0;
			} else {
				return (angle(left, o.right) < angle(right, o.left)) ? 1 : -1;
			}
		}

		/**
		 * Merges this obstacle with the given obstacle.
		 *
		 * @param o obstacle to merge with.
		 */
		protected void merge(Obstacle o) {
			if (o.obscures(left)) {
				left = o.left;
			}
			if (o.obscures(right)) {
				right = o.right;
			}
		}

		/**
		 * @return a string representation of this object.
		 */
		public String toString() {
			return ("[" + rad2deg(left) + "," + rad2deg(right) + "]");
		}
	}

	class ObstacleList extends Object {
		private Vector obstacles;

		protected ObstacleList() {
			obstacles = new Vector();

		}

		protected int size() {
			return obstacles.size();
		}

		protected Obstacle get(int i) {
			return (Obstacle) obstacles.elementAt(i);
		}

		protected Obstacle getBoundaries() {
			return new Obstacle(get(0).getLeft(), get(size() - 1).getRight());
		}

		protected void add(Obstacle o) {
			if (obstacles.isEmpty()) {
				obstacles.addElement(o);
			} else {
				for (int i = obstacles.size() - 1; i >= 0; i--) {
					Obstacle tmp = (Obstacle) obstacles.elementAt(i);

					int c = o.compare(tmp);
					if (c < 0) {
						obstacles.insertElementAt(tmp, i + 1);
						if (i == 0) {
							obstacles.setElementAt(o, 0);
						}
					} else if (c > 0) {
						obstacles.insertElementAt(o, i + 1);
						break;
					} else {
						tmp.merge(o);
						if (i > 0) {
							obstacles.removeElement(tmp);
							o = tmp;
						}
					}
				}
			}
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append('{');
			if (size() > 0) {
				sb.append(get(0));
				for (int i = 1; i < size(); i++) {
					sb.append(' ').append(get(i));
				}
			}
			sb.append('}');
			return sb.toString();
		}
	}

}