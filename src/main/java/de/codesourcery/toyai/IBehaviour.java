package de.codesourcery.toyai;

import java.util.ArrayList;
import java.util.List;

import de.codesourcery.toyai.behaviours.AbstractBehaviour;

public interface IBehaviour 
{
    public static final IBehaviour NOP = new IBehaviour() 
    {
        public Result tick(float deltaSeconds, IBlackboard blackboard) { return Result.SUCCESS; }

        public String toString() { return "NOP behaviour"; }
        
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
    
    public default IBehaviour forever(IBehaviourProducer producer) 
    {
        return new AbstractBehaviour()
        {
            private IBehaviour delegate;
            
            @Override
            protected Result tickHook(float deltaSeconds, IBlackboard blackboard)             
            {
                if ( delegate == null ) 
                {
                    delegate = producer.create(deltaSeconds,blackboard);
                }
                final Result result = delegate.tick(deltaSeconds, blackboard);
                switch(result) 
                {
                    case SUCCESS:
                    case FAILURE:
                        delegate = producer.create(deltaSeconds, blackboard);
                    case PENDING:
                        return Result.PENDING;
                    default:
                        throw new RuntimeException("Unhandled case: "+result);
                }
            }
            
            @Override
            protected void onDiscardHook(IBlackboard blackboard) 
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
            protected void onDiscardHook(IBlackboard blackboard) 
            {
              for (int i = 0 , len = array.length ; i < len ; i++) 
              {
                array[i].discard( blackboard );
              }
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
            protected void onDiscardHook(IBlackboard blackboard) 
            {
              for (int i = 0 , len = array.size() ; i < len ; i++) 
              {
                array.get(i).discard( blackboard );
              }      
            }
        };
    }
    
    public default IBehaviour sequence(IBehaviour b2,IBehaviour... other) 
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
            private int index = 0;
            
            @Override
            protected Result tickHook(float deltaSeconds, IBlackboard blackboard) 
            {
                final Result r = array[index].tick( deltaSeconds, blackboard );
                switch( r ) 
                {
                    case FAILURE:
                        return r;
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
            protected void onDiscardHook(IBlackboard blackboard) 
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
}
