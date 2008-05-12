
import jess.*;
import EDU.gatech.cc.is.abstractrobot.*;
import EDU.gatech.cc.is.util.Vec2;

public class GuijarritosTeam extends ControlSystemSS{
    
    //Jess
    Rete jess_engine;

        private long    tiempo_actual;  //Tiempo actual
        private Vec2    yo;             //Posición en mi marco
        private Vec2    YO;             //Posición en el marco global
        private Vec2    pelota;         //Posición de la pelota
        private Vec2    su_meta;        //Autoexplicativo.
        private Vec2    nuestra_meta;
        private Vec2[]  companeros; //mi equipo
        private Vec2[]  rivales;        //mis rivales
        private long    ID;             //yo
        private Vec2    centro;         //centro del campo
        private double grados;

        private Vec2    pelota_meta;        //distancia de la pelota a la portería contraria
        private Vec2    pelota_portNuestra;     //distancia de la pelota a nuestra portería
        private Vec2    companero_cercano_pelota;//distancia al compañero más cercano a la pelota
        private Vec2    pelota_equipo;      //vector pelota_compañero más cercano
        private Vec2    rival_cercano_pelota;   //distancia del rival más cercano a la pelota
        private Vec2    pelota_rivales;     //vector pelota-rival más cercano
        private Vec2    mas_cercano;        //vector al jugador más cercano

        private Vec2    companero_mas_cercano;      //compañero más cercano a mí
        private Vec2    rival_mas_cercano;      //rival más cercano a mí
        private int     cambio;         //ayuda a intercambiar el ángulo con el cual se dispara
        private double velocidad;
        private boolean chutar;
        private Vec2 coord;


    public void Configure(){
            try{
                //jess
                jess_engine=new Rete();
                //cargar clp
                jess_engine.executeCommand("(batch GuijarritosTeam.clp)");
                jess_engine.executeCommand("(reset)");
                }
            catch(JessException je){
                System.out.println(je);
            }

            //tiempo
            tiempo_actual = abstract_robot.getTime();


        } 

    public int TakeStep(){
          
        //bbdd
        //Res.Resultado.Guardar(this,abstract_robot);
        
        //obtención de los datos: inicializacion de todos los vectores, ángulos...
        obtener_datos();
        try{
        //  
        jess_engine.clearStorage();
        
        //almacenamiento de los datos en jess
        jess_engine.store("x_pel",new Value(pelota.x,RU.FLOAT));
        jess_engine.store("y_pel",new Value(pelota.y,RU.FLOAT));
        jess_engine.store("x_pNuestra",new Value(nuestra_meta.x,RU.FLOAT));
        jess_engine.store("y_pNuestra",new Value(nuestra_meta.y,RU.FLOAT));
        jess_engine.store("x_pRival",new Value(su_meta.x,RU.FLOAT));
        jess_engine.store("y_pRival",new Value(su_meta.y,RU.FLOAT));
        jess_engine.store("numJugador",new Value(ID,RU.INTEGER));
        jess_engine.store("radio_robot",new Value(abstract_robot.RADIUS,RU.FLOAT));
        jess_engine.store("d_pel_meta",new Value(pelota_meta.r,RU.FLOAT));
        jess_engine.store("d_pel_portNuestra",new Value(pelota_portNuestra.r,RU.FLOAT));
        jess_engine.store("d_bola_equipo",new Value(pelota_equipo.r,RU.FLOAT));
        jess_engine.store("x_rival_mas_cercano",new Value(rival_mas_cercano.x,RU.FLOAT));
        jess_engine.store("y_rival_mas_cercano",new Value(rival_mas_cercano.y,RU.FLOAT));
        jess_engine.store("d_rival_mas_cercano",new Value(rival_mas_cercano.r,RU.FLOAT));
        jess_engine.store("x_comp_mas_cercano",new Value(companero_mas_cercano.x,RU.FLOAT));
        jess_engine.store("y_comp_mas_cercano",new Value(companero_mas_cercano.y,RU.FLOAT));
        jess_engine.store("d_comp_mas_cercano",new Value(companero_mas_cercano.r,RU.FLOAT));
        jess_engine.store("sen_ang_pel",new Value(Math.sin(pelota.t),RU.FLOAT));
        jess_engine.store("cos_ang_pel",new Value(Math.cos(pelota.t),RU.FLOAT));
        jess_engine.store("sen_ang_port",new Value(Math.sin(nuestra_meta.t),RU.FLOAT));
        jess_engine.store("cos_ang_port",new Value(Math.cos(nuestra_meta.t),RU.FLOAT));
        jess_engine.store("x_centro",new Value(centro.x,RU.FLOAT));
        jess_engine.store("y_centro",new Value(centro.y,RU.FLOAT));
        jess_engine.store("x_mas_cercano_a_pel",new Value(mas_cercano.x,RU.FLOAT));
        jess_engine.store("y_mas_cercano_a_pel",new Value(mas_cercano.y,RU.FLOAT));
        jess_engine.store("cambio",new Value(cambio,RU.INTEGER));
        
        //ejecutar
        jess_engine.executeCommand("(reset)");
        jess_engine.executeCommand("(assert (fase-obtener-datos))");
        jess_engine.executeCommand("(run)");
        
        //fetch de los datos
        Value x=jess_engine.fetch("coordX");
        Value y=jess_engine.fetch("coordY");
        Value m=jess_engine.fetch("velocidad");
        Value k=jess_engine.fetch("chutar");
        Value c=jess_engine.fetch("cambio");

        
        //ejecutar acciones

        if(x!=null) coord.setx(x.floatValue(null));

        if(y!=null) coord.sety(y.floatValue(null));
        
        //orientacion(en radianes)
        abstract_robot.setSteerHeading(tiempo_actual,coord.t);
        abstract_robot.setDisplayString("Orientando");
        
        //velocidad
        if(m!=null)
            velocidad=m.floatValue(null);
        abstract_robot.setDisplayString("Avanzando");
        //
        if(c!=null)
        cambio=c.intValue(null);

        if(k!=null){
            if( k.intValue(null)==1)
                chutar=true;
            else chutar=false;
        }
        
        //no chutamos
        if ((chutar== false) && abstract_robot.canKick(tiempo_actual))
            coord.sett(pelota.t + Math.PI);

        //orientación actual
        abstract_robot.setSteerHeading(tiempo_actual, coord.t);

        //velocidad
        abstract_robot.setSpeed(tiempo_actual,velocidad);

        //chutar si podemos
        if (chutar && abstract_robot.canKick(tiempo_actual))
            abstract_robot.kick(tiempo_actual);

    }
        catch(JessException je){
            System.out.println(je);
            }
        //
        return(CSSTAT_OK);
    }

    private void obtener_datos(){

        //tiempo
        tiempo_actual = abstract_robot.getTime();
        //origen
        yo = new Vec2(0,0); 
        //posición del robot
        ID = abstract_robot.getPlayerNumber(tiempo_actual);
        //inicialización del robot en el marco global
        YO = abstract_robot.getPosition(tiempo_actual);
        //centro del campo
        centro = new Vec2(-YO.x,-YO.y);
        //vector a la pelota
        pelota = abstract_robot.getBall(tiempo_actual);
        //meta rival
        su_meta= abstract_robot.getOpponentsGoal(tiempo_actual);
        //nuestra meta
        nuestra_meta = abstract_robot.getOurGoal(tiempo_actual);
        //compañeros de equipo
        companeros = abstract_robot.getTeammates(tiempo_actual);
        //rivales
        rivales = abstract_robot.getOpponents(tiempo_actual);

        //jugadores más cercanos de uno y otro equipo
        companero_cercano_pelota = mas_cercano(pelota, companeros);
        rival_cercano_pelota = mas_cercano(pelota, rivales);

        //disparar directo a meta
        cambio = 0;
        velocidad= 1.0;

        //vectores de la bola al más cercano
        pelota_equipo = new Vec2(companero_cercano_pelota.x, companero_cercano_pelota.y);
        pelota_equipo.sub(pelota);
        pelota_rivales = new Vec2(rival_cercano_pelota.x, rival_cercano_pelota.y);
        pelota_rivales.sub(pelota);

        //y ahora calculamos quién es el jugador más cercano
        Vec2 temp = new Vec2(0,0);
        if(pelota_equipo.r > pelota_rivales.r){
            mas_cercano = rival_cercano_pelota;
            temp.setr(pelota_rivales.r);
            temp.sett(pelota_rivales.t);
        }
        else{
            mas_cercano = companero_cercano_pelota;
            temp.setr(pelota_equipo.r);
            temp.sett(pelota_equipo.t);
        }
        if(temp.r >= pelota.r){
            mas_cercano = yo;
        }

        //distancia bola - portería rival
        pelota_meta = new Vec2(su_meta.x,su_meta.y);
        pelota_meta.sub(pelota);

        //distancia pelota - nuestra portería
        pelota_portNuestra = new Vec2(nuestra_meta.x, nuestra_meta.y);
        pelota_portNuestra.sub(pelota);

        //compañero y rival más cercano a mí
        companero_mas_cercano = mas_cercano(yo, companeros);
        rival_mas_cercano = mas_cercano(yo, rivales);
        //
        if(pelota.x*su_meta.x >=0)
            chutar= true;
        else
            chutar = false;
        coord=new Vec2(0,0);
    }

    //calcula cuál es el elemento más cercano del vector elem a ref_pt
    private Vec2 mas_cercano(Vec2 ref_pt, Vec2[] elem){

        Vec2 mc= new Vec2(0,0);
        double minDist = 99999;
        Vec2 pt_a_elem = new Vec2(0,0);
        for(int i=0; i<elem.length; i++){
            
            pt_a_elem.setr(elem[i].r);
            pt_a_elem.sett(elem[i].t);
            pt_a_elem.sub(ref_pt);
            
            if(pt_a_elem.r < minDist){  
                mc.sett(elem[i].t);
                mc.setr(elem[i].r);
                minDist = pt_a_elem.r;
            }
        }
        return mc;
    }
}
