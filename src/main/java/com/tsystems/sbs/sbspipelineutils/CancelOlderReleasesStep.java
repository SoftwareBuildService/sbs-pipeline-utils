package com.tsystems.sbs.sbspipelineutils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
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
			//Get the Run instance from context
			Run currentRun = context.get(Run.class);
			Job currentJob = currentRun.getParent();
			
			//Iterate all the builds of the current job
			RunList builds = currentJob.getBuilds();
			Iterator<WorkflowRun> iter = builds.iterator();
			while(iter.hasNext()) {
				WorkflowRun build = iter.next();
				if(build.getNumber() < currentRun.getNumber() && build.isBuilding()) {
					context.get(TaskListener.class).getLogger().println("Older build " + build.getNumber() + " is running.");
				}
			}
			
			if (currentRun.equals(currentJob.getLastBuild()))
				context.get(TaskListener.class).getLogger().println("This is the last build!");
			else
				context.get(TaskListener.class).getLogger().println("This is NOT the last build :(");
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

