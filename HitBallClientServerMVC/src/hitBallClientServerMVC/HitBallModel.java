package hitBallClientServerMVC;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;

public class HitBallModel implements BalloonEvents {
	protected final static int BALLOON_MIN_SPEED = 100;
	protected final static int BALLOON_MAX_SPEED = 1000;
	protected final static int BALLOON_MIN_RADIUS = 1;
	protected final static int BALLOON_MAX_RADIUS = 50;
	protected final static double BALOON_STROKE_WIDTH = 0.7;

	private int balloonRefreshRateInterval = 500;
	private int balloonRadius = 10;
	private double strokeWidth = BALOON_STROKE_WIDTH;
	private Color fill = Color.TRANSPARENT;
	private Color stroke = Color.BLACK;
	private Map<eventType, ArrayList<ActionListener>> actionListenerHashMap = new HashMap<eventType, ArrayList<ActionListener>>();

	public void setBalloonRefreshRate(int miliseconds) {
		if (miliseconds >= BALLOON_MIN_SPEED && miliseconds <= BALLOON_MAX_SPEED) {
			this.balloonRefreshRateInterval = miliseconds;
			processEvent(eventType.REFRESH_RATE,
					new ActionEvent(this, ActionEvent.ACTION_PERFORMED, eventType.REFRESH_RATE.toString()));
		}
	}

	public int getBalloonRefreshRate() {
		return this.balloonRefreshRateInterval;
	}

	public void setBalloonRadius(int radius) {
		if (radius >= BALLOON_MIN_RADIUS && radius <= BALLOON_MAX_RADIUS) {
			this.balloonRadius = radius;
			processEvent(eventType.RADIUS,
					new ActionEvent(this, ActionEvent.ACTION_PERFORMED, eventType.RADIUS.toString()));
		}
	}

	public int getBalloonRadius() {
		return this.balloonRadius;
	}

	public void setStrokeWidth(double w) {
		this.strokeWidth = w;
		processEvent(eventType.STROKEWIDTH,
				new ActionEvent(this, ActionEvent.ACTION_PERFORMED, eventType.STROKEWIDTH.toString()));
	}

	public double getStrokeWidth() {
		return strokeWidth;
	}

	public void setFill(Color c) {
		this.fill = c;
		processEvent(eventType.FILL, new ActionEvent(this, ActionEvent.ACTION_PERFORMED, eventType.FILL.toString()));
	}

	public Color getFill() {
		return fill;
	}

	public void setStroke(Color c) {
		this.stroke = c;
		processEvent(eventType.STROKE,
				new ActionEvent(this, ActionEvent.ACTION_PERFORMED, eventType.STROKE.toString()));
	}

	public Color getStroke() {
		return stroke;
	}

	/** Register an action event listener */
	public synchronized void addActionListener(ActionListener actListen, eventType event_Type) {
		ArrayList<ActionListener> arr;
		arr = actionListenerHashMap.get(event_Type);
		if (arr == null)
			arr = new ArrayList<ActionListener>();
		arr.add(actListen);
		actionListenerHashMap.put(event_Type, arr);
	}

	/** Remove an action event listener */
	public synchronized void removeActionListener(ActionListener actListen, eventType event_Type) {
		ArrayList<ActionListener> al;
		al = actionListenerHashMap.get(event_Type);
		if (al != null && al.contains(actListen))
			al.remove(actListen);
		actionListenerHashMap.put(event_Type, al);
	}

	private void processEvent(eventType event_Type, ActionEvent e) {
		ArrayList<ActionListener> actListen;
		synchronized (this) {
			actListen = actionListenerHashMap.get(event_Type);
			if (actListen == null)
				return;
		}

		for (int i = 0; i < actListen.size(); i++) {
			ActionListener listener = (ActionListener) actListen.get(i);
			listener.actionPerformed(e);
		}
	}
}
