import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import MaquinaVirtual.MaquinaVirtual;

/**
 * Clase principal que permite seleccionar el archivo que contiene las instrucciones generadas
 * por el compilador y que ordena a un objeto de la clase MaquinaVirtual que ejecute dichas 
 * instrucciones.
 * @author Grupo5 PLG
 *
 */
public class MainMV 
{

	public static void main(String[] args) 
	{
		MaquinaVirtual mv;
		try 
		{
			FileReader fis = new FileReader("codigo.txt");
			mv = new MaquinaVirtual(fis);
			boolean modoEjecucionDebug=true;
			try {
				mv.ejecuta(modoEjecucionDebug);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("ERROR: No se ha encontrado el archivo indicado.");
		} 
		catch (IOException e) 
		{
			System.out.println("ERROR: Fallo en la entrada/salida.");
		}
	}

}
