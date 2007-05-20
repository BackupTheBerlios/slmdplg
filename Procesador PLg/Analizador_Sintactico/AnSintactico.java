package Analizador_Sintactico;

import java.io.FileReader;

import tSimbolos.TablaSimbolos;
import tSimbolos.Tipo.Bool;
import tSimbolos.Tipo.Error;
import tSimbolos.Tipo.Int;
import tSimbolos.Tipo.Record;
import tSimbolos.Tipo.Tipo;
import tSimbolos.TokenTipo;
import tSimbolos.Tipo.TipoAux;
import tSimbolos.Tipo.Pointer;
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

	/** �ltimo token leido por el analizador l�xico.*/
	private Token tActual;
	
	/** Direcci�n de la �ltima variable insertada en la tabla de s�mbolos.
	 *  Se incrementa al introducir una nueva variable.*/
	private int direcci�n;
	
	/**
	 * Indica la etiqueta actual, que se va actualizando seg�n sea necesario (atributo remoto).
	 */
	private int etiqueta;
	
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
		ts = new TablaSimbolos();
		traductor = new Traductor();
		tActual = anLex.analizador();
		direcci�n = 0;
		etiqueta = 0;
		progr();
	} 

	/** M�todo que inicia el an�lisis sint�ctico, es decir el axioma de la grm�tica.
	 * Se corresponde con la regla
	 * 		Prog -> Decs BEGIN Ins END*/
	private void progr()
	{
		//A�adida secci�n tipos (opcional, no tiene por qu� aparecer en todos los programas, en los m�s sencillos
		//se utilizar�n �nicamente tipos simples).
		boolean errorT = false; //Por defecto no hay fallos en tipos
		if (tActual.getTipo().equals("TYPE")) {
			reconoce("TYPE");
			errorT = Tips();
			reconoce("FTYPE");
		}	
		
		boolean errorD = Decs();
		reconoce("BEGIN");
		boolean errorI = Ins();
		reconoce("END");
		traductor.emiteInstruccion("end");
		etiqueta++;
		boolean error = errorD || errorI;
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
		else	
			return tipo1.equals(tipo2);
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		Decs -> Dec RDecs  
	 * @return Indica si ha habido un error duranta la funci�n.*/
	private boolean Decs()
	{
		boolean error1 = Dec();
		boolean error2 = RDecs();
		return (error1 || error2);
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		RDecs -> ; Dec RDecs | null      //NOTA : null se refiere a lambda en la gram�tica.
	 * @return Indica si ha habido un error duranta la funci�n.*/  
	private boolean RDecs()
	{
		if (!tActual.getTipo().equals("BEGIN"))
		{
			reconoce("PYC");
			boolean error1 = Dec();
			boolean error2 = RDecs();
			return (error1 || error2);
		}
		return false;
	}

	/** M�todo para an�lisis de las expresiones:
	 * 		Dec -> CONST Tipo ID = Val
	 * 		Dec -> Tipo ID   
	 * @return Indica si ha habido un error duranta la funci�n.*/
	private boolean Dec()
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
				tSimbolos.Token t = ts.getToken(tipoNombre);
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
			if (compatibles(tipo, new TipoAux(tActual.getTipo())) && !ts.constainsId(nomconst))
			{
				ts.addCte(nomconst, new Integer(valor), tipo);
				reconoce(tActual.getTipo());
				error = false;
			}
			else
			{
				ts.addCte(nomconst, new Integer(0), new Error());
				reconoce(tActual.getTipo());
				error = true;
			}
	}
		else
		{
			String tipoaux = tActual.getLexema();
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
				
				tSimbolos.Token t = ts.getToken(tipoaux);
				if (t!= null && t instanceof TokenTipo) {
					tipo = ((TokenTipo)t).getTipoExpresionTipos();
					reconoce("ID");
				} else {
					error = true;
				}	
			}
			error = Ids(tipo); //Hace el reconoce ID (ver si en el caso de tipos construidos deberia hacerse de otra forma)
			
		}
		return error;
	}
	
	/** M�todo para an�lisis de la expresi�n:
	 * 		Ids -> ID RIds    
	 * @return Indica si ha habido un error duranta la funci�n.*/
	private boolean Ids(Tipo tipo)
	{
		boolean error1;
		String id = tActual.getLexema();
		reconoce("ID");
		if (!ts.constainsId(id))
		{
			ts.addVar(id, direcci�n++, tipo);
			error1 = false;
		}
		else
		{
			ts.addVar(id, direcci�n++, new Error());
			error1 = true;
		}
		boolean error2 = RIds(tipo);
		return (error1 || error2);
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		RIds -> , ID RIds | null         //NOTA : null se refiere a lambda en la gram�tica.
	 * @param tipo El tipo de las variables que se est�n declarando.  
	 * @return Indica si ha habido un error duranta la funci�n.*/
	private boolean RIds(Tipo tipo)
	{
		
		if (!tActual.getTipo().equals("PYC") && !tActual.getTipo().equals("BEGIN"))
		{
			boolean error1 = false;
			reconoce("COMA");
			String id = tActual.getLexema();
			reconoce("ID");
			if (!ts.constainsId(id))
				ts.addVar(id, direcci�n++, tipo);
			else
			{
				ts.addVar(id, direcci�n++, new Error());
				error1 = true;
			}
			boolean error2 = RIds(tipo);
			return (error1 || error2);
		}
		return false;
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		Ins -> I RIns 
	 * @return Un booleano informando de si ha habido un error contextual en la instrucciones. */
	private boolean Ins() 
	{
		boolean err1, err2;
		err1 = I();
		err2 = RIns();
		return (err1 || err2);
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		RIns -> ; I RIns | null            //NOTA : null se refiere a lambda en la gram�tica.*/
	private boolean RIns() 
	{
		if (!tActual.getTipo().equals("END") && !tActual.getTipo().equals("ELSE") &&!tActual.getTipo().equals("UNTIL"))
		{
			boolean err1, err2;
			reconoce("PYC");
			err1 = I();
			err2 = RIns();
			return (err1 || err2);
		}
		return false;
	}
	/** M�todo para an�lisis de la expresi�n
	 * 		I -> IAsig | IComp | IIf | IWhile | IRepeat
	 * @return Un booleano informando de si ha habido un error contextual en la instucci�n.*/
	private boolean I() 
	{
		if (tActual.getLexema().equals("BEGIN"))
			return IComp();
		//Para operaci�n de punteros NEW(<id>)
		else if (tActual.getLexema().equals("NEW"))
			return INewID();
		else if (tActual.getLexema().equals("IF"))
			return IIf();
		else if (tActual.getLexema().equals("WHILE"))
			return IWhile();
		else if (tActual.getLexema().equals("ELSE"))
			return false;
		else if (tActual.getLexema().equals("REPEAT"))
			return IRepeat();
		else if (tActual.getLexema().equals("UNTIL"))
			return false;
		else 
			return IAsig();
	}
	/** M�todo para an�lisis de la expresi�n
	 * 		IComp -> BEGIN IsOpc END
	 * @return Un booleano informando de si ha habido un error contextual en la instucci�n.*/
	private boolean IComp() 
	{
		reconoce("BEGIN");
		boolean err = IsOpc();
		reconoce("END");
		return err;
	}

	private boolean IsOpc() 
	{
		if (!tActual.getLexema().equals("END"))
			return Ins();
		else
			return false;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		IAsig -> ID = Exp 
	 * @return Un booleano informando de si ha habido un error contextual en la instucci�n.*/
	private boolean IAsig() 
	{
		Tipo tipo = Desc();
		reconoce("IGUAL");
		Tipo tipo1 = ExpOr();
		traductor.emiteInstruccion("desapila-ind");
		etiqueta++;
		return (tipo1.getLexema().equals("ERROR") || !compatibles (tipo1, tipo));
	}

	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		ExpOr -> ExpAnd RExpOr
	 * @return El tipo de la expresi�n
	 */
	private Tipo ExpOr() 
	{
		Tipo tipo1 = ExpAnd();
		Tipo tipo2 = RExpOr(tipo1);
		return tipo2;
	}
	
	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		RExpOr -> || ExpAnd RExpOr | null    //NOTA : null se refiere a lambda en la gram�tica.
	 * @return El tipo de la expresi�n
	 */
	private Tipo RExpOr(Tipo tipo1)
	{
		if (tActual.getLexema().equals("||"))
		{
			reconoce("OPSUM");
			
			int irv = etiqueta + 1;
			traductor.emiteInstruccion("copia");
			traductor.emiteInstrucci�nParcheable("ir-v");
			traductor.emiteInstruccion("desapila");
			etiqueta += 3;
			
			Tipo tipo2 = ExpAnd();
			Tipo tipo22;
			if (!tipo1.getLexema().equals("BOOL") || !tipo2.getLexema().equals("BOOL"))
				tipo22 = new Error();
			else
				tipo22 = new Bool();

			traductor.parchea(irv, etiqueta);
			tipo2 = RExpOr(tipo22);
			return tipo2;
		}
		return tipo1;
	}
	
	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		ExpAnd -> Exp RExpAnd
	 * @return El tipo de la expresi�n
	 */
	private Tipo ExpAnd()
	{
		Tipo tipo1 = Exp();
		Tipo tipo2 = RExpAnd(tipo1);
		return tipo2;
	}
	
	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		RExpAnd -> && Exp RExpAnd | null    //NOTA : null se refiere a lambda en la gram�tica.
	 * @return El tipo de la expresi�n
	 */
	private Tipo RExpAnd(Tipo tipo1)
	{
		if (tActual.getLexema().equals("&&"))
		{
			reconoce("OPMUL");

			int irf = etiqueta;
			traductor.emiteInstrucci�nParcheable("ir-f");
			etiqueta++;
			
			Tipo tipo2 = Exp();
			
			Tipo tipo22;
			if (!tipo1.getLexema().equals("BOOL") || !tipo2.getLexema().equals("BOOL"))
				tipo22 = new Error();
			else
				tipo22 = new Bool();
			
			traductor.parchea(irf, etiqueta+1);
			traductor.emiteInstruccion("ir-a", etiqueta+2);
			traductor.emiteInstruccion("apila", 0);
			etiqueta += 2;
			
			tipo2 = RExpAnd(tipo22);
			return tipo2;
		}
		return tipo1;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Exp -> Exp1 RExp 
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo Exp() 
	{
		Tipo tipo1 = Exp1();
		Tipo tipo2 = RExp(tipo1);
		return tipo2;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		RExp -> OpOrd Exp1 RExp | null       //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo RExp(Tipo tipo1) 
	{
		if (tActual.getTipo().equals("OPORD"))
		{
			String lexema = tActual.getLexema();
			reconoce("OPORD");
			Tipo tipo2 = Exp1();
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
			etiqueta++;
			tipo2 = RExp(tipo22);
			return tipo2;
		}
		return tipo1;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Exp1 -> Exp2 RExp1 
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo Exp1()
	{
		Tipo tipo1 = Exp2();
		Tipo tipo2 = RExp1(tipo1);
		return tipo2;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		RExp1 -> OpEq Exp2 RExp1 | null             //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo RExp1(Tipo tipo1) 
	{
		if (tActual.getTipo().equals("OPEQ"))
		{
			String lexema = tActual.getLexema();
			reconoce("OPEQ");
			Tipo tipo2 = Exp2();
			Tipo tipo22;
			if (!compatibles(tipo1, tipo2))
				tipo22 = new Error();
			else
				tipo22 = new Bool();
			if (lexema.equals("!="))
				traductor.emiteInstruccion("distintos");
			else if (lexema.equals("=="))
				traductor.emiteInstruccion("iguales");
			etiqueta++;
			tipo2 = RExp1(tipo22);
			return tipo2;
		}
		return tipo1;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Exp2 -> Exp3 RExp2
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo Exp2()
	{
		Tipo tipo1 = Exp3();
		Tipo tipo2 = RExp2(tipo1);
		return tipo2;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		RExp2 -> OpSum Exp3 RExp2 | null                //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo RExp2(Tipo tipo1) 
	{
		if (tActual.getTipo().equals("OPSUM") && !tActual.getLexema().equals("||")) //Ahora el || se hace con cortocircuito.
		{
			Tipo tipoOp = tipoOpSum();
			String lexema = tActual.getLexema();
			reconoce("OPSUM");
			Tipo tipo2 = Exp3();
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
			etiqueta++;
			tipo2 = RExp2(tipo22);
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
	private Tipo Exp3()
	{
		Tipo tipo1 = Exp4();
		Tipo tipo2 = RExp3(tipo1);
		return tipo2;
	}
	
	/** M�todo para an�lisis de la expresi�n
	 * 		RExp3 -> OpMul Exp4 RExp3 | null             //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private Tipo RExp3(Tipo tipo1) 
	{
		if (tActual.getTipo().equals("OPMUL") && !tActual.getLexema().equals("&&")) //Ahora el && se hace con cortocircuito
		{
			Tipo tipoOp = tipoOpMul();
			String lexema = tActual.getLexema();
			reconoce("OPMUL");
			Tipo tipo2 = Exp4();
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
				traductor.emiteInstruccion("ir-f",etiqueta+6); //No es necesario parchear, se conoce la direccion ya.
				traductor.emiteInstruccion("Stop");
				etiqueta=etiqueta+5;
				traductor.emiteInstruccion("divide"); //La �ltima suma en etiqueta se hace fuera del else
				}
				else if (lexema.equals("MOD"))
				traductor.emiteInstruccion("modulo");
					/*else if (lexema.equals("&&"))
						traductor.emiteInstruccion("and");*/
			etiqueta++;
			tipo2 = RExp3(tipo22);
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
	private Tipo Exp4()
	{
		if (tActual.getTipo().equals("OPSUM") || tActual.getTipo().equals("OPNEG")) //Existe operador unario
		{
			Tipo tipoOp = tipoOpUn();
			Tipo tipo = Fact();
			if (tipoOp.getLexema().equals(new Bool()))
			{
				traductor.emiteInstruccion("negacion");
				etiqueta++;
			}
			else 
				if (tipoOp.getLexema().equals(new Int()))
				{
					traductor.emiteInstruccion("opuesto");  //El valor opuesto de un numero p.e. 2 opuesto de -2
					etiqueta++;
				}
			if (!compatibles(tipoOp, tipo))
				return new Error();
			else
				return tipo;
		}
		else //No existe operador unario.
		{
			Tipo tipo = Fact();
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
	private Tipo Fact()
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
			etiqueta++;
			reconoce("BOOL");
			tipo = new Bool();
		}
		else if (tActual.getTipo().equals("NUM")) //El valor de Fact es un n�mero. 
		{
			traductor.emiteInstruccion("apila", Integer.parseInt(tActual.getLexema()));
			etiqueta++;
			reconoce("NUM");
			tipo = new Int();
		}
		else if (tActual.getTipo().equals("ID")) // El valor de Fact viene determinado por una variable o constante.
		{
			tipo = Desc();
			traductor.emiteInstruccion("apila-ind");
			etiqueta++;
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
			tipo = ExpOr();
			reconoce("PAC");
		}
		else
			tipo = new Error();
		return tipo;
	}
	
	/**
	 * M�todo para el an�lisis de la expresi�n
	 * 		IIf -> IF Exp THEN Ins(0) else Ins(1)
	 * @return Boolean que indica si se ha producido un error en la expresi�n
	 */
	private boolean IIf(){
		boolean error1,error2;
		
		reconoce("IF");
		
		Tipo tipo1 = Exp();
		int etiquetaI0 = etiqueta;
		//Esta instrucci�n ser� parcheada posteriormente.
		traductor.emiteInstrucci�nParcheable("ir_f");
		etiqueta++;
		//Si la expresi�n es de tipo booleano, procedemos a comprobar el if
		if (tipo1.getLexema().equals("BOOL")){
			reconoce("THEN");
			//Generamos el c�digo de I(0). Lo reconocemos como bloque BEGIN-END.
			error1 = IComp();
			int etiquetaI1 = etiqueta;
			//De nuevo, esta instrucci�n ser� parcheada posteriormente.
			traductor.emiteInstrucci�nParcheable("ir_a");
			etiqueta++;
			//Reconocemos un ";" antes del ELSE, que ser�a el del bloque Begin-End del IF (no del else).
			reconoce("PYC");
			reconoce("ELSE");
			//Aqu� ya conocemos la direcci�n para parchear el "ir_f" (else)
			traductor.parchea(etiquetaI0,etiqueta+1);
			//Generamos el c�digo de I(1). Lo reconocemos como bloque BEGIN-END.
			error2 = IComp();
			//Aqu� ya conocemos la direcci�n para parchear el "ir_a" (fin de if)
			traductor.parchea(etiquetaI1,etiqueta+1);
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
	private boolean IWhile(){
		boolean errorIns;
		
		reconoce("WHILE");
		
		int etiquetaExp = etiqueta;
		Tipo tipo1 = Exp();
		int etiquetaIns = etiqueta;
		//Esta instrucci�n ser� parcheada posteriormente
		traductor.emiteInstrucci�nParcheable("ir_f");
		etiqueta++;
		if (tipo1.getLexema().equals("BOOL")){
			reconoce("DO");
			errorIns = IComp();
			//Esta instrucci�n ser� parcheada posteriormente
			traductor.emiteInstruccion("ir_a",etiquetaExp+1);
			etiqueta++;
			//Ahora ya conocemos el destino del primer "ir_f"
			traductor.parchea(etiquetaIns,etiqueta+1);
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
	private boolean IRepeat(){
		
		reconoce("REPEAT");
		//El repeat no precisar� de parcheos.
		int etiquetaIns = etiqueta;
		boolean errorIns = Ins();
		reconoce("UNTIL"); 
		Tipo tipo1 = Exp();
		if (tipo1.getLexema().equals("BOOL"))
		{
			traductor.emiteInstruccion("ir_f",etiquetaIns+1);
			etiqueta++;
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
	private Tipo Desc()
	{
		String id = tActual.getLexema();
		Tipo tipo = null;
		if (!ts.constainsId(id))
			tipo = new Error();
		else
		{
			traductor.emiteInstruccion("apila", ts.getToken(id).getDireccion());
			etiqueta++;
			reconoce("ID");
			tipo = RDesc(ts.getToken(id).getTipo());
		}
		return tipo;
	}

	private Tipo RDesc(Tipo tipo) 
	{
		Tipo tiporet = tipo;
		if (tActual.getTipo().equals("PUNTERO")) //Es un puntero.
		{
			String id = tActual.getLexema();
			tSimbolos.Token tk = ts.getToken(id);
			reconoce("PUNTERO");
			if (tk == null || !tk.getTipo().equals("PUNTERO")) //No se si es puntero o pointer.
				return new Error();
			else
			{
				traductor.emiteInstruccion("apila-ind");
				etiqueta++;
				tiporet = RDesc(tk.getTipo()/*.getTipoApuntado()*/); 
			}
		}
		else 
			if (tActual.getTipo().equals("PUNTO")) //Es el campo de un registro.
			{
				reconoce("PUNTO");
				String id = tActual.getLexema();
				reconoce("ID");
				/*if (!tipo.equals("REGISTRO")) //No se si es puntero o pointer.
					return new Error();
				else*/
				{
					int desplazamiento = 1;//tipo.buscar(id);
					//Tipo tipocampo = tipo.getTipo(desplazamiento);
					if (desplazamiento >= 0)
					{
						traductor.emiteInstruccion("apila",desplazamiento);
						traductor.emiteInstruccion("suma");
						etiqueta += 2;
						//tiporet = RDesc(tipocampo);
					} 
					else 
						return new Error();
				}
			}
		return tiporet;
	}
	
	/**
	 * M�todo para el an�lisis de la expresi�n
	 * 		Tips -> decTipo RTips
	 * @return Boolean que indica si se ha producido un error en la expresi�n
	 */
	private boolean Tips() {
		boolean err1 = decTipo();
		boolean err2 = RTips();
		return (err1 || err2);
	}
	
	/**
	 * M�todo para el an�lisis de la expresi�n
	 * 		RTips -> decTipo | null             //NOTA : null se refiere a lambda en la gram�tica.
	 * @return Boolean que indica si se ha producido un error en la expresi�n
	 */	
	private boolean RTips() {
		if (!tActual.getTipo().equals("FTYPE"))
			{
				reconoce("PYC");
				boolean error1 = decTipo();
				boolean error2 = RTips();
				return (error1 || error2);
			}
		return false;
	}
	
	/**
	 * M�todo para el an�lisis de la expresi�n
	 * 		decTipo -> id : tipo;             
	 * @return Boolean que indica si se ha producido un error en la expresi�n
	 */	
	private boolean decTipo() {
		String nombreTipo = tActual.getLexema();
		reconoce("ID");
		reconoce("PP");
		TipoAux tipoRetorno = tipo();
		boolean err1 = (tipoRetorno ==null);
		/*if (ts==null) {
			int j;
			j = 5;
		}*/
		ts.addTipo(nombreTipo,tipoRetorno);
		//reconoce("PYC");
		return err1;
	}
	
	/**
	 * M�todo para el an�lisis de las expresiones:
	 * tipo -> id | BOOL | INT | ^tipo | tipo
	 * @return
	 */
	private TipoAux tipo() {
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
			TipoAux apuntado= tipo();
			TipoAux puntero = new Pointer("POINTER",apuntado);
			return puntero;
		}
		else if (tActual.getTipo().equals("REGISTRO")) {
			reconoce("RECORD");
			TipoAux registro = new Record(tActual.getLexema());
			LCampos(registro);
			//Tratar el tipo Regsitro de forma adecuada
			//Seguramente no deber�a devolverse ese primer registro, no s�.
			return registro;
		}
		else if (tActual.getTipo().equals("ID")) {
			//De momento en esta versi�n se reconocen en orden los tipos, de modo que no se 
			//permite en el lado derecho utilizar un tipo que no haya sido declarado anteriormente.
			String nombreid = tActual.getLexema();
			tSimbolos.Token tok = ts.getToken(nombreid);
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
	
	private boolean LCampos(TipoAux t){
		//TipoAux registro = new Record(tActual.getLexema());
		boolean error1 = LIdent(t);
		boolean error2 = RLCampos(t);
		return (error1 || error2);
	}
	
	private boolean RLCampos(TipoAux t){
		boolean error = LCampos(t);
		return error;
	}
	
	private boolean LIdent(TipoAux t){
		boolean error;
		String id = tActual.getLexema();
		reconoce("ID");
		TipoAux tipo = tipo();
		((Record)t).a�adirCampo(id, tipo);
		error = RLIdent(t);
		return error;
	}
	
	private boolean RLIdent(TipoAux t){
		boolean error;
		/*
		if (!tActual.getTipo().equals("FRECORD"))
		{
			error = LIdent(t);
			return error;
		}
		else{
			return false;
		}
		*/
		return false;
	}
	
	private boolean INewID() {
		boolean error1=false;
		//......
		reconoce("NEW");
		reconoce("PAA"); // "("

		String lex = tActual.getLexema();
		tSimbolos.Token tSint= ts.getToken(lex);
		if (tSint!=null && !ts.esTipo(tSint)) {
			reconoce("ID");
			reconoce("PAC"); // ")"
			
			//Generar c�digo (en proceso)
			/*traductor.emiteInstruccion("apila",tSint.getDireccion());
			traductor.emiteInstruccion("apilaH");
			traductor.emiteInstruccion("desapila_ind");
			traductor.emiteInstruccion("incrementaH");*/
			
			return error1;
		}
		else {
			error1 = true;
			return error1;
		}
	}
	
}
