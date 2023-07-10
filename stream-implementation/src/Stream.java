import java.util.NoSuchElementException;

public interface Stream<T> {
    Pair<T> eval();

    class Pair<T>{
        private T value;
        private Stream<T> rest;
        private Pair(T value,Stream<T> rest){
            this.value = value;
            this.rest = rest;
        }
    }
    static <T> Stream<T> empty(){
        return () -> null;
    }
    static <T> Stream<T> of(T x){
        return () -> new Pair<>(x,empty());
    }

    default Stream<T> stream(){
        class State{
            Iterator<T> it = iterator();
            Stream<T> stream() {
                return () -> (it.hasNext()) ?
                        new Pair<T>(it.next(),State.this.stream()) :
                        null;
            }

        }
        return new State().stream();
    }

    static <T> Stream<T> of(T[] args){
        class State{
            int count =0;
            Stream<T> of(){
                if(count == args.length) return empty();
                final T value = args[count++];
                return () -> new Pair<T>(value,of());
            }
        }
        return new State().of();
    }

    default Optional<T> findFirst(){
        Pair<T> pair = eval();
        if(pair == null) return Optional.empty();
        return Optional.of(pair.value);
    }

    default void foreach(Consumer<? super T> action){
        for(Pair<T> pair = eval(); pair != null;pair= pair.rest.eval()){
            action.accept(pair.value);
        }
    }


    default Stream<T> filter(Predicate<? super T> p){
        return () -> {
          Pair<T> pair = eval();
          if(pair == null) return  null;
          if(p.test(pair.value))
              return new Pair<>(
                      pair.value,
                      pair.rest.filter(p)
              );
          else return pair.rest.filter(p).eval();
        };
    }


    default <S> Stream <S> map(Function<? super T, ? extends S> f){
        return () -> {
          Pair<T> pair = eval();
          if(pair == null) return null;
          return new Pair<>(
                  f.apply(pair.value),
                  pair.rest.map(f)
          );
        };
    }

    default Stream<T> concat(Stream<T> after){
        return () -> {
          Pair<T> pair = eval();
          if(pair == null) return after.eval();
          return new Pair<>(
                  pair.value,
                  pair.rest.concat(after)
          );
        };
    }

    default <T> Stream<T> flatten(Stream<Stream<T>> ss){
        return () -> {
          Pair<Stream<T>> pair = ss.eval();
          if(pair == null) return null;
          if(pair.value == null)
              return flatten(pair.rest).eval();
          Pair<T> p = pair.value.eval();
          if(p == null) return flatten(pair.rest).eval();
          return new Pair<>(
                  p.value,
                  p.rest.concat(flatten(pair.rest))
          );
        };
    }

    default <T> Stream<T> flatArray(Stream<T[]> ss){
        return flatten(ss.map(Stream::of));
    }

    default Iterator<T> iterator(){
        return new Iterator<T>() {
            Pair<T> pair = Stream.this.eval();
            @Override
            public boolean hasNext() {
                return pair != null;
            }

            @Override
            public T next() {
                if(!hasNext()){
                    throw new NoSuchElementException();
                }
                T result = pair.value;
                pair = pair.rest.eval();
                return result;
            }
        };
    }

    default Optional<T> reduce(BinaryOperator<T> accumulator){
        Iterator<T> it = iterator();
        if(!it.hasNext()) return Optional.empty();
        T result = it.next();
        while (it.hasNext()){
            result = accumulator.apply(result,it.next());
        }
        return Optional.of(result);
    }

    default <U> U reduce(U identity,BiFunction<U,? super T,U> accumulator,BinaryOperator<U> combiner){
        Iterator<T> iterator = iterator();
        U result = identity;
        while (iterator.hasNext()){
            T t = iterator.next();
            result = accumulator.apply(result,t);
        }
        return result;
    }

    default <R> R collect(
            Supplier<R> supplier,
            BiConsumer<R,? super T> accumulator,
            BiConsumer<R,R> combiner){

        Iterator<T> it = iterator();
        R result = supplier.get();
        while (it.hasNext()) {
            T t = it.next();
            accumulator.accept(result, t);
        }
        return result;
    }


}
