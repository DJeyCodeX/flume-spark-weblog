import org.apache.spark.SparkConf
import org.apache.spark.streaming.{StreamingContext,Seconds}
import org.apache.spark.streaming.flume._

object FlumeStreamingDepartmentCount {

   def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("Flume Streaming Word Count").setMaster(args(0))

    val ssc = new StreamingContext(conf, Seconds(10))

    val stream = FlumeUtils.createPollingStream(ssc, args(1), args(2).toInt)
    val messages = stream.
      map(s => new String(s.event.getBody.array()))
    val departmentMessages = messages.
      filter(msg => {
        val endPoint = msg.split(" ")(6)
        endPoint.split("/")(1) == "department"
      })
    val departments = departmentMessages.
      map(rec => {
        val endPoint = rec.split(" ")(6)
        (endPoint.split("/")(2), 1)
      })
    
    val departmentTraffic = departments.
      reduceByKey((total, value) => total + value)
    
    departmentTraffic.saveAsTextFiles("s3://upx-bd-bootcamp/Flume_Spark/departmentcount")

	val hostrequests = messages.
      map(rec => {
        val endPoint = rec.split(" ")(0)
        (endPoint, 1)
      })
    
    val requestTraffic = hostrequests.
      reduceByKey((total, value) => total + value)
    
    requestTraffic.saveAsTextFiles("s3://upx-bd-bootcamp/Flume_Spark/num_of_hostrequests")
	
	
    val responsecodes = messages.
      map(rec => {
        val endPoint = rec.split(" ")(8)
        (endPoint, 1)
      })
	
    val responsecodesTraffic = responsecodes.
      reduceByKey((total, value) => total + value)
    
    responsecodesTraffic.saveAsTextFiles("s3://upx-bd-bootcamp/Flume_Spark/responsecodes_count")

    ssc.start()
    ssc.awaitTermination()

  }
}
