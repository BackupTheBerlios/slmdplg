package MaquinaVirtual;

public class Instruccion 
{
	/**
	 * Representa el tipo de instrucci�n que se va a ejecutar
	 */
	private String operacion;
	
	/**
	 * Primeros valores necesarios para la ejecuci�n de esta instrucci�n. Su valor ser� NULL
	 * cuando la operaci�n no tenga par�metros.
	 */
	private Number parametros1;
	
	/**
	 * Segundos valores necesarios para la ejecuci�n de esta instrucci�n. Su valor ser� NULL
	 * cuando la operaci�n no tenga par�metros.
	 */
	private Number parametros2;	

	/**
	 * M�todo de acceso que devuelve el atributo operaci�n
	 * @return El tipo de instrucci�n que se va a ejecutar
	 */
	public String getOperacion() {
		return operacion;
	}

	/**
	 * M�todo mutador que modifica el atributo operaci�n con el par�metro del m�todo.
	 * @param operacion Es el tipo de instrucci�n con que se modificar� el atributo operaci�n.
	 */
	public void setOperacion(String operacion) {
		this.operacion = operacion;
	}

	/**
	 * M�todo de acceso que devuelve el atributo par�metros1
	 * @return El primer par�metro concreto de la instrucci�n.
	 */
	public Number getParametros1() {
		return parametros1;
	}
	
	/**
	 * M�todo de acceso que devuelve el atributo par�metros
	 * @return El segundo par�metro concreto de la instrucci�n.
	 */
	public Number getParametros2() {
		return parametros2;
	}	
	
	/**
	 * M�todo mutador que modifica el atributo par�metros1 con el primer par�metro del m�todo.
	 * @param operacion Es el nuevo par�metro de la instrucci�n con que se modificar� el atributo parametros1.
	 */
	public void setParametros1(Integer param) {
		this.parametros1 = param;
	}
	
	/**
	 * M�todo mutador que modifica el atributo par�metros2 con el segundo par�metro del m�todo.
	 * @param operacion Es el nuevo par�metro de la instrucci�n con que se modificar� el atributo parametros2.
	 */
	public void setParametros2(Integer param) {
		this.parametros2 = param;
	}	

	/**
	 * Constructor que inicializa todos los atributos de la clase. 
	 * @param operacion Tipo de instrucci�n
	 * @param param1 Primer par�metro de la instrucci�n
	 * @param param2 Segundo par�metro de la instrucci�n
	 */
	public Instruccion(String operacion, Number param1,Number param2) 
	{
		this.operacion = operacion;
		this.parametros1 = param1;
		this.parametros2 = param2;
	}
	
	/**
	 * M�todo no implementado
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
