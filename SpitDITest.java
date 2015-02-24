import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import org.junit.Test;

public class SpitDITest {
	@Test
	public void Inject_by_name_and_type() throws Exception {
		Hello hello = new Hello();
		SpitDI spit = new SpitDI();
		spit.bindByName(String.class, "message", "World")
			.bindByType(Integer.class, 1234)
			.inject(hello);
		assertEquals("World", hello.name);
		assertEquals(1234, hello.number, .001);
	}

	class Hello {
		@Resource
		String name;
		@Resource
		Integer number;
	}
}
