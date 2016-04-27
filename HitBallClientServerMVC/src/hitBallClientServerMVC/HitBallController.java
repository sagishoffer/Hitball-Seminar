package hitBallClientServerMVC;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;

public class HitBallController {
	private final int SLIDER_MAJOR_TICK_SPEED = 100;
	private final int SLIDER_BLOCK_INCREMENT_SPEED = 100;
	private final int SLIDER_MAJOR_TICK_RADIUS = 1;
	private final int SLIDER_BLOCK_INCREMENT_RADIUS = 1;
	private HitBallModel model;
	private Node view;

	public HitBallController(HitBallModel model, Node view) {
		setModel(model);
		this.view = view;
	}

	public void setModel(HitBallModel newModel) {
		model = newModel;
	}

	public HitBallModel getModel() {
		return model;
	}

	public Node getRadiusSlider() {
		VBox vboxTop = new VBox();
		Label lblBalloonRadius = new Label("Balloon radius");
		Slider balloonRadiusSlider = new Slider();
		balloonRadiusSlider.setFocusTraversable(false);
		balloonRadiusSlider.setMin(HitBallModel.BALLOON_MIN_RADIUS);
		balloonRadiusSlider.setMax(HitBallModel.BALLOON_MAX_RADIUS);
		balloonRadiusSlider.setValue(model.getBalloonRadius());
		balloonRadiusSlider.setShowTickLabels(true);
		balloonRadiusSlider.setShowTickMarks(false);
		balloonRadiusSlider.setMajorTickUnit(SLIDER_MAJOR_TICK_RADIUS);
		balloonRadiusSlider.setBlockIncrement(SLIDER_BLOCK_INCREMENT_RADIUS);
		balloonRadiusSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (model == null)
				return;
			model.setBalloonRadius(newValue.intValue());
			setANDrequestFocus();
		});

		vboxTop.setAlignment(Pos.CENTER);
		vboxTop.setPadding(new Insets(10));
		VBox.setMargin(lblBalloonRadius, new Insets(5));
		vboxTop.setBackground(Background.EMPTY);
		vboxTop.getChildren().addAll(lblBalloonRadius, balloonRadiusSlider);

		return vboxTop;
	}

	public Node getSpeedRate() {
		VBox vboxBottom = new VBox();
		Label lblBalloonSpeed = new Label("Balloon Speed Rate");
		Slider balloonSpeedSlider = new Slider();
		balloonSpeedSlider.setFocusTraversable(false);
		balloonSpeedSlider.setMin(HitBallModel.BALLOON_MIN_SPEED);
		balloonSpeedSlider.setMax(HitBallModel.BALLOON_MAX_SPEED - HitBallModel.BALLOON_MIN_SPEED);
		balloonSpeedSlider.setValue(model.getBalloonRefreshRate());
		balloonSpeedSlider.setShowTickLabels(true);
		balloonSpeedSlider.setShowTickMarks(false);
		balloonSpeedSlider.setMajorTickUnit(SLIDER_MAJOR_TICK_SPEED);
		balloonSpeedSlider.setBlockIncrement(SLIDER_BLOCK_INCREMENT_SPEED);
		balloonSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (model == null)
				return;
			model.setBalloonRefreshRate(HitBallModel.BALLOON_MAX_SPEED - newValue.intValue());
			setANDrequestFocus();
		});
		vboxBottom.setAlignment(Pos.CENTER);
		vboxBottom.setPadding(new Insets(10));
		VBox.setMargin(lblBalloonSpeed, new Insets(5));
		vboxBottom.setBackground(Background.EMPTY);
		vboxBottom.getChildren().addAll(lblBalloonSpeed, balloonSpeedSlider);

		return vboxBottom;
	}

	private void setANDrequestFocus() {
		view.setFocusTraversable(true);
		view.requestFocus();
	}
}
