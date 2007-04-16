package Analizador_Sintactico;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import tSimbolos.TablaSimbolos;
import tSimbolos.TokenVar;
import Analizador_Lexico.AnLexico;
import Analizador_Lexico.Token;

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
	
	/** Variable global que se encarga de ir almacenando el c�digo seg�n se va 
	 * pasando por las diversas reglas de la gram�tica. */
	private StringBuffer codigo;

	/** Constructor de la clase. Inicializa los diversos atributos a partir
	 *  del fichero que se le pasa como par�metro y comienza el an�lisis
	 *  sint�ctico con la llamada al metodo prog(). 
	 *  @param fichero Objeto de tipo FileReader asociado al fichero que se quiere compilar.*/
	public AnSintactico(FileReader fichero)
	{
		anLex = new AnLexico(fichero);
		ts = new TablaSimbolos();
		tActual = anLex.analizador();
		direcci�n = 0;
		progr();
	} 

	/** M�todo que inicia el an�lisis sint�ctico, es decir el axioma de la grm�tica.
	 * Se corresponde con la regla
	 * 		Prog -> Decs BEGIN Ins END*/
	private void progr()
	{
		codigo = new StringBuffer("");
		Decs();
		reconoce("BEGIN");
		boolean error = Ins();
		reconoce("END");
		codigo.append("end;");
		if (error)
		{
			System.out.println("Hay errores sint�cticos en el c�digo. No se ha podido completar la compilaci�n.");
		}
		else
		{
			guardarCodigo("codigo.txt");
			System.out.println("Compilaci�n completada con �xito.");
		}
			
	}
	
	/** M�todo que se utiliza para guardar el c�digo en el fichero 
	 * cuya ruta se pasa como par�metro.
	 * @param ruta La ruta del archivo en el que quiere guardarse el c�digo en lenguaje objeto.*/
	private void guardarCodigo(String ruta)
	{
		try
		{
			File archivo = new File(ruta);
			FileWriter archivo2 = new FileWriter(archivo);
			archivo2.write(codigo.toString());
			archivo2.close();
		}
		catch (IOException excepcion){}
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
	 * 		Decs -> Dec RDecs  */
	private void Decs()
	{
		Dec();
		RDecs();
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		RDecs -> ; Dec RDecs | null      //NOTA : null se refiere a lambda en la gram�tica.
	 * */  
	private void RDecs()
	{
		if (!tActual.getTipo().equals("BEGIN"))
		{
			reconoce("PYC");
			Dec();
			RDecs();
		}
	}

	/** M�todo para an�lisis de las expresiones:
	 * 		Dec -> CONST Tipo ID = Val
	 * 		Dec -> Tipo ID */
	private void Dec()
	{
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
			else if (valor.equals("TRUE"))
				valor = "1";
			if (compatibles(tipo, tActual.getTipo()) && !ts.constainsId(nomconst))
			{
				ts.addCte(nomconst, valor, tipo);
				reconoce(tActual.getTipo());
			}
			else
			{
				ts.addCte(nomconst, valor, "ERROR");
				reconoce(tActual.getTipo());
			}
		}
		else
		{
			String tipo = tActual.getLexema();
			reconoce("TIPO");
			Ids(tipo);
		}
	}
	
	/** M�todo para an�lisis de la expresi�n:
	 * 		Ids -> ID RIds  */
	private void Ids(String tipo)
	{
		String id = tActual.getLexema();
		reconoce("ID");
		if (!ts.constainsId(id))
			ts.addVar(id, direcci�n++, tipo);
		else
			ts.addVar(id, direcci�n++, "ERROR");
		
		RIds(tipo);
	}

	/** M�todo para an�lisis de la expresi�n:
	 * 		RIds -> , ID RIds | null         //NOTA : null se refiere a lambda en la gram�tica.
	 * @param tipo El tipo de las variables que se est�n declarando.*/
	private void RIds(String tipo)
	{
		if (!tActual.getTipo().equals("PYC") && !tActual.getTipo().equals("BEGIN"))
		{
			reconoce("COMA");
			String id = tActual.getLexema();
			reconoce("ID");
			if (!ts.constainsId(id))
				ts.addVar(id, direcci�n++, tipo);
			else
				ts.addVar(id, direcci�n++, "ERROR");
			
			RIds(tipo);
		}
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
	 * 		I -> ID = Exp 
	 * @return Un booleano informando de si ha habido un error contextual en la instucci�n.*/
	private boolean I() 
	{
		String id = tActual.getLexema();
		reconoce("ID");
		reconoce("IGUAL");
		String tipo1 = Exp();
		if (ts.constainsId(id) && !(ts.getToken(id).getClase()== tSimbolos.Token.CONSTANTE))
		{
			codigo.append("desapila-dir(" + ts.getToken(id).getDireccion() + ");\n");
			return (tipo1.equals("ERROR") || !compatibles (tipo1, ts.getToken(id).getTipo()));
		}
		else
			return true;
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
				codigo.append("menor;\n");
			else if (lexema.equals(">"))
				codigo.append("mayor;\n");
				else if (lexema.equals("<="))
					codigo.append("menoroigual;\n");
					else if (lexema.equals(">="))
						codigo.append("mayoroigual;\n");
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
				codigo.append("distintos;\n");
			else if (lexema.equals("=="))
				codigo.append("iguales;\n");
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
		if (tActual.getTipo().equals("OPSUM"))
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
				codigo.append("suma;\n");
			else if (lexema.equals("-"))
				codigo.append("resta;\n");
				else if (lexema.equals("||"))
					codigo.append("or;\n");
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
		if (tActual.getTipo().equals("OPMUL"))
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
				codigo.append("multiplica;\n");
			else if (lexema.equals("/"))
				codigo.append("divide;\n");
				else if (lexema.equals("MOD"))
				codigo.append("modulo;\n");
					else if (lexema.equals("&&"))
						codigo.append("and;\n");
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
				codigo.append("negacion;\n");
			}
			else 
				if (tipoOp.equals("NUM"))
				{
					codigo.append("opuesto;\n");  //El valor opuesto de un numero p.e. 2 opuesto de -2
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
			String bool;
			if (tActual.getLexema().equals("TRUE"))
				bool = "1";
			else
				bool = "0";
			codigo.append("apila(" + bool + ");\n");
			reconoce("BOOL");
			tipo = "BOOL";
		}
		else if (tActual.getTipo().equals("NUM")) //El valor de Fact es un n�mero. 
		{
			codigo.append("apila(" + tActual.getLexema() + ");\n");
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
					codigo.append("apila-dir(" + ts.getToken(id).getDireccion() + ");\n");
				}
				else  //id representa una constante.
				{
					if (ts.getToken(id).getTipo().equals("BOOL"))
						if (ts.getToken(id).getValor().equals("TRUE"))
							codigo.append("apila(1);\n");
						else
							codigo.append("apila(0);\n");
					else
						codigo.append("apila(" + ts.getToken(id).getValor() + ");\n");
				}
				tipo = ts.getToken(id).getTipo();
				reconoce("ID");
			}
		}
		else if (tActual.getTipo().equals("PAA")) //El valor de Fact viene dado por una espresi�n parentizada.
		{
			reconoce("PAA");
			tipo = Exp();
			reconoce("PAC");
		}
		else
			tipo = "ERROR";
		return tipo;
	}
}
