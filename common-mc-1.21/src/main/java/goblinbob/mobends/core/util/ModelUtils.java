package goblinbob.mobends.core.util;

import goblinbob.mobends.core.client.model.BoxFactory;
import goblinbob.mobends.core.client.model.FaceRotation;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.client.model.MutatedBox;
import goblinbob.mobends.lib.math.physics.AABBox;
import goblinbob.mobends.lib.math.vector.Vec3f;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Utility methods for working with models.
 * Updated for Minecraft 1.20.1 - removed dependencies on ModelRenderer, ModelBox, etc.
 */
public class ModelUtils
{
    /**
     * Get the bounding box for a MoBends ModelPart
     */
    public static AABBox getBounds(ModelPart modelPart)
    {
        return getBounds(modelPart, new Vec3f(0, 0, 0), new AABBox(0, 0, 0, 0, 0, 0));
    }

    /**
     * Get the bounding box for a MoBends ModelPart with position offset
     */
    public static AABBox getBounds(ModelPart modelPart, Vec3f position, AABBox oldBounds)
    {
        double minX = oldBounds.min.getX();
        double minY = oldBounds.min.getY();
        double minZ = oldBounds.min.getZ();
        double maxX = oldBounds.max.getX();
        double maxY = oldBounds.max.getY();
        double maxZ = oldBounds.max.getZ();

        float x = modelPart.position.x + position.x;
        float y = modelPart.position.y + position.y;
        float z = modelPart.position.z + position.z;

        // Check bounds from mutated boxes in the model part
        MutatedBox box = null;
        try {
            box = modelPart.getBox(0);
        } catch (IndexOutOfBoundsException e) {
            // No boxes in this part
        }

        if (box != null)
        {
            if (x + box.posX1 < minX) minX = x + box.posX1;
            if (y + box.posY1 < minY) minY = y + box.posY1;
            if (z + box.posZ1 < minZ) minZ = z + box.posZ1;
            if (x + box.posX2 > maxX) maxX = x + box.posX2;
            if (y + box.posY2 > maxY) maxY = y + box.posY2;
            if (z + box.posZ2 > maxZ) maxZ = z + box.posZ2;
        }

        return new AABBox((float)minX, (float)minY, (float)minZ, (float)maxX, (float)maxY, (float)maxZ);
    }

    /**
     * This method iterates through all parts looking for the parent of the specified part. It then runs itself with
     * that parent as the next part, and it does that until the part is not null.
     */
    public static IModelPart getRootParent(final IModelPart partIn, final Collection<IModelPart> partsIn)
    {
        for (IModelPart possibleParent : partsIn)
        {
            if (possibleParent != null && possibleParent.getParent() == partIn)
            {
                IModelPart nextParent = getRootParent(possibleParent, partsIn);
                if (nextParent != null)
                    return nextParent;
                else
                    return possibleParent;
            }
        }
        return null;
    }

    public static Collection<IModelPart> getParentsList(IModelPart partIn, Collection<IModelPart> possibleParents, Collection<IModelPart> parentsList)
    {
        IModelPart parent = partIn.getParent();
        if (parent != null && possibleParents.contains(parent))
        {
            parentsList.add(parent);
            getParentsList(parent, possibleParents, parentsList);
        }

        return parentsList;
    }

    public static Collection<IModelPart> getParentsList(IModelPart partIn, Collection<IModelPart> possibleParents)
    {
        return getParentsList(partIn, possibleParents, new LinkedList<>());
    }

    public static Vec3f getGlobalOrigin(IModelPart partIn, Collection<IModelPart> possibleParents)
    {
        Vec3f origin = new Vec3f(partIn.getPosition());

        Collection<IModelPart> parentsList = getParentsList(partIn, possibleParents);
        for (IModelPart parent : parentsList)
        {
            origin.x += parent.getPosition().getX();
            origin.y += parent.getPosition().getY();
            origin.z += parent.getPosition().getZ();
        }

        return origin;
    }

    /**
     * Create a MutatedQuad from vertices and a texture face definition.
     * This is the 1.20.1 version that creates our custom quad type.
     */
    public static MutatedBox.MutatedQuad createQuad(MutatedBox.MutatedVertex[] positions, BoxFactory.TextureFace face, float textureWidth, float textureHeight)
    {
        int uSize = face.uSize;
        int vSize = face.vSize;

        if (face.faceRotation == FaceRotation.CLOCKWISE || face.faceRotation == FaceRotation.COUNTER_CLOCKWISE)
        {
            uSize = face.vSize;
            vSize = face.uSize;
        }

        MutatedBox.MutatedQuad quad = new MutatedBox.MutatedQuad(positions, face.uPos, face.vPos, face.uPos + uSize, face.vPos + vSize, textureWidth, textureHeight);

        applyFaceRotation(quad, face.faceRotation);

        return quad;
    }

    public static void applyFaceRotation(MutatedBox.MutatedQuad quad, FaceRotation rotation)
    {
        if (rotation == FaceRotation.IDENTITY)
            return;

        float[] uCoords = new float[] {
            quad.vertices[0].u,
            quad.vertices[1].u,
            quad.vertices[2].u,
            quad.vertices[3].u,
        };

        float[] vCoords = new float[] {
            quad.vertices[0].v,
            quad.vertices[1].v,
            quad.vertices[2].v,
            quad.vertices[3].v,
        };

        int offset = 2;
        if (rotation == FaceRotation.CLOCKWISE)
            offset = 3;
        else if (rotation == FaceRotation.COUNTER_CLOCKWISE)
            offset = 1;

        for (int i = 0; i < 4; ++i)
        {
            quad.vertices[i].u = uCoords[(i + offset) % 4];
            quad.vertices[i].v = vCoords[(i + offset) % 4];
        }
    }
}
