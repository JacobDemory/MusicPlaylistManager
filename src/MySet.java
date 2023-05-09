import java.util.*;

public class MySet extends AbstractSet {
    private HashMap<Object, Object> mapObject = null;
    private static final Object temp = new Object();

    public MySet() {
        mapObject = new HashMap<>();
    }

    public boolean add(Object object) {
        return mapObject.put(object, temp)==null;
    }

    @Override
    public Iterator iterator() {
        return mapObject.keySet().iterator();
    }

    @Override
    public int size() {
        return mapObject.size();
    }

}