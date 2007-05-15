package Analizador_Lexico;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class AnLexico {
	
	/**
	 * Atributo estatico de tipo fileReader para leer el archivo 
	 */
	private FileReader ficheroFuente;
	/**
	 * Atributo para poder contabilizar el numero de linea actual
	 */
	private int linea;
	/**
	 * Atributo que indica la parte del lexema que se ha leido
	 */
	private  String lexema;
	/**
	 * Atributo que tendra el siguiente caracter a leer
	 */
	private  char buf;
	/**
	 * Atributo que indica el estado actual en el que nos encontramos
	 */
	private int estado;

	/**
	 * Constructor de Analixador pasando como parametro el fichero
	 * @param fichero que es el fichero del cual se va a leer
	 */
	public AnLexico(FileReader fichero)
	{
	 ficheroFuente=fichero;
	 linea=1;
	 lexema=new String();
	 buf=' ';
	 estado=0;
	}
	
	public  char getBuf() {
		return buf;
	}


	public  void setBuf(char buf) {
		this.buf = buf;
	}


	public  int getEstado() {
		return this.estado;
	}


	public  void setEstado(int estado) {
		this.estado = estado;
	}


	public  FileReader getFicheroFuente() {
		return this.ficheroFuente;
	}


	public  void setFicheroFuente(FileReader ficheroFuente) {
		this.ficheroFuente = ficheroFuente;
	}


	public  String getLexema() {
		return this.lexema;
	}


	public  void setLexema(String lexema) {
		this.lexema = lexema;
	}


	public  int getLinea() {
		return this.linea;
	}


	public  void setLinea(int linea) {
		this.linea = linea;
	}
	
	/**
	 * Metodo al que se llama cuando queremos transitar
	 * Se actualizan los valores de lexema y buf
	 */
	private  void transita()
	{
		lexema+=buf; //Se añade el contenido de buf al lexema
		buf=siguiente_caracter(); //En buf se introduce el siguiente caracter
	}
	
	/**
	 * Metodo que va obteniendo el siguiente caracter del fichero
	 * @return un char que indica el siguient caracter leido
	 */
	private  char siguiente_caracter()
	{
		char aux='\t';
		try{
			//while (((aux=='\n') || (aux=='\t')) || (aux=='\r')) //la ºr no se lo que hace
			//{
				aux=(char)ficheroFuente.read();
				if (aux=='\n')  {linea++;}		
			//}
			
		}	
		catch (FileNotFoundException e) {
			System.out.println("El archivo no se encuentra disponible");
		}
		catch (IOException e) {
			System.out.println("Se ha producido un error en el analizador lexico");
		}	
	return(aux);
	}


	
	//Lleva a cabo el tratamiento de errores
	private  void errorLex(){
		//Mensaje de error
		System.out.println("Error léxico en línea: "+linea+"\n");
		System.out.println("'"+buf+"'"+" carácter inesperado\n");
		System.out.println("Parada del analizador léxico.\n");
		lexema="";//El lexema que se llevaba no es válido.
		estado =-1;//Estado erróneo para que pare el escáner.
		System.exit(0);
	 }
	
	public Token analizador()
	{
		if(((estado!=3)&&(estado!=2)&&(estado!=5))&&(estado!=33))	
			buf=siguiente_caracter();
		estado=0;
		Token token = new Token();
		while (estado!=-1)
		{
			switch(estado)
			{
			case 0:
				lexema="";
				switch(buf)
				{
				case ' ':
				{	estado =0;
					token.setLinea(linea);
					transita();
					
				}	
				break;
				case '\n':
				{	estado =0;
					token.setLinea(linea);
					transita();
					
				}	
				break;
				case '\t':
				{	estado =0;
					token.setLinea(linea);
					transita();
					
				}	
				break;
				case '\r':
				{	estado =0;
					token.setLinea(linea);
					transita();
					
				}	
				break;
				case '.':
				{
					estado=1;
					token.setLinea(linea);
					token.setLexema(".");
					token.setTipo("PUNTO");
				}
				break;
				case ',':
				{
					estado=1;
					token.setLexema(",");
					token.setTipo("COMA");
					token.setLinea(linea);
				}	
				break;
				case ';':
				{
					estado=1;
					token.setLexema(";");
					token.setTipo("PYC");
					token.setLinea(linea);
				}	
				break;
				case '+':
				{
					estado=1;
					token.setLexema("+");
					token.setTipo("OPSUM");
					token.setLinea(linea);
				}	
				break;
				case '*':
				{
					estado=1;
					token.setLexema("*");
					token.setTipo("OPMUL");
					token.setLinea(linea);
				}	
				break;
				case '-':
				{
					estado=1;
					token.setLexema("-");
					token.setTipo("OPSUM");
					token.setLinea(linea);
				}	
				break;
				case '/':
				{
					estado=33;
					token.setLinea(linea);
					transita();
					
				}	
				break;
				case '(':
				{
					estado=1;
					token.setLexema("(");
					token.setTipo("PAA");
					token.setLinea(linea);
				}	
				break;
				case ')':
				{
					estado=1;
					token.setLexema(")");
					token.setTipo("PAC");
					token.setLinea(linea);
				}
				break;
				case '[':
				{
					estado=1;
					token.setLinea(linea);
					token.setLexema("[");
					token.setTipo("CORA");
				}
				break;
				case ']':
				{
					estado=1;
					token.setLinea(linea);
					token.setLexema("[");
					token.setTipo("CORC");
				}
				break;
				case ':':
				{
					estado=1;
					token.setLinea(linea);
					token.setLexema(":");
					token.setTipo("PP");
				}
				break;
				case '^':
				{
					estado=1;
					token.setLinea(linea);
					token.setLexema("^");
					token.setTipo("PUNTERO");
				}
				break;
				case '>':
				{
					estado=2;
					token.setLexema(">");
					token.setTipo("OPORD");
					token.setLinea(linea);
					transita();
				}	
				break;
				case '<':
				{
					estado=2;
					token.setLexema("<");
					token.setTipo("OPORD");
					token.setLinea(linea);
					transita();
				}	
				break;
				case '!':
				{
					estado=2;
					token.setLexema("!");
					token.setTipo("OPNEG");
					token.setLinea(linea);
					transita();
				}	
				break;
				case '=':
				{
					estado=2;
					token.setLexema("=");
					token.setTipo("IGUAL");
					token.setLinea(linea);
					transita();
				}	
				break;
				case 'a':
				case 'b'://Letras minúsculas
				case 'c'://para el comienzo de los identificadores.
				case 'd':
				case 'e':
				case 'f':
				case 'g':
				case 'h':
				case 'i':
				case 'j':
				case 'k':
				case 'l':
				case 'm':
				case 'n':
				case 'ñ':
				case 'o':
				case 'p':
				case 'q':
				case 'r':
				case 's':
				case 't':
				case 'u':
				case 'v':
				case 'w':
				case 'x':
				case 'y':
				case 'z':
					estado=3;
					token.setLinea(linea);
					transita();
					break;
				case '0'://Número natural 0.
					estado=4;//Se pasa al estado S1
					token.setLinea(linea);
					break;
				case '1'://Números naturales.
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				{
					token.setLinea(linea);
					transita();	
					estado=5;//Se para al estado S2.
				}	
				break;
				case '|':
				{
					transita();
					estado=6;
					token.setLexema("|");
					token.setTipo("OPSUM");
					token.setLinea(linea);
				}	
				break;
				case '&':
				{
					transita();
					estado=7;
					token.setLexema("&");
					token.setTipo("OPMUL");
					token.setLinea(linea);
				}	
				break;
				case 'B':
				{
					transita();
					estado=8;
					token.setLinea(linea);
				}
				break;
				case 'E':
				{
					transita();
					estado=15;
					token.setLinea(linea);
				}
				break;
				case 'M':
				{
					transita();
					estado=17;
					token.setLinea(linea);
				}
				break;
				case 'C':
				{
					transita();
					estado=19;
					token.setLinea(linea);
				}
				break;
				case 'I':
				{
					transita();
					estado=23;
					token.setLinea(linea);
				}
				break;
				case 'T':
				{
					transita();
					estado=26;
					token.setLinea(linea);
				}
				break;
				case 'F':
				{
					transita();
					estado=30;
					token.setLinea(linea);
				}
				break;
				case 'W':
				{
					transita();
					estado=43;
					token.setLinea(linea);
				}break;
				case 'A':
				{
					transita();
					estado=48;
					token.setLinea(linea);
				}break;
				case 'O':
				{
					transita();
					estado=53;
					token.setLinea(linea);
				}break;
				case 'R':
				{
					transita();
					estado=54;
					token.setLinea(linea);
				}break;
				case 'P':
				{
					transita();
					estado=56;
					token.setLinea(linea);
				}break;
				case 'U':
				{
					transita();
					estado=62;
					token.setLinea(linea);
				}break;
				case 'D':
				{
					transita();
					estado=66;
					token.setLinea(linea);
				}break;
				default:
					errorLex();
				break;
				}
			break;
			case 1:
			{
				return(token);
			}
			case 2:
			{
				if (buf=='=')
				{
					lexema=lexema+buf;
					if (token.getTipo().equals("OPORD"))
					{
						token.setLexema(lexema);
						estado=1;
					}
					else
					{
						if ((token.getTipo().equals("OPNEG"))||(token.getTipo().equals("IGUAL")))
						{
							token.setLexema(lexema);
							token.setTipo("OPEQ");
							estado=1;
						}
						else
							errorLex();
					}
				}
				else 
					return(token);
			}
			break;
			case 3:
			{
			if (((buf>='a') && (buf<='z')) || ((buf>='A') && (buf<='Z')) ||((buf>='0')&&(buf<='9'))) 
			{
				transita();
				estado=3;
			}	 
			else
			{
				token.setLexema(lexema);
				token.setTipo("ID");
				lexema=""+buf;
				return(token);
			}
			break;
			}
			case 4:
			{
				token.setLexema("0");
				token.setTipo("NUM");
				return(token);
			}
			case 5:
			{

				if((buf>='0')&&(buf<='9')){
					transita();
					estado=5;
				}
				else
				{
					token.setLexema(lexema);
					token.setTipo("NUM");
					return(token);
				}
				break;
			}
			case 6:
			{
				if(buf=='|'){
					lexema=lexema+buf;
					token.setLexema(lexema);
					token.setTipo("OPSUM");
					estado=1;
				}
				else {
					errorLex();
				}
				break;
			}
			case 7:
			{
				if(buf=='&'){
					lexema=lexema+buf;
					token.setLexema(lexema);
					token.setTipo("OPMUL");
					estado=1;
				}
				else {
					errorLex();
				}
				break;
			}
			case 8:
			{
				if (buf=='E')
				{
					estado=9;
					transita();
				}
				else
				{
					if (buf=='O')
					{
						estado=13;
						transita();
					}
					else
						errorLex();
				}
				break;
			}
			case 9:
			{
				if (buf=='G')
				{
					estado=10;
					transita();
				}
				else
					errorLex();
			}
			break;
			case 10:
			{
				if (buf=='I')
				{
					estado=11;
					transita();
				}
				else
					errorLex();
			}
			break;
			case 11:
			{
				if (buf=='N')
				{
					lexema+=buf;
					estado=12;
					//token.set_lexema(lexema);
					//token.set_tipo(_tipo)
				}
				else
					errorLex();
			}
			case 12:
			{
				token.setLexema(lexema);
				token.setTipo(lexema);
				return(token);
			}
			case 13:
			{
				if (buf=='O')
				{
					estado=14;
					transita();
				}
				else
					errorLex();
			}
			break;
			case 14:
			{
				if (buf=='L')
				{
					estado=25;
					lexema+=buf;
				}
				else
					errorLex();
			}
			break;
			case 15:
			{
				if (buf=='N')
				{
					estado=16;
					transita();
				}
				else
				if (buf=='L')
				{
					estado=40;
					transita();
				}
				else 
				if (buf=='I')
				{
					estado=23;
					transita();
				}
				else 
				if (buf=='W')
				{
					estado=43;
					transita();
				}
				else errorLex();
			}
			break;
			case 16:
			{
				if (buf=='D')
				{
					lexema+=buf;
					estado=12;
				}
				else
					errorLex();
			}
			break;
			case 17:
			{
				if (buf=='O')
				{
					estado=18;
					transita();
				}
				else
					errorLex();
			}
			break;
			case 18:
			{
				if (buf=='D')
				{
					lexema+=buf;
					token.setLexema(lexema);
					token.setTipo("OPMUL");
					estado=1;
				}
				else
					errorLex();
			}
			break;
			case 19:
			{
				if (buf=='O')
				{
					estado=20;
					transita();
				}
				else
					errorLex();
			}
			break;
			case 20:
			{
				if (buf=='N')
				{
					estado=21;
					transita();
				}
				else
					errorLex();
			}
			break;
			case 21:
			{
				if (buf=='S')
				{
					estado=22;
					transita();
				}
				else
					errorLex();
			}
			break;
			case 22:
			{
				if (buf=='T')
				{
					estado=12;
					lexema+=buf;
				}
				else
					errorLex();
			}
			break;
			case 23:
			{
				if (buf=='N')
				{
					estado=24;
					transita();
				}
				else
				
					if (buf=='F')
					{
						estado=36;
						transita();
					}
				 else errorLex();
			}
			break;
			case 24:
			{
				if (buf=='T')
				{
					estado=25;
					lexema+=buf;
				}
				else
					errorLex();
			}
			break;
			case 25:
			{
				token.setLexema(lexema);
				token.setTipo("TIPO");
				return(token);
			}
			case 26:
			{
				if (buf=='R')
				{
					estado=27;
					transita();
				}
				else
				if (buf=='H')
				{
					estado=37;
					transita();
				}
				else
					if (buf=='Y')
					{
						estado=67;
						transita();
					}
				else errorLex(); 
					
			}
			break;
			case 27:
			{
				if (buf=='U')
				{
					estado=28;
					transita();
				}
				else
					errorLex();
				break;
			}
			case 28:
			{
				if (buf=='E')
				{
					lexema+=buf;
					estado=29;
				}
				else
					errorLex();
				
			}
			break;
			case 29:
			{
				token.setLexema(lexema);
				token.setTipo("BOOL");
				return(token);
			}
			case 30:
			{
				if (buf=='A')
				{
					estado=31;
					transita();
				}
				else
				if (buf=='R')
				{
					estado=54;
					transita();
				}
				else	
				if (buf=='T')
				{
					estado=26;
					transita();
				}
				else	errorLex();
			}
			break;
			case 31:
			{
				if (buf=='L')
				{
					estado=32;
					transita();
				}
				else
					errorLex();
			}
			case 32:
			{
				if (buf=='S')
				{
					estado=28;
					transita();
				}
				else
					errorLex();
			}
			break;
			case 33:
			{
				if (buf=='*')
				{
					estado=34;
					transita();
				}
				else
				{
					token.setLexema(lexema);
					token.setTipo("OPMUL");
					return(token);
				}
			}
			break;
			case 34:
			{
				if (buf=='*')
				{
					estado=35;		
				}
				transita();
			}
			break;
			case 35:
			{
				if (buf=='/')
				{
					estado=0;
					lexema="";
					transita();
				}
				else
				{
					estado=34;
					transita();
				}
			}
			break;
			case 36:
			{
				token.setLinea(linea);
				token.setLexema(lexema);
				token.setTipo(lexema);
				return(token);
			}
			case 37:
			{
				if (buf=='E')
				{
					estado=38;
					transita();
				}
				else errorLex();
			}break;
			case 38:
			{
				if (buf=='N')
				{
					estado=39;
					transita();
				}
				else errorLex();
			} 
			break;
			case 39:
			{
				token.setLexema(lexema);
				token.setLinea(linea);
				token.setTipo("THEN");
				return(token);
			}
			case 40:
			{
				if (buf=='S')
				{
					estado=41;
					transita();
				}
				else errorLex();
			} 
			break;
			case 41:
			{
				if (buf=='E')
				{
					estado=42;
					transita();
				}
				else errorLex();
			} 
			break;
			case 42:
			{
				token.setLexema(lexema);
				token.setLinea(linea);
				token.setTipo(lexema);
				return(token);
			} 
			case 43:
			{
				if (buf=='H')
				{
					estado=44;
					transita();
				}
				else errorLex();
			} 
			break;
			case 44:
			{
				if (buf=='I')
				{
					estado=45;
					transita();
				}
				else errorLex();
			} 
			break;
			case 45:
			{
				if (buf=='L')
				{
					estado=46;
					transita();
				}
				else errorLex();
			} 
			break;
			case 46:
			{
				if (buf=='E')
				{
					estado=47;
					transita();
				}
				else errorLex();
			} 
			break;
			case 47:
			{
				token.setLexema(lexema);
				token.setLinea(linea);
				token.setTipo(lexema);
				return(token);
			}
			case 48:
			{
				if (buf=='R')
				{
					estado=49;
					transita();
				}
				else errorLex();
			}break;
			case 49:
			{
				if (buf=='R')
				{
					estado=50;
					transita();
				}
				else errorLex();
			}
			break;
			case 50:
			{
				if (buf=='A')
				{
					estado=51;
					transita();
				}
				else errorLex();
			}
			break;
			case 51:
			{
				if (buf=='Y')
				{
					estado=52;
					transita();
				}
				else errorLex();
			}
			break;
			case 52:
			{
				token.setLexema(lexema);
				token.setLinea(linea);
				token.setTipo(lexema);
				return(token);
			}
			case 53:
			{
				if (buf=='F')
				{
					estado=52;
					transita();
				}
				else errorLex();
			}
			break;
			case 54:
			{
				if (buf=='E')
				{
					estado=55;
					transita();
				}
				else errorLex();
			}break;
			case 55:
			{
				if (buf=='C')
				{
					estado=69;
					transita();
				}
				else if (buf=='P')
				{
					estado=59;
					transita();
				}else
					errorLex();
				
			}break;
			case 56:
			{
				if (buf=='R')
				{
					estado=57;
					transita();
				}
				else
				if (buf=='O')
				{
					estado=72;
					transita();
				}
				else errorLex();
			}break;
			case 57:
			{
				if (buf=='O')
				{
					estado=58;
					transita();
				}
				else errorLex();
			}break;
			case 58:
			{
				if (buf=='C')
				{
					estado=52;
					transita();
				}
				else errorLex();
			}break;
			case 59:
			{
				if (buf=='E')
				{
					estado=60;
					transita();
				}
				else errorLex();
			}break;
			case 60:
			{
				if (buf=='A')
				{
					estado=61;
					transita();
				}
				else errorLex();
			}break;
			case 61:
			{
				if (buf=='T')
				{
					estado=52;
					transita();
				}
				else errorLex();
			}break;
			case 62:
			{
				if (buf=='N')
				{
					estado=63;
					transita();
				}
				else errorLex();
			}break;
			case 63:
			{
				if (buf=='T')
				{
					estado=64;
					transita();
				}
				else errorLex();
			}break;
			case 64:
			{
				if (buf=='I')
				{
					estado=65;
					transita();
				}
				else errorLex();
			}break;
			case 65:
			{
				if (buf=='L')
				{
					estado=52;
					transita();
				}
				else errorLex();
			}break;
			case 66:
			{
				if (buf=='O')
				{
					estado=52;
					transita();
				}
				else errorLex();
			}break;
			case 67:
			{
				if (buf=='P')
				{
					estado=68;
					transita();
				}
				else errorLex();
			}break;
			case 68:
			{
				if (buf=='E')
				{
					estado=52;
					transita();
				}
				else errorLex();
			}break;
			case 69:
			{
				if (buf=='O')
				{
					estado=70;
					transita();
				}
				else errorLex();
			}break;
			case 70:
			{
				if (buf=='R')
				{
					estado=71;
					transita();
				}
				else errorLex();
			}break;
			case 71:
			{
				if (buf=='D')
				{
					estado=52;
					transita();
				}
				else errorLex();
			}break;
			case 72:
			{
				if (buf=='I')
				{
					estado=73;
					transita();
				}
				else errorLex();
			}break;
			case 73:
			{
				if (buf=='N')
				{
					estado=74;
					transita();
				}
				else errorLex();
			}break;
			case 74:
			{
				if (buf=='T')
				{
					estado=75;
					transita();
				}
				else errorLex();
			}break;
			case 75:
			{
				if (buf=='E')
				{
					estado=76;
					transita();
				}
				else errorLex();
			}break;
			case 76:
			{
				if (buf=='R')
				{
					estado=52;
					transita();
				}
				else errorLex();
			}break;
			default:
				errorLex();
			}//de estado
		
				
			
	}
		return(token);
	}


	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
				
		
		Token tActual=new Token();
		FileReader ficheroFuente = new FileReader("Pruebanueva.txt");
			            try{
			            //Apertura del fichero fuente
						ficheroFuente = new FileReader("Pruebanueva.txt");
			            }catch(FileNotFoundException e) {
			    			System.out.println("Error en el analizador léxico \n");
			    			e.printStackTrace();
			    		}
			    		//Pruebas
			            AnLexico analizador= new AnLexico(ficheroFuente );
			           do{
			         tActual = analizador.analizador();   
			         System.out.println("El token es "+tActual.getLexema()+" y el tipo "+tActual.getTipo() + ". Línea: " + tActual.getLinea() + ".");
			           }while(!tActual.getLexema().equals("END"));

				}
}
				
				


