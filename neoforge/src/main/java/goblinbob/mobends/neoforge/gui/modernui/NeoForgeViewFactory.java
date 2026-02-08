package goblinbob.mobends.neoforge.gui.modernui;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.IViewFactory;
import goblinbob.mobends.api.gui.modernui.view.*;
import goblinbob.mobends.neoforge.gui.modernui.view.*;
import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.*;

/**
 * NeoForge 1.21.1 view factory implementation.
 * Creates Modern UI 3.11.x views wrapped in our abstraction interfaces.
 * Requires Context for view construction.
 */
public class NeoForgeViewFactory implements IViewFactory
{
    private final Context context;
    // Create a temporary view for dp() conversion
    private final View dpConverter;

    public NeoForgeViewFactory(Context context)
    {
        this.context = context;
        this.dpConverter = new View(context);
    }

    @Override
    public int dp(int dp)
    {
        // Convert density-independent pixels to actual pixels
        return dpConverter.dp(dp);
    }

    /**
     * Converts a dimension value, handling MATCH_PARENT/WRAP_CONTENT specially.
     */
    private int convertDimension(int value)
    {
        if (value == ILayoutParams.MATCH_PARENT || value == ILayoutParams.WRAP_CONTENT)
        {
            return value;
        }
        return dp(value);
    }

    @Override
    public IMuiView createView()
    {
        return new NeoForgeView(new icyllis.modernui.view.View(context));
    }

    @Override
    public IMuiButton createButton(String text)
    {
        Button button = new Button(context);
        button.setText(text);
        return new NeoForgeButton(button);
    }

    @Override
    public IMuiToggle createToggle(boolean initialState)
    {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setChecked(initialState);
        return new NeoForgeToggle(checkBox);
    }

    @Override
    public IMuiTextField createTextField(String hint)
    {
        EditText editText = new EditText(context);
        editText.setHint(hint);
        return new NeoForgeTextField(editText);
    }

    @Override
    public IMuiImageView createImageView()
    {
        ImageView imageView = new ImageView(context);
        return new NeoForgeImageView(imageView);
    }

    @Override
    public IMuiTextView createTextView(String text)
    {
        TextView textView = new TextView(context);
        textView.setText(text);
        return new NeoForgeTextView(textView);
    }

    @Override
    public IMuiLinearLayout createLinearLayout(int orientation)
    {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(orientation);
        return new NeoForgeLinearLayout(layout, this);
    }

    @Override
    public IMuiFrameLayout createFrameLayout()
    {
        FrameLayout layout = new FrameLayout(context);
        return new NeoForgeFrameLayout(layout, this);
    }

    @Override
    public IMuiScrollView createScrollView()
    {
        ScrollView scrollView = new ScrollView(context);
        return new NeoForgeScrollView(scrollView);
    }

    @Override
    public IMuiListView createListView()
    {
        return new NeoForgeListView(context);
    }

    @Override
    public ILayoutParams createLayoutParams(int width, int height)
    {
        // Create LinearLayout.LayoutParams by default (most common use case)
        return new NeoForgeLayoutParams(
                new LinearLayout.LayoutParams(convertDimension(width), convertDimension(height)),
                this
        );
    }

    @Override
    public ILayoutParams createLayoutParams(int width, int height, float weight)
    {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                convertDimension(width),
                convertDimension(height),
                weight
        );
        return new NeoForgeLayoutParams(params, this);
    }

    @Override
    public ILayoutParams createFrameLayoutParams(int width, int height, int gravity)
    {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                convertDimension(width),
                convertDimension(height),
                gravity
        );
        return new NeoForgeLayoutParams(params, this);
    }
}
