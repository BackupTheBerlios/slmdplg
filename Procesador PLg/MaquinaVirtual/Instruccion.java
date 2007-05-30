package MaquinaVirtual;

public class Instruccion 
{
	/**
	 * Representa el tipo de instrucción que se va a ejecutar
	 */
	private String operacion;
	
	/**
	 * Primeros valores necesarios para la ejecución de esta instrucción. Su valor será NULL
	 * cuando la operación no tenga parámetros.
	 */
	private Number parametros1;
	
	/**
	 * Segundos valores necesarios para la ejecución de esta instrucción. Su valor será NULL
	 * cuando la operación no tenga parámetros.
	 */
	private Number parametros2;	

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
	 * Método de acceso que devuelve el atributo parámetros1
	 * @return El primer parámetro concreto de la instrucción.
	 */
	public Number getParametros1() {
		return parametros1;
	}
	
	/**
	 * Método de acceso que devuelve el atributo parámetros
	 * @return El segundo parámetro concreto de la instrucción.
	 */
	public Number getParametros2() {
		return parametros2;
	}	
	
	/**
	 * Método mutador que modifica el atributo parámetros1 con el primer parámetro del método.
	 * @param operacion Es el nuevo parámetro de la instrucción con que se modificará el atributo parametros1.
	 */
	public void setParametros1(Integer param) {
		this.parametros1 = param;
	}
	
	/**
	 * Método mutador que modifica el atributo parámetros2 con el segundo parámetro del método.
	 * @param operacion Es el nuevo parámetro de la instrucción con que se modificará el atributo parametros2.
	 */
	public void setParametros2(Integer param) {
		this.parametros2 = param;
	}	

	/**
	 * Constructor que inicializa todos los atributos de la clase. 
	 * @param operacion Tipo de instrucción
	 * @param param1 Primer parámetro de la instrucción
	 * @param param2 Segundo parámetro de la instrucción
	 */
	public Instruccion(String operacion, Number param1,Number param2) 
	{
		this.operacion = operacion;
		this.parametros1 = param1;
		this.parametros2 = param2;
	}
	
	/**
	 * Método no implementado
	 */
	public String toString() {
		return new String(operacion);		
		/*
		String s;
		if (operacion.equals("apila") 
				|| operacion.equals("apila-dir") 
				|| operacion.equals("desapila-dir") )
			s=new String(""+operacion+"("+parametros.intValue()+")");
		else 
			s=new String(""+operacion+"()");
		return s;
		*/
	}
	
}
