package tSimbolos.Tipo;

import java.util.Iterator;
import java.util.LinkedList;

public class Record implements Tipo {

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
	
	public Record()
	{
		this.listaCampos = new LinkedList<Campo>();
	}
	
	public boolean aņadirCampo(String id, Tipo tipo)
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

	public int getTamaņo() 
	{
		return calcularTamaņo();
	}

	private int calcularTamaņo() 
	{
		Iterator<Campo> iter = listaCampos.iterator();
		int tamaņo = 0;
		while (iter.hasNext())
		{
			tamaņo += iter.next().tipo.getTamaņo();
		}
		return tamaņo;
	}

}
