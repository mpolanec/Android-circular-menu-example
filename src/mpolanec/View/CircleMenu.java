package mpolanec.View;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import android.util.DisplayMetrics;
import mpolanec.CircleMenuTestApp.R;

public class CircleMenu extends View {

    public interface OnCircleMenuItemClickListener
    {
        /**
         * Method to be called when a click occurs in menu.
         * index is set to:
         * - -1 if we clicked outside the menu,
         * -  0 if we clicked main menu button,
         * -  1 ... icons.size() if we touched an item in a menu
         * @param index
         */
        public void onCircleMenuItemClick(int index);
    }

    public interface OnCircleMenuAnimationFinishListener
    {
        public void onCircleMenuAnimationFinished();
    }

    public static final int SELECTED_OUTSIDE = -1;
    public static final int SELECTED_CENTER  =  0;

    private Paint borderPencil;
    private Paint itemDefaultColor;
    private Paint itemTouchedBack;
    private Paint rainbowBorderPaint;

    private static final int startAngle = 168;
    private static final int endAngle   = 372;

    private CircleItemAngle angles[] = null;

    public static int[] colors = new int[] {
            0xb2a6ce39,
            0xb200a1e4,
            0xb200a1e4,
            0xb2ed1c24
    };

    private RectF middleOval = new RectF();
    private RectF rainbowOval = new RectF();

    private int centerX, centerY;

    private boolean[] itemTouched = null;

    private EmbossMaskFilter forBig;

    private ArrayList<Bitmap> icons = new ArrayList<Bitmap>();
    /**
     * Is menu ready to be drawn?
     */
    private boolean prepared = false;

    private Paint smallBorderPencil;

    private OnCircleMenuItemClickListener onItemListener;
    private OnCircleMenuAnimationFinishListener onAnimationFinish;

    public CircleMenu(Context context) {
        super(context);
        initMenu();
    }

    public CircleMenu(Context context, AttributeSet attri) {
        super(context, attri);
        initMenu();
    }

    public CircleMenu(Context context, AttributeSet attri, int defaultStyle) {
        super(context, attri, defaultStyle);
        initMenu();
    }
    /**
     * Prepare menu to be drawn.
     */
    public void prepareMenu() throws Exception
    {
        if(icons.size() == 0)
            throw new Exception("No icons!");

        this.itemTouched = new boolean[icons.size()];
        markAllItemsNotTouched();

        if(!CircleMenuSettings.isInitialized)
            CircleMenuSettings.initializeSettings(getContext());

        preparePainters();

        calculateAngles();

        prepared  = true;
    }
    /**
     * Calculate angles for the items.
     */
    private void calculateAngles()
    {
        this.angles = new CircleItemAngle[icons.size()];

        float step = (CircleMenu.endAngle-CircleMenu.startAngle)/icons.size();
        float currentAngle = CircleMenu.startAngle;

        for(int i=0;i<icons.size(); i++, currentAngle += step)
        {
            if(this.angles[i] == null)
                this.angles[i] = new CircleItemAngle();
            this.angles[i].setStartAngle(currentAngle);
            this.angles[i].setEndAngle(currentAngle+step);
        }

        this.angles[0].setStartAngle(180-89);
        this.angles[this.angles.length-1].setEndAngle(360+90);
    }
    private void preparePainters() {

        borderPencil = new Paint(Paint.ANTI_ALIAS_FLAG);

        borderPencil.setStrokeWidth(0.5f);
        borderPencil.setColor(0xFF6d6968);
        borderPencil.setStyle(Paint.Style.STROKE);

        itemDefaultColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        itemDefaultColor.setStyle(Paint.Style.FILL);
        itemDefaultColor.setColor(0xfff3f3f3);

        itemTouchedBack = new Paint(Paint.ANTI_ALIAS_FLAG);
        itemTouchedBack.setColor(0xffFFFFFF);
        itemTouchedBack.setStyle(Paint.Style.FILL);

        rainbowBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rainbowBorderPaint.setStyle(Paint.Style.STROKE);
        rainbowBorderPaint.setStrokeWidth(CircleMenuSettings.rainbowCircleBorderWidth);

        smallBorderPencil = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallBorderPencil.setColor(0xFFE6E6E6);
        smallBorderPencil.setStyle(Paint.Style.FILL);

        forBig = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 1f, 4, 2.5f);
        itemDefaultColor.setMaskFilter(forBig);
    }

    private void initMenu() {
        setFocusable(true);
        this.setBackgroundColor(Color.TRANSPARENT);

        icons.clear();
    }
    public void addIcon(int resourceId)
    {
        icons.add(BitmapFactory.decodeResource(getResources(), resourceId));
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if(!prepared)
            return;

        centerX = getMeasuredWidth() / 2 - 1;
        centerY = getMeasuredHeight()-(int)(CircleMenuSettings.smallCircleRadius/1.82);

        float currentAngle, finalAngle;
        // rainbow frame
        rainbowOval.set(
                centerX - CircleMenuSettings.rainbowCircleRadius + CircleMenuSettings.rainbowCircleBorderWidth/2 + 1, // TOP LEFT X
                centerY - CircleMenuSettings.rainbowCircleRadius + CircleMenuSettings.rainbowCircleBorderWidth/2 + 1, // TOP LEFT Y
                centerX + CircleMenuSettings.rainbowCircleRadius - CircleMenuSettings.rainbowCircleBorderWidth/2 - 1, // BOTTOM RIGHT X
                centerY + CircleMenuSettings.rainbowCircleRadius - CircleMenuSettings.rainbowCircleBorderWidth/2 - 1);// BOTTOM RIGHT Y
        // items frame
        middleOval.set(
                centerX - CircleMenuSettings.middleCircleRadius,
                centerY - CircleMenuSettings.middleCircleRadius,
                centerX + CircleMenuSettings.middleCircleRadius,
                centerY + CircleMenuSettings.middleCircleRadius);

        // draw rainbow
        currentAngle = CircleMenu.startAngle;
        finalAngle   = CircleMenu.endAngle;
        float rainbowStep = (CircleMenu.endAngle - CircleMenu.startAngle)/10;

        for (int i=0;currentAngle < finalAngle;)
        {
            rainbowBorderPaint.setColor(colors[i]);
            canvas.drawArc(rainbowOval, currentAngle+1, rainbowStep, false, rainbowBorderPaint);

            i++;
            if(i==2)
                i++;
            if (i >= colors.length) {
                i = 0;
            }
            currentAngle += rainbowStep;

        }
        // draws a border
        canvas.drawArc(middleOval, 180, 180, false, borderPencil);

        currentAngle = CircleMenu.startAngle;

        float step = (CircleMenu.endAngle-CircleMenu.startAngle)/icons.size();

        for(int i=0;i<icons.size(); i++, currentAngle += step)
        {
            // draw the menu item
            if (itemTouched[i]) {// if it's pressed
                canvas.drawArc(middleOval,
                        this.angles[i].getStartAngle(),
                        this.angles[i].getEndAngle()-this.angles[i].getStartAngle(),
                        true,
                        itemTouchedBack); // draw item
                canvas.drawArc(middleOval,
                        this.angles[i].getStartAngle(),
                        this.angles[i].getEndAngle()-this.angles[i].getStartAngle(),
                        true,
                        borderPencil); // draw item border
            } else { // if it's not pressed
                canvas.drawArc(middleOval,
                        this.angles[i].getStartAngle(),
                        this.angles[i].getEndAngle()-this.angles[i].getStartAngle(),
                        true,
                        itemDefaultColor);
                canvas.drawArc(middleOval,
                        this.angles[i].getStartAngle(),
                        this.angles[i].getEndAngle()-this.angles[i].getStartAngle(),
                        true,
                        borderPencil);
            }
            canvas.drawBitmap(icons.get(i),
                    null,
                    putBitmapTo(currentAngle,
                            step,
                            CircleMenuSettings.iconRadius, // how far from the center
                            centerX,
                            centerY),
                    null); // paint

        }
        canvas.save();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float testCenterX = centerX;
        float testCenterY = centerY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (isInSideCenterCircle(testCenterX, testCenterY, CircleMenuSettings.smallCircleRadius,
                        event.getX(), event.getY()))
                {
                    markAllItemsNotTouched();
                    invalidate();
                    return true;
                }
                else
                {
                    for(int i=0;i<icons.size(); i++)
                    {
                        if (isInItemsArc(this.angles[i].getStartAngle(), this.angles[i].getEndAngle() - this.angles[i].getStartAngle(), CircleMenuSettings.middleCircleRadius, centerX, centerY, event.getX(),
                                event.getY())) {
                            markAllItemsNotTouched();
                            itemTouched[i] = true;
                            invalidate();
                            return true;
                        }
                    }
                }
                markAllItemsNotTouched();
                invalidate();
                return true;//default
            }
            case MotionEvent.ACTION_UP: {

                if (isInSideCenterCircle(testCenterX, testCenterY, CircleMenuSettings.smallCircleRadius,
                        event.getX(), event.getY())) {
                    this.notifyOnCircleMenuItemListener(SELECTED_CENTER);
                    markAllItemsNotTouched();
                    invalidate();
                    return true;
                }
                else
                {
                    for(int i=0;i<icons.size(); i++)
                    {
                        if (isInItemsArc(this.angles[i].getStartAngle(), this.angles[i].getEndAngle() - this.angles[i].getStartAngle(), CircleMenuSettings.middleCircleRadius, centerX, centerY, event.getX(),
                                event.getY())) {
                            this.notifyOnCircleMenuItemListener(i+1);
                            markAllItemsNotTouched();
                            invalidate();
                            return true;
                        }
                    }
                }

                this.notifyOnCircleMenuItemListener(SELECTED_OUTSIDE);
                markAllItemsNotTouched();
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isInSideCenterCircle(testCenterX, testCenterY, CircleMenuSettings.smallCircleRadius,
                        event.getX(), event.getY())) {
                    markAllItemsNotTouched();
                    invalidate();
                    return true;
                }
                else
                {
                    for(int i=0;i<icons.size(); i++)
                    {
                        if (isInItemsArc(this.angles[i].getStartAngle(), this.angles[i].getEndAngle() - this.angles[i].getStartAngle(), CircleMenuSettings.middleCircleRadius, centerX, centerY, event.getX(),
                                event.getY())) {
                            markAllItemsNotTouched();
                            itemTouched[i] = true;
                            invalidate();
                            return true;
                        }
                    }
                }
                markAllItemsNotTouched();
                invalidate();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * Untranslated comment was removed
     */
    private boolean isInSideCenterCircle(float centerX,
                                         float centerY,
                                         float radius,
                                         float eventX,
                                         float eventY)
    {
        return (getVectorLength(centerX, centerY, eventX, eventY)) <= radius;
    }

    /**
     * Untranslated comment was removed
     */
    private RectF putBitmapTo(float startAngle, float SweepAngle, float radius,
                              float centerX, float centerY) {
        float locX = (float) (centerX + ((radius / 17 * 11) * Math.cos(Math
                .toRadians((startAngle + SweepAngle + startAngle) / 2))));
        float locY = (float) (centerY + ((radius / 17 * 11) * Math.sin(Math
                .toRadians((startAngle + SweepAngle + startAngle) / 2))));
        return new RectF(locX - CircleMenuSettings.iconSize, locY - CircleMenuSettings.iconSize, locX + CircleMenuSettings.iconSize, locY + CircleMenuSettings.iconSize);

    }

    /**
     * Untranslated comment was removed
     */
    private boolean isInItemsArc(float startAngle, float sweepAngle, float radius,
                                 float centerX, float centerY, float eventX, float eventY) {

        if (getVectorLength(centerX, centerY, eventX, eventY) <= radius )
        {
            float x0 = centerX - (centerX + radius - 4);
            float y0 = centerY - centerY;

            float x1 = centerX - eventX;
            float y1 = centerY - eventY;

            double angle = getAngleBetweenVectors(getScalarProduct(x0, y0, x1, y1),
                    getVectorLength(centerX, centerY, eventX, eventY),
                    getVectorLength(centerX, centerY, centerX+radius-4, centerY));

            return angle > startAngle && angle <= (startAngle + sweepAngle);
        }
        return false;
    }
    /**
     * Untranslated comment was removed
     */
    private double getVectorLength(float sX, float sY, float eX, float eY)
    {
        return Math.sqrt(Math.pow((sX - eX), 2)+Math.pow((sY - eY), 2));
    }
    /**
     * Untranslated comment was removed
     */
    private double getAngleBetweenVectors(double scalarProductOfVectors, double lengthOfVector1, double lengthOfVector2)
    {
        return rad2deg(Math.acos(scalarProductOfVectors / (lengthOfVector1 * lengthOfVector2)));
    }
    /**
     * Untranslated comment was removed
     */
    private double getScalarProduct(float sX, float sY, float eX, float eY)
    {
        return (double) (sX * eX + sY * eX);
    }
    /**
     * Untranslated comment was removed
     */
    private double rad2deg(double rad)
    {
        return 360 - Math.toDegrees(rad);
    }
    /**
     * Untranslated comment was removed
     */
    private void markAllItemsNotTouched() {
        for (int i = 0; i < itemTouched.length; i++) {
            itemTouched[i] = false;
        }
    }
    public void setOnCircleMenuItemClickListener(OnCircleMenuItemClickListener onCircleMenuItemClickListener)
    {
        this.onItemListener = onCircleMenuItemClickListener;
    }
    private void notifyOnCircleMenuItemListener(int index)
    {
        if(onItemListener != null)
            onItemListener.onCircleMenuItemClick(index);
    }
    public void showMenu()
    {
        this.setVisibility(View.VISIBLE);
        Animation showAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.circle_menu_grow_from_bottom);
        showAnimation.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                animateShrink();
                notifyOnAnimationEndListener();
            }


        });
        this.startAnimation(showAnimation);
    }
    private void animateShrink() {
        Animation showAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.circle_menu_grow_from_bottom_shrink);
        this.startAnimation(showAnimation);
    }
    public void hideMenu()
    {
        Animation hideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.circle_menu_shrink_from_top);
        hideAnimation.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                setVisibility(View.GONE);
                notifyOnAnimationEndListener();
            }
        });
        this.startAnimation(hideAnimation);
    }
    public void setOnAnimationFinishedListener(
            OnCircleMenuAnimationFinishListener onAnimationFinish) {
        this.onAnimationFinish = onAnimationFinish;
    }
    private void notifyOnAnimationEndListener()
    {
        if(onAnimationFinish != null)
            onAnimationFinish.onCircleMenuAnimationFinished();
    }
    private class CircleItemAngle
    {
        private float startAngle;
        private float endAngle;

        public float getStartAngle() {
            return startAngle;
        }
        public void setStartAngle(float startAngle) {
            this.startAngle = startAngle;
        }
        public float getEndAngle() {
            return endAngle;
        }
        public void setEndAngle(float endAngle) {
            this.endAngle = endAngle;
        }
    }

    public static class CircleMenuSettings {

        public static final int ERROR = -1;
        public static int rainbowCircleRadius = ERROR;
        public static int rainbowCircleBorderWidth = ERROR;

        public static int middleCircleRadius = ERROR;

        public static int smallCircleRadius = ERROR;
        public static int smallCircleBorderWidth = ERROR;

        public static int iconRadius = ERROR;

        public static int iconSize = ERROR;

        public static boolean isInitialized = false;


        public static void initializeSettings(Context c)
        {
            if(isInitialized)
                return;
            DisplayMetrics metrics = c.getResources().getDisplayMetrics();

            int minSize = Math.min(metrics.heightPixels, metrics.widthPixels);

            rainbowCircleRadius = getDimension(minSize, 0.925);
            rainbowCircleBorderWidth = getDimension(minSize, 0.040625);

            middleCircleRadius = getDimension(minSize, 0.884375);

            smallCircleRadius = getDimension(minSize, 0.256875);
            smallCircleBorderWidth = getDimension(minSize, 0.0125);

            iconSize = getDimension(minSize, 0.15625);
            iconRadius = getDimension(minSize, 0.9578);


            if(rainbowCircleRadius > 0)
                isInitialized = true;

        }
        private static int getDimension(int size, double factor)
        {
            return (int)( ( (double)size * factor) / 2.0 );
        }

    }

}