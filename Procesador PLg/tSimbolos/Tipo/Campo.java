package tSimbolos.Tipo;

public class Campo 
{
	private String lexema;
	private int offset;
	private Tipo tipoCampo;
	
	public Campo(String lex, Tipo tipoC)
	{
		lexema = lex;
		offset = 0;
		tipoCampo = tipoC;
	}

	public String getLexema() {
		return lexema;
	}

	public void setLexema(String lexema) {
		this.lexema = lexema;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public Tipo getTipoCampo() {
		return tipoCampo;
	}

	public void setTipoCampo(Tipo tipoCampo) {
		this.tipoCampo = tipoCampo;
	}
	
	
}