package MaquinaVirtual;

import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

/**
 * Clase encargada de la ejecuci�n de las instrucciones generadas por el compilador a partir de 
 * un fichero que las contiene.
 * Consta de dos registros datos1 y datos2 para los parametros de las instrucciones.
 * Consta de una memoria de instrucciones y una �nica memoria de datos, tanto para las variables estaticas
 * como para las pilas de registros de activaci�n y de operandos. Dichas pilas se tratan como una s�la.
 * Finalmente indicar que la memoria dinamica se organiza a partir de la posicion
 * 5000 de la memoria de datos, y crece hacia arriba. 
 * @author Grupo5 PLG
 *
 */
public class MaquinaVirtual 
{
	/**
	 * Posicion a partir de la cual comienza la memoria dinamica.
	 */
	public static final int InicioH = 5000;
	
	/**
	 * Define el tama�o de la memoria de datos
	 */
	public static final int TamanoMem = 10000;
	
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
	 * instrucci�n, el primer parametro inicial. 
	 */
	Number datos1;
	
	/**
	 * Variable que se utilizar� para extraer del archivo que contiene el programa compilado, en cada 
	 * instrucci�n, el segundo parametro inicial. 
	 */
	Number datos2;	

	/**
	 * Tabla hash que ralaciona un valor de tipo Integer, con un valor de tipo Number.
	 * Equivale formalente a la memoria del programa, donde la variable
	 * de tipo integer, clave de la tabla Hash representa la direccion y la variable de 
	 * tipo Number representa el contenido.
	 */
	private Number[] mem_datos;

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
	 * Registro que indica cual es la primera posici�n de memoria libre en el
	 * bloque de memoria dinamica. 
	 */
	private int H;
	
	/**
	 * Registro que apunta a la base del registro de activaci�n activo,
	 * es decir, a la base de direcciones relativas en la memoria de datos.
	 */
	private int B;
	
	/**
	 * Registro que apunta a la cima de la pila de la memoria.
	 */
	private int C;

	/**
	 * Constructor por defecto de la m�quina virtual, que crea la memoria din�mica necesaria e inicializa
	 * los par�metros de la clase coloc�ndose en la primera instrucci�n a leer (el PC se inicializa a 0). 
	 * @param fis Archivo con las instrucciones generados por el compilador, para posteriormente ejecutarlas.
	 * @throws IOException Si no encuentra el archivo esperado con las isntrucciones.
	 */
	public MaquinaVirtual(FileReader fis) throws IOException 
	{
		lector = fis;
		funcion = "";
		datos1 = 0;
		datos2 = 0;
		C = 0;
		B = 0;
		H = InicioH;
		mem_datos =  new Number[TamanoMem];
		mem_datos [B] = 0;
		mem_datos [H] = 0;
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
			mem_instrucciones.add(new Instruccion(funcion, datos1,datos2));
		}
		
		divide(lectura);
		mem_instrucciones.add(new Instruccion(funcion, datos1,datos2));
		
		while (program_counter < mem_instrucciones.size() && estadoMaquina==0)
		{		
			funcion = mem_instrucciones.get(program_counter).getOperacion();
			datos1 = mem_instrucciones.get(program_counter).getParametros1();
			datos2 = mem_instrucciones.get(program_counter).getParametros2();
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
		if (estadoMaquina == 2) System.out.println("La maquina ha finalizado con un error");
		
		if (estadoMaquina == 1) System.out.println("Lectura y Ejecuci�n finalizadas con �xito.\n");
		
		if (estadoMaquina == 0) System.out.println("La maquina ha finalizado " +
				"debido a no disponer de m�s instrucciones, pero no ha encontrado " +
				"instruccion de parada.");
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
		for (int i=0;i<C;i++){
			System.out.print("   Direccion de memoria: " + i + " -> ");
			System.out.println("Valor: " + mem_datos[i].intValue() + ".");
		}
		for (int i=InicioH;i<H;i++){
			System.out.print("   Direccion de memoria: " + i + " -> ");
			System.out.println("Valor: " + mem_datos[i].intValue() + ".");
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
		else if (funcion.equals("apila")) {
			ejecutaApila(datos1);
		}
		else if (funcion.equals("desapila")) {
			ejecutaDesapila();
		}
		else if (funcion.equals("apila-dir")) {
			ejecutaApilaDir(datos1,datos2);
		}
		else if (funcion.equals("desapila-dir")) {
			ejecutaDesapilaDir(datos1,datos2);
		}
		else if (funcion.equals("copia")) {
			ejecutaCopia();
		}
		else if (funcion.equals("ir-a")){
			ejecutaIrA(datos1);
		}		
		else if (funcion.equals("ir-v")){
			ejecutaIrV(datos1);			
		}
		else if (funcion.equals("ir-f")){
			ejecutaIrF(datos1);
		}
		else if (funcion.equals("return")){
			ejecutaRet();
		}		
		else if (funcion.equals("apila-ind")){
			ejecutaApilaInd();
		}
		else if (funcion.equals("desapila-ind")){
			ejecutaDesapilaInd();
		}
		else if (funcion.equals("apilaH")){
			ejecutaApilaH();		
		}
		else if (funcion.equals("incrementaH")){
			ejecutaIncrementaH(datos1);
		}
		else if (funcion.equals("incrementaC")){
			ejecutaIncrementaC(datos1);
		}
		else if (funcion.equals("llamada")){
			ejecutaLLamada();
		}
		else if (funcion.equals("retorno")){
			ejecutaRet();
		}
		else if (funcion.equals("end")) {
			ejecutaStop();
		}		
		else if (funcion.equals("Stop")){
			ejecutaStop();
		}
		else {
			estadoMaquina=2; //Pasa a error
			System.out.println("M�quina pasa a estado error");
		}

	}

	/**
	 * M�todo que incrementa el valor de la cima de la pila en el n�mero
	 * de posiciones de memoria pasadas por par�metro.
	 * @param param N�mero de posiciones de memoria a incrementar el valor
	 * de la cima de la pila.
	 * @throws Exception 
	 */
	private void ejecutaIncrementaC(Number param) throws Exception {
		if (C + param.intValue() > InicioH) 
			throw new Exception("Error.IncrementaH : Intenta llevar las pilas m�s all� del" +
					" comienzo de la memoria d�namica.");
		else 
			{
				if (param.intValue() >= 0)
					for (int i = 0; i < param.intValue(); i++)
						push(new Integer(0));
				else
					for (int i = 0; i < -param.intValue(); i++)
						pop();
			}
		
	}

	/**
	 * M�todo que se utiliza de ejecutar la llamada a un procedimiento,
	 * para ello crea su registro de activacion, con la Instruccion Siguiente,
	 * el Enlace Dinamico y el Enlace Estatico.
	 * 
	 * @param fnp Indica el nivel del procedimiento, servir� para reconstruir
	 * su entorno l�xico.
	 */
	private void ejecutaLLamada() {
		
		Number fnp = pop();
		Number etiq = pop(); 
		
		// Obtenemos los tres datos del registro de activacion;
		int IS = program_counter;
		int ED = B;
		int EE = extraeDirBase(fnp.intValue());
		
		// Almacenamos los datos en las posiciones de memoria correspondientes
		mem_datos[C] = EE;
		mem_datos[C+1] = ED;
		mem_datos[C+2] = IS;
						
		// Actualizamos el registro B, y el contador de programa
		B=C;
		C=C+3;
		program_counter = (etiq.intValue());
	}

	/**
	 * M�todo que incrementa el valor del registro H en el
	 * n�mero de posiciones pasadas como parametro.
	 * @param param N�mero de posiciones en que se incrementar� el valor
	 * del registro H.
	 */
	private void ejecutaIncrementaH(Number param) {		
		H += param.intValue();
	}

	/**
	 * M�todo que coloca sobre la cima de la pila el valor del
	 * registro H.
	 *
	 */
	private void ejecutaApilaH() {		
		push(H);
	}

	/**
	 * Funcion que coloca el valor de la cima de la pila sobre la
	 * direccion de memoria especificada por los dos siguientes valores
	 * en la pila, donde la subcima indica el n�mero de niveles a retroceder
	 * y el anterior a la subcima indica el desplazamiento.
	 * @throws Exception 
	 *
	 */
	private void ejecutaDesapilaInd() throws Exception {
		if (tamanoPila()<3)throw new Exception("Error: DesapilaInd. La pila no contiene operandos suficientes.");
		else {
			Number dato = pop();
			int fnv = pop().intValue();
			int offset = pop().intValue();
			int dir = extraeDirBase(fnv) + offset;
			mem_datos[dir] = dato;				
		}		
	}

	/**
	 * Funcion que coloca sobre la pila de operandos el valor contenido
	 * en la direccion de memoria indicada por la actual cima de la pila,
	 * desechando a partir de ah� esa cima.
	 *
	 */
	private void ejecutaApilaInd() throws Exception{
		if (tamanoPila()<2)throw new Exception("Error: ApilaInd. La pila no contiene operandos suficientes.");
		else {
			int fnv = pop().intValue();
			int offset = pop().intValue();
			int dir = extraeDirBase(fnv) + offset;
			if (mem_datos[dir] == null) {
				System.out.println("No encuentra posicion en la mem_datos, en apila-ind\n");
				push(0);
			}
			else
				push(mem_datos[dir]);				
		}			
	}

	/**
	 * M�todo que ejecuta la instruccion retorno de subrutina, para
	 * ello para ello actualiza los valores de los registros de Cima de Pila
	 * y Base del registro de activacion activo.
	 * Finalmente actualiza el contador de programa.
	 */
	private void ejecutaRet() {		
		C = B;
		B= mem_datos[C+1].intValue();
		program_counter = mem_datos[C+2].intValue();
	}

	/**
	 * M�todo que ejecuta la instrucci�n apila(n) en la m�quina virtual, que coloca el valor n 
	 * en la cima de la pila de ejecuci�n.
	 * @param param Valor num�rico a apilar.
	 * @throws Exception
	 */
	private void ejecutaApila(Number param) throws Exception {
		push(param);
	}
	
	/**
	 * M�todo que ejecuta la instrucci�n apila-dir(param): Coloca el valor de la variable de direcci�n n 
	 * en la cima de la pila de ejecuci�n.
	 * @param param Direcci�n de la variable, la cual ha sido definida durante el proceso de compilaci�n.
	 * @throws Exception
	 */
	private void ejecutaApilaDir(Number fnv,Number offset) throws Exception {
		int dir = extraeDirBase(fnv.intValue()) + offset.intValue();		
		if (mem_datos[dir]==null) {
			System.out.println("No encuentra posicion en la mem_datos, en apila-dir\n");
			push(0);
		}
		else
			push(mem_datos[dir]);
	}

	/**
	 * M�todo que ejecuta la instrucci�n desapila(): �nicamente extrae el valor de la cima de la pila.
	 * @throws Exception
	 */
	private void ejecutaDesapila() throws Exception {
		if (tamanoPila()<1)throw new Exception("Error: Desapila. La pila no contiene operandos suficientes.");
		else pop();
	}
	
	
	/**
	 * M�todo que ejecuta la instrucci�n desapila-dir(param): Coloca el valor de la cima de la pila de
	 * ejecuci�n sobre la variable una direccion de memoria. Dicha direccion
	 * de memoria se obtiene, retrocediento tantos niveles de registros de
	 * activaci�n como nos indique fnv, teniendo esa base se le suma param, para
	 * obtener la obtener la direccion de memoria a la que debemos acceder para llegar a la
	 * vaariable en cuestion.  
	 * @param fnv N�mero de niveles de indireccion a aplicar, partiendo de B, para conocer la
	 * direccion base.
	 * @param param N�mero de posiciones de memoria que habra que sumarle
	 * a la direccion base para conocer la direccion de memoria objetivo.
	 * @throws Exception
	 */
	private void ejecutaDesapilaDir(Number fnv,Number param) throws Exception {
		if (tamanoPila()<1)throw new Exception("Error: DesapilaDir. La pila no contiene operandos suficientes.");
		else {			
			int dir = extraeDirBase(fnv.intValue()) + param.intValue();
			Number value = pop();
			mem_datos[dir] = value;	
		}		
	}

	/**
	 * Cambia el estado de la m�quina virtual a parado (valor 1)
	 * @throws Exception
	 */
	private void ejecutaStop() throws Exception{
		estadoMaquina=1; //Pasa a parada, luego dar m�s informaci�n.		
	}

	/**
	 * M�todo que ejecuta la instrucci�n negacion: Extrae el valor de la cima de la pila (booleano representado
	 * como 0 o 1) y realiza la operaci�n l�gica not, apilando el resultado nuevamente en la pila.
	 * pila[cima]=!pila[cima];
	 * @throws Exception
	 */
	private void ejecutaNot() throws Exception {
		if (tamanoPila()<1) 
			throw new Exception("Error: Not. La pila no contiene operandos suficientes.");
		else { 
			Number oper1=pop();
			oper1=(1-oper1.intValue()); 
			push(oper1);
		}
	}

	/**
	 * M�todo que ejecuta la instrucci�n or: Extrae los 2 �ltimos operandos de la pila (booleanos representados
	 * como 0 o 1) y realiza la operaci�n l�gica or, apilando el resultado nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] or pila[cima];
	 * @throws Exception
	 */
	private void ejecutaOr() throws Exception {
		if (tamanoPila()<2) 
			throw new Exception("Error: Or. La pila no contiene operandos suficientes.");
		else {
			Number oper2= pop(); 
			Number oper1= pop();
			Boolean r=(oper1.intValue()==1 || oper2.intValue()==1);
			Number resul=convertirBooleanoaEntero(r);
			push(resul);		
		}
	}

	/**
	 * M�todo que ejecuta la instrucci�n and: Extrae los 2 �ltimos operandos de la pila (booleanos representados
	 * como 0 o 1) y realiza la operaci�n l�gica and, apilando el resultado nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] and pila[cima];
	 * @throws Exception
	 */
	private void ejecutaAnd() throws Exception {
		if (tamanoPila()<2) 
			throw new Exception("Error: And. La pila no contiene operandos suficientes.");
		else {
			Number oper2= pop(); 
			Number oper1= pop();
			Boolean r=(oper1.intValue()==1 && oper2.intValue()==1);
			Number resul=convertirBooleanoaEntero(r);
			push(resul);		
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
		if (tamanoPila()<2) 
			throw new Exception("Error: MenorIgual. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Boolean r= (oper1.intValue() <= oper2.intValue());
			Number resul=convertirBooleanoaEntero(r);
			push(resul);
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
		if (tamanoPila()<2) 
			throw new Exception("Error: MayorIgual. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Boolean r= (oper1.intValue() >= oper2.intValue());
			Number resul=convertirBooleanoaEntero(r);
			push(resul);
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
		if (tamanoPila()<2) 
			throw new Exception("Error: Menor. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Boolean r= (oper1.intValue() < oper2.intValue());
			Number resul=convertirBooleanoaEntero(r);
			push(resul);
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
		if (tamanoPila()<2) 
			throw new Exception("Error: Mayor. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Boolean r= (oper1.intValue() > oper2.intValue());
			Number resul=convertirBooleanoaEntero(r);
			push(resul);
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
		if (tamanoPila()<2) 
			throw new Exception("Error: Distinto. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Integer) pop(); 
			Number oper1=(Integer) pop();
			Boolean r= (oper1.intValue() != oper2.intValue());
			Number resul=convertirBooleanoaEntero(r);

			push(resul);
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
		if (tamanoPila()<2) 
			throw new Exception("Error: Igual. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Boolean r= (oper1.intValue() == oper2.intValue());
			Number resul=convertirBooleanoaEntero(r);

			push(resul);
		}
	}

	/**
	 * M�todo que ejecuta la instrucci�n opuesto: Extrae el valor n de la cima de la pila de ejecuci�n 
	 * y realiza la operaci�n aritm�tica -n, apilando el valor obtenido nuevamente en la pila.
	 * pila[cima] = - pila[cima];
	 * @throws Exception
	 */
	private void ejecutaCambioSigno() throws Exception {
		if (tamanoPila()<1) 
			throw new Exception("Error: Cambio de Signo. La pila no contiene operandos suficientes.");
		else {
			Number oper1=(Number) pop();
			Number resul= (-oper1.intValue());
			push(resul);
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
		if (tamanoPila()<2) 
			throw new Exception("Error: M�dulo. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Number resul= (oper1.intValue() % oper2.intValue());
			push(resul);
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
		if (tamanoPila()<2) 
			throw new Exception("Error: Division. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Number resul= (oper1.intValue() / oper2.intValue()); 
			push(resul);
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
		if (tamanoPila()<2) 
			throw new Exception("Error: Multiplicacion. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Number resul= (oper1.intValue() * oper2.intValue());
			push(resul);
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
		if (tamanoPila()<2) 
			throw new Exception("Error: Resta. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Number resul= (oper1.intValue() - oper2.intValue());
			push(resul);		
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
		if (tamanoPila()<2) 
			throw new Exception("Error: Suma. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Number resul= (oper1.intValue() + oper2.intValue());
			push(resul);		
		}
	}
	
	/**
	 * M�todo que ejecuta la instrucci�n Copia: Replica el �ltimo operando de la pila.
	 * pila[cima+1] = pila[cima];
	 * @throws Exception
	 */
	private void ejecutaCopia() throws Exception {
		if (tamanoPila()<1) 
			throw new Exception("Error: Copia. La pila no contiene operandos suficientes.");
		else {
			Number oper1= pop();			
			push(oper1);		
			push(oper1);
		}
	}
	
	
	/**
	 * M�todo que ejecuta la instrucci�n IR_A: Deja el contador de programa en el punto
	 * anterior a la direcci�n pasada por par�metro, puesto que nuestra MaquinaP
	 * siempre avanza el contador de programa al terminar la ejecucion.
	 * PC = param - 1;
	 * @throws Exception
	 */
	private void ejecutaIrA(Number param) throws Exception {
		program_counter = param.intValue() -1;
	}
	
	
	/**
	 * M�todo que ejecuta la instrucci�n IR_V: Deja el contador de programa en el punto
	 * anterior a la direcci�n pasada por par�metro, siempre que en la cima de la pila 
	 * est� el valor 1,puesto que nuestra MaquinaP siempre avanza el contador de 
	 * programa al terminar la ejecucion, en caso contrario no modifica el contador de programa.
	 * @throws Exception 
	 */
	private void ejecutaIrV(Number param) throws Exception {
		if (pop().intValue() == 1)program_counter = param.intValue() - 1;
	}		
	
	
	/**
	 * M�todo que ejecuta la instrucci�n IR_F: Deja el contador de programa en el punto
	 * anterior a la direcci�n pasada por par�metro, siempre que en la cima de la pila 
	 * est� el valor 0,puesto que nuestra MaquinaP siempre avanza el contador de 
	 * programa al terminar la ejecucion, en caso contrario no modifica el contador de programa.
	 * @throws Exception 
	 */
	private void ejecutaIrF(Number param) throws Exception {
		if (pop().intValue() == 0)program_counter = param.intValue() - 1;
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
	 * separa el tipo de la instrucci�n de sus dos posibles par�metros.
	 * @param lectura Una linea que contiene una instrucci�n completa.
	 */
	private void divide(String lectura) 
	{
		int longitud = lectura.length();
		int parentesis1 = lectura.indexOf('(');
		int parentesis2 = lectura.lastIndexOf('(');
		if (parentesis1 == -1) //No ha encontrado par�ntesis (la linea es una operacion sin par�metros).
		{
			funcion = lectura.substring(0,longitud - 1);
			datos1 = 0;
			datos2 = 0;
		}
		// Al menos existe un parametro
		else{	
			funcion = lectura.substring(0, parentesis1);
			if (parentesis1 == parentesis2){ /* Solo existe un
			parentesis, por tanto un �nico par�metro.*/
				
				//	Try & Catch para detectar cuando es una instrucci�n sin parametros, del tipo de suma; o iguales;
				try 
				{	
					datos1 = Integer.parseInt(lectura.substring(parentesis1 + 1, longitud - 2));
				} 
				catch (NumberFormatException e) 
				{
					datos1 = 0;
				}
				datos2=0;
			}
			else{ // Existen dos paramtros
				try 
				{	
					datos1 = Integer.parseInt(lectura.substring(parentesis1 + 1, parentesis2 - 1));
				} 
				catch (NumberFormatException e) 
				{
					datos1 = 0;
				}

				try 
				{	
					datos2 = Integer.parseInt(lectura.substring(parentesis2 + 1, longitud - 2));
				} 
				catch (NumberFormatException e) 
				{
					datos2 = 0;
				}				
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
	 * M�todo auxiliar para calcular direcciones base, retrocede en los
	 * registros de activacion la cantidad de niveles indicada por el
	 * parametro, hasta conseguir la direccion buscada;
	 * @param niveles Numero de niveles a retroceder.
	 * @return Int - Direccion base buscada
	 */
	private int extraeDirBase(int niveles){
		int base = B;
		for(;niveles > 0;niveles --){
			base = mem_datos[base].intValue();
		}
		return base;
	}
	
	
	/**
	 * M�todo auxiliar, sirve para colocar un elemento en la cima de la pila,
	 * realmente, debido a que la pila de operandos y la de registros de
	 * activacion es la misma, coloca el dato en la direccion de memoria apuntado
	 * por C, e incrementa C en una posicion.
	 * @param param Valor que se colocar� en la cima de la pila.
	 */
	private void push(Number param){
		mem_datos[C] = param;
		C++;
	}
	
	/**
	 * M�todo auxiliar, sirve para extraer el elemento en la cima de la pila,
	 * realmente, debido a que la pila de operandos y la de registros de
	 * activacion es la misma, extrae el dato de la direccion de memoria previa a la
	 * apuntada por C, decrementando C en una posicion.
	 * @return Number - Valor en la cima.
	 */
	private Number pop(){
		C--;
		Number n = mem_datos[C];
		return n;
	}
	
	/**
	 * M�todo que devuelve el n�mero de operandos en la pila, es decir el
	 * n�mero de posiciones de memoria ocupada en marco actual a partir del
	 * registro de activaci�n.
	 * @return N�mero de posiciones en la pila
	 */
	private int tamanoPila(){
		int tam = C - (B + 3);
		if (tam >= 0) return tam;
		else return 0;
	}
	
	/**
	 * M�todo auxiliar indica mediante un Booleano si tamanoPila == 0
	 * @return Boolean - No hay ning�n operando en la pila.
	 */
	private boolean pilaIsEmpty(){
		return (tamanoPila() == 0);
	}

	/**
	 * M�todo que devuelve un String con la informaci�n de la pila de ejecuci�n, posici�n y valor, utilizando
	 * para ello una pila auxiliar.  
	 * @return Informaci�n de la pila de ejecuci�n
	 */
	public String muestraInfoPila(){
		Stack<Number> pilaEjecucionAux = new Stack<Number>();
		System.out.println("B = " + B);
		System.out.println("C = " + C);
		System.out.println("H = " + H);
		String pilaInfo="N�mero de operandos en la pila de ejecuci�n: ";
		int pilanum=(tamanoPila());
		pilaInfo= pilaInfo.concat(""+pilanum);

		if (!pilaIsEmpty()) {
			pilaInfo=pilaInfo.concat(" operandos");
			while(!pilaIsEmpty()){
				Number elem = pop();
				pilaInfo= pilaInfo.concat("\n"+"Operando "+pilanum+": ");
				if (elem != null)
					pilaInfo= pilaInfo.concat(elem.toString());
				else
					pilaInfo = "-----------------";
				pilanum--;
				pilaEjecucionAux.push(elem);
			}

			while(!pilaEjecucionAux.isEmpty()){
				Number elem = pilaEjecucionAux.pop();
				push(elem);
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
