package mpolanec.CircleMenuTestApp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import mpolanec.View.CircleMenu;

public class MainActivity extends Activity implements CircleMenu.OnCircleMenuItemClickListener,
        CircleMenu.OnCircleMenuAnimationFinishListener, View.OnClickListener
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private CircleMenu circleMenu;
    private ImageView menuBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        circleMenu = (CircleMenu) findViewById(R.id.circleMenu);

        menuBtn = (ImageView) findViewById(R.id.imgMainMenuBtn);
        menuBtn.setOnClickListener(this);

        circleMenu.addIcon(R.drawable.ic_action_call);
        circleMenu.addIcon(R.drawable.ic_action_cloud);
        circleMenu.addIcon(R.drawable.ic_action_go_to_today);
        circleMenu.addIcon(R.drawable.ic_action_good);
        circleMenu.addIcon(R.drawable.ic_action_search);
        try {
            circleMenu.prepareMenu();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
        circleMenu.setOnCircleMenuItemClickListener(this);
        circleMenu.setOnAnimationFinishedListener(this);
    }
    public void onCircleMenuItemClick(int index) {
        if(index == CircleMenu.SELECTED_CENTER)
            this.showToastMsg(getString(R.string.touched_center));
        else if(index == CircleMenu.SELECTED_OUTSIDE)
            this.showToastMsg(getString(R.string.touched_outside));
        else
            this.showToastMsg(getString(R.string.touched_index)+index);
        circleMenu.hideMenu();
    }
    private void showToastMsg(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    public void onCircleMenuAnimationFinished() {
        if(circleMenu.isShown())
        {
            menuBtn.setVisibility(View.GONE);
            menuBtn.clearAnimation();
        }
        else
        {
            menuBtn.setVisibility(View.VISIBLE);
            Animation a = AnimationUtils.loadAnimation(this, R.anim.circle_menu_center_button_fade_in);
            menuBtn.startAnimation(a);
        }
    }
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.imgMainMenuBtn:
            {
                if(!circleMenu.isShown())
                {
                    Animation a = AnimationUtils.loadAnimation(this, R.anim.circle_menu_center_button_fade_out);

                    a.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            circleMenu.showMenu();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }
                    });

                    menuBtn.startAnimation(a);
                }
                break;
            }
            case R.id.imgMainMenuButtonBack:
            {
                if(circleMenu.isShown())
                {
                    circleMenu.hideMenu();
                }
            }
        }
    }
}
