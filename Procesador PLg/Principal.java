import java.io.FileNotFoundException;
import java.io.FileReader;

import Analizador_Sintactico.AnSintactico;


public class Principal 
{
	public static void main(String[] args)
	{
		FileReader ficheroFuente;
		try
		{
			for (int i = 8; i<9; i++)
			{
				System.out.println(" >> Prueba " + i + ":");
	            //Apertura del fichero fuente
				ficheroFuente = new FileReader("Pruebas/Prueba" + i + ".txt");
				AnSintactico anSint = new AnSintactico(ficheroFuente);
				System.out.println();
			}
        }
		catch(FileNotFoundException e)
		{
    			System.out.println("Fichero no encontrado \n");
    			e.printStackTrace();
    	}
	}
}
