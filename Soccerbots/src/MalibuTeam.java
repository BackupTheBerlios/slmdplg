/*
 * PonchingTeam.java
 */

import	EDU.gatech.cc.is.util.Vec2;
import	EDU.gatech.cc.is.abstractrobot.*;
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


public class MalibuTeam extends ControlSystemSS {


  long curr_time;
  Vec2 ball,theirgoal,mygoal;
  int mynum;

  /*PORTEROS*/
  Vec2 vEsqSupDer = new Vec2(1.35,0.2);
  Vec2 vEsqInfDer = new Vec2(1.35,-0.2);

  Vec2 vEsqSupIzq = new Vec2(-1.35,0.2);
  Vec2 vEsqInfIzq = new Vec2(-1.35,-0.2);

  /*DEFENSAS*/
  Vec2 vAtrasSupDer = new Vec2(0.6,0.5);
  Vec2 vAtrasInfDer = new Vec2(0.6,-0.5);

  Vec2 vAtrasSupIzq = new Vec2(-0.6,0.5);
  Vec2 vAtrasInfIzq = new Vec2(-0.6,-0.5);

  /*CENTROS*/
  Vec2 vCentroSupIzq = new Vec2(-0.15,0.5);
  Vec2 vCentroInfIzq = new Vec2(-0.15,-0.5);

  Vec2 vCentroSupDer = new Vec2(0.15,0.5);
  Vec2 vCentroInfDer = new Vec2(0.15,-0.5);

  /*DELANTEROS ABAJO*/
  Vec2 vDelante1SupDer = new Vec2(0.35,-0.2);
  Vec2 vDelante1InfDer = new Vec2(0.35,-0.6);

  Vec2 vDelante1SupIzq = new Vec2(-0.35,-0.2);
  Vec2 vDelante1InfIzq = new Vec2(-0.35,-0.6);

  /*DELANTEROS ARRIBA*/
  Vec2 vDelante2SupDer = new Vec2(0.35,0.6);
  Vec2 vDelante2InfDer = new Vec2(0.35,0.2);

  Vec2 vDelante2SupIzq = new Vec2(-0.35,0.6);
  Vec2 vDelante2InfIzq = new Vec2(-0.35,0.2);

        /**
        Configure the Avoid control system.  This method is
        called once at initialization time.  You can use it
        to do whatever you like.
        */
        public void Configure(){

        }


        /**
        Called every timestep to allow the control system to
        run.
        */
        public int TakeStep(){

          this.actualizar_datos();

          if (mynum==0){
            this.comp_portero();
          }
          else if (mynum==1){
            this.comp_defensa();
          }
          else if (mynum==3){
            this.comp_centro();
          }
          else if (mynum==2){
            this.comp_delantero1();
          }
          else{
            this.comp_delantero2();
          }

          return(CSSTAT_OK);
        }

        private void actualizar_datos(){

          curr_time=abstract_robot.getTime();

          ball = abstract_robot.getBall(curr_time);
          theirgoal = abstract_robot.getOpponentsGoal(curr_time);
          mygoal = abstract_robot.getOurGoal(curr_time);

          mynum=abstract_robot.getPlayerNumber(curr_time);
        }

        public void comp_portero(){

          int campo=this.getCampo();

          Vec2 posNueva=abstract_robot.getPosition(curr_time);

          /*CAMPO DERECHO*/
          if (campo == 1){
            if (ball.y < 0.0){

              vEsqInfDer.setx(1.35);
              vEsqInfDer.sety(-0.2);
              vEsqInfDer.sub(posNueva);

              abstract_robot.setSteerHeading(curr_time,vEsqInfDer.t);
              abstract_robot.setSpeed(curr_time,1.0);

              if (abstract_robot.canKick(curr_time)) abstract_robot.kick(curr_time);

            } else if (ball.y > 0.0){

              vEsqSupDer.setx(1.35);
              vEsqSupDer.sety(0.2);
              vEsqSupDer.sub(posNueva);

              abstract_robot.setSteerHeading(curr_time,vEsqSupDer.t);
              abstract_robot.setSpeed(curr_time,1.0);

              if (abstract_robot.canKick(curr_time)) abstract_robot.kick(curr_time);
            }
          }

          /*CAMPO IZQUIERDO*/
          if (campo == -1){
            if (ball.y < 0.0){

              vEsqInfIzq.setx(-1.35);
              vEsqInfIzq.sety(-0.2);
              vEsqInfIzq.sub(posNueva);

              abstract_robot.setSteerHeading(curr_time,vEsqInfIzq.t);
              abstract_robot.setSpeed(curr_time,1.0);

              if (abstract_robot.canKick(curr_time)) abstract_robot.kick(curr_time);

            } else if (ball.y > 0.0){

              vEsqSupIzq.setx(-1.35);
              vEsqSupIzq.sety(0.2);
              vEsqSupIzq.sub(posNueva);

              abstract_robot.setSteerHeading(curr_time,vEsqSupIzq.t);
              abstract_robot.setSpeed(curr_time,1.0);

              if (abstract_robot.canKick(curr_time)) abstract_robot.kick(curr_time);

            }
          }


        }

        public void comp_defensa(){

          int campo=this.getCampo();

          boolean ori=false;

          Vec2 posNueva=abstract_robot.getPosition(curr_time);

          /*si se aleja demasiado vuelve a su porteria*/
          if(mygoal.r>1.0){
            abstract_robot.setSteerHeading(curr_time,mygoal.t);
            abstract_robot.setSpeed(curr_time,1.0);
          }
          else{

            /*CAMPO DERECHO*/
            if (campo == 1){
              if (ball.y < 0.0){

                vAtrasInfDer.setx(0.6);
                vAtrasInfDer.sety(-0.5);
                vAtrasInfDer.sub(posNueva);

                abstract_robot.setSteerHeading(curr_time,vAtrasInfDer.t);
                abstract_robot.setSpeed(curr_time,1.0);

                ori=this.bienOrientado();

                if (abstract_robot.canKick(curr_time) && !ori)
                  abstract_robot.kick(curr_time);

              } else if (ball.y > 0.0){

                vAtrasSupDer.setx(0.6);
                vAtrasSupDer.sety(0.5);
                vAtrasSupDer.sub(posNueva);

                abstract_robot.setSteerHeading(curr_time,vAtrasSupDer.t);
                abstract_robot.setSpeed(curr_time,1.0);

                ori=this.bienOrientado();

                if (abstract_robot.canKick(curr_time) && !ori)
                  abstract_robot.kick(curr_time);
              }
            }

            /*CAMPO IZQUIERDO*/
            if (campo == -1){
              if (ball.y < 0.0){

                vAtrasInfIzq.setx(-0.6);
                vAtrasInfIzq.sety(-0.5);
                vAtrasInfIzq.sub(posNueva);

                abstract_robot.setSteerHeading(curr_time,vAtrasInfIzq.t);
                abstract_robot.setSpeed(curr_time,1.0);

                ori=this.bienOrientado();

                if (abstract_robot.canKick(curr_time) && ori)
                  abstract_robot.kick(curr_time);

              } else if (ball.y > 0.0){

                vAtrasSupIzq.setx(-0.6);
                vAtrasSupIzq.sety(0.5);
                vAtrasSupIzq.sub(posNueva);

                abstract_robot.setSteerHeading(curr_time,vAtrasSupIzq.t);
                abstract_robot.setSpeed(curr_time,1.0);

                ori=this.bienOrientado();

                if (abstract_robot.canKick(curr_time) && ori)
                  abstract_robot.kick(curr_time);

              }
            }

          }
        }

        public void comp_centro(){

          int campo=this.getCampo();

          boolean ori=false;

          Vec2 posNueva=abstract_robot.getPosition(curr_time);

          if(mygoal.r>1.4){
            abstract_robot.setSteerHeading(curr_time,mygoal.t);
            abstract_robot.setSpeed(curr_time,1.0);
          }
          else{
            /*CAMPO IZQUIERDO*/
            if (campo == -1){
              if (ball.y < 0.0){

                vCentroInfIzq.setx(-0.15);
                vCentroInfIzq.sety(-0.5);
                vCentroInfIzq.sub(posNueva);

                abstract_robot.setSteerHeading(curr_time,vCentroInfIzq.t);
                abstract_robot.setSpeed(curr_time,1.0);

                ori=this.bienOrientado();

                if (abstract_robot.canKick(curr_time) && ori)
                  abstract_robot.kick(curr_time);

              } else if (ball.y > 0.0){

                vCentroSupIzq.setx(-0.15);
                vCentroSupIzq.sety(0.5);
                vCentroSupIzq.sub(posNueva);

                abstract_robot.setSteerHeading(curr_time,vCentroSupIzq.t);
                abstract_robot.setSpeed(curr_time,1.0);

                ori=this.bienOrientado();

                if (abstract_robot.canKick(curr_time) && ori)
                  abstract_robot.kick(curr_time);
              }
            }

            /*CAMPO DERECHO*/
            if (campo == 1){
              if (ball.y < 0.0){

                vCentroInfDer.setx(0.15);
                vCentroInfDer.sety(-0.5);
                vCentroInfDer.sub(posNueva);

                abstract_robot.setSteerHeading(curr_time,vCentroInfDer.t);
                abstract_robot.setSpeed(curr_time,1.0);

                ori=this.bienOrientado();

                if (abstract_robot.canKick(curr_time) && !ori)
                  abstract_robot.kick(curr_time);

              } else if (ball.y > 0.0){

                vCentroSupDer.setx(0.15);
                vCentroSupDer.sety(0.5);
                vCentroSupDer.sub(posNueva);

                abstract_robot.setSteerHeading(curr_time,vCentroSupDer.t);
                abstract_robot.setSpeed(curr_time,1.0);

                ori=this.bienOrientado();

                if (abstract_robot.canKick(curr_time) && !ori)
                  abstract_robot.kick(curr_time);
              }
            }
          }

        }

        public void comp_delantero1(){

          int campo=this.getCampo();

          boolean ori=false;

          Vec2 posNueva=abstract_robot.getPosition(curr_time);

          if(mygoal.r<1.4){
            abstract_robot.setSteerHeading(curr_time,theirgoal.t);
            abstract_robot.setSpeed(curr_time,1.0);
          }
          else{
            /*CAMPO DERECHO*/
            /*LOS DELANTEROS SON DEL CAMPO IZQ Y ESTAN EN EL CAMPO DCHO*/
            if (campo == 1){
              if(ball.r<0.4){
                 this.bordearPelotaIzq();

                 abstract_robot.setSpeed(curr_time,1.0);

                 ori=this.bienOrientado();

                 if (abstract_robot.canKick(curr_time) && ori)
                   abstract_robot.kick(curr_time);

               }else{
                 if (ball.y < 0.0){

                   vDelante1InfDer.setx(0.35);
                   vDelante1InfDer.sety(-0.6);
                   vDelante1InfDer.sub(posNueva);

                   // para que el delantero se vuelva defensa
                   //vDelante1InfDer.setx(vDelante1InfDer.x*(-1));
                   //vDelante1InfDer.sety(vDelante1InfDer.y*(-1));

                   abstract_robot.setSteerHeading(curr_time,vDelante1InfDer.t);
                   abstract_robot.setSpeed(curr_time,1.0);

                   ori=this.bienOrientado();

                   if (abstract_robot.canKick(curr_time) && ori)
                     abstract_robot.kick(curr_time);

                 } else if (ball.y > 0.0){

                   vDelante1SupDer.setx(0.35);
                   vDelante1SupDer.sety(-0.2);
                   vDelante1SupDer.sub(posNueva);

                   abstract_robot.setSteerHeading(curr_time,vDelante1SupDer.t);
                   abstract_robot.setSpeed(curr_time,1.0);

                   ori=this.bienOrientado();

                   if (abstract_robot.canKick(curr_time) && ori)
                     abstract_robot.kick(curr_time);
                 }
               }
            }

            /*CAMPO IZQUIERDO*/
            /*LOS DELANTEROS SON DEL CAMPO DCHO Y ESTAN EN EL CAMPO IZQ*/
            if (campo == -1){
              if(ball.r<0.4){
                 this.bordearPelotaDer();

                 abstract_robot.setSpeed(curr_time,1.0);

                 ori=this.bienOrientado();

                 if (abstract_robot.canKick(curr_time) && !ori)
                   abstract_robot.kick(curr_time);

               }else{
                 if (ball.y < 0.0){

                   vDelante1InfIzq.setx(-0.35);
                   vDelante1InfIzq.sety(-0.6);
                   vDelante1InfIzq.sub(posNueva);

                   // para que el delantero se vuelva defensa
                   //vDelante1InfDer.setx(vDelante1InfDer.x*(-1));
                   //vDelante1InfDer.sety(vDelante1InfDer.y*(-1));

                   abstract_robot.setSteerHeading(curr_time,vDelante1InfIzq.t);
                   abstract_robot.setSpeed(curr_time,1.0);

                   ori=this.bienOrientado();

                   if (abstract_robot.canKick(curr_time) && !ori)
                     abstract_robot.kick(curr_time);

                 } else if (ball.y > 0.0){

                   vDelante1SupIzq.setx(-0.35);
                   vDelante1SupIzq.sety(-0.2);
                   vDelante1SupIzq.sub(posNueva);

                   abstract_robot.setSteerHeading(curr_time,vDelante1SupIzq.t);
                   abstract_robot.setSpeed(curr_time,1.0);

                   ori=this.bienOrientado();

                   if (abstract_robot.canKick(curr_time) && !ori)
                     abstract_robot.kick(curr_time);
                 }
               }
            }

          }

        }

        public void comp_delantero2(){

          int campo=this.getCampo();

          boolean ori=false;

           Vec2 posNueva=abstract_robot.getPosition(curr_time);

           if(mygoal.r<1.4){
             abstract_robot.setSteerHeading(curr_time,theirgoal.t);
             abstract_robot.setSpeed(curr_time,1.0);
           }
           else{
             if (campo == 1){
               if(ball.r<0.4){
                 this.bordearPelotaIzq();
                 ball=abstract_robot.getBall(curr_time);

                 abstract_robot.setSteerHeading(curr_time,ball.t);
                 abstract_robot.setSpeed(curr_time,1.0);

                 ori=this.bienOrientado();

                 if (abstract_robot.canKick(curr_time) && ori)
                     abstract_robot.kick(curr_time);
               }else{
                 if (ball.y < 0.0){

                   vDelante2InfDer.setx(0.35);
                   vDelante2InfDer.sety(0.2);
                   vDelante2InfDer.sub(posNueva);

                   // para que el delantero se vuelva defensa
                   //vDelante1InfDer.setx(vDelante1InfDer.x*(-1));
                   //vDelante1InfDer.sety(vDelante1InfDer.y*(-1));

                   abstract_robot.setSteerHeading(curr_time,vDelante2InfDer.t);
                   abstract_robot.setSpeed(curr_time,1.0);

                   ori=this.bienOrientado();

                   if (abstract_robot.canKick(curr_time) && ori)
                     abstract_robot.kick(curr_time);

                 } else if (ball.y > 0.0){

                   vDelante2SupDer.setx(0.35);
                   vDelante2SupDer.sety(0.6);
                   vDelante2SupDer.sub(posNueva);

                   abstract_robot.setSteerHeading(curr_time,vDelante2SupDer.t);
                   abstract_robot.setSpeed(curr_time,1.0);

                   ori=this.bienOrientado();

                   if (abstract_robot.canKick(curr_time) && ori)
                     abstract_robot.kick(curr_time);
                 }
               }
             }

             if (campo == -1){
               if(ball.r<0.4){
                 this.bordearPelotaDer();
                 ball=abstract_robot.getBall(curr_time);

                 abstract_robot.setSteerHeading(curr_time,ball.t);
                 abstract_robot.setSpeed(curr_time,1.0);

                 ori=this.bienOrientado();

                 if (abstract_robot.canKick(curr_time) && !ori)
                     abstract_robot.kick(curr_time);
               }else{
                 if (ball.y < 0.0){

                   vDelante2InfIzq.setx(-0.35);
                   vDelante2InfIzq.sety(0.2);
                   vDelante2InfIzq.sub(posNueva);

                   // para que el delantero se vuelva defensa
                   //vDelante1InfDer.setx(vDelante1InfDer.x*(-1));
                   //vDelante1InfDer.sety(vDelante1InfDer.y*(-1));

                   abstract_robot.setSteerHeading(curr_time,vDelante2InfIzq.t);
                   abstract_robot.setSpeed(curr_time,1.0);

                   ori=this.bienOrientado();

                   if (abstract_robot.canKick(curr_time) && !ori)
                     abstract_robot.kick(curr_time);

                 } else if (ball.y > 0.0){

                   vDelante2SupIzq.setx(-0.35);
                   vDelante2SupIzq.sety(0.6);
                   vDelante2SupIzq.sub(posNueva);

                   abstract_robot.setSteerHeading(curr_time,vDelante2SupIzq.t);
                   abstract_robot.setSpeed(curr_time,1.0);

                   ori=this.bienOrientado();

                   if (abstract_robot.canKick(curr_time) && !ori)
                     abstract_robot.kick(curr_time);
                 }
               }
             }

           }

        }

        public void bordearPelotaIzq(){

          double xp,yp,xr,yr,xj,yj;

          xj=abstract_robot.getPosition(curr_time).x;
          yj=abstract_robot.getPosition(curr_time).y;

          ball=abstract_robot.getBall(curr_time);

          xp=ball.x;
          yp=ball.y;

          if ((xp-0.2)<xj){
            xr=xp-0.2;
            ball.setx(xr);
            ball.sety(yj);
            abstract_robot.setSteerHeading(curr_time,ball.t);
            abstract_robot.setSpeed(curr_time,1.0);
          }

          if((xp-0.2)>=xj && yj!=yp){
            xr=xp-0.3;
            ball.setx(xr);
            abstract_robot.setSteerHeading(curr_time,ball.t);
            abstract_robot.setSpeed(curr_time,1.0);
          }

          if(xp>=xj && yj==yp){
            abstract_robot.setSteerHeading(curr_time,theirgoal.t);
            abstract_robot.setSpeed(curr_time,1.0);
          }
        }

        public void bordearPelotaDer(){

          double xp,yp,xr,yr,xj,yj;

          xj=abstract_robot.getPosition(curr_time).x;
          yj=abstract_robot.getPosition(curr_time).y;

          ball=abstract_robot.getBall(curr_time);

          xp=ball.x;
          yp=ball.y;

          if ((xp+0.2)>xj){
            xr=xp+0.2;
            ball.setx(xr);
            ball.sety(yj);
            abstract_robot.setSteerHeading(curr_time,ball.t);
            abstract_robot.setSpeed(curr_time,1.0);
          }

          if((xp+0.2)<=xj && yj!=yp){
            xr=xp+0.3;
            ball.setx(xr);
            abstract_robot.setSteerHeading(curr_time,ball.t);
            abstract_robot.setSpeed(curr_time,1.0);
          }

          if(xp>=xj && yj==yp){
            abstract_robot.setSteerHeading(curr_time,theirgoal.t);
            abstract_robot.setSpeed(curr_time,1.0);
          }
        }

        public boolean bienOrientado(){
          boolean bien=false;

          Vec2 pos=abstract_robot.getPosition(curr_time);

          double a,b,c,r;

          a=pos.x;
          b=pos.y;
          c=a/b;

          r=Math.atan(c);

          // orientado hacia la derecha
          if((0<=r && r<=90) || (270<=r && r<=360))
            bien=true;
          else
            bien=false;

          return bien;
        }

        public int getCampo(){

          int campo;
          Vec2 v=abstract_robot.getPosition(curr_time);
          if (v.x > 0){ //estamos a la derecha (EAST)
            campo=1;
          }
          else{ //estamos a la izquierda (WEST)
            campo=-1;
          }
          return campo;
        }
}