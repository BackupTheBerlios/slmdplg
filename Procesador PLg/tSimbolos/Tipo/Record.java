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
	
	public boolean añadirCampo(String id, Tipo tipo)
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

	public int getTamaño() 
	{
		return calcularTamaño();
	}

	private int calcularTamaño() 
	{
		Iterator<Campo> iter = listaCampos.iterator();
		int tamaño = 0;
		while (iter.hasNext())
		{
			tamaño += iter.next().tipo.getTamaño();
		}
		return tamaño;
	}

}
