package MaquinaVirtual;

import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * Clase encargada de la ejecuci�n de las instrucciones generadas por el compilador a partir de 
 * un fichero que las contiene
 * @author Grupo5 PLG
 *
 */
public class MaquinaVirtual 
{
	/**
	 * Archivo de texto del que se leer�n las instrucciones generadas por el compilador.
	 */
	private FileReader lector;

	/**
	 * Variable que se utilizar� para extraer del archivo que contiene el programa compilado, en cada 
	 * instrucci�n, el tipo de instrucci�n inicial 
	 */
	private String funcion;

	/**
	 * Variable que se utilizar� para extraer del archivo que contiene el programa compilado, en cada 
	 * instrucci�n, los par�metros iniciales. 
	 */
	int datos;

	/**
	 * Representa la pila de operandos de la m�quina virtual, en la que se realizan todas las operaciones
	 */
	private Stack<Number> pila;

	/**
	 * Tabla hash que relaciona el n�mero de variable otorgado por el compilador acada variable del programa
	 * con el valor que tiene en ejecuci�n.
	 */
	private Hashtable<Integer, DireccionMemoria> mem_datos;

	/**
	 * Vector que almacena todas las instrucciones en un formato m�s eficiente para la gesti�n de la
	 * m�quina virtual, sin tener que obtener nuevamente de un archivo los datos necesarios cada vez 
	 * que se pasa por una misma instrucci�n
	 */
	private Vector<Instruccion> mem_instrucciones;

	/**
	 * Indica el n�mero de instrucci�n que se est� ejecutando en cada momento.
	 */
	private int program_counter;

	/**
	 * 0 - Run (m�quina en ejecuci�n)
	 * 1 - Stop (m�quina parada)
	 * 2 - Error (m�quina con error)
	 */
	private int estadoMaquina;

	/**
	 * Condici�n para mostrar informaci�n adicional cada vez que se ejecuta una instrucci�n. 
	 * True -> Informaci�n del PC, tipo de instrucci�n, pila de ejecuci�n y valor de las variables.
	 * False -> Informaci�n del PC y tipo de instrucci�n �nicamente.
	 */
	private boolean modoEjecucionDebug;

	/**
	 * Constructor por defecto de la m�quina virtual, que crea la memoria din�mica necesaria e inicializa
	 * los par�metros de la clase coloc�ndose en la primera instrucci�n a leer (el PC se inicializa a 0). 
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
	 * M�todo principal de la M�quina virtual que lee las instrucciones de un fichero, las analiza y las ejecuta
	 * @param modoEjecucion Condici�n para mostrar informaci�n adicional cada vez que se ejecuta una instrucci�n. 
	 * @throws Exception Fallos en la descodificaci�n de las instrucciones o por operaciones aritm�ticas no permitidas
	 * (fallos en ejecuci�n no detectables en compilaci�n)
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
			System.out.println("---------------- Nueva instrucci�n ----------------");
			System.out.print("PC="+program_counter+": Operaci�n de: "+mem_instrucciones.get(program_counter).toString());
			ejecutaOrden();
			System.out.println("... Ejecutada");
			program_counter++;
			if (modoEjecucionDebug) {
				//Se muestra el estado de las variables.
				mostrarEstadoVariables();
				//Se muestra la informaci�n de la pila de ejecuci�n.
				System.out.println("Estado de la pila: \n"+muestraInfoPila());
			}
		}
		System.out.println("Lectura y Ejecuci�n finalizadas.\n");
		System.out.println("Estado final de las variables");
		mostrarEstadoVariables();

	}

	/**
	 * Muestra la direcci�n y el valor de todas las variables utilizadas hasta el momento actual de la ejecuci�n.
	 * Si una variable no ha aparecido aun en el programa, no se muestra informaci�n sobre ella.
	 */
	private void mostrarEstadoVariables() 
	{
		System.out.println("Estado de las variables: ");
		Enumeration<DireccionMemoria> iter = mem_datos.elements();
		while (iter.hasMoreElements())
		{
			DireccionMemoria dir = iter.nextElement();
			System.out.print("   Variable Declarada n� " + (dir.getDireccion()+ 1) + " -> ");
			System.out.println("Valor: " + dir.getDato() + ".");
		}
		System.out.println("NOTA: Las variables que no aparecen no tienen asignado ning�n valor.");
	}

	/**
	 * M�todo que a partir del valor que toman funcion y datos realiza la funci�n adecuada de la m�quina virtual
	 * @throws Exception Excepci�n generada en ejecuci�n no detectable en compilaci�n
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
			System.out.println("M�quina pasa a estado error");
		}

	}

	/**
	 * M�todo que ejecuta la instrucci�n apila(n) en la m�quina virtual, que coloca el valor n 
	 * en la cima de la pila de ejecuci�n.
	 * @param param Valor num�rico a apilar.
	 * @throws Exception
	 */
	private void ejecutaApila(Number param) throws Exception {
		pila.push(param);
	}
	
	/**
	 * M�todo que ejecuta la instrucci�n apila-dir(param): Coloca el valor de la variable de direcci�n n 
	 * en la cima de la pila de ejecuci�n.
	 * @param param Direcci�n de la variable, la cual ha sido definida durante el proceso de compilaci�n.
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
	 * M�todo que ejecuta la instrucci�n desapila(): �nicamente extrae el valor de la cima de la pila.
	 * @throws Exception
	 */
	private void ejecutaDesapila() throws Exception {
		pila.pop();
	}

	/**
	 * M�todo que ejecuta la instrucci�n desapila-dir(param): Coloca el valor de la cima de la pila de
	 * ejecuci�n sobre la variable de direcci�n n 
	 * @param param Direcci�n de la variable, la cual ha sido definida durante el proceso de compilaci�n.
	 * @throws Exception
	 */
	private void ejecutaDesapilaDir(Number param) throws Exception {
		int dir = datos;
		mem_datos.put(dir, new DireccionMemoria(pila.pop(), dir));
	}

	/**
	 * Cambia el estado de la m�quina virtual a parado (valor 1)
	 * @throws Exception
	 */
	private void ejecutaStop() throws Exception{
		estadoMaquina=1; //Pasa a parada, luego dar m�s informaci�n.
		System.out.println("M�quina pasa a estado de parada");
	}

	/**
	 * M�todo que ejecuta la instrucci�n negacion: Extrae el valor de la cima de la pila (booleano representado
	 * como 0 o 1) y realiza la operaci�n l�gica not, apilando el resultado nuevamente en la pila.
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
	 * M�todo que ejecuta la instrucci�n or: Extrae los 2 �ltimos operandos de la pila (booleanos representados
	 * como 0 o 1) y realiza la operaci�n l�gica or, apilando el resultado nuevamente en la pila.
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
	 * M�todo que ejecuta la instrucci�n and: Extrae los 2 �ltimos operandos de la pila (booleanos representados
	 * como 0 o 1) y realiza la operaci�n l�gica and, apilando el resultado nuevamente en la pila.
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
	 * M�todo que ejecuta la instrucci�n menoroigual: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n de comparaci�n <=, apilando el resultado booleano obtenido
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
	 * M�todo que ejecuta la instrucci�n mayoroigual: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n de comparaci�n >=, apilando el resultado booleano obtenido
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
	 * M�todo que ejecuta la instrucci�n menor: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n de comparaci�n <, apilando el resultado booleano obtenido
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
	 * M�todo que ejecuta la instrucci�n mayor: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n de comparaci�n >, apilando el resultado booleano obtenido
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
	 * M�todo que ejecuta la instrucci�n distintos: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n de comparaci�n !=, es decir, si los 2 n�meros analizados
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
	 * M�todo que ejecuta la instrucci�n iguales: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n de comparaci�n ==, es decir, si los 2 n�meros analizados
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
	 * M�todo que ejecuta la instrucci�n opuesto: Extrae el valor n de la cima de la pila de ejecuci�n 
	 * y realiza la operaci�n aritm�tica -n, apilando el valor obtenido nuevamente en la pila.
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
	 * M�todo que ejecuta la instrucci�n modulo: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n aritm�tica m�dulo (%), apilando el valor obtenido 
	 * nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] % pila[cima];
	 * @throws Exception
	 */
	private void ejecutaMod() throws Exception {
		if (pila.size()<2) 
			throw new Exception("Error: M�dulo. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pila.pop(); 
			Number oper1=(Number) pila.pop();
			Number resul= (oper1.intValue() % oper2.intValue());
			pila.push(resul);
		}
	}

	/**
	 * M�todo que ejecuta la instrucci�n divide: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n aritm�tica division entera, apilando el valor obtenido 
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
	 * M�todo que ejecuta la instrucci�n multiplica: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n aritm�tica multiplicaci�n, apilando el valor obtenido 
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
	 * M�todo que ejecuta la instrucci�n resta: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n aritm�tica resta, apilando el valor obtenido 
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
	 * M�todo que ejecuta la instrucci�n suma: Extrae los 2 �ltimos operandos de la pila 
	 * (num�ricos) y realiza la operaci�n aritm�tica suma, apilando el valor obtenido 
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
	 * M�todo que lee una l�nea del fichero y la convierte a un String.
	 * @return Una linea del fichero que contendr� una instruccion.
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
	 * M�tood auxiliar para gestionar la lectura del fichero, que a aprtir del string de una linea del fichero 
	 * separa el tipo de la instrucci�n de los posibles par�metros.
	 * @param lectura Una linea que contiene una instrucci�n completa.
	 */
	private void divide(String lectura) 
	{
		int longitud = lectura.length();
		int parentesis = lectura.lastIndexOf('(');
		if (parentesis == -1) //No ha encontrado par�ntesis (la linea es una operacion sin par�metros).
		{
			funcion = lectura.substring(0,longitud - 1);
			datos = 0;
		}
		else
		{	
			funcion = lectura.substring(0, parentesis);
			//	Try & Catch para detectar cuando es una instrucci�n sin parametros, del tipo de suma; o iguales;
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
	 * M�todo que convierte un valor booleano true/false en un entero con valor 1/0 respectivamente.
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
	 * M�todo que devuelve un String con la informaci�n de la pila de ejecuci�n, posici�n y valor, utilizando
	 * para ello una pila auxiliar.  
	 * @return Informaci�n de la pila de ejecuci�n
	 */
	public String muestraInfoPila(){
		Stack<Number> pilaEjecucionAux = new Stack<Number>();
		String pilaInfo="N�mero de operandos en la pila de ejecuci�n: ";
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
			pilaInfo= pilaInfo.concat(" operandos. Pila vac�a\n");
		}
		pilaInfo=pilaInfo.concat("\n---------------------------------------------------\n");
		return pilaInfo;
	}

}
