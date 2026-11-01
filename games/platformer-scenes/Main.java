import pge.Engine;

/**
 * The platformer again, built with Scenes and GameObjects instead of one big Game class.
 * Compare it with games/platformer, which does the same with the plain Game interface.
 *
 *   TitleScene     the menu
 *   LevelScene     the level: loads the map and turns marker tiles into objects
 *   GameOverScene  shown when the hero runs out of hearts
 *
 *   Hero, Slime, Coin   the objects; each one looks after itself in its own update()
 *   Scenery             the sky and hills, shared by all scenes
 */
public class Main {

    public static void main(String[] args) {
        Engine.run(new TitleScene(), 640, 360, "PGE Platformer (scenes)");
    }
}
