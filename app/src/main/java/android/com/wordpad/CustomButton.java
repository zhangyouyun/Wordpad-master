package android.com.wordpad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Auser on 2016/3/7.
 */
public class CustomButton extends LinearLayout implements View.OnClickListener {
    private ArrayList<ButtonItem> item = new ArrayList<>();
    private Context mContext;
    private PaintView mPaintView;

//    private PaintView1 mPaintView1;
    private Boolean isEarse = false;
    private int select_handwrite_size_index = 0;
    private Bitmap mSignBitmap = null;
    private String json = "";
    private Bitmap mBitmap;
    private float x, y;
    public CustomButton(Context context) {
        super(context);
        mContext = context;
        OuterLayout();
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        OuterLayout();
    }

    //    //下划线
//    public View Line() {
//        LinearLayout.LayoutParams lineparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
//        ImageView line = new ImageView(mContext);
//        line.setBackgroundColor(Color.BLACK);
//        line.setLayoutParams(lineparams);
//        return line;
//    }
    public void OuterLayout() {
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//        this.setOrientation(LinearLayout.VERTICAL);
//        this.setLayoutParams(params);
//        this.setBackgroundColor(Color.WHITE);
        /*获取屏幕宽高*/
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        mPaintView = new PaintView(mContext, dm.widthPixels, dm.heightPixels);
        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

    }

    public void CreateLayout(ArrayList<ButtonItem> buttonItem) {
        /*当前屏幕长度宽度*/
        item = buttonItem;
        /*外面的布局*/
        LinearLayout buttonLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams buttonparams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

      /*  画笔存在问题*/
        LinearLayout.LayoutParams ScrollView = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setLayoutParams(buttonparams);
        buttonLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        buttonLayout.setBackgroundColor(Color.WHITE);
        for (int i = 0; i < buttonItem.size(); i++) {
            ButtonItem buttonitem = buttonItem.get(i);
            buttonLayout.addView(getItemView(buttonitem));

        }
        buttonLayout.setBackground(getResources().getDrawable(R.drawable.normal));

        /*滚动条*/
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(mContext);
        horizontalScrollView.setLayoutParams(ScrollView);
        horizontalScrollView.setSmoothScrollingEnabled(true);
        horizontalScrollView.setFillViewport(true);
        horizontalScrollView.addView(buttonLayout);

        this.addView(horizontalScrollView);
        this.addView(mPaintView);
    }


    public View getItemView(final ButtonItem buttonitem) {
        LinearLayout layout = new LinearLayout(mContext);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params1.setMargins(DensityUtil.px2dip(mContext,20), DensityUtil.px2dip(mContext,20),
                DensityUtil.px2dip(mContext,20), DensityUtil.px2dip(mContext,20));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(params1);
       /* 边距*/
//        layout.setPadding(DensityUtil.px2dip(mContext,20), DensityUtil.px2dip(mContext,10),
//                DensityUtil.px2dip(mContext,20), DensityUtil.px2dip(mContext,10));
        Button button = new Button(mContext);
        button.setText(buttonitem.getButtonName());
        button.setTextSize(buttonitem.getButtonSize());
        button.setTextColor(buttonitem.getButtonColor());
        button.setId(buttonitem.getId());
        button.setTag(buttonitem);
        button.setOnClickListener(this);
        button.setGravity(Gravity.CENTER);
        button.setWidth(buttonitem.getButtonWidth());
        button.setHeight(buttonitem.getButtonHeight());
        if (buttonitem.getIsCheck()) {
            button.setText(buttonitem.getButtonName());
        } else {
            button.setText(buttonitem.getCheckName());
        }
        button.setBackground(null);
//        LayoutParams buttonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        button.setLayoutParams(buttonParams);
        button.setBackground(getResources().getDrawable(R.drawable.selector));
        layout.addView(button);

        return layout;
    }

    //监听
    private ButtonListener buttonListener;

    public ButtonListener getButtonListener() {
        return buttonListener;
    }

    public void setButtonListener(ButtonListener buttonListener) {
        this.buttonListener = buttonListener;
    }

    public void onClick(View v) {
        ButtonItem buttonItem = (ButtonItem) v.getTag();
        buttonItem.setIsCheck(!buttonItem.getIsCheck());
        if (buttonListener != null) {
            buttonListener.showTip(buttonItem);
        }
        switch (buttonItem.getId()) {
            case 1:
                /*保存*/
                mPaintView.Save();
//                mPaintView.createSignFile();
                break;
            case 2:
               /* 字体大小*/
                mPaintView.showPaintSizeDialog(v);
                break;
            case 3:
               /* 颜色选择*/
                mPaintView.showPaintColorDialog(v);
                break;
            case 4:
                /*撤销*/
                mPaintView.Revoke();
                break;
            case 5:
              /*  清空*/
                mPaintView.Clear();
                break;
            case 6:
                /*橡皮擦*/
                mPaintView.eraserdo(true);
                break;
            case 7:
                /*恢复*/
                mPaintView.Recover();
                break;
            case 8:
                /*画笔*/
                mPaintView.eraserdo(false);
                break;
            case 9:
                /*上次*/
                mPaintView.lastTime();
                /*刷新*/
                invalidate();
                break;
            default:
                break;
        }
    }
    //正常的回调接口
    public interface ButtonListener {
        void showTip(ButtonItem buttonItem);
    }
}
