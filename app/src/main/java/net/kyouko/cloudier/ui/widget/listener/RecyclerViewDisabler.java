package net.kyouko.cloudier.ui.widget.listener;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;

/**
 * Implementation of {@link RecyclerView.OnItemTouchListener} that can disable all touch events of
 * a {@link RecyclerView}, including scrolling and clicks on item views.
 *
 * @author beta
 */
public class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {

    private boolean disabled = false;


    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }


    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return disabled;
    }


    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }


    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

}
