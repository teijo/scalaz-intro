import scalaz._
import scalaz.std.anyVal._
import scalaz.std.list._

object ScalazIntro {
  def testHandleWith() {
    import scalaz.concurrent.Task

    // Catch matched exceptions
    val task: Task[Int] = Task {
      throw new RuntimeException("problem")
      0
    }.handleWith {
      case t: RuntimeException => Task { -1 }
      case t: NoSuchElementException => Task { 1 }
    }

    assert(-1 == task.run)
  }

  def testAsync() {
    import scalaz.concurrent.Task
    import scalaz.Disjunction.right

    val test = Task.async { (done: Disjunction[Throwable, Int] => Unit) =>
      Thread.sleep(100)
      done(right(1))
    }

    assert(1 == test.attemptRun.getOrElse(-1))

    val failTest = Task.async { (done: Disjunction[Throwable, Int] => Unit) =>
      throw new Exception("problem")
    }

    assert(-1 == failTest.attemptRun.getOrElse(-1))
  }

  def testComposition() {
    import scalaz.stream._

    val names = Process("one", "two", "three")
    val nums = Process(1, 2, 3)

    val result: Process[Nothing, (String, Int)] = names zip nums
    assert(Vector(("one", 1), ("two", 2), ("three", 3)) == result.toSource.runLog.run)
  }

  def testAppendingProcesses() {
    import scalaz.stream._

    val names = Process("one", "two", "three")
    def nums(n: Int): Process[Nothing, Int] = Process(n) ++ nums(n + 1) // Append processes

    val result = names zip nums(1)
    assert(Vector(("one", 1), ("two", 2), ("three", 3)) == result.toSource.runLog.run)
  }

  def testWriter() {
    import scalaz.stream._
    import scala.collection.mutable
    import scalaz.concurrent.Task

    var drainValues = mutable.MutableList[Int]()
    val sideEffect = sink.lift[Task, Int]((data: Int) => Task.now { drainValues += data })

    val output = Process.range(1, 3)
      // Split input into two streams, left writer, right output
      .flatMap(i => Process.emit(-\/(i)) ++ Process.emit(\/-(i)))
      // Map left side stream
      .mapW(_ + 11)
      // Sink for left side
      .drainW(sideEffect)
      // Map right side
      .map(_ + 1)
      .runLog.run

    assert(List(2, 3) == output)
    assert(List(12, 13) == drainValues.toList)
  }

  def testSink() {
    import scalaz.concurrent.Task
    import scalaz.stream.{sink, _}

    var mutable = 0
    val sideEffect = sink.lift[Task, Int]((data: Int) => Task.now { mutable = data })

    Process.emit(1).to(sideEffect).run
    assert(1 == mutable)
  }

  def testProcess() {
    import scalaz.stream._
    import scalaz.concurrent.Task

    val process = Process.range(1, 10)
    assert(Vector(1, 2, 3, 4, 5, 6, 7, 8, 9) == process.toSource.runLog.run)
    // Can be re-run
    assert(Vector(1, 2, 3, 4, 5, 6, 7, 8, 9) == process.toSource.runLog.run)

    val p1: Process[Task, Int] = Process.emit(0)
    assert(Vector(0) == p1.runLog.run)

    // Append processes
    val processChain = Process.emit(1) ++ Process.emit(2)
    assert(Vector(1, 2) == processChain.toSource.runLog.run)

    // Transformations
    val p = Process(5, 4, 3, 2, 1)
    val results = p.collect({
      case 1 => "one"
      case 2 => "two"
      case 3 => "three"
    }).filter(_.length > 3).map(_.toUpperCase)
    assert(Vector("THREE") == results.toSource.runLog.run)
  }

  def testMonad() {
    val m = Monad[List]
    assert(List(2) == m.map(List(1))(_ + 1))
    assert(List("1", "2") == m.bind(List(1, 2))({ i: Int => List(i.toString) }))
  }

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
    testHandleWith()
    testAsync()
    testAppendingProcesses()
    testComposition()
    testWriter()
    testSink()
    testProcess()
    testMonad()
    testApplicative()
    testFunctor()
    testMonoid()
  }
}
