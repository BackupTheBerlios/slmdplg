/**
 * 
 */
package tSimbolos;

/**
 * Clase que hereda de la clase Token, es la implementacion especifica para los
 * tokens de variables.
 * 
 * @author Luis Ortiz Carrillo
 * 
 */
public class TokenVar extends Token {

	/**
	 * Constructor de la clase dados sus parametros.
	 * @param direccion Direccion en la que se encuentra el token.
	 * @param tipo Tipo de datos que contiene el token.
	 */
	public TokenVar(int direccion, String tipo) {
		this.tipo = tipo;
		this.direccion = direccion;
		this.clase = VARIABLE;
	}

	/**
	 * Constructor de copia para la clase
	 * @param tkn Token del que copiará sus parametros.
	 */
	public TokenVar (TokenVar tkn){
		this.tipo = tkn.tipo;
		this.direccion = tkn.direccion;
		this.clase = VARIABLE;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof TokenVar){
			if (this.direccion == ((TokenVar)arg0).direccion &&
					this.tipo.equals(((TokenVar)arg0).getValor()) &&
					this.clase == ((TokenVar)arg0).clase ) return true;
			else return false;
		}
		else return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new String ("TOKEN DE CLASE VARIABLE.\nTipo de datos: " + tipo +
				"\nDireccion: " + direccion + ".\n");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return (new TokenVar(this));
	}
	
}
