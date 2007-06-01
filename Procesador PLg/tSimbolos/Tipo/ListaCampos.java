package tSimbolos.Tipo;

import java.util.LinkedList;

public class ListaCampos 
{
	private LinkedList<Campo> lista;
	private int tamaño;
	
	public ListaCampos()
	{
		lista = new LinkedList<Campo>();
		tamaño = 0;
	}

	public LinkedList<Campo> getLista() {
		return lista;
	}

	public void setLista(LinkedList<Campo> lista) {
		this.lista = lista;
	}

	public int getTamaño() {
		return tamaño;
	}

	public void setTamaño(int tamaño) {
		this.tamaño = tamaño;
	}
	
	public void evaluar_offsets()
	{
		
	}

	/**
	 * Inserta identificadores y actualiza el tamaño.
	 * @param ids Lista de identificadores a insertar.
	 * @param tipo Tipo de los elementos de la lista.
	 * @return Si hay error por identificador repetido.
	 */
	public boolean añadeIdentificadores(LinkedList<String> ids, TipoAux tipo) 
	{
		int numElementos = 0;
		String actual;
		boolean error = false;
		while (!ids.isEmpty())
		{
			actual = ids.get(0);
			ids.remove(0);
			if (lista.contains(actual))
				error = true;
			else
			{
				Campo campoNuevo = new Campo(actual, tipo);
				lista.add(campoNuevo);
				numElementos++;
			}
		}
		tamaño += numElementos * tipo.getTamaño();
		return error;
	}
}
