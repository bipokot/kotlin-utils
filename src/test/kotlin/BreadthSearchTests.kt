
import org.junit.Assert
import org.junit.Test

class BreadthSearchTests {

    @Test
    fun breadthSearch() {
        val list = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

        Assert.assertEquals(null, list.breadthSearch (5) { it == -1 })

        Assert.assertEquals(2, list.breadthSearch (5) { it == 2 })
        Assert.assertEquals(0, list.breadthSearch (5) { it == 0 })
        Assert.assertEquals(9, list.breadthSearch (5) { it == 9 })

        Assert.assertEquals(3, list.breadthSearch (25) { it == 3 })
        Assert.assertEquals(9, list.breadthSearch (25) { it == 9 })
        Assert.assertEquals(3, list.breadthSearch (-25) { it == 3 })
        Assert.assertEquals(9, list.breadthSearch (-25) { it == 9 })

        Assert.assertEquals(9, list.breadthSearch { it == 9 })
        Assert.assertEquals(0, list.breadthSearch { it == 0 })
        Assert.assertEquals(4, list.breadthSearch { it == 4 })
        Assert.assertEquals(7, list.breadthSearch { it == 7 })

        Assert.assertEquals(null, list.breadthSearch { it == -120 })
        Assert.assertEquals(null, list.breadthSearch { it == 120 })
        Assert.assertEquals(null, list.breadthSearch (1) { it == 120 })
        Assert.assertEquals(null, list.breadthSearch (7) { it == 120 })

    }

    @Test
    fun breadthSearchWithIndex() {
        val list = listOf(10, 11, 12, 13, 14, 15, 16, 17, 18, 19)

        Assert.assertEquals(null, list.breadthSearchWithIndex (5) { it == -1 })

        Assert.assertEquals(IndexedValue(2, 12), list.breadthSearchWithIndex (5) { it == 12 })
        Assert.assertEquals(IndexedValue(0, 10), list.breadthSearchWithIndex (5) { it == 10 })
        Assert.assertEquals(IndexedValue(9, 19), list.breadthSearchWithIndex (5) { it == 19 })

        Assert.assertEquals(IndexedValue(3, 13), list.breadthSearchWithIndex (25) { it == 13 })
        Assert.assertEquals(IndexedValue(9, 19), list.breadthSearchWithIndex (25) { it == 19 })
        Assert.assertEquals(IndexedValue(3, 13), list.breadthSearchWithIndex (-25) { it == 13 })
        Assert.assertEquals(IndexedValue(9, 19), list.breadthSearchWithIndex (-25) { it == 19 })

        Assert.assertEquals(IndexedValue(9, 19), list.breadthSearchWithIndex { it == 19 })
        Assert.assertEquals(IndexedValue(0, 10), list.breadthSearchWithIndex { it == 10 })
        Assert.assertEquals(IndexedValue(4, 14), list.breadthSearchWithIndex { it == 14 })
        Assert.assertEquals(IndexedValue(7, 17), list.breadthSearchWithIndex { it == 17 })

        Assert.assertEquals(null, list.breadthSearchWithIndex { it == -120 })
        Assert.assertEquals(null, list.breadthSearchWithIndex { it == 120 })
        Assert.assertEquals(null, list.breadthSearchWithIndex (1) { it == 120 })
        Assert.assertEquals(null, list.breadthSearchWithIndex (7) { it == 120 })

    }

}