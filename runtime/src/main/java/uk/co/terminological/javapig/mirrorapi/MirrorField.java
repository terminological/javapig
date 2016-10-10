package uk.co.terminological.javapig.mirrorapi;

import uk.co.terminological.javapig.javamodel.JMethodName;

public interface MirrorField<T extends Object> {
	public void set(T value) throws UnsupportedOperationException;
	public T get();
	public JMethodName getName();
}
