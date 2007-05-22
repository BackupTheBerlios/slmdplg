import java.io.FileNotFoundException;
import java.io.FileReader;

import Analizador_Sintactico.AnSintactico;


public class Principal 
{
	public static void main(String[] args)
	{
		//Token tActual=new Token();
		FileReader ficheroFuente;
		try
		{
			for (int i = 1; i<8; i++)
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
