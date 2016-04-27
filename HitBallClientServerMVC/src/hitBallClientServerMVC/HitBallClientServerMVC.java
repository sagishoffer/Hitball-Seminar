// Sagi Shoffer
// Oren Yulzary

package hitBallClientServerMVC;

import java.io.File;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HitBallClientServerMVC extends Application {
	private Button server_btn, client_btn;
	private final String SERVER_IMG_PATH = "image/server4.gif";
	private final String CLIENT_IMG_PATH = "image/game.gif";
	private static ArrayList<Stage> clientsList = new ArrayList<>();
	private final int BUTTON_IMAGE_SIZE = 150;

	private static SimpleBooleanProperty serverAlive = new SimpleBooleanProperty(false);
	private static int gameNum;

	public Button createButton(String name, String path) {
		File file = new File(path);
		Image image = new Image(file.toURI().toString());
		ImageView icon = new ImageView(image);
		icon.setFitHeight(BUTTON_IMAGE_SIZE);
		icon.setFitWidth(BUTTON_IMAGE_SIZE);
		return new Button(name, icon);
	}

	private void load_client() {
		GameUI gameClient = new GameUI(++gameNum);
		try {
			Stage gameClientStage = new Stage();
			clientsList.add(gameClientStage);
			gameClient.start(gameClientStage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void load_server() {
		HitBallServer server = new HitBallServer();
		try {
			Stage serverStage = new Stage();
			server.start(serverStage);
			setServerAlive(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setServerAlive(boolean serverAlive) {
		HitBallClientServerMVC.serverAlive.set(serverAlive);
	}

	public static void closeClient(Stage clientStage) {
		clientsList.remove(clientStage);
	}

	public static void closeServer() {
		gameNum = 0;
		setServerAlive(false);

		for (int i = 0; i < clientsList.size(); i++)
			clientsList.get(i).close();
		clientsList.clear();
	}

	public void start(Stage primaryStage) throws Exception {
		server_btn = createButton("Load Server", SERVER_IMG_PATH);
		server_btn.setContentDisplay(ContentDisplay.TOP);
		server_btn.setStyle("-fx-font: 16px Serif; -fx-font-weight: bold;");
		client_btn = createButton("New Game", CLIENT_IMG_PATH);
		client_btn.setContentDisplay(ContentDisplay.TOP);
		client_btn.setStyle("-fx-font: 16px Serif; -fx-font-weight: bold;");

		HBox hb = new HBox(30);
		hb.setAlignment(Pos.CENTER);
		hb.getChildren().addAll(server_btn, client_btn);
		Scene scene = new Scene(hb, 430, 250);

		server_btn.setOnAction(e -> load_server());
		client_btn.setOnAction(e -> load_client());
		server_btn.disableProperty().bind(serverAlive);
		client_btn.disableProperty().bind(serverAlive.not());

		primaryStage.setTitle("Seminar"); // Set the window title
		primaryStage.setScene(scene); // Place the scene in the window
		primaryStage.show(); // Display the window
		primaryStage.setAlwaysOnTop(true);
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent arg0) {
				Platform.exit();
				System.exit(0);
			}
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
}
