package hitBallClientServerMVC;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

class HitBallView extends Pane implements BalloonEvents, GameConstants {
	private final static int NUM_OF_PLAYERS = 2;
	private final static int WINNER_BOX_WIDTH = 300;
	private final static int WINNER_BOX_HIEGHT = 50;

	private final static int ANGLE_DELTA = 3;
	private final static int EXPLOSION_DURATION = 500;
	private final static int GAME_SPEED = 50;
	private final static int BALL_RADIUS = 3;
	private final static int GUN_LENGTH = 60;
	private final static int GUN_WIDTH = 35;
	private final static int PANEL_WIDTH = 200;
	private final static int PANEL_HEIGHT = 100;
	private final static String GUN_IMAGE = "image/gun.gif";

	private VBox winnerBox;
	private Label winnerText;
	private Button reset;
	private SimpleBooleanProperty winner = new SimpleBooleanProperty(false);
	private final Set<KeyCode> pressed = new HashSet<KeyCode>();
	private Color playerColor[] = new Color[NUM_OF_PLAYERS];
	private Timeline balloonTimer;
	private HitBallModel model;
	private Circle balloon;
	private int balloonX;
	private int balloonY;

	private Node gun[] = new Node[NUM_OF_PLAYERS];
	private LinkedList<SmallBall> list = new LinkedList<SmallBall>();
	private SimpleIntegerProperty angle[] = new SimpleIntegerProperty[NUM_OF_PLAYERS];
	private final Rotate rotationTransform[] = new Rotate[NUM_OF_PLAYERS];

	private DataOutputStream osToServer;
	private DataInputStream isFromServer;
	private Socket socket;
	private SimpleBooleanProperty serverAlive = new SimpleBooleanProperty(false);

	public HitBallView(HitBallModel model) {
		initSocket();

		setModel(model);
		initBalloon();
		initGuns();
		initBulletsTimer();
		initWinerBox();

		this.setHeight(PANEL_HEIGHT);
		this.setWidth(PANEL_WIDTH);

		drawGun();
		drawBalloon();
	}

	public void setClose(Stage s) {
		try {
			osToServer.writeUTF("closeGame");
			if (serverAlive.get())
				socket.close();
		} catch (IOException e) {
		}
		HitBallClientServerMVC.closeClient(s);
	}

	public void setModel(HitBallModel newModel) {
		model = newModel;
		if (model != null) {
			model.addActionListener(new RadiusEvent(), eventType.RADIUS);
			model.addActionListener(new RefreshRateEvent(), eventType.REFRESH_RATE);
			model.addActionListener(new FillEvent(), eventType.FILL);
			model.addActionListener(new StrokeEvent(), eventType.STROKE);
			model.addActionListener(new StrokeWidthEvent(), eventType.STROKEWIDTH);
		}
	}

	private void moveBalloonToRandomLocation() {
		this.balloonX = generateBalloonX();
		this.balloonY = generateBalloonY();
		balloon.setCenterX(balloonX);
		balloon.setCenterY(balloonY);
	}

	public int generateBalloonX() {
		int balloonRadius = model.getBalloonRadius();
		int balloonX = (int) (Math.random() * getWidth() + balloonRadius);
		if (balloonX + balloonRadius >= getWidth()) {
			balloonX -= balloonRadius * 2;
		}
		return balloonX;
	}

	public int generateBalloonY() {
		int balloonRadius = model.getBalloonRadius();
		int balloonY = (int) (Math.random() * getHeight() + balloonRadius);
		if (balloonY + balloonRadius >= getHeight()) {
			balloonY -= balloonRadius * 2;
		}
		return balloonY;
	}

	public void setBalloonRefreshRate(int miliseconds) {
		balloonTimer.stop();
		balloonTimer.getKeyFrames().setAll(new KeyFrame(Duration.millis(miliseconds), e -> {
			moveBalloonToRandomLocation();
		}));
		balloonTimer.play();
	}

	private void drawBalloon() {
		balloon.setStroke(model.getStroke());
		balloon.setStrokeWidth(model.getStrokeWidth());
		balloon.setFill(model.getFill());
		try {
			getChildren().add(balloon);
		} catch (IllegalArgumentException ex) {
		}
	}

	private void drawBullets() {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).refresh();
		}
	}

	private void rotateGun(KeyEvent e) {
		if (pressed.size() > 0 && !winner.get()) {
			if (pressed.contains(KeyCode.LEFT)) {
				if (angle[PLAYER1].get() > -90)
					angle[PLAYER1].set(angle[PLAYER1].get() - ANGLE_DELTA);
			}
			if (pressed.contains(KeyCode.RIGHT)) {
				if (angle[PLAYER1].get() < 90)
					angle[PLAYER1].set(angle[PLAYER1].get() + ANGLE_DELTA);
			}
			if (pressed.contains(KeyCode.UP)) {
				SmallBall b = new SmallBall(GUN_LENGTH, angle[PLAYER1].get(), PLAYER1, playerColor[PLAYER1]);
				list.add(b);
				try {
					getChildren().add(b.getBullet());
				} catch (IllegalArgumentException ex) {
				}
			}

			if (pressed.contains(KeyCode.A)) {
				if (angle[PLAYER2].get() > -90)
					angle[PLAYER2].set(angle[PLAYER2].get() - ANGLE_DELTA);
			}
			if (pressed.contains(KeyCode.D)) {
				if (angle[PLAYER2].get() < 90)
					angle[PLAYER2].set(angle[PLAYER2].get() + ANGLE_DELTA);
			}
			if (pressed.contains(KeyCode.W)) {
				SmallBall b = new SmallBall(GUN_LENGTH, angle[PLAYER2].get(), PLAYER2, playerColor[PLAYER2]);
				list.add(b);
				try {
					getChildren().add(b.getBullet());
				} catch (IllegalArgumentException ex) {
				}
			}
		}
	}

	private void initSocket() {
		try { // Create a socket to connect to the server
			socket = new Socket("localhost", 8000);
			isFromServer = new DataInputStream(socket.getInputStream());
			osToServer = new DataOutputStream(socket.getOutputStream());
			serverAlive.set(true);

		} catch (IOException ex) {
			serverAlive.set(false);
		}
	}

	private void initWinerBox() {
		winnerBox = new VBox();
		winnerText = new Label();
		reset = new Button("Start Another Game");

		winnerText.setFont(Font.font("Ariel", FontWeight.BOLD, 30));
		winnerBox.setAlignment(Pos.CENTER);
		winnerBox.visibleProperty().bind(winner);
		winnerBox.setPrefHeight(WINNER_BOX_HIEGHT);
		winnerBox.setPrefWidth(WINNER_BOX_WIDTH);
		winnerBox.getChildren().addAll(winnerText, reset);
		winnerBox.layoutXProperty().bind(widthProperty().divide(2).subtract(WINNER_BOX_WIDTH / 2));
		winnerBox.layoutYProperty().bind(heightProperty().divide(2).subtract(WINNER_BOX_HIEGHT / 2));

		reset.setOnAction(e -> {
			winner.set(false);
			try {
				osToServer.writeUTF("new");
			} catch (Exception e1) {
			}
			for (int i = 0; i < NUM_OF_PLAYERS; i++)
				angle[i].set(0);
			drawGun();
			drawBalloon();
			balloonTimer.play();
		});
	}

	private void initBalloon() {

		// init balloon object
		this.balloonX = generateBalloonX();
		this.balloonY = generateBalloonY();
		this.balloon = new Circle(balloonX, balloonY, model.getBalloonRadius());
		this.balloon.visibleProperty().bind(winner.not());

		// starting balloon timer
		this.balloonTimer = new Timeline(new KeyFrame(Duration.millis(model.getBalloonRefreshRate()), e -> {
			moveBalloonToRandomLocation();
		}));
		this.balloonTimer.setCycleCount(Timeline.INDEFINITE);
		this.balloonTimer.play();
	}

	private void initBulletsTimer() {
		// starting balls speed timer
		Timeline gameTimer = new Timeline(new KeyFrame(Duration.millis(GAME_SPEED), e -> {
			if (!winner.get())
				drawBullets();
		}));
		gameTimer.setCycleCount(Timeline.INDEFINITE);
		gameTimer.play();
	}

	private void initGuns() {
		// init Guns
		for (int i = 0; i < NUM_OF_PLAYERS; i++) {
			float space = (NUM_OF_PLAYERS + 1) / (i + 1.0f);
			this.playerColor[i] = new Color(Math.random(), Math.random(), Math.random(), 1);
			this.gun[i] = new ImageView(new Image(new File(GUN_IMAGE).toURI().toString()));
			((ImageView) gun[i]).setFitHeight(GUN_LENGTH);
			((ImageView) gun[i]).setFitWidth(GUN_WIDTH);
			this.angle[i] = new SimpleIntegerProperty(0);
			rotationTransform[i] = new Rotate(0, 0, 0);
			rotationTransform[i].angleProperty().bind(angle[i]);
			rotationTransform[i].setPivotX(GUN_WIDTH / 2);
			rotationTransform[i].setPivotY(GUN_LENGTH);
			gun[i].getTransforms().add(rotationTransform[i]);
			gun[i].layoutXProperty().bind(widthProperty().divide(space).subtract(GUN_WIDTH / 2));
			gun[i].layoutYProperty().bind(heightProperty().subtract(GUN_LENGTH));
		}

		// action event
		this.setOnKeyPressed(e -> {
			this.pressed.add(e.getCode());
			rotateGun(e);
			if (!winner.get()) {
				drawBullets();
			}
		});

		this.setOnKeyReleased(e -> {
			pressed.remove(e.getCode());
		});
	}

	private void drawGun() {
		for (int i = 0; i < NUM_OF_PLAYERS; i++) {
			try {
				getChildren().add(gun[i]);
			} catch (IllegalArgumentException ex) {
			}
		}
	}

	private void drawExplosion() {
		int balloonRadius = model.getBalloonRadius();
		LinkedList<Ellipse> pieces = new LinkedList<Ellipse>();
		pieces.add(generatePiece(balloonX - balloonRadius / 2 - 5, balloonY - balloonRadius / 2, balloonRadius));
		pieces.add(generatePiece(balloonX + 2 * balloonRadius + 5 - balloonRadius / 2, balloonY - balloonRadius / 2,
				balloonRadius));
		pieces.add(generatePiece(balloonX - balloonRadius / 2, balloonY + 2 * balloonRadius + 5 - balloonRadius / 2,
				balloonRadius));
		pieces.add(generatePiece(balloonX - balloonRadius / 2, balloonY - 2 * balloonRadius - 5 - balloonRadius / 2,
				balloonRadius));
		getChildren().remove(balloon);
		balloonX = -100;
		balloonY = -100;
		try {
			getChildren().addAll(pieces); // run later
		} catch (IllegalArgumentException ex) {
		}
		new Timeline(new KeyFrame(Duration.millis(EXPLOSION_DURATION), e -> {
			getChildren().removeAll(pieces);
			moveBalloonToRandomLocation();
			try {
				getChildren().add(balloon);
			} catch (IllegalArgumentException ex) {
			}
		})).play();
	}

	private Ellipse generatePiece(int x, int y, int radius) {
		Ellipse e = new Ellipse(x, y, radius / 2, radius / 2);
		e.setFill(Color.TRANSPARENT);
		e.setStroke(Color.BLACK);
		e.setStrokeWidth(model.getStrokeWidth());
		return e;
	}

	class SmallBall {
		private final static int BALL_STEP = 5;
		private Ellipse bullet;
		private int length;
		private int angle;
		private int x;
		private int y;
		private int player;

		SmallBall(int length, int angle, int player, Color playerColor) {
			this.player = player;
			this.length = length;
			this.angle = angle + 90;
			this.x = (int) (this.length * Math.cos(Math.toRadians(this.angle)) * -1
					+ getWidth() / (NUM_OF_PLAYERS + 1) * (player + 1));
			this.y = (int) (getHeight() - this.length * Math.sin(Math.toRadians(this.angle)));
			this.bullet = new Ellipse(x - BALL_RADIUS, y - BALL_RADIUS, 2 * BALL_RADIUS, 2 * BALL_RADIUS);
			this.bullet.fillProperty().set(playerColor);
			this.bullet.visibleProperty().bind(winner.not());
		}

		public Ellipse getBullet() {
			return this.bullet;
		}

		public void refresh() {

			// In Case the bullet is out of boundaries
			if (x > getWidth() || x < 0 || y < 0) {
				// remove the bullet
				getChildren().remove(bullet);
				list.remove(this);

				try {
					osToServer.writeUTF("MissShot");
					osToServer.writeInt(player);
				} catch (IOException e) {
				}
				return;
			}

			// in case the bullet overlaps the balloon, remove the balloon and
			// draw an explosion
			int balloonRadius = model.getBalloonRadius();
			if (overlaps(x, y, BALL_RADIUS, balloonX, balloonY, balloonRadius)) {

				// remove the bullet
				getChildren().remove(bullet);
				list.remove(this);

				try {
					osToServer.writeUTF("HitShot");
					osToServer.writeInt(player);

					int status = isFromServer.readInt();
					if (status == CONTINUE) {
						winner.set(false);
						drawExplosion();
					} else {
						winner.set(true);
						balloonTimer.stop();
						list.clear();
						getChildren().clear();
						if (status == PLAYER1_WON)
							winnerText.setText("Player 1 Won!");
						else
							winnerText.setText("Player 2 Won!");
						getChildren().add(winnerBox);
					}
				} catch (IOException e) {
				}
			}

			this.length += BALL_STEP;
			this.x = (int) (this.length * Math.cos(Math.toRadians(this.angle)) * -1
					+ getWidth() / (NUM_OF_PLAYERS + 1) * (player + 1));
			this.y = (int) (getHeight() - this.length * Math.sin(Math.toRadians(this.angle)));
			this.bullet.setCenterX(x);
			this.bullet.setCenterY(y);
		}

		public boolean overlaps(double x1, double y1, double radius1, double x2, double y2, double radius2) {
			return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) <= radius1 + radius2;
		}
	}

	class RadiusEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			balloon.setRadius(model.getBalloonRadius());
			try {
				osToServer.writeUTF("setRadius");
				osToServer.writeInt(model.getBalloonRadius());
			} catch (IOException e1) {
			}
		}
	}

	class RefreshRateEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setBalloonRefreshRate(model.getBalloonRefreshRate());
			try {
				osToServer.writeUTF("refreshRate");
				osToServer.writeInt(HitBallModel.BALLOON_MAX_SPEED - model.getBalloonRefreshRate());
			} catch (IOException e1) {
			}
		}
	}

	class FillEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			balloon.setFill(model.getFill());
		}
	}

	class StrokeEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			balloon.setStroke(model.getStroke());
		}
	}

	class StrokeWidthEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			balloon.setStrokeWidth(model.getStrokeWidth());
		}
	}
}