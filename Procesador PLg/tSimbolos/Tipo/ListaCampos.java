package tSimbolos.Tipo;

import java.util.Iterator;
import java.util.LinkedList;

public class ListaCampos 
{
	private LinkedList<Campo> lista;
	private int tama�o;
	
	public ListaCampos()
	{
		lista = new LinkedList<Campo>();
		tama�o = 0;
	}

	public LinkedList<Campo> getLista() {
		return lista;
	}

	public void setLista(LinkedList<Campo> lista) {
		this.lista = lista;
	}

	public int getTama�o() {
		return tama�o;
	}

	public void setTama�o(int tama�o) {
		this.tama�o = tama�o;
	}
	
	/**
	 * Recorre la lista de campos, actualizando los offsets
	 * dependiendo de los elementos que tiene delante.
	 */
	public void evaluar_offsets()
	{
		int offset = 0;
		Iterator<Campo> it = lista.iterator();
		Campo c;
		while (it.hasNext())
		{
			c = it.next();
			c.setOffset(offset);
			offset += c.getTipoCampo().getTama�o();
		}
	}

	/**
	 * Inserta identificadores y actualiza el tama�o.
	 * @param ids Lista de identificadores a insertar.
	 * @param tipo Tipo de los elementos de la lista.
	 * @return Si hay error por identificador repetido.
	 */
	public boolean a�adeIdentificadores(LinkedList<String> ids, TipoAux tipo) 
	{
		int numElementos = 0;
		String actual;
		boolean error = false;
		while (!ids.isEmpty())
		{
			actual = ids.get(0);
			ids.remove(0);
			if (getCampo(actual) != null)
				error = true;
			else
			{
				Campo campoNuevo = new Campo(actual, tipo);
				lista.add(campoNuevo);
				numElementos++;
			}
		}
		tama�o += numElementos * tipo.getTama�o();
		return error;
	}

	public Campo getCampo(String actual) 
	{
		Iterator<Campo> it = lista.iterator();
		Campo c;
		while (it.hasNext())
		{
			c = it.next();
			if (c.getLexema().equals(actual))
				return c;
		}
		return null;
	}
}
