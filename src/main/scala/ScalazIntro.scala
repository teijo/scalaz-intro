import scalaz._
import scalaz.std.anyVal._
import scalaz.std.list._

object ScalazIntro {
  def testFunctor() {
    val result = List[Int](1,2,3).map{ x:Int => x + 1 }
    assert(List(2, 3, 4) == result)

    val f = Functor[List]
    assert(List(2, 3, 4) == f.map(List(1, 2, 3))(_ + 1))
  }

  def testMonoid() {
    val m = Monoid[Int]
    assert(3 == m.append(1,2))
    assert(0 == m.zero)
  }

  def main(args: Array[String]) {
    testFunctor()
    testMonoid()
  }
}
