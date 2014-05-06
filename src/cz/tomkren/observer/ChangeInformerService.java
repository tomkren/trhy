package cz.tomkren.observer;

public interface ChangeInformerService {
    public void addListener (ChangeListener listener);
    public boolean removeListener (ChangeListener listener);
}
