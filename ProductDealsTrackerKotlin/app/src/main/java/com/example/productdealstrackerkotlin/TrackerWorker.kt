package com.example.productdealstrackerkotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.productdealstrackerkotlin.FlipKartScraper.addProduct
import org.jetbrains.anko.doAsync
import java.lang.Exception

class TrackerWorker(val context: Context,val params: WorkerParameters): Worker(context,params) {

    private val CHANNEL_ID = "channel_id_1"
    private val notificationId = 102

    override fun doWork(): Result {

        println("In Work Manager")
        val retryAttemptCount = runAttemptCount

        try{
            val url_input = inputData.getString("url").toString()
            val budget = inputData.getInt("budget",0)

            println(url_input)
            println(budget)

            doAsync {

                val card : CardData

                if(url_input.contains("flipkart"))
                    card = FlipKartScraper.addProduct(url_input,budget)
                else
                    card = AmazonScraper.addProduct(url_input,budget)

//                val card = FlipKartScraper.addProduct(url_input,budget)
                var actual_price = card.productPrice
                println(actual_price)

                var actual_price1 = card.productPrice.substringAfter("₹").replace(",","").toInt()
                println(budget.plus(actual_price1))

                if(actual_price1 <= budget){
                    println("Yes")
                    createNotificationChannel()
                    sendNotification(card.productName,card.productPrice)
                }

//                try{
//
//                    if(actual_price.toInt() <= budget.toInt()){
//
//                        print("Yes")
//                        createNotificationChannel()
//                        sendNotification(card.productName)
//                    }
//
//                }catch (ex : NumberFormatException){
//                    println("Null Input Exception")
//                }

            }

            return Result.success()

        } catch (e:Exception){
                return Result.failure()
        }

    }

    private fun createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Test Notification"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

    }

    private fun sendNotification(titleName : String, actualPrice : String){

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titleName)
            .setContentText("Offer Available At Your Budget\nCurrent Price Is " + actualPrice)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }

    }


}