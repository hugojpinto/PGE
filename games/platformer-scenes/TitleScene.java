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
 * The menu: a title and two buttons. Nothing here moves, so it has no game objects,
 * only UI elements.
 */
public class TitleScene extends Scene {

    private Button playButton;
    private Button quitButton;
    private int scroll = 0;

    @Override
    public void start() {
        Sounds.loadAll();
        int middle = getScreenWidth() / 2;

        Box panel = new Box(middle - 130, 70, 260, 200);
        panel.setFillColor(Color.DARK_BLUE);
        addUI(panel);

        Label title = new Label("COIN CLIMBER", middle, 95);
        title.setScale(3);
        title.setCentered(true);
        title.setColor(Color.YELLOW);
        title.setShadowColor(Color.BLACK);
        addUI(title);

        Label subtitle = new Label("Collect every coin. Jump on slimes!", middle, 135);
        subtitle.setCentered(true);
        addUI(subtitle);

        playButton = new Button("Play", middle - 50, 170, 100, 24);
        addUI(playButton);

        quitButton = new Button("Quit", middle - 50, 204, 100, 24);
        addUI(quitButton);

        Label hint = new Label("or press Enter.  M: sound on/off", middle, 245);
        hint.setCentered(true);
        hint.setColor(Color.GRAY);
        addUI(hint);
    }

    @Override
    public void update() {
        scroll = scroll + 1;    // slowly slide the hills along, to make the menu feel alive

        if (playButton.wasClicked() || Input.wasPressed(Key.ENTER)) {
            Sounds.click.play();
            Engine.setScene(new LevelScene());
        }
        if (quitButton.wasClicked() || Input.wasPressed(Key.ESCAPE)) {
            Engine.stop();
        }
        if (Input.wasPressed(Key.M)) {
            Sounds.toggleMute();
        }
    }

    @Override
    public void drawBackground(Canvas canvas) {
        Scenery.draw(canvas, scroll, 0);
    }
}
