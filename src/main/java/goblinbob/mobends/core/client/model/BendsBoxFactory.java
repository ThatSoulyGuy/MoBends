package goblinbob.mobends.core.client.model;

import goblinbob.mobends.lib.math.vector.Vec3f;

public class BendsBoxFactory
{
    public BendsModelPart target;
    public final Vec3f min;
    public final Vec3f max;
    public final BoxFactory.TextureFace[] faces;
    public int uvWidth;
    public int uvHeight;
    public int uvLength;
    public boolean mirrored;
    public byte faceVisibilityFlag;

    int textureU, textureV;
    boolean textureUVSet = false;
    float textureWidth = 64.0F;
    float textureHeight = 32.0F;

    public BendsBoxFactory(float x, float y, float z, int dx, int dy, int dz, float delta)
    {
        this.min = new Vec3f(x - delta, y - delta, z - delta);
        this.max = new Vec3f(x + dx + delta, y + dy + delta, z + dz + delta);
        this.faces = new BoxFactory.TextureFace[6];
        this.uvWidth = dx;
        this.uvHeight = dy;
        this.uvLength = dz;
        this.mirrored = false;
        this.faceVisibilityFlag = 0b111111;
        this.textureU = 0;
        this.textureV = 0;
    }

    BendsBoxFactory setTarget(BendsModelPart target)
    {
        this.target = target;
        this.textureWidth = target.getTextureWidth();
        this.textureHeight = target.getTextureHeight();
        this.mirrored = target.mirror;

        if (!this.textureUVSet)
        {
            this.textureU = target.getTextureOffsetX();
            this.textureV = target.getTextureOffsetY();
            this.generateTextureFaces();
        }

        return this;
    }

    public BendsBoxFactory setMinMax(float x0, float y0, float z0, float x1, float y1, float z1)
    {
        this.min.set(x0, y0, z0);
        this.max.set(x1, y1, z1);
        return this;
    }

    public BendsBoxFactory setPosSize(float x, float y, float z, float dx, float dy, float dz)
    {
        this.min.set(x, y, z);
        this.max.set(x + dx, y + dy, z + dz);
        return this;
    }

    public BendsBoxFactory inflate(float dx, float dy, float dz)
    {
        this.min.add(-dx, -dy, -dz);
        this.max.add(dx, dy, dz);
        return this;
    }

    public BendsBoxFactory setWidth(float width)
    {
        this.max.x = this.min.x + width;
        return this;
    }

    public BendsBoxFactory setHeight(float height)
    {
        this.max.y = this.min.y + height;
        return this;
    }

    public BendsBoxFactory setLength(float length)
    {
        this.max.z = this.min.z + length;
        return this;
    }

    public BendsBoxFactory resize(float dx, float dy, float dz)
    {
        this.max.set(this.min.x + dx, this.min.y + dy, this.min.z + dz);
        return this;
    }

    public BendsBoxFactory withUVs(int u, int v)
    {
        this.textureU = u;
        this.textureV = v;
        this.textureUVSet = true;
        this.generateTextureFaces();

        return this;
    }

    public BendsBoxFactory hideFace(BoxSide face)
    {
        byte mask = 1;
        mask <<= face.faceIndex;
        this.faceVisibilityFlag &= (~mask);
        return this;
    }

    public BendsBoxFactory showFace(BoxSide face)
    {
        byte mask = 1;
        mask <<= face.faceIndex;
        this.faceVisibilityFlag |= mask;
        return this;
    }

    public BendsBoxFactory mirror()
    {
        this.mirrored = true;
        return this;
    }

    public BendsBoxFactory offsetTextureQuad(BoxSide face, float x, float y)
    {
        if (!this.textureUVSet)
        {
            this.textureUVSet = true;
            this.generateTextureFaces();
        }

        this.faces[face.faceIndex].uPos += x;
        this.faces[face.faceIndex].vPos += y;
        return this;
    }

    public BendsBoxFactory rotateTextureQuad(BoxSide face, FaceRotation rotation)
    {
        if (!this.textureUVSet)
        {
            this.textureUVSet = true;
            this.generateTextureFaces();
        }

        this.faces[face.faceIndex].faceRotation = rotation;

        return this;
    }

    public BendsBoxFactory offset(float x, float y, float z)
    {
        this.min.add(x, y, z);
        this.max.add(x, y, z);
        return this;
    }

    public BendsCube create()
    {
        BendsCube cube = new BendsCube(this.min.x, this.min.y, this.min.z,
                                       this.max.x, this.max.y, this.max.z,
                                       this.faces, this.faceVisibilityFlag, this.mirrored,
                                       this.textureWidth, this.textureHeight);
        if (this.target != null)
            this.target.addCube(cube);
        return cube;
    }

    private void generateTextureFaces()
    {
        int u = this.textureU;
        int v = this.textureV;

        this.faces[0] = new BoxFactory.TextureFace(u + uvLength + uvWidth, v + uvLength, uvLength, uvHeight);
        this.faces[1] = new BoxFactory.TextureFace(u, v + uvLength, uvLength, uvHeight);
        this.faces[2] = new BoxFactory.TextureFace(u + uvLength, v, uvWidth, uvLength);
        this.faces[3] = new BoxFactory.TextureFace(u + uvLength + uvWidth, v + uvLength, uvWidth, -uvLength);
        this.faces[4] = new BoxFactory.TextureFace(u + uvLength, v + uvLength, uvWidth, uvHeight);
        this.faces[5] = new BoxFactory.TextureFace(u + uvLength + uvWidth + uvLength, v + uvLength, uvWidth, uvHeight);
    }
}
