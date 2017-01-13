package com.axibase.energinet.extractors;

public abstract interface Extractor<T, S>
{
  public abstract T extract(S paramS);
}


/* Location:              /home/shmagrinskiy/Desktop/energinet-grabber.jar!/com/axibase/energinet/extractors/Extractor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */