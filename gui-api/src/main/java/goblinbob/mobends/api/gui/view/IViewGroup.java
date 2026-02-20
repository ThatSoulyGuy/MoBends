package goblinbob.mobends.api.gui.view;

import goblinbob.mobends.api.gui.ILayoutParams;

/**
 * Abstraction for a view container.
 * Holds and lays out child views.
 */
public interface IViewGroup extends IView
{
    void addView(IView child);
    void addView(IView child, ILayoutParams params);
    void addView(IView child, int index);
    void removeView(IView child);
    void removeViewAt(int index);
    void removeAllViews();
    int getChildCount();
    IView getChildAt(int index);
    IView findViewById(int id);
}
