package android.com.wordpad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/3/2.
 */
public class PaintView extends View {
    //内容
    private Context mContext;
    private int select_handwrite_size_index = 0;
    private int select_handwrite_color_index = 0;
    /*当前颜色，尺寸*/
    private int currentColor = Color.BLACK;
    private int currentSize = 5;
    //图的路径
    private String PngPath = Environment.getExternalStorageDirectory() + File.separator + "rj.png";
    //txt路径
    private String txt = Environment.getExternalStorageDirectory() + File.separator
            + "rj.txt";
    private String json = "";
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private float x, y;
    private boolean eraserstate;
//    private Paint mBitmapPaint;// 画布的画笔
    private Paint mPaint;// 真实的画笔
    private float mX, mY;// 临时点坐标
    private static final float TOUCH_TOLERANCE = 4;
    //颜色数组
    private int paintColor[] = {Color.RED, Color.BLUE, Color.BLACK,
            Color.GREEN, Color.YELLOW, Color.CYAN, Color.LTGRAY};
    //--------------------------
    // 保存Path路径的集合,用List集合来模拟栈
    private List<DrawPath> savePath = new ArrayList<>();
    private List<List<DrawPath>> deletePath = new ArrayList<>();
    // 记录Path路径的对象
    private DrawPath dp;
    public int screenWidth, screenHeight;
    private float currentX, currentY;
  /*  单个*/
    private List<PointF> points = new ArrayList<>();

    private List<List<PointF>> savePoint = new ArrayList<>();
    private List<List<List<PointF>>> deletePointsPath = new ArrayList<>();
    private List<List<PointF>> lastPointsPath = new ArrayList<>();
    private float xmax = 0, xmin = 0, ymax = 0, ymin = 0;
    Bitmap Erase;

    Bitmap mSignatureBitmap = null;
    Canvas mSignatureBitmapCanvas = null;
    private RectF mDirtyRect;
    private List<TimedPoint> mPoints;
    private float mVelocityFilterWeight;
    private float mLastVelocity;
    private float mMinWidth;
    private float mMaxWidth;
    private float mLastWidth;
    private boolean mIsEmpty;
    private OnSignedListener mOnSignedListener;
    public PaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public PaintView(Context context, int screenWidth, int screenHeight) {
        super(context);
        this.mContext = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        init();
    }

    //--------------------------
    public void init() {
//    // 从资源文件中生成位图
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grid);
        /*画布*/
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

//        mCanvas.setBitmap(bitmap);
        mCanvas.drawColor(Color.WHITE);
//        PathEffect effects = new DashPathEffect(new float[] { 6, 6, 6, 8}, 1);
//        mCanvas.drawLine();
//        mCanvas.setBitmap(effects);
        /*画笔*/
        mPaint = new Paint();
        mPaint.setAntiAlias(true); // 去除锯齿
        mPaint.setStrokeWidth(5);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
//        mPaint.setFakeBoldText(true); //true为粗体，false为非粗体
        /*变淡的效果*/
        mPaint.setAlpha(0x80);
//        mPaint.setAlpha(0xFF);
//        mPaint.setPathEffect(effects);
        mPath = new Path();

    }
    public interface OnSignedListener {
        public void onSigned();
        public void onClear();
    }
    public void clear()
    {
        mPoints = new ArrayList<TimedPoint>();
        mLastVelocity = 0;
        mLastWidth = (mMinWidth + mMaxWidth) / 2;
        mPath.reset();
        mSignatureBitmap = null;
        setIsEmpty(true);

        invalidate();
    }
    private void setIsEmpty(boolean newValue) {
        if(mIsEmpty != newValue) {
            mIsEmpty = newValue;

            if(mOnSignedListener != null) {
                if(mIsEmpty) {
                    mOnSignedListener.onClear();
                } else {
                    mOnSignedListener.onSigned();
                }
            }
        }
    }
        private void addBezier(Bezier curve, float startWidth, float endWidth) {
        ensureSignatureBitmap();
        float originalWidth = mPaint.getStrokeWidth();
        float widthDelta = endWidth - startWidth;
        float drawSteps = (float) Math.floor(curve.length());

        for (int i = 0; i < drawSteps; i++) {
            // Calculate the Bezier (x, y) coordinate for this step.
            // 计算贝塞尔（X，Y）坐标为这一步。
            float t = ((float) i) / drawSteps;
            float tt = t * t;
            float ttt = tt * t;
            float u = 1 - t;
            float uu = u * u;
            float uuu = uu * u;

            float x = uuu * curve.startPoint.x;
            x += 3 * uu * t * curve.control1.x;
            x += 3 * u * tt * curve.control2.x;
            x += ttt * curve.endPoint.x;

            float y = uuu * curve.startPoint.y;
            y += 3 * uu * t * curve.control1.y;
            y += 3 * u * tt * curve.control2.y;
            y += ttt * curve.endPoint.y;

            // Set the incremental stroke width and draw.将增量笔画宽度和借鉴。
            mPaint.setStrokeWidth(startWidth + ttt * widthDelta);
            mSignatureBitmapCanvas.drawPoint(x, y, mPaint);
            expandDirtyRect(x, y);
        }

        mPaint.setStrokeWidth(originalWidth);
    }
    private void ensureSignatureBitmap() {
        if (mSignatureBitmap == null) {
            mSignatureBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888);
            mSignatureBitmapCanvas = new Canvas(mSignatureBitmap);
        }
    }
    /**
     * Called when replaying history to ensure the dirty region includes all
     * mPoints.
     * 历史回放时，确保脏区包括所有的所谓
          * M点。
     */
    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < mDirtyRect.left) {
            mDirtyRect.left = historicalX;
        } else if (historicalX > mDirtyRect.right) {
            mDirtyRect.right = historicalX;
        }
        if (historicalY < mDirtyRect.top) {
            mDirtyRect.top = historicalY;
        } else if (historicalY > mDirtyRect.bottom) {
            mDirtyRect.bottom = historicalY;
        }
    }
    /*画的方法*/
    public void onDraw(Canvas canvas) {
        canvas.drawColor(-1);
        canvas.drawBitmap(mBitmap, 0, 0,mPaint);
        if (mPath != null) {
            // 实时的显示
            canvas.drawPath(mPath, mPaint);
            /* 判断是否是橡皮擦*/
            if (!eraserstate) {
                Move(canvas);
            } else {
                MoveErase(canvas);
            }
        }
//        addBgLine(canvas);
    }
    /* 移动橡皮擦*/
    public void MoveErase(Canvas canvas) {
        //移动时，显示画笔图标
        if (currentColor != Color.WHITE) {
            //设置画笔的图标
            Erase = BitmapFactory.decodeResource(this.getResources(),
                    R.drawable.eraser);
            canvas.drawBitmap(Erase, x, y - Erase.getHeight(),
                    new Paint(Paint.DITHER_FLAG));
        }
    }
    /* 移动画笔*/
    public void Move(Canvas canvas) {
        //移动时，显示画笔图标
        if (currentColor != Color.WHITE) {
            //设置画笔的图标
            Bitmap pen = BitmapFactory.decodeResource(this.getResources(),
                    R.drawable.pencil);
            canvas.drawBitmap(pen, this.mX, this.mY - pen.getHeight(),
                    new Paint(Paint.DITHER_FLAG));
        }
    }
    //选择画笔大小
    public void selectPaintSize(int which) {
        int size = Integer.parseInt(this.getResources().getStringArray(R.array.paintsize)[which]);
        currentSize = size;
        setPaintStyle();
    }

    //设置画笔颜色
    public void selectHandWriteColor(int which) {
        currentColor = paintColor[which];
        setPaintStyle();
    }

    //设置画笔样式
    public void setPaintStyle() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(currentSize);
        mPaint.setColor(currentColor);
        /*变淡的效果*/
        mPaint.setAlpha(0x80);

    }
    public void addPoint(TimedPoint newPoint) {
        mPoints.add(newPoint);
        if (mPoints.size() > 2) {
            // To reduce the initial lag make it work with 3 mPoints为了降低初始滞后使其与3点工作
            // by copying the first point to the beginning.通过复制第一点到开始处。
            if (mPoints.size() == 3) mPoints.add(0, mPoints.get(0));

            ControlTimedPoints tmp = calculateCurveControlPoints(mPoints.get(0), mPoints.get(1), mPoints.get(2));
            TimedPoint c2 = tmp.c2;
            tmp = calculateCurveControlPoints(mPoints.get(1), mPoints.get(2), mPoints.get(3));
            TimedPoint c3 = tmp.c1;
            Bezier curve = new Bezier(mPoints.get(1), c2, c3, mPoints.get(2));

            TimedPoint startPoint = curve.startPoint;
            TimedPoint endPoint = curve.endPoint;

            float velocity = endPoint.velocityFrom(startPoint);
            velocity = Float.isNaN(velocity) ? 0.0f : velocity;

            velocity = mVelocityFilterWeight * velocity
                    + (1 - mVelocityFilterWeight) * mLastVelocity;

            // The new width is a function of the velocity. Higher velocities新的宽度是速度的函数。更高的速度
            // correspond to thinner strokes.对应于更薄的笔画。
            float newWidth = strokeWidth(velocity);

            // The Bezier's width starts out as last curve's final width, and
            // gradually changes to the stroke width just calculated.
            /*贝塞尔宽度开始作为最后的曲线最终宽度和//逐渐变成只计算笔画宽度。*/

            // The new width calculation is based on the velocity between the Bezier's
            // start and end mPoints.
         /*   新的宽度计算基于贝塞尔曲线的//起点和终点之间的速度。*/
            addBezier(curve, mLastWidth, newWidth);

            mLastVelocity = velocity;
            mLastWidth = newWidth;

            // Remove the first element from the list,
            // so that we always have no more than 4 mPoints in mPoints array.
            // 从列表中删除第一个元素，使我们始终在M点阵列不超过4 M点。
            mPoints.remove(0);
        }
    }
    public float strokeWidth(float velocity) {
        return Math.max(mMaxWidth / (velocity + 1), mMinWidth);
    }
    public ControlTimedPoints calculateCurveControlPoints(TimedPoint s1, TimedPoint s2 ,TimedPoint s3) {
        float dx1 = s1.x - s2.x;
        float dy1 = s1.y - s2.y;
        float dx2 = s2.x - s3.x;
        float dy2 = s2.y - s3.y;

        TimedPoint m1 = new TimedPoint((s1.x + s2.x) / 2.0f, (s1.y + s2.y) / 2.0f);
        TimedPoint m2 = new TimedPoint((s2.x + s3.x) / 2.0f, (s2.y + s3.y) / 2.0f);

        float l1 = (float) Math.sqrt(dx1*dx1 + dy1*dy1);
        float l2 = (float) Math.sqrt(dx2*dx2 + dy2*dy2);

        float dxm = (m1.x - m2.x);
        float dym = (m1.y - m2.y);
        float k = l2 / (l1 + l2);
        TimedPoint cm = new TimedPoint(m2.x + dxm*k, m2.y + dym*k);

        float tx = s2.x - cm.x;
        float ty = s2.y - cm.y;

        return new ControlTimedPoints(new TimedPoint(m1.x + tx, m1.y + ty), new TimedPoint(m2.x + tx, m2.y + ty));
    }
    class ControlTimedPoints {

        public TimedPoint c1;
        public TimedPoint c2;

        public ControlTimedPoints(TimedPoint c1, TimedPoint c2) {
            this.c1 = c1;
            this.c2 = c2;
        }

    }
    /**
     * 线的开始和结束
     */
    private void touch_start(float x, float y) {
//        mPath.reset();//清空path
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    /**
     * 与上一个点之间连线
     */
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(mY - y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            //源代码
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }
    /**
     * 连线结束，在画布上画出并保存
     */
    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        //将一条完整的路径保存下来
        savePath.add(dp);
        mPath = null;// 重新置空
    }
    /**
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void Revoke() {
        if (savePath != null && savePath.size() > 0) {
            List<DrawPath> deletePathItem = new ArrayList<>();
            deletePathItem.add(savePath.get(savePath.size() - 1));
            deletePath.add(deletePathItem);
            /*移除最后一条*/
            savePath.remove(savePath.size() - 1);

            List<List<PointF>> deletePointPathItem = new ArrayList<>();
            deletePointPathItem.add(savePoint.get(savePoint.size() - 1));
            deletePointsPath.add(deletePointPathItem);
            savePoint.remove(savePoint.size() - 1);
            redrawOnBitmap();
        }
    }

    /**
     * 清空功能
     */
    public void Clear() {
        if (savePath.size() != 0) {
            List<DrawPath> deletePathItem = new ArrayList<>();
            deletePathItem.addAll(savePath);
            deletePath.add(deletePathItem);

            List<List<PointF>> deletePointPathItem = new ArrayList<>();
            deletePointPathItem.addAll(savePoint);
            deletePointsPath.add(deletePointPathItem);
            savePoint.clear();
            savePath.clear();
            redrawOnBitmap();
        } else {
        }
    }

    /**
     * 保存txt和图片
     */
    public void Save() {
        if (savePath != null && savePath.size() > 0) {
            json = JSON.toJSONString(savePoint);
            saveToSDCard();
            savePath.clear();
            deletePath.clear();
            savePoint.clear();
            deletePointsPath.clear();
            redrawOnBitmap();
        }
    }

    /**
     * 上次
     */
    public void lastTime() {
        File file = new File(txt);
        Log.i("上次",""+txt);
        if (file.exists()) {
            savePath.clear();
            deletePath.clear();
            savePoint.clear();
            deletePointsPath.clear();
            readTxtFile();
        }
    }

    /**
     * 恢复
     */
    public void Recover() {
        int size = deletePath.size();
        if (deletePath != null && deletePath.size() > 0) {
            savePath.addAll(deletePath.get(deletePath.size() - 1));
            deletePath.remove(deletePath.size() - 1);
            savePoint.addAll(deletePointsPath.get(size - 1));
            deletePointsPath.remove(size - 1);
            redrawOnBitmap();
        }
    }

    /**
     * 橡皮擦
     */
    public void eraserdo(boolean eraserstate) {
        this.eraserstate = eraserstate;
    }

    public void redrawOnBitmap() {
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
                Bitmap.Config.ARGB_8888);

        mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
        Iterator<DrawPath> iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = iter.next();
            mCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();// 刷新
    }
    // 缩放
    public static Bitmap resizeImage(Bitmap bitmap, int width, int height) {
        int originWidth = bitmap.getWidth();
        int originHeight = bitmap.getHeight();

        float scaleWidth = ((float) width) / originWidth;
        float scaleHeight = ((float) height) / originHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, originWidth,
                originHeight, matrix, true);
        return resizedBitmap;
    }
    //--------------------------
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        x = event.getX();
        y = event.getY();
        PointF pointF = new PointF(x, y);
        if (!eraserstate) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 每次down下去重新new一个Path
                    mPath = new Path();
                    //每一次记录的路径对象是不一样的
                    dp = new DrawPath();
                    dp.path = mPath;
                    dp.paint = mPaint;


//                    mPoints.clear();
//                    addPoint(new TimedPoint(eventX, eventY));

                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    points.add(pointF);
//                    addPoint(new TimedPoint(eventX, eventY));
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    savePoint.add(points);
                    points = new ArrayList<>();
//                    addPoint(new TimedPoint(eventX, eventY));
                    touch_up();
                    invalidate();
                    break;
            }
        } else {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    eraserPath();
                    invalidate();
//                    touch_start(x, y);
                    mPath = new Path();
                    dp = new DrawPath();
                    dp.path = mPath;
                    dp.paint = mPaint;

                    break;
                case MotionEvent.ACTION_MOVE:
//                    touch_move(x, y);
                    eraserPath();
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    mPath = null;
//                    touch_up();
                    invalidate();
                    break;
            }

        }
        return true;
    }
    //弹出画笔大小选项对话框
    public void showPaintSizeDialog(View parent) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle("选择画笔大小：");
        alertDialogBuilder.setSingleChoiceItems(R.array.paintsize, select_handwrite_size_index, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                select_handwrite_size_index = which;
                selectPaintSize(which);
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.create().show();
    }
    //弹出画笔颜色选项对话框
    public void showPaintColorDialog(View parent) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle("选择画笔颜色：");
        alertDialogBuilder.setSingleChoiceItems(R.array.paintcolor, select_handwrite_color_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                select_handwrite_color_index = which;
                selectHandWriteColor(which);
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.create().show();
    }
    /**
     * 保存到sd卡
     */
    public void saveToSDCard() {
        ByteArrayOutputStream baos = null;
        wrap();
        try {
            File f = new File(PngPath);
            if (f.exists()) {
                f.delete();
            }
            //进行剪切出 画图部分
            if (xmax > 0 && ymax > 0) {
                if(xmin<0){
                    xmin=0;
                }
                if(ymin<0){
                    ymin=0;
                }
                if(xmax>screenWidth){
                    xmax=screenWidth;
                }
                if(ymax>screenHeight){
                    ymax=screenHeight;
                }
                Bitmap bitmap = Bitmap.createBitmap(mBitmap, (int) xmin, (int) ymin,
                        (int) (xmax - xmin), (int) (ymax - ymin));
                FileOutputStream fos = new FileOutputStream(new File(PngPath));
//                ByteArrayOutputStream baos = new ByteArrayOutputStream(new File(PngPath));
//                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//                byte[] b = baos.toByteArray();
//                if (b != null) {
//                    fos.write(b);
//                }

                fos.flush();
                fos.close();
                //存放数组数据的文件
                File file = new File(txt);
                //文件写入流
                FileWriter out = new FileWriter(file);
                out.write(json);
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取txt文件并画出上次保存的图
     */
    public void readTxtFile() {
        try {
            File file = new File(txt);
            //判断文件是否存在
            if (file.isFile() && file.exists()) {
                //构造一个BufferedReader类来读取文件
                BufferedReader br = new BufferedReader(new FileReader(file));
                //接收的json
                String json = "";
                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
                    json = json + lineTxt;
                    Log.i("txt1", json);
                }
                br.close();
                lastPointsPath = JSON.parseObject(json, new TypeReference<List<List<PointF>>>() {
                });
                Log.e("txt2", "lastPointsPath" + lastPointsPath);
                savePoint.addAll(lastPointsPath);
                drawPath(lastPointsPath);
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
    }
    /**
     * 根据点阵画出图
     */
    public void drawPath(List<List<PointF>> path) {
        if (path.size() > 0) {
            //如果有坐标点，开始绘图
            Log.i("根据点阵画出图", "savePath" + savePath);
            Iterator<List<PointF>> iter = path.iterator();
            while (iter.hasNext()) {
                List<PointF> pointFs = iter.next();
                mPath = new Path();
                dp = new DrawPath();
                dp.path = mPath;
                dp.paint = mPaint;
                touch_start(pointFs.get(0).getX(), pointFs.get(0).getY());
                Iterator<PointF> iterItem = pointFs.iterator();
                while (iterItem.hasNext()) {
                    PointF pointF = iterItem.next();
                    touch_move(pointF.getX(), pointF.getY());
                }
                touch_up();
            }
            Log.e("根据点阵画出图", "savePath" + savePath);
        }
    }
    /**
     * 橡皮擦功能
     */
    public void eraserPath() {
        if (savePoint.size() > 0) {
            //如果有坐标点，开始绘图
            int num = 0;
            /*遍历函数*/
            Iterator<List<PointF>> iter = savePoint.iterator();
            while (iter.hasNext()) {
                List<PointF> pointFs = iter.next();
                Iterator<PointF> iterItem = pointFs.iterator();
                while (iterItem.hasNext()) {
                    PointF pointF = iterItem.next();
                    if (((y - 30) < pointF.getY() && (y + 30) > pointF.getY())
                            && ((x - 30) < pointF.getX() && (x + 30) > pointF.getX())) {
//                        Log.e("》》uuuuuu", "num=" + num);


                        List<DrawPath> deletePathItem = new ArrayList<>();
                        deletePathItem.add(savePath.get(num));
                        deletePath.add(deletePathItem);
                        savePath.remove(num);

                        List<List<PointF>> deletePointPathItem = new ArrayList<>();
                        deletePointPathItem.add(savePoint.get(num));
                        deletePointsPath.add(deletePointPathItem);
                        savePoint.remove(num);
                        redrawOnBitmap();
                        return;
                    }
                    ;
                }
                num++;
            }
        }
    }

    /**
     * 获取点阵的x，y的最大最小值
     */
    public void wrap() {
        if (savePoint.size() > 0) {
            //对x，y的最大最小值的初始化
            PointF first = null;
            try {
                first = savePoint.get(0).get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            xmax = first.getX();
            xmin = ymax;
            ymax = first.getY();
            ymin = ymax;
            //如果有坐标点，开始绘图
            Iterator<List<PointF>> iter = savePoint.iterator();
            while (iter.hasNext()) {
                List<PointF> pointFs = iter.next();
                Iterator<PointF> iterItem = pointFs.iterator();
                while (iterItem.hasNext()) {
                    PointF pointF = iterItem.next();
                    x = pointF.getX();
                    y = pointF.getY();
                    if (x > xmax) {
                        xmax = x;
                    } else if (x < xmin) {
                        xmin = x;
                    }
                    if (y > ymax) {
                        ymax = y;
                    } else if (y < ymin) {
                        ymin = y;
                    }
                }
            }
        }
    }

}