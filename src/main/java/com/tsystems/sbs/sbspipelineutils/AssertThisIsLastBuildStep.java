package com.tsystems.sbs.sbspipelineutils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;

public class AssertThisIsLastBuildStep extends Step {
	
	@DataBoundConstructor
	public AssertThisIsLastBuildStep(){}

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

			if(currentJob.getLastBuild().number == currentRun.number) {
				return null;
			} else {
				throw new InterruptedException("[AssertThisIsLastBuild]: This is not the last build.");
			}

		}
	}

	/**
     * Descriptor for {@link CancelOlderReleasesStep}.
	 */
	@Extension(optional = true)
	public static class StepDescriptorImpl extends StepDescriptor {

		@Override
		public String getDisplayName() {
            return "AssertThisIsLastBuildStep";
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getFunctionName() {
            return "assertThisIsLastBuild";
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return new HashSet();
		}

	}
	
}
