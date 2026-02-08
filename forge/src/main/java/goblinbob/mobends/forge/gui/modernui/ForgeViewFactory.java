package goblinbob.mobends.forge.gui.modernui;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.IViewFactory;
import goblinbob.mobends.api.gui.modernui.view.*;
import goblinbob.mobends.forge.gui.modernui.view.*;
import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.*;

/**
 * Forge 1.20.1 view factory implementation.
 * Creates Modern UI 3.9.x views wrapped in our abstraction interfaces.
 * Requires a Context for view creation (Android-style API).
 */
public class ForgeViewFactory implements IViewFactory
{
    private final Context context;
    // Create a temporary view for dp() conversion
    private final View dpConverter;

    public ForgeViewFactory(Context context)
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
        return new ForgeView(new View(context));
    }

    @Override
    public IMuiButton createButton(String text)
    {
        Button button = new Button(context);
        button.setText(text);
        return new ForgeButton(button);
    }

    @Override
    public IMuiToggle createToggle(boolean initialState)
    {
        // Modern UI 3.9.x uses CheckBox instead of Switch
        CheckBox checkBox = new CheckBox(context);
        checkBox.setChecked(initialState);
        return new ForgeToggle(checkBox);
    }

    @Override
    public IMuiTextField createTextField(String hint)
    {
        EditText editText = new EditText(context);
        editText.setHint(hint);
        return new ForgeTextField(editText);
    }

    @Override
    public IMuiImageView createImageView()
    {
        ImageView imageView = new ImageView(context);
        return new ForgeImageView(imageView);
    }

    @Override
    public IMuiTextView createTextView(String text)
    {
        TextView textView = new TextView(context);
        textView.setText(text);
        return new ForgeTextView(textView);
    }

    @Override
    public IMuiLinearLayout createLinearLayout(int orientation)
    {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(orientation);
        return new ForgeLinearLayout(layout, this);
    }

    @Override
    public IMuiFrameLayout createFrameLayout()
    {
        FrameLayout layout = new FrameLayout(context);
        return new ForgeFrameLayout(layout, this);
    }

    @Override
    public IMuiScrollView createScrollView()
    {
        ScrollView scrollView = new ScrollView(context);
        return new ForgeScrollView(scrollView);
    }

    @Override
    public IMuiListView createListView()
    {
        // Modern UI uses RecyclerView for lists
        // For simplicity, we'll use a LinearLayout inside ScrollView approach
        return new ForgeListView(context);
    }

    @Override
    public ILayoutParams createLayoutParams(int width, int height)
    {
        // Create LinearLayout.LayoutParams by default (most common use case)
        return new ForgeLayoutParams(
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
        return new ForgeLayoutParams(params, this);
    }

    @Override
    public ILayoutParams createFrameLayoutParams(int width, int height, int gravity)
    {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                convertDimension(width),
                convertDimension(height),
                gravity
        );
        return new ForgeLayoutParams(params, this);
    }
}
