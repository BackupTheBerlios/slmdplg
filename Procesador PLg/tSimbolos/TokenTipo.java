package tSimbolos;

import tSimbolos.Tipo.Tipo;
import tSimbolos.Tipo.TipoAux;

public class TokenTipo extends Token {

	TipoAux tipoExpresionTipos;
	
	/**
	 * Constructor dados los parametros de valor y tipo.
	 * @param valor Objeto con el valor que ha de almacenar el token
	 * @param tipo Almacena el tipo de datos que contendra el token
	 */
	public TokenTipo(TipoAux expresionTipos) {
		//this.valor = valor;
		this.tipo = expresionTipos; //No deber�a hacer falta, pero puesto en principio por si es m�s facil que funcione as�.
		this.clase = TIPO;
		this.tipoExpresionTipos = expresionTipos;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public TipoAux getTipoExpresionTipos() {
		return tipoExpresionTipos;
	}

}
