package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * TODO: (Boletín MensajesASCII) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con el
	 * directorio (valores posibles del campo "operation").
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	
	public static final String OPERATION_PING = "ping";
	public static final String OPERATION_SERVE = "serve";
	public static final String OPERATION_SUCCESSFUL = "successful";
	public static final String OPERATION_ERROR = "error"; 
	
	public static final String OPERATION_FILELIST = "filelist";
	public static final String OPERATION_AVAILABLE_FILES = "availableFiles";
	
	public static final String OPERATION_DOWNLOAD = "download";
	public static final String OPERATION_SERVERS_SHARING_THIS_FILE = "serversSharingThisFile";
	
	public static final String OPERATION_UNREGISTER = "unregister";

}
