import java.util.Vector;

import EDU.gatech.cc.is.util.Vec2;

public class Sectores {
	
//	------------------------------------------------------------------------------
	  //Devuelve 2 ángulos que corresponden al sector de círculo que cubre el robot
	  public static double[] calculaSectorCubierto(Vec2 robot,double radio) {
		  double angulo1;
		  double angulo2;
		  double t = robot.t;
		  Vec2 aux;
		  aux = new Vec2(robot.x+Math.sin(t)*radio,robot.y-Math.cos(t)*radio);
		  angulo1 = aux.t;
		  aux = new Vec2(robot.x-Math.sin(t)*radio,robot.y+Math.cos(t)*radio);
		  angulo2 = aux.t;
		  if(angulo1<0)angulo1+=(Math.PI*2);
		  if(angulo2<0)angulo2+=(Math.PI*2);
		  double[] sector = {angulo1, angulo2};
		  return sector;		
	}
//	------------------------------------------------------------------------------  
	  public static Vector eliminaZonaCubierta(Vector zonasLibres, double[] sectorCubierto) {

		  Vector zonas = new Vector();
		  Vector zonasAux = new Vector();
		  double[] zonaLibre;
		  double [] zonaLibreBaja= new double[2];
		  double [] zonaLibreAlta=new double[2];
		  if((sectorCubierto[1]-sectorCubierto[0])<0){//si el obstaculo esta en medio del eje x
			  double cero=sectorCubierto[0];
			  double uno=sectorCubierto[1];
			  zonaLibreBaja[0]=cero;
			  zonaLibreBaja[1]=2*Math.PI;
			  zonaLibreAlta[1]=uno;
			  zonaLibreAlta[0]=0;
			  zonasAux=eliminaZonaCubierta(zonasLibres,zonaLibreAlta);
			  zonas=eliminaZonaCubierta(zonasAux,zonaLibreBaja);  
		  } else//si el obstaculo no esta en medio eje x
		  for(int i=0;i<zonasLibres.size();i++){//xa cada zona libre miramos si esta en medio zona x
			  zonaLibre=(double[])zonasLibres.get(i);
			  if ((zonaLibre[1]-zonaLibre[0])<0){
				  //dividimos en 2
				  double cero=zonaLibre[0];
				  double uno=zonaLibre[1];
				  zonaLibreBaja[0]=cero;
				  zonaLibreBaja[1]=2*Math.PI;
				  zonaLibreAlta[1]=uno;
				  zonaLibreAlta[0]=0;
					zonasLibres.remove(i);
				  zonasLibres.add(zonaLibreBaja);
				  zonasLibres.add(zonaLibreAlta);
				  zonas=eliminaZonaCubierta(zonasLibres,sectorCubierto);
				  return zonas;
			  }else{//si todo bien 
				  //hay 3 casos
				  if((sectorCubierto[0]<=zonaLibre[0])&&(sectorCubierto[1]<zonaLibre[1])&&(sectorCubierto[1]>zonaLibre[0])){//caso oponente abajo de zonalibre pero dentro de ella

					  double unoOcupado=sectorCubierto[1];
					  double uno=zonaLibre[1];
					  zonaLibreAlta[1]=uno;
					  zonaLibreAlta[0]=unoOcupado;
						zonasLibres.remove(i);
					  zonasLibres.add(zonaLibreAlta);
					  return zonas=eliminaZonaCubierta(zonasLibres,sectorCubierto);
				  	}else{
				  		if((sectorCubierto[0]>zonaLibre[0])&&(sectorCubierto[1]<zonaLibre[1])){//oponente en medio
				  			double unoOcupado=sectorCubierto[1];
				  			double ceroOcupado=sectorCubierto[0];
							  double uno=zonaLibre[1];
							  double cero=zonaLibre[0];
				  				zonaLibreBaja[0]=cero;
							  zonaLibreBaja[1]=ceroOcupado;
							  zonaLibreAlta[1]=uno;
							  zonaLibreAlta[0]=unoOcupado;
								zonasLibres.remove(i);
							  zonasLibres.add(zonaLibreBaja);
							  zonasLibres.add(zonaLibreAlta);
							  return zonas=eliminaZonaCubierta(zonasLibres,sectorCubierto);
				  		}else{
				  			if((zonaLibre[0]<sectorCubierto[0])&&(zonaLibre[1]<=sectorCubierto[1])&&(sectorCubierto[0]<zonaLibre[1])){//esta arriba pero sta dentro
				  				double ceroOcupado=sectorCubierto[0];
								double cero=zonaLibre[0];
				  				zonaLibreAlta[1]=ceroOcupado;
								zonaLibreAlta[0]=cero;
								zonasLibres.remove(i);
								zonasLibres.add(zonaLibreAlta);
								return zonas=eliminaZonaCubierta(zonasLibres,sectorCubierto);
				  			
				  			}else{
				  				if((sectorCubierto[0]==zonaLibre[0])&&(sectorCubierto[1]==zonaLibre[1])){//si por casualidad son =les la zona libre y la ocupada simplemente se elimina la libre
				  				zonasLibres.remove(zonaLibre);
				  				return zonas=zonasLibres;
				  				}
				  				else{//si no es ningun caso de los de antes es que no esta ocupando ninguna zona
				  					zonas=zonasLibres;
				  				}
				  			}
				  		}
				  		
				  	}  
			    } //felse	   
		  }//ffor
		  return zonasLibres;
	  }	  
//	------------------------------------------------------------------------------
	  //Devuelve 2 ángulos que corresponden al sector de círculo de palo a palo de la portería
	  public static double[] calculaSectorPorteria(Vec2 goal, int SIDE) {
		  double angulo1;
		  double angulo2;
		  Vec2 aux = new Vec2(goal.x, -0.25*SIDE);
		  angulo1 = aux.t;
		  aux = new Vec2(goal.x, 0.25*SIDE);
		  angulo2 = aux.t;
		  if (angulo1<0) angulo1 += 2*Math.PI;
		  if (angulo2<0) angulo2 += 2*Math.PI;
		  double[] sector = {angulo1, angulo2};
		  return sector;
	  }  
//	------------------------------------------------------------------------------
	  //Indica el cuadrante del punto
	  public static int cuadrante(Vec2 punto) {
		  if (punto.x >= 0 && punto.y>=0) return 0;
		  else if (punto.x < 0 && punto.y>=0) return 1;
		  else if (punto.x < 0 && punto.y<0) return 2;
		  else return 3;
	  }	  
}
