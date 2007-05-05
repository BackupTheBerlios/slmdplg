package Analizador_Sintactico;

import java.io.FileReader;

import tSimbolos.TablaSimbolos;
import tSimbolos.TokenVar;
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
			if (!tActual.getTipo().equals("END"))
				tActual = anLex.analizador();
			else
				tActual = null;
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
	private boolean compatibles (String tipo1, String tipo2)
	{
		return ((tipo1.equals(tipo2)) || (tipo1.equals("INT") && tipo2.equals("NUM")) || (tipo2.equals("INT") && tipo1.equals("NUM")));
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
			String tipo = tActual.getLexema();
			reconoce("TIPO");
			String nomconst = tActual.getLexema();
			reconoce("ID");
			reconoce("IGUAL");
			String valor = tActual.getLexema();
			if (valor.equals("FALSE"))
				valor = "0";
			else 
				if (valor.equals("TRUE"))
					valor = "1";
			if (compatibles(tipo, tActual.getTipo()) && !ts.constainsId(nomconst))
			{
				ts.addCte(nomconst, new Integer(valor), tipo);
				reconoce(tActual.getTipo());
				error = false;
			}
			else
			{
				ts.addCte(nomconst, new Integer(0), "ERROR");
				reconoce(tActual.getTipo());
				error = true;
			}
	}
		else
		{
			String tipo = tActual.getLexema();
			reconoce("TIPO");
			error = Ids(tipo);
		}
		return error;
	}
	
	/** M�todo para an�lisis de la expresi�n:
	 * 		Ids -> ID RIds    
	 * @return Indica si ha habido un error duranta la funci�n.*/
	private boolean Ids(String tipo)
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
			ts.addVar(id, direcci�n++, "ERROR");
			error1 = true;
		}
		boolean error2 = RIds(tipo);
		return (error1 || error2);
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		RIds -> , ID RIds | null         //NOTA : null se refiere a lambda en la gram�tica.
	 * @param tipo El tipo de las variables que se est�n declarando.  
	 * @return Indica si ha habido un error duranta la funci�n.*/
	private boolean RIds(String tipo)
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
				ts.addVar(id, direcci�n++, "ERROR");
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
		if (!tActual.getTipo().equals("END"))
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
	 * 		I -> IAsig | IComp
	 * @return Un booleano informando de si ha habido un error contextual en la instucci�n.*/
	private boolean I() 
	{
		if (tActual.getLexema().equals("BEGIN"))
			return IComp();
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
		String id = tActual.getLexema();
		reconoce("ID");
		reconoce("IGUAL");
		String tipo1 = ExpOr();
		if (ts.constainsId(id) && !(ts.getToken(id).getClase()== tSimbolos.Token.CONSTANTE))
		{
			traductor.emiteInstruccion("desapila-dir", ts.getToken(id).getDireccion());
			etiqueta++;
			return (tipo1.equals("ERROR") || !compatibles (tipo1, ts.getToken(id).getTipo()));
		}
		else
			return IAsig();
	}

	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		ExpOr -> ExpAnd RExpOr
	 * @return El tipo de la expresi�n
	 */
	private String ExpOr() 
	{
		String tipo1 = ExpAnd();
		String tipo2 = RExpOr(tipo1);
		return tipo2;
	}
	
	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		RExpOr -> || ExpAnd RExpOr | null    //NOTA : null se refiere a lambda en la gram�tica.
	 * @return El tipo de la expresi�n
	 */
	private String RExpOr(String tipo1)
	{
		if (tActual.getLexema().equals("||"))
		{
			reconoce("OPSUM");
			
			int irv = etiqueta + 1;
			traductor.emiteInstruccion("copia");
			traductor.emiteInstrucci�nParcheable("ir-v");
			traductor.emiteInstruccion("desapila");
			etiqueta += 3;
			
			String tipo2 = ExpAnd();
			String tipo22;
			if (!tipo1.equals("BOOL") || !tipo2.equals("BOOL"))
				tipo22 = "ERROR";
			else
				tipo22 = "BOOL";

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
	private String ExpAnd()
	{
		String tipo1 = Exp();
		String tipo2 = RExpAnd(tipo1);
		return tipo2;
	}
	
	/**
	 * M�todo para an�lisis de la expresi�n
	 * 		RExpAnd -> && Exp RExpAnd | null    //NOTA : null se refiere a lambda en la gram�tica.
	 * @return El tipo de la expresi�n
	 */
	private String RExpAnd(String tipo1)
	{
		if (tActual.getLexema().equals("&&"))
		{
			reconoce("OPMUL");

			int irf = etiqueta;
			traductor.emiteInstrucci�nParcheable("ir-f");
			etiqueta++;
			
			String tipo2 = Exp();
			
			String tipo22;
			if (!tipo1.equals("BOOL") || !tipo2.equals("BOOL"))
				tipo22 = "ERROR";
			else
				tipo22 = "BOOL";
			
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
	private String Exp() 
	{
		String tipo1 = Exp1();
		String tipo2 = RExp(tipo1);
		return tipo2;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		RExp -> OpOrd Exp1 RExp | null       //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private String RExp(String tipo1) 
	{
		if (tActual.getTipo().equals("OPORD"))
		{
			String lexema = tActual.getLexema();
			reconoce("OPORD");
			String tipo2 = Exp1();
			String tipo22;
			if (!compatibles(tipo1,"INT") || !compatibles(tipo2,"INT"))
				tipo22 = "ERROR";
			else
				tipo22 = "BOOL";
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
	private String Exp1()
	{
		String tipo1 = Exp2();
		String tipo2 = RExp1(tipo1);
		return tipo2;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		RExp1 -> OpEq Exp2 RExp1 | null             //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private String RExp1(String tipo1) 
	{
		if (tActual.getTipo().equals("OPEQ"))
		{
			String lexema = tActual.getLexema();
			reconoce("OPEQ");
			String tipo2 = Exp2();
			String tipo22;
			if (!compatibles(tipo1, tipo2))
				tipo22 = "ERROR";
			else
				tipo22 = "BOOL";
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
	private String Exp2()
	{
		String tipo1 = Exp3();
		String tipo2 = RExp2(tipo1);
		return tipo2;
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		RExp2 -> OpSum Exp3 RExp2 | null                //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private String RExp2(String tipo1) 
	{
		if (tActual.getTipo().equals("OPSUM") && !tActual.getLexema().equals("||")) //Ahora el || se hace con cortocircuito.
		{
			String tipoOp = tipoOpSum();
			String lexema = tActual.getLexema();
			reconoce("OPSUM");
			String tipo2 = Exp3();
			String tipo22;
			if (!compatibles(tipo1, tipo2) || !compatibles(tipo1, tipoOp))
				tipo22 = "ERROR";
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
	private String tipoOpSum()
	{
		if (tActual.getLexema().equals("+") || tActual.getLexema().equals("-"))
			return "INT";
		else
			if (tActual.getLexema().equals("||"))
				return "BOOL";
			else
				return "ERROR";
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Exp3 -> Exp4 RExp3 
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private String Exp3()
	{
		String tipo1 = Exp4();
		String tipo2 = RExp3(tipo1);
		return tipo2;
	}
	
	/** M�todo para an�lisis de la expresi�n
	 * 		RExp3 -> OpMul Exp4 RExp3 | null             //NOTA : null se refiere a lambda en la gram�tica.
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private String RExp3(String tipo1) 
	{
		if (tActual.getTipo().equals("OPMUL") && !tActual.getLexema().equals("&&")) //Ahora el && se hace con cortocircuito
		{
			String tipoOp = tipoOpMul();
			String lexema = tActual.getLexema();
			reconoce("OPMUL");
			String tipo2 = Exp4();
			String tipo22;
			if (!compatibles(tipo1,tipo2) || !compatibles(tipo1, tipoOp))
				tipo22 = "ERROR";
			else
				tipo22 = tipo1;
			if (lexema.equals("*"))
				traductor.emiteInstruccion("multiplica");
			else if (lexema.equals("/")) {
				//A�adido para controlar las excepciones debidas a divisiones por cero.
				traductor.emiteInstruccion("copia");
				traductor.emiteInstruccion("apila", 0);
				traductor.emiteInstruccion("iguales");
				traductor.emiteInstruccion("ir-f",etiqueta+6); //No es necesario aprchear, se conoce la direccion ya.
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
	private String tipoOpMul()
	{
		if (tActual.getLexema().equals("*") || tActual.getLexema().equals("/") || tActual.getLexema().equals("MOD"))
			return "INT";
		else
			if (tActual.getLexema().equals("&&"))
				return "BOOL";
			else
				return "ERROR";
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Exp4 -> OpUn Fact | Fact 
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private String Exp4()
	{
		if (tActual.getTipo().equals("OPSUM") || tActual.getTipo().equals("OPNEG")) //Existe operador unario
		{
			String tipoOp = tipoOpUn();
			String tipo = Fact();
			if (tipoOp.equals("BOOL"))
			{
				traductor.emiteInstruccion("negacion");
				etiqueta++;
			}
			else 
				if (tipoOp.equals("NUM"))
				{
					traductor.emiteInstruccion("opuesto");  //El valor opuesto de un numero p.e. 2 opuesto de -2
					etiqueta++;
				}
			if (!compatibles(tipoOp, tipo))
				return "ERROR";
			else
				return tipo;
		}
		else //No existe operador unario.
		{
			String tipo = Fact();
			return tipo;
		}
	}
	
	/** M�todo que se encarga de comprobar el tipo de un operador de la clase OpUn. 
	 * @return INT, en caso de que sea -, BOOL, en caso de que sea ! o ERROR en otro caso.*/
	private String tipoOpUn()
	{
		if (tActual.getLexema().equals("-"))
		{	
			reconoce("OPSUM");
			return "NUM";           
		}
		else
			if (tActual.getLexema().equals("!"))
			{
				reconoce("OPNEG");
				return "BOOL";
			}
			else
				return "ERROR";     
	}

	/** M�todo para an�lisis de la expresi�n
	 * 		Fact -> TRUE
	 * 		Fact -> FALSE
	 * 		Fact -> NUM
	 * 		Fact -> ID
	 * 		Fact -> ( Exp )        (Si llega esto vuelve a llamar a Exp)
	 * A�ade al c�digo la instrucci�n de la operaci�n correspondiente.
	 * @return El tipo de la expresi�n resultante o ERROR si ha tenido lugar un error contextual en la expresi�n.*/
	private String Fact()
	{
		String tipo;
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
			tipo = "BOOL";
		}
		else if (tActual.getTipo().equals("NUM")) //El valor de Fact es un n�mero. 
		{
			traductor.emiteInstruccion("apila", Integer.parseInt(tActual.getLexema()));
			etiqueta++;
			reconoce("NUM");
			tipo = "NUM";
		}
		else if (tActual.getTipo().equals("ID")) // El valor de Fact viene determinado por una variable o constante.
		{
			String id = tActual.getLexema();
			if (!ts.constainsId(id))
				tipo = "ERROR";
			else
			{
				if (ts.getToken(id) instanceof TokenVar) // id representa una variable.
				{
					traductor.emiteInstruccion("apila-dir", ts.getToken(id).getDireccion());
					etiqueta++;
				}
				else  //id representa una constante.
				{
					if (ts.getToken(id).getTipo().equals("BOOL"))
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
			}
		}
		else if (tActual.getTipo().equals("PAA")) //El valor de Fact viene dado por una espresi�n parentizada.
		{
			reconoce("PAA");
			tipo = ExpOr();
			reconoce("PAC");
		}
		else
			tipo = "ERROR";
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
		
		String tipo1 = Exp();
		int etiquetaI0 = etiqueta; //!\ Duda �lex: �La etiqueta es la actual o la siguiente que se va a insertar?
		//Esta instrucci�n ser� parcheada posteriormente.
		traductor.emiteInstruccion("ir_f",999);
		etiqueta++;
		//Si la expresi�n es de tipo booleano, procedemos a comprobar el if
		if (tipo1 == "BOOL"){
			//Generamos el c�digo de I(0)
			error1 = Ins();
			int etiquetaI1 = etiqueta;
			//De nuevo, esta instrucci�n ser� parcheada posteriormente.
			traductor.emiteInstruccion("ir_a",999);
			etiqueta++;
			//Aqu� ya conocemos la direcci�n para parchear el "ir_f" (else)
			traductor.parchea(etiquetaI0,etiqueta);
			//Generamos el c�digo de I(1)
			error2 = Ins();
			//Aqu� ya conocemos la direcci�n para parchear el "ir_a" (fin de if)
			traductor.parchea(etiquetaI1,etiqueta);
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
		String tipo1 = Exp();
		int etiquetaIns = etiqueta;
		//Esta instrucci�n ser� parcheada posteriormente
		traductor.emiteInstruccion("ir_f",999);
		etiqueta++;
		if (tipo1 == "BOOL"){
			errorIns = Ins();
			//Esta instrucci�n ser� parcheada posteriormente
			traductor.emiteInstruccion("ir_a",etiquetaExp);
			etiqueta++;
			//Ahora ya conocemos el destino del primer "ir_f"
			traductor.parchea(etiquetaIns,etiqueta);
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
		
		reconoce("REPEAT"); //!\ �lex: creo que no est� en el anLexico.
		
		int etiquetaIns = etiqueta;
		boolean errorIns = Ins();
		reconoce("UNTIL"); //!\ �lex: de nuevo creo que no est� en el anL�xico.
		String tipo1 = Exp();
		if (tipo1 == "BOOL"){
			traductor.emiteInstruccion("ir_f",etiquetaIns);
			etiqueta++;
		} else {
			return true;
		}
		
		return errorIns;
	}
}
