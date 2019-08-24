import java.awt.Event;
import java.awt.Polygon;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer extends UnicastRemoteObject implements RMIInterface{

    private static final long serialVersionUID = 1L;

	// Constants

	static final int DELAY = 50; // Milliseconds between screen updates.
	static final int MAX_SHIPS = 3; // Starting number of ships per game.
	static final int MAX_SHOTS = 6; // Maximum number of sprites for photons,
	static final int MAX_ROCKS = 8; // asteroids and explosions.
	static final int MAX_SCRAP = 20;
	static final int SCRAP_COUNT = 30; // Counter starting values.
	static final int HYPER_COUNT = 60;
	static final int STORM_PAUSE = 30;
	static final int MIN_ROCK_SIDES = 8; // Asteroid shape and size ranges.
	static final int MAX_ROCK_SIDES = 12;
	static final int MIN_ROCK_SIZE = 20;
	static final int MAX_ROCK_SIZE = 40;
	static final int MIN_ROCK_SPEED = 2;
	static final int MAX_ROCK_SPEED = 12;
	static final int BIG_POINTS = 25; // Points for shooting different objects.
	static final int SMALL_POINTS = 50;
	static final int NEW_SHIP_POINTS = 5000; // Number of points needed to earn a new ship.

	
	// Game data.
	int score;
	int highScore;
	int newShipScore;
	boolean paused;
	boolean playing;


	// Sprite objects.
	AsteroidsSprite userOne;
	AsteroidsSprite userTwo;
	AsteroidsSprite[] asteroids = new AsteroidsSprite[MAX_ROCKS];
	AsteroidsSprite[] explosions = new AsteroidsSprite[MAX_SCRAP];
	AsteroidsSprite[] userOnePhotons = new AsteroidsSprite[MAX_SHOTS];
	AsteroidsSprite[] userTwoPhotons = new AsteroidsSprite[MAX_SHOTS];
	// Ship data.

	int livesLeft; // Number of ships left to play, including current one.
	int shipCounter; // Time counter for ship explosion.
	int uoneHyperspaceCounter; // Time counter for hyperspace.
	int utwoHyperspaceCounter; // Time counter for hyperspace.
	
	// Photon data.
	int[] uonePhontonCounter = new int[MAX_SHOTS]; // Time counter for life of a photon.
	int photonIndex1; // Next available photon sprite.
	int[] utwoPhontonCounter = new int[MAX_SHOTS]; // Time counter for life of a photon.
	int photonIndex2; // Next available photon sprite.
	
	// Asteroid data.
	boolean[] smallAsteroid = new boolean[MAX_ROCKS]; // Asteroid size flag.
	int asteroidsCount; // Break-time counter.
	int asteroidsSpeed; // Asteroid speed.
	int asteroidsLeft; // Number of active asteroids.

	// Explosion data.
	int[] explosionCount = new int[MAX_SCRAP]; // Time counters for explosions.
	int explosionIndex; // Next available explosion sprite.


    protected RMIServer() throws RemoteException {

        super();

    }
    
    @Override
    public void checkScore() throws RemoteException {
    	if (score > highScore)
			highScore = score;
		if (score > newShipScore) {
			newShipScore += NEW_SHIP_POINTS;
			livesLeft++;
		}

		// If all asteroids have been destroyed create a new batch.

		if (asteroidsLeft <= 0) {
			if (--asteroidsCount <= 0) {
				try {
					initialiseAsteroids();
				}catch(Exception e) {}
			}
		}
				
    }
    
    @Override
    public int getUserID()  {
    	if(userOne==null) {
    		return 1;
    	} else {
    		return 2;
    	}
    	
    }
    
    @Override
    public int initialiseMaxLivesLeft() throws RemoteException{
    	asteroidsSpeed = MIN_ROCK_SPEED;
		newShipScore = NEW_SHIP_POINTS;
		livesLeft = MAX_SHIPS;
		return livesLeft;
    }
    
    @Override
    public void firePhotons(int userID) throws RemoteException{
    	if(userID == 1) {
    		photonIndex1++;
    		if (photonIndex1 >= MAX_SHOTS)
    			photonIndex1 = 0;
    		userOnePhotons[photonIndex1].active = true;
    		userOnePhotons[photonIndex1].currentX = userOne.currentX;
    		userOnePhotons[photonIndex1].currentY = userOne.currentY;
    		userOnePhotons[photonIndex1].deltaX = MIN_ROCK_SIZE * -Math.sin(userOne.angle);
    		userOnePhotons[photonIndex1].deltaY = MIN_ROCK_SIZE * Math.cos(userOne.angle);
    		uonePhontonCounter[photonIndex1] = Math.min(AsteroidsSprite.width, AsteroidsSprite.height) / MIN_ROCK_SIZE;
    	} else {
    		photonIndex2++;
    		if (photonIndex2 >= MAX_SHOTS)
    			photonIndex2 = 0;
    		userTwoPhotons[photonIndex2].active = true;
    		userTwoPhotons[photonIndex2].currentX = userTwo.currentX;
    		userTwoPhotons[photonIndex2].currentY = userTwo.currentY;
    		userTwoPhotons[photonIndex2].deltaX = MIN_ROCK_SIZE * -Math.sin(userTwo.angle);
    		userTwoPhotons[photonIndex2].deltaY = MIN_ROCK_SIZE * Math.cos(userTwo.angle);
    		utwoPhontonCounter[photonIndex2] = Math.min(AsteroidsSprite.width, AsteroidsSprite.height) / MIN_ROCK_SIZE;		
    	}
    }
    
    @Override
    public int initialiseScore() throws RemoteException{
    	score = 0;
    	return score;
    }
    
    @Override
    public int getHighScore() throws RemoteException {
    	return highScore;
    }
    
    @Override
    public int getScore() throws RemoteException {
    	return score;
    }
    
    @Override
    public int getLivesLeft() throws RemoteException {
    	return livesLeft;
    }
    
    @Override
    public AsteroidsSprite[] getAsteroids() throws RemoteException {
    	return asteroids;
    }
    
    @Override
    public AsteroidsSprite initialiseFirstShipShape(int userID) throws RemoteException {
    	if(userID == 1) {
    		userOne = new AsteroidsSprite();
    		userOne.shape.addPoint(0, -10);
    		userOne.shape.addPoint(7, 10);
    		userOne.shape.addPoint(-7, 10);
    		return userOne;
    	} else {
    		userTwo = new AsteroidsSprite();
    		userTwo.shape.addPoint(0, -10);
    		userTwo.shape.addPoint(7, 10);
    		userTwo.shape.addPoint(-7, 10);
    		return userTwo;
    	}
    }
    
    @Override
    public AsteroidsSprite[] initialiseOnePhotons(int userID) throws RemoteException {
    	if(userID == 1) {
    		for (int i = 0; i < MAX_SHOTS; i++) {
    			userOnePhotons[i] = new AsteroidsSprite();
    			userOnePhotons[i].shape.addPoint(1, 1);
    			userOnePhotons[i].shape.addPoint(1, -1);
    			userOnePhotons[i].shape.addPoint(-1, 1);
    			userOnePhotons[i].shape.addPoint(-1, -1);
    		}
    		return userOnePhotons;
    	} else {
    		for (int i = 0; i < MAX_SHOTS; i++) {
    			userTwoPhotons[i] = new AsteroidsSprite();
    			userTwoPhotons[i].shape.addPoint(1, 1);
    			userTwoPhotons[i].shape.addPoint(1, -1);
    			userTwoPhotons[i].shape.addPoint(-1, 1);
    			userTwoPhotons[i].shape.addPoint(-1, -1);
    		}
    		return userTwoPhotons;

    	}
    }

    @Override
    public AsteroidsSprite[] initialiseFirstAsteroidBatch() throws RemoteException {
    	for (int i = 0; i < MAX_ROCKS; i++)
			asteroids[i] = new AsteroidsSprite();
		return asteroids;
    	
    }
    
    @Override
    public AsteroidsSprite[] initialiseFirstExplosionBatch() throws RemoteException {
    	for (int i = 0; i < MAX_SCRAP; i++)
			explosions[i] = new AsteroidsSprite();
		return explosions;
    }
    
    @Override
    public AsteroidsSprite initialiseSecondShipShape(int userID) throws RemoteException {
    	if(userID==1) {
    		userOne.active = true;
    		userOne.angle = 0.0;
    		userOne.deltaAngle = 0.0;
    		userOne.currentX = 0.0;
    		userOne.currentY = 0.0;
    		userOne.deltaX = 0.0;
    		userOne.deltaY = 0.0;
    		userOne.render();
    		
    		uoneHyperspaceCounter = 0;
    		return userOne;
    	} else {
    		userTwo.active = true;
    		userTwo.angle = 0.0;
    		userTwo.deltaAngle = 0.0;
    		userTwo.currentX = 0.0;
    		userTwo.currentY = 0.0;
    		userTwo.deltaX = 0.0;
    		userTwo.deltaY = 0.0;
    		userTwo.render();
    		
    		utwoHyperspaceCounter = 0;
    		return userTwo;
    	}
	}
    
    @Override
	public AsteroidsSprite updateShipPosition(int userID, boolean left, boolean right, boolean up, boolean down, boolean playing) throws RemoteException {
		AsteroidsSprite shipUx = null;
		int hyperCounterToUpdate = 0;
		if(userID == 1) {
			shipUx = userOne;
			hyperCounterToUpdate = uoneHyperspaceCounter;
		} else {
			shipUx = userTwo;
			hyperCounterToUpdate = utwoHyperspaceCounter;

		}
		double dx, dy, limit;
		

		if (!playing)
			return shipUx;

		// Rotate the ship if left or right cursor key is down.

		if (left) {
			shipUx.angle += Math.PI / 16.0;
			if (shipUx.angle > 2 * Math.PI)
				shipUx.angle -= 2 * Math.PI;
		}
		if (right) {
			shipUx.angle -= Math.PI / 16.0;
			if (shipUx.angle < 0)
				shipUx.angle += 2 * Math.PI;
		}

		// Fire thrusters if up or down cursor key is down. Don't let ship go past
		// the speed limit.

		dx = -Math.sin(shipUx.angle);
		dy = Math.cos(shipUx.angle);
		limit = 0.8 * MIN_ROCK_SIZE;
		if (up) {
			if (shipUx.deltaX + dx > -limit && shipUx.deltaX + dx < limit)
				shipUx.deltaX += dx;
			if (shipUx.deltaY + dy > -limit && shipUx.deltaY + dy < limit)
				shipUx.deltaY += dy;
		}
		if (down) {
			if (shipUx.deltaX - dx > -limit && shipUx.deltaX - dx < limit)
				shipUx.deltaX -= dx;
			if (shipUx.deltaY - dy > -limit && shipUx.deltaY - dy < limit)
				shipUx.deltaY -= dy;
		}

		// Move the ship. If it is currently in hyperspace, advance the countdown.
		System.out.println("ShipCounter: "+shipCounter);
		System.out.println("livesLeft: "+livesLeft);
		if (shipUx.active) {
			shipUx.advance();
			shipUx.render();
			if (hyperCounterToUpdate > 0)
				hyperCounterToUpdate--;
		}

		// Ship is down and has collided. Checks to see if there are any more lives
		// if not end the game
		
		else if (--shipCounter <= 0) {
			if (livesLeft > 0) {
				
				shipUx = initialiseSecondShipShape(userID);
				hyperCounterToUpdate = HYPER_COUNT;
			}
		}
		
		return shipUx;
	}
	
	@Override
	public void stopShipCol(AsteroidsSprite ship) throws RemoteException {
		ship.active = false;
		shipCounter = SCRAP_COUNT;
		if (livesLeft > 0)
			livesLeft--;
		
	}
	
	@Override
	public AsteroidsSprite[] initaliseutwoPhotons(int userID) throws RemoteException {
		if(userID == 1) {
			int i;
	
			for (i = 0; i < MAX_SHOTS; i++) {
				userOnePhotons[i].active = false;
				uonePhontonCounter[i] = 0;
			}
			photonIndex1 = 0;
			return userOnePhotons;
		} else {
			int i;
			
			for (i = 0; i < MAX_SHOTS; i++) {
				userTwoPhotons[i].active = false;
				utwoPhontonCounter[i] = 0;
			}
			photonIndex2 = 0;
			return userTwoPhotons;
		}
		
	}
	
	@Override
	public AsteroidsSprite[] updatePhotonPositions(int userID)  throws RemoteException {
		if(userID == 1){
			int i;
			// Move any active photons. Stop it when its counter has expired.
	
			for (i = 0; i < MAX_SHOTS; i++)
				if (userOnePhotons[i].active) {
					userOnePhotons[i].advance();
					userOnePhotons[i].render();
					if (--uonePhontonCounter[i] < 0)
						userOnePhotons[i].active = false;
				}
			return userOnePhotons;
		} else {
			int i;
			
			// Move any active photons. Stop it when its counter has expired.
	
			for (i = 0; i < MAX_SHOTS; i++)
				if (userTwoPhotons[i].active) {
					userTwoPhotons[i].advance();
					userTwoPhotons[i].render();
					if (--utwoPhontonCounter[i] < 0)
						userTwoPhotons[i].active = false;
				}
			return userTwoPhotons;
		}
	}
    
	@Override
    public AsteroidsSprite[] initialiseAsteroids() throws RemoteException {

		int i, j;
		int s;
		double theta, r;
		int x, y;

		// Create random shapes, positions and movements for each asteroid.

		for (i = 0; i < MAX_ROCKS; i++) {

			// Create a jagged shape for the asteroid and give it a random rotation.

			asteroids[i].shape = new Polygon();
			s = MIN_ROCK_SIDES + (int) (Math.random() * (MAX_ROCK_SIDES - MIN_ROCK_SIDES));
			for (j = 0; j < s; j++) {
				theta = 2 * Math.PI / s * j;
				r = MIN_ROCK_SIZE + (int) (Math.random() * (MAX_ROCK_SIZE - MIN_ROCK_SIZE));
				x = (int) -Math.round(r * Math.sin(theta));
				y = (int) Math.round(r * Math.cos(theta));
				asteroids[i].shape.addPoint(x, y);
			}
			asteroids[i].active = true;
			asteroids[i].angle = 0.0;
			asteroids[i].deltaAngle = (Math.random() - 0.5) / 10;

			// Place the asteroid at one edge of the screen.

			if (Math.random() < 0.5) {
				asteroids[i].currentX = -AsteroidsSprite.width / 2;
				if (Math.random() < 0.5)
					asteroids[i].currentX = AsteroidsSprite.width / 2;
				asteroids[i].currentY = Math.random() * AsteroidsSprite.height;
			} else {
				asteroids[i].currentX = Math.random() * AsteroidsSprite.width;
				asteroids[i].currentY = -AsteroidsSprite.height / 2;
				if (Math.random() < 0.5)
					asteroids[i].currentY = AsteroidsSprite.height / 2;
			}

			// Set a random motion for the asteroid.

			asteroids[i].deltaX = Math.random() * asteroidsSpeed;
			if (Math.random() < 0.5)
				asteroids[i].deltaX = -asteroids[i].deltaX;
			asteroids[i].deltaY = Math.random() * asteroidsSpeed;
			if (Math.random() < 0.5)
				asteroids[i].deltaY = -asteroids[i].deltaY;

			asteroids[i].render();
			smallAsteroid[i] = false;
		}

		asteroidsCount = STORM_PAUSE;
		asteroidsLeft = MAX_ROCKS;
		if (asteroidsSpeed < MAX_ROCK_SPEED)
			asteroidsSpeed++;
		
		return asteroids;
	}
    
    @Override
    public boolean userPlaying() throws RemoteException{
    	return playing;
    }
    
    @Override
	public void initialiseSmallAsteroids(int n) throws RemoteException{

		int count;
		int i, j;
		int s;
		double tempX, tempY;
		double theta, r;
		int x, y;

		// Create smaller asteroids from bigger ones

		count = 0;
		i = 0;
		tempX = asteroids[n].currentX;
		tempY = asteroids[n].currentY;
		do {
			if (!asteroids[i].active) {
				asteroids[i].shape = new Polygon();
				s = MIN_ROCK_SIDES + (int) (Math.random() * (MAX_ROCK_SIDES - MIN_ROCK_SIDES));
				for (j = 0; j < s; j++) {
					theta = 2 * Math.PI / s * j;
					r = (MIN_ROCK_SIZE + (int) (Math.random() * (MAX_ROCK_SIZE - MIN_ROCK_SIZE))) / 2;
					x = (int) -Math.round(r * Math.sin(theta));
					y = (int) Math.round(r * Math.cos(theta));
					asteroids[i].shape.addPoint(x, y);
				}
				asteroids[i].active = true;
				asteroids[i].angle = 0.0;
				asteroids[i].deltaAngle = (Math.random() - 0.5) / 10;
				asteroids[i].currentX = tempX;
				asteroids[i].currentY = tempY;
				asteroids[i].deltaX = Math.random() * 2 * asteroidsSpeed - asteroidsSpeed;
				asteroids[i].deltaY = Math.random() * 2 * asteroidsSpeed - asteroidsSpeed;
				asteroids[i].render();
				smallAsteroid[i] = true;
				count++;
				asteroidsLeft++;
			}
			i++;
		} while (i < MAX_ROCKS && count < 2);
		
	}
	
	@Override
    public void setDimensions(int w, int h) throws RemoteException{
    	AsteroidsSprite.width = w;
		AsteroidsSprite.height = h;
    }
    
    @Override
	public AsteroidsSprite[] updateAsteroidPosition() throws RemoteException {

		int i, j;

		// Move any active asteroids and check for collisions.

		for (i = 0; i < MAX_ROCKS; i++) {
			if (asteroids[i].active) {
				asteroids[i].advance();
				asteroids[i].render();
				// If hit by photon, kill asteroid and advance score. If asteroid is large,
				// make some smaller ones to replace it.

				for (j = 0; j < MAX_SHOTS; j++) {
					if (userOnePhotons[j].active && asteroids[i].active && asteroids[i].isColliding(userOnePhotons[j])) {
						asteroidsLeft--;
						asteroids[i].active = false;
						userOnePhotons[j].active = false;
						explodeOnCollision(asteroids[i]);
						if (!smallAsteroid[i]) {
							score += BIG_POINTS;
							initialiseSmallAsteroids(i);
						} else
							score += SMALL_POINTS;
					}
					if (userTwoPhotons[0]!=null && userTwoPhotons[j].active && asteroids[i].active && asteroids[i].isColliding(userTwoPhotons[j])) {
						asteroidsLeft--;
						asteroids[i].active = false;
						userTwoPhotons[j].active = false;
						explodeOnCollision(asteroids[i]);
						if (!smallAsteroid[i]) {
							score += BIG_POINTS;
							initialiseSmallAsteroids(i);
						} else
							score += SMALL_POINTS;
					}

				}

				// If the ship is not in hyperspace, see if it is hit.
				if (userOne.active && uoneHyperspaceCounter <= 0 && asteroids[i].active && asteroids[i].isColliding(userOne)) {
					explodeOnCollision(userOne);
					stopShipCol(userOne);
				}
				
				if (userTwo!=null && userTwo.active && utwoHyperspaceCounter <= 0 && asteroids[i].active && asteroids[i].isColliding(userTwo)) {
					explodeOnCollision(userTwo);
					stopShipCol(userTwo);
				}
			}
		}
		
		return asteroids;
	}
	
	@Override
	public AsteroidsSprite[] explodeOnCollision(AsteroidsSprite s) throws RemoteException {

		int c, i, j;

		// Create sprites for explosion animation. The each individual line segment of
		// the given sprite
		// is used to create a new sprite that will move outward from the sprite's
		// original position
		// with a random rotation.

		s.render();
		c = 2;
		if (true || s.sprite.npoints < 6)
			c = 1;
		for (i = 0; i < s.sprite.npoints; i += c) {
			explosionIndex++;
			if (explosionIndex >= MAX_SCRAP)
				explosionIndex = 0;
			explosions[explosionIndex].active = true;
			explosions[explosionIndex].shape = new Polygon();
			explosions[explosionIndex].shape.addPoint(s.shape.xpoints[i], s.shape.ypoints[i]);
			j = i + 1;
			if (j >= s.sprite.npoints)
				j -= s.sprite.npoints;
			explosions[explosionIndex].shape.addPoint(s.shape.xpoints[j], s.shape.ypoints[j]);
			explosions[explosionIndex].angle = s.angle;
			explosions[explosionIndex].deltaAngle = (Math.random() * 2 * Math.PI - Math.PI) / 15;
			explosions[explosionIndex].currentX = s.currentX;
			explosions[explosionIndex].currentY = s.currentY;
			explosions[explosionIndex].deltaX = -s.shape.xpoints[i] / 5;
			explosions[explosionIndex].deltaY = -s.shape.ypoints[i] / 5;
			explosionCount[explosionIndex] = SCRAP_COUNT;
		}
		
		return explosions;
	}
	
	@Override
	public void setPlayingState(boolean play) throws RemoteException {
		playing = play;
	}
	
	@Override
	public AsteroidsSprite[] updateExplosions() throws RemoteException {

		int i;

		// Move any active explosion debris. Stop explosion when its counter has
		// expired.

		for (i = 0; i < MAX_SCRAP; i++)
			if (explosions[i].active) {
				explosions[i].advance();
				explosions[i].render();
				if (--explosionCount[i] < 0)
					explosions[i].active = false;
			}
		
		return explosions;
	}
	
	@Override
	public int[] getExplosionCount() throws RemoteException {
		return explosionCount;
	}
	
	@Override
	public AsteroidsSprite[] initialiseExplosions() throws RemoteException{

		for (int i = 0; i < MAX_SCRAP; i++) {
			explosions[i].shape = new Polygon();
			explosions[i].active = false;
		}
		explosionIndex = 0;
		return explosions;
	}
	
	@Override
	public int[] initialiseExplosionCounter() throws RemoteException {
		for (int i = 0; i < MAX_SCRAP; i++) {
			explosionCount[i] = 0;
		}
		return explosionCount;
	}
    
	@Override
    public AsteroidsSprite[] getPhotons(int userID) throws RemoteException{
    	if(userID==1) {
        	return userOnePhotons;
        } else {
        	return userTwoPhotons;
        }

    }
    
    @Override
    public int getHyperspaceCounter(int userID) throws RemoteException{
    	if(userID==1) {
        	return uoneHyperspaceCounter;
        } else {
        	return utwoHyperspaceCounter;
        }

    }
    
    @Override
    public AsteroidsSprite getShip(int userID) throws RemoteException{

        if(userID==1) {
        	return userOne;
        } else {
        	return userTwo;
        }

    }

    public static void main(String[] args){

        try {

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("MyServer", new RMIServer());
            //rebind
            System.err.println("Server ready Hoang :)");

        } catch (Exception e) {

            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();

        }

    }




}
