package tSimbolos.Tipo;

public class Bool extends TipoAux implements Tipo {
	
	public Bool(/*String lex*/) {
		super("BOOL");
	}

	public String getLexema() 
	{
		return lexema;
	}

	public int getTama�o() 
	{
		return 1;
	}
	
	//No tendr�a por qu� sobreescribirlo, se hace por uniformidad.
	public boolean equals(Tipo t) {
		//return t instanceof Bool;
		return lexema.equals(t.getLexema());
	}
	
	public int getTama�oTotal() 
	{
		return 1;
	}

}
