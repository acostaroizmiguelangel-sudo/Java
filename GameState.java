import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
    
    private static final long serialVersionUID = 1L; 

    int playerRow;
    int playerCol;
    int playerLives;
    int playerEnergy;
    int stepsTakenState;
    
    int currentLevelState;
    int gameTimerState;

    int[][] mazeState;
    boolean[][] visitedState; 
    
    ArrayList<String> inventoryState;
}