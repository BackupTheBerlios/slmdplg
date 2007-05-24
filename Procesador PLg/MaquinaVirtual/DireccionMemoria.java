package MaquinaVirtual;
/**
 * Clase que guarda la informaci�n necesaria en ejecuci�n de cada variable declarada
 * en el programa que se est� ejecutando (formalmente, una direcci�n de memoria y su dato).
 * @author Grupo5 PLG
 * @deprecated En la segunda parte de la practica se hace innecesaria esta clase.
 */
public class DireccionMemoria 
{
	/**
	 * Valor (num�rico) de la variable.
	 */
	private Number dato;

	/**
	 * Valor de la direcci�n donde se almacena la variable
	 */
	private Integer direccion;

	/**
	 * M�todo de acceso que devuelve el atributo dato
	 * @return Valor de la variable.
	 */
	public Number getDato() {
		return dato;
	}

	/**
	 * M�todo mutador que modifica el atributo dato con un nuevo valor de la variable
	 * @param dato Valor de la variable.
	 */
	public void setDato(Number dato) {
		this.dato = dato;
	}

	/**
	 * M�todo de acceso que devuelve el atributo direccion.
	 * @return Valor de la vduirecci�n donde se almacena la variable.
	 */
	public Integer getDireccion() {
		return direccion;
	}
	/**
	 * M�todo mutador que modifica el atributo direcci�n con una nueva direcci�n de la variable
	 * @param direccion Valor de la direcci�n de la variable.
	 */
	public void setDireccion(Integer direccion) {
		this.direccion = direccion;
	}

	/**
	 * Constructor que inicializa todos los atributos de la clase
	 * @param dato Valor de la variable.
	 * @param direccion Direcci�n de la variable.
	 */
	public DireccionMemoria(Number dato, Integer direccion) {
		super();
		this.dato = dato;
		this.direccion = direccion;
	}
}
