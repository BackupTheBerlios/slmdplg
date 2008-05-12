

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
 * @version $Revision: 1.1 $
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
		else
		{
			if (miPosesion)
				atacar();
			else if (!miPosesion)
				defender();
		}
		

		// tell the parent we're OK
		return(CSSTAT_OK);
		}


	private void defender() 
	{
		if (abstract_robot.getPlayerNumber(curr_time) != 0)
		{
			cubrir(abstract_robot.getPlayerNumber(curr_time));
		}
		
	}


	private void cubrir(int i) 
	{
		if (oponentes.length > i)
		{
			Vec2 vOponenteBalon = (Vec2)balon.clone(); 
			vOponenteBalon.setx(vOponenteBalon.x - oponentes[i].x);
			vOponenteBalon.sety(vOponenteBalon.y - oponentes[i].y);
			
			double pX = (2*SocSmall.RADIUS)*vOponenteBalon.x;
			double pY = (2*SocSmall.RADIUS)*vOponenteBalon.y;
			Vec2 posicionJugador = abstract_robot.getPosition(curr_time);
			Vec2 vJugadorPosicion = new Vec2((pX - posicionJugador.x),(pY - posicionJugador.y));
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
}
