/*
 * JuventusDeTuring.java
 */

import  EDU.gatech.cc.is.util.Vec2;
import  EDU.gatech.cc.is.abstractrobot.*;
import java.util.*;
import java.math.*;

public class JuventusDeTuring extends ControlSystemSS {

  final double D_TIRO=0.7;
  static int IZQUIERDA = 0;
  static int DERECHA = 1;
  static boolean b0,b1,b2,b3,b4,b5;
  static int portero;
  int lado;
  final double CERO=-0.125; //margen del cero en double
  final double B_IZQ=0.1; //distancia de las bandas
  final double B_DRCH=-B_IZQ;
  Vector bloqueadores;
  static long curr_time;

  public void Configure(){
    //Nos colocamos en el lado correspondiente
    curr_time = abstract_robot.getTime();
    if(abstract_robot.getPosition(curr_time).x < 0) lado = IZQUIERDA;
    else lado = DERECHA;
    b1=b2=b3=b4=b0=false;
    portero=0;
    bloqueadores = new Vector();
  }


  //Pone una variable de bloqueo al valor que se pasa como parámetro
  public void b(int n, boolean b){
    if (n==0) b0=b;
    if (n==1) b1=b;
    if (n==2) b2=b;
    if (n==3) b3=b;
    if (n==4) b4=b;
  }


  public int portero(int mynum){
    //Momento actual, posicion de portero y de la pelota,
    long curr_time = abstract_robot.getTime();
    Vec2 vPosicion = abstract_robot.getPosition(curr_time);
    Vec2 vPelota = abstract_robot.getBall(curr_time);
    Vec2 vPorteria = abstract_robot.getOurGoal(curr_time);

   if (vPorteria.r>0 && Math.abs(vPosicion.x)<1.3) {
     abstract_robot.setSteerHeading(curr_time,vPorteria.t);
     abstract_robot.setSpeed(curr_time, 1.0);
   }

    //2A Si no, es que está colocado en la porteria
    else {
      //Si el balón está por encima o debajo, va hacia el extremo de la porteria
      //PERO sin salir de los límites para evitar tiros cruzados
      //Si está a su altura, chuta para despejar
      double pelota = Math.abs(vPelota.y);
      double portero = Math.abs(vPosicion.y);

      //Si estamos en los límites de la portería (en coordenadas Y)
      if (portero < 0.275){
        //Balón por encima del portero
        //en Y positiva y negativa
        if (pelota > portero && vPelota.y >= 0) {
          abstract_robot.setSteerHeading(curr_time, (90 * Math.PI) / 180.0d);
          abstract_robot.setSpeed(curr_time, 1);
        }
        else if (pelota > portero && vPelota.y < 0) {
          abstract_robot.setSteerHeading(curr_time, (270 * Math.PI) / 180.0d);
          abstract_robot.setSpeed(curr_time, 1);
        }
        //Balón por debajo del portero
        //en Y positiva y negativa
        else if (pelota < portero && vPelota.y >= 0) {
          abstract_robot.setSteerHeading(curr_time, (90 * Math.PI) / 180.0d);
          abstract_robot.setSpeed(curr_time, 1);
        }
        else if (pelota < portero && vPelota.y < 0) {
          abstract_robot.setSteerHeading(curr_time, (270 * Math.PI) / 180.0d);
          abstract_robot.setSpeed(curr_time, 1);
        }
        //Balón a la altura del portero: despeja de frente. puede ser problemático x rebotes
        else if (pelota == portero) {
          abstract_robot.setSpeed(curr_time, 0);
          if (abstract_robot.canKick(curr_time)) {
            if (lado == IZQUIERDA)
              abstract_robot.setSteerHeading(curr_time, 0);
            else
              abstract_robot.setSteerHeading(curr_time, Math.PI);

            abstract_robot.kick(curr_time);
          }
        }
        //Si puede chutar, chuta
        if (abstract_robot.canKick(curr_time)) {
          if (lado == IZQUIERDA)
            abstract_robot.setSteerHeading(curr_time, 0);
          else
            abstract_robot.setSteerHeading(curr_time, Math.PI);
          abstract_robot.kick(curr_time);
        }
      }
      //2B Si no estamos dentro de los límites de la porteria volvemos a ella
      //Si es por arriba
      else if (portero >0.275 && vPosicion.y>0){
        abstract_robot.setSteerHeading(curr_time, (270 * Math.PI) / 180.0d);
      }
      //Si es por abajo
      else if (portero >0.275 && vPosicion.y<0){
        abstract_robot.setSteerHeading(curr_time, (90 * Math.PI) / 180.0d);
      }
      else if (portero == 0.275) abstract_robot.setSpeed(curr_time, 0);
    }
    return(CSSTAT_OK);
  }

//Comportamiento para el lateral derecho
  public int bandaIzquierda(){
    Vec2 result = new Vec2(0,0);
    Vec2 ball = abstract_robot.getBall(curr_time);
    // get vector to our and their goal
    Vec2 ourgoal = abstract_robot.getOurGoal(curr_time);
    Vec2 theirgoal = abstract_robot.getOpponentsGoal(curr_time);
    // get a list of the positions of our teammates
    Vec2[] teammates = abstract_robot.getTeammates(curr_time);
    // find the closest teammate
    Vec2 closestteammate = new Vec2(99999,0);
    for (int i=0; i< teammates.length; i++) {
      if (teammates[i].r < closestteammate.r)
        closestteammate = teammates[i];
    }

    Vec2 pos=abstract_robot.getPosition(curr_time);
    //calculamos el vector que apunta a la portería desde donde
    //está la bola
    Vec2 porteria=abstract_robot.getOpponentsGoal(curr_time);
    porteria.sub(ball);
    //calculamos de una forma mejor la posicion a colocarnos
    Vec2 kick=new Vec2(-porteria.x,-porteria.y);
    kick.setr(abstract_robot.RADIUS);
    kick.add(ball);
    //obtenemos en bola las posiciones de bola
    Vec2 bola=new Vec2(pos.x,pos.y);
    bola.add(ball);
    int factor;
    if (theirgoal.x>0)
      //su equipo es el OESTE atacamos a la derecha
      factor=1;
    else
      //su equipo es el ESTE atacamos a la izquierda
      factor=-1;
      //Buscamos el compañero que está más cerca a la pelota
    Vec2 cercaBola = new Vec2(99999,0);
    for (int i=0; i< teammates.length; i++){
      Vec2 disBola=new Vec2(ball.x,ball.y);
      disBola.sub(teammates[i]);
      if (disBola.r < cercaBola.r)
        cercaBola = teammates[i];
    }
    Vec2 disBola=new Vec2(ball.x,ball.y);
    disBola.sub(cercaBola);
    //si la pelota está en campo contrario cerca de la banda izq
    //y no tenemos la bola y ninguno de los compañeros está más
    //cerca que nosotros.
    if (bola.x*factor>=CERO && bola.y>=B_IZQ &&
        !abstract_robot.canKick(curr_time) && disBola.r>ball.r){
      result = kick;
      abstract_robot.setSpeed(curr_time, 1.0);
    }
    //si la pelota está en campo contrario y no tenemos la bola
    //y hay un compañero más cerca de la bola
    else if (bola.x*factor>=CERO && !abstract_robot.canKick(curr_time) &&
             disBola.r<ball.r && bola.y>=B_DRCH) {
      if (ball.r>4*abstract_robot.RADIUS)
        result = kick;
      else {
        Vec2 v = new Vec2(1, 1);
        v.setr(4*abstract_robot.RADIUS);
        v.sett(Math.PI/6);
        Vec2 ir=new Vec2(ball.x,ball.y);
        ir.add(v);
        result = ir;
      }
      abstract_robot.setSpeed(curr_time, 1.0);
    }
    //si la pelota está en campo contrario y podemos disparar
    else if (bola.x*factor>=CERO && abstract_robot.canKick(curr_time) && porteria.r<D_TIRO) {
      abstract_robot.setSteerHeading(curr_time,porteria.t);
      abstract_robot.kick(curr_time);
      abstract_robot.setSpeed(curr_time, 1.0);
    }
    //si tenemos el balón en campo contrario y no estamos suficientemente cerca
    else if (bola.x*factor>=CERO && abstract_robot.canKick(curr_time)){
      result=kick;
      abstract_robot.setSteerHeading(curr_time,porteria.t);
      abstract_robot.setSpeed(curr_time, 1.0);
    }
    else if (bola.x*factor<=-CERO) {
      result=kick;
      if (abstract_robot.canKick(curr_time)) {
        abstract_robot.setSteerHeading(curr_time,porteria.t);
        abstract_robot.kick(curr_time);
      }
      abstract_robot.setSpeed(curr_time, 1.0);
    }
    else
      abstract_robot.setSpeed(curr_time, 0.0);
    abstract_robot.setSteerHeading(curr_time, result.t);
    // tell the parent we're OK
    return(CSSTAT_OK);
  }



  //Comportamiento para el lateral derecho
  public int bandaDerecha(){
    Vec2 result = new Vec2(0,0);
    Vec2 ball = abstract_robot.getBall(curr_time);
    // get vector to our and their goal
    Vec2 ourgoal = abstract_robot.getOurGoal(curr_time);
    Vec2 theirgoal = abstract_robot.getOpponentsGoal(curr_time);
    // get a list of the positions of our teammates
    Vec2[] teammates = abstract_robot.getTeammates(curr_time);
    // find the closest teammate
    Vec2 closestteammate = new Vec2(99999,0);
    for (int i=0; i< teammates.length; i++) {
      if (teammates[i].r < closestteammate.r)
        closestteammate = teammates[i];
    }

    Vec2 pos=abstract_robot.getPosition(curr_time);
    //calculamos el vector que apunta a la portería desde donde
    //está la bola
    Vec2 porteria=abstract_robot.getOpponentsGoal(curr_time);
    porteria.sub(ball);
    //calculamos de una forma mejor la posicion a colocarnos
    Vec2 kick=new Vec2(-porteria.x,-porteria.y);
    kick.setr(abstract_robot.RADIUS);
    kick.add(ball);
    //obtenemos en bola las posiciones de bola
    Vec2 bola=new Vec2(pos.x,pos.y);
    bola.add(ball);
    int factor;
    if (theirgoal.x>0)
      //su equipo es el OESTE atacamos a la derecha
      factor=1;
    else
      //su equipo es el ESTE atacamos a la izquierda
      factor=-1;
      //Buscamos el compañero que está más cerca a la pelota
    Vec2 cercaBola = new Vec2(99999,0);
    for (int i=0; i< teammates.length; i++){
      Vec2 disBola=new Vec2(ball.x,ball.y);
      disBola.sub(teammates[i]);
      if (disBola.r < cercaBola.r)
        cercaBola = teammates[i];
    }
    Vec2 disBola=new Vec2(ball.x,ball.y);
    disBola.sub(cercaBola);
    //si la pelota está en campo contrario cerca de la banda izq
    //y no tenemos la bola y ninguno de los compañeros está más
    //cerca que nosotros.
    if (bola.x*factor>=CERO && bola.y<=B_DRCH &&
        !abstract_robot.canKick(curr_time) && disBola.r>ball.r){
      result = kick;
      abstract_robot.setSpeed(curr_time, 1.0);
    }
    //si la pelota está en campo contrario y no tenemos la bola
    //y hay un compañero más cerca de la bola
    else if (bola.x*factor>=CERO && !abstract_robot.canKick(curr_time) &&
             disBola.r<ball.r && bola.y<=B_IZQ) {
      if (ball.r>4*abstract_robot.RADIUS)
        result = kick;
      else {
        Vec2 v = new Vec2(1, 1);
        v.setr(4*abstract_robot.RADIUS);
        v.sett(Math.PI/6);
        Vec2 ir=new Vec2(ball.x,ball.y);
        ir.add(v);
        result = ir;
      }
      abstract_robot.setSpeed(curr_time, 1.0);
    }
    //si la pelota está en campo contrario y podemos disparar
    else if (bola.x*factor>=CERO && abstract_robot.canKick(curr_time) && porteria.r<D_TIRO) {
      abstract_robot.setSteerHeading(curr_time,porteria.t);
      abstract_robot.kick(curr_time);
      abstract_robot.setSpeed(curr_time, 1.0);
    }
    //si tenemos el balón en campo contrario y no estamos suficientemente cerca
    else if (bola.x*factor>=CERO && abstract_robot.canKick(curr_time)){
      result=kick;
      abstract_robot.setSteerHeading(curr_time,porteria.t);
      abstract_robot.setSpeed(curr_time, 1.0);
    }
    else if (bola.x*factor<=-CERO) {
      result=kick;
      if (abstract_robot.canKick(curr_time)) {
        abstract_robot.setSteerHeading(curr_time,porteria.t);
        abstract_robot.kick(curr_time);
      }
      abstract_robot.setSpeed(curr_time, 1.0);
    }
    else
      abstract_robot.setSpeed(curr_time, 0.0);
    abstract_robot.setSteerHeading(curr_time, result.t);
    // tell the parent we're OK
    return(CSSTAT_OK);
  }


  //Comportamiento para el delantero
  public int delantero(){
    Vec2  result = new Vec2(0,0);
    long  curr_time = abstract_robot.getTime();
    Vec2 ball = abstract_robot.getBall(curr_time);
    Vec2 ourgoal = abstract_robot.getOurGoal(curr_time);
    Vec2 theirgoal = abstract_robot.getOpponentsGoal(curr_time);
    Vec2[] teammates = abstract_robot.getTeammates(curr_time);
    Vec2 closestteammate = new Vec2(99999,0);
    for (int i=0; i< teammates.length; i++){
      if (teammates[i].r < closestteammate.r)
        closestteammate = teammates[i];
    }
    /*--- now compute some strategic places to go ---*/
    // compute a point one robot radius
    // behind the ball.
    Vec2 kickspot = new Vec2(ball.x, ball.y);
    kickspot.sub(theirgoal);
    kickspot.setr(abstract_robot.RADIUS);
    kickspot.add(ball);

    // compute a point three robot radii
    // behind the ball.
    Vec2 backspot = new Vec2(ball.x, ball.y);
    backspot.sub(theirgoal);
    backspot.setr(abstract_robot.RADIUS*5);
    backspot.add(ball);

    // compute a north and south spot
    Vec2 northspot = new Vec2(backspot.x,backspot.y+0.7);
    Vec2 southspot = new Vec2(backspot.x,backspot.y-0.7);

    // compute a position between the ball and defended goal
    Vec2 goaliepos = new Vec2(ourgoal.x + ball.x,
                              ourgoal.y + ball.y);
    goaliepos.setr(goaliepos.r*0.5);

    // a direction away from the closest teammate.
    Vec2 awayfromclosest = new Vec2(closestteammate.x,
                                    closestteammate.y);
    awayfromclosest.sett(awayfromclosest.t + Math.PI);


    Vec2 pos=abstract_robot.getPosition(curr_time);
    //calculamos el vector que apunta a la portería desde donde
    //está la bola
    Vec2 porteria=abstract_robot.getOpponentsGoal(curr_time);
    porteria.sub(ball);
    //calculamos de una forma mejor la posicion a colocarnos
    Vec2 kick=new Vec2(-porteria.x,-porteria.y);
    kick.setr(abstract_robot.RADIUS);
    kick.add(ball);
    int factor;
    if (theirgoal.x>0)
      //su equipo es el OESTE atacamos a la derecha
      factor=1;
    else
      //su equipo es el ESTE atacamos a la izquierda
      factor=-1;
      //si estamos en campo contrario y no tiene el balón
    if (pos.x*factor > 0 && !abstract_robot.canKick(curr_time)) {
      result = kick;
      //abstract_robot.setSteerHeading(curr_time, result.t);
    }
    //si estamos en nuestro campo y no tenemos el balón
    else if (!abstract_robot.canKick(curr_time)) {
      result=new Vec2(-pos.x,-pos.y);
      //abstract_robot.setSteerHeading(curr_time, result.t);
    }
    //si tenemos el balón y estamos suficientemente cerca
    else if (abstract_robot.canKick(curr_time) && porteria.r<D_TIRO) {
      abstract_robot.setSteerHeading(curr_time,porteria.t);
      long ang=(long)Math.PI/6;
      abstract_robot.setSteerHeading(ang, result.t);
      abstract_robot.kick(curr_time);
    }
    //si tenemos el balón y no estamos suficientemente cerca
    else if (abstract_robot.canKick(curr_time)){
      result=kick;
      abstract_robot.setSteerHeading(curr_time,porteria.t);
    }
    abstract_robot.setSpeed(curr_time, 1.0);
    abstract_robot.setSteerHeading(curr_time, result.t);

    return(CSSTAT_OK);
  }

  //Indica si un enemigo ya está bloqueando para que 2 jugadores no se crean que
  //estan bloqueados por 2.
  public boolean estaOcupado(Vec2 pesado){
    boolean esta=false;
    Vec2[] amigos = abstract_robot.getTeammates(abstract_robot.getTime());
    Vec2 cercano = new Vec2(99999,0);

    for(int j=0;j<bloqueadores.size();j++){
      Vec2 actual = (Vec2)bloqueadores.elementAt(j);
      if(pesado.x==actual.x && pesado.y==actual.y) esta=true;
    }

    return esta;
  }


  public Vec2 dameEnemigoMasCercano(){
    Vec2[] pesados = abstract_robot.getOpponents(abstract_robot.getTime());
     Vec2 elPesado = new Vec2(99999,0);
     for (int i=0;i<pesados.length;i++){
       if((pesados[i].r<elPesado.r)) {
         elPesado=pesados[i];
       }
     }
     return elPesado;
  }


  public boolean estoyBloqueado(int num){
    boolean bloqueado=false;
    long curr_time=abstract_robot.getTime();
    Vec2 vPosicion = abstract_robot.getPosition(curr_time);
    Vec2 elPesado = this.dameEnemigoMasCercano();

   //Esto creo q ya no hace nada
   boolean sigue=true;
   Vec2 elNuevoPesado;
   for(int t=0;t<10000;t++){
     elNuevoPesado = this.dameEnemigoMasCercano();
     sigue = sigue && elPesado.x==elNuevoPesado.x && elPesado.y==elNuevoPesado.y;
   }

   //Ver si el pájaro lleva la pelotaaaaaaaaa!
   Vec2 balon = abstract_robot.getBall(curr_time);
   if (balon.r>0.2) sigue= true;
     else {sigue = false;
        b(num, false);
           };

    //Si tenemos un enemigo cerca y sin balón es q nos bloquea
    if(elPesado.r<0.1 && sigue) {
      //System.out.println("Estoy bloqueado");
      b(num, true);
      portero=(num+1)%5;
      bloqueado=true;
      //neutralizarlo en una esquina
      elPesado.add(vPosicion);

      double supos=elPesado.y;
      double mipos=vPosicion.y;

      if((mipos>supos && mipos>=0) || (mipos>supos && mipos<0))
         abstract_robot.setSteerHeading(curr_time, (90 * Math.PI) / 180.0d);
      else if ((mipos<supos && mipos<0) || (mipos<supos && mipos>=0))
          abstract_robot.setSteerHeading(curr_time, (270 * Math.PI) / 180.0d);
    }

    return bloqueado;
  }

  //Indica si un jugador esta "bloqueado" o no
  public void actualizaJugador(int num){
    Vec2 elPesado = this.dameEnemigoMasCercano();
    long curr_time=abstract_robot.getTime();
    Vec2 balon = abstract_robot.getBall(curr_time);
    boolean sigue;
    Vec2 vPosicion=abstract_robot.getPosition(curr_time);

    //balón lejos
    if (balon.r>0.2) sigue= true;
    else sigue = false;

    if(elPesado.r<0.1 && sigue) {
      b(num,true);
      elPesado.add(vPosicion);

      double supos=elPesado.y;
      double mipos=vPosicion.y;

      if((mipos>supos)){
        abstract_robot.setSteerHeading(curr_time, (90 * Math.PI) / 180.0d);
      }
      else
        abstract_robot.setSteerHeading(curr_time, (270 * Math.PI) / 180.0d);
        abstract_robot.setSpeed(curr_time,1);
    }


    else b(num,false);
  }

  //Indica si nos bloquea uno del propio equipo
  public boolean bloqueaCompañero(int num){
    Vec2 compa = new Vec2(9999,0);
    long curr_time=abstract_robot.getTime();
    Vec2 vPosicion=abstract_robot.getPosition(curr_time);
    boolean resul = false;
    Vec2[] compañeros = abstract_robot.getTeammates(curr_time);

    //Busca al compañero más cercano
    for(int i=0;i<compañeros.length;i++){
      if(compañeros[i].r < compa.r)
        compa=compañeros[i];
    }

    if(compa.r<0.1) {
     resul=true;
    }
    else resul = false;

    return resul;
}




  public void escapa(){
    double angulo = Math.random()*360;
    abstract_robot.setSteerHeading(abstract_robot.getTime(),(angulo * Math.PI) / 180.0d);
  }
  
  
  public int defensa() {
    Vec2 result = new Vec2(0,0);
    long tiempo = abstract_robot.getTime();
    Vec2 bola = abstract_robot.getBall(tiempo);
    Vec2 miPorteria = abstract_robot.getOurGoal(tiempo);
    //la posicion de defensa es el punto medio entre la pelota y el centro de nuestra porteria
    Vec2 posDefensa = new Vec2(miPorteria.x + bola.x, miPorteria.y + bola.y);
    posDefensa.setr(posDefensa.r*0.5);
    Vec2 pos = abstract_robot.getPosition(tiempo);
    //calculamos el vector que apunta a la portería desde donde está la bola
    Vec2 suPorteria = abstract_robot.getOpponentsGoal(tiempo);
    suPorteria.sub(bola);
    //calculamos la posicion a colocarnos para chutar hacia la porteria contraria
    Vec2 chutar = new Vec2(-suPorteria.x,-suPorteria.y);
    chutar.setr(abstract_robot.RADIUS);
    chutar.add(bola);
    
    // si no esta en nuestro campo, se coloca detras del centro del campo
    if (lado == IZQUIERDA && pos.x >= 0) {
      abstract_robot.setDisplayString("Def-volver");
      result = new Vec2(-1, 0);
      //result.sub(pos);
    }
    else if (lado == DERECHA && pos.x <= 0) {
      abstract_robot.setDisplayString("Def-volver");
      result = new Vec2(1, 0);
      //result.sub(pos);
    }
    //si tiene la pelota
    else if (abstract_robot.canKick(tiempo)) {
      //si esta mirando hacia una banda o hacia la porteria contraria despeja
      //deberia comprobar que no hay un rival en esa direccion => SIN HACER
      if ((lado==IZQUIERDA && bola.x>=0) || (lado==DERECHA && bola.y<=0)) {
        abstract_robot.setDisplayString("Def-despejar");
        abstract_robot.kick(tiempo);
      }
      // si la pelota esta mirando hacia su porteria, rodea la pelota para
      // despejarla
      else if ((lado == IZQUIERDA && bola.x < 0)
          || (lado == DERECHA && bola.y > 0)) {
        abstract_robot.setDisplayString("Def-rodear-bola");
        result = posDefensa;
      }
    }
    // si no tiene la pelota y esta cerca, se coloca detras de la pelota
    else if (bola.r < 0.2) {
      abstract_robot.setDisplayString("Def-pelota");
      result = chutar;
    }
    //si la pelota esta lejos
    else {
      abstract_robot.setDisplayString("Def-cerrar");
      result = posDefensa;
    }
    // se orienta hacia el objetivo
    abstract_robot.setSteerHeading(tiempo, result.t);
    abstract_robot.setSpeed(tiempo, 1.0);
    return (CSSTAT_OK);
  }


  //Placa SOLO al portero para jugar sin abusar.
  public int placador(){
    long curr = abstract_robot.getTime();
    Vec2[] enemigos = abstract_robot.getOpponents(curr);
    Vec2 porteriaEnemiga = abstract_robot.getOpponentsGoal(curr);
    Vec2 porteroEnemigo = new Vec2(99999, 0);

    for (int i = 0; i < enemigos.length; i++) {
      Vec2 aux = porteriaEnemiga;
      aux.sub(enemigos[i]);
      if (aux.r < porteroEnemigo.r)
        porteroEnemigo = enemigos[i];
    }
    abstract_robot.setSteerHeading(curr, porteroEnemigo.t);
    abstract_robot.setSpeed(curr, 1);
    return (CSSTAT_OK);
  }

  //Paso de ejecución
  public int TakeStep() {
    //Res.Resultado.Guardar(this,abstract_robot);

    int mynum = abstract_robot.getPlayerNumber(abstract_robot.getTime());
    boolean bc=false;

    //PORTERO
    if (mynum == 0) {
      actualizaJugador(mynum);
      if (!b0) {
        portero(mynum);
      }
      else {
        escapa();
      }
    }

    //DEFENSA
    else if (mynum == 1) {
      actualizaJugador(mynum);
      bc = this.bloqueaCompañero(mynum);
      if(b0)
        portero(mynum);
      else if (!b1)  defensa();
      else if (b1 || bc) escapa();
    }

    //LATERAL IZQUIERDO
    else if (mynum == 2)  {
      actualizaJugador(mynum);
      bc = this.bloqueaCompañero(mynum);
      if (bc || b2) escapa();
      else bandaIzquierda();
    }

    //DELANTERO
    else if (mynum == 3) {
      actualizaJugador(mynum);
      bc = this.bloqueaCompañero(mynum);
      if (bc || b3) escapa();
      else delantero();
    }

     //LATERAL DERECHO
   else if (mynum == 4) {
    actualizaJugador(mynum);
    bc = this.bloqueaCompañero(mynum);
    if (bc || b4) escapa();
    else bandaDerecha();
   }

    return(CSSTAT_OK);
  }

}




