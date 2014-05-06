package cz.tomkren.observer;

import cz.tomkren.trhy.Log;

import java.util.*;

public class ChangeInformer implements ChangeInformerService {
    private List<ChangeListener> listeners;

    public ChangeInformer () {
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
