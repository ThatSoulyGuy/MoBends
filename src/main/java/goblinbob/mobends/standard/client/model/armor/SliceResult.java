package goblinbob.mobends.standard.client.model.armor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SliceResult
{
    private final List<CapturedVertex> upperVertices;
    private final List<CapturedVertex> lowerVertices;

    private SliceResult(List<CapturedVertex> upperVertices, List<CapturedVertex> lowerVertices)
    {
        this.upperVertices = Collections.unmodifiableList(new ArrayList<>(upperVertices));
        this.lowerVertices = Collections.unmodifiableList(new ArrayList<>(lowerVertices));
    }

    public static SliceResult sliced(List<CapturedVertex> upperVertices, List<CapturedVertex> lowerVertices)
    {
        return new SliceResult(upperVertices, lowerVertices);
    }

    public static SliceResult entirelyAbove(List<CapturedVertex> vertices)
    {
        return new SliceResult(vertices, Collections.emptyList());
    }

    public static SliceResult entirelyBelow(List<CapturedVertex> vertices)
    {
        return new SliceResult(Collections.emptyList(), vertices);
    }

    public static SliceResult empty()
    {
        return new SliceResult(Collections.emptyList(), Collections.emptyList());
    }

    public List<CapturedVertex> getUpperVertices()
    {
        return upperVertices;
    }

    public List<CapturedVertex> getLowerVertices()
    {
        return lowerVertices;
    }
}
