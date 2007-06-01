package tSimbolos.Tipo;

public class TipoAux implements Tipo{

	protected String lexema;
	
	public TipoAux(String lex)
	{
		lexema = lex;
	}
	
	public String getLexema() 
	{
		return lexema;
	}

	public int getTamaño() 
	{
		return 1;
	}
	
	//Por defecto sólo se compara el nombre, aunque se puede reescribir para punteros
	//o registros, donde hay que hacer más comprobaciones.
	public boolean equals(Tipo t) { 
		return lexema.equals(t.getLexema());
	}

}
