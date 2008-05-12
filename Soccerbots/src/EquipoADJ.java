import java.util.Random;

import EDU.gatech.cc.is.abstractrobot.ControlSystemSS;
import EDU.gatech.cc.is.util.Vec2;

public class EquipoADJ extends ControlSystemSS{

	//Datos para el jugador J
	private long Jcurr_time;	
	private long Jid;			
	private int Jlado;
	private double Jrotacion;	
	private Vec2 Jposicion;
	private Vec2 Jbola;	
	private Vec2 JegoBola;
	private Vec2 JnuestraPorteria; 
	private Vec2 JsuPorteria;		
	private Vec2[] Jnosotros;
	private Vec2[] Jellos;		
	private Vec2 Jresultado;
	private boolean	Jchuta;	
	private final double Jradio = abstract_robot.RADIUS;

	//Datos para el jugador A
	private static int ASIDE;
	
	//Datos para los jugadores D
	private int DpasosSinMover;
	private int DpasosEscape;
	private Vec2 DantiguaPos;
	private Random Dr;
	
	public void Configure()
	{
		Jresultado=new Vec2(0,0);
		Jcurr_time = abstract_robot.getTime();
		if(abstract_robot.getOurGoal(Jcurr_time).x>0)
			Jlado=1;
		else
			Jlado=-1;
		
		DpasosSinMover=0;
		DpasosEscape = 0;
		DantiguaPos = new Vec2();
		Dr = new Random();
	}
	
	public int TakeStep()
	{
		//Res.Resultado.Guardar(this,abstract_robot);
		
		long	Acurr_time = abstract_robot.getTime();
		int mynum = abstract_robot.getPlayerNumber(Acurr_time);
		if(mynum==0)
			porteroA();
		else if(mynum==1)
			defensaJ();
		else
			jugadoresD(mynum);
		
		return(CSSTAT_OK);

	 }
	
	private void porteroA()
	{
		// the eventual movement command is placed here
		Vec2	Aresult = new Vec2(0,0);
		long	Acurr_time = abstract_robot.getTime();
		//get absolute position
		Vec2 AmyPosition = abstract_robot.getPosition(Acurr_time);
		
		Vec2 Aourgoal = abstract_robot.getOurGoal(Acurr_time);
		if( Aourgoal.x < 0)
			ASIDE = -1;
		else
			ASIDE = 1;
		if(ASIDE==-1 && Aourgoal.t<0)
			Aourgoal.sett(Aourgoal.t + 2*Math.PI);
		
		Vec2 Aball = abstract_robot.getBall(Acurr_time);
		if(ASIDE==-1 && Aball.t<0)
			Aball.sett(Aball.t + 2*Math.PI);
		
		Vec2 Atheirgoal = abstract_robot.getOpponentsGoal(Acurr_time);
		if(ASIDE==-1 && Atheirgoal.t<0)
			Atheirgoal.sett(Atheirgoal.t + 2*Math.PI);
		
		Vec2[] Ateammates = abstract_robot.getTeammates(Acurr_time);
		Vec2[] Aopponents = abstract_robot.getOpponents(Acurr_time);
		
		Vec2 Aclosestteammate = new Vec2(99999,0);
		for (int i=0; i< Ateammates.length; i++)
			{
			if (Ateammates[i].r < Aclosestteammate.r)
				Aclosestteammate = Ateammates[i];
			}
		if(ASIDE==-1 && Aclosestteammate.t<0)
			Aclosestteammate.sett(Aclosestteammate.t + 2*Math.PI);
		
		Vec2 Aclosestopponent = new Vec2(99999,0);
		for (int i=0; i< Aopponents.length; i++)
			{
			if (Aopponents[i].r < Aclosestopponent.r)
				Aclosestopponent = Aopponents[i];
			}
		if(ASIDE==-1 && Aclosestopponent.t<0)
			Aclosestopponent.sett(Aclosestopponent.t + 2*Math.PI);
		
			/*--- go to one of the places depending on player num ---*/
			int mynum = abstract_robot.getPlayerNumber(Acurr_time);

			/*--- Goalie ---*/
			if (mynum == 0)
			{
				if(Aball.r<abstract_robot.RADIUS*1.1)
				{
					Aresult.r=0;
					Aresult.t=Aball.t;
				}
				else
				{
				//Si estoy en la y de la pelota no me muevo
				if(Aball.y==0)
				{   //A no ser que esté fuera de portería, que voy a ella
					if(Aourgoal.r>abstract_robot.RADIUS*2)
					{
//						Si hay un oponente molestando lo rodeo
						if(Aclosestopponent.r <= abstract_robot.RADIUS*1.5 &&
						   Math.abs(Aourgoal.t-Aclosestopponent.t)<Math.PI/2)
						{
							if(Aball.y>0)
							{
								Aresult.sett(Aclosestopponent.t + ASIDE*Math.PI/2);
								Aresult.setr(1.0);
							}
							else
							{
								Aresult.sett(Aclosestopponent.t - ASIDE*Math.PI/2);
								Aresult.setr(1.0);
							}
						}//Si hay un compañero también lo evito
						else if(Aclosestteammate.r <= abstract_robot.RADIUS*1.5 &&
								   Math.abs(Aourgoal.t-Aclosestteammate.t)<Math.PI/2)
								{
									if(Aball.y>0)
									{
										Aresult.sett(Aclosestteammate.t + ASIDE*Math.PI/2);
										Aresult.setr(1.0);
									}
									else
									{
										Aresult.sett(Aclosestteammate.t - ASIDE*Math.PI/2);
										Aresult.setr(1.0);
									}
						}
						else//Tengo vía libre
						{
							Aresult.setx(Aourgoal.x);
							Aresult.sety(Aball.y);
							Aresult.setr(1.0);
						}
					}
					else //Estoy en la portería y en la y de de la pelota 
					{
						Aresult.setx(0.0);
						Aresult.sety(0.0);
						Aresult.setr(0.0);
					}
				}
				else //No estoy en la y de la pelota
				{
					int cuadranteRelativoPelota =dameCuadranteReal(Aball); 
					if((ASIDE==-1 && (cuadranteRelativoPelota==1 || cuadranteRelativoPelota==2))
					   ||(ASIDE==1 && (cuadranteRelativoPelota==0 || cuadranteRelativoPelota==3)))
					{//Si estoy más adelantado que la pelota
						//me muevo entre la portería y la pelota
						Vec2 aux7 = new Vec2(Aourgoal.x + Aball.x,Aourgoal.y + Aball.y);
						if(ASIDE==-1 && aux7.t<0)
							aux7.sett(aux7.t + Math.PI*2);
//						Si hay un oponente molestando lo rodeo
						if(Aclosestopponent.r <= abstract_robot.RADIUS*1.5 &&
						   Math.abs(aux7.t-Aclosestopponent.t)<Math.PI/2)
						{
							if(Aball.y>0)
							{
								Aresult.sett(Aclosestopponent.t + ASIDE*Math.PI/2);
								Aresult.setr(1.0);
							}
							else
							{
								Aresult.sett(Aclosestopponent.t - ASIDE*Math.PI/2);
								Aresult.setr(1.0);
							}
						}
						//Si hay un compañero en medio también lo rodeo
						else if(Aclosestteammate.r <= abstract_robot.RADIUS*1.5 &&
								   Math.abs(Aourgoal.t-Aclosestteammate.t)<Math.PI/2)
								{
									if(Aball.y>0)
									{
										Aresult.sett(Aclosestteammate.t + ASIDE*Math.PI/2);
										Aresult.setr(1.0);
									}
									else
									{
										Aresult.sett(Aclosestteammate.t - ASIDE*Math.PI/2);
										Aresult.setr(1.0);
									}
						}
						else//Tengo vía libre
						{
						Aresult.setx(Aourgoal.x + Aball.x);
						Aresult.sety(Aourgoal.y + Aball.y);
						Aresult.setr(0.5);
					}
					}
					//Estoy más retrasado que la bola pero fuera de portería, voy a ella
					else if(Math.abs(Aourgoal.y)>abstract_robot.RADIUS*4.25
							|| Math.abs(Aourgoal.r)>abstract_robot.RADIUS*4.25)
					{
						//Si hay un oponente molestando lo rodeo
						if(ASIDE==1 && Aclosestopponent.t>Math.PI*3/2)
							Aclosestopponent.sett(2*Math.PI - Aclosestopponent.t);
						if(Aclosestopponent.r <= abstract_robot.RADIUS*1.5 &&
						   Math.abs(Aourgoal.t-Aclosestopponent.t)<Math.PI/2)
						{
							if(Aball.y>0)
							{
								Aresult.sett(Aclosestopponent.t + ASIDE*Math.PI/2);
								Aresult.setr(1.0);
							}
							else
							{
								Aresult.sett(Aclosestopponent.t - ASIDE*Math.PI/2);
								Aresult.setr(1.0);
							}
						}
						//Si hay un compañero en medio también lo rodeo
						else if(Aclosestteammate.r <= abstract_robot.RADIUS*1.5 &&
								   Math.abs(Aourgoal.t-Aclosestteammate.t)<Math.PI/2)
								{
									if(Aball.y>0)
									{
										Aresult.sett(Aclosestteammate.t + ASIDE*Math.PI/2);
										Aresult.setr(1.0);
									}
									else
									{
										Aresult.sett(Aclosestteammate.t - ASIDE*Math.PI/2);
										Aresult.setr(1.0);
									}
						}
						else//Tengo vía libre
						{
							Aresult.setx(Aourgoal.x);
							if((ASIDE==-1 && cuadranteRelativoPelota==0)
								|| (ASIDE==1 && cuadranteRelativoPelota==1))
									Aresult.sety(Aourgoal.y + abstract_robot.RADIUS*4.25);
							else
								Aresult.sety(Aourgoal.y - abstract_robot.RADIUS*4.25);
							Aresult.setr(1.0);
						}
					}
					else //Estoy en distinta y que la pelota, pero dentro del área
					{	//Me moveré hacia arriba o hacia abajo para estar a su altura
						double anguloNuevo = (Aball.y > 0) ? Math.PI/2 : Math.PI*3/2;
						Vec2 aux = new Vec2();
						aux.sett(anguloNuevo);
						//Si me estorba un oponente
						if(ASIDE==1 && Aclosestopponent.t>Math.PI*3/2)
							Aclosestopponent.sett(2*Math.PI - Aclosestopponent.t);
						if(Aclosestopponent.r <= abstract_robot.RADIUS*1.5 &&
						   Math.abs(aux.t-Aclosestopponent.t)<Math.PI/2)
						{
							if(Aball.y>0)
							{
								Aresult.sett(Aclosestopponent.t + ASIDE*Math.PI/2);
								Aresult.setr(1.0);
							}
							else
							{
								Aresult.sett(Aclosestopponent.t - ASIDE*Math.PI/2);
								Aresult.setr(1.0);
							}
						}
						//o un compañero
							else if(Aclosestteammate.r <= abstract_robot.RADIUS*1.5 &&
									   Math.abs(aux.t-Aclosestteammate.t)<Math.PI/2)
									{
										if(Aball.y>0)
										{
											Aresult.sett(Aclosestteammate.t + ASIDE*Math.PI/2);
											Aresult.setr(1.0);
										}
										else
										{
											Aresult.sett(Aclosestteammate.t - ASIDE*Math.PI/2);
											Aresult.setr(1.0);
										}
							}
						else//Tengo vía libre
						{
							Aresult.setx(0.0);
				    		Aresult.sety(Aball.y);
				    		Aresult.setr(1.0);
						}
					}
				}
				}
			}	
			// set the heading
			abstract_robot.setSteerHeading(Acurr_time, Aresult.t);

			// set speed at maximum
			abstract_robot.setSpeed(Acurr_time, Aresult.r);

			// kick it if we can
			if (abstract_robot.canKick(Acurr_time))
				abstract_robot.kick(Acurr_time);
	}
	
	private void defensaJ()
	{
		//Analiza campo
		Jcurr_time = abstract_robot.getTime();
		Jposicion=abstract_robot.getPosition(Jcurr_time);
		Jid = abstract_robot.getPlayerNumber(Jcurr_time);
		JegoBola= abstract_robot.getBall(Jcurr_time);
		Jbola=creaAbsolutos(JegoBola);
		JnuestraPorteria = abstract_robot.getOurGoal(Jcurr_time);
		JsuPorteria = abstract_robot.getOpponentsGoal(Jcurr_time);
		Jnosotros = abstract_robot.getTeammates(Jcurr_time);
		Jellos = abstract_robot.getOpponents(Jcurr_time);
		Jrotacion=abstract_robot.getSteerHeading( Jcurr_time);
		Jchuta=false;
		
		//Estrategia
//		Pelota en nuestro campo
		if(pelotaEnNuestroCampo())
		{
			//Si estoy lejos de la pelota 
			if(Jbola.octant()!= Jposicion.octant() )
			{
				Jresultado.setx(JegoBola.x);
				Jresultado.sety(JegoBola.y);
				Jresultado.setr(1.0);
			}
			//estoy cerca, intento correr con ella.
			else
			{
				correConLaPelota();
			}
			
		}
		//Pelota en campo contrario
		else
		{
			//si estoy lejos de la pelota, vuelvo al centro
			if(Jbola.octant()!= Jposicion.octant())
			{
				Vec2 temp=new Vec2(-Jposicion.x,-Jposicion.y);
				Jresultado.sett(temp.t);
				Jresultado.setr(1.0);
			}
			//corro con  la pelota.
			else
			{
				correConLaPelota();
			}
		}
		
		//Ejecuta movimiento
		abstract_robot.setSteerHeading(Jcurr_time, Jresultado.t);
		abstract_robot.setSpeed(Jcurr_time, Jresultado.r);
		if (Jchuta && abstract_robot.canKick( Jcurr_time))
			abstract_robot.kick(Jcurr_time);
		
	}
	
	private void jugadoresD(int mynum)
	{
//		 guarda el resultado
		Vec2	result = new Vec2(0,0);

		// momento actual
		long	curr_time = abstract_robot.getTime();


		/*--- Datos de los sensores ---*/
		// pelota
		Vec2 ball = abstract_robot.getBall(curr_time);

		// porterías
		Vec2 ourgoal = abstract_robot.getOurGoal(curr_time);
		Vec2 theirgoal = abstract_robot.getOpponentsGoal(curr_time);

		// compañeros y oponentes
		Vec2[] teammates = abstract_robot.getTeammates(curr_time);
		Vec2[] opponents = abstract_robot.getOpponents(curr_time);

		
		// compañero más cercano
		Vec2 closestteammate = new Vec2(99999,0);
		for (int i=0; i< teammates.length; i++)
			{
			if (teammates[i].r < closestteammate.r)
				closestteammate = teammates[i];
			}
		
		Vec2 closestopponent = new Vec2(99999,0);
		for (int i=0; i< opponents.length; i++)
			{
			if (opponents[i].r < closestopponent.r)
				closestopponent = opponents[i];
			}
		
		
			/*--- puntos estratégicos ---*/
			
			// entre la pelota y la portería propia
			Vec2 goaliepos = new Vec2(ourgoal.x + ball.x,ourgoal.y + ball.y);
			goaliepos.setr(goaliepos.r*0.5);

			// alejarse del compañero más cercano
			Vec2 awayfromclosest = new Vec2(closestteammate.x,closestteammate.y);
			awayfromclosest.sett(awayfromclosest.t + Math.PI);
			
			//alejarse del oponente más cercano
			Vec2 awayfromclosestO = new Vec2(closestopponent.x,
					closestopponent.y);
			awayfromclosestO.sett(awayfromclosestO.t - Math.PI-Math.PI/2);
						
			Vec2 actualPos = abstract_robot.getPosition(curr_time);
			
			//Estrategia para evitar bloqueos (común)
			boolean iguales = (actualPos.x-DantiguaPos.x<0.01) &&(actualPos.y-DantiguaPos.y<0.01);
			if(iguales) DpasosSinMover=DpasosSinMover+1;
			else DantiguaPos=actualPos;
			if(DpasosSinMover==20){
				DpasosSinMover=0;
				DpasosEscape=1;
				
				double radianAleat = (Dr.nextDouble()* 360.0)/2*Math.PI;
				if(closestopponent.r<closestteammate.r)
				 radianAleat = awayfromclosestO.t;//(r.nextDouble()* 360.0)/2*Math.PI;
				else radianAleat = awayfromclosest.t;
				result.sett(radianAleat);
				result.setr(1.0);
			}
			else
				if(DpasosEscape!=0){
					if(DpasosEscape<2) {
						DpasosEscape++;
					}
					else DpasosEscape=0;
					double radianAleat = Dr.nextDouble()* 2*Math.PI;
					result.setr(1.0);
					result.sett(radianAleat);
						}
				else
					
			 if (mynum == 2)/*--- Lateral 1 ---*/
				{
				/*ESTRATEGIA DEL LATERAL*/
				if(ball.r<=abstract_robot.RADIUS*1.1){
					if(ball.t == theirgoal.t){
						//estoy en posición de chut
						result.sett(ball.t);
						result.setr(1.0);
					}
					else{
						result.sett(ball.t);
						result.setr(1.0);
					}
				}else
				if(closestteammate.r<abstract_robot.RADIUS){
					//lo principal, es no estorbarse
					double anguloMate = (2*Math.PI + closestteammate.t)% (2*Math.PI);
					double anguloPelota =  (2*Math.PI + ball.t)% (2*Math.PI);
					if(Math.abs(anguloMate-anguloPelota)<0.01){
						result.sett(ball.t);
						result.setr(1.0);
					}
					else{
					result.sett(ourgoal.t);
					result.setr( 1.0);
					}
				}
				else{
				if(ball.r<abstract_robot.RADIUS*10){
					if(ball.r<abstract_robot.RADIUS*2){
						//la pelota está cerca
						if(ball.t == theirgoal.t){
							//estoy en posición de chut
							result.sett(ball.t);
							result.setr(1.0);
						}
						else{
							int myOctant1 = ourgoal.octant();
							int myOctant2;
							if (myOctant1 == 4)myOctant2 = 3;//soy el equipo de la izq
							else myOctant2 = 0;//soy el equipo de la derecha
							if(ball.octant()==myOctant1 || ball.octant()==myOctant2){
								//está en nuestros octantes
								result.sett(goaliepos.t);
								result.setr(1.0);							
							}
							else{
								//no está en nuestros octantes, busco situarme para chutar a gol
								Vec2 posChut = new Vec2(ball);
								theirgoal.setr(abstract_robot.RADIUS);
								posChut.sub(theirgoal);
								result.sett(posChut.t);
								result.setr(1.0);
							}
						}
					}
					else{
					//si la pelota está a media distancia, ir hacia la posición
					//de chut
						Vec2 posChut = new Vec2(ball);
						theirgoal.setr(abstract_robot.RADIUS);
						posChut.sub(theirgoal);
						result.sett(posChut.t);
						result.setr(1.0);
					}
				}
				else{
					result.sett(ball.t);
					result.setr(1.0);
					
				}
				}
				}

			/*--- Delantero ---*/			
			else if (mynum == 3)
			{
				if(ball.r<=abstract_robot.RADIUS*1.2){
					if(ball.t==0){
						result.sett(theirgoal.t);
						result.setr(1.0);
					}
					else{
					result.sett(ball.t);
					result.setr(1.0);
					}
				}else
				if(closestteammate.r<abstract_robot.RADIUS *2){
					//lo principal, es no estorbarse
					result.sett( awayfromclosest.t);
					result.setr(1.0);
				}
				else{
				if(ball.r<abstract_robot.RADIUS*2){
					if(ball.t==theirgoal.t){
						result.sett(theirgoal.t);
						result.setr(1.0);
					}
					else{
						Vec2 posChut = new Vec2(ball);
						theirgoal.setr(abstract_robot.RADIUS);
						posChut.sub(theirgoal);
						result.sett(posChut.t);
						result.setr(1.0);
					}
				}
				else{
					result.sett(ball.t);
					result.setr(1.0);
				} 	
				}
			}
			/*--- Lateral 2 ---*/	
			
			else if (mynum == 4)
			{
				/*ESTRATEGIA DEL LATERAL*/
				if(ball.r<=abstract_robot.RADIUS*1.1){
					if(ball.t == theirgoal.t){
						//estoy en posición de chut
						result.sett(ball.t);
						result.setr(1.0);
					}
					else{
						result.sett(ball.t);
						result.setr(1.0);
					}
				}else
				if(closestteammate.r<abstract_robot.RADIUS){
					//lo principal, es no estorbarse
					double anguloMate = (2*Math.PI + closestteammate.t)% (2*Math.PI);
					double anguloPelota =  (2*Math.PI + ball.t)% (2*Math.PI);
					if(Math.abs(anguloMate-anguloPelota)<0.01){
						result.sett(ball.t);
						result.setr(1.0);
					}
					else{
					result.sett( ourgoal.t);
					result.setr(1.0);
					}
				}
				else{
				if(ball.r<abstract_robot.RADIUS*10){
					if(ball.r<abstract_robot.RADIUS*2){
						//la pelota está cerca
						if(ball.t == theirgoal.t){
							//estoy en posición de chut
							result.sett(ball.t);
							result.setr(1.0);
						}
						else{
							int myOctant1 = ourgoal.octant();
							int myOctant2;
							if (myOctant1 == 4)myOctant2 = 3;//soy el equipo de la izq
							else myOctant2 = 0;//soy el equipo de la derecha
							if(ball.octant()==myOctant1 || ball.octant()==myOctant2){
								//está en nuestros octantes
								result.sett(goaliepos.t);
								result.setr(1.0);							
							}
							else{
								//no está en nuestros octantes, busco situarme para chutar a gol
								Vec2 posChut = new Vec2(ball);
								theirgoal.setr(abstract_robot.RADIUS);
								posChut.sub(theirgoal);
								result.sett(posChut.t);
								result.setr(1.0);
							}
						}
					}
					else{
					//si la pelota está a media distancia, ir hacia la posición
					//de chut
						Vec2 posChut = new Vec2(ball);
						theirgoal.setr(abstract_robot.RADIUS);
						posChut.sub(theirgoal);
						result.sett(posChut.t);
						result.setr(1.0);
					}
				}
				else{
					result.sett(ball.t);
					result.setr(1.0);
					
				}
				}
			}

			/*--- Send commands to actuators ---*/
			// set the heading
			abstract_robot.setSteerHeading(curr_time, result.t);

			// set speed at maximum
			abstract_robot.setSpeed(curr_time, result.r);

			// kick it if we can
			if (abstract_robot.canKick(curr_time))
				abstract_robot.kick(curr_time);
	}
	
	private int dameCuadranteReal(Vec2 vector)
	{
		if(Math.abs(vector.x) <= 0.000001 && Math.abs(vector.y)<=0.000001)
			return (ASIDE==-1) ? 0: 1;
		if(vector.x > 0 && vector.y>0)
			return 0;
		if(vector.x < 0 && vector.y>0)
			return 1;
		if(vector.x < 0 && vector.y<0)
			return 2;
		if(vector.x > 0 && vector.y<0)
			return 3;
		
		return 0;
	}
	
	private void correConLaPelota()
	{
		//Estoy detras de la pelota.
		if(detrasDe(JegoBola,JsuPorteria))
		{
			//Si estoy cerca, chuto
			if( (Math.abs( Jrotacion - JsuPorteria.t) < Math.PI/8) &&
				    (JsuPorteria.r < 0.35))
					Jchuta = true;
			//Estoy lejos, corro a la porteria.
			else 
				Jresultado.sett(JsuPorteria.t);
				Jresultado.setr(1.0);
		}
		//Me tengo q poner detras de la pelota.
		else
		{
			ponteDetrasDeLaPelota();
		}
	}
	
//	Reutilizado de DTEAM.
	private boolean detrasDe(Vec2 objeto,Vec2 referencia)
	{
		if( Math.abs( objeto.t - referencia.t) < Math.PI/10) 
			return true;
		else
			return false;
	}
	
	//REUTILIZADO DE DTEAM
	private void ponteDetrasDeLaPelota()
	{
		Vec2 behind_point = new Vec2(0,0);
		double behind = 0;
		double point_side = 0;

		// find a vector from the point, away from the orientation
		// you want to be
		behind_point.sett( JsuPorteria.t);
		behind_point.setr( JsuPorteria.r);

		behind_point.sub( JegoBola);
		behind_point.setr( -Jradio*1.8);

			// determine if you are behind the object with respect
			// to the orientation
		behind = Math.cos( Math.abs( JegoBola.t - behind_point.t));

			// determine if you are on the left or right hand side
			// with respect to the orientation
		point_side = Math.sin( Math.abs( JegoBola.t - behind_point.t));

			// if you are in FRONT
		if( behind > 0)
		{
			// make the behind point more of a beside point
			// by rotating it depending on the side of the
			// orientation you are on
			if( point_side > 0)
				behind_point.sett( behind_point.t + Math.PI/2);
			else
				behind_point.sett( behind_point.t - Math.PI/2);
		}

		// move toward the behind point
		Jresultado.sett( Jbola.t);
		Jresultado.setr( Jbola.r);
		Jresultado.add( behind_point);

		Jresultado.setr( 1.0);

	}
	
//	devuelve si la pelota esta en nuestro campo o no.
	private boolean pelotaEnNuestroCampo()
	{
		if((Jbola.quadrant()==1 || Jbola.quadrant()==2)&& Jlado==-1 )
			return true;
		else if ((Jbola.quadrant()==0 || Jbola.quadrant()==3)&& Jlado==1 )
			return true;
		else
			return false;
	}
	
//	Calcula coordenadas absolutas (centro=centro del campo) del objeto.
	private Vec2 creaAbsolutos(Vec2 objeto)
	{
		Vec2 temp= new Vec2(objeto.x,objeto.y);
		temp.add(Jposicion);
		return temp;
	}
	
}
