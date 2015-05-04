package com.youchip.youmobile.view.shop;

import com.youchip.youmobile.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class ShopItemTextView extends TextView{
    

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final String MULTI_PLACEHOLDER = "++";
    private static final String SPECIAL_PLACEHOLDER = "*";
    private long quantity = 0;
    private int quantity_bg_color = Color.LTGRAY;
    private int quantity_font_color = Color.BLACK;
    private float quantity_font_scale = 0.9f;
    private float quantity_bg_scale = 0.2f;    
    private int radius = 0;
    private int centerX = 0;
    private int centerY = 0;

    public ShopItemTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ShopItemTextView,
                0, 0);

           try {
               quantity             = a.getInt(R.styleable.ShopItemTextView_quantity, 0);
               quantity_bg_scale    = a.getFloat(R.styleable.ShopItemTextView_quantity_bg_scale, 0.2f);
               quantity_font_scale  = a.getFloat(R.styleable.ShopItemTextView_quantity_font_scale, 0.9f);
               quantity_bg_color    = a.getColor(R.styleable.ShopItemTextView_quantity_bg_color, Color.LTGRAY);
               quantity_font_color  = a.getColor(R.styleable.ShopItemTextView_quantity_font_color, Color.BLACK);
           } finally {
               a.recycle();
           }
        
        setCounterRadius();
        
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(Math.round((float) getTextSize() * quantity_font_scale));
    }
    
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        
        setCounterRadius();
        
        if (quantity != 0 && centerX > 0 && centerY > 0){
            drawCircle(canvas);
            drawQuantity(canvas);
        }
    }
    
    private void drawCircle(Canvas canvas){
        //draw the circle
        paint.setColor(quantity_bg_color);
        paint.setStyle(Style.FILL);
        canvas.drawCircle(centerX, centerY, radius, paint);
    }
    
    private void drawQuantity(Canvas canvas){
        //draw the number
        paint.setColor(quantity_font_color);
        String value = String.valueOf(quantity);
        if (quantity > 0 && paint.measureText(value) < radius*2) {
            canvas.drawText(value, centerX, centerY + 5, paint);
        } else if (quantity < 0){
            canvas.drawText(SPECIAL_PLACEHOLDER, centerX, centerY+5, paint);
        } else {
            canvas.drawText(MULTI_PLACEHOLDER, centerX, centerY+5, paint);
        }
    }



    private void setCounterRadius(){
        radius = Math.min(Math.round(((float) getWidth()) * (quantity_bg_scale)), Math.round(((float) getHeight()) * quantity_bg_scale));
        calcCenterX();
        calcCenterY();
        invalidate();
        requestLayout();
    }
    
    @Override
    public void setWidth(int width){
        super.setWidth(width);
        setCounterRadius();
    }
    
    protected void calcCenterX(){
        centerX = getWidth() - radius;
    }
    
    @Override
    public void setHeight(int height){
        super.setHeight(height);
        setCounterRadius();
    }
    
    protected void calcCenterY(){
        centerY = radius;
    }
    

    public int getItemCounterFontColor() {
        return quantity_font_color;
    }

    public void setItemCounterFontColor(int itemCounterFontColor) {
        this.quantity_font_color = itemCounterFontColor;
        invalidate();
        requestLayout();
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long itemCounter) {
        this.quantity = itemCounter;
        invalidate();
        requestLayout();
    }

    public int getItemCounterBackgroundColor() {
        return quantity_bg_color;
    }

    public void setItemCounterBackgroundColor(int itemCounterBGColor) {
        this.quantity_bg_color = itemCounterBGColor;
        invalidate();
        requestLayout();
    }

    public float getQuantityFontScale() {
        return quantity_font_scale;
    }

    public void setQuantityFontScale(float quantity_font_scale) {
        this.quantity_font_scale = quantity_font_scale;
        invalidate();
        requestLayout();
    }

    public float getQuantityBackgroundScale() {
        return quantity_bg_scale;
    }

    public void setQuantityBackgroundScale(float quantity_bg_scale) {
        this.quantity_bg_scale = quantity_bg_scale;
        invalidate();
        requestLayout();
    }

    
    
}
