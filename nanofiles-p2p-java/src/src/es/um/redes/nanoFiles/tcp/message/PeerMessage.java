package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PeerMessage {

	/*
	 * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */
	private static final int HASH_LENGTH = 20;
	private static final int MAX_FILENAME_LENGTH = 255;

	private byte opcode;
	private String filename;
	private byte[] hash;
	private long fileSize;	
	private long offset;
	private int chunkSize;
	private byte[] chunkData;

	
	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	// Constructor para DOWNLOAD_FILE y UPLOAD_FILE
	public PeerMessage(byte opcode, String filename) {
		if (opcode != PeerMessageOps.OPCODE_DOWNLOAD_FILE && opcode != PeerMessageOps.OPCODE_UPLOAD_FILE) {
			throw new IllegalArgumentException(
					"Invalid opcode for filename constructor");
		}
		this.opcode = opcode;
		if (filename.getBytes().length > MAX_FILENAME_LENGTH) {
			throw new IllegalArgumentException(
					"Filename too long (max 255 bytes)");
		}
		this.filename = filename;
	}

	// Constructor sin campos extra (FILE_NOT_FOUND, AMBIGUOUS_NAME, CLOSE_CONNECTION)
		public PeerMessage(byte op) {
			if (op != PeerMessageOps.OPCODE_FILE_NOT_FOUND && 
				op != PeerMessageOps.OPCODE_AMBIGUOUS_NAME && 
				op != PeerMessageOps.OPCODE_INVALID_CODE &&
				op != PeerMessageOps.OPCODE_CLOSE_CONNECTION) {
				throw new IllegalArgumentException(
						"This opcode requires extra data");
			}
			this.opcode = op;
		}
	
	// Constructor para FILE_FOUND 
	public PeerMessage(byte opcode, byte[] hash, long fileSize) {
		if (opcode != PeerMessageOps.OPCODE_FILE_FOUND) {
			throw new IllegalArgumentException(
					"Invalid opcode for FILE_FOUND constructor");
		}
		if (hash.length != HASH_LENGTH) {
			throw new IllegalArgumentException(
					"Invalid hash length. Expected 20 bytes.");
		}
		this.opcode = opcode;
		this.hash = hash;
		this.fileSize = fileSize;
	}

	// Constructor para GET_CHUNK 
	public PeerMessage(byte opcode, long offset, int chunkSize) {
		if (opcode != PeerMessageOps.OPCODE_GET_CHUNK) {
			throw new IllegalArgumentException(
					"Invalid opcode for GET_CHUNK constructor");
		}
		this.opcode = opcode;
		this.offset = offset;
		this.chunkSize = chunkSize;
	}

	// Constructor para GIVE_CHUNK
	public PeerMessage(byte opcode, byte[] chunkData) {
		if (opcode != PeerMessageOps.OPCODE_GIVE_CHUNK) {
			throw new IllegalArgumentException(
					"Invalid opcode for GIVE_CHUNK constructor");
		}
		this.opcode = opcode;
		this.chunkData = chunkData;
	}


	/*
	 * TODO: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public byte getOpcode() {
		return opcode;
	}
	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		if (opcode != PeerMessageOps.OPCODE_DOWNLOAD_FILE && opcode != PeerMessageOps.OPCODE_UPLOAD_FILE) {
			throw new RuntimeException(
					"PeerMessage: setFilename called for message of unexpected type (" + opcode + ", " + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		if (filename.getBytes().length > MAX_FILENAME_LENGTH) {
			throw new IllegalArgumentException(
					"Filename too long (max 255 bytes)");
		}
		this.filename = filename;
	}

	public byte[] getHash() {
		return hash;
	}
	public void setHash(byte[] hash) {
		if (opcode != PeerMessageOps.OPCODE_FILE_FOUND) {
			throw new RuntimeException(
					"PeerMessage: setHash called for message of unexpected type (" + opcode + ", " + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		if (hash.length != HASH_LENGTH) {
			throw new IllegalArgumentException(
					"Invalid hash length. Expected 32 bytes.");
		}
		this.hash = hash;
	}
	
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		if (opcode != PeerMessageOps.OPCODE_FILE_FOUND) {
			throw new RuntimeException(
					"PeerMessage: setFileSize called for message of unexpected type (" + opcode + ", " + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.fileSize = fileSize;
	}
	
	public long getOffset() {
		return offset;
	}
	public void setOffset(long offset) {
		if (opcode != PeerMessageOps.OPCODE_GET_CHUNK) {
			throw new RuntimeException(
					"PeerMessage: setOffset called for message of unexpected type (" + opcode + ", " + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.offset = offset;
	}
	
	public int getChunkSize() {
		return chunkSize;
	}
	public void setChunkSize(int chunkSize) {
		if (opcode != PeerMessageOps.OPCODE_GET_CHUNK) {
			throw new RuntimeException(
					"PeerMessage: setChunkSize called for message of unexpected type (" + opcode + ", " + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.chunkSize = chunkSize;
	}
	
	public byte[] getChunkData() {
		return chunkData;
	}
	public void setChunkData(byte[] chunkData) {
		if (opcode != PeerMessageOps.OPCODE_GIVE_CHUNK) {
			throw new RuntimeException(
					"PeerMessage: setChunkData called for message of unexpected type (" + opcode + ", " + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.chunkData = chunkData;
	}
	
	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto DirMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		message.setOpcode(opcode);
		switch (opcode) {			
		case PeerMessageOps.OPCODE_DOWNLOAD_FILE:
		case PeerMessageOps.OPCODE_UPLOAD_FILE: 
			byte len = dis.readByte();
			if (len > MAX_FILENAME_LENGTH) throw new IOException("Filename length exceeds 255 bytes");
			byte[] nameBytes = new byte[len];
			dis.readFully(nameBytes);
			message.setFilename(new String(nameBytes));
            break;
            
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
		case PeerMessageOps.OPCODE_AMBIGUOUS_NAME:
		case PeerMessageOps.OPCODE_CLOSE_CONNECTION:
			break;
			
		case PeerMessageOps.OPCODE_FILE_FOUND: 
			byte[] hash = new byte[HASH_LENGTH];
			dis.readFully(hash);
			long size = dis.readLong();
			message.setHash(hash);
			message.setFileSize(size);
			break;
		
		case PeerMessageOps.OPCODE_GET_CHUNK: 
			long offset = dis.readLong();
			int chunkSize = dis.readInt();
			message.setOffset(offset);
			message.setChunkSize(chunkSize);
			break;
			
		case PeerMessageOps.OPCODE_GIVE_CHUNK: 
			int length = dis.readInt();
			if (length < 0) throw new IOException("Invalid chunk length");
			byte[] data = new byte[length];
			dis.readFully(data);
			message.setChunkData(data);
			break;
			
		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para leer un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_DOWNLOAD_FILE:
		case PeerMessageOps.OPCODE_UPLOAD_FILE: 
			byte[] nameBytes = filename.getBytes();
			if (nameBytes.length > MAX_FILENAME_LENGTH) throw new IOException("Filename too long");
			dos.writeByte(nameBytes.length);
			dos.write(nameBytes);
			break;
			
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
		case PeerMessageOps.OPCODE_AMBIGUOUS_NAME:
		case PeerMessageOps.OPCODE_CLOSE_CONNECTION:
			break;
			
		case PeerMessageOps.OPCODE_FILE_FOUND: 
			if (hash.length != HASH_LENGTH) throw new IOException("Invalid hash length");
			dos.write(hash);
			dos.writeLong(fileSize);
			break;
		
		case PeerMessageOps.OPCODE_GET_CHUNK: 
			dos.writeLong(offset);
			dos.writeInt(chunkSize);
			break;

		case PeerMessageOps.OPCODE_GIVE_CHUNK: 
			if (chunkData == null) throw new IOException("Invalid chunk data");
			dos.writeInt(chunkData.length);
			dos.write(chunkData);
			break;
			
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}




}
