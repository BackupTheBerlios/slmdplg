package tSimbolos.Tipo;


public class Record extends TipoAux implements Tipo {


	private ListaCampos listaCampos;
	private int tamaño;
	
	public Record(String lexema, ListaCampos listaCampos)
	{
		super(lexema);
		this.listaCampos = listaCampos;
		tamaño = listaCampos.getTamaño();
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

	public int getTamaño() {
		return tamaño;
	}

	public void setTamaño(int tamaño) {
		this.tamaño = tamaño;
	}
	
	public int getTamañoTotal() {
		return tamaño;
	}
}
