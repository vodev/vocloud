package cz.rk.worker;

import uws.UWSException;
import uws.job.JobList;
import uws.job.JobThread;
import uws.job.UWSJob;
import uws.job.parameters.InputParamController;
import uws.service.UWSServlet;

/**
 *
 * @author radiokoza
 */
public class UWSService extends UWSServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void initUWS() throws UWSException {
        //add job list
        addJobList(new JobList("sum"));
        addExpectedAdditionalParameter("a");
        addExpectedAdditionalParameter("b");
        setInputParamController("a", new ParamController());
        setInputParamController("b", new ParamController());
    }

    @Override
    public JobThread createJobThread(UWSJob uwsjob) throws UWSException {
        if (uwsjob.getJobList().getName().equals("sum")){
            return new CompWork(uwsjob);
        } else {
            throw new UWSException("No way baby");
        }
    }

    public static class ParamController implements InputParamController {

        @Override
        public Object getDefault() {
            return null;
        }

        @Override
        public Object check(Object o) throws UWSException {
            int res;
            if (o instanceof Integer) {
                res = (Integer) o;
            } else if (o instanceof String) {
                try {
                    res = Integer.parseInt((String) o);
                } catch (NumberFormatException ex) {
                    throw new UWSException(UWSException.BAD_REQUEST, ex, "Wrong request");
                }
            } else {
                throw new UWSException(UWSException.BAD_REQUEST, "Wrong request");
            }
            return res;
        }

        @Override
        public boolean allowModification() {
            return true;
        }

    }
    

}
