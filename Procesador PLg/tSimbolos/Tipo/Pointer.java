package tSimbolos.Tipo;

public class Pointer implements Tipo 
{
	private Tipo apuntado;

	public Pointer(Tipo apuntado)
	{
		this.apuntado = apuntado;
	}
	
	public String getLexema() 
	{
		return "POINTER";
	}

	public int getTamaño() 
	{
		return 1;
	}

	
	public Tipo getTipoApuntado()
	{
		return apuntado;
	}
}
