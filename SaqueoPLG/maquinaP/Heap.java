/**
 * 
 */
package maquinaP;

import java.util.Vector;

/**
 * @author paloma
 *
 */
public class Heap {
	
	private Vector heap;
	private Vector ocupados;
	private int ultimo;
	
	/**
	 * constructor de heap con un parametro entero
	 */
	public Heap(int i) {
		super();
		heap= new Vector(i);
		ocupados= new Vector(i);
		ultimo=-1;
	}
	
	/**
	 * Accesor del heap
	 * @return Returns the heap.
	 */
	public Vector getHeap() {
		return heap;
	}
	
	/**
	 * Mutador del heap
	 * @param heap The heap to set.
	 */
	public void setHeap(Vector heap) {
		this.heap = heap;
	}
	
	/**
	 * Accesor de ocupados
	 * @return Returns the ocupados.
	 */
	public Vector getOcupados() {
		return ocupados;
	}
	
	/**
	 * Mutador de ocupados
	 * @param ocupados The ocupados to set.
	 */
	public void setOcupados(Vector ocupados) {
		this.ocupados = ocupados;
	}
	
	/**
	 * metodo que coloca un elemento el montculo
	 * @param v
	 * @param u
	 */
	private void flotar(Vector v, int u){
		int i=u;
		Integer aux;
		if (!ocupados.isEmpty()){
			Integer j=(Integer)ocupados.get(i);
			Integer z=(Integer)ocupados.get(i/2);
			while((i!=0)&& (j.intValue()<z.intValue())){
				aux=j;
				j= z;
				z=aux;
				ocupados.setElementAt(j,i);
				ocupados.setElementAt(z,i/2);
				i= i/2;
			}
		}
	}
	
	/**
	 * 
	 *
	 */
	private void monticulizar(){
		for(int j=1; j==ultimo;j++){
			flotar(ocupados,j);
		}
	}
	/**
	 * Metodo que reserva un tam de memoria
	 * @param tam entero que indica la cantidad de memoria que se quiere reservar
	 * @return int
	 * @throws Exception
	 */
	public int reserva(int tam)throws Exception{
		if (ultimo+tam>ocupados.capacity()){
			throw new Exception("Acceso de Memoria no valido, la memoria esta llena");
		}
		int c=ultimo;
		int b= ultimo+tam;
		for (int i=c+1;i<=b;i++){
			heap.addElement(null);
			ocupados.addElement(new Integer(i));
			ultimo=ultimo+1;
			flotar(ocupados,i);
		}
		return ultimo;
	}
	
	/**
	 * Libera desde dir tantas celdas como le indica tam, nos interesa que mueva los indices, las direcciones??
	 * @param dir
	 * @param tam
	 * @throws Exception
	 */
	public void libera(int dir, int tam)throws Exception{
		for(int i=dir;i<dir+tam;i++){
			heap.setElementAt(null,i);
			ocupados.removeElement(new Integer(i));
		}
		monticulizar();
	}
	
	/**
	 * Metodo que devuelve la direccion de memoria de un elemento
	 * @param d
	 * @return int
	 * @throws Exception
	 */
	public int getElementAt(int d) throws Exception{
		if (ocupados.contains(new Integer(d))){
			return ((Integer)heap.get(d)).intValue();
		}
		else{
			throw new Exception("Acceso de Memoria no valido");
		}
	}
	
	/**
	 * Metodo que modifica el contenido de una posicion de memoria
	 * @param d
	 * @param value
	 * @throws Exception
	 */
	public void setElementAt(int d, Integer value) throws Exception{
		if (ocupados.contains(new Integer(d))){
			heap.setElementAt(value,d);
		}
		else{
			throw new Exception("Acceso de Memoria no valido");
		}
	}
}