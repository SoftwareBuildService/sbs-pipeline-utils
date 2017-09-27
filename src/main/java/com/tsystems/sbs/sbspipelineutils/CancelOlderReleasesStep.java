package com.tsystems.sbs.sbspipelineutils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.RunList;

public class CancelOlderReleasesStep extends Step {
	

	@DataBoundConstructor
	public CancelOlderReleasesStep(){}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new Execution(context);
	}

	private static class Execution extends SynchronousStepExecution<Void> {

		private static final long serialVersionUID = 1L;
		private StepContext context;

		protected Execution(StepContext context) throws IOException, InterruptedException {
			super(context);
			this.context = context;
		}

		@Override
		protected Void run() throws Exception {
			TaskListener listener = context.get(TaskListener.class);

			//Get the Run instance from context
			Run currentRun = context.get(Run.class);
			Job currentJob = currentRun.getParent();

			//Iterate all the builds of the current job to check whether they are in the release stage and, if so, cancel the release stage
			RunList builds = currentJob.getBuilds();
			Iterator<WorkflowRun> iter = builds.iterator();

			while(iter.hasNext()) {
				WorkflowRun build = iter.next();
				//Process older builds which are still running
				if(build.getNumber() < currentRun.getNumber() && build.isBuilding()) {
					context.get(TaskListener.class).getLogger().println("Older build " + build.getNumber() + " is running.");
					//Get input step action and abort it
					//TODO: do not get just any input action, only the one which asks for a release
					
					InputAction inputAction = build.getAction(InputAction.class);
					if(inputAction != null) {
						List<InputStepExecution> executions = inputAction.getExecutions();
						for (InputStepExecution execution : executions) {
							listener.getLogger().println("Aborting input on build " + build.getNumber() + "...");
							execution.doAbort();
							listener.getLogger().println("Input on build " + build.getNumber() + " aborted.");
						}
					}
				}
			}

			return null;
		}
	}

	/**
     * Descriptor for {@link CancelOlderReleasesStep}.
	 */
	@Extension(optional = true)
	public static class StepDescriptorImpl extends StepDescriptor {

		@Override
		public String getDisplayName() {
            return "CancelOlderReleasesStep";
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getFunctionName() {
            return "cancelOlderReleases";
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return new HashSet();
		}

	}


}

