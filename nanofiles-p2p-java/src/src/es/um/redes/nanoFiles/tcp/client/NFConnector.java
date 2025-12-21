package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;

	private DataInputStream dis;
	private DataOutputStream dos;


	public NFConnector(InetSocketAddress fserverAddr) {
		this.serverAddr = fserverAddr;
		/*
		 * TODO: (Boletín SocketsTCP) Se crea el socket a partir de la dirección del
		 * servidor (IP, puerto). La creación exitosa del socket significa que la
		 * conexión TCP ha sido establecida.
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Se crean los DataInputStream/DataOutputStream a
		 * partir de los streams de entrada/salida del socket creado. Se usarán para
		 * enviar (dos) y recibir (dis) datos del servidor.
		 */
		try {
			socket = new Socket(fserverAddr.getAddress(), fserverAddr.getPort());	
			
			dis = new DataInputStream(socket.getInputStream());
	        dos = new DataOutputStream(socket.getOutputStream());
	    } catch (UnknownHostException e) {
	        System.err.println("[NFConnector] Error: Host desconocido " + serverAddr.getHostName());
	    } catch (IOException e) {
	        System.err.println("[NFConnector] Error de I/O al conectar con el servidor: " + e.getMessage());
	    }
	}

	public void test() {
		/*
		 * TODO: (Boletín SocketsTCP) Enviar entero cualquiera a través del socket y
		 * después recibir otro entero, comprobando que se trata del mismo valor.
		 */
		try {
			int valueOut = 12345;
			dos.writeInt(valueOut);
			int valueIn = dis.readInt();
			if (valueOut == valueIn) {
				System.out.println("[NFConnector] Test passed: received same value " + valueIn);
			} else {
				System.err.println("[NFConnector] Test failed: expected " + valueOut + ", got " + valueIn);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DataInputStream getDis() {
		return dis;
	}

	public DataOutputStream getDos() {
		return dos;
	}

	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

	public void close() {
		try {
			if (dis != null) dis.close();
			if (dos != null) dos.close();
			if (socket != null && !socket.isClosed()) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
