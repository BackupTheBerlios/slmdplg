package tSimbolos.Tipo;

public class Bool extends TipoAux implements Tipo {
	
	public Bool(/*String lex*/) {
		super("BOOL");
		this.nombre = "BOOL";
	}

	public String getLexema() 
	{
		return nombre;
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

}
