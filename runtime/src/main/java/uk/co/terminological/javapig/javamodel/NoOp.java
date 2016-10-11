package uk.co.terminological.javapig.javamodel;

public class NoOp<Y> implements JModelAdaptor<Y> {
	@Override
	public Y adapt(Y model) {
		return model;
	}
}