/*
 * EntrenadorJSQTeam.java
 * Entrenador delequipo JSQTeam
 * Jose -
 * Sergio -
 * Quique -
 * Team
 */

import	EDU.gatech.cc.is.util.Vec2;
import	EDU.gatech.cc.is.abstractrobot.*;
// imports para las comunicaciones
import	java.util.Enumeration;
import	EDU.gatech.cc.is.communication.*;
// imports para lectura de ficheros
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.Integer;
import java.lang.Long;

/**
 * <p>Title: EntrenadorJSQTeam</p>
 * <p>Description: Entrenador del equipo realizado por Jose, Sergio 
 *	y Quique para el campeonato de SoccerBots</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * @author José María Sobrinos García, Sergio Díaz Jubera, Enrique Aguilera
 * @version 1.0
 */


public class EntrenadorJSQTeam
{
	// ----------------------------------------------------------------------
	// CONSTANTES -----------------------------------------------------------
	// ----------------------------------------------------------------------
	// a) tácticas con jugadores portero, defense y delantero
	public static final int TACTICA40 = 2040;
	public static final int TACTICA31 = 2031;
	public static final int TACTICA22 = 2022;
	public static final int TACTICA13 = 2013;
	public static final int TACTICA04 = 2004;
	// b) marcajes al hombre y por zona
	// MARCAJECCxZy = x jugadores estarán marcando cuerpo a cuerpo a jugadores 
	// del otro equipo e y jugadores estarán marcando por zona. 
	// 
	// Por ejemplo, MARCAJECC1Z3 hace que el jugador 1 marque 
	// al jugador 4 cuerpo a cuerpo y que los demas marquen por zona
	// ¡Ojo! El portero no puede marcar a nadie
	public static final int MARCAJECC0Z4 = 3004;
	public static final int MARCAJECC1Z3 = 3013;
	public static final int MARCAJECC2Z2 = 3022;
	public static final int MARCAJECC3Z1 = 3031;
	public static final int MARCAJECC4Z0 = 3040;
	// ruta a los ficheros
	//public static String ruta_fichero_tacticas = "C:\\HLOCAL\\Prototipo_v_2\\tb\\Domains\\SoccerBots\\teams\\datosJSQTeam\\baseCasosTactica.txt";
	//public static String ruta_fichero_marcajes = "C:\\HLOCAL\\Prototipo_v_2\\tb\\Domains\\SoccerBots\\teams\\datosJSQTeam\\baseCasosMarcaje.txt";
	public static String ruta_fichero_tacticas = ".\\baseCasosTactica.txt";
	public static String ruta_fichero_marcajes = ".\\baseCasosMarcaje.txt";
	
	
	// ----------------------------------------------------------------------
	// ATRIBUTOS ------------------------------------------------------------
	// ----------------------------------------------------------------------
	// tabla de tácticas y éxitos
	private long[][] tablaTactica =  new long[9][16];
	// tabla de tipos de marcaje y éxitos
	private long[][] tablaMarcajes = new long[9][16];
	// tactica actual del equipo
	private int tacticaActual = TACTICA22;
	// marcajes actuales del equipo
	private int marcajeActual = MARCAJECC0Z4; 
	// capitan del equipo que le transmite las ordenes a sus compañeros
	private JSQTeam capitan = null;
	// información actual sobre los puestos de los rivales en el campo
	private int[] puestoRiv = {0,0,1,1};
	// informacion de las veces que ha ocupado una posicion un jugador rival
	// tiene dos filas
	//    1  2  3  4
	// 0 23 45 45 46 (veces como delantero)
	// 1 27  5  5  4 (veces como defensa)
	private double[][] vecesEnPosicion = {{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0}};
	// posicion jugadores que el entrenador ha estimado que tiene el rival con 
	// la información que tiene hasta ahora
	private int[] posicionRivales = {0,0,0,0};
	// iteraciones hasta ahora
	private double iteraciones = 0.0;
	// contiene el resultado de la anterior 
	private int[] resultadoAntes = {0,0};
	// contiene la posicion de los jugadores en el ultimo aprendizaje
	private int filaUltimoAprendizaje = 0;

	// ----------------------------------------------------------------------
	// CONSTRUCTOR ----------------------------------------------------------
	// ----------------------------------------------------------------------
	/** al crear el entrenador se lee toda su tabla de tácticas y éxitos
	 * y la tabla de marcajes y éxitos después se envía un mensaje 
	 * a todos los jugadores del equipo para que sepan con qué táctica 
	 * les ha mandado el entrenador jugar y qué tipo de marcajes 
	 * deben realizar
	 */
	public EntrenadorJSQTeam(JSQTeam objeto){
			// le indicamos al entrenador quien es el capitan del equipo
			// que se encargará de mandarle los mensajes al resto del equipo
			capitan = objeto;
			// leemos los datos 
			leeTabla(ruta_fichero_tacticas,1);
			leeTabla(ruta_fichero_marcajes,0);
			muestraTablasTacticasYMarcajes();
		}
		
	public void siguienteDecision(int[] puestoR){
		// ... toma decision de tactica y marcajes en este momento
		for (int i = 0; i< 4; i++){
			if (puestoR[i]==0)
				vecesEnPosicion[0][i]++;
			else
				vecesEnPosicion[1][i]++;
			}
		// calcula la colocación de los rivales en el campo
		for (int ii = 0; ii < 4; ii++){
			if (vecesEnPosicion[0][ii]>vecesEnPosicion[1][ii])
				posicionRivales[ii] = 0;
			else
				posicionRivales[ii] = 1;
			}
		// la eleccion de la mejor táctica se inicia con el peor valor posible
		long valor_elegido = Long.MIN_VALUE; 
		int columna_elegida = 4;
		for (int fila=0; fila< 16; fila++)
			for (int col = 4; col < 9; col++){
				// revisamos los resultados que se obtuvieron en el pasado con 
				// la tácticas disponibles y nos quedamos con la que ofrece 
				// mejores resultados
				if ((tablaTactica[0][fila]==posicionRivales[0])&&
						(tablaTactica[1][fila]==posicionRivales[1])&&
						(tablaTactica[2][fila]==posicionRivales[2])&&
						(tablaTactica[3][fila]==posicionRivales[3])&&
						(tablaTactica[col][fila]>valor_elegido)){
						// si los jugadores del rival están del mismo modo colocados
						// y los resultados ofrecidos son mejores que el encontrado hasta ahora
						// entonces ambiamos la tactica por esta
						columna_elegida = col;
						valor_elegido = tablaTactica[col][fila];
						filaUltimoAprendizaje = fila;
						}
				} // for
		// en columna_elegida estará un valor que indicará cual será la nueva táctica	
		switch (columna_elegida){
			case 4: tacticaActual = TACTICA40;
							break;
			case 5: tacticaActual = TACTICA31;
							break;
			case 6: tacticaActual = TACTICA22;
							break;
			case 7: tacticaActual = TACTICA13;
							break;
			case 8: tacticaActual = TACTICA04;
							break;
			}
		// elecion de la mejor forma de marcar
		// la eleccion de la mejor forma de marcar se inicia con el peor valor posible
		valor_elegido = Long.MIN_VALUE; 
		columna_elegida = 4;
		for (int fila=0; fila< 16; fila++)
			for (int col = 4; col < 9; col++){
				// revisamos los resultados que se obtuvieron en el pasado con 
				// la tácticas disponibles y nos quedamos con la que ofrece 
				// mejores resultados
				if ((tablaMarcajes[0][fila]==posicionRivales[0])&&
						(tablaMarcajes[1][fila]==posicionRivales[1])&&
						(tablaMarcajes[2][fila]==posicionRivales[2])&&
						(tablaMarcajes[3][fila]==posicionRivales[3])&&
						(tablaMarcajes[col][fila]>valor_elegido)){
						// si los jugadores del rival están del mismo modo colocados
						// y los resultados ofrecidos son mejores que el encontrado hasta ahora
						// entonces ambiamos el marcaje por esta
						columna_elegida = col;
						valor_elegido = tablaMarcajes[col][fila];
						}
				} // for
		// en columna_elegida estará un valor que indicará cual será el nuevo marcaje
		switch (columna_elegida){
			case 4: marcajeActual = MARCAJECC0Z4;
							break;
			case 5: marcajeActual = MARCAJECC1Z3;
							break;
			case 6: marcajeActual = MARCAJECC2Z2;
							break;
			case 7: marcajeActual = MARCAJECC3Z1;
							break;
			case 8: marcajeActual = MARCAJECC4Z0;
							break;
			}
		// manda la tactica y el marcaje
		mandarTacticaYMarcaje();
		iteraciones++;
		if (JSQTeam.MOSTRAR_MENSAJES){
			System.out.println("Iteracion: " + iteraciones);
			}
		}
		
	/**
	 * Lee los datos del fichero almacenado en ruta_tabla y los guarda en la
	 * tabla pasada como segundo parámetro.
	 * seleccionaTabla = 0 => tabla destino = tablaMarcajes
	 * seleccionaTabla = 1 => tabla destino = tablaTactica
	 * Este método solo sirve para las tablas de 9 filas por 16 columnas
	 */
	public void leeTabla(String ruta_tabla, int seleccionaTabla){
    // para poder leer los trozos de cada linea del fichero
    String tmp = new String("");
    String pref = new String("");
    String aux = new String("");
    int indice;
    // contador
    int i=0;
    
    long[][] tabla = new long[9][16];
   	
   	int numeroCol = 0; // para saber la columna en la que escribir(9 columnas)
   	
    int numeroLineas; // para saber la fila en la que esribir (16 filas)
    try{
      // Apertura del fichero de texto(modo de lectura)
      FileReader file = new FileReader(ruta_tabla);
      // ...con buffer para leer línea a línea
      BufferedReader d = new BufferedReader(file);
      //leemos el número de líneas en la primera línea
      numeroLineas = Integer.parseInt(d.readLine());
       
      while (i<numeroLineas){
      	tmp=d.readLine();
      	if (tmp.length()==0 || tmp.charAt(0)!='#'){
      		// procesar la línea i
      		for (numeroCol = 0;numeroCol < 8; numeroCol ++){
      			//lectura cada celda
    				indice = tmp.indexOf(' '); // posicion del primer espacio
    				pref = tmp.substring(0, indice);	// pref tiene pj1
    				if (JSQTeam.MOSTRAR_MENSAJES){
    					System.out.println("(" + numeroCol + "," + i + ")");
    				}
    				tabla[numeroCol][i] = (long) Integer.parseInt(pref);		// pl1 tiene el valor 
    				tmp = tmp.substring(indice+1,tmp.length());	// quita parte inutil
    				if (JSQTeam.MOSTRAR_MENSAJES){
    					System.out.println(tmp);
    				}
      		}
      		// leemos el ultimo valor
      		tabla[8][i] = (long) Integer.parseInt(tmp);		// pl1 tiene el valor     		
      	}
      	i++;
      }
    				
      if (seleccionaTabla == 0) {
      	for (int f= 0; f<16; f++)
      		for (int c = 0; c<9; c++)
      			tablaMarcajes[c][f] = tabla[c][f];
      			}
      else {
      	for (int f= 0; f<16; f++)
      		for (int c = 0; c<9; c++)
      			tablaTactica[c][f] = tabla[c][f];
      			}
      	
      d.close();
    }//fin try
    catch (FileNotFoundException e){
      //errores al abrir los ficheros
      System.out.println("El fichero "+ ruta_tabla +" fallo en su lectura.");
    }
    catch (IOException e){
      //errores en los métodos read o readLine
      System.out.println("El fichero "+ ruta_tabla +" fallo en las lecturas (read).");
    }		
		
	} // fin leeTabla
	
	
	/**
	 * muestra los datos de las tablas de tacticas y marcajes
	 * Este método solo sirve para las tablas de 9 filas por 16 columnas
	 */
	public void muestraTablasTacticasYMarcajes(){
		// tabla marcajes
		System.out.println("TABLA MARCAJES");
		for (int f = 0; f < 16; f++) {// por filas
			System.out.print("Fila " + f + ": "); 
			for (int c = 0; c < 9; c++) {// por columnas
					System.out.print(tablaMarcajes[c][f] + " - ");
					}
			System.out.println("");
		}
		// tabla tacticas
		System.out.println("TABLA TACTICAS");
		for (int f = 0; f < 16; f++) {// por filas
			System.out.print("Fila " + f + ": "); 
			for (int c = 0; c < 9; c++) {// por columnas
					System.out.print(tablaTactica[c][f] + " - ");
					}
			System.out.println("");
		}
	} // fin muestraTablasTacticasYMarcajes
		
	/**
	 * envia la tactica y el marcaje a cada jugador del equipo
	 */
	public void mandarTacticaYMarcaje(){
		// mandamos la tactica y el marcaje al capitan del equipo para que 
		// se lo transmita a sus compañeros
		capitan.ordenesDelEntrenador(tacticaActual, marcajeActual);
		}
	
	/**
	 * este método intenta aprender cada vez que ocurre un gol
	 * si golMetido > 0 gol metido por nosotros
	 *	valor en tabla mas 2
	 * si golMeido < 0 gol que nos han metido
	 *	valor en tabla menos 2 
	 * Y después actualiza la tabla en los ficheros
	 */
	public void intentaAprender(int golMetido){
			int columna=4, columnaT=4;
			switch (marcajeActual){
				case MARCAJECC0Z4: columna = 4;
												break;
				case MARCAJECC1Z3: columna = 5;
												break;
				case MARCAJECC2Z2: columna = 6;
												break;
				case MARCAJECC3Z1: columna = 7;
												break;
				case MARCAJECC4Z0: columna = 8;
												break;
				} // switch 
			switch (tacticaActual){
				case TACTICA40: columnaT = 4;
												break;
				case TACTICA31: columnaT = 5;
												break;
				case TACTICA22: columnaT = 6;
												break;
				case TACTICA13: columnaT = 7;
												break;
				case TACTICA04: columnaT = 8;
												break;
				} // switch 

			if (golMetido > 0) {
				// gol metido por nosotros
				tablaTactica[columnaT][filaUltimoAprendizaje] += 2;
				tablaMarcajes[columnaT][filaUltimoAprendizaje] += 2;
				} // fin if metido nosotros el gol
			else {
				// gol metido por el contrario
				tablaTactica[columnaT][filaUltimoAprendizaje] -= 2;
				tablaMarcajes[columnaT][filaUltimoAprendizaje] -= 2;
				} // fin else metido el rival el gol
			System.out.println("El sistema está aprendiendo...");
			System.out.println("Las correciones realizadas son las siguientes:");
			muestraTablasTacticasYMarcajes();
			// actualizar tablas en memoria
			
			guardaTabla(ruta_fichero_tacticas,1);
			guardaTabla(ruta_fichero_marcajes,0);		
		}

	/**
	 * guarda los datos de los atributos tablaMarcajes o tablaTacticas
	 * en el archivo que contiene los valores almacenados de los exitos y fracasos
	 * de cada CASO
	 * seleccionaTabla = 0 => tabla destino = tablaMarcajes
	 * seleccionaTabla = 1 => tabla destino = tablaTactica
	 * Este método solo sirve para las tablas de 9 filas por 16 columnas
	 */	
	public void guardaTabla(String ruta_tabla, int seleccionaTabla){
		int posicionfila = 0;
		try { 
			// Apertura del fichero de texto(modo de escritura)
			FileWriter file = new FileWriter(ruta_tabla);
      // ...con buffer para leer línea a línea
      BufferedWriter d = new BufferedWriter(file);			
			d.write("16");
			d.newLine();
			for (int i = 0; i < 2; i++)
				for (int ii = 0; ii < 2; ii++)
					for (int iii = 0; iii < 2; iii++)
						for (int iiii = 0; iiii < 2; iiii++){
							posicionfila = iiii + iii*2 + ii * 4 + i * 8;
							d.write("" + i + " " + ii + " " + iii + " " + iiii + " ");
							if (seleccionaTabla == 0){
								// tabla marcajes
								d.write(tablaMarcajes[4][posicionfila] + " ");
								d.write(tablaMarcajes[5][posicionfila] + " ");
								d.write(tablaMarcajes[6][posicionfila] + " ");
								d.write(tablaMarcajes[7][posicionfila] + " ");
								d.write("" + tablaMarcajes[8][posicionfila]);
								d.newLine();
							}
							else{
								// tabla tacticas
								d.write(tablaTactica[4][posicionfila] + " ");
								d.write(tablaTactica[5][posicionfila] + " ");
								d.write(tablaTactica[6][posicionfila] + " ");
								d.write(tablaTactica[7][posicionfila] + " ");
								d.write("" + tablaTactica[8][posicionfila]);
								d.newLine();
								}
							} // for
			d.write("# todas las filas que empiezan por # son comentarios");
			d.newLine();
			d.write("# códigos jugador");
			d.newLine();
			d.write("# 0 = delantero");
			d.newLine();
			d.write("# 1 = defensa");
			d.newLine();
			d.write("# formato:");
			d.newLine();
			d.write("# ");
			d.newLine();
			d.write("# <numero de lineas>");
			d.newLine();
			d.write("# <codjugador1> <codjugador2> <codjugador3> <codjugador4> <victorias_marcaje_MARCAJECC0Z4> ... sigue");
			d.newLine();
			d.write("# ... <victorias_marcaje_MARCAJECC1Z3> <victorias_marcaje_MARCAJECC2Z2> <victorias_marcaje_MARCAJECC3Z1> ... sigue");
			d.newLine();
			d.write("# ... <victorias_marcaje_MARCAJECC4Z0>");
			d.newLine();
			d.write("# <codjugador1> <codjugador2> <codjugador3> <codjugador4> <victorias_marcaje_MARCAJECC0Z4> ... sigue");
			d.newLine();
			d.write("# ... <victorias_marcaje_MARCAJECC1Z3> <victorias_marcaje_MARCAJECC2Z2> <victorias_marcaje_MARCAJECC3Z1> ... sigue");
			d.newLine();
			d.write("# ... <victorias_marcaje_MARCAJECC4Z0>");
			d.newLine();
			
		d.close();
    }//fin try
    catch (FileNotFoundException e){
      //errores al abrir los ficheros
      System.out.println("El fichero "+ ruta_tabla +" fallo en su lectura.");
    }
    catch (IOException e){
      //errores en los métodos read o readLine
      System.out.println("El fichero "+ ruta_tabla +" fallo en las lecturas (read).");
    }	
	}
} // fin EntrenadorJSQTeam