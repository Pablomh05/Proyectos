package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PeerMessageTest {

	public static void main(String[] args) throws IOException {
		String nombreArchivo = "peermsg.bin";
		DataOutputStream fos = new DataOutputStream(new FileOutputStream(nombreArchivo));

		/*
		 * TODO: Probar a crear diferentes tipos de mensajes (con los opcodes válidos
		 * definidos en PeerMessageOps), estableciendo los atributos adecuados a cada
		 * tipo de mensaje. Luego, escribir el mensaje a un fichero con
		 * writeMessageToOutputStream para comprobar que readMessageFromInputStream
		 * construye un mensaje idéntico al original.
		 */
		// 1. DOWNLOAD_FILE
        PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_FILE, "documento.txt");

        // 2. UPLOAD_FILE
        //PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_UPLOAD_FILE, "subida.txt");

        // 3. FILE_NOT_FOUND
        // PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);

        // 4. AMBIGUOUS_NAME
        // PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_AMBIGUOUS_NAME);

        // 5. FILE_FOUND
        // byte[] hash = new byte[32]; Arrays.fill(hash, (byte) 1); // hash simulado
        // long fileSize = 123456L;
        // PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_FOUND, hash, fileSize);

        // 6. GET_CHUNK
        // PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_GET_CHUNK, 2048L, 4096);

        // 7. GIVE_CHUNK
        // byte[] chunk = "Este es un trozo del archivo".getBytes();
        // PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_GIVE_CHUNK, chunk);

		msgOut.writeMessageToOutputStream(fos);

		DataInputStream fis = new DataInputStream(new FileInputStream(nombreArchivo));
		PeerMessage msgIn = PeerMessage.readMessageFromInputStream((DataInputStream) fis);

		/*
		 * TODO: Comprobar que coinciden los valores de los atributos relevantes al tipo
		 * de mensaje en ambos mensajes (msgOut y msgIn), empezando por el opcode.
		 */
		if (msgOut.getOpcode() != msgIn.getOpcode()) {
			System.err.println("Opcode does not match!");
		}else {
            System.out.println("Tipo leído: " + PeerMessageOps.opcodeToOperation(msgIn.getOpcode()));
		}
		switch (msgIn.getOpcode()) {
        case PeerMessageOps.OPCODE_DOWNLOAD_FILE:
        case PeerMessageOps.OPCODE_UPLOAD_FILE:
            System.out.println("Filename: " + msgIn.getFilename());
            break;
        case PeerMessageOps.OPCODE_FILE_FOUND:
            System.out.println("Hash: " + Arrays.toString(msgIn.getHash()));
            System.out.println("File size: " + msgIn.getFileSize());
            break;
        case PeerMessageOps.OPCODE_GET_CHUNK:
            System.out.println("Offset: " + msgIn.getOffset());
            System.out.println("Chunk size: " + msgIn.getChunkSize());
            break;
        case PeerMessageOps.OPCODE_GIVE_CHUNK:
            System.out.println("Chunk data: " + new String(msgIn.getChunkData()));
            break;
        case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
            System.out.println("Mensaje: FILE_NOT_FOUND");
            break;
        case PeerMessageOps.OPCODE_AMBIGUOUS_NAME:
            System.out.println("Mensaje: AMBIGUOUS_NAME");
            break;
        default:
            System.err.println("Opcode no reconocido");
		}
	}
}

