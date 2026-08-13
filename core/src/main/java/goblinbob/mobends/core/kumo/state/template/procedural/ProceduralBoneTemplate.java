package goblinbob.mobends.core.kumo.state.template.procedural;

public class ProceduralBoneTemplate
{
    public String rotationX;

    public String rotationY;

    public String rotationZ;

    public String offsetX;

    public String offsetY;

    public String offsetZ;

    public boolean hasRotation()
    {
        return rotationX != null || rotationY != null || rotationZ != null;
    }

    public boolean hasOffset()
    {
        return offsetX != null || offsetY != null || offsetZ != null;
    }

    public boolean hasAnyExpression()
    {
        return hasRotation() || hasOffset();
    }
}
