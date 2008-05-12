
import	EDU.gatech.cc.is.util.Vec2;
import	EDU.gatech.cc.is.abstractrobot.*;

public class Equipo2 extends ControlSystemSS{
  static final double ANCHO = 1.525;
  static final double LARGO = 2.74;
  final double radioRob = abstract_robot.RADIUS;
  static final double R_DEF = 0.6;
  long tiempo;
  Vec2[] Miequipo;
  Vec2[] equipoContrario;
  Vec2 posicion;
  Vec2 mov;
  int amigoCercano, contrarioCercano;
  Vec2 contrario_bola,amigo_bola;
  Vec2 bola;
  Vec2 porteriaContraria;
  Vec2 Miporteria;
  Vec2 Suporteria;
  Vec2 posteDerecho;
  Vec2 posteIzquierdo;
  Vec2 MiPosteIzquierdo;
  Vec2 MiPosteDerecho;
  boolean empezado;
  double anchoPorteria = 0.5;
  int ladoCampo;  //lado en el que jugamos -1 oeste +1 este
  Vec2 posicionAnterior;
  long tiempoBola;
  int contTiempo = 0;
  boolean chocando = false;

  /**
        Configure the Avoid control system.  This method is
        called once at initialization time.  You can use it
        to do whatever you like.
        */
  public void Configure(){
    tiempo = abstract_robot.getTime();
    tiempoBola = tiempo;
    if( abstract_robot.getOurGoal(tiempo).x < 0)
      ladoCampo = -1;
    else
      ladoCampo = 1;
    mov = new Vec2(0, 0);
    posicionAnterior = new Vec2(0,0);
  }

  private void leerDatos() {
    tiempo = abstract_robot.getTime();
    Miequipo = abstract_robot.getTeammates(tiempo);
    equipoContrario = abstract_robot.getOpponents(tiempo);
    posicion = abstract_robot.getPosition(tiempo);
    bola = abstract_robot.getBall(tiempo);
    porteriaContraria = abstract_robot.getOpponentsGoal(tiempo);
    Miporteria = abstract_robot.getOurGoal(tiempo);
    Suporteria = abstract_robot.getOpponentsGoal(tiempo);
    amigoCercano = menorDistancia(Miequipo);
    contrarioCercano = menorDistancia(equipoContrario);
    posteIzquierdo = new Vec2(porteriaContraria.x,porteriaContraria.y+anchoPorteria/2);
    posteDerecho = new Vec2(porteriaContraria.x,porteriaContraria.y-anchoPorteria/2);
    MiPosteIzquierdo = new Vec2(Miporteria.x,Miporteria.y+anchoPorteria/2);
    MiPosteDerecho = new Vec2(Miporteria.x,Miporteria.y-anchoPorteria/2);
    contrario_bola = closest_opponent_to(bola);
  }



  /**
        Called every timestep to allow the control system to
        run.
        */
  public int TakeStep()
  {
    //Res.Resultado.Guardar(this,abstract_robot);
    leerDatos();
    empezado = tiempo>0;

    if(empezado){
      int numero=abstract_robot.getPlayerNumber(tiempo);
      if(numero==0) {
        //Portero
        Portero2();
      }
      if(numero==1) {
        //Defensa1
        Portero();
      }
      if(numero==2) {
        //Defensa2
        Defensa1();
      }
      if(numero==3) {
        //Medio
        medioCentro();
      }
      if(numero==4) {
        //Delantero
        DelanteroChupon();
      }
    }
    posicionAnterior = abstract_robot.getPosition(tiempo);
    return (CSSTAT_OK);
  }
//------------------------------------------------------------------------------
// Delantero
  public void Delantero(){
    if (abstract_robot.canKick(tiempo)){
      if ((ladoCampo==1)&&(bola.t>posteIzquierdo.t)&&(bola.t<posteDerecho.t)){
        mov.setx(bola.x);
        mov.sety(bola.y);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
        abstract_robot.kick(tiempo);
      }else if((ladoCampo==-1)&&(bola.t<posteIzquierdo.t)&&(bola.t>posteDerecho.t)){
        mov.setx(bola.x);
        mov.sety(bola.y);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
        abstract_robot.kick(tiempo);
      }
    }
    // Si no puede disparar y está cerca de la bola se mueve hacia la bola.
    else if((bola.r<=0.3)&&(Math.abs(bola.x+porteriaContraria.x)>Math.abs(porteriaContraria.x))){
      //estas de cara a su porteria pero no puedes tirar
      if(bola.y > porteriaContraria.y){
        mov.setx(bola.x+ladoCampo*0.05);
        mov.sety(bola.y + 0.01);
      }
      else if(bola.y < porteriaContraria.y){
        mov.setx(bola.x+ladoCampo*0.05);
        mov.sety(bola.y - 0.01);
      }
      else{
        mov.setx(bola.x);
        mov.sety(bola.y);
      }
      mov.setr(1);
    }
    // Si no puede disparar y está lejos de la bola se mueve hacia la bola.
    else{
      mov.setx(bola.x+(0.1*ladoCampo));
      mov.sety(bola.y);
      abstract_robot.setSteerHeading(tiempo, mov.t);
      abstract_robot.setSpeed(tiempo, 1.0);
    }
  }
//------------------------------------------------------------------------------
  public void DelanteroChupon(){
    if(abstract_robot.canKick(tiempo)){
      if((ladoCampo==1)&&(bola.t>posteIzquierdo.t)&&(bola.t<posteDerecho.t)){
        mov.setx(bola.x);
        mov.sety(bola.y);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
        abstract_robot.kick(tiempo);
      }else if((ladoCampo==-1)&&(bola.t<posteIzquierdo.t)&&(bola.t>posteDerecho.t)){
        mov.setx(bola.x);
        mov.sety(bola.y);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
        abstract_robot.kick(tiempo);
      }
    }
    else{
      //juega
      if((posicion.x*ladoCampo<0)&&(bola.x*ladoCampo<0)){
        if(bola.y > porteriaContraria.y){
          mov.setx(bola.x+ladoCampo*0.05);
          mov.sety(bola.y + 0.01);
        }
        else if(bola.y < porteriaContraria.y){
          mov.setx(bola.x+ladoCampo*0.05);
          mov.sety(bola.y - 0.01);
        }
        else{
          mov.setx(bola.x);
          mov.sety(bola.y);
        }
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
      }else{//Se coloca
        mov.sety(-0.4);
        mov.setx(-0.3*ladoCampo);
        mov.sub(posicion);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
      }
    }
    evitarChoques(equipoContrario);
    abstract_robot.setSteerHeading(tiempo, mov.t);
    abstract_robot.setSpeed(tiempo, 1.0);
  }


//------------------------------------------------------------------------------
  public void Defensa1(){
    int objetivo = masCercaNuestra(equipoContrario);//contrario mas cercano a mi porteria
    mov = equipoContrario[objetivo];//coge como posicion siguiente la pos objetivo
    abstract_robot.setSteerHeading(tiempo, mov.t);
    abstract_robot.setSpeed(tiempo, 1.0);
  }
//------------------------------------------------------------------------------
  private void Defensa2(){//defensa
    //si la bola lejos
    if(bola.r>0.6){
      mov.setx(Miporteria.x+((ANCHO/4)*-ladoCampo));//el campo dividido entre 4
      if(bola.y>=Miporteria.y+(anchoPorteria/2)){//como el portero la y
        mov.sety(Miporteria.y+(anchoPorteria/2));
      }
      else if(bola.y<=Miporteria.y-(anchoPorteria/2)){
        mov.sety(Miporteria.y-(anchoPorteria/2));
      }
      else mov.sety(bola.y);
      abstract_robot.setSteerHeading(tiempo, mov.t);
      abstract_robot.setSpeed(tiempo, 1.0);
    }
    //si la bola esra cerca
    else {
      if(abstract_robot.canKick(tiempo)&&i_am_closest_to(bola)){
        mov = find_around_ball(behindball());
        //behind ball clacula la posicion de la bola por detras
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
        abstract_robot.kick(tiempo);
      }
      else if(i_am_closest_to(bola)){
        mov = find_around_ball(behindball());
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
      }
      else{//me enfrento al que tenga la bola
        mov.setx(contrario_bola.x);
        mov.sety(contrario_bola.x);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
      }
    }
    abstract_robot.setDisplayString("Baresi");
  }
//------------------------------------------------------------------------------
/**
       returns a vector to get around the ball to the behindspot without bumping
the ball
*/
  private Vec2 find_around_ball (Vec2 spotfromball){
    Vec2 me = new Vec2(0,0);
    Vec2 behindspot = new Vec2(spotfromball);
    behindspot.setr(radioRob*0.3);
    behindspot.add(bola);
    if(Math.abs(normalizeZero(spotfromball.t-hacia(bola,me).t))>Math.PI/2){
      if (normalizeZero(behindspot.t-bola.t) >0){
        behindspot.rotate(1.2*Math.asin(Math.min(1,radioRob/bola.r)));
        abstract_robot.setDisplayString("Turn 'left");
      }
      else{
        behindspot.rotate(-1.2*Math.asin(Math.min(1,radioRob/bola.r)));
        abstract_robot.setDisplayString("Turn 'right");
      };
    }
    else
      if ((behindspot.r<radioRob)&&tengo_bola()){
    double dribble_cheat = 0.5;
    if (normalizeZero(behindspot.t-bola.t)>0)
      behindspot.rotate(dribble_cheat);
    else
      behindspot.rotate(-dribble_cheat);
    abstract_robot.setDisplayString("Dribble");
      }
      return behindspot;
  }
//------------------------------------------------------------------------------
  private double normalizeZero(double angle){
    while (angle>Math.PI)
      angle -= 2*Math.PI;
    while (angle<-Math.PI)
      angle += 2*Math.PI;
    return angle;
  }
//------------------------------------------------------------------------------
  private boolean tengo_bola(){
    Vec2 spot = new Vec2(bola);
    return(i_am_closest_to(bola)&&(Math.abs(normalizeZero(Suporteria.t-bola.t))<Math.PI/2));
  }
//------------------------------------------------------------------------------
  /*Devuelve al rival más cercano a nuestra porteria*/
  private int masCercaNuestra(Vec2[] equi) {
    int mejor = -1;
    double minDist = Double.MAX_VALUE;
    Vec2 temp=new Vec2();
    for (int i = 0; i < equi.length; i++) {
      temp.setx(Miporteria.x);
      temp.sety(Miporteria.y);
      temp.sub(equi[i]);
      if (temp.r < minDist) {
        minDist = temp.r;
        mejor = i;
      }
    }
    return mejor;
  }
//-------------------------------------------------------------------------------
  /*Devuelve al rival más cercano a mi*/
  private int masCercaMio(Vec2[] equi) {
    int mejor = -1;
    double minDist = Double.MAX_VALUE;
    Vec2 temp=new Vec2();
    for (int i = 0; i < equi.length; i++) {
      temp.setx(posicion.x);
      temp.sety(posicion.y);
      temp.sub(equi[i]);
      if (temp.r < minDist) {
        minDist = temp.r;
        mejor = i;
      }
    }
    return mejor;
  }
//-------------------------------------------------------------------------------

  private void evitarChoques(Vec2[] equi) {
    // an easy way to avoid collision

    // if the closest opponent is too close, move away to try to
    // go around
    try {
      int cerca = masCercaMio(equi);
      if (equi[cerca].r < abstract_robot.RADIUS * 1.4) {
        mov.setx( -equi[cerca].x);
        mov.sety( -equi[cerca].y);
        mov.setr(1.0);
      }
    } catch (ArrayIndexOutOfBoundsException ex) {
    }
  }
//-------------------------------------------------------------------------------
  public void Portero(){
    if(abstract_robot.canKick(tiempo)&&(Math.abs(bola.x+porteriaContraria.x)>Math.abs(porteriaContraria.x))){
      mov.setx(bola.x);
      mov.sety(bola.y);
      abstract_robot.setSteerHeading(tiempo, mov.t);
      abstract_robot.setSpeed(tiempo, 1.0);
      abstract_robot.kick(tiempo);
    }
    else{
      double contx = equipoContrario[contrarioCercano].x;
      double conty = equipoContrario[contrarioCercano].y;
      if(bola.y>MiPosteIzquierdo.y){
        double px = MiPosteIzquierdo.x;
        double py = MiPosteIzquierdo.y;
        px = (px + contx)/2;
        py = (py + conty)/2;
        mov.setx(px);
        mov.sety(py);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
      }
      else if(bola.y<MiPosteDerecho.y){
        double px = MiPosteDerecho.x;
        double py = MiPosteDerecho.y;
        px = (px + contx)/2;
        py = (py + conty)/2;
        mov.setx(px);
        mov.sety(py);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);

      }
      else{
        double px = Miporteria.x;
        double py = Miporteria.y;
        px = (px + contx) / 2;
        py = (py + conty) / 2;
        mov.setx(px);
        mov.sety(py);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
      }
    }
  }
//-----------------------------------------------------------------------------
  Vec2 GoonMode()
  {
    abstract_robot.setDisplayString("Goon");
    Vec2 Victim = new Vec2(99999,0);
    Vec2 CmdReturn;
    for(int i=0; i < equipoContrario.length; i++)
    {
      if(Undefended(equipoContrario[i]) && (equipoContrario[i].r < Victim.r))
        Victim = equipoContrario[i];
    }
    return(Victim);
  }

  boolean Undefended(Vec2 opponent)
  {
    Vec2 AbsOpp = EgoToAbs(opponent);
    // return true if there is no teammate within
    //  DEFENDED_DISTANCE of opponent.
    for(int i = 0; i < Miequipo.length; i++)
    {
      Vec2 AbsPos = EgoToAbs(Miequipo[i]);
      Vec2 DiffPos = new Vec2(AbsOpp.x - AbsPos.x, AbsOpp.y - AbsPos.y);

      if(DiffPos.r < 2 * radioRob + 0.6) return(false);
    }
    return(true);
  }

  Vec2 EgoToAbs(Vec2 EgoPos)
  {
    Vec2 AbsPosition = new Vec2(EgoPos.x, EgoPos.y);
    AbsPosition.add(posicion);
    return(AbsPosition);
  }
//------------------------------------------------------------------------------
  public void Portero2(){
    if(abstract_robot.canKick(tiempo)){
      mov.setx(bola.x);
      mov.sety(bola.y);
      abstract_robot.setSteerHeading(tiempo, mov.t);
      abstract_robot.setSpeed(tiempo, 1.0);
      abstract_robot.kick(tiempo);
    }
    // No puede sacar
    else if(bola.y > MiPosteIzquierdo.y){ // Bola por encima del poste Izquierdo
      mov.setx(Miporteria.x);
      mov.sety(Miporteria.y + anchoPorteria/2);
    }
    else if(bola.y < MiPosteDerecho.y){ // Bola por debajo del poste Izquierdo
      mov.setx(Miporteria.x);
      mov.sety(Miporteria.y - anchoPorteria/2);
    }else{ // Bola a la altura del poste Izquierdo
      mov.setx(Miporteria.x);
      mov.sety(bola.y);
    }
    abstract_robot.setSteerHeading(tiempo, mov.t);
    abstract_robot.setSpeed(tiempo, 1.0);
  }
//------------------------------------------------------------------------------
  //PORTERO CONCHI
  public void PorteroConchi(){
    //si la bola esta lejos del portero
    if (bola.r > 0.4) {
      //el portero s situa en el en la coordenada x de su portaria
      mov.setx(Miporteria.x);
      //y la coordenada y en la y de la bola pero sin salirse de la porteria
      if (bola.y >= MiPosteIzquierdo.y) {
        mov.sety(Miporteria.y + (anchoPorteria / 2));
      }
      else if (bola.y <= MiPosteDerecho.y) {
        mov.sety(Miporteria.y - ( (anchoPorteria / 2)));
      }
      else {
        mov.sety(bola.y);
      }
      abstract_robot.setSteerHeading(tiempo, mov.t);
      abstract_robot.setSpeed(tiempo, 1.0);
    }
    //si la bola esta cerca, a una distancia menor de 0.4 entonmces la despeja
    else
      if (abstract_robot.canKick(tiempo) && i_am_closest_to(bola)) {
    //este es el caso en el que soy el mas cercano a la bola y le puedo dar
    mov.setx(bola.x);
    mov.sety(bola.y);
    abstract_robot.setSteerHeading(tiempo, mov.t);
    abstract_robot.setSpeed(tiempo, 1.0);
    abstract_robot.kick(tiempo);
      }
    /* else if(i_am_closest_to(bola)){//soy el mas cercano-> voy hacia la bola
       mov.setx(bola.x);
       mov.sety(bola.y);
       abstract_robot.setSteerHeading(tiempo, mov.t);
       abstract_robot.setSpeed(tiempo, 1.0);
      }*/
      //voy a por el enemigo que tiene la bola
      else if(i_am_closest_to(contrario_bola)){
        mov.setx(contrario_bola.x);
        mov.sety(contrario_bola.x);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
      }
    /* else{
       mov.setx(contrario_bola.x);
       mov.sety(contrario_bola.x);
       abstract_robot.setSteerHeading(tiempo, mov.t);
       abstract_robot.setSpeed(tiempo, 1.0);

     }*/


  }

  //----------------
  private void pesao(){//(estorba al enemigo mas cercano a la bola)
    mov = closest_opponent_to(bola);
    abstract_robot.setSteerHeading(tiempo, mov.t);
    abstract_robot.setSpeed(tiempo, 1.0);
  }

//------------------------------------------------------------------------------
  private Vec2 closest_opponent_to(Vec2 spot){
    double closest_r;
    Vec2 me = new Vec2(0,0);
    Vec2 closest = me;
    closest_r =distancia(me,spot);
    for (int i=0; i< equipoContrario.length; i++){
      if (distancia(equipoContrario[i],spot)<closest_r){
        closest = equipoContrario[i];
        closest_r=distancia(closest,spot);
      }
    }
    return closest;
  }

//---------------------------------------------------------------------------
  private boolean i_am_closest_to(Vec2 spot){
    Vec2 behind = behindball();
    behind.setr(2*radioRob);
    behind.add(bola);
    return ((distancia(closest_teammate_to(spot),spot) > spot.r*0.9)||
            (distancia(closest_teammate_to(behind),behind)>behind.r*0.9));
  }
//------------------------------------------------------------------------------
/*metodo cogido de uno de los equipos que da ella:Es muy util pq permite a los
   jugadores no solo llevarse la bola sino hacerlo en la direccion que les viene bien
   segun donde deban ir*/
  private Vec2 behindball(){
    Vec2 behind_ball = hacia(bola,Miporteria);
    if (behind_ball.r >R_DEF){	// choose offensive strategy if ball far from our goal
      //  --> behind is calculated with respect to the other goal:
      behind_ball = hacia(Miporteria,bola);
      behind_ball.setr(radioRob*1.0);
    }
    else{
      behind_ball.setr(radioRob*1.5);
    }
    return behind_ball;
  }
//------------------------------------------------------------------------------
  private Vec2 hacia(Vec2 a, Vec2 b) {
    Vec2 temp = new Vec2(b.x, b.y);
    temp.sub(a);
    return temp;
  }
//--------------------------------------------------------------------------
  private double distancia(Vec2 a,Vec2 b){
    Vec2 temp = new Vec2(a);
    temp.sub(b);
    return temp.r;
  }


//------------------------------------------------------------------------------
  private Vec2 closest_teammate_to(Vec2 spot){
    double closest_r;
    Vec2 closest = null;
    closest_r=9999;
    for (int i=0; i< Miequipo.length; i++){
      if (distancia(Miequipo[i],spot)<closest_r){
        closest = Miequipo[i];
        closest_r=distancia(closest,spot);
      }
    }
    return closest;
  }
//------------------------------------------------------------------------------
  private void medioCentro(){
    //Si la bola esta cerca y esta del mismo lado que la porteria contraria
    if(abstract_robot.canKick(tiempo)){
      if((ladoCampo==1)&&(bola.t>posteIzquierdo.t)&&(bola.t<posteDerecho.t)){
        mov.setx(bola.x);
        mov.sety(bola.y);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
        abstract_robot.kick(tiempo);
      }else if((ladoCampo==-1)&&(bola.t<posteIzquierdo.t)&&(bola.t>posteDerecho.t)){
        mov.setx(bola.x);
        mov.sety(bola.y);
        abstract_robot.setSteerHeading(tiempo, mov.t);
        abstract_robot.setSpeed(tiempo, 1.0);
        abstract_robot.kick(tiempo);
      }
    }
    else if((bola.r<=0.3)&&(Math.abs(bola.x+porteriaContraria.x)>Math.abs(porteriaContraria.x))){
      if(bola.y<porteriaContraria.y){
        mov.setx(bola.x+ladoCampo*0.05);
        mov.sety(bola.y-0.01);
      }
      else{
        mov.setx(bola.x+ladoCampo*0.05);
        mov.sety(bola.y+0.01);
      }
      abstract_robot.setSteerHeading(tiempo, mov.t);
      abstract_robot.setSpeed(tiempo, 1.0);
    }
    else if(bola.r<=0.3){
      mov.setx(bola.x+(0.1*ladoCampo));
      mov.sety(bola.y);
    }
    else{
      Vec2 temp = abstract_robot.getPosition(tiempo);
      mov.setx(0.2*ladoCampo);
      mov.sety(0);
      mov.sub(temp);
      abstract_robot.setSteerHeading(tiempo, mov.t);
      abstract_robot.setSpeed(tiempo, 1.0);
    }
    abstract_robot.setSteerHeading(tiempo, mov.t);
    abstract_robot.setSpeed(tiempo, 1.0);
  }
//------------------------------------------------------------------------------
  private int menorDistancia(Vec2[] equi) {
    int mejor = -1;
    double minDist = Double.MAX_VALUE;
    Vec2 temp=new Vec2();
    for (int i = 0; i < equi.length; i++) {
      temp.setx(bola.x);
      temp.sety(bola.y);
      temp.sub(equi[i]);
      if (temp.r < minDist) {
        minDist = temp.r;
        mejor = i;
      }
    }
    return mejor;
  }
//-------------------------------------------------------------------------------
}