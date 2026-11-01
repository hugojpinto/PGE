import pge.Engine;
import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.gfx.SpriteSheet;
import pge.input.Input;
import pge.input.Key;
import pge.scene.Scene;
import pge.ui.Box;
import pge.ui.Button;
import pge.ui.Label;
import pge.ui.Picture;
import pge.world.TileMap;

/**
 * The level. It loads the map, turns marker tiles into objects, keeps the score
 * and shows it. The objects (Hero, Slime, Coin) do the rest themselves.
 */
public class LevelScene extends Scene {

    // Tile numbers in assets/tiles.png
    static final int GRASS = 0;
    static final int DIRT = 1;
    static final int BRICK = 2;
    static final int STONE = 3;
    static final int COIN_MARKER = 7;
    static final int SLIME_MARKER = 8;
    static final int HERO_MARKER = 9;

    static final int MAX_HEARTS = 3;

    private TileMap level;
    private Hero hero;

    private int coinsTotal = 0;
    private int coinsCollected = 0;
    private int hearts = MAX_HEARTS;

    private Label coinLabel;
    private Picture[] heartPictures = new Picture[MAX_HEARTS];
    private Box winPanel;
    private Label winLabel;
    private Button againButton;

    @Override
    public void start() {
        Sounds.loadAll();    // already loaded by the title screen, unless a level starts first
        SpriteSheet tiles = SpriteSheet.load("assets/tiles.png", 16, 16);
        SpriteSheet sprites = SpriteSheet.load("assets/sprites.png", 16, 16);

        addLayer(TileMap.load("assets/level1-back.csv", tiles));
        level = TileMap.load("assets/level1.csv", tiles);
        level.setSolid(GRASS, true);
        level.setSolid(DIRT, true);
        level.setSolid(BRICK, true);
        level.setSolid(STONE, true);
        addLayer(level);

        // Turn every marker tile into a real object, and clear the tile. This lets us
        // place coins, slimes and the hero with the same level file and editor as the
        // walls. (The Tiled editor has "object layers" for the same job; marker tiles
        // are a simpler way to do it.)
        for (int row = 0; row < level.getRows(); row++) {
            for (int column = 0; column < level.getColumns(); column++) {
                int tile = level.getTile(column, row);
                int x = column * level.getTileWidth();
                int y = row * level.getTileHeight();
                if (tile == COIN_MARKER) {
                    add(new Coin(x, y, sprites));
                    coinsTotal = coinsTotal + 1;
                } else if (tile == SLIME_MARKER) {
                    add(new Slime(x, y, sprites));
                } else if (tile == HERO_MARKER) {
                    hero = new Hero(x, y, sprites);
                    add(hero);
                }
                if (tile == COIN_MARKER || tile == SLIME_MARKER || tile == HERO_MARKER) {
                    level.setTile(column, row, TileMap.EMPTY);
                }
            }
        }
        if (hero == null) {
            throw new IllegalStateException("The level has no hero marker (tile " + HERO_MARKER + ")");
        }

        getCamera().setLimits(level.getWidth(), level.getHeight());
        getCamera().setSmoothness(0.15);
        getCamera().setDeadZone(40, 160);    // jumps don't move the camera up and down
        getCamera().centerOn(hero.getCenterX(), hero.getCenterY());

        buildUI(sprites);
    }

    private void buildUI(SpriteSheet sprites) {
        Box scorePanel = new Box(4, 4, 112, 24);
        addUI(scorePanel);
        addUI(new Picture(sprites.get(8), 8, 8));
        coinLabel = new Label("", 26, 12);
        addUI(coinLabel);
        for (int i = 0; i < MAX_HEARTS; i++) {
            heartPictures[i] = new Picture(sprites.get(12), 62 + i * 17, 8);
            addUI(heartPictures[i]);
        }
        updateScoreText();

        Label help = new Label("Arrows: run   Space: jump   M: sound   Esc: menu", 6, getScreenHeight() - 12);
        help.setShadowColor(Color.BLACK);
        addUI(help);

        int middle = getScreenWidth() / 2;
        winPanel = new Box(middle - 120, 100, 240, 110);
        winPanel.setFillColor(Color.DARK_BLUE);
        winLabel = new Label("ALL COINS!", middle, 120);
        winLabel.setScale(3);
        winLabel.setCentered(true);
        winLabel.setColor(Color.YELLOW);
        winLabel.setShadowColor(Color.BLACK);
        againButton = new Button("Play again", middle - 55, 165, 110, 24);
        addUI(winPanel);
        addUI(winLabel);
        addUI(againButton);
        winPanel.setVisible(false);
        winLabel.setVisible(false);
        againButton.setVisible(false);
    }

    @Override
    public void update() {
        getCamera().follow(hero.getCenterX(), hero.getCenterY());

        if (Input.wasPressed(Key.ESCAPE)) {
            Engine.setScene(new TitleScene());
        }
        if (Input.wasPressed(Key.R) || againButton.wasClicked()) {
            Engine.setScene(new LevelScene());
        }
        if (Input.wasPressed(Key.M)) {
            Sounds.toggleMute();
        }
    }

    @Override
    public void drawBackground(Canvas canvas) {
        Scenery.draw(canvas, getCamera().getX(), getCamera().getY());
    }

    // ================================================================== called by the objects

    /** The map, so objects can ask where the walls are. */
    public TileMap getLevel() {
        return level;
    }

    public Hero getHero() {
        return hero;
    }

    /** A Coin calls this when the hero picks it up. */
    public void coinCollected() {
        coinsCollected = coinsCollected + 1;
        updateScoreText();
        if (coinsCollected == coinsTotal) {
            Sounds.win.play();
            winPanel.setVisible(true);
            winLabel.setVisible(true);
            againButton.setVisible(true);
        }
    }

    /**
     * The Hero calls this when a slime hurts it. The hero needs a cast to call it:
     * (LevelScene) getScene(), because getScene() only knows it is some kind of Scene.
     */
    public void heroHurt() {
        hearts = hearts - 1;
        updateScoreText();
        Sounds.hurt.play();
        if (hearts <= 0) {
            Engine.setScene(new GameOverScene(coinsCollected, coinsTotal));
        }
    }

    private void updateScoreText() {
        coinLabel.setText(coinsCollected + "/" + coinsTotal);
        for (int i = 0; i < MAX_HEARTS; i++) {
            heartPictures[i].setVisible(i < hearts);
        }
    }
}
