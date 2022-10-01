package flappyBird;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.Timer;

public class FlappyBird implements ActionListener, MouseListener, KeyListener
{

    protected static FlappyBird flappyBird;
    private final int WIDTH = 800, HEIGHT = 800;
    private final Renderer renderer;
    private final ArrayList<Rectangle> columns;
    private Rectangle bird;

    private int[] highScores = new int[]{0, 0, 0, 0 ,0};
    private int ticks, yMotion, score;
    private boolean gameOver, started;
    private final Random rand;

    private FlappyBird() {
        // initialize interface
        JFrame jframe = new JFrame();
        Timer timer = new Timer(20, this);

        this.renderer = new Renderer();
        this.rand = new Random();

        jframe.add(this.renderer);
        jframe.setTitle("Flappy Bird");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(this.WIDTH, this.HEIGHT);
        jframe.addMouseListener(this);
        jframe.addKeyListener(this);
        jframe.setResizable(false);
        jframe.setVisible(true);

        // create bird
        this.bird = new Rectangle(this.WIDTH / 2 - 10, this.HEIGHT / 2 - 10, 20, 20);

        // intialize column array to generate columns on screen
        this.columns = new ArrayList<>();
        for (int i = 0; i <= 4; i ++){
            this.addColumn(true);
        }

        timer.start();
    }

    private void addColumn(boolean start) {
        int space = 300;
        int width = 100;
        int height = 50 + this.rand.nextInt(300);

        // generate two top/bottom columns when game intializes
        if (start) {
            this.columns.add(new Rectangle(this.WIDTH + width + this.columns.size() * 300, this.HEIGHT - height - 120, width, height));
            this.columns.add(new Rectangle(this.WIDTH + width + (this.columns.size() - 1) * 300, 0, width, this.HEIGHT - height - space));
        }
        // generate top/bottom columns as game progresses
        else {
            this.columns.add(new Rectangle(this.columns.get(this.columns.size() - 1).x + 600, this.HEIGHT - height - 120, width, height));
            this.columns.add(new Rectangle(this.columns.get(this.columns.size() - 1).x, 0, width, this.HEIGHT - height - space));
        }
    }

    private void paintColumn(Graphics g, Rectangle column) {
        g.setColor(Color.green.darker());
        g.fillRect(column.x, column.y, column.width, column.height);
    }

    private void jump() {
        // control bird movement when game starts
        if (!this.started) {
            this.started = true;
        }
        else {
            if (this.yMotion > 0) {
                this.yMotion = 0;
            }

            this.yMotion -= 7;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int speed = 10;

        this.ticks++;

        // start game
        if (this.started) {
            // move columns across screen by changing x coord
            for (Rectangle column : this.columns) {
                column.x -= speed;

            }

            // control amount at which bird moves up and down
            if (this.ticks % 2 == 0 && this.yMotion < 15) {
                this.yMotion += 2;
            }

            // remove columns as bird passes them
            for (int i = 0; i < this.columns.size(); i++) {
                Rectangle column = this.columns.get(i);

                if (column.x + column.width < 0) {
                    this.columns.remove(column);

                    if (column.y == 0) {
                        this.addColumn(false);
                    }
                }
            }

            this.bird.y += this.yMotion;

            // increase score if bird passes column completely
            for (Rectangle column : this.columns) {
                if (column.y == 0 && this.bird.x + this.bird.width / 2 > column.x + column.width / 2 - 10 && this.bird.x + this.bird.width / 2 < column.x + column.width / 2 + 10) {
                    this.score++;
                }

                if (column.intersects(this.bird)) {
                    this.gameOver = true;

                    if (this.bird.x <= column.x) {
                        this.bird.x = column.x - this.bird.width;

                    }
                    else {
                        if (column.y != 0) {
                            this.bird.y = column.y - this.bird.height;
                        }
                        else if (this.bird.y < column.height) {
                            this.bird.y = column.height;
                        }
                    }
                }
            }

            // check if bird hits the column or falls
            if (this.bird.y > this.HEIGHT - 120 || this.bird.y < 0) {
                this.gameOver = true;
            }

            if (this.bird.y + this.yMotion >= this.HEIGHT - 120) {
                this.bird.y = this.HEIGHT - 120 - this.bird.height;
                this.gameOver = true;
            }
            this.renderer.repaint();
        }
    }

    protected void repaintGraphics(Graphics g) {
        g.setColor(Color.cyan);
        g.fillRect(0, 0, this.WIDTH, this.HEIGHT);

        g.setColor(Color.orange);
        g.fillRect(0, this.HEIGHT - 120, this.WIDTH, 120);

        g.setColor(Color.green);
        g.fillRect(0, this.HEIGHT - 120, this.WIDTH, 20);

        g.setColor(Color.red);
        g.fillRect(this.bird.x, this.bird.y, this.bird.width, this.bird.height);

        for (Rectangle column : this.columns) {
            this.paintColumn(g, column);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 100));

        if (!this.started) {
            g.drawString("Click to start!", 75, this.HEIGHT / 2 - 50);
        }

        if (!this.gameOver && this.started) {
            g.drawString(String.valueOf(this.score), this.WIDTH / 2 - 25, 100);
        }
    }

    protected void scoreText(Graphics g){
        if (this.gameOver){
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 75));
            g.drawString("Game Over!", 100, this.HEIGHT / 2 - 300 );
            String curScore = Integer.toString(this.score);
            g.drawString("Current Score:" + curScore, 100, 200);
            g.drawString("High Scores:",100, 300);
            int yPos = 370;
            for (int i = 4; i >= 0; i--){
                if (this.highScores[i] != 0){
                    g.drawString(Integer.toString(this.highScores[i]), 400, yPos);
                    yPos += 70;
                }
            }
        }
    }
    private void start(){
        if (this.gameOver) {
            if (this.score > this.highScores[0]){
                this.highScores[0] = this.score;
            }
            Arrays.sort(this.highScores);

            this.bird = new Rectangle(this.WIDTH / 2 - 10, this.HEIGHT / 2 - 10, 20, 20);
            this.columns.clear();
            this.yMotion = 0;
            this.score = 0;

            for (int i = 0; i <= 4; i ++){
                this.addColumn(true);
            }

            this.gameOver = false;
        }
    }

    public static void main(String[] args) {
        flappyBird = new FlappyBird();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // move bird with left click
        this.started = true;
        this.start();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // move bird with space bar
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!this.gameOver){
                this.jump();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

}