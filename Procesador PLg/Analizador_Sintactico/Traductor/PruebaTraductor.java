package Analizador_Sintactico.Traductor;

public class PruebaTraductor {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		Traductor tr = new Traductor();
		tr.emiteInstruccion("suma");
		int linea = tr.getIndice();
		tr.emiteInstrucciónParcheable("ir-f");
		tr.parchea(linea, 0);
		tr.emiteInstruccion("apila", 3);
	}

}
