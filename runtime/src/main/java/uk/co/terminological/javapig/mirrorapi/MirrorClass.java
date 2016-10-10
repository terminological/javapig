package uk.co.terminological.javapig.mirrorapi;

import uk.co.terminological.javapig.javamodel.JClassName;

public interface MirrorClass<T extends Object> {
	public T reflect();
	public JClassName getName();
}
