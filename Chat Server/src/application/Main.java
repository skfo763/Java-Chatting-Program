package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;


public class Main extends Application {
	
	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	//서버 작동
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[Client Connected]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread());
						
					} catch (Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
			
		};
		
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	//서버 중지
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	//UI 생성
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("consolas", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("start");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";
		int port = 10000;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("start")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String message = String.format("[Server Start]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("Exit");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[Exit Server]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("start");
				});
			}
		});
		
		Scene scene = new Scene(root, 500, 500);
		primaryStage.setTitle("[Chatting Server...]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	
	//main method
	public static void main(String[] args) {
		launch(args);
	}
}
