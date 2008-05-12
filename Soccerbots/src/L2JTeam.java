
import  EDU.gatech.cc.is.util.Vec2;
import  EDU.gatech.cc.is.abstractrobot.*;
import EDU.gatech.cc.is.communication.Message;
import jess.*;
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


public class L2JTeam extends ControlSystemSS
{
    Rete jess_engine;
    int i;
    String cadena;
    
    
    private long    tiempo;     //What time is it?
    private long    mynum;          //Who am I?
    private double  rotacion;       //What direction am I pointing?
    private Vec2    posJugador;
    
    private Vec2    posPelota;          //Where is the ball?
    private Vec2    posPorteriaMia;     //Where is our goal?
    private Vec2    posPorteriaSuya;        //Where is their goal?
    private Vec2[]  posEquipo;      //Where are my teammates?
    private Vec2[]  posEquipoContrario;     //Where are my opponents?

                        //Where is the closest...
    private Vec2    amigoCercano;       //Teammate?
    private Vec2    enemigoCercano;     //Opponent?
    private Vec2    amigoCercanoAPelota;    //Teammate to the Ball?


    private Vec2    movimiento;         //Move in move.t direction
                                        //  with speed move.r
    private boolean kickit;             //Try to kick it
    private boolean conPelota;

    // what side of the field are we on? -1 for west +1 for east
    private  int SIDE;

    // a vector pointing to me.
    private static final Vec2 YO = new Vec2(0,0);

    // restated here for convenience
    private final double ROBOT_RADIUS = abstract_robot.RADIUS;

    private static final boolean DEBUG = false;
    
    private int marcador;   // 0-> empate, >0 -> ganando, <0 ->perdiendo.

    
    private static boolean portero_bloqueado = false;
    
    /**
    Configure the Avoid control system.  This method is
    called once at initialization time.  You can use it
    to do whatever you like.
    */
    public void Configure()
    {
            try{
                // Se inicializa el motor de razonamiento de jess
                jess_engine = new Rete();
                jess_engine.executeCommand("(batch jugadores1.clp)");
                jess_engine.executeCommand("(reset)");
            }
            catch(JessException e)
            {
                System.out.println(e);
                System.out.println("Error al inicializar Clips");
            }
            
            tiempo = abstract_robot.getTime();
            if( abstract_robot.getOurGoal(tiempo).x < 0)
                SIDE = -1;
            else
                SIDE = 1;
                        
            marcador = 0;


            
    }
        
    
    /**
    Called every timestep to allow the control system to
    run.
    */
    public int TakeStep()
    {
        //Res.Resultado.Guardar(this,abstract_robot);
        try
        {
            //------------------ PERCEPCION -------------------------//
            Percepcion();
            //----------------- ANALISIS/DECISION ---------------------//
            Decision();     
            //-------------------------- ACCION --------------------
            Accion();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        // tell the parent we're OK
        return(CSSTAT_OK);
    }
    
    
    public static void main(String[] args){
        System.out.println("Static compilado");
        
    }
    
    private void Percepcion()
    {

        // Se obtienen los datos de la simulacion en el estado actual.
        tiempo = abstract_robot.getTime();
        posJugador = abstract_robot.getPosition(tiempo);
        posPelota = abstract_robot.getBall(tiempo);
        posPorteriaMia = abstract_robot.getOurGoal(tiempo);
        posPorteriaSuya = abstract_robot.getOpponentsGoal(tiempo);
        posEquipo = abstract_robot.getTeammates(tiempo);
        posEquipoContrario = abstract_robot.getOpponents(tiempo);
        rotacion = abstract_robot.getSteerHeading(tiempo);
        posPelota = abstract_robot.getBall(tiempo);
        conPelota = (posPelota.r < ROBOT_RADIUS * 4);
        mynum = abstract_robot.getPlayerNumber(tiempo);
        amigoCercano = closest_to(YO, posEquipo);
        enemigoCercano = closest_to(YO, posEquipoContrario);
        marcador += abstract_robot.getJustScored(tiempo);
        
        // Se pasan los datos relevantes a clips
        try
        {
            jess_engine.store("num_jugador", new Value(mynum,RU.INTEGER));
            jess_engine.store("marcador", new Value(marcador,RU.INTEGER));
            if(portero_bloqueado)
            {
                jess_engine.store("portero_bloqueado", new Value(1,RU.INTEGER));
                
            }
            else
            {
                jess_engine.store("portero_bloqueado", new Value(0,RU.INTEGER));
            }
            
            jess_engine.executeCommand("(reset)");
            jess_engine.executeCommand("(assert(inicio))");
            jess_engine.executeCommand("(run)");
        }
        catch(Exception e)
        {
            System.out.println("Error al pasar los datos a Jess");
            e.printStackTrace();
        }
        
        
    }

    private void Decision() throws JessException
    {
    
        movimiento = new Vec2();
        kickit = false;
        
        
        Value x = jess_engine.fetch("jugada");
        int jugada = x.intValue(null);
            
        switch(jugada)
        {
        case 0: Portero();
                break;
        case 1: Atacar();
                break;
        case 2: Ganando(); 
                break;
        case 3: PorteroBloqueado();
                break;
        case 4: Salinas(25);
        default:
                break;
        }
        
    
        
    }
    
    private void Accion()
    {
        //       set heading towards it
        abstract_robot.setSteerHeading(tiempo, movimiento.t);
        // set speed at zero
        abstract_robot.setSpeed(tiempo, movimiento.r);      
        // kick it if we can
        if (abstract_robot.canKick(tiempo) && this.kickit)
        {
            
                    abstract_robot.kick(tiempo);
                    System.out.println("Disparo");
                    
        }
        
    }
    
    //------------- METODOS AUXILIARES -------------------------//
    

    private void Ganando(){
        if(mynum == 1) Defensa_Bloqueo(8,20);
        else if(mynum == 2) Defensa_Bloqueo(15,27);
        else if(mynum == 3) Defensa_Bloqueo(22,34);
    }
    
    private void PorteroBloqueado()
    {
        if(mynum == 1)
        {   
            Defensa_Bloqueo(6,12);
        }       
        
    }
    
    private void Catenaccio()
    {
        double dist = 1.1;
        double zonaReunion = 1.2;
        movimiento.setr(0);
        
        // Si no hemos alcanzado la zona de reunion (cerca de nuestra porteria),
        // nos movemos hacia ella pero manteniendo una amplia distancia respecto
        // a nuestros compañeros.
        if((zonaReunion - Math.abs(posJugador.x))> 0)
        {
            if(mynum == 0)
            {
                
                if(posPorteriaMia.r > ROBOT_RADIUS *dist)
                {
                    movimiento.sett(posPorteriaMia.t);
                    movimiento.setr(1.0);
                    avoidcollision();
                }
                else{
                    movimiento.sett(posPorteriaSuya.t);
                    kickit = true;
                }
            }
            
            else if(mynum == 1)
            {
                Vec2 pos = posPorteriaMia;
                pos.add(new Vec2(0, ROBOT_RADIUS * dist * 4));
                
               movimiento.setr(0);
               if(pos.r > ROBOT_RADIUS*  dist)
                {
                    movimiento.sett(pos.t);
                    movimiento.setr(1.0);
                    avoidcollision();
                }
              else{
                    movimiento.sett(posPorteriaSuya.t);
                    kickit = true;
                }
            }
            else if(mynum == 2)
            {
                Vec2 pos = posPorteriaMia;
                pos.add(new Vec2(0 , ROBOT_RADIUS * 6 * dist));
                
              movimiento.setr(0);
              if(pos.r > ROBOT_RADIUS*  dist)
                {
                    movimiento.sett(pos.t);
                    movimiento.setr(1.0);
                    avoidcollision();
                }
              else{
                    movimiento.sett(posPorteriaSuya.t);
                    kickit = true;
                }
            }
            else if(mynum == 3)
            {
                Vec2 pos = posPorteriaMia;
                pos.add(new Vec2(0 , -ROBOT_RADIUS * 4 *dist));
                
              movimiento.setr(0);
              if(pos.r > ROBOT_RADIUS * dist)
                {
                    movimiento.sett(pos.t);
                    movimiento.setr(1.0);
                    avoidcollision();
                }
              else{
                    movimiento.sett(posPorteriaSuya.t);
                    kickit = true;
                }
            }
            else if(mynum == 4)
            {
                Vec2 pos = posPorteriaMia;
                pos.add(new Vec2(0, -ROBOT_RADIUS * 6*dist ));
                
              movimiento.setr(0);
              if(pos.r > ROBOT_RADIUS *dist)
                {
                    movimiento.sett(pos.t);
                    movimiento.setr(1.0);
                    avoidcollision();
                }
              else{
                    movimiento.sett(posPorteriaSuya.t);
                    kickit = true;
                }
            }
            
        
        
        }
        
        // Cuando ya hemos alcanzado la zona de reunion, el jugador se dirige
        // hacia el centro de la porteria sin evitar colisionar para crear
        // un muro lo más compacto posible.
        else
        {
            if(mynum == 0)
            {
                if(posPorteriaMia.r > ROBOT_RADIUS *dist)
                {
                    movimiento.sett(posPorteriaMia.t);
                    movimiento.setr(1.0);
                }
                
            }
            
            else if(mynum == 1)
            {
                Vec2 pos = posPorteriaMia;
            
                                                
               movimiento.setr(0);
               if(pos.r > ROBOT_RADIUS )
                {
                    movimiento.sett(pos.t);
                    movimiento.setr(1.0);
                }
              
            }
            else if(mynum == 2)
            {
                Vec2 pos = posPorteriaMia;
                pos.add(new Vec2(0, ROBOT_RADIUS * 2*dist ));
                                
              movimiento.setr(0);
              if(pos.r > ROBOT_RADIUS)
                {
                    movimiento.sett(pos.t);
                    movimiento.setr(1.0);
                
                }
              
            }
            else if(mynum == 3)
            {
                Vec2 pos = posPorteriaMia;
                
                
              movimiento.setr(0);
              if(pos.r > ROBOT_RADIUS )
                {
                    movimiento.sett(pos.t);
                    movimiento.setr(1.0);
                }

            }
            else if(mynum == 4)
            {
                
            Vec2 pos = posPorteriaMia;
            pos.add(new Vec2(0, -ROBOT_RADIUS * 2*dist ));
                            
              movimiento.setr(0);
              if(pos.r > ROBOT_RADIUS )
                {
                    movimiento.sett(pos.t);
                    movimiento.setr(1.0);
            
                }

            }
        }
    }
    
    private void Atacar()
    {
        movimiento =  new Vec2(posPelota);
        movimiento.setr(1);
        
        if(conPelota && behind_point(posPelota, posPorteriaSuya)) 
        {
            movimiento.sett(posPorteriaSuya.t);
            
            if( (Math.abs( rotacion  - posPorteriaSuya.t) < Math.PI/8) &&
                    (posPorteriaSuya.r < ROBOT_RADIUS * 25))
            {
                    abstract_robot.setDisplayString("Disparar");
                    kickit = true;
            }

        }
        else
        {
            get_behind(posPelota, posPorteriaSuya);
        }
        
        avoidcollision();
        
    }
    
    /**
     * 
     *
     */
    private void Portero()
    {
        // Cuando la pelota ha sobrepasado al portero intenta despejarla
        if( posPelota.x * SIDE > 0)
        {
            movimiento.sett( posPelota.t);
            movimiento.setr( 1.0);
            kickit = true;
        }

        // Se mantiene al portero en las cercanias de la porteria
        else if( (Math.abs(posPorteriaMia.x) > ROBOT_RADIUS * 1.4) ||
             (Math.abs(posPorteriaMia.y) > ROBOT_RADIUS * 4.25) )

        {
            movimiento.sett( posPorteriaMia.t);
            movimiento.setr( 1.0);
        }

        // Mantiene la posicion entre la porteria y la pelota
        else
        {
            if( posPelota.y > 0)
                movimiento.sety( 7);
            else
                movimiento.sety( -7);

            movimiento.setx( (double)SIDE);

            if( Math.abs( posPelota.y) < ROBOT_RADIUS * 0.15)
                movimiento.setr( 0.0);
            else
                movimiento.setr( 1.0);
        }
        
        if( enemigoCercano.r < ROBOT_RADIUS*1.4)
        {
                        
            portero_bloqueado = true;
            this.abstract_robot.setDisplayString("Portero bloqueado");
        }
        else
        {
            portero_bloqueado = false;
            this.abstract_robot.setDisplayString("Libre");
        }
        
    }
    
    /**
     * 
     *
     */
    private void Portero_Suplente()
    {
        // Cuando la pelota ha sobrepasado al portero intenta despejarla
        if( posPelota.x * SIDE > 0)
        {
            movimiento.sett( posPelota.t);
            movimiento.setr( 1.0);
            kickit = true;
        }

        // Se mantiene al portero en las cercanias de la porteria
        else if( (Math.abs(posPorteriaMia.x) > ROBOT_RADIUS * 1.4) ||
             (Math.abs(posPorteriaMia.y) > ROBOT_RADIUS * 4.25) )

        {
            movimiento.sett( posPorteriaMia.t);
            movimiento.setr( 1.0);
        }

        // Mantiene la posicion entre la porteria y la pelota
        else
        {
            if( posPelota.y > 0)
                movimiento.sety( 7);
            else
                movimiento.sety( -7);

            movimiento.setx( (double)SIDE);

            if( Math.abs( posPelota.y) < ROBOT_RADIUS * 0.15)
                movimiento.setr( 0.0);
            else
                movimiento.setr( 1.0);
        }
        
        avoidcollision();
        
    }
    
    private void Defensa_Bloqueo(int d1, int d2)
    {
        
        
        Vec2 dPorteriaPelota = new Vec2(posPelota);
        dPorteriaPelota.sub(posPorteriaMia);
            
        if( posPelota.x * SIDE > 0)
        {
            get_behind(posPelota, posPorteriaSuya);
            kickit = true;
            abstract_robot.setDisplayString("Despejando");
        
        }
        
        else if(dPorteriaPelota.r < ROBOT_RADIUS * d2)
        {
            movimiento.sett(posPelota.t);
            movimiento.setr(1);
            abstract_robot.setDisplayString("Achicando");
        }
        else if( (posPorteriaMia.r > ROBOT_RADIUS * d1))
        {
            movimiento.sett(posPorteriaMia.t);
            movimiento.setr(1);
            abstract_robot.setDisplayString("Volviendo");
            avoidcollision();
        }
        else if(Math.abs(posPorteriaMia.x) < ROBOT_RADIUS * d1)
        {
            movimiento.sett(posPelota.t);
            movimiento.setr(1);
            avoidcollision();
            abstract_robot.setDisplayString("HaciaPosicion");
        }
        else
        {
            movimiento.setr(0);
            abstract_robot.setDisplayString("EnPosicion");
        }
        
        

        
    }
    
    private void Salinas(int d1)
    {
        if(Math.abs(posPorteriaSuya.x) < ROBOT_RADIUS *d1)
        {
            Atacar();
        }       
        else
        {
            movimiento.sett(posPorteriaSuya.t);
            movimiento.setr(1);
            avoidcollision();
        }
    }
    
    /**
     *  Devuelve la posicion de una lista más cercana a una posicion dada
     */
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

    private void avoidcollision( )
    {
        // an easy way to avoid collision
    
        // first keep out of your teammates way
        // if your closest teammate is too close, the move away from
        this.abstract_robot.setDisplayString("Colisionando");
        this.abstract_robot.setDisplayString("Libre");
        if( amigoCercano.r < ROBOT_RADIUS*1.4)
        {
            movimiento.setx( -amigoCercano.x);
            movimiento.sety( -amigoCercano.y);
            movimiento.setr( 1.0);
            this.abstract_robot.setDisplayString("Colisionando");
        }
    
        // if the closest opponent is too close, movimiento away to try to
        // go around
        
        else if( enemigoCercano.r < ROBOT_RADIUS*1.4)
        {
                        
            movimiento.setx( -enemigoCercano.x);
            movimiento.sety( -enemigoCercano.y);
            movimiento.setr( 1.0);
            this.abstract_robot.setDisplayString("Colisionando");
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
        movimiento.sett( point.t);
        movimiento.setr( point.r);
        movimiento.add( behind_point);

        movimiento.setr( 1.0);

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

}// EOF
