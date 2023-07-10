
public interface Function<T,R> {
    R apply(T arg);
    static <S> Function<S,S> identity(){
        return (S x) -> x;
    }
    default <V> Function<T,V> andThen(Function<? super R,? extends V> after){
        return (T x) -> after.apply(apply(x));
    }

    default <V> Function<V,R> compose(Function<? super V, ? extends T> before){
        return (V x) -> apply(before.apply(x));
    }
}
