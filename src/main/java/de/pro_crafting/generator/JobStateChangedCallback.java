package de.pro_crafting.generator;


public interface JobStateChangedCallback 
{
	public void jobStateChanged(Job job, JobState fromState);
}
