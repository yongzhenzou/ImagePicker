package zyz.hero.imagepicker

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        var listNode = ListNode(1, ListNode(2,
            ListNode(3, ListNode(4, ListNode(5)))))
        println(listNode)
        reverseNode(listNode)
    }

    fun reverseNode(listNode: ListNode) {
        var p1 = listNode
        var p2 = listNode.next
        var p3:ListNode? = null
        while (p2!=null){
            p3 = p2.next
            p2.next = p1
            p1 = p2
            p2 = p3
        }
        listNode.next = null
        println(p1)
    }

    data class ListNode(var value: Int, var next: ListNode? = null)
}