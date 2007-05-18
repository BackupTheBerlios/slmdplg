package tSimbolos.Tipo;

public class TipoAux implements Tipo{

	protected String lexema;
	
	protected String nombre;
	
	public TipoAux(String lex)
	{
		lexema = lex;
	}
	
	public String getLexema() 
	{
		return lexema;
	}

	public int getTama�o() 
	{
		return 1;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	//Por defecto s�lo se compara el nombre, aunque se puede reescribir para punteros
	//o registros, donde hay que hacer m�s comprobaciones.
	public boolean equals(Tipo t) { 
		return lexema.equals(t.getLexema());
	}

}
