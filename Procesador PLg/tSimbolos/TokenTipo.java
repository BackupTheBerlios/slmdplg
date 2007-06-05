package tSimbolos;

import tSimbolos.Tipo.TipoAux;

public class TokenTipo extends Token {

	TipoAux tipoExpresionTipos;
	
	/**
	 * Constructor dados los parametros de valor y tipo.
	 * @param id 
	 * @param valor Objeto con el valor que ha de almacenar el token
	 * @param tipo Almacena el tipo de datos que contendra el token
	 */
	public TokenTipo(String id, TipoAux expresionTipos) {
		this.id = id;
		this.tipo = expresionTipos; //No debería hacer falta, pero puesto en principio por si es más facil que funcione así.
		this.clase = TIPO;
		this.tipoExpresionTipos = expresionTipos;
	}

	
	public TipoAux getTipoExpresionTipos() {
		return tipoExpresionTipos;
	}

}
