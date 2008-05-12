

/**
 * <p>Title: </p>
 * MODIFICADO: Antiguo Equipo Katenacho
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import java.lang.Math;
import java.util.Enumeration;
import EDU.gatech.cc.is.util.Vec2;
import EDU.gatech.cc.is.abstractrobot.*;
import EDU.gatech.cc.is.communication.*;

public class Profes2
    extends ControlSystemSS {

  //Constantes para dimensiones

  private final double ANCHO = 1.43 * 2;
  private final double ALTO = 0.8625 * 2;
  private final double DIST_TENGO_PELOTA = 0.1;
  private final double RADIO_ROBOT = abstract_robot.RADIUS; //0.06;
  private final double CERCANIA = 0.05;
  private final double RADIO_PELOTA = 0.01;
  private final double TAM_PORTERIA = 0.4;

  private long curr_time;
  private Vec2 me, pelota, pelotaAbs, miPorteria, suPorteria, centroCampo;
  private Vec2[] partners, enemies;
  private Enumeration messagesin;
  int mynum;
  private int estadoActual = 3;
  private int estadoPortero = 0;
  private int estadoDefensa = 0;
  private int portero_o_defensa = 1; //si es uno juega de defensa,sino juega de portero
  private boolean disparar = false; //indica si podemos disparar o no
  private double angulo; //direccion en la que estoy apuntando

  //variable que indica donde debemos tirar, 1 es abajo y -1 arriba
  private int lugarTiro=1;

  private static final Vec2 yo = new Vec2(0, 0); //este vector apunta a mi

  private Vec2 closest_team; //Teammate?
  private Vec2 closest_opp; //Opponent?
  private Vec2 closest_to_ball; //Teammate to the Ball?
  private Vec2 move; //Move in move.t direction,with speed move.r

  private static int CAMPO;
  private String mensaje = "";
  static final int
      TIRO = 0,
      AVANZAR = 1,//ir hacia la porteria contraria
      DETRAS_PELOTA = 2,//colocarse detrás de la pelota respecto a la porteria contraria
      //DEFENDIENDO = 3,
      EN_POSICION_DELANTERO = 3, //se coloca cerca del centro del campo
      EN_POSICION_DEFENSA = 4, //un defensa se coloca en la posición que le corresponde
      //cuando no tiene que hacer nada
      DESPEJAR = 5,//disparar=true para despejar el balón
      CUBRIR_DELANTERO = 6,//un defensa cubre a su delantero que entre en nuestro campo
      BUSCA_PELOTA=7,
      VOLVER_PORTERIA = 8,
      MOVER_PORTERO=9;

  static final String mensajesEstado[] = {
      " VA A DISPARAR!",
      " está avanzando hacia la porteria contraria.",
      " se coloca detrás de la pelota.",
      " el delantero se coloca en el centro del campo.",
      " el defensa se coloca en la frontal del área.",
      " despejamos el balón.",
      " delantero cubierto.",
      " el centrocampista busca la pelota.",
      "tengo la pelota.",
      "vuelve a porteria.",
      "se mueve el portero."
       };

  public Profes2() {
  }

  public void escribeEstado() {
    System.out.println(abstract_robot.getID() + " " +
                       mensajesEstado[estadoActual]);
  }

  public int TakeStep() {
    //Res.Resultado.Guardar(this,abstract_robot);
    move = new Vec2(0, 0);
    if (abstract_robot.getOurGoal(curr_time).x < 0) {
      CAMPO = -1;
    }
    else {
      CAMPO = 1;

    }
    juego2();
    return CSSTAT_OK;
  }

  public void inicializar2() {
    curr_time = abstract_robot.getTime();

    me = abstract_robot.getPosition(curr_time);

    centroCampo = new Vec2( -me.x, -me.y);

    mynum = abstract_robot.getPlayerNumber(curr_time);
    pelota = abstract_robot.getBall(curr_time);

    partners = abstract_robot.getTeammates(curr_time);
    enemies = abstract_robot.getOpponents(curr_time);

    //Vec2 compMasCercano=dameCompMasCercano();
    //Vec2 rivalMasCercano=dameRivalMasCercano();

    //posiciones respecto al origen que está en la parte inferior izquierda
    miPorteria = abstract_robot.getOurGoal(curr_time);
    suPorteria = abstract_robot.getOpponentsGoal(curr_time);

    closest_team = closest_to(me, partners);
    closest_opp = closest_to(me, enemies);
    Vec2 closest = closest_to(pelota, partners);

    Vec2 temp = new Vec2(0, 0);
    temp.sett(closest.t);
    temp.setr(closest.r);

    temp.sub(pelota);

    if (temp.r > pelota.r) {
      closest_to_ball = me;
    }
    else {
      closest_to_ball = closest;

    }
    angulo = abstract_robot.getSteerHeading(curr_time);

    // if (CAMPO==1)
    move.sett(0.0);
    move.setr(0.0);
  }

  public void juego2() {
    inicializar2();

    //estrategia para el portero
    if (mynum == 0) {
      //if (!delantero_cubre_a_portero())
      estrategiaPortero();
      abstract_robot.setDisplayString(mensajesEstado[estadoActual]);
     /* else
        estrategiaDefensa();
      abstract_robot.setDisplayString(mensaje);
    }*/
    }
    //estrategia para un defensa
    else if (mynum == 1) {
      /* if (delantero_cubre_a_portero()){
             estrategiaPortero();
             System.out.println("Defensa pasa a cubrir portero");
             abstract_robot.setDisplayString("Defensa cambia a portero");
                           }
         else{*/
      estrategiaDefensa();

      abstract_robot.setDisplayString(mensajesEstado[estadoActual]);
      // }
    }

    //estrategia para un defensa
    else if (mynum == 2) {
      //estrategiaMedio();
      estrategiaDefensa();
      //estrategiaKaka();
      abstract_robot.setDisplayString(mensajesEstado[estadoActual]);
    }

    //estrategia para el delantero centro
    else if (mynum == 3) {
      //drive_ball();
      estrategiaMedio();
      //estrategiaKaka();
      //estrategiaCubrirPortero();
      //estrategiaMedio();
      abstract_robot.setDisplayString(mensajesEstado[estadoActual]);
    }

    //estrategia para el centrocampista
    else {
      //estrategiaInzaghi();
      //estrategiaDelantero();
      //delanteroBloqueante();
      estrategiaMedio();
      abstract_robot.setDisplayString(mensajesEstado[estadoActual]);
    }

    /*--- Send commands to actuators ---*/
    // set the heading
    // if (CAMPO==1)
    abstract_robot.setSteerHeading(curr_time, move.t);
    // else
    //   abstract_robot.setSteerHeading(curr_time, -move.t);

    // set the speed
    abstract_robot.setSpeed(curr_time, move.r);

    if ( (disparar == false) && abstract_robot.canKick(curr_time)) {
      me.sett(pelota.t + Math.PI);
    }
    //si podemos disparar
    if ( (disparar == true) & abstract_robot.canKick(curr_time)) {
      abstract_robot.kick(curr_time);

    }
    disparar = false;
    //estadoActual = DEFENDIENDO;
  }

 private void estrategiaKaka(){
      if (tengoBalon()) {
        if (behind_point(pelota, suPorteria) && pelota.t < RADIO_ROBOT * 4) {
          if ((Math.abs(angulo - suPorteria.t) < Math.PI / 8) &&
              (suPorteria.r < RADIO_ROBOT * 15)){
            if (abstract_robot.canKick(curr_time)) {
              //Mejorar sitio tirar
              move = calculaTiro();
              disparar = true;

  }
            else {
              //mejorar sitio adónde ir
              Vec2 antes_pelota = new Vec2(pelota.x - CAMPO*0.1, pelota.y);
              get_behind(antes_pelota, suPorteria);
              //move.sett(suPorteria.t);
              //move.setr(1.0);
            }
          }
          else{
            //Se escora a la izquierda
            move.sett(-Math.PI/4);
            move.setr(1);
          }
        }

      else {
          //mejorar sitio adónde ir
          //System.out.println("Voy antes de la pelota");
          Vec2 antes_pelota = new Vec2(pelota.x - CAMPO*0.1, pelota.y);
          get_behind(antes_pelota, suPorteria);
          move.setr(1);
          /*move.sett(suPorteria.t);
                   move.setr(1.0);*/
        }
      }
      else {
        if (tenemosPelota()) {
          //Nos situamos delante del centro del campo
          Vec2 delanteCentroCampo = new Vec2(suPorteria.x-0.8,0);
          move.sett(delanteCentroCampo.t);
          move.setr(1);
        }
        else {
          if (Math.abs(closest_to_ball.r - pelota.r) < 0.1) {
            move.sett(pelota.t);
            move.setr(1);
            //System.out.println("Soy el más cercano a la pelota");
          }
          else {
            estrategiaDefensa();

          }
        }
      }
    }


  private void estrategiaPortero() {
    //System.out.println("Miportero.y="+damePos(me).y);
    // si tengo la pelota detrás,intento sacarla
    if (pelota.x * CAMPO > 0)
    //if ((((CAMPO==-1) & (pelota.x<0.2) & (me.x<0.2)) || ((CAMPO==1) & (pelota.x>-0.2) & (me.x>-0.2))))
    {
      move.sett(pelota.t);
      move.setr(1.0);
      disparar = true;
      //mensaje = "Despejar";
      estadoActual=DESPEJAR;
    }

    // si estoy fuera del area vuelvo a ella
    else if ( (Math.abs(miPorteria.x) > RADIO_ROBOT * 1.4) ||
             (Math.abs(miPorteria.y) > RADIO_ROBOT * 4.25))

    {
      //if (!delantero_cubre_a_portero()){
        move.sett(miPorteria.t);
        move.setr(1.0);
      //}else
      //  evitarColision();
      //mensaje = "Volver porteria";
      estadoActual=VOLVER_PORTERIA;
    }

    // se queda entre la pelota y la porteria
    else {
      if (pelota.y > 0) {
        move.sety(7);
      }
      else {
        move.sety( -7);

      }
      move.setx( (double) CAMPO);

      if (Math.abs(pelota.y) < RADIO_ROBOT * 0.15) {
        move.setr(0.0);
      }
      else {
        move.setr(1.0);
        //evitarColision();
      }
      estadoActual=MOVER_PORTERO;
    }
  } //fin estrategiaPortero

  private void estrategiaPortero1() {
    //si no ponemos esto, al jugar en el campo izquierdo empieza mirando hacia nuestra porteria
    if (estadoPortero == 0) {
      move.sett(miPorteria.t);
      move.setr(1.0);
      mensaje = "Inicial,Detras=" + estoy_Detras_Pelota() + " balon=" +
          tengoBalon();
    }
    else

    //si la pelota está cerca de la frontal del área sale a por ella para despejarla
    if ( ( ( (CAMPO == -1) & (pelota.x < 0.2) & (me.x < 0.2)) ||
          ( (CAMPO == 1) & (pelota.x > -0.2) & (me.x > -0.2)))) {
      //if( pelota.x * CAMPO > 0){
      //System.out.println("estado = 1");
      move.sett(pelota.t);
      move.setr(1.0);
      mensaje = "Salgo a despejar el balón";
      //if ((behind_point(me,pelota)))
      if (tengoBalon() & (estoy_Detras_Pelota())) {
        disparar = true;
      }
      else {
        disparar = false;
        // else
        // disparar=false;
        //System.out.println("ESTOY DISPARANDO.....................");
      }
      estadoPortero = 1; //significa que esta saliendo a despejar el balón
    }
    else //condiciones para volver a la porteria

    //condición para el campo izquierdo    || condición para el campo derecho
    if ( ( (me.x != -1.2) & (estadoPortero == 1)) ||
        ( (me.x != 1.2) & (estadoPortero == 1)) || (estadoPortero == 2)) { //estadoPortero=0 cuando está en su sitio
      //if (pelota.x*CAMPO>0){
      if (estoy_Detras_Pelota() & !tengoBalon()) {
        move.sett(miPorteria.t);
        move.setr(1.0);
        estadoPortero = 2; //vuelve al centro de la porteria
        disparar = false;
        mensaje = "Vuelvo a la porteria";
      }
      else {
        get_behind(pelota, miPorteria);
        evitarColision();
        //mensaje="Debo volver a porteria pero me pongo detrás de la pelota "+tengoBalon();
        mensaje = "Detras=" + estoy_Detras_Pelota() + " balon=" + tengoBalon();
      }
    }
    /* if ((estadoPortero==2) ){// || (me.x>0.2)){
       //si estoy volviendo a porteria,me tengo que asegurar de no llevar el balón
       //con esta condición evitamos los goles del portero en propia puerta
      //if (behind_point( pelota, miPorteria))  {//si no estoy detrás de la pelota voy hacia la porteria
        if(estoyDetrasPelota()){
        move.sett(miPorteria.t);
        move.setr(1.0);
        evitarColision();
      }
      else {
          get_behind( pelota, miPorteria);
          evitarColision();
        }
     }*/
    //fin estadoPortero==2
    //condición para dejar de volver hacia la porteria
    if ( ( (Math.abs(me.x) + RADIO_ROBOT) >= 1.29) &
        ( (me.y <= 0.1) && (me.y > -0.1))) {
      estadoPortero = 3;
      //evitarColision();
      mensaje = "Dejo de volver a porteria";
    }

  }

  //devuelve si el jugador esta detras de la pelota
  private boolean estoy_Detras_Pelota() {
    boolean detras = false;
    if ( (miPorteria.x >= 0) & (pelota.x < me.x)) {
      detras = true;
    }
    else
    if ( (miPorteria.x < 0) & (pelota.x > me.x)) {
      detras = true;

    }
    return detras;
  }





  public boolean meChocoConUnoDeMiEquipo() {
    boolean choque = false;
    //System.out.println("distancia="+distancia(closest_team));
    if (distancia(closest_team) < 0.85) {
      choque = true;
    }
    return choque;
  }

  //defendemos si estamos fuera del area
   public boolean condicionDefensa() {
     return ( ( (me.x > -1.0) & (CAMPO == -1)) || ( (me.x > 1.0) & (CAMPO == -1)));
   }

   private void delanteroBloqueante(){
       if (tengoBalon()) {
         if (behind_point(pelota, suPorteria) && pelota.t < RADIO_ROBOT * 4) {
           if ((Math.abs(angulo - suPorteria.t) < Math.PI / 8) &&
               (suPorteria.r < RADIO_ROBOT * 15)){
             if (abstract_robot.canKick(curr_time)) {
               //Mejorar sitio tirar
               move = calculaTiro();
               disparar = true;
        }
             else {
               //mejorar sitio adónde ir
               Vec2 antes_pelota = new Vec2(pelota.x - CAMPO*0.1, pelota.y);
               get_behind(antes_pelota, suPorteria);
               //move.sett(suPorteria.t);
               //move.setr(1.0);
             }
           }
           else{
             if (pelota.quadrant()==0){
               move.sett(Math.PI/4);
             }else{
               move.sett (-Math.PI/4);
             }
             move.setr(1);
           }
         }
         else {
           //mejorar sitio adónde ir
           if (pelota.quadrant()==0){
               move.sett(Math.PI/4);
             }else{
               move.sett(-Math.PI/4);
             }
             move.setr(1);
         }
       }
       else {
         if (closest_opp.r<0.05 && suPorteria.r>=0.2){
           //estrategiaKaka();
           estrategiaMedio();
           System.out.println("Estrategia medio");
         }
         else{
           Vec2 suPortero = closest_to(suPorteria,enemies);
           move.sett(suPortero.t);
           move.setr(1);
         }
       }
     }


   private void estrategiaInzaghi(){
       if (tengoBalon()) {
         if (behind_point(pelota, suPorteria) && pelota.t < RADIO_ROBOT * 4) {
           if ((Math.abs(angulo - suPorteria.t) < Math.PI / 8) &&
               (suPorteria.r < RADIO_ROBOT * 15)){
             if (abstract_robot.canKick(curr_time)) {
               //Mejorar sitio tirar
               move = calculaTiro();
               disparar = true;
      }
             else {
               //mejorar sitio adónde ir
               Vec2 antes_pelota = new Vec2(pelota.x - CAMPO*0.1, pelota.y);
               get_behind(antes_pelota, suPorteria);
               //move.sett(suPorteria.t);
               //move.setr(1.0);
             }
           }
           else{
             if (pelota.quadrant()==0){
               move.sett(Math.PI/4);
             }else{
               move.sett(- Math.PI/4);
             }
             move.setr(1);
           }
         }
         else {
           //mejorar sitio adónde ir
           if (pelota.quadrant()==0){
               move.sett(Math.PI/4);
             }else{
               move.sett(-Math.PI/4);
             }
             move.setr(1);
         }
       }
       else {
         if (tenemosPelota()) {
           Vec2 opuesto;
           if (pelota.y>=0){
             //Voy al cuadrante
             opuesto = new Vec2(-me.x + 0.8,-me.y - 0.3);
           }else{
             opuesto = new Vec2(-me.x + 0.8,-me.y + 0.3);
           }
           //get_behind(me,opuesto);
           move.sett(opuesto.t);
           move.setr(1);
         }
         else {
           if (Math.abs(closest_to_ball.r - pelota.r) < 0.1) {
             move.sett(pelota.t);
             move.setr(1);
             //System.out.println("Soy el m
           }
           else {
             Vec2 opuesto;
             if (pelota.y >= 0) {
               //Voy al cuadrante 3
               opuesto = new Vec2( -me.x + 0.8, -me.y - 0.3);
             }
             else {
               opuesto = new Vec2( -me.x + 0.8, -me.y + 0.3);
             }
             //get_behind(me,opuesto);
             move.sett(opuesto.t);
             move.setr(1);
           }

         }
       }
     }


  private void estrategiaDefensa() {
    //cuando pelota.x es negativo,es que se encuentra detrás del jugador
    //Vec2 cercano_opp = closest_to( me, enemies);
    Vec2 closest_opp_pelota = closest_to(pelota, enemies);
    Vec2 closest_team_goal = closest_to(suPorteria, partners);
    //Vec2 closest_defensa_delantero=closest_to(closest_opp_pelota,partners);
    Vec2 closest_delantero = closest_to(miPorteria, enemies);

    //si la pelota está en nuestro campo
    Vec2 closest_pelota = dameMasCercanoPelota(partners);
    //Vec2 centro= new Vec2 (centroCampo.x,centroCampo.y);

    if (vectorEnNuestroCampo(pelota) & (estadoDefensa == 0) & condicionDefensa() ) {
      if (behind_point(pelota, suPorteria)) {
        move.sett(pelota.t);
        //move.sett(centro.t);
        move.setr(1.0);
        estadoDefensa = 1;
        estadoActual = TIRO; //para que la despeje
        disparar = true;
        evitarColision();
        //mensaje = "Despejar";
        estadoActual=DESPEJAR;
      }
      else {
        //mensaje = "Vuelta a despejar";
        get_behind(pelota, suPorteria);
        evitarColision();
        estadoActual=DETRAS_PELOTA;
        // estadoDefensa=1;
      }
    }
    else //con esta condición pasamos a cubrir a su delantero
    if (vectorEnNuestroCampo(pelota) & (estadoDefensa == 0) & !condicionDefensa()) {
      move.sett(closest_delantero.t);
      move.setr(1.0);
      estadoDefensa = 2; //probar a poner un 1
      //mensaje = "Cubre delantero";
      estadoActual=CUBRIR_DELANTERO;
    }
    else
    if ( (estadoDefensa == 1) & (vectorEnNuestroCampo(pelota))) {
      //if (tengoBalon()){
     // if ((closest_pelota==me) & (behind_point(pelota, suPorteria))){
      //if (behind_point( pelota, suPorteria)){
      move.sett(closest_team_goal.t);
      move.setr(1.0);
      estadoDefensa = 2; // antes estaba a 2
      //estadoActual = TIRO; //para que la despeje
      disparar = true;
      estadoActual=DESPEJAR;
      //mensaje = "Paseeee";
      //System.out.println("despejamos");

    }
    else /*if ( (estadoDefensa == 2) || (estadoDefensa == 1)) { //para que se pare
      Vec2 paloArriba = new Vec2(miPorteria.x, miPorteria.y + TAM_PORTERIA / 2);
      Vec2 paloAbajo = new Vec2(miPorteria.x, miPorteria.y - TAM_PORTERIA / 2);
      Vec2 aux;
      //si jugamos en el campo izquierdo los colocamos asi
      if (CAMPO==-1){
        if (mynum == 2) {
          aux = new Vec2(paloArriba.x + 0.2, paloArriba.y);
        }
        else {
          aux = new Vec2(paloAbajo.x + 0.2, paloAbajo.y);
        }
      }//si jugamos en el campo derecho los colocamos asi
      else
      if (mynum == 2) {
       aux = new Vec2(CAMPO*centroCampo.x + 0.6, centroCampo.y + 0.2);
     }
     else {

       //if (mynum==2)
       aux = new Vec2(CAMPO*centroCampo.x + 0.6, centroCampo.y - 0.2);
     }


      move.sett(aux.t);
      move.setr(1.0);
      estadoDefensa = 0;
      estadoActual = EN_POSICION_DEFENSA;
    }
    else*/ if (!vectorEnNuestroCampo(me)) { //si me voy fuera de mi campo vuelvo hacia mi porteria
      /*move.sett(miPorteria.t + Math.PI / 10);
      move.setx( -1.0);
      move.setr(1.0);*/
      Vec2 nuevaposicion=new Vec2(miPorteria.x,miPorteria.y);
      move.sett(miPorteria.t);
      move.setr(1.0);
      estadoDefensa = 0;
    }
    else

    //va a por el jugador que está más cercano de la pelota cuando entra en nuestro campo
    /*if (vectorEnNuestroCampo(closest_opp_pelota) & (vectorEnNuestroCampo(pelota))) {
      //get_behind( pelota,delanteroEnemigo);//con esta condición hacemos mucho catenachio
      evitarColision();
      estadoDefensa = 0;
      estadoActual=DESCONOCIDO;
      System.out.println("entraaaaaaaaaaaaaaaaaaaa");
    }
    else */{//colocamos a los dos defensas en dos posiciones determinadas
      Vec2 paloArriba = new Vec2(miPorteria.x, miPorteria.y + TAM_PORTERIA / 2);
      Vec2 paloAbajo = new Vec2(miPorteria.x, miPorteria.y - TAM_PORTERIA / 2);
      Vec2 aux;
      //si jugamos en el campo izquierdo los colocamos asi
      if (CAMPO==-1){
        if (mynum == 2) {
          aux = new Vec2(paloArriba.x + 0.6, paloArriba.y);
        }
        else
       // if (mynum==1){
          aux = new Vec2(paloAbajo.x + 0.6, paloAbajo.y);
        /*}//si hacemos que el portero juegue de defensa cuando le cubran hacemos esto
          else
             aux = new Vec2(paloAbajo.x + 0.6, centroCampo.y);*/


      }
      else//si jugamos en el campo derecho los colocamos asi
      if (mynum == 2) {
       aux = new Vec2(CAMPO*centroCampo.x + 0.6, centroCampo.y + 0.2);
     }
     else {

       //if (mynum==2)
       aux = new Vec2(CAMPO*centroCampo.x + 0.6, centroCampo.y - 0.2);
     }
      move.sett(aux.t);
      move.setr(1.0);
      estadoDefensa = 0;
      //evitarColision();
      //mensaje = "En Posición";
      estadoActual = EN_POSICION_DEFENSA;
    }


  } //fin estrategia defensa

  //devuelve si las coordenadas y de un jugador están dentro de las y de la porteria
  private boolean estaDentroPorteriaPortero() {
    boolean esta = false;
    Vec2 portero_enemigo = closest_to(suPorteria, enemies);
    Vec2 centro = new Vec2(0, 0);

    Vec2 aux = (Vec2) me.clone();

    double yPortero = Math.abs(portero_enemigo.y - centro.y);

    //Vec2 jug_abs=damePos(jugador);
    //System.out.println("jug_abs.y="+jug_abs.y);
    /*System.out.println("portero_enemigo.y="+yPortero);
       System.out.println("limite superior ="+((ALTO+TAM_PORTERIA)/2));
       System.out.println("limite inferior ="+((ALTO-TAM_PORTERIA)/2));*/

    if ( (portero_enemigo.y <= (ALTO + TAM_PORTERIA) / 2) &&
        (portero_enemigo.y >= (ALTO - TAM_PORTERIA) / 2)) {
      esta = true;

    }
    return esta;
  }

  private double anguloTiro() {
    double anguloTiro = 0;
    //obtenemos el jugador más cercano a su porteria
    Vec2 portero_enemigo = closest_to(suPorteria, enemies);
    Vec2 absoluta = damePos(me);
    //System.out.println("portero.y="+portero_enemigo.y);
    System.out.println("damePos(portero=.y=" + damePos(portero_enemigo).y);
    System.out.println("absoluta.y=" + absoluta.y);
    //si el portero está detrás mía, es que nadie me molesta y tiro directamente a puerta
    if (behind_point(portero_enemigo, me)) {
      anguloTiro = suPorteria.t;
    }
    //si alguien me molesta, pues desplazamos el ángulo de tiro
    //primero vemos si el portero está fuera de la porteria
    else if (estaDentroPorteriaPortero()) {
      anguloTiro = suPorteria.t;
      System.out.println("esta dentro porteria");
    }
    //si el portero está entre los palos
    /* else if (!estaDentroPorteriaPortero()){
       anguloTiro=suPorteria.t;
       System.out.println("fuera porteria");
     }*/
    System.out.println("");

    return anguloTiro;
  }

  //calcular una posición para tirar a puerta
  private Vec2 calcularTiro() {
    Vec2 kick_pos = new Vec2(pelota.x, pelota.y);
    Vec2 off_goal = new Vec2(pelota.x, pelota.y + (RADIO_ROBOT * 1));
    kick_pos.sub(off_goal);
    kick_pos.setr(RADIO_ROBOT);
    kick_pos.add(pelota);
    return kick_pos;
  }

  private Vec2 calculaTiro(){
      //Tiramos al sitio más alejado del portero
      Vec2 suPortero = closest_to(suPorteria,enemies);
      Vec2 tiro = new Vec2(suPorteria.x,0);
      Vec2 paloArriba = new Vec2(suPorteria.x, suPorteria.y + TAM_PORTERIA / 2);
      Vec2 paloAbajo = new Vec2(suPorteria.x, suPorteria.y - TAM_PORTERIA / 2);
      if (suPortero.y>0){
        tiro.sety(paloAbajo.y);
       //tiro.rotate  (-me.t);
      }
      else{
        tiro.sety(paloArriba.y);
        //tiro.rotate(-me.t);
      }
      //tiro.sett(tiro.t - me.t);
      //tiro.sett(Math.PI/2-me.t);
      tiro.setr(1.0);
      //System.out.println("Tiro en calculaTiro-> " + tiro.toString());
      return tiro;
    }

    private Vec2 calculaTiro3() {
        //Tiramos al sitio más alejado del portero
        Vec2 suPortero = closest_to(suPorteria, enemies);
        Vec2 tiro = new Vec2(suPorteria.x, 0);
        Vec2 paloArriba = new Vec2(suPorteria.x, TAM_PORTERIA / 2);
        Vec2 paloAbajo = new Vec2(suPorteria.x, TAM_PORTERIA / 2);
        paloArriba.sub(me);
        paloAbajo.sub(me);
        if (suPortero.y >= 0) {
          //tiro.sety(paloAbajo.y);
          //tiro.rotate(-me.t);
          tiro.sett( -getAngulo(me, paloAbajo));
        }
        else {
          //tiro.sety(paloArriba.y);
          //tiro.rotate(-me.t);
          tiro.sett(getAngulo(me, paloArriba));
        }
        //tiro.sett(tiro.t - me.t);
        //tiro.sett(Math.PI/2-me.t);
        //tiro.setr(1.0);
        //System.out.println("Tiro en calculaTiro-> " + tiro.toString());
        return tiro;
      }

      private double getAngulo(Vec2 v1, Vec2 v2) {
          //Devolvemos el ángulo que forman v1 y v2;
          double prodEsc = v1.x * v2.x + v1.y * v2.y;
          double prodMod = v1.r * v2.r;
          //System.out.println(prodEsc/prodMod);
          double coc = prodEsc / prodMod;
          if (coc > 1) {
            return Math.PI / 2;
          }
          else {
            double ang = Math.acos(coc);
            //System.out.println("ang->" + ang);
            return ang;
          }
        }


  //me devuelve el jugador más cercano a la pelota de un equipo
  private Vec2 dameMasCercanoPelota(Vec2[] equipo) {
    Vec2 closest = closest_to(pelota, equipo);
    Vec2 aux = new Vec2(0, 0);
    Vec2 closest_pelota;
    aux.sett(closest.t);
    aux.setr(closest.r);
    aux.sub(pelota);
    if (aux.r > pelota.r) {
      closest_pelota = yo;
    }
    else {
      closest_pelota = closest;
    }
    return closest_pelota;
  }

  private void estrategiaCubrirPortero(){

                  // the other team's goalie is whoever is closest to the goal
                  Vec2 goalie = closest_to( suPorteria, enemies);

                  // find the point just behind the "goalie"
                  // in the way of their goal
                  suPorteria.sub( goalie);
                  suPorteria.setr( RADIO_ROBOT );
                  suPorteria.add( goalie);

                  move.sett( suPorteria.t);
                  move.setr( 1.0);

                  // if you aren't blocking the "goalie" then don't collide
                  if( goalie != closest_opp)
                          evitarColision();


  }


  private void intentaTirarAPuerta(){
       if (behind_point(pelota, suPorteria) && pelota.t < RADIO_ROBOT * 4) {
             //si sitioAnterior es 1 tiramos arriba,sino tiramos abajo
             Vec2 sitioTirar= new Vec2(suPorteria.x,suPorteria.y + lugarTiro*0.3);
             //cambiamos el lugar de tiro para la siguiente vez, esto sirve si tiramos
             //una vez y recibimos el rebote,al cambiar el tiro, al portero
             //le costará mas llegar a donde va el balón
            lugarTiro=-1*lugarTiro;
            move.sett(sitioTirar.t);
            //move.sett(suPorteria.t);
            //Vec2 direccion=posicionTiro();
            //move.sett(direccion.t);
            move.setr(1.0);

             //si estamos cerca de la porteria tiramos
             if ( (Math.abs(angulo - suPorteria.t) < Math.PI / 8) &&
                 (suPorteria.r < RADIO_ROBOT * 13)) {
               mensaje = "Voy a disparar";
               disparar = true;
               estadoActual=TIRO;
               /*Vec2 direccion=posicionTiro();
               move.sett(direccion.t);
               move.setr(1.0);*/
             }
           }
           else {
             // colocarse detrás de la pelota y evitar la colisión con otros jugadores.
             get_behind(pelota, suPorteria);
             evitarColision();
             mensaje = "Detrás pelota";
             estadoActual=DETRAS_PELOTA;
           }
   }


  //el centrocampista si es el más cercano a la pelota,juega de delantero
     //sino va a por la pelota
     private void estrategiaMedio() {
       Vec2 closest_pelota = dameMasCercanoPelota(partners);
       //si es el más cercano a la pelota,juega de delantero
       if (closest_pelota == yo) {
         estrategiaDelantero();
       }
       else  //si la pelota no está en nuestro campo, vuelve a una posición dada
         if (!tenemosPelota()){
         //Vec2 mediapunta= new Vec2(centroCampo.x+0.3,centroCampo.y-0.2);
         //evitarColision();
         move.sett(pelota.t);
         move.setr(1.0);
         evitarColision();
         estadoActual=BUSCA_PELOTA;
       } //
       // else //si la pelota está en nuestro campo juega de defensa
       // estrategiaDefensa();
       else
         {Vec2 nuevaPosicion = posicionMedioDerecho();
          move.sett(nuevaPosicion.t);
          move.setr(1.0);
}
     } //estrategiaMedio

     private Vec2 posicionMedioDerecho() {
               Vec2 posicion;
               Vec2 posPelota = new Vec2(pelota.x, pelota.y);
               posPelota.sub(suPorteria);
               posPelota.setr(RADIO_ROBOT * 5);
               posPelota.add(pelota);
               Vec2 otraPos = new Vec2(posPelota.x, posPelota.y + 1.525 / 4);
               if (pelota.r > RADIO_ROBOT * 5)
                 posicion = otraPos;
               else
                 posicion = pelota;

               Vec2 lejos = new Vec2(closest_team.x,closest_team.y);
               lejos.sett(lejos.t + Math.PI);
               //si estamos cerca de un compañero, nos alejamos de él
               if (closest_team.r < abstract_robot.RADIUS * 2)
                 posicion = lejos;

                 return posicion;
             }


  private double distancia(Vec2 jugador) {
    double dist = 0;
    if (jugador != null) {
      double x = jugador.x - me.x;
      double y = jugador.y - me.y;
      dist = Math.sqrt( (x * x) + (y * y));
    }
    return dist;
  }



private Vec2 posicionTiro(){
  Vec2 suPortero=closest_to(suPorteria,enemies);
  Vec2 nuevaposicion=new Vec2(suPortero.x,suPortero.y+0.2);
  return nuevaposicion;

  }

    private void estrategiaDelantero() {
    // if i'm behind the ball (oriented toward the goal) then
    // start charging the goal
    //double angTiro=anguloTiro();
    Vec2 closest_pelota = dameMasCercanoPelota(partners);
    if (closest_pelota == yo) {
    //if (tenemosPelota()){
      if (behind_point(pelota, suPorteria) && pelota.t < RADIO_ROBOT * 4) {
        //si sitioAnterior es 1 tiramos arriba,sino tiramos abajo
        /*Vec2 sitioTirar= new Vec2(suPorteria.x,suPorteria.y + lugarTiro*0.3);
        //cambiamos el lugar de tiro para la siguiente vez, esto sirve si tiramos
        //una vez y recibimos el rebote,al cambiar el tiro, al portero
        //le costará mas llegar a donde va el balón
       lugarTiro=-1*lugarTiro;*/
        Vec2 sitioTirar=calculaTiro3();
       move.sett(sitioTirar.t);
       //move.sett(suPorteria.t);
       //Vec2 direccion=posicionTiro();
       //move.sett(direccion.t);
       move.setr(1.0);

        // if i'm within 15x ROBOT_RADII away from and aiming
        // relatively at the goal try to kick the ball
        if ( (Math.abs(angulo - suPorteria.t) < Math.PI / 8) &&
            (suPorteria.r < RADIO_ROBOT * 15)) {
          mensaje = "Voy a disparar";
          disparar = true;
          estadoActual=TIRO;
          /*Vec2 direccion=posicionTiro();
          move.sett(direccion.t);
          move.setr(1.0);*/
        }
      }
      else {
        // colocarse detrás de la pelota y evitar la colisión con otros jugadores.
        get_behind(pelota, suPorteria);
        evitarColision();
        //move.setr(0.5);
        mensaje = "Detrás pelota";
        estadoActual=DETRAS_PELOTA;
      }
    }
    else if (!tenemosPelota()){ //Vec2 paloArriba=new Vec2(suPorteria.x, suPorteria.y + TAM_PORTERIA/2);
      Vec2 desmarque;
      if (mynum==3)
       desmarque = new Vec2(centroCampo.x, centroCampo.y + 0.1);
      else
       desmarque = new Vec2(centroCampo.x, centroCampo.y - 0.1);
      //evitarColision();
      move.sett(desmarque.t); //con este ángulo decimos por donde nos desmarcamos
      move.setr(1.0);
      //evitarColision();
      mensaje = "Desmarcado";
      estadoActual = EN_POSICION_DELANTERO;
    }
      else {
        move.sett(pelota.t);
        move.setr(1.0);
      }
   } //fin estrategiaDelantero


//devuelve un vector con direccion del primero al segundo
  private Vec2 vectorDireccion(Vec2 a, Vec2 b) {
    Vec2 temp = new Vec2(b.x, b.y);
    temp.sub(a);
    return temp;
  }

  /* double NormalizePi
   *  Normalize an angle into the range [0,2*PI]
   */
  static double NormalizePI(double t) {
    while (t > 2 * Math.PI) {
      t -= 2 * Math.PI;
    }
    while (t < 0) {
      t += 2 * Math.PI;
    }
    return t;
  }

   private void evitarColision() { // an easy way to avoid collision

    // first keep out of your teammates way
    // if your closest teammate is too close, the move away from
    if (closest_team.r < RADIO_ROBOT * 1.4) {
      move.setx( -closest_team.x);
      move.sety( -closest_team.y);
      move.setr(1.0);
    }

    // if the closest opponent is too close, move away to try to
    // go around
    else if (closest_opp.r < RADIO_ROBOT * 1.4) {
      move.setx( -closest_opp.x);
      move.sety( -closest_opp.y);
      move.setr(1.0);
    }

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
  } //fin closest_to

  private void get_behind(Vec2 point, Vec2 orient) {
    Vec2 behind_point = new Vec2(0, 0);
    double behind = 0;
    double point_side = 0;

    // find a vector from the point, away from the orientation
    // you want to be
    behind_point.sett(orient.t);
    behind_point.setr(orient.r);

    behind_point.sub(point);
    behind_point.setr( -RADIO_ROBOT * 1.8);

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
      if (point_side > 0) {
        behind_point.sett(behind_point.t + Math.PI / 2);
      }
      else {
        behind_point.sett(behind_point.t - Math.PI / 2);
      }
    }

    // move toward the behind point
    move.sett(point.t);
    move.setr(point.r);
    move.add(behind_point);

    move.setr(1.0);

  } //fin get_behind

  private boolean behind_point(Vec2 point, Vec2 orient) { // you are behind an object relative to the orientation
    // if your position relative to the point and the orientation
    // are approximately the same
    if (Math.abs(point.t - orient.t) < Math.PI / 10) {
      return true;
    }
    else {
      return false;
    }
  }

//devuelve si la pelota está en nuestro campo o no
  public boolean vectorEnNuestroCampo(Vec2 elemento) {
    int direction = 1; // by default, WEST team
    if (abstract_robot.getOpponentsGoal(curr_time).x <
        abstract_robot.getOurGoal(curr_time).x) {
      direction = -1;//equipo este

    }
    boolean in_our_half = true;
    {
      Vec2 ball_abs = new Vec2(elemento.x, elemento.y);
      ball_abs.add(abstract_robot.getPosition(curr_time));
      if (ball_abs.x * direction < 0) {
        in_our_half = true;
      }
      else {
        in_our_half = false;
      }
    }
    //System.out.println("pelota en nuestro campo="+in_our_half);
    return in_our_half;
  }

  public Vec2[] ordenaPorCercania(Vec2[] vector) {
    //Método que ordena un array de vectores según la proximidad a nuestra posición.
    //Utilizamos una versión del algoritmo de ordenación por inserción (creo), ya que
    // a lo sumo tendremos 4 Vec2 que ordenar
    int n = vector.length;
    Vec2[] resultado = new Vec2[n];
    boolean[] flags = new boolean[n];
    for (int i = 0; i < n; i++) {
      flags[i] = false;
    }
    double minimo;
    int indexMinimo;
    for (int i = 0; i < n; i++) {
      minimo = 1000;
      indexMinimo = -1;
      for (int j = 0; j < n; j++) {
        //Bucle para buscar el minimo no usado
        if (!flags[j] && vector[j].r < minimo) {
          minimo = vector[j].r;
          indexMinimo = j;
        }
      }
      resultado[i] = new Vec2(vector[indexMinimo].x, vector[indexMinimo].y);
      flags[indexMinimo] = true;
    }
    //escribeVec2(resultado);
    return resultado;
  }

  public Vec2 damePos(Vec2 pos) {
    //Devolvemos la posición con respecto al nuevo origen, situado en la esquina
    //inferior izquierda
    //Esto deberia hacerse al principio y no cada vez que necesite la posición
    if (pos != null) {
      Vec2 posCambioCoord = new Vec2(pos.x + ANCHO / 2, pos.y + ALTO / 2);
      return posCambioCoord;
    }
    else {
      return null;
    }
  }

  public Vec2[] damePosV(Vec2[] poss) {
    //Devolvemos el vector de posiciones con respecto al nuevo origen, situado en la
    //esquina inferior izquierda
    int n = poss.length;
    Vec2[] posiciones = new Vec2[n];
    for (int i = 0; i < n; i++) {
      posiciones[i] = damePos(poss[i]);
    }
    return posiciones;
  }

  public double modulo(Vec2 v) {
    //Método que nos devuelve el módulo de un vector
    return Math.sqrt(v.x * v.x + v.y * v.y);
  }

  public boolean tengoBalon() {
    Vec2 aux = new Vec2(me.x, me.y);
    aux.sub(pelota);
    double limite = RADIO_ROBOT + RADIO_PELOTA + CERCANIA;
    boolean b = aux.r < limite;
    return b;
  }

  private boolean tenemosPelota() {
    //Vec2[] posAmigos = abstract_robot.getTeammates(time);
    //Vec2 pelota = abstract_robot.getBall(time);
    /*int i = 0;
    while (i < partners.length &&
           distancia(partners[i], pelota) >= RADIO_ROBOT + RADIO_PELOTA + CERCANIA) {
      i++;
    }
    return i != partners.length;*/
    boolean tenemos=false;
   int i = 0;
   while (i < partners.length && (!tenemos)){
     tenemos= distanciaDosVectores(partners[i], pelota) < RADIO_ROBOT + RADIO_PELOTA + CERCANIA;
     i++;
   }
   return tenemos;

  }

  private double distanciaDosVectores(Vec2 v1, Vec2 v2) {
   Vec2 tmp = (Vec2) v1.clone();
   tmp.sub(v2);
   return tmp.r;
 }




  public Vec2 dameRivalMasCercano() {
    //Método que devuelve un Vec2 al rival más cercano
    if (enemies != null && enemies.length > 0) {
      //Poner el >0 !!!!
      return enemies[0];
    }
    else {
      return null;
    }
  }

  public Vec2 dameCompMasCercano() {
    //Método que devuelve un Vec2 al compañero mas cercano
    if (partners != null && partners.length > 0) {
      return partners[0];
    }
    else {
      return null;
    }
  }

  private void escribeVec2(Vec2[] v) {
    int n = v.length;
    System.out.println("*******ESCRIBIMOS VEC2[] -> " + n);
    for (int i = 0; i < n; i++) {
      System.out.println(v.toString());
    }
  }

}
