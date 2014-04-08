package me.Postremus.Generator;

import me.Postremus.Generator.JobState;

public interface JobStateChangedCallback 
{
	public void jobStateChanged(Job job, JobState fromState);
}
