package tablaSimbolos;
/**
 * La clase <B>Par</B> define los atributos y mtodos relacionados con los identificadores reconocidos. Los elementos de esta clase sern almacenados en la tabla de smbolos
 * <P>Los atributos que tiene esta clase son los siguientes:
 * <UL><LI><CODE>id:</CODE> String que almacena el nombre del identificador</LI>
 * <LI><CODE>tipo:</CODE> String que almacena el tipo del identificador.</LI></UL></P>
 * 
 * @author Jons Andradas, Paloma de la Fuente, Leticia Garca y Silvia Martn
 *
 */
public class Par {
	
	/*
	 * Atributos de la clase:
	 * 
	 * id almacena el nombre del identificador,
	 * tipo es un string que almacena el tipo del identificador.
	 */
	
	String id;
	Atributos props;
	String clase;
	int dir;
	int nivel;
	TablaSimbolos t;
	
	/**
	 * 
	 * Constructor de la clase sin parametros.
	 */
	public Par() {
		super();
		this.id = "";
		this.props = new Atributos();
		this.clase = "";
		this.dir = 0;
		this.nivel = 0;
		this.t = null;
	}

	/**
	 * Constructor de la clase Par con valores de inicializacin recibidos por parmetro
	 * 
	 * @param id String con el nombre del identificador.
	 * @param clase String con la clase del identificador.
	 *  
	 */
	public Par(String id, Atributos props, String clase, int dir, int n) {
		super();
		this.id = id;
		this.props = props;
		this.clase = clase;
		this.dir = dir;
		this.nivel = n;
		this.t = null;
	}
	
	/**
	 * Constructor de la clase Par con valores de inicializacin recibidos por parmetro
	 * @param id
	 * @param props
	 * @param clase
	 * @param dir
	 * @param n
	 * @param ts
	 * @throws Exception
	 */
	public Par(String id, Atributos props, String clase, int dir, int n, TablaSimbolos ts) throws Exception{
		super();
		this.id = id;
		this.props = props;
		this.clase = clase;
		this.dir = dir;
		this.nivel = n;
		this.t = new TablaSimbolos (ts.getTabla());
	}

	/**
	 * 
	 * @param a
	 * @throws Exception
	 */
	public Par(Par a) throws Exception{
		super();
		this.id = a.getId();
		this.props = a.getProps();
		this.clase = a.getClase();
		this.dir = a.getDir();
		this.nivel = a.getNivel();
		this.t = a.getT();
	}
	
	/**
	 * Accesor el atributo de la clase id.
	 * @return String con el nombre del identificador.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Mutador el atributo de la clase id.
	 * @param id String con el nombre del identificador.
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Accesor el atributo de la clase props.
	 * @return String con el props del identificador.
	 */
	public Atributos getProps() {
		return props;
	}
	
	/**
	 * Mutador el atributo de la clase props.
	 * @param props String con el props del identificador.
	 */
	public void setProps(Atributos props) {
		this.props = props;
	}
	
	/**
	 * Accesor el atributo de la clase clae.
	 * @return String con el clase del identificador.
	 */
	public String getClase() {
		return clase;
	}
	
	/**
	 * Mutador el atributo de la clase clase.
	 * @param clase String con el nombre del identificador.
	 */
	public void setClase(String clase) {
		this.clase = clase;
	}

	/**
	 * Accesor el atributo de la clase dir.
	 * @return entero con la direccin del identificador.
	 */
	public int getDir() {
		return dir;
	}
	
	/**
	 * Mutador de la direccin
	 * @param dir entero con la direccin.
	 */
	public void setDir(int dir) {
		this.dir = dir;
	}
	
	/**
	 * Accesor de la tabla de smbolos
	 * @return Tabla de smbolos
	 */
	public TablaSimbolos getT() {
		return t;
	}

	/**
	 * Mutador el atributo de la tabla de smbolos
	 * @param t TablaSmbolos
	 */
	public void setT(TablaSimbolos t) {
		this.t = t;
	}

	/**
	 * @return Returns the nivel.
	 */
	public int getNivel() {
		return nivel;
	}

	/**
	 * @param nivel The nivel to set.
	 */
	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	/**
	 * El mtodo toString pasa a String la clase Par para poder mostrarla por pantalla.
	 * @return String que contiene un par para poderlo mostrar por pantalla.
	 */
	public String toString(){
		String aux = "(";
		aux = aux.concat(id);
		aux = aux.concat(",");
		aux = aux.concat(props.toString());
		aux = aux.concat(",");
		aux = aux.concat(clase);
		aux = aux.concat(",");
		aux = aux.concat((new Integer(dir)).toString());
		aux = aux.concat(")");
		aux = aux.concat("\n");
		return aux;
	}
}