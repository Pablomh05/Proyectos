package es.um.redes.nanoFiles.udp.message;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: (Boletín MensajesASCII) Definir de manera simbólica los nombres de
	 * todos los campos que pueden aparecer en los mensajes de este protocolo
	 * (formato campo:valor)
	 */
	private static final String FIELDNAME_PROTOCOL_ID = "protocol"; // ID de protocolo (ping)
	private static final String FIELDNAME_FILENAME = "filename"; // Nombre del archivo (filelist, serve, download)
	private static final String FIELDNAME_SIZE = "size"; // Tamaño del archivo (filelist, serve)
	private static final String FIELDNAME_HASH = "hash"; // hash del archivo (filelist, serve)
	private static final String FIELDNAME_SERVERS = "servers"; //servidores (avilableFiles)
	private static final String FIELDNAME_PORT = "port"; //Puerto (serve, unregister)
	private static final String FIELDNAME_SERVER = "server"; //servidor (serversSharingThisFile)
	
	
	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/**
	 * Identificador de protocolo usado, para comprobar compatibilidad del directorio.
	 */
	private String protocolId; //este es para ping
	/*
	 * TODO: (Boletín MensajesASCII) Crear un atributo correspondiente a cada uno de
	 * los campos de los diferentes mensajes de este protocolo.
	 */
	/**
	 * Este es para availableFiles. Es un mapa donde para cada fichero que el servidor dice que hay disponible, 
	 * se le asocia las direcciones de los servidores que lo tienen.
	 */
	private Map<FileInfo, List<InetSocketAddress>> availableFiles = new HashMap<FileInfo, List<InetSocketAddress>>();
	
	/**
	 * Este es para serve. Puerto desde el cual escucha el servidor.
	 * También sirve para pasar el puerto para dar de baja un servidor de ficheros.
	 */
	private int port; 
	
	/**
	 * Este es para serve. Lista de ficheros que el sevidor quiere compartir
	 */
	private List<FileInfo> filesToServe = new ArrayList<FileInfo>();
	
	/**
	 * Este es para download. Guarda una subcadena del nombre del fichero que se quiere descargar.
	 */
	private String fileNameSubString; 
	
	/**
	 * Este es para serversSharingThisFile. Guarda los ficheros cuyo nombre contiene la subcadena del nombre
	 * del archivo que se quiere descargar con la correspodiente dirección del servidor.
	 */
	private Map<InetSocketAddress, List<String>> serversWithFile = new HashMap<InetSocketAddress, List<String>>();
	
	
	/*
	 * TODO: (Boletín MensajesASCII) Crear diferentes constructores adecuados para
	 * construir mensajes de diferentes tipos con sus correspondientes argumentos
	 * (campos del mensaje)
	 */

	//Constructor para mensajes successful, error, filelist
	public DirMessage(String op) {
		this.operation = op;
	}

	// Constructor para mensajes ping
	public DirMessage(String op, String protocolId) {
		this.operation = op;
        this.protocolId = protocolId;
    }
	//Constructor para availablesFiles
	public DirMessage(String op, Map<FileInfo,List<InetSocketAddress>> files) {
		this.operation = op;
		this.availableFiles = new HashMap<FileInfo,List<InetSocketAddress>>(files);
	}
	
	//Constructor para mensajes serve
	public DirMessage(String op, int port, List<FileInfo> files) {
		this.operation = op;
		this.port = port;
	    this.filesToServe = new ArrayList<FileInfo>(files);
	}
	
	//Constructor para download
	public DirMessage(String op, String subString, boolean isDownload) { //la variable booleana para diferenciar del segundo constructor
		if(isDownload) {
			this.operation = op;
			this.fileNameSubString = subString;
		}
	}
	
	//Constructor para serversSharingThisFile
	public DirMessage(String op, Map<InetSocketAddress,List<String>> serversWithFile, boolean isResponseDownload) {
		if(isResponseDownload){
			this.operation = op;
			this.serversWithFile = new HashMap<InetSocketAddress, List<String>>(serversWithFile);
		}
	}
	
	// Constructor para unregister
	public DirMessage(String op, int port) {
		this.operation = op;
		this.port = port;
	}

	/*
	 * TODO: (Boletín MensajesASCII) Crear métodos getter y setter para obtener los
	 * valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public String getProtocolId() {
		return protocolId;
	}
	public void setProtocolID(String protocolID) {
		if (!operation.equals(DirMessageOps.OPERATION_PING)) {
			throw new RuntimeException(
					"DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
		}
		this.protocolId = protocolID;
	}

	public Map<FileInfo,List<InetSocketAddress>> getAvailableFiles() {
		return new HashMap<FileInfo,List<InetSocketAddress>>(availableFiles);
	}
	public void setAvailableFiles(Map<FileInfo,List<InetSocketAddress>> availableFiles) {
		if (!operation.equals(DirMessageOps.OPERATION_AVAILABLE_FILES)) {
			throw new RuntimeException(
					"DirMessage: setAvailablesFiles called for message of unexpected type (" + operation + ")");
		}
		this.availableFiles = new HashMap<FileInfo,List<InetSocketAddress>>(availableFiles);
	}

    public int getPort() {
    	return port;
    }
    public void setPort(int port) {
    	 if (!operation.equals(DirMessageOps.OPERATION_SERVE) && !operation.equals(DirMessageOps.OPERATION_UNREGISTER)) {
             throw new RuntimeException(
             		"DirMessage: setPort called for message of unexpected type (" + operation + ")");
         }
    	 this.port = port;
    }
    
    public List<FileInfo> getFilesToServe() {
		return new ArrayList<FileInfo>(filesToServe);
	}
	public void setFilesToServe(List<FileInfo> filesToServe) {
		if (!operation.equals(DirMessageOps.OPERATION_SERVE)) {
			throw new RuntimeException(
					"DirMessage: setFilesToServe called for message of unexpected type (" + operation + ")");
		}
		this.filesToServe = new ArrayList<FileInfo>(filesToServe);
	}

    public String getFileNameSubString() {
    	return fileNameSubString;
    }
    public void setFileNameSubString(String fileNameSubString) {
    	if (!operation.equals(DirMessageOps.OPERATION_DOWNLOAD)) {
            throw new RuntimeException(
            		"DirMessage: setFileNameSubString called for message of unexpected type (" + operation + ")");
        }
    	this.fileNameSubString = fileNameSubString;
    }

	public Map<InetSocketAddress, List<String>> getServersWithFile() {
		return new HashMap<InetSocketAddress, List<String>>(serversWithFile);
	}
	public void setServersWithFile(Map<InetSocketAddress, ArrayList<String>> serversWithFile) {
		if (!operation.equals(DirMessageOps.OPERATION_SERVERS_SHARING_THIS_FILE)) {
			throw new RuntimeException(
					"DirMessage: setServersWithFile called for message of unexpected type (" + operation + ")");
		}
		this.serversWithFile = new HashMap<InetSocketAddress, List<String>>(serversWithFile);
	}

	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
		 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
		 * delimitador DELIMITER, y guardarlo en variables locales.
		 */

		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null; 	   
		String tempFileName = null;
		Long tempFileSize = null;
		String tempFileHash = null;

		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
				case FIELDNAME_OPERATION: {
					assert (m == null);
					m = new DirMessage(value);
					break;
				} 
				case FIELDNAME_PROTOCOL_ID:
					if (m != null && m.getOperation().equals(DirMessageOps.OPERATION_PING)) {
						m.setProtocolID(value);
					}
					break;
				case FIELDNAME_FILENAME:
	                if (m != null) {
	                	if(m.getOperation().equals(DirMessageOps.OPERATION_AVAILABLE_FILES) || m.getOperation().equals(DirMessageOps.OPERATION_SERVE)
	                			|| m.getOperation().equals(DirMessageOps.OPERATION_SERVERS_SHARING_THIS_FILE)) {
	                		tempFileName = value;
	                	}
	                	else if(m.getOperation().equals(DirMessageOps.OPERATION_DOWNLOAD)) {
	                		m.setFileNameSubString(value);
	                	}
	                }
	                break;
	            case FIELDNAME_SIZE:
	                if (m != null) {
	                	if(m.getOperation().equals(DirMessageOps.OPERATION_AVAILABLE_FILES) || m.getOperation().equals(DirMessageOps.OPERATION_SERVE)) {
	                		if(tempFileName !=  null) {
	                			tempFileSize = Long.valueOf(value);
	                		}
	                	}
	                }
	                break;
	            case FIELDNAME_HASH:
	                if (m != null) {
	                	if(tempFileName != null && tempFileSize != null) {
	                		if(m.getOperation().equals(DirMessageOps.OPERATION_AVAILABLE_FILES)){
	                			tempFileHash = value;
	                		}
	                		else if(m.getOperation().equals(DirMessageOps.OPERATION_SERVE)) {
	                			FileInfo file = new FileInfo(value, tempFileName, tempFileSize, "");
		                		m.filesToServe.add(file);
		                		tempFileName = null;
		                		tempFileSize = null;
	                		}
	                	}
	                }
	                break;
	            case FIELDNAME_PORT:
	                if (m != null && (m.getOperation().equals(DirMessageOps.OPERATION_SERVE) || m.getOperation().equals(DirMessageOps.OPERATION_UNREGISTER))) {
	                	m.setPort(Integer.parseInt(value));
	                }
	                break;
	            case FIELDNAME_SERVERS:
	            	if (m != null && m.getOperation().equals(DirMessageOps.OPERATION_AVAILABLE_FILES)) {
	            		if(tempFileName != null && tempFileSize != null && tempFileHash != null) {
	            			FileInfo file = new FileInfo(tempFileHash, tempFileName, tempFileSize, "");
	            			
	            			List<InetSocketAddress> addrses = new ArrayList<InetSocketAddress>();
	            			
	            			String[] stringAddrses = value.split(",");
	            			for (String address : stringAddrses) {
	            	            // Separar IP y puerto por ":"
	            	            String[] parts = address.split(":");
	            	            String ip = parts[0].trim().replace("/", "");
            	                int port = Integer.parseInt(parts[1].trim());
            	                addrses.add(new InetSocketAddress(ip, port));
	            			}
	            			m.availableFiles.putIfAbsent(file, new ArrayList<InetSocketAddress>());
	            			for(InetSocketAddress addr : addrses) {
	            				if(!m.availableFiles.get(file).contains(addr)) {
	            					m.availableFiles.get(file).add(addr);
	            				}
	            			}
	            			tempFileName = null;
	                		tempFileSize = null;
	                		tempFileHash = null;
	            		}
	            	}
	            	break;
	            case FIELDNAME_SERVER:
	            	if (m != null && m.getOperation().equals(DirMessageOps.OPERATION_SERVERS_SHARING_THIS_FILE)) {
	            		if(tempFileName != null) {
	            			String[] parts = value.split(":");
	            			String ip = parts[0].trim().replace("/", "");
	            			int port = Integer.parseInt(parts[1].trim());
	            			InetSocketAddress address = new InetSocketAddress(ip, port);
	            			m.serversWithFile.putIfAbsent(address, new ArrayList<String>());
	            			m.serversWithFile.get(address).add(tempFileName);
	            			tempFileName = null;
	            		}
	                }
					break;
				default:
					System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
					System.err.println("Message was:\n" + message);
					System.exit(-1);
				}	
		}
		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: (Boletín MensajesASCII) En función de la operación del mensaje, crear
		 * una cadena la operación y concatenar el resto de campos necesarios usando los
		 * valores de los atributos del objeto.
		 */
		switch (operation) {
        	case DirMessageOps.OPERATION_PING:
        		sb.append(FIELDNAME_PROTOCOL_ID + DELIMITER + protocolId + END_LINE);
        		break;
        	case DirMessageOps.OPERATION_SUCCESSFUL:
		    case DirMessageOps.OPERATION_ERROR:
        	case DirMessageOps.OPERATION_FILELIST:
                break;
        	case DirMessageOps.OPERATION_UNREGISTER:
                sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
                break;
        	case DirMessageOps.OPERATION_AVAILABLE_FILES:
        		if (availableFiles != null) {
        			// Agrupar por hash
        			Map<String, FileInfo> hashMapFileInfo = new HashMap<String, FileInfo>();
        			Map<String, Set<String>> hashMapAddrss = new HashMap<String, Set<String>>();

        			for (Map.Entry<FileInfo, List<InetSocketAddress>> entry : availableFiles.entrySet()) {
        				FileInfo file = entry.getKey();
        				String hash = file.fileHash;

        				// Solo guardar un FileInfo por hash 
        				hashMapFileInfo.putIfAbsent(hash, file);

        				// Añadir servidores sin repeticiones para cada hash
        				hashMapAddrss.putIfAbsent(hash, new HashSet<>());
        				for (InetSocketAddress addr : entry.getValue()) {
        					String ipPort = addr.getAddress().getHostAddress() + ":" + addr.getPort();
        					hashMapAddrss.get(hash).add(ipPort);
        				}
        			}

        			for (String hash : hashMapFileInfo.keySet()) {
        				FileInfo file = hashMapFileInfo.get(hash);
        				Set<String> addrs = hashMapAddrss.get(hash);

        				sb.append(FIELDNAME_FILENAME + DELIMITER + file.fileName + END_LINE);
        				sb.append(FIELDNAME_SIZE + DELIMITER + file.fileSize + END_LINE);
        				sb.append(FIELDNAME_HASH + DELIMITER + file.fileHash + END_LINE);
        				sb.append(FIELDNAME_SERVERS + DELIMITER + String.join(",", addrs) + END_LINE);
        			}
        		}
                break;
        	case DirMessageOps.OPERATION_SERVE:
                sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
        		if (filesToServe != null) {
                    for (FileInfo file : filesToServe) {
                        sb.append(FIELDNAME_FILENAME + DELIMITER + file.fileName + END_LINE);
                        sb.append(FIELDNAME_SIZE + DELIMITER +file.fileSize + END_LINE);
                        sb.append(FIELDNAME_HASH + DELIMITER +file.fileHash + END_LINE);
                    }
                }
        		break;
        		
        	case DirMessageOps.OPERATION_DOWNLOAD:
        		sb.append(FIELDNAME_FILENAME + DELIMITER + fileNameSubString + END_LINE);
        		break;
        		
        	case DirMessageOps.OPERATION_SERVERS_SHARING_THIS_FILE:
        		for(InetSocketAddress address : serversWithFile.keySet()) {
        			for(String name : serversWithFile.get(address)) {
                        sb.append(FIELDNAME_FILENAME + DELIMITER + name + END_LINE);
                        sb.append(FIELDNAME_SERVER + DELIMITER + address + END_LINE);
        			}
        		}
        		break;
		}

		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}

}
