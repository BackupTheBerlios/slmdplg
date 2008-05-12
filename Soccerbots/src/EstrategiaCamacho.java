
/*
*Estrategia1.java
 */

import  EDU.gatech.cc.is.util.Vec2;
import  EDU.gatech.cc.is.abstractrobot.*;
//import Entrenador;
import java.util.Random;
import java.util.Vector;


public class EstrategiaCamacho extends ControlSystemSS {

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

  private static final int NUM_JUGADORES = 5;
 /* private static final double FIELD_WIDTH = 1.525;
  private static final double FIELD_LENGTH = 2.7;//2.74;
  private static final double GOAL_WIDTH = 0.5;
  private static final double MARGIN = 0.02;
  private static final double RANGE = 0.3;
  private static final double TEAMMATE_G = 1.0;
  private static final double WALL_G = 1.0;
  private static final double GOALIE_G = 2.0;
  private static final double FORCE_LIMIT = 1.0;*/

  //private ZonaJuego[] zonasDeJuego= new ZonaJuego[NUM_ESTARTEGIAS];

  private Entrenador camacho;

  private long  curr_time;      //What time is it?
  private long  mynum;          //Who am I?
  private double rotation;      //What direction am I pointing?

  private Vec2 me;
  private Vec2  ball;           //Where is the ball?
  //private Vec2    ourgoal;        //Where is our goal?
  //private Vec2    theirgoal;      //Where is their goal?
  private Vec2[] teammates = new Vec2[NUM_JUGADORES];       //Where are my teammates?
  private Vec2[] opponents = new Vec2[NUM_JUGADORES];       //Where are my opponents?

  private Vec2  ego_ball;       //Where is the ball?
  private Vec2  ego_ourgoal;        //Where is our goal?
  private Vec2  ego_theirgoal;      //Where is their goal?
  private Vec2[] ego_teammates;     //Where are my teammates?
  private Vec2[] ego_opponents;     //Where are my opponents?

  
                                        //Where is the closest...
  //private Vec2    closest_team;       //Teammate?
  //private Vec2    closest_opp;        //Opponent?
  //private Vec2    closest_to_ball;    //Teammate to the Ball?

  private Vec2  ego_closest_team;   //Teammate?
  private Vec2  ego_closest_opp;    //Opponent?
  private Vec2  ego_closest_to_ball;    //Teammate to the Ball?
  

  private Vec2  move;           //Move in move.t direction
                                          //  with speed move.r
  private boolean   kickit;         //Try to kick it
  
  private boolean ataco = true;
  private int estrategia;
  private boolean voyPorElBalon;


  // what side of the field are we on? -1 for west +1 for east
  private static int SIDE;

  // restated here for convenience
  private final double ROBOT_RADIUS = abstract_robot.RADIUS;

  private static final boolean DEBUG = false;




  /**
 Configure the Avoid control system.  This method is
 called once at initialization time.  You can use it
 to do whatever you like.
 */
  public void Configure()
  {
    curr_time = abstract_robot.getTime();
    if( abstract_robot.getOurGoal(curr_time).x < 0) {
            SIDE = -1;
           // zonasDeJuego[0] = new ZonaJuego(-1.3,-1.2,-0.5,0.5);
    }
    else {
            SIDE = 1;
    }
        move = new Vec2(0,0);
        
  }


  /**
 Called every timestep to allow the control system to
 run.
 */
  public int TakeStep()
  {
    //Res.Resultado.Guardar(this,abstract_robot);
    //Vec2 closest = new Vec2(0,0);
    leerSensores();
    //voyPorElBalon = 
    /*if (mynum == 0) estrategia = PORTERO;
    else if(mynum == 2) { estrategia = LATERAL_IZQUIERDO; voyPorElBalon = true; }
    else if (mynum == 4) { estrategia = DELANTERO_OFENSIVO; voyPorElBalon = true; }
    else if (mynum == 1) { estrategia = CENTROCAMPISTA_DEFENSIVO; }
    else if (mynum == 3) { estrategia = LATERAL_DERECHO;}//CENTROCAMPISTA_OFENSIVO; }
    else estrategia = DELANTERO_DEFENSIVO;*/

    switch(estrategia) {
      case PORTERO : {portero(); break;
      }
      case DEFENSA_CERRADA : {defensaCerrada(); break;
      }
      case DEFENSA_OFENSIVA : {defensaOfensiva(); break;
      }
      case CENTROCAMPISTA_DEFENSIVO : {centrocampistaDefensivo(); break;
      }
      case CENTROCAMPISTA_OFENSIVO : {centrocampistaOfensivo(); break;
      }
      case LATERAL_DERECHO : {lateral(1); break;
      }      
      case LATERAL_IZQUIERDO : {lateral(-1); break;
      }      
      case DELANTERO_DEFENSIVO : {delantero(); break;
      }
      case DELANTERO_OFENSIVO : {delantero(); break;
      }
      case DELANTERO_COÑAZO : {play_offside(); break;
      }      
      //default: {portero();  break;
      //}
    }

  ejecutar();
  // tell the parent we're OK
  return(CSSTAT_OK);


  }
//------------------------------------------------------------------------------
  private void portero(){
    /*if(me.x>zonasDeJuego[0].getXMax()){
      move.sett( ego_ourgoal.t);
      move.setr( 1.0);
    }
    if(me.x<zonasDeJuego[0].getXMin()){
      move.sett( ego_theirgoal.t);
      move.setr( 1.0);
    }
    if(me.y>zonasDeJuego[0].getYMax()){
      move.sett( -1.0);
      move.setr( 1.0);
    }
    if(me.y<zonasDeJuego[0].getYMin()){
      move.sett( 1.0);
      move.setr( 1.0);
    }*/

      Vec2 contrario = closest_to(ego_ball,ego_opponents);
    if( (Math.abs(ego_ourgoal.x) > ROBOT_RADIUS * 2) ||
        (Math.abs(ego_ourgoal.y) > ROBOT_RADIUS * 4) )

    {
      move.sett( ego_ourgoal.t);
      move.setr( 1.0);
    }
    else
    {
      if( ego_ball.y > 0)
        move.sety( 15);
      else if(ego_ball.y<0)
        move.sety( -15);

      if( Math.abs( ego_ball.y) < this.ROBOT_RADIUS * 0.15)
        move.setr( 0.0);
      else
        move.setr( 1.0);
    }
    //esquivar(ego_closest_team);
   //if( contrario != ego_closest_opp)
    //  avoidcollision();
  }
//------------------------------------------------------------------------------
  private void defensaCerrada(){//No va muy bien todavia
        double margen=0.65;//con 0.35 más adelantado, con 0.65 a mitad de nuestro campo y con 0.90 por fuera de nuestro area
        if (!cercaDeMiPorteria(me,margen)) {
          move.setx(SIDE * 1.145);
          move.sety(0.0);
        } else if (((SIDE == -1) && (ego_ball.x > 0)) || ((SIDE == 1) && (ego_ball.x < 0))) {
          move.sett(ego_ball.t);
          move.setr(1.0);
          kickit=true;
              } else if (cercaDeMiPorteria(me,margen)) {
                  Vec2 contrario = closest_to(ego_ball,ego_opponents);
                  Vec2 porteria = new Vec2(ego_ourgoal.x, ego_ourgoal.y);
                  porteria.sub(contrario);
                  porteria.setr(ROBOT_RADIUS);
                  porteria.add(contrario);
                  move.sett(porteria.t);
                  move.setr(1.0);
                  avoidcollision();
        }
  }
//------------------------------------------------------------------------------  
  private void defensaOfensiva(){
        double margen=-0.50;//con -0.20 avanza bastante,con -0.50 es muy arriesgado
        if (!cercaDeMiPorteria(me,margen)) {
          move.setx(SIDE * 1.145);
          move.sety(0.0);
        } else if (((SIDE == -1) && (ego_ball.x > 0)) || ((SIDE == 1) && (ego_ball.x < 0))) {
          move.sett(ego_ball.t);
          move.setr(1.0);
          kickit=true;
               } else if (cercaDeMiPorteria(me,margen)) {
                   Vec2 contrario = closest_to(ego_ball,ego_opponents);
                   Vec2 porteria = new Vec2(ego_ourgoal.x, ego_ourgoal.y);
                   porteria.sub(contrario);
                   porteria.setr(ROBOT_RADIUS);
                   porteria.add(contrario);
                   move.sett(porteria.t);
                   move.setr(1.0);
                   avoidcollision();
        }
      }
//------------------------------------------------------------------------------
  private void centrocampistaDefensivo() {
      Vec2 backup = new Vec2(0,0);

      if( ego_closest_to_ball == me)
              centrocampistaOfensivo();
      else{
        defensaCerrada();
        }
    }
//------------------------------------------------------------------------------  
  private void centrocampistaOfensivo() {
        double margen=0.65;
        if (detrasPelota(ego_ball, ego_theirgoal) && ego_ball.t < ROBOT_RADIUS * 4) {
            move.sett(ego_theirgoal.t);
            move.setr(1.0);

            if ((Math.abs(abstract_robot.getSteerHeading(curr_time) - ego_theirgoal.t) < Math.PI / 8) &&
                (ego_theirgoal.r < ROBOT_RADIUS * 15) && (!cercaDeMiPorteria(me,margen) && !cercaMedioCampo(me,margen))) {
                //kickit=true;
                disparaAPuerta();
            }
        } else {
              Vec2 contrario = closest_to(ego_ball,ego_opponents);
              Vec2 porteria = new Vec2(ego_ourgoal.x, ego_ourgoal.y);
              porteria.sub(contrario);
              porteria.setr(ROBOT_RADIUS);
              porteria.add(contrario);
              move.sett(porteria.t);
              move.setr(1.0);
              avoidcollision();
              }
        }  
//------------------------------------------------------------------------------  
  private void lateral(int BANDA) {
        //SI BANDA = 1 LATERAL DERECHO, SI = -1 LATERAL IZQUIERDO
        Vec2 contrario = closest_to( ego_ball, ego_opponents);
        Vec2 dirLateral;    
        //Si estoy delante de la bola entonces ataco
        if (ataco) {
            //Por defecto avanzo por el lateral hasta la línea de fondo
            dirLateral = new Vec2(ego_theirgoal.x+SIDE*0.2,ego_theirgoal.y+(SIDE*0.24)*BANDA);
            //move.setx(me.x-SIDE);
            move.sett(dirLateral.t);
            move.setr(1.0);
            //Si tengo un contrario cerca y por delante mío
            //Si tengo un contrario cerca y por delante mío
            /*if (ego_closest_opp.r < 0.2 && !detrasDeMi(ego_closest_opp)) {
                paseDesmarque();
            }
            //Si estoy cerca de portería disparo
            else if (puedoDisparar()) {
                delantero();
            }               
            //Sino si estoy cerca de la línea de fondo de su campo
            else if ( Math.abs(ego_theirgoal.x) < 0.5) {
                paseCorto();
            }
            //Si voy a por el balon pero no está pegado
            else if(voyPorElBalon) {
                dirigirPelota();
            }*/
            if (voyPorElBalon && !dirigirPelota() && ego_theirgoal.r<0.7) disparaAPuerta();
            //if (voyPorElBalon && !dirigirPelota()) disparaAPuerta();
            /*else {
                disparaAPuerta();*/
                //Si tengo un contrario cerca y por delante mío
                /*if (ego_closest_opp.r < 0.2 && !detrasDeMi(ego_closest_opp)) {
                    paseDesmarque();
                }
                //Si estoy cerca de portería disparo
                else if (puedoDisparar()) {
                    disparaAPuerta();
                }               
                //Sino si estoy cerca de la línea de fondo de su campo
                else if ( Math.abs(ego_theirgoal.x) < 0.5) {
                    paseCorto();
                }*/
            //}
            //Siempre que ataco trato de esquivar a los oponenetes
            //esquivar(ego_closest_opp);
            //esquivar(ego_closest_team);
        }
        else {
                dirLateral = new Vec2(ego_ourgoal.x,ego_ourgoal.y+(SIDE*0.4)*BANDA);
                move.setx(me.x+SIDE);
                move.sett(dirLateral.t);
                move.setr(1.0);
                if(voyPorElBalon && ego_ball.r>0.1) {
                    move.sety(ego_ball.y+SIDE*0.1);
                    move.setx(ego_ball.x+SIDE*0.1);             
                    move.setr(1.0);             
                }
                else {
                    //defensaCerrada();
                    //Si hay un oponente cerca mia y estoy cerca de la portería
                    if (ego_ourgoal.r < 0.8) {
                        //El oponenete está en la banda derecha
                        if (ego_closest_opp.y*SIDE > 0 ) move.sety(ego_closest_opp.y - 0.2*SIDE);
                        //Sino está en la banda derecha
                        else move.sety(ego_closest_opp.y + 0.2*SIDE);
                        move.setx(ego_closest_opp.x+SIDE*0.3);
                        move.setr(1.0);
                    }
                }
            //esquivar(ego_closest_team);
        }
        if( contrario != ego_closest_opp)
            avoidcollision();
        
  }  
//------------------------------------------------------------------------------
  //Devuelve cierto si era necesario dirigir la pelota o ya estabas orienta
  private boolean dirigirPelota() {
      //Miro si tengo que esquivar a un oponente
      boolean dirigirPelotaEsquivarOp = ego_closest_opp.r<ROBOT_RADIUS*4 && !detrasDeMi(ego_closest_opp); 
      if (dirigirPelotaEsquivarOp) {
          esquivar(ego_closest_opp);
      }  
      //Si no estoy con la pelota delante dirigo la bola
      boolean dirigirPelota = (Math.abs(move.t-ego_ball.t)>0.5); 
      if (dirigirPelota) {
          Vec2 aux = new Vec2(move);
          aux.sub(ego_ball);
          aux.setr(aux.r+ROBOT_RADIUS);
          move.sub(aux);
          move.setr(1);
      }
      return dirigirPelota; 
  }
  
//------------------------------------------------------------------------------
  private boolean detrasPelota(Vec2 ball, Vec2 objetivo) {
      if (Math.abs(ball.t - objetivo.t) < Math.PI / 10)
        return true;
      else
        return false;
    }  
//------------------------------------------------------------------------------
  private boolean cercaDeMiPorteria(Vec2 jug, double margen) {
        if (SIDE == -1) {
            if (jug.x < (-margen)) {
                return true;
            }
        } else {
            if ((margen) < jug.x) {
                return true;
            }
        }
        return false;
    }
//------------------------------------------------------------------------------
    private boolean cercaMedioCampo(Vec2 jug,double margen) {
      if (SIDE == -1) {
        if ( ((-margen) < jug.x) && (jug.x < 0.0) ) {
          return true;
        }
      } else {
        if ( (0.0 < jug.x) && ((margen) < jug.x) ) {
          return true;
        }
      }
      return false;
    }    
//------------------------------------------------------------------------------
  private boolean puedoDisparar() {
      Random alea = new Random();
      double num = alea.nextDouble()*3;
      //Dependiendo de la distancia a puerta disparo o no
      return num > 2-ego_theirgoal.r;
  }
  

//------------------------------------------------------------------------------
  private void pase(Vec2 destino) {
      move.sett(destino.t);
      move.setr(destino.r+0.2);
      kickit = true;
  }
//------------------------------------------------------------------------------
  private void paseCorto() {
     pase(ego_closest_team); 
  }  
//------------------------------------------------------------------------------
  private void paseDesmarque() {
      Vec2[] compisDetras = new Vec2[NUM_JUGADORES];
      Vec2 compañero;
      int numDetras = 0;
      int i = 0;
      boolean compiDesmarcado = false;
      //Busco a los jugadores por delante mía que estén desmarcados
      while (!compiDesmarcado && i<ego_teammates.length) {
          compañero = ego_teammates[i];
          //Mi compañero está detras
          if (detrasDeMi(compañero)) {
              compisDetras[numDetras] = compañero;
              numDetras++;
          }
          //Mi compañero está delante
          else if (desmarcado(compañero)) {
              compiDesmarcado = true;
              //Doy el pase un poco adelantado
              Vec2 aux = new Vec2(compañero.x-0.05*SIDE,compañero.y);
              pase(aux);
          }
          i++;
      }
      //Si no he conseguido pasarla a nadie desmarcado por delante
      //se la paso a alguien que esté detrás
      if (!compiDesmarcado) {
          i = 0;
          while (!compiDesmarcado && i<numDetras) {
              compañero = compisDetras[i];
              if (desmarcado(compañero)) {
                  //Doy el pase un poco adelantado
                  Vec2 aux = new Vec2(compañero.x-0.05*SIDE,compañero.y);
                  pase(aux);
                  compiDesmarcado = true;
              }
              i++;
          }
      }
      //Si tampoco hay nadie desmarcado por detrás tiro
      if (!compiDesmarcado) disparaAPuerta();
  }
//------------------------------------------------------------------------------
 /* private void salirDeBloqueo(int hacia) {
      switch(hacia) {
          //Voy arriba a la dcha
          case(0): {
              move.setx(me.x+3);
              move.sety(me.y+3);
          }
          //Voy arriba a la izda
          case(1): {
              move.setx(me.x-3);
              move.sety(me.y+3);
          }
          //Voy abajo a la izda
          case(2): {
              move.setx(me.x-3);
              move.sety(me.y-3);
          }
          //Voy abajo a la dcha
          case(3): {
              move.setx(me.x+3);
              move.sety(me.y-3);
          }       
      }
  }*/
  
//------------------------------------------------------------------------------  
  //Indica si un compañero está libre de marca con respecto a mí 
  private boolean desmarcado(Vec2 compañero) {
      boolean desmarcado = true;
      Random alea=new Random();
      //Para todos los contrarios
      int i=0;
      while (i<ego_opponents.length && desmarcado) {
          Vec2 oponente = ego_opponents[i];
          //Si está más cerca de mí que mi compañero
          if (oponente.r < compañero.r) {
              double[] zonaCubierta = Sectores.calculaSectorCubierto(oponente,ROBOT_RADIUS); 
              //Si el oponente no le cubre por completo
              desmarcado = (compañero.t<zonaCubierta[0] || compañero.t>zonaCubierta[1]);
              
              /*if (zonaCubierta[1]-zonaCubierta[0]>0 && move.t>=zonaCubierta[0] && move.t<=zonaCubierta[1]) {
                  if (alea.nextBoolean()) { move.sett(zonaCubierta[0]-0.1); }
                  else { move.sett(zonaCubierta[1]+0.1); }
              }
              else if (zonaCubierta[1]-zonaCubierta[0]<0 && move.t >= zonaCubierta[0] && move.t<=0) {
                  if (alea.nextBoolean()) { move.sett(zonaCubierta[0]-0.1); }
                  else { move.sett(zonaCubierta[1]+0.1); }
              }
              else if (zonaCubierta[1]-zonaCubierta[0]<0 && move.t <= zonaCubierta[1] && move.t>=0) {
                  if (alea.nextBoolean()) { move.sett(zonaCubierta[0]-0.1); }
                  else { move.sett(zonaCubierta[1]+0.1); }
              } */            
          }
          i++;
      }
      
      /*double angulo = compañero.t;
      int i=0;
      while (i<ego_opponents.length && desmarcado) {
          desmarcado = (ego_opponents[i].t + 0.2 < angulo)&&(ego_opponents[i].t - 0.2 > angulo);
          i++;
      }*/
      return desmarcado;
  }
//------------------------------------------------------------------------------
 /* private Vec2 contrarioDelante() {
      Vec2 op = null;
      double cerca = 0.5;
      double[] zonaCubierta = Sectores.calculaSectorCubierto(op,ROBOT_RADIUS);
      for (int i=0; i<ego_opponents.length; i++) {
          if (ego_opponents[i].r < cerca) {
              if (zonaCubierta[1]-zonaCubierta[0]>0 && move.t>=zonaCubierta[0] && move.t<=zonaCubierta[1]) {
                  op =ego_opponents[i]; 
              }
              else if (zonaCubierta[1]-zonaCubierta[0]<0 && move.t >= zonaCubierta[0] && move.t<=0) {
                  op = ego_opponents[i];
              }
              else if (zonaCubierta[1]-zonaCubierta[0]<0 && move.t <= zonaCubierta[1] && move.t>=0) {
                  op = ego_opponents[i];
              }           
          }
      }
      return op;
  }*/
  private void esquivar(Vec2 jugador) {
      Random alea = new Random();
      Vec2 obst;
      double cerca = 0.4;
      if (jugador.r < cerca) { 
          double[] zonaCubierta = Sectores.calculaSectorCubierto(jugador,ROBOT_RADIUS*2);
          double ang = Math.asin(ROBOT_RADIUS/jugador.r);
          if (zonaCubierta[1]-zonaCubierta[0]>0 && move.t>=zonaCubierta[0] && move.t<=zonaCubierta[1]) {
              if (alea.nextBoolean()) {               
                  move.sett(zonaCubierta[0]-ang); 
              }
              else { move.sett(zonaCubierta[1]+ang); }
          }
          else if (zonaCubierta[1]-zonaCubierta[0]<0 && move.t >= zonaCubierta[0] && move.t<=0) {
              if (alea.nextBoolean()) { move.sett(zonaCubierta[0]-ang); }
              else { move.sett(zonaCubierta[1]+ang); }
          }
          else if (zonaCubierta[1]-zonaCubierta[0]<0 && move.t <= zonaCubierta[1] && move.t>=0) {
              if (alea.nextBoolean()) { move.sett(zonaCubierta[0]-ang); }
              else { move.sett(zonaCubierta[1]+ang); }
          }           
      }
      move.setr(1);
  }  
//------------------------------------------------------------------------------
  //Se trata de esquivar una zona cubierta
 /* private void esquivar(Vec2 jugador) {
      double[] zonaCubierta = Sectores.calculaSectorCubierto(jugador,ROBOT_RADIUS);
      //Aleatoiramente le esquivo por la derecha o por la izquierda
      Random alea = new Random();
      if (alea.nextBoolean()) { move.sett(zonaCubierta[0]-0.1); }
      else { move.sett(zonaCubierta[1]+0.1); }
  }*/
//------------------------------------------------------------------------------  
  private boolean tenemosBalon() {
      Vec2 contrarioMasCercaBola = closest_to( ego_ball, ego_opponents);      
      return ego_closest_to_ball.r <= contrarioMasCercaBola.r;
  }
//------------------------------------------------------------------------------
  //Indica si el elemento está detras de mi con respecto a mi pertería
  private boolean detrasDeMi(Vec2 elem) {
      return elem.x * SIDE > 0;
  }
//------------------------------------------------------------------------------
  private void leerSensores()
  {
    //int[] decision = this.camacho.getEstrategia();
    //estrategia = decision[0];
    //voyPorElBalon = decision[1]>0;
    estrategia = Entrenador.getInstance().getEstrategia(abstract_robot);
      
    Vec2 closest;
    Vec2 temp = new Vec2(0,0);
    //tiempo actual
    curr_time = abstract_robot.getTime();
    me = abstract_robot.getPosition(curr_time);
    //mi identificacion
    mynum = abstract_robot.getPlayerNumber(curr_time);
    //vector de la pelota
    ego_ball = abstract_robot.getBall(curr_time);
    ball = egoToCenter(ego_ball);
    //situacion de las porterial
    ego_ourgoal = abstract_robot.getOurGoal(curr_time);
    //ourgoal =egoToCenter(ego_ourgoal);

    ego_theirgoal = abstract_robot.getOpponentsGoal(curr_time);
    //theirgoal = egoToCenter(ego_theirgoal);
    //lista de posiciones de mis compañeros
    ego_teammates = abstract_robot.getTeammates(curr_time);
    for (int i=0; i<ego_teammates.length; i++) {
        teammates[i] = egoToCenter(ego_teammates[i]);
    }
    //lista de posiciones de los contrarios
    ego_opponents = abstract_robot.getOpponents(curr_time);
    for (int i=0; i<ego_opponents.length; i++) {
        opponents[i] = egoToCenter(ego_opponents[i]);
    }
    //Compañero más cercano
    ego_closest_team = closest_to( me, ego_teammates);
    //Contrario más cercano
    ego_closest_opp = closest_to( me, ego_opponents);
    //Compañero más cerca de la pelota
    closest = closest_to( ego_ball, ego_teammates);

    temp.sett( closest.t);
    temp.setr( closest.r);

    temp.sub(ego_ball);

    if( temp.r > ego_ball.r) {
        ego_closest_to_ball = me;
        voyPorElBalon = true;
    }
    else {
      ego_closest_to_ball = closest;
      voyPorElBalon = false;
    }
    //hacia donde apunto
    rotation = abstract_robot.getSteerHeading( curr_time);

    // set movement data: rotation and speed;
    move.sett(0.0);
    move.setr(0.0);

    //disparar
    kickit = false;
    
    ataco = !detrasDeMi(ego_ball);

  }
//------------------------------------------------------------------------------
  void ejecutar(){
   /*--- Send commands to actuators ---*/
    // set the heading
    abstract_robot.setSteerHeading(curr_time, move.t);

    // set the speed
    abstract_robot.setSpeed(curr_time, move.r);

    // maybe kick it
    if (kickit && abstract_robot.canKick( curr_time))
      abstract_robot.kick(curr_time);
  }

  //Funciones útiles
//------------------------------------------------------------------------------
  //Devuelve el vector mas cercano.
  //Si los object y el point son egocéntricos la salida tb lo es.
  //Si los object y el point son centrados la salida tb lo es.
  private Vec2 closest_to( Vec2 point, Vec2[] objects)
        {
                double dist = 9999;
                Vec2 result = new Vec2(0, 0);
                Vec2 temp = new Vec2(0, 0);

                for( int i=0; i < objects.length; i++)
                {
                        // find the distance from the point to the current
                        // object
                        temp.sett( objects[i].t);
                        temp.setr( objects[i].r);
                        temp.sub( point);

                        // if the distance is smaller than any other distance
                        // then you have something closer to the point
                        if(temp.r < dist)
                        {
                                result = objects[i];
                                dist = temp.r;
                        }
                }

                return result;
        }
//------------------------------------------------------------------------------
        private void avoidcollision( )
        {
          // an easy way to avoid collision

          // first keep out of your teammates way
          // if your closest teammate is too close, the move away from
          if( ego_closest_team.r < ROBOT_RADIUS*1.4 )
          {
            move.setx( -ego_closest_team.x);
            move.sety( -ego_closest_team.y);
            move.setr( 1.0);
          }

          // if the closest opponent is too close, move away to try to
          // go around
          else if( ego_closest_opp.r < ROBOT_RADIUS*1.4)
          {
            move.setx( -ego_closest_opp.x);
            move.sety( -ego_closest_opp.y);
            move.setr( 1.0);
          }

        }
//------------------------------------------------------------------------------
    private Vec2 centerToEgo(Vec2 vCenter) {
      Vec2 vEgo = new Vec2(vCenter);
      vEgo.sub(me);
      return vEgo;
    }

//------------------------------------------------------------------------------
    private Vec2 egoToCenter(Vec2 vEgo) {
      Vec2 vCenter = new Vec2(vEgo);
      vCenter.add(me);
      return vCenter;
    }
    
    //**********************************************DELANTERO*/////////////////////////////////
    public Vec2 closest_to_ball(){
        Vec2 closest_to_ball=new Vec2();
        Vec2 temp=new Vec2();
        Vec2 closest1 = closest_to( ego_ball, ego_teammates);
        Vec2 closest2 = closest_to( ego_ball, ego_opponents);

        Vec2[] aux=new Vec2[2];
        aux[0]=closest1;aux[1]=closest2;
        Vec2 closest = closest_to(ego_ball,aux);

        temp.sett( closest.t);
        temp.setr( closest.r);

        temp.sub(ego_ball);

        if( temp.r > ego_ball.r)
          closest_to_ball = me;
        else
          closest_to_ball = closest;
        return closest_to_ball;
      }
    private boolean behind_point( Vec2 point, Vec2 orient)
    {

            // you are behind an object relative to the orientation
            // if your position relative to the point and the orientation
            // are approximately the same
            if( Math.abs( point.t - orient.t) < Math.PI/10)
                    return true;
            else
                    return false;
    }
    private void drive_ball()
    {
      // if i'm behind the ball (oriented toward the goal) then
      // start charging the goal
      if( behind_point( ego_ball, ego_theirgoal) && ego_ball.t < ROBOT_RADIUS * 4)
      {
        move.sett( ego_theirgoal.t);
        move.setr( 1.0);

     /*   // if i'm within 15x ROBOT_RADII away from and aiming
        // relatively at the goal try to kick the ball
        if( (Math.abs( rotation - ego_theirgoal.t) < Math.PI/8) &&
            (ego_theirgoal.r < ROBOT_RADIUS * 15))
            kickit = true;*/
        /****************************************************************************************************/
        disparaAPuerta();//no he cambiado nada
        /****************************************************************************************************/
      }
      else

        // otherwise get behind the ball and avoid colliding with
        // other players
      {
        get_behind( ego_ball, ego_theirgoal);
        avoidcollision();
      }
    }
    private void get_behind( Vec2 point, Vec2 orient)
    {
      Vec2 behind_point = new Vec2(0,0);
      double behind = 0;
      double point_side = 0;

      // find a vector from the point, away from the orientation
      // you want to be
      behind_point.sett( orient.t);
      behind_point.setr( orient.r);

      behind_point.sub( point);
      behind_point.setr( -ROBOT_RADIUS*1.8);

      // determine if you are behind the object with respect
      // to the orientation
      behind = Math.cos( Math.abs( point.t - behind_point.t));

      // determine if you are on the left or right hand side
      // with respect to the orientation
      point_side = Math.sin( Math.abs( point.t - behind_point.t));

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
      move.sett( point.t);
      move.setr( point.r);
      move.add( behind_point);

      move.setr( 1.0);

    }

    
    boolean i_am_on_east_team()
    {
      return !i_am_on_west_team();
    }

    boolean i_am_on_west_team()
    {
      Vec2 ourgoal2 = new Vec2( ego_ourgoal.x, ego_ourgoal.y );
      ourgoal2.add( me );
      return ourgoal2.x < 0;
    }
    boolean robot_close_to_ball( Vec2 robot )
    {
      Vec2 temp = new Vec2( robot.x, robot.y );
      temp.sub( ball );
      if( temp.r > ROBOT_RADIUS)
        return false;
      return true;
    }
//  returns theta direction to go if we want to get behind the ball
    double toward_behind_ball(){
     // for now, we'll just be stupid and bump it backwards
     Vec2 result;
     double FUDGE = 0.02;
     if( i_am_on_east_team() )
       result = new Vec2( ball.x + (ROBOT_RADIUS+FUDGE), ball.y );
     else
       result = new Vec2( ball.x - (ROBOT_RADIUS+FUDGE), ball.y );

     // make egocentric
     result.sub( me );
     if( robot_close_to_ball( me ) && !(closest_to_ball()== me && behind_point(ego_ball,ego_theirgoal)) )
     {
       // i am very near it, but am not behind it
       // try and run around it

       if( (me.y > 0.1)/*parte de arriba del campo*/ != i_am_on_east_team() )
       {
         return result.t + Math.PI/3;
       }
       else
       {

         return result.t - Math.PI/3;
       }
     }
     else
       return result.t;
   }
    static Vec2 toward( Vec2 a, Vec2 b )
    {
      Vec2 temp = new Vec2( b.x, b.y );
      temp.sub( a );
      return temp;
    }
    private void delantero(){
        
  /*
          Vec2 contrario = closest_to( ego_ball, ego_opponents);

          // find the point just behind the contrario
          ego_theirgoal.sub( contrario);
          ego_theirgoal.setr( ROBOT_RADIUS );
          ego_theirgoal.add( contrario);

          move.sett( ego_theirgoal.t);
          move.setr( 1.0);

          if( (Math.abs( rotation - ego_theirgoal.t) < Math.PI/8) &&
                                    (ego_theirgoal.r < ROBOT_RADIUS * 15))
                        kickit = true;

          if( contrario != ego_closest_opp)
                    avoidcollision();*/
        //SI BANDA = 1 LATERAL DERECHO, SI = -1 LATERAL IZQUIERDO
        
        Vec2 contrario = closest_to( ego_ball, ego_opponents);  
        //Si estoy delante de la bola entonces ataco
        if (ataco) {
            //Por defecto avanzo hacia su portería
            move.sett(ego_theirgoal.t);
            move.setr(1.0);
            
            /*if (ego_theirgoal.r<0.7) {
                disparaAPuerta();
            }
            //Si voy a por el balon pero no está pegado
            else if(voyPorElBalon) {
                dirigirPelota();
            }*/
            
            if (voyPorElBalon && !dirigirPelota() && ego_theirgoal.r<0.7) disparaAPuerta();
            
            //Siempre que ataco o de esquivar a los oponenetes
            //esquivar(ego_closest_opp);
            //esquivar(ego_closest_team);
        }
        else {
            //Por defecto voy hasta casi medio campo
            Vec2 dir = new Vec2(-me.x+SIDE*0.2, -me.y); 
            move.sett(dir.t);
            move.setr(1.0);
            /*if(voyPorElBalon && ego_ball.r>0.1 && !detrasDeMi(ego_ball)) {
                move.sety(ego_ball.y+SIDE*0.1);
                move.setx(ego_ball.x+SIDE*0.1);             
                move.setr(1.0);             
            }*/
            //Si hay un oponente cerca mia y estoy cerca de la portería
            /*else if (ego_ourgoal.r < 0.8) {
                    //El oponenete está en la banda derecha
                    if (ego_closest_opp.y*SIDE > 0 ) move.sety(ego_closest_opp.y - 0.2*SIDE);
                    //Sino está en la banda derecha
                    else move.sety(ego_closest_opp.y + 0.2*SIDE);
                    move.setx(ego_closest_opp.x+SIDE*0.3);
                    move.setr(1.0);
            }*/
            //esquivar(ego_closest_team);
        }
        if( contrario != ego_closest_opp)
            avoidcollision();

          }   
    
    
//  ------------------------------------------------------------------------------
    private void disparaAPuerta() {
    /*Vector angulosCubiertos = new Vector();
      for (int i=0; i<ego_opponents.length; i++) {
          if (!detrasDeMi(ego_opponents[i])) {
              angulosCubiertos.add(Sectores.calculaSectorCubierto(ego_opponents[i],ROBOT_RADIUS));
          }
      }
      Vector sectoresLibresPorteria = new Vector();
      sectoresLibresPorteria.add(Sectores.calculaSectorPorteria(ego_theirgoal, -SIDE));
      for (int i=0; i<angulosCubiertos.size(); i++) {
          double[] sectorCubierto = (double[])angulosCubiertos.get(i);
          sectoresLibresPorteria=Sectores.eliminaZonaCubierta(sectoresLibresPorteria,sectorCubierto);
      }
      int contadore=0;
      double[] sectorPorteria;
      int indice=0;
      double difAngulos=-200;
      while(contadore<sectoresLibresPorteria.size()){
        sectorPorteria=(double[])sectoresLibresPorteria.get(contadore);
        if(((double)(sectorPorteria[1] - sectorPorteria[0]))>difAngulos){
            indice=contadore;
            difAngulos=(sectorPorteria[1] - sectorPorteria[0]);
        }
        contadore++;
      }*/
    /*sectorPorteria=(double[])sectoresLibresPorteria.get(indice);
      Random alea = new Random();
      int cual=alea.nextInt(sectoresLibresPorteria.size());
      sectorPorteria=(double[])sectoresLibresPorteria.get(cual);*/
     // double difAngulos = sectorPorteria[1] - sectorPorteria[0];
        
      double[] cubrePortero = Sectores.calculaSectorCubierto(ego_closest_opp, ROBOT_RADIUS);
      Vector sectoresLibresPorteria = new Vector();
      sectoresLibresPorteria.add(Sectores.calculaSectorPorteria(ego_theirgoal, -SIDE));
      sectoresLibresPorteria=Sectores.eliminaZonaCubierta(sectoresLibresPorteria,cubrePortero);
      Random alea = new Random();
      double[] sectorDisparo = (double[])sectoresLibresPorteria.get(alea.nextInt(sectoresLibresPorteria.size()));
      
      if (alea.nextBoolean()) move.sett(sectorDisparo[0]);
      else move.sett(sectorDisparo[1]);
      
      
     /* sectorPorteria=(double[])sectoresLibresPorteria.get(0);//indice
      move.sett(sectorPorteria[0]);*/
      move.setr(1);
      kickit = true;
    }
    private void play_offside( )
    {
        // the other team's goalie is whoever is closest to the goal
        Vec2 goalie = closest_to( ego_theirgoal, ego_opponents);

        // find the point just behind the "goalie" 
        // in the way of their goal
        ego_theirgoal.sub( goalie);
        ego_theirgoal.setr( ROBOT_RADIUS );
        ego_theirgoal.add( goalie);

        move.sett( ego_theirgoal.t);
        move.setr( 1.0);

        // if you aren't blocking the "goalie" then don't collide
        if( goalie != ego_closest_opp)
            avoidcollision();
    }    
}
