package com.example.productdealstrackerkotlin

import org.jsoup.Jsoup

object FlipKartScraper {
    fun addProduct(URL: String,budget:Int): CardData {
        var doc = Jsoup.connect(URL).get()
        var productTitle = doc.getElementsByClass("B_NuCI").text()
        var pt: Array<String> = productTitle.split(" ").toTypedArray()
        val p = pt.filter { pt.indexOf(it) < 6 }
        productTitle = p.joinToString(" ")

            var productPrice = doc.getElementsByClass("_30jeq3 _16Jk6d").text()
            var offerVal = doc.getElementsByClass("rd9nIL").text()

            var offerAvail: String = if (offerVal == "Available offers") {
                "Offer Available"
            } else {
                "No Offers"
            }

            var imgURL = doc.getElementsByClass("q6DClP").attr("src").replace("128", "832")
            return CardData(imgURL, productTitle, offerAvail, productPrice, URL,budget)
    }
}