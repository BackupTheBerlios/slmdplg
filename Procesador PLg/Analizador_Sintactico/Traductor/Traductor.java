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
	 * @param tipoOp Nombre de la operación a guardar.*/
	public void emiteInstruccion(String tipoOp)
	{
		String instrucción = tipoOp + ";";
		instrucciones.add(indice++, instrucción);
	}
	
	/** Guarda en la secuencia de instrucciones una operacion con un parametro, del tipo 
	 * de apila(2);
	 * @param tipoOp Nombre de la operación a guardar.
	 * @param parametro Parametro de la operación.*/
	public void emiteInstruccion(String tipoOp, int parametro)
	{
		String instrucción = tipoOp + "(" + parametro + ");";
		instrucciones.add(indice++, instrucción);
	}
	
	/** Almacena una operacion del tipo que se le pasa como parámetro, pero sin añadir ';' para que se le puedan 
	 * añadir nuevos parámetros más tarde.
	 * @param tipoOp Nombre de la operación a guardar.*/
	public void emiteInstrucciónParcheable(String tipoOp)
	{
		instrucciones.add(indice++, tipoOp);
	}
	
	/** Añade a la instruccion parcheable de la linea que se le pasa como parámetro el parámetro indicado.*/
	public void parchea(int linea, int parametro)
	{
		String ins = instrucciones.get(linea);
		ins += "(" + parametro + ");";
		instrucciones.remove(linea);
		instrucciones.add(linea, ins);
	}
	
	/** Método que se utiliza para guardar el código en el fichero cuya ruta se pasa como parámetro.
	 * @param ruta La ruta del archivo en el que quiere guardarse el código en lenguaje objeto.*/
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
