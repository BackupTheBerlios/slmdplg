package MaquinaVirtual;

public class Instruccion 
{
	/**
	 * Representa el tipo de instrucción que se va a ejecutar
	 */
	private String operacion;
	
	/**
	 * Valores necesarios para la ejecución de esta instrucción. Su valor será NULL
	 * cuando la operación no tenga parámetros.
	 */
	private Integer parametros;

	/**
	 * Método de acceso que devuelve el atributo operación
	 * @return El tipo de instrucción que se va a ejecutar
	 */
	public String getOperacion() {
		return operacion;
	}

	/**
	 * Método mutador que modifica el atributo operación con el parámetro del método.
	 * @param operacion Es el tipo de instrucción con que se modificará el atributo operación.
	 */
	public void setOperacion(String operacion) {
		this.operacion = operacion;
	}

	/**
	 * Método de acceso que devuelve el atributo parámetros
	 * @return El parámetro concreto de la isntrucción.
	 */
	public Integer getParametros() {
		return parametros;
	}
	
	/**
	 * Método mutador que modifica el atributo parámetros con el parámetro del método.
	 * @param operacion Es el nuevo parámetro de la instrucción con que se modificará el atributo parametros.
	 */
	public void setParametros(Integer parametros) {
		this.parametros = parametros;
	}

	/**
	 * Constructor que inicializa todos los atributos de la clase. 
	 * @param operacion Tipo de instrucción
	 * @param parametros Parámetros de la instrucción
	 */
	public Instruccion(String operacion, Integer parametros) 
	{
		this.operacion = operacion;
		this.parametros = parametros;
	}
	
	/**
	 * Método para pasar la información de la clase (tipo de instrucción y posibles parámetros) a un String
	 */
	public String toString() {
		String s;
		if (operacion.equals("apila") 
				|| operacion.equals("apila-dir") 
				|| operacion.equals("desapila-dir") )
			s=new String(""+operacion+"("+parametros.intValue()+")");
		else 
			s=new String(""+operacion+"()");
		return s;
	}
	
}
