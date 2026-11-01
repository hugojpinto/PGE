import pge.Engine;
import pge.Game;
import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.input.Input;
import pge.input.Key;

/**
 * M0 demo: a world bigger than the screen, a camera that follows the player,
 * and a little screen-space UI on top.
 *
 *   Arrows or WASD   move
 *   Space            change color
 *   Escape           quit
 *   F11 / Alt+Enter  fullscreen
 *   F3               speed statistics in the title bar
 */
public class Main implements Game {

    static final int WORLD_WIDTH = 2000;
    static final int WORLD_HEIGHT = 1200;
    static final int GRID_SIZE = 100;
    static final int STAR_COUNT = 400;

    // The player
    double playerX = 300;
    double playerY = 200;
    double playerSpeed = 3;
    int playerSize = 16;
    int[] playerColors = { Color.YELLOW, Color.GREEN, Color.PINK, Color.CYAN };
    int colorIndex = 0;

    // A ball that bounces around the world on its own
    double ballX = 900;
    double ballY = 500;
    double ballSpeedX = 2.5;
    double ballSpeedY = 1.75;
    int ballRadius = 20;

    // Background stars: their positions in the world
    int[] starX = new int[STAR_COUNT];
    int[] starY = new int[STAR_COUNT];

    public static void main(String[] args) {
        Engine.run(new Main(), 640, 360, "PGE Demo");
    }

    @Override
    public void init() {
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = (int) (Math.random() * WORLD_WIDTH);
            starY[i] = (int) (Math.random() * WORLD_HEIGHT);
        }
    }

    @Override
    public void update() {
        // Move the player
        if (Input.isDown(Key.LEFT) || Input.isDown(Key.A)) {
            playerX = playerX - playerSpeed;
        }
        if (Input.isDown(Key.RIGHT) || Input.isDown(Key.D)) {
            playerX = playerX + playerSpeed;
        }
        if (Input.isDown(Key.UP) || Input.isDown(Key.W)) {
            playerY = playerY - playerSpeed;
        }
        if (Input.isDown(Key.DOWN) || Input.isDown(Key.S)) {
            playerY = playerY + playerSpeed;
        }
        // Stay inside the world
        playerX = clamp(playerX, 0, WORLD_WIDTH - playerSize);
        playerY = clamp(playerY, 0, WORLD_HEIGHT - playerSize);

        if (Input.wasPressed(Key.SPACE)) {
            colorIndex = (colorIndex + 1) % playerColors.length;
        }
        if (Input.wasPressed(Key.ESCAPE)) {
            Engine.stop();
        }

        // Move the ball and bounce off the edges of the world
        ballX = ballX + ballSpeedX;
        ballY = ballY + ballSpeedY;
        if (ballX < ballRadius || ballX > WORLD_WIDTH - ballRadius) {
            ballSpeedX = -ballSpeedX;
        }
        if (ballY < ballRadius || ballY > WORLD_HEIGHT - ballRadius) {
            ballSpeedY = -ballSpeedY;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.clear(Color.DARK_BLUE);

        // ---- World space: point the camera so the player is in the middle of the screen
        int cameraX = (int) playerX + playerSize / 2 - canvas.getWidth() / 2;
        int cameraY = (int) playerY + playerSize / 2 - canvas.getHeight() / 2;
        cameraX = (int) clamp(cameraX, 0, WORLD_WIDTH - canvas.getWidth());
        cameraY = (int) clamp(cameraY, 0, WORLD_HEIGHT - canvas.getHeight());
        canvas.setCamera(cameraX, cameraY);

        for (int i = 0; i < STAR_COUNT; i++) {
            canvas.setPixel(starX[i], starY[i], Color.LIGHT_GRAY);
        }
        for (int x = 0; x <= WORLD_WIDTH; x = x + GRID_SIZE) {
            canvas.drawLine(x, 0, x, WORLD_HEIGHT, Color.DARK_GRAY);
        }
        for (int y = 0; y <= WORLD_HEIGHT; y = y + GRID_SIZE) {
            canvas.drawLine(0, y, WORLD_WIDTH, y, Color.DARK_GRAY);
        }
        canvas.drawRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT, Color.RED);

        // A line from the player to the ball (drawn first, so the ball covers its end)
        canvas.drawLine((int) playerX + playerSize / 2, (int) playerY + playerSize / 2,
                (int) ballX, (int) ballY, Color.PURPLE);
        canvas.fillCircle((int) ballX, (int) ballY, ballRadius, Color.ORANGE);
        canvas.drawCircle((int) ballX, (int) ballY, ballRadius, Color.WHITE);

        canvas.fillRect((int) playerX, (int) playerY, playerSize, playerSize, playerColors[colorIndex]);
        canvas.drawRect((int) playerX, (int) playerY, playerSize, playerSize, Color.WHITE);

        // ---- Screen space: the camera is off, these stay put
        canvas.resetCamera();
        drawColorPicker(canvas);
        drawMiniMap(canvas);
    }

    /** Top-left: the four colors, with a frame around the chosen one. */
    void drawColorPicker(Canvas canvas) {
        canvas.fillRect(4, 4, 4 + playerColors.length * 14, 18, Color.BLACK);
        canvas.drawRect(4, 4, 4 + playerColors.length * 14, 18, Color.GRAY);
        for (int i = 0; i < playerColors.length; i++) {
            int x = 8 + i * 14;
            canvas.fillRect(x, 8, 10, 10, playerColors[i]);
            if (i == colorIndex) {
                canvas.drawRect(x - 2, 6, 14, 14, Color.WHITE);
            }
        }
    }

    /** Bottom-right: the whole world shrunk 20 times. */
    void drawMiniMap(Canvas canvas) {
        int shrink = 20;
        int mapWidth = WORLD_WIDTH / shrink;
        int mapHeight = WORLD_HEIGHT / shrink;
        int left = canvas.getWidth() - mapWidth - 6;
        int top = canvas.getHeight() - mapHeight - 6;

        canvas.fillRect(left, top, mapWidth, mapHeight, Color.BLACK);
        canvas.drawRect(left - 1, top - 1, mapWidth + 2, mapHeight + 2, Color.GRAY);
        canvas.fillCircle(left + (int) ballX / shrink, top + (int) ballY / shrink, 1, Color.ORANGE);
        canvas.fillRect(left + (int) playerX / shrink, top + (int) playerY / shrink, 2, 2, playerColors[colorIndex]);
    }

    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
