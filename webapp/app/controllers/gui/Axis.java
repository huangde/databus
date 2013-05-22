package controllers.gui;

import play.templates.JavaExtensions;

public class Axis {

	private String name;
	private String units;
	private String color;
	private boolean opposite;
	
	public String getName() {
		return JavaExtensions.escapeJavaScript(name);
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUnits() {
		return JavaExtensions.escapeJavaScript(units);
	}
	public void setUnits(String units) {
		this.units = units;
	}
	public String getColor() {
		return JavaExtensions.escapeJavaScript(color);
	}
	public void setColor(String color) {
		this.color = color;
	}
	public void setOpposite(boolean b) {
		this.opposite = b;
	}
	public boolean isOpposite() {
		return opposite;
	}
}
