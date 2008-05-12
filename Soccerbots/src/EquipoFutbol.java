/*
 * EquipoFutbol.java.

 */

//package ISBC;
import EDU.gatech.cc.is.util.Vec2;
import EDU.gatech.cc.is.abstractrobot.*;
import java.lang.Math;
import jess.*;

public class EquipoFutbol
  extends ControlSystemSS {
  private int resultado;
  private boolean tenemos_balon;
  private boolean balon_nuestro_campo;
  private boolean defendemos;
  private int goles_a_favor_ataque;
  private int goles_a_favor_empate;
  private int goles_en_contra_defensa;
  private String estrategia;

  Rete jess_engine;
  int i;
  String cadena;

  private long tiempoActual; //Indica el momento
  private long miNumero; //Indica el jugador
  private double giro; //Indica  a que direccion

  private Vec2 balon; //Indica donde está el balón
  private Vec2 nuestra_porteria; //Indica donde está nuestra porteria
  private Vec2 su_porteria; //Indica donde está la porteria del oponente
  private Vec2[] companieros; //Indica donde están mis compañeros de equipo
  private Vec2[] oponentes; //Indica donde están mis oponentes
  private Vec2 equipo_mas_cercano; //Indica donde está el compañero más cercano
  private Vec2 op_mas_cercano; //Indica donde está el oponente más cercano
  private Vec2 mas_cercano_balon; //Indica donde está el compañero más cercano al balón

  private Vec2 mover; //Mover en direccion mover.t con velocidad mover.r
  private boolean chutar; //Indica si se Intenta chutar

  //Indica en qué lado del campo se está actualmente.  -1 para oeste +1 para este
  private static int LADO;

  // Vector que apunta al jugador en curso
  private static final Vec2 YO = new Vec2(0, 0);

  // Radio del jugador
  private final double ROBOT_RADIUS = abstract_robot.RADIUS;

  private static final boolean DEBUG = false;

  /**
       Configura el control del sistema. Se llama una vez en la inicializacion.
   */
  public void configure() {
    tiempoActual = abstract_robot.getTime();
    if (abstract_robot.getOurGoal(tiempoActual).x < 0) {
      LADO = -1;
    }
    else {
      LADO = 1;

    }
    mover = new Vec2(0, 0);
    resultado = 0;
    tenemos_balon = false;
    balon_nuestro_campo = false;
    defendemos = false;
    goles_a_favor_ataque = 0;
    goles_a_favor_empate = 0;
    goles_en_contra_defensa = 0;
    estrategia = "empate";

    try
    {
      jess_engine = new Rete();
      jess_engine.executeCommand("(batch futbol.clp)");
      jess_engine.executeCommand("(reset)");
    }
    catch(JessException je){
     	System.out.println(je);
    }
  }

  /**
   * Este método es llamado en cada paso para permitir al control de sistema ejecutarse.
   */
  public int takeStep() {
    //Res.Resultado.Guardar(this,abstract_robot);
    update_env();
    seleccionarEstrategia();
    actualizarAccion();
    return CSSTAT_OK;
  }

  public void seleccionarEstrategia() {
    Vec2 closest = new Vec2(0, 0);

      try {
     
      jess_engine.reset();
      jess_engine.store("resultado",new Value(resultado, 4));
      jess_engine.store("tiempoActual",new Value(tiempoActual,4));
      jess_engine.store("goles_a_favor_ataque",new Value(goles_a_favor_ataque,4));
      jess_engine.store("goles_a_favor_empate",new Value(goles_a_favor_empate,4));
      jess_engine.store("defendemos",new Value(defendemos));
      jess_engine.store("goles_en_contra_defensa",new Value(goles_en_contra_defensa,4));

      jess_engine.run();
    }
    catch (jess.JessException e21) {
    }
    Value estrategia = jess_engine.fetch("estrategia");
   
    String est;
    if (estrategia == null)
       est = "Empate";
    else
       est = estrategia.toString();

    if (est.compareTo("Empate")==0) Empate();
    if (est.compareTo("Ataque")==0) Ataque();
    if (est.compareTo("Defensa")==0) Defensa();

    dorsal();
  }

  private void dorsal() {
    if (miNumero == 0) {
      abstract_robot.setDisplayString("iker");
    }
    else if (miNumero == 1) {
      abstract_robot.setDisplayString("zidane");
    }
    else if (miNumero == 2) {
      abstract_robot.setDisplayString("xavi");
    }
    else if (miNumero == 3) {
      abstract_robot.setDisplayString("caminero");
    }
    else if (miNumero == 4) {
      abstract_robot.setDisplayString("torres");
    }
  }

  private void Empate() {
    estrategia = "empate";

    if (miNumero == 0) {
      juega_portero();
    }
    else if (miNumero == 1) {
      juega_backup();
    }
    else if (miNumero == 2) {
      juega_offside();
    }
    else if (miNumero == 3) {
      juega_center();
    }
    else if (miNumero == 4) {
      conduce_balon();
    }
  }

  private void Ataque() {
    estrategia = "ataque";

    if (miNumero == 0) {
      juega_portero();
    }
    else if (miNumero == 1) {
      juega_backup();
    }
    else if (miNumero == 2) {
      juega_backup();
    }
    else if (miNumero == 3) {
      juega_center();
    }
    else if (miNumero == 4) {
      conduce_balon();
    }
  }

  private void Defensa() {
    estrategia = "defensa";

    if (miNumero == 0) {
      juega_portero();
    }
    else if (miNumero == 1) {
      juega_bloquear();
    }
    else if (miNumero == 2) {
      juega_backup();
    }
    else if (miNumero == 3) {
      juega_center();
    }
    else if (miNumero == 4) {
      conduce_balon();
    }
  }


  public int actualizarAccion() {
         // Establece la cabecera
         abstract_robot.setSteerHeading(tiempoActual, mover.t);

         // Establece la velocidad
    abstract_robot.setSpeed(tiempoActual, mover.r);

    // posibilidad de chutar
    if (chutar && abstract_robot.canKick(tiempoActual)) {
      abstract_robot.kick(tiempoActual);

    }
    return (CSSTAT_OK);
  }

  private void update_env() {
    Vec2 closest;
    Vec2 temp = new Vec2(0, 0);

    /*--- datos de mantenimiento ---*/
    // recoge el tiempo actual para timestamps
    tiempoActual = abstract_robot.getTime();

    // recoge el id del jugador
    miNumero = abstract_robot.getPlayerNumber(tiempoActual);

    /*--- Sensores---*/
    // recoge un vector al balón
    balon = abstract_robot.getBall(tiempoActual);

    //recoge respectivos vectores a nuestra porteria y a la del oponente
    nuestra_porteria = abstract_robot.getOurGoal(tiempoActual);
    su_porteria = abstract_robot.getOpponentsGoal(tiempoActual);

    // recoge una lista con las posiciones de nuestros compañeros
    companieros = abstract_robot.getTeammates(tiempoActual);

    //  recoge una lista con las posiciones de nuestros oponentes
    oponentes = abstract_robot.getOpponents(tiempoActual);

    // recoge datos de cercanía
    equipo_mas_cercano = closest_to(YO, companieros);
    op_mas_cercano = closest_to(YO, oponentes);

    closest = closest_to(balon, companieros);

    temp.sett(closest.t);
    temp.setr(closest.r);

    temp.sub(balon);

    if (temp.r > balon.r) {
      mas_cercano_balon = YO;
    }
    else {
      mas_cercano_balon = closest;

    }
    giro = abstract_robot.getSteerHeading(tiempoActual);

    /*--- Control del actuador por defecto ---*/
    //Establece datos de movimientos: giro y velocidad;
    mover.sett(0.0);
    mover.setr(0.0);

    // Establece el "chute"
    chutar = false;

    Vec2 nos = closest_to(balon, companieros);
    Vec2 vos = closest_to(balon, oponentes);
    nos.sub(balon);
    vos.sub(balon);
    tenemos_balon = (nos.r < vos.r) ? true : false;

    int hayGol = this.abstract_robot.getJustScored(tiempoActual);
    resultado += hayGol;
    if (hayGol == -1) {
      goles_en_contra_defensa += estrategia == "defensa" ? 1 : 0;
     
    }
    if (hayGol == 1) {
      goles_a_favor_ataque += estrategia == "ataque" ? 1 : 0;
      goles_a_favor_empate += estrategia == "empate" ? 1 : 0;
    }

    balon_nuestro_campo = balon.x * LADO >= 0 ? true : false;
    defendemos = (!tenemos_balon) && (balon_nuestro_campo);
  }

  private void juega_bloquear() {
    Vec2 delantero = closest_to(nuestra_porteria, oponentes);

    //busca el punto justo detrás del portero en el camino hacia su porteria
    balon.sub(delantero);
    balon.setr(ROBOT_RADIUS);
    balon.add(delantero);

    mover.sett(balon.t);
    mover.setr(1.0);

    // Si no se está bloqueando al portero  entonces no colisionar
    if (delantero != op_mas_cercano) {
      avoidcollision();
    }
  }

  private void juega_portero() {
    // Si el balón esta detrás  intentar chutar
    if (balon.x * LADO > 0) {
      mover.sett(balon.t);
      mover.setr(1.0);
      chutar = true;
    }

    //Si se está fuera del area  volver hacia la porteria
    else if ( (Math.abs(nuestra_porteria.x) > ROBOT_RADIUS * 1.4) ||
             (Math.abs(nuestra_porteria.y) > ROBOT_RADIUS * 4.25))

    {
      mover.sett(nuestra_porteria.t);
      mover.setr(1.0);
    }

    // posicionarse entre la pelota y la porteria
    else {
      if (balon.y > 0) {
        mover.sety(7);
      }
      else {
        mover.sety( -7);

      }
      mover.setx( (double) LADO);

      if (Math.abs(balon.y) < ROBOT_RADIUS * 0.15) {
        mover.setr(0.0);
      }
      else {
        mover.setr(1.0);
      }
    }
  }

  private void juega_offside() {
    //El portero del otro equipo es aquel más cercano a su porteria
    Vec2 portero = closest_to(su_porteria, oponentes);

    //busca el punto justo detrás del portero en el camino hacia su porteria
    su_porteria.sub(portero);
    su_porteria.setr(ROBOT_RADIUS);
    su_porteria.add(portero);

    mover.sett(su_porteria.t);
    mover.setr(1.0);

    // Si no se esta bloqueando al portero  no colisionar
    if (portero != op_mas_cercano) {
      avoidcollision();

    }
  }

  private void juega_backup() {

    Vec2 backup = new Vec2(0, 0);

    if (mas_cercano_balon == YO) {
      conduce_balon();
    }
    else
    // Si no se es el más cercano al balón, establecer una posicion de 3 veces el radio del robot tras el balón
    {
      backup.sett(balon.t);
      backup.setr(balon.r);
      backup.setx(backup.x + ROBOT_RADIUS * 3 * LADO);
      get_behind(backup, su_porteria);
      avoidcollision();
    }

  }

  private void juega_center() {
    Vec2 center = new Vec2(0, 0);

    // encontrar el centro
    center = abstract_robot.getPosition(tiempoActual);
    center.setr( -center.r);

    if (mas_cercano_balon == YO) {
      conduce_balon();
    }
    else
    // Si no se es el más cercano al balón quedarse en el centro y esperar jugada rapida
    {
      get_behind(center, su_porteria);
      avoidcollision();
    }
  }

  private void conduce_balon() {
    //Si se está detrás del balon (orientado hacia la porteria del oponente) dirigirse con el hacia ella
    if (behind_point(balon, su_porteria) && balon.t < ROBOT_RADIUS * 4) {
      mover.sett(su_porteria.t);
      mover.setr(1.0);

      // Si se está en un radio de 15 veces el radio del robot lejano de la porteria intentar chutar

      if ( (Math.abs(giro - su_porteria.t) < Math.PI / 8) &&
          (su_porteria.r < ROBOT_RADIUS * 15)) {
        chutar = true;
      }
    }
    else
    // en cualquier otro caso permanecer tras el balón y evitar colisionar con otros jugadores
    {
      get_behind(balon, su_porteria);
      avoidcollision();
    }
  }


  private Vec2 closest_to(Vec2 point, Vec2[] objects) {
    double dist = 9999;
    Vec2 result = new Vec2(0, 0);
    Vec2 temp = new Vec2(0, 0);

    for (int i = 0; i < objects.length; i++) {

      // encontrar la distancia desde el punto al objeto actual

      temp.sett(objects[i].t);
      temp.setr(objects[i].r);
      temp.sub(point);

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
    double point_LADO = 0;

    // Encontrar un vector desde el punto, lejos de la orientacion donde se quiere estar

    behind_point.sett(orient.t);
    behind_point.setr(orient.r);

    behind_point.sub(point);
    behind_point.setr( -ROBOT_RADIUS * 1.8);

    // determinar si se está detras del objeto con respecto a la orientacion
    behind = Math.cos(Math.abs(point.t - behind_point.t));

    //determinar si se esta a mano derecha o izquierda con respecto a la orientacion
    point_LADO = Math.sin(Math.abs(point.t - behind_point.t));

    // Si se está enfrente
    if (behind > 0) {
      // Hacer del punto de atras el punto más cercano mediante rotacion (depende del lado en el que se esté: mano derecha o izquierda)
      if (point_LADO > 0) {
        behind_point.sett(behind_point.t + Math.PI / 2);
      }
      else {
        behind_point.sett(behind_point.t - Math.PI / 2);
      }
    }

    // moverse hacia el punto de atras
    mover.sett(point.t);
    mover.setr(point.r);
    mover.add(behind_point);

    mover.setr(1.0);

  }

  private boolean behind_point(Vec2 point, Vec2 orient) {

    // Se está detrás de un obejto(respecto a la orientacion)
    // Si tu posicion relativa al punto y la orientacion son aproximadamente iguales
    if (Math.abs(point.t - orient.t) < Math.PI / 10) {
      return true;
    }
    else {
      return false;
    }
  }

  private void avoidcollision() {

    // Mantenerse lejos del camino de los compañeros
    // Si mi comapñero más cercano está demasiado cerca, entonces moverse
    if (equipo_mas_cercano.r < ROBOT_RADIUS * 1.4) {
      mover.setx( -equipo_mas_cercano.x);
      mover.sety( -equipo_mas_cercano.y);
      mover.setr(1.0);
    }

    //Si el oponente más cercano está demasiado cerca, alejarse e intentar dar la vuelta
    else if (op_mas_cercano.r < ROBOT_RADIUS * 1.4) {
      mover.setx( -op_mas_cercano.x);
      mover.sety( -op_mas_cercano.y);
      mover.setr(1.0);
    }

  }

  private void debug(String message) {
    if (DEBUG) {
      System.out.println(miNumero + ":  " + message);
    }
  }
}