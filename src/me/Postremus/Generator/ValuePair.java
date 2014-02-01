package me.Postremus.Generator;

public class ValuePair<T, T1> 
{
	private final T value1;
	private final T1 value2;
	
	public ValuePair(T value1, T1 value2)
	{
		this.value1 = value1;
		this.value2 = value2;
	}
	
	public T getValue1()
	{
		return this.value1;
	}
	
	public T1 getValue2()
	{
		return this.value2;
	}
}
