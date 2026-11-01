# The PGE Guide

This guide takes you from "I have never used PGE" to "I added a feature to the
engine". Read it in order the first time; later, jump to the part you need.

1. [Getting started](#1-getting-started)
2. [Your first game](#2-your-first-game)
3. [How a game runs](#3-how-a-game-runs)
4. [Pictures, animation and text](#4-pictures-animation-and-text)
5. [Levels, walls and the camera](#5-levels-walls-and-the-camera)
6. [Bigger games: scenes and game objects](#6-bigger-games-scenes-and-game-objects)
7. [Handing in your game](#7-handing-in-your-game)
8. [When something goes wrong](#8-when-something-goes-wrong)
9. [Inside the engine](#9-inside-the-engine)
10. [Extending the engine](#10-extending-the-engine)

---

## 1. Getting started

You need **Java 25** (a JDK, which includes the compiler). Check in a terminal:

```sh
java -version
```

It should say 25 or higher. Then, from the PGE folder:

```sh
java Build.java demo
```

This compiles the engine and every game, then runs `games/demo`. Try the others too:
`platformer`, `platformer-scenes` and `starter`.

`Build.java` is the whole build system: a normal Java program that calls the Java
compiler. Open it and read it; there is no magic in it either.

### Using an editor

Any editor works, since you build from the terminal. For autocompletion in
**IntelliJ IDEA** or **VS Code** (with the "Extension Pack for Java"), tell the
editor where the source code is:

- `engine/src`, the engine;
- `games/yourgame`, the game you are working on.

Add only **one** game folder at a time: every game has its own `Main` class, and an
editor that sees two of them gets confused. In IntelliJ, set the project SDK to Java
25 and mark both folders as *Sources Root*. In VS Code, add them to
`java.project.sourcePaths` in `.vscode/settings.json`.

---

## 2. Your first game

Copy `games/starter` to a new folder, say `games/mygame`, and run it:

```sh
java Build.java mygame
```

You get a yellow square you can move with the arrows. Open `games/mygame/Main.java`.
It has three methods, and the engine calls them for you:

| Method | When | What to do there |
|---|---|---|
| `init()` | once, at the start | load pictures, set things up |
| `update()` | 60 times per second | move things, check rules, read the keyboard |
| `draw(canvas)` | after every update | paint everything onto the canvas |

Now change things, one at a time, running the game after each change:

1. **Make the player faster.** Change `playerSpeed`.
2. **Change the colors.** Try `Color.PINK`, or any color as a number: `0xFF8800` is
   orange (red FF, green 88, blue 00).
3. **Keep the player on the screen.** In `update()`, after moving:
   ```java
   if (playerX < 0) {
       playerX = 0;
   }
   if (playerX > Engine.getWidth() - 16) {
       playerX = Engine.getWidth() - 16;
   }
   ```
   (16 is the size of the square.) Do the same for `playerY` with `Engine.getHeight()`.
4. **Add something to collect.** Add `coinX` and `coinY` fields, draw a circle there
   with `canvas.fillCircle(...)`, and in `update()` check whether the player is
   close to it. If so, add a point and move the coin somewhere random:
   `coinX = Math.random() * 600;`.
5. **Add a timer.** Count updates in a field; 60 updates is one second. Show the time
   left with `canvas.drawText(...)`, and stop the game when it reaches zero.

At that point you have a real game. Everything after this is about making it look
better and grow bigger without becoming a mess.

---

## 3. How a game runs

### Update, then draw

The engine runs `update()` exactly 60 times per second on every computer, and calls
`draw()` after each one. Keep the two jobs apart:

- `update()` **changes** the game (positions, scores, timers) and never draws.
- `draw()` **shows** the game and never changes it.

Because the rate is fixed, `x = x + 2` always means "2 pixels per update", or 120
pixels per second. You never need to measure time.

### Coordinates

The game picture is 640 x 360 pixels (you choose this in `Engine.run`). `(0, 0)` is
the **top-left** corner. `x` grows to the right and **`y` grows downward**, so
"jumping up" means making `y` smaller.

The window is bigger than the picture: the engine scales it up 2x, 3x, 4x... so
every pixel stays a crisp square. Your code never needs to know the window size.

### World and screen

When the world is bigger than the screen, the **camera** decides which part is
visible. Things in the world (player, enemies, the level) are drawn with the camera
on; the score and menus are drawn with it off, so they stay put:

```java
canvas.setCamera(cameraX, cameraY);   // world
canvas.drawBitmap(hero, heroX, heroY);
canvas.resetCamera();                 // screen
canvas.drawText("Score: " + score, 4, 4, Color.WHITE);
```

The `Camera` class (section 5) does the arithmetic for you.

### Keyboard and mouse

| Question | Code |
|---|---|
| Is the key held down right now? | `Input.isDown(Key.LEFT)` |
| Was it pressed just now? (once per press) | `Input.wasPressed(Key.SPACE)` |
| Was it let go just now? | `Input.wasReleased(Key.SPACE)` |
| Where is the mouse? | `Input.getPointerX()`, `Input.getPointerY()` |
| Was it clicked just now? | `Input.wasPointerPressed()` |

Use `isDown` for things that last (walking) and `wasPressed` for things that happen
once (jumping, shooting, choosing a menu item).

The engine has three keys of its own: **F11** (or Alt+Enter) for fullscreen, and
**F3** to show how many milliseconds each frame takes. You have about 16.7 ms per
frame; PGE's example games use about 2.

---

## 4. Pictures, animation and text

### Loading pictures

Put PNG files in your game's `assets` folder.

```java
Bitmap hero = Bitmap.load("assets/hero.png");
```

See-through parts of the PNG are not drawn. Old-style art that uses a background
color (often bright pink) instead works too:
`Bitmap.load("assets/hero.png", 0xFF00FF)`.

Games usually keep many small pictures in one image, a **sprite sheet**:

```java
SpriteSheet sheet = SpriteSheet.load("assets/sprites.png", 16, 16);   // cells of 16x16
Bitmap hero = sheet.get(0);       // the first cell
Bitmap coin = sheet.get(8);       // cells count left to right, then down
```

Look at `games/demo/assets/sprites.png` to see how it is laid out. Draw your own
art with any pixel editor: [Piskel](https://www.piskelapp.com/) is free and runs in
the browser, [Aseprite](https://www.aseprite.org/) is the favorite of many pixel artists.

### Drawing

```java
canvas.drawBitmap(hero, x, y);                     // as it is
canvas.drawBitmap(hero, x, y, true, false);        // mirrored left-right (walking left)
canvas.drawSilhouette(hero, x, y, false, Color.WHITE);   // one solid color: a "hit" flash
```

Positions are whole numbers (`int`). If you keep positions as `double` (smoother
movement), convert when drawing: `(int) heroX`.

### Animation

An `Animation` is a list of pictures shown one after the other:

```java
Animation walk = new Animation(sheet.getFrames(0, 4), 8);   // cells 0-3, 8 updates each

// in update():
walk.update();

// in draw():
canvas.drawBitmap(walk.getFrame(), x, y);
```

### Text

```java
canvas.drawText("Score: " + score, 4, 4, Color.WHITE);
canvas.drawText("GAME OVER", 200, 150, Color.RED, 4);   // 4 times bigger
int width = canvas.getTextWidth("GAME OVER", 4);        // for centering
```

The font is `engine/src/pge/gfx/font.png`. It has the English letters, digits and
punctuation; letters like `é` or `ç` show as a box. Fix that as an exercise
(section 10).

---

## 5. Levels, walls and the camera

### Tile maps

A level is a grid of tiles, stored as a text file of numbers (CSV), one line per row:

```
-1,-1,-1,-1
-1, 3, 3,-1
 0, 0, 0, 0
```

Each number is a cell of a sprite sheet (the *tile set*); `-1` is empty.

```java
SpriteSheet tiles = SpriteSheet.load("assets/tiles.png", 16, 16);
TileMap level = TileMap.load("assets/level1.csv", tiles);
level.setSolid(0, true);    // tile 0 is a wall
level.setSolid(3, true);

// in draw(), with the camera on:
level.draw(canvas);
```

Editing numbers by hand gets old fast. The free [Tiled](https://www.mapeditor.org/)
editor lets you paint levels, then **File > Export As > CSV** writes one file per
layer in exactly this format.

### Stopping at walls

Ask the map whether a rectangle touches a wall:

```java
level.overlapsSolid(x, y, width, height)
```

The simplest movement that never goes through walls: **move one pixel at a time,
and before each step, check whether the next position is free.** Find
`moveHorizontally` in `games/platformer/Main.java`: it is about 15 lines, and worth
understanding line by line. Gravity is just a speed that grows every update:

```java
speedY = speedY + GRAVITY;
moveVertically(speedY);
```

### The camera

```java
Camera camera = new Camera(Engine.getWidth(), Engine.getHeight());
camera.setLimits(level.getWidth(), level.getHeight());   // never show outside the level
camera.setSmoothness(0.15);                              // glide instead of jump
camera.setDeadZone(40, 160);                             // ignore small moves, like jumps

// in update():
camera.follow(heroX + 8, heroY + 8);    // the middle of the hero

// in draw():
camera.applyTo(canvas);
level.draw(canvas);
...
canvas.resetCamera();
```

---

## 6. Bigger games: scenes and game objects

With one `Game` class, everything lives in one file: fine for small games, messy
for big ones. PGE's second way to build a game splits it up:

- a **`Scene`** is one screen: the menu, a level, game over;
- a **`GameObject`** is one thing in a scene: the hero, a slime, a coin.

Each object takes care of itself in its own `update()`, and the scene updates and
draws all of them for you.

**Compare `games/platformer` with `games/platformer-scenes`.** It is the same game
written both ways. Notice how `Hero.java` contains the movement code that used to be
in the middle of `Main.java`.

### A game object

```java
public class Coin extends GameObject {

    public Coin(double x, double y, SpriteSheet sprites) {
        super(x, y);
        setName("coin");
        setAnimation(new Animation(sprites.getFrames(8, 4), 7));
    }

    @Override
    public void update() {
        LevelScene level = (LevelScene) getScene();
        if (overlaps(level.getHero())) {
            remove();
            level.coinCollected();
        }
    }
}
```

| You want | Use |
|---|---|
| a picture | `setSprite(bitmap)` or `setAnimation(animation)`; the scene draws it |
| to move | `move(dx, dy)`, `setPosition(x, y)`, `getX()`, `getY()` |
| to know if two objects touch | `overlaps(other)`; make the box smaller with `setHitbox(...)` |
| to find other objects | `getScene().find("hero")`, `getScene().findAll("slime")` |
| to disappear | `remove()` |
| to be drawn on top | `setDepth(10)` (higher is in front) |

`getScene()` returns a plain `Scene`. To call your own methods, like
`coinCollected()`, *cast* it to your scene class: `(LevelScene) getScene()`.

### A scene

```java
public class LevelScene extends Scene {

    @Override
    public void start() {                      // once: build the scene
        addLayer(TileMap.load("assets/level1.csv", tiles));
        add(new Hero(32, 300, sprites));
        add(new Coin(100, 280, sprites));
        addUI(new Label("Collect the coins!", 4, 4));
    }

    @Override
    public void update() {                     // every update, after all objects
        getCamera().follow(hero.getCenterX(), hero.getCenterY());
    }
}
```

Start the game with a scene, and switch screens at any time:

```java
Engine.run(new TitleScene(), 640, 360, "My Game");
Engine.setScene(new LevelScene());
```

### Menus and buttons

UI elements are drawn on top of everything, in screen coordinates:

```java
Button play = new Button("Play", 270, 170, 100, 24);
addUI(play);

// in update():
if (play.wasClicked()) {
    Engine.setScene(new LevelScene());
}
```

Also available: `Box` (a panel), `Label` (text, with `setScale`, `setCentered`,
`setShadowColor`), `Picture` (an icon, like a heart for each life). Hide and show
any of them with `setVisible(...)`. `TitleScene.java` is a complete menu.

---

## 7. Handing in your game

```sh
java Build.java package mygame
```

This makes `dist/mygame/` (and the same as `dist/mygame.zip`) with:

- `mygame.jar`: the engine and your game in one file;
- `assets/`: your pictures and levels;
- `run.sh` and `run.bat`: double-click (or run) these to start the game;
- `README.txt`: how to play.

The person running it needs Java 25 too. Check what your course asks for: some want
the source code instead, which is everything in `engine/src` and `games/mygame`.

---

## 8. When something goes wrong

When the game crashes, the window closes and the terminal shows a **stack trace**.
Read it from the top: the first line says what went wrong, and the first line that
mentions one of *your* files says where.

| Message or symptom | Usual cause |
|---|---|
| `Could not find the image 'assets/hero.png'` | Typo in the name, or the file is not in your game's `assets` folder. Names are case-sensitive: `Hero.png` is not `hero.png`. |
| `NullPointerException` | Something was never given a value: a `Bitmap` field you forgot to load in `init()`, or `find("hero")` with a name nobody has. |
| `drawBitmap was given null` | Same thing: the picture was never loaded. |
| Nothing moves | The change is in `draw()` instead of `update()`, or `update()` never reaches it (check your `if`s). |
| It moves on its own forever | A speed that should be reset each update (like `runDistance`) is a field instead of a local variable. |
| A jump goes down | `y` grows downward: up is negative. |
| Things don't line up | Integer division: `7 / 2` is `3`. Use `7 / 2.0` for `3.5`. |
| Two strings that look equal are not | Compare strings with `a.equals(b)`, never `a == b`. |
| Animation stuck on frame one | You forgot `animation.update()` in `update()` (game objects do it for you). |
| `Engine.setScene only works in games started with a Scene` | You started with `Engine.run(new Main(), ...)` where `Main` is a `Game`. Start with a scene instead. |
| `Compilation failed` | The lines above it say which file and line, and what Java expected. Fix the first error first; the rest often disappear with it. |

---

## 9. Inside the engine

The whole engine is in `engine/src/pge`, about 3,500 lines, much of it comments. Read
it in this order:

| File | What it teaches |
|---|---|
| `Game.java` | the three-method contract |
| `Engine.java` | the game loop: fixed time steps, catching up, measuring time |
| `gfx/Canvas.java` | how every drawing is just writing numbers into an array; Bresenham lines, midpoint circles, clipping |
| `gfx/Color.java` | colors as numbers; bit shifting |
| `gfx/Bitmap.java`, `SpriteSheet.java` | images as arrays; loading PNGs; cutting a sheet into cells |
| `gfx/Font.java` | a font is just a sprite sheet; measuring letter widths |
| `input/Input.java` | two threads sharing data safely; "pressed this update" from "down now" and "down before" |
| `Window.java` | the only place pixels meet the screen: one image, scaled, shown 60 times per second |
| `world/TileMap.java` | 2D grids stored in 1D arrays; pixels to tiles; drawing only what's visible |
| `world/Camera.java` | smoothing, dead zones, limits: small formulas with a big effect |
| `scene/Scene.java`, `GameObject.java`, `SceneRunner.java` | object-oriented design; lists that change while you loop over them; insertion sort |
| `ui/*.java` | abstract classes and inheritance: every element is a `UIElement` |

### One frame, from start to finish

1. `Engine` decides it is time for an update.
2. `Input.update()` copies what the keyboard and mouse did since last time.
3. Your `update()` runs (or, with scenes: the UI, every object, then the scene).
4. Your `draw(canvas)` runs: every drawing call writes colors into `int[] pixels`.
5. `Window.present()` shows that array, scaled up, in the window.

The picture you see is literally an array of 230,400 numbers (640 x 360), and every
pixel of it was put there by Java code you can read.

---

## 10. Extending the engine

The best way to understand the engine is to change it. These exercises go from
small to big. For each: where to start, and a hint.

When you add to the engine, keep its style: plain classes, private fields with
getters and setters, comments that explain *why*, error messages a beginner can
understand. Run `java Build.java` after every change: it compiles the engine with all
warnings on.

### Warm-ups

- **New palette colors.** `gfx/Color.java`. Pick names, add constants.
- **`drawTriangle` / `fillTriangle`.** `gfx/Canvas.java`. Hint: an outline is three
  `drawLine` calls. Filling: for each row, find where the edges cross it, then
  `fillSpan`.
- **Accented letters.** `tools/MakeAssets.java` draws the font. Add a row of glyphs
  for `á é í ó ú ã õ ç`, then teach `Font.java` where to find them (now only codes
  32 to 127 are mapped).
- **A pause key.** In your game: when P is pressed, skip everything in `update()`
  except checking P again. Show "PAUSED" in `draw()`.

### Medium

- **Camera shake.** `world/Camera.java`. Add `shake(strength, updates)`; while
  shaking, `getX()` and `getY()` add a small random offset. Great for explosions.
- **Scaled sprites.** `drawBitmap(bitmap, x, y, scale)` in `Canvas.java`. Hint: like
  `drawText` with a scale: each source pixel becomes a `scale x scale` square.
- **Particles.** A `Particle extends GameObject` with a speed, gravity and a timer
  that calls `remove()` at zero. Spawn 20 when a slime is stomped.
- **High score file.** Save with `Files.writeString(Path.of("highscore.txt"), "" + score)`,
  read it back with `Files.readString(...)` and `Integer.parseInt(...)`. What if the
  file does not exist yet?
- **Moving platforms.** A `GameObject` that moves back and forth. The hard part:
  the hero should ride on it. Hint: if the hero stands on it, move the hero by the
  same amount.

### Bigger

- **Sound.** Java can play WAV files with `javax.sound.sampled.Clip`. Make a
  `pge.audio.Sound` class with `load(path)` and `play()`. Watch out: loading is slow,
  so load in `start()`/`init()`, never in `update()`.
- **Semi-transparency.** Now a pixel is either drawn or not. Keep the PNG's alpha
  (0-255) in `Bitmap`, and in `Canvas.drawBitmap` mix the colors:
  `result = (source * alpha + destination * (255 - alpha)) / 255`, for red, green and
  blue separately. Then measure with F3 how much slower it is.
- **Animated tiles.** Water that ripples: let `TileMap` know that some tile numbers
  are animations, and draw the current frame instead.
- **Rotation.** `drawBitmapRotated(bitmap, x, y, angle)`. Hint: go through every
  pixel of the *destination* area, rotate its position backwards, and see which
  source pixel lands there. Doing it the other way around leaves holes. Why?
- **Tiled's own format.** Tiled saves `.tmx` files (XML) with all layers and
  objects in one file. Java can read XML with `javax.xml.parsers`.
