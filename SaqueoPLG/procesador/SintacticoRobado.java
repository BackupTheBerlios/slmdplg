package procesador;

import java.io.RandomAccessFile;
import java.util.Vector;
import maquinaP.Codigo;
import tablaSimbolos.TablaSimbolos;
import tablaSimbolos.Par;
import tablaSimbolos.Atributos;

/**
 * La clase <B>Sintactico</B> analiza los tokens que han sido reconocidos por <B>Lexico</B>. 
 * <P>La clase Sintactico cuenta con los siguientes atributos:
 * <UL><LI><CODE>codigo:</CODE> Se encarga de almacenar el cdigo generado por las instrucciones del lenguaje. De tipo Codigo, clase
 * incluida en el paquete <B>maquinaP</B>.</LI>
 * <LI><CODE>lexico:</CODE> Analiza el fichero de entrada para reconocer tokens. De tipo Lexico.</LI>
 * <LI><CODE>TS:</CODE> Tabla de Simbolos que vamos a utilizar en el analisis del fichero, para almacenar los simbolos. De tipo TablaSimbolos.</LI>
 * <LI><CODE>dir:</CODE> Entero que marca la posicin de la pila con la que estamos trabajando. De tipo Entero.</LI>
 * </UL></P>
 * 
 * @author Paloma de la Fuente, Jonas Andradas, Leticia Garcia y Silvia Martin
 *
 */

public class SintacticoRobado{
	
	/*
	 * Atributos de la clase:
	 * 
	 * codigo: Se encarga de almacenar el codigo generado por las instrucciones del lenguaje.
	 * lexico: Analiza el fichero de entrada para reconocer tokens.
 	 * TS: Tabla de Simbolos que vamos a utilizar en el analisis del fichero, para almacenar los simbolos.
 	 * dir: Entero que marca la posicin de la pila con la que estamos trabajando.
	 */
	Codigo codigo;
	Lexico lexico;
	TablaSimbolos TS;
	int dir;
	int etq;
	int nivel;
	int nmax;
	
	private static int longApilaRet = 5;
	//private static int longPrologo = 13;
	private static int longPrologo = 2;
	private static int longEpilogo = 8;
	//private static int longEpilogo = 12;
	private static int longInicioPaso = 3;
	private static int longFinPaso = 1;
	//private static int longAccesoVar = 4;
	private static int longInicio = 2;
	private static int longPasoParametro = 2;
	/**
	 * Constructor que inicializa los atributos con los datos que recibe por parametro.
	 * 
	 * @param fuente RandomAccessFile que se utiliza para leer del fichero que contiene el cdigo a analizar.
	 * @param T Tabla de Simbolos que vamos a utilizar en el analisis del fichero, para almacenar los simbolos.
	 * @param f String donde se guarga la ruta del fichero donde se va a guardar el codigo generado por el compilador.
	 * @throws Exception Propaga una excepcion que haya sucedido en otro lugar.
	 */
	public SintacticoRobado(RandomAccessFile fuente, TablaSimbolos T, String f) throws Exception{
		codigo = new Codigo(f);
		lexico = new Lexico(fuente);		
		TS = T;
	}

	/**
	 * Accesor para el atributo de la clase sintactico. 
	 * @return Devuelve el codigo generado hasta ese momento
	 */
	public Codigo getCodigo() {
		return codigo;
	}

	/**
	 * Mutador para el atributo de la clase sintactico. 
	 * @param codigo Recibe el nuevo valor del codigo que se ha generado
	 */
	public void setCodigo(Codigo codigo) {
		this.codigo = codigo;
	}

	/**
	 * Comienza el analisis sintactico del fichero que queremos analizar. Cuando acaba muestra el codigo que ha reconocido.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public void startParsing() throws Exception{
		if (Prog()){
			throw new Exception("El programa contiene errores de tipo");
		}
		//TS.muestra();
		codigo.muestraCodigo();
	}

	/**
	 * Evalua el programa.  Primero lee las declaraciones de variables (identificadores), que se encuentran
	 * separados del conjunto de instrucciones "Is" mediante un "#".  Acto seguido, procesa cada instruccisn de Is.
	 * 
	 * @return errDeProg Devuelve un booleano que indica si existio un error al analizar el codigo del Programa. 
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */	
	public boolean Prog() throws Exception{
		etq = 0;
		//System.out.println("etq: " + etq);
		dir = 2;
		nivel = 0;
		int etqs2=etq;
		////System.out.println("etqs2: " + etqs2);
		codigo.inicio();
		etq = etq + longInicio;
		int etqs = etq;
		//System.out.println("etqs: " + etqs);
		codigo.genIns("ir-a");
		etq ++;
		//System.out.println("etq: " + etq);
		Par atrDeDecs = Decs();
		TS.muestra();
		//System.out.println("Tras decs etq: " + etq);
		codigo.parchea(etqs2,dir+1); // como es tamDatos+2 y tamDatos= dir-1
		codigo.parchea(etqs,etq);
		//System.out.println("Termino decs");
		Par atrDeIs = Is();
		boolean errDeProg = atrDeDecs.getProps().getTipo().equals("error") || atrDeIs.getProps().getTipo().equals("error"); 
		return errDeProg;
	}
	
	/**
	 * Recorre el conjunto de declaraciones (Dec) una por una.  Si tras una declaracion encontramos
	 * un punto y coma (";"), procesamos otra mas.  Si en cambio lo que encontramos es una almohadilla
	 * ("#"), dejamos de leer Decs.
	 * 
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par Decs() throws Exception{
		Par a = new Par();
		////System.out.println("Estoy en Decs y llamo a dec");
		Par atrDeDec = Dec(); 
		////System.out.println("A?ado");
		if (!atrDeDec.getClase().equals("proc")){
			TS.agnadeID(atrDeDec.getId(), atrDeDec.getProps(), atrDeDec.getClase(), atrDeDec.getDir(),atrDeDec.getNivel());
		}
		else{
			TS.agnadeID(atrDeDec.getId(), atrDeDec.getProps(), atrDeDec.getClase(), atrDeDec.getDir(),atrDeDec.getNivel(),atrDeDec.getT());
		}
		//System.out.println("La TS que tengo despues de aadir en Decs, aado: " + atrDeDec.getId());
		//TS.muestra();
		if (atrDeDec.getClase().equals("var")){
			dir = dir + atrDeDec.getProps().getTam();
		}
		if (atrDeDec.getProps().getTipo().equals("error")){
			a.getProps().setTipo("error");
		}
		Token tk = lexico.getNextToken();
		if (tk.equals(new Token("#", Tipos.TKCUA))){
			lexico.lexer(); //consumimos #
			return a;
		}
		else{
			if (tk.equals(new Token(";", Tipos.TKPYCOMA))){
				lexico.lexer(); //consumimos ;
				Par atrDeDecs = Decs();
				if (atrDeDecs.getProps().getTipo().equals("error")){
					a.getProps().setTipo("error");
				}
				return a;
			}
			else{
				a.getProps().setTipo("error");
				return a;
			}
		}
			
    }
	
	/**
	 * 
	 * @return Par
	 * @throws Exception
	 */
	public Par Dec() throws Exception{
		Par a = new Par();
		Token tk = lexico.getNextToken();
		if (tk.equals(new Token("tipo", Tipos.TKTIPO))){
			Par atrDeDecTipo = DecTipo();
			a.setId(atrDeDecTipo.getId());
			a.setProps(atrDeDecTipo.getProps());
			a.setClase("tipo");
			a.setDir(0);
			return a;
		}
		else if (tk.equals(new Token("proc", Tipos.TKPROC))){
			Par atrDeDecProc = DecProc();
			a.setId(atrDeDecProc.getId());
			a.setProps(atrDeDecProc.getProps());
			a.setClase("proc");
			a.setDir(0);
			a.setNivel(atrDeDecProc.getNivel());
			a.setT(atrDeDecProc.getT());
			return a;
		}
		else{	
			Par atrDeDecVar = DecVar();
			a.setId(atrDeDecVar.getId());
			a.setProps(atrDeDecVar.getProps());
			a.setClase("var");
			a.setDir(dir);
			return a;
		}
	}
	
	public Par DecProc() throws Exception{
		Par a = new Par();
		Token tk = lexico.lexer(); // consumimos "proc"
		if (! lexico.reconoce(Tipos.TKPROC)){
			throw new Exception("ERROR: Deberiamos haber encontrado 'proc'.");
		}
		tk = lexico.lexer(); // Consumimos el iden
		if (!lexico.reconoce(Tipos.TKIDEN)){
			throw new Exception ("ERROR: Necesitas un identificador");
		}
		a.setId(tk.getLexema());
		a.setNivel(nivel);
		nivel ++;
		if (nivel>nmax){
			nmax=nivel;
		}
		Par atrDeFParams = FParams();
		//System.out.println("La TS padre: ");
		//TS.muestra();
		TablaSimbolos tsAux = new TablaSimbolos(TS.getTabla());
		System.out.println(" Esto es un proc ");
		//TS.muestra();
		
		TS = new TablaSimbolos(TS.getTabla(), atrDeFParams.getProps().getParams());
		a.setT(TS);
		//System.out.println("La TS hijo: ");
		//TS.muestra();
		a.getProps().setTam(0);
		a.getProps().setElems(atrDeFParams.getProps().getElems());
		a.getProps().setParams(atrDeFParams.getProps().getParams());
		a.setNivel(nivel);
		tk = lexico.lexer();
		if (!lexico.reconoce(Tipos.TKLLAP)){
			throw new Exception("ERROR: Falta una llave de apertura");
		}
		a.setDir(etq);
		a.setClase("proc");
		TS.agnadeID(a.getId(), a.getProps(),a.getClase(), a.getDir(),a.getNivel());
		//System.out.println("La TS hijo, creo despues de codigo Palo: ");
		//TS.muestra();
		//System.out.println("La dir de instrucciones que teno ahora es: "+ etq);
		//FinCodigo de Palo
		Bloque();
		//System.out.println("La dir de instrucciones que tenodespues de bloque es: "+ etq);
		//System.out.println("La TS hijo despues de bloque: ");
		//TS.muestra();
		a.setT(TS);
		if (!lexico.reconoce(Tipos.TKLLCI)){
			throw new Exception("ERROR: Falta una llave de cierre");
		}
		nivel --;
		TS = tsAux;
		//System.out.println("La TS padre despues de bloque: ");
		//TS.muestra();
		return a;
	}
	
	public Par DecTipo() throws Exception{
		Par a = new Par();
		lexico.lexer(); // consumimos tipo
		Token tk = lexico.lexer();
		if (!lexico.reconoce(Tipos.TKIDEN)){
			throw new Exception ("ERROR: Necesitas un identificador");
		}
		a.setId(tk.getLexema());
		lexico.lexer(); //consumimos =
		if (!lexico.reconoce(Tipos.TKIG)){
			throw new Exception ("ERROR: Necesitas un =");
		}
		Par atrDeTipo = Tipo();
		a.getProps().setTam(atrDeTipo.getProps().getTam());
		a.getProps().setElems(atrDeTipo.getProps().getElems());
		a.getProps().setTipo("ref");
		a.getProps().setTbase(atrDeTipo.getProps());
		return a;
	}	
	
	/**
	 * Procesa una declaracion de variable.  Cada declaracion Dec consta de dos elementos:  El tipo de la variable
	 * y su nombre, de la forma: 
	 * 			tipo identificador;
	 * 
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par DecVar() throws Exception{
		/*
		 * DecVar.pend = Tipo.pend
		 */
		Par a = new Par();
		Par atrDeTipo = Tipo();
		Token tk = lexico.lexer();
		if (!lexico.reconoce(Tipos.TKIDEN)){
			throw new Exception ("ERROR: Necesitas un identificador");
		}
		a.setId(tk.getLexema());
		a.getProps().setElems(atrDeTipo.getProps().getElems());
		a.getProps().setTam(atrDeTipo.getProps().getTam());
		a.getProps().setTipo(atrDeTipo.getProps().getTipo());
		a.getProps().setTbase(atrDeTipo.getProps().getTbase());
		
		if (TS.existeID(tk.getLexema()) || TS.referenciaErronea(atrDeTipo)){
			a.getProps().setTipo("error");
		}
		return a;
	}	
	
	public Par Tipo() throws Exception{
		Par a = new Par();
		Token tk = lexico.lexer(); // cosumimos int, bool, iden, array o pointer
		if (lexico.reconoce(Tipos.TKINT) || lexico.reconoce(Tipos.TKBOOL)){
			a.getProps().setTipo(tk.getLexema());
			a.getProps().setTam(1);
			a.getProps().setElems(1);
			a.getProps().setTbase(null);
		}
		else if(lexico.reconoce(Tipos.TKIDEN)){
			a.setId(tk.getLexema());
			a.getProps().setTipo("ref");
			a.getProps().setTam(TS.getProps(tk.getLexema()).getTam());
			a.getProps().setElems(TS.getProps(tk.getLexema()).getElems());
			a.getProps().setTbase(new Atributos(tk.getLexema(),"",0,1, new Vector()));
		}
		else if(lexico.reconoce(Tipos.TKARRAY)){
			lexico.lexer(); //consumimos [
			if (!lexico.reconoce(Tipos.TKCAP)){
				throw new Exception ("ERROR: Necesitas un [");
			}
			tk = lexico.lexer(); //consumimos num
			if (!lexico.reconoce(Tipos.TKNUM)){
				throw new Exception ("ERROR: Necesitas un numero");
			}
			int n = Integer.parseInt(tk.getLexema());
			lexico.lexer(); //consumimos ]
			if (!lexico.reconoce(Tipos.TKCCI)){
				throw new Exception ("ERROR: Necesitas un ]");
			}
			lexico.lexer(); //consumimos of
			if (!lexico.reconoce(Tipos.TKOF)){
				throw new Exception ("ERROR: Necesitas un of");
			}
			Par atrDeTipo = Tipo();
			a.getProps().setTipo("array");
			a.getProps().setElems(n);
			a.getProps().setTam(atrDeTipo.getProps().getTam() * n);
			a.getProps().setTbase(atrDeTipo.getProps());
		}
		else if(lexico.reconoce(Tipos.TKPUNT)){
			Par atrDeTipo = Tipo();
			if (a.getProps().getTipo().equals("error")){
				a.getProps().setTipo("error");
			}
			a.getProps().setTipo("pointer");
			a.getProps().setTbase(atrDeTipo.getProps());
			a.getProps().setElems(atrDeTipo.getProps().getElems());
			a.getProps().setTam(atrDeTipo.getProps().getTam() * a.getProps().getElems());
		}
		return a;
	}
	
	public Par FParams() throws Exception{
		Par a  = new Par();
		a.setT(new TablaSimbolos());
		Token tk = lexico.lexer();//consumimos ( 
		if (!lexico.reconoce(Tipos.TKPAP)){
			throw new Exception ("ERROR: Necesitas un (");
		}
		tk = lexico.getNextToken();
		if (tk.equals(new Token(")", Tipos.TKPCI))){
			lexico.lexer();
			a.getProps().setElems(0);
			return a;
		}
		Par atrDeLFParams = LFParams();
		tk = lexico.lexer(); //consumimos )
		if (!lexico.reconoce(Tipos.TKPCI)){
			throw new Exception ("ERROR: Necesitas un )");
		}
		a.getProps().setParams(atrDeLFParams.getProps().getParams());
		a.getProps().setElems(atrDeLFParams.getProps().getElems());
		return a;
	}
	/*
	 * AParams ::= LAParams
cod = iniciopaso
|| genIns(LAParams) || finpaso
etq = etq + longInicioPaso
etq = etq + longFinPaso
AParams ::= ??
AParams.props.i = 0
	 */
	public Par AParams() throws Exception{
		Par a  = new Par();
		//codigo.inicio_paso();
		//etq = etq + longInicioPaso;
		Token tk = lexico.lexer();//consumimos ( 
		if (!lexico.reconoce(Tipos.TKPAP)){
			throw new Exception ("ERROR: Necesitas un (");
		}
		tk = lexico.getNextToken();
		//System.out.println("El Token en AParams es: " + tk.getLexema());
		if (tk.equals(new Token(")", Tipos.TKPCI))){
			lexico.lexer();
			a.getProps().setElems(0);
			return a;
		}
		Par atrDeLAParams = LAParams();
		tk = lexico.getLookahead(); // leemos )
		//System.out.println("Volvemos de LAParams, y estamos en AParams. Leemos: " + tk.getLexema());
		if (!lexico.reconoce(Tipos.TKPCI)){
			throw new Exception ("ERROR: Necesitas un )");
		}
		tk = lexico.lexer(); // Consumimos )
		//System.out.println("Y unas l?neas m?s abajo, leemos: " + tk.getLexema());
		//codigo.fin_paso();
		//etq = etq + longFinPaso;
		return atrDeLAParams;
	}
	
	
	public Par LFParams() throws Exception{
		Par a = new Par();
		a.setT(new TablaSimbolos())	;
		Par atrDeLFParams = null;
		
		Par atrDeFParam = FParam();
		a.getProps().getParams().add(atrDeFParam);
		a.getProps().setElems(a.getProps().getElems() + 1);
		
		Token tk = lexico.getNextToken();
		if (tk.getCategoriaLexica() == Tipos.TKPCI){
			return a;
		}
		tk = lexico.lexer(); // Consumimos ","
		
		if (lexico.reconoce(Tipos.TKCOMA)){
			atrDeLFParams = LFParams();
			if (! atrDeLFParams.getProps().getParams().isEmpty()){
				a.getProps().getParams().addAll(atrDeLFParams.getProps().getParams());
				a.setT(atrDeLFParams.getT());
				a.getProps().setElems(a.getProps().getElems() + atrDeLFParams.getProps().getElems());
			}
		}
		else {
			throw new Exception("ERROR: Los parametros han de ir separados por comas.");
		}
		a.getT().agnadeID(atrDeFParam.getId(), atrDeFParam.getProps(),atrDeFParam.getClase(),atrDeFParam.getDir(),nivel);
		return a;
	}
	
	
	public Par LAParams() throws Exception{
		Par a = new Par();
		Par atrDeLAParams = null;
		Par atrDeAParam = AParam();
		a.getProps().getParams().add(atrDeAParam);
		a.getProps().setElems(a.getProps().getElems() + 1);
		System.out.println("LAParams: " + a.getProps().toString());
		Token tk = lexico.getLookahead();
		System.out.println("Llevamos leido un: " + tk.getLexema());
		if (lexico.reconoce(Tipos.TKCOMA)){
			codigo.genIns("copia");
			etq ++;
			System.out.println("Holaaaaaaaaaaaaaaaaaaaaaaaa 1");
			atrDeLAParams = LAParams(); // NO LO USAMOS PARA NADA!!
			System.out.println("Holaaaaaaaaaaaaaaaaaaaaaaaa 2");
			a.getProps().getParams().addAll(atrDeLAParams.getProps().getParams());
			System.out.println("LAParams 2: " + a.getProps().toString());
			codigo.genIns("flip");
			etq ++;
		}
		codigo.paso_parametro(atrDeAParam.getDir());
		etq += longPasoParametro;
		return a;
	}
	
	
	public Par FParam() throws Exception{
		Par a  = new Par();
		Par atrDeTipo = Tipo();
		Token tk = lexico.lexer();
		if(! lexico.reconoce(Tipos.TKIDEN)){
			throw new Exception("ERROR: falta el identificador del parametro");
		}
		a.setId(tk.getLexema());
		if (atrDeTipo.getProps() == null){
		}
		a.setProps(atrDeTipo.getProps());
		
		a.setNivel(nivel);
		return a;
	}
	
	public Par AParam() throws Exception{
		Par a  = new Par();
		a = ExpC ();
		return a;
	}
	
	public Par Bloque() throws Exception{
		Par a  = new Par();
		int etqs1;
		int inicio = etq;
		//System.out.println("inicio: " + inicio);
		//System.out.println("etq: " + etq);
		int auxDir = dir;
		////System.out.println();
		Token tk = lexico.getNextToken();
		Par atrDeDecs;
		codigo.genIns("ir-a");
		etq++;
		if(tk.getCategoriaLexica()!=Tipos.TKCUA){
			atrDeDecs = Decs();
		}
		else{
			lexico.lexer();
			atrDeDecs=new Par();
			atrDeDecs.getProps().setTipo("");
		}
		System.out.println("La TS que tengo en bloque: ");
		//TS.muestra();
		int tamlocales = dir - auxDir;
		codigo.parchea(inicio,etq);
		etqs1 = etq + longPrologo;
		//System.out.println("tamlocales: " + tamlocales);
		codigo.prologo(tamlocales);
		etq= etqs1;
		Par atrDeIs = Is();
		codigo.epilogo(nivel);
		etq = etq + longEpilogo +1;
		//System.out.println("etq: " + etq);
		codigo.genIns("ir-ind");
		dir = auxDir;
		boolean err = atrDeDecs.getProps().getTipo().equals("error") || atrDeIs.getProps().getTipo().equals("error");
		if (err){
			throw new Exception ("ERROR: porcedimietno erroneo");
		}
		return a;
	}
	/**
	 * Recorre el conjunto de Instrucciones del programa.  Cada instruccion I se separa del conjunto de 
	 * instrucciones restantes (Is) mediante un punto y coma (";").  Si encontramos el token Fin de Fichero,
	 * hemos terminado de leer instrucciones. 
	 * 
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par Is() throws Exception{
		Par atrDeIs;
		Par atrDeI; 
		Par a = new Par();
		atrDeI = I();
		if (lexico.reconoce(Tipos.TKFF)){
			a.getProps().setTipo(""); 
		}
		else if (lexico.reconoce(Tipos.TKLLCI)){
			a.getProps().setTipo(""); 
		}
		else{
			if (!lexico.reconoce(Tipos.TKPYCOMA)){
				throw new Exception("ERROR: Secuencia de Instrucciones Incorrecta. Cada instruccion ha de ir separada de la siguiente por un \";\"");
			}
			atrDeIs = Is();
			if (atrDeI.getProps().getTipo().equals("error") || atrDeIs.getProps().getTipo().equals("error")){
				a.getProps().setTipo("error");
			}
			else {
				a.getProps().setTipo("");
			}
			return a;	
		}
		return a;	
	}

	/**
	 * Procesa cada instruccion el conjunto de instrucciones del Programa.
	 * 
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par I() throws Exception{
		Par atrDeIns;
		Token t = lexico.lexer();
		//System.out.println("token "+ t.getLexema());
		if (lexico.reconoce(Tipos.TKBEG)){
			//System.out.println("Begin");
			atrDeIns = ICompuesta();
		}
		else{
			if (lexico.reconoce(Tipos.TKIF)){
				//System.out.println("If");
				atrDeIns = IIf();
			}
			else if(lexico.reconoce(Tipos.TKWHL)){
				//System.out.println("While");
					atrDeIns = IWhile();
			}
			else if (lexico.reconoce(Tipos.TKNEW)){
				//System.out.println("New");
				atrDeIns = INew();
			}
			else if (lexico.reconoce(Tipos.TKDEL)){
				//System.out.println("Del");
				atrDeIns = IDel();
			}
			else{
				Token tk = lexico.getNextToken(); 
				//System.out.println("token "+ tk.getLexema());
				if (tk.equals(new Token ("(", Tipos.TKPAP))){
					//System.out.println("ICall");
					atrDeIns = ICall();
				}
				else{
					//System.out.println("IAsig");
					atrDeIns = IAsig();
				}	
			}		
		}
		return atrDeIns;
	}


	public Par ICompuesta() throws Exception{
		Par atrDeIns = IsOpc();
		if (!lexico.reconoce(Tipos.TKEND)){
			throw new Exception("ERROR: begin sin end.  El formato correcto es \"begin ... end;\".");
		}
		if (lexico.reconoce(Tipos.TKLLCI)){
			atrDeIns.getProps().setTipo("");
			return atrDeIns;
		}
		lexico.lexer();
		if (! (lexico.reconoce(Tipos.TKPYCOMA))){
			throw new Exception("ERROR: end sin ;. El formato correcto es \"begin ... end;\".");
		}
		return atrDeIns;	
	}
	
	/**
	 * 
	 * @return PAr
	 * @throws Exception
	 */
	public Par IsOpc() throws Exception{
		Par atrDeIsOpc;
		Par atrDeI;
		Par a = new Par();
		boolean errDeIsOpc = false;
		atrDeI = I();
		if (lexico.reconoce(Tipos.TKFF)){
			throw new Exception("ERROR: begin sin end.  El formato correcto es \"begin ... end;\".");
		}
		if (lexico.reconoce(Tipos.TKLLCI)){
			a.getProps().setTipo("");
			return a;
		}
		
		if (lexico.reconoce(Tipos.TKPYCOMA)){
			Token tk;
			tk = lexico.getNextToken();
			if (tk.equals(new Token("end",Tipos.TKEND))){
				tk = lexico.lexer();
				a = atrDeI;
				return a;
			}
			else{
				atrDeIsOpc = IsOpc();
				errDeIsOpc = (atrDeI.getProps().getTipo().equals("error") || atrDeIsOpc.getProps().getTipo().equals("error"));
			}	
		}
		else{
			throw new Exception("ERROR: Secuencia de Instrucciones Incorrecta. Todo begin debe llevar end.");
		}
		if (errDeIsOpc) {
			a.getProps().setTipo("error");
		}
		else {
			a = atrDeI;
		}
		return a;	
	}
	
	public Par IIf() throws Exception{
		Par a = new Par();
		Par atrDeExpC;
		Par atrDeI;
		Par atrDePElse;
		int etqs1;
		int etqs2;
		atrDeExpC = ExpC();
		//System.out.println("Fin ExpC del If");
		if (!atrDeExpC.getProps().getTipo().equals("bool")){
			throw new Exception("ERROR: La condicion del If ha de ser una expresion booleana.");
		}
		if (lexico.reconoce(Tipos.TKLLCI)){
			a.getProps().setTipo("");
			return a;
		}
		else if (lexico.reconoce(Tipos.TKTHN)){
			codigo.emite("ir-f");
			etqs1 = etq; 
			etq ++;
			//System.out.println("etq: " + etq);
			//System.out.println("I del If");
			atrDeI = I();
			//System.out.println("Fin I del If");
			codigo.emite("ir-a");
			//System.out.println("Fin emite del If");
			etqs2 = etq;
			etq ++;
			//System.out.println("etq: " + etq);
			//System.out.println("Inicio parchea del If");
			codigo.parchea(etqs1,etq);
			//System.out.println("Fin parchea del If");
			//System.out.println("PElse del If");
			atrDePElse = PElse();
			//System.out.println("Fin PElse del If");
			codigo.parchea(etqs2,etq);
			if ( atrDeI.getProps().getTipo().equals("error") || atrDePElse.getProps().getTipo().equals("error") || atrDeExpC.getProps().getTipo().equals("error")){
				a.getProps().setTipo("error");
			}
			else {
				a.getProps().setTipo(""); 
			}
		}
		else{
			a.getProps().setTipo("error");
		}	
		return a;	
	}
	
	public Par PElse() throws Exception{
		Par atrDeIns = new Par();
		if (lexico.reconoce(Tipos.TKPYCOMA)){
			Token tk;
			tk = lexico.getNextToken();
			if (!tk.equals(new Token ("else",Tipos.TKELS))){
				atrDeIns.getProps().setTipo("");
				return atrDeIns; //terminamos con exito
			}
			tk = lexico.lexer();
			atrDeIns = I();
		}
		else{
			atrDeIns.getProps().setTipo("error");
		}	
		return atrDeIns;	
	}

	public Par IWhile() throws Exception{
		Par a = new Par();
		Par atrDeExpC;
		Par atrDeI;
		int etqb = etq;
		int etqs;
		atrDeExpC = ExpC();

		if (!atrDeExpC.getProps().getTipo().equals("bool")){
			throw new Exception("ERROR: La condicion del while ha de ser una expresion booleana.");
		}
		else {
			if (lexico.reconoce(Tipos.TKDO)){
				codigo.emite("ir-f");
				etqs = etq; 
				etq ++;
				//System.out.println("etq: " + etq);
				atrDeI = I();
				codigo.emite("ir-a " + etqb);
				etq ++;
				//System.out.println("etq: " + etq);
				codigo.parchea(etqs,etq);
			}
			else{
				a.getProps().setTipo("error");
				return a;
			}	
			if ( atrDeI.getProps().getTipo().equals("error") || atrDeExpC.getProps().getTipo().equals("error")){
				a.getProps().setTipo("error");
			}
			else {
				a.getProps().setTipo("");
			}
		}
		return a;	
	}
	
	
	public Par INew() throws Exception{
		
		Token tk = lexico.lexer(); //consumimo.s el iden
		Par a = new Par();
		a.setId(tk.getLexema());
		a.setProps(TS.getProps(tk.getLexema()));
		a.getProps().setTbase(Mem().getProps());
		if (TS.ref(a.getProps()).getTipo().equals("pointer")){
			if (a.getProps().getTipo().equals("ref")){
				codigo.genIns("new",TS.ref(a.getProps()).getTam(), dir);
			}
			else {
				codigo.genIns("new",a.getProps().getTam(), dir);
			}
			codigo.genIns("desapila-ind");
			etq += 2;
			//System.out.println("etq: " + etq);
		}
		else {
			a.getProps().setTipo("error");
		}
		tk = lexico.lexer();
		return a;
	}
	
	public Par IDel() throws Exception{
		Token tk = lexico.lexer(); //consumimo.s el iden
		Par a = new Par();
		a.setId(tk.getLexema());
		a.setProps(TS.getProps(tk.getLexema()));
		a.getProps().setTbase(Mem().getProps());
		codigo.genIns("apila-ind");
		etq ++;
		//System.out.println("etq: " + etq);
		if (TS.ref(a.getProps()).getTipo().equals("pointer")){
			if (a.getProps().getTipo().equals("ref")){
				codigo.genIns("delete",TS.ref(a.getProps()).getTam());
			}
			else {
				codigo.genIns("delete",a.getProps().getTam());
			}
			etq ++;
		}
		else {
			a.getProps().setTipo("error");
		}
		tk = lexico.lexer();	
		return a;
	}
	
	/**
	 * Procesa una instruccion de asignacion, de la forma:
	 * 
	 * 		identificador := Expresion.
	 * 
	 * Si hay un error en el formato de la instruccisn de asignacion, o si 
	 * el tipo del identificador usado no coincide con el de la expresion, 
	 * se lanza una Excepcion.
	 * 
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */

	public Par IAsig() throws Exception{
		
		Par  atrDeExpC = new Par();
		Par a = new Par();
		boolean errDeIAsig = false; 
		String lex = "";
		Token tk;
		tk = lexico.getLookahead();
		if (lexico.reconoce(Tipos.TKLLCI)){
			a.getProps().setTipo("");
			return a;
		}		
		if (lexico.reconoce(Tipos.TKIDEN)){
			lex = tk.getLexema();
			//System.out.println("Lexema: " + lex);
			if (! TS.getClase(lex).equals("var")){
				throw new Exception ("ERROR: Solo se puede asignar a variables en modo lectura - escritura");
			}
			a = Mem();
			tk = lexico.lexer(); //consumimos :=
			if (lexico.reconoce(Tipos.TKASIGN)){
				atrDeExpC = ExpC();
				boolean tiposIguales = atrDeExpC.getProps().getTipo().equals(TS.ref(a.getProps()).getTipo());
				errDeIAsig = (!(tiposIguales) || !(TS.existeID(lex)) || (atrDeExpC.getProps().getTipo().equals("error")));
				//System.out.println("errDeIAsig: " + errDeIAsig);
				if (!(TS.existeID(lex))){
					errDeIAsig = true;
					throw new Exception("ERROR: Identificador no declarado. \nEl identificador ha de estar declarado en la seccion de Declaraciones antes de que se le pueda asignar un valor.");
				}
				else{
					if (TS.compatibles(a.getProps(), new Atributos("int","",0,1, new Vector())) || TS.compatibles(a.getProps(), new Atributos("bool","",0,1, new Vector()))){
						//System.out.println("Compatibles");
							codigo.genIns("desapila-ind");
							etq ++;		
							//System.out.println("etq: " + etq);
					}
					else {
						//System.out.println("No compatibles");
						codigo.genIns("mueve",a.getProps().getTam());
						etq ++;
						//System.out.println("etq: " + etq);
					}
				}	
				a.setId(lex);
				a.getProps().setTipo(TS.getProps(lex).getTipo());
				//System.out.println("Fin IAsig");
			}
		}
		else{
			if (! (lexico.reconoce(Tipos.TKPYCOMA) || lexico.reconoce(Tipos.TKFF))){
				errDeIAsig = true;
				a.getProps().setTipo("error");
				throw new Exception("ERROR: Asignacisn Incorrecta. El formato correcto es \"identificador := Expresion;\".");
			} 
			else {
				errDeIAsig = false;
			}
		}
		if (errDeIAsig){
			a.getProps().setTipo("error");
		}
		//System.out.println("Salimos de IAsig");
		return a;
	}

	/**
	 * ICall ::= iden AParamsa
	 * cod = apilaret(
	 * etq) //Deber?? parchearse || genIns(AParams)||ira(TS[iden.lex].inicio)
	 * etq = etq + longApilaRet
	 * etq = etq + 1
	 */
	 public Par ICall() throws Exception{
		 Par a = new Par();
		 int etqs1= etq; 
		 String lex = lexico.getLookahead().getLexema(); // iden
		 codigo.apila_ret(etqs1);
		 //System.out.println("etqs1 :" + etqs1);
		 etq= etq + longApilaRet;
		 TablaSimbolos TSAux = new TablaSimbolos(TS.getTabla());
		 //System.out.println("La TS en ICall antes de cambiarla");
		 //TS.muestra();
		 //System.out.println("La TS en ICall después de cambiarla");
		 //TS.muestra(); 
		 TS = TS.getTS(lex);
		 a.setT(TS);
		 Par atrDeAParams = AParams();
		 System.out.println(" Params en icall ");
		 System.out.println(atrDeAParams.getProps());
		 System.out.println(TS.getProps(lex));
		 if (!TS.compatibles(atrDeAParams.getProps().getParams(), TS.getProps(lex).getParams())){
			 throw new Exception("ERROR: la llamada al procedimiento no es comaprtible con el procedimiento.");
		 }
		 System.out.println("Antes de llamar a parchea en el ICall ");
		 codigo.parchea (etqs1+3,etq);
		 System.out.println("Antes de llamar a TS en el ICall ");
		 System.out.println("Antes de llamar a TS en el ICall "+ lex );
		 System.out.println("La TS en ICall");
		 TS.muestra();
		 System.out.println("Antes de llamar a TS en el ICall " + " " + TS.getDir(lex));
		 codigo.genIns("ir-a", TS.getDir(lex));
		 //System.out.println("TS.getDir(lex) :" + TS.getDir(lex));
		 etq = etq + 1;
		 a.setProps(atrDeAParams.getProps());
		 //System.out.println("Terminamos el ICall");
		 a.setId(lex);
		 TS = TSAux;
		 return a;
	 }
	
	/**
	 * Procesa y desarrolla una Expresion de Comparacion, ExpC, llamando a Exp y a RExpC, 
	 * para empezar a desarrollar el arbol sintactico que reconocera la Expresion. 
	 * 
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par ExpC() throws Exception{
		Par atrDeExp;
		Par atrDeRExpC;
		//System.out.println("ExpC");
		atrDeExp = Exp();
		Par a = new Par();
		atrDeRExpC = RExpC();
		
		if ( atrDeExp.getProps().getTipo().equals(atrDeRExpC.getProps().getTipo())){
			if (atrDeExp.getProps().getTipo().equals("int"))
					a.getProps().setTipo("bool");
			else if (atrDeExp.getProps().getTipo().equals("bool"))
					a.getProps().setTipo("bool");
			else a.getProps().setTipo("error");
			a.getProps().setTam(1);
			a.getProps().setElems(1);
		}
		else{
			
			if (atrDeRExpC.getProps().getTipo().equals("")){
				a.getProps().setTipo(atrDeExp.getProps().getTipo());
				a.getProps().setTam(atrDeExp.getProps().getTam());
				a.getProps().setElems(atrDeExp.getProps().getElems());
			}else{
				a.getProps().setTipo("error");
			}
		}
		//System.out.println("Termino ExpC");
		return a;
	}
	
	/**
	 * Reconoce la segunda mitad de una Expresisn de Comparacisn de la forma:
	 * 
	 *       OpComp Exp RExpC | landa
	 * 
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par RExpC() throws Exception{
		Par atrDeExp;
		Par atrDeRExpC;
		Par a = new Par();
		//System.out.println("RExpC");
		if (lexico.reconoce(Tipos.TKFF)){
			a.getProps().setTipo("error");
			return a;
		}
		if (lexico.reconoce(Tipos.TKCOMA)){
			a.getProps().setTipo("");
			return a;
		}
		if (!lexico.reconoce(Tipos.TKPYCOMA) || !lexico.reconoce(Tipos.TKEND) || 
				! lexico.reconoce(Tipos.TKTHN) || !lexico.reconoce(Tipos.TKDO) || !lexico.reconoce(Tipos.TKPCI) ) {
			if (lexico.reconoce(Tipos.TKMAY) || lexico.reconoce(Tipos.TKMAYIG) || lexico.reconoce(Tipos.TKMEN) || lexico.reconoce(Tipos.TKMENIG) || lexico.reconoce(Tipos.TKIG) || lexico.reconoce(Tipos.TKDIF)){
				Token tk = lexico.getLookahead();
				atrDeExp = Exp();
				genOpComp(tk.getLexema());				
				atrDeRExpC = RExpC();
				if (atrDeRExpC.getProps().getTipo().equals("")){
					a.getProps().setTipo(atrDeExp.getProps().getTipo());
				}
				else {
					if ( ( (atrDeExp.getProps().getTipo().equals(atrDeRExpC.getProps().getTipo())) && (atrDeExp.getProps().getTipo().equals("bool")) ) || atrDeRExpC.getProps().getTipo().equals("")){
						a.getProps().setTipo(atrDeExp.getProps().getTipo());
					}
					else{
						a.getProps().setTipo("error");
					}
				}
				return a;
			} 
		} 
		a.getProps().setTipo("");
		//System.out.println("Termino RExpC");
		return a;
	}
	
	/**
	 * Reconoce una Expresisn, tanto aritmitica como lsgica (booleana).
	 *  
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par Exp() throws Exception{
		Par atrDeTerm;
		Par atrDeRExp;
		Par a = new Par();
		//System.out.println("Exp");
		atrDeTerm = Term();
		atrDeRExp = RExp();
		
		if ( atrDeTerm.getProps().getTipo().equals(atrDeRExp.getProps().getTipo())){
			if (atrDeTerm.getProps().getTipo().equals("int")){
					a.getProps().setTipo("int");
					a.getProps().setElems(1);
					a.getProps().setTam(1);
			}
			else if (atrDeTerm.getProps().getTipo().equals("bool")){
					a.getProps().setTipo("bool");
					a.getProps().setElems(1);
					a.getProps().setTam(1);
			}
			else a.getProps().setTipo("error");
		}
		else{
			if (atrDeRExp.getProps().getTipo().equals("")){
				a.getProps().setTipo(atrDeTerm.getProps().getTipo());
				a.getProps().setElems(atrDeTerm.getProps().getElems());
				a.getProps().setTam(atrDeTerm.getProps().getTam());
			}else{
				a.getProps().setTipo("error");
			}
		}
		
		return a;
	}
	
	/**
	 * Reconoce la segunda mitad (con el operador) de la descomposicisn de una Expresion booleana o aritmitica.
	 * 
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par RExp() throws Exception{
		Par atrDeTerm = new Par();
		Par atrDeRExp;
		Par a = new Par();
		//System.out.println("RExp");
		if (lexico.reconoce(Tipos.TKFF)){
			a.getProps().setTipo("error");
			return a;
		}
		if (lexico.getNextToken().getCategoriaLexica()== Tipos.TKCCI){
			a.getProps().setTipo("");
			return a;
		}
		if (lexico.reconoce(Tipos.TKCOMA)){
			a.getProps().setTipo("");
			return a;
		}
		if (!(lexico.reconoce(Tipos.TKPYCOMA) || lexico.reconoce(Tipos.TKMEN) ||
				lexico.reconoce(Tipos.TKMENIG) || lexico.reconoce(Tipos.TKIG) ||
				lexico.reconoce(Tipos.TKDIF) || lexico.reconoce(Tipos.TKMAYIG) ||
				lexico.reconoce(Tipos.TKMAY) || lexico.reconoce(Tipos.TKPAP) ||
				lexico.reconoce(Tipos.TKPCI) || lexico.reconoce(Tipos.TKEND) || 
				lexico.reconoce(Tipos.TKTHN) || lexico.reconoce(Tipos.TKDO) || 
				lexico.reconoce(Tipos.TKCCI)) ){
			Token tk;
			tk = lexico.getLookahead(); 
		
			boolean numerico = lexico.reconoce(Tipos.TKSUMA) || lexico.reconoce(Tipos.TKRESTA);
			boolean booleano = lexico.reconoce(Tipos.TKOR); 
		
			atrDeTerm = Term();
			if (numerico){
				genOpAd(tk.getLexema());
			} else if (booleano) {
				genOpAd("or");    
			}
			atrDeRExp = RExp();
			
			if ( (atrDeTerm.getProps().getTipo().equals("error")) || (atrDeRExp.getProps().getTipo().equals("error")) ){
				a.getProps().setTipo("error");
			} else {
				if (atrDeRExp.getProps().getTipo().equals("")){
					a.getProps().setTipo(atrDeTerm.getProps().getTipo());
				}
				else {
					if (numerico){
						if ( atrDeRExp.getProps().getTipo().equals("int") && atrDeRExp.getProps().getTipo().equals(atrDeTerm.getProps().getTipo()) ){
							a.getProps().setTipo("int");
						} else {
							a.getProps().setTipo("error");
						}
					} 
					else if (booleano){
						if ( atrDeTerm.getProps().getTipo().equals("bool") && atrDeTerm.getProps().getTipo().equals(atrDeRExp.getProps().getTipo()) ){
							a.getProps().setTipo("bool");
						} else {
							a.getProps().setTipo("error");
						}
					}
				}
			}
		} 
		else {
			a.getProps().setTipo("");
		}
		return a;
	}
	
	/**
	 * Reconoce un Termino, compuesto de un Factor y un Tirmino Recursivo:
	 *  
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par Term() throws Exception{
		Par atrDeFact;
		Par atrDeRTerm;
		Par a = new Par();
		//System.out.println("Term");
		atrDeFact = Fact();
		atrDeRTerm = RTerm();
		
		if ( atrDeFact.getProps().getTipo().compareTo(atrDeRTerm.getProps().getTipo()) == 0){
			if (atrDeFact.getProps().getTipo().compareTo("int") == 0){
					a.getProps().setTipo("int");
					a.getProps().setElems(1);
					a.getProps().setTam(1);
			}
			else if (atrDeFact.getProps().getTipo().compareTo("bool") == 0){
					a.getProps().setTipo("bool");
					a.getProps().setElems(1);
					a.getProps().setTam(1);
			}
			else a.getProps().setTipo("error");
		}
		else{
			if (atrDeRTerm.getProps().getTipo().equals("")){
				a.getProps().setTipo(atrDeFact.getProps().getTipo());
				a.getProps().setElems(atrDeFact.getProps().getElems());
				a.getProps().setTam(atrDeFact.getProps().getTam());
			}else{
				a.getProps().setTipo("error");
			}
		}
		//System.out.println("Term :" + a.toString());
		return a;
	}
	
	/**
	 * Reconoce un Termino Aritmitico recursivo.
	 *  
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par RTerm() throws Exception{
		Par atrDeFact;
		Par atrDeRTerm;
		Par a = new Par();
		//System.out.println("RTerm");
		if (lexico.getNextToken().getCategoriaLexica()== Tipos.TKCCI){
			a.getProps().setTipo("");
			return a;
		}
		
		Token tk = lexico.lexer();
		if (lexico.reconoce(Tipos.TKCOMA)){
			a.getProps().setTipo("");
			System.out.println("Hola");
			return a;
		}
		else if (lexico.reconoce(Tipos.TKFF)){
			a.getProps().setTipo("error");
			return a;
		}
		else if (!(lexico.reconoce(Tipos.TKPYCOMA) || lexico.reconoce(Tipos.TKMEN) ||
				lexico.reconoce(Tipos.TKMENIG) || lexico.reconoce(Tipos.TKIG) ||
				lexico.reconoce(Tipos.TKDIF) || lexico.reconoce(Tipos.TKMAYIG) ||
				lexico.reconoce(Tipos.TKMAY) || lexico.reconoce(Tipos.TKPAP) ||
				lexico.reconoce(Tipos.TKPCI) || lexico.reconoce(Tipos.TKSUMA) ||
				lexico.reconoce(Tipos.TKRESTA) || lexico.reconoce(Tipos.TKEND) || 
				lexico.reconoce(Tipos.TKTHN) || lexico.reconoce(Tipos.TKDO) ) ){
			

			boolean numerico = lexico.reconoce(Tipos.TKMULT) || lexico.reconoce(Tipos.TKDIV);
			boolean booleano = lexico.reconoce(Tipos.TKAND); 
		
			atrDeFact = Fact();
			
			if (numerico){
				genOpMul(tk.getLexema());
			} else if (booleano) {
				genOpMul("and");    // O deber?amos cambiarlo por un genOpAd(tk.getLexema()) tambi?n??  CONSULTAR
			}
			atrDeRTerm = RTerm();
			
			if ( atrDeFact.getProps().getTipo().equals("error") || atrDeRTerm.getProps().getTipo().equals("error")){
				a.getProps().setTipo("error");
			} else {
				if (atrDeRTerm.getProps().getTipo().equals("")){
					a.getProps().setTipo(atrDeFact.getProps().getTipo());
				}
				else {
					if (numerico){ 
						if (atrDeFact.getProps().getTipo().equals(atrDeRTerm.getProps().getTipo()) && atrDeFact.getProps().getTipo().equals("int") ){
							a.getProps().setTipo(atrDeFact.getProps().getTipo()); // int
						} else {
							a.getProps().setTipo("error");
						}
					} else if (booleano) {
						if (atrDeFact.getProps().getTipo().equals(atrDeRTerm.getProps().getTipo()) && atrDeFact.getProps().getTipo().equals("bool") ){
							a.getProps().setTipo(atrDeFact.getProps().getTipo()); // bool
						} else {
							a.getProps().setTipo("error");
						}
					}
				}
			}
		}
		else{
			a.getProps().setTipo("");
		}
		return a;
	}

	
	/**
	 * Reconoce un Factor. Un Factor puede ser un entero, un identificador o una Expresion aritmitica
	 * entre parintesis.
	 * 
	 * @return Par devuelve los Par obtenidos en el analisis del Programa.
	 * @throws Exception Si sucede algun error en otras funciones se propaga la Excepcion.
	 */
	public Par Fact() throws Exception{
		Par a = new Par();
		Par atrDeExpC;
		Par atrDeFact;
		Token tk;
		tk = lexico.lexer();
		
		if (lexico.reconoce(Tipos.TKNUM)){
			a.getProps().setTipo("int");
			a.getProps().setElems(1);
			a.getProps().setTam(1);
			codigo. genIns("apila", Integer.parseInt(tk.getLexema()) );
			etq ++;
			//System.out.println("etq: " + etq);
		} 
		else if (lexico.reconoce(Tipos.TKTRUE) || lexico.reconoce(Tipos.TKFALSE)){
			a.getProps().setTipo("bool");
			a.getProps().setElems(1);
			a.getProps().setTam(1);
			int cod;
			if (tk.getLexema().equals("false"))
				cod = 0;
			else
				cod = 1;
			codigo.genIns("apila", cod);
			etq ++;
			//System.out.println("etq: " + etq);
		} else if (lexico.reconoce(Tipos.TKNOT) || lexico.reconoce(Tipos.TKRESTA)) {  // es un OpUn
			boolean numerico = lexico.reconoce(Tipos.TKRESTA); // numerico != true ==> booleano = true
			atrDeFact = Fact();
			a = atrDeFact;
			if (numerico){
				genOpNega();
				if (atrDeFact.getProps().getTipo().equals("int")){
					a.getProps().setTipo(atrDeFact.getProps().getTipo());
					a.getProps().setElems(1);
					a.getProps().setTam(1);
					
				}else{
					a.getProps().setTipo("error");
				}
			} else {
				genOpNot();
				if (atrDeFact.getProps().getTipo().equals("bool")){
					a.getProps().setTipo(atrDeFact.getProps().getTipo());
					a.getProps().setElems(1);
					a.getProps().setTam(1);
				}else{
					a.getProps().setTipo("error");
				}
			}
		}
		else {
			if (lexico.reconoce(Tipos.TKIDEN)){
				a = Mem();
				codigo.genIns("apila-ind");
				etq ++;
				//System.out.println("etq: " + etq);
			}
			else {
				if (lexico.reconoce(Tipos.TKPAP)){
					atrDeExpC = ExpC();
					if (lexico.reconoce(Tipos.TKPCI)){
						a.getProps().setTipo(atrDeExpC.getProps().getTipo());
					}
					else{
						a.getProps().setTipo("error");
					}
				}
				else{ 
					if (lexico.getNextToken().getCategoriaLexica()== Tipos.TKCCI){
						a.getProps().setTipo("");
						return a;
					}
					else{
						a.getProps().setTipo("error");
					}
				}
			}
		}
		//System.out.println("Fact :" + a.toString());
		return a;
	}
	
	/** 
	 * Resuelve un Fact en el que intervienen identificadores, tanto si es un identificador solo, como si es un puntero o un array.
	 * @return Par El objeto Par con los atributos obtenidos de resolver el identificador y sus referencias.
	 * @throws Exception En caso de producirse una excepcion, esta se propaga.
	 */
	public Par Mem() throws Exception{
		
		Atributos atrDeRMem = null;
		Par a = new Par();
		
		Token tk = lexico.getLookahead();

		if (!lexico.reconoce(Tipos.TKIDEN)){
			a = null;
			throw new Exception ("ERROR: Deberiamos haber leido un iden.");
		}
		a.setProps(new Atributos(TS.getProps(tk.getLexema())));
		a.setId(tk.getLexema());
		a.setClase(TS.getClase(tk.getLexema()));
		a.setDir(TS.getDir(tk.getLexema()));
		//System.out.println("TS.getDir(tk.getLexema()) " + TS.getDir(tk.getLexema()));
		codigo.genIns("apila",TS.getDir(tk.getLexema()));
		etq ++;
		//System.out.println("etq: " + etq);
		atrDeRMem = RMem(a.getProps()/*.getTbase()*/);
		if (atrDeRMem != null){
			if (atrDeRMem.getTipo().equals("")){
				a.setProps(TS.getProps(tk.getLexema()));
			}
			else a.setProps(atrDeRMem);
		}
		return a;
	}
	
	public Atributos RMem(Atributos a) throws Exception{
		Atributos atrDeRMem = null;
		
		Token tk = lexico.getNextToken();
		
		if (tk.getCategoriaLexica() == Tipos.TKPUNT){
			tk = lexico.lexer();
			codigo.genIns("apila-ind");
			etq ++;
			//System.out.println("etq: " + etq);
			if (a.getTbase().getTbase() == null)
				return a.getTbase();
			else
				atrDeRMem = RMem(a.getTbase());
		} 
		else if (tk.getCategoriaLexica() == Tipos.TKCAP){
			tk = lexico.lexer();
			Exp();
			tk = lexico.lexer(); // consumo ]
			
			if (lexico.reconoce(Tipos.TKCCI)){
				codigo.genIns("apila",a.getTbase().getTam());
				codigo.genIns("multiplica");
				codigo.genIns("suma");
				etq += 3;
				//System.out.println("etq: " + etq);
				if (a.getTipo().equals("ref")){
					Atributos aux = TS.ref(a);
					a = aux;
				}
				
				if (a.getTbase().getTbase() == null)
					return a.getTbase();
				else
					atrDeRMem = RMem(a.getTbase());
			}
			else{
				a.getTbase().setTipo("error"); 
			}
		}
		else {
			if (a.getTbase() == null){
				return a;
			}
			else{
				return a.getTbase();
			}
		}
		
		// Comprobamos que los tipos son iguales.
		if (!(a.getTbase().equals(atrDeRMem)) || a.getTipo().equals("error")){
			a.setTipo("error");
			throw new Exception("ERROR: RMEM Error en los tipos. /// EXCEPTION PARA QUITAR!!!!");
		}
		
		return atrDeRMem.getTbase();
	}
	
	/**
	 * Genera el cdigo de la operacin de suma, resta o el or.
	 * 
	 * @param opDeOpAd
	 */
	public void genOpAd(String opDeOpAd){
		
		if (opDeOpAd == "+")
			codigo.genIns("suma");
		else if (opDeOpAd.equals("-"))
			codigo.genIns("resta");
		else
			codigo.genIns("or");
		etq ++;
		//System.out.println("etq: " + etq);
	}
	
	/**
	 * Genera el codigo de la operacisn de multiplicacion, division o and.
	 * 
	 * @param opDeOpMul
	 */
	public void genOpMul(String opDeOpMul){
		
		if (opDeOpMul == "*")
			codigo.genIns("multiplica");
		else if (opDeOpMul.equals("/"))
			codigo.genIns("divide");
		else 
			codigo.genIns("and");
		etq ++;
		//System.out.println("etq: " + etq);
	}
	
	/**
	 * Genera el codigo de la operacion de comparacion
	 * @param opDeOpComp
	 */
	public void genOpComp(String opDeOpComp){
		
		if (opDeOpComp == "<="){
			codigo.genIns("menor_o_igual");
		}	
		if (opDeOpComp == "<"){
				codigo.genIns("menor");
		}
		if (opDeOpComp == ">="){
			codigo.genIns("mayor_o_igual");
		}	
		if (opDeOpComp == ">"){
				codigo.genIns("mayor");
		}
		if (opDeOpComp == "="){
			codigo.genIns("igual");
		}	
		if (opDeOpComp == "!="){
				codigo.genIns("distinto");
		}
		etq ++;
		//System.out.println("etq: " + etq);
	}

	/**
	 * Genera el codigo de la negacion booleana "not".
	 *
	 */
	public void genOpNot(){
		codigo.genIns("not");
		etq ++;
		//System.out.println("etq: " + etq);
	}
	
	public void genOpNega(){
		codigo.genIns("neg");
		etq ++;
		//System.out.println("etq: " + etq);
	}
	
	/*private String sacaTipo(Atributos atr){
		if (atr.getTbase() == null){
			return atr.getTipo();
		}
		else {
			return sacaTipo(atr.getTbase());
		}
	}*/
}