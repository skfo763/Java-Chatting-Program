package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	
	Socket socket;
	
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}
	
	public void receive() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = (InputStream) socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						
						while(length == -1) {
							throw new IOException();
						}
						
						System.out.println("[Successfully get Message]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						
						String message = new String(buffer, 0, length, "UTF-8");
						
						for(Client client : Main.clients) {
							client.send(message);
						}
					}
					
					
				} catch(Exception e) {
					try {
						System.out.println("[Message Receive Error]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread());
						
					} catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		
		Main.threadPool.submit(thread);
	}
	
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();	
					
				} catch (Exception e) {
					try {
						System.out.println("[Message Send Error]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread());
						Main.clients.remove(Client.this);
						socket.close();
						
					} catch(Exception e2) {
						e2.printStackTrace();
					}
				}	
			}
		};
		
		Main.threadPool.submit(thread);
	}
}
