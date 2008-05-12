/*
 * GoToBall.java
 */

import EDU.gatech.cc.is.util.Vec2;
import EDU.gatech.cc.is.abstractrobot.*;

//import Res.*;

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


public class freakTeam3
    extends ControlSystemSS {
  /**
           Configure the Avoid control system.  This method is
           called once at initialization time.  You can use it
           to do whatever you like.
   */
  private int dir;
  private static long curr_time;
  private Vec2 ball;
  private Vec2 posJug;
  private Vec2 miPorteria, suPorteria;
  private Vec2 PorteriaBalon;
  private Vec2 detrasBalon;
  private Vec2[] teammates;
  private Vec2 closestteammate;
  private Vec2[] opponents;
  private Vec2 closestopponentmate;

  public void Configure() {
    curr_time = abstract_robot.getTime();
    if ( ( (Vec2) abstract_robot.getOurGoal(curr_time)).x < 0) {
      dir = 1;
    }
    else {
      dir = -1;
    }
  }

  /**
           Called every timestep to allow the control system to
           run.
   */
  public int TakeStep() {


//   Res.Resultado.Guardar(this, abstract_robot);

    long curr_time = abstract_robot.getTime();

    int numJug = abstract_robot.getPlayerNumber(curr_time);

    //Posicion del jugador actual
    posJug = abstract_robot.getPosition(curr_time);

    opponents = abstract_robot.getOpponents(curr_time);

    // get a list of the positions of our teammates
    teammates = abstract_robot.getTeammates(curr_time);

    //pos del balon
    ball = abstract_robot.getBall(curr_time);
    miPorteria = abstract_robot.getOurGoal(curr_time);
    suPorteria = abstract_robot.getOpponentsGoal(curr_time);

    //Vector de la porteria contraria al balon
    PorteriaBalon = new Vec2(0, 0);
    PorteriaBalon.add(ball);
    PorteriaBalon.sub(suPorteria);

    //posicion detras del balon
    detrasBalon = new Vec2(0, 0);
    detrasBalon.add(ball);
    PorteriaBalon.setr(abstract_robot.KICKER_SPOT_RADIUS);
    detrasBalon.add(PorteriaBalon);
    closestteammate = new Vec2(99999, 0);
    for (int i = 0; i < teammates.length; i++) {
      if (teammates[i].r < closestteammate.r) {
        closestteammate = teammates[i];
      }
    }

    closestopponentmate = new Vec2(99999, 0);
    for (int i = 0; i < opponents.length; i++) {
      if (opponents[i].r < closestopponentmate.r) {
        closestopponentmate = opponents[i];
      }
    }

    if (numJug == 0) {
      playPortero();
    }

    else if (numJug == 1) {
      if (Math.abs(ball.x - miPorteria.x) < 0.5) {
        playPortero();
      }

      else {
        defensaFerrea();
      }
    }
    else if (numJug == 2) {
      beckenbawer();
    }
    else if (closestteammate.r < 0.08) {
      goAway();
    }


    //Cesc Fabregas
    else if (numJug == 4) {
      cesc();
    }

    else if (closestopponentmate.r < 0.08) {
      goAwayOp();
    }

    //henryyyyyyyyyyyyyyyyyyyyyyy
    else if (numJug == 3) {
      henry();
    }

    // tell the parent we're OK
    return (CSSTAT_OK);
  }

  private void goAway() {
    // a direction away from the closest teammate.
    Vec2 awayfromclosest = new Vec2(closestteammate.x,
                                    closestteammate.y);
    awayfromclosest.sett(awayfromclosest.t + Math.PI);
    abstract_robot.setSteerHeading(curr_time, awayfromclosest.t);
    abstract_robot.setSpeed(curr_time, 1.0);
  }

  private void goAwayOp() {
    // a direction away from the closest teammate.
    Vec2 awayfromclosest = new Vec2(closestopponentmate.x,
                                    closestopponentmate.y);
    awayfromclosest.sett(awayfromclosest.t + Math.PI);
    abstract_robot.setSteerHeading(curr_time, awayfromclosest.t);
    abstract_robot.setSpeed(curr_time, 1.0);
  }

  private void defensaFerrea() {
    //defensa ferrea

    double x = abstract_robot.getPosition(curr_time).x;

    double y = ball.y;

    if (dir==1) {
      if (x < ( -0.9)) {
        x = ball.x;
      }
      else if (x > ( -0.9)) {
        x = ( -0.5 * dir);
      }

    }
    else {
      if (x > (0.9)) {
        x = ball.x;
      }
      else if (x < (0.9)) {
        x = ( -0.5 * dir);
      }
    }

    Vec2 resultado = new Vec2(x, y);

    abstract_robot.setSteerHeading(curr_time, resultado.t);
    abstract_robot.setSpeed(curr_time, 1.0);
    if(dir==1)
    {
      if (abstract_robot.canKick(curr_time) && (ball.x > posJug.x))
        abstract_robot.kick(curr_time);

    }
    else
    {
      if (abstract_robot.canKick(curr_time) && (ball.x > posJug.x))
        abstract_robot.kick(curr_time);

    }
  }

  private void playPortero() {
    //abstract_robot.setSteerHeading(curr_time, miPorteria.t);


    double x = miPorteria.x;
    double y = ball.y;

    double yPor = posJug.y;

    if (yPor > 0.26) {
      y = -0.2;
    }
    else if (yPor < -0.26) {
      y = 0.2;
    }

    Vec2 portero = new Vec2(x, y);
    abstract_robot.setSteerHeading(curr_time, portero.t);

    abstract_robot.setSpeed(curr_time, 1.0);

  }

  private void henry() {
    double x=0;
    double y=0;
    x = detrasBalon.x;
    y= detrasBalon.y;

    if (dir==1)
    {
      if (posJug.x < - (abstract_robot.RADIUS))
      {
        x = 0.5;
        if (posJug.y < 0)
        {
          y = 1;
        }
        else
        {
          y = -1;
        }
      }
    }
    else
    {
      if (posJug.x >(abstract_robot.RADIUS))
      {
        x = -0.5;
        if (posJug.y < 0)
        {
          y = 1;
        }
        else
        {
          y = -1;
        }
      }

    }
    Vec2 resultado = new Vec2(x, y);
    abstract_robot.setSteerHeading(curr_time, resultado.t);
    abstract_robot.setSpeed(curr_time, 1.0);

    if ( (Math.abs(suPorteria.t - ball.t) < 1 && suPorteria.r < 1))
    {
      if (abstract_robot.canKick(curr_time))
      {
        abstract_robot.kick(curr_time);
      }
    }
  }

  private void beckenbawer() {
    if (dir==1)
    {
      if (ball.quadrant() == 2 || ball.quadrant() == 3)
      {
        defensaFerrea();
      }
      else
      {
        double x=0;
        double y=0;
        if(ball.r < 0.2)
        {
          x = detrasBalon.x;
          y = detrasBalon.y;

        }
        else
        {
          if (posJug.x > -(abstract_robot.RADIUS))
          {
            x=-0.5;
            if (posJug.y < 0)
            {

              y=1;
            }
            else
            {

              y=-1;
            }
          }
        }
        Vec2 resultado = new Vec2(x, y);
        abstract_robot.setSteerHeading(curr_time, resultado.t);
        abstract_robot.setSpeed(curr_time, 1.0);

        if ( (Math.abs(suPorteria.t - ball.t) < 1 && suPorteria.r < 1))
        {
          if (abstract_robot.canKick(curr_time))
          {
            abstract_robot.kick(curr_time);
          }
        }



      }

    }
    else
    {
      if (ball.quadrant() == 0 || ball.quadrant() == 1)
      {
        defensaFerrea();
      }
      else
      {
        double x=0;
        double y=0;

        if(ball.r < 0.2)
        {
          x = detrasBalon.x;
          y = detrasBalon.y;

        }
        else
        {
          if (posJug.x > (abstract_robot.RADIUS))
          {
            x=-0.5;
            if (posJug.y < 0)
            {

              y=1;
            }
            else
            {

              y=-1;
            }
          }
        }
        Vec2 resultado = new Vec2(x, y);
        abstract_robot.setSteerHeading(curr_time, resultado.t);
        abstract_robot.setSpeed(curr_time, 1.0);

        if ( (Math.abs(suPorteria.t - ball.t) < 1 && suPorteria.r < 1))
        {
          if (abstract_robot.canKick(curr_time))
          {
            abstract_robot.kick(curr_time);
          }
        }
      }
    }
  }

  private void cesc() {

     Vec2 goalie = closest_to(suPorteria, opponents);

    Vec2 direccion = suPorteria;
    direccion.sub(goalie);
    direccion.setr(abstract_robot.KICKER_SPOT_RADIUS);
    direccion.add(goalie);

    abstract_robot.setSteerHeading(curr_time, direccion.t);
    abstract_robot.setSpeed(curr_time, 1.0);
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
  }

} //end class
