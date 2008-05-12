import EDU.gatech.cc.is.abstractrobot.ControlSystemSS;
import EDU.gatech.cc.is.util.Vec2;

public class GabiJaviTeam extends ControlSystemSS{
    private long    numero;
    private long    tiempo;
    private double  direccion;
    private Vec2    posicion;
    private Vec2    miPorteria;
    private Vec2    porteriaContraria;
    private Vec2    pelota;
    private Vec2[]  miEquipo;
    private Vec2[]  rivales;
    private Vec2 masCercanoAmigo;
    private Vec2 masCercanoEnemigo;
    private boolean masCercanoAPelota;
    private  int lado; //si estoy en el izquierdo es FALSE y en el derecho es TRUE.
    
    public void configure(){
        if( abstract_robot.getOurGoal(tiempo).x < 0)
            lado=-1;
        else
            lado=1;
    }
    
    public int takeStep(){
        //Res.Resultado.Guardar(this,abstract_robot);
         actualizarInfo();
        
        if (numero==0){
            portero();
        }
        if (numero==1){
            cierre();
        }
        if (numero==2){
            ala1(1);
        }
        if (numero==3){
            pivote();
        }
        if (numero==4){
            ala1(-1);
        }
        return 0;
    }
    private void actualizarInfo() {
        tiempo=abstract_robot.getTime();
        posicion=abstract_robot.getPosition(tiempo);
        numero=abstract_robot.getPlayerNumber(tiempo);
        direccion=abstract_robot.getSteerHeading(tiempo);
        miPorteria=abstract_robot.getOurGoal(tiempo);
        porteriaContraria=abstract_robot.getOpponentsGoal(tiempo);
        pelota=abstract_robot.getBall(tiempo);
        miEquipo=abstract_robot.getTeammates(tiempo);
        rivales=abstract_robot.getOpponents(tiempo);
        masCercanoAmigo=elMasCercano(posicion,miEquipo);
        masCercanoEnemigo=elMasCercano(posicion,rivales);
        masCercanoAPelota=hallarMasCercano();
        
        
    }

    private boolean hallarMasCercano() {
        int i=0;
        double radio=10000;
        while (i<miEquipo.length){
            Vec2 aux=new Vec2(0,0);
            aux.sett(pelota.t);
            aux.setr(pelota.r);
            aux.sub(miEquipo[i]);
            if (radio>aux.r){
                radio=aux.r;
            }
            i++;
        }
        if (radio>pelota.r){
            return true;
        }else{
            return false;
        }
        
    }

    private void pivote() {
        boolean enSuSitio=posicion.x*lado<=0.5;
        boolean detrasDePelota=pelota.x*lado<0;
        if (enSuSitio){
            if (detrasDePelota){
                if (masCercanoAPelota){
                    abstract_robot.setDisplayString("Oriento con parabola!");
                    Vec2 aux=orientaConParabola();//orientoA(porteriaContraria);
                    avanza(aux.t,1.0,false);
                    direccion=pelota.t;
                    if (porteriaContraria.r<1)
                        disparar();
                    else conduceBola();
                    
                }
                else{
                    abstract_robot.setDisplayString("Detras de pelota pero lejos");
                    Vec2 aux=new Vec2(0,0);
                    aux.setx(porteriaContraria.x);
                    aux.sety(porteriaContraria.y);
                    aux.sub(pelota);
                    avanza(aux.t,1,true);
                }
            }
            else{
                
                if ((posicion.x<=0.5*lado)&&(Math.abs(posicion.y)<=0.25)){
                    abstract_robot.setDisplayString("Quieto");
                    avanza(0,0,true);
                }
                else{
                Vec2 aux=new Vec2(0,0);
                aux.setx(miPorteria.x);
                aux.sety(miPorteria.y);
                aux.sub(pelota);
                avanza(aux.t,1,true);
                abstract_robot.setDisplayString("Delante de la pelota x="+aux.x+" y="+aux.y);
                }
            }
        }
        else{abstract_robot.setDisplayString("No en mi sitio");
            double angulo;
            Vec2 aa=new Vec2(0,0);
            if (lado>0){
                angulo=Math.PI;
                
            }else{
                angulo=0;
            }
//          avanza(angulo,0.5,true);
            avanza(angulo,0.5,true);
        }
    
    }
    
    private void ala1(int n) {
        boolean ladoAdecuado= n*posicion.y>0;
        if (ladoAdecuado){
            boolean detrasDePelota=pelota.x*lado<0;
            if (detrasDePelota){
                if (masCercanoAPelota){
                    abstract_robot.setDisplayString("Ala: a por la bola!");
                    Vec2 aux=orientaConParabola();
                    avanza(aux.t,1,true);
                    disparar();//si se puede
                }
                else{abstract_robot.setDisplayString("Ala: a por la porteria");
                    avanza(porteriaContraria.t,1,true);
                }
                    
            }
            else{abstract_robot.setDisplayString("Ala: a defender!");
                Vec2 aux=new Vec2(0,0);
                aux.setx(miPorteria.x);
                aux.sety(miPorteria.y);
                aux.add(pelota);
                avanza(aux.t,1.0,false);
            
            }
        }       
        else{ abstract_robot.setDisplayString("Ala: a tu sitio!");
                avanza(n*Math.PI/2,0.5,true);
                
            }       
    }
    private void cierre() {
        if (posicion.x*lado<0){
            avanza(miPorteria.t,1.0,true);
            abstract_robot.setDisplayString("Nada");
        }
        else{boolean balonDetras=pelota.x*lado > 0;
            if (balonDetras){
                Vec2 aux=new Vec2(0,0);
                aux.t=miPorteria.t;
                aux.r=miPorteria.r;
                aux.add(pelota);
                avanza(aux.t,1,false);
                if (abstract_robot.canKick(tiempo)){
                    abstract_robot.setDisplayString("Podria disparar...");
                    avanza(direccion+Math.PI/6*lado*(-1/posicion.y),1,false);
                }
            }
            else{abstract_robot.setDisplayString("Nada");
                boolean balonCerca=pelota.r<0.50;
                if (balonCerca){
                    if (masCercanoAPelota){
                        avanza(pelota.t,1,false);
                        disparar();
                    }
                    else{
                        Vec2 aux=new Vec2(0,0);
                        aux.t=miPorteria.t;
                        aux.r=miPorteria.r;
                        aux.add(pelota);
                        avanza(aux.t,0.5,false);
                    }
                    
                }
                else{//balon no esta cerca: no pasa nada
                    abstract_robot.setDisplayString("Nada");
                    boolean estoyColocado=((posicion.x==lado*0.60)&&(posicion.y==0));
                    if (!estoyColocado){
                        Vec2 aux= new Vec2(0.60*lado,0);
                        aux.sub(posicion);
                        avanza(aux.t,1.0,true);
            
                    }
                    else{
                        avanza(direccion,0,true);
                    }
                }
            }
            
            
            
        }
        
        
    }
private void portero() {
    
    double radioPelota=pelota.r;
    boolean vienePelota=radioPelota<0.40;
    boolean detrasDePelota=pelota.x*lado < 0;
    if (detrasDePelota){
        if (vienePelota){
            avanza(pelota.t,1.0,false);
            disparar();
        }
        else{//no pasa nada
            double radio=miPorteria.r;
            boolean fueraArea=radio>0.12;
            if (fueraArea){
                avanza(miPorteria.t,1.0,false);
            }
            else{
            avanza(pelota.t,0.1,false);
            }
        }
    }
    else{
        Vec2 aux=new Vec2(0,0);
        aux.add(pelota);
        aux.add(miPorteria);
        avanza(aux.t,1,false);
    }
    
}   
/*  
public void mover_la_bola(Vec2 objetivo){
if ((estoyOrientado(pelota,objetivo))){
    boolean estoyCercaPorteria=objetivo.r<2;  
    if (estoyCercaPorteria){
        avanza(objetivo.t,1.0,true);
        disparar();
    }
    else{avanza(objetivo.t,1.0,true);}
    
}
else{orientarse(objetivo);
    //estoy cerca pero no detras.
        
    }
    

}*/

public Vec2 elMasCercano(Vec2 sitio,Vec2[] chapas){
    Vec2 resultado=new Vec2(0,0);
    double distancia=10000;
    int i=0;
    while (i<chapas.length){
        if (distancia>chapas[i].r){
            distancia=chapas[i].r;
            resultado=chapas[i];
            
        }
        i++;
    }
    return resultado;
    
}

public void avanza(double donde, double vel, boolean evitar){
    //avanza hacia el punto 'donde', con velocidad 'vel' y evitando obstaculos
if (evitar){
    donde=evitarChapas(donde);
}
    abstract_robot.setSteerHeading(tiempo,donde);
    abstract_robot.setSpeed(tiempo,vel);
    }

private double evitarChapas(double donde) {
    if (masCercanoAmigo.r<0.1){
        boolean direccionesContrarias=Math.abs(masCercanoAmigo.t-donde)>Math.PI/2;
        if (!direccionesContrarias){
            donde+=Math.PI+masCercanoAmigo.t;
        }
        
    }
    else{
        if (masCercanoEnemigo.r<0.1){
            boolean direccionesContrarias=Math.abs(masCercanoEnemigo.t-donde)>Math.PI/2;
            if (!direccionesContrarias){
                donde+=Math.PI+masCercanoEnemigo.t;
            }
        }
    }
    return donde;
}

public Vec2 orientoA(Vec2 destino){
    //solo llamo a esta funcion cuando veo que estoy detras y quiero orientarme a un destino.
boolean estoyBienDirigido= Math.abs(pelota.t-destino.t)<Math.PI/20;//valorar si hacer el angulo menor!!
Vec2 direccion=new Vec2(0,0);
if (estoyBienDirigido){
    direccion.setx(pelota.x);
    direccion.sety(pelota.y);
}
else{boolean enLaMismaVertical=Math.abs(pelota.x)<0.15;
    if (enLaMismaVertical){
        direccion.sety(pelota.y);
        direccion.setx(-pelota.x*2);//para que gire un poco mas
    }else{
        direccion.sety(pelota.y);
        direccion.setx(0);
        //direccion.add(pelota);
    }
    
}
return direccion;   
}

public void disparar(){
    if (porteriaContraria.r<0.5){//valorar las distancias
        avanza(direccion+Math.PI/10,1,true);
    }
    
    if (abstract_robot.canKick(tiempo)){
        abstract_robot.kick(tiempo);
    }
    
}

public Vec2 orientaConParabola(){ //el cambio no ha sido bueno...
    Vec2 direccionP=new Vec2();
//if (pelota.r>0.2){
    double mitadDistanciaEntrePuntos=pelota.r;
    double radioParabola=Math.sqrt(Math.pow(mitadDistanciaEntrePuntos,2)*2);
    double anguloParabola=pelota.t+(Math.PI/4);//ojo cambio de lado!
    if (pelota.y<0){
        anguloParabola*=-1;
        }
    direccionP.setr(radioParabola);
    direccionP.sett(anguloParabola);
    return direccionP;
/*  }
else{double diferencia=pelota.t-porteriaContraria.t;
    if (Math.abs(diferencia)>Math.PI/20){
        if (diferencia>0){//fatal para el cambio de lado!
            direccionP.sett(Math.PI/2+porteriaContraria.t);
        }
        else{
            direccionP.sett(porteriaContraria.t-Math.PI/2);
        }
    }
    else{
        direccionP.setx(porteriaContraria.x);
        direccionP.sety(porteriaContraria.y);
        direccionP.sub(pelota);

    }
    
        return direccionP;
}*/
}

public void conduceBola(){
disparar();
}

}
