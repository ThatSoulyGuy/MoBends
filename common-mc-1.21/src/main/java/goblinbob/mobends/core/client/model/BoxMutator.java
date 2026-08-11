package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.client.model.BoxFactory.TextureFace;

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

    public static BoxMutator createFrom(ModelPart modelPart, MutatedBox box)
    {
        if (box == null)
        {
            return null;
        }

        int width = (int)(box.posX2 - box.posX1);
        int height = (int)(box.posY2 - box.posY1);
        int length = (int)(box.posZ2 - box.posZ1);

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

    public void offsetBy(float offsetX, float offsetY, float offsetZ)
    {
        this.factory.offset(offsetX, offsetY, offsetZ);
    }

    public BoxFactory sliceFromBottom(float sliceY)
    {
        final float height = this.factory.max.y - this.factory.min.y;

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

            final BoxFactory sliced = new BoxFactory(factory.min.x, sliceY, factory.min.z, factory.max.x, factory.max.y, factory.max.z, newBoxFaces);
            sliced.hideFace(BoxSide.TOP);

            factory.max.setY(sliceY);
            factory.hideFace(BoxSide.BOTTOM);

            return sliced;
        }

        return null;
    }
}
