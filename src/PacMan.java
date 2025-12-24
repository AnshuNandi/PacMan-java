import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x, y, width, height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;
        
        // Ghost & item state variables
        boolean isScared = false;
        boolean isEyes = false; // returning-to-pen state after being eaten
        int respawnTimer = 0;   // countdown at pen before respawn
        int speed;              // pixels per tick for moving blocks
        char ghostType = 'N';   // r, p, b, o for ghosts; otherwise N


        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
            this.speed = tileSize/4; // default speed
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for(Block wall : walls){
                if(collision(this, wall)){
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                    break;
                }
            }
        }

        void updateVelocity() {
            switch (this.direction) {
                case 'U':
                    this.velocityX = 0;
                    this.velocityY = -this.speed;
                    break;
                case 'D':
                    this.velocityX = 0;
                    this.velocityY = this.speed;
                    break;
                case 'L':
                    this.velocityX = -this.speed;
                    this.velocityY = 0;
                    break;
                case 'R':
                    this.velocityX = this.speed;
                    this.velocityY = 0;
                    break;
            }
        }
        void reset(){
            this.x = this.startX;
            this.y = this.startY;
            this.direction = 'U';
            updateVelocity();
        }

    }
    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;
    
    private Image powerFoodImage;
    private Image scaredGhostImage;
    private Image cherryImage1;
    private Image cherryImage2;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    //* = power food
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X*       X       *X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X*               *X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> powerFoods;
    HashSet<Block> fruits;
    HashSet<Block> ghosts;
    Block pacman;
    
    int powerUpTimer = 0;
    int basePowerUpDuration = 300;    // ~15s
    int currentPowerUpDuration = 300; // will decrease with levels

    // Level progression
    int level = 1;
    int baseSpeed = tileSize/4;       // pacman default speed

    // Ghost mode (scatter/chase) timers
    boolean scatterMode = true;
    int modeTimer = 0;
    int scatterDuration = 140; // ~7s
    int chaseDuration = 240;   // ~12s

    // Fruit spawn/animation state
    int fruitSpawnCounter = 0;      // counts frames until spawn
    int fruitSpawnInterval = 600;   // ~30s at 20fps
    int fruitActiveTimer = 0;       // countdown while fruit visible
    int fruitActiveDuration = 200;  // ~10s visible
    boolean fruitVisible = false;
    int cherryAnimTick = 0;
    int fruitSpawnIndex = 0;        // rotate through candidate spawn points

    // Pac-Man mouth animation
    int pacAnimTick = 0;

    // Audio
    private Clip backgroundMusic;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0;
    int highestScore = 0;
    int lives = 3;
    boolean gameOver = false;
    
    // Buffered turning: desired next direction
    private char nextDirection = 'R';

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //loading images
        wallImage = new ImageIcon(getClass().getResource("/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/pacmanRight.png")).getImage();
        
        powerFoodImage = new ImageIcon(getClass().getResource("/powerFood.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("/scaredGhost.png")).getImage();
        cherryImage1 = new ImageIcon(getClass().getResource("/cherry.png")).getImage();
        cherryImage2 = new ImageIcon(getClass().getResource("/cherry2.png")).getImage();

        loadMap();
        applyLevelSettings();
        loadBackgroundMusic();
        for (Block ghost : ghosts) {
            ghost.updateDirection(directions[random.nextInt(4)]);
        }
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();
        
    }

    public void loadMap(){
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        powerFoods = new HashSet<Block>();
        fruits = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for(int r = 0; r < rowCount; r++){
            for(int c = 0; c < columnCount; c++){
                char tileMapChar = tileMap[r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch(tileMapChar){
                    case 'X': //block wall
                        walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                        break;
                    case 'b': //blue ghost
                        Block blue = new Block(blueGhostImage, x, y, tileSize, tileSize);
                        blue.ghostType = 'b';
                        ghosts.add(blue);
                        break;
                    case 'o': //orange ghost
                        Block orange = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                        orange.ghostType = 'o';
                        ghosts.add(orange);
                        break;
                    case 'p': //pink ghost
                        Block pink = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                        pink.ghostType = 'p';
                        ghosts.add(pink);
                        break;
                    case 'r': //red ghost
                        Block red = new Block(redGhostImage, x, y, tileSize, tileSize);
                        red.ghostType = 'r';
                        ghosts.add(red);
                        break;
                    case 'P': //pacman
                        pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                        break;
                    case ' ': //food
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                    case '*': //power food
                        powerFoods.add(new Block(powerFoodImage, x + 8, y + 8, 16, 16));
                        break;
                }
            }
        }

        // reset fruit state each level
        fruits.clear();
        fruitSpawnCounter = 0;
        fruitActiveTimer = 0;
        fruitVisible = false;
        cherryAnimTick = 0;
        fruitSpawnIndex = 0;
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    // Audio helper: generate and play a tone
    private void playTone(int frequency, int durationMs) {
        new Thread(() -> {
            try {
                int sampleRate = 44100;
                int samples = sampleRate * durationMs / 1000;
                byte[] audioData = new byte[samples];
                double twoPi = 2.0 * Math.PI;
                for(int i = 0; i < samples; i++){
                    double sample = Math.sin(twoPi * i * frequency / sampleRate);
                    audioData[i] = (byte)(sample * 120);
                }
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                AudioInputStream ais = new AudioInputStream(
                    new java.io.ByteArrayInputStream(audioData),
                    format, samples);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
            } catch(Exception e){
                // silent fail
            }
        }).start();
    }

    // Load background music
    private void loadBackgroundMusic(){
        new Thread(() -> {
            try {
                javax.sound.sampled.AudioInputStream ais;
                java.net.URL url = getClass().getResource("/happy-man.wav");
                if(url != null){
                    ais = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
                } else {
                    java.io.File f = new java.io.File("happy-man.wav");
                    ais = javax.sound.sampled.AudioSystem.getAudioInputStream(f);
                }

                backgroundMusic = javax.sound.sampled.AudioSystem.getClip();
                backgroundMusic.open(ais);
                backgroundMusic.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
                backgroundMusic.start();
            } catch(Exception e){
                System.out.println("Music init error: " + e.getMessage());
            }
        }).start();
    }

    // Sound effects
    private void playSoundPowerFood(){
        playTone(800, 150);
        playTone(1000, 150);
    }

    private void playSoundFruit(){
        playTone(600, 100);
        playTone(800, 100);
        playTone(1000, 100);
    }

    private void playSoundGhostEaten(){
        playTone(400, 80);
        playTone(300, 80);
        playTone(200, 160);
    }

    private void playSoundGhostHit(){
        playTone(900, 100);
        playTone(1100, 120);
        playTone(1300, 160);
    }

    private void playSoundGameOver(){
        new Thread(() -> {
            try {
                javax.sound.sampled.AudioInputStream ais;
                java.net.URL url = getClass().getResource("/game-over.wav");
                if(url != null){
                    ais = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
                } else {
                    java.io.File f = new java.io.File("game-over.wav");
                    ais = javax.sound.sampled.AudioSystem.getAudioInputStream(f);
                }
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(ais);
                
                // Set volume to maximum
                if(clip.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)){
                    javax.sound.sampled.FloatControl gainControl = 
                        (javax.sound.sampled.FloatControl) clip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                    gainControl.setValue(gainControl.getMaximum());
                }
                
                clip.start();
            } catch(Exception e){
                System.out.println("Game over sound error: " + e.getMessage());
            }
        }).start();
    }

    private void playSoundLevelUp(){
        playTone(1200, 150);
        playTone(1400, 150);
        playTone(1600, 300);
    }

    // Helper: check if a map tile is a wall
    private boolean isWall(int r, int c){
        if(r < 0 || r >= rowCount || c < 0 || c >= columnCount){
            return true;
        }
        return tileMap[r].charAt(c) == 'X';
    }

    private int[] tileAhead(int row, int col, char direction, int tiles){
        switch(direction){
            case 'U': return new int[] {row - tiles, col};
            case 'D': return new int[] {row + tiles, col};
            case 'L': return new int[] {row, col - tiles};
            case 'R': return new int[] {row, col + tiles};
            default: return new int[] {row, col};
        }
    }

    private char chooseDirection(Block ghost, int targetRow, int targetCol){
        // Only consider changes when aligned to grid; caller should check alignment
        int row = ghost.y / tileSize;
        int col = ghost.x / tileSize;

        // Build list of possible directions not hitting walls
        char[] dirCandidates = {'U','L','D','R'}; // order tuned to avoid bias
        char bestDir = ghost.direction;
        int bestDist = Integer.MAX_VALUE;
        char opposite = 'U';
        switch(ghost.direction){
            case 'U': opposite = 'D'; break;
            case 'D': opposite = 'U'; break;
            case 'L': opposite = 'R'; break;
            case 'R': opposite = 'L'; break;
        }

        for(char d : dirCandidates){
            int nr = row, nc = col;
            switch(d){
                case 'U': nr = row - 1; break;
                case 'D': nr = row + 1; break;
                case 'L': nc = col - 1; break;
                case 'R': nc = col + 1; break;
            }
            if(isWall(nr, nc)) continue;
            int dr = targetRow - nr;
            int dc = targetCol - nc;
            int dist = dr*dr + dc*dc;
            // Prefer non-opposite unless no other options
            boolean isOpposite = (d == opposite);
            if(isOpposite && bestDist != Integer.MAX_VALUE) continue;
            if(dist < bestDist){
                bestDist = dist;
                bestDir = d;
            }
        }

        // If all options were opposite (corridor), allow opposite
        if(bestDist == Integer.MAX_VALUE){
            for(char d : dirCandidates){
                int nr = row, nc = col;
                switch(d){
                    case 'U': nr = row - 1; break;
                    case 'D': nr = row + 1; break;
                    case 'L': nc = col - 1; break;
                    case 'R': nc = col + 1; break;
                }
                if(!isWall(nr, nc)){
                    bestDir = d;
                    break;
                }
            }
        }
        return bestDir;
    }

    // Helper method to update score and track highest score
    private void addScore(int points){
        score += points;
        if(score > highestScore){
            highestScore = score;
        }
    }

    // Scared movement: pick a random valid direction avoiding immediate reversal when possible
    private char chooseScaredDirection(Block ghost){
        int row = ghost.y / tileSize;
        int col = ghost.x / tileSize;
        char opposite;
        switch(ghost.direction){
            case 'U': opposite = 'D'; break;
            case 'D': opposite = 'U'; break;
            case 'L': opposite = 'R'; break;
            case 'R': opposite = 'L'; break;
            default: opposite = 'U';
        }
        char[] dirs = {'U','D','L','R'};
        // simple Fisher–Yates shuffle for randomness
        for(int i = dirs.length - 1; i > 0; i--){
            int j = random.nextInt(i + 1);
            char tmp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = tmp;
        }
        char fallback = ghost.direction;
        for(char d : dirs){
            int nr = row, nc = col;
            switch(d){
                case 'U': nr = row - 1; break;
                case 'D': nr = row + 1; break;
                case 'L': nc = col - 1; break;
                case 'R': nc = col + 1; break;
            }
            if(isWall(nr, nc)) continue;
            if(d == opposite) { fallback = d; continue; }
            return d; // first non-opposite valid
        }
        // If only opposite works, use it
        return fallback;
    }

    // Apply level-based tuning (ghost speeds, power-up duration, fruit cadence)
    private void applyLevelSettings(){
        // Power-up gets shorter as level increases (min ~4s)
        currentPowerUpDuration = Math.max(80, basePowerUpDuration - (level - 1) * 40);

        // Update existing ghosts (constant speed to avoid oscillation)
        if(ghosts != null){
            for(Block gh : ghosts){
                gh.speed = baseSpeed;
                gh.updateVelocity();
            }
        }

        // Keep Pac-Man constant speed
        if(pacman != null){
            pacman.speed = baseSpeed;
            pacman.updateVelocity();
        }

        // Slightly faster fruit cadence at higher levels
        fruitSpawnInterval = Math.max(300, 600 - (level - 1) * 40);
        fruitActiveDuration = Math.max(120, 200 - (level - 1) * 10);
    }

    public void draw(Graphics g){
        //drawing walls
        for(Block wall : walls){
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
        //drawing foods
        g.setColor(Color.WHITE);
        for(Block food : foods){
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        
        //drawing power foods
        for(Block powerFood : powerFoods){
            g.drawImage(powerFood.image, powerFood.x, powerFood.y, powerFood.width, powerFood.height, null);
        }

        //drawing fruits (cherries)
        for(Block fruit : fruits){
            g.drawImage(fruit.image, fruit.x, fruit.y, fruit.width, fruit.height, null);
        }

        //drawing ghosts
        for(Block ghost : ghosts){
            if(ghost.isEyes){
                // Draw simple eyes (white ovals with blue pupils) if no eyes image
                int gx = ghost.x + ghost.width/4;
                int gy = ghost.y + ghost.height/4;
                int eyeW = ghost.width/5;
                int eyeH = ghost.height/3;
                g.setColor(Color.WHITE);
                g.fillOval(gx - eyeW, gy, eyeW, eyeH);
                g.fillOval(gx + eyeW, gy, eyeW, eyeH);
                g.setColor(Color.BLUE);
                g.fillOval(gx - eyeW + eyeW/3, gy + eyeH/3, eyeW/3, eyeH/3);
                g.fillOval(gx + eyeW + eyeW/3, gy + eyeH/3, eyeW/3, eyeH/3);
            } else if(ghost.isScared){
                g.drawImage(scaredGhostImage, ghost.x, ghost.y, ghost.width, ghost.height, null);
            } else {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }
        }

        //drawing pacman with mouth animation (angles are counterclockwise: 0=right, 90=up)
        boolean pacMoving = pacman.velocityX != 0 || pacman.velocityY != 0;
        int mouthAngle = 0;
        if(pacMoving){
            pacAnimTick = (pacAnimTick + 1) % 20;
            int phase = pacAnimTick;
            mouthAngle = (phase < 10) ? phase * 4 : (20 - phase) * 4; // oscillates 0..40
        }
        int heading;
        switch(pacman.direction){
            case 'U': heading = 90; break;
            case 'D': heading = 270; break;
            case 'L': heading = 180; break;
            case 'R': heading = 0; break;
            default: heading = 0; break;
        }
        int startAngle = heading + mouthAngle/2;
        int extent = 360 - mouthAngle;
        if(pacMoving){
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.YELLOW);
            g2.fillArc(pacman.x, pacman.y, pacman.width, pacman.height, startAngle, extent);
        } else {
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        }

        //score
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if(gameOver){
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", boardWidth/2 - 150, boardHeight/2);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.drawString("Final Score: " + String.valueOf(score), boardWidth/2 - 130, boardHeight/2 + 60);
        } else {
            g.setColor(Color.WHITE);
            g.drawString("Score: " + String.valueOf(score), 10, 20);
            g.drawString("Highest: " + String.valueOf(highestScore), 10, 45);
            g.drawString("Lives: " + String.valueOf(lives), boardWidth - 75, 20);
            g.drawString("Level: " + String.valueOf(level), boardWidth/2 - 30, 40);
            
            // Display power-up timer if active
            if(powerUpTimer > 0){
                g.setColor(Color.YELLOW);
                g.drawString("POWER UP: " + (powerUpTimer/20) + "s", boardWidth/2 - 60, 20);
            }
        }
    }

    public void move(){
        // Try to turn using buffered input when aligned to grid
        if(nextDirection != pacman.direction){
            if(pacman.x % tileSize == 0 && pacman.y % tileSize == 0){
                int row = pacman.y / tileSize;
                int col = pacman.x / tileSize;
                int targetRow = row;
                int targetCol = col;
                switch(nextDirection){
                    case 'U': targetRow = row - 1; break;
                    case 'D': targetRow = row + 1; break;
                    case 'L': targetCol = col - 1; break;
                    case 'R': targetCol = col + 1; break;
                }
                if(!isWall(targetRow, targetCol)){
                    pacman.direction = nextDirection;
                    pacman.updateVelocity();
                    // update sprite to match new direction
                    if(pacman.direction == 'U'){
                        pacman.image = pacmanUpImage;
                    } else if(pacman.direction == 'D'){
                        pacman.image = pacmanDownImage;
                    } else if(pacman.direction == 'L'){
                        pacman.image = pacmanLeftImage;
                    } else if(pacman.direction == 'R'){
                        pacman.image = pacmanRightImage;
                    }
                }
            }
        }

        // Move forward in current direction
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        //teleporting through tunnels
        if(pacman.x < -pacman.width){
            pacman.x = boardWidth;
        } else if(pacman.x > boardWidth){
            pacman.x = -pacman.width;
        }

        //checking collision with walls
        for(Block wall : walls){
            if(collision(pacman, wall)){
                //move back
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // Update global ghost mode timer
        modeTimer++;
        if(scatterMode && modeTimer >= scatterDuration){
            scatterMode = false;
            modeTimer = 0;
        } else if(!scatterMode && modeTimer >= chaseDuration){
            scatterMode = true;
            modeTimer = 0;
        }

        int pacRow = pacman.y / tileSize;
        int pacCol = pacman.x / tileSize;
        int[] ahead2 = tileAhead(pacRow, pacCol, pacman.direction, 2);
        int[] ahead4 = tileAhead(pacRow, pacCol, pacman.direction, 4);

        // find blinky (red) for Inky calc
        Block blinky = null;
        for(Block g : ghosts){
            if(g.ghostType == 'r'){
                blinky = g;
                break;
            }
        }

        //checking ghosts collisions and movement
        for(Block ghost : ghosts){
            int targetR = pacRow;
            int targetC = pacCol;
            // Handle collision with Pac-Man
            if(collision(pacman, ghost)){
                if(ghost.isEyes){
                    // No effect when colliding with returning eyes
                } else if(ghost.isScared){
                    addScore(200);
                    playSoundGhostEaten();
                    ghost.isScared = false;
                    ghost.isEyes = true;
                } else {
                    lives -= 1;
                    playSoundGhostHit();
                    if(lives == 0){
                        playSoundGameOver();
                        gameOver = true;
                        return;
                    }
                    resetPositions();
                }
            }

            if(ghost.isEyes){
                int speed = tileSize/4;
                int dx = ghost.startX - ghost.x;
                int dy = ghost.startY - ghost.y;

                if(Math.abs(dx) <= speed){ ghost.x = ghost.startX; } else { ghost.x += (dx > 0 ? speed : -speed); }
                if(Math.abs(dy) <= speed){ ghost.y = ghost.startY; } else { ghost.y += (dy > 0 ? speed : -speed); }

                if(ghost.x == ghost.startX && ghost.y == ghost.startY){
                    if(ghost.respawnTimer == 0){
                        ghost.respawnTimer = 60; // ~3 seconds
                    } else {
                        ghost.respawnTimer--;
                        if(ghost.respawnTimer == 0){
                            ghost.isEyes = false;
                            ghost.isScared = false;
                            ghost.updateDirection(directions[random.nextInt(4)]);
                        }
                    }
                }
                continue;
            }

            // Gate behavior: simple upward push in center lane
            if(ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D'){
                ghost.updateDirection('U');
            }

            // Decide direction only when aligned to grid
            if(ghost.x % tileSize == 0 && ghost.y % tileSize == 0){
                if(ghost.isScared){
                    char newDir = chooseScaredDirection(ghost);
                    ghost.direction = newDir;
                    ghost.updateVelocity();
                } else if(scatterMode){
                    // scatter corners
                    switch(ghost.ghostType){
                        case 'r': targetR = 0; targetC = columnCount - 1; break; // top-right
                        case 'p': targetR = 0; targetC = 0; break;              // top-left
                        case 'b': targetR = rowCount - 1; targetC = columnCount - 1; break; // bottom-right
                        case 'o': targetR = rowCount - 1; targetC = 0; break;    // bottom-left
                        default: targetR = pacRow; targetC = pacCol; break;
                    }
                    char newDir = chooseDirection(ghost, targetR, targetC);
                    ghost.direction = newDir;
                    ghost.updateVelocity();
                } else {
                    // chase logic per ghost
                    switch(ghost.ghostType){
                        case 'r': // Blinky: direct chase
                            targetR = pacRow; targetC = pacCol; break;
                        case 'p': { // Pinky: four tiles ahead
                            targetR = ahead4[0]; targetC = ahead4[1]; break;
                        }
                        case 'b': { // Inky: vector using Blinky and two-ahead tile
                            int tr = ahead2[0];
                            int tc = ahead2[1];
                            if(blinky != null){
                                int br = blinky.y / tileSize;
                                int bc = blinky.x / tileSize;
                                targetR = tr + (tr - br);
                                targetC = tc + (tc - bc);
                            } else {
                                targetR = tr; targetC = tc;
                            }
                            break;
                        }
                        case 'o': { // Clyde: chase if far, else scatter corner
                            int dr = pacRow - (ghost.y / tileSize);
                            int dc = pacCol - (ghost.x / tileSize);
                            int dist2 = dr*dr + dc*dc;
                            if(dist2 >= 64){ // >=8 tiles
                                targetR = pacRow; targetC = pacCol;
                            } else {
                                targetR = rowCount - 1; targetC = 0;
                            }
                            break;
                        }
                        default:
                            targetR = pacRow; targetC = pacCol; break;
                    }
                    char newDir = chooseDirection(ghost, targetR, targetC);
                    ghost.direction = newDir;
                    ghost.updateVelocity();
                }
            }

            // Normal ghost movement step
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            //checking collision with walls
            for(Block wall : walls){
                if(collision(ghost, wall) || ghost.x < 0 || ghost.x + ghost.width > boardWidth || ghost.y < 0 || ghost.y + ghost.height > boardHeight){
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    if(ghost.isScared){
                        char newDir = chooseScaredDirection(ghost);
                        ghost.direction = newDir;
                        ghost.updateVelocity();
                    } else {
                        char newDir = chooseDirection(ghost, targetR, targetC);
                        ghost.direction = newDir;
                        ghost.updateVelocity();
                    }
                    break;
                }
            }
        }

        //checking collision with foods
        Block foodEaten = null;
        for (Block food : foods){
            if(collision(pacman, food)){
                foodEaten = food;
                addScore(10);
            }
        }
        foods.remove(foodEaten);
        
        //checking collision with power foods
        Block powerFoodEaten = null;
        for (Block powerFood : powerFoods){
            if(collision(pacman, powerFood)){
                powerFoodEaten = powerFood;
                addScore(50);
                playSoundPowerFood();
                // Activate power-up mode
                powerUpTimer = currentPowerUpDuration;
                for(Block ghost : ghosts){
                    ghost.isScared = true;
                }
            }
        }
        powerFoods.remove(powerFoodEaten);

        // Fruit (cherry) spawn, animation, and pickup
        if(!fruitVisible){
            fruitSpawnCounter++;
            if(fruitSpawnCounter >= fruitSpawnInterval){
                // Candidate spawn points (row, col). We'll choose the first open space.
                int[][] candidates = new int[][] { {13,4}, {13,14}, {9,4}, {9,14} };
                for(int i = 0; i < candidates.length; i++){
                    int idx = (fruitSpawnIndex + i) % candidates.length;
                    int r = candidates[idx][0];
                    int c = candidates[idx][1];
                    if(r >= 0 && r < rowCount && c >= 0 && c < columnCount && tileMap[r].charAt(c) == ' '){
                        int fx = c * tileSize + 8;
                        int fy = r * tileSize + 8;
                        Block fruit = new Block(cherryImage1, fx, fy, 16, 16);
                        fruits.add(fruit);
                        fruitVisible = true;
                        fruitActiveTimer = fruitActiveDuration;
                        fruitSpawnCounter = 0;
                        fruitSpawnIndex = (idx + 1) % candidates.length;
                        break;
                    }
                }
            }
        } else {
            // Animate and expire
            cherryAnimTick = (cherryAnimTick + 1) % 20;
            for(Block fruit : fruits){
                fruit.image = (cherryAnimTick < 10) ? cherryImage1 : cherryImage2;
            }
            if(fruitActiveTimer > 0){
                fruitActiveTimer--;
                if(fruitActiveTimer == 0){
                    fruits.clear();
                    fruitVisible = false;
                }
            }
        }

        // Pickup cherry
        Block fruitEaten = null;
        for(Block fruit : fruits){
            if(collision(pacman, fruit)){
                fruitEaten = fruit;
                addScore(100); // cherry bonus
            }
        }
        if(fruitEaten != null){
            fruits.remove(fruitEaten);
            playSoundFruit();
            fruitVisible = false;
            fruitActiveTimer = 0;
        }
        
        // Update power-up timer
        if(powerUpTimer > 0){
            powerUpTimer--;
            if(powerUpTimer == 0){
                for(Block ghost : ghosts){
                    if(!ghost.isEyes){
                        ghost.isScared = false;
                    }
                }
            }
        }

        if(foods.isEmpty() && powerFoods.isEmpty()){
            level++;
            playSoundLevelUp();
            loadMap();
            applyLevelSettings();
            resetPositions();
            powerUpTimer = 0;
        }
    }

    public boolean collision(Block a, Block b){
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y; 
    }

    public void resetPositions(){
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for(Block ghost : ghosts){
            ghost.reset();
            ghost.isScared = false;
            ghost.isEyes = false;
            ghost.respawnTimer = 0;
            ghost.updateDirection(directions[random.nextInt(4)]);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if(gameOver){
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if(code == KeyEvent.VK_UP){
            nextDirection = 'U';
        } else if(code == KeyEvent.VK_DOWN){
            nextDirection = 'D';
        } else if(code == KeyEvent.VK_LEFT){
            nextDirection = 'L';
        } else if(code == KeyEvent.VK_RIGHT){
            nextDirection = 'R';
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(gameOver){
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            powerUpTimer = 0;
            gameLoop.start();
        }
        // Also accept buffered change on key release (optional)
        int code = e.getKeyCode();
        if(code == KeyEvent.VK_UP){
            nextDirection = 'U';
        } else if(code == KeyEvent.VK_DOWN){
            nextDirection = 'D';
        } else if(code == KeyEvent.VK_LEFT){
            nextDirection = 'L';
        } else if(code == KeyEvent.VK_RIGHT){
            nextDirection = 'R';
        }
    }
}
