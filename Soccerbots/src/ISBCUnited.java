/*
 * ISBCUnited.java
 *
 * Created on 12 de mayo de 2007, 15:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.util.ArrayList; 
import EDU.gatech.cc.is.util.Vec2;
import EDU.gatech.cc.is.abstractrobot.*;
import	EDU.gatech.cc.is.communication.*;
import	java.util.Enumeration;

/**
 *
 * @author Administrador
 */
public class ISBCUnited extends ControlSystemSS {
    
  // Constantes para desginar los distintos tipos de jugadore
  static final int PORTERO = 0;
  static final int DEFENSA = 1;
  static final int MEDIO = 2;
  static final int DELANTERO = 3;
  static final int BLOQUEADOR = 4;

  private static final double LONGITUD_CAMPO = 2.74;
  private static final double ANCHO_PORTERIA = 0.45;
  private Enumeration messagesin;	//COMMUNICATION
  private double RADIO_ROBOT = abstract_robot.RADIUS;
  int miNumero;
  long tiempo;
  int miCampo, suCampo;
  Vec2 miPorteria, suPorteria, suPosteDerecho, suPosteIzquierdo;
  Vec2 yoAbsoluta;
  Vec2 balon;
  Vec2[] companeros, contrarios;
  Vec2 Centro;


  long ultimo_tiempo;
  Vec2 ultimo_balon;
  
  int[] jugadores;
  

  public void configure() {
      jugadores = new int[5];
      jugadores[0] = PORTERO;
      jugadores[1] = MEDIO;
      jugadores[2] = DELANTERO;
      jugadores[3] = DELANTERO;
      jugadores[4] = DELANTERO;
      
      messagesin = abstract_robot.getReceiveChannel();//COMMUNICATION
      miNumero = abstract_robot.getPlayerNumber(tiempo);
      if (abstract_robot.getOurGoal(tiempo).x < 0)
        miCampo = -1;
      else
        miCampo = 1;

      suCampo = -miCampo;
      
      ultimo_tiempo = 0;
      ultimo_balon = new Vec2(0.0,0.0);
  }

  public int TakeStep() {

    tiempo = abstract_robot.getTime();
    
    yoAbsoluta = new Vec2(abstract_robot.getPosition(tiempo));
    balon = abstract_robot.getBall(tiempo);
    miPorteria = abstract_robot.getOurGoal(tiempo);
    suPorteria = abstract_robot.getOpponentsGoal(tiempo);
    suPosteIzquierdo = new Vec2(suPorteria.x,suPorteria.y-ANCHO_PORTERIA/2.5);
    suPosteDerecho = new Vec2(suPorteria.x,suPorteria.y+ANCHO_PORTERIA/2.5);

    companeros = abstract_robot.getTeammates(-1);
    contrarios = abstract_robot.getOpponents(-1);

    for (int i = 0; i < companeros.length; i++)
      companeros[i].setr(companeros[i].r + RADIO_ROBOT);
    for (int i = 0; i < contrarios.length; i++)
      contrarios[i].setr(contrarios[i].r + RADIO_ROBOT);

    Centro = new Vec2(miCampo*RADIO_ROBOT*3, RADIO_ROBOT/2);
    Centro.sub(yoAbsoluta);
    

    switch (jugadores[miNumero]) {

      case BLOQUEADOR:
        Bloqueador();
        break;

      case PORTERO:
        Portero();
        break;
      
      case DEFENSA:
        Defensa();
        break;

      case DELANTERO:
        Delantero();
        break;

      case MEDIO:
        Medio();
        break;
    }

    ultimo_tiempo = tiempo;  
    ultimo_balon = new Vec2(balon.x, balon.y); 
    ultimo_balon.add(yoAbsoluta);
    
    // Devuelve un código de retorno indicando éxito
    return (CSSTAT_OK);
  }

  private void Portero() {

       Vec2 resultado = new Vec2(0,0);
       Vec2 BalonRespectoPorteria = new Vec2(balon);
       BalonRespectoPorteria.sub(miPorteria);
       
       //Calculamos la posición entre la porteria y el balón que diste 0.32
       double x = Math.sqrt(0.32*0.32/(1+Math.pow(BalonRespectoPorteria.y/BalonRespectoPorteria.x,2.0)));
       x = x*-miCampo;
       double y = x*BalonRespectoPorteria.y/BalonRespectoPorteria.x;
       resultado.setx(x);
       resultado.sety(y);
       resultado.add(miPorteria);
       double h = Math.sqrt(resultado.x*resultado.x+resultado.y*resultado.y);
       
       //Evita el movimiento constante
       if (h < RADIO_ROBOT*0.4){         
          resultado.setr(0.0);
       }else{
          resultado.setr(1.0);
       }
  
       if (miPorteria.r > BalonRespectoPorteria.r){
	  resultado.sett(balon.t);
	  resultado.setr(1.0);
       }

       
       avoidcollision(resultado);
 
       abstract_robot.setSteerHeading(tiempo, resultado.t);
       abstract_robot.setSpeed(tiempo, resultado.r);
  }
  
  private void Defensa(){
       
       Vec2 resultado = new Vec2(0,0);
       Vec2 BalonRespectoPorteria = new Vec2(balon);
       BalonRespectoPorteria.sub(miPorteria);
       
       if (balon.x*miCampo < 0.4){ 
          //Calculamos la posición entre la porteria y el balón que diste 1.0
         double x = Math.sqrt(1.0*1.0/(1+Math.pow(BalonRespectoPorteria.y/BalonRespectoPorteria.x,2.0)));
         x = x*-miCampo;
         double y = x*BalonRespectoPorteria.y/BalonRespectoPorteria.x;
         resultado.setx(x);
         resultado.sety(y);
         resultado.add(miPorteria);
         double h = Math.sqrt(resultado.x*resultado.x+resultado.y*resultado.y);   
         avoidcollision(resultado);
         abstract_robot.setSteerHeading(tiempo, resultado.t);
         abstract_robot.setSpeed(tiempo, resultado.r);
       }else{
          conducirBalon(); 
       }
        
            
  }


  private void avoidcollision(Vec2 resultado) {
	
     if (resultado.r > 0.0) {
       Vec2 closest_team  = closest_to(new Vec2(0,0),companeros);
       Vec2 closest_opp  = closest_to(new Vec2(0,0),contrarios);
     
       if ((Math.pow(resultado.x-closest_team.x,2.0)+Math.pow(resultado.y-closest_team.y,2.0)) > Math.pow(RADIO_ROBOT,2.0)){
	 if ((miNumero != 0)&&(closest_team.r < RADIO_ROBOT*2.5)&&(molesta(resultado,closest_team))){
           double[] angulos = esquiva(closest_team);

           if ((mejor(resultado,angulos)))
             resultado.sett(angulos[0]);
           else
             resultado.sett(angulos[1]);
	 }

	 if ((closest_opp.r < RADIO_ROBOT*2.5)&&(molesta(resultado,closest_opp))){
           double[] angulos = esquiva(closest_opp);

           if (mejor(resultado,angulos))
             resultado.sett(angulos[0]);
           else
             resultado.sett(angulos[1]);
         }
         
         resultado.setr( 1.0);
       }
     }
  }
  
  private boolean molesta(Vec2 trayectoria, Vec2 jugador){
      double k = trayectoria.y/trayectoria.x;
      double distancia = Math.abs((k*jugador.x)-jugador.y)/Math.sqrt((k*k)+1);
          
      if ((Math.abs(jugador.t-trayectoria.t) <= Math.PI/2)&&(distancia <= RADIO_ROBOT*2.5))
          return true;
      else
          return false;
  }
  
    private boolean molesta2(Vec2 trayectoria, Vec2 jugador){
      double k = trayectoria.y/trayectoria.x;
      double distancia = Math.abs((k*jugador.x)-jugador.y)/Math.sqrt((k*k)+1);
           
      if ((((jugador.x == 0)&&(jugador.y == 0))||(Math.abs(jugador.t-trayectoria.t) <= Math.PI/2))&&(distancia <= RADIO_ROBOT))
          return true;
      else
          return false;
  }
  
  private double distancia_punto_recta(Vec2 trayectoria, double x, double y){
      double k = trayectoria.y/trayectoria.x;
      double distancia = Math.abs((k*x)-y)/Math.sqrt((k*k)+1);
            
      return distancia;
  }
  
   private boolean mejor(Vec2 trayectoria, double[] angulos){
      double k = trayectoria.y/trayectoria.x;
      Vec2 auxiliar1 = new Vec2(trayectoria);
      Vec2 auxiliar2 = new Vec2(trayectoria);
      auxiliar1.sett(angulos[0]);
      double distancia1 = Math.sqrt(Math.pow(trayectoria.x-auxiliar1.x,2.0)+Math.pow(trayectoria.y-auxiliar1.y,2.0));
      auxiliar2.sett(angulos[1]);
      double distancia2 = Math.sqrt(Math.pow(trayectoria.x-auxiliar2.x,2.0)+Math.pow(trayectoria.y-auxiliar2.y,2.0));

      if (distancia1 <= distancia2)
          return true;
      else
          return false;
  }
  
  private Vec2 closest_to( Vec2 point, Vec2[] objects){
      double dist = 9999;
      Vec2 result = new Vec2(0, 0);
      Vec2 temp = new Vec2(0, 0);

      for( int i=0; i < objects.length; i++){
	temp.sett( objects[i].t);
	temp.setr( objects[i].r);
	temp.sub( point);

	if(temp.r < dist){
          result = objects[i];
	  dist = temp.r;
	}
      }

      return result;
  }

  private void Medio() {
    Vec2 resultado;
    Vec2 temp = new Vec2(balon);
    temp.add(yoAbsoluta);
       
    if (MasCercanoBalon()) {
      Delantero();
      return;
    }else {

      resultado = new Vec2(Centro);

      if (resultado.r > RADIO_ROBOT * 2) {
        avoidcollision(resultado);
	abstract_robot.setSteerHeading(tiempo, resultado.t);
	abstract_robot.setSpeed(tiempo, resultado.r);
      }else {
        avoidcollision(resultado);
	abstract_robot.setSteerHeading(tiempo, yoAbsoluta.t + Math.PI);
	abstract_robot.setSpeed(tiempo,0.0);
      }
    }
  }
  
  private void Bloqueador() {
    Vec2 suPortero, suPorteroAbsoluta, resultado;

    Vec2 temp = new Vec2(balon.x, balon.y);
    temp.add(yoAbsoluta);
    temp.sub(ultimo_balon);
    temp.sub(yoAbsoluta);
    
    if ((molesta2(temp,new Vec2(0,0)))&&(balon.r <= 0.25)){
        Vec2 contrario_cercano = closest_to(new Vec2(0,0),contrarios);
        contrario_cercano.setr(contrario_cercano.r+RADIO_ROBOT*3.5);
        abstract_robot.setSpeed(tiempo, 1.0);
        avoidcollision(contrario_cercano);
        abstract_robot.setSteerHeading(tiempo, contrario_cercano.t);
        return;
    }

    if ((MasCercanoBalon())&&(balon.x*miCampo < 0)) {
      Delantero();
      return;
    }else {
      suPortero = contrarios[0];
      double xMaxima = miCampo * LONGITUD_CAMPO;
      for (int i = 0; i < contrarios.length; i++) {
        Vec2 tmp = new Vec2(contrarios[i]);
        tmp.add(yoAbsoluta);
        if ((miCampo == -1 ? tmp.x > xMaxima : tmp.x < xMaxima) &&
            (tmp.y > -ANCHO_PORTERIA / 2 - RADIO_ROBOT * 3) &&
            (tmp.y < ANCHO_PORTERIA / 2 + RADIO_ROBOT * 3)) {
          xMaxima = tmp.x;
          suPortero = contrarios[i];
        }
      }

      suPorteroAbsoluta = new Vec2(suPortero);
      suPorteroAbsoluta.add(yoAbsoluta);
      resultado = new Vec2(suPorteroAbsoluta);

      if (miCampo == -1 ?
          suPorteroAbsoluta.x > LONGITUD_CAMPO / 2 - RADIO_ROBOT * 4 :
          suPorteroAbsoluta.x < -LONGITUD_CAMPO / 2 + RADIO_ROBOT * 5.5) {

        Vec2 suPorteriaAbsoluta = new Vec2(suPorteria);
        suPorteriaAbsoluta.add(yoAbsoluta);


        Vec2 puntoDeBloqueoDelPortero = new Vec2(suPorteriaAbsoluta);
        puntoDeBloqueoDelPortero.setr(puntoDeBloqueoDelPortero.r - RADIO_ROBOT * 2);
        puntoDeBloqueoDelPortero.sub(yoAbsoluta);


        Vec2 PorteroReferidoAlPuntoDeBloqueoDelPortero = new Vec2(suPortero);
        PorteroReferidoAlPuntoDeBloqueoDelPortero.sub(puntoDeBloqueoDelPortero);
        resultado = PorteroReferidoAlPuntoDeBloqueoDelPortero;
        resultado.add(puntoDeBloqueoDelPortero);

        avoidcollision(resultado);
        // set the heading
	abstract_robot.setSteerHeading(tiempo, resultado.t);
        // set the speed
        if (suPortero.r <= 2.1*RADIO_ROBOT)
            abstract_robot.setSpeed(tiempo, 0.0);
        else
	  abstract_robot.setSpeed(tiempo, 1.0);
      }

      else {
        Delantero();
      }
    }
  }

  private void Delantero() {

    Vec2 resultado = new Vec2(0,0);
    Vec2 temp = new Vec2(balon);
    
    temp.add(yoAbsoluta);
    
    if ((temp.x*miCampo > 0)&& !((balon.r < RADIO_ROBOT + 0.04)&&(balon.x*miCampo < 0)&&(Math.abs(balon.y) < RADIO_ROBOT*0.5))){
        //Defensa();
        conducirBalon();
        return;
    }
    
    if (MasCercanoBalon()){
      conducirBalon();
      if ((balon.r < RADIO_ROBOT + 0.04)&&(balon.x*miCampo < 0)&&(Math.abs(balon.y) < RADIO_ROBOT*0.5)){
        if (yoAbsoluta.x*miCampo < 0){
          resultado = new Vec2(buscar_mejor_disparo());
          resultado = calcularPosicion(resultado);
        }
        if (suPorteria.r < 0.3){
            if (abstract_robot.canKick(tiempo)) 
               abstract_robot.kick(tiempo);
        }
     }
    }else {
      Vec2 leftwing;
      Vec2 rightwing;
      StringMessage m = new StringMessage();
      boolean alaIzquierda = false;
      boolean alaDerecha = false;
      Vec2 behind_ball = new Vec2(balon);
      
      behind_ball.sub(suPorteria);
      behind_ball.setr(RADIO_ROBOT);
      
      leftwing = new Vec2(behind_ball);
      leftwing.rotate(-Math.PI/2);
      leftwing.setr(Math.min(distancia(balon,suPorteria)/4+0.15,0.3));
		
      rightwing = new Vec2(behind_ball);	
      rightwing.rotate(Math.PI/2);
      rightwing.setr(Math.min(distancia(balon,suPorteria)/4+0.15,0.3));

      while (messagesin.hasMoreElements()){
	StringMessage recvd = (StringMessage)messagesin.nextElement();
        if ((recvd.val.equals("leftwing"))&&(recvd.sender != miNumero))
          alaIzquierda = true;
        if ((recvd.val.equals("rightwing"))&&(recvd.sender != miNumero))
          alaDerecha = true;
      }
  
      if (alaIzquierda == false){
	behind_ball.add(leftwing);
	behind_ball.add(balon);
	resultado= behind_ball;
	abstract_robot.setDisplayString("leftwing");
        m.val = "leftwing";
	abstract_robot.broadcast(m);                       
      } else if  (alaDerecha == false){
	behind_ball.add(rightwing);
	behind_ball.add(balon);
	resultado= behind_ball;
	abstract_robot.setDisplayString("rightwing");
        m.val = "rightwing";
        abstract_robot.broadcast(m);
      }else{
        conducirBalon(); 
      }
    }
    
    if ((resultado.x != 0)||(resultado.y != 0)){
      avoidcollision(resultado);
      abstract_robot.setSteerHeading(tiempo, resultado.t);
      abstract_robot.setSpeed(tiempo, 1.0); 
    }
 
  }

  private boolean MasCercanoBalon() {
    double dist = balon.r;
    for (int i = 0; i < companeros.length; i++) {
      Vec2 aux = new Vec2(balon);
      aux.sub(companeros[i]);
      if (aux.r <= dist)return false;
    }
    return true;
  }

  
  private double[] esquiva(Vec2 jugador) {

   double[] angulos = new double[2];

   
   angulos[0] = jugador.t + (Math.PI/2);
   angulos[1] = jugador.t - (Math.PI/2);
   
   Vec2 aux = new Vec2(jugador);
   aux.sett(jugador.t + (Math.PI/2));
   
   if ((Math.abs(yoAbsoluta.x)+1.1*RADIO_ROBOT)>(LONGITUD_CAMPO/2)){
     aux.sett(jugador.t + (Math.PI/2));
     if (aux.x*yoAbsoluta.x > 0){
        angulos[0] = angulos[1];
      abstract_robot.setDisplayString("CAMBIO");
     }
     
     aux.sett(jugador.t - (Math.PI/2));
     if (aux.x*yoAbsoluta.x > 0){
        angulos[1] = angulos[0];
        abstract_robot.setDisplayString("CAMBIO_2");
     }
   }
  
   return angulos;
  }
  
  public void conducirBalon(){
          
     Vec2 posChute;
           
     posChute = new Vec2(balon);
     posChute.sub(suPorteria);
     posChute.setr(0.054);
     posChute.add(balon);
            
     if(!abstract_robot.canKick(tiempo)){
       abstract_robot.setDisplayString("A por el balon");
       Vec2 resultado = new Vec2();
       resultado.sett(this.buscarCamino(posChute));
       resultado.setr(1.0);
       avoidcollision(resultado);
       abstract_robot.setSteerHeading(tiempo,resultado.t );
       abstract_robot.setSpeed(tiempo, 1.0);
     }
     
  }
         
 public double buscarCamino(Vec2 posicion) {

        final double ANG_LIM = Math.PI / 6;
        final double ANG_INC = Math.PI / 180;
        double menorCosto;
        double anguloMenorCosto;
        Vec2[] obstaculos;
        int i;
       
        menorCosto = Double.MAX_VALUE;
        anguloMenorCosto = 0;

        obstaculos = new Vec2[contrarios.length + companeros.length ];
        for (i = 0; i < companeros.length; i++) {
            obstaculos[i] = companeros[i];
        }       
        for (int j = 0; j < contrarios.length; j++) {
            obstaculos[i] = contrarios[j];
            i++;
        }
        
        for (int r = -(int)(ANG_LIM/ANG_INC) ; r < (int)(ANG_LIM/ANG_INC) ; r++) {
            double custo = calculaCosto(posicion, obstaculos, r * ANG_INC);
            if ( custo < menorCosto ){
                menorCosto = custo;
                anguloMenorCosto = r * ANG_INC;
            }
        }

        if (menorCosto == 0)
            anguloMenorCosto = 0;
             
        return posicion.t+anguloMenorCosto;     
  }
    
  private double calculaCosto(Vec2 posicion, Vec2[] obstaculos, double angDesvio){
           
    Vec2   OP;   
    double costoTotal = 0;
    double costoObstaculo; 
        
    for (int k = 0; k < obstaculos.length; k++) {
      OP = new Vec2(posicion);
      OP.sub(obstaculos[k]);

      if((Math.PI - Math.abs(OP.t-obstaculos[k].t)) > Math.PI / 4 ){
        costoObstaculo = ( 1 / (obstaculos[k].r * obstaculos[k].r ))*(Math.cos(posicion.t + angDesvio - obstaculos[k].t ));
        if (costoObstaculo > 0)
          costoTotal = costoTotal + costoObstaculo;           
      }
    }        
    return costoTotal;
  }

  private double calculaCosto2(Vec2 posicion, Vec2[] obstaculos){
         
     Vec2   OP,obstaculo;   
     double costoTotal = 0;
     double distanciaJugador = 0; 
     double m = posicion.y/posicion.x;
       
     OP = new Vec2(posicion);
     for (int k = 0; k < obstaculos.length; k++) {
       obstaculo = new Vec2(obstaculos[k]);
       obstaculo.sub(balon);
       if (molesta2(OP,obstaculo))
         costoTotal++;
     }
     
     for (int k = 0; k < contrarios.length; k++) {
       obstaculo = new Vec2(contrarios[k]);
       obstaculo.sub(balon);
       if (obstaculo.x >= miCampo*(2*RADIO_ROBOT))
         distanciaJugador += distancia_punto_recta(posicion, obstaculo.x, obstaculo.y);  
     } 
     
     return costoTotal-distanciaJugador;
  }

  private double distancia(Vec2 spot1,Vec2 spot2){
     Vec2 temp = new Vec2(spot1);
     temp.sub(spot2);
     return temp.r;
  }

  private Vec2 buscar_mejor_disparo(){
    double tempvalor=0,maxvalor=10000, distancia=10000;
    String donde = "";
    Vec2 mejor_disparo=suPorteria;
    
    Vec2 tempvect  = new Vec2(suPorteria);
    tempvect.sub(/*balon*/predecirBalon(tiempo+(long)200.0));
    if ((tempvalor=evaluar_disparo(tempvect))<=maxvalor) {
	mejor_disparo = new Vec2(tempvect);
	maxvalor=tempvalor;
        distancia = tempvect.r;
	donde = new String("Centro");
    }

    tempvect  = new Vec2(suPosteIzquierdo);
    tempvect.sub(/*balon*/predecirBalon(tiempo+(long)200.0));
    if ((tempvalor=evaluar_disparo(tempvect))<=maxvalor) {
      if ((tempvalor<maxvalor)||((tempvect.r < distancia)&&(tempvalor == maxvalor))){
	mejor_disparo = new Vec2(tempvect);
	maxvalor=tempvalor;
        distancia = tempvect.r;
	donde = new String("P.Inferior");
      }
    }			

    tempvect  = new Vec2(suPosteDerecho);
    tempvect.sub(/*balon*/predecirBalon(tiempo+(long)200.0));
    if ((tempvalor=evaluar_disparo(tempvect))<=maxvalor) {
      if ((tempvalor<maxvalor)||((tempvect.r < distancia)&&(tempvalor == maxvalor))){
	mejor_disparo = new Vec2(tempvect);
	maxvalor=tempvalor;
        distancia = tempvect.r;
	donde = new String("P.Superior");
      }
    }

    abstract_robot.setDisplayString(donde);
    return mejor_disparo;
  }

  private Vec2 calcularPosicion(Vec2 tiro){
    Vec2 pos,prediccion;
    double y = 0;
    double x;
    
    pos = new Vec2(tiro);
    prediccion = new Vec2(predecirBalon(tiempo+(long)200.0));
      
    for (x=0.003*miCampo; Math.sqrt(x*x+y*y)<0.054; x+=miCampo*0.001)
      y = (pos.y/pos.x)*(x-balon.x)+balon.y;
    pos = new Vec2(x,y);
    pos.add(prediccion);
        
    return pos;
  }
	
  private double evaluar_disparo(Vec2 vect){
            
     int i;
     double costo = 0.0;       
     Vec2[] obstaculos = new Vec2[contrarios.length + companeros.length];  
     
     for (i = 0; i < companeros.length; i++)
         obstaculos[i] = companeros[i];
        
      for (int j = 0; j < contrarios.length; j++) {
         obstaculos[i] = contrarios[j];
         i++;
      }

      costo = calculaCosto2(vect, obstaculos);       
      return costo;
  }
			
  Vec2 predecirBalon(long time){
    Vec2 temp = new Vec2(balon.x, balon.y);
    temp.add(yoAbsoluta);
    temp.sub(ultimo_balon);

    long diff_time = tiempo - ultimo_tiempo;
    if (diff_time==0.0) diff_time = (long) 0.0001; 
    temp.setr(temp.r/(diff_time)*(time-tiempo));

    if(temp.r==0) 
      temp = new Vec2(Centro);
    else{
        temp.add(balon);
        temp.add(yoAbsoluta);
    }
    
    temp.sub(yoAbsoluta);
    return temp;
  }
}


  