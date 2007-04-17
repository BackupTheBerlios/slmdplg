package Analizador_Sintactico.Traductor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Traductor 
{
	private ArrayList<String> instrucciones;
	private int indice;
	
	public Traductor()
	{
		instrucciones = new ArrayList<String>();
		indice = 0;
	}
	
	public void emiteInstruccion(String tipoOp)
	{
		String instrucci�n = tipoOp + ";";
		instrucciones.add(indice++, instrucci�n);
	}
	
	public void emiteInstruccion(String tipoOp, int parametro)
	{
		String instrucci�n = tipoOp + "(" + parametro + ");";
		instrucciones.add(indice++, instrucci�n);
	}
	
	/** M�todo que se utiliza para guardar el c�digo en el fichero cuya ruta se pasa como par�metro.
	 * @param ruta La ruta del archivo en el que quiere guardarse el c�digo en lenguaje objeto.*/
	public void guardar(String ruta)
	{
		try
		{
			File archivo = new File(ruta);
			FileWriter escritor = new FileWriter(archivo);
			Iterator iter = instrucciones.iterator();
			while (iter.hasNext())
			{
				escritor.write(iter.next() + "\n");
			}
			escritor.close();
		}
		catch (IOException excepcion){}
	}
}
