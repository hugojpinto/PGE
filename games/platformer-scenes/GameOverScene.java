import pge.Engine;
import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.input.Input;
import pge.input.Key;
import pge.scene.Scene;
import pge.ui.Box;
import pge.ui.Button;
import pge.ui.Label;

/**
 * Shown when the hero has no hearts left. The level passes in how it went.
 */
public class GameOverScene extends Scene {

    private final int coinsCollected;
    private final int coinsTotal;
    private Button retryButton;
    private Button menuButton;

    public GameOverScene(int coinsCollected, int coinsTotal) {
        this.coinsCollected = coinsCollected;
        this.coinsTotal = coinsTotal;
    }

    @Override
    public void start() {
        Sounds.gameOver.play();
        int middle = getScreenWidth() / 2;

        Box panel = new Box(middle - 120, 90, 240, 170);
        panel.setFillColor(Color.DARK_BLUE);
        addUI(panel);

        Label title = new Label("GAME OVER", middle, 110);
        title.setScale(3);
        title.setCentered(true);
        title.setColor(Color.RED);
        title.setShadowColor(Color.BLACK);
        addUI(title);

        Label result = new Label("You found " + coinsCollected + " of " + coinsTotal + " coins", middle, 150);
        result.setCentered(true);
        addUI(result);

        retryButton = new Button("Try again", middle - 55, 180, 110, 24);
        addUI(retryButton);

        menuButton = new Button("Menu", middle - 55, 214, 110, 24);
        addUI(menuButton);
    }

    @Override
    public void update() {
        if (retryButton.wasClicked() || Input.wasPressed(Key.ENTER)) {
            Sounds.click.play();
            Engine.setScene(new LevelScene());
        }
        if (menuButton.wasClicked() || Input.wasPressed(Key.ESCAPE)) {
            Sounds.click.play();
            Engine.setScene(new TitleScene());
        }
        if (Input.wasPressed(Key.M)) {
            Sounds.toggleMute();
        }
    }

    @Override
    public void drawBackground(Canvas canvas) {
        Scenery.draw(canvas, 0, 0);
    }
}
