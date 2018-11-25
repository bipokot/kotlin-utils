import org.junit.Assert
import org.junit.Test
import java.util.*

class Tests : Assert() {

    @Test
    fun Name() {
        val rndBool = Random().nextBoolean()

        val f1 = { println("in 1"); 1 }
        val f2 = { println("in 2"); 2 }

        val a: Int = if (rndBool) f1() else f2()
    }

    @Test
    fun name10() {

        var fooCheck = ""

        class Foo {
            var a: Int by observer(1) { println(it); fooCheck += it }
        }

        val foo = Foo()

        foo.a = 2
        foo.a = 3
        foo.a = 3
        foo.a = 4
        foo.a = 5
        foo.a = 5
        foo.a = 6

        Assert.assertEquals("2334556", fooCheck)

        var barCheck = ""

        class Bar {
            var a: Int by changeObserver(1) { println(it); barCheck += it }
        }

        val bar = Bar()

        bar.a = 2
        bar.a = 3
        bar.a = 3
        bar.a = 4
        bar.a = 5
        bar.a = 5
        bar.a = 6

        Assert.assertEquals("23456", barCheck)
    }

}

