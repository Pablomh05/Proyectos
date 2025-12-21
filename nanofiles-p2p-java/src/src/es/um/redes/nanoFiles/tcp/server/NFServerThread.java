package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.Socket;

public class NFServerThread extends Thread {
	/*
	 * TODO: Esta clase modela los hilos que son creados desde NFServer y cada uno
	 * de los cuales simplemente se encarga de invocar a
	 * NFServer.serveFilesToClient con el socket retornado por el método accept
	 * (un socket distinto para "conversar" con un cliente)
	 */

	private Socket clientSocket;

	public NFServerThread(Socket socket) {
		this.clientSocket = socket;
	}

	@Override
	public void run() {
		try {
			NFServer.serveFilesToClient(clientSocket);
		} catch (IOException e) {
			System.err.println("[NFServerThread] Error while serving client: " + e.getMessage());
		}
	}


}
