package MaquinaVirtual;

public class Instruccion 
{
	/**
	 * Representa el tipo de instrucci�n que se va a ejecutar
	 */
	private String operacion;
	
	/**
	 * Valores necesarios para la ejecuci�n de esta instrucci�n. Su valor ser� NULL
	 * cuando la operaci�n no tenga par�metros.
	 */
	private Integer parametros;

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
	 * M�todo de acceso que devuelve el atributo par�metros
	 * @return El par�metro concreto de la isntrucci�n.
	 */
	public Integer getParametros() {
		return parametros;
	}
	
	/**
	 * M�todo mutador que modifica el atributo par�metros con el par�metro del m�todo.
	 * @param operacion Es el nuevo par�metro de la instrucci�n con que se modificar� el atributo parametros.
	 */
	public void setParametros(Integer parametros) {
		this.parametros = parametros;
	}

	/**
	 * Constructor que inicializa todos los atributos de la clase. 
	 * @param operacion Tipo de instrucci�n
	 * @param parametros Par�metros de la instrucci�n
	 */
	public Instruccion(String operacion, Integer parametros) 
	{
		this.operacion = operacion;
		this.parametros = parametros;
	}
	
	/**
	 * M�todo para pasar la informaci�n de la clase (tipo de instrucci�n y posibles par�metros) a un String
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
