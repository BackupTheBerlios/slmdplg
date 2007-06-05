package tSimbolos.Tipo;

public class Error extends TipoAux implements Tipo {

	public Error() {
		super("ERROR");
		// TODO Auto-generated constructor stub
	}

	public String getLexema() 
	{
		return "ERROR";
	}

	public int getTamaño() 
	{
		return Integer.MAX_VALUE; //Devuelve un valor que no se utilizará.
	}

	public String getNombre() {
		return null;
	}
	
	//Hereda el equals por defecto de TipoAux (no hace falta redefinirlo)

	public int getTamañoTotal() 
	{
		return Integer.MAX_VALUE; //Devuelve un valor que no se utilizará.
	}
}
