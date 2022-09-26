package com.example.icross;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {

    private static final String TAG = "GameView";

    private int x0 = 40;
    private int y0 = 125;
    private final int c = 100;
    private int rows = 18;
    private int cols = 10;
    private int[][] colorTable;
    private Point[][] points;
    private final int numOfColors = 7;
    private final Paint paint = new Paint();
    private Point cursorPoint;
    private final Bitmap cursorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cursor2);
    private boolean showCrossLine = false;
    private final Bitmap hintCursorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);
    private final Bitmap[] chesses = new Bitmap[numOfColors];
    private final int[][] chessColors = new int[numOfColors][5];
    private boolean playMusic = true;
    private boolean showSplinter = false;
    private final ArrayList<Splinter> splinters = new ArrayList<>();
    private boolean isStart = false;
    private Point enablePoint = new Point();
    private boolean showHint = false;
    private boolean inDeleteStatus = false;
    private boolean showText = false;
    private String text;
    private final int textDy = 0;
    private int textAlpha = 255;
    private float textX;
    private float textY;
    Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "font.ttf");

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initChessData();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int pw = right - left;
        int ph = bottom - top;
        cols = pw / c;

        x0 = (pw - c * cols) / 2;
        rows = ph / c - 2;
        y0 = (ph - c * rows) / 2 - 45;
        colorTable = new int[rows][cols];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 160, 160, 160 -> 135,206,250(灰色->天蓝色)
        canvas.drawColor(Color.rgb(135,206,250));
        drawBoard(canvas);
        // 绘制cursor
        if (cursorPoint != null) {
            Rect cursorLoc = getRectOfLattice(cursorPoint);
            cursorLoc.top -= 20;
            cursorLoc.left -= 20;
            cursorLoc.bottom += 20;
            cursorLoc.right += 20;
            canvas.drawBitmap(cursorBitmap, null, cursorLoc, paint);
        }
        // 绘制路径
        if (showCrossLine) {
            drawCrossLine(canvas);
        }
        // 绘制碎片
        if (showSplinter) {
            drawSplinter(canvas);
        }

        if (showHint) { // 显示提示
            Rect hintCursorLoc = getRectOfLattice(enablePoint);
            hintCursorLoc.top -= 10;
            hintCursorLoc.left -= 10;
            hintCursorLoc.bottom += 10;
            hintCursorLoc.right += 10;
            canvas.drawBitmap(hintCursorBitmap, null, hintCursorLoc, paint);
        }
        if (showText) {
            paint.setTypeface(typeface);
            drawText(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private Rect getRectOfLattice(Point point) {
        Rect rect = new Rect();
        rect.left = x0 + c * point.y;
        rect.top = y0 + c * point.x;
        rect.right = x0 + c * (point.y + 1);
        rect.bottom = y0 + c * (point.x + 1);
        return rect;
    }

    private PointF getCenterLocOfLattice(Point point) {
        PointF center = new PointF();
        center.x = (float) (x0 + c * (point.y + 0.5));
        center.y = (float) (y0 + c * (point.x + 0.5));
        return center;
    }

    private Rect getRectOfBox(Rect rectOfLattice) {
        Rect rect = new Rect();
        int boxMargin = 5;
        rect.left = rectOfLattice.left + boxMargin;
        rect.top = rectOfLattice.top + boxMargin;
        rect.right = rectOfLattice.right - boxMargin;
        rect.bottom = rectOfLattice.bottom - boxMargin;
        return rect;
    }

    private void drawCrossLine(Canvas canvas) {
//        PointF start = getCenterLocOfLattice(cursorPoint);
        PointF nowLoc;
        Point now = new Point();
        paint.setColor(Color.YELLOW);
        if (points != null) {
            for (Point[] points1: points) {
                if (points1 != null) {
                    for (Point point: points1) {
                        if (point != null) {
                            // end = getCenterLocOfLattice(point);
                            // canvas.drawLine(start.x, start.y, end.x, end.y, paint);

                            // cursorPoint -> point画黄色的圆形
//                            canvas.drawCircle(start.x, start.y, 10, paint);
                            int radius = 20;
                            if (cursorPoint.x == point.x) {
                                now.x = cursorPoint.x;
                                if (cursorPoint.y > point.y) { // 左边
                                    for (int i = point.y; i < cursorPoint.y; ++i) {
                                        now.y = i;
                                        nowLoc = getCenterLocOfLattice(now);
                                        canvas.drawCircle(nowLoc.x, nowLoc.y, radius, paint);
                                    }
                                } else { // 右边
                                    for (int i = point.y; i > cursorPoint.y; --i) {
                                        now.y = i;
                                        nowLoc = getCenterLocOfLattice(now);
                                        canvas.drawCircle(nowLoc.x, nowLoc.y, radius, paint);
                                    }
                                }
                            } else if (cursorPoint.y == point.y) {
                                now.y = cursorPoint.y;
                                if (cursorPoint.x > point.x) {  // 上边
                                    for (int i = point.x; i < cursorPoint.x; ++i) {
                                        now.x = i;
                                        nowLoc = getCenterLocOfLattice(now);
                                        canvas.drawCircle(nowLoc.x, nowLoc.y, radius, paint);
                                    }
                                } else {    // 下边
                                    for (int i = point.x; i > cursorPoint.x; --i) {
                                        now.x = i;
                                        nowLoc = getCenterLocOfLattice(now);
                                        canvas.drawCircle(nowLoc.x, nowLoc.y, radius, paint);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void initText() {
        text = "time - 5";
        PointF textPoint = getCenterLocOfLattice(cursorPoint);
        float width = 60 * text.length();
        textPoint.x -= width / 3;
        if (textPoint.x + 10 < x0) {
            textPoint.x += width / 7;
        } else if (textPoint.x + width / 2 > x0 + c * cols) {
            textPoint.x -= width / 10;
        }
        textPoint.y -= 40;
        textX = textPoint.x;
        textY = textPoint.y;
        textAlpha = 255;
    }

    public void updateText() {
        textY -= 1;
        textAlpha -= 5;
        if (textAlpha < 0) {
            textAlpha = 0;
        }
    }

    private void drawText(Canvas canvas) {
        paint.setColor(Color.RED);
        paint.setTextSize(60);
        paint.setAlpha(textAlpha);
        canvas.drawText(text, textX, textY, paint);
    }

    // 绘制棋盘并更新
    private void drawBoard(Canvas canvas) {
        Rect rect;
        Rect rectOfBox;
        RectF rectF = new RectF();

        // 绘制棋盘的彩色方格
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                rect = getRectOfLattice(new Point(i, j));
                if ((i + j) % 2 == 0) {
                    // (230, 230, 230)浅灰色->(浅蓝色)
                    rectF.left = rect.left;
                    rectF.top = rect.top;
                    rectF.right = rect.right;
                    rectF.bottom = rect.bottom;
                    paint.setColor(Color.rgb(0, 255, 255));
                    canvas.drawRoundRect(rectF, 15, 15, paint);
                } else {
                    // (220, 220, 220)深灰色->(深蓝色)
                    paint.setColor(Color.rgb(0, 225, 255));
                    canvas.drawRect(rect, paint);
                }
                if (colorTable[i][j] != 0) {
                    rectOfBox = getRectOfBox(rect);
//                    paint.setColor(colors[colorTable[i][j] - 1]);
//                    canvas.drawRect(rectOfBox, paint);
                    canvas.drawBitmap(chesses[colorTable[i][j] - 1], null, rectOfBox, paint);
                }
            }
        }
    }

    private void drawSplinter(Canvas canvas) {
        for (Splinter s: splinters) {
            paint.setColor(s.color);
            canvas.drawCircle(s.x, s.y, s.r, paint);
        }
    }

    // 更新碎片位置（爆炸动画）
    public void updateSplinter() {
        for (Splinter splinter: splinters) {
            splinter.x += splinter.vx;
            ++splinter.vy;
            splinter.y += splinter.vy;
        }
    }

    // 成功消除时初始化碎片
    public void initSplinter() {
        PointF pointLoc;
        int vx, vy, r, clr;
        Random random = new Random();
        Splinter splinter;
        splinters.clear();
        if (points != null) {
            for (Point[] points1: points) {
                if (points1 != null) {
                    for (Point point: points1) {
                        if (point != null) {
                            for (int i = 0; i < 20; ++i) {
                                pointLoc = getCenterLocOfLattice(point);
                                vx = random.nextInt(11) - 5;
                                vy = random.nextInt(26) - 20;
                                // 半径3-15
                                r = random.nextInt(12) + 3;
                                clr = random.nextInt(5);
//                                splinter = new Splinter(pointLoc.x, pointLoc.y, 10, vx, vy, colors[colorTable[point.x][point.y] - 1]);
                                splinter = new Splinter(pointLoc.x, pointLoc.y, r, vx, vy, chessColors[colorTable[point.x][point.y] - 1][clr]);
                                splinters.add(splinter);
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateBox() {
        if (points != null) {
            for (Point[] points1: points) {
                if (points1 != null) {
                    for (Point point: points1) {
                        if (point != null) {
                            colorTable[point.x][point.y] = 0;
                        }
                    }
                }
            }
        }
    }

    private void initChessData() {
        chesses[0] = BitmapFactory.decodeResource(getResources(), R.drawable.chess1);
        chesses[1] = BitmapFactory.decodeResource(getResources(), R.drawable.chess2);
        chesses[2] = BitmapFactory.decodeResource(getResources(), R.drawable.chess3);
        // chesses[3] = BitmapFactory.decodeResource(getResources(), R.drawable.chess4);
        chesses[3] = BitmapFactory.decodeResource(getResources(), R.drawable.chess5);
        chesses[4] = BitmapFactory.decodeResource(getResources(), R.drawable.chess6);
        chesses[5] = BitmapFactory.decodeResource(getResources(), R.drawable.chess7);
        chesses[6] = BitmapFactory.decodeResource(getResources(), R.drawable.chess8);
        // chesses[8] = BitmapFactory.decodeResource(getResources(), R.drawable.chess9);

        for (int i = 0; i < numOfColors; ++i) {
            int w = chesses[i].getWidth();
            int h = chesses[i].getHeight();
            chessColors[i][0] = chesses[i].getPixel(w / 2, h / 2);
            chessColors[i][1] = chesses[i].getPixel(10, h / 2);
            chessColors[i][2] = chesses[i].getPixel(w - 10, h / 2);
            chessColors[i][3] = chesses[i].getPixel(w / 2, 10);
            chessColors[i][4] = chesses[i].getPixel(w / 2, h - 10);
        }
    }

    public void setColorTable(int x, int y, int value) {
        colorTable[x][y] = value;
    }

    public void cleanColorTable() {
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                colorTable[i][j] = 0;
            }
        }
    }

    public int getX0() {
        return x0;
    }

    public int getY0() {
        return y0;
    }

    public int getC() {
        return c;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int[][] getColorTable() {
        return colorTable;
    }

    public void setPoints(Point[][] points) {
        this.points = points;
    }

    public double getSpace() {
        double space = 0.3;
        return space;
    }


    public int getNumOfColors() {
        return numOfColors;
    }


    public Point getCursorPoint() {
        return cursorPoint;
    }

    public void setCursorPoint(Point cursorPoint) {
        this.cursorPoint = cursorPoint;
    }

    public void setShowCrossLine(boolean showCrossLine) {
        this.showCrossLine = showCrossLine;
    }

    public boolean isPlayMusic() {
        return playMusic;
    }

    public void setPlayMusic(boolean playMusic) {
        this.playMusic = playMusic;
    }

    public boolean isShowSplinter() {
        return showSplinter;
    }

    public void setShowSplinter(boolean showSplinter) {
        this.showSplinter = showSplinter;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }


    public void setEnablePoint(Point enablePoint) {
        this.enablePoint = enablePoint;
    }


    public boolean isShowHint() {
        return showHint;
    }

    public void setShowHint(boolean showHint) {
        this.showHint = showHint;
    }

    public boolean isInDeleteStatus() {
        return inDeleteStatus;
    }

    public void setInDeleteStatus(boolean inDeleteStatus) {
        this.inDeleteStatus = inDeleteStatus;
    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
