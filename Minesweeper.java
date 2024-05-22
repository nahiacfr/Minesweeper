import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Minesweeper extends JFrame {
    private static final int SIZE = 10; // Grid size
    private static final int MINES = 20; // Number of mines
    private final JButton[][] buttons = new JButton[SIZE][SIZE];
    private final boolean[][] mines = new boolean[SIZE][SIZE];
    private final int[][] neighbors = new int[SIZE][SIZE];
    private boolean gameOver = false;
    private boolean firstClick = true;

    private ImageIcon flagIcon;
    private ImageIcon mineIcon;

    private JLabel minesLabel;
    private JLabel timeLabel;
    private int remainingMines = MINES;
    private Timer timer;
    private int elapsedTime = 0;

    public Minesweeper() {
        setTitle("Buscaminas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        minesLabel = new JLabel("Minas: " + remainingMines);
        timeLabel = new JLabel("Tiempo: 0");

        topPanel.add(minesLabel, BorderLayout.WEST);
        topPanel.add(timeLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(SIZE, SIZE));
        add(gridPanel, BorderLayout.CENTER);

        loadIcons();
        initializeGrid(gridPanel);

        timer = new Timer(1000, e -> {
            elapsedTime++;
            timeLabel.setText("Tiempo: " + elapsedTime);
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadIcons() {
        try {
            BufferedImage flagImage = ImageIO.read(new File("flag.png"));
            BufferedImage mineImage = ImageIO.read(new File("mine.png"));
            int size = 40; // Size of the buttons
            flagIcon = new ImageIcon(flagImage.getScaledInstance(size, size, Image.SCALE_SMOOTH));
            mineIcon = new ImageIcon(mineImage.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeGrid(JPanel gridPanel) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                buttons[row][col] = new JButton();
                buttons[row][col].setPreferredSize(new Dimension(40, 40));
                buttons[row][col].setMargin(new Insets(0, 0, 0, 0));
                buttons[row][col].setFocusable(false);
                buttons[row][col].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JButton source = (JButton) e.getSource();
                        Point point = getButtonPosition(source);
                        if (gameOver || point == null) return;

                        if (SwingUtilities.isRightMouseButton(e)) {
                            handleRightClick(source);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            handleLeftClick(point.x, point.y);
                        }
                    }
                });
                gridPanel.add(buttons[row][col]);
            }
        }
    }

    private Point getButtonPosition(JButton button) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (buttons[row][col] == button) {
                    return new Point(row, col);
                }
            }
        }
        return null;
    }

    private void handleRightClick(JButton button) {
        if (button.getIcon() == null) {
            button.setIcon(flagIcon);
            remainingMines--;
        } else {
            button.setIcon(null);
            remainingMines++;
        }
        minesLabel.setText("Minas: " + remainingMines);
    }

    private void handleLeftClick(int row, int col) {
        if (firstClick) {
            placeMines(row, col);
            calculateNeighbors();
            firstClick = false;
            timer.start();
        }

        if (mines[row][col]) {
            revealMines();
            showGameOverDialog(false);
        } else {
            revealCell(row, col);
            checkWinCondition();
        }
    }

    private void revealMines() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (mines[row][col]) {
                    buttons[row][col].setIcon(mineIcon);
                }
            }
        }
        gameOver = true;
        timer.stop();
    }

    private void revealCell(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE || !buttons[row][col].isEnabled()) {
            return;
        }

        buttons[row][col].setEnabled(false);
        if (neighbors[row][col] > 0) {
            buttons[row][col].setText(String.valueOf(neighbors[row][col]));
        } else {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        revealCell(row + i, col + j);
                    }
                }
            }
        }
    }

    private void checkWinCondition() {
        boolean won = true;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!mines[row][col] && buttons[row][col].isEnabled()) {
                    won = false;
                    break;
                }
            }
        }
        if (won) {
            revealMines();
            showGameOverDialog(true);
        }
    }

    private void showGameOverDialog(boolean won) {
        String message = won ? "¡Ganaste!" : "¡Perdiste!";
        int option = JOptionPane.showConfirmDialog(this, message + "\n¿Desea jugar una nueva partida?", "¿Qué desea hacer?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    private void resetGame() {
        gameOver = false;
        firstClick = true;
        remainingMines = MINES;
        minesLabel.setText("Minas: " + remainingMines);
        elapsedTime = 0;
        timeLabel.setText("Tiempo: " + elapsedTime);
        timer.stop();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                buttons[row][col].setEnabled(true);
                buttons[row][col].setIcon(null);
                buttons[row][col].setText("");
                mines[row][col] = false;
                neighbors[row][col] = 0;
            }
        }
    }

    private void placeMines(int initialRow, int initialCol) {
        Random rand = new Random();
        int placedMines = 0;
        while (placedMines < MINES) {
            int row = rand.nextInt(SIZE);
            int col = rand.nextInt(SIZE);
            if ((row != initialRow || col != initialCol) && !mines[row][col]) {
                mines[row][col] = true;
                placedMines++;
            }
        }
    }

    private void calculateNeighbors() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (mines[row][col]) continue;
                int count = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (row + i >= 0 && row + i < SIZE && col + j >= 0 && col + j < SIZE && mines[row + i][col + j]) {
                            count++;
                        }
                    }
                }
                neighbors[row][col] = count;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Minesweeper::new);
    }
}
