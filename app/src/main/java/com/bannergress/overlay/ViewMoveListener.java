package com.bannergress.overlay;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class ViewMoveListener implements View.OnTouchListener {
    private final OverlayView view;
    private final WindowManager windowManager;
    private float pointerStartX;
    private float pointerStartY;
    private int windowStartX;
    private int windowStartY;

    public ViewMoveListener(OverlayView view) {
        this.view = view;
        this.windowManager = view.getContext().getSystemService(WindowManager.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointerStartX = event.getRawX();
                pointerStartY = event.getRawY();
                windowStartX = view.getWindowParams().x;
                windowStartY = view.getWindowParams().y;
                break;
            case MotionEvent.ACTION_MOVE:
                float pointerX = event.getRawX();
                float pointerY = event.getRawY();
                view.getWindowParams().x = (int) (pointerX - pointerStartX + windowStartX);
                view.getWindowParams().y = (int) (pointerY - pointerStartY + windowStartY);
                windowManager.updateViewLayout(view, view.getWindowParams());
                break;
        }
        return true;
    }
}
