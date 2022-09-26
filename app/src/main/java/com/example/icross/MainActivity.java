package com.example.icross;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private static final String TAG = "MainActivity";
    public static Typeface typeface;
    private ProgressBar progressBar;
    private GameView gameView;
    private ConstraintLayout welcomeView;
    private ConstraintLayout endView;
    private MediaPlayer mp;
    protected MediaPlayer destroyMp;
    protected MediaPlayer emptyMp;
    protected MediaPlayer gameoverMp;
    private int progress = 10;
    public static int score = 0;
    private Timer timer;
    private TimerTask updateProgressTask;
    private final int gameTime = 120;
    private final int delayTime = 1200;   // isEnd为True后，delayTime时间后结束游戏
    public static boolean showHint = false;

    private long lastExitTime = 0;

    public static TextView txtName;
    TextView txtScore;
    TextView txtScoreLabel;
    TextView txtRegister;
    TextView txtStart;
    ImageView imgHis;
    ImageView imgReplay;
    ImageView imgRank;
    ImageView imgDel;
    ImageView imgHint;
    ImageView imgRestart;
    ImageView imgSound;

    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        gameView = (GameView) findViewById(R.id.game_view);
        welcomeView = (ConstraintLayout) findViewById(R.id.welcome_view);
        endView = (ConstraintLayout) findViewById(R.id.EndView);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        txtName = (TextView) findViewById(R.id.user_name);
        txtScore = (TextView) findViewById(R.id.txt_score);
        txtScoreLabel = (TextView) findViewById(R.id.txt_score_label);
        txtRegister = (TextView) findViewById(R.id.txt_register);
        txtStart = (TextView) findViewById(R.id.txt_start);
        imgHis = (ImageView) findViewById(R.id.img_history);
        imgReplay = (ImageView) findViewById(R.id.img_replay);
        imgRank = (ImageView) findViewById(R.id.img_rank);
        imgDel = (ImageView) findViewById(R.id.img_delete);
        imgHint = (ImageView) findViewById(R.id.img_hint);
        imgRestart = (ImageView) findViewById(R.id.img_restart);
        imgSound = (ImageView) findViewById(R.id.img_sound);
        mp = MediaPlayer.create(MainActivity.this, R.raw.bgm);
        destroyMp = MediaPlayer.create(MainActivity.this, R.raw.destroy);
        emptyMp = MediaPlayer.create(MainActivity.this, R.raw.empty);
        gameoverMp = MediaPlayer.create(MainActivity.this, R.raw.gameover);

        typeface = Typeface.createFromAsset(getAssets(), "font.ttf");
        txtRegister.setTypeface(typeface);
        txtStart.setTypeface(typeface);
        txtName.setTypeface(typeface);
        txtScore.setTypeface(typeface);
        txtScoreLabel.setTypeface(typeface);

        progressBar.setProgress(progress);  // 60ms*1000约1分钟的倒计
        // gameView游戏画面处理逻辑
        gameView.setOnTouchListener(this);
        // 欢迎界面/结束界面/游戏界面的点击事件处理
        txtRegister.setOnClickListener(this);
        txtStart.setOnClickListener(this);
        imgHis.setOnClickListener(this);
        imgReplay.setOnClickListener(this);
        imgRank.setOnClickListener(this);
        imgDel.setOnClickListener(this);
        imgHint.setOnClickListener(this);
        imgRestart.setOnClickListener(this);
        imgSound.setOnClickListener(this);
        if (gameView.isPlayMusic()) {
            mp.start();
        }
        // 背景音乐循环播放
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (gameView.isPlayMusic())
                    mp.start();
            }
        });

        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(new File(getCacheDir(), "info"));
            properties.load(fis);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Common.userName = properties.getProperty("name");
        Common.location = properties.getProperty("location");
        // Log.i(TAG, "onCreate: ////////////////" +Common.location);
        txtName.setText(Common.userName);
    }

    // 控件点击事件处理
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_register:
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.txt_start:
                welcomeView.setVisibility(View.GONE);
                startGame();
                break;
            case R.id.img_replay:
                endView.setVisibility(View.INVISIBLE);
                startGame();
                break;
            case R.id.img_rank:
                Intent intent1 = new Intent(MainActivity.this, RankActivity.class);
                startActivity(intent1);
                break;
            case R.id.img_history:
                Intent intent2 = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent2);
            case R.id.img_delete:
                gameView.setInDeleteStatus(!gameView.isInDeleteStatus());
                break;
            case R.id.img_hint:
                if (!showHint) {
                    Timer hintTimer = new Timer();
                    showHint = true;
                    hintTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            gameView.setShowHint(!gameView.isShowHint());
                            gameView.invalidate();
                            if (!showHint) {
                                hintTimer.cancel();
                                gameView.setShowHint(false);
                            }
                        }
                    }, 0, 500);
                } else {
                    showHint = false;
                }
                break;
            case R.id.img_restart:
                if (gameView.isPlayMusic()) {
                    gameoverMp.start();
                }
                gameView.setStart(false);
                progress = 1000;
                startGame();
                break;
            case R.id.img_sound:
                if (gameView.isPlayMusic()) {
                    gameView.setPlayMusic(false);
                    imgSound.setImageResource(R.drawable.sound_f);
                    mp.pause();
                } else {
                    gameView.setPlayMusic(true);
                    imgSound.setImageResource(R.drawable.sound_t);
                    mp.start();
                }
                break;
            default:
                break;
        }
    }

    // 游戏逻辑处理
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int x0 = gameView.getX0();
        int y0 = gameView.getY0();
        float fx = motionEvent.getX();
        float fy = motionEvent.getY();
        int x = (int) ((fy - y0) / gameView.getC());
        int y = (int) ((fx - x0) / gameView.getC());
        Point lastCursorPoint;

        // 动画没有显示完不接受新的动作
        // 进度条未开始计数不响应棋盘的点击事件
        // 棋盘外区域不响应
        if (gameView.isShowText() || gameView.isShowSplinter()
                ||!gameView.isStart()
                || fy < y0 || fy > y0 + gameView.getC() * gameView.getRows()
                || fx < gameView.getX0()
                || fx > gameView.getX0() + gameView.getCols() * gameView.getC()) {
            return true;
        }

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:   // 绘制cursor
                gameView.setCursorPoint(new Point(x, y));
                gameView.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:   // 移动cursor
                lastCursorPoint = gameView.getCursorPoint();
                if (lastCursorPoint == null || x != lastCursorPoint.x || y != lastCursorPoint.y) {
                    gameView.setCursorPoint(new Point(x, y));
                    gameView.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (gameView.isInDeleteStatus()) {
                    if (gameView.getColorTable()[x][y] == 0) {
                        return true;
                    }
                    gameView.setInDeleteStatus(false);

                    ValueAnimator splinterAnimator = ValueAnimator.ofInt(0, 500);
                    splinterAnimator.setDuration(600);
                    splinterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            gameView.updateSplinter();
                            gameView.invalidate();
                        }
                    });
                    splinterAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            gameView.setShowSplinter(false);

                            // 这里可能导致游戏结束
                            if (isEnd()) {
                                Toast.makeText(MainActivity.this, "游戏结束了！",
                                        Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        gameover();
                                    }
                                }, delayTime);
                            }
                        }
                    });
                    Point[][] pointsRef = new Point[1][1];
                    pointsRef[0][0] = new Point(x, y);
                    gameView.setPoints(pointsRef);
                    gameView.initSplinter();
                    gameView.updateBox();
                    gameView.setShowSplinter(true);
                    splinterAnimator.start();
                    return true;
                } // fi gameView.isInDeleteStatus

                Point[] points = getCrossBox(new Point(x, y), gameView.getColorTable());
                if (points != null) {
                    Point[][] points1 = getSameColorBox(points, gameView.getColorTable());
                    if (points1 != null) {  // 成功消除
                        for (Point[] points2 : points1) {
                            if (points2 != null) {
                                switch (points2.length) {
                                    case 2:
                                        score += 15;
                                        break;
                                    case 3:
                                        score += 30;
                                        break;
                                    case 4:
                                        score += 50;
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        showHint = false;
                        // 消除成功音效
                        if (gameView.isPlayMusic())
                            destroyMp.start();
                        // 更新gameView参数并通知重绘
                        gameView.setPoints(points1);
                        // 在更新colorBox前需要获取颜色
                        gameView.initSplinter();
                        gameView.updateBox();
                        // 画连线，持续一段时间消失
                        gameView.setShowCrossLine(true);
                        gameView.invalidate();
                        Timer timer1 = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                gameView.setShowCrossLine(false);
                                gameView.invalidate();
                                timer1.cancel();
                            }
                        };
                        timer1.schedule(task, 600, 10000);

                        if (!gameView.isShowSplinter()) {
                            gameView.setShowSplinter(true);
                            ValueAnimator animator = ValueAnimator.ofInt(0, 500);
                            animator.setDuration(600);
                            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    // Log.i(TAG, "onAnimationUpdate: " + gameView.isShowSplinter());
                                    gameView.updateSplinter();
                                    gameView.invalidate();
                                }
                            });
                            animator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    gameView.setShowSplinter(false);
                                    gameView.invalidate();

                                    // 游戏当中没有可以消除的元素了，结束游戏
                                    if (isEnd()) {
                                        // 进度条停止
                                        timer.cancel();
                                        Toast.makeText(MainActivity.this, "游戏结束了！",
                                                Toast.LENGTH_SHORT).show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                gameover();
                                            }
                                        }, delayTime);
                                    }
                                }

                                @Override
                                public void onAnimationStart(Animator animation) {
                                    super.onAnimationStart(animation);
                                }
                            });
                            animator.start();   // 棋子爆炸动画效果
                        }
                    } else { // 点击空白方格，但十字连线上没有相同颜色的方块
                        // 播放音效empty
                        if (gameView.isPlayMusic())
                            emptyMp.start();
                        progress -= (float)gameTime * 15 / 60;
                        if (progress < 0) {
                            progress = 0;
                        }
                        progressBar.setProgress(progress);

                        // TextView动画
                        ValueAnimator animator = ValueAnimator.ofInt(1000);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                gameView.updateText();
                                gameView.invalidate();
                            }
                        });
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                gameView.setShowText(false);
                                gameView.invalidate();
                            }
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                gameView.initText();
                                gameView.setShowText(true);
                            }
                        });
                        animator.setDuration(600);
                        animator.start();
                    }
                } // fi points != null
                else { // 点击到了有色方块上
                    // 没有处理
                    return true;
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void submit(String n, String l, int s) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url = Common.baseUrl + "/submitRankItem";
        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        LocalDateTime dateTime = LocalDateTime.now();
        try {
            jsonObject.put("name", n);
            jsonObject.put("location", l);
            jsonObject.put("score", s);
            jsonObject.put("time", dateTime);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        String json = jsonObject.toString();
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        // 异步请求
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: 数据提交失败：" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.i(TAG, "onResponse: " + response.body().string());
            }
        });
    }

    // 再按一次退出游戏
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - lastExitTime > 1000) {
                Toast.makeText(MainActivity.this, "再按一次退出游戏", Toast.LENGTH_SHORT).show();
                lastExitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 游戏初始化
    private void startGame() {
        showHint = false;
        gameView.setInDeleteStatus(false);
        score = 0;
        gameView.setCursorPoint(null);
        gameView.cleanColorTable();
        progressBar.setProgress(progress);

        int[] array = getArray();

        Timer startGameTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                while(index < array.length && array[index] == 0) {
                    ++index;
                }
                if (index == array.length) {
                    gameView.setStart(true);
                    index = 0;
                    startGameTimer.cancel();
                    if (isEnd()) {
                        Toast.makeText(MainActivity.this, "游戏结束了！",
                                Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                gameover();
                            }
                        }, delayTime);
                        return;
                    }
                    initTimer();
                    timer.schedule(updateProgressTask, 0, gameTime);  // 60ms * 1000
                } else {
                    gameView.setColorTable(index / gameView.getCols(), index % gameView.getCols(), array[index]);
                    gameView.postInvalidate();
                    ++index;
                }
            }
        };
        startGameTimer.schedule(task, 0, 20);
    }

    // 游戏结束处理
    private void gameover() {
        ValueAnimator splinterAnimator = ValueAnimator.ofInt(0, 500);
        splinterAnimator.setDuration(600);
        splinterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                gameView.updateSplinter();
                gameView.invalidate();
            }
        });
        splinterAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                gameView.setShowSplinter(false);
                if (score != 0)
                    score += progress * 2;
                // 游戏结束处理
                if (gameView.isPlayMusic())
                    gameoverMp.start();
                // 记录分数
                SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                HistoryItem historyItem = new HistoryItem(ft.format(new Date()), score);
                historyItem.save();
                txtScore.setText(String.valueOf(score));
                // 如果注册过，尝试提交到远程数据库
                if (!"".equals(Common.userName)) {
                    // Log.i(TAG, "onAnimationEnd: /////////////////提交数据");
                    submit(Common.userName, Common.location, score);
                }
                // 显示分数界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtScore.setText(String.valueOf(score));
                        endView.setVisibility(View.VISIBLE);
                    }
                });
                gameView.setStart(false);
                progress = 1000;
                timer.cancel();
                timer = null;
                gameView.invalidate();
            }
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        Point[][] pointsRef = new Point[1][200];
        for (Point points2: pointsRef[0]) {
            points2 = null;
        }
        int cnt = 0;
        for (int i = 0; i < gameView.getRows(); ++i) {
            for (int j = 0; j < gameView.getCols(); ++j) {
                if (gameView.getColorTable()[i][j] > 0) {
                    if (score != 0)
                        score += 5;
                    pointsRef[0][cnt++] = new Point(i, j);
                }
            }
        }
        gameView.setPoints(pointsRef);
        // 必须先获取颜色才能updateBox
        gameView.initSplinter();
        gameView.updateBox();
        gameView.setShowSplinter(true);
        splinterAnimator.start();
    }

    int index = 0;
    private final Random random = new Random();

    private int[] getArray() {
        int num = (int) (gameView.getRows() * gameView.getCols() * (1 - gameView.getSpace()));
        int cnt = 0;
        int[] array = new int[gameView.getRows() * gameView.getCols()];
        while (cnt < num) {
            int rand = random.nextInt(gameView.getNumOfColors()) + 1;
            array[cnt++] = rand;
        }
        upsetArray(array);
        return array;
    }

    private void upsetArray(int[] array) {
        for (int i = 0; i < array.length; ++i) {
            int randLoc = random.nextInt(array.length);
            int tmp = array[i];
            array[i] = array[randLoc];
            array[randLoc] = tmp;
        }
    }

    private void initTimer() {
        timer = new Timer();
        updateProgressTask = new TimerTask() {
            @Override
            public void run() {
                if (progress <= 0) {
                    timer.cancel();

                    // 游戏结束处理
                    if (gameView.isPlayMusic())
                        gameoverMp.start();
                    // 记录分数
                    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    HistoryItem historyItem = new HistoryItem(ft.format(new Date()), score);
                    historyItem.save();
                    gameView.setStart(false);
                    progress = 1000;
                    gameView.invalidate();
                    if (!"".equals(Common.userName)) {
                        // Log.e(TAG, "onAnimationEnd: 提交数据");
                        submit(Common.userName, Common.location, score);
                    }
                    // 显示分数界面(只有在主线程中才能更新UI)
                    endView.post(new Runnable() {
                        @Override
                        public void run() {
                            txtScore.setText(String.valueOf(score));
                            endView.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    progressBar.setProgress(--progress);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
        super.onDestroy();
    }

    private boolean isEnd() {
        Point point = new Point();
        Point[] points;
        for (int i = 0; i < gameView.getRows(); ++i) {
            for (int j = 0; j < gameView.getCols(); ++j) {
                point.x = i;
                point.y = j;
                points = getCrossBox(point, gameView.getColorTable());
                if (points != null) {
                    Point[][] points1 = getSameColorBox(points, gameView.getColorTable());
                    if (points1 != null) {
//                        Log.i(TAG, "isEnd: " + points1[0][0]);
                        gameView.setEnablePoint(point);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // 获取以点击位置为中心的十字交叉线上的最近的方格的逻辑坐标
    private Point[] getCrossBox(Point point, int[][] colorTable) {
        if (colorTable[point.x][point.y] != 0) {
            return null;
        }
        Point[] points = new Point[4];
        // 上
        int x = point.x - 1;
        while (x >= 0 && colorTable[x][point.y] == 0) {
            --x;
        }
        if (x >= 0) {
            points[0] = new Point(x, point.y);
        }
        // 下
        x = point.x + 1;
        while (x < gameView.getRows() && colorTable[x][point.y] == 0) {
            ++x;
        }
        if (x < gameView.getRows()) {
            points[1] = new Point(x, point.y);
        }
        // 左
        int y = point.y - 1;
        while (y >= 0 && colorTable[point.x][y] == 0) {
            --y;
        }
        if (y >= 0) {
            points[2] = new Point(point.x, y);
        }
        // 右
        y = point.y + 1;
        while (y < gameView.getCols() && colorTable[point.x][y] == 0) {
            ++y;
        }
        if (y < gameView.getCols()) {
            points[3] = new Point(point.x, y);
        }
        return points;
    }

    // 在最近方格中选择相同颜色方块的坐标；函数返回值的每一行存放相同颜色的方块坐标
    private Point[][] getSameColorBox(Point[] points, int[][] colorTable) {
        HashMap<Integer, ArrayList<Point>> map = new HashMap<>();
        Point[][] res = new Point[2][4];
        for (Point point: points) {
            if (point != null) {
                if (!map.containsKey(colorTable[point.x][point.y])) {
                    map.put(colorTable[point.x][point.y], new ArrayList<>());
                }
                map.get(colorTable[point.x][point.y]).add(point);
            }
        }
        boolean hasSame = false;
        int num = 0;
        for (int it: map.keySet()) {
            if (map.get(it).size() > 1) {
                int j = 0;
                for (Point point: map.get(it)) {
                    res[num][j++] = point;
                }
                hasSame = true;
                ++num;
            }
        }
        if (!hasSame) {
            return null;
        }
        return res;
    }
}