
import java.rmi.Remote;
import java.awt.*;
import java.net.*;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
	
	 public boolean userPlaying() throws RemoteException; //bool to check if user is playing or not (press s)
	 
	 public void setDimensions(int w, int h) throws RemoteException;	 // Takes with width and height and uses it for scaleing.
	 
	 public void setPlayingState(boolean play) throws RemoteException;
	
	 public void checkScore() throws RemoteException; //Periodically with game ticks will check if current score is higher than high score.
	 public int getUserID() throws RemoteException;//Get the user's ID
	 
	 public int getHighScore() throws RemoteException; //returns High score
	 
	 public int getScore() throws RemoteException; //Returns score (sc\ore is shared between both parties)
	 
	 public int getLivesLeft() throws RemoteException; //Returns toal lives (they are simultaneous - not enough time to code each in)
	 
	 public int[] getExplosionCount() throws RemoteException; // Get the current value of Explosion Count
	 
	 public int getHyperspaceCounter(int userID) throws RemoteException; //get Hyperspace value
	 
	 public AsteroidsSprite getShip(int userID) throws RemoteException; //Find out which user is playing the game
	 
	 public AsteroidsSprite[] getPhotons(int userID) throws RemoteException; //Get the value of the current user's photons
	 
	 public AsteroidsSprite[] getAsteroids() throws RemoteException; //Return an array of asteroids
	 
	 public int initialiseScore() throws RemoteException; //initialise the score
	 
	 public int initialiseMaxLivesLeft() throws RemoteException; // initalises maxNumberofLives
	 public AsteroidsSprite[] initialiseExplosions() throws RemoteException; // initalise explosions
	 
	 public AsteroidsSprite initialiseFirstShipShape(int userID) throws RemoteException; //initialise shape for ship 1
	 
	 public AsteroidsSprite initialiseSecondShipShape(int userID) throws RemoteException; //initalise data for ship 2
	 
	 public AsteroidsSprite[] updateExplosions() throws RemoteException; // updates and redraws explosions
	 
	 public AsteroidsSprite[] updateAsteroidPosition() throws RemoteException; // update asteroid so they are shown on screen
	 
	 public AsteroidsSprite[] updatePhotonPositions(int userID)  throws RemoteException; // Update Photon Positions 
	 
	 public AsteroidsSprite[] initialiseOnePhotons(int userID) throws RemoteException; //initialise the 1st user's photon shapes
	 
	 public AsteroidsSprite[] initaliseutwoPhotons(int userID) throws RemoteException; //initialise the 2nd user's photon shapes

	 public AsteroidsSprite[] initialiseFirstExplosionBatch() throws RemoteException; //Initialise and return an array of explosion (AsteroidsSprite) objects 
	 
	 public int[] initialiseExplosionCounter() throws RemoteException; //Initialise the Explosion Counter
	 
	 public AsteroidsSprite[] initialiseAsteroids() throws RemoteException; //Initialise and return an array of Asteroid (AsteroidsSprite) objects
	 
	 public AsteroidsSprite[] initialiseFirstAsteroidBatch() throws RemoteException; //Initialise and return an array of the first set of asteroid (AsteroidsSprite) objects
	 
	 public void initialiseSmallAsteroids(int n) throws RemoteException; // returns an array of smaller asteroids
	 

	 
	 
	 public AsteroidsSprite updateShipPosition(int userID, boolean left, boolean right, boolean up, boolean down, boolean playing) throws RemoteException; //Update the ship's position as it moves through the map
	 
	 public void firePhotons(int userID) throws RemoteException; // fire photons and displays it to all users
	 
	 public void stopShipCol(AsteroidsSprite ship) throws RemoteException; // stalls the ship to a half to allow server to process what to do next - normally done when ship collides
	 
	 public AsteroidsSprite[] explodeOnCollision(AsteroidsSprite s) throws RemoteException; //Function to show the user sprites that have collided
	 	 

}
