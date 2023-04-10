import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MinesweeperGame implements ActionListener {
    JFrame frame;
    JPanel textPanel;
    JPanel buttonPanel;
    JButton[][] buttons;
    JButton buttonFlag;
    JButton buttonRestart;
    JLabel textField;
    JLabel textFieldScore;

    private final int SIDE;
    private GameObject[][] gameField;
    private int countMinesOnField;
    private int countFlags;
    private int countClosedTiles;
    private int score = 0;
    private boolean isFlagMode = false;
    private boolean isGameStopped = false;

    private static final Font font = new Font("SansSerif", Font.BOLD, 20);
    private static final String HAND = "\uD83C\uDFAF";
    private static final String MINE = "\uD83D\uDCA3";
    private static final String FLAG = "\uD83D\uDEA9";
    private static final Color STANDART_BUTTON_COLOR = new JButton().getBackground();

    MinesweeperGame(int size) {
        SIDE = size;
        createGame(); // fills logic 2d array
        initialize(); // 
    }

    private void initialize() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setLayout(new BorderLayout());

        textPanel = new JPanel(); // TOP panel
        textPanel.setVisible(true);
        textPanel.setBackground(Color.BLACK);

        buttonPanel = new JPanel(); // CENTER Grid panel
        buttonPanel.setVisible(true);
        buttonPanel.setLayout(new GridLayout(SIDE, SIDE));

        textField = new JLabel(); // 2 in textPanel
        textField.setHorizontalAlignment(JLabel.LEFT);
        textField.setFont(font);
        textField.setForeground(Color.WHITE);
        textField.setText("Bombs: " + countMinesOnField);

        textFieldScore = new JLabel(); // 3 in textPanel
        textFieldScore.setHorizontalAlignment(JLabel.RIGHT);
        textFieldScore.setFont(font);
        textFieldScore.setForeground(Color.WHITE);
        textFieldScore.setText("Score: " + score);

        buttons = new JButton[SIDE][SIDE]; // fill buttonPanel Grid
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons.length; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setFocusable(false);
                buttons[i][j].addActionListener(this);
                buttons[i][j].setFont(font);
                buttons[i][j].setText("");
                buttonPanel.add(buttons[i][j]);
            }
        }

        buttonFlag = new JButton(); // 1 in textPanel
        buttonFlag.setFocusable(false);
        buttonFlag.addActionListener(this);
        buttonFlag.setFont(font);
        buttonFlag.setText(HAND);
        buttonFlag.setVisible(true);

        buttonRestart = new JButton(); // 4 in textPanel
        buttonRestart.setFocusable(false);
        buttonRestart.addActionListener(this);
        buttonRestart.setFont(font);
        buttonRestart.setText("Restart");
        buttonRestart.setVisible(false);

        // fill textPanel
        textPanel.add(buttonFlag);
        textPanel.add(textField);
        textPanel.add(textFieldScore);
        textPanel.add(buttonRestart);

        // fill frame
        frame.add(textPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);

        frame.setSize(60 * SIDE, 60 * SIDE);
        frame.revalidate();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameStopped) {
            if (e.getSource() == buttonFlag) {
                if (isFlagMode) {
                    buttonFlag.setText(HAND);
                    buttonFlag.setForeground(Color.BLACK);
                    buttonFlag.setBackground(STANDART_BUTTON_COLOR);
                    isFlagMode = false;
                } else {
                    buttonFlag.setText(FLAG);
                    buttonFlag.setForeground(Color.RED);
                    buttonFlag.setBackground(Color.gray);
                    isFlagMode = true;
                }
            } else {
                stopLoop:
                for (int i = 0; i < buttons.length; i++) {
                    for (int j = 0; j < buttons.length; j++) {
                        if (e.getSource() == buttons[i][j]) {
                            if (isFlagMode) {
                                markTile(i, j);
                            } else {
                                openTile(i, j);
                            }
                            break stopLoop;
                        }
                    }
                }
            }
        } else {
            restart();
        }
    }

    private void createGame() {
        gameField = new GameObject[SIDE][SIDE];
        countClosedTiles = SIDE * SIDE;

        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                boolean isMine = getRandomNumber() == 0;
                if (isMine) {
                    countMinesOnField++;
                }
                gameField[y][x] = new GameObject(x, y, isMine);
            }
        }
        countMineNeighbors();
        countFlags = countMinesOnField;
    }

    private int getRandomNumber() {
        return (int) (Math.random() * 10);
    }

    private List<GameObject> getNeighbors(GameObject gameObject) {
        List<GameObject> result = new ArrayList<>();
        for (int y = gameObject.y - 1; y <= gameObject.y + 1; y++) {
            for (int x = gameObject.x - 1; x <= gameObject.x + 1; x++) {
                if (y < 0 || y >= SIDE) {
                    continue;
                }
                if (x < 0 || x >= SIDE) {
                    continue;
                }
                if (gameField[y][x] == gameObject) {
                    continue;
                }
                result.add(gameField[y][x]);
            }
        }
        return result;
    }

    private void countMineNeighbors() {
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                GameObject gameObject = gameField[y][x];
                if (!gameObject.isMine) {
                    for (GameObject neighbor : getNeighbors(gameObject)) {
                        if (neighbor.isMine) {
                            gameObject.countMineNeighbors++;
                        }
                    }
                }
            }
        }
    }

    private void openTile(int x, int y) {
        GameObject gameObject = gameField[y][x];
        if (gameObject.isOpen || gameObject.isFlag || isGameStopped) {
            return;
        }
        gameObject.isOpen = true;
        countClosedTiles--;
        buttons[x][y].setEnabled(false);
        if (gameObject.isMine) {
            buttons[x][y].setText(MINE);
            buttons[x][y].setBackground(Color.RED);
            gameOver();
            return;
        } else if (gameObject.countMineNeighbors == 0) {
            List<GameObject> neighbors = getNeighbors(gameObject);
            for (GameObject neighbor : neighbors) {
                if (!neighbor.isOpen) {
                    openTile(neighbor.x, neighbor.y);
                }
            }
        } else {
            buttons[x][y].setText(String.valueOf(gameObject.countMineNeighbors));
        }
        score += 5;
        textFieldScore.setText("Score: " + score);

        if (countClosedTiles == countMinesOnField) {
            win();
        }
    }

    private void markTile(int x, int y) {
        GameObject gameObject = gameField[y][x];

        if (gameObject.isOpen || isGameStopped || (countFlags == 0 && !gameObject.isFlag)) {
            return;
        }

        if (gameObject.isFlag) {
            countFlags++;
            gameObject.isFlag = false;
            buttons[x][y].setText("");
            buttons[x][y].setForeground(Color.BLACK);
        } else {
            countFlags--;
            gameObject.isFlag = true;
            buttons[x][y].setText(FLAG);
            buttons[x][y].setForeground(Color.RED);
        }
    }

    private void gameOver() {
        isGameStopped = true;
        textField.setForeground(Color.RED);
        textField.setText("GAME OVER");
        textPanel.add(buttonRestart);
        textFieldScore.setVisible(false);
        buttonFlag.setVisible(false);
        buttonRestart.setVisible(true);
        disableAllButtons();
    }

    private void win() {
        isGameStopped = true;
        textField.setForeground(Color.GREEN);
        textField.setText("YOU WIN");
        buttonFlag.setVisible(false);
        buttonRestart.setVisible(true);
        disableAllButtons();
    }

    private void restart() {
        isGameStopped = false;
        isFlagMode = false;
        countClosedTiles = SIDE * SIDE;
        score = 0;
        countMinesOnField = 0;
        createGame();

        textField.setText("Bombs: " + countMinesOnField);
        textField.setForeground(Color.BLUE);
        textFieldScore.setText("Score: " + score);
        textFieldScore.setVisible(true);
        buttonFlag.setText(HAND);
        buttonFlag.setVisible(true);
        buttonRestart.setVisible(false);
        enableAllButtons();
    }

    private void enableAllButtons() {
        for (JButton[] button : buttons) {
            for (int j = 0; j < buttons.length; j++) {
                button[j].setText("");
                button[j].setEnabled(true);
                button[j].setBackground(STANDART_BUTTON_COLOR);
            }
        }
    }

    private void disableAllButtons() {
        for (JButton[] button : buttons) {
            for (int j = 0; j < buttons.length; j++) {
                button[j].setEnabled(false);
            }
        }
    }
}