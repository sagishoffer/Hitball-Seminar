package hitBallClientServerMVC;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class HitBallServer extends Application implements GameConstants {

	private final int MAX_ROUND_HIT = 5;
	private HashMap<Integer, Socket> clientsMap = new HashMap<Integer, Socket>();
	private TableView<Statistics> table = new TableView<Statistics>();
	private ObservableList<Statistics> tableData = FXCollections.observableArrayList();

	private TextArea ta = new TextArea();
	private int GameNum = 0;
	private ServerSocket serverSocket;
	private Socket socket;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		createTable();
		VBox box = new VBox(5);
		box.setPadding(new Insets(10));
		box.getChildren().addAll(table, new Label(), new Label("Log:"), ta);
		Scene scene = new Scene(box, 545, 500);
		primaryStage.setTitle("Server"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
		primaryStage.setAlwaysOnTop(true);
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent arg0) {
				try {
					for (int key : clientsMap.keySet())
						clientsMap.get(key).close();
					serverSocket.close();
				} catch (IOException e) {
				}
				HitBallClientServerMVC.closeServer();
			}
		});
		new Thread(() -> {
			try { // Create a server socket
				serverSocket = new ServerSocket(8000);
				Platform.runLater(() -> {
					ta.appendText("Game started at " + new Date() + '\n');
				});
				while (true) { // Listen for a new connection request
					socket = serverSocket.accept();
					// Increment clientNo
					GameNum++;
					InetAddress clientInetAddress = socket.getInetAddress();
					Platform.runLater(() -> { // Display the client number
						ta.appendText("Starting thread for game " + GameNum + " at " + new Date() + '\n');
						ta.appendText(
								"Client " + GameNum + "'s host name is " + clientInetAddress.getHostName() + "\n");
						ta.appendText(
								"Client " + GameNum + "'s IP Address is " + clientInetAddress.getHostAddress() + "\n");
					});
					// Create and start a new thread for the connection
					new Thread(new HandleAClient(socket, GameNum)).start();
				}
			} catch (IOException ex) {
			}
		}).start();
	}

	@SuppressWarnings("unchecked")
	private void createTable() {

		TableColumn<Statistics, Integer> gameNumCol = new TableColumn<>("Game Number");
		gameNumCol.setCellValueFactory(new PropertyValueFactory<Statistics, Integer>("gameNum"));
		gameNumCol.setCellFactory(new Callback<TableColumn<Statistics, Integer>, TableCell<Statistics, Integer>>() {
			public TableCell<Statistics, Integer> call(TableColumn<Statistics, Integer> p) {
				TableCell<Statistics, Integer> cell = new TableCell<Statistics, Integer>() {
					@Override
					public void updateItem(Integer item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty)
							setText("" + item);
						else
							setText(null);
					}
				};
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		gameNumCol.setMinWidth(100);

		TableColumn<Statistics, Integer> P1_hitShotsCol = new TableColumn<>("Player 1 Hits Shots");
		P1_hitShotsCol.setCellValueFactory(new PropertyValueFactory<Statistics, Integer>("hitShots_P1"));
		P1_hitShotsCol.setCellFactory(new Callback<TableColumn<Statistics, Integer>, TableCell<Statistics, Integer>>() {
			public TableCell<Statistics, Integer> call(TableColumn<Statistics, Integer> p) {
				TableCell<Statistics, Integer> cell = new TableCell<Statistics, Integer>() {
					@Override
					public void updateItem(Integer item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty) {
							int total = 0;
							if (tableData != null)
								total = tableData.get(this.getIndex()).getTotalShots_P1();
							setText(item + " / " + total);
						} else
							setText(null);
					}
				};
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		P1_hitShotsCol.setMinWidth(120);

		TableColumn<Statistics, Integer> P2_hitShotsCol = new TableColumn<>("Player 2 Hits Shots");
		P2_hitShotsCol.setCellValueFactory(new PropertyValueFactory<Statistics, Integer>("hitShots_P2"));
		P2_hitShotsCol.setCellFactory(new Callback<TableColumn<Statistics, Integer>, TableCell<Statistics, Integer>>() {
			public TableCell<Statistics, Integer> call(TableColumn<Statistics, Integer> p) {
				TableCell<Statistics, Integer> cell = new TableCell<Statistics, Integer>() {
					@Override
					public void updateItem(Integer item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty) {
							int total = 0;
							if (tableData != null)
								total = tableData.get(this.getIndex()).getTotalShots_P2();
							setText(item + " / " + total);
						} else
							setText(null);
					}
				};
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		P2_hitShotsCol.setMinWidth(120);

		TableColumn<Statistics, Integer> scoreCol = new TableColumn<>("Score");
		scoreCol.setCellValueFactory(new PropertyValueFactory<Statistics, Integer>("score_P1"));
		scoreCol.setCellFactory(new Callback<TableColumn<Statistics, Integer>, TableCell<Statistics, Integer>>() {
			public TableCell<Statistics, Integer> call(TableColumn<Statistics, Integer> p) {
				TableCell<Statistics, Integer> cell = new TableCell<Statistics, Integer>() {
					@Override
					public void updateItem(Integer item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty) {
							int score_P2 = 0;
							if (tableData != null)
								score_P2 = tableData.get(this.getIndex()).getScore_P2();
							setText(item + " : " + score_P2);
						} else
							setText(null);
					}
				};
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		scoreCol.setMinWidth(40);

		TableColumn<Statistics, String> statusCol = new TableColumn<>("Game Status");
		statusCol.setCellValueFactory(new PropertyValueFactory<Statistics, String>("status"));
		statusCol.setCellFactory(new Callback<TableColumn<Statistics, String>, TableCell<Statistics, String>>() {
			@Override
			public TableCell<Statistics, String> call(TableColumn<Statistics, String> p) {
				TableCell<Statistics, String> cell = new TableCell<Statistics, String>() {
					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty)
							setText(item);
						else
							setText(null);
					}
				};
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		statusCol.setMinWidth(110);

		table.getColumns().addAll(gameNumCol, P1_hitShotsCol, P2_hitShotsCol, scoreCol, statusCol);
		table.setItems(tableData);
	}

	class HandleAClient extends Thread {
		private Socket connectToClient;
		private int gameNum;

		public HandleAClient(Socket socket, int gameNum) {
			this.connectToClient = socket;
			this.gameNum = gameNum;
		}

		public void run() {
			try {
				DataInputStream isFromClient = new DataInputStream(connectToClient.getInputStream());
				DataOutputStream outputToClient = new DataOutputStream(connectToClient.getOutputStream());
				clientsMap.put(gameNum, connectToClient);

				tableData.add(new Statistics(gameNum));

				while (true) {
					String mission = isFromClient.readUTF();

					if (mission.equals("MissShot")) {
						MissShot(isFromClient);
					} else if (mission.equals("HitShot")) {
						HitShot(isFromClient, outputToClient);
					} else if (mission.equals("closeGame")) {
						closeGame();
					} else if (mission.equals("new")) {
						newGame();
					} else if (mission.equals("refreshRate")) {
						refreshRate(isFromClient);
					} else if (mission.equals("setRadius")) {
						setRadius(isFromClient);
					}
				}

			} catch (IOException e) {
			}
		}

		private void MissShot(DataInputStream isFromClient) {
			try {
				int player = isFromClient.readInt();
				Statistics updateRow = tableData.get(gameNum - 1);
				updateRow.addMissShots(player);
				tableData.set(gameNum - 1, updateRow);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void HitShot(DataInputStream isFromClient, DataOutputStream outputToClient) {
			try {
				int player = isFromClient.readInt();
				Statistics updateRow = tableData.get(gameNum - 1);
				updateRow.addHitShots(player);
				tableData.set(gameNum - 1, updateRow);
				checkWin(player, updateRow, outputToClient);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void checkWin(int player, Statistics updateRow, DataOutputStream outputToClient) {
			try {
				if (player == PLAYER1 && updateRow.getHitShots_P1() >= MAX_ROUND_HIT) {
					outputToClient.writeInt(PLAYER1_WON);
					updateRow.setStatus("Player 1 Won!");
					updateRow.addOneToScore(player);
					tableData.set(gameNum - 1, updateRow);
				} else if (player == PLAYER2 && updateRow.getHitShots_P2() >= MAX_ROUND_HIT) {
					outputToClient.writeInt(PLAYER2_WON);
					updateRow.setStatus("Player 2 Won!");
					updateRow.addOneToScore(player);
					tableData.set(gameNum - 1, updateRow);
				} else
					outputToClient.writeInt(CONTINUE);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void closeGame() {
			Statistics updateRow = tableData.get(gameNum - 1);
			updateRow.setStatus("Game Ended");
			tableData.set(gameNum - 1, updateRow);
			Platform.runLater(() -> { // Display the client number
				ta.appendText("Game " + gameNum + " was closed\n");
			});
			try {
				clientsMap.get(gameNum).close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			clientsMap.remove(gameNum);
		}

		public void newGame() {
			Statistics updateRow = tableData.get(gameNum - 1);
			updateRow.setStatus("In Game");
			updateRow.setHitShots_P1(0);
			updateRow.setHitShots_P2(0);
			updateRow.setTotalShots_P1(0);
			updateRow.setTotalShots_P2(0);
			tableData.set(gameNum - 1, updateRow);
		}

		public void refreshRate(DataInputStream isFromClient) {
			try {
				int refreshRate = isFromClient.readInt();
				Platform.runLater(() -> { // Display the client number
					ta.appendText("Refresh rate of game " + gameNum + " was change to " + refreshRate + "\n");
				});
			} catch (IOException e) {
			}
		}

		public void setRadius(DataInputStream isFromClient) {
			try {
				int radius = isFromClient.readInt();
				Platform.runLater(() -> { // Display the client number
					ta.appendText("Radius of game " + gameNum + " was change to " + radius + "\n");
				});
			} catch (IOException e) {
			}
		}
	}
}
