import EDU.gatech.cc.is.util.Vec2;
import EDU.gatech.cc.is.abstractrobot.*;

public class EquipoPrueba extends ControlSystemSS
{
    //datos configuracion
    private long curr_time; 
    private long id;            
    private int lado;
    private double rotacion;    
    
    //Datos relevantes
    private Vec2 posicion;
    private Vec2 bola;  
    private Vec2 egoBola;
    private Vec2 nuestraPorteria; 
    private Vec2 suPorteria;        
    private Vec2[] nosotros;
    private Vec2[] ellos;       
    private Vec2 resultado;
    
    
    private boolean chuta;  
    
    private final double radio = abstract_robot.RADIUS;

    public void Configure()
    {
        resultado=new Vec2(0,0);
        curr_time = abstract_robot.getTime();
        if(abstract_robot.getOurGoal(curr_time).x>0)
            lado=1;
        else
            lado=-1;
    }
    //analizamos el campo, calculamos la estrategia, la ejecutamos.
    public int TakeStep()
    {       //Res.Resultado.Guardar(this,abstract_robot);

        analizaCampo();
        seleccionaJugador();
        ejecutaMovimiento();
        return(CSSTAT_OK);

     }
    
    //Sensores. 
    private void analizaCampo()
    {
        curr_time = abstract_robot.getTime();
        posicion=abstract_robot.getPosition(curr_time);
        id = abstract_robot.getPlayerNumber(curr_time);
        egoBola= abstract_robot.getBall(curr_time);
        bola=creaAbsolutos(egoBola);
        nuestraPorteria = abstract_robot.getOurGoal(curr_time);
        suPorteria = abstract_robot.getOpponentsGoal(curr_time);
        nosotros = abstract_robot.getTeammates(curr_time);
        ellos = abstract_robot.getOpponents(curr_time);
        rotacion=abstract_robot.getSteerHeading( curr_time);
        chuta=false;
    }
    //Actuadores
    private void ejecutaMovimiento()
    {
        abstract_robot.setSteerHeading(curr_time, resultado.t);
        abstract_robot.setSpeed(curr_time, resultado.r);
        if (chuta && abstract_robot.canKick( curr_time))
            abstract_robot.kick(curr_time);
    }
    
    //Calcula coordenadas absolutas (centro=centro del campo) del objeto.
    private Vec2 creaAbsolutos(Vec2 objeto)
    {
        Vec2 temp= new Vec2(objeto.x,objeto.y);
        temp.add(posicion);
        return temp;
    }
    //segun el id del jugador, se utiliza un comportamiento.
    private void seleccionaJugador()
    {
        if(id==0)
        {
            portero();
        }
        else if(id==1)
        {
            defensa();
        }
        else if(id==2)
        {
            this.medioEstorbo();
        }
        else if(id==3)
        {
            central();
        }
        else
        {
            delantero();
        }
        
    }
    
    //-SI esta fuera del area ENTONCES se va a la porteria.
    //-SI esta en la porteria ENTONCES se pondrá siempre entre la pelota y la porteria.
    //REUTILIZADO DE DTEAM.
    private void portero()
    {
        //esta fuera del area
        if( (Math.abs(nuestraPorteria.x) > radio * 1.4) ||
                 (Math.abs(nuestraPorteria.y) > radio * 4.25) )

            {
                resultado.sett( nuestraPorteria.t);
                resultado.setr( 1.0);
            }

        //Esta dentro
        else 
        {
            if( egoBola.y > 0)
                resultado.sety( 7);
            else if(egoBola.y<0)
                resultado.sety( -7);
            
            //muevete hasta los limites de la porteria.
            if( Math.abs( egoBola.y) < this.radio * 0.15)
                resultado.setr( 0.0);
            else
                resultado.setr( 1.0);
        }
    }
    
    //SI la pelota esta en nuestro campo 
    //      SI estoy lejos de la pelota ENTONCES voy a ella
    //      SI estoy cerca de la pelota ENTONCES la conduzco.
    //SI la pelota esta en el campo contrario
    //      SI estoy lejos de la pelota ENTONCES me voy al centro del campo
    //      SI estoy cerca de la pelota ENTONCES la conduzco.
    private void defensa()
    {
        //Pelota en nuestro campo
        if(pelotaEnNuestroCampo())
        {
            //Si estoy lejos de la pelota 
            if(bola.octant()!= posicion.octant() )
            {
                resultado.setx(egoBola.x);
                resultado.sety(egoBola.y);
                resultado.setr(1.0);
            }
            //estoy cerca, intento correr con ella.
            else
            {
                correConLaPelota();
            }
            
        }
        //Pelota en campo contrario
        else
        {
            //si estoy lejos de la pelota, vuelvo al centro
            if(bola.octant()!= posicion.octant())
            {
                Vec2 temp=new Vec2(-posicion.x,-posicion.y);
                resultado.sett(temp.t);
                resultado.setr(1.0);
            }
            //corro con  la pelota.
            else
            {
                correConLaPelota();
            }
        }
        
    }
    
    /*Su mision es bloquear a su portero, para dejar la porteria libre.
     * Siempre va a ir a la dirección donde esta este, con velocidad maxima 
     * y lo intentará bloquear.
     */
    private void medioEstorbo()
    {
        Vec2 porteroOponente=masCercano(suPorteria,ellos);
        if(lado==-1 && (posicion.octant()==0||posicion.octant()==7))
            resultado.sett(porteroOponente.t);
        else if(lado==1 && (posicion.octant()==3||posicion.octant()==4))
            resultado.sett(porteroOponente.t);
        else 
            resultado.sett(suPorteria.t);
        resultado.setr(1.0);
    }
    
    //SI soy el mas cercano de mi equipo a la pelota ENTONCES corro con ella
    //SINO me espero en el centro del campo
    private void delantero()
    {
        Vec2 cercano=masCercano(egoBola,nosotros);
        Vec2 temp =new Vec2(cercano.x,cercano.y);
        temp.sub(egoBola);
        if( temp.r > egoBola.r)
            this.correConLaPelota();
        else
        {
            Vec2 temp2=new Vec2(-posicion.x,-posicion.y);
            resultado.sett(temp2.t);
            resultado.setr(1.0);
        }
    }

    //Siempre voy a por la pelota, me pongo mirando 
    //a la porteria contraria y corro con ella en esa direccion
    
    private void central()
    {
        this.correConLaPelota();
    }
    
    //devuelve si la pelota esta en nuestro campo o no.
    private boolean pelotaEnNuestroCampo()
    {
        if((bola.quadrant()==1 || bola.quadrant()==2)&& lado==-1 )
            return true;
        else if ((bola.quadrant()==0 || bola.quadrant()==3)&& lado==1 )
            return true;
        else
            return false;
    }
    
    //SI no estoy detras de la pelota ENTONCES me pongo detras.
    //SI estoy detras ENTONCES corro hacia la porteria contraria
    //SI estoy detras y estoy cerca de la porteria contraria ENTONCES chuto.
    private void correConLaPelota()
    {
        //Estoy detras de la pelota.
        if(detrasDe(egoBola,suPorteria))
        {
            //Si estoy cerca, chuto
            if( (Math.abs( rotacion - suPorteria.t) < Math.PI/8) &&
                    (suPorteria.r < 0.35))
                    chuta = true;
            //Estoy lejos, corro a la porteria.
            else 
                resultado.sett(suPorteria.t);
                resultado.setr(1.0);
        }
        //Me tengo q poner detras de la pelota.
        else
        {
            ponteDetrasDeLaPelota();
        }
    }
    
    //Indica si estoy detras de un objeto con respecto a una referencia
    //Reutilizado de DTEAM.
    private boolean detrasDe(Vec2 objeto,Vec2 referencia)
    {
        if( Math.abs( objeto.t - referencia.t) < Math.PI/10) 
            return true;
        else
            return false;
    }
    
    //REUTILIZADO DE DTEAM
    private void ponteDetrasDeLaPelota()
    {
        Vec2 behind_point = new Vec2(0,0);
        double behind = 0;
        double point_side = 0;

        // find a vector from the point, away from the orientation
        // you want to be
        behind_point.sett( suPorteria.t);
        behind_point.setr( suPorteria.r);

        behind_point.sub( egoBola);
        behind_point.setr( -radio*1.8);

            // determine if you are behind the object with respect
            // to the orientation
        behind = Math.cos( Math.abs( egoBola.t - behind_point.t));

            // determine if you are on the left or right hand side
            // with respect to the orientation
        point_side = Math.sin( Math.abs( egoBola.t - behind_point.t));

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
        resultado.sett( bola.t);
        resultado.setr( bola.r);
        resultado.add( behind_point);

        resultado.setr( 1.0);

    }
    
    //indica quien de objects, esta más cerca de punto.
    private Vec2 masCercano( Vec2 punto, Vec2[] objects)
    {
        Vec2 mejor=new Vec2(0,0);
        if(objects.length>0)//inicialmente el más cercanos es el primero
        mejor=objects[0];
        Vec2 actual=new Vec2(0,0);
        //Para el resto
        for( int i=1; i < objects.length; i++)
        {
            actual.sett( objects[i].t);
            actual.setr( objects[i].r);
            actual.sub( punto);
            //Si la distancia es menor que las distancia mejor
            //tenemos un vector más cercano.
            if(actual.r < mejor.r)
                mejor = objects[i];
        }
        
        return mejor;
    }
}
