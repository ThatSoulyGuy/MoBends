package goblinbob.mobends.standard.client.model.armor.tier;

public enum RenderTier
{
    TIER_1_TRANSFORM_INJECTION(1, "Transform Injection"),

    TIER_2_MODEL_INTERCEPTION(2, "Model Interception");

    private final int tierNumber;
    private final String displayName;

    RenderTier(int tierNumber, String displayName)
    {
        this.tierNumber = tierNumber;
        this.displayName = displayName;
    }

    public int getTierNumber()
    {
        return tierNumber;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public boolean requiresGeometrySlicing()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "Tier " + tierNumber + " (" + displayName + ")";
    }
}
