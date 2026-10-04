package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.data.IEntityDataFactory;
import net.minecraft.world.entity.LivingEntity;

public class LunarianMutator<E extends LivingEntity> extends VillagerMutator<E>
{
    private static final int TEXTURE_SIZE = 128;

    private static final float ARM_X = 5.0F;
    private static final float ARM_INNER_X = 4.0F + 0.7F - 0.05F;
    private static final int FOREARM_LENGTH = 4;
    private static final int HAND_EXPOSED = 2;

    private static final int ROBE_WIDTH = 8;
    private static final int ROBE_DEPTH = 6;
    private static final int ROBE_TORSO_HEIGHT = 12;
    private static final int ROBE_HEIGHT = 19;
    private static final int SKIRT_OVERLAP = 3;
    private static final float SKIRT_TUCK = 0.05F;

    public LunarianMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    protected int textureWidth()
    {
        return TEXTURE_SIZE;
    }

    @Override
    protected int textureHeight()
    {
        return TEXTURE_SIZE;
    }

    @Override
    protected int handTexU()
    {
        return 2;
    }

    @Override
    protected int handTexV()
    {
        return 73;
    }

    private static float lunarianArmBoxX(boolean left)
    {
        return left ? ARM_INNER_X - ARM_X : ARM_X - ARM_INNER_X - 4;
    }

    private BendsModelPart part(int u, int v)
    {
        return new BendsModelPart(u, v).setTextureSize(TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected BendsModelPart buildBody(float scaleFactor)
    {
        final BendsModelPart body = part(100, 0).setPosition(0.0F, 12.0F, 0.0F).setMirror(true);

        body.addCube(-4.0F, -12.0F, -3.0F, 8, 12, 6, scaleFactor);
        body.setTextureOffset(0, 36);
        body.addCube(-4.0F, -12.0F, -3.0F, ROBE_WIDTH, ROBE_TORSO_HEIGHT, ROBE_DEPTH, scaleFactor + 0.5F);
        body.setTextureOffset(28, 36);
        body.addCube(-4.0F, -12.0F, -3.0F, ROBE_WIDTH, ROBE_TORSO_HEIGHT, ROBE_DEPTH, scaleFactor + 0.7F);

        body.addChild(buildSkirt(scaleFactor));

        return body;
    }

    @Override
    protected BendsModelPart buildSkirt(float scaleFactor)
    {
        final int skirtTexV = 36 + ROBE_TORSO_HEIGHT - SKIRT_OVERLAP;
        final int skirtHeight = ROBE_HEIGHT - ROBE_TORSO_HEIGHT + SKIRT_OVERLAP;

        final BendsModelPart skirt = part(0, skirtTexV).setPosition(0.0F, 0.0F, 0.0F).setMirror(true);

        skirt.developBox(-4.0F, -SKIRT_OVERLAP, -3.0F, ROBE_WIDTH, skirtHeight, ROBE_DEPTH,
                        scaleFactor + 0.5F - SKIRT_TUCK)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -(ROBE_TORSO_HEIGHT - SKIRT_OVERLAP))
                .create();
        skirt.setTextureOffset(28, skirtTexV);
        skirt.developBox(-4.0F, -SKIRT_OVERLAP, -3.0F, ROBE_WIDTH, skirtHeight, ROBE_DEPTH,
                        scaleFactor + 0.7F - SKIRT_TUCK)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -(ROBE_TORSO_HEIGHT - SKIRT_OVERLAP))
                .create();

        lastSkirt = skirt;

        return skirt;
    }

    @Override
    protected BendsModelPart buildHead(float scaleFactor, boolean outer)
    {
        final BendsModelPart head = part(0, 19).setPosition(0.0F, -12.0F, 0.0F).setMirror(true);

        head.addCube(-4.0F, -9.0F, -4.0F, 8, 9, 8, scaleFactor);
        head.setTextureOffset(0, 0);
        head.addCube(-4.5F, -18.0F, -4.5F, 9, 10, 9, scaleFactor);
        head.setTextureOffset(80, 18);
        head.addCube(-8.0F, -14.0F, -8.0F, 16, 0, 16, scaleFactor);
        head.setTextureOffset(0, 20);
        head.addCube(-1.0F, -3.0F, -6.0F, 2, 4, 2, scaleFactor);

        head.setMirror(false);
        head.setTextureOffset(36, 0);
        head.addCube(-4.5F, -18.0F, -4.5F, 9, 10, 9, scaleFactor + 0.5F);
        head.setTextureOffset(32, 19);
        head.addCube(-4.0F, -9.0F, -4.0F, 8, 9, 8, scaleFactor + 0.5F);

        return head;
    }

    @Override
    protected BendsModelPart buildArm(float scaleFactor, boolean left)
    {
        final BendsModelPart arm = part(0, 61).setPosition(left ? ARM_X : -ARM_X, -10.0F, 0.0F).setMirror(left);

        arm.developBox(lunarianArmBoxX(left), -2.0F, -2.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        arm.setTextureOffset(16, 61);
        arm.developBox(lunarianArmBoxX(left), -2.0F, -2.0F, 4, 6, 4, scaleFactor + 0.5F)
                .hideFace(BoxSide.BOTTOM)
                .create();

        return arm;
    }

    @Override
    protected BendsModelPart buildForeArm(float scaleFactor, boolean left, boolean outer)
    {
        final BendsModelPart foreArm = part(0, 65).setPosition(0.0F, 4.0F, 2.0F).setMirror(left);

        foreArm.developBox(lunarianArmBoxX(left), 0.0F, -4.0F, 4, FOREARM_LENGTH, 4, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -4F)
                .create();
        foreArm.setTextureOffset(16, 65);
        foreArm.developBox(lunarianArmBoxX(left), 0.0F, -4.0F, 4, FOREARM_LENGTH, 4, scaleFactor + 0.5F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -4F)
                .create();

        final BendsModelPart hand = part(handTexU(), handTexV()).setPosition(0.0F, 0.0F, 0.0F).setMirror(left);
        hand.developBox(lunarianArmBoxX(left), FOREARM_LENGTH, -4.0F, 4, HAND_EXPOSED, 4, scaleFactor)
                .offsetTextureQuad(BoxSide.LEFT, -4, 0)
                .offsetTextureQuad(BoxSide.RIGHT, 4, 0)
                .offsetTextureQuad(BoxSide.BACK, -8, 0)
                .offsetTextureQuad(BoxSide.BOTTOM, -4, 0)
                .create();
        foreArm.addChild(hand);

        if (outer)
        {
            lastOuterHand = hand;
        }
        else
        {
            lastWristTrim = new BendsModelPart();
        }

        return foreArm;
    }

    @Override
    protected BendsModelPart buildLeg(float scaleFactor, boolean left)
    {
        final BendsModelPart leg = part(0, 81).setPosition(0.0F, 12.0F, 0.0F).setMirror(left);
        final float boxX = left ? 0.1F : -4.1F;

        leg.developBox(boxX, 0.0F, -2.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        leg.setTextureOffset(16, 81);
        leg.developBox(boxX, 0.0F, -2.0F, 4, 6, 4, scaleFactor + 0.5F)
                .hideFace(BoxSide.BOTTOM)
                .create();

        return leg;
    }

    @Override
    protected BendsModelPart buildForeLeg(float scaleFactor, boolean left)
    {
        final BendsModelPart foreLeg = part(0, 87).setPosition(0.0F, 6.0F, -2.0F).setMirror(left);
        final float boxX = left ? 0.1F : -4.1F;

        foreLeg.developBox(boxX, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .hideFace(BoxSide.TOP)
                .create();
        foreLeg.setTextureOffset(16, 87);
        foreLeg.developBox(boxX, 0.0F, 0.0F, 4, 6, 4, scaleFactor + 0.5F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .hideFace(BoxSide.TOP)
                .create();

        return foreLeg;
    }
}
