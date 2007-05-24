package tablaSimbolos;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * La clase <B>TablaSimbolos</B> define los atributos y mtodos relacionados con la tabla de smbolos.
 * <P>Esta clase cuenta con el siguiente atributo:
 * <UL><LI><CODE>tabla:</CODE> Usaremos del API de JAVA una tabla Hash como tabla de smbolos. El valor que almacenamos ser un par con el nombre del identificador y el tipo del mismo. 
 * No se podrn almacenar datos repetidos.</LI></UL></P>
 * 
 * @author Jons Andradas, Paloma de la Fuente, Leticia Garca y Silvia Martn
 *
 */
public class TablaSimbolos {
	
	/*
	 * Atributos de la clase:
	 * 
	 * Usaremos del API de java una tabla Hash como tabla de simbolos. El valor 
	 * que almacenamos sera un par con el nombre del identificador y el tipo del 
	 * mismo. No se podran almacenar datos repetidos.
	 */
	Hashtable tabla;

	/**
	 * Constructor de la clase sin parmetros que inicializa la tabla de smbolos con una tabla vaca.
	 */
	public TablaSimbolos() {
		super();
		this.tabla = new Hashtable();
	}

	/**
	 * Constructor de la clase que recibe una tabla de smbolos por parametro y la usa para inicializar la tabla de smbolos creada.
	 * 
	 * @param tabla Hashtable recibida por parmetro y a la que se inicializa la nueva tabla creada.
	 */
	public TablaSimbolos(Hashtable tabla) {
		super();
		this.tabla = new Hashtable(tabla);
	}

	public TablaSimbolos(Hashtable t, Vector params) throws Exception{
		this.tabla = new Hashtable();
		Enumeration e = t.keys();
		while (e.hasMoreElements()){ //copiamos
			Object aux = e.nextElement();
			Par elem = new Par ((Par)t.get(aux));
			this.tabla.put(elem.getId(),elem);
		}
		e = t.keys();
		while (e.hasMoreElements()){ //los pasamos a valor
			Object aux = e.nextElement();
			if (((Par)this.tabla.get(aux)).getClase().equals("var")){
				((Par)this.tabla.get(aux)).setClase("valor");
			}
		}
		for (int i = 0; i<params.size(); i++){ //si los del vector están en la ts
			Par p = (Par)params.elementAt(i);
			String aux = p.getId();
			if (this.existeID(aux)){ //los ponemos a ver
				((Par)this.tabla.get(aux)).setClase("var");
			}	
			else{ //sino los añadimos
				this.agnadeID(p.getId(),p.getProps(),"var",p.getDir(),p.getNivel());
			}
		}
	}
	
	/**
	 * Accesor para el atributo de la clase tabla.
	 * @return Hashtable recibida por parmetro y a la que se inicializa la nueva tabla creada.
	 */
	public Hashtable getTabla() {
		return tabla;
	}
	
	/**
	 * Mutador para el atributo de la clase tabla.
	 * @param tabla Hashtable recibida por parmetro y a la que se inicializa la nueva tabla creada.
	 */
	public void setTabla(Hashtable tabla) {
		this.tabla = tabla;
	}

	/**
	 * El mtodo existeID discrimina si un identificador 'id' de tipo 't' existe en la tabla de smbolos, 
	 * solo si existe su clave. La clave de cada identificador de un tipo es nica, no habr repeticiones.
	 *
	 * @param id String que almacena el nombre del identificador 
	 * @return Booleano que indica la existencia o no de este valor en la tabla.
	 */
	public boolean existeID(String id){	
		return this.tabla.containsKey(id);
	}
	
	/**
	 * Este mtodo devuelve el tipo de un identificador que le llega por prametro. Y el resultado lo devuelve como un String.
	 * @param id Identificador sobre el cual se va a buscar su tipo.
	 * @return String que devuelve el tipo asociado al id que entra como parmetro.
	 */
	public Atributos getProps(String id){
		if (this.tabla.containsKey(id))
			return ((Par)this.tabla.get(id)).getProps();
		else
			return null;
	}
	
	public TablaSimbolos getTS(String id){
		if (this.tabla.containsKey(id))
			return ((Par)this.tabla.get(id)).getT();
		else
			return null;
	}
	
	/**
	 * Accesor de la direccin de un identificador en la tabla de smbolos
	 * @param id String con el identificador
	 * @return el entero con la direccin
	 */
	public int getDir(String id){
		if (this.tabla.containsKey(id))
			return ((Par)this.tabla.get(id)).getDir();// Devolver la dir...
		else
			return -1;
	}
	/**
	 * Accesor de la clase de un identificador en la tabla de smbolos
	 * @param id String con el identificador
	 * @return String con el nombre de la clase del identificador
	 */
	public String getClase(String id){
		if (this.tabla.containsKey(id))
			return ((Par)this.tabla.get(id)).getClase();// Devolver la clase...
		else
			return "";
	}

	/**
	 * El mtodo agnadeID aade un identificador 'id' de tipo 't' a la tabla si no existe ningun identificador con ese nombre 
	 * El valor de la clave en la tabla hash es znico, ya que concatenamos el nombre del identificador con el tipo usando 
	 * entre medias un carcter de separacion: '#'. Como valor para la tabla introducimos un par, que son el nombre del id 
	 * y el tipo. Si ya existe el par entonces no es posible aadir y lanzamos una excepcin
	 * 
	 * @param id String que tienen almacenado el nombre del identificador
	 * @param t String que tienen almacenado el tipo del identificador
	 * @return Booleano que indica si la operacin se realizo bien.
	 * @exception Exception Porque no se puede duplicar un identificador
	 * 
	 */
	public boolean agnadeID(String id, Atributos t, String clase, int dir, int nivel) throws Exception{
		if (this.tabla.containsKey(id)){
			throw new Exception ("No se puede duplicar el identificador");
		}
		else{
			Par p = new Par(id, t, clase, dir, nivel);
			this.tabla.put(id,p);
			return true;	
		}	
	}
	
	/**
	 * 
	 * @param id
	 * @param t
	 * @param clase
	 * @param dir
	 * @param nivel
	 * @param TS
	 * @return boolean
	 * @throws Exception
	 */
	public boolean agnadeID(String id, Atributos t, String clase, int dir, int nivel, TablaSimbolos TS) throws Exception{
		if (this.tabla.containsKey(id)){
			throw new Exception ("No se puede duplicar el identificador");
		}
		else{
			Par p = new Par(id, t, clase, dir, nivel,TS);
			this.tabla.put(id,p);
			return true;	
		}	
	}
	
	/**
	 * El mtodo eliminaID elimina un identificador 'id' de la tabla si existe. 
	 * Si no, lanzara una excepcion.
	 * 
	 * @param id String que tienen almacenado el nombre del identificador
	 * @return Booleano que indica si la operacin se realizo bien.
	 * @exception  Exception Porque no se encuentra el par a eliminar
	 * 
	 */
	public boolean eliminaID(String id) throws Exception{
		if (!this.tabla.containsKey(id)){
			throw new Exception("No se puede eliminar el identificador: no existe");
		}
		else{
			this.tabla.remove(id);
			return true;	
		}	
	}
	
	/**
	 * El mtodo muestra se usa para mostrar por pantalla la tabla de smbolos actual.
	 */
	public void muestra(){
		String aux = this.tabla.toString();
		System.out.println(aux);
	}

	/**
	 * El mC)todo dirI obtiene la posicin de memoria del identificador "id" proporcionado como parametro.
	 * Es necesario para saber, cuando generemos el cdigo, de dnde tomar los valores necesarios.
	 *  
     * @param id String que tienen almacenado el nombre del identificador
	 * @return i int que nos indica la posicin en memoria del identificador que indica si la operacin se realizo bien.
	 * 
	 *  
	 */
	public int dirID(String id){
		int i = 0;
		for (Enumeration e = this.tabla.keys(); e.hasMoreElements();){
			String s = (String)e.nextElement();
			if(s.equals(id)){
				return i;
			}
			i ++;
	     }
		return i;
	}	
	
	/**
	 * Mtodo que resuelve la referencia de tipos de identificadores.
	 * Recibe como parmetro exp que es del tipo Atributos
	 */
	public Atributos ref(Atributos exp){
		if (exp.getTipo().equals("ref")){
			String iden = exp.getTbase().getTipo();
			if (iden.equals("bool") || iden.equals("int") || iden.equals("pointer") || iden.equals("array")){
				return exp.getTbase();
			} 
			else if(this.existeID( iden )){
				return ref( this.getProps(iden) );
			}
			else{
				Atributos a = new Atributos("err","",0,0, new Vector());
				return a;
			}
		}
		else if (existeID(exp.getTipo())){
			return ref(getProps(exp.getTipo()));
			
		}
		else{
			return exp;
		}
		
	}
	
	/**
	 * Mtodo que devuelve el booleano correspondiente a evaluar la condicin de si el Par que se le pasa como parmetro es un referencia errnea
	 */
	public boolean referenciaErronea(Par e){
		return e.getProps().getTipo().equals("ref") && !this.existeID(e.getId());
	}
	/**
	 * Funcin que devuelve el booleano correspondiente a evaluar si dos Atributos son compatibles
	 * @param e1 Atributo
	 * @param e2 Atributo
	 * @return Booleano
	 */
	public boolean compatibles (Atributos e1, Atributos e2)
	{
		Vector visitadas = new Vector();
		return compatibles2 (visitadas, e1, e2);
	}
	
	/**
	 * Funcin que devuelve el booleano correspondiente a evaluar si los Atributos que contienen los vectores son compatibles.
	 * @param e1 Vector
	 * @param e2 Vector
	 * @return Booleano
	 */
	public boolean compatibles (Vector e1, Vector e2)
	{
		boolean compatible=true;
		int i;
		if (e1.size()!=e2.size()) return false;
		for (i=0 ; i< e1.size(); i++){
			if (!compatibles (((Par)e1.get(i)).getProps(), ((Par)e2.get(i)).getProps())){
				return false;
			}
		}
		return compatible;
	}
	
	/**
	 * Funcin auxliar para implementar la funcin compatibles
	 * @param v Vector
	 * @param e1 Atributos
	 * @param e2 Atributos
	 * @return el entero que dice si son compatibles
	 */

	public boolean compatibles2 (Vector v, Atributos e1, Atributos e2)
	{
		if (v.contains(e1) && v.contains(e2)){
			return true;
		}
		else{
			v.addElement(e1);
			v.addElement(e2);
		}
		if ((e1.getTipo().equals(e2.getTipo()) && e2.getTipo().equals("int"))
				|| (e1.getTipo().equals(e2.getTipo()) && e2.getTipo().equals("bool"))){
			return true;
		}
		else if (e1.getTipo().equals("ref"))
				return compatibles2 (v, e1.getTbase(), e2);
			else if (e2.getTipo().equals("ref")) 
				return compatibles2 (v, e1, e2.getTbase());	
			else if (((e1.getTipo()==e2.getTipo() && e2.getTipo().equals("array")) && (e1.getElems() == e2.getElems())))
						return compatibles2 (v, e1.getTbase(), e2.getTbase());
				else if (e1.getTipo().equals("puntero") && e2.getTipo().equals("puntero"))
						return compatibles2(v, e1.getTbase(), e2.getTbase());
					else
						return false;
	}
}