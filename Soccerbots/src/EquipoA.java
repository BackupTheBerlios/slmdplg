import EDU.gatech.cc.is.abstractrobot.*;
import EDU.gatech.cc.is.util.*;

public class EquipoA extends ControlSystemSS{
	private static final double UMBRAL_TIENE_PELOTA = 0.1;
	private static final double UMBRAL_ESTA_EN_PUNTO = 0.03;
	private static final double UMBRAL_DISPARO = 0.5;
	private static final double ANGULO_DISPARO = 0.1;
	private static final double RADIO_ACCION_GUARDIA = 0.3;
	private static final int MAXIMO_REPETICIONES = 25;
	private static final double SALIDA_PORTERO = 0.3;
	private static final double UMBRAL_CESION = 0.2;
	private static final double ALTO_PORTERIA = 0.55;

	// Vale 1.0 si esta en el izquierdo y -1.0 si esta en el derecho
	private double modificadorCampo;
	
	private Vec2 posicionOfensiva;
	private Vec2 posicionDefensiva;
	private Vec2 posicionInicial;
	private Vec2 posicionAnterior = null;
	private int repeticiones = 0;

	private long time;
	private static final double UMBRAL_BANDA = 0.8;
	
	
	private int turnos = 0;
	private int id;
	
	private Vec2[] ptosFormacionDefensa = {
			// Portero
			new Vec2(-1.2, 0.0),
			// Defensa Izquierdo
			new Vec2(-0.9, 0.25),
			// Defensa Derecho
			new Vec2(-0.9, -0.25),
			// Delantero Izquierdo
			new Vec2(-0.5, 0.5),
			// Delantero Derecho
			new Vec2(-0.5, -0.5)
	};
	
	private Vec2[] ptosFormacion = {
			// Portero
			new Vec2(-1.2, 0.0),
			// Defensa Izquierdo
			new Vec2(-0.65, 0.25),
			// Defensa Derecho
			new Vec2(-0.65, -0.25),
			// Delantero Izquierdo
			new Vec2(0.15, 0.5),
			// Delantero Derecho
			new Vec2(0.15, -0.5)
	};
	
	private Vec2[] ptosFormacionAtaque = {
			// Portero
			new Vec2(-1.2, 0.0),
			// Defensa Izquierdo
			new Vec2(-0.65, 0.0),
			// Defensa Derecho
			new Vec2(-0.15, -0.0),
			// Delantero Izquierdo
			new Vec2(0.8, 0.4),
			// Delantero Derecho
			new Vec2(0.8, -0.4)
	};
	
	public void configure() {
		id = abstract_robot.getPlayerNumber(time)%5;
		calculaPosicionOfensiva();
		calculaPosicionDefensiva();
		posicionInicial = (Vec2)abstract_robot.getPosition(time).clone();
		
		// Campo izquierdo
		if(posicionInicial.x < 0){
			modificadorCampo = 1.0;
		}
		// Campo derecho
		else{
			modificadorCampo = -1.0;
		}
		
		for(int i=0; i<ptosFormacion.length; i++) {
			ptosFormacion[i].setx(ptosFormacion[i].x*modificadorCampo);
			ptosFormacionAtaque[i].setx(ptosFormacionAtaque[i].x*modificadorCampo);
			ptosFormacionDefensa[i].setx(ptosFormacionDefensa[i].x*modificadorCampo);
		}
		
	}

	private int takeStepNormal() {
		boolean mia = tengoPelota();
		boolean mascercano = mia || yoSoyMasCercanoPelota();
		boolean nuestra = mia || tienePelotaAmigo();
		boolean suya = tienePelotaEnemigo();
		
		if (mia) {
			abstract_robot.setDisplayString("MIA " + nuestra + " " + suya);
			Vec2 portOponente = tRelativaToAbsoluta(abstract_robot
					.getOpponentsGoal(time));
			Vec2 portContrario = damePosicionPorteroEnemigo();
			
			if(abstract_robot.getPlayerNumber(time) == 1){
				disparar(ptosFormacionAtaque[3].x, ptosFormacionAtaque[3].y);
			}
			else if(abstract_robot.getPlayerNumber(time) == 2){
				disparar(ptosFormacionAtaque[4].x, ptosFormacionAtaque[4].y);
			}
			else if(abstract_robot.getOpponentsGoal(time).r > UMBRAL_BANDA){
				llevarPelota_a(
						abstract_robot.getPosition(time).x + modificadorCampo*0.2, 
						tRelativaToAbsoluta(abstract_robot.getOpponentsGoal(time)).y);
			} 
			else{
				// disparar(portOponente.x, portOponente.y);
				// Disparas al palo de abajo
				if(portContrario != null && portContrario.y >= 0){
					disparar(portOponente.x, portOponente.y-(ALTO_PORTERIA/2)+0.1);
				}
				// Disparas al palo de arriba 
				else{
					disparar(portOponente.x, portOponente.y+(ALTO_PORTERIA/2)-0.1);
				}
			}
		} else if (mascercano) {
			if(suya && !nuestra){
				Vec2 enemPos = damePosicionEnemigoMasCercanoBalon();
				Vec2 pelota = tRelativaToAbsoluta(abstract_robot.getBall(time));
				Vec2 v = vector2ptos(enemPos, pelota);
				
				v = new Vec2(-v.y, v.x);
				v.setr(0.1);
				
				Vec2 ira = tRelativaToAbsoluta(abstract_robot.getBall(time));
				ira.add(v);
				
				llevarPelota_a(ira.x, ira.y);
			}
			else{
				abstract_robot.setDisplayString("CERCANO" + nuestra + " " + suya);
				//System.out.println(id + " el mas cercano en turno: " + turnos);
				Vec2 p = tRelativaToAbsoluta(abstract_robot.getBall(time));
				ir_a(p.x, p.y);
			}
			
		
		} else{
			Vec2 posicion;
			if ((nuestra && !suya) || 
					(modificadorCampo > 0 && tRelativaToAbsoluta(abstract_robot.getBall(time)).x >= 0.7)||
					(modificadorCampo < 0 && tRelativaToAbsoluta(abstract_robot.getBall(time)).x <= -0.7)
			) {
				abstract_robot.setDisplayString("OFENSIVA" + nuestra + " "+ suya);
				posicion = ptosFormacionAtaque[abstract_robot.getPlayerNumber(time)%5];
			} else if ((suya && !nuestra) || 
					(modificadorCampo < 0 && tRelativaToAbsoluta(abstract_robot.getBall(time)).x >= 0.7)||
					(modificadorCampo > 0 && tRelativaToAbsoluta(abstract_robot.getBall(time)).x <= -0.7)
					
			) {
				abstract_robot.setDisplayString("DEFENSIVA" + nuestra + " "+ suya);
				posicion = ptosFormacionDefensa[abstract_robot.getPlayerNumber(time)%5];
			} else{
				abstract_robot.setDisplayString("FORMACION" + nuestra + " "+ suya);
				posicion = ptosFormacion[abstract_robot.getPlayerNumber(time)%5];
			}
			guardar(posicion.x, posicion.y);
		}
		
		// Si tengo la pelota o puedo tenerla, si estoy orientado hacia el portero
		// y lo suficientemente cerca, intento hacerle cesion.
		if( (mia || mascercano) && 
				abstract_robot.getOurGoal(time).r < UMBRAL_CESION &&
				abstract_robot.getSteerHeading(time) >= (3*Vec2.PI)/4&&
				abstract_robot.getSteerHeading(time) <= (5*Vec2.PI)/4)
		{
//			Vec2 portero = damePosicionPorteroAmigo();
//			disparar(portero.x+(modificadorCampo*0.2), portero.y);
//			System.out.println("CESION");
		}
		if ((mia || mascercano)
				&& posicionAnterior != null
				&& vector2ptos(abstract_robot.getPosition(time),
						posicionAnterior).r < 0.01) {
			repeticiones++;
		} else {
			repeticiones = 0;
		}

		if ((mia || mascercano) && repeticiones > MAXIMO_REPETICIONES) {
			// System.out.println("GIRO");
			double anterior = abstract_robot.getSteerHeading(time);
			abstract_robot.setSteerHeading(time, anterior + Vec2.PI / 2);
		}
		posicionAnterior = abstract_robot.getPosition(time);
		
		Vec2 pelota = abstract_robot.getBall(time);
		//pelota.setr(pelota.r+0.04);
		Vec2 pelo2 = tRelativaToAbsoluta((Vec2)pelota.clone());
		if(mia && pelo2.y <= -0.68){
			Vec2 op = abstract_robot.getBall(time);
			op = new Vec2(-op.y, op.x);
			abstract_robot.setSteerHeading(time, op.t);
			if(abstract_robot.canKick(time)){
				abstract_robot.kick(time);
			}
		}
		else if(mia && pelo2.y >= 0.68){
			Vec2 op = abstract_robot.getBall(time);
			op = new Vec2(op.y, -op.x);
			abstract_robot.setSteerHeading(time, op.t);
			if(abstract_robot.canKick(time)){
				abstract_robot.kick(time);
			}
			
		}
			
//		try {
//			Thread.sleep(10);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		return CSSTAT_OK;
	}
	
	private Vec2 damePosicionEnemigoMasCercanoBalon() {
		Vec2 balon = tRelativaToAbsoluta(abstract_robot.getBall(time));
		return damePosicionEnemigoMasCercanoPosicion(balon);
	}
	
	private Vec2 damePosicionEnemigoMasCercanoPosicion(Vec2 p) {
		Vec2 pc = (Vec2)p.clone();
		Vec2[] posEnemigos = abstract_robot.getOpponents(time);
		
		Vec2 pos = null;
		double dist = 1000;
		
		for(int i=0; i<posEnemigos.length; i++){
			Vec2 tmp = posEnemigos[i];
			tmp.setr(tmp.r+SocSmall.RADIUS);
			Vec2 pEActual = tRelativaToAbsoluta(tmp);
			Vec2 f = vector2ptos(pc, pEActual);
			if(f.r < dist){
				pos = pEActual;
				dist = f.r; 
			}
		}
		return pos;
	}

	private void guardar(double x, double y) {
		Vec2 P = new Vec2(x,y);
		Vec2 B = tRelativaToAbsoluta((Vec2)abstract_robot.getBall(time).clone());
		Vec2 radio = vector2ptos(P,B);
		
		radio.setr((RADIO_ACCION_GUARDIA<(radio.r-0.2))?RADIO_ACCION_GUARDIA:radio.r-0.2);
		radio.add(P);
		ir_a(radio.x, radio.y);
	}

	public int takeStep() {
		//Res.Resultado.Guardar(this, abstract_robot);
		time = abstract_robot.getTime();
		turnos ++;

		if(turnos > 1){
			if(id==0){
				return takeStepPortero();
			}
			else{
				return takeStepNormal();
			}
		}
		return CSSTAT_OK;
	}
	
	private int takeStepPortero() {
		abstract_robot.setDisplayString("Portero");
		Vec2 porteria = tRelativaToAbsoluta(abstract_robot.getOurGoal(time));
		porteria.setx(porteria.x - (modificadorCampo*0.3));
		Vec2 tmp = vector2ptos(
			porteria,
			tRelativaToAbsoluta(abstract_robot.getBall(time))
		);
		tmp.normalize(SALIDA_PORTERO);
		
		Vec2 to = (Vec2)porteria.clone();
		to.add(tmp);
		
		double posx = tRelativaToAbsoluta(abstract_robot.getOurGoal(time)).x + (modificadorCampo*SocSmall.RADIUS);//porteria.x+(modificadorCampo*SocSmall.RADIUS);
		double posy = tRelativaToAbsoluta(abstract_robot.getBall(time)).y;
		
		if(posy >= ALTO_PORTERIA/2) posy = ALTO_PORTERIA/2;
		if(posy <= -ALTO_PORTERIA/2) posy = -ALTO_PORTERIA/2;
		
		if(ir_a(posx, posy)){
			Vec2 p = abstract_robot.getPosition(time);
			Vec2 amigoMasCercano = calcularAmigoMasCercano();
			if(amigoMasCercano != null){
				amigoMasCercano = tAbsolutaToRelativa(amigoMasCercano);
				abstract_robot.setSteerHeading(time, (p.y/(Math.abs(p.y)))*modificadorCampo*amigoMasCercano.t);
			}
		}
		Vec2 choque = choqueConEnemigo(porteria.x, to.y);
		if(choque!=null && abstract_robot.getOurGoal(time).r < SALIDA_PORTERO){
			abstract_robot.setSteerHeading(time, choque.t);
		}
		
		return CSSTAT_OK;
	}


	private Vec2 calcularAmigoMasCercano() {
		Vec2 amigo = null;
		double dist = 1000;
		Vec2[] posAmigos = abstract_robot.getTeammates(time);
		
		for(int i=0; i<posAmigos.length; i++){
			if(posAmigos[i].r <= dist){
				amigo = posAmigos[i];
				dist = posAmigos[i].r;
			}
		}
		if(amigo == null)return null;
		else return tRelativaToAbsoluta((Vec2)amigo.clone());
	}

	private boolean tengoPelota() {
		return distancia(
				abstract_robot.getPosition(time), 
				tRelativaToAbsoluta(abstract_robot.getBall(time))
			) < UMBRAL_TIENE_PELOTA;
	}


	private boolean yoSoyMasCercanoPelota() {
		// Supongo que son relativos, luego mi posicion relativa es 0,0
		Vec2 miPosicion = abstract_robot.getPosition(time);
		Vec2[] posAmigos = abstract_robot.getTeammates(time);
		Vec2 pelota = abstract_robot.getBall(time);
		
		// Primero busco cual de todos los amigos es el mas cercano con mayor angulo
		double dist_mejor = 100;
		double angulo_mejor = -100;
		
		for(int i=0; i<posAmigos.length; i++){
			Vec2 pno = (Vec2)posAmigos[i].clone();
			pno.setr(pno.r + SocSmall.RADIUS);
			Vec2 pactual = tRelativaToAbsoluta(pno);

			Vec2 pball = tRelativaToAbsoluta(abstract_robot.getBall(time));
			Vec2 vcalculo = vector2ptos(pball, pactual);
			
			Vec2 cooPortero = damePosicionPorteroAmigo();
			if(	(Math.abs(pactual.x - cooPortero.x) > 0.01 || Math.abs(pactual.y - cooPortero.y) > 0.01) 
					&&
				(dist_mejor > vcalculo.r || 
				(Math.abs(dist_mejor-vcalculo.r) <= 0.01 && angulo_mejor < vcalculo.t))){
				
				angulo_mejor = vcalculo.t;
				dist_mejor = vcalculo.r;
			}
		}
		
		// Ahora comprobamos que tal lo hago yo
		double dist_mia = pelota.r;
		double ang_mio = vector2ptos(tRelativaToAbsoluta(pelota), miPosicion).t;
		
		return dist_mejor > dist_mia || (Math.abs(dist_mejor-dist_mia) <= 0.01 && angulo_mejor < ang_mio);
	}

	private boolean tienePelotaEnemigo() {
		Vec2[] posEnemigos = abstract_robot.getOpponents(time);
		Vec2 pelota = abstract_robot.getBall(time);
		int i=0;
		while(	i<posEnemigos.length && 
				distancia(posEnemigos[i],pelota) >= UMBRAL_TIENE_PELOTA){
			i++;
		}
		return i!=posEnemigos.length;
	}

	private boolean tienePelotaAmigo() {
		Vec2[] posAmigos = abstract_robot.getTeammates(time);
		Vec2 pelota = abstract_robot.getBall(time);
		int i=0;
		while(	i<posAmigos.length && 
				distancia(posAmigos[i],pelota) >= UMBRAL_TIENE_PELOTA){
			i++;
		}
		return i!=posAmigos.length;
	}
	
	private void calculaPosicionDefensiva() {
		posicionDefensiva = (Vec2)abstract_robot.getPosition(time).clone();
		posicionDefensiva.setx(posicionDefensiva.x + modificadorCampo*(-0.5));
	}

	private void calculaPosicionOfensiva() {
		posicionOfensiva = (Vec2)abstract_robot.getPosition(time).clone();
		posicionOfensiva.setx(posicionOfensiva.x + modificadorCampo*0.9);
	}
	
	private Vec2 tRelativaToAbsoluta(Vec2 relativa){
		Vec2 jugador = (Vec2)abstract_robot.getPosition(time).clone();
		Vec2 absoluta = (Vec2)relativa.clone();
		absoluta.add(jugador);
		return absoluta;
	}
	
	private Vec2 tAbsolutaToRelativa(Vec2 absoluta){
		Vec2 jugador = (Vec2)abstract_robot.getPosition(time).clone();
		Vec2 relativa = (Vec2)absoluta.clone();
		relativa.sub(jugador);
		return relativa;
	}
	
	private double distancia(Vec2 v1, Vec2 v2){
		Vec2 tmp = (Vec2)v1.clone();
		tmp.sub(v2);
		return tmp.r;
	}
	
	private Vec2 vector2ptos(Vec2 v1, Vec2 v2){
		Vec2 tmp = (Vec2)v2.clone();
		tmp.sub(v1);
		return tmp;
	}
	
	private boolean ir_a(double x, double y){
		Vec2 pos = abstract_robot.getPosition(time);
		Vec2 ir_a = new Vec2(x,y);
		ir_a.sub(pos);
		abstract_robot.setSteerHeading(time, ir_a.t);
		
		Vec2 choque = choqueConEnemigo(x, y);
		if(choque!=null){
			abstract_robot.setSteerHeading(time, choque.t);
		}
		
		if(ir_a.r >  UMBRAL_ESTA_EN_PUNTO){
			abstract_robot.setSpeed(time, 1.0);
			return false;
		}
		else{
			abstract_robot.setSpeed(time, 0.0);
			return true;
		}
	}
	
	private boolean llevarPelota_a(double x, double y){
		Vec2 res = new Vec2(x,y);
		Vec2 p = abstract_robot.getPosition(time);
		res.sub(p);
		Vec2 dir = abstract_robot.getBall(time);
		res.sub(dir);
		res.normalize(-0.05);
		dir.add(res);
		
		if(Math.abs(res.t - abstract_robot.getBall(time).t) < (Math.PI/4) ){
			Vec2 perp = new Vec2(-dir.y, dir.x);
			perp.normalize(0.08);
			dir.add(perp);
		}
		
		abstract_robot.setSteerHeading(time, dir.t);
		
		Vec2 pos = (Vec2)abstract_robot.getPosition(time).clone();
		pos.sub(abstract_robot.getBall(time));
		Vec2 a = new Vec2(x,y);
		a.sub(pos);
		
		if(a.r >  UMBRAL_ESTA_EN_PUNTO){
			abstract_robot.setSpeed(time, 1.0);
			return false;
		}
		else{
			abstract_robot.setSpeed(time, 0.0);
			return true;
		}
	}
	
	private void disparar(double x, double y){
		Vec2 mialpunto = tAbsolutaToRelativa(new Vec2(x,y));
		Vec2 dir = vector2ptos(
				tRelativaToAbsoluta(abstract_robot.getBall(time)),
				new Vec2(x,y));
		
		if(anguloEntreVectores(dir, mialpunto) <= ANGULO_DISPARO
				&& dir.r <= UMBRAL_DISPARO
				&& abstract_robot.canKick(time)){
			abstract_robot.kick(time);
		}
		else{
			llevarPelota_a(x,y);
		}
	}
	
	private Vec2 choqueConEnemigo(double x, double y){
		Vec2[] posEnemigos = abstract_robot.getOpponents(time);
		Vec2[] posAmigos = abstract_robot.getTeammates(time);
		
		Vec2 devuelve = null;
		Vec2 enemigoActual = null;
		
		for(int i=0; i<posEnemigos.length+posAmigos.length; i++){
			Vec2 P;
			if(i < posEnemigos.length)
				P = posEnemigos[i];
			else
				P = posAmigos[i-posEnemigos.length];
			
			P.setr(P.r + SocSmall.RADIUS*1.1);
			
			Vec2 Q = new Vec2(x,y);
			Q = tAbsolutaToRelativa(Q);
			
			if(P.r < Q.r && (enemigoActual == null || enemigoActual.r > P.r)){
				Vec2 ptoI = new Vec2(Q.y, -Q.x);
				ptoI.setr(SocSmall.RADIUS*1.1);
				Vec2 IA = vector2ptos(ptoI, Q);
				
				Vec2 ptoS = new Vec2(-Q.y, Q.x);
				ptoS.setr(SocSmall.RADIUS*1.1);
				Vec2 SA = vector2ptos(ptoS, Q);
				
				Vec2 ptoB = new Vec2(-Q.y, Q.x);
				ptoB.setr(SocSmall.RADIUS*1.1);
				ptoB.add(P);
				
				Vec2 ptoC = new Vec2(Q.y, -Q.x);
				ptoC.setr(SocSmall.RADIUS*1.1);
				ptoC.add(P);
				
				Vec2 IB = vector2ptos(ptoI, ptoB);
				Vec2 IC = vector2ptos(ptoI, ptoC);
				
				Vec2 SB = vector2ptos(ptoS, ptoB);
				Vec2 SC = vector2ptos(ptoS, ptoC);
				
				boolean inferior = dentro(IA, IB, IC);
				boolean superior = dentro(SA, SB, SC);
				if(P.r <= SocSmall.RADIUS*3 && ( inferior || superior) ){
					devuelve = new Vec2(-P.y, P.x);
					devuelve.setr(SocSmall.RADIUS*3);
					if(!vectorBueno(devuelve)){
						devuelve = new Vec2(P.y,-P.x);
					}
					enemigoActual = P;
				}
			}
		}
		
//		if(devuelve != null){
//			Vec2 imprime = tRelativaToAbsoluta((Vec2)enemigoActual.clone());
//			System.out.println(id+":"+" Danger!!("+imprime.x+","+imprime.y+")");
//		}
		return devuelve;
	}
	
	private boolean vectorBueno(Vec2 dir) {
		if(abstract_robot.getPosition(time).x <0.0 && dir.x <0){
			return false;
		}
		else if(abstract_robot.getPosition(time).x >0 && dir.x >0){
			return false;
		}
		else if(abstract_robot.getPosition(time).y >0 && dir.y >0){
			return false;
		}
		else if(abstract_robot.getPosition(time).y <0 && dir.y <0){
			return false;
		}
		else{
			return true;
		}
	}
	
	private Vec2 damePosicionPorteroAmigo(){
		Vec2 posicionPorteria = tRelativaToAbsoluta(abstract_robot.getOurGoal(time));
		Vec2[] posAmigos = abstract_robot.getTeammates(time);
		
		Vec2 pos = null;
		double dist = 1000;
		
		for(int i=0; i<posAmigos.length; i++){
			Vec2 tmp = (Vec2)posAmigos[i].clone();
			tmp.setr(tmp.r + SocSmall.RADIUS);
			Vec2 actual = tRelativaToAbsoluta(tmp);
			if(vector2ptos(actual, posicionPorteria).r < dist){
				pos = actual;
			}
		}
		return pos;
	}
	
	private Vec2 damePosicionPorteroEnemigo(){
		Vec2 posicionPorteria = tRelativaToAbsoluta(abstract_robot.getOpponentsGoal(time));
		Vec2[] posAmigos = abstract_robot.getOpponents(time);
		
		Vec2 pos = null;
		double dist = 1000;
		
		for(int i=0; i<posAmigos.length; i++){
			Vec2 tmp = (Vec2)posAmigos[i].clone();
			tmp.setr(tmp.r + SocSmall.RADIUS);
			Vec2 actual = tRelativaToAbsoluta(tmp);
			if(vector2ptos(actual, posicionPorteria).r < dist){
				pos = actual;
			}
		}
		return pos;
	}
	
	private double anguloEntreVectores(Vec2 a, Vec2 b){
		Vec2 a1 = (Vec2)a.clone();
		Vec2 b1 = (Vec2)b.clone();
		
		double escalar = a1.x*b1.x + a1.y*b1.y;
		double prodModulos = a1.r * b1.r;
		
		return Math.acos(escalar/prodModulos);
	}
	
	// Comprueba si el vector A esta dentro del angulo 
	// formado por B y C
	private boolean dentro(Vec2 A, Vec2 B, Vec2 C){
		double ang1 = anguloEntreVectores(B,C);
		double ang2 = anguloEntreVectores(B,A);
		double ang3 = anguloEntreVectores(A,C);
		
		return Math.abs(ang1 - (ang2+ang3)) <= 0.01;
	}
}
