/************************************************************************************************

Asteroids.java

  Usage:

  <applet code="Asteroids.class" width=w height=h></applet>

  Keyboard Controls:

  S            - Start Game    P           - Pause Game
  Cursor Left  - Rotate Left   Cursor Up   - Fire Thrusters
  Cursor Right - Rotate Right  Cursor Down - Fire Retro Thrusters
  Spacebar     - Fire Cannon   H           - Hyperspace
  M            - Toggle Sound  D           - Toggle Graphics Detail

************************************************************************************************/

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


/************************************************************************************************
 * Main applet code.
 ************************************************************************************************/

public class Asteroids extends Applet implements Runnable {

	// Thread control variables.
	Thread loadThread; //Thread for loading processes
	Thread loopThread; //Thread for loop processes
    static RMIInterface look_up;

    // Constants

	static final int DELAY = 50; // Milliseconds between screen updates.
	static final long A_DELAY = 10;
	
	static final int MAX_SHIPS = 3; // Starting number of ships per game.

	static final int MAX_SHOTS = 6; // Maximum number of sprites for photons,
	static final int MAX_ROCKS = 8; // asteroids and explosions.
	static final int MAX_SCRAP = 20;

	static final int SCRAP_COUNT = 30; // Counter starting values.
	static final int HYPER_COUNT = 60;
	static final int STORM_PAUSE = 30; //30 milliseconds
	////////// ROCK PROPERTIES //////////
	static final int MIN_ROCK_SIDES = 8; // Asteroid shape and size ranges.
	static final int MAX_ROCK_SIDES = 12; // Maximum amount of sides that a polygon asteroid can have
	static final int MIN_ROCK_SIZE = 20; // Minimum size of the asteroid generated
	static final int MAX_ROCK_SIZE = 40; // Maximum size of the asteroid generated
	static final int MIN_ROCK_SPEED = 2; // Minimum rock speed
	static final int MAX_ROCK_SPEED = 12; // Maximum rock speed

	static final int BIG_POINTS = 25; // Points for shooting different big objects (asteroids)
	static final int SMALL_POINTS = 50; //Points for shooting different small objects (asteroids)

	static final int NEW_SHIP_POINTS = 5000; // Number of points needed to earn a new ship.

	// Background stars.
	int numStars; //Number of stars
	Point[] stars; 

	// Game data.
	int score;
	int highScore;
	int newShipScore;

	boolean loaded = false;
	boolean paused;
	boolean playing;
	boolean sound;
	boolean detail;
	
	// Key flags.

	boolean left = false;
	boolean right = false;
	boolean up = false;
	boolean down = false;


	// Sprite objects.

	AsteroidsSprite firstPlayer;
	AsteroidsSprite secondPlayer;
	AsteroidsSprite[] userOnePhotons = new AsteroidsSprite[MAX_SHOTS];
	AsteroidsSprite[] userTwoPhotons = new AsteroidsSprite[MAX_SHOTS];

	AsteroidsSprite[] asteroids = new AsteroidsSprite[MAX_ROCKS];
	AsteroidsSprite[] explosions = new AsteroidsSprite[MAX_SCRAP];

	// Ship data.

	int livesLeft; // Number of ships left to play, including current one.
	int shipCounter; // Time counter for ship explosion.
	int hyperspaceCounter; // Time counter for hyperspace.
	int hyperspaceCounter2; // Time counter for hyperspace.
	
	int userID;

	// Photon data.

	int[] photonCounter = new int[MAX_SHOTS]; // Time counter for life of a photon.
	int photonIndex; // Next available photon sprite.

	// Asteroid data.

	boolean[] asteroidIsSmall = new boolean[MAX_ROCKS]; // Asteroid size flag.
	int asteroidsCounter; // Break-time counter.
	int asteroidsSpeed; // Asteroid speed.
	int asteroidsLeft; // Number of active asteroids.

	// Explosion data.

	int[] explosionCounter = new int[MAX_SCRAP]; // Time counters for explosions.
	int explosionIndex; // Next available explosion sprite.

	// Sound clips.

	AudioClip crashSound;
	AudioClip explosionSound;
	AudioClip fireSound;
	AudioClip thrustersSound;
	AudioClip warpSound;

	// Flags for looping sound clips.

	boolean thrustersPlaying;

	// Values for the offscreen image.

	Dimension offDimension;
	Image offImage;
	Graphics offGraphics;

	// Font data.

	Font font = new Font("Helvetica", Font.BOLD, 12);
	FontMetrics fm;
	int fontWidth;
	int fontHeight;

	// Applet information.

	public String getAppletInfo() {

		return ("Asteroids, Copyright 1998 by Mike Hall.");
	}
	
	//Initialise the Applet
	public void init() {
        try{
        	// Have applet and Localhost bounded together so they are able to communicate
            Registry registry = LocateRegistry.getRegistry("localhost"); 
    		look_up = (RMIInterface) registry.lookup("MyServer");

            
            //Find the player's ID which can either be 1 or 2. if there is 1 player, it will be 1. Otherwise it will be 2
            userID = look_up.getUserID();
            
    		Graphics g;
    		Dimension d;
    		int i;

    		// Take credit.

    		System.out.println("Asteroids, Copyright 1998 by Mike Hall.");

    		// Find the size of the screen and set the values for sprites.

    		g = getGraphics();
    		d = size();
    		
    		//lookup the method which sets the game graphics (located on the RMIServer (Asteroid Server)
    		look_up.setDimensions(d.width, d.height);
    		
    		AsteroidsSprite.width = d.width;
    		AsteroidsSprite.height = d.height;

    		// Generate starry background.

    		numStars = AsteroidsSprite.width * AsteroidsSprite.height / 5000;
    		stars = new Point[numStars];
    		for (i = 0; i < numStars; i++)
    			stars[i] = new Point((int) (Math.random() * AsteroidsSprite.width),
    					(int) (Math.random() * AsteroidsSprite.height));

    		// Create shape for the ship sprite.
    		
    		firstPlayer = look_up.initialiseFirstShipShape(userID);
    		
    		// Create shape for the photon sprites.
    		
    		userOnePhotons = look_up.initialiseOnePhotons(userID);

    		// Create asteroid sprites.
    		
    		asteroids = look_up.initialiseFirstAsteroidBatch();

    		// Create explosion sprites.

    		explosions = look_up.initialiseFirstExplosionBatch();
    		// Set font data.

    		g.setFont(font);
    		fm = g.getFontMetrics();
    		fontWidth = fm.getMaxAdvance();
    		fontHeight = fm.getHeight();

    		// Initialize game data and put us in 'game over' mode.

    		highScore = 0;
    		//highScore = look_up.initHighScore();
    		sound = true;
    		detail = true;
    		initialiseGame();
    		endGame();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
	}

	public void initialiseGame() {

		// Initialise game data and sprites.
		try {
			score = look_up.initialiseScore();
			//score = look_up.initScore();
			
			livesLeft = look_up.initialiseMaxLivesLeft();
			//shipsLeft = look_up.getMaxShips();
			firstPlayer = look_up.initialiseFirstShipShape(userID);
		if (loaded)
			thrustersSound.stop();
		thrustersPlaying = false;
		hyperspaceCounter = 0;
		
		
		userOnePhotons = look_up.initialiseOnePhotons(userID);
		
		if (loaded)
		
		asteroids = look_up.initialiseAsteroids();
		explosions = look_up.initialiseExplosions();
		playing = true;
		try {
			look_up.setPlayingState(playing);
		}catch(Exception h) {}
		paused = false;
		}catch (Exception e) {
			
		}
	}

	public void endGame() {

		playing = false;
		try {
			look_up.setPlayingState(playing);
		}catch(Exception x) {}
		
		firstPlayer.active = false;
		if(secondPlayer!=null) {
			secondPlayer.active = false;
		}
		
		if (loaded)
			thrustersSound.stop();
		thrustersPlaying = false;

	}

	public void start() {

		if (loopThread == null) {
			loopThread = new Thread(this);
			loopThread.start();
		}
		if (!loaded && loadThread == null) {
			loadThread = new Thread(this);
			loadThread.start();
		}
	}

	public void stop() {

		if (loopThread != null) {
			loopThread.stop();
			loopThread = null;
		}
		if (loadThread != null) {
			loadThread.stop();
			loadThread = null;
		}
	}

	public void run() {

		int i, j;
		long startTime;

		// Lower this thread's priority and get the current time.

		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		startTime = System.currentTimeMillis();

		// Run thread for loading sounds.

		if (!loaded && Thread.currentThread() == loadThread) {
			loadSounds();
			loaded = true;
			loadThread.stop();
		}
		

		// This is the main loop.

		while (Thread.currentThread() == loopThread) {

			if (!paused) {

				// Move and process all sprites.
				try {
					loopThread.sleep(A_DELAY);

					firstPlayer = look_up.updateShipPosition(userID, left, right, up, down, playing);
					userOnePhotons = look_up.updatePhotonPositions(userID);
					asteroids = look_up.updateAsteroidPosition();

					explosions = look_up.updateExplosions();
					explosionCounter = look_up.getExplosionCount();
					
					look_up.checkScore();
					asteroids = look_up.getAsteroids();
					highScore = look_up.getHighScore();
					score = look_up.getScore();
					livesLeft = look_up.getLivesLeft();
					if(livesLeft <= 0) {
						endGame();
					}
					firstPlayer = look_up.getShip(userID);
					hyperspaceCounter = look_up.getHyperspaceCounter(userID);
					secondPlayer = look_up.getShip(userID==1?2:1);
					hyperspaceCounter2 = look_up.getHyperspaceCounter(userID==1?2:1);
					userOnePhotons = look_up.getPhotons(userID);
					userTwoPhotons = look_up.getPhotons(userID==1?2:1);
					//playing = look_up.getPlaying();
				} catch(RemoteException e) {
					System.out.println(e.getMessage());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			// Update the screen and set the timer for the next loop.

			repaint();
			try {
				startTime += DELAY;
				Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public void loadSounds() {

		// Load all sound clips by playing and immediately stopping them.

		try {
			crashSound = getAudioClip(new URL(getDocumentBase(), "crash.au"));
			explosionSound = getAudioClip(new URL(getDocumentBase(), "explosion.au"));
			fireSound = getAudioClip(new URL(getDocumentBase(), "fire.au"));
			thrustersSound = getAudioClip(new URL(getDocumentBase(), "thrusters.au"));
			warpSound = getAudioClip(new URL(getDocumentBase(), "warp.au"));
		} catch (MalformedURLException e) {
		}

		crashSound.play();
		crashSound.stop();
		explosionSound.play();
		explosionSound.stop();
		fireSound.play();
		fireSound.stop();
		thrustersSound.play();
		thrustersSound.stop();
		warpSound.play();
		warpSound.stop();
	}
	
	public boolean keyDown(Event e, int key){

		// Check if any cursor keys have been pressed and set flags.

		if (key == Event.LEFT)
			left = true;
		if (key == Event.RIGHT)
			right = true;
		if (key == Event.UP)
			up = true;
		if (key == Event.DOWN)
			down = true;

		if ((up || down) && firstPlayer.active && !thrustersPlaying) {
			if (sound && !paused)
				thrustersSound.loop();
			thrustersPlaying = true;
		}

		// Spacebar: fire a photon and start its counter.

		if (key == 32 && firstPlayer.active) {
			if (sound & !paused)
				fireSound.play();
			try {
				look_up.firePhotons(userID);
			} catch(Exception h) {}
		}

		// 'H' key: warp ship into hyperspace by moving to a random location and
		// starting counter.

		if (key == 104 && firstPlayer.active && hyperspaceCounter <= 0) {
			firstPlayer.currentX = Math.random() * AsteroidsSprite.width;
			firstPlayer.currentX = Math.random() * AsteroidsSprite.height;
			hyperspaceCounter = HYPER_COUNT;
			if (sound & !paused)
				warpSound.play();
			
		}

		// 'P' key: toggle pause mode and start or stop any active looping sound clips.

		if (key == 112) {
			if (paused) {
				if (sound && thrustersPlaying)
					thrustersSound.loop();
			} else {
				if (thrustersPlaying)
					thrustersSound.stop();
			}
			paused = !paused;
			
		}

		// 'M' key: toggle sound on or off and stop any looping sound clips.

		if (key == 109 && loaded) {
			if (sound) {
				crashSound.stop();
				explosionSound.stop();
				fireSound.stop();
				thrustersSound.stop();
				warpSound.stop();
			} else {
				if (thrustersPlaying && !paused)
					thrustersSound.loop();
			}
			sound = !sound;
		}

		// 'D' key: toggle graphics detail on or off.

		if (key == 100)
			detail = !detail;

		// 'S' key: start the game, if not already in progress.

		if (key == 115 && loaded && !playing) {
			initialiseGame();
		}
		return true;
	}
	
	public boolean keyUp(Event e, int key) {

		// Check if any cursor keys where released and set flags.

		if (key == Event.LEFT)
			left = false;
		if (key == Event.RIGHT)
			right = false;
		if (key == Event.UP)
			up = false;
		if (key == Event.DOWN)
			down = false;

		if (!up && !down && thrustersPlaying) {
			thrustersSound.stop();
			thrustersPlaying = false;
		}

		return true;
	}

	public void paint(Graphics g) {

		update(g);
	}

	public void update(Graphics g) {

		Dimension d = size();
		int i;
		int c;
		String s;

		// Create the offscreen graphics context, if no good one exists.

		if (offGraphics == null || d.width != offDimension.width || d.height != offDimension.height) {
			offDimension = d;
			offImage = createImage(d.width, d.height);
			offGraphics = offImage.getGraphics();
		}

		// Fill in background and stars.

		offGraphics.setColor(Color.black);
		offGraphics.fillRect(0, 0, d.width, d.height);
		if (detail) {
			offGraphics.setColor(Color.white);
			for (i = 0; i < numStars; i++)
				offGraphics.drawLine(stars[i].x, stars[i].y, stars[i].x, stars[i].y);
		}

		// Draw photon bullets.

		offGraphics.setColor(Color.white);
		for (i = 0; i < MAX_SHOTS; i++) {
			if (userOnePhotons[i].active)
				offGraphics.drawPolygon(userOnePhotons[i].sprite);
			
			if(userTwoPhotons[0]!=null)
				if (userTwoPhotons[i].active)
					offGraphics.drawPolygon(userTwoPhotons[i].sprite);
			
		}

		// Draw the asteroids.

		for (i = 0; i < MAX_ROCKS; i++)
			if (asteroids[i].active) {
				if (detail) {
					offGraphics.setColor(Color.black);
					offGraphics.fillPolygon(asteroids[i].sprite);
				}
				offGraphics.setColor(Color.white);
				offGraphics.drawPolygon(asteroids[i].sprite);
				offGraphics.drawLine(asteroids[i].sprite.xpoints[asteroids[i].sprite.npoints - 1],
						asteroids[i].sprite.ypoints[asteroids[i].sprite.npoints - 1], asteroids[i].sprite.xpoints[0],
						asteroids[i].sprite.ypoints[0]);
			}


		// Draw the ship, counter is used to fade color to white on hyperspace.

		c = 255 - (255 / HYPER_COUNT) * hyperspaceCounter;
		if (firstPlayer.active) {
			if (detail && hyperspaceCounter == 0) {
				offGraphics.setColor(Color.black);
				offGraphics.fillPolygon(firstPlayer.sprite);
			}
			offGraphics.setColor(new Color(c, c, c));
			offGraphics.drawPolygon(firstPlayer.sprite);
			offGraphics.drawLine(firstPlayer.sprite.xpoints[firstPlayer.sprite.npoints - 1],
					firstPlayer.sprite.ypoints[firstPlayer.sprite.npoints - 1], firstPlayer.sprite.xpoints[0], firstPlayer.sprite.ypoints[0]);
		}

		if(secondPlayer!=null) {
			c = 255 - (255 / HYPER_COUNT) * hyperspaceCounter2;
			if (secondPlayer.active) {
				if (detail && hyperspaceCounter2 == 0) {
					offGraphics.setColor(Color.black);
					offGraphics.fillPolygon(secondPlayer.sprite);
				}
				offGraphics.setColor(new Color(c, c, c));
				offGraphics.drawPolygon(secondPlayer.sprite);
				offGraphics.drawLine(secondPlayer.sprite.xpoints[secondPlayer.sprite.npoints - 1],
						secondPlayer.sprite.ypoints[secondPlayer.sprite.npoints - 1], secondPlayer.sprite.xpoints[0], secondPlayer.sprite.ypoints[0]);
			}
		}

		// Draw any explosion debris, counters are used to fade color to black.

		for (i = 0; i < MAX_SCRAP; i++)
			if (explosions[i].active) {
				c = (255 / SCRAP_COUNT) * explosionCounter[i];
				offGraphics.setColor(new Color(c, c, c));
				offGraphics.drawPolygon(explosions[i].sprite);
			}

		// Display status and messages.

		offGraphics.setFont(font);
		offGraphics.setColor(Color.white);

		offGraphics.drawString("Score: " + score, fontWidth, fontHeight);
		offGraphics.drawString("Ships: " + livesLeft, fontWidth, d.height - fontHeight);
		s = "High: " + highScore;
		offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), fontHeight);
		if (!sound) {
			s = "Mute";
			offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), d.height - fontHeight);
		}
		if (!playing) {
			s = "A S T E R O I D S";
			offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2);
			s = "Copyright 1998 by Mike Hall";
			offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight);
			if (!loaded) {
				s = "Loading sounds...";
				offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
			} else {
				s = "Game Over";
				offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
				s = "'S' to Start";
				offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + fontHeight);
			}
		} else if (paused) {
			s = "Game Paused";
			offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
		}

		// Copy the off screen buffer to the screen.

		g.drawImage(offImage, 0, 0, this);
	}
}
