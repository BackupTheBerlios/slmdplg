/**
 * 
 */
package tSimbolos;

/**
 * Clase que hereda de la clase Token, es la implementacion especifica para
 * los tokens de constantes.
 * @author Luis Ortiz Carrillo
 *
 */

public class TokenCte extends Token {
	/**
	 * Constructor dados los parametros de valor y tipo.
	 * @param valor Objeto con el valor que ha de almacenar el token
	 * @param tipo Almacena el tipo de datos que contendra el token
	 */
	public TokenCte(Object valor, String tipo) {
		this.valor = valor;
		this.tipo = tipo;
		this.clase = CONSTANTE;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof TokenCte){
			if (this.valor.equals(((TokenCte)arg0).getValor()) &&
					this.tipo.equals(((TokenCte)arg0).getValor()) &&
					this.clase == ((TokenCte)arg0).clase ) return true;
			else return false;
		}
		else return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = new String ("TOKEN DE CLASE CONSTANTE.\nTipo de datos: " + tipo +
				"\nValor: ");
		if (valor == null) str += "null";
		else str += valor.toString();
		str += ".\n";
		
		return str;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return (new TokenCte(this));
	}
	

}
