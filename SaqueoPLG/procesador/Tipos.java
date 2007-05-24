package procesador;
/**
 * La clase <B>Tipos</B> declara las categoras lexicas que nuestro lenguaje puede reconocer. Cada una de ellas es un valor entero.
 * <P>No tiene atributos, slo las constantes de los tipos.</P>
 * 
 * @author Jonas Andradas, Paloma de la Fuente, Leticia Garcia y Silvia Martin
 *
 */
public class Tipos {
	
	/*
	 * Declaracion de categorias lexicas, cada categoria lexica reconocida
	 * sera un valor entero.
	 */
	
	/**
	 * Valor para reconocer numeros enteros.
	 */
	public static final int TKNUM 		= 1;
	
	/**
	 * Valor para reconocer un tipo.
	 */
	public static final int TKTIPO 		= 2; 
	
	/**
	 * Valor para reconocer un identificador.
	 */
	public static final int TKIDEN 		= 3; 
	
	/**
	 * Valor para reconocer una asignacion.
	 */
	public static final int TKASIGN	 	= 4; 
	
	/**
	 * Valor para reconocer un punto y coma ';'.
	 */
	public static final int TKPYCOMA		= 5; 
	
	/**
	 * Valor para reconocer un signo de suma '+'.
	 */
	public static final int TKSUMA		= 6; 
	
	/**
	 * Valor para reconocer un signo de resta '-'.
	 */
	public static final int TKRESTA		= 7; 
	
	/**
	 * Valor para reconocer un signo de multiplicacion '*'.
	 */
	public static final int TKMULT		= 8; 
	
	/**
	 * Valor para reconocer un signo de division '/'.
	 */
	public static final int TKDIV	 	= 9; 
	
	/**
	 * valor para reconocer un parentesis de apertura '('.
	 */
	public static final int TKPAP		= 10; 
	
	/**
	 * Valor para reconocer un parentesis de cierre ')'.
	 */
	public static final int TKPCI		= 11; 
	
	/**
	 * Valor para reconocer un menor o igual '<='.
	 */
	public static final int TKMENIG		= 12;
	
	/**
	 * Valor para reconocer un menor '<'.
	 */
	public static final int TKMEN		= 13;
	
	/**
	 * Valor para reconocer un mayor o igual '=>'.
	 */
	public static final int TKMAYIG		= 14;
	
	/**
	 * Valor para reconocer un mayor '>'.
	 */
	public static final int TKMAY		= 15;
	
	/**
	 * Valor para reconocer un igual '='.
	 */
	public static final int TKIG			= 16;
	
	/**
	 * Valor para reconocer el operador 'and'.
	 */
	public static final int TKAND		= 17;
	
	/**
	 * Valor para reconocer el operador 'or'.
	 */
	public static final int TKOR			= 18;
	
	/**
	 * Valor para reconocer el operador 'not'.
	 */
	public static final int TKNOT		= 19;
	
	/**
	 * Valor para reconocer el valor booleano 'true'.
	 */
	public static final int TKTRUE		= 20;
	
	/**
	 * Valor para reconocer el valor booleano 'false'.
	 */
	public static final int TKFALSE		= 21;
	
	/**
	 * Valor para reconocer el tipo int.
	 */
	public static final int TKINT		= 22;
	
	/**
	 * Valor para reconocer el tipo bool.
	 */
	public static final int TKBOOL		= 23;
	
	/**
	 * Valor para reconocer caracter que indica el final de las declaraciones '#'.
	 */
	public static final int TKCUA		= 24;
	
	/**
	 * Valor para reconocer el operador de distinto.
	 */
	public static final int TKDIF		= 25;
	
	/**
	 * Valor para reconocer la palabra reservada 'begin'.
	 */
	public static final int TKBEG		= 26;

	/**
	 * Valor para reconocer la palabra reservada 'end'.
	 */
	public static final int TKEND		= 27;
	
	/**
	 * Valor para reconocer la palabra reservada 'do'.
	 */
	public static final int TKDO		= 28;
	
	/**
	 * Valor para reconocer la palabra reservada 'while'.
	 */
	public static final int TKWHL		= 29;
	
	/**
	 * Valor para reconocer la palabra reservada 'if'.
	 */
	public static final int TKIF		= 30;
	
	/**
	 * Valor para reconocer la palabra reservada 'else'.
	 */
	public static final int TKELS		= 31;
	
	/**
	 * Valor para reconocer la palabra reservada 'then'.
	 */
	public static final int TKTHN		= 32;
	
	/**
	 * Valor para reconocer el final del fichero.
	 */
	public static final int TKFF		= 33;	
	
	/**
	 * Valor para reconocer la palabra array.
	 */
	public static final int TKARRAY		= 34;	
	
	/**
	 * Valor para reconocer la palabra of.
	 */
	public static final int TKOF		= 35;	
	
	/**
	 * Valor para reconocer la palabra pointer.
	 */
	public static final int TKPUNT		= 36;
	
	/**
	 * Valor para reconocer la palabra memdir.
	 */
	public static final int TKMEMDIR		= 37;
	
	/**
	 * Valor para reconocer la palabra proc.
	 */
	public static final int TKPROC		= 38;
	
	/**
	 * Valor para reconocer el caracter '['
	 */
	public static final int TKCAP		= 39;
	
	/**
	 * Valor para reconocer el caracter ']'
	 */
	public static final int TKCCI		= 40;
	
	/**
	 * Valor para reconocer la palabra "new".
	 */
	public static final int TKNEW		= 41;
	
	/**
	 * Valor para reconocer la palabra "del".
	 */
	public static final int TKDEL		= 42;
	
	/**
	 * Valor para reconocer el caracter ','
	 */
	public static final int TKCOMA		= 43;
	
	/**
	 * Valor para reconocer el caracter '{'
	 */
	public static final int TKLLAP		= 44;
	
	/**
	 * Valor para reconocer el caracter '}'
	 */
	public static final int TKLLCI		= 45;
}