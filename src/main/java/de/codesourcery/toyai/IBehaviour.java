package de.codesourcery.toyai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.codesourcery.toyai.behaviours.AbstractBehaviour;

public interface IBehaviour
{
	public static final SuppressingLogger LOG = new SuppressingLogger();

    public static final IBehaviour NOP = new IBehaviour()
    {
        @Override
		public Result tick(float deltaSeconds, IBlackboard blackboard) { return Result.SUCCESS; }

        @Override
		public String toString() { return "NOP behaviour"; }

        @Override
		public long getId() { return 0; }
    };

    public long getId();

    public static enum Result {
        PENDING,
        FAILURE,
        SUCCESS;
    }

    public interface IBehaviourProducer {

        public IBehaviour create(float deltaSeconds,IBlackboard bb);
    }

    public default IBehaviour peek(IBehaviour delegate)
    {
    	return new AbstractBehaviour() {

			@Override
			public Result tickHook(float deltaSeconds, IBlackboard blackboard)
			{
				final Result result = delegate.tick( deltaSeconds , blackboard );
				if ( LOG.isDebugEnabled() ) {
					LOG.log("PEEK #"+getId()+": "+delegate+" => "+result);
				}
				return result;
			}

			@Override
			public String toString()
			{
				return "PEEK #"+getId()+" ( "+delegate+" )";
			}

			@Override
			protected void discardHook(IBlackboard bb)
			{
				delegate.discard( bb );
			}
		};
    }

    public default IBehaviour peek()
    {
    	return peek(this);
    }

    public default IBehaviour doWhile(final IBehaviour delegate)
    {
    	final IBehaviour self = this;
    	return new AbstractBehaviour()
    	{
			@Override
			protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
			{
				final Result cond = delegate.tick(deltaSeconds, blackboard);
				switch( cond )
				{
					case FAILURE:
					case PENDING:
						return cond;
					case SUCCESS:
						return self.tick( deltaSeconds , blackboard );
					default:
                        throw new RuntimeException("Unhandled case: "+cond);
				}
			}

			@Override
			protected void discardHook(IBlackboard bb) {
				delegate.discard( bb );
			}

			@Override
			public String toString()
			{
				return "do "+self+" WHILE "+delegate;
			}
		};
    }

    public default IBehaviour doUntil(final IBehaviour delegate)
    {
    	final IBehaviour self = this;
    	return new AbstractBehaviour()
    	{
			@Override
			protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
			{
				final Result cond = delegate.tick(deltaSeconds, blackboard);
				switch( cond )
				{
					case FAILURE:
						return self.tick( deltaSeconds , blackboard );
					case PENDING:
						return cond;
					case SUCCESS:
						return Result.FAILURE;
					default:
                        throw new RuntimeException("Unhandled case: "+cond);
				}
			}

			@Override
			protected void discardHook(IBlackboard bb) {
				delegate.discard( bb );
			}

			@Override
			public String toString()
			{
				return "do "+self+" UNTIL "+delegate;
			}
		};
    }

    public default IBehaviour forever( Supplier<IBehaviour> supp)
    {
        return new AbstractBehaviour()
        {
        	private IBehaviour delegate = supp.get();

            @Override
            protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
            {
                final Result result = delegate.tick(deltaSeconds, blackboard);
                switch( result )
                {
					case FAILURE:
						// $$FALL-THROUGH
						delegate.discard( blackboard );
						delegate = supp.get();
						break;
					case SUCCESS:
					case PENDING:
						break;
					default:
                        throw new RuntimeException("Unhandled case: "+result);
                }
                return Result.PENDING;
            }

            @Override
            public String toString() {
            	return "forever "+delegate;
            }

            @Override
            protected void discardHook(IBlackboard blackboard)
            {
                if ( delegate != null )
                {
                    delegate.discard(blackboard);
                }
            }
        };
    }

    public default IBehaviour or(IBehaviour b2,IBehaviour... other)
    {
        final IBehaviour[] array = new IBehaviour[2+(other == null ? 0 : other.length ) ];
        array[0]=this;
        array[1] = b2;
        if ( other != null ) {
            for ( int i = 0 ; i < other.length ; i++ ) {
                array[2+i] = other[i];
            }
        }
        return new AbstractBehaviour()
        {
            private int currentIndex=0;
            private IBehaviour current = array[0];

            @Override
            protected void discardHook(IBlackboard blackboard)
            {
              for (int i = 0 , len = array.length ; i < len ; i++)
              {
                array[i].discard( blackboard );
              }
            }

            @Override
            public String toString() {
            	return "{ "+Arrays.stream( array ).map( s -> s.toString() ).collect( Collectors.joining(" OR ") )+" }";
            }

            @Override
            protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
            {
                final Result result = current.tick(deltaSeconds, blackboard);
                switch( result )
                {
                    case FAILURE:
                        if ( (currentIndex+1) < array.length ) {
                            currentIndex++;
                            current = array[currentIndex];
                            return Result.PENDING;
                        }
                        return result;
                    case PENDING:
                        // $$FALL-THROUGH$$
                    case SUCCESS:
                        return result;
                    default:
                        throw new RuntimeException("Unhandled case: "+result);
                }
            }
        };
    }

    public default IBehaviour parallel(IBehaviour b2,IBehaviour... other)
    {
        final List<IBehaviour> array = new ArrayList<>();
        array.add( this );
        array.add( b2 );
        if ( other != null )
        {
            for ( int i = 0 ; i < other.length ; i++ ) {
                array.add( other[i] );
            }
        }
        return new AbstractBehaviour()
        {
        	@Override
        	public String toString()
        	{
        		return "parallel { "+array.stream().map( s -> s.toString() ).collect( Collectors.joining(" | ") )+" }";
        	}

            @Override
            protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
            {
                if ( array.isEmpty() ) {
                    return Result.SUCCESS;
                }
                for ( int i = 0 ; i < array.size() ; i++)
                {
                    final IBehaviour b = array.get(i);
                    final Result result = b.tick( deltaSeconds, blackboard);
                    switch( result )
                    {
                        case FAILURE:
                            return Result.FAILURE;
                        case PENDING:
                            break;
                        case SUCCESS:
                            array.remove( i );
                            i--;
                            break;
                        default:
                            throw new RuntimeException("Unhandled case: "+result);
                    }
                }
                return Result.PENDING;
            }

            @Override
            protected void discardHook(IBlackboard blackboard)
            {
              for (int i = 0 , len = array.size() ; i < len ; i++)
              {
                array.get(i).discard( blackboard );
              }
            }
        };
    }

    public default IBehaviour sequence(IBehaviour b1,IBehaviour b2,IBehaviour... other)
    {
        final IBehaviour[] array = new IBehaviour[2+(other == null ? 0 : other.length ) ];
        array[0] = b1;
        array[1] = b2;

        if ( other != null ) {
            for ( int i = 0 ; i < other.length ; i++ ) {
                array[2+i] = other[i];
            }
        }
        return new AbstractBehaviour()
        {
            private int index = 0;

            @Override
            protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
            {
                final Result r = array[index].tick( deltaSeconds, blackboard );
                switch( r )
                {
                    case FAILURE:
                        // $$FALL-THROUGH$$
                    case PENDING:
                        return r;
                    case SUCCESS:
                        if ( (index+1) < array.length ) {
                            index++;
                            return Result.PENDING;
                        }
                        return Result.SUCCESS;
                    default:
                        throw new RuntimeException("Unhandled case: "+r);
                }
            }

            @Override
            public String toString() {
            	return " { " +Arrays.stream( array ).map( s -> s.toString() ).collect( Collectors.joining(" AND ") )+" }";
            }

            @Override
            protected void discardHook(IBlackboard blackboard)
            {
              for (int i = 0 , len = array.length ; i < len ; i++)
              {
                array[i].discard( blackboard );
              }
            }
        };
    }

    public default IBehaviour andThen(IBehaviour b2)
    {
        return sequence(this,b2);
    }

    public Result tick(float deltaSeconds, IBlackboard blackboard);

    public default void discard(IBlackboard blackboard)
    {
    }

    public default IBehaviour not(IBehaviour delegate)
    {
    	return new AbstractBehaviour() {

			@Override
			protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
			{
				final Result result = delegate.tick(deltaSeconds, blackboard);
				switch( result ) {
					case FAILURE:
						return Result.SUCCESS;
					case PENDING:
						return result;
					case SUCCESS:
						return Result.FAILURE;
					default:
						throw new RuntimeException("Unhandled switch/case: "+result);
				}
			}

			@Override
			public String toString() {
				return "NOT { "+delegate+" }";
			}

			@Override
			protected void discardHook(IBlackboard bb)
			{
				delegate.discard( bb );
			}
		};
    }

    public default IBehaviour negated() {
    	return not(this);
    }
}