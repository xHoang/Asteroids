/************************************************************************************************
 * The AsteroidsSprite class defines a game object, including it's shape,
 * position, movement and rotation. It also can detemine if two objects collide.
 ************************************************************************************************/

import java.awt.*;
import java.net.*;
import java.util.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;

public class AsteroidsSprite implements Serializable {

	// Fields:

	private static final long serialVersionUID = 7895875246648014114L;
	static int width; // Dimensions of the graphics area.
	static int height;

	Polygon shape; // Initial sprite shape, centered at the origin (0,0).
	boolean active; // Active flag.
	double angle; // Current angle of rotation.
	double deltaAngle; // Amount to change the rotation angle.
	double currentX, currentY; // Current position on screen.
	double deltaX, deltaY; // Amount to change the screen position.
	Polygon sprite; // Final location and shape of sprite after applying rotation and
					// moving to screen position. Used for drawing on the screen and
					// in detecting collisions.

	// Constructors:

	public AsteroidsSprite() throws RemoteException {

		this.shape = new Polygon();
		this.active = false;
		this.angle = 0.0;
		this.deltaAngle = 0.0;
		this.currentX = 0.0;
		this.currentY = 0.0;
		this.deltaX = 0.0;
		this.deltaY = 0.0;
		this.sprite = new Polygon();
	}

	// Methods:

	public void advance() {

		// Update the rotation and position of the sprite based on the delta values. If
		// the sprite
		// moves off the edge of the screen, it is wrapped around to the other side.

		this.angle += this.deltaAngle;
		if (this.angle < 0)
			this.angle += 2 * Math.PI;
		if (this.angle > 2 * Math.PI)
			this.angle -= 2 * Math.PI;
		this.currentX += this.deltaX;
		if (this.currentX < -width / 2)
			this.currentX += width;
		if (this.currentX > width / 2)
			this.currentX -= width;
		this.currentY -= this.deltaY;
		if (this.currentY < -height / 2)
			this.currentY += height;
		if (this.currentY > height / 2)
			this.currentY -= height;
	}

	public void render() {

		int i;

		// Render the sprite's shape and location by rotating it's base shape and moving
		// it to
		// it's proper screen position.

		this.sprite = new Polygon();
		for (i = 0; i < this.shape.npoints; i++)
			this.sprite.addPoint(
					(int) Math.round(
							this.shape.xpoints[i] * Math.cos(this.angle) + this.shape.ypoints[i] * Math.sin(this.angle))
							+ (int) Math.round(this.currentX) + width / 2,
					(int) Math.round(
							this.shape.ypoints[i] * Math.cos(this.angle) - this.shape.xpoints[i] * Math.sin(this.angle))
							+ (int) Math.round(this.currentY) + height / 2);
	}

	public boolean isColliding(AsteroidsSprite s) {

		int i;

		// Determine if one sprite overlaps with another, i.e., if any vertice
		// of one sprite lands inside the other.

		for (i = 0; i < s.sprite.npoints; i++)
			if (this.sprite.inside(s.sprite.xpoints[i], s.sprite.ypoints[i]))
				return true;
		for (i = 0; i < this.sprite.npoints; i++)
			if (s.sprite.inside(this.sprite.xpoints[i], this.sprite.ypoints[i]))
				return true;
		return false;
	}
}
