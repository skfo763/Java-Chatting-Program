package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class Main extends Application {
	
	Socket socket;
	TextArea textArea;
	
	//Client program running method
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
					
				} catch (Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[Server Connection failed]");
						Platform.exit();
					}
					return;
				}
			}
		};
		thread.start();
	}
	
	//Terminate client program
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();		}
	}
	
	//receiving message
	public void receive() {
		while(true) {
			try {
				InputStream in = (InputStream) socket.getInputStream();
				byte[] buff = new byte[512];
				int length = in.read(buff);
				
				if(length == -1) {
					throw new IOException();
				}
				
				String message = new String(buff, 0, length, "UTF-8");
				System.out.println(message);
				Platform.runLater(() -> {
					textArea.appendText(message);
				});
				
			} catch (Exception e) {
				stopClient();
				e.printStackTrace();
				break;
			}
		}
	}
	
	//sending message
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = (OutputStream) socket.getOutputStream();
					byte[] buf = message.getBytes("UTF-8");
					out.write(buf);
					out.flush();
					
				} catch (Exception e) {
					stopClient();
				}
			}
			
		};
		thread.start();
	}
	
	//UI design
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("Nickname");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		TextField IPText = new TextField("127.0.0.1");
		TextField porttext = new TextField("10000");
		porttext.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName, IPText, porttext);
		root.setTop(hbox);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
		
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button sendbtn = new Button("Send");
		sendbtn.setDisable(true);
		
		sendbtn.setOnAction(event -> {
			send(userName.getText()+": "+input.getText()+"\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button connectionbtn = new Button("Connect");
		connectionbtn.setOnAction(event -> {
			if(connectionbtn.getText().equals("Connect")) {
				int port = 10000;
				try {
					port = Integer.parseInt(porttext.getText());
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				startClient(IPText.getText(), port);
				Platform.runLater(() -> {
					textArea.appendText("[Chatting room connected!]\n");
				});
				connectionbtn.setText("Terminate");
				input.setDisable(false);
				sendbtn.setDisable(false);
				input.requestFocus();
				
			} else {
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[Exit chatting room]\n");
				});
				connectionbtn.setText("Connect");
				input.setDisable(false);
				sendbtn.setDisable(true);
				
			}
		});
		
		BorderPane pan = new BorderPane();
		pan.setLeft(connectionbtn);
		pan.setCenter(input);
		pan.setRight(sendbtn);
		
		root.setBottom(pan);
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[Chatting Client..]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		connectionbtn.requestFocus();			
	}
	
	//main function
	public static void main(String[] args) {
		launch(args);
	}
}
