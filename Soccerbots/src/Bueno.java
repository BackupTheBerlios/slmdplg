import EDU.gatech.cc.is.util.Vec2;
import EDU.gatech.cc.is.abstractrobot.*;

import java.lang.Math;

public class Bueno extends ControlSystemSS{
    
    private double angulo= 0;
    private int numero;
    private boolean izq;
    private final double ancho= 274.0;
    private final double alto= 152.2;
    private Vec2 vecTrIzq;
    /* 
     * En result.t guardo el nuevo heading
     * y en result.r guardo la nueva velocidad
     */
    private Vec2 result;
    private boolean disparo;
    
    public void Configure(){
        numero= abstract_robot.getPlayerNumber( 0 );
        result= new Vec2();
        disparo= true;
        izq= abstract_robot.getPosition( 0 ).x <= 0;
    }
    public int TakeStep(){
               //Res.Resultado.Guardar(this,abstract_robot);

        long c_time= abstract_robot.getTime();
        // me está en globales
        switch( numero ){
            case 0:
                // Portero:
                takeStepPortero( c_time );
                break;
            case 1:
                takeStepDefIzq( c_time );
                break;
            case 2:
                takeStepDefDer( c_time );
                break;
            case 3:
                takeStepDelIzq( c_time );
                break;
            case 4:
                takeStepDelDer( c_time );
                break;
        }
        // Coloca el ángulo
        abstract_robot.setSteerHeading( c_time, result.t );
        // Coloca la velocidad
        abstract_robot.setSpeed( c_time, result.r );
        // Si he decidido disparar
        if( disparo && abstract_robot.canKick( c_time ) )
            abstract_robot.kick( c_time );
        return CSSTAT_OK;
    }

    private void takeStepPortero( long c_time ){
        // Se va a la portería y se pone siempre entre la bola y la portería.
        Vec2 ball= abstract_robot.getBall( c_time );
        Vec2 ownGoal= abstract_robot.getOurGoal( c_time );
        Vec2 destino= new Vec2( ball );
        destino.sub( ownGoal );
        destino.setr( 0.2 );
        ownGoal.add( destino );
        result.sett( ownGoal.t );
        if( ball.r <= 0.6 ) result.setr( 1.0 );
        else result.setr( ownGoal.r );
        if( ball.r <= 0.2 ){
            result.setr( 3.0 );
            result.sett( ball.t );
        }
    }
    
    private void takeStepDefIzq( long c_time ){
        Vec2 ball= abstract_robot.getBall( c_time );
        Vec2 ourGoal= abstract_robot.getOurGoal( c_time );
        Vec2[] malos= abstract_robot.getOpponents( c_time );
        boolean defendiendo;
        if( izq ){
            defendiendo= ball.x<= 0;
        } else {
            defendiendo= ball.x>= 0;
        }
        if( defendiendo ){
            // Defendiendo
            int cerca= -1;
            for( int i= 0; i< 5; i++ ){
                if( malos[ i ].r > 0.01 ){
                    cerca= i;
                }
            }
            Vec2 me= abstract_robot.getPosition( c_time );
            if( cerca>= 0 ){
                if( izq && me.x < 0 ){
                    result.sett( malos[ cerca ].t );
                } else {
                    defensaIzqNoPlaca( me );
                }
                if( !izq && me.x > 0 ){
                    result.sett( malos[ cerca ].t );
                } else {
                    defensaIzqNoPlaca( me );
                }
            } else {
                defensaIzqNoPlaca( me );
            }
            result.setr( 3.0 );
        } else {
            // Atacando
            result.sett( ball.t );
            result.setr( 3.0 );
        }
    }

    private void takeStepDefDer( long c_time ){
        Vec2 ball= abstract_robot.getBall( c_time );
        Vec2 ourGoal= abstract_robot.getOurGoal( c_time );
        Vec2[] malos= abstract_robot.getOpponents( c_time );
        boolean defendiendo;
        if( izq ){
            defendiendo= ball.x<= 0;
        } else {
            defendiendo= ball.x>= 0;
        }
        if( defendiendo ){
            // Defendiendo
            int cerca= -1;
            for( int i= 0; i< 5; i++ ){
                if( malos[ i ].r > 0.01 ){
                    cerca= i;
                }
            }
            Vec2 me= abstract_robot.getPosition( c_time );
            if( cerca>= 0 ){
                if( izq && me.x < 0 ){
                    result.sett( malos[ cerca ].t );
                } else {
                    defensaDerNoPlaca( me );
                }
                if( !izq && me.x > 0 ){
                    result.sett( malos[ cerca ].t );
                } else {
                    defensaDerNoPlaca( me );
                }
            } else {
                defensaDerNoPlaca( me );
            }
            result.setr( 3.0 );
        } else {
            // Atacando
            result.sett( ball.t );
            result.setr( 3.0 );
        }
    }

    private void defensaDerNoPlaca( Vec2 me ){
        Vec2 e_me= new Vec2();
        if( izq ){
            e_me.setx( -0.7 - me.x );
            e_me.sety( -0.3 - me.y );
        } else {
            e_me.setx( 0.7 - me.x );
            e_me.sety( 0.3 - me.x );
        }
        result.setr( e_me.r );
        result.sett( e_me.t );
    }
    
    private void defensaIzqNoPlaca( Vec2 me ){
        Vec2 e_me= new Vec2();
        if( izq ){
            e_me.setx( -0.7 - me.x );
            e_me.sety( 0.3 - me.y );
        } else {
            e_me.setx( 0.7 - me.x );
            e_me.sety( -0.3 - me.x );
        }
        result.setr( e_me.r );
        result.sett( e_me.t );
    }

    private void takeStepDelIzq( long c_time ){
        Vec2 ball= abstract_robot.getBall( c_time );
        Vec2 ourGoal= abstract_robot.getOurGoal( c_time );
        Vec2[] malos= abstract_robot.getOpponents( c_time );
        boolean defendiendo;
        if( izq ){
            defendiendo= ball.x<= 0;
        } else {
            defendiendo= ball.x>= 0;
        }
        if( defendiendo ){
            // Defendiendo
            int cerca= 0;
            for( int i= 0; i< 5; i++ ){
                if( malos[ i ].r < malos[ cerca ].r ){
                    cerca= i;
                }
            }
            result.sett( malos[ cerca ].t );
            result.setr( 3.0 );
        } else {
            // Atacando
            if( ball.r< 0.01 ){
                int cerca= -1;
                for( int i= 0; i< 5; i++ ){
                    if( malos[ i ].r > 0.01 ){
                        cerca= i;
                    }
                }
                if( cerca>= 0 ){
                    disparo= false;
                    result.sett( ball.t );
                } else {
                    disparo= true;
                    result.sett( ball.t );
                }
            } else {
                result.sett( ball.t );
            }
            result.setr( 3.0 );
        }
    }

    private void takeStepDelDer( long c_time ){
        Vec2 ball= abstract_robot.getBall( c_time );
        Vec2 ourGoal= abstract_robot.getOurGoal( c_time );
        Vec2[] malos= abstract_robot.getOpponents( c_time );
        boolean defendiendo;
        if( izq ){
            defendiendo= ball.x<= 0;
        } else {
            defendiendo= ball.x>= 0;
        }
        if( defendiendo ){
            // Defendiendo
            int cerca= 0;
            for( int i= 0; i< 5; i++ ){
                if( malos[ i ].r < malos[ cerca ].r ){
                    cerca= i;
                }
            }
            result.sett( malos[ cerca ].t );
            result.setr( 3.0 );
        } else {
            // Atacando
            if( ball.r< 0.01 ){
                int cerca= -1;
                for( int i= 0; i< 5; i++ ){
                    if( malos[ i ].r > 0.01 ){
                        cerca= i;
                    }
                }
                if( cerca>= 0 ){
                    disparo= false;
                    result.sett( ball.t );
                } else {
                    disparo= true;
                    result.sett( ball.t );
                }
            } else {
                result.sett( ball.t );
            }
            result.setr( 3.0 );
        }
    }
    
    private double angulo( Vec2 v ){
        // Ángulo del vector
        return Math.atan2( v.y, v.x );
    }
}
