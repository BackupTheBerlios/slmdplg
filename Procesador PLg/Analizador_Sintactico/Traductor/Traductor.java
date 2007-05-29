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
	
	/** Guarda en la secuencia de instrucciones una operacion sin parametro, del tipo 
	 * de suma; o apila-ind;
	 * @param tipoOp Nombre de la operaci�n a guardar.*/
	public void emiteInstruccion(String tipoOp)
	{
		String instrucci�n = tipoOp + ";";
		instrucciones.add(indice++, instrucci�n);
	}
	
	/** Guarda en la secuencia de instrucciones una operacion con un parametro, del tipo 
	 * de apila(2);
	 * @param tipoOp Nombre de la operaci�n a guardar.
	 * @param parametro Parametro de la operaci�n.*/
	public void emiteInstruccion(String tipoOp, int parametro)
	{
		String instrucci�n = tipoOp + "(" + parametro + ");";
		instrucciones.add(indice++, instrucci�n);
	}
	
	/** Almacena una operacion del tipo que se le pasa como par�metro, pero sin a�adir ';' para que se le puedan 
	 * a�adir nuevos par�metros m�s tarde.
	 * @param tipoOp Nombre de la operaci�n a guardar.*/
	public void emiteInstrucci�nParcheable(String tipoOp)
	{
		instrucciones.add(indice++, tipoOp);
	}
	
	/** A�ade a la instruccion parcheable de la linea que se le pasa como par�metro el par�metro indicado.*/
	public void parchea(int linea, int parametro)
	{
		String ins = instrucciones.get(linea);
		ins += "(" + parametro + ");";
		instrucciones.remove(linea);
		instrucciones.add(linea, ins);
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

	public int getEtiqueta() {
		return indice;
	}
}
