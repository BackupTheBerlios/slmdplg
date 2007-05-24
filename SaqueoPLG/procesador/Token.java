package procesador; 
/**
 * La clase <B>Token</B> define los atributos y mtodos relacionados con los token que usan nuestro lenguaje.
 * 
 * <P>Cuenta con los siguientes atributos:
 * <UL><LI><CODE>categoriaLexica:</CODE> entero que indica a que categora lexica pertenece el token.</LI>
 * <LI><CODE>lexema:</CODE> string que representa la parte lex del Token.</LI></UL></P>
 * 
 * @author Paloma de la Fuente, Jonas Andradas, Leticia Garcia y Silvia Martin
 *
 */
public class Token {
	
	/*
	 * Atributos de la clase:
	 * 
	 * categoria lexica nos indica a que categoria Lexica pertenece el Token,
	 * lexema es un string que nos da una representacion de lex del Token.
	 */
	private int categoriaLexica;
	private String lexema;
	
	/**
	 * Constructor de la clase sin parametros. Inicializa lexema a vacio y categora lexica a un valor que no indica nada.
	 */
	public Token() {
		lexema = "";
		categoriaLexica = 0;
	}
	
	/**
	 * Constructor de la clase que inicializa Token con los valores que se reciben por parametro.
	 * 
	 * @param lex String que almacena el lex del Token.
	 * @param tipo entero con el tipo del Token.
	 *  
	 */
	public Token(String lex, int tipo) {
		lexema = lex;
		categoriaLexica = tipo;
	}

	/**
	 * Accesor el atributo de la clase categoriaLexica.
	 * @return Entero que indica a que categora lexica pertenece el token.
	 */
	public int getCategoriaLexica() {
		return categoriaLexica;
	}
	
	/**
	 * Mutador el atributo de la clase categoriaLexica.
	 * @param categoriaLexica Entero que indica a que categora lexica pertenece el token.
	 */
	public void setCategoriaLexica(int categoriaLexica) {
		this.categoriaLexica = categoriaLexica;
	}
	
	/**
	 * Accesor el atributo de la clase lexema.
	 * @return String que representa la parte lex del Token.
	 */
	public String getLexema() {
		return lexema;
	}
	
	/**
	 * Mutador el atributo de la clase lexema.
	 * @param lexema String que representa la parte lex del Token.
	 */
	public void setLexema(String lexema) {
		this.lexema = lexema;
	}
	
	/**
	 * 
	 * El metodo equals compara el Token que recibe como parametro consigo mismo. Son iguales si tienen la misma categoria lexica y el mismo lexema.
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @param tk Token con el que se quiere comparar si son iguales. 
	 * @return Booleano que nos indica la igualdad o no de los dos Tokens.
	 * 
	 */
	public boolean equals(Token tk){
		boolean b = true;
		if (this.categoriaLexica == tk.getCategoriaLexica()){
			b = b && true;
		}
		else{
			b = b && false;
		}
		if (this.getLexema() == tk.getLexema()){
			b = b && true;
		}
		else{
			b = b && false;
		}
		return b;
	}
	
	/**
	 * El mtodo muestraToken pasa un token a String para poderlo mostrar por pantalla.
	 * 
	 * @return String para poder mostrar el contenido de Token.
	 */
	public String muestraToken(){
		String aux= "(";
		aux= aux.concat(lexema);
		String aux2= (new Integer(categoriaLexica)).toString();
		aux= aux.concat(" , ");
		aux= aux.concat(aux2);
		aux= aux.concat(")");
		return aux;
	}
}