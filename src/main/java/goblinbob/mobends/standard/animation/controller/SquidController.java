package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.lib.util.GUtil;
import goblinbob.mobends.standard.data.SquidData;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class SquidController implements IAnimationController<SquidData>
{

    @Override
    @Nullable
    public void perform(SquidData data)
    {
        Squid squid = data.getEntity();

        float squidRotation = squid.oldTentacleMovement + (squid.tentacleMovement - squid.oldTentacleMovement) * DataUpdateHandler.partialTicks + 1.1F;
        float f = squidRotation / GUtil.PI;
        f = Math.max(0.0F, f);
        float baseTentacleAngle = 0.0F;
        if (squid.oldTentacleMovement < GUtil.PI)
        {
            baseTentacleAngle = Mth.sin(f * f * (float) Math.PI) * 60.0f;
        }

        for (int i = 0; i < data.squidTentacles.length; ++i)
        {

            double d0 = (double) i * -360.0D / (double) data.squidTentacles.length + 90.0D;
            data.squidTentacles[i][0].rotation.setSmoothness(0.1F).orientX(baseTentacleAngle).rotateY((float) d0);

            float f2 = squidRotation / (GUtil.PI * 2);
            f2 = Math.max(0.0F, f2);
            for (int j = 1; j < SquidData.TENTACLE_SECTIONS; ++j)
            {
                float tentacleAngle = 0;
                if (squid.tentacleMovement < GUtil.PI)
                {
                    tentacleAngle = Mth.sin(f2 * GUtil.PI * 2 + j * 0.1F) * 10.0F;
                }
                data.squidTentacles[i][j].rotation.setSmoothness(0.1F).orientX(-tentacleAngle);
            }
        }
    }

}
