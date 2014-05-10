package cz.tomkren.views;


import java.awt.*;

public class FramePosouvac {

    private Point framePos;
    private int nextFrameDeltaX;
    private int xDeltaDelta;
    private int miniDelta;

    public static final int MINI_DELTA_DELTA = 4;

    public FramePosouvac(int x, int y, int xDeltaDelta){
        this(new Point(x,y),xDeltaDelta);
    }

    public FramePosouvac(Point framePos, int xDeltaDelta) {
        this.framePos = framePos;
        this.xDeltaDelta = xDeltaDelta;
        nextFrameDeltaX = 0;
        miniDelta = 0;
    }

    public Point nextFramePos() {

        int newX = framePos.x+nextFrameDeltaX;
        nextFrameDeltaX += xDeltaDelta;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (newX + xDeltaDelta/2 > screenSize.getWidth()) {
            nextFrameDeltaX = 0;
            miniDelta += MINI_DELTA_DELTA;
            return nextFramePos();
        }

        return new Point(newX+miniDelta, framePos.y+miniDelta);
    }
}
