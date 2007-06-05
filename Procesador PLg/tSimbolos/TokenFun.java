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
public class TokenFun extends Token 
{

	private int etiqueta;

	/**
	 * Constructor de la clase dados sus parametros.
	 * @param id 
	 * @param direccion Direccion en la que se encuentra el token.
	 * @param tipo Tipo de datos que contiene el token.
	 */
	public TokenFun(String id, int etiqueta_comienzo, Tipo tipo, int nivel) 
	{
		this.tipo = tipo;
		this.etiqueta = etiqueta_comienzo;
		this.clase = FUNCTION;
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
	public TokenFun (TokenFun tkn){
		this.tipo = tkn.tipo;
		this.clase = VARIABLE;
	}
	
	
	public String toString() {
		return new String ("TOKEN DE CLASE VARIABLE.\nTipo de datos: " + tipo +
				"\nDireccion: " + direccion + ".\n");
	}

	
	protected Object clone() throws CloneNotSupportedException {
		return (new TokenFun(this));
	}

	public int getEtiqueta() {
		return etiqueta;
	}

	public void setEtiqueta(int etiqueta) {
		this.etiqueta = etiqueta;
	}
	
}
