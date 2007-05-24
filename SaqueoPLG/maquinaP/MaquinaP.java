/**
 * 
 */
package maquinaP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JOptionPane;

/**
 * La clase <B>MaquinaP</B> implementa la maquina virtual. Para que el lenguaje objeto que hemos creado tenga valor 
 * es necesario que sea ejecutado en una maquina y que esta traduzca los elementos del lenguaje fuente al lenguaje 
 * objeto.
 * <P>La clase MaquinaP cuenta con los siguientes atributos:
 * <UL><LI><CODE>pila:</CODE> La pila de los operandos de la maquina.</LI>
 * <LI><CODE>PC:</CODE> Contador de programa. Al final de la ejecucion nos dice cuantas lineas tiene dicho programa.</LI>
 * <LI><CODE>H:</CODE> Indica si la maquina esta en ejecucion, parada por error, o acabo su ejecucion.</LI>
 * <LI><CODE>ST:</CODE> Puntero a la cima de la pila.</LI>
 * <LI><CODE>Prog:</CODE>Memoria de programas. Aqui habia puesto el nombre del fichero pero quizas deberia ser el
 * c?digo del programa.</LI>
 * <LI><CODE>Mem:</CODE> Memoria de datos estatica.</LI>
 * <LI><CODE>heap:</CODE> Memoria de datos dinamica.</LI>
 * <LI><CODE>fichero:</CODE> Fichero donde se encuetra el codigo que va a ejecutar la MaquinaP. Sera un fichero con extension '.obj'</LI>
 * <LI><CODE>pasos:</CODE> String con todos los pasos que ejecuta la MaquinaP.</LI>
 * </UL></P>
 * 
 * @author Jonas Andradas, Paloma de la Fuente, Leticia Garcia y Silvia Martin
 * @see java.io.FileReader#FileReader(java.lang.String)
 */

public class MaquinaP {

	/*
	 * Atributos de la clase:
	 * 
	 * pila: La pila de los operandos de la m?quina.
	 * PC: Contador de programa. Al final de la ejecuci?n nos dice cuantas lienas tiene dicho programa.
	 * H: Indica si la m?quina esta en ejecuci?n, parada por error, o acabo su ejecuci?n.
	 * ST: Puntero a la cima de la pila.
	 * Prog:Memoria de programas. Aqui hab?a puesto el nombre del fichero pero quizas deberia ser el
	 * codigo del programa.
	 * Mem: Memoria de datos estatica.
	 * heap:Memoria de datos din?mica.
	 * fichero: Fichero donde se encuetra el codigo que va a ejecutar la MaquinaP.
	 * pasos: String con todos los pasos que ejecuta la MaquinaP.
	 */
	private Stack pila;
	private int PC;
	private int H;
	private int ST;
	private Vector Prog;
	private Vector Mem;
	private Heap heap;
	private FileReader fichero;
	private String pasos;
	private int tamMem;

	/**
	 * El constructor de la clase MaquinaP que solo tiene el buffer de lectura del fichero como parametro de entrada.
	 * @param f Recibe como parametro la ruta del fichero a ejecutar para poder inicializar todo.
	 *
	 */	
	public MaquinaP(String f) {
		super();
		pila = new Stack();
		PC = 0;
		H = 0;
		tamMem= Integer.MAX_VALUE;
		ST = -1;
		Mem= new Vector();
		heap= new Heap(50);
		int i= f.length();
		String fcod = new String(f.substring( 0,i-3));
		fcod = fcod.concat("obj");
		File fich= new File(fcod);
		try{
			fichero = new FileReader(fich);
		}
		catch(java.io.FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,"Archivo no encontrado: " + fcod,"Error",JOptionPane.ERROR_MESSAGE);
		}
		Prog = damePrograma(fichero);
		pasos="";
	}
	
	/**
	 * Accesor para el atributo de la clase, pasos. 
	 * @return String con los pasos ejecutados y estados de la pila.
	 */
	public String getPasos() {
		return pasos;
	}

	/**
	 * Accesor para el atributo de la clase, H. Que indica el estado de la pila. 
	 * @return String con los pasos ejecutados y estados de la pila.
	 */
	public int getH() {
		return H;
	}
	
	/**
	 * Mutador para el atributo de la clase, H. 
	 * @param h Entero que controla el estado actual de la pila, ademas es donde se refleja el error.
	 */
	public void setH(int h) {
		H = h;
	}
	
	/**
	 * Accesor para el atributo de la clase, Mem. Que indica el estado de la memoria del Programa. 
	 * @return Vector donde cada celda es una posicion de Memoria.
	 */
	public Vector getMem() {
		return Mem;
	}
	
	/**
	 * Mutador para el atributo de la clase, Mem. Que indica el estado de la memoria del Programa. 
	 * @param mem Se recibe un vector a modo de memoria de Progrma.
	 */
	public void setMem(Vector mem) {
		Mem = mem;
	}
	
	/**
	 * Accesor para el atributo de la clase, PC. Que indica el contador de instrucciones del Programa. 
	 * @return Entero que indica el numero de instruccion.
	 */
	public int getPC() {
		return PC;
	}
	
	/**
	 * Mutador para el atributo de la clase, PC. Que indica el contador de instrucciones del Programa. 
	 * @param pc Entero para actualizar la nueva poscion del programa.
	 */
	public void setPC(int pc) {
		PC = pc;
	}
	
	/**
	 * Accesor para el atributo de la clase, pila. Pila del programa. 
	 * @return Devuelve la pila del programa.
	 */
	public Stack getPila() {
		return pila;
	}
	
	/**
	 * Mutador para el atributo de la clase, pila. Se actualizara la pila del programa.
	 * @param pila una nueva pila con valores y operaciones.
	 */
	public void setPila(Stack pila) {
		this.pila = pila;
	}
	
	/**
	 * Accesor para el atributo de la clase, Prog. Vector con las intrucciones del programa que se ha de ejecutar.
	 * @return Vector con el contenido del programa.
	 */
	public Vector getProg() {
		return Prog;
	}
	
	/**
	 * Mutador para el atributo de la clase, Prog. Se actualizaran el vector con las instrucciones del Programa.
	 * @param prog Se recibe el vector con el nuevo programa.
	 */
	public void setProg(Vector prog) {
		Prog = prog;
	}
	
	/**
	 * Accesor para el atributo la clase, ST. Se debuelve el valor que apunta a la cima de la pila.
	 * @return Entero que apunta a la cima de la pila de la maquina.
	 */
	public int getST() {
		return ST;
	}
	
	/**
	 * Mutador para el atributo de la clase ST. Se cambia el puntero a la cima de la pila, con lo que cambiara la cima de la pila.
	 * @param st Recibe por parametro el entero con el que cambiar la cima de la pila.
	 */
	public void setST(int st) {
		ST = st;
	}
	
	/**
	 * @return Returns the fichero.
	 */
	public FileReader getFichero() {
		return fichero;
	}

	/**
	 * @param fichero The fichero to set.
	 */
	public void setFichero(FileReader fichero) {
		this.fichero = fichero;
	}

	/**
	 * @return Returns the heap.
	 */
	public Heap getHeap() {
		return heap;
	}

	/**
	 * @param heap The heap to set.
	 */
	public void setHeap(Heap heap) {
		this.heap = heap;
	}

	/**
	 * Obtiene el programa del fichero que recibe por parametro. Guarda cada instruccion del programa en una posicion del 
	 * vector Prog. Para obtener el programa crea un BufferReader y se lanzan y capturan excepciones al respecto. 
	 * 
	 * @param f Recibe por parametro el fichero del cual obtiene el programa. Ha de ser un FileReader para luego trabajar con el.
	 * @return Devuelve el Vector con el programa. Cada posicion es una instruccion de la maquina P.
	 * @exception java.io.FileNotFoundException Se lanza y se captura en este mismo metodo.
	 * @exception java.io.IOException Se lanza y se captura en este mismo metodo.
	 */
	private Vector damePrograma(FileReader f){
		Vector v=new Vector();
		BufferedReader entrada = null;
	    try {
	      entrada = new BufferedReader(f);
	      String linea = null;
	      while ((linea = entrada.readLine()) != null){
	    	  v.add(linea.trim());
	      }
	    }
	    catch (FileNotFoundException ex) {
		      JOptionPane.showMessageDialog(null,"Archivo no encontrado: " + ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException ex){
			JOptionPane.showMessageDialog(null,"Archivo no encontrado: " + ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		return v;
	}
	
	/**
	 * Aumenta el tama?o del vector memoria segun las necesidades del programa que va a ejecutar.
	 * 
	 * @param tam Recibe un entero con el tamaño que ha de aumentar.
	 */
	private void aumentoMem(int tam){
		for(int i = Mem.size();i<tam+1;i++){
			Mem.add(i,null);
		}
	}
	
	/**
	 * Metodo que ejecuta la Maquina P. Va leyendo las intrucciones que ha generado el compilador y las ejecuta.
	 *
	 */
	public void ejecuta() throws Exception{
		String i;
		String[] linea;
		int j= 0;
		pasos=pasos.concat("Comenzamos con la ejecucion de la pila. \n\n");
		System.out.println("\n\n\nComenzamos con la ejecucion de la pila. \n\n\n");
		System.out.println(Prog.size());
		while (H==0){
			if(PC<Prog.size()){
				i= (String)Prog.get(PC);
				linea = i.split(" ");
				if (linea[0].compareTo("apila")==0){
					System.out.println(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat(" \n");
					apila((new Integer(Integer.parseInt(linea[1]))).intValue());
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("desapila-dir")==0){
					System.out.println(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat(" \n");
					desapila_dir((new Integer(Integer.parseInt(linea[1]))).intValue());
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("apila-dir")==0){
					System.out.println(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat(" \n");
					apila_dir((new Integer(Integer.parseInt(linea[1]))).intValue());
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("suma")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					suma();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("resta")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					resta();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("multiplica")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					multiplica();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("divide")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					divide();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("and")==0){
					System.out.println(linea[0]+ "  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					and();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("or")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					or();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("not")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					not();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("neg")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					neg();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("menor")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					menor();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("menor_o_igual")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					menorIgual();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("mayor")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					mayor();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("mayor_o_igual")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					mayorIgual();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("igual")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					igual();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("distinto")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					distinto();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("ir-a")==0){
					System.out.println(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat(" \n");
					ir_a((new Integer(Integer.parseInt(linea[1]))).intValue());
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("ir-f")==0){
					System.out.println(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat(" \n");
					ir_f((new Integer(Integer.parseInt(linea[1]))).intValue());
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("delete")==0){
					System.out.println(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat(" \n");
					delete((new Integer(Integer.parseInt(linea[1]))).intValue());
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("new")==0){
					System.out.println(linea[0]+"  "+Integer.parseInt(linea[1])+"  "+Integer.parseInt(linea[2]));
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  "+Integer.parseInt(linea[1])+"  "+Integer.parseInt(linea[2]));
					pasos= pasos.concat(" \n");
					new_o((new Integer(Integer.parseInt(linea[1]))).intValue(),(new Integer(Integer.parseInt(linea[2]))).intValue());
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("apila-ind")==0){
					System.out.println(linea[0]);
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]);
					pasos= pasos.concat(" \n");
					apila_ind();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("desapila-ind")==0){
					System.out.println(linea[0]);
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]);
					pasos= pasos.concat(" \n");
					desapila_ind();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("mueve")==0){
					System.out.println(linea[0]);
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  "+Integer.parseInt(linea[1]));
					pasos= pasos.concat(" \n");
					mueve((new Integer(Integer.parseInt(linea[1]))).intValue());
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("copia")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					copia();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("ir-ind")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					ir_ind();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else if (linea[0].compareTo("flip")==0){
					System.out.println(linea[0]+"  ");
					pasos= pasos.concat("El numero de instruccion es: ("+PC+") - ");
					pasos= pasos.concat(linea[0]+"  ");
					pasos= pasos.concat(" \n");
					flip();
					if (!pila.empty()){
						pasos= pasos.concat("La cima de la pila cambio, ahora es: "+ pila.peek());
						pasos= pasos.concat(" \n");
					}
					else{
						pasos= pasos.concat("La pila ahora esta vacia");
						pasos= pasos.concat(" \n");
					}
					j++;
				}
				else{
					error();
					j++;
				}
			}
			else{
				eof();
			}
		}
	}
	
	/**
	 * Metodo que devuelve un String con el contenido de la Memoria. 
	 * Se usa para ver el contenido final de la memoria despues de ejecutar la maquina P.
	 * 
	 * @return String con el contenido del vector Memoria.
	 */
	public String resultadoMem(){
		String s="Memoria estatica:"+"\n";
		for (int i=0;i<Mem.size();i++){
			System.out.println(Mem.elementAt(i));
			if(Mem.elementAt(i)!=null){
				s= s.concat("posicion "+i+":  "+((Integer)Mem.elementAt(i)).toString());
				s=s.concat(" \n");
			}
			else{
				s=s.concat("posicion "+i+": "+" null");
				s=s.concat(" \n");
			}
		}
		s = s.concat("Memoria dinamica:"+"\n");
		for (int i=0;i<heap.getHeap().size();i++){
			System.out.println(heap.getHeap().elementAt(i));
			if(heap.getHeap().elementAt(i)!=null){
				s= s.concat("posicion "+i+":  "+((Integer)heap.getHeap().elementAt(i)).toString());
				s=s.concat(" \n");
			}
			else{
				s=s.concat("posicion "+i+": "+" null");
				s=s.concat(" \n");
			}
		}
		return s;
	}
	
	/**
	 * Metodo que realiza una operacion de suma. Se desapilan los dos primeros elementos de la pila y se suman.  Despues se apila 
	 * en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se aumenta en uno 
	 * el contador del programa.
	 * 
	 * (R1) suma:
	 * Pila[ST-1] <-- Pila[ST-1] + Pila[ST]
	 * ST <-- ST -1 
	 * PC <-- PC + 1
	 */

	public void suma() throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Suma. La pila no contiene los datos necesarios.");
		}
		Integer s1 = (Integer)pila.pop();
		Integer s2 = (Integer)pila.pop();
		Integer s = new Integer(s2.intValue()+s1.intValue());
		pila.push(s);
		ST = ST-1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de resta. Se desapilan los dos primeros elementos de la pila y se restan.  Despues se apila 
	 * en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se aumenta en uno 
	 * el contador del programa.
	 * 
	 * (R2) resta:
	 *	Pila[ST-1] <-- Pila[ST-1] - Pila[ST]
	 *	ST <-- ST -1 
	 *	PC <-- PC + 1
	 */
	public void resta()throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Resta. La pila no contiene los datos necesarios.");
		}
		Integer s1 = (Integer)pila.pop();
		Integer s2 = (Integer)pila.pop();
		Integer s = new Integer(s2.intValue()-s1.intValue());
		pila.push(s);
		ST = ST-1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de multiplicacion. Se desapilan los dos primeros elementos de la pila y se multiplican.  Despues se apila 
	 * en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se aumenta en uno 
	 * el contador del programa.
	 * 
	 * (R3) multiplica:
	 *	Pila[ST-1] <-- Pila[ST-1] * Pila[ST]  
	 *	ST <-- ST -1 
	 *	PC <-- PC + 1
	 */
	public void multiplica()throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Multiplica. La pila no contiene los datos necesarios.");
		}
		Integer s1 = (Integer)pila.pop();
		Integer s2 = (Integer)pila.pop();
		Integer s = new Integer(s2.intValue()*s1.intValue());
		pila.push(s);
		ST = ST-1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de division. Se desapilan los dos primeros elementos de la pila y se dividen.  Despues se apila 
	 * en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se aumenta en uno 
	 * el contador del programa.
	 * 
	 * (R4) divide:
	 *	Pila[ST-1] <-- Pila[ST-1] / Pila[ST]  
	 *	ST <-- ST -1 
	 *	PC <-- PC + 1
	 */
	public void divide()throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Divide. La pila no contiene los datos necesarios.");
		}
		Integer s1 = (Integer)pila.pop();
		Integer s2 = (Integer)pila.pop();
		Integer s = new Integer(s2.intValue()/s1.intValue());
		pila.push(s);
		ST = ST-1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que apila un entero en la pila. Se aumenta en uno el tamao de la pila y se apila el entero que recibe como
	 * parametro. Tambien se aumenta en uno el contador del programa.
	 * 
	 * ST <-- ST + 1 
	 * Pila[ST] <-- n
	 * PC <-- PC + 1
	 * 
	 * @param n
	 */
	public void apila (int n){
		ST=ST+1;
		pila.push(new Integer (n));
		PC=PC+1;
	}
	
	/**
	 * Metodo que apila en la cima de la pila el valor que contiene la direccion de memoria que recibe como parametro. Se comprueba antes 
	 * si la direccion de memoria pertenece a memoria estatica o memoria dinamica. Tambien se aumenta en uno el contador del programa.
	 * 
	 * (R6) apila-dir(d):
	 *	ST <-- ST + 1 
	 *	Pila[ST] <-- Mem[d]  
	 *	PC <-- PC + 1
	 *
	 * @param d
	 */
	public void apila_dir (int d) throws Exception{
		ST = ST + 1; 
		if(d<tamMem){
			if (d >= 0){
				if ((Mem.size()>=d)&&(Mem.elementAt(d)!=null)){ 
					pila.push(Mem.elementAt(d));  
					PC = PC + 1;
				}
				else{				
					throw new Exception("ERROR: Variable sin inicializar.");
				}
			}
			else{
				throw new Exception("ERROR: Puntero sin inicializar.");
			}
		}
		else{ //memo dinamica
			d=d-tamMem;
			if (d<heap.getHeap().size()){
				pila.push(new Integer(heap.getElementAt(d)));
				PC = PC + 1;
			}
			else{
				throw new Exception("ERROR: Memoria sin inicializar.");
			}
		}
	}
	
	/**
	 * Metodo que desapila la cima de la pila y lo guarda en la direccion de memoria que recibe como parametro. Se disminuye en uno el tamao 
	 * de la pila y se comprueba si es memoria dinamica o estatica. Tambien se aumenta en uno el contador del programa.
	 * 
	 * (R7) desapila-dir(d):
	 *	Mem[d] <-- Pila[ST]
	 *	ST <-- ST -1 
	 *	PC <-- PC + 1
	 *
	 * @param d
	 */
	public void desapila_dir(int d) throws Exception{
		//Primero comprobamos que la memoria sea suficiente.
		//Sino lo es aumentamos el tama?o del vector.
		if (ST<0){
			throw new Exception("ERROR: Desapila_dir. La pila no contiene los datos necesarios.");
		}
		if (d<tamMem){
			if (d >= 0){
				if (d>=Mem.size()){
					aumentoMem(d);
					Mem.set(d,pila.pop());
				}
				else{
					Mem.set(d,pila.pop());
				}
			}
			else{
				throw new Exception("ERROR: Puntero sin inicializar.");
			}
			
		}
		else{ //memo dinamica
			d=d-tamMem;
			if (d<heap.getHeap().size()){
				heap.setElementAt(d,((Integer)pila.pop()));
			}
			else{
				throw new Exception("ERROR: Puntero sin inicializar.");
			}
		}
		ST = ST -1;
		PC = PC + 1;
	}

	/**
	 * Metodo que para la ejecucion de la mquina P cuando se recibe un final de fichero.
	 * 
	 * (R8) EOF:
	 *	H <-- 1
	 */
	public void eof(){
		H=1;
	}
	
	/**
	 * Metodo que indica a la pila que pare la ejecucion con un error.
	 * 
	 * (R9) En cualquier otro caso, la m?quina entra en estado de error y se detiene la ejecuci?n.
	 * H <-- -1
	 */
	public void error(){
		H = -1;
	}
	
	/**
	 * Metodo que realiza una operacion and. Se desapilan los dos primeros elementos de la pila y se realiza una and.  Despues se apila 
	 * en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se aumenta en uno 
	 * el contador del programa.
	 * 
	 * (R13) And:
	 *	Pila[ST - 1] <-- "true" si Pila[ST ? 1] ? "false" & Pila[ST] ? "false"
	 *		"false" en cualquier otro caso
	 *	ST <-- ST ? 1
	 *	PC <-- PC + 1 
	 */
	public void and() throws Exception{
		if (ST<1){
			throw new Exception("ERROR: And. La pila no contiene los datos necesarios.");
		}
		int a1 = ((Integer)pila.pop()).intValue();
		int a2 =((Integer)pila.pop()).intValue();
		if ((a1!=0)&&(a2!=0)){
			pila.push(new Integer(1));
		}
		else{
			pila.push(new Integer(0));
		}
		ST = ST -1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion or. Se desapilan los dos primeros elementos de la pila y se realiza la operacion.  Despues se apila 
	 * en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se aumenta en uno 
	 * el contador del programa.
	 * 
	 * (R14) Or:
	 *	Pila[ST - 1] <-- "false" si Pila[ST ? 1] ? "false" & Pila[ST] ? "false"
	 *		"true" en c.o.c.
	 *	ST <-- ST ? 1
	 *	PC <-- PC + 1 
	 */
	public void or()throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Or. La pila no contiene los datos necesarios.");
		}
		int o1= ((Integer)pila.pop()).intValue();
		int o2=((Integer)pila.pop()).intValue();
		if ((o1==0)&&(o2==0)){
			pila.push(new Integer(0));
		}
		else{
			pila.push(new Integer(1));
		}
		ST = ST -1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de negacion. Se desapila la cima de la pila y se realiza una negacion.  Despues se apila 
	 * en la cima el resultado. Tambien se aumenta en uno el contador del programa.
	 * 
	 * (R15) Not:
	 *	Pila[ST] <-- "true" si Pila[ST] = "false"
	 *				"false" en c.o.c
	 *	PC <-- PC + 1 
	 */
	public void not()throws Exception{
		if (ST<0){
			throw new Exception("ERROR: Not. La pila no contiene los datos necesarios.");
		}
		int n=((Integer)pila.pop()).intValue();
		if (n==0){
			pila.push(new Integer(1));
		}
		else{
			pila.push(new Integer(0));
		}
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion negacion de un entero. Se desapilan los dos primeros elementos de la pila y se realiza una and.  
	 * Despues se apila en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se aumenta 
	 * en uno el contador del programa.
	 * 
	 * (R16) Neg:
	 *	Pila[ST] <--  - Pila[ST]
	 *	PC <-- PC + 1 
	 */
	public void neg()throws Exception{
		if (ST<0){
			throw new Exception("ERROR: Neg. La pila no contiene los datos necesarios.");
		}
		Integer n= (Integer)pila.pop();
		pila.push(new Integer(-(n.intValue())));
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de menor con dos operandos. Se desapilan los dos primeros elementos de la pila y se realiza la operacion.
	 * Despues se apila en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se aumenta en uno 
	 * el contador del programa.
	 * 
	 * (R17) Menor:
	 *	Pila[ST ? 1] <-- "true" si Pila[ST - 1] < Pila[ST]
	 *					"false" en c.o.c
	 *	ST <-- ST ? 1
	 *	PC <-- PC + 1
	 */
	public void menor()throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Menor. La pila no contiene los datos necesarios.");
		}
		int c1= ((Integer)pila.pop()).intValue();
		int c2 = ((Integer)pila.pop()).intValue();
		if (c2<c1){
			pila.push(new Integer(1));
		}
		else{
			pila.push(new Integer(0));
		}
		ST = ST -1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de menor o igual con dos operandos. Se desapilan los dos primeros elementos de la pila y se realiza la 
	 * operacion.  Despues se apila en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se 
	 * aumenta en uno el contador del programa.
	 * 
	 * (R18) MenorIgual:
	 *	Pila[ST ? 1] <-- "true" si Pila[ST ? 1] ? Pila[ST]
	 *			"false" en c.o.c
	 *	ST <-- ST ? 1
	 *	PC <-- PC + 1
	 */
	public void menorIgual()throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Menor o igual. Memoria sin inicializar.");
		}
		int c1= ((Integer)pila.pop()).intValue();
		int c2=((Integer)pila.pop()).intValue(); 
		if (c2<=c1){
			pila.push(new Integer(1));
		}
		else{
			pila.push(new Integer(0));
		}
		ST = ST -1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de myor con dos operandos. Se desapilan los dos primeros elementos de la pila y se realiza la 
	 * operacion.  Despues se apila en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se 
	 * aumenta en uno el contador del programa.
	 * 
	 * (R19) Mayor:
	 *	Pila[ST ? 1] <-- "true" si Pila[ST ? 1] > Pila[ST]
	 *			"false" en c.o.c
	 *	ST <-- ST ? 1
	 *	PC <-- PC + 1
	 */
	public void mayor()throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Mayor. La pila no contiene los datos necesarios.");
		}
		int c1= ((Integer)pila.pop()).intValue();
		int c2 = ((Integer)pila.pop()).intValue(); 
		if (c2>c1){
			pila.push(new Integer(1));
		}
		else{
			pila.push(new Integer(0));
		}
		ST = ST -1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de mayor o igual con dos operandos. Se desapilan los dos primeros elementos de la pila y se realiza la 
	 * operacion.  Despues se apila en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se 
	 * aumenta en uno el contador del programa.
	 * 
	 * (R20) MayorIgual:
	 *	Pila[ST ? 1] <-- "true" si Pila[ST ? 1] ? Pila[ST]
	 *			"false" en c.o.c
	 *	ST <-- ST ? 1
	 *	PC <-- PC + 1
	 */
	public void mayorIgual()throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Mayor o igual. La pila no contiene los datos necesarios.");
		}
		int c1= ((Integer)pila.pop()).intValue();
		int c2=((Integer)pila.pop()).intValue(); 
		if (c2>=c1){
			pila.push(new Integer(1));
		}
		else{
			pila.push(new Integer(0));
		}
		ST = ST -1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de igual con dos operandos. Se desapilan los dos primeros elementos de la pila y se realiza la 
	 * operacion.  Despues se apila en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se 
	 * aumenta en uno el contador del programa.
	 * 
	 * (R21) Igual:
	 *	Pila[ST ? 1] <-- "true" si Pila[ST ? 1] = Pila[ST]
	 *			"false" en c.o.c
	 *	ST <-- ST ? 1
	 *	PC <-- PC + 1
	 */
	public void igual()throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Igual. La pila no contiene los datos necesarios.");
		}
		int c1= ((Integer)pila.pop()).intValue();
		int c2= ((Integer)pila.pop()).intValue();
		if (c1==c2){
			pila.push(new Integer(1));
		}
		else{
			pila.push(new Integer(0));
		}
		ST = ST -1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de distinto con dos operandos. Se desapilan los dos primeros elementos de la pila y se realiza la 
	 * operacion.  Despues se apila en la cima el resultado, disminuye en 1 el puntero a la cima ya que habra un elemento menos. Tambien se 
	 * aumenta en uno el contador del programa.
	 * 
	 * (R22) Distinto:
	 *	Pila[ST ? 1] <-- "true" si Pila[ST ? 1] ? Pila[ST]
	 *			"false" en c.o.c
	 *	ST <-- ST ? 1
	 *	PC <-- PC + 1
	 */
	public void distinto()throws Exception{
		if (ST<1){
			throw new Exception("ERROR: Distinto. La pila no contiene los datos necesarios.");
		}
		int c1= ((Integer)pila.pop()).intValue();
		int c2 =((Integer)pila.pop()).intValue();
		if (c1!=c2){
			pila.push(new Integer(1));
		}
		else{
			pila.push(new Integer(0));
		}
		ST = ST -1;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que realiza una operacion de salto del programa. Se comprueba que el salto no supere el tamao del programa y se aumenta el contador
	 * del programa segun el valor que recibimos por parametro.
	 * 
	 * (R20) ir-a(s):
	 * PC <-- s
	 * 
	 * @param s Numero de instrucciones que ha de saltar y no ejecutar la maquinaP
	 */
	public void ir_a(int s){
		if (s<Prog.size()){
			PC = s;
		}
		else{
			PC =s;
			eof(); 
		}
	}
	
	/**
	 * Metodo que realiza una operacion de salto condicional del programa. Se comprueba que el salto no supere el tamao del programa y se aumenta 
	 * el contador del programa segun el valor que recibimos por parametro, si la cima de la pila es un valor booleano cierto.
	 * 
	 * (R21) ir-f(s):
	 *	si Pila[ST] = 0 PC <-- s
	 *	sino PC <--PC+1 fsi
	 *	ST <-- ST - 1	
	 *
	 * @param s Numero de instrucciones que ha de saltar y no ejecutar la maquinaP
	 */
	public void ir_f(int s)throws Exception{
		if (ST<0){
			throw new Exception("ERROR: Salto sin definir.");
		}
		if (((Integer)pila.pop()).intValue()==0){
			System.out.println("Paso bien");
			PC = s;
		}
		else{
			PC = PC + 1;
		}
		ST = ST -1;
	}
	
	/**
	 * Metodo que desapila una direccion de comienzo (d) de la cima de la pila, y libera de la memoria dinamica t celdas consecutivas a partir de d.
	 * Tambien se aumenta en uno el contador del programa.
	 * 
	 * (R22) delete (d)
	 *  d<-- Pila[ST]
	 *  Mem[d] <-- null
	 *  PC <-- PC +1
	 *  ST <-- ST - 1
	 *  
	 *  @param t Tamano de celdas que hay que liberar
	 */
	public void delete (int t) throws Exception{
		if (ST<0){
			throw new Exception("ERROR: Memoria sin inicializar.");
		}
		int d= ((Integer)pila.pop()).intValue();
		d -= tamMem;
		ST= ST-1;
		heap.libera(d,t);
		PC++;
	}
	
	/**
	 * Metodo que reserva un numero (i) de celdas en la memoria segun recibe por parametro. Una vez reservadas las celdas en la memoria dinamica
	 * se apila en la pila la direccion mas el tamano de la memoria estatica en la cima de la pila. Tambien se aumenta en uno el contador del 
	 * programa. 
	 *  
	 * (R23) new (i, dir)
	 *  d <-- heap.reserva(i)
	 *  ST <-- ST + 1
	 *  Pila[ST] <-- d + tamDir
	 *  PC <-- PC +1
	 *  
	 * @throws Exception
	 * @param i Entero que indica el numero de celdas que hay que reservar.
	 * @param dir Valor que hay que sumar a la direccion donde se reservo espacio, para poder indicar despues que es memoria dinamica.
	 */
	public void new_o(int i, int dir) throws Exception{
		int j=heap.reserva(i);
		ST=ST+1;
		pila.push(new Integer (j+dir));
		if (tamMem>dir){
			tamMem = dir;
		}
		PC++;
	}
	
	/**
	 * Metodo que desapila la cima de la pila y lo trata como una direccion de la memoria, y substituye dicho valor por el almacenado en dicha celda.
	 * Se comprueba primero si la direccion de memoria obtenida es de la memoria dinamica o de la estatica y se obtiene el contenido de la celda 
	 * correcto. Tambien se aumenta en uno el contador del programa.
	 * 
	 * (R24)apila-ind(): 
	 *	Pila[ST] <-- Mem[Pila[ST]]	
	 *	PC <-- PC+1
	 *	
	 * @throws Exception
	 */
	public void apila_ind() throws Exception{
		if (ST<0){
			throw new Exception("ERROR: Memoria sin inicializar.");
		}
		int i = ((Integer)pila.pop()).intValue();
		if (i<tamMem){
			if ((i>=Mem.size())|| (i<0)){
				throw new Exception("Posicion de memoria no inicializada");
			}
			pila.push(Mem.elementAt(i));
		}

		else{ //memo dinamica
			i=i-tamMem;
			if (i<heap.getHeap().size()){
				pila.push(new Integer(heap.getElementAt(i)));
			}
			else{
				throw new Exception("ERROR: Puntero sin inicializar.");
			}
		}
		PC = PC +1;
	}
	
	/**
	 * Metodo que desapila el valor de la cima (v) y de la subcima (d), interpreta d como un numero de celda en la memoria(comprobando si es memoria
	 * dinamica o estatica), y almacena v en dicha celda. Se disminuye en dos(ST) el tamano de la pila y tambien se aumenta en uno el contador del 
	 * programa(PC).
	 * 
	 * (R25)despila-ind(): 
	 *  Mem[Pila[ST]] <-- Pila[ST-1]
	 *  ST<-- ST - 2
	 *  PC <-- PC + 1
	 *  @throws Exception
	 */
	public void desapila_ind() throws Exception{
		// Primero comprobamos que la memoria sea suficiente.
		// Sino lo es aumentamos el tama?o del vector.
		if (ST<1){
			throw new Exception("ERROR: Desapila_ind. La pila no contiene los datos necesarios. \n Puede que algun puntero no este inicializado");
		}
		Integer valor=(Integer)pila.pop();
		int d = ((Integer)pila.pop()).intValue();
		if (d<tamMem){	
			if (d >= 0){
				if (d>=Mem.size()){
					aumentoMem(d);
					Mem.set(d,valor);
				}
				else{
					Mem.set(d,valor);
				}
			}
			else{
				throw new Exception("ERROR: Memoria sin inicializar.");
			}
		}
		else{ //memo dinamica
			d=d-tamMem;
			if (d<heap.getHeap().size()){
				heap.setElementAt(d, valor);
			}
			else{
				throw new Exception("ERROR: Puntero sin inicializar.");
			}
		}
		ST = ST-2;
		PC = PC + 1;
	}
	
	/**
	 * Metodo que desapila de la cima la direccion origen (o) y en la subcima la direccion destino (d), y realiza el movimiento de s celdas desde 
	 * o a d. Comprueba antes que las direcciones pertenezcan a memoria dinamica o estatica. Se disminuye en dos(ST) el tamano de la pila y 
	 * tambien se aumenta en uno el contador del programa (PC).
	 * 
	 * (R26)mueve(s): 
	 * para i=0 hasta s-1 hacer
	 * 		Mem[Pila[ST-1]+i] <-- Mem[Pila[ST]+i]
	 * fpara
	 * ST <-- ST-2
	 * PC <-- PC+1
	 * 
	 * @param s Parametro que indica el numero de celdas que hay que trasladar.
	 * @throws Exception
	 */
	public void mueve (int s) throws Exception{
		if (pila.size()>2){ 
			int o = ((Integer)pila.pop()).intValue(); //pop desapilar
			int d = ((Integer)pila.pop()).intValue(); //push apilar
			for (int i=0;i<s;i++){
				if(d+i<Mem.size()-1){ 
					//Mem set cambia el elemento de la posicin d+i por el elemento que le pasas. 
					//en este caso el elemento que devuelve mem.get(o+i)
					Mem.set(d+i,Mem.get(o+i));
				}
				else{
					aumentoMem(d+i);
					Mem.set(d+i,Mem.get(o+i));
				}
			}
		}
		ST = ST-2;
		PC = PC +1;
	}

	/**
	 * Metodo que duplica la cima de la pila. 
	 * 
	 * @throws Exception
	 */
	public void copia () throws Exception {
		if (ST >= 0){
			pila.push(pila.peek());
			ST = ST + 1;
			PC = PC + 1;
		}
		else{
			throw new Exception("ERROR en Copia: Pila vacia.");
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void flip () throws Exception {
		if (ST >= 1){
			Integer i = (Integer) pila.pop();
			Integer j = (Integer) pila.pop();
			pila.push(i);
			pila.push(j);
			PC = PC + 1;
		}
		else{
			throw new Exception("ERROR en Copia: Pila vacia.");
		}
	}
	
	/**
	 * Metodo que desplaza el contador del programa a la intruccion que indica el entero almacenado en la cima de la pila
	 * @throws Exception
	 */
	public void ir_ind() throws Exception{
		if (ST >= 0){
			ST = ST - 1;
			PC = ((Integer)pila.pop()).intValue();
		}
		else{
			throw new Exception("ERROR: Pila vacia.");
		}
	}
	
	/**
	 * Metodo que obtiene el contenido de la pila en un String para ver su contenido.
	 * 
	 * @return String con el contenido de la Pila
	 */
	public String muestraPila(){
		Stack aux = new Stack();
		String pilas="El contenido de la pila es: \n";
		while(!pila.isEmpty()){
			Integer n = (Integer)pila.pop();
			pilas= pilas.concat(n.toString());
			pilas = pilas.concat("\n");
			aux.push(n);
		}
		while(!aux.isEmpty()){
			Integer n = (Integer)aux.pop();
			pila.push(n);
		}
		return pilas;
	}
}
