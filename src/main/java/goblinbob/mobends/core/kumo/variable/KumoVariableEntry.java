package goblinbob.mobends.core.kumo.variable;

import net.minecraft.client.resources.language.I18n;

public class KumoVariableEntry
{

    private IKumoVariable variable;
    private String key;

    public KumoVariableEntry(IKumoVariable variable, String key)
    {
        this.variable = variable;
        this.key = key;
    }

    public IKumoVariable getVariable()
    {
        return variable;
    }

    public String getKey()
    {
        return key;
    }

    public String getLocalizedName()
    {
        return I18n.get(String.format("mobends.variable.%s", this.key));
    }

}
