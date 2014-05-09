package cz.tomkren.observer;

public interface ChangeInformer {
    public void addListener (ChangeListener listener);
    public boolean removeListener (ChangeListener listener);
}
