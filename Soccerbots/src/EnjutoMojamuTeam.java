

import	EDU.gatech.cc.is.util.Vec2;
import	EDU.gatech.cc.is.abstractrobot.*;
//Clay not used

/**
 * This is about the simplest possible soccer strategy, just go to the ball.
 * <P>
 * <A HREF="../COPYRIGHT.html">Copyright</A>
 * (c)1997 Georgia Tech Research Corporation
 *
 * @author Tucker Balch
 * @version $Revision: 1.3 $
 */


public class EnjutoMojamuTeam extends ControlSystemSS
	{
	/**
	Configure the Avoid control system.  This method is
	called once at initialization time.  You can use it
	to do whatever you like.
	*/
	private boolean miPosesion;
	private Vec2[] oponentes, companeros;
	private Vec2 balon;
	private long curr_time;
	
	
	public void Configure()
	{
		int numRobot = abstract_robot.getPlayerNumber(abstract_robot.getTime());
		if (numRobot == 0)
			abstract_robot.setDisplayString("Casillas");
		else if (numRobot == 1)
			abstract_robot.setDisplayString("S.Ramos");
		else if (numRobot == 2)
			abstract_robot.setDisplayString("Pajares");
		else if (numRobot == 3)
			abstract_robot.setDisplayString("Raúl");
		else if (numRobot == 4)
			abstract_robot.setDisplayString("Guti.Haz");
	}
		
	
	private boolean calcularPosesion() 
	{
		double distanciaOponenteMasCercanoBalon = 99999;
		Vec2 posBalon = abstract_robot.getBall(curr_time);
		double distanciaOponenteBalon;
		for (int i=0; i< oponentes.length; i++)
		{
			distanciaOponenteBalon = calcularDistancia(posBalon, oponentes[i]);
			if (distanciaOponenteBalon < distanciaOponenteMasCercanoBalon)
				distanciaOponenteMasCercanoBalon = distanciaOponenteBalon;
		}
		
		double distanciaJugadorMasCercanoBalon = calcularDistancia(posBalon, abstract_robot.getPosition(curr_time));
		double distanciaCompaneroBalon;
		for (int i=0; i< companeros.length; i++)
		{
			distanciaCompaneroBalon = calcularDistancia(posBalon, companeros[i]);
			if (distanciaCompaneroBalon < distanciaJugadorMasCercanoBalon)
				distanciaJugadorMasCercanoBalon = distanciaCompaneroBalon;
		}
		if (distanciaJugadorMasCercanoBalon < distanciaOponenteMasCercanoBalon)
			return true;
		else
			return false;
	}


	private double calcularDistancia(Vec2 posBalon, Vec2 vec2) 
	{
		double x = posBalon.x - vec2.x;
		double y = posBalon.y - vec2.y;
		
		double modulo = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		return modulo;
	}


	/**
	Called every timestep to allow the control system to
	run.
	*/
	public int TakeStep()
	{
		curr_time = abstract_robot.getTime();
		oponentes = abstract_robot.getOpponents(curr_time);
		companeros = abstract_robot.getTeammates(curr_time);
		balon = abstract_robot.getBall(curr_time);
		
		//System.out.println(abstract_robot.getOpponents(curr_time).toString());
		showDatos();
		
		if (abstract_robot.getPlayerNumber(curr_time) == 0)
		{
			boolean pos = calcularPosesion();
			if (!miPosesion && pos)
			{
				miPosesion = pos;
				System.out.println("RECUPERO");
			}
			else if (miPosesion && !pos)
			{
				miPosesion = pos;
				System.out.println("PIERDO");
			}
		}
		//else
		//{
			if (miPosesion)
				atacar();
			else if (!miPosesion)
				defender();
		//}
		

		// tell the parent we're OK
		return(CSSTAT_OK);
		}


	private void defender() 
	{
		//Incluso comentando esto se ve más gráfico, con el portero cubriendo también.
		if (abstract_robot.getPlayerNumber(curr_time) != 0)
		{
			cubrir(abstract_robot.getPlayerNumber(curr_time));
		}
		
	}


	private void cubrir(int i) 
	{
		//Nunca entendido, de hecho he puesto ahora >= para que cubra también el jugador 4.
		if (oponentes.length >= i)
		{
			Vec2 vOponenteBalon = (Vec2)balon.clone(); 
			vOponenteBalon.setx(vOponenteBalon.x - oponentes[i].x);
			vOponenteBalon.sety(vOponenteBalon.y - oponentes[i].y);
			//Hacemos que el vector sea unitario, porque se multiplicará después por el 2*radio (Menuda diferencia!!).
			vOponenteBalon.normalize(1.0);
			
			//System.out.println("La medida de 2*Radius es = " + (2*SocSmall.RADIUS));
			
			//Sí, multiplicar, porque lo que quieres es una parte del vector, para hacer la corrección.
			double pX = (2*SocSmall.RADIUS)*vOponenteBalon.x;
			double pY = (2*SocSmall.RADIUS)*vOponenteBalon.y;
			Vec2 posicionJugador = abstract_robot.getPosition(curr_time);
			//Antes
				//Vec2 vJugadorPosicion = new Vec2((pX - posicionJugador.x),(pY - posicionJugador.y));
			//Ahora
			
			//No vale, prueba inicial..
			//Vec2 vJugadorPosicion = new Vec2((pX + posicionJugador.x),(pY + posicionJugador.y));
			
			//Yendo a la bola, pero con corrección de ir "por donde vaya el rival" (poco útil) 
			//Vec2 vJugadorPosicion = new Vec2((pX + balon.x),(pY + balon.y));

			//Directamente al jugador rival (bloqueas más):
			//Vec2 vJugadorPosicion = new Vec2(oponentes[i].x,oponentes[i].y);
			
			//Entre la bola y el jugador:
			Vec2 vJugadorPosicion = new Vec2((pX + oponentes[i].x),(pY + oponentes[i].y));

			//Como siempre
				abstract_robot.setSteerHeading(curr_time, vJugadorPosicion.t);
			
			abstract_robot.setSpeed(curr_time, 1.0);
		}
	}


	private void atacar() 
	{
		if (abstract_robot.getPlayerNumber(curr_time) != 0)
		{
			// set heading towards it
			abstract_robot.setSteerHeading(curr_time, balon.t);
	
			// set speed at maximum
			abstract_robot.setSpeed(curr_time, 1.0);
	
			// kick it if we can
			if (abstract_robot.canKick(curr_time))
				abstract_robot.kick(curr_time);
		}
		
	}
	
	public void showDatos() {
		if (abstract_robot.getPlayerNumber(curr_time) == 0) {
			long curr_timeAux = abstract_robot.getTime();
			Vec2[] oponentesAux = abstract_robot.getOpponents(curr_time);
			Vec2[] companerosAux = abstract_robot.getTeammates(curr_time);
			Vec2 balonAux = abstract_robot.getBall(curr_time);
			
			System.out.println("Listado de datos disponibles, desde jugador: " + (abstract_robot.getPlayerNumber(curr_time)) + ".");
			System.out.println("Compañeros:");
			printVectorVec2(companerosAux);
			System.out.println("Oponentes");
			printVectorVec2(oponentesAux);
			System.out.println("Posicion del balón:");
			System.out.println("Vector/Posicion Balón: (" +balonAux.x + "," + balonAux.y + "). R=" + balonAux.r + " . T=" + balonAux.t + ". ");			
			System.out.println("__________________________________________________________________________");
		}
	}
	
	public void printVectorVec2(Vec2[] vector) {
		for (int i=0; i< vector.length; i++)		
		{
			System.out.println("Vector Op[" + i + "] --> (" +vector[i].x + "," + vector[i].y + "). R=" + vector[i].r + " . T=" + vector[i].t + ". ");
		}
		//System.out.println("__________________________________________________________________________");
	}
}
