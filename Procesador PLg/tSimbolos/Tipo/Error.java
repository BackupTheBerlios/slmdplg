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

	public int getTama�o() 
	{
		return Integer.MAX_VALUE; //Devuelve un valor que no se utilizar�.
	}

	public String getNombre() {
		return null;
	}
	
	//Hereda el equals por defecto de TipoAux (no hace falta redefinirlo)

	public int getTama�oTotal() 
	{
		return Integer.MAX_VALUE; //Devuelve un valor que no se utilizar�.
	}
}
