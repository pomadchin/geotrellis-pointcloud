/*
 * Copyright 2020 Azavea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package geotrellis.pointcloud.raster.ept

import geotrellis.raster.{CellSize, GridExtent, MultibandTile, Raster, RasterExtent}
import geotrellis.proj4.LatLng

import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class DEMRasterSourceBench {
  val catalogPath = "../pointcloud/src/test/resources/red-rocks"

  /** This bench benchmarks the actual PDAL reads, not the RasterSource initialization time. */
  val rs   = DEMRasterSource(catalogPath)
  val gtrs = GeoTrellisDEMRasterSource(catalogPath)
  val jrs  = JavaDEMRasterSource(catalogPath)

  /**
    * jmh:run -i 3 -wi 1 -f1 -t1 .*DEMRasterSourceBench.*
    *
    * 03/06/2020 #1
    * [info] Benchmark                                                      Mode  Cnt        Score         Error  Units
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll                    avgt    3  1289156.313 ± 3962647.278  us/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll          avgt    3  2727125.104 ± 4561499.533  us/op
    * // this is a test to move PDALTrianglesRasterzer into a separate object
    * [info] DEMRasterSourceBench.TriangulationSplitDEMRasterSourceReadAll  avgt    3  1236215.715 ± 1164712.450  us/op
    *
    * 03/06/2020 #2
    * [info] Benchmark                                              Mode  Cnt        Score         Error  Units
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll            avgt    3  1196822.191 ± 1135355.015  us/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll  avgt    3  3209836.953 ± 8172952.538  us/op
    *
    * jmh:run -i 10 -wi 5 -f1 -t1 .*DEMRasterSourceBench.*
    *
    * 03/09/2020 #3
    * [info] Benchmark                                              Mode  Cnt     Score       Error  Units
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll            avgt   10   888.188 ±  139.047  ms/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll  avgt   10  2702.808 ±  188.939  ms/op
    * [info] DEMRasterSourceBench.JavaDEMRasterSourceReadAll        avgt   10  1802.555 ± 1048.380  ms/op
    *
    * 03/11/2020 #4
    * [info] Benchmark                                               Mode  Cnt       Score       Error  Units
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll             avgt   10     878.513 ±   102.391  ms/op
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll2            avgt   10    6223.862 ±   610.427  ms/op
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll4            avgt   10   11113.148 ±  5216.283  ms/op
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll8            avgt   10   10907.534 ±  2789.634  ms/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll   avgt   10    3301.495 ±   846.053  ms/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll2  avgt   10   95570.479 ± 11310.479  ms/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll4  avgt   10  179422.928 ± 22267.424  ms/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll8  avgt   10  204982.543 ± 86162.431  ms/op
    * [info] DEMRasterSourceBench.JavaDEMRasterSourceReadAll         avgt   10    1877.393 ±  1127.682  ms/op
    * [info] DEMRasterSourceBench.JavaDEMRasterSourceReadAll2        avgt   10   13023.915 ±  1695.589  ms/op
    * [info] DEMRasterSourceBench.JavaDEMRasterSourceReadAll4        avgt   10   15685.002 ±  1306.871  ms/op
    * [info] DEMRasterSourceBench.JavaDEMRasterSourceReadAll8        avgt   10   20032.444 ±  9338.665  ms/op
    *
    * 03/16/2020 reproject only
    * // here DEM uses region reproject and Java uses native
    * [info] Benchmark                                                 Mode  Cnt     Score     Error  Units
    * [info] DEMRasterSourceBench.DEMReprojectRasterSourceReadAll      avgt   10   976.935 ±  43.521  ms/op
    * [info] DEMRasterSourceBench.JavaDEMReprojectRasterSourceReadAll  avgt   10  1154.920 ± 334.336  ms/op
    *
    * 03/23/2020
    * [info] Benchmark                                                 Mode  Cnt       Score       Error  Units
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll               avgt   10     382.476 ±    44.347  ms/op
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll2              avgt   10    5573.016 ±   493.018  ms/op
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll4              avgt   10    8083.490 ±  1786.544  ms/op
    * [info] DEMRasterSourceBench.DEMRasterSourceReadAll8              avgt   10    7697.157 ±   396.745  ms/op
    * [info] DEMRasterSourceBench.DEMReprojectRasterSourceReadAll      avgt   10     527.233 ±    15.586  ms/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll     avgt   10    2319.073 ±   171.407  ms/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll2    avgt   10   88464.543 ± 10692.569  ms/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll4    avgt   10  158994.121 ± 23280.155  ms/op
    * [info] DEMRasterSourceBench.GeoTrellisDEMRasterSourceReadAll8    avgt   10  153764.608 ± 12090.790  ms/op
    * [info] DEMRasterSourceBench.JavaDEMRasterSourceReadAll           avgt   10     735.026 ±    10.594  ms/op
    * [info] DEMRasterSourceBench.JavaDEMRasterSourceReadAll2          avgt   10   12579.721 ±  2345.998  ms/op
    * [info] DEMRasterSourceBench.JavaDEMRasterSourceReadAll4          avgt   10   16152.638 ±  4003.070  ms/op
    * [info] DEMRasterSourceBench.JavaDEMRasterSourceReadAll8          avgt   10   16710.832 ±  2661.915  ms/op
    * [info] DEMRasterSourceBench.JavaDEMReprojectRasterSourceReadAll  avgt   10     531.069 ±    47.114  ms/op
    */

  @Benchmark
  def GeoTrellisDEMRasterSourceReadAll(): Option[Raster[MultibandTile]] = gtrs.read()

  @Benchmark
  def GeoTrellisDEMRasterSourceReadAll2(): Option[Raster[MultibandTile]] = {
    val RasterExtent(extent, cw, ch, _, _) = gtrs.gridExtent.toRasterExtent
    gtrs.resampleToRegion(GridExtent(extent, CellSize(cw / 2, ch / 2))).read()
  }

  @Benchmark
  def GeoTrellisDEMRasterSourceReadAll4(): Option[Raster[MultibandTile]] = {
    val RasterExtent(extent, cw, ch, _, _) = gtrs.gridExtent.toRasterExtent
    gtrs.resampleToRegion(GridExtent(extent, CellSize(cw / 4, ch / 4))).read()
  }

  @Benchmark
  def GeoTrellisDEMRasterSourceReadAll8(): Option[Raster[MultibandTile]] = {
    val RasterExtent(extent, cw, ch, _, _) = gtrs.gridExtent.toRasterExtent
    gtrs.resampleToRegion(GridExtent(extent, CellSize(cw / 8, ch / 8))).read()
  }

  @Benchmark
  def JavaDEMRasterSourceReadAll(): Option[Raster[MultibandTile]] = jrs.read()

  @Benchmark
  def JavaDEMReprojectRasterSourceReadAll(): Option[Raster[MultibandTile]] = jrs.reproject(LatLng).read()

  @Benchmark
  def JavaDEMRasterSourceReadAll2(): Option[Raster[MultibandTile]] = {
    val RasterExtent(extent, cw, ch, _, _) = jrs.gridExtent.toRasterExtent
    jrs.resampleToRegion(GridExtent(extent, CellSize(cw / 2, ch / 2))).read()
  }

  @Benchmark
  def JavaDEMRasterSourceReadAll4(): Option[Raster[MultibandTile]] = {
    val RasterExtent(extent, cw, ch, _, _) = jrs.gridExtent.toRasterExtent
    jrs.resampleToRegion(GridExtent(extent, CellSize(cw / 4, ch / 4))).read()
  }

  @Benchmark
  def JavaDEMRasterSourceReadAll8(): Option[Raster[MultibandTile]] = {
    val RasterExtent(extent, cw, ch, _, _) = jrs.gridExtent.toRasterExtent
    jrs.resampleToRegion(GridExtent(extent, CellSize(cw / 8, ch / 8))).read()
  }

  @Benchmark
  def DEMRasterSourceReadAll(): Option[Raster[MultibandTile]] = rs.read()

  @Benchmark
  def DEMReprojectRasterSourceReadAll(): Option[Raster[MultibandTile]] = jrs.reproject(LatLng).read()

  @Benchmark
  def DEMRasterSourceReadAll2(): Option[Raster[MultibandTile]] = {
    val RasterExtent(extent, cw, ch, _, _) = rs.gridExtent.toRasterExtent
    rs.resampleToRegion(GridExtent(extent, CellSize(cw / 2, ch / 2))).read()
  }

  @Benchmark
  def DEMRasterSourceReadAll4(): Option[Raster[MultibandTile]] = {
    val RasterExtent(extent, cw, ch, _, _) = rs.gridExtent.toRasterExtent
    rs.resampleToRegion(GridExtent(extent, CellSize(cw / 4, ch / 4))).read()
  }

  @Benchmark
  def DEMRasterSourceReadAll8(): Option[Raster[MultibandTile]] = {
    val RasterExtent(extent, cw, ch, _, _) = rs.gridExtent.toRasterExtent
    rs.resampleToRegion(GridExtent(extent, CellSize(cw / 8, ch / 8))).read()
  }
}
