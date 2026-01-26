package goblinbob.mobends.lib.math.matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Mat4x4dBuffered extends Mat4x4d
{

	private final FloatBuffer buffer;

	public Mat4x4dBuffered()
	{
		super();
		// Allocate a direct buffer with native byte order (required for OpenGL)
		this.buffer = ByteBuffer.allocateDirect(16 * Float.BYTES)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer();
	}
	
	public void updateBuffer()
	{
		MatrixUtils.matToGlMatrix(this, this.buffer);
	}
	
	public FloatBuffer getBuffer()
	{
		return this.buffer;
	}
	
}
