package com.example.luggageassistant.utils;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback.makeMovementFlags;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeController extends ItemTouchHelper.Callback {

    public interface SwipeActionListener {
        void onPin(int position);
        void onDelete(int position);
    }

    private boolean swipeBack = false;
    private final SwipeActionListener listener;

    private final int buttonWidth = 200; // px
    private int swipedPosition = -1;


    public SwipeController(SwipeActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Nothing here: we'll handle manually in onChildDraw
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;

        if (dX < -buttonWidth * 2) dX = -buttonWidth * 2;

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (dX < 0) {
            drawButtons(c, itemView);
            if (isCurrentlyActive) {
                swipedPosition = viewHolder.getAdapterPosition();
            }
        }
    }

    private void drawButtons(Canvas canvas, View itemView) {
        Paint paint = new Paint();

        int width = itemView.getWidth();
        int height = itemView.getHeight();

        int top = 0;
        int bottom = height;

        // Rect-uri locale (relative la itemView)
        Rect deleteButton = new Rect(width - buttonWidth, top, width, bottom);
        paint.setColor(Color.parseColor("#EF5350")); // red
        canvas.drawRect(deleteButton, paint);
        drawText("🗑️", canvas, deleteButton, paint);

        Rect pinButton = new Rect(width - 2 * buttonWidth, top, width - buttonWidth, bottom);
        paint.setColor(Color.parseColor("#66BB6A")); // green
        canvas.drawRect(pinButton, paint);
        drawText("📌", canvas, pinButton, paint);

        this.deleteButtonInstance = deleteButton;
        this.pinButtonInstance = pinButton;
    }

    private void drawText(String text, Canvas c, Rect button, Paint p) {
        float textSize = 50;
        p.setColor(Color.WHITE);
        p.setAntiAlias(true);
        p.setTextSize(textSize);

        float textWidth = p.measureText(text);
        float textX = button.centerX() - textWidth / 2;
        float textY = button.centerY() + textSize / 2 - 10;

        c.drawText(text, textX, textY, p);
    }

    private Rect pinButtonInstance = null;
    private Rect deleteButtonInstance = null;

    public boolean handleClick(MotionEvent e, RecyclerView recyclerView) {
        View touchedView = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (touchedView == null) {
            Log.d("SwipeDebug", "No touched view found under finger");
            return false;
        }

        int pos = recyclerView.getChildAdapterPosition(touchedView);
        if (pos == RecyclerView.NO_POSITION) {
            Log.d("SwipeDebug", "Invalid position");
            return false;
        }

        float relativeX = e.getX() - touchedView.getLeft();
        float relativeY = e.getY() - touchedView.getTop();

        Log.d("SwipeDebug", "Touch at: x=" + relativeX + ", y=" + relativeY);

        if (deleteButtonInstance != null) {
            Log.d("SwipeDebug", "Delete bounds: " + deleteButtonInstance.toShortString());
        }
        if (pinButtonInstance != null) {
            Log.d("SwipeDebug", "Pin bounds: " + pinButtonInstance.toShortString());
        }

        if (deleteButtonInstance != null && deleteButtonInstance.contains((int) relativeX, (int) relativeY)) {
            Log.d("SwipeDebug", "DELETE button pressed");
            listener.onDelete(pos);
            swipedPosition = -1;
            return true;
        }

        if (pinButtonInstance != null && pinButtonInstance.contains((int) relativeX, (int) relativeY)) {
            Log.d("SwipeDebug", "PIN button pressed");
            listener.onPin(pos);
            swipedPosition = -1;
            return true;
        }

        Log.d("SwipeDebug", "No button hit");
        return false;
    }

    public int getSwipedPosition() {
        return swipedPosition;
    }
}
