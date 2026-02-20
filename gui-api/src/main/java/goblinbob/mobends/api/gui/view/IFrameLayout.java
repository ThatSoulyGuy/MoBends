package goblinbob.mobends.api.gui.view;

/**
 * Abstraction for a frame layout.
 * Stacks children on top of each other.
 */
public interface IFrameLayout extends IViewGroup
{
    void setMeasureAllChildren(boolean measureAll);
}
