package org.blade;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(NimTestRunner.class)
@NimTestSuite({"tests"})
public class NimTests {
  public static void main(String[] args) throws Exception {
    NimTestRunner.runInMain(NimTestSuite.class, args);
  }

  /*
   * Our "mx unittest" command looks for methods that are annotated with @Test. By just defining
   * an empty method, this class gets included and the test suite is properly executed.
   */
  @Test
  public void unittest() {
  }
}
