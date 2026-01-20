package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.client.model.BoxFactory.TextureFace;

/**
 * BoxMutator for mutating box geometry in models.
 * Updated for Minecraft 1.20.1 - no longer depends on ModelBase/ModelRenderer/ModelBox.
 *
 * In 1.20.1, the model system is completely different, so this class now works
 * with our custom ModelPart and BoxFactory classes instead.
 */
public class BoxMutator
{
    protected ModelPart targetPart;
    protected BoxFactory factory;

    protected int textureOffsetX;
    protected int textureOffsetY;

    public BoxMutator(ModelPart targetPart, BoxFactory factory, int textureOffsetX, int textureOffsetY)
    {
        this.targetPart = targetPart;
        this.factory = factory;
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
    }

    /**
     * Creates a BoxMutator from a MutatedBox.
     * This is the 1.20.1 version that works with our custom model classes.
     */
    public static BoxMutator createFrom(ModelPart modelPart, MutatedBox box)
    {
        if (box == null)
        {
            return null;
        }

        // Calculate dimensions from box bounds
        int width = (int)(box.posX2 - box.posX1);
        int height = (int)(box.posY2 - box.posY1);
        int length = (int)(box.posZ2 - box.posZ1);

        // Get texture offset from the model part
        int texU = modelPart != null ? modelPart.getTextureOffsetX() : 0;
        int texV = modelPart != null ? modelPart.getTextureOffsetY() : 0;

        BoxFactory target = new BoxFactory(
            box.posX1, box.posY1, box.posZ1,
            width, height, length, 0.0F
        );

        if (modelPart != null)
        {
            target.setTarget(modelPart);
        }

        return new BoxMutator(modelPart, target, texU, texV);
    }

    public BoxFactory getFactory()
    {
        return this.factory;
    }

    public int getTextureOffsetX()
    {
        return this.textureOffsetX;
    }

    public int getTextureOffsetY()
    {
        return this.textureOffsetY;
    }

    /**
     * Move the box to a new location.
     */
    public void offsetBy(float offsetX, float offsetY, float offsetZ)
    {
        this.factory.offset(offsetX, offsetY, offsetZ);
    }

    /**
     * Slice the box from the bottom at the specified Y position.
     * @param sliceY Position of the slice relative to the parent part.
     * @return The lower portion of the box, or null if no slice was made.
     */
    public BoxFactory sliceFromBottom(float sliceY)
    {
        final float height = this.factory.max.y - this.factory.min.y;

        // If slicing is necessary (if the cut plane intersects the box)
        if (sliceY > this.factory.min.y && sliceY < this.factory.max.y)
        {
            final float newHeight = sliceY - this.factory.min.y;

            final TextureFace[] newBoxFaces = new TextureFace[6];
            final BoxSide[] faces = { BoxSide.BACK, BoxSide.FRONT, BoxSide.LEFT, BoxSide.RIGHT };
            for (BoxSide faceEnum : faces)
            {
                final float textureScale = newHeight / height;

                final TextureFace face = this.factory.faces[faceEnum.faceIndex];
                int vSizeSlice = (int) (face.vSize * textureScale);

                newBoxFaces[faceEnum.faceIndex] = new TextureFace(face.uPos, face.vPos + vSizeSlice, face.uSize, face.vSize - vSizeSlice);
                face.vSize = vSizeSlice;
            }

            newBoxFaces[BoxSide.TOP.faceIndex] = new TextureFace(this.factory.faces[BoxSide.TOP.faceIndex]);
            newBoxFaces[BoxSide.BOTTOM.faceIndex] = new TextureFace(this.factory.faces[BoxSide.BOTTOM.faceIndex]);

            // Create the sliced box
            final BoxFactory sliced = new BoxFactory(factory.min.x, sliceY, factory.min.z, factory.max.x, factory.max.y, factory.max.z, newBoxFaces);
            sliced.hideFace(BoxSide.TOP);

            // Shorten the original part
            factory.max.setY(sliceY);
            factory.hideFace(BoxSide.BOTTOM);

            return sliced;
        }

        // Nothing was cut
        return null;
    }
}
