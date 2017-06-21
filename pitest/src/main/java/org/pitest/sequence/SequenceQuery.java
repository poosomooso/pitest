package org.pitest.sequence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pitest.functional.predicate.False;
import org.pitest.functional.predicate.Predicate;

public class SequenceQuery<T> {
  
  private final Partial<T> token;
  
  SequenceQuery(Partial<T> token) {
    this.token = token;
  }

  public SequenceQuery<T> then(Predicate< T> next) {
    return then(new SequenceQuery<T>(new Literal<T>(next)));
  }

  public SequenceQuery<T> then(SequenceQuery<T> next) {
    final Concat<T> concat = new Concat<T>(this.token, next.token);
    return new SequenceQuery<T>(concat);
  }

  public SequenceQuery<T> or(SequenceQuery<T> next) {
    final Or<T> or = new Or<T>(this.token, next.token);
    return new SequenceQuery<T>(or);
  }

  public SequenceQuery<T> thenAnyOf(SequenceQuery<T> left,
      SequenceQuery<T> right) {
    final Or<T> or = new Or<T>(left.token, right.token);
    final Concat<T> concat = new Concat<T>(this.token, or);
    return new SequenceQuery<T>(concat);
  }

  public SequenceQuery<T> zeroOrMore(SequenceQuery<T> next) {
    final Concat<T> concat = new Concat<T>(this.token, new Repeat<T>(next.token));
    return new SequenceQuery<T>(concat);
  }
  


  public SequenceQuery<T> oneOrMore(SequenceQuery<T> next) {
    final Concat<T> concat = new Concat<T>(this.token, new Plus<T>(next.token));
    return new SequenceQuery<T>(concat);
  }
  
  public SequenceMatcher<T> compile() {
    return compileIgnoring(False.<T>instance());
  }

  @SuppressWarnings("unchecked")
  public SequenceMatcher<T> compileIgnoring(Predicate<T> ignoring) {
    return new NFASequenceMatcher<T>(ignoring, this.token.make(Match.MATCH));
  }

  interface Partial<T> {
    State<T> make(State<T> andThen);
  }

  static class Literal<T> implements Partial<T> {
    Predicate< T> c;

    Literal(Predicate<  T> p) {
      this.c = p;
    }

    @Override
    public State<T> make(State<T> andThen) {
      return new Consume<T>(this.c, andThen);
    }
  }

  static class Or<T> implements Partial<T> {
    Partial<T> left;
    Partial<T> right;

    Or(Partial<T> left, Partial<T> right) {
      this.left = left;
      this.right = right;
    }

    @Override
    public State<T> make(State<T> andThen) {
      final State<T> l = this.left.make(andThen);
      final State<T> r = this.right.make(andThen);
      return new Split<T>(l, r);
    }

  }

  static class Concat<T> implements Partial<T> {
    Partial<T> left;
    Partial<T> right;

    Concat(Partial<T> left, Partial<T> right) {
      this.left = left;
      this.right = right;
    }

    @Override
    public State<T> make(State<T> andThen) {
      return this.left.make(this.right.make(andThen));
    }
  }

  static class Repeat<T> implements Partial<T> {
    Partial<T> r;

    Repeat(Partial<T> r) {
      this.r = r;
    }

    @Override
    public State<T> make(State<T> andThen) {
      final Split<T> placeHolder = new Split<T>(null, null);
      final State<T> right = this.r.make(placeHolder);
      final Split<T> split = new Split<T>(right, andThen);

      placeHolder.out1 = split;
      return placeHolder;
    }

  }

  static class Plus<T> implements Partial<T> {
    Partial<T> r;

    Plus(Partial<T> r) {
      this.r = r;
    }

    @Override
    public State<T> make(State<T> andThen) {
      final Concat<T> concat = new Concat<T>(this.r, new Repeat<T>(this.r));
      return concat.make(andThen);
    }
  }

}

class NFASequenceMatcher<T> implements SequenceMatcher<T> {

  private final Predicate<T> ignore;
  private final State<T> start;

  NFASequenceMatcher( Predicate<T> ignore, State<T> state) {
    this.ignore = ignore;
    this.start = state;
  }

  @Override
  public boolean matches(List<T> sequence) {
    Set<State<T>> currentState = new HashSet<State<T>>();

    addstate(currentState, this.start);

    for (int i = 0; i != sequence.size(); i++) {
      final T c = sequence.get(i);
      
      if (ignore.apply(c)) {
        continue;
      }
      
      final Set<State<T>> nextStates = step(currentState, c);
      currentState = nextStates;
    }
    return isMatch(currentState);
  }

  private static <T> void addstate(Set<State<T>> set, State<T> state) {
    if (state == null) {
      return;
    }
    if (state instanceof Split) {
      final Split<T> split = (Split<T>) state;
      addstate(set, split.out1);
      addstate(set, split.out2);
    } else {
      set.add(state);
    }

  }

  private static <T> Set<State<T>> step(Set<State<T>> currentState, T c) {

    final Set<State<T>> nextStates = new HashSet<State<T>>();
    for (final State<T> each : currentState) {
      if (each instanceof Consume) {
        final Consume<T> consume = (Consume<T>) each;
        if (consume.c.apply(c)) {
          addstate(nextStates, consume.out);
        }
      }
    }
    return nextStates;
  }

  private static <T> boolean isMatch(Set<State<T>> currentState) {
    return currentState.contains(Match.MATCH);
  }

}

interface State<T> {

}

class Consume<T> implements State<T> {
  final Predicate< T> c;
  State<T>           out;

  Consume(Predicate<T> c, State<T> out) {
    this.c = c;
    this.out = out;
  }
  
  boolean matches(T t) {
    return c.apply(t);
  }
}

class Split<T> implements State<T> {
  State<T> out1;
  State<T> out2;

  Split(State<T> out1, State<T> out2) {
    this.out1 = out1;
    this.out2 = out2;
  }
}

@SuppressWarnings("rawtypes")
enum Match implements State {
  MATCH;
}