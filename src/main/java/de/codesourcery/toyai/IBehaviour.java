package de.codesourcery.toyai;

public interface IBehaviour 
{
    public static final IBehaviour NOP = new IBehaviour() 
    {
        public Result tick(float deltaSeconds) { return Result.SUCCESS; }

        public String toString() { return "NOP behaviour"; }
    };
    
    public static enum Result {
        PENDING,
        FAILURE,
        SUCCESS;
    }
    
    public Result tick(float deltaSeconds);
    
    public default void onCancel() {
    }
    
    public default IBehaviour andThen(IBehaviour b2) 
    {
        final IBehaviour self = this;
        return new IBehaviour() 
        {
            private boolean behaviour1Finished=false;

            private IBehaviour current = self;
            
            @Override
            public Result tick(float deltaSeconds) 
            {
                final Result result = current.tick(deltaSeconds);
                if ( result == Result.FAILURE ) {
                    return result;
                }
                if ( result == Result.SUCCESS )
                {
                    if ( ! behaviour1Finished ) {
                        behaviour1Finished = true;
                        current = b2 ;
                        return Result.PENDING;
                    }
                    return Result.SUCCESS;
                }
                return Result.PENDING;
            }
            
            @Override
            public void onCancel() 
            {
                current.onCancel();
            }
            
            @Override
            public String toString() 
            {
                return "andThen( "+self+" => "+b2+")";
            }
        };
    }    
}
