package tSimbolos.Tipo;

public class Int extends TipoAux implements Tipo {
	
	public Int(/*String lex*/) {
		super("INT");
	}

	public String getLexema() 
	{
		return "INT";
	}

	public int getTama�o() 
	{
		return 1;
	}
	
	//No tendr�a por qu� sobreescribirlo, se hace por uniformidad.
	public boolean equals(Tipo t) {
		//return t instanceof Int;
		return lexema.equals(t.getLexema());
	}

}
