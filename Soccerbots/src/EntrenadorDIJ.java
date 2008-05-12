
import	EDU.gatech.cc.is.util.Vec2;
import	EDU.gatech.cc.is.abstractrobot.*;
import javax.swing.JOptionPane;
import java.util.Vector;
import java.io.*;
import javax.swing.*;


public class EntrenadorDIJ {
	
	//Para no perder el contenido del archivo
	Vector baseDeCasosInicial;
	//La leemos del archivo y la metemos en un vector
	Vector baseDeCasos;
	//Cada caso estara compuesto por un vector con las posiciones¡x,y!
	//de los jugadores de nuestro equipo, las posiciones¡x,y! del equipo contrario
	//la posicion del balon¡x,y!; el numero de estrategia:
	//0 5 area del contrario
	//1 3 centrocampistas y 2 en area del contrario
	//2 portero y 2 centrocampistas y 2 en area del contrario
	//3 1 portero 1 defensa y 3 atacando
	//4 1 portero 1 defensa 1 centocampista y 2 atacando
	//5 1 portero 2 defensas 1 centroampista y 1 atacando
	//6 1 portero 3 defensas y 1 centrocampista
	//7 1 portero y 4 defensas
	//8 1 portero 2 defensas y 2 centrocampistas
	//9 1 portero 1 defensa 3 centrocampistas
	//y el numero de goles que se han marcado con ella(gol a favor suma 1 gol en contra resta 1)
	//Por ejemplo
	//¡1.2,0.2!¡1.2,0.2!¡1.2,0.2!¡1.2,0.2!¡1.2,0.2!
	//¡1.2,0.2!¡1.2,0.2!¡1.2,0.2!¡1.2,0.2!¡1.2,0.2!
	//¡0.0,0.2!¡5!¡2!
	Vector caso;
	//indica la estrategia a seguir
	int estrategia;
	//se incrementa en:
	//-1 si marcar el equipo contrario
	//0 al inicializarlo
	//1 si mrcamos gol
	int gol;
	//el archivo correspondiente a la base de casos
	File archivo;

	

	EntrenadorDIJ(){
			baseDeCasos=new Vector();
			baseDeCasosInicial=new Vector();
			caso = new Vector();
			gol=0;
			leeBC();
	}
	
	//imprimer el contenido del caso actual
	public void imprimete(){
		
		for(int i = 0; i < caso.size();i++){
			if(caso.get(i)!=null)
				System.out.println(i+"-->"+caso.get(i).toString());
		}
	}
	
	//lee de un archivo todos los casos y lso mete en el vector
	public void leeBC(){
		/*JFileChooser chooser=new JFileChooser();
		chooser.showOpenDialog(null);
		if (chooser.getSelectedFile()!=null){
			archivo= chooser.getSelectedFile();*/
			try{
				BufferedReader reader= new BufferedReader(new FileReader("casosDIJ.txt"));
				String linea= reader.readLine();
				while(linea!=null) {
					baseDeCasos.add(linea);
					baseDeCasosInicial.add(linea);
					linea=reader.readLine();
				}			
			}catch(Exception ex){
				ex.printStackTrace();
			}
		//}

	}
	
	//reinicia el caso(lo borra)
	public void reiniciaCaso(){
		baseDeCasos= new Vector();
		baseDeCasos = (Vector)baseDeCasosInicial.clone();
		caso= new Vector();
		gol=0;
	}
	
	//actuliza la base de casos guardandola en un archivo(el mismo de la base de casos inicial)
	public void actualizaBC(){
		try{
			String casoAux = "";
			FileWriter escritura = new FileWriter(archivo);
			for(int i=0;i<baseDeCasosInicial.size();i++){
				escritura.write(baseDeCasosInicial.get(i).toString());
				escritura.write("\n");
			}
			for(int i=0;i<caso.size();i++){
				casoAux=casoAux+caso.get(i);
				escritura.write(caso.get(i).toString());
			}
			baseDeCasosInicial.add(casoAux);
			escritura.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	public void aniadePosicionLocal(double x, double y,int numJugador){
		String posicion;
		posicion="¡"+x+","+y+"!";
		caso.insertElementAt(posicion, numJugador);
	}
	
	public void aniadePosicionVisitante(double x, double y,int numJugador){
		String posicion;
		posicion="¡"+x+","+y+"!";
		caso.insertElementAt(posicion, numJugador+5);
	}
	
	public void aniadePosicionBalon(double x, double y){
		String posicion;
		posicion="¡"+x+","+y+"!";
		caso.insertElementAt(posicion, 10);
	}
	
	public void aniadeEstrategia(int estrategia){
		String est="";
		est="¡"+estrategia+"!";
		caso.insertElementAt(est, 11);
	}
	
	public void aniadeResultado(int resultado){
		String rst="";
		rst="¡"+resultado+"!";
		caso.insertElementAt(rst, 12);
	}
	
	//va de 0 a 11
	//por cada posicion q esta en el msimo cuadrante que el caso suma 1
	public double calculaSimilitud(String casoBase){
		double similitud=0.0;
		double LARGO = 1.37;
		double ANCHO = 0.7625;
		Vector cb=new Vector();
		Vector c= new Vector();
		String auxiliarC, auxiliarCB;
		double auxXC,auxXCB;
		double auxYC,auxYCB;
		cb=pasaCaV(casoBase);
		for(int i =0;i<10;i++){
			auxiliarC = (String)caso.get(i);
			auxiliarCB = (String)cb.get(i);
			auxiliarC = auxiliarC.substring(1, auxiliarC.length()-1);
			auxiliarCB = auxiliarCB.substring(1, auxiliarCB.length()-1);
			auxXC = Double.valueOf(auxiliarC.split(",")[0]).doubleValue();
			auxYC = Double.valueOf(auxiliarC.split(",")[1]).doubleValue();
			auxXCB = Double.valueOf(auxiliarCB.split(",")[0]).doubleValue();
			auxYCB = Double.valueOf(auxiliarCB.split(",")[1]).doubleValue();
			if(cuadrante(auxXC,auxYC)==cuadrante(auxXCB,auxYCB)){
				similitud++;
			}
		}		
		return similitud;
	}

	//el umbral va de 0 a 11 como el rango de la similitud posible
	//nos quedamos en la base de casos solo con los casos que superan el umbral
	//y de estos casos nos quedamos con el que mejor resultado tenga
	//si ninguno supera el umbral nos quedamos con una estrategia aleatoria del 0 al 9
	
	public int dameEstrategia(double umbral){
		int estrategia=0;
		int rstAux=0;
		String rstBDC="";
		Vector caso= new Vector();
		barridoCasos(umbral);
		estrategia=estrategiaAleatoria();
		if(baseDeCasos.size()>0){
			//System.out.println(baseDeCasos.size());
			for(int i =0; i< baseDeCasos.size();i++){
				caso=pasaCaV((String)baseDeCasos.get(i));
				rstBDC=(String)caso.get(12);
				if(Integer.parseInt(rstBDC.substring(1,rstBDC.length()-1))>=rstAux){
						rstAux=Integer.parseInt(rstBDC.substring(1,rstBDC.length()-1));
						estrategia=Integer.parseInt(((String)caso.get(11)).substring(1,((String)caso.get(11)).length()-1));
				}
			}
		}
		return estrategia;
	}
	
	public void barridoCasos(double umbral){
		Vector similitudes = new Vector();
		for(int i=0;i<baseDeCasos.size();i++){
			if(calculaSimilitud((String)baseDeCasos.get(i))<umbral){
				baseDeCasos.remove(i);
				i=i-1;
			}
		}
	}
	
	public int estrategiaAleatoria(){
		int estrategia = (int)((Math.random()*10)%9);
		return estrategia;
	}
	
	public Vector pasaCaV(String caso){
		Vector vCaso = new Vector();
		String[] aux;
		String auxStr;
		aux=caso.split("!");
		for(int i=0;i<13;i++){
			auxStr=aux[i]+"!";
			vCaso.add(auxStr);
		}
		return vCaso;
	}
	
	public int cuadrante(double x, double y){
		int cuadrante=0;
		double LARGO = 1.37;
		double ANCHO = 0.7625;
		if(y>=0){
			if(x<=-(LARGO/2)) {
				cuadrante=0;
			}else if(x<=0){
				cuadrante=1;
			}else if(x>(LARGO/2)){
				cuadrante=3;
			}else{
				cuadrante=2;
			}
		}else{
			if(x<=-(LARGO/2)) {
				cuadrante=7;
			}else if(x<=0){
				cuadrante=6;
			}else if(x>(LARGO/2)){
				cuadrante=4;
			}else{
				cuadrante=5;
			}
		}
		return cuadrante;
	}
	
	public double xPosicion(Vec2 porteria){
		double x=0.0;
			x=-1.37-porteria.x;
		return x;
	}
	
	public double yPosicion(Vec2 porteria){
		double y=0.0;
			y=-porteria.y;
		return y;
	}
	
	public double xPosicionO(Vec2 porteria){
		double x=0.0;
			if(porteria.x>=1.37)
				x=-porteria.x+1.37;
			else
				x=porteria.x;
		return x;
	}
	
	public double yPosicionO(Vec2 porteria){
		double y=0.0;
			y=-porteria.y;
		return y;
	}
	
	public double xPosicion(Vec2 meta, Vec2 oponente){
		return 1.37-(meta.x-oponente.x);
	}
	
	public double yPosicion(Vec2 meta, Vec2 oponente){
		return -(meta.y-oponente.y);
	}
}
