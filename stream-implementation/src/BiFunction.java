public interface BiFunction<U,V,R> {
    R apply(U u, V v);
    default <S> BiFunction<U,V,S> andThen(Function<? super R, ? extends S> f) {
        return(U u, V v) -> f.apply(apply(u,v));
    }
}