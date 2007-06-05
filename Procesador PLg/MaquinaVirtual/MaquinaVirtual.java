package MaquinaVirtual;

import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

/**
 * Clase encargada de la ejecución de las instrucciones generadas por el compilador a partir de 
 * un fichero que las contiene.
 * Consta de dos registros datos1 y datos2 para los parametros de las instrucciones.
 * Consta de una memoria de instrucciones y una única memoria de datos, tanto para las variables estaticas
 * como para las pilas de registros de activación y de operandos. Dichas pilas se tratan como una sóla.
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
	 * Define el tamaño de la memoria de datos
	 */
	public static final int TamanoMem = 10000;
	
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
	 * instrucción, el primer parametro inicial. 
	 */
	Number datos1;
	
	/**
	 * Variable que se utilizará para extraer del archivo que contiene el programa compilado, en cada 
	 * instrucción, el segundo parametro inicial. 
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
	 * Registro que indica cual es la primera posición de memoria libre en el
	 * bloque de memoria dinamica. 
	 */
	private int H;
	
	/**
	 * Registro que apunta a la base del registro de activación activo,
	 * es decir, a la base de direcciones relativas en la memoria de datos.
	 */
	private int B;
	
	/**
	 * Registro que apunta a la cima de la pila de la memoria.
	 */
	private int C;

	/**
	 * Constructor por defecto de la máquina virtual, que crea la memoria dinámica necesaria e inicializa
	 * los parámetros de la clase colocándose en la primera instrucción a leer (el PC se inicializa a 0). 
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
			mem_instrucciones.add(new Instruccion(funcion, datos1,datos2));
		}
		
		divide(lectura);
		mem_instrucciones.add(new Instruccion(funcion, datos1,datos2));
		
		while (program_counter < mem_instrucciones.size() && estadoMaquina==0)
		{		
			funcion = mem_instrucciones.get(program_counter).getOperacion();
			datos1 = mem_instrucciones.get(program_counter).getParametros1();
			datos2 = mem_instrucciones.get(program_counter).getParametros2();
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
		if (estadoMaquina == 2) System.out.println("La maquina ha finalizado con un error");
		
		if (estadoMaquina == 1) System.out.println("Lectura y Ejecución finalizadas con éxito.\n");
		
		if (estadoMaquina == 0) System.out.println("La maquina ha finalizado " +
				"debido a no disponer de más instrucciones, pero no ha encontrado " +
				"instruccion de parada.");
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
		for (int i=0;i<C;i++){
			System.out.print("   Direccion de memoria: " + i + " -> ");
			System.out.println("Valor: " + mem_datos[i].intValue() + ".");
		}
		for (int i=InicioH;i<H;i++){
			System.out.print("   Direccion de memoria: " + i + " -> ");
			System.out.println("Valor: " + mem_datos[i].intValue() + ".");
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
			System.out.println("Máquina pasa a estado error");
		}

	}

	/**
	 * Método que incrementa el valor de la cima de la pila en el número
	 * de posiciones de memoria pasadas por parámetro.
	 * @param param Número de posiciones de memoria a incrementar el valor
	 * de la cima de la pila.
	 * @throws Exception 
	 */
	private void ejecutaIncrementaC(Number param) throws Exception {
		if (C + param.intValue() > InicioH) 
			throw new Exception("Error.IncrementaH : Intenta llevar las pilas más allá del" +
					" comienzo de la memoria dínamica.");
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
	 * Método que se utiliza de ejecutar la llamada a un procedimiento,
	 * para ello crea su registro de activacion, con la Instruccion Siguiente,
	 * el Enlace Dinamico y el Enlace Estatico.
	 * 
	 * @param fnp Indica el nivel del procedimiento, servirá para reconstruir
	 * su entorno léxico.
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
	 * Método que incrementa el valor del registro H en el
	 * número de posiciones pasadas como parametro.
	 * @param param Número de posiciones en que se incrementará el valor
	 * del registro H.
	 */
	private void ejecutaIncrementaH(Number param) {		
		H += param.intValue();
	}

	/**
	 * Método que coloca sobre la cima de la pila el valor del
	 * registro H.
	 *
	 */
	private void ejecutaApilaH() {		
		push(H);
	}

	/**
	 * Funcion que coloca el valor de la cima de la pila sobre la
	 * direccion de memoria especificada por los dos siguientes valores
	 * en la pila, donde la subcima indica el número de niveles a retroceder
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
	 * desechando a partir de ahí esa cima.
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
	 * Método que ejecuta la instruccion retorno de subrutina, para
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
	 * Método que ejecuta la instrucción apila(n) en la máquina virtual, que coloca el valor n 
	 * en la cima de la pila de ejecución.
	 * @param param Valor numérico a apilar.
	 * @throws Exception
	 */
	private void ejecutaApila(Number param) throws Exception {
		push(param);
	}
	
	/**
	 * Método que ejecuta la instrucción apila-dir(param): Coloca el valor de la variable de dirección n 
	 * en la cima de la pila de ejecución.
	 * @param param Dirección de la variable, la cual ha sido definida durante el proceso de compilación.
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
	 * Método que ejecuta la instrucción desapila(): Únicamente extrae el valor de la cima de la pila.
	 * @throws Exception
	 */
	private void ejecutaDesapila() throws Exception {
		if (tamanoPila()<1)throw new Exception("Error: Desapila. La pila no contiene operandos suficientes.");
		else pop();
	}
	
	
	/**
	 * Método que ejecuta la instrucción desapila-dir(param): Coloca el valor de la cima de la pila de
	 * ejecución sobre la variable una direccion de memoria. Dicha direccion
	 * de memoria se obtiene, retrocediento tantos niveles de registros de
	 * activación como nos indique fnv, teniendo esa base se le suma param, para
	 * obtener la obtener la direccion de memoria a la que debemos acceder para llegar a la
	 * vaariable en cuestion.  
	 * @param fnv Número de niveles de indireccion a aplicar, partiendo de B, para conocer la
	 * direccion base.
	 * @param param Número de posiciones de memoria que habra que sumarle
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
	 * Cambia el estado de la máquina virtual a parado (valor 1)
	 * @throws Exception
	 */
	private void ejecutaStop() throws Exception{
		estadoMaquina=1; //Pasa a parada, luego dar más información.		
	}

	/**
	 * Método que ejecuta la instrucción negacion: Extrae el valor de la cima de la pila (booleano representado
	 * como 0 o 1) y realiza la operación lógica not, apilando el resultado nuevamente en la pila.
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
	 * Método que ejecuta la instrucción or: Extrae los 2 últimos operandos de la pila (booleanos representados
	 * como 0 o 1) y realiza la operación lógica or, apilando el resultado nuevamente en la pila.
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
	 * Método que ejecuta la instrucción and: Extrae los 2 últimos operandos de la pila (booleanos representados
	 * como 0 o 1) y realiza la operación lógica and, apilando el resultado nuevamente en la pila.
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
	 * Método que ejecuta la instrucción menoroigual: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación <=, apilando el resultado booleano obtenido
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
	 * Método que ejecuta la instrucción mayoroigual: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación >=, apilando el resultado booleano obtenido
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
	 * Método que ejecuta la instrucción menor: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación <, apilando el resultado booleano obtenido
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
	 * Método que ejecuta la instrucción mayor: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación >, apilando el resultado booleano obtenido
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
	 * Método que ejecuta la instrucción distintos: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación !=, es decir, si los 2 números analizados
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
	 * Método que ejecuta la instrucción iguales: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación de comparación ==, es decir, si los 2 números analizados
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
	 * Método que ejecuta la instrucción opuesto: Extrae el valor n de la cima de la pila de ejecución 
	 * y realiza la operación aritmética -n, apilando el valor obtenido nuevamente en la pila.
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
	 * Método que ejecuta la instrucción modulo: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación aritmética módulo (%), apilando el valor obtenido 
	 * nuevamente en la pila.
	 * pila[cima-1] = pila[cima-1] % pila[cima];
	 * @throws Exception
	 */
	private void ejecutaMod() throws Exception {
		if (tamanoPila()<2) 
			throw new Exception("Error: Módulo. La pila no contiene operandos suficientes.");
		else {
			Number oper2=(Number) pop(); 
			Number oper1=(Number) pop();
			Number resul= (oper1.intValue() % oper2.intValue());
			push(resul);
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
	 * Método que ejecuta la instrucción multiplica: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación aritmética multiplicación, apilando el valor obtenido 
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
	 * Método que ejecuta la instrucción resta: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación aritmética resta, apilando el valor obtenido 
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
	 * Método que ejecuta la instrucción suma: Extrae los 2 últimos operandos de la pila 
	 * (numéricos) y realiza la operación aritmética suma, apilando el valor obtenido 
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
	 * Método que ejecuta la instrucción Copia: Replica el último operando de la pila.
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
	 * Método que ejecuta la instrucción IR_A: Deja el contador de programa en el punto
	 * anterior a la dirección pasada por parámetro, puesto que nuestra MaquinaP
	 * siempre avanza el contador de programa al terminar la ejecucion.
	 * PC = param - 1;
	 * @throws Exception
	 */
	private void ejecutaIrA(Number param) throws Exception {
		program_counter = param.intValue() -1;
	}
	
	
	/**
	 * Método que ejecuta la instrucción IR_V: Deja el contador de programa en el punto
	 * anterior a la dirección pasada por parámetro, siempre que en la cima de la pila 
	 * esté el valor 1,puesto que nuestra MaquinaP siempre avanza el contador de 
	 * programa al terminar la ejecucion, en caso contrario no modifica el contador de programa.
	 * @throws Exception 
	 */
	private void ejecutaIrV(Number param) throws Exception {
		if (pop().intValue() == 1)program_counter = param.intValue() - 1;
	}		
	
	
	/**
	 * Método que ejecuta la instrucción IR_F: Deja el contador de programa en el punto
	 * anterior a la dirección pasada por parámetro, siempre que en la cima de la pila 
	 * esté el valor 0,puesto que nuestra MaquinaP siempre avanza el contador de 
	 * programa al terminar la ejecucion, en caso contrario no modifica el contador de programa.
	 * @throws Exception 
	 */
	private void ejecutaIrF(Number param) throws Exception {
		if (pop().intValue() == 0)program_counter = param.intValue() - 1;
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
	 * separa el tipo de la instrucción de sus dos posibles parámetros.
	 * @param lectura Una linea que contiene una instrucción completa.
	 */
	private void divide(String lectura) 
	{
		int longitud = lectura.length();
		int parentesis1 = lectura.indexOf('(');
		int parentesis2 = lectura.lastIndexOf('(');
		if (parentesis1 == -1) //No ha encontrado paréntesis (la linea es una operacion sin parámetros).
		{
			funcion = lectura.substring(0,longitud - 1);
			datos1 = 0;
			datos2 = 0;
		}
		// Al menos existe un parametro
		else{	
			funcion = lectura.substring(0, parentesis1);
			if (parentesis1 == parentesis2){ /* Solo existe un
			parentesis, por tanto un único parámetro.*/
				
				//	Try & Catch para detectar cuando es una instrucción sin parametros, del tipo de suma; o iguales;
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
	 * Método auxiliar para calcular direcciones base, retrocede en los
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
	 * Método auxiliar, sirve para colocar un elemento en la cima de la pila,
	 * realmente, debido a que la pila de operandos y la de registros de
	 * activacion es la misma, coloca el dato en la direccion de memoria apuntado
	 * por C, e incrementa C en una posicion.
	 * @param param Valor que se colocará en la cima de la pila.
	 */
	private void push(Number param){
		mem_datos[C] = param;
		C++;
	}
	
	/**
	 * Método auxiliar, sirve para extraer el elemento en la cima de la pila,
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
	 * Método que devuelve el número de operandos en la pila, es decir el
	 * número de posiciones de memoria ocupada en marco actual a partir del
	 * registro de activación.
	 * @return Número de posiciones en la pila
	 */
	private int tamanoPila(){
		int tam = C - (B + 3);
		if (tam >= 0) return tam;
		else return 0;
	}
	
	/**
	 * Método auxiliar indica mediante un Booleano si tamanoPila == 0
	 * @return Boolean - No hay ningún operando en la pila.
	 */
	private boolean pilaIsEmpty(){
		return (tamanoPila() == 0);
	}

	/**
	 * Método que devuelve un String con la información de la pila de ejecución, posición y valor, utilizando
	 * para ello una pila auxiliar.  
	 * @return Información de la pila de ejecución
	 */
	public String muestraInfoPila(){
		Stack<Number> pilaEjecucionAux = new Stack<Number>();
		System.out.println("B = " + B);
		System.out.println("C = " + C);
		System.out.println("H = " + H);
		String pilaInfo="Número de operandos en la pila de ejecución: ";
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
			pilaInfo= pilaInfo.concat(" operandos. Pila vacía\n");
		}
		pilaInfo=pilaInfo.concat("\n---------------------------------------------------\n");
		return pilaInfo;
	}

}
