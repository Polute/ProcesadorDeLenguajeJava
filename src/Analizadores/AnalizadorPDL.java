package Analizadores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.aedlib.Pair;

public class AnalizadorPDL {
	private static BufferedReader reader;
	private static String sig_token;
	private static String parse;
	private static boolean hayErrorSin;
	private static int linea = 0;
	private static int contador = 2;
	private static List<String> tipoParam = new ArrayList<>();
	private static List<String> paramDentro = new ArrayList<>();
	private static int numParam = 0;
	private static String retorno = "void";
	private static String retorno1 = "void";
	private static String tipo;
	private static int lineNumber = 0;
	private static int desplazamiento = 0;
	private static String nombreArchivoLectura = "ficheros/PruebaAnalizadorLexico.txt"; 
	private static String nombreArchivoTokens = "ficheros/TokensAnalizadorLexico.txt";
	private static String nombreArchivoTablaSimbolosG = "ficheros/TablaDeSimbolosGlobal.txt";
	private static String nombreArchivoTablaSimbolosL = "ficheros/TablaDeSimbolosLocal.txt";
	private static String nombreArchivoGestorErrores = "ficheros/GestorErrores.txt";
	private static String nombreArchivoParse = "ficheros/Parse.txt";
	private static Map<Integer, StringBuilder> reservadas2 = new HashMap<>();
	private static boolean soloEnteros = false;
	private static boolean soloEnterosYCadenas = false;
	private static boolean esFuncion = false;
	private static String lexema2;
	private static Map<String, Integer> reservadas3 = new HashMap<>();
	private static boolean esAsignacion;
	private static boolean esPut, esIF, esLet, tieneParentesis = false;
	private static String lexemaFuncion = "";
	private static List<String> tipos = new ArrayList<>();
	private static boolean primerArgFun = false;
	
	//Lista que contiene todos los lexemas
	private static List<String> lexemas = new ArrayList<>();
	
	//Lista con nombre el ID y numero de linea
	private static List<Pair<String, Integer>> listaIDs = new ArrayList<>();
	
	//Lista con token y numero de linea
	private static List<Pair<String, Integer>> listaTokenLinea = new ArrayList<>();
	
	//Contador que solo aumenta cuando ya se recoge el lexema de los ID, o se llega a funcion
	private static int contadorGlobal, contadorTabla, contadorTablaF, contadorTokens = 0;
	
	public static void AnalizadorSintactico(String filePath) {
		try {
			reader = new BufferedReader(new FileReader(filePath));
			hayErrorSin = false;
			sig_token = getSig_token();
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getSig_token() throws IOException {
		linea++;
		contadorTokens++;
		return reader.readLine();
	}

	public static void equipara(String token) {
		if (sig_token.equals(token) && !hayErrorSin) {
			try {
				sig_token = getSig_token();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Error sintactico en la linea: " + linea);
		}
	}

	// Funci n para crear un archivo de texto si no existe y agregar l neas de texto
	public static void agregarLineasAUnArchivo(String nombreArchivo, String[] lineas) {
		try {
			File archivo = new File(nombreArchivo);

			// Si el archivo no existe, se crea
			if (!archivo.exists()) {
				archivo.createNewFile();
			}

			// Se abre el archivo para escritura
			FileWriter fw = new FileWriter(archivo, true);
			BufferedWriter bw = new BufferedWriter(fw);

			// Se agregan las l neas de texto al archivo
			for (String linea : lineas) {
				if (linea != null) {
					bw.write(linea);
					bw.newLine();
				}
			}

			// Se cierra el archivo
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void lector() {
		try {
			FileReader fr = new FileReader(nombreArchivoLectura);
			BufferedReader br = new BufferedReader(fr);
			Map<String, String> reservadas = new HashMap<>();
			reservadas.put("for", "CodFor");
			reservadas.put("true", "CodTrue");
			reservadas.put("false", "CodFalse");
			reservadas.put("boolean", "CodBoolean");
			reservadas.put("function", "CodFunction");
			reservadas.put("get", "CodGet");
			reservadas.put("if", "CodIf");
			reservadas.put("int", "CodInt");
			reservadas.put("let", "CodLet");
			reservadas.put("put", "CodPut");
			reservadas.put("return", "CodReturn");
			reservadas.put("string", "CodString");
			reservadas.put("void", "CodVoid");
			int caracter;
			int caracterAnterior = 0;
			int posicion = 0;
			int posicionID = 0;
			String lexema = "";
			int linea = 1;
			boolean seguirLexema = false;
			boolean noSeEjecuto = true;
			while ((caracter = br.read()) != -1 && !seguirLexema) {
				if (caracter == '\n') {
					linea++;
				}
				posicionID = posicion;
				posicion++;
				if (Character.isLetter(caracter)) {
					if (Character.isDigit(caracterAnterior)) {

						error(1, linea);
					} else {
						StringBuilder palabra = new StringBuilder();
						palabra.append((char) caracter);
						while ((caracter = br.read()) != -1
								&& (Character.isLetterOrDigit(caracter) || caracter == '_')) {
							if (caracter == '\n') {
								linea++;
							}
							posicion++;
							palabra.append((char) caracter);
						}
						String palabraReservada = palabra.toString();
						if (reservadas.containsKey(palabraReservada)) {
							String token = reservadas.get(palabraReservada);
							genToken(token, "",linea);
						} else {
							reservadas2.put(posicionID, palabra);
							genToken("ID", posicionID,linea);
						}
					}
				}
				if (caracter == '=') {
					noSeEjecuto = false;
					if (br.ready()) {
						caracter = br.read();
						if (caracter == '\n') {
							linea++;
						}
						posicion++;
						posicionID = posicion;
					}
					if (caracter == '=') {
						caracterAnterior = caracter;
						genToken("Igual", "",linea);
					} else if (Character.isWhitespace(caracter)) {
						caracterAnterior = caracter;
						genToken("Asignacion", "",linea);
					} else {
						genToken("Asignacion", "",linea);
						if (Character.isLetter(caracter)) {
							StringBuilder palabra = new StringBuilder();
							palabra.append((char) caracter);
							while ((caracter = br.read()) != -1
									&& (Character.isLetterOrDigit(caracter) || caracter == '_')) {
								if (caracter == '\n') {
									linea++;
								}
								posicion++;
								palabra.append((char) caracter);
							}
							String palabraReservada = palabra.toString();
							if (reservadas.containsKey(palabraReservada)) {
								String token = reservadas.get(palabraReservada);
								genToken(token, "",linea);
							} else {
								reservadas2.put(posicionID, palabra);
								genToken("ID", posicionID,linea);
							}
						}
					}
				}
				if (caracter == '-') {
					if (br.ready()) {
						caracter = br.read();
						if (caracter == '\n') {
							linea++;
						}
						posicion++;
						posicionID = posicion;
					}
					if (caracter == '-') {
						caracterAnterior = caracter;
						genToken("DEC", "",linea);
						posicion++;
					} else if (Character.isWhitespace(caracter)) {
						caracterAnterior = caracter;
						genToken("Resta", "",linea);
						posicion++;
					} else {
						genToken("Resta", "",linea);
						if (Character.isLetter(caracter)) {
							StringBuilder palabra = new StringBuilder();
							palabra.append((char) caracter);
							while ((caracter = br.read()) != -1
									&& (Character.isLetterOrDigit(caracter) || caracter == '_')) {
								if (caracter == '\n') {
									linea++;
								}
								posicion++;
								palabra.append((char) caracter);
							}
							String palabraReservada = palabra.toString();
							if (reservadas.containsKey(palabraReservada)) {
								String token = reservadas.get(palabraReservada);
								genToken(token, "",linea);
							} else {
								reservadas2.put(posicionID, palabra);
								genToken("ID", posicionID,linea);
							}
						}
					}
				}
				if (caracter == '=' && noSeEjecuto) {
					if (br.ready()) {
						caracter = br.read();
						if (caracter == '\n') {
							linea++;
						}
						posicion++;
						posicionID = posicion;
					}
					if (caracter == '=') {
						caracterAnterior = caracter;
						genToken("Igual", "",linea);
						posicion++;
					} else if (Character.isWhitespace(caracter)) {
						caracterAnterior = caracter;
						genToken("Asignacion", "",linea);
					} else {
						genToken("Asignacion", "",linea);
						if (Character.isLetter(caracter)) {
							StringBuilder palabra = new StringBuilder();
							palabra.append((char) caracter);
							while ((caracter = br.read()) != -1
									&& (Character.isLetterOrDigit(caracter) || caracter == '_')) {
								if (caracter == '\n') {
									linea++;
								}
								posicion++;
								palabra.append((char) caracter);
							}
							String palabraReservada = palabra.toString();
							if (reservadas.containsKey(palabraReservada)) {
								String token = reservadas.get(palabraReservada);
								genToken(token, "",linea);
							} else {
								reservadas2.put(posicionID, palabra);
								genToken("ID", posicionID,linea);
							}
						}
					}
				}
				if (caracter == '"') {
					seguirLexema = true;
					char comillas = '"';
					lexema += comillas;
					while ((caracter = br.read()) != -1 && seguirLexema) {
						if (caracter == '\n') {
							linea++;
						}
						posicion++;
						if (caracter != '"') {
							lexema += (char) caracter;
						} else {
							lexema += '"';
							genToken("Cadena", lexema,linea);
							lexema = "";
							seguirLexema = false;
						}
					}
					caracterAnterior = caracter;
				}
				if (caracter == '/' && br.ready()) {
					if (br.read() == '*') {
						seguirLexema = true;
						while ((caracter = br.read()) != -1 && seguirLexema) {
							if (caracter == '\n') {
								linea++;
							}
							posicion++;
							if (caracter == '*') {
								if (br.read() == '/') {
									seguirLexema = false;
								}
							}
						}
					} else {
						caracterAnterior = caracter;
						error(3, linea);
					}

				}
				if (Character.isDigit(caracter)) {
					int valor = caracter - '0';
					caracterAnterior = caracter;
					while ((caracter = br.read()) != -1 && Character.isDigit(caracter)) {
						if (caracter == '\n') {
							linea++;
						}
						posicion++;
						valor = valor * 10 + (caracter - '0');
						caracterAnterior = caracter;
					}

					if (valor > 32767) {
						error(2, linea);

					} else {
						genToken("Entero", valor,linea);
					}
				}

				if (caracter == '&' && br.ready()) {
					caracter = br.read();
					if (caracter == '\n') {
						linea++;
					}
					if (caracter == '&') {
						posicion++;
						posicion++;
						posicion++;
						caracterAnterior = caracter;
						genToken("AND", "",linea);
					} else {
						error(6, linea);
					}
				}

				if (caracter == '|' && br.ready()) {
					caracter = br.read();
					if (caracter == '\n') {
						linea++;
					}
					if (caracter == '|') {
						posicion++;
						posicion++;
						posicion++;
						caracterAnterior = caracter;
						genToken("OR", "",linea);
					} else {
						caracterAnterior = caracter;
						error(5, linea);
					}
				}
				if (caracter == '+') {
					caracterAnterior = caracter;
					genToken("Suma", "",linea);
					posicion++;

				}
				if (caracter == '!') {
					if (br.ready()) {
						caracter = br.read();
						if (caracter == '\n') {
							linea++;
						}
						if (caracter == '=') {
							posicion++;
							posicion++;
							posicion++;
							caracterAnterior = caracter;
							genToken("Distinto", "",linea);
						} else {
							error(4, linea);
						}
					}
				}
				if (caracter == '*') {
					caracterAnterior = caracter;
					genToken("Mult", "",linea);

				}
				if (caracter == '(') {
					caracterAnterior = caracter;
					genToken("ParentesisA", "",linea);

				}
				if (caracter == ')') {
					caracterAnterior = caracter;
					genToken("ParentesisC", "",linea);

				}
				if (caracter == ',') {
					caracterAnterior = caracter;
					genToken("Coma", "",linea);
				}
				if (caracter == ';') {
					caracterAnterior = caracter;
					genToken("PuntoyComa", "",linea);
				}
				if (caracter == '{') {
					caracterAnterior = caracter;
					genToken("LlaveA", "",linea);
				}
				if (caracter == '}') {
					caracterAnterior = caracter;
					genToken("LlaveC", "",linea);
				}
				noSeEjecuto = true;
				if (Character.isWhitespace(caracter)) {
					caracterAnterior = caracter;
				}
				if(caracter == '@' || caracter == '#' || caracter == '~' || caracter == '$' || caracter == '%' 
						|| caracter == '^' || caracter == '.' || caracter == ':'
						|| caracter == '<' || caracter == '>' || caracter == '_'  ) {
					error(caracter, linea);
				}
			}
			genToken("EOF", "",linea);

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void genToken(String codigo, Object e, int linea) {
		String[] token = new String[1];
		token[0] = "<" + codigo + "," + e + ">";
		listaTokenLinea.add(new Pair<String, Integer>(token[0], linea));
		agregarLineasAUnArchivo(nombreArchivoTokens, token);
	}

	public static void agregarIdentificadoresATablaSimbolos() {
		try {
			Map<String, String> reservadas = new HashMap<>();
			reservadas.put("for", "CodFor");
			reservadas.put("true", "CodTrue");
			reservadas.put("false", "CodFalse");
			reservadas.put("function", "CodFunction");
			reservadas.put("get", "CodGet");
			reservadas.put("if", "CodIf");
			reservadas.put("int", "CodInt");
			reservadas.put("boolean", "CodBoolean");
			reservadas.put("let", "CodLet");
			reservadas.put("put", "CodPut");
			reservadas.put("return", "CodReturn");
			reservadas.put("string", "CodString");
			reservadas.put("void", "CodVoid");

			FileReader fr = new FileReader(nombreArchivoLectura);
			BufferedReader br = new BufferedReader(fr);
			String[] lineas = new String[1];
			
			

			int caracter;
			boolean seguirLexema = true;
			int posicionIDLinea = 0;
			while ((caracter = br.read()) != -1) {
				
				seguirLexema = true;
				if(caracter == '\n') {
					posicionIDLinea++;
				}
				if (Character.isLetter(caracter)) {
					String lexema = "";
					lexema += (char) caracter;
					while ((caracter = br.read()) != -1 && (Character.isLetterOrDigit(caracter) || caracter == '_')) {
						lexema += (char) caracter;
					}
					if (!reservadas.containsKey(lexema)) {
						lineas[0] = "* lexema : '" + lexema + "'";
						reservadas.put(lexema, "Agregado");
						listaIDs.add(new Pair<String, Integer>(lexema,posicionIDLinea));
					}

				}
				if (caracter == '"') {
					while ((caracter = br.read()) != -1 && seguirLexema) {
						if (caracter == '"')
							seguirLexema = false;
					}
				}
				if (caracter == '/' && br.ready()) {
					if (br.read() == '*') {
						seguirLexema = true;
						while ((caracter = br.read()) != -1 && seguirLexema) {
							if(caracter == '\n') {
								posicionIDLinea++;
							}
							if (caracter == '*') {
								if (br.read() == '/') {
									seguirLexema = false;
								}
							}
						}
					}
				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void erroresLexico() {
		for (int i=0; i< listaTokenLinea.size()-1; i++) {
            String tokenActual = listaTokenLinea.get(i).getLeft();
            int line = listaTokenLinea.get(i).getRight();     
            if (tokenActual.equals(listaTokenLinea.get(i+1).getLeft()) && tokenActual.contains("PuntoyComa")) {
            	error(9, line);
            }
            else if(tokenActual.equals(listaTokenLinea.get(i+1).getLeft()) && tokenActual.contains("Coma")) {
            	error(7, line);
            }
        }
    }


	// Localizados por posiciones, y no por lineas
	public static void error(int codigoError, int posicion) {
		String msgErrorLex = "Error Lexico -> ";
		if(codigoError < 11) {
			Map<Integer, String> errores = new HashMap<>();
			
			String[] errorGenerico = new String[1];
			errorGenerico[0] = msgErrorLex + "Ha ocurrido un error en la linea " + posicion + " relacionado con: ";

			errores.put(1, "El ID/palabra reservada no empieza  por una letra");
			errores.put(2, "El valor del entero supera al numero 32767");
			errores.put(3, "Falta un '*' para empezar el comentario");
			errores.put(4, "Falta un '=' para formar la operacion != ");
			errores.put(5, "Falta un '|' para formar la operacion OR ");
			errores.put(6, "Falta un '&' para formar la operacion AND ");
			errores.put(7, "No puede haber dos ',' seguidas ");
			errores.put(9, "No puede haber dos ';' seguidos ");
		
			errorGenerico[0] += errores.get(codigoError);
			agregarLineasAUnArchivo(nombreArchivoGestorErrores, errorGenerico);
		}
		else {
			String[] mensajeErrorS = new String[1];
			String mensajeError = msgErrorLex + "Caracter invalido ( "+(char)codigoError+" ) en la linea "+ posicion;
			mensajeErrorS[0] = mensajeError;
			agregarLineasAUnArchivo(nombreArchivoGestorErrores, mensajeErrorS);
		}
		

	}

	public static Map<Integer, StringBuilder> get(Map<Integer, StringBuilder> lexemas) {
		return lexemas;
	}
	public static void erroresSintactico(String token, int numeroToken, int tipoError) {
		String[] mensajeErrorS = new String[1];
		String msgErrorSint = "Error Sintactico -> ";
		String mensajeError = "";
		if(tipoError == 0) {
			mensajeError = "Error detectado debido al token " + token + " en la linea " + listaTokenLinea.get(numeroToken).getRight();
			mensajeErrorS[0] = msgErrorSint + mensajeError;
			agregarLineasAUnArchivo(nombreArchivoGestorErrores, mensajeErrorS);
			return;
		}
		switch(tipoError) {
			case 1:
				mensajeError = "Se esperaba una expresion";
				break;
			case 2:
				mensajeError = "Falta un operador";
				break;
			case 3:
				mensajeError = "Se esperaba un 'identificador' o 'put' o 'return'";
				break;
			case 4:
				mensajeError = "Se esperaba un '=' o '(' o ';'";
				break;
			case 5:
				mensajeError = "Expresiones o argumentos no validos";
				break;
			case 6:
				mensajeError = "Falta un identificador";
				break;
			case 7:
				mensajeError = "Se esperaba un 'if' o 'let' o 'for'"
						+ " o 'identificador' o 'put' o 'get' o 'return'";
				break;
			case 8:
				mensajeError = "Tipo de datos no reconocido o no valido";
				break;
			case 9:
				mensajeError = "Declaracion de funcion incorrecta";
				break;
			case 10:
				mensajeError = "Tipo de parametro no valido o inexistente";
				break;
			case 11:
				mensajeError = "Faltan los subtipos de una funcion en los argumentos";
				break;
			case 12:
				mensajeError = "Declaracion de funcion incompleta";
				break;
			case 13:
				mensajeError = "Falta declaracion del tipo con let";
				break;
		}
		String mensajeErrorAux = ", ocurre en la linea "+ listaTokenLinea.get(numeroToken).getRight();
		mensajeErrorS[0] =  msgErrorSint + mensajeError + mensajeErrorAux ;
		agregarLineasAUnArchivo(nombreArchivoGestorErrores, mensajeErrorS);
	}
	public static void erroresSemantico(String token, int numeroToken, int tipoError) {
		String[] mensajeErrorS = new String[1];
		String msgErrorSem = "Error Semantico -> ";
		String mensajeError = "";
		if(tipoError == 0) {
			mensajeError = "Error detectado";
			mensajeErrorS[0] = mensajeError;
			agregarLineasAUnArchivo(nombreArchivoGestorErrores, mensajeErrorS);
			return;
		}
		switch(tipoError) {
			case 1:
				mensajeError =  "Se ha hecho predecremento a un tipo incorrecto";
				break;
			case 2:
				mensajeError = "Asignacion no valida";
				break;
			case 3:
				mensajeError = "Las operaciones aritmeticas no aceptan logicos";
				break;
			case 4:
				mensajeError = "Tipo IF = tipo_error";
				break;
			case 5:
				mensajeError = "La funcion no devuelve lo que debe";
				break;
			case 6:
				mensajeError = "No se puede volver a inicializar un objeto ya inicializado";
				break;
		}
		String mensajeErrorAux = ", ocurre en la linea "+ listaTokenLinea.get(numeroToken).getRight();
		mensajeErrorS[0] = msgErrorSem + mensajeError + mensajeErrorAux ;
		agregarLineasAUnArchivo(nombreArchivoGestorErrores, mensajeErrorS);
	}
	//Analizador sintactico y Semantico
	public static void P() {
		if (!hayErrorSin) {
			if (sig_token.equals("<CodIf,>") || sig_token.equals("<CodLet,>") || sig_token.contains("ID")
					|| sig_token.equals("<CodPut,>") || sig_token.equals("<CodGet,>")
					|| sig_token.equals("<CodReturn,>") || sig_token.equals("<CodFor,>")) {
				parse += " 1";
				B();
				P();
			} else if (sig_token.equals("<CodFunction,>")) {
				parse += " 2";
				esFuncion = true;
				F();
				esFuncion = false;
				P();
			} else if (sig_token.equals("<DEC,>")) {
				parse += " 3";
				equipara("<DEC,>");
				S();
				P();
			} else if (sig_token.equals("<CodInt,>") || sig_token.equals("<CodBoolean,>")
					|| sig_token.equals("<CodString,>")) {
				parse += " 4";
				T();
				P();
			} else if (sig_token.equals("<PuntoyComa,>")) {
				equipara("<PuntoyComa,>");
				P();
			} else if (sig_token.equals("<EOF,>")) {
				parse += " 5";
				equipara("<EOF,>");
			} else if (sig_token.equals("<ParentesisC,>")) {
			} else {
				erroresSintactico(sig_token,contadorTokens,0);
				hayErrorSin = true;
			}
		}
	}

	public static void E() {
		parse += " 6";
		Y();
	}

	public static void Y() {

		if (!hayErrorSin) {

			if (sig_token.contains("ID") || sig_token.equals("<ParentesisA,>") || sig_token.contains("Entero")
					|| sig_token.contains("Cadena") || sig_token.equals("<CodTrue,>") || sig_token.equals("<CodFalse,>")
					|| sig_token.equals("<CodVoid,>") || sig_token.equals("<DEC,>")) {
				parse += " 7";
				U();
			}

			// Y -> LAMBDA
			else if (sig_token.equals("<EOF,>")) {
				parse += " 8";
			}
			else {
				erroresSintactico(sig_token,contadorTokens,1);
				//agregarErrorAGestorErrores("Se esperaba una expresion");
				hayErrorSin = true;
			}
		}

	}

	public static void U() {
		if (!hayErrorSin) {
			if (sig_token.contains("ID") || sig_token.equals("<ParentesisA,>") || sig_token.contains("Entero")
					|| sig_token.contains("Cadena") || sig_token.equals("<CodTrue,>") || sig_token.equals("<CodFalse,>")
					|| sig_token.equals("<CodVoid,>")) {
				parse += " 9";
				V();
				Z();
			}

			else {
				erroresSintactico(sig_token,contadorTokens,1);
				hayErrorSin = true;
			}

		}
	}

	public static void Z() {

		if (!hayErrorSin) {
			// Para los operadores aritmeticos
			if (sig_token.equals("<Suma,>")) {
				parse += " 10";
				equipara("<Suma,>");
				soloEnterosYCadenas = true;
				V();
				Z();
			}

			else if (sig_token.equals("<Resta,>")) {
				parse += " 11";
				equipara("<Resta,>");
				soloEnterosYCadenas = true;
				V();
				Z();
			}

			else if (sig_token.equals("<Mult,>")) {
				parse += " 12";
				equipara("<Mult,>");
				soloEnterosYCadenas = true;
				V();
				Z();
			}
			if (sig_token.equals("<AND,>")) {
				parse += " 13";
				equipara("<AND,>");
				V();
				Z();
			} else if (sig_token.equals("<OR,>")) {
				parse += " 14";
				equipara("<OR,>");
				V();
				Z();
			}

			// Para los operadores de comparacion
			else if (sig_token.equals("<Distinto,>")) {
				parse += " 15";
				equipara("<Distinto,>");
				Z();
			}

			else if (sig_token.equals("<Igual,>")) {
				parse += " 16";
				equipara("<Igual,>");
				Z();
			}

			else if (sig_token.contains("ID") || sig_token.equals("<ParentesisA,>") || sig_token.contains("Entero")
					|| sig_token.contains("Cadena") || sig_token.equals("<CodTrue,>") || sig_token.equals("<CodFalse,>")
					|| sig_token.equals("<CodVoid,>")) {
				parse += " 17";
				V();
			} else if (sig_token.equals("<Coma,>")) {
				parse += " 18";

			} else if (sig_token.equals("<EOF,>") || sig_token.equals("<ParentesisC,>") || sig_token.equals("<PuntoyComa,>")) {
				if (!parse.trim().endsWith("18")) {
					parse += " 18";
				}
				else if(esAsignacion && tieneParentesis){
					parse += " 18";
				}
			}

			else {
				erroresSintactico(sig_token,contadorTokens,2);
				hayErrorSin = true;
			}
		}
	}

	public static void V() {
		if (!hayErrorSin) {
			if (sig_token.contains("ID")) {
				parse += " 19";
				if (!esAsignacion)
					tipos.add(comprobarTipo(obtenerPos(sig_token)));	
				if (esFuncion && retorno != "void")
					retorno1 = comprobarTipo(obtenerPos(sig_token));
				equipara(sig_token);
				G();
				if (soloEnteros) {
					if (!tipos.contains("entero")) {
						erroresSemantico(sig_token,contadorTokens,1);
						//agregarErrorAGestorErroresSeman("Se ha hecho predecremento a un " + tipos.get(0));
					} else {
						System.out.println("D.tipo = tipo_ok");
					}
					tipos.clear();
				}

			}

			else if (sig_token.equals("<ParentesisA,>")) {
				parse += " 20";
				equipara("<ParentesisA,>");
				tieneParentesis = true;
				E();
				tieneParentesis = false;
				equipara("<ParentesisC,>");
			}

			else if (sig_token.contains("Entero")) {
				parse += " 21";
				if(esIF) {
					tipos.add("entero");
				}
				if(esFuncion) {
					retorno1 = "entero";
				}
				if (!reservadas3.containsKey(lexema2)) {
					reservadas3.put(lexema2, 1);
					if (!esPut && !yaEnTS(lexema2)) {
						if(!esFuncion) {
							agregarAtributosATablaSimbolos("entero", 0, null, null, contador, false, false);
						}
						else {
							agregarAtributosATablaSimbolos("entero", 0, null, null, contador, true, primerArgFun);
						}
						contador++;
					}
				}
				equipara(sig_token);
			}

			else if (sig_token.contains("Cadena")) {
				parse += " 22";
				if(esIF) {
					tipos.add("cadena");
				}
				if(esFuncion) {
					retorno1 = "cadena";
				}
				if (!reservadas3.containsKey(lexema2)) {
					reservadas3.put(lexema2, 1);
					if (!esPut && !yaEnTS(lexema2)) {
						if(!esFuncion) {
							agregarAtributosATablaSimbolos("cadena", 0, null, null, contador, false, false);
						}
						else {
							agregarAtributosATablaSimbolos("cadena", 0, null, null, contador, true, primerArgFun);
						}
						contador++;
					}
				}
				equipara(sig_token);
			}

			else if (sig_token.equals("<CodTrue,>") || sig_token.equals("<CodFalse,>")) {
				parse += " 23";
				if(esIF) {
					tipos.add("logico");
				}
				if(esFuncion) {
					retorno1 = "boolean";
				}

				if (!reservadas3.containsKey(lexema2) && !esIF) {
					reservadas3.put(lexema2, 1);
					if (!esPut && !yaEnTS(lexema2)) {
						if(!esFuncion) {
							agregarAtributosATablaSimbolos("logico", 0, null, null, contador, false, false);
						}
						else {
							agregarAtributosATablaSimbolos("logico", 0, null, null, contador, true, primerArgFun);
						}
						contador++;
						
					}
				}
				equipara(sig_token);
			}

			else if (sig_token.equals("<CodVoid,>")) {
				parse += " 24";
				if(esIF) {
					tipos.add("void");
				}
				if (!reservadas3.containsKey(lexema2)) {
					reservadas3.put(lexema2, 1);
					if (!esPut && !yaEnTS(lexema2)) {
						if(!esFuncion) {
							agregarAtributosATablaSimbolos("void", 0, null, null, contador, false, false);
						}
						else {
							agregarAtributosATablaSimbolos("void", 0, null, null, contador, true, primerArgFun);
						}
						contador++;
					}
				}
				equipara("<CodVoid,>");
			}

			else {
				erroresSintactico(sig_token,contadorTokens,1);
				hayErrorSin = true;
			}
		}

	}

	public static void G() {

		if (!hayErrorSin) {
			if (sig_token.equals("<ParentesisA,>")) {
				parse += " 25";
				equipara("<ParentesisA,>");
				L();
				equipara("<ParentesisC,>");
				esAsignacion = false;
			} else if (sig_token.equals("<PuntoyComa,>") || sig_token.equals("<ParentesisC,>")) {
				parse += " 26";

			} else if (sig_token.equals("<EOF,>")) {
				parse += " 26";
			} else if (sig_token.equals("<Suma,>") || sig_token.equals("<Resta,>") || sig_token.equals("<Coma,>")
					|| sig_token.equals("<Mult,>") || sig_token.equals("<Igual,>") || sig_token.equals("<Distinto,>")
					|| sig_token.equals("<AND,>") || sig_token.equals("<OR,>")) {
				parse += " 26";
			}

			else {
				erroresSintactico(sig_token,contadorTokens,1);
				hayErrorSin = true;
			}
		}
	}

	public static void S() {

		if (!hayErrorSin) {
			if (sig_token.contains("ID")) {
				parse += " 27";
				lexema2 = "";
				lexema2 = reservadas2.get(obtenerPos(sig_token)).toString();
				if (reservadas3.containsKey(lexema2)) {
					tipos.add(comprobarTipo(obtenerPos(sig_token)));

				}
				equipara(sig_token);
				R();
			}

			else if (sig_token.equals("<CodPut,>")) {
				parse += " 28";
				equipara("<CodPut,>");
				esPut = true;
				E();
				esPut = false;
				equipara("<PuntoyComa,>");
				tipos.clear();
			}

			else if (sig_token.equals("<CodGet,>")) {
				parse += " 29";
				equipara("<CodGet,>");
				equipara(sig_token);
				equipara("<PuntoyComa,>");
			}

			else if (sig_token.equals("<CodReturn,>")) {
				parse += " 30";
				equipara("<CodReturn,>");
				X();
				equipara("<PuntoyComa,>");
				
			} else if (sig_token.equals("<LlaveA,>")) {
				parse += " 31";
				equipara("<LlaveA,>");
				C();
				equipara("<LlaveC,>");

			}

			else {
				erroresSintactico(sig_token,contadorTokens,3);
				hayErrorSin = true;
			}
		}

	}

	public static void R() {

		if (!hayErrorSin) {
			if (sig_token.equals("<Asignacion,>")) {
				parse += " 32";
				equipara("<Asignacion,>");
				esAsignacion = true;
				tipos.add(comprobarTipo(obtenerPos(sig_token)));
				E();
				if (OPLogCorrecta(tipos)) {
					System.out.println("Asignacion correcta");
				}
				else if(tipos.size() == 1 && tipos.get(0).equals("entero")) {
					System.out.println("Entero global inicializado");
				} else {
					erroresSemantico(sig_token,contadorTokens,2);
					//agregarErrorAGestorErroresSeman("Asignacion no valida");
				}
				if (soloEnterosYCadenas) {
					if (tipos.contains("logico")) {
						erroresSemantico(sig_token,contadorTokens,3);
						//agregarErrorAGestorErroresSeman("La operaciones aritmeticas no aceptan logicos");
					} else {
						System.out.println("Asignacion y operaciones correctas");
					}
					soloEnterosYCadenas = false;
				}
				esAsignacion = false;
				equipara("<PuntoyComa,>");
				tipos.clear();
			}

			else if (sig_token.equals("<ParentesisA,>")) {
				parse += " 33";
				equipara("<ParentesisA,>");
				L();
				equipara("<ParentesisC,>");
				equipara("<PuntoyComa,>");
			} else if (sig_token.equals("<PuntoyComa,>")) {
				parse += " 34";
				equipara("<PuntoyComa,>");
			} else {
				erroresSintactico(sig_token,contadorTokens,4);
				hayErrorSin = true;
			}

		}

	}

	public static void L() {

		if (!hayErrorSin) {
			if (sig_token.equals("<AND,>") || sig_token.equals("<OR,>") || sig_token.equals("<DEC,>")
					|| sig_token.contains("ID") || sig_token.equals("<ParentesisA,>") || sig_token.contains("Entero")
					|| sig_token.contains("Cadena") || sig_token.equals("<CodTrue,>") || sig_token.equals("<CodFalse,>")
					|| sig_token.equals("<CodVoid,>") || sig_token.equals("<Suma,>") || sig_token.equals("<Resta,>")
					|| sig_token.equals("<Mult,>") || sig_token.equals("<Igual,>") || sig_token.equals("<Distinto,>")) {
				parse += " 35";
				E();
				Q();
			} else if (sig_token.equals("<ParentesisC,>")) {
				parse += " 36";

			} else if (sig_token.equals("<EOF,>")) {
				parse += " 36";
			}

			else {
				erroresSintactico(sig_token,contadorTokens,5);
				hayErrorSin = true;
			}
		}

	}

	public static void Q() {

		if (!hayErrorSin) {
			if (sig_token.equals("<Coma,>")) {
				parse += " 37";
				equipara("<Coma,>");
				E();
				Q();
			}

			else if (sig_token.equals("<AND,>") || sig_token.equals("<OR,>") || sig_token.equals("<DEC,>")
					|| sig_token.equals("") || sig_token.contains("ID") || sig_token.equals("<ParentesisA,>")
					|| sig_token.contains("Entero") || sig_token.contains("Cadena") || sig_token.equals("<CodTrue,>")
					|| sig_token.equals("<CodFalse,>") || sig_token.equals("<CodVoid,>") || sig_token.equals("<Suma,>")
					|| sig_token.equals("<Resta,>") || sig_token.equals("<Mult,>") || sig_token.equals("<Igual,>")
					|| sig_token.equals("<Distinto,>")) {

			} else if (sig_token.equals("<ParentesisC,>")) {
				parse += " 38";
			} else if (sig_token.equals("<EOF,>")) {
				parse += " 38";
			}

			else {
				erroresSintactico(sig_token,contadorTokens,5);
				hayErrorSin = true;
			}

		}
	}

	public static void X() {

		if (!hayErrorSin) {
			if (sig_token.equals("<AND,>") || sig_token.equals("<OR,>") || sig_token.equals("<DEC,>")
					|| sig_token.equals("") || sig_token.contains("ID") || sig_token.equals("<ParentesisA,>")
					|| sig_token.contains("Entero") || sig_token.contains("Cadena") || sig_token.equals("<CodTrue,>")
					|| sig_token.equals("<CodFalse,>") || sig_token.equals("<CodVoid,>")) {
				parse += " 39";
				E();
			} else if (sig_token.equals("<PuntoyComa,>")) {
				parse += " 40";
				retorno1 = "void";
			} else if (sig_token.equals("<EOF,>") || sig_token.equals("<LlaveC,>")) {
				parse += " 40";
			} else {
				erroresSintactico(sig_token,contadorTokens,5);
				hayErrorSin = true;
			}
		}
	}

	public static void B() {

		if (!hayErrorSin) {
			soloEnterosYCadenas = false;
			if (sig_token.equals("<CodIf,>")) {
				parse += " 41";
				equipara("<CodIf,>");
				esIF = true;
				equipara("<ParentesisA,>");
				E();
				equipara("<ParentesisC,>");
				esIF = false;
				if (OPLogCorrecta(tipos)) {
					System.out.println("If correcto, se ejecuta lo de dentro");
					tipos.clear();
					S();
				} else {
					
					tipos.clear();
					erroresSemantico(sig_token,contadorTokens,4);
					//agregarErrorAGestorErroresSeman("Tipo IF = tipo_error");
				}
			}

			else if (sig_token.equals("<CodLet,>")) {
				parse += " 42";
				equipara("<CodLet,>");
				if (sig_token.contains("ID")) {
					String lexema1 = reservadas2.get(obtenerPos(sig_token)).toString();
					reservadas3.put(lexema1, 0);
					equipara(sig_token);
					esLet = true;
					T();
					equipara("<PuntoyComa,>");
					esLet = false;
					if (reservadas3.get(lexema1) == 0) {
						if(!esFuncion) {
							agregarAtributosATablaSimbolos(tipo, numParam, null, null, contador, false, false);
							reservadas3.put(lexema1, 1);
							
						}else {
							agregarAtributosATablaSimbolos(tipo, numParam, null, null, contador, true, primerArgFun);
						}
						contador++;
					}
				} else {
					erroresSintactico(sig_token,contadorTokens,6);
				}
			} else if (sig_token.contains("ID") || sig_token.equals("<CodPut,>") || sig_token.equals("<CodGet,>")
					|| sig_token.contains("<CodReturn,>")) {
				parse += " 43";
				S();
				
			}

			else if (sig_token.equals("<CodFor,>")) {
				parse += " 44";
				equipara("<CodFor,>");
				equipara("<ParentesisA,>");
				B();
				E();
				equipara("<PuntoyComa,>");
				P();
				equipara("<ParentesisC,>");
				tipos.clear();
				equipara("<LlaveA,>");
				C();
				equipara("<LlaveC,>");
			}

			else {
				erroresSintactico(sig_token,contadorTokens,7);
				hayErrorSin = true;
			}
		}
	}

	public static void T() {

		if (!hayErrorSin) {
			if (sig_token.equals("<CodInt,>")) {
				parse += " 45";
				tipo = "entero";
				equipara("<CodInt,>");
			}

			else if (sig_token.equals("<CodBoolean,>")) {
				parse += " 46";
				tipo = "logico";
				equipara("<CodBoolean,>");
			}

			else if (sig_token.equals("<CodString,>")) {
				parse += " 47";
				tipo = "cadena";
				equipara("<CodString,>");
			}

			else {
				if(esLet) {
					erroresSintactico(sig_token,contadorTokens,13);
				}
				else {
					erroresSintactico(sig_token,contadorTokens,8);
				}
				hayErrorSin = true;
			}
		}

	}

	public static void F() {

        if (!hayErrorSin) {
            equipara("<CodFunction,>");
            if (sig_token.contains("ID")) {
                numParam = 1;
                parse += " 48";
                tipoParam.clear();
                equipara(sig_token);
                H();
                retorno = tipo;
                equipara("<ParentesisA,>");
                A();
                equipara("<ParentesisC,>");
                if (!tipoParam.contains("void")) {
                    agregarAtributosATablaSimbolos("funcion", numParam, tipoParam.toArray(new String[0]), retorno,
                            contador, false, false);
                    contador++;
                    String[] paramDentroArray = tipoParam.toArray(new String[0]);
                    primerArgFun  = true;
                    for (int i = 0; i < paramDentroArray.length; i++) {
                        agregarAtributosATablaSimbolos(paramDentroArray[i], i, null, null, contador, true, primerArgFun);
                        primerArgFun  = false;
                    }
                }
                equipara("<LlaveA,>");
                esFuncion = true;
                retorno1 = "void";
                tipos.clear();
                C();
                if (retorno.equals(retorno1)) {
                    System.out.println("Funcion correcta");
                } else {
                	erroresSemantico(sig_token,contadorTokens,5);
                	//agregarErrorAGestorErroresSeman("La funcion no devuelve lo que debe");
                }
                equipara("<LlaveC,>");
                tipos.clear();
                esFuncion = false;
                contador +=numParam;
                lexemaFuncion = "";
            }

            else {
            	erroresSintactico(sig_token,contadorTokens,9);
                hayErrorSin = true;
            }
        }

    }


	public static void H() {

		if (!hayErrorSin) {
			if (sig_token.equals("<CodInt,>") || sig_token.equals("<CodString,>")
					|| sig_token.equals("<CodBoolean,>")) {
				parse += " 49";
				T();
			}

			else if (sig_token.equals("<CodVoid,>")) {
				parse += " 50";
				tipo = "void";
				retorno = "void";
				equipara("<CodVoid,>");
			}

			else {
				erroresSintactico(sig_token,contadorTokens,10);
				hayErrorSin = true;
			}
		}
	}

	public static void A() {

		if (!hayErrorSin) {
			if (sig_token.equals("<CodInt,>") || sig_token.equals("<CodString,>")
					|| sig_token.equals("<CodBoolean,>")) {
				parse += " 51";
				primerArgFun = true;
				T();
				tipoParam.add(tipo);
				paramDentro.add(tipo);
				if (sig_token.contains("ID")) {
					primerArgFun = false;
					String lexema1 = reservadas2.get(obtenerPos(sig_token)).toString();
					reservadas3.put(lexema1, 2);
					equipara(sig_token);
					K();
				} else {
					erroresSintactico(sig_token,contadorTokens,6);
					hayErrorSin = true;
				}
			}

			else if (sig_token.equals("<CodVoid,>")) {
				parse += " 52";
				equipara("<CodVoid,>");

				tipoParam.add("void");
				agregarAtributosATablaSimbolos("funcion", 1, tipoParam.toArray(new String[0]), retorno, contador, false, false);
				contador++;
				primerArgFun  = true;
			}

			else {
				erroresSintactico(sig_token,contadorTokens,11);
				hayErrorSin = true;
			}
		}
	}

	public static void K() {

		if (!hayErrorSin) {

			if (sig_token.equals("<Coma,>")) {
				primerArgFun = false;
				numParam++;
				parse += " 53";
				equipara("<Coma,>");
				
				T();
				tipoParam.add(tipo);
				if (sig_token.contains("ID")) {
					equipara(sig_token);
					K();
				} else {
					erroresSintactico(sig_token,contadorTokens,6);
					hayErrorSin = true;
				}
			}

			else if (sig_token.contains("ID")) {
				
			} else if (sig_token.equals("<EOF,>") || sig_token.equals("<ParentesisC,>")) {
				parse += " 54";
			}

			else {
				erroresSintactico(sig_token,contadorTokens,12);
				hayErrorSin = true;
			}
		}

	}

	public static void C() {

		if (!hayErrorSin) {
			if (sig_token.equals("<CodIf,>") || sig_token.equals("<CodLet,>") || sig_token.contains("ID")
					|| sig_token.equals("<CodPut,>") || sig_token.equals("<CodGet,>")
					|| sig_token.equals("<CodReturn,>")) {
				parse += " 55";
				B();
				if(primerArgFun) {
					primerArgFun = false;
				}
				C();
			}

			else if (sig_token.equals("<LlaveC,>")) {
				parse += " 56";
			} else if (sig_token.equals("<EOF,>")) {
				parse += " 56";
			}
		}
	}

	public void setError(boolean valor) {
		hayErrorSin = valor;
	}

	public static void ejecuta() {
		parse = "D";
		P();
	}

	public static void agregarAtributosATablaSimbolos(String tipo, int numParam, String[] tipoParam, String devuelve,
			int contador, boolean local, boolean primerArgFun) {
		try {
			if (contadorGlobal > listaIDs.size()-1) {
				erroresSemantico(sig_token,contadorTokens,6);
				//agregarErrorAGestorErroresSeman("No se puede volver a inicializar un objeto" + " ya inicializado");
			} else {
				FileReader fr = new FileReader(nombreArchivoTablaSimbolosG); //Leo los lexemas de la tabla global
				BufferedReader br = new BufferedReader(fr);
				String[] lineas = new String[1];
				if(!local) {
					if(!tipo.equals("funcion"))
					lineas[0] = "\nTABLA DE SIMBOLOS # " + contadorTabla + " :";
					else {
						lineas[0] = "\nTABLA DE SIMBOLOS # " + contadorTabla + " :";
					}
					agregarLineasAUnArchivo(nombreArchivoTablaSimbolosG, lineas);
					contador++;
				}
				String[] lineascompletas = null;
				lexemas.add(listaIDs.get(contadorGlobal).getLeft());
				String[] lexemasArray = lexemas.toArray(new String[0]);
				switch (tipo) {
				case "funcion":
					lexemaFuncion = lexemas.get(contadorGlobal);
					String[] lineas1 = new String[8 + 2 * numParam];
					lineas1[1] = "lexema : '" + lexemaFuncion + "'";
					lineas1[2] = "+ tipo: funcion ";
					lineas1[3] = "  + numParam: " + numParam;
					int i;
					for (i = 0; i < numParam; i++) {
						lineas1[((2 * i) + 4)] = "  + TipoParam" + (i + 1) + ": '" + tipoParam[i] + "'";
						lineas1[((2 * i) + 5)] = "    + ModoParam" + (i + 1) + ": 'por valor'";
					}
					lineas1[((2 * i) + 4)] = "  + TipoRetorno: '" + devuelve + "'";
					
					lineas1[((2 * i) + 5)] = "  + EtiqFuncion: 'Et" + lexemasArray[contadorGlobal] + "01'";

					lineascompletas = lineas1;
					agregarLineasAUnArchivo(nombreArchivoTablaSimbolosG, lineascompletas);
					contadorTablaF = contadorTabla + 1;
					if(numParam == 1) {
						contadorTabla = contadorTabla - numParam;
					}else {
						contadorTabla = contadorTabla - numParam-1;
					}
					break;
				case "entero":
					if(!local) {
						String[] lineas2 = new String[3];
						lineas2[0] = "lexema : '" + lexemas.get(contadorGlobal) + "'";
						lineas2[1] = "+ tipo: entero ";
						lineas2[2] = "+ desp: " + desplazamiento+"\n";
						desplazamiento++;
						lineascompletas = lineas2;
						agregarLineasAUnArchivo(nombreArchivoTablaSimbolosG, lineascompletas);
					}
					else {
						if(primerArgFun) {
							String[] lineas2 = new String[4];
							lineas2[0] = "--------------------------------------------------------\n"
									+ "Tabla de la FUNCION "+lexemaFuncion+" #"+contadorTablaF+":";
							lineas2[1] = "lexema : '" +  lexemas.get(contadorGlobal) + "'";
							lineas2[2] = "+ tipo: entero ";
							lineas2[3] = "+ desp: " + desplazamiento+"\n";
							desplazamiento++;
							lineascompletas = lineas2;
							agregarLineasAUnArchivo(nombreArchivoTablaSimbolosL, lineascompletas);
						}
						else {
							String[] lineas2 = new String[3];
							lineas2[0] = "lexema : '" +  lexemas.get(contadorGlobal) + "'";
							lineas2[1] = "+ tipo: entero ";
							lineas2[2] = "+ desp: " + desplazamiento+"\n";
							desplazamiento++;
							lineascompletas = lineas2;
							agregarLineasAUnArchivo(nombreArchivoTablaSimbolosL, lineascompletas);
						}
					}
					break;
				case "cadena":
					if(!local) {
						String[] lineas3 = new String[3];
						lineas3[0] = "lexema : '" +  lexemas.get(contadorGlobal) + "'";
						lineas3[1] = "+ tipo: cadena ";
						lineas3[2] = "+ desp: " + desplazamiento+"\n";
						desplazamiento = desplazamiento + 2;
						lineascompletas = lineas3;
						agregarLineasAUnArchivo(nombreArchivoTablaSimbolosG, lineascompletas);
					}
					else {
						if(primerArgFun) {
							String[] lineas3 = new String[4];
							lineas3[0] = "--------------------------------------------------------\n"
									+ "Tabla de la FUNCION "+lexemaFuncion+" #"+contadorTablaF+":";
							contador++;
							lineas3[1] = "lexema : '" +  lexemas.get(contadorGlobal) + "'";
							lineas3[2] = "+ tipo: cadena ";
							lineas3[3] = "+ desp: " + desplazamiento+"\n";
							desplazamiento = desplazamiento + 2;
							lineascompletas = lineas3;
							agregarLineasAUnArchivo(nombreArchivoTablaSimbolosL, lineascompletas);
						}
						else {
							String[] lineas3 = new String[3];
							lineas3[0] = "lexema : '" +  lexemas.get(contadorGlobal) + "'";
							lineas3[1] = "+ tipo: cadena ";
							lineas3[2] = "+ desp: " + desplazamiento+"\n";
							desplazamiento = desplazamiento + 2;
							lineascompletas = lineas3;
							agregarLineasAUnArchivo(nombreArchivoTablaSimbolosL, lineascompletas);
						}
					}
					break;
				case "logico":
					if(!local) {
						String[] lineas4 = new String[3];
						lineas4[0] = "lexema : '" +  lexemas.get(contadorGlobal) + "'";
						lineas4[1] = "+ tipo: logico ";
						lineas4[2] = "+ desp: " + desplazamiento+"\n";
						desplazamiento = desplazamiento + 3;
						lineascompletas = lineas4;
						agregarLineasAUnArchivo(nombreArchivoTablaSimbolosG, lineascompletas);
					}
					else {
						if(primerArgFun) {
							String[] lineas4 = new String[4];
							contador++;
							lineas4[0] = "Tabla de la FUNCION "+lexemaFuncion+" #"+contadorTablaF+":";
							contador++;
							lineas4[1] = "lexema : '" +  lexemas.get(contadorGlobal) + "'";
							lineas4[2] = "+ tipo: logico ";
							lineas4[3] = "+ desp: " + desplazamiento+"\n";
							desplazamiento = desplazamiento + 3;
							lineascompletas = lineas4;
							agregarLineasAUnArchivo(nombreArchivoTablaSimbolosL, lineascompletas);
						}
						else {
							String[] lineas4 = new String[3];
							lineas4[0] = "lexema : '" +  lexemas.get(contadorGlobal) + "'";
							lineas4[1] = "+ tipo: logico ";
							lineas4[2] = "+ desp: " + desplazamiento+"\n";
							desplazamiento = desplazamiento + 3;
							lineascompletas = lineas4;
							agregarLineasAUnArchivo(nombreArchivoTablaSimbolosL, lineascompletas);
						}
					}
					break;
				}
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		contadorGlobal++;//Ya se recogio ese ID, luego se pasara al siguiente
		contadorTabla++;
	}

	public static void agregarErrorAGestorErrores(String mensaje) {
		String[] erroresGenericos = new String[1];
		erroresGenericos[0] = "Error sintactico -> ";
		erroresGenericos[0] += mensaje + " en el token generado en la linea del codigo" + linea;
		agregarLineasAUnArchivo(nombreArchivoGestorErrores, erroresGenericos);

	}

	public static void agregarErrorAGestorErroresSeman(String mensaje) {
		String[] erroresGenericos = new String[1];
		erroresGenericos[0] = "Error semantico -> ";
		erroresGenericos[0] += mensaje + " en el token generado en la linea del codigo " + linea;
		agregarLineasAUnArchivo(nombreArchivoGestorErrores, erroresGenericos);
	}

	@SuppressWarnings("resource")
	public static String comprobarTipo(int idpos) {
		String type = "void";
		try {
			if (idpos != -1) {
				//Para las variables globales
				File file = new File(nombreArchivoTablaSimbolosG); // Tu archivo de entrada
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				boolean foundLexema = false;
				String lexema = reservadas2.get(idpos).toString();
				while ((line = reader.readLine()) != null) {
					if (line.contains("lexema : '" + lexema + "'")) {
						foundLexema = true;
					} else if (foundLexema && line.contains("+ tipo: ")) {
						type = line.split("\\+ tipo: ")[1].trim();
						if (type.equals("funcion")) {
							continue; // Contin�a leyendo las siguientes l�neas
						} else {
							return type;
						}
					}
					if (type != null && type.equals("funcion") && line.contains("+ TipoRetorno: '")) {
						type = line.split("\\+ TipoRetorno: '")[1].trim();
						if (type.endsWith("'")) {
							return type.substring(0, type.length() - 1); // Esto eliminar� el �ltimo car�cter
						}
						break; // Sal del bucle una vez que encuentres el TipoRetorno
					}
				}
				reader.close();
				//Para las variables locales
				File fileL = new File(nombreArchivoTablaSimbolosL); // Tu archivo de entrada
				BufferedReader readerL = new BufferedReader(new FileReader(fileL));
				String lineL;
				boolean foundLexemaL = false;
				String lexemaL = reservadas2.get(idpos).toString();
				while ((lineL = readerL.readLine()) != null) {
					if (lineL.contains("lexema : '" + lexemaL + "'")) {
						foundLexemaL = true;
					} else if (foundLexemaL && lineL.contains("+ tipo: ")) {
						return lineL.split("\\+ tipo: ")[1].trim();
					
					}
				}
				reader.close();
			} else {
				if (sig_token.equals("<CodTrue,>") || sig_token.equals("<CodFalse,>")) {
					type = "logico";
				} else if (sig_token.contains("Entero")) {
					type = "entero";
				} else {
					type = "cadena";
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return type;

	}

	public static int obtenerPos(String line) {
		if (line.contains("ID")) {
			String[] partes = line.split(","); // Divide la cadena en dos partes
			String parte2 = partes[1]; // Toma la segunda parte
			String posStr = parte2.substring(0, parte2.length() - 1); // Elimina el ltimo car cter '>'
			int pos = Integer.parseInt(posStr);
			return pos;
		} 
		else {
			return -1;
		}
	}
	//Funcion para que no se meta un ID dos veces en la TS
	@SuppressWarnings("resource")
	public static boolean yaEnTS(String lexema) {
		try {
			File file = new File(nombreArchivoTablaSimbolosG); // Tu archivo de entrada
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("lexema : '" + lexema + "'")) {
					return true;
				}
			}
			reader.close();
			//Para las variables locales
			File fileL = new File(nombreArchivoTablaSimbolosL); // Tu archivo de entrada
			BufferedReader readerL = new BufferedReader(new FileReader(fileL));
			String lineL;
			while ((lineL = readerL.readLine()) != null) {
				if (lineL.contains("lexema : '" + lexema + "'")) {
					return true;
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean OPLogCorrecta(List<String> tipos) {
		if (tipos.size() > 2) {
			for (int i = 0; i < tipos.size() - 1; i++) {
				if (!tipos.get(i).equals(tipos.get(i + 1))) {
					return false;
				}
			}
		} else if (tipos.size() == 1) {
			if (tipos.get(0).equals("logico")) {
				return true;
			} else {
				return false;
			}
		}
		return tipos.get(0).equals(tipos.get(1));
	}

	public static void main(String[] args) {
		try (FileWriter escritorTokens = new FileWriter(nombreArchivoTokens, false)) {
			// Escribe una cadena vac a en el archivo para borrar su contenido
			escritorTokens.write("");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (FileWriter escritorSimbolos = new FileWriter(nombreArchivoTablaSimbolosG)) {
			// Escribe una cadena vac a en el archivo para borrar su contenido
			escritorSimbolos.write("");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (FileWriter escritorSimbolos = new FileWriter(nombreArchivoTablaSimbolosL)) {
			// Escribe una cadena vac a en el archivo para borrar su contenido
			escritorSimbolos.write("");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (FileWriter escritorErrores = new FileWriter(nombreArchivoGestorErrores)) {
			// Escribe una cadena vac a en el archivo para borrar su contenido
			escritorErrores.write("");
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		File archivo = new File(nombreArchivoLectura);
		if (archivo.exists()) {
			lector();
			erroresLexico();
			agregarIdentificadoresATablaSimbolos();
			try {
				File archivo1 = new File(nombreArchivoTablaSimbolosG);
				// Primero, contamos las l neas
				BufferedReader lineCounter = new BufferedReader(new FileReader(archivo1));

				while (lineCounter.readLine() != null) {
					lineNumber++;
				}
				lineCounter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				reader = new BufferedReader(new FileReader(nombreArchivoTokens));
				hayErrorSin = false;
				sig_token = getSig_token();
			}

			catch (IOException e) {
				e.printStackTrace();
			}
			ejecuta();
			if (!hayErrorSin) {
				System.out.println(parse);
				 try {
			            FileWriter fileWriter = new FileWriter(nombreArchivoParse);
			            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			            bufferedWriter.write(parse);
			            bufferedWriter.close();
			            System.out.println("Se ha escrito el parse con exito!");
			        } catch (IOException e) {
			            System.out.println("Ha ocurrido un error.");
			            e.printStackTrace();
			        }
			}
		} else {
			error(10, 0);
		}
	}
}