package resources.model;

public class RequestParameter<T> {

    T value;

    public RequestParameter() {
    }

    public RequestParameter(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
