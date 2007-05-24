package MaquinaVirtual;
/**
 * Clase que guarda la información necesaria en ejecución de cada variable declarada
 * en el programa que se está ejecutando (formalmente, una dirección de memoria y su dato).
 * @author Grupo5 PLG
 * @deprecated En la segunda parte de la practica se hace innecesaria esta clase.
 */
public class DireccionMemoria 
{
	/**
	 * Valor (numérico) de la variable.
	 */
	private Number dato;

	/**
	 * Valor de la dirección donde se almacena la variable
	 */
	private Integer direccion;

	/**
	 * Método de acceso que devuelve el atributo dato
	 * @return Valor de la variable.
	 */
	public Number getDato() {
		return dato;
	}

	/**
	 * Método mutador que modifica el atributo dato con un nuevo valor de la variable
	 * @param dato Valor de la variable.
	 */
	public void setDato(Number dato) {
		this.dato = dato;
	}

	/**
	 * Método de acceso que devuelve el atributo direccion.
	 * @return Valor de la vduirección donde se almacena la variable.
	 */
	public Integer getDireccion() {
		return direccion;
	}
	/**
	 * Método mutador que modifica el atributo dirección con una nueva dirección de la variable
	 * @param direccion Valor de la dirección de la variable.
	 */
	public void setDireccion(Integer direccion) {
		this.direccion = direccion;
	}

	/**
	 * Constructor que inicializa todos los atributos de la clase
	 * @param dato Valor de la variable.
	 * @param direccion Dirección de la variable.
	 */
	public DireccionMemoria(Number dato, Integer direccion) {
		super();
		this.dato = dato;
		this.direccion = direccion;
	}
}
