import EDU.gatech.cc.is.abstractrobot.ControlSystemSS;
import EDU.gatech.cc.is.util.Vec2;

public class EquipoD extends ControlSystemSS {

    /**
    Inicializaciones.
    */
    public void Configure()
        {
        
        }
        
    
    /**
    Paso
    */
    public int TakeStep()
        {
               //Res.Resultado.Guardar(this,abstract_robot);

        // guarda el resultado
        Vec2    result = new Vec2(0,0);

        // momento actual
        long    curr_time = abstract_robot.getTime();


        /*--- Datos de los sensores ---*/
        // pelota
        Vec2 ball = abstract_robot.getBall(curr_time);

        // porterías
        Vec2 ourgoal = abstract_robot.getOurGoal(curr_time);
        Vec2 theirgoal = abstract_robot.getOpponentsGoal(curr_time);

        // compañeros y oponentes
        Vec2[] teammates = abstract_robot.getTeammates(curr_time);
        Vec2[] opponents = abstract_robot.getOpponents(curr_time);

        
        // compañero más cercano
        Vec2 closestteammate = new Vec2(99999,0);
        for (int i=0; i< teammates.length; i++)
            {
            if (teammates[i].r < closestteammate.r)
                closestteammate = teammates[i];
            }
        
        Vec2 closestopponent = new Vec2(99999,0);
        for (int i=0; i< opponents.length; i++)
            {
            if (opponents[i].r < closestopponent.r)
                closestopponent = opponents[i];
            }
        
        
            /*--- puntos estratégicos ---*/
            
            // entre la pelota y la portería propia
            Vec2 goaliepos = new Vec2(ourgoal.x + ball.x,ourgoal.y + ball.y);
            goaliepos.setr(goaliepos.r*0.5);

            // alejarse del compañero más cercano
            Vec2 awayfromclosest = new Vec2(closestteammate.x,closestteammate.y);
            awayfromclosest.sett(awayfromclosest.t + Math.PI);
            
            //alejarse del oponente más cercano
            Vec2 awayfromclosestO = new Vec2(closestopponent.x,
                    closestopponent.y);
            awayfromclosestO.sett(awayfromclosestO.t - Math.PI-Math.PI/2);
            

            /*--- dependiendo del jugador que sea, se le da un comportamiento u otro ---*/
            int mynum = abstract_robot.getPlayerNumber(curr_time);

            /*--- Portero ---*/
            if (mynum == 0)
                {
    /*ESTRATEGIA DEL PORTERO
     * Si está muy cerca de un compañero (0.1), se aleja, para evitar molestarse
     * Si no:
     *  Si tiene la pelota cerca, se la pasa al compañero.
     *  Si no:
     *      Si la pelota está en los dos octantes que defiende se pone entre la pelota y la portería
     *      Si no:
     *          Si está cerca de su área (a menos de 0.25) se pone entre la pelota y la portería
     *          Si no:
     *              Si no tiene obstáculos (oponentes) en el camino, va hacia su área.
     *              Si no: se aparta del oponente para intentar rodearlo.
     * */
                if(ball.r<=abstract_robot.RADIUS*1.1){
                    if(ball.t==0){
                        Vec2 posPase = new Vec2(ball.x, ball.y);
                        closestteammate.setr(abstract_robot.RADIUS);
                        posPase.sub(closestteammate);
                        posPase.add(ball);
                        result.setr(1.0);
                        result.sett(posPase.t);
                    }
                    else
                    {
                    result.sett(ball.t);
                    result.setr(1.0);
                    }
                }else
                if(closestteammate.r<0.05){
                    //lo principal, es no estorbarse
                    result.t = awayfromclosest.t;
                    result.r = 1.0;
                }
                else{
                    //si la pelota está cerca
                
                    if(ball.r<=abstract_robot.RADIUS*2){
                        //pasa al compañero más cercano
                        
                        Vec2 posPase = new Vec2(ball.x, ball.y);
                        closestteammate.setr(abstract_robot.RADIUS);
                        posPase.sub(closestteammate);
                        posPase.add(ball);
                        result.setr(1.0);
                        result.sett(posPase.t);
                    }
                    else{
                    //está la pelota en mis octantes?
                        int myOctant1 = ourgoal.octant();
                        int myOctant2;
                        if (myOctant1 == 4)myOctant2 = 3;//soy el equipo de la izq
                        else myOctant2 = 0;//soy el equipo de la derecha
                        if(ball.octant()==myOctant1 || ball.octant()==myOctant2){
                            //me pongo entre la pelota y la portería
                                result.sett(goaliepos.t);
                                result.setr(1.0);
                        }
                        else{
                            if(ourgoal.r > 0.1){//si estoy a más 0.1
                                //voy hacia la portería, si no hay obstáculo
                                boolean hayObst = false;
                                double anguloPorteria = (2*Math.PI + ourgoal.t)% (2*Math.PI);
                                double anguloOp = (2*Math.PI + closestopponent.t)% (2*Math.PI);
                                if(closestopponent.r<1.0 && (Math.abs(anguloPorteria-anguloOp))<(Math.PI/2))hayObst = true;
                                
                                if(!hayObst){
                                    result.sett(ourgoal.t);
                                    result.setr(1.0);
                                }
                                else{
                                        //me alejo del oponente más cercano
                                        result.sett(awayfromclosestO.t);
                                        result.setr(1.0);
                                }
                            }
                            else{
                                //me pongo entre la pelota y la portería
                                result.sett(goaliepos.t);
                                result.setr(1.0);
                            }
                        }
                    }
                }
                }
            

            /*--- Defensa ---*/

    else if (mynum == 1)
                {
            /*ESTRATEGIA DEL DEFENSA:
             * Si estorba a un compañero(0.3), se aparta.
             * Si no:
             *      Si la pelota está cerca:
             *          Si el oponente más cercano está cerca: se coloca entre éste y la portería
             *          Si no: lanza la pelota hacia el campo contrario
             *      Si no: se coloca entre el oponente más cercano y la portería
             * */
        if(ball.r<=abstract_robot.RADIUS*1.1){
            if(theirgoal.t==ball.t){
                result.sett(theirgoal.t);
                result.setr(1.0);
            }
            else{
            double anguloPorteria = (2*Math.PI + ourgoal.t)% (2*Math.PI);
            double anguloBola = (2*Math.PI + ball.t)% (2*Math.PI);
            if(Math.abs(anguloPorteria-anguloBola)<Math.PI/4){
                //la pelota está situada hacia mi portería
                //debo rodearla antes de empujarla
                theirgoal.setr(abstract_robot.RADIUS);
                ball.sub(theirgoal);
                result.sett(ball.t);
                result.setr(1.0);
            }
            else{
                //la pelota no está en mi camino
                //me coloco entre la portería y la pelota
                if(goaliepos.r<=abstract_robot.RADIUS*3 && ball.y!=0){
                    result.sety(ball.y);
                    result.setx(0.0);
                    double anguloDestino = (2*Math.PI + result.t)% (2*Math.PI);
                    double anguloOponente =(2*Math.PI + closestopponent.t)% (2*Math.PI);
                    if(Math.abs(anguloDestino-anguloOponente)<(Math.PI/2)){
                        result.setr(1.0);
                        result.sett(awayfromclosestO.t);
                    }
                    }
                else{
                result.sett(goaliepos.t);
                result.setr(1.0);
                }
            }
        }
        }else
        if(closestteammate.r<0.08){
            //lo principal, es no estorbarse
            result.t = awayfromclosest.t;
            result.r = 1.0;
        }
        else{                           
                if(ball.r<abstract_robot.RADIUS*4){
                //si la pelota está cerca de mí
                    
                    if(closestopponent.r < abstract_robot.RADIUS*4){
                        //el oponente está cerca
                        
                        //vector que indica la posición entre
                        //el oponente más cercano y la portería
                        Vec2 porteriaOp = new Vec2(ourgoal.x + closestopponent.x,
                                ourgoal.y + closestopponent.y);
                        porteriaOp.setr(porteriaOp.r*0.5);
                        result.setr(1.0);
                        result.sett(porteriaOp.t);
                        
                        
                    }
                    else{
                        //tirar al campo contrario
                        theirgoal.setr(abstract_robot.RADIUS);
                        Vec2 situacionCentro = new Vec2(ball.x - theirgoal.x, ball.y-theirgoal.y);
                        result.sett(situacionCentro.t);
                        result.setr(1.0);
                    }
                    
                }
                else{
                    //defender al contrario
//                  vector que indica la posición entre
                    //el oponente más cercano y la portería
                    Vec2 porteriaOp = new Vec2(ourgoal.x + closestopponent.x,
                            ourgoal.y + closestopponent.y);
                    porteriaOp.setr(porteriaOp.r*0.5);
                    result.setr(1.0);
                    result.sett(porteriaOp.t);
                }
        }
                }
            /*--- Lateral 1 ---*/
            else if (mynum == 2)
                {
                /*ESTRATEGIA DEL LATERAL:
                 * Si molesta a un compañero, se aleja.
                 * Si no:
                 *  Si la pelota está muy cerca:
                 *      Si la pelota está en posición correcta para tirar a puerta, voy hacia ella y tiro.
                 *      Si no:
                 *          Si la pelota está en los octantes de mi área me pongo entre ella y la portería
                 *          Si no, busco ponerme en la posición de chut a puerta.
                 *  Si no:
                 *      Si está a media distancia, voy hacia la posición de chut
                 *      Si no (distancia grande) voy hacia la pelota.
                 * 
                 * */
                if(ball.r<=abstract_robot.RADIUS*1.1){
                    if(ball.t == theirgoal.t){
                        //estoy en posición de chut
                        result.sett(ball.t);
                        result.setr(1.0);
                    }
                    else{
                        result.sett(ball.t);
                        result.setr(1.0);
                    }
                }else
                if(closestteammate.r<abstract_robot.RADIUS){
                    //lo principal, es no estorbarse
                    double anguloMate = (2*Math.PI + closestteammate.t)% (2*Math.PI);
                    double anguloPelota =  (2*Math.PI + ball.t)% (2*Math.PI);
                    if(Math.abs(anguloMate-anguloPelota)<0.01){
                        result.sett(ball.t);
                        result.setr(1.0);
                    }
                    else{
                    result.t = ourgoal.t;
                    result.r = 1.0;
                    }
                }
                else{
                if(ball.r<abstract_robot.RADIUS*10){
                    if(ball.r<abstract_robot.RADIUS*2){
                        //la pelota está cerca
                        if(ball.t == theirgoal.t){
                            //estoy en posición de chut
                            result.sett(ball.t);
                            result.setr(1.0);
                        }
                        else{
                            int myOctant1 = ourgoal.octant();
                            int myOctant2;
                            if (myOctant1 == 4)myOctant2 = 3;//soy el equipo de la izq
                            else myOctant2 = 0;//soy el equipo de la derecha
                            if(ball.octant()==myOctant1 || ball.octant()==myOctant2){
                                //está en nuestros octantes
                                result.sett(goaliepos.t);
                                result.setr(1.0);                           
                            }
                            else{
                                //no está en nuestros octantes, busco situarme para chutar a gol
                                Vec2 posChut = new Vec2(ball);
                                theirgoal.setr(abstract_robot.RADIUS);
                                posChut.sub(theirgoal);
                                result.sett(posChut.t);
                                result.setr(1.0);
                            }
                        }
                    }
                    else{
                    //si la pelota está a media distancia, ir hacia la posición
                    //de chut
                        Vec2 posChut = new Vec2(ball);
                        theirgoal.setr(abstract_robot.RADIUS);
                        posChut.sub(theirgoal);
                        result.sett(posChut.t);
                        result.setr(1.0);
                    }
                }
                else{
                    result.sett(ball.t);
                    result.setr(1.0);
                    
                }
                }
                }

            /*--- Delantero ---*/           
            else if (mynum == 3)
            {
                if(ball.r<=abstract_robot.RADIUS*1.2){
                    if(ball.t==0){
                        result.sett(theirgoal.t);
                        result.setr(1.0);
                    }
                    else{
                    result.sett(ball.t);
                    result.setr(1.0);
                    }
                }else
                if(closestteammate.r<abstract_robot.RADIUS *2){
                    //lo principal, es no estorbarse
                    result.t = awayfromclosest.t;
                    result.r = 1.0;
                }
                else{
                if(ball.r<abstract_robot.RADIUS*2){
                    if(ball.t==theirgoal.t){
                        result.sett(theirgoal.t);
                        result.setr(1.0);
                    }
                    else{
                        Vec2 posChut = new Vec2(ball);
                        theirgoal.setr(abstract_robot.RADIUS);
                        posChut.sub(theirgoal);
                        result.sett(posChut.t);
                        result.setr(1.0);
                    }
                }
                else{
                    result.sett(ball.t);
                    result.setr(1.0);
                }   
                }
            }
            /*--- Lateral 2 ---*/   
            
            else if (mynum == 4)
            {
                /*ESTRATEGIA DEL LATERAL:
                 * Si molesta a un compañero, se aleja.
                 * Si no:
                 *  Si la pelota está muy cerca:
                 *      Si la pelota está en posición correcta para tirar a puerta, voy hacia ella y tiro.
                 *      Si no:
                 *          Si la pelota está en los octantes de mi área me pongo entre ella y la portería
                 *          Si no, busco ponerme en la posición de chut a puerta.
                 *  Si no:
                 *      Si está a media distancia, voy hacia la posición de chut
                 *      Si no (distancia grande) voy hacia la pelota.
                 * 
                 * */
                if(ball.r<=abstract_robot.RADIUS*1.1){
                    if(ball.t == theirgoal.t){
                        //estoy en posición de chut
                        result.sett(ball.t);
                        result.setr(1.0);
                    }
                    else{
                        result.sett(ball.t);
                        result.setr(1.0);
                    }
                }else
                if(closestteammate.r<abstract_robot.RADIUS){
                    //lo principal, es no estorbarse
                    double anguloMate = (2*Math.PI + closestteammate.t)% (2*Math.PI);
                    double anguloPelota =  (2*Math.PI + ball.t)% (2*Math.PI);
                    if(Math.abs(anguloMate-anguloPelota)<0.01){
                        result.sett(ball.t);
                        result.setr(1.0);
                    }
                    else{
                    result.sett( ourgoal.t);
                    result.setr(1.0);
                    }
                }
                else{
                if(ball.r<abstract_robot.RADIUS*10){
                    if(ball.r<abstract_robot.RADIUS*2){
                        //la pelota está cerca
                        if(ball.t == theirgoal.t){
                            //estoy en posición de chut
                            result.sett(ball.t);
                            result.setr(1.0);
                        }
                        else{
                            int myOctant1 = ourgoal.octant();
                            int myOctant2;
                            if (myOctant1 == 4)myOctant2 = 3;//soy el equipo de la izq
                            else myOctant2 = 0;//soy el equipo de la derecha
                            if(ball.octant()==myOctant1 || ball.octant()==myOctant2){
                                //está en nuestros octantes
                                result.sett(goaliepos.t);
                                result.setr(1.0);                           
                            }
                            else{
                                //no está en nuestros octantes, busco situarme para chutar a gol
                                Vec2 posChut = new Vec2(ball);
                                theirgoal.setr(abstract_robot.RADIUS);
                                posChut.sub(theirgoal);
                                result.sett(posChut.t);
                                result.setr(1.0);
                            }
                        }
                    }
                    else{
                    //si la pelota está a media distancia, ir hacia la posición
                    //de chut
                        Vec2 posChut = new Vec2(ball);
                        theirgoal.setr(abstract_robot.RADIUS);
                        posChut.sub(theirgoal);
                        result.sett(posChut.t);
                        result.setr(1.0);
                    }
                }
                else{
                    result.sett(ball.t);
                    result.setr(1.0);
                    
                }
                }
            }

            /*--- Send commands to actuators ---*/
            // set the heading
            abstract_robot.setSteerHeading(curr_time, result.t);

            // set speed at maximum
            abstract_robot.setSpeed(curr_time, result.r);

            // kick it if we can
            if (abstract_robot.canKick(curr_time))
                abstract_robot.kick(curr_time);

            // tell the parent we're OK
            return(CSSTAT_OK);
            }

}
