package org.crawl.http.payload.web.base;

import org.junit.Test;

/**
 * @author wb-wj
 * @date //
 */
public class StaticDispatch {
    static abstract class Human {
        protected abstract void sayHello();
    }

    static class Man extends Human {
        @Override
        protected void sayHello() {
            System.out.println("man say hello");
        }
    }

    static class Woman extends Human {
        @Override
        protected void sayHello() {
            System.out.println("woman say hello");
        }
    }

    @Test
    public void test() {
        Human man = new Man();
        Human woman = new Woman();
        StaticDispatch sr = new StaticDispatch();
        sr.sayHello(man);
        sr.sayHello(woman);
        
        System.out.println("``````````````");
        sr.sayHello((Man)man);
        sr.sayHello((Woman)woman);

    }

    public void sayHello(Human guy) {
        System.out.println("Hello guy");
    }

    public void sayHello(Man guy) {
        System.out.println("Hello man");
    }

    public void sayHello(Woman guy) {
        System.out.println("Hello woman");
    }

    @Test
    public void test2() {
        Human man = new Man();
        Human woman = new Woman();
        man.sayHello();
        woman.sayHello();
        man = new Woman();
        man.sayHello();

    }
}
