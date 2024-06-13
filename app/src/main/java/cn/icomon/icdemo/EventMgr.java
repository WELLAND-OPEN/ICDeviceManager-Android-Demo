package cn.icomon.icdemo;

import java.util.HashMap;

public class EventMgr {

    public static HashMap<String, Event> events = new HashMap<>();

    public interface Event {
        public void onCallBack(Object obj);
    }

    public static void addEvent(String name, Event event)
    {
        events.put(name, event);
    }

    public static void post(String name, Object obj)
    {
        if (events.containsKey(name)) {
            events.get(name).onCallBack(obj);
        }
    }

}
