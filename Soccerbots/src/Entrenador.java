import EDU.gatech.cc.is.abstractrobot.ControlSystemSS;
import EDU.gatech.cc.is.abstractrobot.SocSmall;
import EDU.gatech.cc.is.util.Vec2;


///import InfoJugadores;//package equiposisbc;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Entrenador {
	
	  private final int NUM_ESTARTEGIAS = 10;
	  private final int PORTERO = 0;
	  private final int DEFENSA_CERRADA = 1;
	  private final int DEFENSA_OFENSIVA = 2;
	  private final int CENTROCAMPISTA_DEFENSIVO = 3;
	  private final int CENTROCAMPISTA_OFENSIVO = 4;
	  private final int LATERAL_DERECHO = 5;
	  private final int LATERAL_IZQUIERDO = 6;
	  private final int DELANTERO_DEFENSIVO = 7;
	  private final int DELANTERO_OFENSIVO = 8;	
	  private final int DELANTERO_COÑAZO = 9;
	
	
  //Tipos de situaciones en las que puede estar un jugador	
  private final int SALIR_BLOQUEO = 0;
	
  private static Entrenador _instancia=null;


  private int numJugsAsignadaEstrategia = 0;
  
  //private int JUGADAS_RECORDADAS = 100;
  private int NUM_JUGADORES= 5;

  //Array (tam=JUGADAS_RECORDADAS) de de arrays de tamaño numJugadores
  private InfoJugador[] infoOponentes = new InfoJugador[NUM_JUGADORES];
  private InfoJugador[] infoCompañeros = new InfoJugador[NUM_JUGADORES];  
  
  //Indica las estrategias de cada jugador
  int[] alineaciones = new int[NUM_JUGADORES];

  private int portero = 0; //Indica que numero es el portero
  
  //Es el robot que representa al jugador que pide la estrategia
  SocSmall[] abstract_robots = new SocSmall[NUM_JUGADORES];
  int numbJugActual;
  
  //Contador para que ciertas acciones duren un tiempo determminado
  private int contador = 0; 
  
//------------------------------------------------------------------------------
  private Entrenador() {
	  alineaciones[0] = PORTERO;
	  alineaciones[1] = CENTROCAMPISTA_DEFENSIVO;
	  alineaciones[2] = DELANTERO_COÑAZO;//LATERAL_IZQUIERDO;//
	  alineaciones[3] = LATERAL_DERECHO;
	  alineaciones[4] = DELANTERO_OFENSIVO;
	  for (int i=0; i<NUM_JUGADORES; i++) {
		  infoCompañeros[i] = new InfoJugador(i);
		  infoOponentes[i] = new InfoJugador(i);
	  }
  }
//------------------------------------------------------------------------------
  public static Entrenador getInstance(){
    if (_instancia==null)
      _instancia=new Entrenador();
    return _instancia;
  }
  
//------------------------------------------------------------------------------
  public /*synchronized*/ int getEstrategia(SocSmall abstract_rob) {  
	  
    //Almacenamos mi información
    long time = abstract_rob.getTime();
    Vec2 me = abstract_rob.getPosition(time);
    numbJugActual = abstract_rob.getPlayerNumber(time);
    abstract_robots[numbJugActual] = abstract_rob;
    int estrategia = alineaciones[numbJugActual];   
    //Almaceno la información del jugador actual
    infoCompañeros[numbJugActual].addNuevaPos(me);
    
    //Cuando conozco toda la info de mis jugadores entonces analizo la situación
    if (numJugsAsignadaEstrategia == 4) {
        //Obtenemos información del adversario
        obtenerInfoAdversario();
        analizaSituacion();
    }    
    numJugsAsignadaEstrategia = (numJugsAsignadaEstrategia+1)%NUM_JUGADORES;
    return estrategia;
  }
//------------------------------------------------------------------------------
  private void analizaSituacion() {
	  int cambiaPorteroPor = -1;
	  for (int i=0; i<NUM_JUGADORES; i++) {
		  long time = abstract_robots[i].getTime();
		  InfoJugador info = infoCompañeros[i];
		  //Si es el portero
		  /*if (portero==i) { 
			  //Calculo las posiciones egocéntricas respecto al portero
			  Vec2[] ego_oponentes = abstract_robots[i].getOpponents(time);
			  Vec2[] ego_compañeros = abstract_robots[i].getTeammates(time);
			  Vec2 ego_ball = abstract_robots[i].getBall(time);
			  Vec2 ego_op_cercano = closest_to_me(ego_oponentes);
			  Vec2 ego_compi_cercano = closest_to_me(ego_compañeros);
			  Vec2 compi_a_bola = new Vec2(ego_ball); 
			  compi_a_bola.sub(ego_compi_cercano);
			  //Si el compi +cercano está mas cerca que el contrario mas cercano, y estoy + cerca de la
			  //bola que mi compi, entonces mi compi pasa a ser el portero y yo defensa
			  if (ego_compi_cercano.r < ego_op_cercano.r  &&  ego_ball.r<compi_a_bola.r) {
				  Vec2 compi = egoToCenter(ego_compi_cercano);
				  cambiaPorteroPor = buscaCompi(compi);
				  alineaciones[i] = LATERAL_IZQUIERDO;
				  alineaciones[cambiaPorteroPor] = PORTERO;
				  //Inicio el contador para que se realice la acción
				  contador = 0;
				  portero = cambiaPorteroPor;
			  }
		  }*/
		  Vec2[] ego_compañeros = abstract_robots[i].getTeammates(time);
		  Vec2 ego_compi_cercano = closest_to_me(ego_compañeros);
		  if (bloqueado(info) && portero ==i && ego_compi_cercano.r>0.3) {
			  cambiaPorteroPor = buscaCompi();
			  portero = cambiaPorteroPor;
			  //alineaciones[i] = (alineaciones[i]-1)%NUM_ESTARTEGIAS;
			  alineaciones[i] = CENTROCAMPISTA_DEFENSIVO;
			  alineaciones[portero] = PORTERO;
		  }
	  }
  }
//------------------------------------------------------------------------------
  private void obtenerInfoAdversario() {
	  long time = abstract_robots[numbJugActual].getTime();
	  Vec2[] ego_oponentes = abstract_robots[numbJugActual].getOpponents(time);
	  for (int i=0; i<ego_oponentes.length; i++) {
		  Vec2 ego_op = ego_oponentes[i];
		  Vec2 op = egoToCenter(ego_op); 
		  infoOponentes[i].addNuevaPos(op);
	  }
  }
//------------------------------------------------------------------------------
  private boolean bloqueado(InfoJugador inf) {	  
	  boolean bloqueado = false;
	  double minRad = 9999;
	  double maxRad = -9999;
	  //double angulo 
	  Vec2[] posiciones = inf.getPosiciones();
	  if (inf.getNumPosiciones() > 50) {
		  int indice = InfoJugador.anterior(inf.getUltimaPos());
		  for (int i=0; i<50; i++) {
			  Vec2 pos = posiciones[indice];
			  minRad = Math.min(minRad,pos.r);
			  maxRad = Math.max(maxRad,pos.r);
			  indice = InfoJugador.anterior(indice);
		  }
		  bloqueado = (Math.abs(minRad-maxRad) <= 0.01);
	  }	  
	  return bloqueado;
  }
//------------------------------------------------------------------------------  
  private int salirBloqueo() {
	  return 0;
  }

//------------------------------------------------------------------------------  
  private int buscaCompi(/*Vec2 compi*/) {
	  boolean encontrado = false;
	  int i=0;
	  //InfoJugador posible = null;
	  while (!encontrado) {
		  //posible = infoCompañeros[i];
		  //Obtengo la última posición en la que estuvo compi
		  /*Vec2 c = posible.getPosiciones()[InfoJugador.anterior(posible.getUltimaPos())];
		  encontrado = Math.abs(c.x-compi.x) < 0.08 &&
		  			   Math.abs(c.y-compi.y) < 0.08 ; */
		  encontrado = alineaciones[i] == CENTROCAMPISTA_DEFENSIVO;
		  i++;
	  }
	  return i-1;
  }
//------------------------------------------------------------------------------
  private Vec2 centerToEgo(Vec2 vCenter) {
	long time = abstract_robots[numbJugActual].getTime();
	Vec2 jugador = abstract_robots[numbJugActual].getPosition(time);
    Vec2 vEgo = new Vec2(vCenter);
    vEgo.sub(jugador);
    return vEgo;
  }

//------------------------------------------------------------------------------
  private Vec2 egoToCenter(Vec2 vEgo) {
	long time = abstract_robots[numbJugActual].getTime();
	Vec2 jugador = abstract_robots[numbJugActual].getPosition(time);
    Vec2 vCenter = new Vec2(vEgo);
    vCenter.add(jugador);
    return vCenter;
  }  
//------------------------------------------------------------------------------  
  //Devuelve el vector mas cercano.
  //los objects son egocéntricos
  private Vec2 closest_to_me( Vec2[] objects)
        {
                double dist = 9999;
                Vec2 result = new Vec2(0, 0);

                for( int i=0; i < objects.length; i++)
                {
                	// if the distance is smaller than any other distance
                    // then you have something closer to the point
                    if(objects[i].r < dist) {
                    	result = objects[i];
                    	dist = objects[i].r;
                    }
                }

                return result;
        }  
//------------------------------------------------------------------------------  
}