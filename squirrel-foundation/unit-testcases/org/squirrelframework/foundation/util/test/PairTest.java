package org.squirrelframework.foundation.util.test;

import org.junit.Test;
import org.squirrelframework.foundation.util.Pair;
import static org.junit.Assert.assertEquals;

public class PairTest {


	@Test
	public void equalsTest(){
		Pair pair = new Pair("","");
		assertEquals(pair.equals(null),false);
	}
	
	@Test
	public void hashCodeTest(){
		Pair pair = new Pair("first","second");
		assertEquals(pair.hashCode(),-782265828);
	}
	
	@Test
	public void toStringTest(){
		Pair pair = new Pair("first","second");
		assertEquals(pair.toString(),"first:second");
	}
	
}
