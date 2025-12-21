package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.client.DirectoryConnector;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	public static final String RESPONSE_MESSAGE = "welcome";
	public static final String DENIED_MESSAGE = "denied";
	public static final String INVALID_MESSAGE = "invalid";
	
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	
	public static Map<InetSocketAddress, ArrayList<FileInfo>> addrMapFiles= new HashMap<InetSocketAddress, ArrayList<FileInfo>>();
	
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar el atributo socket: Crear un socket
		 * UDP ligado al puerto especificado por el argumento directoryPort en la
		 * máquina local,
		 */
		socket = new DatagramSocket(DIRECTORY_PORT);

		/*
		 * TODO: (Boletín SocketsUDP) Inicializar atributos que mantienen el estado del
		 * servidor de directorio: ficheros, etc.)
		 */
		if (NanoFiles.testModeUDP) {
			if (socket == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public DatagramPacket receiveDatagram() throws IOException {
		DatagramPacket datagramReceivedFromClient = null;
		boolean datagramReceived = false;
		while (!datagramReceived) {
			/*
			 * TODO: (Boletín SocketsUDP) Crear un búfer para recibir datagramas y un
			 * datagrama asociado al búfer (datagramReceivedFromClient)
			 */
			byte[] recvBuf = new byte[DirMessage.PACKET_MAX_SIZE];
			datagramReceivedFromClient = new DatagramPacket(recvBuf, recvBuf.length);

			/*
			 * TODO: (Boletín SocketsUDP) Recibimos a través del socket un datagrama
			 */
			socket.receive(datagramReceivedFromClient);
			
			// Vemos si el mensaje debe ser ignorado (simulación de un canal no confiable)
			double rand = Math.random();
			if (rand < messageDiscardProbability) {
				System.err.println(
						"Directory ignored datagram from " + datagramReceivedFromClient.getSocketAddress());
			} else {
				datagramReceived = true;
				System.out
						.println("Directory received datagram from " + datagramReceivedFromClient.getSocketAddress()
								+ " of size " + datagramReceivedFromClient.getLength() + " bytes.");
			}

		}

		return datagramReceivedFromClient;
	}

	public void runTest() throws IOException {
		System.out.println("[testMode] Directory starting...");

		System.out.println("[testMode] Attempting to receive 'ping' message...");
		try {
			DatagramPacket rcvDatagram = receiveDatagram();
			sendResponseTestMode(rcvDatagram);
			
			System.out.println("[testMode] Attempting to receive 'ping&PROTOCOL_ID' message...");
			rcvDatagram = receiveDatagram();
			sendResponseTestMode(rcvDatagram);
		}catch (SocketTimeoutException e) {
            System.err.println("Timeout waiting for message.");
        }

		
	}

	private void sendResponseTestMode(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín SocketsUDP) Construir un String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración.
		 */

		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: " + messageFromClient);
		/*
		 * TODO: (Boletín SocketsUDP) Después, usar la cadena para comprobar que su
		 * valor es "ping"; en ese caso, enviar como respuesta un datagrama con la
		 * cadena "pingok". Si el mensaje recibido no es "ping", se informa del error y
		 * se envía "invalid" como respuesta.
		 */
		 /*
		 * TODO: (Boletín Estructura-NanoFiles) Ampliar el código para que, en el caso
		 * de que la cadena recibida no sea exactamente "ping", comprobar si comienza
		 * por "ping&" (es del tipo "ping&PROTOCOL_ID", donde PROTOCOL_ID será el
		 * identificador del protocolo diseñado por el grupo de prácticas (ver
		 * NanoFiles.PROTOCOL_ID). Se debe extraer el "protocol_id" de la cadena
		 * recibida y comprobar que su valor coincide con el de NanoFiles.PROTOCOL_ID,
		 * en cuyo caso se responderá con "welcome" (en otro caso, "denied").
		 */
		InetSocketAddress clientAddr = (InetSocketAddress) pkt.getSocketAddress();
		
		String messageToClient;
		if(messageFromClient.startsWith(DirectoryConnector.MESSAGE_TO_SEND)) {
			if(messageFromClient.startsWith(DirectoryConnector.MESSAGE_TO_SEND+"&")) {
				String protocol_id = messageFromClient.split("&")[1];
				if(protocol_id.equals(NanoFiles.PROTOCOL_ID)) {
					messageToClient = RESPONSE_MESSAGE;
				}else {
					messageToClient = DENIED_MESSAGE;
				}
			}else {
				messageToClient = new String(messageFromClient+"ok");
			}
		}else {
			System.err.println("The received message is not \"" + DirectoryConnector.MESSAGE_TO_SEND + "\".");
			messageToClient = INVALID_MESSAGE;
		}
		byte[] dataToClient = messageToClient.getBytes();
		System.out.println("Sending response datagram with message \"" + messageToClient + "\"");
		System.out.println("Destination is client at addr: " + clientAddr);
		DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
		socket.send(packetToClient);
		System.out.println("Datagram sent to Client correctly.");
		
	}

	public void run() throws IOException {

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			DatagramPacket rcvDatagram = receiveDatagram();

			sendResponse(rcvDatagram);

		}
	}

	private void sendResponse(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín MensajesASCII) Construir String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración. Después, usar la cadena para construir un objeto
		 * DirMessage que contenga en sus atributos los valores del mensaje. A partir de
		 * este objeto, se podrá obtener los valores de los campos del mensaje mediante
		 * métodos "getter" para procesar el mensaje y consultar/modificar el estado del
		 * servidor.
		 */
		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: \n" + messageFromClient);
	    DirMessage receivedMessage = DirMessage.fromString(messageFromClient);

		InetSocketAddress clientAddr = (InetSocketAddress) pkt.getSocketAddress();
		

		/*
		 * TODO: Una vez construido un objeto DirMessage con el contenido del datagrama
		 * recibido, obtener el tipo de operación solicitada por el mensaje y actuar en
		 * consecuencia, enviando uno u otro tipo de mensaje en respuesta.
		 */
		String operation = DirMessageOps.OPERATION_INVALID;
	    if(receivedMessage != null) {
			operation = receivedMessage.getOperation();
	    }
		/*
		 * TODO: (Boletín MensajesASCII) Construir un objeto DirMessage (msgToSend) con
		 * la respuesta a enviar al cliente, en función del tipo de mensaje recibido,
		 * leyendo/modificando según sea necesario el "estado" guardado en el servidor
		 * de directorio (atributos files, etc.). Los atributos del objeto DirMessage
		 * contendrán los valores adecuados para los diferentes campos del mensaje a
		 * enviar como respuesta (operation, etc.)
		 */
	    DirMessage msgToSend = null; // Mensaje de respuesta

		switch (operation) {
			case DirMessageOps.OPERATION_PING:

				/*
				 * TODO: (Boletín MensajesASCII) Comprobamos si el protocolId del mensaje del
				 * cliente coincide con el nuestro.
				 */
				/*
				 * TODO: (Boletín MensajesASCII) Construimos un mensaje de respuesta que indique
				 * el éxito/fracaso del ping (compatible, incompatible), y lo devolvemos como
				 * resultado del método.
				 */
				/*
				 * TODO: (Boletín MensajesASCII) Imprimimos por pantalla el resultado de
				 * procesar la petición recibida (éxito o fracaso) con los datos relevantes, a
				 * modo de depuración en el servidor
				 */
				String clientProtocolId = receivedMessage.getProtocolId();
            
				if (clientProtocolId.equals(NanoFiles.PROTOCOL_ID)) {
					//Protocolo compatible
					msgToSend = new DirMessage(DirMessageOps.OPERATION_SUCCESSFUL);
					System.out.println("Ping successful: Client supports server.\n");
				} else {
					//Protocolo incompatible
					msgToSend = new DirMessage(DirMessageOps.OPERATION_ERROR);
					System.err.println("Ping failed: Client protocol incompatible.\n");
				}
				break;
				
			case DirMessageOps.OPERATION_FILELIST:

				Map<FileInfo, List<InetSocketAddress>> availableFiles = new HashMap<FileInfo, List<InetSocketAddress>>();
				
				for (Map.Entry<InetSocketAddress, ArrayList<FileInfo>> entry : addrMapFiles.entrySet()) {
				    InetSocketAddress address = entry.getKey();
				    List<FileInfo> files = entry.getValue();

				    for (FileInfo file : files) {
				        availableFiles.computeIfAbsent(file, k -> new ArrayList<>()).add(address);
				    }
				}
				
				msgToSend = new DirMessage(DirMessageOps.OPERATION_AVAILABLE_FILES, availableFiles);
				break;
				
			case DirMessageOps.OPERATION_SERVE:
				if(receivedMessage.getFilesToServe() != null) {
					int cont = 0;
					InetSocketAddress serverAddrs = new InetSocketAddress(clientAddr.getAddress(), receivedMessage.getPort());
					for (FileInfo file : receivedMessage.getFilesToServe()) {
						cont++;
						List<FileInfo> files = addrMapFiles.computeIfAbsent(serverAddrs, k -> new ArrayList<>());
						if (!files.contains(file)) { // Evita duplicados
							files.add(file);
						}
					}
					msgToSend = new DirMessage(DirMessageOps.OPERATION_SUCCESSFUL);
					System.out.println("* Client "+ clientAddr + " serving " + cont + " files on address " + serverAddrs);
				}else {
					msgToSend = new DirMessage(DirMessageOps.OPERATION_ERROR);
					System.err.println("There are no files to serve.");
				}
						
				break;
				
			case DirMessageOps.OPERATION_DOWNLOAD:
				Map<InetSocketAddress, List<String>> serversWithFile = new HashMap<InetSocketAddress, List<String>>();
				for(InetSocketAddress address : addrMapFiles.keySet()) {
					FileInfo[] serverFiles = addrMapFiles.get(address).toArray(new FileInfo[0]);
					FileInfo[] matchingFiles = FileInfo.lookupFilenameSubstring(serverFiles, receivedMessage.getFileNameSubString());
					if(matchingFiles != null) {
						serversWithFile.putIfAbsent(address, new ArrayList<String>());
						for(FileInfo file : matchingFiles) {
							serversWithFile.get(address).add(file.fileName);
						}
					}
				}
				msgToSend = new DirMessage(DirMessageOps.OPERATION_SERVERS_SHARING_THIS_FILE,serversWithFile, true);
				break;
				
			case DirMessageOps.OPERATION_UNREGISTER:
				InetSocketAddress serverAddrs = new InetSocketAddress(clientAddr.getAddress(), receivedMessage.getPort());
				addrMapFiles.remove(serverAddrs); 
				msgToSend = new DirMessage(DirMessageOps.OPERATION_SUCCESSFUL);
				System.out.println("Server " + serverAddrs + " unregistered successfully.");
				break;
				
			default:
				System.err.println("Unexpected message operation: \"" + operation + "\"");
				System.exit(-1);
		}

		/*
		 * TODO: (Boletín MensajesASCII) Convertir a String el objeto DirMessage
		 * (msgToSend) con el mensaje de respuesta a enviar, extraer los bytes en que se
		 * codifica el string y finalmente enviarlos en un datagrama
		 */
		if(msgToSend!=null) {
			String messageToClient = msgToSend.toString();
		    byte[] dataToClient = messageToClient.getBytes();
		    
		    System.out.println("Sending response datagram with message: \n" + messageToClient);
			System.out.println("Destination is client at addr: " + clientAddr);
		    DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
			socket.send(packetToClient);
			System.out.println("Datagram sent to Client correctly.");
		}
		
	}
}
