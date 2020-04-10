package utils;

public class Pair<Item1, Item2> {

    private Item1 key;
    private Item2 value;

    public Pair(Item1 key, Item2 value) {
        this.key = key;
        this.value = value;
    }

    public Item1 getKey() {
        return key;
    }

    public void setKey(Item1 key) {
        this.key = key;
    }

    public Item2 getValue() {
        return value;
    }

    public void setValue(Item2 value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}