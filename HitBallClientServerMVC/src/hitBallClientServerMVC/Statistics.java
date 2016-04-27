package hitBallClientServerMVC;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Statistics implements GameConstants {
	private SimpleIntegerProperty gameNum;
	private SimpleIntegerProperty hitShots_P1;
	private SimpleIntegerProperty hitShots_P2;
	private SimpleIntegerProperty totalShots_P1;
	private SimpleIntegerProperty totalShots_P2;
	private SimpleIntegerProperty score_P1;
	private SimpleIntegerProperty score_P2;
	private SimpleStringProperty status;

	public Statistics(int gameNum) {
		this.gameNum = new SimpleIntegerProperty(gameNum);
		this.hitShots_P1 = new SimpleIntegerProperty(0);
		this.hitShots_P2 = new SimpleIntegerProperty(0);
		this.totalShots_P1 = new SimpleIntegerProperty(0);
		this.totalShots_P2 = new SimpleIntegerProperty(0);
		this.score_P1 = new SimpleIntegerProperty(0);
		this.score_P2 = new SimpleIntegerProperty(0);
		this.status = new SimpleStringProperty("In Game");
	}

	public void addOneToScore(int player) {
		int score;
		if (player == PLAYER1) {
			score = score_P1.get();
			this.score_P1.set(++score);
		} else if (player == PLAYER2) {
			score = score_P2.get();
			this.score_P2.set(++score);
		}
	}

	public void addMissShots(int player) {
		int total;
		if (player == PLAYER1) {
			total = totalShots_P1.get();
			this.totalShots_P1.set(++total);
		} else if (player == PLAYER2) {
			total = totalShots_P2.get();
			this.totalShots_P2.set(++total);
		}
	}

	public void addHitShots(int player) {
		int hit, total;
		if (player == PLAYER1) {
			total = totalShots_P1.get();
			hit = hitShots_P1.get();
			this.hitShots_P1.set(++hit);
			this.totalShots_P1.set(++total);
		} else if (player == PLAYER2) {
			total = totalShots_P2.get();
			hit = hitShots_P2.get();
			this.hitShots_P2.set(++hit);
			this.totalShots_P2.set(++total);
		}
	}

	public void setGameNum(int gameNum) {
		this.gameNum.set(gameNum);
	}

	public void setTotalShots_P1(int totalShots) {
		this.totalShots_P1.set(totalShots);
	}

	public void setTotalShots_P2(int totalShots) {
		this.totalShots_P2.set(totalShots);
	}

	public void setHitShots_P1(int hitShots) {
		this.hitShots_P1.set(hitShots);
	}

	public void setHitShots_P2(int hitShots) {
		this.hitShots_P2.set(hitShots);
	}

	public void setScore_P1(int score) {
		this.score_P1.set(score);
	}

	public void setScore_P2(int score) {
		this.score_P2.set(score);
	}

	public void setStatus(String status) {
		this.status.set(status);
	}

	public Integer getGameNum() {
		return gameNum.get();
	}

	public Integer getTotalShots(int player) {
		if (player == PLAYER1)
			return totalShots_P1.get();
		else
			return totalShots_P2.get();
	}

	public Integer getTotalShots_P1() {
		return totalShots_P1.get();
	}

	public Integer getTotalShots_P2() {
		return totalShots_P2.get();
	}

	public Integer getHitShots(int player) {
		if (player == PLAYER1)
			return hitShots_P1.get();
		else
			return hitShots_P2.get();
	}

	public Integer getHitShots_P1() {
		return hitShots_P1.get();
	}

	public Integer getHitShots_P2() {
		return hitShots_P2.get();
	}

	public Integer getScore_P1() {
		return score_P1.get();
	}

	public Integer getScore_P2() {
		return score_P2.get();
	}

	public String getStatus() {
		return status.get();
	}

}