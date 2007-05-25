package tSimbolos.Tipo;

public class Pointer extends TipoAux implements Tipo
{
	private TipoAux apuntado;

	public Pointer(String lex, TipoAux apuntado)
	{
		super(lex);
		nombre = "POINTER";
		this.apuntado = apuntado;
	}
	
	public String getLexema() 
	{
		return "POINTER";
	}

	public int getTama�o() 
	{
		return 1+apuntado.getTama�o();
	}

	public TipoAux getTipoApuntado()
	{
		return apuntado;
	}
	
	//Se comprueba que sea un puntero, y adem�s recursivamente que apunten al mismo tipo.
	public boolean equals(Tipo t) {
		boolean iguales1 = lexema.equals(t.getLexema());
		//boolean iguales1 = t.getNombre().equals("POINTER");
		if (iguales1 == true) {
			boolean iguales2 = apuntado.equals(((Pointer)t).getTipoApuntado());
			return iguales2;
		}
		else
			return false;
	}
}
