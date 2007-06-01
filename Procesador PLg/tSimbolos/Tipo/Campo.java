package tSimbolos.Tipo;

public class Campo 
{
	private String lexema;
	private int offset;
	private Tipo tipoCampo;
	private Campo sig;
	
	public Campo(String lex, Tipo tipoC)
	{
		lexema = lex;
		offset = 0;
		tipoCampo = tipoC;
		sig = null;
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

	public Campo getSig() {
		return sig;
	}

	public void setSig(Campo sig) {
		this.sig = sig;
	}

	public Tipo getTipoCampo() {
		return tipoCampo;
	}

	public void setTipoCampo(Tipo tipoCampo) {
		this.tipoCampo = tipoCampo;
	}
	
	
}