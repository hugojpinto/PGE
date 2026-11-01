package pge.input;

import java.awt.event.KeyEvent;

/**
 * Names for keyboard keys, to use with Input.isDown(...), Input.wasPressed(...) and
 * Input.wasReleased(...).
 *
 * Each name is just a number: the code the JDK uses for that key.
 */
public final class Key {

    // Arrows
    public static final int LEFT  = KeyEvent.VK_LEFT;
    public static final int RIGHT = KeyEvent.VK_RIGHT;
    public static final int UP    = KeyEvent.VK_UP;
    public static final int DOWN  = KeyEvent.VK_DOWN;

    // Common keys
    public static final int SPACE     = KeyEvent.VK_SPACE;
    public static final int ENTER     = KeyEvent.VK_ENTER;
    public static final int ESCAPE    = KeyEvent.VK_ESCAPE;
    public static final int TAB       = KeyEvent.VK_TAB;
    public static final int BACKSPACE = KeyEvent.VK_BACK_SPACE;
    public static final int SHIFT     = KeyEvent.VK_SHIFT;
    public static final int CONTROL   = KeyEvent.VK_CONTROL;
    public static final int ALT       = KeyEvent.VK_ALT;     // "Option" on a Mac

    // Letters
    public static final int A = KeyEvent.VK_A;
    public static final int B = KeyEvent.VK_B;
    public static final int C = KeyEvent.VK_C;
    public static final int D = KeyEvent.VK_D;
    public static final int E = KeyEvent.VK_E;
    public static final int F = KeyEvent.VK_F;
    public static final int G = KeyEvent.VK_G;
    public static final int H = KeyEvent.VK_H;
    public static final int I = KeyEvent.VK_I;
    public static final int J = KeyEvent.VK_J;
    public static final int K = KeyEvent.VK_K;
    public static final int L = KeyEvent.VK_L;
    public static final int M = KeyEvent.VK_M;
    public static final int N = KeyEvent.VK_N;
    public static final int O = KeyEvent.VK_O;
    public static final int P = KeyEvent.VK_P;
    public static final int Q = KeyEvent.VK_Q;
    public static final int R = KeyEvent.VK_R;
    public static final int S = KeyEvent.VK_S;
    public static final int T = KeyEvent.VK_T;
    public static final int U = KeyEvent.VK_U;
    public static final int V = KeyEvent.VK_V;
    public static final int W = KeyEvent.VK_W;
    public static final int X = KeyEvent.VK_X;
    public static final int Y = KeyEvent.VK_Y;
    public static final int Z = KeyEvent.VK_Z;

    // Number keys on the main keyboard (Java names cannot start with a digit)
    public static final int DIGIT_0 = KeyEvent.VK_0;
    public static final int DIGIT_1 = KeyEvent.VK_1;
    public static final int DIGIT_2 = KeyEvent.VK_2;
    public static final int DIGIT_3 = KeyEvent.VK_3;
    public static final int DIGIT_4 = KeyEvent.VK_4;
    public static final int DIGIT_5 = KeyEvent.VK_5;
    public static final int DIGIT_6 = KeyEvent.VK_6;
    public static final int DIGIT_7 = KeyEvent.VK_7;
    public static final int DIGIT_8 = KeyEvent.VK_8;
    public static final int DIGIT_9 = KeyEvent.VK_9;

    // Function keys
    public static final int F1  = KeyEvent.VK_F1;
    public static final int F2  = KeyEvent.VK_F2;
    public static final int F3  = KeyEvent.VK_F3;
    public static final int F4  = KeyEvent.VK_F4;
    public static final int F5  = KeyEvent.VK_F5;
    public static final int F6  = KeyEvent.VK_F6;
    public static final int F7  = KeyEvent.VK_F7;
    public static final int F8  = KeyEvent.VK_F8;
    public static final int F9  = KeyEvent.VK_F9;
    public static final int F10 = KeyEvent.VK_F10;
    public static final int F11 = KeyEvent.VK_F11;
    public static final int F12 = KeyEvent.VK_F12;

    private Key() {
    }
}
