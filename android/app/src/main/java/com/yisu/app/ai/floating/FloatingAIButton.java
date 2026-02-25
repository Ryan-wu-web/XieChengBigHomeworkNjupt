package com.yisu.app.ai.floating;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.yisu.app.R;

public class FloatingAIButton {
    private Context context;
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private boolean isDragging = false;
    private float initialX, initialY;
    private int initialTouchX, initialTouchY;
    private OnFloatingButtonClickListener listener;

    public interface OnFloatingButtonClickListener {
        void onClick();
    }

    public FloatingAIButton(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initFloatingView();
    }

    private void initFloatingView() {
        floatingView = LayoutInflater.from(context).inflate(R.layout.floating_ai_button, null);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 300;

        floatingView.setOnClickListener(v -> {
            if (!isDragging && listener != null) {
                listener.onClick();
            }
        });

        floatingView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isDragging = false;
                    initialX = params.x;
                    initialY = params.y;
                    initialTouchX = (int) event.getRawX();
                    initialTouchY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(event.getRawX() - initialTouchX) > 10 ||
                            Math.abs(event.getRawY() - initialTouchY) > 10) {
                        isDragging = true;
                    }
                    params.x = (int) (initialX + event.getRawX() - initialTouchX);
                    params.y = (int) (initialY + event.getRawY() - initialTouchY);
                    windowManager.updateViewLayout(floatingView, params);
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isDragging && listener != null) {
                        listener.onClick();
                    }
                    break;
            }
            return false;
        });
    }

    public void setOnFloatingButtonClickListener(OnFloatingButtonClickListener listener) {
        this.listener = listener;
    }

    public void show() {
        if (floatingView.getParent() == null) {
            windowManager.addView(floatingView, params);
        }
    }

    public void hide() {
        if (floatingView.getParent() != null) {
            windowManager.removeView(floatingView);
        }
    }

    public boolean isShowing() {
        return floatingView.getParent() != null;
    }
}