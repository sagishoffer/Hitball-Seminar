package hitBallClientServerMVC;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GameUI extends Application {
	private int gameNum;
	private HitBallController controller;
	private HitBallView view;
	private HitBallModel model;

	public GameUI(int gameNum) {
		this.model = new HitBallModel();
		this.view = new HitBallView(model);
		this.controller = new HitBallController(model, view);
		this.gameNum = gameNum;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(view);
		borderPane.setBottom(controller.getSpeedRate());
		borderPane.setTop(controller.getRadiusSlider());

		Scene scene = new Scene(borderPane, 400, 400);
		scene.setFill(Color.WHITESMOKE);
		primaryStage.setTitle("Gun Game " + gameNum); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
		primaryStage.setAlwaysOnTop(true);
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent arg0) {
				view.setClose(primaryStage);
			}
		});
		setANDrequestFocus();
	}

	private void setANDrequestFocus() {
		view.setFocusTraversable(true);
		view.requestFocus();
	}

}
