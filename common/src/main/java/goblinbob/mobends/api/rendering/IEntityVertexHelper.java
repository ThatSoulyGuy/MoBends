package goblinbob.mobends.api.rendering;

public interface IEntityVertexHelper
{
    void emitVertex(Object vertexConsumer, float x, float y, float z,
                    int color, float u, float v,
                    int overlay, int light,
                    float normalX, float normalY, float normalZ);

    class Holder
    {
        private static IEntityVertexHelper helper;

        public static void setHelper(IEntityVertexHelper helper)
        {
            Holder.helper = helper;
        }

        public static IEntityVertexHelper getHelper()
        {
            return helper;
        }
    }
}
