/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 * 
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package au.org.ala.util

import au.org.ala.biocache.DAO
import org.wyki.cassandra.pelops.Pelops
import java.util.UUID
import au.com.bytecode.opencsv.CSVReader
import java.io.{FileReader, File}

/**
 * Reads a DwC-A and writes the data to the BioCache
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
object FasterDwCLoader {

  val hosts = Array { "localhost" }
  val keyspace = "occ"
  val columnFamily = "occ"
  val poolName = "occ-pool"
  val resourceUid = "dp20"

  def main(args: Array[String]): Unit = {

    import ReflectBean._
    println(">>> Starting DwC loader ....")
    val csvReader = new CSVReader(new FileReader("/data/biocache/ozcam/ozcam.csv"));

    var count = 0

    var startTime = System.currentTimeMillis
    var finishTime = System.currentTimeMillis

    val columnsHeaders = csvReader.readNext
    var columns:Array[String] = null
    columns = csvReader.readNext

    while (columns != null) {

      count += 1
      //the newly assigned record UUID

      val fieldTuples = {
          for {
             i <- 0 until columns.length-1
             if(columns(i).length > 0)
          } yield (columnsHeaders(i), columns(i))
      }

       //lookup the column
      val recordUuid = UUID.randomUUID.toString
      val map = Map(fieldTuples map {s => (s._1, s._2)} : _*)

      val cn = map.get("catalogNumber").getOrElse(null)
      val cc = map.get("collectionCode").getOrElse(null)
      val ic = map.get("institutionCode").getOrElse(null)
      val uniqueID = resourceUid + "|" + ic + "|" + cc + "|" + cn

      DAO.persistentManager.put(recordUuid, "occ", map)
      DAO.persistentManager.put(uniqueID, "dr", "uuid", recordUuid)
      //debug
      if (count % 100 == 0 && count > 0) {
        finishTime = System.currentTimeMillis
        println(count + ", >>  UUID: " + recordUuid + ", records per sec: " + 100 / (((finishTime - startTime).toFloat) / 1000f))
        startTime = System.currentTimeMillis
      }

      columns = csvReader.readNext
    }

    Pelops.shutdown
    println("Finished DwC loader. Records processed: " + count)
  }
}