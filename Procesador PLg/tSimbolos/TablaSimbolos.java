/**
 * 
 */
package tSimbolos;


import java.util.Hashtable;

/**
 * Clase que representa la tabla de simbolos, cuenta con los m�todos
 * necesarios para realizar las modificaciones necesarias, est� implementada
 * a trav�s de una tabla hash.
 * @author Luis Ortiz Carrillo
 */
public class TablaSimbolos {

	/**
	 * Se trata de la Tabla Hash donde se almacenar� la informaci�n necesaria,
	 * para realizar las operaiones sobre la tabla de simbolos.
	 */
	private Hashtable<String,Token> tabla;
	
	/**
	 * Constructor por defecto.
	 *
	 */
	public TablaSimbolos() { 
		tabla = new Hashtable<String,Token>(); 
	}
	
	/**
	 * Constructor por copia. Realiza una copia de la HashTable
	 * de la TablaSimbolos pasada.
	 * @param t TablaSimbolos que utilizar� para copiar su HashTable.
	 * Si la Hashtable de la TablaSimbolos pasada es null, se creara una
	 * nueva tabla de simbolos vacia.
	 */
	@SuppressWarnings("unchecked")
	public TablaSimbolos (TablaSimbolos t){
		if (t.tabla != null)tabla = (Hashtable<String, Token>) t.tabla.clone();
		else tabla = new Hashtable<String,Token>();
	}

	/**
	 * M�todo para a�adir un token de tipo variable, dados el identificador,
	 * la direcci�n en la que se encuentra y el tipo de datos que
	 * contiene, en el propio m�todo es donde se construye el token.
	 * @param id Identificador para el token a construir.
	 * @param dir Direcci�n en la que se encuentra el token.
	 * @param tipo Tipo de datos que contiene.
	 * @return Devuelve el token asociado a ese identificador previamente
	 * en la tabla de simbolos, si no habia ninguno devuelve null.
	 */
    public Token addVar(String id, int dir, String tipo){
    	if (id == null) return null;
    	else return tabla.put(id, new TokenVar(dir, tipo));
	}

	/**
	 * M�todo para a�adir un token de tipo variable, dados el identificador,
	 * el valor y el tipo de datos que contiene.En el propio m�todo es 
	 * donde se construye el token.
	 * @param id Identificador para el token a construir.
	 * @param valor Objeto que contendra el valor del token.
	 * @param tipo Tipo de datos que contiene.
	 * @return Devuelve el token asociado a ese identificador previamente
	 * en la tabla de simbolos, si no habia ninguno devuelve null.
	 */
    public Token addCte(String id, Integer valor, String tipo){
    	if (id == null) return null;
    	else return tabla.put(id, new TokenCte(valor, tipo));
	}

    
    /**
     * M�todo que indica si existe alg�n token relacionado con el
     * identificador proporcionado en la tabla de simbolos.
     * @param id Identificador que se va a consultar si est�.
     * @return True si hay alguno, False en caso contrario.
     */
    public boolean constainsId(String id){
    	if (id == null) return false;
    	else return tabla.containsKey(id);
	}

    /**
     * M�todo para obtener el token relacionado con el identificador
     * proporcionado.
     * @param id Identificador para el que se buscara su token relacionado.
     * @return Token relacionado con el identificador id, en caso que no
     * exiwsta ninguna entrada para est� en la tabla null.
     */
    public Token getToken(String id){
    	if (id == null) return null;
    	else return tabla.get(id);
   }
    
    /**
     * M�todo para eliminar un identificador y su token asociado de la
     * tabla de simbolos. Devuelve el token que tenia asociado el
     * identificador previamente a su eliminaci�n. Si el identificador no
     * est� en la tabla no hace nada.
     * @param id Identificador que se desea eliminar junto con su
     * token asociado.
     * @return El token asociado previamente a ese identificador,
     * null si no habia ninguno.
     */
	public Token remove (String id){
		if (id == null) return null;
		else return tabla.remove(id);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {		
		return new TablaSimbolos(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof TablaSimbolos){
			return tabla.equals(((TablaSimbolos)arg0).tabla);
		}
		else return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return tabla.toString();
	}
	
	/**
	 * Indica el n�mero de entradas en la tabla.
	 * @return el n�mero de entradas en la tabla.
	 */
	public int size(){		
		if (tabla == null) return 0;
		else return tabla.size();
	}
	
}
