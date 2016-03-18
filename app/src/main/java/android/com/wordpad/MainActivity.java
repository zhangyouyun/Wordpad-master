package android.com.wordpad;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Auser on 2016/2/29.
 */
public class MainActivity extends Activity {
    private ImageView imageView;
    private Button mButton;
    private Bitmap mSignBitmap;
    private Context mContext;
    CustomButton customButton;
    private Boolean isEarse = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordpad);
        addView();
    }

    private void addView() {
        //传入参数
        ArrayList<ButtonItem> buttonList = new ArrayList<>();
        ButtonItem buttonItem = new ButtonItem();
        buttonItem.setButtonName("保存");
        buttonItem.setButtonSize(19);
        buttonItem.setButtonColor(Color.BLACK);
        buttonItem.setId(1);
//        buttonItem.setButtonHeight(180);
//        buttonItem.setButtonWidth(240);
        buttonItem.setIsCheck(true);
//--------------------------
        ButtonItem buttonItem1 = new ButtonItem();
        buttonItem1.setButtonName("大小");
        buttonItem1.setButtonSize(19);
        buttonItem1.setButtonColor(Color.BLACK);
        buttonItem1.setId(2);
//        buttonItem1.setButtonHeight(180);
//        buttonItem1.setButtonWidth(240);
        buttonItem1.setIsCheck(true);
//--------------------------
        ButtonItem buttonItem2 = new ButtonItem();
        buttonItem2.setButtonName("颜色");
        buttonItem2.setButtonSize(19);
        buttonItem2.setButtonColor(Color.BLACK);
        buttonItem2.setId(3);
//        buttonItem2.setButtonHeight(180);
//        buttonItem2.setButtonWidth(240);
        buttonItem2.setIsCheck(true);
//--------------------------
        ButtonItem buttonItem3 = new ButtonItem();
        buttonItem3.setButtonName("撤销");
        buttonItem3.setButtonSize(19);
        buttonItem3.setButtonColor(Color.BLACK);
        buttonItem3.setId(4);
//        buttonItem3.setButtonHeight(180);
//        buttonItem3.setButtonWidth(240);
        buttonItem3.setIsCheck(true);
//--------------------------
        ButtonItem buttonItem4 = new ButtonItem();
        buttonItem4.setButtonName("清空");
        buttonItem4.setButtonSize(19);
        buttonItem4.setButtonColor(Color.BLACK);
        buttonItem4.setId(5);
//        buttonItem4.setButtonHeight(180);
//        buttonItem4.setButtonWidth(240);
        buttonItem4.setIsCheck(true);

        ButtonItem buttonItem5 = new ButtonItem();
        buttonItem5.setButtonName("橡皮擦");
//        buttonItem5.setCheckName("画笔");
        buttonItem5.setButtonSize(19);
        buttonItem5.setButtonColor(Color.BLACK);
        buttonItem5.setId(6);
//        buttonItem5.setButtonHeight(180);
//        buttonItem5.setButtonWidth(240);
        buttonItem5.setIsCheck(true);

        ButtonItem buttonItem6 = new ButtonItem();
        buttonItem6.setButtonName("恢复");
        buttonItem6.setButtonSize(19);
        buttonItem6.setButtonColor(Color.BLACK);
        buttonItem6.setId(7);
//        buttonItem6.setButtonHeight(180);
//        buttonItem6.setButtonWidth(240);
        buttonItem6.setIsCheck(true);

        ButtonItem buttonItem7 = new ButtonItem();
        buttonItem7.setButtonName("画笔");
        buttonItem7.setButtonSize(19);
        buttonItem7.setButtonColor(Color.BLACK);
        buttonItem7.setId(8);
//        buttonItem7.setButtonHeight(180);
//        buttonItem7.setButtonWidth(240);
        buttonItem7.setIsCheck(true);

        ButtonItem buttonItem8 = new ButtonItem();
        buttonItem8.setButtonName("上次");
        buttonItem8.setButtonSize(19);
        buttonItem8.setButtonColor(Color.BLACK);
        buttonItem8.setId(9);
//        buttonItem8.setButtonHeight(180);
//        buttonItem8.setButtonWidth(240);
        buttonItem8.setIsCheck(true);

        buttonList.add(buttonItem);
        buttonList.add(buttonItem1);
        buttonList.add(buttonItem2);
        buttonList.add(buttonItem3);
        buttonList.add(buttonItem4);
        buttonList.add(buttonItem5);
        buttonList.add(buttonItem6);
        buttonList.add(buttonItem7);
        buttonList.add(buttonItem8);
        customButton = (CustomButton) findViewById(R.id.custombutton);
        customButton.CreateLayout(buttonList);
        customButton.setButtonListener(new CustomButton.ButtonListener() {
            public void showTip(ButtonItem buttonItem) {
                Toast.makeText(getApplicationContext(), "" + buttonItem.getButtonName(), Toast.LENGTH_SHORT).show();

            }
        });
    }
}
