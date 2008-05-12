

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
import java.io.*;
import java.lang.Math;
import EDU.gatech.cc.is.util.Vec2;
import EDU.gatech.cc.is.communication.StringMessage;
import EDU.gatech.cc.is.abstractrobot.*;
import java.util.*;
import EDU.gatech.cc.is.communication.*;



public class RoniesTeam extends ControlSystemSS{

/**************ATRIBUTOS******************/

  private Vec2 result=new Vec2();
  private long curr_time;
  private Vec2 ball, ego_ball,ourgoal, theirgoal, closest, me,closest_team,closest_opp,mipor,supor;
  private Vec2[] teammates,opponents;
  private boolean tira;
  private Vec2 posCubreArriba,posCubreAbajo;

  double GOAL_WIDTH =  0.5; //?
  public static final double ROBOT_RADIUS = 0.06; // should refer to
  // actual value, but is hardcoded for convenience

  public RoniesTeam() {
  }
  /**************METODOS AUXILIARES******************/

  static Vec2 toward( Vec2 a, Vec2 b )
  {
    Vec2 temp = new Vec2( b.x, b.y );
    temp.sub( a );
    return temp;
  }

  boolean i_am_on_east_team()
  {
    return !i_am_on_west_team();
  }

  boolean i_am_on_west_team()
  {
    Vec2 ourgoal2 = new Vec2( ourgoal.x, ourgoal.y );
    ourgoal2.add( me );
    return ourgoal2.x < 0;
  }

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


  private void drive_ball()
  {
        Vec2 TargetSpot = new Vec2(ego_ball.x, ego_ball.y);
        Vec2 GoalSpot = new Vec2(theirgoal.x, theirgoal.y);
        if(me.y > 0) GoalSpot.y += 0.9 * (GOAL_WIDTH / 2.0);
        if(me.y < 0) GoalSpot.y -= 0.9 * (GOAL_WIDTH / 2.0);
        TargetSpot.sub(GoalSpot);
        TargetSpot.setr(ROBOT_RADIUS);
        TargetSpot.add(ego_ball);


        if(Math.abs(theirgoal.r) < 1) tira = true;
        else tira = false;

        result=TargetSpot;
  }

   boolean closestTo2(Vec2 yo, Vec2 SpotAbs)
    {
        // Stolen from Kechze
        Vec2 temp = new Vec2( yo.x, yo.y);
        temp.sub(SpotAbs);

        double MyDist = temp.r;
        for (int i=0; i< teammates.length; i++)
            {
                temp = new Vec2( teammates[i].x, teammates[i].y);
                temp.add(yo);
                temp.sub(SpotAbs);
                double TheirDist = temp.r;
                if (TheirDist <= MyDist)
                    return false;
            }
        return true;
    }

    public boolean bloqueado(){
        return (closest_opp.r < ROBOT_RADIUS*1.15 && closest_opp.r!=0);
    }


    public boolean marcadoCicinho(){
      boolean marcado=false;
      Enumeration r = abstract_robot.getReceiveChannel();
      while (r.hasMoreElements() && !marcado){
        StringMessage mens= (StringMessage)r.nextElement();
        marcado=mens.val.equals("Marcado Cicinho");
      }
      return marcado;
    }

    public boolean marcadoSergio(){
      boolean marcado=false;
      Enumeration r = abstract_robot.getReceiveChannel();
      while (r.hasMoreElements() && !marcado){
        StringMessage mens= (StringMessage)r.nextElement();
        marcado=mens.val.equals("Marcado Sergio");
      }
      return marcado;
    }

    public void porteroEntero(){
      double x,y=0;
      x=mipor.x;
      if(ball.y > mipor.y - GOAL_WIDTH/2 && ball.y < mipor.y + GOAL_WIDTH/2){
        y=ball.y;
      }
      else if(ball.y < mipor.y - GOAL_WIDTH/2){
        y=(mipor.y - GOAL_WIDTH/2 - 0.07);
      }else{
        y=(mipor.y + GOAL_WIDTH/2 + 0.07);
      }
      result=toward(me,new Vec2(x,y));
    }

   /**************INICIACIÓN DE VARIABLES******************/

   public void configure()
   {

   }

   private void update_env( )
   {
     Vec2 closest;

     // get the current time for timestamps
     curr_time = abstract_robot.getTime();
     me= abstract_robot.getPosition(curr_time);
     // get vector to the ball
     ego_ball = abstract_robot.getBall(curr_time);
     ball = new Vec2( ego_ball.x, ego_ball.y );
     ball.add( me );

     // get vector to our and their goal
     ourgoal = abstract_robot.getOurGoal(curr_time);
     mipor=new Vec2(ourgoal.x,ourgoal.y);
     mipor.add(me);
     theirgoal = abstract_robot.getOpponentsGoal(curr_time);
     supor=new Vec2(theirgoal.x,theirgoal.y);
     supor.add(me);

     // get a list of the positions of our teammates
     teammates = abstract_robot.getTeammates(curr_time);
     // get a list of the positions of the opponents
     opponents = abstract_robot.getOpponents(curr_time);

     // get closest data
     closest_team = closest_to( me, teammates);
     closest_opp = closest_to( me, opponents);

     // set movement data: rotation and speed;
     result.sett(0.0);
     result.setr(0.0);

     // set kicking
     tira = false;

     if(i_am_on_east_team()){
       posCubreArriba=new Vec2(ball.x+0.2,ball.y+ROBOT_RADIUS);
       posCubreAbajo=new Vec2(ball.x+0.2,ball.y-ROBOT_RADIUS);
     }
     else{
       posCubreArriba=new Vec2(ball.x-0.2,ball.y+ROBOT_RADIUS);
       posCubreAbajo=new Vec2(ball.x-0.2,ball.y-ROBOT_RADIUS);
     }
   }


   /**************METODOS DE LOS JUGADORES******************/

   public void sergioRamos(){
     abstract_robot.setDisplayString("Sergio Ramos");

     if(bloqueado()){
//       abstract_robot.broadcast(new StringMessage("Marcado Sergio"));
       result=toward(me,new Vec2(me.x,mipor.y+0.15));
     }
     else{
       double x,y;
       x=mipor.x;
/*       if(marcadoCicinho()){
         porteroEntero();
       }
       else{*/
         if( ball.y > 0 && ball.y < mipor.y + GOAL_WIDTH/2){
           y=ball.y;
         }
         else if(ball.y > 0){
           y=(mipor.y + GOAL_WIDTH/2 + 0.07);
         }
         else{
           y=(mipor.y);
         }
         result=toward(me,new Vec2(x,y));
//       }
     }
   }

     public void cicinho(){
       abstract_robot.setDisplayString("Cicinho");
       if(bloqueado()){
//        abstract_robot.broadcast(new StringMessage("Marcado Cicinho"));
         result=toward(me,new Vec2(me.x,mipor.y - 0.15));
       }
       else{
         double x,y;
         x=mipor.x;
/*        if(marcadoSergio()){
           porteroEntero();
           System.out.println("PORTERO ENTERO");
         }
         else{*/
           if( ball.y < 0 && ball.y > mipor.y - GOAL_WIDTH/2){
             y=ball.y;
           }
           else if(ball.y < 0){
             y=(mipor.y - GOAL_WIDTH/2 - 0.07);
           }
           else{
             y=(mipor.y);
           }
           result=toward(me,new Vec2(x,y));
//         }
       }
     }


  public void beckham( ){
    abstract_robot.setDisplayString("Beckham");
    boolean encontrado=false;

    if(closestTo2(me, ball))
    {
      drive_ball();
    }
    else
    {
      Enumeration e=this.abstract_robot.getReceiveChannel();
      while (e.hasMoreElements()&&!encontrado){

        StringMessage mens=(StringMessage)e.nextElement();
        if (mens.val.equals("cubroArriba"))
          encontrado=true;
      }
      if (encontrado)
        result=toward(me,posCubreAbajo);
      else {
        result=toward(me,posCubreArriba);
        this.abstract_robot.broadcast(new StringMessage("cubroArriba"));
      }

    }

  }
  public void rony( ){

    abstract_robot.setDisplayString("Ronaldo");
    boolean encontrado=false;

    if(closestTo2(me, ball))
    {
      drive_ball();
    }
    else
    {
      Enumeration e=this.abstract_robot.getReceiveChannel();
      while (e.hasMoreElements()&&!encontrado){

        StringMessage mens=(StringMessage)e.nextElement();
        if (mens.val.equals("cubroArriba"))
          encontrado=true;
      }
      if (encontrado)
        result=toward(me,posCubreAbajo);
      else {
        result=toward(me,posCubreArriba);
        this.abstract_robot.broadcast(new StringMessage("cubroArriba"));
      }

    }
  }

  public void robinho( ){
    abstract_robot.setDisplayString("Robinho");
    boolean encontrado=false;

    if(closestTo2(me, ball))
    {
      drive_ball();
    }
    else
    {
      Enumeration e=this.abstract_robot.getReceiveChannel();
      while (e.hasMoreElements()&&!encontrado){

        StringMessage mens=(StringMessage)e.nextElement();
        if (mens.val.equals("cubroArriba"))
          encontrado=true;
      }
      if (encontrado)
        result=toward(me,posCubreAbajo);
      else {
        result=toward(me,posCubreArriba);
        this.abstract_robot.broadcast(new StringMessage("cubroArriba"));
      }

    }
  }
/**************TAKE STEP******************/

  public int TakeStep(){

    //Res.Resultado.Guardar(this,abstract_robot);
    // the eventual movement command is placed here

    int my_number = this.abstract_robot.getPlayerNumber(curr_time);

    update_env();

    switch (my_number) {
      //cicinho
      case 0:cicinho();
        break;
        //sergio ramos
      case 1:sergioRamos();
        break;
        //beckham
      case 2:beckham();
        break;
        //rony!!!!!!
      case 3:rony();
        break;
        //robinho
      case 4:robinho();
        break;
        //Zidane ya sa retirao..........
    }

    // set the heading
    abstract_robot.setSteerHeading(curr_time, result.t);

    // set speed at maximum
    abstract_robot.setSpeed(curr_time, 1);

    // maybe kick it
    if (tira && abstract_robot.canKick( curr_time))
      abstract_robot.kick(curr_time);

    return CSSTAT_OK;
  }
}


