package hudson.plugins.fitnesse;

import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.StaplerProxy;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import static java.util.Collections.*;

public class FitnesseHistoryAction implements StaplerProxy, Action {
	private final AbstractProject<?, ?> project;

	public FitnesseHistoryAction(AbstractProject<?, ?> project) {
		this.project = project;
	}

	public List<String> getPages(List<FitnesseResults> builds) {
		Map<String, PageInfo> pages = new HashMap<String, PageInfo>();

		for (FitnesseResults results : builds) {
			for (FitnesseResults result : results.getChildResults()) {
				PageInfo info = pages.get(result.getName());
				if (info == null) {
					info = new PageInfo(result.getName());
					pages.put(result.getName(), info);
				}
				info.recordResult(result);
			}
		}

		return sorted(pages);
	}

	private List<String> sorted(Map<String, PageInfo> map) {
		List<PageInfo> pages = new ArrayList<PageInfo>(map.values());
		sort(pages, PageInfo.defaultOrdering());
		return Lists.transform(pages, new Function<PageInfo, String>() {

			public String apply(PageInfo input) {
				return input == null ? null : input.page;
			}
		});
	}

	private List<FitnesseResults> getBuilds(AbstractProject<?, ?> project) {
		List<FitnesseResults> result = new ArrayList<FitnesseResults>();

		for (AbstractBuild<?, ?> build : project.getBuilds()) {
			FitnesseResultsAction action = build.getAction(FitnesseResultsAction.class);
			if (action != null) {
				result.add(action.getResult());
			}
		}

		return result;
	}

	public Object getTarget() {
		List<FitnesseResults> builds = getBuilds(project);
		List<String> pages = getPages(builds);

		return new FitnesseHistory(project, pages, builds);
	}

	public String getIconFileName() {
		return "/plugin/fitnesse/icons/fitnesselogo-32x32.gif";
	}

	public String getDisplayName() {
		return "FitNesse History";
	}

	public String getUrlName() {
		return "fitnesseHistory";
	}

	private static class PageInfo {
		private final String page;

		private boolean lastResultWasPass = true;

		/** The number of switches between 'fail' and 'pass' or vice-versa. */
		private int numberOfSwitches = 0;

		/** The number of times this test was seen at all */
		private int numberOfOccurrances = 0;

		public PageInfo(String page) {
			this.page = page;
		}

		public void recordResult(FitnesseResults result) {
			if (result.isPassedOverall() || result.isFailedOverall()) {
				numberOfOccurrances++;
				if (lastResultWasPass == result.isFailedOverall()) {
					numberOfSwitches++;
				}
				lastResultWasPass = result.isPassedOverall();
			}
		}

		private Integer erraticnessIndex() {
			if (numberOfOccurrances == 0) {
				return 0;
			} else {
				return 100 * numberOfSwitches / numberOfOccurrances;
			}
		}

		public static Comparator<PageInfo> defaultOrdering() {
			return new PageInfo.ByErraticness().reverse().compound(new PageInfo.ByPage());
		}

		private static class ByErraticness extends Ordering<PageInfo> {

			public int compare(PageInfo o1, PageInfo o2) {
				return o1.erraticnessIndex().compareTo(o2.erraticnessIndex());
			}
		}

		private static class ByPage extends Ordering<PageInfo> {

			public int compare(PageInfo o1, PageInfo o2) {
				return o1.page.compareTo(o2.page);
			}
		}
	}
}
