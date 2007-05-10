package tSimbolos.Tipo;

public class Error implements Tipo {

	public String getLexema() 
	{
		return "ERROR";
	}

	public int getTamaño() 
	{
		return Integer.MAX_VALUE; //Devuelve un valor que no se utilizará.
	}

}
