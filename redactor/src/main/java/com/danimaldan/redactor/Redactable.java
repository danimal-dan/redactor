package com.danimaldan.redactor;

public class Redactable<T> {
    private T value;
    private boolean redacted = false;

    public Redactable(T value) {
        this.value = value;
    }

    public Redactable(T value, boolean redacted) {
        this.value = value;
        this.redacted = redacted;
    }

    public static <T> Redactable<T> of(T value) {
        return new Redactable<>(value);
    }

    public void redact() {
        value = null;
        redacted = true;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isRedacted() {
        return redacted;
    }
}
