package tSimbolos.Tipo;

public class TipoAux implements Tipo {

	private String lexema;
	
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

}
