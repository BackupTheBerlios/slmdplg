/**
 * 
 */
package tSimbolos;

import tSimbolos.Tipo.Error;
import tSimbolos.Tipo.Tipo;

/**
 * Clase que representa los tokens, contiene la direccion, el tipo
 * de token, el tipo de datos que contiene y si es constante o variable.    
 * @author Luis Ortiz Carrillo
 */
public class Token {	
	/**
	 * Dirección en la que se encuentra el token.
	 */
	protected int direccion;
	
	/**
	 * Tipo de datos que contiene el token.
	 */
    protected Tipo tipo;
    
    /**
     * Indica si se trata de un token de tipo constante o variable.
     */
	protected int clase;
	
	/**
	 * Almacena un objeto con el valor de token.
	 */
	protected Integer valor;
	
	/**
	 * Entero que representa los tokens de clase constante.
	 */
    public static final int CONSTANTE = 0;
    
    /**
     * Entero que representa los tokens de clase variable.
     */
	public static final int VARIABLE = 1;

	/**
	 * Constructor por defecto, crea un token de clase constante,
	 * de tipo "ERROR", y cuya direccion y valor es -1. 
	 */
	public Token(){
		clase = CONSTANTE;
		tipo = new Error();
	    direccion = -1;	    	    
	    valor = new Integer(-1);
	}

	/**
	 * Método para mostrar por pantalla las caracteristicas del token.
	 * @param id Identificador relacionado con el token.
	 */
	public void imprime(String id){
		String str = this.toString();
		str += ("\nIdentificador: " + id);
		System.out.println(str);
	  }
	



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = new String ("Token:\nDireccion: " + direccion + "\nTipo: ");
		
		if (tipo == null) str += "null";
		else str += tipo; 
			
		str +="\nValor: ";			
		
		if (valor == null) str += "null";
		else str += valor.toString();
		
		str += "\nClase: ";
		
		if (clase == CONSTANTE ) str +="constante.\n" ;
		else if (clase == VARIABLE) str += "variable.\n";		
		
		return str;

	}

	/**
	 * @return Returns the clase.
	 */
	public int getClase() {
		return clase;
	}

	/**
	 * @param clase The clase to set.
	 */
	public void setClase(int clase) {
		this.clase = clase;
	}

	/**
	 * @return Returns the direccion.
	 */
	public int getDireccion() {
		return direccion;
	}

	/**
	 * @param direccion The direccion to set.
	 */
	public void setDireccion(int direccion) {
		this.direccion = direccion;
	}

	/**
	 * @return Returns the tipo.
	 */
	public Tipo getTipo() {
		return tipo;
	}

	/**
	 * @param tipo The tipo to set.
	 */
	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	/**
	 * @return Returns the valor.
	 */
	public Integer getValor() {
		return valor;
	}

	/**
	 * @param valor The valor to set.
	 */
	public void setValor(Integer valor) {
		this.valor = valor;
	}
		
}

