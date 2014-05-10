package de.hrc_gaming.generator;


public interface JobStateChangedCallback 
{
	public void jobStateChanged(Job job, JobState fromState);
}
