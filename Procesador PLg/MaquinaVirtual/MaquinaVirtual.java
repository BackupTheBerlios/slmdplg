package MaquinaVirtual;

import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * Clase encargada de la ejecución de las instrucciones generadas por el compilador a partir de 
 * un fichero que las contiene
 * @author Grupo5 PLG
 *
 */
public class MaquinaVirtual 
{
	/**
	 * Archivo de texto del que se leerán las instrucciones generadas por el compilador.
	 */
	private FileReader lector;

	/**
	 * Variable que se utilizará para extraer del archivo que contiene el programa compilado, en cada 
	 * instrucción, el tipo de instrucción inicial 
	 */
	private String funcion;

	/**
	 * Variable que se utilizará para extraer del archivo que contiene el programa compilado, en cada 
	 * instrucción, los parámetros iniciales. 
	 */
	int datos;

	/**
	 * Representa la pila de operandos de la máquina virtual, en la que se realizan todas las operaciones
	 */
	private Stack<Number> pila;

	/**
	 * Tabla hash que relaciona el número de variable otorgado por el compilador acada variable del programa
	 * con el valor que tiene en ejecución.
	 */
	private Hashtable<Integer, DireccionMemoria> mem_datos;

	/**
	 * Vector que almacena todas las instrucciones en un formato más eficiente para la gestión de la
	 * máquina virtual, sin tener que obtener nuevamente de un archivo los datos necesarios cada vez 
	 * que se pasa por una misma instrucción
	 */
	private Vector<Instruccion> mem_instrucciones;

	/**
	 * Indica el número de instrucción que se está ejecutando en cada momento.
	 */
	private int program_counter;

	/**
	 * 0 - Run (máquina en ejecución)
	 * 1 - Stop (máquina parada)
	 * 2 - Error (máquina con error)
	 */
	private int estadoMaquina;

	/**
	 * Condición para mostrar información adicional cada vez que se ejecuta una instrucción. 
	 * True -> Información del PC, tipo de instrucción, pila de ejecución y valor de las variables.
	 * False -> Información del PC y tipo de instrucción únicamente.
	 */
	private boolean modoEjecucionDebug;

	/**
	 * Constructor por defecto de la máquina virtual, que crea la memoria dinámica necesaria e inicializa
	 * los parámetros de la clase colocándose en la primera instrucción a leer (el PC se inicializa a 0). 
	 * @param fis Archivo con las instrucciones generados por el compilador, para posteriormente ejecutarlas.
	 * @throws IOException Si no encuentra el archivo esperado con las isntrucciones.
	 */
	public MaquinaVirtual(FileReader fis) throws IOException 
	{
		lector    = fis;
		funcion   = "";
		datos     = 0;
		pila      = new Stack<Number>(); 
		mem_datos = new Hashtable<Integer, DireccionMemoria>();
		mem_instrucciones = new Vector<Instruccion>();
		program_counter = 0;
		estadoMaquina=0;
		modoEjecucionDebug=true;
	}

	/**
	 * Método principal de la Máquina virtual que lee las instrucciones de un fichero, las analiza y las ejecuta
	 * @param modoEjecucion Condición para mostrar información adicional cada vez que se ejecuta una instrucción. 
	 * @throws Exception Fallos en la descodificación de las instrucciones o por operaciones aritméticas no permitidas
	 * (fallos en ejecución no detectables en compilación)
	 */
	public void ejecuta(boolean modoEjecucion) throws Exception 
	{
		String lectura = "";
		modoEjecucionDebug=modoEjecucion;
		while (!(lectura = leerLinea()).equals("end;"))
		{
			divide(lectura);
			mem_instrucciones.add(new Instruccion(funcion, datos));
		}
		while (program_counter < mem_instrucciones.size())
		{		
			funcion = mem_instrucciones.get(program_counter).getOperacion();
			datos = mem_instrucciones.get(program_counter).getParametros();
			System.out.println("---------------- Nueva instrucción ----------------");
			System.out.print("PC="+program_counter+": Operación de: "+mem_instrucciones.get(program_counter).toString());
			ejecutaOrden();
			System.out.println("... Ejecutada");
			program_counter++;
			if (modoEjecucionDebug) {
				//Se muestra el estado de las variables.
				mostrarEstadoVariables();
				//Se muestra la información de la pila de ejecución.
				System.out.println("Estado de la pila: \n"+muestraInfoPila());
			}
		}
		System.out.println("Lectura y Ejecución finalizadas.\n");
		System.out.println("Estado final de las variables");
		mostrarEstadoVariables();

	}

	/**
	 * Muestra la dirección y el valor de todas las variables utilizadas hasta el momento actual de la ejecución.
	 * Si una variable no ha aparecido aun en el programa, no se muestra información sobre ella.
	 */
	private void mostrarEstadoVariables() 
	{
		System.out.println("Estado de las variables: ");
		Enumeration<DireccionMemoria> iter = mem_datos.elements();
		while (iter.hasMoreElements())
		{
			DireccionMemoria dir = iter.nextElement();
			System.out.print("   Variable Declarada nº " + (dir.getDireccion()+ 1) + " -> ");
			System.out.println("Valor: " + dir.getDato() + ".");
		}
		System.out.println("NOTA: Las variables que no aparecen no tienen asignado ningún valor.");
	}

	/**
	 * Método que a partir del valor que toman funcion y datos realiza la función adecuada de la máquina virtual
	 * @throws Exception Excepción generada en ejecución no detectable en compilación
	 */
	private void ejecutaOrden() throws Exception {

		if (funcion.equals("suma")) {
			ejecutaSuma();
		}
		else if (funcion.equals("resta")) {
			ejecutaResta();
		}
		else if (funcion.equals("multiplica")) {
			ejecutaMultiplicacion();
		}
		else if (funcion.equals("divide")) {
			ejecutaDivision();
		}
		else if (funcion.equals("modulo")) {
			ejecutaMod();
		}
		else if (funcion.equals("opuesto")) {
			ejecutaCambioSigno();
		}
		else if (funcion.equals("iguales")) {
			ejecutaIgual();
		}
		else if (funcion.equals("distintos")) {
			ejecutaDistinto();
		}
		else if (funcion.equals("mayor")) {
			ejecutaMayor();
		}
		else if (funcion.equals("menor")) {
			ejecutaMenor();
		}
		else if (funcion.equals("mayoroigual")) {
			ejecutaMayorIgual();
		}
		else if (funcion.equals("menoroigual")) {
			ejecutaMenorIgual();
		}
		else if (funcion.equals("and")) {
			ejecutaAnd();
		}
		else if (funcion.equals("or")) {
			ejecutaOr();
		}
		else if (funcion.equals("negacion")) {
			ejecutaNot();
		}
		else if (funcion.equals("Stop")) {
			ejecutaStop();
		}
		else if (funcion.equals("apila")) {
			ejecutaApila(datos);
		}
		else if (funcion.equals("desapila")) {
			ejecutaDesapila();
		}
		else if (funcion.equals("apila-dir")) {
			ejecutaApilaDir(datos);
		}
		else if (funcion.equals("desapila-dir")) {
			ejecutaDesapilaDir(datos);
		}
		else {
			estadoMaquina=2; //Pasa a error
			System.out.println("Máquina pasa a estado error");
		}

	}

	/**
	 * Método que ejecuta la instrucción apila(n) en la máquina virtual, que coloca el valor n 
	 * en la cima de la pila de ejecución.
	 * @param param Valor numérico a apilar.
	 * @throws Exception
	 */
	private void ejecutaApila(Number param) throws Exception {
		pila.push(param);
	}
	
	/**
	 * Método que ejecuta la instrucción apila-dir(param): Coloca el valor de la variable de dirección n 
	 * en la cima de la pila de ejecución.
	 * @param param Dirección de la variable, la cual ha sido definida durante el proceso de compilación.
	 * @throws Exception
	 */
	private void ejecutaApilaDir(Number param) throws Exception {	
		Integer dir = datos;
		if (!mem_datos.containsKey(dir)) {
			System.out.println("No encuentra posicion en la mem_datos, en apila-dir\n");
			pila.push(0);
		}
		else
			pila.push(mem_datos.get(dir).getDato());
	}

	/**
	 * Método que ejecuta la instrucción desapila(): Únicamente extrae el valor de la cima de la pila.
	 * @throws Exception
	 */
	private void ejecutaDesapila() throws Exception {
		pila.pop();
	}

	/**
	 * Método que ejecuta la instrucción desapila-dir(param): Coloca el valor de la cima de la pila de
	 * ejecución sobre la variable de dirección n 
	 * @param param Dirección de la variable, la cual ha sido definida durante el proceso de compilación.
	 * @throws Exception
	 */
	private void ejecutaDesapilaDir(Number param) throws Exception {
		int dir = datos;
		mem_datos.put(dir, new DireccionMemoria(pila.pop(), dir));
	}

	/**
	 * Cambia el estado de la máquina virtual a parado (valor 1)
	 * @throws Exception
	 */
	private void ejecutaStop() throws Exception{
		estadoMaquina=1; //Pasa a parada, luego dar más información.
		System.out.println("Máquina pasa a estado de parada");
	}

	/**
	 * Método que ejecuta la instrucción negacion: Extrae el valor de la cima de la pila (booleano representado
	 * como 0 o 1) y realiza la operación lógica not, apilando el resultado nuevamente en la pila.
	 * pila[cima]=!pila[cima];
	 * @throws Exception
	 */
	private void ejecutaNot() throws Exception {
		if (pila.size()<1) 
			throw new Exception("Error: Not. La pila no contiene operandos suficientes.");
		else { 
			Number oper1=pila.pop();
			oper1=(1-oper1.intValue()); 
			pila.push(oper1);
		}
	}

	/**
	 * Método que ejecuta la instrucción or: Extrae los 2 últimos operandos de la pila (booleanos representados
	 * como 0 o 1) y realiza la operación lógica or, apilando el resultado nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] or pila[cima];
	 * @throws Exception
	 */
	private void ejecutaOr() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: Or. La pila no contiene operandos suficientes.");
		else {
			Number oper2= pila.pop(); 
			Number oper1= pila.pop();
			Boolean r=(oper1.intValue()==1 || oper2.intValue()==1);
			Number resul=convertirBooleanoaEntero(r);
			pila.push(resul);		
		}
	}

	/**
	 * Método que ejecuta la instrucción and: Extrae los 2 últimos operandos de la pila (booleanos representados
	 * como 0 o 1) y realiza la operación lógica and, apilando el resultado nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] and pila[cima];
	 * @throws Exception
	 */
	private void ejecutaAnd() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: And. La pila no contiene operandos suficientes.");
		else {
			Number oper2= pila.pop(); 
			Number oper1= pila.pop();
			Boolean r=(oper1.intValue()==1 && oper2.intValue()==1);
			Number resul=convertirBooleanoaEntero(r);
			pila.push(resul);		
		}
	}

	/**
	 * Método que ejecuta la instrucción menoroigual: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación <=, apilando el resultado booleano obtenido
	 * en la pila (apilando 1 si cierto, 0 si falso).
	 * pila[cima-1] = pila[cima-1] <= pila[cima];
	 * @throws Exception
	 */
	private void ejecutaMenorIgual() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: MenorIgual. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Boolean r= (oper1.floatValue() <= oper2.floatValue());
			Number resul=convertirBooleanoaEntero(r);
			pila.push(resul);
		}
	}

	/**
	 * Método que ejecuta la instrucción mayoroigual: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación >=, apilando el resultado booleano obtenido
	 * en la pila (apilando 1 si cierto, 0 si falso).
	 * pila[cima-1] = pila[cima-1] >= pila[cima];
	 * @throws Exception
	 */
	private void ejecutaMayorIgual() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: MayorIgual. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Boolean r= (oper1.floatValue() >= oper2.floatValue());
			Number resul=convertirBooleanoaEntero(r);
			pila.push(resul);
		}
	}

	/**
	 * Método que ejecuta la instrucción menor: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación <, apilando el resultado booleano obtenido
	 * en la pila (apilando 1 si cierto, 0 si falso).
	 * pila[cima-1] = pila[cima-1] < pila[cima];
	 * @throws Exception
	 */
	private void ejecutaMenor() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: Menor. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Boolean r= (oper1.floatValue() < oper2.floatValue());
			Number resul=convertirBooleanoaEntero(r);
			pila.push(resul);
		}
	}

	/**
	 * Método que ejecuta la instrucción mayor: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación >, apilando el resultado booleano obtenido
	 * en la pila (apilando 1 si cierto, 0 si falso).
	 * pila[cima-1] = pila[cima-1] > pila[cima];
	 * @throws Exception
	 */
	private void ejecutaMayor() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: Mayor. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Boolean r= (oper1.floatValue() > oper2.floatValue());
			Number resul=convertirBooleanoaEntero(r);
			pila.push(resul);
		}
	}

	/**
	 * Método que ejecuta la instrucción distintos: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación !=, es decir, si los 2 números analizados
	 * son diferentes, y apila el resultado booleano obtenido en la pila (1 si cierto, 0 si falso).
	 * pila[cima-1] = pila[cima-1] != pila[cima];
	 * @throws Exception
	 */
	private void ejecutaDistinto() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: Distinto. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Integer) pila.pop(); 
			Number oper1=(Integer) pila.pop();
			Boolean r= (oper1.floatValue() != oper2.floatValue());
			Number resul=convertirBooleanoaEntero(r);

			pila.push(resul);
		}
	}

	/**
	 * Método que ejecuta la instrucción iguales: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación ==, es decir, si los 2 números analizados
	 * son iguales, y apila el resultado booleano obtenido en la pila (1 si cierto, 0 si falso).
	 * pila[cima-1] = pila[cima-1] == pila[cima];
	 * @throws Exception
	 */
	private void ejecutaIgual() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: Igual. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Boolean r= (oper1.floatValue() == oper2.floatValue());
			Number resul=convertirBooleanoaEntero(r);

			pila.push(resul);
		}
	}

	/**
	 * Método que ejecuta la instrucción opuesto: Extrae el valor n de la cima de la pila de ejecución 
	 * y realiza la operación aritmética -n, apilando el valor obtenido nuevamente en la pila.
	 * pila[cima] = - pila[cima];
	 * @throws Exception
	 */
	private void ejecutaCambioSigno() throws Exception {
		if (pila.size()<1) 
			throw new Exception("Error: Cambio de Signo. La pila no contiene operandos suficientes.");
		else {
			Number oper1=(Number) pila.pop();
			Number resul= (-oper1.intValue());
			pila.push(resul);
		}
	}

	/**
	 * Método que ejecuta la instrucción modulo: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación aritmética módulo (%), apilando el valor obtenido 
	 * nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] % pila[cima];
	 * @throws Exception
	 */
	private void ejecutaMod() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: Módulo. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Number resul= (oper1.intValue() % oper2.intValue());
			pila.push(resul);
		}
	}

	/**
	 * Método que ejecuta la instrucción divide: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación aritmética division entera, apilando el valor obtenido 
	 * nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] / pila[cima];
	 * @throws Exception
	 */
	private void ejecutaDivision() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: Division. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Number resul= (oper1.intValue() / oper2.intValue()); 
			pila.push(resul);
		}
	}

	/**
	 * Método que ejecuta la instrucción multiplica: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación aritmética multiplicación, apilando el valor obtenido 
	 * nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] * pila[cima];
	 * @throws Exception
	 */
	private void ejecutaMultiplicacion() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: Multiplicacion. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Number resul= (oper1.intValue() * oper2.intValue());
			pila.push(resul);
		}
	}

	/**
	 * Método que ejecuta la instrucción resta: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación aritmética resta, apilando el valor obtenido 
	 * nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] - pila[cima];
	 * @throws Exception
	 */
	private void ejecutaResta() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: Resta. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Number resul= (oper1.intValue() - oper2.intValue());
			pila.push(resul);		
		}
	}

	/**
	 * Método que ejecuta la instrucción suma: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación aritmética suma, apilando el valor obtenido 
	 * nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] + pila[cima];
	 * @throws Exception
	 */
	private void ejecutaSuma() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: Suma. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Number resul= (oper1.intValue() + oper2.intValue());
			pila.push(resul);		
		}
	}

	/**
	 * Método que lee una línea del fichero y la convierte a un String.
	 * @return Una linea del fichero que contendrá una instruccion.
	 * @throws IOException
	 */
	private String leerLinea() throws IOException 
	{
		char l = ' ';
		int n = 0;
		String cadena = "";
		while (l != '\n' && n!=-1)
		{
			n = lector.read();
			l = (char)n;
			if (l != '\n' && n != -1)
				cadena += l;
		}
		return cadena;
	}

	/**
	 * Métood auxiliar para gestionar la lectura del fichero, que a aprtir del string de una linea del fichero 
	 * separa el tipo de la instrucción de los posibles parámetros.
	 * @param lectura Una linea que contiene una instrucción completa.
	 */
	private void divide(String lectura) 
	{
		int longitud = lectura.length();
		int parentesis = lectura.lastIndexOf('(');
		if (parentesis == -1) //No ha encontrado paréntesis (la linea es una operacion sin parámetros).
		{
			funcion = lectura.substring(0,longitud - 1);
			datos = 0;
		}
		else
		{	
			funcion = lectura.substring(0, parentesis);
			//	Try & Catch para detectar cuando es una instrucción sin parametros, del tipo de suma; o iguales;
			try 
			{	
				datos = Integer.parseInt(lectura.substring(parentesis + 1, longitud - 2));
			} 
			catch (NumberFormatException e) 
			{
				datos = 0;
			}
		} 
	}

	/**
	 * Método que convierte un valor booleano true/false en un entero con valor 1/0 respectivamente.
	 * @param r Booleano que se desea convertir a entero.
	 * @return Valor booleano representado por un entero
	 */
	private Number convertirBooleanoaEntero(Boolean r) {
		Number resul;
		if (r)	resul=new Integer(1);
		else resul=new Integer(0);
		return resul;
	}

	/**
	 * Método que devuelve un String con la información de la pila de ejecución, posición y valor, utilizando
	 * para ello una pila auxiliar.  
	 * @return Información de la pila de ejecución
	 */
	public String muestraInfoPila(){
		Stack<Number> pilaEjecucionAux = new Stack<Number>();
		String pilaInfo="Número de operandos en la pila de ejecución: ";
		int pilanum=(pila.size());
		pilaInfo= pilaInfo.concat(""+pilanum);

		if (!pila.isEmpty()) {
			pilaInfo=pilaInfo.concat(" operandos");
			while(!pila.isEmpty()){
				Number elem = pila.pop();
				pilaInfo= pilaInfo.concat("\n"+"Operando "+pilanum+": ");
				pilaInfo= pilaInfo.concat(elem.toString());
				pilanum--;
				pilaEjecucionAux.push(elem);
			}

			while(!pilaEjecucionAux.isEmpty()){
				Number elem = pilaEjecucionAux.pop();
				pila.push(elem);
			}
		}
		else {
			//Pila vacia
			pilaInfo= pilaInfo.concat(" operandos. Pila vacía\n");
		}
		pilaInfo=pilaInfo.concat("\n---------------------------------------------------\n");
		return pilaInfo;
	}

}
