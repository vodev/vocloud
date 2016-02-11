package cz.rk.worker;

import java.io.BufferedOutputStream;
import java.io.IOException;
import uws.UWSException;
import uws.job.ErrorType;
import uws.job.JobThread;
import uws.job.Result;
import uws.job.UWSJob;

/**
 *
 * @author radiokoza
 */
public class CompWork extends JobThread{
    
    public CompWork(UWSJob j) throws UWSException{
        super(j);
    }
    
    
    @Override
    protected void jobWork() throws UWSException, InterruptedException {
        // retrieve parameters
        int a = (int) getJob().getAdditionalParameterValue("a");
        int b = (int) getJob().getAdditionalParameterValue("b");
        
        int counter = 0;
        for (int i = 0; i < 20; i++){
            if (isInterrupted()){
                throw new InterruptedException();
            }
            Thread.sleep(1000);
        }
        
        int result = a + b;
        //write result file
        Result res = createResult("sum");
        try (BufferedOutputStream output = new BufferedOutputStream(getResultOutput(res))) {
            output.write(("the sum is " + result).getBytes());
            publishResult(res);
        } catch (IOException ex){
            throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex, "Impossible to write to result", ErrorType.TRANSIENT);
        }
    }
    
}
