package com.vedantu.comm.pojos.news.clustering;

public interface IClusterable {
	public int hashCode();
	@Override
	public boolean equals( Object o );
	public String toString();
}
