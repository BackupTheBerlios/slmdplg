package tSimbolos.Tipo;

import java.util.Iterator;
import java.util.LinkedList;

public class Record extends TipoAux implements Tipo {

	class Campo
	{
		public Campo(String id, Tipo tipo2) 
		{
			lexema = id;
			tipo = tipo2;
		}
		
		public String lexema;
		public Tipo tipo;
	}

	private LinkedList<Campo> listaCampos;
	
	public Record(String lexema)
	{
		super(lexema);
		nombre = "RECORD";
		this.listaCampos = new LinkedList<Campo>();
	}
	
	public boolean a�adirCampo(String id, Tipo tipo)
	{
		Iterator<Campo> iter = listaCampos.iterator();
		boolean esta = false;
		while (!esta && iter.hasNext())
		{
			esta = iter.next().lexema.equals(id);
		}
		if (!esta)
		{
			listaCampos.add(new Campo(id, tipo));
			return true;
		}
		else 
			return false;
	}
	
	public String getLexema() 
	{
		return "RECORD";
	}

	public int getTama�o() 
	{
		return calcularTama�o();
	}

	private int calcularTama�o() 
	{
		Iterator<Campo> iter = listaCampos.iterator();
		int tama�o = 0;
		while (iter.hasNext())
		{
			tama�o += iter.next().tipo.getTama�o();
		}
		return tama�o;
	}
	
	//Se comprueba que sea un registro, y adem�s que sean.
	public boolean equals(Tipo t) {
		boolean iguales1 = nombre.equals(t.getNombre());
		if (iguales1 == true) {
			boolean iguales2 = camposIguales(((Record)t).getListaCampos());
			return iguales2;
		}
		return false;
	}
	
	public boolean camposIguales(LinkedList<Campo> listaC) {
		boolean camposIguales = false;
		
		
		//.................. Por definir......
		
		
		return camposIguales;
	}

	public LinkedList<Campo> getListaCampos() {
		return listaCampos;
	}

	public void setListaCampos(LinkedList<Campo> listaCampos) {
		this.listaCampos = listaCampos;
	}

}
