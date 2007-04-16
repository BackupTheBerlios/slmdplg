package Analizador_Lexico;

public class Token {
	
	/**
	 * Atributo que indica la categoria lexica del token
	 */
	private String tipo; 
	/**
	 * Atributo que indice el lexema del token
	 */
	private String lexema;
	
	/**
	 * Indica la línea donde se encontró el token
	 */
	private int linea;
	
	/**
	 * Metodo para obtener el valor de linea
	 * @return
	 */
	public int getLinea() {
		return linea;
	}
	
	/**
	 * Metodo para modificar el valor de linea
	 * @return
	 */
	public void setLinea(int linea) {
		this.linea = linea;
	}
	/**
	 * Metodo para obtener el valor de lexema
	 * @return
	 */
	public String getLexema() {
		return lexema;
	}
	/**
	 * Metodo para modificar el valor de lexema
	 * @param _lexema
	 */
	public void setLexema(String _lexema) {
		this.lexema = _lexema;
	}
	/**
	 * Metodo  para obtener el valor de tipo
	 * @return
	 */
	public String getTipo() {
		return tipo;
	}
	/**
	 * Metodo para modificar el valor de tipo
	 * @param _tipo
	 */
	public void setTipo(String _tipo) {
		this.tipo = _tipo;
	}
}
