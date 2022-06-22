package com.example.productdealstrackerkotlin
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.add_tracking_details_layout.view.*



class ProductListAdapter(private val productListArray: MutableList<CardData>, private val context: Context, private val db: Database) : RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder>() {

    class ProductListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        val imageView : ImageView = itemView.findViewById(R.id.productImage)
        val productName : TextView = itemView.findViewById(R.id.productName)
        val offerAvail : TextView = itemView.findViewById(R.id.offerAvailability)
        val productPrice : TextView = itemView.findViewById(R.id.productPrice)
        val budgetPrice : TextView = itemView.findViewById(R.id.budgetPrice)
        val link: Button = itemView.findViewById(R.id.link)
        val dlt: ImageView = itemView.findViewById(R.id.dlt)
        val share : ImageView = itemView.findViewById(R.id.share)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_layout_new,
            parent,false)

        return ProductListViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {

        val currentItem = productListArray[position]

//        var r = Random()
//
//        var red: Int = r.nextInt(255 - 0 + 1) + 0
//        var green: Int = r.nextInt(255 - 0 + 1) + 0
//
//        var draw = GradientDrawable()
//        draw.shape = GradientDrawable.RECTANGLE
//        draw.setColor(Color.rgb(red, green)
//
//        holder.viewInside.setBackground(draw)
//        holder.cardView.setCardBackgroundColor()
//        var card: CardView = itemView.findViewById(com.mullr.neurd.R.id.learn_def_card)

//        cardview.setBackgroundColor(Color.parseColor("#EAEDED"));

        holder.productName.text = currentItem.productName
        holder.offerAvail.text = currentItem.offerAvail
        holder.productPrice.text = currentItem.productPrice
        holder.budgetPrice.text = currentItem.budget.toString()
        Glide.with(this.context).load(currentItem.imageURL).into(holder.imageView)


        // For go to site
        holder.link.setOnClickListener {
            val viewIntent = Intent(
                "android.intent.action.VIEW",
                //Uri.parse("https://www.flipkart.com/")
                Uri.parse(currentItem.url)
            )
            context.startActivity(viewIntent)
        }

        holder.dlt.setOnClickListener{
            deleteItem(position)
            WorkManager.getInstance().cancelAllWorkByTag(currentItem.url)
            Toast.makeText(context,"Worker Cancelled", Toast.LENGTH_SHORT).show()
        }

        holder.share.setOnClickListener {

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Product On Sale! Buy Now!\n\n" + currentItem.url)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
        }
    }

    private fun deleteItem(index: Int){
        val check = db.delete(productListArray[index].url)
        productListArray.removeAt(index)
        notifyItemRemoved(index)
    }

    override fun getItemCount() = productListArray.size

}