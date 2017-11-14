package edu.gatech.dynodroid.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/***
 * This is a wrapper class that provides connection to a port on local host
 * as of now this is used to communicate with ViewServer of emulator
 * @author machiry
 *
 */
public class LocalHostConnection {
	private SocketChannel mSocketChannel;
	private BufferedReader mIn;
	private BufferedWriter mOut;
	private int port;

	public LocalHostConnection(int portNo) throws IOException {
		this.mSocketChannel = SocketChannel.open();
		this.port = portNo;
		int i = this.port;

		if (i == -1) {
			throw new IOException();
		}

		this.mSocketChannel.connect(new InetSocketAddress("127.0.0.1", i));
		this.mSocketChannel.socket().setSoTimeout(40000);
	}

	public BufferedReader getInputStream() throws IOException {
		if (this.mIn == null) {
			this.mIn = new BufferedReader(new InputStreamReader(
					this.mSocketChannel.socket().getInputStream()));
		}
		return this.mIn;
	}

	public BufferedWriter getOutputStream() throws IOException {
		if (this.mOut == null) {
			this.mOut = new BufferedWriter(new OutputStreamWriter(
					this.mSocketChannel.socket().getOutputStream()));
		}

		return this.mOut;
	}

	public Socket getSocket() {
		return this.mSocketChannel.socket();
	}

	public void sendCommand(String paramString) throws IOException {
		BufferedWriter localBufferedWriter = getOutputStream();
		localBufferedWriter.write(paramString);
		localBufferedWriter.newLine();
		localBufferedWriter.flush();
	}

	public void close() {
		try {
			if (this.mIn != null)
				this.mIn.close();
		} catch (IOException localIOException1) {
			Logger.logException(localIOException1);
		}
		try {
			if (this.mOut != null)
				this.mOut.close();
		} catch (IOException localIOException2) {
			Logger.logException(localIOException2);
		}
		try {
			this.mSocketChannel.close();
		} catch (IOException localIOException3) {
			Logger.logException(localIOException3);
		}
	}
}
