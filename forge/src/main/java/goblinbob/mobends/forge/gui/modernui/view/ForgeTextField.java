package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.view.IMuiTextField;
import icyllis.modernui.text.InputFilter;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.widget.EditText;

import java.util.function.Consumer;

/**
 * Forge wrapper for Modern UI EditText.
 */
public class ForgeTextField extends ForgeView implements IMuiTextField
{
    private final EditText nativeEditText;
    private Consumer<String> textListener;

    public ForgeTextField(EditText nativeView)
    {
        super(nativeView);
        this.nativeEditText = nativeView;

        // Set up text change listener
        nativeEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (textListener != null)
                {
                    textListener.accept(s.toString());
                }
            }

            @Override
            public void afterTextChanged(icyllis.modernui.text.Editable s)
            {
            }
        });
    }

    @Override
    public void setText(String text)
    {
        nativeEditText.setText(text);
    }

    @Override
    public String getText()
    {
        CharSequence text = nativeEditText.getText();
        return text != null ? text.toString() : "";
    }

    @Override
    public void setHint(String hint)
    {
        nativeEditText.setHint(hint);
    }

    @Override
    public String getHint()
    {
        CharSequence hint = nativeEditText.getHint();
        return hint != null ? hint.toString() : "";
    }

    @Override
    public void setTextColor(int color)
    {
        nativeEditText.setTextColor(color);
    }

    @Override
    public void setHintTextColor(int color)
    {
        nativeEditText.setHintTextColor(color);
    }

    @Override
    public void setTextSize(float sizeSp)
    {
        nativeEditText.setTextSize(sizeSp);
    }

    @Override
    public void setOnTextChangedListener(Consumer<String> listener)
    {
        this.textListener = listener;
    }

    @Override
    public void setSingleLine(boolean singleLine)
    {
        nativeEditText.setSingleLine(singleLine);
    }

    @Override
    public void setMaxLength(int maxLength)
    {
        if (maxLength > 0)
        {
            nativeEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }
        else
        {
            nativeEditText.setFilters(new InputFilter[0]);
        }
    }

    @Override
    public void requestFocus()
    {
        nativeEditText.requestFocus();
    }

    @Override
    public void clearFocus()
    {
        nativeEditText.clearFocus();
    }
}
