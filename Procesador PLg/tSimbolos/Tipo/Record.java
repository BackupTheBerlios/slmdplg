package tSimbolos.Tipo;


public class Record extends TipoAux implements Tipo {


	private ListaCampos listaCampos;
	private int tama�o;
	
	public Record(String lexema, ListaCampos listaCampos)
	{
		super(lexema);
		this.listaCampos = listaCampos;
		tama�o = listaCampos.getTama�o();
	}
	
	public String getLexema() 
	{
		return "RECORD";
	}

	public ListaCampos getListaCampos() {
		return listaCampos;
	}

	public void setListaCampos(ListaCampos listaCampos) {
		this.listaCampos = listaCampos;
	}

	public int getTama�o() {
		return tama�o;
	}

	public void setTama�o(int tama�o) {
		this.tama�o = tama�o;
	}
	
	public int getTama�oTotal() {
		return tama�o;
	}
}
