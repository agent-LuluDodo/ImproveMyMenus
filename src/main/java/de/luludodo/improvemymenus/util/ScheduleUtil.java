package de.luludodo.improvemymenus.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.Util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.LongConsumer;

public class ScheduleUtil {
    private record Entry(long time, LongConsumer callback) {}

    private static final Map<Object, List<Entry>> ENTRIES = new Object2ObjectOpenHashMap<>();

    public static void init() {
        ClientTickEvents.START_CLIENT_TICK.register(_ -> tick());
    }

    public static void set(Object id, long time, LongConsumer callback) {
        ENTRIES.compute(id, (_, value) -> {
            if (value == null) {
                value = new ObjectArrayList<>();
            } else {
                value.clear();
            }
            value.add(new Entry(time, callback));
            return value;
        });
    }

    public static void add(Object id, long time, LongConsumer callback) {
        ENTRIES.computeIfAbsent(id, _ -> new ObjectArrayList<>()).add(new Entry(time, callback));
    }

    public static void clear(Object id) {
        ENTRIES.remove(id);
    }

    public static void tick() {
        long curTime = Util.getMillis();
        Iterator<Map.Entry<Object, List<Entry>>> it = ENTRIES.entrySet().iterator();
        while (it.hasNext()) {
            List<Entry> value = it.next().getValue();
            Iterator<Entry> entries = value.iterator();
            while (entries.hasNext()) {
                Entry entry = entries.next();
                if (entry.time <= curTime) {
                    entries.remove();
                    entry.callback.accept(entry.time);
                }
            }
            if (value.isEmpty())
                it.remove();
        }
    }
}
