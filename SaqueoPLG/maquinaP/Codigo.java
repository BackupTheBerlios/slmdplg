package maquinaP;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.String;
import java.util.Vector;

import javax.swing.JOptionPane;

/**
 * La clase <B>Codigo</B> se encarga de manejar el cdigo generado por las instrucciones del lenguaje.
 * <P>Tiene dos atributos:
 * <UL><LI><CODE>cod:</CODE> string donde se almacena el c�digo del lenguaje objeto, que es el c�digo de la m�quina P. Como la mquina P de momento no existe, este el contenido de este atributo se mostrar por pantalla.</LI></UL>
 * <UL><LI><CODE>fichero:</CODE> Es el fichero donde vamos a guardar las instrucciones que podra ejecutar la m�quinaP. Se guarda con extensin '.obj'</LI></UL></P>
 * 
 * @author Jons Andradas, Paloma de la Fuente, Leticia Garca y Silvia Mart�n
 *
 */
public class Codigo {
	
	/*
	 * Atributos de la clase:
	 * 
	 *  El String cod guarda el codigo del lenguaje objeto, que es el codigo de la maquina P.
	 *  En el fichero se guarda tambien el codigo generado, por si se quiere ejecutar en otra ocasin. El fichero se guarda con extension '.obj'
	 */
	Vector cod;
	FileOutputStream fichero;
	
	/**
	 * El constructor de la clase Codigo no tiene parmetros de entrada ni de salida. Este constructor inicializa el atributo cod con la cadena vac�a.
	 * 
	 * @param f String que guarda la ruta del fichero donde se almacenara el c�digo. Este se almacena en el mismo directorio que se encuentra el c�digo fuente.
	 */
	public Codigo(String f){		
		cod = new Vector();
		String fcod;
		int i= f.length();
		fcod = new String(f.substring( 0,i-3));
		fcod = fcod.concat("obj");
		File fich= new File(fcod);
		try{
			fichero = new FileOutputStream(fich, false);
		}
		catch(java.io.FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,"Archivo no encontrado: " + fcod +e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	/**
	 * Accesor para el atributo cod de la clase C�digo. 
	 * @return Devuelve el codigo generado hasta ese momento
	 */
	public Vector getCod() {
		return cod;
	}

	/**
	 * Este m�todo inicializa el atributo cod con la cadena vac�a. No cuenta con par�metros de entrada ni de salida. No devuelve nada.
	 */
	public void inicializaCodigo(){
		cod = new Vector();
	}
	
	/**
	 * M�todo que modifica el contenido del atributo cod agreg�ndole la instrucci�n y el n�mero de l�nea que recibe como par�metro. 
	 * Adem�s de modificar el fichero donde se almacena el codigo generado parra que lo ejecute la mquinaP. No devuelve nada.
	 * 
	 * @param instr String con la instrucci�n que ha codificado.
	 * @param num Entero indica el n�mero de lnea.
	 * 
	 */
	public void genIns(String instr, int num){
		String i =instr + " " + num;
		cod.add(i);
	}
	/**
	 * M�todo que genera el c�digo de una instrucci�n
	 * @param instr String con la instrucci�n
	 * @param num Entero que indica el n�mero de l�nea
	 * @param dir entero que indica la direcci�n
	 */
	public void genIns(String instr, int num, int dir){
		String i =instr + " " + num + " " + dir;
		cod.add(i);
	}
	
	/**
	 * M�todo que modifica el contenido del atributo cod agreg�ndole la instrucci�n que recibe como par�metro y un salto de l�nea. 
	 * Adem�s de modificar el fichero donde se almacena el codigo generado para que lo ejecute la mquinaP. No devuelve nada.
	 * 
	 * @param instr String con la instruccion que ha codificado.
	 * 
	 */
	public void genIns(String instr){
		String i= instr;
		cod.add(i);
		i= i.concat("\n");
	}
	
	/**
	 * M�todo que imprime por pantalla el contenido del atributo cod. No tiene parmetros de entrada ni de salida.
	 * 
	 */
	public void muestraCodigo(){		
		for (int i=0;i<cod.size();i++){
			System.out.println(i+"  "+cod.elementAt(i));
		}
		String ins;
		try{
			for (int i=0;i<cod.size();i++){
				ins = (String)cod.elementAt(i);
				ins = ins+("\n");
				fichero.write(ins.getBytes());
			}
			fichero.close();
		}
		catch(java.io.IOException e){
			JOptionPane.showMessageDialog(null,"No he podido cerrar el fichero. "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	/**
	 * M�todo que genera el c�digo necesario para hacer ir-f o ir-a
	 * @param s String contiene el c�digo ir-f o ir-a
	 */
	public void emite(String s){
		if (s.startsWith("ir-f")){
			genIns(s);
		}
		if (s.startsWith("ir-a")){
			genIns(s);
		}
	}
	/**
	 * M�todo que parchea la direcci�n a sum�ndole la direcci�n de b
	 * @param a int a parchear
	 * @param b int necesario para hacer el parcheo
	 */
	public void parchea(int a, int b){
		String i = (String)cod.elementAt(a) + " " + b;
		cod.setElementAt(i,a);
		//System.out.println("Paso por parchea con: "+ cod.get(a) + "He aadido: " + b);
	}
	
	/**
	 * M�todo que genera el c�digo que salva la direcci�n de retorno de la prellamada asociada a la invocaci�n
	 * @param ret entero con la direcci�n 
	 */
	public void apila_ret (int ret) {
		this.genIns("apila-dir",0);
		this.genIns("apila",1);
		this.genIns("suma");
		this.genIns("apila");//, ret
		this.genIns("desapila-ind");
	}
	/**
	 * M�todo que que genera el c�digo de lo siguiente:
	 * - Salva el valor anterior del display asociado con el procedimiento.
	 * - Fija el valor del display para la activaci�n actual.
	 * - Reserva de espacio para los datos locales
	 * @param tamlocales entero que contiene el tama�o de las variables locales al procedimiento
	 */
	public void prologo (int tamlocales){
		this.genIns("apila", tamlocales);
		this.genIns("desapila_dir", 0);
	}
	
	/**
	 * M�todo que genera el c�digo para poder realizar el paso del par�metro formal
	 *
	 *	 * @param pformal entero con el par?metro formal
	 */
	public void paso_parametro (int pformal) {
		this.genIns("apila", pformal);
		//this.genIns("suma");
		this.genIns("desapila-ind");
	}
	/**
	 * M�todo que genera el c�digo para poder acceder a la informaci�n de la variable
	 * @param infoID_nivel entero con el nivel de la variable
	 * @param infoID_dir entero con la direcci�n de la variable
	 */
	/*public void acceso_var (int infoID_nivel, int infoID_dir){
		this.genIns("apila-dir", 1 + infoID_nivel);
		this.genIns("apila", infoID_dir);
		this.genIns("suma");
		this.genIns("apila-ind");}*/
	
	
	/**
	 * M�todo que genera el c�digo para hacer el inicio relativo a los procedimientos
	 * 
	 */
	public void inicio () {
		this.genIns("apila");
		this.genIns("desapila-dir", 0);
	}
	/**
	 * M�todo que genera el c�digo relativo al ep�logo
	 * @param tamlocales entero con el tama�o de las locales
	 */
	public void epilogo (int tamlocales) {

		this.genIns("apila-dir", 0);
		this.genIns("apila", tamlocales + 2);
		this.genIns("resta");
		this.genIns("copia");
		this.genIns("desapila-dir",0);
		this.genIns("apila",1);
		this.genIns("suma");
		this.genIns("desapila-ind");
		
	}	
	/**
	 * M�todo que implementa las posiciones de destino en el registro de activaci�n
	 *
	 */
	public void fin_paso (){
		this.genIns("desapila");
	}
	/**
	 * M�todo que implementa las posiciones de destino en el registro de activaci�n
	 *
	 */
	public void inicio_paso (){
		this.genIns("apila-dir", 0);
		this.genIns("apila", 3);
		this.genIns("suma");
	}
}