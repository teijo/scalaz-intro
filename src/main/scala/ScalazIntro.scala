import scalaz._
import scalaz.std.anyVal._
import scalaz.std.list._

object ScalazIntro {
  def testApplicative() {
    val a = Applicative[List]
    assert(List(1) == a.point(1))
    assert(List(2, 3, 4) == a.map(List(1, 2, 3))(_ + 1))

    val result = a.ap(a.point(1))(List({ i: Int => i + 1 }, { i: Int => i - 1 }))
    assert(List(2, 0) == result)
  }

  def testFunctor() {
    val result = List[Int](1, 2, 3).map { x: Int => x + 1 }
    assert(List(2, 3, 4) == result)

    val f = Functor[List]
    assert(List(2, 3, 4) == f.map(List(1, 2, 3))(_ + 1))
  }

  def testMonoid() {
    val m = Monoid[Int]
    assert(3 == m.append(1, 2))
    assert(0 == m.zero)
  }

  def main(args: Array[String]) {
    testApplicative()
    testFunctor()
    testMonoid()
  }
}
