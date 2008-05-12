import	EDU.gatech.cc.is.util.Vec2;
import	EDU.gatech.cc.is.abstractrobot.*;

/**
 * Primer intento de hacer un jugador
 * <P>
 * @author Nosotros
 */


public class Minnesota_Caballo_Rey extends ControlSystemSS
	{
	
	private static boolean hayPortero=false;
	private static boolean hayDelantero=false;
	private static boolean hayIzquierda=false;
	private static boolean hayDerecha=false;
	private static boolean hayLibre=false;
	
	private int ciclosChoque=0;
	private Vec2 ultimaPos=new Vec2(0,0);
	private boolean enChoque=false;
	
		
	/**
	Configure the Avoid control system.  This method is
	called once at initialization time.  You can use it
	to do whatever you like.
	*/
	public void Configure(){
	}

	/**
	Called every timestep to allow the control system to
	run.
	*/
	public int TakeStep()
		{
		//Res.Resultado.Guardar(this,abstract_robot);
		Vec2	result,ball,porteriaAtaque,porteriaDefensa,aux;
		
		long	curr_time = abstract_robot.getTime();
		
		String texto="";
	
		// get vector to the ball
		ball = abstract_robot.getBall(curr_time);
		porteriaDefensa= abstract_robot.getOurGoal(curr_time);
		porteriaAtaque= abstract_robot.getOpponentsGoal(curr_time);
		
		if (!enChoque){		
			if (abstract_robot.getPosition(curr_time).x==ultimaPos.x && abstract_robot.getPosition(curr_time).y==ultimaPos.y){
				ciclosChoque++;
				//System.out.println(abstract_robot.getPlayerNumber(curr_time)+" se ha chocado");
			}
			else{
				ciclosChoque=0;
				enChoque=false;
			}
			if (ciclosChoque==15){
				enChoque=true;
				ciclosChoque=0;
				abstract_robot.setDisplayString("chocandose");
			}
		}else{
			if (ciclosChoque==15){
				ciclosChoque=0;
				enChoque=false;
			}
			if (soyMasCercanoPorteriaDefensa(curr_time)){
				aux=posContrarioMasCercano(curr_time);
				aux.sub(ball);
				aux=new Vec2(-aux.x,-aux.y);
			}
			else
				aux=new Vec2(-ball.x,-ball.y);
			abstract_robot.setSteerHeading(curr_time,aux.t);
			abstract_robot.setSpeed(curr_time, 1);
			abstract_robot.setDisplayString("Alejandose");
			ciclosChoque++;
			return (CSSTAT_OK);
		}
		
		ultimaPos=abstract_robot.getPosition(curr_time);


		if (soyMasCercanoPorteriaDefensa(curr_time)){
			comportamientoPortero();
			return (CSSTAT_OK);
		}
		
		
		if (soyMasCercanoPorteriaAtaque(curr_time)){
			comportamientoDelantero();
			return (CSSTAT_OK);
		}
		
		if (soyMasIzquierdo(curr_time)){
			comportamientoIzquierda();
			return (CSSTAT_OK);
		}

		if (soyMasDerecho(curr_time)){
			comportamientoDerecha();
			return (CSSTAT_OK);
		}

		int c=comportamientoDudoso();
		
		//System.out.println("comportamiento dudoso para el jugador "+abstract_robot.getPlayerNumber(curr_time)+" es: "+c);
		
		switch (c){
			case 0:
				comportamientoPortero();			
				return(CSSTAT_OK);
			case 1:
				comportamientoDelantero();
				return(CSSTAT_OK);
			case 2:
				comportamientoIzquierda();
				return(CSSTAT_OK);
			case 3:
				comportamientoDerecha();			
				return(CSSTAT_OK);
			default:
				comportamientoLibre();
				return(CSSTAT_OK);
		}
		/*comportamientoLibre();
		
		return(CSSTAT_OK);*/
		}

	
	private boolean soyMasCercanoPorteriaDefensa(long ts){
		Vec2[] equipo=abstract_robot.getTeammates(ts);
		Vec2 porteria=abstract_robot.getOurGoal(ts);
		double aux=Math.abs(porteria.r);
		int i=0;
		boolean resultado=true;
		
		while (i<equipo.length && resultado){
			equipo[i].sub(porteria);
			if (aux>Math.abs(equipo[i].r))
				resultado=false;
			i++;
		}
		return resultado;		
	}

	private boolean soyMasCercanoPorteriaAtaque(long ts){
		if (hayDelantero)
			return false;
		Vec2[] equipo=abstract_robot.getTeammates(ts);
		Vec2 porteria=abstract_robot.getOpponentsGoal(ts);
		double aux=Math.abs(porteria.r);
		int i=0;
		boolean resultado=true;
		
		while (i<equipo.length && resultado){
			equipo[i].sub(porteria);
			if (aux>Math.abs(equipo[i].r)){
				//if (Math.abs(aux-Math.abs(equipo[i].r))>=0.1){					
					resultado=false;
				//}
			}
			i++;
		}
		return resultado;				
	}
	
	private boolean soyMasCercanoBalon(long ts){
		Vec2[] equipo=abstract_robot.getTeammates(ts);
		Vec2 balon=abstract_robot.getOpponentsGoal(ts);
		double aux=Math.abs(balon.r);
		int i=0;
		boolean resultado=true;
		
		while (i<equipo.length && resultado){
			equipo[i].sub(balon);
			if (aux>Math.abs(equipo[i].r)){
				if (Math.abs(aux-Math.abs(equipo[i].r))>=0.1){					
					resultado=false;
				}
			}
			i++;
		}
		
		equipo=abstract_robot.getOpponents(ts);

		i=0;
		while (i<equipo.length && resultado){
			equipo[i].sub(balon);
			if (aux>Math.abs(equipo[i].r)){
				if (Math.abs(aux-Math.abs(equipo[i].r))>=0.1){					
					resultado=false;
				}
			}
			i++;
		}
		
		return resultado;				
	}
		
	private boolean soyMasIzquierdo(long ts){
		if (hayIzquierda)
			return false;		
		Vec2[] equipo=abstract_robot.getTeammates(ts);
		double aux=abstract_robot.getPosition(ts).y;
		int i=0;
		boolean resultado=true;
		
		while (i<equipo.length && resultado){
			if (equipo[i].y>0){
				resultado=false;
			}
			i++;
		}
		return resultado;				
	}
	
	private boolean soyMasDerecho(long ts){
		if (hayDerecha)
			return false;
		Vec2[] equipo=abstract_robot.getTeammates(ts);
		double aux=abstract_robot.getPosition(ts).y;
		int i=0;
		boolean resultado=true;
		
		while (i<equipo.length && resultado){
			if (equipo[i].y<0){
				resultado=false;
			}
			i++;
		}
		return resultado;				
	}	
	
	private boolean enAreaAtaque(long ts){
		Vec2 porteria=abstract_robot.getOpponentsGoal(ts);
		
		if (Math.abs(porteria.r)<0.45)
			return true;
		else
			return false;
	}

	private boolean enAreaDefensa(long ts){
		Vec2 porteria=abstract_robot.getOurGoal(ts);
		
		if (Math.abs(porteria.r)<0.25)
			return true;
		else
			return false;
	}
	
	private Vec2 posCompañeroMasCercano(long ts){
		Vec2[] equipo=abstract_robot.getTeammates(ts);
		double min=10000000;
		int indiceMin=-1;
				
		for (int i=0;i<equipo.length;i++){
			if (Math.abs(equipo[i].r)<min){
				min=equipo[i].r;
				indiceMin=i;
			}
		}
		return equipo[indiceMin];
	}

	private Vec2 posContrarioMasCercano(long ts){
		Vec2[] equipo=abstract_robot.getOpponents(ts);
		double min=10000000;
		int indiceMin=-1;
				
		for (int i=0;i<equipo.length;i++){
			if (Math.abs(equipo[i].r)<min){
				min=equipo[i].r;
				indiceMin=i;
			}
		}
		return equipo[indiceMin];
	}

	private Vec2 posCompañeroMasAdelantado(long ts){
		Vec2[] equipo=abstract_robot.getTeammates(ts);
		Vec2 porteria=abstract_robot.getOpponentsGoal(ts);
		
		double min=10000000;
		int indiceMin=-1;
				
		for (int i=0;i<equipo.length;i++){
			equipo[i].sub(porteria);
			if (Math.abs(equipo[i].r)<min){
				min=equipo[i].r;
				indiceMin=i;
			}
		}
		return equipo[indiceMin];
	}

	private boolean tenemosPosesion(long ts){
		Vec2[] equipo=abstract_robot.getTeammates(ts);
		Vec2 ball=abstract_robot.getBall(ts);
		boolean resultado=false;
		int i=0;
		
		if (abstract_robot.canKick(ts))
			resultado=true;
		
		while (i<equipo.length && !resultado){
			equipo[i].sub(ball);
			if (equipo[i].r<abstract_robot.RADIUS)
				resultado=true;
			i++;
		}
		
		return resultado;				
	}
	
	private boolean tienenPosesion(long ts){
		Vec2[] equipo=abstract_robot.getOpponents(ts);
		Vec2 ball=abstract_robot.getBall(ts);
		boolean resultado=false;
		int i=0;
		
		while (i<equipo.length && !resultado){
			equipo[i].sub(ball);
			if (equipo[i].r<abstract_robot.RADIUS)
				resultado=true;
			i++;
		}
		
		return resultado;				
	}




	private boolean tapado(Vec2 destino,long ts){
		Vec2[] obstaculos=abstract_robot.getObstacles(ts);
		boolean resultado=true;
		
		for (int i = 0; i<obstaculos.length; i++){
			if (Math.abs(destino.t-obstaculos[i].t)<=0.3)
				resultado=false;
		}
		return resultado;
	}

	private void compruebaTodasPosiciones(){
		if (hayPortero && hayDelantero && hayDerecha && hayIzquierda && hayLibre){
			hayPortero=false;
			hayDelantero=false;
			hayIzquierda=false;
			hayDerecha=false;
			hayLibre=false;
		}
		
	}
	
	private Vec2 apuntarA(Vec2 destino,long ts){
		Vec2 ball=abstract_robot.getBall(ts);
		Vec2 aux=destino;
		double x,y;
		Vec2 resultado;
		
		aux.sub(ball);
		
		//desde aqui cambia
		aux=new Vec2(-aux.x,-aux.y);
		
		resultado=ball;
		resultado.add(aux);
		
		/*if (resultado.r<abstract_robot.RADIUS*2)
			resultado=ball;*/
		return resultado;

	}

	private boolean apuntando(Vec2 receptor,long ts){
		return 	(Math.abs(abstract_robot.getSteerHeading(ts)-receptor.t)/receptor.r<0.53);
	}
	
	private void comportamientoPortero(){
		Vec2	result,ball,porteriaAtaque,porteriaDefensa,aux;
		
		long	curr_time = abstract_robot.getTime();
		
		String texto="";
	
		// get vector to the ball
		ball = abstract_robot.getBall(curr_time);
		porteriaDefensa= abstract_robot.getOurGoal(curr_time);
		porteriaAtaque= abstract_robot.getOpponentsGoal(curr_time);
		
		hayPortero=true;
		//System.out.println("El jugador "+abstract_robot.getPlayerNumber(curr_time)+" es portero");
		compruebaTodasPosiciones();
		if (soyMasCercanoBalon(curr_time)){
			if (abstract_robot.canKick(curr_time) && abstract_robot.getSteerHeading(curr_time)*porteriaAtaque.x>0){
				abstract_robot.kick(curr_time);
				abstract_robot.setSpeed(curr_time, 1.0);
			}
			else{
				abstract_robot.setSteerHeading(curr_time, ball.t);
				if (abstract_robot.canKick(curr_time))
					abstract_robot.kick(curr_time);
				abstract_robot.setSpeed(curr_time, 1.0);
				texto="salida";
			}
			abstract_robot.setDisplayString("Portero: "+texto);
			return;
		}
		if (enAreaDefensa(curr_time)){
			if (abstract_robot.canKick(curr_time)){
				abstract_robot.setSteerHeading(curr_time, porteriaAtaque.t);
				abstract_robot.kick(curr_time);
				texto="despeja";
			}
			else{
				aux=new Vec2(porteriaDefensa.x,ball.y);
				abstract_robot.setSteerHeading(curr_time, aux.t);
				abstract_robot.setSpeed(curr_time, 1.0);
				texto="cubrir portería";
			}
		}
		else{
			if (Math.abs(porteriaDefensa.x)<0.05){
				abstract_robot.setSpeed(curr_time, 0);
			}
			else{
				abstract_robot.setSteerHeading(curr_time, porteriaDefensa.t);
				abstract_robot.setSpeed(curr_time, 1.0);
				texto="hacia la portería";
			}
		}
		abstract_robot.setDisplayString("Portero: "+texto);

	}
	
	
	private void comportamientoDelantero(){
		Vec2	result,ball,porteriaAtaque,porteriaDefensa,aux;
		
		long	curr_time = abstract_robot.getTime();
		
		String texto="";
	
		// get vector to the ball
		ball = abstract_robot.getBall(curr_time);
		porteriaDefensa= abstract_robot.getOurGoal(curr_time);
		porteriaAtaque= abstract_robot.getOpponentsGoal(curr_time);
		
		hayDelantero=true;
		//System.out.println("El jugador "+abstract_robot.getPlayerNumber(curr_time)+" es delantero");
		compruebaTodasPosiciones();
		if (tenemosPosesion(curr_time)){
			if (enAreaAtaque(curr_time)){
				if (abstract_robot.canKick(curr_time)){
					aux=new Vec2(porteriaAtaque.x,2*porteriaAtaque.y);
					abstract_robot.setSteerHeading(curr_time, apuntarA(aux,curr_time).t);
					if (apuntando(aux,curr_time)){
						abstract_robot.kick(curr_time);
						System.out.println("Delantero tira a puerta");
						texto="tira a puerta";
					}
					else{
						abstract_robot.setSpeed(curr_time, 0.4);
						texto="apunta";
					}
				}
				else{
					aux=new Vec2(0,-ball.y);
					abstract_robot.setSteerHeading(curr_time, aux.t);
					abstract_robot.setSpeed(curr_time, 0.4);
					texto="segundo palo";
				}
			}
			else{
				if (abstract_robot.canKick(curr_time)){
					abstract_robot.setSteerHeading(curr_time, apuntarA(porteriaAtaque,curr_time).t);
					abstract_robot.setSpeed(curr_time, 1);
					texto="llevar balón a portería";
				}
				else{
					abstract_robot.setSteerHeading(curr_time, porteriaAtaque.t);
					abstract_robot.setSpeed(curr_time, 1.0);
					texto="ir a portería";
				}
			}
		}
		else{
			if (!tienenPosesion(curr_time) && soyMasCercanoBalon(curr_time)){
					abstract_robot.setSteerHeading(curr_time, ball.t);
					abstract_robot.setSpeed(curr_time, 1.0);
					texto="ir al balon";
			}
			else{
				aux=abstract_robot.getPosition(curr_time);
				if ((porteriaDefensa.x<0 && aux.x>=0)||(porteriaDefensa.x>0 && aux.x<=0)){
					aux=new Vec2(porteriaDefensa.x,0);
					abstract_robot.setSteerHeading(curr_time, aux.t);
					abstract_robot.setSpeed(curr_time, 1.0);
					texto="retroceder";
				}
				else{
					aux=new Vec2(0,porteriaAtaque.y);
					abstract_robot.setSteerHeading(curr_time, aux.t);
					abstract_robot.setSpeed(curr_time, 1.0);
					texto="quedarse adelantado";
				}
			}
		}
		abstract_robot.setDisplayString("Delantero: "+texto);		
	}

	private void comportamientoIzquierda(){
		Vec2	result,ball,porteriaAtaque,porteriaDefensa,aux;
		
		long	curr_time = abstract_robot.getTime();
		
		String texto="";
	
		// get vector to the ball
		ball = abstract_robot.getBall(curr_time);
		porteriaDefensa= abstract_robot.getOurGoal(curr_time);
		porteriaAtaque= abstract_robot.getOpponentsGoal(curr_time);
		hayIzquierda=true;
		//System.out.println("El jugador "+abstract_robot.getPlayerNumber(curr_time)+" es izquierda");
		compruebaTodasPosiciones();			
		if (tenemosPosesion(curr_time)){
			if (abstract_robot.canKick(curr_time)){
				if (porteriaAtaque.r<0.4){
					aux=new Vec2(porteriaAtaque.x,2*porteriaAtaque.y);
					abstract_robot.setSteerHeading(curr_time, apuntarA(aux,curr_time).t);
					if (apuntando(aux,curr_time)){
						abstract_robot.kick(curr_time);
						System.out.println("Izquierda tira a puerta");
						texto="tira a puerta";
					}
					else{
						abstract_robot.setSpeed(curr_time, 0.4);
						texto="apunta";
					}
				}
				else{
					Vec2 pos=abstract_robot.getPosition(curr_time);
					if (pos.y>= 0.68 && pos.x*porteriaAtaque.x>0){
						abstract_robot.kick(curr_time);
					}
					else{
						if (!tapado(porteriaAtaque,curr_time)){
							abstract_robot.setSteerHeading(curr_time, apuntarA(porteriaAtaque,curr_time).t);
							abstract_robot.setSpeed(curr_time, 1);
						}
						else{
							Vec2 receptor;
							if (!tapado(posCompañeroMasAdelantado(curr_time),curr_time))
								receptor=posCompañeroMasAdelantado(curr_time);
							else
								receptor=posCompañeroMasCercano(curr_time);
		
							abstract_robot.setSteerHeading(curr_time, apuntarA(receptor,curr_time).t);
							if (apuntando(receptor,curr_time)){
								abstract_robot.kick(curr_time);
								System.out.println("Izquierda pasa");
								texto="pasa";
							}
							else{
								abstract_robot.setSpeed(curr_time, 0.4);
								texto="apunta";
							}
						}
					}
				}
			}
			else{					
				Vec2 pos=abstract_robot.getPosition(curr_time);
				if (pos.y>= 0.58){
					abstract_robot.setSteerHeading(curr_time, ball.t);
					abstract_robot.setSpeed(curr_time, 0.5);
				}
				else{
					aux=new Vec2(ball.x,1);
					abstract_robot.setSteerHeading(curr_time, aux.t);
					abstract_robot.setSpeed(curr_time, 1);
				}
			}
		}
		else{
			if (!tienenPosesion(curr_time) && soyMasCercanoBalon(curr_time)){
					abstract_robot.setSteerHeading(curr_time, ball.t);
					abstract_robot.setSpeed(curr_time, 1.0);
					texto="ir al balon";
			}
			else{
				/*Vec2 aux2=ball;
				aux=porteriaDefensa;
				aux2.sub(porteriaDefensa);
				aux2=new Vec2(aux2.x/4,aux2.y/4);			
				aux.add(aux2);*/
				aux=new Vec2(ball.x,ball.y/4);
				abstract_robot.setSteerHeading(curr_time, aux.t);
				abstract_robot.setSpeed(curr_time, 1);
			}
		}
		
		abstract_robot.setDisplayString("Izquierda: "+texto);	
	}
	
	private void comportamientoDerecha(){
		Vec2	result,ball,porteriaAtaque,porteriaDefensa,aux;
		
		long	curr_time = abstract_robot.getTime();
		
		String texto="";
	
		// get vector to the ball
		ball = abstract_robot.getBall(curr_time);
		porteriaDefensa= abstract_robot.getOurGoal(curr_time);
		porteriaAtaque= abstract_robot.getOpponentsGoal(curr_time);
		hayDerecha=true;
		//System.out.println("El jugador "+abstract_robot.getPlayerNumber(curr_time)+" es derecha");
		compruebaTodasPosiciones();			
		if (tenemosPosesion(curr_time)){
			if (abstract_robot.canKick(curr_time)){
				if (porteriaAtaque.r<0.4){
					aux=new Vec2(porteriaAtaque.x,2*porteriaAtaque.y);
					abstract_robot.setSteerHeading(curr_time, apuntarA(aux,curr_time).t);
					if (apuntando(aux,curr_time)){
						abstract_robot.kick(curr_time);
						texto="tira a puerta";
						System.out.println("Derecha tira a puerta");							
					}
					else{
						abstract_robot.setSpeed(curr_time, 0.4);
						texto="apunta";
					}
				}
				else{
					Vec2 pos=abstract_robot.getPosition(curr_time);
					if (pos.y<= -0.68 && pos.x*porteriaAtaque.x>0){
						abstract_robot.kick(curr_time);
					}
					else{
						if (!tapado(porteriaAtaque,curr_time)){
							abstract_robot.setSteerHeading(curr_time, apuntarA(porteriaAtaque,curr_time).t);
							abstract_robot.setSpeed(curr_time, 1);
						}
						else{
							Vec2 receptor;
							if (!tapado(posCompañeroMasAdelantado(curr_time),curr_time))
								receptor=posCompañeroMasAdelantado(curr_time);
							else
								receptor=posCompañeroMasCercano(curr_time);
		
							abstract_robot.setSteerHeading(curr_time, apuntarA(receptor,curr_time).t);
							if (apuntando(receptor,curr_time)){
								System.out.println("Derecha pasa");
								abstract_robot.kick(curr_time);
								texto="pasa";
							}
							else{
								abstract_robot.setSpeed(curr_time, 0.4);
								texto="apunta";
							}
						}
					}
				}
			}
			else{
				Vec2 pos=abstract_robot.getPosition(curr_time);
				if (pos.y<= -0.58){
					abstract_robot.setSteerHeading(curr_time, ball.t);
					abstract_robot.setSpeed(curr_time, 0.5);
				}
				else{
					aux=new Vec2(ball.x,-1);
					abstract_robot.setSteerHeading(curr_time, aux.t);
					abstract_robot.setSpeed(curr_time, 1);
				}
			}
		}
		else{
			if (!tienenPosesion(curr_time) && soyMasCercanoBalon(curr_time)){
					abstract_robot.setSteerHeading(curr_time, ball.t);
					abstract_robot.setSpeed(curr_time, 1.0);
					texto="ir al balon";
			}
			else{
				/*Vec2 aux2=ball;
				aux=porteriaDefensa;
				aux2.sub(porteriaDefensa);
				aux2=new Vec2(aux2.x/4,aux2.y/4);			
				aux.add(aux2);*/
				aux=new Vec2(ball.x,ball.y/4);
				abstract_robot.setSteerHeading(curr_time, aux.t);
				abstract_robot.setSpeed(curr_time, 1);
			}
		}
		
		abstract_robot.setDisplayString("Derecha: "+texto);

	}

	private void comportamientoLibre(){
		Vec2	result,ball,porteriaAtaque,porteriaDefensa,aux;
		
		long	curr_time = abstract_robot.getTime();
		
		String texto="";
	
		// get vector to the ball
		ball = abstract_robot.getBall(curr_time);
		porteriaDefensa= abstract_robot.getOurGoal(curr_time);
		porteriaAtaque= abstract_robot.getOpponentsGoal(curr_time);
		
		hayLibre=true;
		//System.out.println("El jugador "+abstract_robot.getPlayerNumber(curr_time)+" es libre");
		compruebaTodasPosiciones();

		if (tenemosPosesion(curr_time)){
			if (abstract_robot.canKick(curr_time)){
					if (porteriaAtaque.r<0.4){
						aux=new Vec2(porteriaAtaque.x,2*porteriaAtaque.y);
						abstract_robot.setSteerHeading(curr_time, apuntarA(aux,curr_time).t);
						if (apuntando(aux,curr_time)){
							abstract_robot.kick(curr_time);
							System.out.println("Libre tira a puerta");
							texto="tira a puerta";
						}
						else{
							abstract_robot.setSpeed(curr_time, 0.4);
							texto="apunta";
						}
					}
					else{
						if (!tapado(porteriaAtaque,curr_time)){
							abstract_robot.setSteerHeading(curr_time, apuntarA(porteriaAtaque,curr_time).t);
							abstract_robot.setSpeed(curr_time, 1);
						}
						else{
							Vec2 receptor;
							if (!tapado(posCompañeroMasAdelantado(curr_time),curr_time))
								receptor=posCompañeroMasAdelantado(curr_time);
							else
								receptor=posCompañeroMasCercano(curr_time);
		
							abstract_robot.setSteerHeading(curr_time, apuntarA(receptor,curr_time).t);
							if (apuntando(receptor,curr_time)){
								abstract_robot.kick(curr_time);
								System.out.println("Libre pasa");
								texto="pasa";
							}
							else{
								abstract_robot.setSpeed(curr_time, 0.4);
								texto="pasa";
							}
						}
					}				
			}
			else{				
				if ((porteriaDefensa.x<0 && abstract_robot.getPosition(curr_time).x<-0.3)||(porteriaDefensa.x>0 && abstract_robot.getPosition(curr_time).x>0.3)
					&& (porteriaDefensa.x<0 && ball.x>0)||(porteriaDefensa.x>0 && ball.x<0)){
					aux=new Vec2(ball.x,0);
					abstract_robot.setSteerHeading(curr_time, aux.t);
					abstract_robot.setSpeed(curr_time, 1);
				}
				else{
					abstract_robot.setSteerHeading(curr_time, porteriaDefensa.t);
					abstract_robot.setSpeed(curr_time, 0.2);
				}
			}
		}
		else{
			if (!tienenPosesion(curr_time) && soyMasCercanoBalon(curr_time)){
					abstract_robot.setSteerHeading(curr_time, ball.t);
					abstract_robot.setSpeed(curr_time, 1.0);
					texto="ir al balon";
			}
			else{
				if (porteriaDefensa.r<0.27){
					aux=new Vec2(porteriaAtaque.x,ball.y);
					abstract_robot.setSteerHeading(curr_time, aux.t);
					abstract_robot.setSpeed(curr_time, 1);
					texto="apartarse";			
				}
				else{
					abstract_robot.setSteerHeading(curr_time, ball.t);
					abstract_robot.setSpeed(curr_time, 1);
					texto="ir hacia balón";
				}
			}
		}
		
		abstract_robot.setDisplayString("Libre: "+texto);		
	}

	private int comportamientoDudoso(){
		int aux=0;
		
		if (!hayPortero)
			return 0;
		if (hayDelantero)
			aux++;
		if (hayIzquierda)
			aux=aux+2;
		if (hayDerecha)
			aux=aux+4;
		if (hayLibre)
			aux=aux+8;
		
		//System.out.println("El valor es: "+aux);
		switch (aux){
			case 4:
			case 6:
			case 8:			
			case 10:
			case 12:
			case 14:			
				return 1; //Del
			case 1:
			case 5:			
			case 9:
			case 13:			
			case 15:
				return 2; //Izq
			case 2:
			case 3:
			case 11:			
				return 3; //Der
			case 16:
				return 0;
			default:
				return 4; //Lib
		}
	}
	
}