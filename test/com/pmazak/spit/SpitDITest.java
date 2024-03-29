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

package com.pmazak.spit;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SpitDITest {
    @Test
    public void Can_set_collection_interfaces() {
        Hello hello = new Hello();
        SpitDI spit = new SpitDI();
        spit.bindByName(Set.class, "items", new LinkedHashSet<String>())
            .bindByName(String.class, "message", "Universe")
            .inject(hello);
        assertEquals(0, hello.items.size());
    }

    @Test
    public void Inject_by_name_and_type() {
        Hello hello = new Hello();
        SpitDI spit = new SpitDI();
        spit.bindByName(String.class, "message", "World")
            .bindByType(Integer.class, 1234)
            .inject(hello);
        assertEquals("World", hello.message);
        assertEquals(1234, hello.number, .001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void Does_not_allow_duplicate_bindings() {
        Hello hello = new Hello();
        SpitDI spit = new SpitDI();
        spit.bindByName(String.class, "message", "World")
            .bindByName(String.class, "message", "Universe")
            .inject(hello);
    }

    @Test(expected = IllegalArgumentException.class)
    public void Does_not_allow_byType_binding_to_overwrite_byName_binding() {
        Hello hello = new Hello();
        SpitDI spit = new SpitDI();
        spit.bindByName(String.class, "message", "World")
            .bindByType(String.class, "Universe")
            .inject(hello);
    }

    @Test(expected = IllegalArgumentException.class)
    public void Does_not_allow_byName_binding_if_same_byType_binding_was_already_used() {
        Hello hello = new Hello();
        SpitDI spit = new SpitDI();
        spit.bindByType(String.class, "World")
            .bindByName(String.class, "message", "Universe")
            .inject(hello);
    }

    @Test
    public void Configure_overwrite_for_duplicate_binding() {
        Hello hello = new Hello();
        SpitDI spit = new SpitDI();
        spit.bindByName(String.class, "message", "World")
            .bindByName(String.class, "message", "Universe", true)
            .inject(hello);
        assertEquals("Universe", hello.message);
    }

    @Test
    public void Inject_static_resources() {
        SpitDI spit = new SpitDI();
        spit.bindByName(Long.class, "time", 987654321L)
            .bindStatic(Hello.class)
            .inject();
        assertEquals(987654321L, Hello.time, .001);
    }

    @Test
    public void Instance_binding_sets_static_resources_as_well() {
        SpitDI spit = new SpitDI();
        Hello hello = new Hello();
        spit.bindByName(Hello.class, "singleton", hello)
            .inject();
        assertEquals(hello, Hello.singleton);
    }

    @Test
    public void Instance_binding_sets_instance_resources_as_well() {
        SpitDI spit = new SpitDI();
        Hello hello = new Hello();
        spit.bindByName(Hello.class, "singleton", hello)
            .bindByName(String.class, "message", "world")
            .inject();
        assertEquals(hello, Hello.singleton);
        assertEquals("world", hello.message);
    }

    @Test
    public void Static_binding_of_null_may_overwrite_instance_binding() {
        SpitDI spit = new SpitDI();
        Hello hello = new Hello();
        spit.bindByName(Hello.class, "singleton", hello)
            .bindStatic(Hello.class, true)
            .inject();
        assertEquals(null, Hello.singleton);
    }

    @Test
    public void Instance_binding_may_overwrite_static_binding() {
        SpitDI spit = new SpitDI();
        Hello hello = new Hello();
        spit.bindStatic(Hello.class)
            .bindByName(Hello.class, "singleton", hello, true)
            .inject();
        assertEquals(hello, Hello.singleton);
    }

    @Test
    public void Inject_using_setter_of_name_and_type() {
        Hello hello = new Hello();
        SpitDI spit = new SpitDI();
        spit.bindByName(File.class, "file1", new File("/tmp/1"))
            .bindByName(File.class, "fileTwo", new File("/tmp/2"))
            .inject(hello);
        assertEquals("/tmp/1", hello.file1.getAbsolutePath());
        assertEquals("/tmp/2", hello.file2.getAbsolutePath());
    }

    @Test
    public void Inject_setter_of_name_that_matches_parameter_type_when_multiple_setters() {
        Hello hello = new Hello();
        SpitDI spit = new SpitDI();
        spit.bindByName(String.class, "file1", "/tmp/1")
            .inject(hello);
        assertEquals("/tmp/1", hello.file1.getAbsolutePath());
    }

    @Test
    public void Does_not_inject_using_setter_of_type_only() {
        Hello hello = new Hello();
        SpitDI spit = new SpitDI();
        spit.bindByType(File.class, new File("/tmp"))
            .inject(hello);
        assertEquals(null, hello.file1);
        assertEquals(null, hello.file2);
    }

    static class Hello {
        @Resource
        static Long time;
        @Resource
        static Hello singleton;
        
        @Resource
        String message;
        @Resource
        Integer number;
        @Resource
        Set<String> items;

        File file1;
        File file2;

        @Resource
        void setFile1(File f) {
            this.file1 = f;
        }
        @Resource
        void setFile1(String s) {
            this.file1 = new File(s);
        }
        @Resource
        void setFileTwo(File f) {
            this.file2 = f;
        }
    }
}