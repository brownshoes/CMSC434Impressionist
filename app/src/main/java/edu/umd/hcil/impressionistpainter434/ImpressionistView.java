package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

import java.text.MessageFormat;
import java.util.Random;
import java.util.UUID;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    public Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 150;
    private int _defaultRadius = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;

    private VelocityTracker mVelocityTracker = null;

    public Bitmap overlay;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        //TODO

        //from:
        //http://stackoverflow.com/questions/5729377/android-canvas-how-do-i-clear-delete-contents-of-a-canvas-bitmaps-livin
        _offScreenCanvas.drawColor(Color.WHITE);
        invalidate();
    }

    public void setBitmap(Bitmap bitmap){

        overlay = bitmap;
        Log.d("", "in setBitmap bitmap height " + overlay.getHeight());
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //TODO
        //Basically, the way this works is to listen for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        float curTouchX = motionEvent.getX();
        float curTouchY = motionEvent.getY();

        int curTouchXRounded = (int) curTouchX;
        int curTouchYRounded = (int) curTouchY;

        int historySize = motionEvent.getHistorySize();

        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:

                Log.d("", "ACTION_DOWN " + _lastPointTime);

                _lastPointTime = motionEvent.getEventTime();
                _lastPoint = new Point(curTouchXRounded, curTouchYRounded);
                _lastPointTime = motionEvent.getEventTime();

                break;
            case MotionEvent.ACTION_MOVE:

                Log.d("", "ACTION_MOVE @" + _lastPointTime);

                //from:
                //http://stackoverflow.com/questions/7807360/how-to-get-pixel-colour-in-android
                Bitmap bitmap; //= _imageView.getDrawingCache();
                //Bitmap bitmap = ((BitmapDrawable)_imageView.getDrawable()).getBitmap();


                if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                    //bitmap = overlay;
                    //Log.d("", "bitmap height after assignment from overlay bitmap" + bitmap.getHeight());
                } else {
                    //bitmap = _imageView.getDrawingCache();
                }
                bitmap = _imageView.getDrawingCache();
                // For efficiency, motion events with ACTION_MOVE may batch together multiple movement samples within a single object.
                // The most current pointer coordinates are available using getX(int) and getY(int).
                // Earlier coordinates within the batch are accessed using getHistoricalX(int, int) and getHistoricalY(int, int).
                // See: http://developer.android.com/reference/android/view/MotionEvent.html

                for (int i = 0; i < historySize; i++) {

                    float touchX = motionEvent.getHistoricalX(i);
                    float touchY = motionEvent.getHistoricalY(i);

                    try {
                        int pixel = bitmap.getPixel(curTouchXRounded, curTouchYRounded);

                        int red = Color.red(pixel);
                        int blue = Color.blue(pixel);
                        int green = Color.green(pixel);
                        int color = Color.argb(_alpha, red, green, blue);

                        _paint.setColor(color);

                        Log.d("", "bitmap height in Historical loop" + bitmap.getHeight());
                        Log.d("", "green" + green);

                    } catch(NullPointerException e) {
                        Log.d("", "Historical null pointer exception");
                        break;
                    } catch(IllegalArgumentException e){

                        Log.d("", "Historical IllegalArgumentException");
                        break;
                    }

                    // TODO: draw to the offscreen bitmap for current x,y point.
                    // Insert one line of code here
                    //_offScreenCanvas.drawPoint(touchX, touchY, _paint);

                    if(_brushType == BrushType.Square){

                        _offScreenCanvas.drawRect(curTouchX, curTouchY, curTouchX + 12, curTouchY + 12, _paint);
                    }

                    if(_brushType == BrushType.Circle) {

                        //_offScreenCanvas.drawCircle(curTouchX, curTouchY, _defaultRadius - 18, _paint);
                        _offScreenCanvas.drawPoint(touchX, touchY, _paint);
                    }
                }

                Log.d("", "End historical loop");

                    // TODO: draw to the offscreen bitmap for historical x,y points
                    // Insert one line of code here

                    try{
                        int pixel = bitmap.getPixel(curTouchXRounded, curTouchYRounded);
                        int red = Color.red(pixel);
                        int blue = Color.blue(pixel);
                        int green = Color.green(pixel);

                        int color = Color.argb(_alpha, red, green, blue);

                        _paint.setColor(color);

                        Log.d("", "one line bitmap height " + bitmap.getHeight());
                        Log.d("", "green" + green);

                    } catch(NullPointerException e) {
                        Log.d("", "Current null pointer exception");
                        break;
                    } catch(IllegalArgumentException e){

                        Log.d("", "Current IllegalArgumentException");
                        break;
                    }

                    Random ran = new Random();

                    if(_brushType == BrushType.Square){

                        _offScreenCanvas.drawRect(curTouchX, curTouchY, curTouchX + 12, curTouchY + 12, _paint);
                    }

                    if(_brushType == BrushType.Circle){

                        //calculate distance
                        long distX = (curTouchXRounded -  _lastPoint.x);
                        distX = distX * distX;

                        long distY = (curTouchYRounded -  _lastPoint.y);
                        distY = distY * distY;

                        long distance = (long) Math.sqrt(distX + distY);

                        //calculate time
                        long currentPointTime = motionEvent.getEventTime();
                        long time = currentPointTime - _lastPointTime;

                        //calculate velocity
                        float velocity = (float) distance/ time;

                        Log.d("", "distance: " + distance + " time: " + time + " velocity: " + velocity);

                        //_offScreenCanvas.drawCircle(curTouchX, curTouchY, _defaultRadius * velocity, _paint);
                        _offScreenCanvas.drawCircle(curTouchX, curTouchY, _defaultRadius - 18, _paint);

                        _lastPointTime = motionEvent.getEventTime();
                        _lastPoint.set(curTouchXRounded, curTouchYRounded);
                    }

                    if(_brushType == BrushType.Line){

                        _offScreenCanvas.drawLine(curTouchX, curTouchY, curTouchX, curTouchY + 25, _paint);

                    }

                    if(_brushType == BrushType.CircleSplatter){

                        for(int count = 0; count < 10; count++) {
                            _offScreenCanvas.drawCircle(curTouchX + (ran.nextInt(20)),
                                    curTouchY + (ran.nextInt(20)),  ran.nextInt(12), _paint);
                        }
                    }

                    if(_brushType == BrushType.LineSplatter){

                        for(int count = 0; count < 10; count++){
                            _offScreenCanvas.drawLine(curTouchX, curTouchY, curTouchX + ran.nextInt(10), curTouchY + ran.nextInt(10), _paint);
                        }

                    }



                //_offScreenCanvas.drawPoint(curTouchX, curTouchY, _paint);


                invalidate();

                break;

            case MotionEvent.ACTION_UP:

                _lastPointTime = -1;
                _lastPoint = null;

                Log.d("", "ACTION_UP @ " + motionEvent.getEventTime());
                //setVisibility(View.GONE);
                break;

        }

        return true;
    }




    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }
}

