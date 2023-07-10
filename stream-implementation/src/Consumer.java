public interface Consumer<T> {
    void accept(T x);

    default Consumer<T> andThen(Consumer<? super T> after){
        return (T x) -> {accept(x); after.accept(x);};
    }
}
