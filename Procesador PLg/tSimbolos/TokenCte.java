/**
 * 
 */
package tSimbolos;

import tSimbolos.Tipo.Tipo;

/**
 * Clase que hereda de la clase Token, es la implementacion especifica para
 * los tokens de constantes.
 * @author Luis Ortiz Carrillo
 *
 */

public class TokenCte extends Token {
	/**
	 * Constructor dados los parametros de valor y tipo.
	 * @param id 
	 * @param valor Objeto con el valor que ha de almacenar el token
	 * @param tipo Almacena el tipo de datos que contendra el token
	 * @param i 
	 */
	public TokenCte(String id, Integer valor, Tipo tipo, int i) {
		this.id = id;
		this.valor = valor;
		this.tipo = tipo;
		this.clase = CONSTANTE;
		this.nivel = i;
	}
	
	/**
	 * Constructor de copia, dado otro token.
	 * @param tkn Token del que copiará los datos.
	 */
	public TokenCte (TokenCte tkn){
		this.valor = tkn.valor;
		this.tipo = tkn.tipo;
		this.clase = tkn.clase;
	}

	public String toString() {
		String str = new String ("TOKEN DE CLASE CONSTANTE.\nTipo de datos: " + tipo +
				"\nValor: ");
		if (valor == null) str += "null";
		else str += valor.toString();
		str += ".\n";
		
		return str;
	}

	protected Object clone() throws CloneNotSupportedException {
		return (new TokenCte(this));
	}
	

}
