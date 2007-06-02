package Analizador_Sintactico;

import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import tSimbolos.TablaSimbolos;
import tSimbolos.TokenCte;
import tSimbolos.TokenFun;
import tSimbolos.TokenTipo;
import tSimbolos.Tipo.Bool;
import tSimbolos.Tipo.Campo;
import tSimbolos.Tipo.Error;
import tSimbolos.Tipo.Int;
import tSimbolos.Tipo.ListaCampos;
import tSimbolos.Tipo.Pointer;
import tSimbolos.Tipo.Record;
import tSimbolos.Tipo.Tipo;
import tSimbolos.Tipo.TipoAux;
import Analizador_Lexico.AnLexico;
import Analizador_Lexico.Token;
import Analizador_Sintactico.Traductor.Traductor;

public class AnSintactico
{
	/** Enlace al analizador l�xico, al que se le van pidiendo
	 *  los token seg�n se vayan necesitando.*/
	private AnLexico anLex;
	
	/** Tabla de Simbolos del procesador. En ella se van introduciendo
	 *  las nuevas variables declaradas.*/
	private TablaSimbolos ts;
	
	/** Tabla para almacenar todas las tablas de s�mbolos. Inicialmente solo 
	 * contiene la del programa principal*/
	private Hashtable<String, TablaSimbolos> tablas;

	/** �ltimo token leido por el analizador l�xico.*/
	private Token tActual;
		
	/** Traductor que se encarga de convertir las instrucciones en codigo fuente
	 * que se van analizando a codigo objeto. */
	private Traductor traductor; 
	
	/** Constructor de la clase. Inicializa los diversos atributos a partir
	 *  del fichero que se le pasa como par�metro y comienza el an�lisis
	 *  sint�ctico con la llamada al metodo prog(). 
	 *  @param fichero Objeto de tipo FileReader asociado al fichero que se quiere compilar.*/
	public AnSintactico(FileReader fichero)
	{
		anLex = new AnLexico(fichero);
		ts = new TablaSimbolos(null);
		ts.setDireccion(3); //Las primeras est�n reservadas.
		traductor = new Traductor();
		tablas = new Hashtable<String, TablaSimbolos>();
		tablas.put("PRINCIPAL",ts);
		tActual = anLex.analizador();
		progr();
	} 

	/** M�todo que inicia el an�lisis sint�ctico, es decir el axioma de la grm�tica.
	 * Se corresponde con la regla
	 * 		Prog -> Decs BEGIN Ins END*/
	private void progr()
	{
		//A�adida secci�n tipos (opcional, no tiene por qu� aparecer en todos los programas, en los m�s sencillos
		//se utilizar�n �nicamente tipos simples).
		int instruccion_comienzo = traductor.getEtiqueta();
		traductor.emiteInstrucci�nParcheable("ir-a");
		
		boolean errorT = false; //Por defecto no hay fallos en tipos
		if (tActual.getTipo().equals("TYPE")) {
			reconoce("TYPE");
			errorT = Tips(ts, 0);
			reconoce("FTYPE");
		}	
		
		boolean errorD = Decs(ts, 0);
		
		//A�adida secci�n de funciones. Al igual que los tipos, tambi�n es opcional;
		boolean errorF = false;
		if (tActual.getTipo().equals("FUNCTION"))
		{
			errorF = Functions(ts, 0);
		}
		reconoce("BEGIN");
		traductor.parchea(instruccion_comienzo, traductor.getEtiqueta());
		
		Enumeration<tSimbolos.Token> tokens = ts.getTabla().elements();
		LinkedList<tSimbolos.Token> reorden = reordenar(tokens);
		traductor.emiteInstruccion("incrementaC", reorden.getFirst().getTipo().getTama�o() + reorden.getFirst().getDireccion());
		
		boolean errorI = Ins(ts, 0);
		
		reconoce("END");
		traductor.emiteInstruccion("end");
		boolean error = errorD || errorI || errorT || errorF;
		if (error)
		{
			System.out.println("Hay errores sint�cticos en el c�digo. No se ha podido completar la compilaci�n.");
		}
		else
		{
			traductor.guardar("codigo.txt");
			System.out.println("Compilaci�n completada con �xito.");
		}
			
	}

	private boolean Functions(TablaSimbolos ts_padre, int nivel) 
	{
		boolean err1 = function(ts_padre, nivel + 1);
		boolean err2 = RFuncs(ts_padre, nivel + 1);
		return (err1 || err2);
	}

	private boolean RFuncs(TablaSimbolos ts_padre, int nivel) 
	{
		if (tActual.getTipo().equals("FUNCTION"))
		{
			boolean err1 = function(ts_padre, nivel);
			boolean err2 = RFuncs(ts_padre, nivel);
			return err1 || err2;
		}
		return false;
	}

	private boolean function(TablaSimbolos ts_padre, int nivel) 
	{
		TablaSimbolos tablafun = new TablaSimbolos(ts_padre);
		int dir_actual = traductor.getEtiqueta();
		traductor.emiteInstrucci�nParcheable("ir-a");
		Tipo t1 = cabeceraFun(ts_padre, tablafun, nivel, dir_actual);
		if (!(t1 instanceof Error))
		{
			tablafun.actualizarDir(); //Los par�metros se a�aden con direcci�n positiva y hay que modificarlos
			tablafun.setDireccion(3); //Las tres primeras direcciones estan reservadas.
		}
		
		boolean errorT = false; //Por defecto no hay fallos en tipos
		if (tActual.getTipo().equals("TYPE")) {
			reconoce("TYPE");
			errorT = Tips(tablafun, nivel);
			reconoce("FTYPE");
		}	
		
		Decs(tablafun, nivel + 1);
		
		boolean errorF = false;
		if (tActual.getTipo().equals("FUNCTION"))
		{
			errorF = Functions(ts_padre, nivel);
		}
		reconoce("BEGIN");
		traductor.parchea(dir_actual, traductor.getEtiqueta());
		
		Enumeration<tSimbolos.Token> tokens = tablafun.getTabla().elements();
		LinkedList<tSimbolos.Token> reorden = reordenar(tokens);
		
		traductor.emiteInstruccion("incrementaC", reorden.getFirst().getTipo().getTama�o() + reorden.getFirst().getDireccion() - 3);
		
		boolean errorI = Ins(tablafun, nivel);
		reconoce("RETURN");
				
		traductor.emiteInstruccion("apila", Math.min(0, reorden.getLast().getDireccion()) - t1.getTama�o());
		traductor.emiteInstruccion("apila", 0);
		Tipo t = ExpOr(tablafun, nivel);
		traductor.emiteInstruccion("desapila-ind");
		traductor.emiteInstruccion("retorno");
		
		reconoce("END");
		return !compatibles(t1, t) || errorF || errorI || errorT || t instanceof Error;
	}

	private Tipo cabeceraFun(TablaSimbolos ts_padre, TablaSimbolos tablafun, int nivel, int dir_actual) 
	{
		reconoce("FUNCTION");
		String nombreFun = tActual.getLexema();
		reconoce("ID");
		reconoce("PAA");
		if (tActual.getTipo().equals("TIPO"))
		{
			LParametros(tablafun, nivel);
		}
		reconoce("PAC");
		reconoce("PP");
		TipoAux tiporet = tipo(tablafun, nivel);
		if (!ts_padre.constainsId(nombreFun))
		{
			ts_padre.addFun(nombreFun, dir_actual, tiporet, nivel);
			tablas.put(nombreFun, tablafun);
		}
		else
			return new Error();
		return tiporet;
	}

	private boolean LParametros(TablaSimbolos tablafun, int nivel) 
	{
		boolean err1 = param(tablafun, nivel);
		boolean err2 = RParam(tablafun, nivel);
		return err1 || err2;
	}

	/** M�todo para analizar un par�metro de la funci�n.*/
	private boolean param(TablaSimbolos tablafun, int nivel) 
	{
		TipoAux tipo = tipo(tablafun, nivel);
		String nombre = tActual.getLexema();
		reconoce("ID");
		if (!tablafun.constainsId(nombre))
		{
			tablafun.addVar(nombre, tipo, nivel + 1);
		}
		else
			return true;
		return false;
	}

	private boolean RParam(TablaSimbolos tablafun, int nivel) 
	{
		if (!tActual.getTipo().equals("PAC"))
		{
			reconoce("COMA");
			TipoAux tipo = tipo(tablafun, nivel);
			String nombre = tActual.getLexema();
			reconoce("ID");
			if (!tablafun.constainsId(nombre))
			{
				tablafun.addVar(nombre, tipo, nivel + 1);
				return RParam(tablafun, nivel);
			}
			else
				return true;
		}
		return false;
	}

	/** M�todo que comprueba si el tipo del token actual es igual al que se le pasa como par�metro.
	 * Si es el mismo, le indica al analizador l�xico que lea el siguiente token.
	 * Si no es el mismo, sale del compilador con un mensaje de error.
	 * @param tipo El tipo del token que se quiere comprobar.*/
	private void reconoce(String tipo)
	{
		if (tActual.getTipo().equals(tipo))
	//		if (!tActual.getTipo().equals("END"))
				tActual = anLex.analizador();
	/*		else
				tActual = null;*/
		else
		{
			System.out.println("Error (l�nea " + tActual.getLinea() + "): " + "Token \"" + tActual.getLexema() + "\" inesperado: Se esperaba un token de tipo \"" + tipo + "\".");
			System.exit(0);
		}
	}

	/** Comprueba si dos tipos son compatibles o no, es decir, si son iguales o si ambos son num�ricos (Tipos NUM e INT)
	 * @param tipo1 El primero de los tipos que se quiere comprobar.
	 * @param tipo2 El segundo de los tipos que se quiere comprobar.
	 * @return Devuelve un booleano que informa si son compatibles o no.*/
	private boolean compatibles (Tipo tipo1, Tipo tipo2)
	{	
		/*return ((tipo1.getLexema().equals(tipo2.getLexema())) || 
				(tipo1.getLexema().equals("INT") && tipo2.getLexema().equals("NUM")) || 
				(tipo2.getLexema().equals("INT") && tipo1.getLexema().equals("NUM")));*/
		//Redefinido con equals apra soportar comparaci�n de tipos construidos (lo que deriva en que se 
		//eval�an las 2 expresiones de tipos y se comparan sus contenidos (recursivamente).
		if (tipo1.getLexema().equals("INT") && tipo2.getLexema().equals("NUM") ||
			tipo1.getLexema().equals("NUM") && tipo2.getLexema().equals("INT") )
				return true;
		else {	
			boolean compatibles = tipo1.equals(tipo2);
			if (compatibles == false) {
				errorTiposNoCompatibles(tipo1.getLexema(),tipo2.getLexema()); //No vale solo con el lexema, mejorar.
				return false;
			}
			else
				return true;
		}
	}
	
	/**
	 * Muestra un mensaje de error concreto que se refiere a un error en la secci�n de declaraciones,
	 * ya que se han declarado dos variables con el mismo nombre (restricci�n de integridad vulnerada)
	 * @param id Nombre de la variable que est� duplicada
	 */
	private void errorVariableYaDeclarada(String id) {
		System.out.println("Error (l�nea " + tActual.getLinea() + "): " 
			   + "Variable con nombre: "+id+", ya declarada anteriormente.");
	}
	
	/**
	 * Muestra un mensaje de error concreto referente a un error de incompatiblidad de tipos,
	 * que puede aparecer tanto en la secci�n de declaraciones (en las constantes) como en la 
	 * secci�n de instrucciones 
	 * @param tipo1 Tipo del valor o variable que se quiere asignar en la variable de tipo2
	 * @param tipo2 Tipo de la variable a la que se quiere asignar un valor
	 */
	private void errorTiposNoCompatibles(String tipo1,String tipo2) {
		if (!tipo1.equals("ERROR") && !tipo2.equals("ERROR")) {
			System.out.println("Error (l�nea " + tActual.getLinea() + "): " 
						+ "Tipo: "+tipo1+", no compatible con Tipo: "+tipo2+".");
		}
	}
	
	/**
	 * Muestra un mensaje de error concreto referente a tratar de asignar un valor a una constante.
	 * @param id Nombre de la constante que se trata de modificar
	 */
	private void errorAsignacionAConstante(String id) {
		System.out.println("Error (l�nea " + tActual.getLinea() + "): " 
				   + "Se trat� se asignar un valor a la constante: "+id);
	}
	
	/**
	 * Muestra un mensaje de error debido a que se ha le�do una variable que no ha sido declarada
	 * @param id Nombre de la variable que no ha sido declarada.
	 */
	private void errorVariableDesconocida(String id) {
		System.out.println("Error (l�nea " + tActual.getLinea() + "): " 
				   + "Variable desconocida: "+id);	
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		Decs -> Dec RDecs  
	 * @param nivel 
	 * @param tablafun 
	 * @return Indica si ha habido un error duranta la funci�n.*/
	private boolean Decs(TablaSimbolos tablafun, int nivel)
	{
		boolean error1 = Dec(tablafun, nivel);
		boolean error2 = RDecs(tablafun, nivel);
		return (error1 || error2);
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		RDecs -> ; Dec RDecs | null      //NOTA : null se refiere a lambda en la gram�tica.
	 * @param nivel 
	 * @param tablafun 
	 * @return Indica si ha habido un error duranta la funci�n.*/  
	private boolean RDecs(TablaSimbolos tablafun, int nivel)
	{
		if (!tActual.getTipo().equals("BEGIN") && !tActual.getTipo().equals("FUNCTION"))
		{
			reconoce("PYC");
			boolean error1 = Dec(tablafun, nivel);
			boolean error2 = RDecs(tablafun, nivel);
			return (error1 || error2);
		}
		return false;
	}

	/** M�todo para an�lisis de las expresiones:
	 * 		Dec -> CONST Tipo ID = Val
	 * 		Dec -> Tipo ID   
	 * @param nivel 
	 * @param tablafun 
	 * @return Indica si ha habido un error duranta la funci�n.*/
	private boolean Dec(TablaSimbolos tablafun, int nivel)
	{
		boolean error = false;
		if (tActual.getTipo().equals("CONST"))
		{
			reconoce("CONST");
			String tipoNombre = tActual.getLexema();
			Tipo tipo = null;
			if (tipoNombre.equals("INT")) {
				tipo = new Int();
				reconoce("TIPO");
			}
			else if (tipoNombre.equals("BOOL")) {
				tipo = new Bool();
				reconoce("TIPO");
			}
			//Aqu� se tratan los tipos construidos
			else {
				//Se trata de un tipo construido, es decir, de un TokenTipo que debe aparecer en la tabla de s�mbolos
				tSimbolos.Token t = tablafun.getToken(tipoNombre);
				if (t!= null && t instanceof TokenTipo) {
					tipo = ((TokenTipo)t).getTipoExpresionTipos();
					reconoce("ID");
				} else {
					error = true;
					//reconoce("Error");
				}	
			}
			String nomconst = tActual.getLexema();
			reconoce("ID");
			reconoce("IGUAL");
			String valor = tActual.getLexema();
			if (valor.equals("FALSE"))
				valor = "0";
			else 
				if (valor.equals("TRUE"))
					valor = "1";
			if (compatibles(tipo, new TipoAux(tActual.getTipo())) && !tablafun.constainsId(nomconst))
			{
				tablafun.addCte(nomconst, new Integer(valor), tipo, nivel + 1);
				reconoce(tActual.getTipo());
				error = false;
			}
			else
			{
				if (tablafun.constainsId(nomconst)) 
					errorVariableYaDeclarada(nomconst);
				tablafun.addCte(nomconst, new Integer(0), new Error(), nivel + 1);
				reconoce(tActual.getTipo());
				error = true;
			}
	}
		else
		{
			/*String tipoaux = tActual.getLexema();
			Tipo tipo = null;
			if (tipoaux.equals("INT")) {
				tipo = new Int();
				reconoce("TIPO");
			}
			else if (tipoaux.equals("BOOL")) {
				tipo = new Bool();
				reconoce("TIPO");
			}
			//Aqu� se tratan los tipos construidos
			else {
				//Se trata de un tipo construido, es decir, de un TokenTipo que debe aparecer en la tabla de s�mbolos
				
				// Ver si hace falta hacer aqu� alguna comprobaci�n m�s de error.
				
				tSimbolos.Token t = tablafun.getToken(tipoaux);
				if (t!= null && t instanceof TokenTipo) {
					tipo = ((TokenTipo)t).getTipoExpresionTipos();
					reconoce("ID");
				} else {
					error = true;
				}	
			}
			*/
			Tipo tipo = tipo(tablafun, nivel);
			boolean err1 = (tipo ==null);
			error = Ids(tipo, tablafun, nivel); //Hace el reconoce ID (ver si en el caso de tipos construidos deberia hacerse de otra forma)
			
		}
		return error;
	}
	
	/** M�todo para an�lisis de la expresi�n:
	 * 		Ids -> ID RIds    
	 * @param nivel 
	 * @param tablafun 
	 * @return Indica si ha habido un error duranta la funci�n.*/
	private boolean Ids(Tipo tipo, TablaSimbolos tablafun, int nivel)
	{
		boolean error1;
		String id = tActual.getLexema();
		reconoce("ID");
		if (!tablafun.constainsId(id))
		{
			tablafun.addVar(id, tipo, nivel + 1);
			error1 = false;
		}
		else
		{
			errorVariableYaDeclarada(id);
			tablafun.addVar(id, new Error(), nivel + 1);
			error1 = true;
		}
		boolean error2 = RIds(tipo, tablafun, nivel);
		return (error1 || error2);
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		RIds -> , ID RIds | null         //NOTA : null se refiere a lambda en la gram�tica.
	 * @param tipo El tipo de las variables que se est�n declarando.  
	 * @return Indica si ha habido un error duranta la funci�n.*/
	private boolean RIds(Tipo tipo, TablaSimbolos tablafun, int nivel)
	{
		
		if (!tActual.getTipo().equals("PYC") && !tActual.getTipo().equals("BEGIN") && !tActual.getTipo().equals("FUNCTION"))
		{
			boolean error1 = false;
			reconoce("COMA");
			String id = tActual.getLexema();
			reconoce("ID");
			if (!tablafun.constainsId(id))
				tablafun.addVar(id, tipo, 0);
			else
			{
				errorVariableYaDeclarada(id);
				tablafun.addVar(id, new Error(), 0);
				error1 = true;
			}
			boolean error2 = RIds(tipo, tablafun, nivel);
			return (error1 || error2);
		}
		return false;
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		Ins -> I RIns 
	 * @return Un booleano informando de si ha habido un error contextual en la instrucciones. */
	private boolean Ins(TablaSimbolos tablafun, int nivel) 
	{
		boolean err1, err2;
		err1 = I(tablafun, nivel);
		err2 = RIns(tablafun, nivel);
		return (err1 || err2);
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		RIns -> ; I RIns | null            //NOTA : null se refiere a lambda en la gram�tica.*/
	private boolean RIns(TablaSimbolos tablafun, int nivel) 
	{
		if (!tActual.getTipo().equals("END") && !tActual.getTipo().equals("ELSE") &&!tActual.getTipo().equals("UNTIL") && !tActual.getTipo().equals("RETURN"))
		{
			boolean err1, err2;
			reconoce("PYC");
			err1 = I(tablafun, nivel);
			err2 = RIns(tablafun, nivel);
			return (err1 || err2);
		}
		return false;
	}
	/** M�todo para an�lisis de la expresi�n
	 * 		I -> IAsig | IComp | IIf | IWhile | IRepeat
	 * @return Un booleano informando de si ha habido un error contextual en la instucci�n.*/
	private boolean I(TablaSimbolos tablafun, int nivel) 
	{
		if (tActual.getLexema().equals("BEGIN"))
			return IComp(tablafun, nivel);
		//Para operaci�n de punteros NEW(<id>)
		else if (tActual.getLexema().equals("NEW"))
			return INewID(tablafun, nivel);
		else if (tActual.getLexema().equals("IF"))
			return IIf(tablafun, nivel);
		else if (tActual.getLexema().equals("WHILE"))
			return IWhile(tablafun, nivel);
		else if (tActual.getLexema().equals("ELSE"))
			return false;
		else if (tActual.getLexema().equals("REPEAT"))
			return IRepeat(tablafun, nivel);
		else if (tActual.getLexema().equals("UNTIL"))
			return false;
		else 
			return IAsig(tablafun, nivel);
	}
	/** M�todo para an�lisis de la expresi�n
	 * 		IComp -> BEGIN IsOpc END
	 * @return Un booleano informando de si ha habido un error contextual en la instucci�n.*/
	private boolean IComp(TablaSimbolos tablafun, int nivel) 
	{
		reconoce("BEGIN");
		boolean err = IsOpc(tablafun, nivel);
		reconoce("END");
		return err;
	}

	private boolean IsOpc(TablaSimbolos tablafun, int nivel) 
	{
		if (!tActual.getLexema().equals("END"))
			return Ins(tablafun, nivel);
		else
			return false;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		IAsig -> ID = Exp 
	 * @return Un booleano informando de si ha habido un error contextual en la instucci�n.*/
	private boolean IAsig(TablaSimbolos tablafun, int nivel) 
	{
		if (tablafun.getToken(tActual.getLexema()).getClase() == tSimbolos.Token.CONSTANTE)
		{
			reconoce("ID");
			return true;
		}
		else
		{
			Tipo tipo = Desc(tablafun, nivel);
			reconoce("IGUAL");
			Tipo tipo1 = ExpOr(tablafun, nivel);
			traductor.emiteInstruccion("desapila-ind");
			return (tipo1.getLexema().equals("ERROR") || !compatibles (tipo1, tipo));
		}
	}

	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		ExpOr -> ExpAnd RExpOr
	 * @return El tipo de la expresi�n
	 */
	private Tipo ExpOr(TablaSimbolos tablafun, int nivel) 
	{
		Tipo tipo1 = ExpAnd(tablafun, nivel);
		Tipo tipo2 = RExpOr(tipo1, tablafun, nivel);
		return tipo2;
	}
	
	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		RExpOr -> || ExpAnd RExpOr | null    //NOTA : null se refiere a lambda en la gram�tica.
	 * @return El tipo de la expresi�n
	 */
	private Tipo RExpOr(Tipo tipo1, TablaSimbolos tablafun, int nivel)
	{
		if (tActual.getLexema().equals("||"))
		{
			reconoce("OPSUM");
			
			int irv = traductor.getEtiqueta() + 1;
			traductor.emiteInstruccion("copia");
			traductor.emiteInstrucci�nParcheable("ir-v");
			traductor.emiteInstruccion("desapila");
			
			Tipo tipo2 = ExpAnd(tablafun, nivel);
			Tipo tipo22;
			if (!tipo1.getLexema().equals("BOOL") || !tipo2.getLexema().equals("BOOL"))
				tipo22 = new Error();
			else
				tipo22 = new Bool();

			traductor.parchea(irv, traductor.getEtiqueta());
			tipo2 = RExpOr(tipo22, tablafun, nivel);
			return tipo2;
		}
		return tipo1;
	}
	
	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		ExpAnd -> Exp RExpAnd
	 * @return El tipo de la expresi�n
	 */
	private Tipo ExpAnd(TablaSimbolos tablafun, int nivel)
	{
		Tipo tipo1 = Exp(tablafun, nivel);
		Tipo tipo2 = RExpAnd(tipo1, tablafun, nivel);
		return tipo2;
	}
	
	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		RExpAnd -> && Exp RExpAnd | null    //NOTA : null se refiere a lambda en la gram�tica.
	 * @return El tipo de la expresi�n
	 */
	private Tipo RExpAnd(Tipo tipo1, TablaSimbolos tablafun, int nivel)
	{
		if (tActual.getLexema().equals("&&"))
		{
			reconoce("OPMUL");

			int irf = traductor.getEtiqueta();
			traductor.emiteInstrucci�nParcheable("ir-f");
			
			Tipo tipo2 = Exp(tablafun, nivel);
			
			Tipo tipo22;
			if (!tipo1.getLexema().equals("BOOL") || !tipo2.getLexema().equals("BOOL"))
				tipo22 = new Error();
			else
				tipo22 = new Bool();
			
			traductor.parchea(irf, traductor.getEtiqueta()+1);
			traductor.emiteInstruccion("ir-a", traductor.getEtiqueta()+2);
			traductor.emiteInstruccion("apila", 0);
			
			tipo2 = RExpAnd(tipo22, tablafun, nivel);
			return tipo2;
		}
		return tipo1;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Exp -> Exp1 RExp 
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo Exp(TablaSimbolos tablafun, int nivel) 
	{
		Tipo tipo1 = Exp1(tablafun, nivel);
		Tipo tipo2 = RExp(tipo1, tablafun, nivel);
		return tipo2;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		RExp -> OpOrd Exp1 RExp | null       //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo RExp(Tipo tipo1, TablaSimbolos tablafun, int nivel) 
	{
		if (tActual.getTipo().equals("OPORD"))
		{
			String lexema = tActual.getLexema();
			reconoce("OPORD");
			Tipo tipo2 = Exp1(tablafun, nivel);
			Tipo tipo22;
			if (!compatibles(tipo1,new Int()) || !compatibles(tipo2,new Int()))
				tipo22 = new Error();
			else
				tipo22 = new Bool();
			if (lexema.equals("<"))
				traductor.emiteInstruccion("menor");
			else if (lexema.equals(">"))
				traductor.emiteInstruccion("mayor");
				else if (lexema.equals("<="))
					traductor.emiteInstruccion("menoroigual");
					else if (lexema.equals(">="))
						traductor.emiteInstruccion("mayoroigual");
			tipo2 = RExp(tipo22, tablafun, nivel);
			return tipo2;
		}
		return tipo1;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Exp1 -> Exp2 RExp1 
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo Exp1(TablaSimbolos tablafun, int nivel)
	{
		Tipo tipo1 = Exp2(tablafun, nivel);
		Tipo tipo2 = RExp1(tipo1, tablafun, nivel);
		return tipo2;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		RExp1 -> OpEq Exp2 RExp1 | null             //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo RExp1(Tipo tipo1, TablaSimbolos tablafun, int nivel) 
	{
		if (tActual.getTipo().equals("OPEQ"))
		{
			String lexema = tActual.getLexema();
			reconoce("OPEQ");
			Tipo tipo2 = Exp2(tablafun, nivel);
			Tipo tipo22;
			if (!compatibles(tipo1, tipo2))
				tipo22 = new Error();
			else
				tipo22 = new Bool();
			if (lexema.equals("!="))
				traductor.emiteInstruccion("distintos");
			else if (lexema.equals("=="))
				traductor.emiteInstruccion("iguales");
			tipo2 = RExp1(tipo22, tablafun, nivel);
			return tipo2;
		}
		return tipo1;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Exp2 -> Exp3 RExp2
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo Exp2(TablaSimbolos tablafun, int nivel)
	{
		Tipo tipo1 = Exp3(tablafun, nivel);
		Tipo tipo2 = RExp2(tipo1, tablafun, nivel);
		return tipo2;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		RExp2 -> OpSum Exp3 RExp2 | null                //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo RExp2(Tipo tipo1, TablaSimbolos tablafun, int nivel) 
	{
		if (tActual.getTipo().equals("OPSUM") && !tActual.getLexema().equals("||")) //Ahora el || se hace con cortocircuito.
		{
			Tipo tipoOp = tipoOpSum();
			String lexema = tActual.getLexema();
			reconoce("OPSUM");
			Tipo tipo2 = Exp3(tablafun, nivel);
			Tipo tipo22;
			if (!compatibles(tipo1, tipo2) || !compatibles(tipo1, tipoOp))
				tipo22 = new Error();
			else
				tipo22 = tipo1;
			if (lexema.equals("+"))
				traductor.emiteInstruccion("suma");
			else if (lexema.equals("-"))
				traductor.emiteInstruccion("resta");
				/*else if (lexema.equals("||"))
					traductor.emiteInstruccion("or");*/
			tipo2 = RExp2(tipo22, tablafun, nivel);
			return tipo2;
		}
		return tipo1;
	}

	/** M�todo que se encarga de comprobar el tipo de un operador de la clase OpSum. 
	 * @return INT, en caso de que sea + o -, BOOL, en caso de que sea || o ERROR en otro caso.*/
	private Tipo tipoOpSum()
	{
		if (tActual.getLexema().equals("+") || tActual.getLexema().equals("-"))
			return new Int();
		else
			if (tActual.getLexema().equals("||"))
				return new Bool();
			else
				return new Error();
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Exp3 -> Exp4 RExp3 
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo Exp3(TablaSimbolos tablafun, int nivel)
	{
		Tipo tipo1 = Exp4(tablafun, nivel);
		Tipo tipo2 = RExp3(tipo1,tablafun, nivel);
		return tipo2;
	}
	
	/** M�todo para an�lisis de la expresi�n
	 * 		RExp3 -> OpMul Exp4 RExp3 | null             //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo RExp3(Tipo tipo1, TablaSimbolos tablafun, int nivel) 
	{
		if (tActual.getTipo().equals("OPMUL") && !tActual.getLexema().equals("&&")) //Ahora el && se hace con cortocircuito
		{
			Tipo tipoOp = tipoOpMul();
			String lexema = tActual.getLexema();
			reconoce("OPMUL");
			Tipo tipo2 = Exp4(tablafun, nivel);
			Tipo tipo22 = null;
			if (!compatibles(tipo1,tipo2) || !compatibles(tipo1, tipoOp))
				tipo22 = new Error();
			else
				tipo22 = tipo1;
			if (lexema.equals("*"))
				traductor.emiteInstruccion("multiplica");
			else if (lexema.equals("/")) {
				//A�adido para controlar las excepciones debidas a divisiones por cero.
				traductor.emiteInstruccion("copia");
				traductor.emiteInstruccion("apila", 0);
				traductor.emiteInstruccion("iguales");
				traductor.emiteInstruccion("ir-f",traductor.getEtiqueta()+6); //No es necesario parchear, se conoce la direccion ya.
				traductor.emiteInstruccion("Stop");
				traductor.emiteInstruccion("divide"); //La �ltima suma en etiqueta se hace fuera del else
				}
				else if (lexema.equals("MOD"))
				traductor.emiteInstruccion("modulo");
					/*else if (lexema.equals("&&"))
						traductor.emiteInstruccion("and");*/
			tipo2 = RExp3(tipo22, tablafun, nivel);
			return tipo2;
		}
		return tipo1;
	}

	/** M�todo que se encarga de comprobar el tipo de un operador de la clase OpMul. 
	 * @return INT, en caso de que sea * o /, BOOL, en caso de que sea && o ERROR en otro caso.*/
	private Tipo tipoOpMul()
	{
		if (tActual.getLexema().equals("*") || tActual.getLexema().equals("/") || tActual.getLexema().equals("MOD"))
			return new Int();
		else
			if (tActual.getLexema().equals("&&"))
				return new Bool();
			else
				return new Error();
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Exp4 -> OpUn Fact | Fact 
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo Exp4(TablaSimbolos tablafun, int nivel)
	{
		if (tActual.getTipo().equals("OPSUM") || tActual.getTipo().equals("OPNEG")) //Existe operador unario
		{
			Tipo tipoOp = tipoOpUn();
			Tipo tipo = Fact(tablafun, nivel);
			if (tipoOp.getLexema().equals(new Bool()))
			{
				traductor.emiteInstruccion("negacion");
			}
			else 
				if (tipoOp.getLexema().equals(new Int()))
				{
					traductor.emiteInstruccion("opuesto");  //El valor opuesto de un numero p.e. 2 opuesto de -2
				}
			if (!compatibles(tipoOp, tipo))
				return new Error();
			else
				return tipo;
		}
		else //No existe operador unario.
		{
			Tipo tipo = Fact(tablafun, nivel);
			return tipo;
		}
	}
	
	/** M�todo que se encarga de comprobar el tipo de un operador de la clase OpUn. 
	 * @return INT, en caso de que sea -, BOOL, en caso de que sea ! o ERROR en otro caso.*/
	private Tipo tipoOpUn()
	{
		if (tActual.getLexema().equals("-"))
		{	
			reconoce("OPSUM");
			return new Int();           
		}
		else
			if (tActual.getLexema().equals("!"))
			{
				reconoce("OPNEG");
				return new Bool();
			}
			else
				return new Error();     
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Fact -> TRUE
	 * 		Fact -> FALSE
	 * 		Fact -> NUM
	 * 		Fact -> ID
	 * 		Fact -> ( Exp )        (Si llega esto vuelve a llamar a Exp)
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo Fact(TablaSimbolos tablafun, int nivel)
	{
		Tipo tipo;
		if (tActual.getTipo().equals("BOOL")) // El valor de Fact es TRUE o FALSE.
		{
			int bool;
			if (tActual.getLexema().equals("TRUE"))
				bool = 1;
			else
				bool = 0;
			traductor.emiteInstruccion("apila", bool);
			reconoce("BOOL");
			//tipo = new Bool();
			if (tablafun.constainsId(tActual.getLexema())) {
				tSimbolos.Token aux = tablafun.getToken(tActual.getLexema());
				tipo = aux.getTipo();
				tipo = RDesc(tipo, tablafun, nivel);
			}
			else
				tipo = new Bool();
			//Reconoce s�mbolos posteriores?
		}
		else if (tActual.getTipo().equals("NUM")) //El valor de Fact es un n�mero. 
		{
			traductor.emiteInstruccion("apila", Integer.parseInt(tActual.getLexema()));
			reconoce("NUM");
			tipo = new Int();
			//tipo = RDesc();
			//Reconoce s�mbolos posteriores?
		}
		else if (tActual.getTipo().equals("ID")) // El valor de Fact viene determinado por una variable o constante.
		{
			if (!tActual.getLexema().equals("PRINCIPAL") && tablas.containsKey(tActual.getLexema())) //Est� declarada y no es el programa principal
			{
				TablaSimbolos tsaux = tablas.get(tActual.getLexema()); 
				TablaSimbolos padre = tsaux.getTabla_padre();
				tipo = padre.getToken(tActual.getLexema()).getTipo();
				int etq = ((TokenFun)(padre.getToken(tActual.getLexema()))).getEtiqueta();
				reconoce("ID");
				reconoce("PAA");
				traductor.emiteInstruccion("incrementaC", tipo.getTama�o());
				
				boolean error1 = false;
				Enumeration<tSimbolos.Token> tokens = tsaux.getTabla().elements();
				
				LinkedList<tSimbolos.Token> reorden = reordenar(tokens);
				int restar = 0;
				while (!reorden.isEmpty() && !error1)
				{
					tSimbolos.Token parametro = reorden.getLast();
					reorden.removeLast();
					if (parametro.getDireccion() < 0)
					{
						restar += parametro.getTipo().getTama�o();
						Tipo t = ExpOr(tablafun, nivel);
						if (!compatibles(t, parametro.getTipo()))
							error1 = true;
						if (!reorden.isEmpty() && reorden.getLast().getDireccion() < 0)
							reconoce("COMA");
					}
				}
				reconoce("PAC");
				traductor.emiteInstruccion("apila", etq);
				traductor.emiteInstruccion("apila",0); //Nivel del procedimiento
				traductor.emiteInstruccion("llamada"); //Direcci�n de comienzo de la funci�n
				if (restar != 0)
					traductor.emiteInstruccion("incrementaC", -restar);
				if (error1)
					return new Error();
				else
					return tipo;
			}
			else
			{
				String id = tActual.getLexema();
				if (tablafun.getToken(id) instanceof TokenCte)  //id representa una constante.
				{
					if (tablafun.getToken(id).getTipo().equals(new Bool()))
						if (tablafun.getToken(id).getValor().equals("TRUE"))
							traductor.emiteInstruccion("apila", 1);
						else
							traductor.emiteInstruccion("apila", 0);
					else
						traductor.emiteInstruccion("apila", tablafun.getToken(id).getValor());
					tipo = ts.getToken(id).getTipo();
					reconoce("ID");
				}
				else
				{
					tipo = Desc(tablafun, nivel);
					traductor.emiteInstruccion("apila-ind");
				}
			}
			/*String id = tActual.getLexema();
			if (!ts.constainsId(id))
				tipo = new Error();
			else
			{
				if (ts.getToken(id) instanceof TokenVar) // id representa una variable.
				{
					traductor.emiteInstruccion("apila-dir", ts.getToken(id).getDireccion());
					etiqueta++;
				}
				else  //id representa una constante.
				{
					if (ts.getToken(id).getTipo().equals(new Bool()))
						if (ts.getToken(id).getValor().equals("TRUE"))
							traductor.emiteInstruccion("apila", 1);
						else
							traductor.emiteInstruccion("apila", 0);
					else
						traductor.emiteInstruccion("apila", ts.getToken(id).getValor());
					etiqueta++;
				}
				tipo = ts.getToken(id).getTipo();
				reconoce("ID");
			}*/
		}
		else if (tActual.getTipo().equals("PAA")) //El valor de Fact viene dado por una espresi�n parentizada.
		{
			reconoce("PAA");
			tipo = ExpOr(tablafun, nivel);
			reconoce("PAC");
		}
		else
			tipo = new Error();
		return tipo;
	}
	
	/** Reordena la lista de tokens atendiendo a su direci�n.*/
	private LinkedList<tSimbolos.Token> reordenar(Enumeration<tSimbolos.Token> tokens) 
	{
		LinkedList<tSimbolos.Token> lista = new LinkedList<tSimbolos.Token>();
		while (tokens.hasMoreElements())
		{
			tSimbolos.Token tk = tokens.nextElement();
			if (lista.isEmpty())
				lista.add(tk);
			else
			{
				int i = 0;
				while (i < lista.size() && lista.get(i).getDireccion() > tk.getDireccion())
				{
					i++;
				}
				lista.add(i, tk);
			}
		}
		return lista;
	}

	/**
	 * M�todo para el an�lisis de la expresi�n
	 * 		IIf -> IF Exp THEN Ins(0) else Ins(1)
	 * @return Boolean que indica si se ha producido un error en la expresi�n
	 */
	private boolean IIf(TablaSimbolos tablafun, int nivel){
		boolean error1,error2;
		
		reconoce("IF");
		
		Tipo tipo1 = Exp(tablafun, nivel);
		int etiquetaI0 = traductor.getEtiqueta();
		//Esta instrucci�n ser� parcheada posteriormente.
		traductor.emiteInstrucci�nParcheable("ir-f");
		//Si la expresi�n es de tipo booleano, procedemos a comprobar el if
		if (tipo1.getLexema().equals("BOOL")){
			reconoce("THEN");
			//Generamos el c�digo de I(0). Lo reconocemos como bloque BEGIN-END.
			error1 = IComp(tablafun, nivel);
			int etiquetaI1 = traductor.getEtiqueta();
			//De nuevo, esta instrucci�n ser� parcheada posteriormente.
			traductor.emiteInstrucci�nParcheable("ir-a");
			//Reconocemos un ";" antes del ELSE, que ser�a el del bloque Begin-End del IF (no del else).
			reconoce("PYC");
			reconoce("ELSE");
			//Aqu� ya conocemos la direcci�n para parchear el "ir-f" (else)
			traductor.parchea(etiquetaI0,traductor.getEtiqueta()+1);
			//Generamos el c�digo de I(1). Lo reconocemos como bloque BEGIN-END.
			error2 = IComp(tablafun, nivel);
			//Aqu� ya conocemos la direcci�n para parchear el "ir-a" (fin de if)
			traductor.parchea(etiquetaI1,traductor.getEtiqueta()+1);
		} else {
			return true;
		}
		return (error1 || error2);
	}
	
	/**
	 * M�todo para el an�lisis de la expresi�n
	 * 		IWhile -> WHILE Exp DO Ins
	 * @return Boolean que indica si se ha producido un error en la expresi�n
	 */
	private boolean IWhile(TablaSimbolos tablafun, int nivel){
		boolean errorIns;
		
		reconoce("WHILE");
		
		int etiquetaExp = traductor.getEtiqueta();
		Tipo tipo1 = Exp(tablafun, nivel);
		int etiquetaIns = traductor.getEtiqueta();
		//Esta instrucci�n ser� parcheada posteriormente
		traductor.emiteInstrucci�nParcheable("ir-f");
		if (tipo1.getLexema().equals("BOOL")){
			reconoce("DO");
			errorIns = IComp(tablafun, nivel);
			//Esta instrucci�n ser� parcheada posteriormente
			traductor.emiteInstruccion("ir-a",etiquetaExp);
			//Ahora ya conocemos el destino del primer "ir-f"
			traductor.parchea(etiquetaIns,traductor.getEtiqueta());
		} else {
			return true;
		}
		return errorIns;
	}
	
	/**
	 * M�todo para el an�lisis de la expresi�n
	 * 		IRepeat -> REPEAT Ins UNTIL Exp
	 * @return Boolean que indica si se ha producido un error en la expresi�n
	 */
	private boolean IRepeat(TablaSimbolos tablafun, int nivel){
		
		reconoce("REPEAT");
		//El repeat no precisar� de parcheos.
		int etiquetaIns = traductor.getEtiqueta();
		boolean errorIns = Ins(tablafun, nivel);
		reconoce("UNTIL"); 
		Tipo tipo1 = Exp(tablafun, nivel);
		if (tipo1.getLexema().equals("BOOL"))
		{
			traductor.emiteInstruccion("ir-f",etiquetaIns+1);
		} else {
			return true;
		}
		
		return errorIns;
	}
	
	/**
	 * M�todo para el an�lisis de la expresi�n 
	 * 		Desc -> ID RDesc
	 * @return Boolean que indica se se ha producido un error en la expresi�n
	 * */ 
	private Tipo Desc(TablaSimbolos tablafun, int nivel)
	{
		String id = tActual.getLexema();
		Tipo tipo = null;
		if (!tablafun.constainsId(id))
			tipo = new Error();
		else
		{
			traductor.emiteInstruccion("apila", tablafun.getToken(id).getDireccion());
			reconoce("ID");
			if (tablafun.getToken(id).getInstanciada() == 1)
				tipo = RDesc(tablafun.getToken(id).getTipo()/*,id*/, tablafun, nivel);
			else {//error
				//mostrarInfoVariableNoInstanciada();
				return new Error();
			}
		}
		return tipo;
	}

	private Tipo RDesc(Tipo tipo/*,String lexema*/,TablaSimbolos tablafun, int nivel) 
	{
		Tipo tiporet = tipo;
		//String id = lexema;
		if (tActual.getTipo().equals("PUNTERO")) //Es un puntero.
		{
			//String id = tActual.getLexema();
			//tSimbolos.Token tk = ts.getToken(id);
			reconoce("PUNTERO");
			//if (tk == null || !tk.getTipo().equals("PUNTERO")) //No se si es puntero o pointer.
			//	return new Error();
			//else
			if (tipo!=null && tipo instanceof Pointer)
			{
				traductor.emiteInstruccion("apila-ind");
				tiporet = RDesc(((Pointer)tipo).getTipoApuntado()/*,id*/, tablafun, nivel);
				//tiporet = RDesc(tk.getTipo()/*.getTipoApuntado()*/,id); 
			}
			else
				return new Error();
			
		}
		else 
			if (tActual.getTipo().equals("PUNTO")) //Es el campo de un registro.
			{
				reconoce("PUNTO");
				
				if (tipo!=null && tipo instanceof Record)
				{
					String idCampo = tActual.getLexema(); //Cambio BY Nacho Es: AUNQUE creo que no est� acabado, y por tanto tener cuidado pero tal vez no afecte, lo que pasa que deb�a cambiar el nombre.
					reconoce("ID");
					
					Campo c = ((Record)tipo).getListaCampos().getCampo(idCampo);
					if (c == null)
						return new Error();
					else
					{
						int offset = c.getOffset();
						traductor.emiteInstruccion("apila",offset);
						traductor.emiteInstruccion("suma");
						tiporet = RDesc(c.getTipoCampo(),/*,id*/ tablafun, nivel);
					}
				}
				else
					return new Error();
			}
		traductor.emiteInstruccion("apila", 0); //Aqui ir�a la diferencia de niveles.
		return tiporet;
	}
	
	/**
	 * M�todo para el an�lisis de la expresi�n
	 * 		Tips -> decTipo RTips
	 * @return Boolean que indica si se ha producido un error en la expresi�n
	 */
	private boolean Tips(TablaSimbolos tablafun, int nivel) {
		boolean err1 = decTipo(tablafun, nivel);
		boolean err2 = RTips(tablafun, nivel);
		return (err1 || err2);
	}
	
	/**
	 * M�todo para el an�lisis de la expresi�n
	 * 		RTips -> decTipo | null             //NOTA : null se refiere a lambda en la gram�tica.
	 * @return Boolean que indica si se ha producido un error en la expresi�n
	 */	
	private boolean RTips(TablaSimbolos tablafun, int nivel) {
		if (!tActual.getTipo().equals("FTYPE"))
		{
			reconoce("PYC");
			boolean error1 = decTipo(tablafun, nivel);
			boolean error2 = RTips(tablafun, nivel);
			return (error1 || error2);
		}
		return false;
	}
	
	/**
	 * M�todo para el an�lisis de la expresi�n
	 * 		decTipo -> id : tipo;             
	 * @return Boolean que indica si se ha producido un error en la expresi�n
	 */	
	private boolean decTipo(TablaSimbolos tablafun, int nivel) {
		String nombreTipo = tActual.getLexema();
		reconoce("ID");
		reconoce("PP");
		TipoAux tipoRetorno = tipo(tablafun, nivel);
		boolean err1 = (tipoRetorno ==null);
		/*if (ts==null) {
			int j;
			j = 5;
		}*/
		if (!err1)
			tablafun.addTipo(nombreTipo,tipoRetorno);
		//reconoce("PYC");
		return err1;
	}
	
	/**
	 * M�todo para el an�lisis de las expresiones:
	 * tipo -> id | BOOL | INT | ^tipo | tipo
	 * @return
	 */
	private TipoAux tipo(TablaSimbolos tablafun, int nivel) {
		boolean err1;
		//Creo que deber�a reconocer algo distinto de TIPO, pero as� es como est� hecho en las 
		//declaraciones de variables (seguramente porque haya que hacer cambios en tipos).
		if (tActual.getLexema().equals("BOOL")) {
			reconoce("TIPO");
			TipoAux tipoBooleano = new Bool(/*"BOOL"*/); //Creo que el lexema es BOOL
			return tipoBooleano;
		}
		else if (tActual.getLexema().equals("INT") || tActual.getTipo().equals("NUM")) {
			reconoce("TIPO");
			TipoAux tipoInt = new Int(/*"INT"*/); //Creo que el lexema es INT
			return tipoInt;
		}
		else if (tActual.getTipo().equals("PUNTERO")) { //O "POINTER", comprobar.
			reconoce("PUNTERO");
			//Crea nodoPointer
			TipoAux apuntado= tipo(tablafun, nivel);
			TipoAux puntero = new Pointer("POINTER",apuntado);
			return puntero;
		}
		else if (tActual.getTipo().equals("RECORD")) {
			reconoce("RECORD");
			ListaCampos listacampos = new ListaCampos();
			boolean err = LCampos(tablafun, nivel, listacampos);
			TipoAux tipoDevuelto = null;
			if (!err)
			{
				listacampos.evaluar_offsets();
				tipoDevuelto = new Record("RECORD", listacampos);
			}
			reconoce("FRECORD");
			return tipoDevuelto;
		}
		else if (tActual.getTipo().equals("ID")) {
			//De momento en esta versi�n se reconocen en orden los tipos, de modo que no se 
			//permite en el lado derecho utilizar un tipo que no haya sido declarado anteriormente.
			String nombreid = tActual.getLexema();
			tSimbolos.Token tok = tablafun.getToken(nombreid);
			if ((tok==null) || !(tok instanceof TokenTipo) ) {
				err1=true;
				return null; //Forma de propagar el error, meramente cuesti�n de implementaci�n (Java solo permite devolver un valor).
			}
			else {
				err1=false;
				reconoce("ID"); //Es un TIPO, pero en el l�xico "s�lo" se ha podido reconocer como ID.
				return ((TokenTipo)tok).getTipoExpresionTipos();
			}
		}
		else return null; //Error.
		
		//El ; lo reconoce fuera del Tipo, aqu� no se incluye.
	}
	
	//Reconoce el tipo y el nombre de los campos
	private boolean LCampos(TablaSimbolos tablafun, int nivel, ListaCampos listacampos)
	{
		LinkedList<String> ids = new LinkedList<String>();
		LIdent(tablafun, nivel, ids);
		reconoce("PP");
		TipoAux tipo = tipo(tablafun, nivel);
		if (tipo != null)
		{
			boolean err1 = listacampos.a�adeIdentificadores(ids, tipo);
			boolean err2 = RLCampos(tablafun, nivel, listacampos);
			return (err1 || err2);
		}
		else
			return true;
	}
	
	

	//Resto de campos
	private boolean RLCampos(TablaSimbolos tablafun, int nivel, ListaCampos listacampos)
	{
		if(!tActual.getTipo().equals("FRECORD")) {
			reconoce("PYC");
			LinkedList<String> ids = new LinkedList<String>();
			LIdent(tablafun, nivel, ids);
			reconoce("PP");
			TipoAux tipo = tipo(tablafun, nivel);
			boolean err = listacampos.a�adeIdentificadores(ids, tipo);
			RLCampos(tablafun, nivel, listacampos);
			return err;
		}
		return false;
	}
	
	//para declaraciones con varios identificadores del mismo tipo
	//NUNCA VA A DEVOLVER ERROR. CUIDADO CON IDENTIFICADORES REPETIDOS!!
	private void LIdent(TablaSimbolos tablafun, int nivel, LinkedList<String> ids){
		String id = tActual.getLexema();
		reconoce("ID");
		ids.add(id);
		RLIdent(tablafun, nivel, ids);
	}
	
	//Resto de identificadores
	private void RLIdent(TablaSimbolos tablafun, int nivel, LinkedList<String> ids){
		if (tActual.getTipo().equals("COMA"))
		{
			reconoce("COMA");
			String id = tActual.getLexema();
			reconoce("ID");
			ids.add(id);
			RLIdent(tablafun, nivel, ids);
		}
	}
	
	private boolean INewID(TablaSimbolos tablafun, int nivel) {
		boolean error1=false;
		//......
		reconoce("NEW");
		reconoce("PAA"); // "("

		String lex = tActual.getLexema();
		tSimbolos.Token tSint= tablafun.getToken(lex);
		if (tSint!=null && (tSint.getTipo() instanceof Pointer) && !tablafun.esTipo(tSint)) { //Aqu� habr�a que asegurarse de que es puntero
			reconoce("ID");
			reconoce("PAC"); // ")"
			
			//Generar c�digo (en proceso)
			traductor.emiteInstruccion("apila",tSint.getDireccion());
			traductor.emiteInstruccion("apilaH");
			traductor.emiteInstruccion("desapila_ind");
			//Aclarar entre estas 2.
			//traductor.emiteInstruccion("incrementaH"+((TokenTipo)tSint).getTipo().getTama�o());
			traductor.emiteInstruccion("incrementaH("+tSint.getTipo().getTama�o()+")");
			tSint.instancia();
			return error1;
		}
		else {
			error1 = true;
			return error1;
		}
	}
	
}
