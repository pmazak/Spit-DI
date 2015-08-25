/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;

public class SpitDITest {
	@Test
	public void Can_set_collection_interfaces() throws Exception {
		Hello hello = new Hello();
		SpitDI spit = new SpitDI();
		spit.bindByName(Set.class, "items", new LinkedHashSet<String>())
			.bindByName(String.class, "message", "Universe")
			.inject(hello);
		assertEquals(0, hello.items.size());
	}

	@Test
	public void Configure_overwrite_for_duplicate_binding() throws Exception {
		Hello hello = new Hello();
		SpitDI spit = new SpitDI();
		spit.bindByName(String.class, "message", "World")
			.bindByType(Integer.class, 1234)
			.inject(hello);
		assertEquals("World", hello.message);
		assertEquals(1234, hello.number, .001);
	}

	@Test(expected = IllegalArgumentException.class)
	public void Does_not_allow_duplicate_bindings() throws Exception {
		Hello hello = new Hello();
		SpitDI spit = new SpitDI();
		spit.bindByName(String.class, "message", "World")
			.bindByName(String.class, "message", "Universe")
			.inject(hello);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void Does_not_allow_byType_binding_to_overwrite_byName_binding() throws Exception {
		Hello hello = new Hello();
		SpitDI spit = new SpitDI();
		spit.bindByName(String.class, "message", "World")
			.bindByType(String.class, "Universe")
			.inject(hello);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void Does_not_allow_byName_binding_if_same_byType_binding_was_already_used() throws Exception {
		Hello hello = new Hello();
		SpitDI spit = new SpitDI();
		spit.bindByType(String.class, "World")
			.bindByName(String.class, "message", "Universe")
			.inject(hello);
	}

	@Test
	public void Inject_by_name_and_type() throws Exception {
		Hello hello = new Hello();
		SpitDI spit = new SpitDI();
		spit.bindByName(String.class, "message", "World")
			.bindByName(String.class, "message", "Universe", true)
			.inject(hello);
		assertEquals("Universe", hello.message);
	}

	class Hello {
		@Resource
		String message;
		@Resource
		Integer number;
		@Resource
		Set<String> items;
	}
}