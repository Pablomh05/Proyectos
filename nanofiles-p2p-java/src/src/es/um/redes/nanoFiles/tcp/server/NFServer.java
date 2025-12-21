package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;


public class NFServer implements Runnable {

	private int port; // para puerto efímero

	private ServerSocket serverSocket = null;

	public NFServer(int port) throws IOException {
		/*
		 * TODO: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
	    this.port = port;
	    InetSocketAddress serverSocketAddress = new InetSocketAddress(port);
	    /*
		 * TODO: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
	    serverSocket = new ServerSocket();
	    serverSocket.bind(serverSocketAddress);

	    // actualizar el puerto real en caso de que se haya usado 0 (busca uno libre)
	    this.port = serverSocket.getLocalPort();
	}
	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * @throws IOException 
	 * 
	 */
	public void test() throws IOException {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out
					.println("[fileServerTestMode] NFServer running on " + serverSocket.getLocalSocketAddress() + ".");
		}

		while (true) {
			/*
			 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
			 * otros peers que soliciten descargar ficheros.
			 */
			Socket socket = serverSocket.accept();
			
			System.out.println("[NFServer] Client connected: " + socket.getRemoteSocketAddress());

			// Test básico: recibir y devolver un entero
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

			int received = dis.readInt();
			System.out.println("[NFServer] Received integer: " + received);
			dos.writeInt(received);
			dos.flush();
			System.out.println("[NFServer] Echoed integer back to client.");

			socket.close();
			/*
			 * TODO: (Boletín SocketsTCP) Tras aceptar la conexión con un peer cliente, la
			 * comunicación con dicho cliente para servir los ficheros solicitados se debe
			 * implementar en el método serveFilesToClient, al cual hay que pasarle el
			 * socket devuelto por accept.
			 */
			
			//serveFilesToClient(socket);
		}
	}

	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Al establecerse la conexión con un peer, la
		 * comunicación con dicho cliente se hace en el método
		 * serveFilesToClient(socket), al cual hay que pasarle el socket devuelto por
		 * accept
		 */
		/*
		 * TODO: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
		 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
		 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
		 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
		 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
		 * más de un cliente conectado a este servidor.
		 */
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[NFServer] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out.println("[NFServer] File server running on " + serverSocket.getLocalSocketAddress() + "...");
		}
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("[NFServer] Client connected: " + socket.getRemoteSocketAddress());
				NFServerThread clientThread = new NFServerThread(socket);
				clientThread.start();			
				
			} catch (SocketException e) {
	            // Detecta cierre voluntario del socket con unregisterServer para no imprimir error
	            if (serverSocket.isClosed()) {
	                break;
	            } else {
	                System.err.println("[NFServer] Socket exception: " + e.getMessage());
	            }
			} catch (IOException e) {
	            System.err.println("[NFServer] Error accepting or serving client: " + e.getMessage());
			}
		}
		
	}
	/*
	 * TODO: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */
	public void startInBackground() {
		Thread t = new Thread(this);
		t.start();
	}

	public void stopServer() {
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				System.out.println("[NFServer] Server stopped.");
			}
		} catch (IOException e) {
			System.err.println("[NFServer] Error stopping server: " + e.getMessage());
		}
	}
	
	public int getPort() {
		return port;
	}
	
	
	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 * @throws IOException 
	 */
	public static void serveFilesToClient(Socket socket) throws IOException {
		/*
		 * TODO: (Boletín SocketsTCP) Crear dis/dos a partir del socket
		 */
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		/*
		 * TODO: (Boletín SocketsTCP) Mientras el cliente esté conectado, leer mensajes
		 * de socket, convertirlo a un objeto PeerMessage y luego actuar en función del
		 * tipo de mensaje recibido, enviando los correspondientes mensajes de
		 * respuesta.
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
		 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
		 * compartidos. Los ficheros compartidos se pueden obtener con
		 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
		 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
		 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
		 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
		 * de su hash completo.
		 */
		byte[] currentFileHash = null; // hash del archivo solicitado por el cliente en caso de encontrarlo
		String uploadFilename = null;
		FileOutputStream fos = null;
		File uploaded = null; 
		
		try {
			boolean keepRunning = true;
			while (keepRunning && !socket.isClosed()) {
				PeerMessage request = PeerMessage.readMessageFromInputStream(dis);
				byte opcode = request.getOpcode();

				switch (opcode) {
					case PeerMessageOps.OPCODE_DOWNLOAD_FILE: 
						String filenamePart = request.getFilename();
						FileInfo[] candidates = FileInfo.lookupFilenameSubstring(NanoFiles.db.getFiles(), filenamePart);

						if (candidates.length == 0) {
							new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND).writeMessageToOutputStream(dos);
						} else if (candidates.length > 1) {
							new PeerMessage(PeerMessageOps.OPCODE_AMBIGUOUS_NAME).writeMessageToOutputStream(dos);
						} else {
							FileInfo found = candidates[0];
							currentFileHash = FileDigest.hexStringToByteArray(found.fileHash);
							PeerMessage foundMsg = new PeerMessage(PeerMessageOps.OPCODE_FILE_FOUND, currentFileHash, found.fileSize);
							foundMsg.writeMessageToOutputStream(dos);
						}
						break;

					case PeerMessageOps.OPCODE_GET_CHUNK: 
						String hexHash = FileDigest.getChecksumHexString(currentFileHash);
						String path = NanoFiles.db.lookupFilePath(hexHash);

						try (RandomAccessFile raf = new RandomAccessFile(path, "r")) { //es un try-with-resources, cierra raf cuando termina
							raf.seek(request.getOffset());
							byte[] buffer = new byte[request.getChunkSize()];
							raf.readFully(buffer); 
							PeerMessage chunk = new PeerMessage(PeerMessageOps.OPCODE_GIVE_CHUNK, buffer);
							chunk.writeMessageToOutputStream(dos);
						} catch (IOException e) {
							System.err.println("[NFServer] Error reading chunk at offset: " + request.getOffset());
						}
						break;
						
					case PeerMessageOps.OPCODE_UPLOAD_FILE: 
						FileInfo[] files = NanoFiles.db.getFiles();
						String sharedFolder;
						if (files.length > 0) {
							sharedFolder = new File(NanoFiles.db.lookupFilePath(files[0].fileHash)).getParent();
						} else {
							System.err.println("[NFServer] No files in DB to deduce shared folder path. Files will be in the default folder.");
							sharedFolder = NanoFiles.DEFAULT_SHARED_DIRNAME;
						}
						uploadFilename = request.getFilename();
						uploaded = new File(sharedFolder, "uploaded_" + uploadFilename);

						int i = 1;
						while (uploaded.exists()) {
							uploaded = new File(sharedFolder, "uploaded_" + i + "_" + uploadFilename);
							i++;
						}

						fos = new FileOutputStream(uploaded);
						System.out.println("[NFServer] Starting upload for: " + uploaded.getName());
						uploadFilename = uploaded.getName();
						
						break;
					
					case PeerMessageOps.OPCODE_GIVE_CHUNK: 
						if(fos != null) {
							fos.write(request.getChunkData());
						}
						break;
						
					case PeerMessageOps.OPCODE_CLOSE_CONNECTION:
						System.out.println("[NFServer] Client requested connection close.");
						keepRunning = false;
						break;
						
					default:
						System.err.println("[NFServer] Unknown opcode received: " + opcode);
				}
			}
		} catch (IOException e) {
			System.err.println("[NFServer] Connection error: " + e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
					System.out.println("[NFServer] Upload completed: " + uploadFilename);
					if (uploaded != null && uploaded.exists()) {
						String hash = FileDigest.computeFileChecksumString(uploaded.getAbsolutePath());
						long size = uploaded.length();
						String path = uploaded.getAbsolutePath();

						FileInfo fileInfo = new FileInfo(hash, uploadFilename, size, path);
						NanoFiles.db.addFile(fileInfo);
					}
				} catch (IOException e) {
					System.err.println("[NFServer] Failed to close upload file: " + e.getMessage());
				}
			}
			socket.close();		
		}
	}

	
}




