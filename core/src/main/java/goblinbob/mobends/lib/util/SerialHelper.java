package goblinbob.mobends.lib.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SerialHelper
{
    public static String readChar(DataInputStream stream, int length) throws IOException
    {
        byte[] buffer = new byte[length];
        for (int i = 0; i < length; ++i)
        {
            buffer[i] = stream.readByte();
        }

        return new String(buffer, StandardCharsets.UTF_8);
    }
}
