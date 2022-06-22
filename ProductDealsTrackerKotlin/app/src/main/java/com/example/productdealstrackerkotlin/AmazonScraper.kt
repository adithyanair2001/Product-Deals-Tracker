package com.example.productdealstrackerkotlin
import org.jsoup.Jsoup

object AmazonScraper {

    fun addProduct(URL: String,budget:Int) : CardData{

        var doc = Jsoup.connect(URL).userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1").get()
//        println(doc)

        var productTitle = doc.getElementsByClass("a-size-small a-color-secondary a-text-normal").select("span").text()
        var productPrice = ""
        var offerVal = ""
        var img = ""

        if(productTitle.isEmpty())
            productTitle = doc.getElementsByClass("a-size-large product-title-word-break").text()

        println(productTitle)

        try {

            var productPriceClass = doc.getElementsByClass("a-price a-text-price a-size-medium apexPriceToPay")
            productPrice = productPriceClass.select("span.a-offscreen").text()
            offerVal = doc.getElementById("add-to-cart-button").attr("title")

            if(offerVal.equals("Add to Shopping Cart"))
                offerVal = "In Stock"
            else
                offerVal = "Currently Unavailable"

            println(offerVal)

            if (productPrice.isEmpty()) {
                productPriceClass = doc.getElementsByClass("a-price aok-align-center reinventPricePriceToPayMargin priceToPay")
                productPrice = productPriceClass.select("span.a-offscreen").text()
            }

            productPrice = productPrice.substringBefore(".")
            println(productPrice.substringAfter("₹").replace(",",""))

        }catch (e: Exception){

            productPrice = "0"
            println(productPrice)
            offerVal = "Currently Unavailable"
            println(offerVal)

        }

        try{
            img = doc.getElementById("landing-image-wrapper").select("img").attr("data-zoom-hires")

        }catch (ex : Exception){

            img = doc.getElementsByClass("imgTagWrapper").select("img").attr("data-old-hires")
        }

        println("Link: $img")
        var pt: Array<String> = productTitle.split(" ").toTypedArray()
        val p = pt.filter { pt.indexOf(it) < 6 }
        productTitle = p.joinToString(" ")

        return CardData(img, productTitle, offerVal, productPrice, URL,budget)

    }

}