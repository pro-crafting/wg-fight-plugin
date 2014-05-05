package de.hrc_gaming.generator;

import de.hrc_gaming.generator.JobState;

public interface JobStateChangedCallback 
{
	public void jobStateChanged(Job job, JobState fromState);
}
