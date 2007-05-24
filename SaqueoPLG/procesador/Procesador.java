package procesador;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * La clase <B>Lexico</B> analiza el fichero de entrada para reconocer tokens. Lanza una excepción en caso de que esto no sea posible. Puede suceder
 * que el token sea erroneo o que ocurra algun problema con el fichero de lectura.
 * <P>La clase Lexico cuenta con los siguientes atributos:
 * <UL><LI><CODE>linea:</CODE> Entero que controla la lnea del código donde se detecta el error.</LI>
 * <LI><CODE>lookahead:</CODE> Token que almacena los caracteres de prean?lisis.</LI>
 * <LI><CODE>fuente:</CODE> RandomAccessFile que se utiliza para leer del fichero que contine que contine el código a analizar.</LI>
 * <LI><CODE>posicion:</CODE> Entero que marca la posición en la que se esté leyendo dentro de una línea.</LI>
 * </UL></P>
 * 
 * @author Jonas Andradas, Paloma de la Fuente, Leticia Garcia y Silvia Martin
 *
 */

public class Lexico {
	
	/*
	 * Los Atributos:
	 * 
	 * linea sirve para controlar en que linea de codigo detectamos el error.
	 * lookahead sirve para almacenar el caracteres de preanalisis.
	 * fuente se utiliza para leer del fichero que contiene el codigo a analizar.
	 * posicion entero que marca la posicion dentro de una linea por donde se esta
	 * leyendo.
	 */
	int linea;
	Token lookahead;
	RandomAccessFile fuente;
	int posicion;
	
	/**
	 * El constructor de la clase Lexico que sólo tiene el buffer de lectura del fichero como parmetro de entrada.
	 * @param f Buffer de entrada del que se leen caracteres. Que es del tipo RandomAccessFile.
	 * 
	 */
	public Lexico(RandomAccessFile f) {
		linea = 0;
		lookahead = new Token();
		fuente = f;
		posicion = 0;
	}
	
	/**
	 * Accesor para el atributo de la clase, linea. 
	 * @return Entero que controla la lnea del código donde se detecta el error.
	 */
	public int getLinea() {
		return linea;
	}
	
	/**
	 * Mutador para el atributo de la clase linea. 
	 * @param linea Entero que controla la lnea del cdigo donde se detecta el error.
	 */
	public void setLinea(int linea) {
		this.linea = linea;
	}
	
	/**
	 * Accesor para el atributo de la clase posicion. 
	 * @return Entero que marca la posicin en la que se este leyendo dentro de una linea.
	 */
	public int getPosicion() {
		return posicion;
	}
	
	/**
	 * Mutador para el atributo de la clase posicion. 
	 * @param posicion Entero que marcara la posicin en la que se est leyendo dentro de una linea.
	 */
	public void setPosicion(int posicion) {
		this.posicion = posicion;
	}

	/**
	 * Accesor para el atributo de la clase lookahead. 
	 * @return Token actual que almacena los caracteres de preanalisis.
	 */
	public Token getLookahead() {
		return lookahead;
	}
	
	/**
	 * Mutador para el atributo de la clase lookahead. 
	 * @param lookahead Token para modificar el token actual que almacena los caracteres de preanalisis.
	 */
	public void setLookahead(Token lookahead) {
		this.lookahead = lookahead;
	}
	
	/**
	 * Accesor para el atributo de la clase fuente. 
	 * @return RandomAccessFile que se utiliza para leer del fichero que contiene el codigo a analizar
	 */
	public RandomAccessFile getFuente() {
		return fuente;
	}
	
	/**
	 * Mutador para el atributo de la clase fuente. 
	 * @param fuente RandomAccessFile que se utiliza para leer del fichero que contine el codigo a analizar
	 */
	public void setFuente(RandomAccessFile fuente) {
		this.fuente = fuente;
	}

	/**
	 * El metodo getToken lee caracteres del flujo hasta que identifica un token y lo devuelve, o detecta un error y genera una excepcion.
	 * Utiliza las funciones que ofrece RandomAccessFile para manejar el flujo que lee de fichero.
	 * 
	 * @see java.io.RandomAccessFile#read() 
	 * @see java.io.RandomAccessFile#seek(long) 
	 * @return Token que hemos identificado.
	 * @exception IOException que propagamos de las funciones de los RandomAccessFile de JAVA.
	 * @exception Exception que generamos cuando detectamos una secuencia de caracteres incorrecta.
	 * 
	 */
	public Token getToken () throws IOException, Exception{
		
		char a;
		int error;
		/*
		 * La funcion read() saca un caracter del flujo de entrada
		 */
		while ((error = fuente.read())!=-1){
			a = (char) error;
			posicion ++;
			switch (a){
			
			/*
			 * En primer lugar identificamos todos los caracteres especiales.
			 */
			case '\n':	linea ++;
						break;
			case '\t':	break;
			case ' ':	break;
			case '\f':	break;
			case '\r':	break;
			case '0':	return new Token("0",Tipos.TKNUM);
			case '(':	return new Token("(",Tipos.TKPAP);
			case ')':	return new Token(")",Tipos.TKPCI);
			case '[':	return new Token("[",Tipos.TKCAP);
			case ']':	return new Token("]",Tipos.TKCCI);
			case '{':	return new Token("{",Tipos.TKLLAP);
			case '}':	return new Token("}",Tipos.TKLLCI);
			case '*':	return new Token("*",Tipos.TKMULT);
			case '/':	return new Token("/",Tipos.TKDIV);
			case ';':	return new Token(";",Tipos.TKPYCOMA);
			case ',':	return new Token(",",Tipos.TKCOMA);
			case '=':	return new Token("=",Tipos.TKIG);
			case '#':	return new Token("#",Tipos.TKCUA);
			
			case '!':	a = (char)fuente.read();
						posicion ++;
						if (a == '='){
							return new Token("!=",Tipos.TKDIF);
						}	
						else{
							throw new Exception("ERROR en linea "+linea+": ':' debe ir seguido de '='");
						}
			
			/*
			 * Si detectamos ':' hay que discernir si es el operador de asignacion o un error.
			 * Si es un error lanzamos una excepcion. 
			 */
			case ':':	a = (char)fuente.read();
						posicion ++;
						if (a == '='){
							return new Token(":=",Tipos.TKASIGN);
						}	
						else{
							throw new Exception("ERROR en linea "+linea+": ':' debe ir seguido de '='");
						}
			
			/*
			 * Si detectamos '<' hay que discernir si es el operador 'menor que' o 'menor o igual que'
			 * 
			 * La funcion seek(int), mueve el puntero de lectura a donde nos marca el entero. Asi podemos
			 * modificar la posicion del flujo.
			 */
			case '<':	a = (char)fuente.read();
						posicion ++;
						if (a == '='){
							return new Token("<=",Tipos.TKMENIG);
						}	
						else{
							posicion --;
							fuente.seek(posicion);
							return new Token ("<",Tipos.TKMEN);
						}
			
			/*
			 * Si detectamos '>' hay que discernir si es el operador 'mayor que' o 'mayor o igual que' 
			 */
			case '>':	a = (char)fuente.read();
						posicion ++;
						if (a == '='){
							return new Token(">=",Tipos.TKMAYIG);
						}	
						else{
							posicion --;
							fuente.seek(posicion);
							return new Token (">",Tipos.TKMAYIG);
						}
						
			/*
			 * Si detectamos '+' hay que discernir si es el operador '+' o es un numero positivo.
			 * Para leer secuencias de digitos, usamos leerNumero. 
			 * 
			 * Eliminamos el + para que no de conflictos con los enteros de java.
			 */
			case '+':	a = (char)fuente.read();
						posicion ++;
						if ((a >= '1') && (a<='9')){
							posicion --;
							fuente.seek(posicion);
							String aux;
							aux = leerNumero("");
							return new Token (aux,Tipos.TKNUM);
						}	
						else{
							posicion --;
							fuente.seek(posicion);
							return new Token ("+",Tipos.TKSUMA);
						}
						
			/*
			 * Si detectamos '-' hay que discernir si es el operador '-' o es un numero negativo.
			 * Para leer secuencias de digitos, usamos leerNumero.  
			 */
			case '-':	a = (char)fuente.read();
						posicion ++;
						if ((a >= '1') && (a<='9')){
							posicion --;
							fuente.seek(posicion);
							String aux;
							aux = leerNumero("-");
							return new Token (aux,Tipos.TKNUM);
						}	
						else{
							posicion --;
							fuente.seek(posicion);
							return new Token ("-",Tipos.TKRESTA);
						}
						
			/*
			 * Si detectamos 'a' hay que discernir si es el operador 'and', 'array' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
						
			case 'a':	a = (char)fuente.read();
						posicion ++;
						if (a =='n'){
							a = (char)fuente.read();
							posicion ++;
							if (a =='d'){
								a = (char)fuente.read();
								posicion ++;
								if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("and");
									return new Token (aux,Tipos.TKIDEN);
								}
								else{
									posicion --;
									fuente.seek(posicion);
									return new Token ("and",Tipos.TKAND);
								}
							}	
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("an");
								return new Token (aux,Tipos.TKIDEN);
							}	
						}	
						else{	
							if (a =='r'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='r'){
									a = (char)fuente.read();
									posicion ++;
									if (a =='a'){
										a = (char)fuente.read();
										posicion ++;
										if (a =='y'){
											a = (char)fuente.read();
											posicion ++;
											if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
												posicion --;
												fuente.seek(posicion);
												String aux;
												aux = leerCaracter("array");
												return new Token (aux,Tipos.TKIDEN);
											}
											else{
												posicion --;
												fuente.seek(posicion);
												return new Token ("array",Tipos.TKARRAY);
											}
										}
										else{
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("arra");
											return new Token (aux,Tipos.TKIDEN);}	
										}	
									else{
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("arr");
										return new Token (aux,Tipos.TKIDEN);
									}
								}	
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("ar");
									return new Token (aux,Tipos.TKIDEN);
								}
							}
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("a");
								return new Token (aux,Tipos.TKIDEN);
							}
						}
			/*
			 * Si detectamos 'e' hay que discernir si es la palabra reservada 'end' o la palabra reservada 'else' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			case 'e':	a = (char)fuente.read();
						posicion ++;
						if (a =='n'){
							a = (char)fuente.read();
							posicion ++;
							if (a =='d'){
								a = (char)fuente.read();
								posicion ++;
								if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("end");
									return new Token (aux,Tipos.TKIDEN);
								}
								else{
									posicion --;
									fuente.seek(posicion);
									return new Token ("end",Tipos.TKEND);
								}
							}	
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("en");
								return new Token (aux,Tipos.TKIDEN);
							}	
						}	
						else{	
							if (a =='l'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='s'){
									a = (char)fuente.read();
									posicion ++;
									if (a =='e'){
										a = (char)fuente.read();
										posicion ++;
										if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("else");
											return new Token (aux,Tipos.TKIDEN);
										}
										else{
											posicion --;
											fuente.seek(posicion);
											return new Token ("else",Tipos.TKELS);
										}
									}	
									else{
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("els");
										return new Token (aux,Tipos.TKIDEN);
									}
								}	
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("el");
									return new Token (aux,Tipos.TKIDEN);
								}
							}
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("e");
								return new Token (aux,Tipos.TKIDEN);
							}
						}
			/*
			 * Si detectamos 'n' hay que discernir si es el operador 'not' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			case 'n':	a = (char)fuente.read();
						posicion ++;
						if (a =='o'){
							a = (char)fuente.read();
							posicion ++;
							if (a =='t'){
								a = (char)fuente.read();
								posicion ++;
								if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("not");
									return new Token (aux,Tipos.TKIDEN);
								}
								else{
									posicion --;
									fuente.seek(posicion);
									return new Token ("not",Tipos.TKNOT);
								}
							}	
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("no");
								return new Token (aux,Tipos.TKIDEN);
							}	
						}	
						else{
							if (a =='e'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='w'){
									a = (char)fuente.read();
									posicion ++;
									if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("new");
										return new Token (aux,Tipos.TKIDEN);
									}
									else{
										posicion --;
										fuente.seek(posicion);
										return new Token ("new",Tipos.TKNEW);
									}
								}	
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("ne");
									return new Token (aux,Tipos.TKIDEN);
								}	
							}	
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("n");
								return new Token (aux,Tipos.TKIDEN);
							}
						}
			
			/*
			 * Si detectamos 'o' hay que discernir si es el operador 'or', 'of' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			
			case 'o':	a = (char)fuente.read();
						posicion ++;
						if (a =='f'){
							a = (char)fuente.read();
							posicion ++;
								if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("of");
									return new Token (aux,Tipos.TKIDEN);
								}
								else{
									posicion --;
									fuente.seek(posicion);
									return new Token ("of",Tipos.TKOF);
								}
						}	
						else {	
							if (a =='r'){ 
								a = (char)fuente.read();
								posicion ++;
									if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("or");
											return new Token (aux,Tipos.TKIDEN);
									}
									else {
											posicion --;
											fuente.seek(posicion);
											return new Token ("or",Tipos.TKOR);
									}
							}	
							else {
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("o");
								return new Token (aux,Tipos.TKIDEN);
							}
						} 
						
			/*
			 * Si detectamos 'd' hay que discernir si es la palabra reservada 'do' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			case 'd':	a = (char)fuente.read();
						posicion ++;
						if (a =='o'){
							a = (char)fuente.read();
							posicion ++;
							if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("do");
								return new Token (aux,Tipos.TKIDEN);
							}
							else{
								posicion --;
								fuente.seek(posicion);
								return new Token ("do",Tipos.TKDO);
							}
						}	
						else{
							if (a =='e'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='l'){
									a = (char)fuente.read();
									posicion ++;
									if (a =='e'){
										a = (char)fuente.read();
										posicion ++;
										if (a =='t'){
											a = (char)fuente.read();
											posicion ++;
											if (a =='e'){
												a = (char)fuente.read();
												posicion ++;
												if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
													posicion --;
													fuente.seek(posicion);
													String aux;
													aux = leerCaracter("delete");
													return new Token (aux,Tipos.TKIDEN);
												}
												else{
													posicion --;
													fuente.seek(posicion);
													return new Token ("delete",Tipos.TKDEL);
												}
											}
											else{
												posicion --;
												fuente.seek(posicion);
												String aux;
												aux = leerCaracter("delet");
												return new Token (aux,Tipos.TKIDEN);
											}
										}
										else{
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("dele");
											return new Token (aux,Tipos.TKIDEN);
										}
									}
									else{
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("del");
										return new Token (aux,Tipos.TKIDEN);
									}
								}
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("de");
									return new Token (aux,Tipos.TKIDEN);
								}
							}
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("d");
								return new Token (aux,Tipos.TKIDEN);
							}
						}
						
			/*
			 * Si detectamos 't' hay que discernir si es el valor boolenao 'true' o la palabra reservada then o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			case 't':	a = (char)fuente.read();
						posicion ++;
						if (a =='r'){
							a = (char)fuente.read();
							posicion ++;
							if (a =='u'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='e'){
									a = (char)fuente.read();
									posicion ++;
									if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("true");
										return new Token (aux,Tipos.TKIDEN);
									}
									else{
										posicion --;
										fuente.seek(posicion);
										return new Token ("true",Tipos.TKTRUE);
									}
								}	
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("tru");
									return new Token (aux,Tipos.TKIDEN);
								}
							}	
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("tr");
								return new Token (aux,Tipos.TKIDEN);
							}
						}
						else{							
							if (a =='h'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='e'){
									a = (char)fuente.read();
									posicion ++;
									if (a =='n'){
										a = (char)fuente.read();
										posicion ++;
										if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("then");
											return new Token (aux,Tipos.TKIDEN);
										}
										else{
											posicion --;
											fuente.seek(posicion);
											return new Token ("then",Tipos.TKTHN);
										}
									}	
									else{
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("the");
										return new Token (aux,Tipos.TKIDEN);
									}
								}	
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("th");
									return new Token (aux,Tipos.TKIDEN);
								}
							}
							else{							
								if (a =='i'){
									a = (char)fuente.read();
									posicion ++;
									if (a =='p'){
										a = (char)fuente.read();
										posicion ++;
										if (a =='o'){
											a = (char)fuente.read();
											posicion ++;
											if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
												posicion --;
												fuente.seek(posicion);
												String aux;
												aux = leerCaracter("tipo");
												return new Token (aux,Tipos.TKIDEN);
											}
											else{
												posicion --;
												fuente.seek(posicion);
												return new Token ("tipo",Tipos.TKTIPO);
											}
										}	
										else{
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("tip");
											return new Token (aux,Tipos.TKIDEN);
										}
									}	
									else{
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("ti");
										return new Token (aux,Tipos.TKIDEN);
									}
								}
							}
						}
			
			/*
			 * Si detectamos 'f' hay que discernir si es el valor booleano 'false' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			case 'f':	a = (char)fuente.read();
						posicion ++;
						if (a =='a'){
							a = (char)fuente.read();
							posicion ++;
							if (a =='l'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='s'){
									a = (char)fuente.read();
									posicion ++;
									if (a =='e'){
										a = (char)fuente.read();
										posicion ++;
										if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("false");
											return new Token (aux,Tipos.TKIDEN);
										}
										else{
											posicion --;
											fuente.seek(posicion);
											return new Token ("false",Tipos.TKFALSE);
										}
									}	
									else{
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("fals");
										return new Token (aux,Tipos.TKIDEN);
										}
								}	
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("fal");
									return new Token (aux,Tipos.TKIDEN);
								}
							}	
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("fa");
								return new Token (aux,Tipos.TKIDEN);
							}
						}
						else{							
							posicion --;
							fuente.seek(posicion);
							String aux;
							aux = leerCaracter("f");
							return new Token (aux,Tipos.TKIDEN);
						}
						
			/*
			 * Si detectamos 'i' hay que discernir si es el identificador de tipo 'int' o es la palabra reservada 'if' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			case 'i':	a = (char)fuente.read();
						posicion ++;
						if (a =='n'){
							a = (char)fuente.read();
							posicion ++;
							if (a =='t'){
								a = (char)fuente.read();
								posicion ++;
								if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("int");
									return new Token (aux,Tipos.TKIDEN);
								}
								else{
									posicion --;
									fuente.seek(posicion);
									return new Token ("int",Tipos.TKINT);
								}
							}	
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("in");
								return new Token (aux,Tipos.TKIDEN);
							}	
						}	
						else{							
							if (a =='f'){
								a = (char)fuente.read();
								posicion ++;
								if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("if");
									return new Token (aux,Tipos.TKIDEN);
								}
								else{
									posicion --;
									fuente.seek(posicion);
									return new Token ("if",Tipos.TKIF);
								}
							}	
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("i");
								return new Token (aux,Tipos.TKIDEN);
							}
						}

			/*
			 * Si detectamos 'b' hay que discernir si es el identificador de tipo 'bool', la palabra reservada 'begin' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			case 'b':	a = (char)fuente.read();
						posicion ++;
						if (a =='o'){
							a = (char)fuente.read();
							posicion ++;
							if (a =='o'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='l'){
									a = (char)fuente.read();
									posicion ++;
									if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("bool");
										return new Token (aux,Tipos.TKIDEN);
									}
									else{
										posicion --;
										fuente.seek(posicion);
										return new Token ("bool",Tipos.TKBOOL);
									}
								}
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("boo");
									return new Token (aux,Tipos.TKIDEN);
								}
							}
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("bo");
								return new Token (aux,Tipos.TKIDEN);
							}	
						}	
						else{							
							if (a =='e'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='g'){
									a = (char)fuente.read();
									posicion ++;
									if (a =='i'){
										a = (char)fuente.read();
										posicion ++;
										if (a =='n'){
											a = (char)fuente.read();
											posicion ++;
											if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
												posicion --;
												fuente.seek(posicion);
												String aux;
												aux = leerCaracter("begin");
												return new Token (aux,Tipos.TKIDEN);
											}
											else{
												posicion --;
												fuente.seek(posicion);
												return new Token ("begin",Tipos.TKBEG);
											}
										}
										else{
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("begi");
											return new Token (aux,Tipos.TKIDEN);
										}
									}
									else{
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("beg");
										return new Token (aux,Tipos.TKIDEN);
									}	
								}	
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("be");
									return new Token (aux,Tipos.TKIDEN);
								}
							}
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("b");
								return new Token (aux,Tipos.TKIDEN);
							}
						}
			/*
			 * Si detectamos 'p' hay que discernir si es 'proc', la palabra reservada 'pointer' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			case 'p':	a = (char)fuente.read();
						posicion ++;
						if (a =='r'){
							a = (char)fuente.read();
							posicion ++;
							if (a =='o'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='c'){
									a = (char)fuente.read();
									posicion ++;
									if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("proc");
										return new Token (aux,Tipos.TKIDEN);
									}
									else{
										posicion --;
										fuente.seek(posicion);
										return new Token ("proc",Tipos.TKPROC);
									}
								}
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("pro");
									return new Token (aux,Tipos.TKIDEN);
								}
							}
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("pr");
								return new Token (aux,Tipos.TKIDEN);
								}	
						}	
						else{							
							if (a =='o'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='i'){
									a = (char)fuente.read();
									posicion ++;
									if (a =='n'){
										a = (char)fuente.read();
										posicion ++;
										if (a =='t'){
											a = (char)fuente.read();
											posicion ++;
											if (a =='e'){//1
												a = (char)fuente.read();
												posicion ++;
												if (a =='r'){//2
													a = (char)fuente.read();
													posicion ++;
													if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
														posicion --;
														fuente.seek(posicion);
														String aux;
														aux = leerCaracter("pointer");
														return new Token (aux,Tipos.TKIDEN);
													}
													else{
														posicion --;
														fuente.seek(posicion);
														return new Token ("pointer",Tipos.TKPUNT);
													}
												}
												else{
													posicion --;
													fuente.seek(posicion);
													String aux;
													aux = leerCaracter("pointe");
													return new Token (aux,Tipos.TKIDEN);
												}
											}
											else{
												posicion --;
												fuente.seek(posicion);
												String aux;
												aux = leerCaracter("point");
												return new Token (aux,Tipos.TKIDEN);
											}
										}
										else{
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("poin");
											return new Token (aux,Tipos.TKIDEN);
										}
									}
									else{
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("poi");
										return new Token (aux,Tipos.TKIDEN);
									}	
								}	
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("po");
									return new Token (aux,Tipos.TKIDEN);
								}
							}
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("p");
								return new Token (aux,Tipos.TKIDEN);
							}
						}
						
				
			/*
			 * Si detectamos 'm' hay que discernir si es la palabra reservada 'memdir' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			case 'm':	a = (char)fuente.read();
								posicion ++;
								if (a =='e'){
									a = (char)fuente.read();
									posicion ++;
									if (a =='m'){
										a = (char)fuente.read();
										posicion ++;
										if (a =='d'){
											a = (char)fuente.read();
											posicion ++;
											if (a =='i'){
												a = (char)fuente.read();
												posicion ++;
												if (a =='r'){
														a = (char)fuente.read();
														posicion ++;
														if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
															posicion --;
															fuente.seek(posicion);
															String aux;
															aux = leerCaracter("memdir");
															return new Token (aux,Tipos.TKIDEN);
														}
														else{
															posicion --;
															fuente.seek(posicion);
															return new Token ("memdir",Tipos.TKMEMDIR);
														}
													}	
												else{
													posicion --;
													fuente.seek(posicion);
													String aux;
													aux = leerCaracter("memdi");
													return new Token (aux,Tipos.TKIDEN);
												}
											}	
											else{
												posicion --;
												fuente.seek(posicion);
												String aux;
												aux = leerCaracter("memd");
												return new Token (aux,Tipos.TKIDEN);
											}
										}	
										else{
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("mem");
											return new Token (aux,Tipos.TKIDEN);
										}
									}	
									else{
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("me");
										return new Token (aux,Tipos.TKIDEN);
									}
								}
								else{							
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("m");
									return new Token (aux,Tipos.TKIDEN);
								}
			
		
									
			/*
			 * Si detectamos 'w' hay que discernir si es la palabra reservada 'while' o es un identificador.
			 * Para leer identificadores, usamos leerCaracter().  
			 */
			case 'w':	a = (char)fuente.read();
						posicion ++;
						if (a =='h'){
							a = (char)fuente.read();
							posicion ++;
							if (a =='i'){
								a = (char)fuente.read();
								posicion ++;
								if (a =='l'){
									a = (char)fuente.read();
									posicion ++;
									if (a =='e'){
										a = (char)fuente.read();
										posicion ++;
										if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
											posicion --;
											fuente.seek(posicion);
											String aux;
											aux = leerCaracter("while");
											return new Token (aux,Tipos.TKIDEN);
										}
										else{
											posicion --;
											fuente.seek(posicion);
											return new Token ("while",Tipos.TKWHL);
										}
									}	
									else{
										posicion --;
										fuente.seek(posicion);
										String aux;
										aux = leerCaracter("whil");
										return new Token (aux,Tipos.TKIDEN);
									}
								}	
								else{
									posicion --;
									fuente.seek(posicion);
									String aux;
									aux = leerCaracter("whi");
									return new Token (aux,Tipos.TKIDEN);
								}
							}	
							else{
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("wh");
								return new Token (aux,Tipos.TKIDEN);
							}
						}
						else{							
							posicion --;
							fuente.seek(posicion);
							String aux;
							aux = leerCaracter("w");
							return new Token (aux,Tipos.TKIDEN);
						}
			
			/*
			 * En el caso por defecto detectamos las secuencias de digitos y los indentificadores.
			 * Si es un digito, llamamos a leerNumero.
			 * Si es una letra, llamamos a leerCaracter. Tiene que ser una letra porque los identificadores
			 * comienzan por letra y luego pueden llevar digitos o letras o '_'.
			 * Sino, hemos detectado un error y lanzamos una excepcion. 
			 */
			default:		if ((a>='1') && (a<='9')){
							posicion --;
							fuente.seek(posicion);
							String aux;
							aux = leerNumero("");
							return new Token (aux,Tipos.TKNUM);
						}
						else{
							if ((a>='A') && (a<='z')){
								posicion --;
								fuente.seek(posicion);
								String aux;
								aux = leerCaracter("");
								return new Token (aux,Tipos.TKIDEN);
							}
							else{
								throw new Exception("ERROR en linea "+linea+": No existe ese identificador");
							}
						}			
			}
		}	
		
		/*
		 * Si se sale del while es que read detecto un error y lanzamos una excepcion de entrada/salida. 
		 */
		if (error != -1)
			throw new Exception("ERROR en linea "+linea+": Error de entrada/salida");
		else {
			return new Token ("eof",Tipos.TKFF);
		}
	}
	
	/**
	 * El metodo leerNumero se encarga de leer un entero completo del fichero fuente. 
	 * Como la definicion de nuestro lenguaje no permite la representacion del 0 como '+0' ni '-0', si detectamos un 0
	 * lanzamos una excepcion. Sino, vamos leyendo mientras sean digitos hasta terminar de leer el entero.
	 * 
	 * @param aux String que almacena la parte leda del entero.
	 * @return aux String que almacena el entero reconocido.
	 * @exception IOException que propagamos de las funciones de los RandomAccessFile de JAVA.
	 * @exception Exception que generamos cuando detectamos una secuencia de caracteres incorrecta.
	 */
	public String leerNumero(String aux) throws Exception,IOException{
		char a;
		int i = 0;
		a = (char)fuente.read();
		posicion ++;
		if (a == '0'){
			throw new Exception("ERROR en linea "+linea+": No existe ese numero");
		}
		else{
			aux = aux.concat(new Character(a).toString());
			while (((a = (char)fuente.read()) != -1) && (i <= 8)){
				posicion ++;
				if ((a>='0') && (a<'9')){
					aux = aux.concat(new Character(a).toString());			
				}
				else{
					posicion --;
					fuente.seek(posicion);
					return aux;
				}
				i ++;
			}
			if (i<=8){
				posicion --;
				fuente.seek(posicion);
			}
			return aux;
		}	
	}
	
	/**
	 * El metodo leerCaracter es llamado cuando al menos hemos leido ya una letra. Asi, sslo tenemos que controlar que se lean letras, digitos o '_'.
	 * Es decir, leemos mientras sean caracteres validos segun nuestra especificacion y hasta terminar de leer el identificador.
	 * 
	 * @param aux String que almacena la parte ya leda del identificador.
	 * @return String que almacena el identificador ya reconocido.
	 * @exception IOException que propagamos de las funciones de los RandomAccessFile de JAVA.
	 * @exception Exception que generamos cuando detectamos una secuencia de caracteres incorrecta.
	 * 
	 */
	
	public String leerCaracter(String aux) throws Exception, IOException{
		char a;
		while ((a = (char)fuente.read()) != -1){
			posicion ++;
			if (a!='['){
				if (a!=']'){	
					if (((a>='A') && (a<'z')) || (a=='_') || ((a>='0') && (a<'9'))){
						aux = aux.concat(new Character(a).toString());			
					}
					else{
						posicion --;
						fuente.seek(posicion);
						return aux;
					}
				}
				else{
					posicion --;
					fuente.seek(posicion);
					return aux;
				}
			}
			else{
				posicion --;
				fuente.seek(posicion);
				return aux;
			}
		}	
		return aux;	
	}
	
	/**
	 * El metodo getNextToken devuelve el siguiente Token para poder realizar el preanalisis. 
	 * No avanza el cursor, sino que s?lo "mira" cual es el siguiente Token que leera "lexer()"
	 * @see procesador.Lexico#getToken()
	 * @return Token actual que ha reconocido.
	 * @exception IOException que propagamos de las funciones de los RandomAccessFile de JAVA.
	 * @exception Exception que propagamos de la funcion getToken().
	 */
	public Token getNextToken() throws IOException, Exception{
		int aux = posicion;
		Token tk = getToken();
		fuente.seek(aux);
		posicion = aux;
		return tk;	
		
	}
	
	/**
	 * El metodo lexer actualiza el lxico con el nuevo Token del preanalisis. El nuevo Token se obtiene llamando a getNextToken.
	 * 
	 * @return Token actual que ha reconocido.
	 * @exception IOException que propagamos de las funciones de los RandomAccessFile de JAVA.
	 * @exception Exception que propagamos de la funcion getNextToken().
	 */
	public Token lexer() throws IOException, Exception{
			lookahead = getToken();
			return lookahead;
	}
	
	/**
	 * El metodo reconoce indica si una categora lexica dada es igual o no a la categoria lexica del Token
	 * @param tk Entero que indica una categora lexica. 
	 * @return Booleano que nos dir si son iguales las categoras lexicas.
	 * 
	 */
	public boolean reconoce(int tk){
		boolean aux = true;
		if (tk == lookahead.getCategoriaLexica()){
			aux = true;
		}	
		else{ 
			aux = false;	
		}
		return aux;
	}
}