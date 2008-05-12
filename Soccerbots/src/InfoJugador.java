import EDU.gatech.cc.is.util.Vec2;

//package equiposisbc;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class InfoJugador {

  public static int POS_RECORDADAS = 100;
  private Vec2[] posiciones = new Vec2[POS_RECORDADAS];
  //Índice para hacer que el array de posiciones sea circular
  private int ultimaPosicion = 0;
  private int numPos = 0;
	
  private int numJugador;
  
  private double maxX;  //Máxima x a la que ha ido el jugador
  private double minX;  //Mínima x a la que ha ido el jugador  
  private double maxY;  //Máxima y a la que ha ido el jugador
  private double minY;  //Mínima y a la que ha ido el jugador
  private double xPromedio; //Valor de la x en la que ha estado más
  private double yPromedio;	//Valor de la y en la que ha estado más
  
  private boolean inicializado = false;
  

//------------------------------------------------------------------------------
  public InfoJugador(int jug, Vec2 posInicial) {
    numJugador = jug;
    maxX = posInicial.x;
    minX = posInicial.x;    
    maxY = posInicial.y;
    minY = posInicial.y;    
    yPromedio = posInicial.y;
    xPromedio = posInicial.x;
  }
  public InfoJugador(int numJ) {
	  numJugador = numJ;
  }
//------------------------------------------------------------------------------
  public int getNumJugador() {
    return numJugador;
  }
//------------------------------------------------------------------------------
  public double minX() {
	return minX;
  }
//------------------------------------------------------------------------------
  public double maxX() {
	return maxX;
  }
//------------------------------------------------------------------------------
  public double minY() {
	return minY;
  }
//------------------------------------------------------------------------------
  public double maxY() {
	return maxY;
  }
//------------------------------------------------------------------------------
  public double xPromedio() {
	return xPromedio;
  }
//------------------------------------------------------------------------------
  public double yPromedio() {
	return yPromedio;
  }
//------------------------------------------------------------------------------
  public Vec2[] getPosiciones() {
	  return posiciones;
  }
//------------------------------------------------------------------------------
  public int getNumPosiciones() {
	  return numPos;
  }
//------------------------------------------------------------------------------
  public int getUltimaPos() {
	  return ultimaPosicion;
  }  
//------------------------------------------------------------------------------
  //METODOS PARA HACER EL ARRAY CIRCULAR
  public static int siguiente(int indice) {
	  return (indice+1)%POS_RECORDADAS;
  }
  public static int anterior(int indice) {
	  return (POS_RECORDADAS+(indice-1))%POS_RECORDADAS;
  }  
//------------------------------------------------------------------------------  
//------------------------------------------------------------------------------
  public void addNuevaPos(Vec2 pos) {
	  if (!inicializado) {
		    maxX = pos.x;
		    minX = pos.x;    
		    maxY = pos.y;
		    minY = pos.y;    
		    yPromedio = pos.y;
		    xPromedio = pos.x;
		    inicializado = true;
		    posiciones[ultimaPosicion] = pos;
			ultimaPosicion = siguiente(ultimaPosicion);
			numPos++;
	  }
	  else {
		  posiciones[ultimaPosicion] = pos;
		  ultimaPosicion = siguiente(ultimaPosicion);
		  minX = Math.min(minX,pos.x);
		  maxX = Math.max(maxX,pos.x);
		  minY = Math.min(minY,pos.y);
		  maxY = Math.max(maxY,pos.y);
		  xPromedio = (xPromedio+pos.x)/2;
		  yPromedio = (yPromedio+pos.y)/2;
		  if (numPos < POS_RECORDADAS) numPos++;
	  }
  }

}