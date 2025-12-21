package es.um.redes.nanoFiles.logic;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.application.NanoFiles;



import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicP2P {
	/*
	 * TODO: Se necesita un atributo NFServer que actuará como servidor de ficheros
	 * de este peer
	 */
	private NFServer fileServer = null;




	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean startFileServer() {
		boolean serverRunning = false;
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		if (fileServer != null) {
			System.err.println("File server is already running");
		} else {
			/*
			 * TODO: (Boletín Servidor TCP concurrente) Arrancar servidor en segundo plano
			 * creando un nuevo hilo, comprobar que el servidor está escuchando en un puerto
			 * válido (>0), imprimir mensaje informando sobre el puerto de escucha, y
			 * devolver verdadero. Las excepciones que puedan lanzarse deben ser capturadas
			 * y tratadas en este método. Si se produce una excepción de entrada/salida
			 * (error del que no es posible recuperarse), se debe informar sin abortar el
			 * programa
			 * 
			 */
			try {
				fileServer = new NFServer(0);
				fileServer.startInBackground(); // lanza hilo concurrente
				if (fileServer.getPort() > 0) {
					System.out.println("* File server started on port " + fileServer.getPort());
					serverRunning = true;
				}
			} catch (IOException e) {
				System.err.println("[NFControllerP2P] Failed to start file server: " + e.getMessage());
			}
			
		}
		return serverRunning;

	}

	protected void testTCPServer() {
		assert (NanoFiles.testModeTCP);
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		assert (fileServer == null);
		try {

			fileServer = new NFServer(0);
			/*
			 * (Boletín SocketsTCP) Inicialmente, se creará un NFServer y se ejecutará su
			 * método "test" (servidor minimalista en primer plano, que sólo puede atender a
			 * un cliente conectado). Posteriormente, se desactivará "testModeTCP" para
			 * implementar un servidor en segundo plano, que se ejecute en un hilo
			 * secundario para permitir que este hilo (principal) siga procesando comandos
			 * introducidos mediante el shell.
			 */
			fileServer.test();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Cannot start the file server");
			fileServer = null;
		}
	}

	public void testTCPClient() {

		assert (NanoFiles.testModeTCP);
		/*
		 * (Boletín SocketsTCP) Inicialmente, se creará un NFConnector (cliente TCP)
		 * para conectarse a un servidor que esté escuchando en la misma máquina y un
		 * puerto fijo. Después, se ejecutará el método "test" para comprobar la
		 * comunicación mediante el socket TCP. Posteriormente, se desactivará
		 * "testModeTCP" para implementar la descarga de un fichero desde múltiples
		 * servidores.
		 */

		NFConnector nfConnector = new NFConnector(new InetSocketAddress(fileServer.getPort()));
		nfConnector.test();
		nfConnector.close();
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList       La lista de direcciones de los servidores a
	 *                                los que se conectará
	 * @param targetFileNameSubstring Subcadena del nombre del fichero a descargar
	 * @param localFileName           Nombre con el que se guardará el fichero
	 *                                descargado
	 */
	protected boolean downloadFileFromServers(InetSocketAddress[] serverAddressList, String targetFileNameSubstring,
			String localFileName) {
		boolean downloaded = false;

		if (serverAddressList.length == 0) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		/*
		 * TODO: Crear un objeto NFConnector distinto para establecer una conexión TCP
		 * con cada servidor de ficheros proporcionado, y usar dicho objeto para
		 * descargar trozos (chunks) del fichero. Se debe comprobar previamente si ya
		 * existe un fichero con el mismo nombre (localFileName) en esta máquina, en
		 * cuyo caso se informa y no se realiza la descarga. Se debe asegurar que el
		 * fichero cuyos datos se solicitan es el mismo para todos los servidores
		 * involucrados (el fichero está identificado por su hash). Una vez descargado,
		 * se debe comprobar la integridad del mismo calculando el hash mediante
		 * FileDigest.computeFileChecksumString. Si todo va bien, imprimir resumen de la
		 * descarga informando de los trozos obtenidos de cada servidor involucrado. Las
		 * excepciones que puedan lanzarse deben ser capturadas y tratadas en este
		 * método. Si se produce una excepción de entrada/salida (error del que no es
		 * posible recuperarse), se debe informar sin abortar el programa
		 */
		FileInfo[] files = NanoFiles.db.getFiles();
		String sharedFolder;
		if (files.length > 0) {
			// Usa cualquier archivo de la base de datos para averiguar el path de la carpeta compartida. 
			sharedFolder = new File(NanoFiles.db.lookupFilePath(files[0].fileHash)).getParent();
		} else {
			System.err.println("[NFServer] No files in DB to deduce shared folder path. Files will be in the default folder.");
			sharedFolder = NanoFiles.DEFAULT_SHARED_DIRNAME;
		}
		
		File finalFile = new File(sharedFolder, localFileName);
		if (finalFile.exists()) {
			System.err.println("* File already exists locally: " + localFileName);
			return false;
		}

		byte[] fileHash = null;
		long fileSize = -1;

		try {
			// Primera conexión a cualquier servidor para obtener el hash y tamaño
			for (InetSocketAddress serverAddr : serverAddressList) {
				try {
					NFConnector connector = new NFConnector(serverAddr);
					PeerMessage downloadRequest = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_FILE, targetFileNameSubstring);
					downloadRequest.writeMessageToOutputStream(connector.getDos());
					PeerMessage response = PeerMessage.readMessageFromInputStream(connector.getDis());
	                PeerMessage closeMsg = new PeerMessage(PeerMessageOps.OPCODE_CLOSE_CONNECTION);
	                closeMsg.writeMessageToOutputStream(connector.getDos());
					connector.close();
					if (response.getOpcode() == PeerMessageOps.OPCODE_FILE_FOUND) {
						fileHash = response.getHash();
						fileSize = response.getFileSize();
						System.out.println("Server at " + serverAddr + " found matching filename with hash: " + FileDigest.getChecksumHexString(fileHash));
					}
				} catch (IOException e) {
					continue;
				}
			}

			if (fileHash == null || fileSize <= 0) {
				System.err.println("Failed to retrieve file info from any server");
				return false;
			}

			RandomAccessFile raf = new RandomAccessFile(finalFile, "rw");
			raf.setLength(fileSize);

			int chunkSize = 16384; // 16KB
	        Map<InetSocketAddress, int[]> downloadSummary = new HashMap<InetSocketAddress, int[]>();

	        long partSize = fileSize / serverAddressList.length;
	        
	        System.out.println("Downloading file " + localFileName + "...");
	        
			for (int i = 0; i < serverAddressList.length; i++) {
	            InetSocketAddress serverAddr = serverAddressList[i];
	            long startOffset = i * partSize;
	            long endOffset = (i == serverAddressList.length - 1) ? fileSize : startOffset + partSize;

	            try {
	                NFConnector connector = new NFConnector(serverAddr);

	                // Confirmar que tiene el mismo archivo
	                PeerMessage downloadRequest = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_FILE, targetFileNameSubstring);
	                downloadRequest.writeMessageToOutputStream(connector.getDos());
	                PeerMessage response = PeerMessage.readMessageFromInputStream(connector.getDis());

	                if (response.getOpcode() != PeerMessageOps.OPCODE_FILE_FOUND ||
	                    !Arrays.equals(fileHash, response.getHash())) {
		                PeerMessage closeMsg = new PeerMessage(PeerMessageOps.OPCODE_CLOSE_CONNECTION);
		                closeMsg.writeMessageToOutputStream(connector.getDos());
	                    connector.close();
	                    continue;
	                }

	                downloadSummary.putIfAbsent(serverAddr, new int[]{0, 0});

	                for (long offset = startOffset; offset < endOffset; offset += chunkSize) {
	                    int sizeToRead = (int) Math.min(chunkSize, endOffset - offset);
	                    PeerMessage getChunk = new PeerMessage(PeerMessageOps.OPCODE_GET_CHUNK, offset, sizeToRead);
	                    getChunk.writeMessageToOutputStream(connector.getDos());

	                    PeerMessage chunkResponse = PeerMessage.readMessageFromInputStream(connector.getDis());
	                    if (chunkResponse.getOpcode() == PeerMessageOps.OPCODE_GIVE_CHUNK) {
	                        byte[] data = chunkResponse.getChunkData();
	                        raf.seek(offset);
	                        raf.write(data);
	                        downloadSummary.get(serverAddr)[0] += data.length;
	                        downloadSummary.get(serverAddr)[1] += 1;
	                    } else {
	                        System.err.println("Unexpected response from " + serverAddr);
	                    }
	                }
	                PeerMessage closeMsg = new PeerMessage(PeerMessageOps.OPCODE_CLOSE_CONNECTION);
	                closeMsg.writeMessageToOutputStream(connector.getDos());
	                connector.close();
	            } catch (IOException e) {
	                System.err.println("Error with server " + serverAddr + ": " + e.getMessage());
	            }
	        }

	        raf.close();
			
			String downloadedHash = FileDigest.computeFileChecksumString(finalFile.getAbsolutePath());
			String expectedHash = FileDigest.getChecksumHexString(fileHash);
			if (!downloadedHash.equals(expectedHash)) {
				System.err.println("File integrity check failed: expected " + expectedHash + ", got " + downloadedHash);
				finalFile.delete();
			} else {
				System.out.println("File integrity check passed: FileDigest.computeFileChecksum returned " + downloadedHash);
				System.out.println("Successfully downloaded remote file to " + finalFile.getAbsolutePath());
				System.out.println(" * Download summary *");
				for (Map.Entry<InetSocketAddress, int[]> entry : downloadSummary.entrySet()) {
					int[] values = entry.getValue();
					System.out.println(values[0] + " bytes (" + values[1] + " chunks) from server at " + entry.getKey());
				}
				downloaded = true;
			}

		} catch (IOException e) {
			System.err.println("Error preparing download: " + e.getMessage());
			return false;
		}
		
		return downloaded;
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	protected int getServerPort() {
		int port = 0;
		/*
		 * TODO: Devolver el puerto de escucha de nuestro servidor de ficheros
		 */
		if (fileServer != null) {
			port = fileServer.getPort();
		}
		return port;
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	protected void stopFileServer() {
		/*
		 * TODO: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		if (fileServer != null) {
			fileServer.stopServer();
			fileServer = null;
		}
	}

	protected boolean serving() {
		return fileServer != null;
	}

	protected boolean uploadFileToServer(FileInfo matchingFile, String uploadToServer) {
		boolean result = false;
		try {
			// Dirección del peer al que se va a subir
			String[] ipPort = uploadToServer.split(":");
			InetSocketAddress serverAddr = new InetSocketAddress(ipPort[0], Integer.parseInt(ipPort[1]));

			NFConnector connector = new NFConnector(serverAddr);

			// Enviar mensaje de inicio de subida
			PeerMessage startUpload = new PeerMessage(PeerMessageOps.OPCODE_UPLOAD_FILE, matchingFile.fileName);
			startUpload.writeMessageToOutputStream(connector.getDos());

			// Abrir archivo local y leerlo en chunks
			File file = new File(NanoFiles.db.lookupFilePath(matchingFile.fileHash));
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[16384]; // Chunk de 16 KB
			int chunkCount = 0;
			int bytesRead = fis.read(buffer);

			System.out.println("[UPLOAD] Uploading file: " + matchingFile.fileName + " to " + serverAddr);
			while (bytesRead != -1) {
				byte[] chunkData = Arrays.copyOf(buffer, bytesRead);
				PeerMessage chunkMsg = new PeerMessage(PeerMessageOps.OPCODE_GIVE_CHUNK, chunkData);
				chunkMsg.writeMessageToOutputStream(connector.getDos());
				chunkCount++;
			    bytesRead = fis.read(buffer); 
			}
			fis.close();

			PeerMessage closeMsg = new PeerMessage(PeerMessageOps.OPCODE_CLOSE_CONNECTION);
			closeMsg.writeMessageToOutputStream(connector.getDos());

			System.out.println("[UPLOAD] Upload completed with " + chunkCount + " chunks.");
			connector.close();
			result = true;

		} catch (IOException e) {
			System.err.println("[UPLOAD] Error uploading file: " + e.getMessage());
		}


		return result;
	}

}
