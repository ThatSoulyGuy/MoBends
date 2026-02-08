package goblinbob.mobends.api.gui.modernui.view;

/**
 * Abstraction for Modern UI FrameLayout.
 * Stacks children on top of each other, typically showing one at a time.
 */
public interface IMuiFrameLayout extends IMuiViewGroup
{
    /**
     * Sets whether the frame should measure all children, even invisible ones.
     *
     * @param measureAll True to measure all children
     */
    void setMeasureAllChildren(boolean measureAll);
}
