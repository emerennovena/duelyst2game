// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/vincentiasutardji/Desktop/cardgame/conf/routes
// @DATE:Tue Feb 04 21:08:40 GMT 2025


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
