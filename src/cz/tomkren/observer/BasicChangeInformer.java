package cz.tomkren.observer;

import java.util.LinkedList;
import java.util.List;

public class BasicChangeInformer implements ChangeInformer {
    private List<ChangeListener> listeners;

    public BasicChangeInformer() {
        listeners = new LinkedList<ChangeListener>();
    }

    public void addListener (ChangeListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener (ChangeListener listener) {
        return listeners.remove(listener);
    }

    public void informListeners() {
        for (ChangeListener listener : listeners) {
            listener.onChange();
        }
    }
}
