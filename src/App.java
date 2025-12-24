import javax.swing.JFrame;
import javax.swing.ImageIcon;
import java.awt.Image;

public class App {
    public static void main(String[] args) throws Exception {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("Pac Man");
        // frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set window icon
        try {
            Image icon = new ImageIcon(App.class.getResource("/pacmanRight.png")).getImage();
            frame.setIconImage(icon);
        } catch (Exception e) {
            System.out.println("Could not load window icon: " + e.getMessage());
        }

        PacMan pacmanGame = new PacMan();
        frame.add(pacmanGame);
        frame.pack();
        pacmanGame.requestFocus();
        frame.setVisible(true);
        
    }
}
