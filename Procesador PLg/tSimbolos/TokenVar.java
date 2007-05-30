/**
 * 
 */
package tSimbolos;

import tSimbolos.Tipo.Pointer;
import tSimbolos.Tipo.Tipo;

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
	 * @param id 
	 * @param direccion Direccion en la que se encuentra el token.
	 * @param tipo Tipo de datos que contiene el token.
	 */
	public TokenVar(String id, int direccion, Tipo tipo, int nivel) {
		this.id = id;
		this.tipo = tipo;
		this.direccion = direccion;
		this.clase = VARIABLE;
		if (tipo instanceof Pointer)
			instanciada = 0;
		else
			instanciada=1;
		this.nivel = nivel;
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
