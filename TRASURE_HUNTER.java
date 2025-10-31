import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
//Primera revision 13/10/25
//Ultima revision 22/10/25
public class TRASURE_HUNTER {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameFrame();
        });
    }
}

class GameFrame extends JFrame {
    private MazePanel mazePanel;

    public GameFrame() {
        setTitle("Treasure Hunter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        mazePanel = new MazePanel(this); 
        add(mazePanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public void showPauseMenuDialog() {
        if (mazePanel.isPaused()) {
            JDialog pauseDialog = createPauseDialog();
            pauseDialog.setVisible(true);
        }
    }

    private JDialog createPauseDialog() {
        JDialog dialog = new JDialog(this, "Menú de Pausa", true);
        dialog.setLayout(new GridLayout(4, 1, 10, 10)); 
        dialog.setPreferredSize(new Dimension(250, 200));
        dialog.setResizable(false);

        JLabel title = new JLabel("Menú de Pausa", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton saveButton = new JButton("Guardar Partida [S]");
        JButton loadButton = new JButton("Cargar Partida [L]");
        JButton exitButton = new JButton("Salir del Juego");

        saveButton.addActionListener(e -> mazePanel.saveGame());
        loadButton.addActionListener(e -> {
            mazePanel.loadGame();
            dialog.dispose();
            mazePanel.togglePause(); 
        });
        exitButton.addActionListener(e -> System.exit(0));

        dialog.add(title);
        dialog.add(saveButton);
        dialog.add(loadButton);
        dialog.add(exitButton);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                mazePanel.togglePause();
            }
        });
        
        return dialog;
    }
}

class MazePanel extends JPanel {
    private final int TILE_SIZE = 32;
    private static final int PATH = 0;
    private static final int WALL = 1;
    private static final int GOAL = 2; 
    private static final int TRAP = 3;
    private static final int ENERGY_PICKUP = 4;
    private static final int LIFE_PICKUP = 5;
    private static final int CHEST = 6;
    private static final int LOCKED_DOOR = 7;

    private final int[][] MAZE_TEMPLATE_1 = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
        {1,0,0,4,1,0,0,0,0,1,3,0,0,0,0,0,0,0,6,1}, 
        {1,0,1,0,1,0,1,1,0,1,1,1,1,1,1,1,1,1,0,1}, 
        {1,0,1,0,0,0,0,1,0,0,0,0,0,0,0,3,5,1,0,1}, 
        {1,0,1,1,1,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1}, 
        {1,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,1,0,1}, 
        {1,1,1,1,0,1,1,1,0,1,0,1,1,1,1,1,0,1,0,1}, 
        {1,3,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,1}, 
        {1,0,5,1,1,1,1,1,0,1,0,1,0,1,1,1,1,1,0,1}, 
        {1,0,0,0,0,0,0,1,0,0,0,1,0,1,0,0,0,0,3,1}, 
        {1,1,1,1,1,1,0,1,1,1,0,1,0,1,0,1,1,1,1,1}, 
        {1,4,0,0,0,1,0,0,0,1,0,1,0,1,0,0,0,0,0,1}, 
        {1,0,1,1,0,1,1,1,0,1,0,1,0,1,1,1,1,1,0,1}, 
        {1,0,0,1,0,0,0,0,0,1,3,1,0,0,0,0,0,0,0,1}, 
        {1,1,0,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,0,1}, 
        {1,0,0,0,0,0,4,1,0,0,0,0,0,0,0,0,0,3,0,1}, 
        {1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1}, 
        {1,5,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1}, 
        {1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,7,0,1}, 
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    private final int[][] MAZE_TEMPLATE_2 = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,4,0,1,3,0,0,0,1,5,0,0,0,1,0,0,6,1},
        {1,1,1,1,0,1,1,1,0,1,1,1,1,1,0,1,0,1,1,1},
        {1,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1,0,5,0,1},
        {1,0,1,1,1,1,1,1,0,1,0,1,0,1,0,1,1,1,0,1},
        {1,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0,1},
        {1,1,1,1,0,1,0,1,1,1,1,1,1,1,3,1,0,1,1,1},
        {1,0,0,0,0,1,0,0,0,1,0,0,0,1,0,1,0,0,0,1},
        {1,4,1,1,1,1,1,1,0,1,0,1,0,1,0,1,1,1,0,1},
        {1,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,1,0,1},
        {1,0,0,0,0,0,0,1,0,0,0,0,5,0,0,0,0,1,0,1},
        {1,0,1,1,1,1,0,1,1,1,1,0,1,1,1,1,0,1,0,1},
        {1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,1,0,0,0,1},
        {1,1,1,0,1,1,1,1,1,0,1,1,1,4,1,1,1,1,0,1},
        {1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,3,0,1},
        {1,0,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,0,1},
        {1,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,7,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    private final int[][] MAZE_TEMPLATE_3 = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,1,3,1,1,1,1,1,1,1,5,0,0,1,1,1,0,0,1},
        {1,0,1,0,0,0,0,0,0,0,1,1,1,0,1,1,1,0,1,1},
        {1,0,1,0,1,1,1,1,1,0,1,1,1,0,1,1,1,0,1,1},
        {1,4,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,0,0,1},
        {1,1,1,0,1,0,1,0,1,1,1,0,1,1,1,1,1,1,0,1},
        {1,0,0,0,1,0,1,0,0,0,1,0,0,0,3,1,0,1,0,1},
        {1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,0,1,0,1},
        {1,4,1,1,1,0,0,0,1,0,1,1,1,0,1,1,0,0,0,1},
        {1,1,1,1,1,1,1,0,1,0,0,0,0,0,1,1,1,1,1,1},
        {1,0,0,0,0,0,1,0,1,1,1,1,1,1,1,0,0,0,4,1},
        {1,0,1,1,1,0,1,0,1,0,0,0,1,0,0,0,1,1,1,1},
        {1,0,1,1,1,0,1,0,1,0,1,0,1,0,1,0,1,0,0,1},
        {1,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,1,0,1,1},
        {1,1,1,1,1,1,1,1,1,0,1,1,1,0,1,0,1,0,5,1},
        {1,0,0,0,1,0,0,0,1,0,1,0,0,0,1,0,0,0,1,1},
        {1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,1,1,1,1,1}, 
        {1,5,1,0,1,0,1,0,1,0,1,0,1,0,0,0,0,0,0,1},
        {1,6,1,0,0,0,1,0,0,0,1,0,1,0,1,1,1,0,7,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    private int playerRow = 1;
    private int playerCol = 1;
    private int playerLives = 3;
    private final int INITIAL_LIVES = 3;
    private int playerEnergy;
    private final int INITIAL_ENERGY = 100;
    private final int MAX_ENERGY = 100;
    
    private int stepsTaken = 0;
    private int currentLevel = 1;
    private boolean isPaused = false; 
    private transient GameFrame gameFrame; 
    
    private final int INITIAL_TIME_PER_LEVEL = 180; 
    private int gameTimer; 
    private javax.swing.Timer gameLoopTimer; 

    private boolean[][] visited;
    private final double VISION_RADIUS = 1.5;

    private ArrayList<String> inventory = new ArrayList<>();

    private BufferedImage imgWall;
    private BufferedImage imgPath;
    private BufferedImage imgGoal;
    private BufferedImage imgTrap;
    private BufferedImage imgEnergy;
    private BufferedImage imgLife;
    private BufferedImage imgPlayer;
    private BufferedImage imgChest;
    private BufferedImage imgLockedDoor;

    private int[][] maze;

    public MazePanel(GameFrame frame) {
        this.gameFrame = frame;

        int panelWidth = MAZE_TEMPLATE_1[0].length * TILE_SIZE;
        int panelHeight = MAZE_TEMPLATE_1.length * TILE_SIZE;
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        
        loadImages();
        setBackground(Color.GRAY);
        
        loadLevel(1); 
        this.playerLives = INITIAL_LIVES;
        this.playerEnergy = INITIAL_ENERGY;
        this.gameTimer = INITIAL_TIME_PER_LEVEL;

        gameLoopTimer = new javax.swing.Timer(1000, e -> updateGameTimer());
        gameLoopTimer.start();

        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    togglePause(); 
                    return; 
                }
                
                if (!isPaused) { 
                    movePlayer(e.getKeyCode());
                }
            }
        });
    }

    public void togglePause() {
        isPaused = !isPaused;
        
        if (isPaused) {
            gameLoopTimer.stop();
            gameFrame.showPauseMenuDialog(); 
        } else {
            gameLoopTimer.start();
        }
        
        repaint();
    }
    
    public boolean isPaused() {
        return isPaused;
    }

    private void loadImages() {
        try {
            imgWall = ImageIO.read(getClass().getResource("wall.png"));
            imgPath = ImageIO.read(getClass().getResource("path.png"));
            imgGoal = ImageIO.read(getClass().getResource("goal.png"));
            imgTrap = ImageIO.read(getClass().getResource("trap.png"));
            imgEnergy = ImageIO.read(getClass().getResource("energy.png"));
            imgLife = ImageIO.read(getClass().getResource("life.png"));
            imgPlayer = ImageIO.read(getClass().getResource("player.png"));
            imgChest = ImageIO.read(getClass().getResource("chest.png"));
            imgLockedDoor = ImageIO.read(getClass().getResource("locked_door.png"));

        } catch (IOException e) {
            System.err.println("Error al cargar las imágenes: " + e.getMessage());
            System.err.println("El juego usará colores sólidos como alternativa.");
        }
    }

    private void updateGameTimer() {
        if (gameTimer > 0) {
            gameTimer--;
        } else {
            gameLoopTimer.stop();
            playerLives--;
            
            if (playerLives <= 0) {
                JOptionPane.showMessageDialog(this, "¡Te has quedado sin tiempo y sin vidas! El juego se reiniciará.", "Game Over", JOptionPane.ERROR_MESSAGE);
                resetGame();
            } else {
                JOptionPane.showMessageDialog(this, "¡Se acabó el tiempo! Pierdes una vida. El nivel se reiniciará.", "¡Tiempo!", JOptionPane.WARNING_MESSAGE);
                loadLevel(currentLevel); 
            }
        }
        repaint();
    }

    private void movePlayer(int keyCode) {
        int nextRow = playerRow;
        int nextCol = playerCol;

        switch (keyCode) {
            case KeyEvent.VK_UP:    nextRow--; break;
            case KeyEvent.VK_DOWN:  nextRow++; break;
            case KeyEvent.VK_LEFT:  nextCol--; break;
            case KeyEvent.VK_RIGHT: nextCol++; break;
            
            case KeyEvent.VK_S: saveGame(); return;
            case KeyEvent.VK_L: loadGame(); return;
            case KeyEvent.VK_I: showInventory(); return;
            case KeyEvent.VK_U: useInventoryItem(); return;
        }

        if (nextRow < 0 || nextRow >= maze.length || nextCol < 0 || nextCol >= maze[0].length) {
            return;
        }

        int nextTile = maze[nextRow][nextCol];

        if (nextTile == WALL) {
            playerLives--;
            flashWallCollision();
            if (playerLives <= 0) {
                JOptionPane.showMessageDialog(this, "¡Te has quedado sin vidas! El juego se reiniciará.", "Game Over", JOptionPane.ERROR_MESSAGE);
                resetGame();
            } else {
                JOptionPane.showMessageDialog(this, "¡Chocaste contra un muro! Te quedan " + playerLives + " vidas.", "¡Cuidado!", JOptionPane.WARNING_MESSAGE);
            }
            repaint(); 
        
        } else if (nextTile == TRAP) {
            JOptionPane.showMessageDialog(this, "¡Caíste en una trampa! Vuelves al inicio.", "¡Trampa!", JOptionPane.WARNING_MESSAGE);
            playerRow = 1;
            playerCol = 1;
            playerEnergy = INITIAL_ENERGY;
            stepsTaken = 0; 
            repaint();
        
        } else if (nextTile == LOCKED_DOOR) {
            if (inventory.contains("Llave")) {
                inventory.remove("Llave"); 
                nextLevel(); 
            } else {
                JOptionPane.showMessageDialog(this, "¡La puerta está cerrada! Necesitas una llave.", "Cerrado", JOptionPane.WARNING_MESSAGE);
            }
            repaint();
        
        } else {
            playerRow = nextRow;
            playerCol = nextCol;
            playerEnergy--; 
            stepsTaken++; 

            if (stepsTaken % 3 == 0) { 
                playerEnergy = Math.min(playerEnergy + 5, MAX_ENERGY); 
            }

            if (nextTile == ENERGY_PICKUP) {
                inventory.add("Poción de Energía");
                maze[playerRow][playerCol] = PATH;
                JOptionPane.showMessageDialog(this, "¡Recogiste una Poción de Energía!", "Ítem", JOptionPane.INFORMATION_MESSAGE);
            
            } else if (nextTile == LIFE_PICKUP) {
                inventory.add("Vida Extra");
                maze[playerRow][playerCol] = PATH;
                JOptionPane.showMessageDialog(this, "¡Recogiste una Vida Extra!", "Ítem", JOptionPane.INFORMATION_MESSAGE);
            
            } else if (nextTile == CHEST) {
                inventory.add("Llave"); 
                maze[playerRow][playerCol] = PATH; 
                JOptionPane.showMessageDialog(this, "¡Encontraste una llave en el cofre!", "¡Llave!", JOptionPane.INFORMATION_MESSAGE);
            }
            
            if (playerEnergy <= 0) {
                 JOptionPane.showMessageDialog(this, "¡Te has quedado sin energía! El juego se reiniciará.", "Game Over", JOptionPane.ERROR_MESSAGE);
                resetGame();
            }

            repaint();
        }
    }

    private void flashWallCollision() {
        Color originalBg = getBackground();
        setBackground(Color.RED);
        repaint();

        @SuppressWarnings("unused")
        javax.swing.Timer timer = new javax.swing.Timer(100, e -> {
            setBackground(originalBg);
            repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void loadLevel(int levelNumber) {
        this.currentLevel = levelNumber;
        int[][] template; 

        switch (levelNumber) {
            case 2:
                template = MAZE_TEMPLATE_2;
                break;
            case 3:
                template = MAZE_TEMPLATE_3;
                break;
            case 1:
            default:
                template = MAZE_TEMPLATE_1;
                break;
        }

        this.maze = new int[template.length][template[0].length];
        this.visited = new boolean[template.length][template[0].length]; 

        for (int r = 0; r < template.length; r++) {
            for (int c = 0; c < template[r].length; c++) {
                this.maze[r][c] = template[r][c];
            }
        }

        this.playerRow = 1;
        this.playerCol = 1;
        this.stepsTaken = 0;
        
        inventory.remove("Llave"); 
        
        this.gameTimer = INITIAL_TIME_PER_LEVEL;
        if (gameLoopTimer != null) gameLoopTimer.start(); 
        repaint();
    }

    private void nextLevel() {
        if (currentLevel < 3) {
            currentLevel++;
            loadLevel(currentLevel);
            JOptionPane.showMessageDialog(this, "¡Bienvenido al Nivel " + currentLevel + "!", "Nivel Superado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "¡Felicidades! ¡Has completado el juego!", "¡VICTORIA!", JOptionPane.INFORMATION_MESSAGE);
            resetGame(); 
        }
    }

    private void resetGame() {
        loadLevel(1);
        this.playerLives = INITIAL_LIVES;
        this.playerEnergy = INITIAL_ENERGY;
        this.gameTimer = INITIAL_TIME_PER_LEVEL;
        inventory.clear();
        this.currentLevel = 1; 
    }

    private void showInventory() {
        if (inventory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El inventario está vacío.", "Inventario", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder invStr = new StringBuilder("Inventario:\n");
        for (String item : inventory) {
            invStr.append("- ").append(item).append("\n");
        }
        JOptionPane.showMessageDialog(this, invStr.toString(), "Inventario", JOptionPane.INFORMATION_MESSAGE);
    }

    private void useInventoryItem() {
        gameLoopTimer.stop(); 

        if (inventory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes ítems para usar.", "Inventario", JOptionPane.WARNING_MESSAGE);
            if (!isPaused) gameLoopTimer.start(); 
            return;
        }

        Object[] items = inventory.toArray();
        
        String selectedItem = (String) JOptionPane.showInputDialog(
            this,
            "Elige un ítem para usar:",
            "Inventario",
            JOptionPane.PLAIN_MESSAGE,
            null,
            items, 
            items[0] 
        );

        if (selectedItem != null) {
            
            switch (selectedItem) {
                case "Poción de Energía":
                    playerEnergy = MAX_ENERGY;
                    inventory.remove(selectedItem); 
                    JOptionPane.showMessageDialog(this, "¡Has usado una Poción de Energía!", "Ítem Usado", JOptionPane.INFORMATION_MESSAGE);
                    break;
                
                case "Vida Extra":
                    playerLives++;
                    inventory.remove(selectedItem); 
                    JOptionPane.showMessageDialog(this, "¡Has usado una Vida Extra!", "Ítem Usado", JOptionPane.INFORMATION_MESSAGE);
                    break;
                
                case "Llave":
                    JOptionPane.showMessageDialog(this, "La llave se usa automáticamente en la puerta (presiona una dirección hacia ella).", "Ítem", JOptionPane.INFORMATION_MESSAGE);
                    break;
                
                default:
                    JOptionPane.showMessageDialog(this, "No puedes usar '" + selectedItem + "' así.", "Error", JOptionPane.WARNING_MESSAGE);
                    break;
            }
            
            repaint(); 
        }

        if (!isPaused) gameLoopTimer.start(); 
    }

    public void saveGame() {
        GameState gs = new GameState();
        gs.playerRow = this.playerRow;
        gs.playerCol = this.playerCol;
        gs.playerLives = this.playerLives;
        gs.playerEnergy = this.playerEnergy;
        gs.mazeState = this.maze;
        gs.inventoryState = this.inventory;
        gs.stepsTakenState = this.stepsTaken;
        gs.currentLevelState = this.currentLevel;
        gs.gameTimerState = this.gameTimer;
        gs.visitedState = this.visited;

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("savegame.dat"))) {
            oos.writeObject(gs);
            JOptionPane.showMessageDialog(this, "¡Partida Guardada!", "Guardar", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            System.err.println("Error al guardar la partida: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error: No se pudo guardar la partida.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadGame() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("savegame.dat"))) {
            GameState gs = (GameState) ois.readObject();
            
            this.playerRow = gs.playerRow;
            this.playerCol = gs.playerCol;
            this.playerLives = gs.playerLives;
            this.playerEnergy = gs.playerEnergy;
            this.maze = gs.mazeState;
            this.inventory = gs.inventoryState;
            this.stepsTaken = gs.stepsTakenState;
            this.currentLevel = gs.currentLevelState;
            this.gameTimer = gs.gameTimerState;
            this.visited = gs.visitedState;

            this.gameFrame = (GameFrame) SwingUtilities.getWindowAncestor(this);

            repaint();
            JOptionPane.showMessageDialog(this, "¡Partida Cargada!", "Cargar", JOptionPane.INFORMATION_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "No se encontró archivo de guardado.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar la partida: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error: No se pudo cargar la partida.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void drawTile(Graphics g, int row, int col) {
        int tileType = maze[row][col];
        int x = col * TILE_SIZE;
        int y = row * TILE_SIZE;

        if (imgPath != null) { g.drawImage(imgPath, x, y, TILE_SIZE, TILE_SIZE, null); } 
        else { g.setColor(new Color(210, 180, 140)); g.fillRect(x, y, TILE_SIZE, TILE_SIZE); }

        switch (tileType) {
            case WALL:
                if (imgWall != null) g.drawImage(imgWall, x, y, TILE_SIZE, TILE_SIZE, null);
                else { g.setColor(new Color(110, 80, 50)); g.fillRect(x, y, TILE_SIZE, TILE_SIZE); }
                break;
            case TRAP:
                if (imgTrap != null) g.drawImage(imgTrap, x, y, TILE_SIZE, TILE_SIZE, null);
                else { g.setColor(new Color(180, 0, 0)); g.fillRect(x, y, TILE_SIZE, TILE_SIZE); }
                break;
            case ENERGY_PICKUP:
                if (imgEnergy != null) g.drawImage(imgEnergy, x, y, TILE_SIZE, TILE_SIZE, null);
                else { g.setColor(Color.CYAN); g.fillRect(x, y, TILE_SIZE, TILE_SIZE); }
                break;
            case LIFE_PICKUP:
                if (imgLife != null) g.drawImage(imgLife, x, y, TILE_SIZE, TILE_SIZE, null);
                else { g.setColor(Color.GREEN); g.fillRect(x, y, TILE_SIZE, TILE_SIZE); }
                break;
            case CHEST:
                if (imgChest != null) g.drawImage(imgChest, x, y, TILE_SIZE, TILE_SIZE, null);
                else { g.setColor(Color.MAGENTA); g.fillRect(x, y, TILE_SIZE, TILE_SIZE); }
                break;
            case LOCKED_DOOR:
                if (imgLockedDoor != null) g.drawImage(imgLockedDoor, x, y, TILE_SIZE, TILE_SIZE, null);
                else { g.setColor(Color.ORANGE); g.fillRect(x, y, TILE_SIZE, TILE_SIZE); }
                break;
            case GOAL:
                if (imgGoal != null) g.drawImage(imgGoal, x, y, TILE_SIZE, TILE_SIZE, null);
                else { g.setColor(Color.YELLOW); g.fillRect(x, y, TILE_SIZE, TILE_SIZE); }
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (maze == null || visited == null) return;

        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;
                double distance = Math.sqrt(Math.pow(playerRow - row, 2) + Math.pow(playerCol - col, 2));

                if (distance <= VISION_RADIUS) {
                    drawTile(g, row, col);
                    visited[row][col] = true;

                } else if (visited[row][col]) {
                    drawTile(g, row, col);
                    g.setColor(new Color(0, 0, 0, 150)); 
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                } else {
                    g.setColor(Color.BLACK);
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        if (imgPlayer != null) {
            g.drawImage(imgPlayer, playerCol * TILE_SIZE, playerRow * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        } else {
            g.setColor(Color.BLUE);
            g.fillOval(playerCol * TILE_SIZE, playerRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        
        String livesText = "Vidas: " + playerLives;
        g.drawString(livesText, 10, 25); 

        g.drawString("Energía:", 10, 50); 
        g.setColor(Color.DARK_GRAY);
        g.fillRect(100, 35, MAX_ENERGY, 15);
        g.setColor(Color.GREEN);
        g.fillRect(100, 35, playerEnergy, 15);
        g.setColor(Color.WHITE);
        g.drawRect(100, 35, MAX_ENERGY, 15);

        int minutes = gameTimer / 60;
        int seconds = gameTimer % 60;
        String timeText = String.format("Tiempo: %02d:%02d", minutes, seconds);
        
        if (gameTimer <= 30) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.WHITE);
        }
        g.drawString(timeText, 10, 75); 
        
        if (isPaused) {
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.setColor(Color.YELLOW);
            g.drawString("INVENTARIO (U):", 10, getHeight() - 150);

            if (inventory.isEmpty()) {
                g.setColor(Color.LIGHT_GRAY);
                g.drawString("Vacío", 10, getHeight() - 130);
            } else {
                g.setColor(Color.WHITE);
                for (int i = 0; i < inventory.size(); i++) {
                    g.drawString("- " + inventory.get(i), 10, getHeight() - 130 + i * 20);
                }
            }
        }
    }
}