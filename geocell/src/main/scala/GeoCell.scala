package highchair.geocell

import Resolution._

/**
 * A library for pure-functional geocell calculations, initially
 * ported from javageomodel http://code.google.com/p/javageomodel/.
 *
 * @see http://code.google.com/apis/maps/articles/geospatial.html
 */
object GeoCell {
  
  type Point = (Double, Double)
  
  val GRID_SIZE = 4
  val ALPHABET = "0123456789abcdef"
  val RADIUS = 6378135 /* meters */
  
  // Returns the geocell character in the 4x4 alphabet grid at pos. (x, y).
  def subdivChar(pos: (Int, Int)) =
    ALPHABET(
      (pos._2 & 2) << 2 |
      (pos._1 & 2) << 1 |
      (pos._2 & 1) << 1 |
      (pos._1 & 1) << 0)
  
  /** Calculate the geocell for a point at the given resolution. */
  def ?(point: Point, resolution: Resolution = Resolution.Max): String = {
    def _compute(n: Double, s: Double, e: Double, w: Double, cell: String = "", depth: Option[Resolution] = Some(Resolution.Min)): String = {
        val x = math.min((GRID_SIZE * (point._2 - w) / (e - w)),
          GRID_SIZE - 1).toInt;
        val y = math.min((GRID_SIZE * (point._1 - s) / (n - s)),
          GRID_SIZE - 1).toInt;
        val subcellLonSpan = (e - w) / GRID_SIZE
        val subcellLatSpan = (n - s) / GRID_SIZE
        
        val nextCell = cell + subdivChar((x, y))
        
        depth match {
          case Some(r) if r == resolution => nextCell
          case _ => _compute(
            (s + (subcellLatSpan * y)) + subcellLatSpan,
            s + (subcellLatSpan * y),
            (w + (subcellLonSpan * x)) + subcellLonSpan,
            w + (subcellLonSpan * x),
            nextCell,
            depth flatMap (_ next)
          )
        }
    }
    _compute(90d, -90d, 180d, -180d)
  }
  
  /** Calculate the geocells in which the point resides at every resolution. */
  def ??(point: Point) = 
    (Resolution.Min to Resolution.Max) map(?(point, _)) toList
  
  /** Calculate the distance between two points in meters. */
	def distance(p1: Point, p2: Point) = {
		val p1lat = math.toRadians(p1._1)
		val p1lon = math.toRadians(p1._2)
		val p2lat = math.toRadians(p2._1)
		val p2lon = math.toRadians(p2._2)
		RADIUS * math.acos(fixDouble(math.sin(p1lat) * math.sin(p2lat)
						+ math.cos(p1lat) * math.cos(p2lat)
						* math.cos(p2lon - p1lon)))
	}
	
	def fixDouble(d: Double): Double = d match {
	  case _ if d > 1   => 1
	  case _ if d < -1  => -1
	  case _            => d
	}
	
}