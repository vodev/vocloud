package cz.mrq.vocloud.worker.korel;

import uws.UWSException;
import uws.job.AbstractJob;
import uws.job.JobList;
import uws.service.AbstractUWS;
import uws.service.UWSUrl;
import uws.service.actions.UWSAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class GetLastAction<JL extends JobList<J>, J extends AbstractJob> extends UWSAction<JL, J> {
	private static final long serialVersionUID = 1L;

	private String lastAction = null;
	
	@Override
	public String getDescription() {
		return "Gets the last action executed by the UWS.";
	}

	@Override
	public String getName() {
		return "Last Action";
	}

	protected GetLastAction(AbstractUWS<JL, J> u) {
		super(u);
	}

	@Override
	public boolean match(UWSUrl urlInterpreter, String userId, HttpServletRequest request) throws UWSException {
		lastAction = (uws.getExecutedAction() != null)?uws.getExecutedAction().getName():null;
		return (!urlInterpreter.hasJobList()
				&& request.getQueryString() != null
				&& request.getQueryString().equalsIgnoreCase("lastAction"));
	}

	@Override
	public boolean apply(UWSUrl urlInterpreter, String userId, HttpServletRequest request, HttpServletResponse response) throws UWSException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.println(lastAction);
		out.close();
		return true;
	}
}
